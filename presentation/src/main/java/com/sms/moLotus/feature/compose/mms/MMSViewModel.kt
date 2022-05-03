package com.sms.moLotus.feature.compose.mms

import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.ContactsContract
import android.telephony.SmsMessage
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.content.getSystemService
import com.sms.moLotus.PreferenceHelper
import com.sms.moLotus.R
import com.sms.moLotus.common.Navigator
import com.sms.moLotus.common.base.QkViewModel
import com.sms.moLotus.common.util.ClipboardUtils
import com.sms.moLotus.common.util.extensions.makeToast
import com.sms.moLotus.compat.SubscriptionManagerCompat
import com.sms.moLotus.compat.TelephonyCompat
import com.sms.moLotus.extensions.*
import com.sms.moLotus.feature.compose.ComposeActivity
import com.sms.moLotus.feature.compose.ComposeState
import com.sms.moLotus.interactor.*
import com.sms.moLotus.manager.ActiveConversationManager
import com.sms.moLotus.manager.PermissionManager
import com.sms.moLotus.model.*
import com.sms.moLotus.repository.ContactRepository
import com.sms.moLotus.repository.ConversationRepository
import com.sms.moLotus.repository.MessageRepository
import com.sms.moLotus.util.ActiveSubscriptionObservable
import com.sms.moLotus.util.PhoneNumberUtils
import com.sms.moLotus.util.Preferences
import com.sms.moLotus.util.tryOrNull
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.layout_send_paid_message.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class MMSViewModel @Inject constructor(
    @Named("query") private val query: String,
    @Named("threadId") private val threadId: Long,
    @Named("addresses") private val addresses: List<String>,
    @Named("text") private val sharedText: String,
    @Named("attachments") private val sharedAttachments: Attachments,
    private val contactRepo: ContactRepository,
    private val context: Context,
    private val activity: MMSActivity,
    private val activeConversationManager: ActiveConversationManager,
    private val addScheduledMessage: AddScheduledMessage,
    private val cancelMessage: CancelDelayedMessage,
    private val conversationRepo: ConversationRepository,
    private val deleteMessages: DeleteMessages,
    private val markRead: MarkRead,
    private val messageRepo: MessageRepository,
    private val navigator: Navigator,
    private val permissionManager: PermissionManager,
    private val phoneNumberUtils: PhoneNumberUtils,
    private val prefs: Preferences,
    private val retrySending: RetrySending,
    private val sendMessage: SendMessage,
    private val subscriptionManager: SubscriptionManagerCompat,
) : QkViewModel<MMSView, ComposeState>(
    ComposeState(
        editingMode = threadId == 0L && addresses.isEmpty(),
        threadId = threadId,
        query = query
    )
) {

    private val attachments: Subject<List<Attachment>> =
        BehaviorSubject.createDefault(sharedAttachments)
    private val chipsReducer: Subject<(List<Recipient>) -> List<Recipient>> =
        PublishSubject.create()
    private val conversation: Subject<Conversation> = BehaviorSubject.create()
    private val messages: Subject<List<Message>> = BehaviorSubject.create()
    private val selectedChips: Subject<List<Recipient>> = BehaviorSubject.createDefault(listOf())
    private val searchResults: Subject<List<Message>> = BehaviorSubject.create()
    private val searchSelection: Subject<Long> = BehaviorSubject.createDefault(-1)

    private var shouldShowContacts = threadId == 0L && addresses.isEmpty()

    init {
        val initialConversation = threadId.takeIf { it != 0L }
            ?.let(conversationRepo::getConversationAsync)
            ?.asObservable()
            ?: Observable.empty()

        val selectedConversation = selectedChips
            .skipWhile { it.isEmpty() }
            .map { chips -> chips.map { it.address } }
            .distinctUntilChanged()
            .doOnNext { newState { copy(loading = true) } }
            .observeOn(Schedulers.io())
            .map { addresses ->
                Pair(
                    conversationRepo.getOrCreateConversation(addresses)?.id ?: 0,
                    addresses
                )
            }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { newState { copy(loading = false) } }
            .switchMap { (threadId, addresses) ->
                // If we already have this thread in realm, or we're able to obtain it from the
                // system, just return that.
                threadId.takeIf { it > 0 }?.let {
                    return@switchMap conversationRepo.getConversationAsync(threadId).asObservable()
                }

                // Otherwise, we'll monitor the conversations until our expected conversation is created
                conversationRepo.getConversations().asObservable()
                    .filter { it.isLoaded }
                    .observeOn(Schedulers.io())
                    .map { conversationRepo.getOrCreateConversation(addresses)?.id ?: 0 }
                    .observeOn(AndroidSchedulers.mainThread())
                    .switchMap { actualThreadId ->
                        when (actualThreadId) {
                            0L -> Observable.just(Conversation(0))
                            else -> conversationRepo.getConversationAsync(actualThreadId)
                                .asObservable()
                        }
                    }
            }

        // Merges two potential conversation sources (threadId from constructor and contact selection) into a single
        // stream of conversations. If the conversation was deleted, notify the activity to shut down
        disposables += selectedConversation
            .mergeWith(initialConversation)
            .filter { conversation -> conversation.isLoaded }
            .doOnNext { conversation ->
                if (!conversation.isValid) {
                    newState { copy(hasError = true) }
                }
            }
            .filter { conversation -> conversation.isValid }
            .subscribe(conversation::onNext)

        if (addresses.isNotEmpty()) {
            selectedChips.onNext(addresses.map { address -> Recipient(address = address) })
        }

        disposables += chipsReducer
            .scan(listOf<Recipient>()) { previousState, reducer -> reducer(previousState) }
            .doOnNext { chips -> newState { copy(selectedChips = chips) } }
            .skipUntil(state.filter { state -> state.editingMode })
            .takeUntil(state.filter { state -> !state.editingMode })
            .subscribe(selectedChips::onNext)

        // When the conversation changes, mark read, and update the recipientId and the messages for the adapter
        disposables += conversation
            .distinctUntilChanged { conversation -> conversation.id }
            .observeOn(AndroidSchedulers.mainThread())
            .map { conversation ->
                val messages = messageRepo.getMessages(conversation.id)
                newState {
                    copy(
                        threadId = conversation.id,
                        messages = Pair(conversation, messages)
                    )
                }
                messages
            }
            .switchMap { messages -> messages.asObservable() }
            .subscribe(messages::onNext)

        disposables += conversation
            .map { conversation -> conversation.getTitle() }
            .distinctUntilChanged()
            .subscribe { title -> newState { copy(conversationtitle = title) } }

        /*disposables += prefs.sendAsGroup.asObservable()
            .distinctUntilChanged()
            .subscribe { enabled -> newState { copy(sendAsGroup = enabled) } }*/

        disposables += attachments
            .subscribe { attachments -> newState { copy(attachments = attachments) } }

        /*disposables += conversation
            .map { conversation -> conversation.id }
            .distinctUntilChanged()
            .withLatestFrom(state) { id, state -> messageRepo.getMessages(id, state.query) }
            .switchMap { messagesendAsGroupSwitchs -> messages.asObservable() }
            .takeUntil(state.map { it.query }.filter { it.isEmpty() })
            .filter { messages -> messages.isLoaded }
            .filter { messages -> messages.isValid }
            .subscribe(searchResults::onNext)*/
        disposables += conversation
            .map { conversation -> conversation.id }
            .distinctUntilChanged()
            .withLatestFrom(state) { id, state -> messageRepo.getMessages(id, state.query) }
            .switchMap { messages -> messages.asObservable() }
            .takeUntil(state.map { it.query }.filter { it.isEmpty() })
            .filter { messages -> messages.isLoaded }
            .filter { messages -> messages.isValid }
            .subscribe(searchResults::onNext)

        disposables += Observables.combineLatest(
            searchSelection,
            searchResults
        ) { selected, messages ->
            if (selected == -1L) {
                messages.lastOrNull()?.let { message -> searchSelection.onNext(message.id) }
            } else {
                val position = messages.indexOfFirst { it.id == selected } + 1
                newState { copy(searchSelectionPosition = position, searchResults = messages.size) }
            }
        }.subscribe()

        val latestSubId = messages
            .map { messages -> messages.lastOrNull()?.subId ?: -1 }
            .distinctUntilChanged()

        val subscriptions = ActiveSubscriptionObservable(subscriptionManager)
        disposables += Observables.combineLatest(latestSubId, subscriptions) { subId, subs ->
            val sub = if (subs.size > 1) subs.firstOrNull { it.subscriptionId == subId }
                ?: subs[0] else null
            newState { copy(subscription = sub) }
        }.subscribe()
    }

    override fun bindView(view: MMSView) {
        super.bindView(view)

        val sharing = sharedText.isNotEmpty() || sharedAttachments.isNotEmpty()
        if (shouldShowContacts) {
            shouldShowContacts = false
            view.showContacts(sharing, selectedChips.blockingFirst())
        }

        view.chipsSelectedIntent
            .withLatestFrom(selectedChips) { hashmap, chips ->
                // If there's no contacts already selected, and the user cancelled the contact
                // selection, close the activity
                if (hashmap.isEmpty() && chips.isEmpty()) {
                    newState { copy(hasError = true) }
                }
                // Filter out any numbers that are already selected
                hashmap.filter { (address) ->
                    chips.none { recipient -> phoneNumberUtils.compare(address, recipient.address) }
                }
            }
            .filter { hashmap -> hashmap.isNotEmpty() }
            .map { hashmap ->
                hashmap.map { (address, lookupKey) ->
                    conversationRepo.getRecipients()
                        .asSequence()
                        .filter { recipient -> recipient.contact?.lookupKey == lookupKey }
                        .firstOrNull { recipient ->
                            phoneNumberUtils.compare(
                                recipient.address,
                                address
                            )
                        }
                        ?: Recipient(
                            address = address,
                            contact = lookupKey?.let(contactRepo::getUnmanagedContact)
                        )
                }
            }
            .autoDisposable(view.scope())
            .subscribe { chips ->
                chipsReducer.onNext { list -> list + chips }
                view.showKeyboard()
            }


        // Set the contact suggestions list to visible when the add button is pressed
        view.optionsItemIntent
            .filter { it == R.id.add }
            .withLatestFrom(selectedChips) { _, chips ->
                view.showContacts(sharing, chips)
            }
            .autoDisposable(view.scope())
            .subscribe()

        // Update the list of selected contacts when a new contact is selected or an existing one is deselected
        view.chipDeletedIntent
            .autoDisposable(view.scope())
            .subscribe { contact ->
                chipsReducer.onNext { contacts ->
                    val result = contacts.filterNot { it == contact }
                    if (result.isEmpty()) {
                        view.showContacts(sharing, result)
                    }
                    result
                }
            }

        // When the menu is loaded, trigger a new state so that the menu options can be rendered correctly
        view.menuReadyIntent
            .autoDisposable(view.scope())
            .subscribe { newState { copy() } }

        // Open the phone dialer if the call button is clicked
        view.optionsItemIntent
            .filter { it == R.id.call }
            .withLatestFrom(conversation) { _, conversation -> conversation }
            .mapNotNull { conversation -> conversation.recipients.firstOrNull() }
            .map { recipient -> recipient.address }
            .autoDisposable(view.scope())
            .subscribe { address -> navigator.makePhoneCall(address) }

        // Open the conversation settings if info button is clicked
        view.optionsItemIntent
            .filter { it == R.id.info }
            .withLatestFrom(conversation) { _, conversation -> conversation }
            .autoDisposable(view.scope())
            .subscribe { conversation -> navigator.showConversationInfo(conversation.id) }

        // Copy the message contents
        view.optionsItemIntent
            .filter { it == R.id.copy }
            .withLatestFrom(view.messagesSelectedIntent) { _, messageIds ->
                val messages = messageIds.mapNotNull(messageRepo::getMessage).sortedBy { it.date }
                val text = when (messages.size) {
                    1 -> messages.first().getText()
                    else -> messages.foldIndexed("") { index, acc, message ->
                        when {
                            index == 0 -> message.getText()
                            messages[index - 1].compareSender(message) -> "$acc\n${message.getText()}"
                            else -> "$acc\n\n${message.getText()}"
                        }
                    }
                }

                ClipboardUtils.copy(context, text)
            }
            .autoDisposable(view.scope())
            .subscribe { view.clearSelection() }

        // Show the message details
//        view.optionsItemIntent
//                .filter { it == R.id.details }
//                .withLatestFrom(view.messagesSelectedIntent) { _, messages -> messages }
//                .mapNotNull { messages -> messages.firstOrNull().also { view.clearSelection() } }
//                .mapNotNull(messageRepo::getMessage)
//                .map(messageDetailsFormatter::format)
//                .autoDisposable(view.scope())
//                .subscribe { view.showDetails(it) }

        // Delete the messages
        view.optionsItemIntent
            .filter { it == R.id.delete }
            .filter { permissionManager.isDefaultSms().also { if (!it) view.requestDefaultSms() } }
            .withLatestFrom(
                view.messagesSelectedIntent,
                conversation
            ) { _, messages, conversation ->
                deleteMessages.execute(DeleteMessages.Params(messages, conversation.id))
            }
            .autoDisposable(view.scope())
            .subscribe { view.clearSelection() }

        // Forward the message
        view.optionsItemIntent
            .filter { it == R.id.forward }
            .withLatestFrom(view.messagesSelectedIntent) { _, messages ->

                messages?.firstOrNull()?.let { messageRepo.getMessage(it) }?.let { message ->
                    val images =
                        message.parts.filter { it.isImage() || it.isAudio() || it.isVideo() || it.isVCard() || it.isDoc() || it.isWordDoc() || it.isXLDoc() }
                            .mapNotNull { it.getUri() }
                    //navigator.showCompose("<Subject: Fwd: ${message.subject}>",message.getText(), images)
                }
            }
            .autoDisposable(view.scope())
            .subscribe { view.clearSelection() }

        // Share the message
        view.optionsItemIntent
            .filter { it == R.id.share }
            .withLatestFrom(view.messagesSelectedIntent) { _, messages ->
                messages?.firstOrNull()?.let { messageRepo.getMessage(it) }?.let { message ->
                    Log.e("========", "message.parts::: ${message.parts}")
                    if (message.parts.isNotEmpty()) {
                        message.parts.filter { it.isImage() || it.isAudio() || it.isVideo() || it.isVCard() || it.isDoc() || it.isWordDoc() || it.isXLDoc() }
                            .mapNotNull {
                                messageRepo.savePart(it.id)?.let { it1 ->
                                    navigator.shareFile(
                                        message.getText(),
                                        it1
                                    )
                                }
                            }
                    } else {
                        navigator.shareToOtherApps(message.getText())
                    }
                }
            }.doOnError { throwable -> Timber.e("ONERROR", "== ${throwable.message}") }
            .autoDisposable(view.scope())
            .subscribe { view.clearSelection() }


        // Show the previous search result
//        view.optionsItemIntent
//                .filter { it == R.id.previous }
//                .withLatestFrom(searchSelection, searchResults) { _, selection, messages ->
//                    val currentPosition = messages.indexOfFirst { it.id == selection }
//                    if (currentPosition <= 0L) messages.lastOrNull()?.id ?: -1
//                    else messages.getOrNull(currentPosition - 1)?.id ?: -1
//                }
//                .filter { id -> id != -1L }
//                .autoDisposable(view.scope())
//                .subscribe(searchSelection)

        // Show the next search result
//        view.optionsItemIntent
//                .filter { it == R.id.next }
//                .withLatestFrom(searchSelection, searchResults) { _, selection, messages ->
//                    val currentPosition = messages.indexOfFirst { it.id == selection }
//                    if (currentPosition >= messages.size - 1) messages.firstOrNull()?.id ?: -1
//                    else messages.getOrNull(currentPosition + 1)?.id ?: -1
//                }
//                .filter { id -> id != -1L }
//                .autoDisposable(view.scope())
//                .subscribe(searchSelection)

        // Clear the search
        view.optionsItemIntent
            .filter { it == R.id.clear }
            .autoDisposable(view.scope())
            .subscribe { newState { copy(query = "", searchSelectionId = -1) } }

        // Toggle the group sending mode
        /*view.sendAsGroupIntent
            .autoDisposable(view.scope())
            .subscribe { prefs.sendAsGroup.set(!prefs.sendAsGroup.get()) }*/

        // Scroll to search position
        searchSelection
            .filter { id -> id != -1L }
            .doOnNext { id -> newState { copy(searchSelectionId = id) } }
            .autoDisposable(view.scope())
            .subscribe(view::scrollToMessage)

        // Theme changes
        prefs.keyChanges
            .filter { key -> key.contains("theme") }
            .doOnNext { view.themeChanged() }
            .autoDisposable(view.scope())
            .subscribe()

        // Retry sending
        view.messageClickIntent
            .mapNotNull(messageRepo::getMessage)
            .filter { message -> message.isFailedMessage() }
            .doOnNext { message -> retrySending.execute(message.id) }
            .autoDisposable(view.scope())
            .subscribe()

        // Media attachment clicks
        view.messagePartClickIntent
            .mapNotNull(messageRepo::getPart)
            .filter { part -> part.isImage() || part.isVideo() || part.isAudio() }
            .autoDisposable(view.scope())
            .subscribe { part ->
                if (part.isAudio()) {
                    navigator.showAudioMedia(part.getUri())
                } else {
                    navigator.showMedia(part.id)
                }
            }

        // Non-media attachment clicks
        view.messagePartClickIntent
            .mapNotNull(messageRepo::getPart)
            .filter { part -> !part.isImage() && !part.isVideo() && !part.isAudio() }
            .autoDisposable(view.scope())
            .subscribe { part ->
                if (permissionManager.hasStorage()) {
                    messageRepo.savePart(part.id)?.let(navigator::viewFile)
                } else {
                    view.requestStoragePermission()
                }
            }

        // Update the State when the message selected count changes
        view.messagesSelectedIntent
            .map { selection -> selection.size }
            .autoDisposable(view.scope())
            .subscribe { messages ->
                newState {
                    copy(
                        selectedMessages = messages,
                        editingMode = false
                    )
                }
            }

        // Cancel sending a message
        view.cancelSendingIntent
            .mapNotNull(messageRepo::getMessage)
            .doOnNext { message -> view.setDraft(message.getText()) }
            .autoDisposable(view.scope())
            .subscribe { message ->
                cancelMessage.execute(CancelDelayedMessage.Params(message.id, message.threadId))
            }

        // Set the current conversation
        // Set the current conversation
        Observables
            .combineLatest(
                view.activityVisibleIntent.distinctUntilChanged(),
                conversation.mapNotNull { conversation ->
                    conversation.takeIf { it.isValid }?.id
                }.distinctUntilChanged()
            )
            { visible, threadId ->
                when (visible) {
                    true -> {
                        activeConversationManager.setActiveConversation(threadId)
                        markRead.execute(listOf(threadId))
                    }

                    false -> activeConversationManager.setActiveConversation(null)
                }
            }
            .autoDisposable(view.scope())
            .subscribe()

        // Save draft when the activity goes into the background
        view.activityVisibleIntent
            .filter { visible -> !visible }
            .withLatestFrom(conversation) { _, conversation -> conversation }
            .mapNotNull { conversation -> conversation.takeIf { it.isValid }?.id }
            .observeOn(Schedulers.io())
            .withLatestFrom(view.textChangedIntent) { threadId, draft ->
                conversationRepo.saveDraft(threadId, draft.toString())
            }
            .autoDisposable(view.scope())
            .subscribe()

        // Open the attachment options
        view.attachIntent
            .autoDisposable(view.scope())
            .subscribe { newState { copy(attaching = !attaching) } }

        // Attach a photo from camera
        view.cameraIntent
            .autoDisposable(view.scope())
            .subscribe({
                run {
                    if (permissionManager.hasStorage()) {
                        newState { copy(attaching = false) }
                        view.requestCamera()
                    } else {
                        view.requestStoragePermission()
                    }
                }
            }
            ) { throwable ->
                Timber.e(
                    "ERROR",
                    "Throwable " + throwable.message
                )
            }/*{

                if (permissionManager.hasStorage()) {
                    newState { copy(attaching = false) }
                    view.requestCamera()
                } else {
                    view.requestStoragePermission()
                }
            }
*/
        // Attach a photo from gallery
        view.galleryIntent
            .doOnNext { newState { copy(attaching = false) } }
            .autoDisposable(view.scope())
            .subscribe { view.requestGallery() }

        // Attach a video from gallery
        view.videoGalleryIntent
            .doOnNext { newState { copy(attaching = false) } }
            .autoDisposable(view.scope())
            .subscribe { view.requestVideoGallery() }

        // Take a video
        view.takeVideoIntent
            .doOnNext { newState { copy(attaching = false) } }
            .autoDisposable(view.scope())
            .subscribe { view.requestTakeVideo() }

        view.addDocumentsIntent
            .doOnNext { newState { copy(attaching = false) } }
            .autoDisposable(view.scope())
            .subscribe { view.addDocuments() }

        view.addAudioIntent
            .doOnNext { newState { copy(attaching = false) } }
            .autoDisposable(view.scope())
            .subscribe { view.addAudio() }


        // Choose a time to schedule the message
//        view.scheduleIntent
//                .doOnNext { newState { copy(attaching = false) } }
//                .withLatestFrom(billingManager.upgradeStatus) { _, upgraded -> upgraded }
//                .filter { upgraded ->
//                    upgraded.also { if (!upgraded) view.showQksmsPlusSnackbar(R.string.compose_scheduled_plus) }
//                }
//                .autoDisposable(view.scope())
//                .subscribe { view.requestDatePicker() }

//        // A photo/video was selected
        Observable.merge(
            view.attachmentSelectedIntent.map { uri -> Attachment.Image(uri) },
            view.inputContentIntent.map { inputContent -> Attachment.Image(inputContent = inputContent) })
            .withLatestFrom(attachments) { attachment, attachments -> attachments + attachment }
            .doOnNext(attachments::onNext)
            .autoDisposable(view.scope())
            .subscribe {
                ComposeActivity.recordButton?.visibility = View.GONE
                newState { copy(attaching = false) }
            }

        // Set the scheduled time
        view.scheduleSelectedIntent
            .filter { scheduled ->
                (scheduled > System.currentTimeMillis()).also { future ->
                    if (!future) context.makeToast(R.string.compose_scheduled_future)
                }
            }
            .autoDisposable(view.scope())
            .subscribe { scheduled -> newState { copy(scheduled = scheduled) } }

        // Attach a contact
        view.attachContactIntent
            .doOnNext { newState { copy(attaching = false) } }
            .autoDisposable(view.scope())
            .subscribe {
                view.requestContact()

            }

        // Contact was selected for attachment
        view.contactSelectedIntent
            .map { uri -> Attachment.Contact(getVCard(uri)!!) }
            .withLatestFrom(attachments) { attachment, attachments ->
                ComposeActivity.recordButton?.visibility = View.GONE
                attachments + attachment
            }
            .subscribeOn(Schedulers.io())
            .autoDisposable(view.scope())
            .subscribe(attachments::onNext) { error ->
                context.makeToast(R.string.compose_contact_error)
                Timber.w(error)
            }

        // Detach a photo
        view.attachmentDeletedIntent
            .withLatestFrom(attachments) { bitmap, attachments ->
                attachments.filter {
                    //    Log.e("attachments::::", "== $attachments")
                    if (attachments.size == 1) {
                        ComposeActivity.recordButton?.visibility = View.VISIBLE
                    }
                    it !== bitmap
                }
            }
            .autoDisposable(view.scope())
            .subscribe {
                attachments.onNext(it)
            }

        conversation
            .map { conversation -> conversation.draft }
            .distinctUntilChanged()
            .autoDisposable(view.scope())
            .subscribe { draft ->

                // If text was shared into the conversation, it should take priority over the
                // existing draft
                //
                // TODO: Show dialog warning user about overwriting draft
                if (sharedText.isNotBlank()) {
                    view.setDraft(sharedText)
                } else {
                    view.setDraft(draft)
                }
            }

        // Enable the send button when there is text input into the new message body or there's
        // an attachment, disable otherwise
        Observables
            .combineLatest(view.textChangedIntent, attachments) { text, attachments ->
                text.isNotBlank() || attachments.isNotEmpty()
            }
            .autoDisposable(view.scope())
            .subscribe { canSend -> newState { copy(canSend = canSend) } }

        // Show the remaining character counter when necessary
        view.textChangedIntent
            .observeOn(Schedulers.computation())
            .mapNotNull { draft ->
                tryOrNull {
                    SmsMessage.calculateLength(
                        draft,
                        prefs.unicode.get()
                    )
                }
            }
            .map { array ->
                val messages = array[0]
                val remaining = array[2]

                when {
                    messages <= 1 && remaining > 10 -> ""
                    messages <= 1 && remaining <= 10 -> "$remaining"
                    else -> "$remaining / $messages"
                }
            }
            .distinctUntilChanged()
            .autoDisposable(view.scope())
            .subscribe { remaining -> newState { copy(remaining = remaining) } }

        // Cancel the scheduled time
        view.scheduleCancelIntent
            .autoDisposable(view.scope())
            .subscribe { newState { copy(scheduled = 0) } }

        // Toggle to the next sim slot
        view.changeSimIntent
            .withLatestFrom(state) { _, state ->
                val subs = subscriptionManager.activeSubscriptionInfoList
                val subIndex =
                    subs.indexOfFirst { it.subscriptionId == state.subscription?.subscriptionId }
                val subscription = when {
                    subIndex == -1 -> null
                    subIndex < subs.size - 1 -> subs[subIndex + 1]
                    else -> subs[0]
                }

                if (subscription != null) {
                    if (Build.VERSION.SDK_INT >= 26) {
                        context.getSystemService<Vibrator>()
                            ?.vibrate(VibrationEffect.createOneShot(40, 10))
                    } else {
                        context.getSystemService<Vibrator>()?.vibrate(40)
                    }
                    context.makeToast(
                        context.getString(
                            R.string.compose_sim_changed_toast,
                            subscription.simSlotIndex + 1, subscription.displayName
                        )
                    )
                }

                newState { copy(subscription = subscription) }
            }
            .autoDisposable(view.scope())
            .subscribe()


        // Send a message when the send button is clicked, and disable editing mode if it's enabled
        view.sendIntent
            .filter { permissionManager.isDefaultSms().also { if (!it) view.requestDefaultSms() } }
            .filter { permissionManager.hasSendSms().also { if (!it) view.requestSmsPermission() } }
            .withLatestFrom(view.textChangedIntent) { _, body -> body }
            .map { body -> body.toString() }
            .withLatestFrom(
                state,
                attachments,
                conversation,
                selectedChips
            ) { body, state, attachments,
                conversation, chips ->

                if (PreferenceHelper.getPreference(context, "SendPaidMessage")) {
                    showSendPaidMessageDialog(view, state, attachments, conversation, chips, body)
                } else {
                    sendMessage(view, state, attachments, conversation, chips, body)
                }

            }.observeOn(AndroidSchedulers.mainThread()).doOnNext {

                /* Log.e("========", messageRepo.markDeliveredStatus().toString())
                 when {
                     messageRepo.markDeliveredStatus() -> {
                         Toast.makeText(context, "Message Delivered", Toast.LENGTH_SHORT).show()
                         Log.e("========", "Message Delivered!!!!!!!!!!!!")
                     }

                     else -> {
                         Toast.makeText(context, "Message Failed", Toast.LENGTH_SHORT).show()
                     }
                 }*/
            }
            .autoDisposable(view.scope())
            .subscribe()


        /*// Send a message when the send button is clicked, and disable editing mode if it's enabled
        view.sendIntent
            .filter { permissionManager.isDefaultSms().also { if (!it) view.requestDefaultSms() } }
            .filter { permissionManager.hasSendSms().also { if (!it) view.requestSmsPermission() } }
            .withLatestFrom(view.textChangedIntent) { _, body -> body }
            .map { body -> body.toString() }
            .withLatestFrom(
                state,
                attachments,
                conversation,
                selectedChips
            ) { body, state, attachments, conversation, chips ->

                val subId = state.subscription?.subscriptionId ?: -1
                val addresses = when (conversation.recipients.isNotEmpty()) {
                    true -> conversation.recipients.map { it.address }
                    false -> chips.map { chip -> chip.address }
                }
                val delay = when (prefs.sendDelay.get()) {
                    Preferences.SEND_DELAY_SHORT -> 3000
                    Preferences.SEND_DELAY_MEDIUM -> 5000
                    Preferences.SEND_DELAY_LONG -> 10000
                    else -> 0
                }
                val sendAsGroup = !state.editingMode || state.sendAsGroup

                when {
                    // Scheduling a message
                    state.scheduled != 0L -> {
                        newState { copy(scheduled = 0) }
                        val uris = attachments
                            .mapNotNull { it as? Attachment.Image }
                            .map { it.getUri() }
                            .map { it.toString() }
                        val params = AddScheduledMessage
                            .Params(state.scheduled, subId, addresses, sendAsGroup, body, uris)
                        addScheduledMessage.execute(params)
                        context.makeToast(R.string.compose_scheduled_toast)
                    }

                    // Sending a group message
                    sendAsGroup -> {
                        sendMessage.execute(
                            SendMessage
                                .Params(subId, conversation.id, addresses, body, attachments, delay)
                        )
                    }

                    // Sending a message to an existing conversation with one recipient
                    conversation.recipients.size == 1 -> {
                        val address = conversation.recipients.map { it.address }
                        sendMessage.execute(
                            SendMessage.Params(
                                subId,
                                threadId,
                                address,
                                body,
                                attachments,
                                delay
                            )
                        )
                    }


                    // Create a new conversation with one address
                    addresses.size == 1 -> {
                        sendMessage.execute(
                            SendMessage
                                .Params(subId, threadId, addresses, body, attachments, delay)
                        )
                    }

                    // Send a message to multiple addresses
                    else -> {
                        addresses.forEach { addr ->
                            val threadId = tryOrNull(false) {
                                TelephonyCompat.getOrCreateThreadId(context, addr)
                            } ?: 0
                            val address = listOf(
                                conversationRepo
                                    .getConversation(threadId)?.recipients?.firstOrNull()?.address
                                    ?: addr
                            )
                            sendMessage.execute(
                                SendMessage
                                    .Params(subId, threadId, address, body, attachments, delay)
                            )

//                                syncManager.syncMessages()

                        }
                    }
                }
                view.setDraft("")
                this.attachments.onNext(ArrayList())

                if (state.editingMode) {
                    newState { copy(editingMode = false, hasError = !sendAsGroup) }
                }


            }
            .autoDisposable(view.scope())
            .subscribe()*/

        // View moLotus+
//        view.viewQksmsPlusIntent
//                .autoDisposable(view.scope())
//                .subscribe { navigator.showQksmsPlusActivity("compose_schedule") }

        // Navigate back
        view.optionsItemIntent
            .filter { it == android.R.id.home }
            .map { }
            .mergeWith(view.backPressedIntent)
            .withLatestFrom(state) { _, state ->
                when {
                    state.selectedMessages > 0 -> view.clearSelection()
                    else -> newState { copy(hasError = true) }
                }
            }
            .autoDisposable(view.scope())
            .subscribe()

    }

    private fun sendMessage(
        view: MMSView,
        state: ComposeState,
        attachments: List<Attachment>,
        conversation: Conversation,
        chips: List<Recipient>,
        body: String
    ) {
        val subId = state.subscription?.subscriptionId ?: -1
        val addresses = when (conversation.recipients.isNotEmpty()) {
            true -> conversation.recipients.map { it.address }
            false -> chips.map { chip -> chip.address }
        }
        val delay = when (prefs.sendDelay.get()) {
            Preferences.SEND_DELAY_SHORT -> 3000
            Preferences.SEND_DELAY_MEDIUM -> 5000
            Preferences.SEND_DELAY_LONG -> 10000
            else -> 0
        }
        val sendAsGroup = !state.editingMode /*|| state.sendAsGroup*/

        when {
            // Scheduling a message
            state.scheduled != 0L -> {
                newState { copy(scheduled = 0) }
                val uris = attachments
                    .mapNotNull { it as? Attachment.Image }
                    .map { it.getUri() }
                    .map { it.toString() }
                val params = AddScheduledMessage
                    .Params(state.scheduled, subId, addresses, sendAsGroup, body, uris)
                addScheduledMessage.execute(params)
                context.makeToast(R.string.compose_scheduled_toast)
            }

            // Sending a group message
            sendAsGroup -> {
                sendMessage.execute(
                    SendMessage
                        .Params(subId, conversation.id, addresses, body, attachments, delay)
                )
            }

            // Sending a message to an existing conversation with one recipient
            conversation.recipients.size == 1 -> {
                val address = conversation.recipients.map { it.address }
                sendMessage.execute(
                    SendMessage.Params(
                        subId, threadId, address, body,
                        attachments, delay
                    )
                )
            }

            // Create a new conversation with one address
            addresses.size == 1 -> {
                sendMessage.execute(
                    SendMessage
                        .Params(subId, threadId, addresses, body, attachments, delay)
                )
            }

            // Send a message to multiple addresses
            else -> {
                addresses.forEach { addr ->
                    val threadId = tryOrNull(false) {
                        TelephonyCompat.getOrCreateThreadId(context, addr)
                    } ?: 0
                    val address = listOf(
                        conversationRepo
                            .getConversation(threadId)?.recipients?.firstOrNull()?.address ?: addr
                    )
                    sendMessage.execute(
                        SendMessage
                            .Params(subId, threadId, address, body, attachments, delay)
                    )
                }
            }
        }

        view.setDraft("")
        view.scrollToLastPosition()
        this.attachments.onNext(ArrayList())


        if (state.editingMode) {
            newState { copy(editingMode = false, hasError = !sendAsGroup) }
        }

    }


    private fun showSendPaidMessageDialog(
        view: MMSView,
        state: ComposeState,
        attachments: List<Attachment>,
        conversation: Conversation,
        chips: List<Recipient>,
        body: String
    ) {
        val dialog = Dialog(activity)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.layout_send_paid_message)

        val window: Window? = dialog.window
        val wlp: WindowManager.LayoutParams? = window?.attributes
        wlp?.gravity = Gravity.CENTER_VERTICAL
        wlp?.width = FrameLayout.LayoutParams.MATCH_PARENT
        wlp?.height = FrameLayout.LayoutParams.WRAP_CONTENT
        window?.attributes = wlp

        dialog.txtOk?.setOnClickListener {
            if (dialog.chkRemember.isChecked) {
                PreferenceHelper.setPreference(context, "SendPaidMessage", false)
            } else {
                PreferenceHelper.setPreference(context, "SendPaidMessage", true)
            }
            sendMessage(view, state, attachments, conversation, chips, body)

            dialog.dismiss()
        }

        dialog.txtCancel?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun getVCard(contactData: Uri): String? {
        val lookupKey =
            context.contentResolver.query(contactData, null, null, null, null)?.use { cursor ->
                cursor.moveToFirst()
                cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY))
            }

        val vCardUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey)
        return context.contentResolver.openAssetFileDescriptor(vCardUri, "r")
            ?.createInputStream()
            ?.readBytes()
            ?.let { bytes -> String(bytes) }
    }

}