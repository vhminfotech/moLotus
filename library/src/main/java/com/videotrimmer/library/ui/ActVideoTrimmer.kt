package com.videotrimmer.library.ui

import android.Manifest
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.media.MediaFormat
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar
import com.linkedin.android.litr.MediaTransformer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.linkedin.android.litr.io.MediaRange
import com.linkedin.android.litr.TransformationOptions
import com.linkedin.android.litr.utils.CodecUtils
import com.linkedin.android.litr.TransformationListener
import com.linkedin.android.litr.analytics.TrackTransformationInfo
import com.videotrimmer.library.R
import com.videotrimmer.library.utils.*
import com.videotrimmer.library.utils.FileUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ActVideoTrimmer : AppCompatActivity() {
    private var playerView: PlayerView? = null
    private var videoPlayer: SimpleExoPlayer? = null
    private var imagePlayPause: ImageView? = null
    private var imageViews: Array<ImageView>? = null
    private var totalDuration: Long = 0
    private var dialog: Dialog? = null
    private var uri: Uri? = null
    private var txtStartDuration: TextView? = null
    private var txtEndDuration: TextView? = null
    private var seekbar: CrystalRangeSeekbar? = null
    private var lastMinValue: Long = 0
    private var lastMaxValue: Long = 0
    private var menuDone: MenuItem? = null
    private var seekbarController: CrystalSeekbar? = null
    private var isValidVideo = true
    private var isVideoEnded = false
    private var seekHandler: Handler? = null
    private var currentDuration: Long = 0
    private var lastClickedTime: Long = 0
    private var outputPath: String? = null
    private var lastRequestId: String? = null
    private var trimType = 0
    private var fixedGap: Long = 0
    private var minGap: Long = 0
    private var minFromGap: Long = 0
    private var maxToGap: Long = 0
    private var hidePlayerSeek = false
    private var progressView: CustomProgressView? = null
    private var fileName: String? = null
    private var realUri: Uri? = null
    private var compressOption: CompressOption? = null
    private var mediaTransformer: MediaTransformer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_video_trimmer)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        setUpToolBar(supportActionBar, getString(R.string.txt_edt_video))
        toolbar.setNavigationOnClickListener { v: View? -> finish() }
        progressView = CustomProgressView(this)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        playerView = findViewById(R.id.player_view_lib)
        imagePlayPause = findViewById(R.id.image_play_pause)
        seekbar = findViewById(R.id.range_seek_bar)
        txtStartDuration = findViewById(R.id.txt_start_duration)
        txtEndDuration = findViewById(R.id.txt_end_duration)
        seekbarController = findViewById(R.id.seekbar_controller)
        val imageOne = findViewById<ImageView>(R.id.image_one)
        val imageTwo = findViewById<ImageView>(R.id.image_two)
        val imageThree = findViewById<ImageView>(R.id.image_three)
        val imageFour = findViewById<ImageView>(R.id.image_four)
        val imageFive = findViewById<ImageView>(R.id.image_five)
        val imageSix = findViewById<ImageView>(R.id.image_six)
        val imageSeven = findViewById<ImageView>(R.id.image_seven)
        val imageEight = findViewById<ImageView>(R.id.image_eight)
        imageViews = arrayOf(
            imageOne, imageTwo, imageThree,
            imageFour, imageFive, imageSix, imageSeven, imageEight
        )
        seekHandler = Handler(mainLooper)
        mediaTransformer = MediaTransformer(this)
        initPlayer()
        if (checkStoragePermission()) setDataInView()
    }

    private fun setUpToolBar(actionBar: ActionBar?, title: String) {
        try {
            actionBar!!.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.title = title
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * SettingUp exoplayer
     */
    private fun initPlayer() {
        try {
            videoPlayer = SimpleExoPlayer.Builder(this).build()
            playerView!!.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            playerView!!.player = videoPlayer
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MOVIE)
                    .build()
                videoPlayer?.setAudioAttributes(audioAttributes, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setDataInView() {
        try {
            uri = Uri.parse(intent.getStringExtra(TrimVideo.TRIM_VIDEO_URI))
            realUri = uri
            uri = Uri.parse(FileUtils.getPath(this, uri))
            LogMessage.v("VideoUri:: $uri")
            totalDuration = TrimmerUtils.getDuration(this, uri)
            imagePlayPause!!.setOnClickListener { v: View? -> onVideoClicked() }
            Objects.requireNonNull(playerView!!.videoSurfaceView)
                ?.setOnClickListener { v: View? -> onVideoClicked() }
            initTrimData()
            buildMediaSource(uri)
            loadThumbnails()
            setUpSeekBar()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initTrimData() {
        try {
            val trimVideoOptions: TrimVideoOptions =
                intent.getParcelableExtra(TrimVideo.TRIM_VIDEO_OPTION)!!
            trimType = TrimmerUtils.getTrimType(trimVideoOptions.trimType)
            fileName = trimVideoOptions.fileName
            hidePlayerSeek = trimVideoOptions.hideSeekBar
            compressOption = trimVideoOptions.compressOption
            fixedGap = trimVideoOptions.fixedDuration
            fixedGap = if (fixedGap != 0L) fixedGap else totalDuration
            minGap = trimVideoOptions.minDuration
            minGap = if (minGap != 0L) minGap else totalDuration
            if (trimType == 3) {
                minFromGap = trimVideoOptions.minToMax[0]
                maxToGap = trimVideoOptions.minToMax[1]
                minFromGap = if (minFromGap != 0L) minFromGap else totalDuration
                maxToGap = if (maxToGap != 0L) maxToGap else totalDuration
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    private fun onVideoClicked() {
        try {
            if (isVideoEnded) {
                seekTo(lastMinValue)
                videoPlayer?.playWhenReady = true
                imagePlayPause!!.visibility = View.GONE
                return
            }
            if (currentDuration - lastMaxValue > 0) seekTo(lastMinValue)
            videoPlayer?.playWhenReady = !videoPlayer?.playWhenReady!!
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun seekTo(sec: Long) {
        if (videoPlayer != null) videoPlayer?.seekTo(sec * 1000)
    }

    private fun buildMediaSource(mUri: Uri?) {
        try {
            val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                this, getString(
                    R.string.app_name
                )
            )
            val mediaSource: MediaSource =
                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                    MediaItem.fromUri(
                        mUri!!
                    )
                )
            videoPlayer?.addMediaSource(mediaSource)
            videoPlayer?.prepare()
            videoPlayer?.playWhenReady = true
            videoPlayer?.addListener(object : Player.EventListener {
                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    imagePlayPause!!.visibility = if (playWhenReady) View.GONE else View.VISIBLE
                }

                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_ENDED -> {
                            LogMessage.v("onPlayerStateChanged: Video ended.")
                            imagePlayPause!!.visibility = View.VISIBLE
                            isVideoEnded = true
                        }
                        Player.STATE_READY -> {
                            isVideoEnded = false
                            startProgress()
                            LogMessage.v("onPlayerStateChanged: Ready to play.")
                        }
                        Player.STATE_BUFFERING -> LogMessage.v("onPlayerStateChanged: STATE_BUFFERING.")
                        Player.STATE_IDLE -> LogMessage.v("onPlayerStateChanged: STATE_IDLE.")
                        else -> {}
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*
     *  loading thumbnails
     * */
    private fun loadThumbnails() {
        try {
            val diff = totalDuration / 8
            var sec = 1
            for (img in imageViews!!) {
                val interval = diff * sec * 1000000
                val options = RequestOptions().frame(interval)
                Glide.with(this)
                    .load(realUri)
                    .apply(options)
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .into(img)
                if (sec < totalDuration) sec++
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setUpSeekBar() {
        seekbar!!.visibility = View.VISIBLE
        txtStartDuration!!.visibility = View.VISIBLE
        txtEndDuration!!.visibility = View.VISIBLE
        seekbarController!!.setMaxValue(totalDuration.toFloat()).apply()
        seekbar!!.setMaxValue(totalDuration.toFloat()).apply()
        seekbar!!.setMaxStartValue(totalDuration.toFloat()).apply()
        lastMaxValue = if (trimType == 1) {
            seekbar!!.setFixGap(fixedGap.toFloat()).apply()
            totalDuration
        } else if (trimType == 2) {
            seekbar!!.setMaxStartValue(minGap.toFloat())
            seekbar!!.setGap(minGap.toFloat()).apply()
            totalDuration
        } else if (trimType == 3) {
            seekbar!!.setMaxStartValue(maxToGap.toFloat())
            seekbar!!.setGap(minFromGap.toFloat()).apply()
            maxToGap
        } else {
            seekbar!!.setGap(2f).apply()
            totalDuration
        }
        if (hidePlayerSeek) seekbarController!!.visibility = View.GONE
        seekbar!!.setOnRangeSeekbarFinalValueListener { minValue: Number?, maxValue: Number? ->
            if (!hidePlayerSeek) seekbarController!!.visibility = View.VISIBLE
        }
        seekbar!!.setOnRangeSeekbarChangeListener { minValue: Number, maxValue: Number ->
            val minVal = minValue as Long
            val maxVal = maxValue as Long
            if (lastMinValue != minVal) {
                seekTo(minValue)
                if (!hidePlayerSeek) seekbarController!!.visibility = View.INVISIBLE
            }
            lastMinValue = minVal
            lastMaxValue = maxVal
            txtStartDuration!!.text = TrimmerUtils.formatSeconds(minVal)
            txtEndDuration!!.text = TrimmerUtils.formatSeconds(maxVal)
            /*    MediaTransformer mediaTransformer= new MediaTransformer(this);
            LogMessage.v("Estimated size "+mediaTransformer.getEstimatedTargetVideoSize(
                    realUri,getVideoFormat(),null
            ));*/if (trimType == 3) setDoneColor(minVal, maxVal)
        }
        seekbarController!!.setOnSeekbarFinalValueListener { value: Number ->
            val value1 = value as Long
            if (value1 < lastMaxValue && value1 > lastMinValue) {
                seekTo(value1)
                return@setOnSeekbarFinalValueListener
            }
            if (value1 > lastMaxValue) seekbarController?.setMinStartValue(lastMaxValue.toFloat())
                ?.apply() else if (value1 < lastMinValue) {
                seekbarController?.setMinStartValue(lastMinValue.toFloat())?.apply()
                if (videoPlayer?.playWhenReady == true) seekTo(lastMinValue)
            }
        }
    }

    /**
     * will be called whenever seekBar range changes
     * it checks max duration is exceed or not.
     * and disabling and enabling done menuItem
     *
     * @param minVal left thumb value of seekBar
     * @param maxVal right thumb value of seekBar
     */
    private fun setDoneColor(minVal: Long, maxVal: Long) {
        try {
            if (menuDone == null) return
            //changed value is less than maxDuration
            if (maxVal - minVal <= maxToGap) {
                menuDone!!.icon.colorFilter = PorterDuffColorFilter(
                    ContextCompat.getColor(this, R.color.colorWhite), PorterDuff.Mode.SRC_IN
                )
                isValidVideo = true
            } else {
                menuDone!!.icon.colorFilter = PorterDuffColorFilter(
                    ContextCompat.getColor(this, R.color.colorWhiteLt), PorterDuff.Mode.SRC_IN
                )
                isValidVideo = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PER_REQ_CODE) {
            if (isPermissionOk(*grantResults)) setDataInView() else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        videoPlayer?.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaTransformer!!.release()
        if (videoPlayer != null) videoPlayer?.release()
        if (progressView != null && progressView!!.isShowing) progressView!!.dismiss()
        stopRepeatingTask()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_done, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menuDone = menu.findItem(R.id.action_done)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_done) {
            //prevent multiple clicks
            if (SystemClock.elapsedRealtime() - lastClickedTime < 800) return true
            lastClickedTime = SystemClock.elapsedRealtime()
            trimVideo()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun trimVideo() {
        //not exceed given maxDuration if has given
        if (isValidVideo) {
            val outputFile = outputFile
            outputPath = outputFile.toString()
            Log.e("=========outputPath::", "==$outputPath")
            Log.e("=========sourcePath::", "" + uri)
            videoPlayer?.playWhenReady = false
            lastRequestId = UUID.randomUUID().toString()
            val mediaRange = MediaRange(
                TimeUnit.MILLISECONDS.toMicros(lastMinValue * 1000),
                TimeUnit.MILLISECONDS.toMicros(lastMaxValue * 1000)
            )
            try {
                val transformationOptions = TransformationOptions.Builder()
                    .setGranularity(MediaTransformer.GRANULARITY_DEFAULT)
                    .setSourceMediaRange(mediaRange)
                    .build()
                mediaTransformer!!.transform(
                    lastRequestId!!, realUri!!,
                    outputFile.path,
                    videoFormat,
                    null,
                    transformListener,
                    transformationOptions
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else Toast.makeText(
            this,
            getString(R.string.txt_smaller) + " " + TrimmerUtils.getLimitedTimeFormatted(maxToGap),
            Toast.LENGTH_SHORT
        ).show()
    }

    //No compression option
    @get:RequiresApi(api = Build.VERSION_CODES.P)
    private val videoFormat: MediaFormat?
        private get() =//No compression option
            if (compressOption == null) {
                LogMessage.v("No compression option used")
                null
            } else if (compressOption!!.bitRate <= 0 && compressOption!!.frameRate == 30 && compressOption!!.height <= 0 && compressOption!!.width <= 0) {
                LogMessage.v("Default compression option used")
                defaultVideoFormat
            } else {
                LogMessage.v("Custom compression option used")
                val widthHeight = TrimmerUtils.getVideoWidthHeight(this, uri)
                var width = compressOption!!.width
                var height = compressOption!!.height
                if (width <= 0 || height <= 0) {
                    width = widthHeight[0]
                    height = widthHeight[1]
                    if (width > 800) {
                        width = width / 2
                        height = height / 2
                    }
                }
                val mediaFormat = MediaFormat()
                val mimeType = CodecUtils.MIME_TYPE_VIDEO_AVC
                mediaFormat.setString(MediaFormat.KEY_MIME, mimeType)
                mediaFormat.setInteger(MediaFormat.KEY_WIDTH, width)
                mediaFormat.setInteger(MediaFormat.KEY_HEIGHT, height)
                mediaFormat.setInteger(
                    MediaFormat.KEY_BIT_RATE,
                    compressOption!!.bitRate
                )
                mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5)
                mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, compressOption!!.frameRate)
                mediaFormat
            }
    private val defaultVideoFormat: MediaFormat
        private get() {
            val widthHeight = TrimmerUtils.getVideoWidthHeight(this, uri)
            var width = widthHeight[0]
            var height = widthHeight[1]
            if (width > 800) {
                width = width / 2
                height = height / 2
            }
            val mediaFormat = MediaFormat()
            val mimeType = CodecUtils.MIME_TYPE_VIDEO_AVC
            mediaFormat.setString(MediaFormat.KEY_MIME, mimeType)
            mediaFormat.setInteger(MediaFormat.KEY_WIDTH, width)
            mediaFormat.setInteger(MediaFormat.KEY_HEIGHT, height)
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 2000000)
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5)
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
            return mediaFormat
        }
    private val transformListener: TransformationListener = object : TransformationListener {
        override fun onStarted(id: String) {
            LogMessage.v("onStarted")
            showProcessingDialog()
        }

        override fun onProgress(id: String, progress: Float) {
            LogMessage.v("onProgress $progress")
            val progressBar = dialog!!.findViewById<ProgressBar>(R.id.progress_trimmer)
            progressBar.progress = (progress * 100).toInt()
        }

        override fun onCompleted(
            id: String,
            trackTransformationInfos: List<TrackTransformationInfo>?
        ) {
            LogMessage.v("onCompleted")
            dialog!!.dismiss()
            val intent = Intent()
            intent.putExtra(TrimVideo.TRIMMED_VIDEO_PATH, outputPath)
            setResult(RESULT_OK, intent)
            finish()
        }

        override fun onCancelled(
            id: String,
            trackTransformationInfos: List<TrackTransformationInfo>?
        ) {
            LogMessage.v("onCancelled")
            if (dialog != null && dialog!!.isShowing) dialog!!.dismiss()
        }

        override fun onError(
            id: String,
            cause: Throwable?,
            trackTransformationInfos: List<TrackTransformationInfo>?
        ) {
            Log.e("==========", "onError $cause")
            if (dialog != null && dialog!!.isShowing) dialog!!.dismiss()
            runOnUiThread {
                Toast.makeText(
                    this@ActVideoTrimmer,
                    "Failed to trim",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    private val fileDateTime: String
        private get() {
            val calender = Calendar.getInstance()
            return calender[Calendar.YEAR].toString() + "_" +
                    calender[Calendar.MONTH] + "_" +
                    calender[Calendar.DAY_OF_MONTH] + "_" +
                    calender[Calendar.HOUR_OF_DAY] + "_" +
                    calender[Calendar.MINUTE] + "_" +
                    calender[Calendar.SECOND]
        }

    /*Environment.DIRECTORY_MOVIES +*/
    /*String path =Environment.getExternalStorageDirectory() */ /*getExternalCacheDir().getAbsolutePath()*/ /* +"/" + "/VideoCompress";
        if (!new File(path).exists()) {

            new File(path).mkdir();

        }
        String fName = "trimmed_video_";
        if (fileName != null && !fileName.isEmpty())
            fName = fileName;
        File newFile = new File(path + File.separator +
                (fName) + getFileDateTime() + "." + TrimmerUtils.getFileExtension(this, uri));*/
    private val outputFile: File
        get() {
            return File(
                Environment.getExternalStorageDirectory().absolutePath + "/" + SimpleDateFormat(
                    "yyyyMMdd_HHmmss",
                    Locale.getDefault()
                ).format(
                    Date()
                ) + ".mp4"
            )

            /*val uri = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                .let { timestamp ->
                    ContentValues().apply {
                        put(
                            MediaStore.Video.Media.TITLE,
                            timestamp
                        )
                    }
                }
                .let { cv ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentResolver.insert(
                            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                            cv
                        )
                    } else {
                        contentResolver.insert(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            cv
                        )
                    }
                }*/
            /*val newFile = File(
                Environment.getExternalStorageDirectory(),  *//*Environment.DIRECTORY_MOVIES +*//*
                "/videoCompress.mp4"
            )
            if (newFile.exists()) {
                try {
                    newFile.delete()
                    newFile.createNewFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                try {
                    newFile.createNewFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }*/


            /*String path =Environment.getExternalStorageDirectory() */ /*getExternalCacheDir().getAbsolutePath()*/ /* +"/" + "/VideoCompress";
        if (!new File(path).exists()) {

            new File(path).mkdir();

        }
        String fName = "trimmed_video_";
        if (fileName != null && !fileName.isEmpty())
            fName = fileName;
        File newFile = new File(path + File.separator +
                (fName) + getFileDateTime() + "." + TrimmerUtils.getFileExtension(this, uri));*/



             //getFile(this, uri)
        }


    @Throws(IOException::class)
    fun getFile(context: Context, uri: Uri?): File {
        val destinationFilename: File =
            File(Environment.getExternalStorageDirectory().absolutePath +/*context.getFilesDir().getPath() + */File.separatorChar + uri?.let {
                queryName(
                    context,
                    it
                )
            })
        try {
            if (uri != null) {
                context.getContentResolver().openInputStream(uri).use { ins ->
                    ins?.let {
                        createFileFromStream(
                            it,
                            destinationFilename
                        )
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("Save File", ex.message!!)
            ex.printStackTrace()
        }
        return destinationFilename
    }

    fun createFileFromStream(ins: InputStream, destination: File?) {
        try {
            FileOutputStream(destination).use { os ->
                val buffer = ByteArray(4096)
                var length: Int
                while (ins.read(buffer).also { length = it } > 0) {
                    os.write(buffer, 0, length)
                }
                os.flush()
            }
        } catch (ex: Exception) {
            Log.e("Save File", ex.message!!)
            ex.printStackTrace()
        }
    }

    private fun queryName(context: Context, uri: Uri): String {
        val returnCursor: Cursor = context.getContentResolver().query(uri, null, null, null, null)!!
        val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name: String = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }


    /*    private String getFileName() {
        String path = getExternalFilesDir("Download").getPath();
        String fName = "trimmed_video_";
        if (fileName != null && !fileName.isEmpty())
            fName = fileName;
        File newFile = new File(path + File.separator +
                (fName) +getFileDateTime()+ "." + TrimmerUtils.getFileExtension(this, uri));
        return String.valueOf(newFile);
    }*/
    private fun showProcessingDialog() {
        try {
            if (dialog == null) {
                dialog = Dialog(this)
                dialog!!.setCancelable(false)
                dialog!!.setContentView(R.layout.alert_convert)
                val txtCancel = dialog!!.findViewById<TextView>(R.id.txt_cancel)
                dialog!!.setCancelable(false)
                dialog!!.window!!
                    .setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                txtCancel.setOnClickListener { v: View? ->
                    mediaTransformer!!.cancel(
                        lastRequestId!!
                    )
                }
            }
            dialog!!.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkStoragePermission(): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                if (!Environment.isExternalStorageManager()) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse(String.format("package:%s", applicationContext?.packageName))
                    startActivity(intent)
                    finish()
                    Environment.isExternalStorageManager()

                }else{
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.MANAGE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                        0
                    )
                    checkPermission(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_MEDIA_LOCATION
                    )
                }

            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                checkPermission(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_MEDIA_LOCATION
                )
            }
            else -> checkPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun checkPermission(vararg permissions: String): Boolean {
        var allPermitted = false
        for (permission in permissions) {
            allPermitted = (ContextCompat.checkSelfPermission(this, permission)
                    == PackageManager.PERMISSION_GRANTED)
            if (!allPermitted) break
        }
        if (allPermitted) return true
        ActivityCompat.requestPermissions(
            this, permissions,
            PER_REQ_CODE
        )
        return false
    }

    private fun isPermissionOk(vararg results: Int): Boolean {
        var isAllGranted = true
        for (result in results) {
            if (PackageManager.PERMISSION_GRANTED != result) {
                isAllGranted = false
                break
            }
        }
        return isAllGranted
    }

    fun startProgress() {
        updateSeekbar.run()
    }

    fun stopRepeatingTask() {
        seekHandler!!.removeCallbacks(updateSeekbar)
    }

    var updateSeekbar: Runnable = object : Runnable {
        override fun run() {
            try {
                currentDuration = videoPlayer?.currentPosition ?: 0 / 1000
                if (!videoPlayer?.playWhenReady!!) return
                if (currentDuration <= lastMaxValue) seekbarController?.setMinStartValue(
                    currentDuration.toFloat()
                )?.apply() else videoPlayer?.playWhenReady = false
            } finally {
                seekHandler!!.postDelayed(this, 1000)
            }
        }
    }

    companion object {
        private const val PER_REQ_CODE = 115
    }
}