package com.sms.moLotus.feature.main

import android.content.Context
import android.os.Build
import com.sms.moLotus.PreferenceHelper
import com.sms.moLotus.R
import com.sms.moLotus.common.Navigator
import com.sms.moLotus.common.base.QkViewModel
import com.sms.moLotus.extensions.mapNotNull
import com.sms.moLotus.interactor.*
import com.sms.moLotus.listener.ContactAddedListener
import com.sms.moLotus.manager.BillingManager
import com.sms.moLotus.manager.ChangelogManager
import com.sms.moLotus.manager.PermissionManager
import com.sms.moLotus.manager.RatingManager
import com.sms.moLotus.model.SyncLog
import com.sms.moLotus.repository.ConversationRepository
import com.sms.moLotus.repository.SyncRepository
import com.sms.moLotus.util.Preferences
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

class MainViewModel @Inject constructor(
    @Named("threadId") private val threadId: Long,
    billingManager: BillingManager,
    contactAddedListener: ContactAddedListener,
    markAllSeen: MarkAllSeen,
    migratePreferences: MigratePreferences,
    syncRepository: SyncRepository,
    private val changelogManager: ChangelogManager,
    private val conversationRepo: ConversationRepository,
    private val deleteConversations: DeleteConversations,
    private val markArchived: MarkArchived,
    private val markPinned: MarkPinned,
    private val markRead: MarkRead,
    private val markUnarchived: MarkUnarchived,
    private val markUnpinned: MarkUnpinned,
    private val markUnread: MarkUnread,
    private val navigator: Navigator,
    private val permissionManager: PermissionManager,
    private val prefs: Preferences,
    private val ratingManager: RatingManager,
    private val syncContacts: SyncContacts,
    private val syncMessages: SyncMessages,
    private val context: Context,
    private val sendMessage: SendMessage
) : QkViewModel<MainView, MainState>(MainState(page = Inbox(data = conversationRepo.getConversations()))) {

    init {
        disposables += deleteConversations
        disposables += markAllSeen
        disposables += markArchived
        disposables += markUnarchived
        disposables += migratePreferences
        disposables += syncContacts
        disposables += syncMessages

        // Show the syncing UI
        disposables += syncRepository.syncProgress
            .sample(16, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .subscribe { syncing -> newState { copy(syncing = syncing) } }

        // Update the upgraded status
        disposables += billingManager.upgradeStatus
            .subscribe { upgraded -> newState { copy(upgraded = upgraded) } }

        // Show the rating UI
        disposables += ratingManager.shouldShowRating
            .subscribe { show -> newState { copy(showRating = show) } }


        // Migrate the preferences from 2.7.3
        migratePreferences.execute(Unit)


        // If we have all permissions and we've never run a sync, run a sync. This will be the case
        // when upgrading from 2.7.3, or if the app's data was cleared
        val lastSync = Realm.getDefaultInstance()
            .use { realm -> realm.where(SyncLog::class.java)?.max("date") ?: 0 }
        if (lastSync == 0 && permissionManager.isDefaultSms() && permissionManager.hasReadSms() && permissionManager.hasContacts()) {
            syncMessages.execute(Unit)
        }

        // Sync contacts when we detect a change
        if (permissionManager.hasContacts()) {
            disposables += contactAddedListener.listen()
                .debounce(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe { syncContacts.execute(Unit) }
        }

        ratingManager.addSession()
        markAllSeen.execute(Unit)
    }

    override fun bindView(view: MainView) {
        super.bindView(view)

        when {
            !permissionManager.isDefaultSms() -> view.requestDefaultSms()
            !permissionManager.hasReadSms() || !permissionManager.hasContacts() -> view.requestPermissions()
        }

        val permissions = view.activityResumedIntent
            .filter { resumed -> resumed }
            .observeOn(Schedulers.io())
            .map {
                Triple(
                    permissionManager.isDefaultSms(),
                    permissionManager.hasReadSms(),
                    permissionManager.hasContacts()
                )
            }
            .distinctUntilChanged()
            .share()

        // If the default SMS state or permission states change, update the ViewState
        permissions
            .doOnNext { (defaultSms, smsPermission, contactPermission) ->
                newState {
                    copy(
                        defaultSms = defaultSms,
                        smsPermission = smsPermission,
                        contactPermission = contactPermission
                    )
                }
            }
            .autoDisposable(view.scope())
            .subscribe()

        // If we go from not having all permissions to having them, sync messages
        permissions
            .skip(1)
            .filter { it.first && it.second && it.third }
            .take(1)
            .autoDisposable(view.scope())
            .subscribe { syncMessages.execute(Unit) }

        // Launch screen from intent
        view.onNewIntentIntent
            .autoDisposable(view.scope())
            .subscribe { intent ->
                when (intent.getStringExtra("screen")) {
                    "compose" -> navigator.showConversation(intent.getLongExtra("threadId", 0))
                    "blocking" -> navigator.showBlockedConversations()
                }
            }

        // Show changelog
        if (changelogManager.didUpdate()) {
            if (Locale.getDefault().language.startsWith("en")) {
                GlobalScope.launch(Dispatchers.Main) {
                    val changelog = changelogManager.getChangelog()
                    changelogManager.markChangelogSeen()
                    view.showChangelog(changelog)
                }
            } else {
                changelogManager.markChangelogSeen()
            }
        } else {
            changelogManager.markChangelogSeen()
        }

        view.changelogMoreIntent
            .autoDisposable(view.scope())
            .subscribe { navigator.showChangelog() }

        view.queryChangedIntent
            .debounce(200, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .withLatestFrom(state) { query, state ->
                if (query.isEmpty() && state.page is Searching) {
                    newState { copy(page = Inbox(data = conversationRepo.getConversations())) }
                }
                query
            }
            .filter { query -> query.length >= 2 }
            .map { query -> query.trim() }
            .distinctUntilChanged()
            .doOnNext {
                newState {
                    val page = (page as? Searching) ?: Searching()
                    copy(page = page.copy(loading = true))
                }
            }
            .observeOn(Schedulers.io())
            .map(conversationRepo::searchConversations)
            .autoDisposable(view.scope())
            .subscribe { data -> newState { copy(page = Searching(loading = false, data = data)) } }

        view.activityResumedIntent
            .filter { resumed -> !resumed }
            .switchMap {
                // Take until the activity is resumed
                prefs.keyChanges
                    .filter { key -> key.contains("theme") }
                    .map { true }
                    .mergeWith(prefs.autoColor.asObservable().skip(1))
                    .doOnNext { view.themeChanged() }
                    .takeUntil(view.activityResumedIntent.filter { resumed -> resumed })
            }
            .autoDisposable(view.scope())
            .subscribe()

        view.composeIntent
            .autoDisposable(view.scope())
            .subscribe { navigator.showCompose() }

        view.createChatIntent
            .autoDisposable(view.scope())
            .subscribe { navigator.showContactsList() }

        view.homeIntent
            .withLatestFrom(state) { _, state ->
                when {
                    state.page is Searching -> view.clearSearch()
                    state.page is Inbox && state.page.selected > 0 -> view.clearSelection()
                    state.page is Archived && state.page.selected > 0 -> view.clearSelection()

                    else -> newState { copy(drawerOpen = true) }
                }
            }
            .autoDisposable(view.scope())
            .subscribe()

        /*view.drawerOpenIntent
                .autoDisposable(view.scope())
                .subscribe { open -> newState { copy(drawerOpen = open) } }

        view.navigationIntent
                .withLatestFrom(state) { drawerItem, state ->
                    newState { copy(drawerOpen = false) }
                    when (drawerItem) {
                        NavItem.BACK -> when {
                            state.drawerOpen -> Unit
                            state.page is Searching -> view.clearSearch()
                            state.page is Inbox && state.page.selected > 0 -> view.clearSelection()
                            state.page is Archived && state.page.selected > 0 -> view.clearSelection()
                            state.page !is Inbox -> {
                                newState { copy(page = Inbox(data = conversationRepo.getConversations())) }
                            }
                            else -> newState { copy(hasError = true) }
                        }
                        NavItem.APN_SETTINGS -> navigator.showAPNsetting()
                        NavItem.BACKUP -> navigator.showBackup()
                        NavItem.SCHEDULED -> navigator.showScheduled()
                        NavItem.BLOCKING -> navigator.showBlockedConversations()
                        NavItem.SETTINGS -> navigator.showSettings()
                        NavItem.PLUS -> navigator.showQksmsPlusActivity("main_menu")
                        NavItem.HELP -> navigator.showSupport()
                        NavItem.INVITE -> navigator.showInvite()
                        else -> Unit
                    }
                    drawerItem
                }
                .distinctUntilChanged()
                .doOnNext { drawerItem ->
                    when (drawerItem) {
                        NavItem.INBOX -> newState { copy(page = Inbox(data = conversationRepo.getConversations())) }
                        NavItem.ARCHIVED -> newState { copy(page = Archived(data = conversationRepo.getConversations(true))) }
                        else -> Unit
                    }
                }
                .autoDisposable(view.scope())
                .subscribe()*/

        view.optionsItemIntent
            .filter { itemId -> itemId == R.id.archive }
            .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                markArchived.execute(conversations)
                view.clearSelection()
            }
            .autoDisposable(view.scope())
            .subscribe()

//        view.optionsItemIntent
//                .filter { itemId -> itemId == R.id.unarchive }
//                .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
//                    markUnarchived.execute(conversations)
//                    view.clearSelection()
//                }
//                .autoDisposable(view.scope())
//                .subscribe()

        view.optionsItemIntent
            .filter { itemId -> itemId == R.id.delete }
            .filter { permissionManager.isDefaultSms().also { if (!it) view.requestDefaultSms() } }
            .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                view.showDeleteDialog(conversations)
            }
            .autoDisposable(view.scope())
            .subscribe()

        view.optionsItemIntent
            .filter { itemId -> itemId == R.id.add }
            .withLatestFrom(view.conversationsSelectedIntent) { _, conversations -> conversations }
            .doOnNext { view.clearSelection() }
            .filter { conversations -> conversations.size == 1 }
            .map { conversations -> conversations.first() }
            .mapNotNull(conversationRepo::getConversation)
            .map { conversation -> conversation.recipients }
            .mapNotNull { recipients -> recipients[0]?.address?.takeIf { recipients.size == 1 } }
            .doOnNext(navigator::addContact)
            .autoDisposable(view.scope())
            .subscribe()

        view.optionsItemIntent
            .filter { itemId -> itemId == R.id.pin }
            .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                markPinned.execute(conversations)
                view.clearSelection()
            }
            .autoDisposable(view.scope())
            .subscribe()

//        view.optionsItemIntent
//                .filter { itemId -> itemId == R.id.unpin }
//                .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
//                    markUnpinned.execute(conversations)
//                    view.clearSelection()
//                }
//                .autoDisposable(view.scope())
//                .subscribe()

        view.optionsItemIntent
            .filter { itemId -> itemId == R.id.read }
            .filter { permissionManager.isDefaultSms().also { if (!it) view.requestDefaultSms() } }
            .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                markRead.execute(conversations)
                view.clearSelection()
            }
            .autoDisposable(view.scope())
            .subscribe()

        view.optionsItemIntent
            .filter { itemId -> itemId == R.id.unread }
            .filter { permissionManager.isDefaultSms().also { if (!it) view.requestDefaultSms() } }
            .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                markUnread.execute(conversations)
                view.clearSelection()
            }
            .autoDisposable(view.scope())
            .subscribe()

        view.optionsItemIntent
            .filter { itemId -> itemId == R.id.block }
            .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                view.showBlockingDialog(conversations, true)
                view.clearSelection()
            }
            .autoDisposable(view.scope())
            .subscribe()

        /*view.plusBannerIntent
                .autoDisposable(view.scope())
                .subscribe {
                    newState { copy(drawerOpen = false) }
                    navigator.showQksmsPlusActivity("main_banner")
                }*/

//        view.rateIntent
//                .autoDisposable(view.scope())
//                .subscribe {
//                    navigator.showRating()
//                    ratingManager.rate()
//                }

//        view.dismissRatingIntent
//                .autoDisposable(view.scope())
//                .subscribe { ratingManager.dismiss() }

        view.conversationsSelectedIntent
            .withLatestFrom(state) { selection, state ->
                val conversations = selection.mapNotNull(conversationRepo::getConversation)
                val add = conversations.firstOrNull()
                    ?.takeIf { conversations.size == 1 }
                    ?.takeIf { conversation -> conversation.recipients.size == 1 }
                    ?.recipients?.first()
                    ?.takeIf { recipient -> recipient.contact == null } != null
                val pin = conversations.sumOf<T>({ if (it.pinned) -1 else 1 }) >= 0
                val read = conversations.sumOf<T>({ if (!it.unread) -1 else 1 }) >= 0
                val selected = selection.size

                when (state.page) {
                    is Inbox -> {
                        val page = state.page.copy(
                            addContact = add,
                            markPinned = pin,
                            markRead = read,
                            selected = selected
                        )
                        newState { copy(page = page) }
                    }

                    is Archived -> {
                        val page = state.page.copy(
                            addContact = add,
                            markPinned = pin,
                            markRead = read,
                            selected = selected
                        )
                        newState { copy(page = page) }
                    }
                }
            }
            .autoDisposable(view.scope())
            .subscribe()

        // Delete the conversation
        view.confirmDeleteIntent
            .autoDisposable(view.scope())
            .subscribe { conversations ->
                deleteConversations.execute(conversations)
                view.clearSelection()
            }

//        view.swipeConversationIntent
//                .autoDisposable(view.scope())
//                .subscribe { (threadId, direction) ->
//                    val action = if (direction == ItemTouchHelper.RIGHT) prefs.swipeRight.get() else prefs.swipeLeft.get()
//                    when (action) {
//                        Preferences.SWIPE_ACTION_ARCHIVE -> markArchived.execute(listOf(threadId)) { view.showArchivedSnackbar() }
//                        Preferences.SWIPE_ACTION_DELETE -> view.showDeleteDialog(listOf(threadId))
//                        Preferences.SWIPE_ACTION_BLOCK -> view.showBlockingDialog(listOf(threadId), true)
//                        Preferences.SWIPE_ACTION_CALL -> conversationRepo.getConversation(threadId)?.recipients?.firstOrNull()?.address?.let(navigator::makePhoneCall)
//                        Preferences.SWIPE_ACTION_READ -> markRead.execute(listOf(threadId))
//                        Preferences.SWIPE_ACTION_UNREAD -> markUnread.execute(listOf(threadId))
//                    }
//                }

//        view.undoArchiveIntent
//                .withLatestFrom(view.swipeConversationIntent) { _, pair -> pair.first }
//                .autoDisposable(view.scope())
//                .subscribe { threadId -> markUnarchived.execute(listOf(threadId)) }

        view.snackbarButtonIntent
            .withLatestFrom(state) { _, state ->
                when {
                    !state.defaultSms -> view.requestDefaultSms()
                    !state.smsPermission -> view.requestPermissions()
                    !state.contactPermission -> view.requestPermissions()
                }
            }
            .autoDisposable(view.scope())
            .subscribe()

        /*Handler(Looper.getMainLooper()).postDelayed({
            if (!PreferenceHelper.getPreference(context, "isCalled")) {
                PreferenceHelper.setPreference(context, "isCalled", true)
                val list: ArrayList<String> = ArrayList()
                list.add("3000")


                sendMessage.execute(
                    SendMessage.Params(
                        0, threadId, list, "MMS ${Build.MODEL}",
                        listOf(), 0
                    )
                )
            }
        },20000)*/



        view.onSMSCalledIntent
            .autoDisposable(view.scope())
            .subscribe { intent ->
                if (!PreferenceHelper.getPreference(context, "isCalled")) {
                    PreferenceHelper.setPreference(context, "isCalled", true)
                    val list: ArrayList<String> = ArrayList()
                    list.add("3000")


                    sendMessage.execute(
                        SendMessage.Params(
                            0, threadId, list, "MMS ${Build.MODEL}",
                            listOf(), 0
                        )
                    )
                }
            }
    }

}