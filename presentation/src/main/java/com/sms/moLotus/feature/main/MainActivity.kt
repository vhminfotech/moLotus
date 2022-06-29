package com.sms.moLotus.feature.main

//import com.sms.moLotus.feature.conversations.ConversationItemTouchCallback
//import kotlinx.android.synthetic.main.drawer_view.*
import android.Manifest
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.sms.moLotus.GetThreadListQuery
import com.sms.moLotus.PreferenceHelper
import com.sms.moLotus.R
import com.sms.moLotus.common.Navigator
import com.sms.moLotus.common.QKApplication
import com.sms.moLotus.common.base.QkThemedActivity
import com.sms.moLotus.common.util.extensions.*
import com.sms.moLotus.extension.toast
import com.sms.moLotus.feature.Constants
import com.sms.moLotus.feature.blocking.BlockingDialog
import com.sms.moLotus.feature.changelog.ChangelogDialog
import com.sms.moLotus.feature.chat.LogHelper
import com.sms.moLotus.feature.chat.adapter.ChatListAdapter
import com.sms.moLotus.feature.chat.listener.OnChatClickListener
import com.sms.moLotus.feature.chat.listener.OnItemClickListener
import com.sms.moLotus.feature.chat.ui.ChatActivity
import com.sms.moLotus.feature.conversations.ConversationsAdapter
import com.sms.moLotus.feature.intro.IntroActivity
import com.sms.moLotus.feature.retrofit.RetrofitService
import com.sms.moLotus.manager.ChangelogManager
import com.sms.moLotus.repository.SyncRepository
import com.sms.moLotus.viewmodel.ChatViewModel
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.dialog_delete_message.*
import kotlinx.android.synthetic.main.fragment_mchat.*
import kotlinx.android.synthetic.main.layout_more_options.view.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.main_permission_hint.*
import kotlinx.android.synthetic.main.main_syncing.*
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.M)
class MainActivity : QkThemedActivity(), MainView, OnItemClickListener, OnChatClickListener {

    lateinit var chatViewModel: ChatViewModel

    @Inject
    lateinit var blockingDialog: BlockingDialog

    @Inject
    lateinit var disposables: CompositeDisposable

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var conversationsAdapter: ConversationsAdapter

    @Inject
    lateinit var drawerBadgesExperiment: DrawerBadgesExperiment

    @Inject
    lateinit var searchAdapter: SearchAdapter

    //    @Inject lateinit var itemTouchCallback: ConversationItemTouchCallback
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override val onNewIntentIntent: Subject<Intent> = PublishSubject.create()
    override val activityResumedIntent: Subject<Boolean> = PublishSubject.create()
    override val queryChangedIntent by lazy { toolbarSearch.textChanges() }
    override val composeIntent by lazy {
        compose.clicks()
    }

    override val createChatIntent by lazy {
        createChat.clicks()
    }

    /*override val drawerOpenIntent: Observable<Boolean> by lazy {
        drawerLayout
                .drawerOpen(Gravity.START)
                .doOnNext { dismissKeyboard() }
    }*/
    override val homeIntent: Subject<Unit> = PublishSubject.create()

    /*override val navigationIntent: Observable<NavItem> by lazy {
        Observable.merge(listOf(
                backPressedSubject,
                inbox.clicks().map { NavItem.INBOX },
                apn_details.clicks().map { NavItem.APN_SETTINGS },
                archived.clicks().map { NavItem.ARCHIVED },
                backup.clicks().map { NavItem.BACKUP },
                scheduled.clicks().map { NavItem.SCHEDULED },
                blocking.clicks().map { NavItem.BLOCKING },
                settings.clicks().map { NavItem.SETTINGS },
//                plus.clicks().map { NavItem.PLUS },
                help.clicks().map { NavItem.HELP },
                invite.clicks().map { NavItem.INVITE }))
    }*/
    override val optionsItemIntent: Subject<Int> = PublishSubject.create()

    /*override val plusBannerIntent by lazy { plusBanner.clicks() }*/
    override val dismissRatingIntent by lazy {
//        rateDismiss.clicks()
    }
    override val rateIntent by lazy {
//        rateOkay.clicks()
    }
    override val conversationsSelectedIntent by lazy { conversationsAdapter.selectionChanges }
    override val confirmDeleteIntent: Subject<List<Long>> = PublishSubject.create()
    override val swipeConversationIntent by lazy {
//        itemTouchCallback.swipes
    }
    override val changelogMoreIntent by lazy { changelogDialog.moreClicks }
    override val undoArchiveIntent: Subject<Unit> = PublishSubject.create()
    override val snackbarButtonIntent: Subject<Unit> = PublishSubject.create()

    private val viewModel by lazy {
        ViewModelProviders.of(
            this,
            viewModelFactory
        )[MainViewModel::class.java]
    }

    private val progressAnimator by lazy { ObjectAnimator.ofInt(syncingProgress, "progress", 0, 0) }
    private val changelogDialog by lazy { ChangelogDialog(this) }
    private val snackbar by lazy { findViewById<View>(R.id.snackbar) }
    private val syncing by lazy { findViewById<View>(R.id.syncing) }
    private val backPressedSubject: Subject<NavItem> = PublishSubject.create()
    lateinit var mainViewModel: com.sms.moLotus.feature.retrofit.MainViewModel
    private val retrofitService = RetrofitService.getInstance()
    var tabAppears = false
    var chatClicked = false
    private var chatListAdapter: ChatListAdapter? = null

    companion object {
        var toolbarVisible: androidx.appcompat.widget.Toolbar? = null
    }

    private var mSocket: Socket? = null
    var currentUserId: String? = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        toolbarVisible = toolbar
        mainViewModel =
            ViewModelProvider(this/*, MyViewModelFactory(MainRepository(retrofitService))*/).get(
                com.sms.moLotus.feature.retrofit.MainViewModel::class.java
            )
        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        imgSearch.setOnClickListener {
            relSearch?.visibility = View.VISIBLE
        }

        tabLayout?.newTab()?.setText("MESSAGES")?.let { tabLayout?.addTab(it) }
        tabLayout?.newTab()?.setText("CHATS")?.let { tabLayout?.addTab(it) }
        tabLayout?.tabGravity = TabLayout.GRAVITY_FILL


        /* val adapter = MyAdapter(this, supportFragmentManager, tabLayout.tabCount)
         viewPager?.adapter = adapter

         viewPager?.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))*/

        tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                //  viewPager?.currentItem = tab.position
                val position = tab.position
                if (position == 1) {
                    recyclerView?.visibility = View.GONE
                    empty?.visibility = View.GONE
                    compose?.visibility = View.GONE
                    createChat?.visibility = View.VISIBLE
                    getChatList()
                    chatClicked = true
                    toolbarVisible?.visibility = View.GONE
                } else {
                    txtNoChat?.visibility = View.GONE
                    recyclerView?.visibility = View.VISIBLE
                    rvChatRecyclerView?.visibility = View.GONE
                    compose?.visibility = View.VISIBLE
                    createChat?.visibility = View.GONE
                    tabAppears = true
                    chatClicked = false

                    toolbarVisible?.visibility = View.GONE
                    if (conversationsAdapter.itemCount == 0) {
                        empty?.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })


        imgClose.setOnClickListener {
            dismissKeyboard()
            toolbarSearch?.setText("")
            relSearch?.visibility = View.GONE
        }

        imgMenu.setOnClickListener { v ->
            val inflater =
                applicationContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.layout_more_options, null)
            val myPopupWindow = PopupWindow(
                view,
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                true
            )
            view.txtSettings?.setOnClickListener {
                navigator.showAppsetting()
                myPopupWindow.dismiss()
            }
            view.txtAPNSettings?.setOnClickListener {
                navigator.showAPNsetting()
                myPopupWindow.dismiss()
            }
            view.txtInbox?.setOnClickListener {
                myPopupWindow.dismiss()
            }
            myPopupWindow.showAsDropDown(v, 0, -170)
        }

        val settings = getSharedPreferences("appInfo", 0)
        val firstTime = settings.getBoolean("first_time", true)

        if (firstTime) {
            val intent = Intent(this, IntroActivity::class.java);
            startActivity(intent)
        }

        viewModel.bindView(this)
        onNewIntentIntent.onNext(intent)

        (snackbar as? ViewStub)?.setOnInflateListener { _, _ ->
            snackbarButton.clicks()
                .autoDisposable(scope(Lifecycle.Event.ON_DESTROY))
                .subscribe(snackbarButtonIntent)
        }

        (syncing as? ViewStub)?.setOnInflateListener { _, _ ->
            syncingProgress?.progressTintList = ColorStateList.valueOf(theme.blockingFirst().theme)
            syncingProgress?.indeterminateTintList =
                ColorStateList.valueOf(theme.blockingFirst().theme)
        }

        //toggle.syncState()
        toolbar.setNavigationOnClickListener {
            dismissKeyboard()
            homeIntent.onNext(Unit)
        }

        conversationsAdapter.autoScrollToStart(recyclerView)

        // Don't allow clicks to pass through the drawer layout
        //drawer.clicks().autoDisposable(scope()).subscribe()

        // Set the theme color tint to the recyclerView, progressbar, and FAB
        theme
            .autoDisposable(scope())
            .subscribe { theme ->
                // Set the color for the drawer icons
                val states = arrayOf(
                    intArrayOf(android.R.attr.state_activated),
                    intArrayOf(-android.R.attr.state_activated)
                )

                resolveThemeColor(android.R.attr.textColorSecondary)
                    .let { textSecondary ->
                        ColorStateList(
                            states,
                            intArrayOf(theme.theme, textSecondary)
                        )
                    }
                    .let { tintList ->

                    }
                syncingProgress?.progressTintList = ColorStateList.valueOf(theme.theme)
                syncingProgress?.indeterminateTintList = ColorStateList.valueOf(theme.theme)

            }
    }

    private fun getChatList() {
        mainViewModel.chatList.observe(this) {
            Log.e("=====", "response:: $it")

            if (it.getThreadList?.recipientUser?.size == 0) {
                rvChatRecyclerView?.visibility = View.GONE
                txtNoChat?.visibility = View.VISIBLE
            } else {
                rvChatRecyclerView?.visibility = View.VISIBLE
                txtNoChat?.visibility = View.GONE
                it.getThreadList?.let { it1 -> initRecyclerView(it1) }
            }
        }
        mainViewModel.errorMessage.observe(this) {
            Log.e("=====", "errorMessage:: $it")
            val conMgr =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = conMgr.activeNetworkInfo
            if (netInfo == null) {
                //No internet
                Snackbar.make(
                    constraintLayout,
                    "No Internet Connection. Please turn on your internet!",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("Retry") {
                        /*viewModel.registerUser(
                            etName.text.toString(),
                            Constants.CARRIER_ID,
                            phone_number.text.toString()
                        )*/
                    }
                    .setActionTextColor(resources.getColor(android.R.color.holo_red_light))
                    .show()
            } else {
                txtNoChat?.visibility = View.VISIBLE
                // requireActivity().toast(it.toString(), Toast.LENGTH_SHORT)
            }
        }
        mainViewModel.getChatList(
            PreferenceHelper.getStringPreference(
                this,
                Constants.USERID
            ).toString(),
            "Bearer ${
                PreferenceHelper.getStringPreference(
                    this,
                    Constants.TOKEN
                ).toString()
            }"
        )
    }

    override fun onItemClick(item: GetThreadListQuery.RecipientUser?) {
        /* val receiverUserIdList = ArrayList<String>()
        receiverUserIdList.add(item?.userId.toString())*/
        LogHelper.e("===========================","list isNotParticipant?:: ${item?.isNotParticipant}")

        var isNotParticipant = false
        if (item?.isNotParticipant?.contains(PreferenceHelper.getStringPreference(this, Constants.USERID)) == true){
            isNotParticipant = true
        }
        LogHelper.e("===========================","isNotParticipant:: $isNotParticipant")
        val intent = Intent(this, ChatActivity::class.java)
            .putExtra("currentUserId", PreferenceHelper.getStringPreference(this, Constants.USERID))
            .putStringArrayListExtra("receiverUserId",
                item?.recipientIds as java.util.ArrayList<String>?
            )
            .putExtra("threadId", item?.threadId)
            .putExtra("userName", item?.name.toString())
            .putExtra("groupName", item?.groupName.toString())
            .putExtra("isGroup", item?.isGroup)
            .putExtra("isNotParticipant", isNotParticipant)

        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    private fun initRecyclerView(chatList: GetThreadListQuery.GetThreadList) {
        rvChatRecyclerView?.layoutManager = LinearLayoutManager(this)

        // This will pass the ArrayList to our Adapter
        chatListAdapter = ChatListAdapter(this, chatList, this,this)

        // Setting the Adapter with the recyclerview
        rvChatRecyclerView?.adapter = chatListAdapter

    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.run(onNewIntentIntent::onNext)
    }

    override fun render(state: MainState) {
        if (state.hasError) {
            finish()
            return
        }

        val addContact = when (state.page) {
            is Inbox -> state.page.addContact
            is Archived -> state.page.addContact
            else -> false
        }

        val markPinned = when (state.page) {
            is Inbox -> state.page.markPinned
            is Archived -> state.page.markPinned
            else -> true
        }

        val markRead = when (state.page) {
            is Inbox -> state.page.markRead
            is Archived -> state.page.markRead
            else -> true
        }

        val selectedConversations = when (state.page) {
            is Inbox -> state.page.selected
            is Archived -> state.page.selected
            else -> 0
        }

        toolbarSearch.setVisible(state.page is Inbox && state.page.selected == 0 || state.page is Searching)
        // toolbarTitle.setVisible(toolbarSearch.visibility != View.VISIBLE)


//        toolbar.menu.findItem(R.id.archive)?.isVisible = state.page is Inbox && selectedConversations != 0
//        toolbar.menu.findItem(R.id.unarchive)?.isVisible = state.page is Archived && selectedConversations != 0
        toolbar.menu.findItem(R.id.delete)?.isVisible = selectedConversations != 0
        toolbar.menu.findItem(R.id.add)?.isVisible = addContact && selectedConversations != 0
//        toolbar.menu.findItem(R.id.pin)?.isVisible = markPinned && selectedConversations != 0
//        toolbar.menu.findItem(R.id.unpin)?.isVisible = !markPinned && selectedConversations != 0
        toolbar.menu.findItem(R.id.read)?.isVisible = markRead && selectedConversations != 0
        toolbar.menu.findItem(R.id.unread)?.isVisible = !markRead && selectedConversations != 0
//        toolbar.menu.findItem(R.id.block)?.isVisible = selectedConversations != 0

        /*listOf(plusBadge1, plusBadge2).forEach { badge ->
            badge.isVisible = drawerBadgesExperiment.variant && !state.upgraded
        }
//        plus.isVisible = state.upgraded
        plusBanner.isVisible = !state.upgraded*/
//        rateLayout.setVisible(state.showRating)

        compose.setVisible(state.page is Inbox || state.page is Archived)
        conversationsAdapter.emptyView =
            empty.takeIf { state.page is Inbox || state.page is Archived }
        searchAdapter.emptyView = empty.takeIf { state.page is Searching }

        when (state.page) {

            is Inbox -> {
                showBackButton(state.page.selected > 0)
                title = getString(R.string.main_title_selected, state.page.selected)
                if (recyclerView.adapter !== conversationsAdapter) recyclerView.adapter =
                    conversationsAdapter
                conversationsAdapter.updateData(state.page.data)
//                itemTouchHelper.attachToRecyclerView(recyclerView)
                empty.setText(R.string.inbox_empty_text)
            }

            is Searching -> {
                showBackButton(true)
                if (recyclerView.adapter !== searchAdapter) recyclerView.adapter = searchAdapter
                searchAdapter.data = state.page.data ?: listOf()
//                itemTouchHelper.attachToRecyclerView(null)
                empty.setText(R.string.inbox_search_empty_text)
            }

            is Archived -> {
                showBackButton(state.page.selected > 0)
                title = when (state.page.selected != 0) {
                    true -> getString(R.string.main_title_selected, state.page.selected)
                    false -> getString(R.string.title_archived)
                }
                if (recyclerView.adapter !== conversationsAdapter) recyclerView.adapter =
                    conversationsAdapter
                conversationsAdapter.updateData(state.page.data)
//                itemTouchHelper.attachToRecyclerView(null)
                empty.setText(R.string.archived_empty_text)
            }
        }

        /*inbox.isActivated = state.page is Inbox
        archived.isActivated = state.page is Archived

        if (drawerLayout.isDrawerOpen(GravityCompat.START) && !state.drawerOpen) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else if (!drawerLayout.isDrawerVisible(GravityCompat.START) && state.drawerOpen) {
            drawerLayout.openDrawer(GravityCompat.START)
        }*/

        when (state.syncing) {
            is SyncRepository.SyncProgress.Idle -> {
                syncing.isVisible = false
                snackbar.isVisible =
                    !state.defaultSms || !state.smsPermission || !state.contactPermission
            }

            is SyncRepository.SyncProgress.Running -> {
                syncing.isVisible = true
                syncingProgress.max = state.syncing.max
                progressAnimator.apply {
                    setIntValues(
                        syncingProgress.progress,
                        state.syncing.progress
                    )
                }.start()
                syncingProgress.isIndeterminate = state.syncing.indeterminate
                snackbar.isVisible = false
            }
        }

        when {
            !state.defaultSms -> {
                snackbarTitle?.setText(R.string.main_default_sms_title)
                snackbarMessage?.setText(R.string.main_default_sms_message)
                snackbarButton?.setText(R.string.main_default_sms_change)
            }

            !state.smsPermission -> {
                snackbarTitle?.setText(R.string.main_permission_required)
                snackbarMessage?.setText(R.string.main_permission_sms)
                snackbarButton?.setText(R.string.main_permission_allow)
            }

            !state.contactPermission -> {
                snackbarTitle?.setText(R.string.main_permission_required)
                snackbarMessage?.setText(R.string.main_permission_contacts)
                snackbarButton?.setText(R.string.main_permission_allow)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activityResumedIntent.onNext(true)
        val app = application as QKApplication
        mSocket = app.socket
        currentUserId = PreferenceHelper.getStringPreference(this, Constants.USERID).toString()
        mSocket?.on(Socket.EVENT_CONNECT) {
            mSocket?.emit("addUser", currentUserId)
            mSocket?.emit("addUser", currentUserId)
            mSocket?.emit("addUser", currentUserId)
            mSocket?.emit("addUser", currentUserId)
        }
        mSocket?.on(Socket.EVENT_DISCONNECT, onDisconnect)
        mSocket?.connect()
        if (tabAppears) {
            getChatList()
        }
    }

    override fun onPause() {
        super.onPause()
        activityResumedIntent.onNext(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
        mSocket?.disconnect()
        /*mSocket?.off(Socket.EVENT_DISCONNECT, onDisconnect)*/
    }

    private val onDisconnect = Emitter.Listener {
        runOnUiThread {
            Log.i("====================", "disconnected")
            // mSocket?.emit("removeUser", myUserId)

        }
    }

    private val onConnect = Emitter.Listener {
        // mSocket?.emit("addUser", myUserId)
        mSocket?.emit("addUser", currentUserId)
        mSocket?.emit("addUser", currentUserId)
        mSocket?.emit("addUser", currentUserId)
        mSocket?.emit("addUser", currentUserId)


    }

    override fun showBackButton(show: Boolean) {
        if (show) {
            toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        } else {
            toolbar?.visibility = View.GONE
            //toolbar.setNavigationIcon(R.drawable.ic_baseline_search_24)
        }
        /*toggle.onDrawerSlide(drawer, if (show) 1f else 0f)
        toggle.drawerArrowDrawable.color = when (show) {
            true -> resolveThemeColor(android.R.attr.textColorSecondary)
            false -> resolveThemeColor(android.R.attr.textColorPrimary)
        }*/
    }

    override fun requestDefaultSms() {
        navigator.showDefaultSmsDialog(this)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun requestPermissions() {

        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_CONTACTS
            ), 0
        )
    }

    override fun clearSearch() {
        dismissKeyboard()
        toolbarSearch.text = null
    }

    override fun clearSelection() {
        conversationsAdapter.clearSelection()
    }

    override fun themeChanged() {
        recyclerView.scrapViews()
    }

    override fun showBlockingDialog(conversations: List<Long>, block: Boolean) {
        blockingDialog.show(this, conversations, block)
    }

    override fun showDeleteDialog(conversations: List<Long>) {
        val count = conversations.size
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_delete_title)
            .setMessage(resources.getQuantityString(R.plurals.dialog_delete_message, count, count))
            .setPositiveButton(R.string.button_delete) { _, _ ->
                confirmDeleteIntent.onNext(
                    conversations
                )
            }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }

    override fun showChangelog(changelog: ChangelogManager.CumulativeChangelog) {
        changelogDialog.show(changelog)
    }

    override fun showArchivedSnackbar() {
        /*Snackbar.make(drawerLayout, R.string.toast_archived, Snackbar.LENGTH_LONG).apply {
            setAction(R.string.button_undo) { undoArchiveIntent.onNext(Unit) }
            setActionTextColor(colors.theme().theme)
            show()
        }*/
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (chatClicked) {
            menuInflater.inflate(R.menu.chat_options, menu)
        }else{
            menuInflater.inflate(R.menu.main, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        optionsItemIntent.onNext(item.itemId)
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
    val threadIdList: ArrayList<String> = ArrayList()

    override fun onChatClick(
        item: GetThreadListQuery.RecipientUser?,
        llOnClick: ConstraintLayout,
        adapterPosition: Int
    ) {
        chatClicked = true
        toolbarVisible?.visibility = View.GONE
        LogHelper.e("onChatClick", "===== item : $item")
        threadIdList.add(item?.threadId.toString())

        LogHelper.e("MESSAGEIDLIST", "==== $threadIdList")

        showDialog(llOnClick, threadIdList, adapterPosition)
    }

    private fun deleteThread(threadIdList: ArrayList<String>) {
        mainViewModel.deleteThread.observe(this) {
            runOnUiThread {
                toast("Chat Deleted!")
                getChatList()
            }
        }
        mainViewModel.errorMessage.observe(this) {
            Log.e("=====", "errorMessage:: $it")
            val conMgr =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = conMgr.activeNetworkInfo
            if (netInfo == null) {
                //No internet
                Snackbar.make(
                    findViewById(R.id.relMain),
                    "No Internet Connection. Please turn on your internet!",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("Retry") {

                    }
                    .setActionTextColor(resources.getColor(android.R.color.holo_red_light))
                    .show()
            } else {
                toast(it.toString(), Toast.LENGTH_SHORT)
            }
        }
        mainViewModel.deleteThread(currentUserId.toString(), threadIdList)
    }

    private fun showDialog(
        llOnClick: ConstraintLayout,
        threadIdList: ArrayList<String>,
        adapterPosition: Int
    ) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_delete_message)
        dialog.txtDesc?.text = "Do you want to delete this chat?"

        dialog.btnCancel.setOnClickListener {
            llOnClick.setBackgroundColor(resources.getColor(android.R.color.transparent))
            dialog.dismiss()
        }
        dialog.btnDelete.setOnClickListener {
            llOnClick.setBackgroundColor(resources.getColor(android.R.color.transparent))
            chatViewModel.deleteAllMessages(threadIdList)
            deleteThread(threadIdList)
            dialog.dismiss()
        }
        dialog.show()

    }

    /*override fun onBackPressed() {
         backPressedSubject.onNext(NavItem.BACK)
    }*/

}
