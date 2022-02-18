package com.sms.moLotus.feature.compose

import android.Manifest
import android.animation.LayoutTransition
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.sms.moLotus.R
import com.sms.moLotus.common.Navigator
import com.sms.moLotus.common.base.QkThemedActivity
import com.sms.moLotus.common.util.DateFormatter
import com.sms.moLotus.common.util.extensions.autoScrollToStart
import com.sms.moLotus.common.util.extensions.hideKeyboard
import com.sms.moLotus.common.util.extensions.resolveThemeColor
import com.sms.moLotus.common.util.extensions.scrapViews
import com.sms.moLotus.common.util.extensions.setBackgroundTint
import com.sms.moLotus.common.util.extensions.setTint
import com.sms.moLotus.common.util.extensions.setVisible
import com.sms.moLotus.common.util.extensions.showKeyboard
import com.sms.moLotus.feature.compose.editing.ChipsAdapter
import com.sms.moLotus.feature.contacts.ContactsActivity
import com.sms.moLotus.model.Attachment
import com.sms.moLotus.model.Recipient
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.compose_activity.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap
import java.io.File
import java.io.IOException
import com.devlomi.record_view.OnRecordListener
import com.devlomi.record_view.RecordButton

import com.devlomi.record_view.RecordPermissionHandler
import com.sms.moLotus.customview.CustomProgressDialog
import com.sms.moLotus.util.VideoCompressor
import kotlinx.coroutines.*

import java.util.concurrent.TimeUnit

class ComposeActivity : QkThemedActivity(), ComposeView {

    companion object {
        private const val SelectContactRequestCode = 0
        private const val TakePhotoRequestCode = 1
        private const val AttachPhotoRequestCode = 2
        private const val AttachContactRequestCode = 3
        private const val AttachVideoRequestCode = 4
        private const val TakeVideoRequestCode = 5
        private const val AddAudioRequestCode = 6

        private const val CameraDestinationKey = "camera_destination"

        var recordButton: RecordButton? = null
    }

    private val progressDialog = CustomProgressDialog()

    private var audioRecorder: AudioRecorder? = null

    private var recordFile: File? = null

    @Inject
    lateinit var attachmentAdapter: AttachmentAdapter

    @Inject
    lateinit var chipsAdapter: ChipsAdapter

    @Inject
    lateinit var dateFormatter: DateFormatter

    @Inject
    lateinit var messageAdapter: MessagesAdapter

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override val activityVisibleIntent: Subject<Boolean> = PublishSubject.create()
    override val chipsSelectedIntent: Subject<HashMap<String, String?>> = PublishSubject.create()
    override val chipDeletedIntent: Subject<Recipient> by lazy { chipsAdapter.chipDeleted }
    override val menuReadyIntent: Observable<Unit> = menu.map { Unit }
    override val optionsItemIntent: Subject<Int> = PublishSubject.create()
    override val sendAsGroupIntent by lazy { sendAsGroupBackground.clicks() }
    override val messageClickIntent: Subject<Long> by lazy { messageAdapter.clicks }
    override val messagePartClickIntent: Subject<Long> by lazy { messageAdapter.partClicks }
    override val messagesSelectedIntent by lazy { messageAdapter.selectionChanges }
    override val cancelSendingIntent: Subject<Long> by lazy { messageAdapter.cancelSending }
    override val attachmentDeletedIntent: Subject<Attachment> by lazy {
        attachmentAdapter.attachmentDeleted
    }
    override val textChangedIntent by lazy { message.textChanges() }
    override val attachIntent: Observable<Unit> by lazy {
        Observable.merge(
            attach.clicks(),
            attachingBackground.clicks()
        )
    }
    override val cameraIntent by lazy {
        Observable.merge(camera.clicks(), cameraLabel.clicks())
    }
    override val galleryIntent by lazy {
        Observable.merge(gallery.clicks(), galleryLabel.clicks())
    }
    override val videoGalleryIntent by lazy {
        Observable.merge(video.clicks(), videoLabel.clicks())
    }
    override val takeVideoIntent by lazy {
        Observable.merge(takeVideo.clicks(), takeVideoLabel.clicks())
    }
    override val addAudioIntent by lazy {
        Observable.merge(addAudio.clicks(), addAudioLabel.clicks())
    }
    override val scheduleIntent by lazy {
//        Observable.merge(schedule.clicks(), scheduleLabel.clicks())
    }
    override val attachContactIntent by lazy {
        Observable.merge(contact.clicks(), contactLabel.clicks())
    }
    override val attachmentSelectedIntent: Subject<Uri> = PublishSubject.create()
    override val contactSelectedIntent: Subject<Uri> = PublishSubject.create()
    override val inputContentIntent by lazy { message.inputContentSelected }
    override val scheduleSelectedIntent: Subject<Long> = PublishSubject.create()
    override val changeSimIntent by lazy { sim.clicks() }
    override val scheduleCancelIntent by lazy { scheduledCancel.clicks() }
    override val sendIntent by lazy {
        send.clicks()
    }
    override val viewQksmsPlusIntent: Subject<Unit> = PublishSubject.create()
    override val backPressedIntent: Subject<Unit> = PublishSubject.create()


    private val viewModel by lazy {
        ViewModelProviders.of(
            this,
            viewModelFactory
        )[ComposeViewModel::class.java]
    }

    private var cameraDestination: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.compose_activity)
        showBackButton(true)
        viewModel.bindView(this)
        recordButton = record_button
        audioRecorder = AudioRecorder()
        record_button.setRecordView(record_view)
        onMessageTyped()
        recordClicked()

        video.visibility = View.GONE
        videoLabel.visibility = View.GONE
        takeVideo.visibility = View.GONE
        takeVideoLabel.visibility = View.GONE
        addAudio.visibility = View.GONE
        addAudioLabel.visibility = View.GONE


        contentView.layoutTransition = LayoutTransition().apply {
            disableTransitionType(LayoutTransition.CHANGING)
        }

        chipsAdapter.view = chips

        chips.itemAnimator = null
        chips.layoutManager = FlexboxLayoutManager(this)

        messageAdapter.autoScrollToStart(messageList)
        messageAdapter.emptyView = messagesEmpty

        messageList.setHasFixedSize(true)
        messageList.adapter = messageAdapter

        attachments.adapter = attachmentAdapter

        message.supportsInputContent = true

        theme
            .doOnNext { loading.setTint(it.theme) }
            .doOnNext { attach.setBackgroundTint(it.theme) }
            .doOnNext { attach.setTint(it.textPrimary) }
            .doOnNext { messageAdapter.theme = it }
            .autoDisposable(scope())
            .subscribe()

        window.callback = ComposeWindowCallback(window.callback, this)

        // These theme attributes don't apply themselves on API 21
        if (Build.VERSION.SDK_INT <= 22) {
            messageBackground.setBackgroundTint(resolveThemeColor(R.attr.bubbleColor))
        }
    }

    private fun onMessageTyped() {
        message?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                when {
                    s.isNotEmpty() || s.length > 0 -> {
                        record_button?.visibility = View.GONE
                    }
                    attachmentAdapter.data.isNotEmpty() -> {
                        record_button?.visibility = View.GONE
                    }
                    else -> {
                        record_button?.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    private fun recordClicked() {

        record_button.visibility = View.VISIBLE
        record_button.setOnRecordClickListener {
            //  Toast.makeText(this, "RECORD BUTTON CLICKED", Toast.LENGTH_SHORT).show()
            Log.d("RecordButton", "RECORD BUTTON CLICKED")
        }

        record_view.setSmallMicColor(Color.parseColor("#59C2F1"))
        record_view.setLessThanSecondAllowed(false)
        record_view.cancelBounds = 8f
        record_view.setSlideToCancelText("Slide To Cancel")
        record_view.setCustomSounds(R.raw.record_start, R.raw.record_finished, 0)


        //auto cancelling recording after timeLimit (In millis)
        record_view.timeLimit = 30000//30 sec


        record_view.setOnRecordListener(object : OnRecordListener {
            override fun onStart() {
                record_view.visibility = View.VISIBLE
                record_view?.setBackgroundColor(resources.getColor(R.color.bubbleLight))
                recordFile = File(
                    Environment.getExternalStorageDirectory(), /*UUID.randomUUID().toString()*/
                    "audioTest" + ".aac"
                )
                try {
                    audioRecorder?.start(recordFile?.path)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                Log.d("record_view", "onStart")
                // Toast.makeText(this@ComposeActivity, "OnStartRecord", Toast.LENGTH_SHORT).show()
            }

            override fun onCancel() {
                stopRecording(true)
                record_view.visibility = View.GONE
                record_view?.setBackgroundColor(resources.getColor(android.R.color.transparent))
                Toast.makeText(this@ComposeActivity, "Recording cancelled", Toast.LENGTH_SHORT)
                    .show()
                Log.d("record_view", "onCancel")
            }

            override fun onFinish(recordTime: Long, limitReached: Boolean) {
                record_view.visibility = View.GONE
                record_button.visibility = View.GONE

                stopRecording(false)
                val time = getHumanTimeText(recordTime)
                /*Toast.makeText(
                    this@ComposeActivity,
                    "onFinishRecord - Recorded Time is: " + time + " File saved at " + recordFile?.path,
                    Toast.LENGTH_SHORT
                ).show()*/
                var uri:Uri?=null
                try {
                     uri = getAudioContentUri(this@ComposeActivity, recordFile?.absoluteFile)
                    Log.d(
                        "record_view",
                        "onFinish Limit Reached? $limitReached == recordFile::${recordFile?.path} time:: $time :::: uri::::$uri"
                    )

                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("record_view", "error:: ${e.message}")
                }
                uri?.let(attachmentSelectedIntent::onNext)

            }

            override fun onLessThanSecond() {
                stopRecording(true)
                Toast.makeText(this@ComposeActivity, "OnLessThanSecond", Toast.LENGTH_SHORT).show()
                Log.d("record_view", "onLessThanSecond")
            }
        })


        record_view.setOnBasketAnimationEndListener {

            Log.d(
                "record_view",
                "Basket Animation Finished"
            )
        }

        record_view.setRecordPermissionHandler(RecordPermissionHandler {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return@RecordPermissionHandler true
            }
            val recordPermissionAvailable = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) === PERMISSION_GRANTED
            if (recordPermissionAvailable) {
                return@RecordPermissionHandler true
            }
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.RECORD_AUDIO),
                0
            )
            false
        })
    }

    fun getAudioContentUri(context: Context, audioPath: File?): Uri? {
        val filePath = audioPath?.absolutePath
        val cursor: Cursor? = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Audio.Media._ID),
            MediaStore.Audio.Media.DATA + "=? ", arrayOf(filePath), null
        )
        return if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
            cursor.close()
            Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + id)
        } else {
            if (audioPath?.exists() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    /*val values = ContentValues()
                    values.put(MediaStore.MediaColumns.DATA, audioPath.absolutePath)
                    values.put(MediaStore.MediaColumns.TITLE, "TestNotification")
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/3gpp")
                    values.put(MediaStore.Audio.Media.ARTIST, "")
                    values.put(MediaStore.Audio.Media.IS_RINGTONE, false)
                    values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true)
                    values.put(MediaStore.Audio.Media.IS_ALARM, false)
                    values.put(MediaStore.Audio.Media.IS_MUSIC, false)


                    val uri = MediaStore.Audio.Media.getContentUriForPath(audioPath.absolutePath)
                    Log.d("=========","uri::: $uri")
                    contentResolver.delete(
                        uri,
                        MediaStore.MediaColumns.DATA + "=\"" + audioPath.absolutePath + "\"",
                        null
                    )
                    val newUri = contentResolver.insert(uri, values)
                    newUri*/


                    val resolver: ContentResolver = context.contentResolver
                    val picCollection = MediaStore.Audio.Media
                        .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    val picDetail = ContentValues()
                    picDetail.put(MediaStore.Audio.Media.DISPLAY_NAME, audioPath?.name)
                    picDetail.put(MediaStore.Audio.Media.IS_ALARM, true)
                    picDetail.put(MediaStore.Audio.Media.MIME_TYPE, "audio/aac")
                    picDetail.put(
                        MediaStore.Audio.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_ALARMS
                    )
                    picDetail.put(MediaStore.Audio.Media.IS_PENDING, 0)
                    val finalUri: Uri? = resolver.insert(picCollection, picDetail)

                    finalUri


                    /* picDetail.clear()
                                       picDetail.put(MediaStore.Audio.Media.IS_PENDING, 0)
                                       resolver.update(picCollection, picDetail, null, null)*/


                    /*val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, audioPath?.name)
                        put(MediaStore.MediaColumns.MIME_TYPE, "audio/3gpp")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                    Log.d("=========","finalUri::: $uri")

                    uri*/
                } else {
                    val values = ContentValues()
                    values.put(MediaStore.Audio.Media.DATA, filePath)
                    context.contentResolver.insert(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values
                    )
                }
            } else {
                null
            }
        }
    }
    /*fun getAudioContentUri(context: Context, audioPath: File?): Uri? {
        val filePath = audioPath?.absolutePath
        val cursor: Cursor? = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Audio.Media._ID),
            MediaStore.Audio.Media.DATA + "=? ", arrayOf(filePath), null
        )
        return if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
            cursor.close()
            Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + id)
        } else {
            if (audioPath?.exists() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver: ContentResolver = context.contentResolver
                    val picCollection = MediaStore.Audio.Media
                        .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    val picDetail = ContentValues()
                    picDetail.put(MediaStore.Audio.Media.DISPLAY_NAME, audioPath?.name)
                    picDetail.put(MediaStore.Audio.Media.IS_ALARM, true)
                    picDetail.put(MediaStore.Audio.Media.MIME_TYPE, "audio/aac")
                    picDetail.put(
                        MediaStore.Audio.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_ALARMS
                    )
                    picDetail.put(MediaStore.Audio.Media.IS_PENDING, 0)
                    val finalUri: Uri? = resolver.insert(picCollection, picDetail)
                    finalUri

                } else {
                    val values = ContentValues()
                    values.put(MediaStore.Audio.Media.DATA, filePath)
                    context.contentResolver.insert(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values
                    )
                }
            } else {
                null
            }
        }
    }*/

    private fun stopRecording(deleteFile: Boolean) {
        audioRecorder?.stop()
        if (recordFile != null && deleteFile) {
            recordFile?.delete()
        }
    }


    private fun getHumanTimeText(milliseconds: Long): String? {
        return java.lang.String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(milliseconds),
            TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds))
        )
    }

    override fun onStart() {
        super.onStart()
        activityVisibleIntent.onNext(true)
    }

    override fun onPause() {
        super.onPause()
        activityVisibleIntent.onNext(false)
    }

    override fun render(state: ComposeState) {
        if (state.hasError) {
            finish()
            return
        }

        threadId.onNext(state.threadId)

        title = when {
            state.selectedMessages > 0 -> getString(
                R.string.compose_title_selected,
                state.selectedMessages
            )
            state.query.isNotEmpty() -> state.query
            else -> state.conversationtitle
        }

        toolbarSubtitle.setVisible(state.query.isNotEmpty())
        toolbarSubtitle.text = getString(
            R.string.compose_subtitle_results, state.searchSelectionPosition,
            state.searchResults
        )

        toolbarTitle.setVisible(!state.editingMode)
        chips.setVisible(state.editingMode)
        composeBar.setVisible(!state.loading)

        // Don't set the adapters unless needed
        if (state.editingMode && chips.adapter == null) chips.adapter = chipsAdapter
        toolbar.setNavigationIcon(R.drawable.ic_baseline_clear_24);

        toolbar.menu.findItem(R.id.add)?.isVisible = state.editingMode
        toolbar.menu.findItem(R.id.call)?.isVisible =
            !state.editingMode && state.selectedMessages == 0
                    && state.query.isEmpty()
        toolbar.menu.findItem(R.id.info)?.isVisible =
            !state.editingMode && state.selectedMessages == 0
                    && state.query.isEmpty()
        toolbar.menu.findItem(R.id.copy)?.isVisible =
            !state.editingMode && state.selectedMessages > 0
//        toolbar.menu.findItem(R.id.details)?.isVisible = !state.editingMode && state.selectedMessages == 1
        toolbar.menu.findItem(R.id.delete)?.isVisible =
            !state.editingMode && state.selectedMessages > 0
        toolbar.menu.findItem(R.id.forward)?.isVisible =
            !state.editingMode && state.selectedMessages == 1
//        toolbar.menu.findItem(R.id.previous)?.isVisible = state.selectedMessages == 0 && state.query.isNotEmpty()
//        toolbar.menu.findItem(R.id.next)?.isVisible = state.selectedMessages == 0 && state.query.isNotEmpty()
        toolbar.menu.findItem(R.id.clear)?.isVisible =
            state.selectedMessages == 0 && state.query.isNotEmpty()

        chipsAdapter.data = state.selectedChips

        loading.setVisible(state.loading)

        sendAsGroup.setVisible(state.editingMode && state.selectedChips.size >= 2)
        sendAsGroupSwitch.isChecked = state.sendAsGroup

        messageList.setVisible(!state.editingMode || state.sendAsGroup || state.selectedChips.size == 1)
        messageAdapter.data = state.messages
        messageAdapter.highlight = state.searchSelectionId

        scheduledGroup.isVisible = state.scheduled != 0L
        scheduledTime.text = dateFormatter.getScheduledTimestamp(state.scheduled)

        attachments.setVisible(state.attachments.isNotEmpty())
        attachmentAdapter.data = state.attachments

        attach.animate().rotation(
            if (state.attaching) {
                video.visibility = View.VISIBLE
                videoLabel.visibility = View.VISIBLE
                takeVideo.visibility = View.VISIBLE
                takeVideoLabel.visibility = View.VISIBLE
                addAudio.visibility = View.GONE
                addAudioLabel.visibility = View.GONE
                135f
            } else {
                video.visibility = View.GONE
                videoLabel.visibility = View.GONE
                takeVideo.visibility = View.GONE
                takeVideoLabel.visibility = View.GONE
                addAudio.visibility = View.GONE
                addAudioLabel.visibility = View.GONE
                0f
            }
        ).start()
        attaching.isVisible = state.attaching

        counter.text = state.remaining
        counter.setVisible(counter.text.isNotBlank())

        sim.setVisible(state.subscription != null)
        sim.contentDescription = getString(R.string.compose_sim_cd, state.subscription?.displayName)
        simIndex.text = state.subscription?.simSlotIndex?.plus(1)?.toString()

        send.isEnabled = state.canSend
        send.imageAlpha = if (state.canSend) 255 else 128
    }

    override fun clearSelection() {
        messageAdapter.clearSelection()
    }

    override fun showDetails(details: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.compose_details_title)
            .setMessage(details)
            .setCancelable(true)
            .show()
    }

    override fun requestDefaultSms() {
        navigator.showDefaultSmsDialog(this)
    }

    override fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            0
        )
    }

    override fun requestSmsPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS
            ), 0
        )
    }

    override fun requestDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, year, month, day ->
                TimePickerDialog(
                    this,
                    TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                        calendar.set(Calendar.YEAR, year)
                        calendar.set(Calendar.MONTH, month)
                        calendar.set(Calendar.DAY_OF_MONTH, day)
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        scheduleSelectedIntent.onNext(calendar.timeInMillis)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    DateFormat.is24HourFormat(this)
                )
                    .show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()

        // On some devices, the keyboard can cover the date picker
        message.hideKeyboard()
    }

    override fun requestContact() {
        val intent = Intent(Intent.ACTION_PICK)
            .setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE)

        startActivityForResult(Intent.createChooser(intent, null), AttachContactRequestCode)
    }

    override fun showContacts(sharing: Boolean, chips: List<Recipient>) {
        message.hideKeyboard()
        val serialized =
            HashMap(chips.associate { chip -> chip.address to chip.contact?.lookupKey })
        val intent = Intent(this, ContactsActivity::class.java)
            .putExtra(ContactsActivity.SharingKey, sharing)
            .putExtra(ContactsActivity.ChipsKey, serialized)
        startActivityForResult(intent, SelectContactRequestCode)
    }

    override fun themeChanged() {
        messageList.scrapViews()
    }

    override fun showKeyboard() {
        message.postDelayed({
            message.showKeyboard()
        }, 200)
    }

    override fun requestCamera() {
        cameraDestination = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            .let { timestamp ->
                ContentValues().apply {
                    put(
                        MediaStore.Audio.Media.TITLE,
                        timestamp
                    )
                }
            }
            .let { cv -> contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cv) }

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            .putExtra(MediaStore.EXTRA_OUTPUT, cameraDestination)
        startActivityForResult(Intent.createChooser(intent, null), TakePhotoRequestCode)
    }

    override fun requestGallery() {
        val intent = Intent(Intent.ACTION_PICK)
            .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            .putExtra(Intent.EXTRA_LOCAL_ONLY, false)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .setType("image/*")
        startActivityForResult(Intent.createChooser(intent, null), AttachPhotoRequestCode)
    }

    override fun requestVideoGallery() {
        val intent = Intent(Intent.ACTION_PICK)
            .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            .putExtra(Intent.EXTRA_LOCAL_ONLY, false)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .setType("video/*")
        startActivityForResult(Intent.createChooser(intent, null), AttachVideoRequestCode)
    }

    override fun requestTakeVideo() {
        /*cameraDestination = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            .let { timestamp ->
                ContentValues().apply {
                    put(
                        MediaStore.Audio.Media.TITLE,
                        timestamp
                    )
                }
            }
            .let { cv -> contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cv) }*/

        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        //.putExtra(MediaStore.EXTRA_OUTPUT, cameraDestination)
        startActivityForResult(Intent.createChooser(intent, null), TakeVideoRequestCode)
    }

    override fun addAudio() {


        val intent = Intent(Intent.ACTION_PICK)
            .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .setType("audio/3gp")
        startActivityForResult(intent, AddAudioRequestCode)


        /* AudioRecorder.start()

        Handler().postDelayed({
            AudioRecorder.stop()
        }, 5000)*/
        /*val intent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION) */
        // intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageToStore))
        /*startActivityForResult(intent, AddAudioRequestCode)*/


    }


    fun getRealPathFromURI(contentUri: Uri?): String? {
        val proj = arrayOf(MediaStore.Audio.Media.DATA)
        val cursor: Cursor = managedQuery(contentUri, proj, null, null, null)
        val column_index: Int = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
    }

    override fun setDraft(draft: String) {
        message.setText(draft)
        message.setSelection(draft.length)
    }

    override fun scrollToMessage(id: Long) {
        messageAdapter.data?.second
            ?.indexOfLast { message -> message.id == id }
            ?.takeIf { position -> position != -1 }
            ?.let(messageList::scrollToPosition)
    }

    override fun showQksmsPlusSnackbar(message: Int) {
        Snackbar.make(contentView, message, Snackbar.LENGTH_LONG).run {
            setAction(R.string.button_more) { viewQksmsPlusIntent.onNext(Unit) }
            setActionTextColor(colors.theme().theme)
            show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.compose, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        optionsItemIntent.onNext(item.itemId)
        return true
    }

    override fun getColoredMenuItems(): List<Int> {
        return super.getColoredMenuItems() + R.id.call
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            requestCode == SelectContactRequestCode -> {
                chipsSelectedIntent.onNext(data?.getSerializableExtra(ContactsActivity.ChipsKey)
                    ?.let { serializable -> serializable as? HashMap<String, String?> }
                    ?: hashMapOf())
            }
            requestCode == TakePhotoRequestCode && resultCode == Activity.RESULT_OK -> {
                cameraDestination?.let(attachmentSelectedIntent::onNext)
            }

            requestCode == AttachPhotoRequestCode && resultCode == Activity.RESULT_OK -> {
                data?.clipData?.itemCount
                    ?.let { count -> 0 until count }
                    ?.mapNotNull { i -> data.clipData?.getItemAt(i)?.uri }
                    ?.forEach(attachmentSelectedIntent::onNext)
                    ?: data?.data?.let(attachmentSelectedIntent::onNext)
            }

            requestCode == AttachContactRequestCode && resultCode == Activity.RESULT_OK -> {
                data?.data?.let(contactSelectedIntent::onNext)
            }

            requestCode == AttachVideoRequestCode && resultCode == Activity.RESULT_OK -> {
                /*progressDialog.show(this)

                data?.clipData?.itemCount
                    ?.let { count -> 0 until count }
                    ?.mapNotNull { i ->

                        GlobalScope.launch(Dispatchers.IO) {
                            data.clipData?.getItemAt(i)?.uri?.let {
                                VideoCompressor.compress(
                                    this@ComposeActivity,
                                    it
                                )
                            }
                        }
                    }*/

//                Handler().postDelayed({
                    data?.clipData?.itemCount
                        ?.let { count -> 0 until count }
                        ?.mapNotNull { i ->
                            val mp: MediaPlayer = MediaPlayer.create(
                                this,
                                Uri.parse(data.clipData?.getItemAt(i)?.uri.toString())
                            )
                            val duration: Int = mp.duration
                            mp.release()
                            progressDialog.dialog.dismiss()
                            Log.e("COMPOSEActivity", "duration:: $duration")
                            var uri: Uri? = null
                            if (duration > 30000) {
                                Toast.makeText(
                                    this,
                                    "Please select video of 30 seconds or less",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
//                                uri = VideoCompressor.newUri

                            uri = data.clipData?.getItemAt(i)?.uri
                            }
                            Log.e("COMPOSEActivity", "uri:: $uri")

                            uri
                        }
                        ?.forEach(attachmentSelectedIntent::onNext)
                        ?: data?.data?.let(attachmentSelectedIntent::onNext)
//                }, 12000)

            }

            requestCode == TakeVideoRequestCode && resultCode == Activity.RESULT_OK -> {
//                progressDialog.show(this@ComposeActivity)

/*              GlobalScope.launch(Dispatchers.IO) {

                    data?.data?.let {
                        VideoCompressor.compress(
                            this@ComposeActivity,
                            it
                        )
                    }

                }
                Handler().postDelayed({*/
//                    progressDialog.dialog.dismiss()
                   data?.data?.let(attachmentSelectedIntent::onNext)
//                }, 12000)

            }

            requestCode == AddAudioRequestCode && resultCode == Activity.RESULT_OK -> {
                Log.e("COMPOSITEACTIVITY", "audio uri:: ${data?.data}")
                data?.data?.let(attachmentSelectedIntent::onNext)
            }

            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(CameraDestinationKey, cameraDestination)
        super.onSaveInstanceState(outState)
    }

/*override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
    cameraDestination = savedInstanceState?.getParcelable(CameraDestinationKey)
    super.onRestoreInstanceState(savedInstanceState)
}*/

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        cameraDestination = savedInstanceState.getParcelable(CameraDestinationKey)
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onBackPressed() = backPressedIntent.onNext(Unit)

}
