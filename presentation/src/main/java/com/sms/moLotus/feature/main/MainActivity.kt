package com.sms.moLotus.feature.main

//import com.sms.moLotus.feature.conversations.ConversationItemTouchCallback
//import kotlinx.android.synthetic.main.drawer_view.*
//import kotlinx.android.synthetic.main.main_syncing.*
import android.Manifest
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.tabs.TabLayout
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.sms.moLotus.R
import com.sms.moLotus.common.Navigator
import com.sms.moLotus.common.base.QkThemedActivity
import com.sms.moLotus.common.util.extensions.autoScrollToStart
import com.sms.moLotus.common.util.extensions.dismissKeyboard
import com.sms.moLotus.common.util.extensions.resolveThemeColor
import com.sms.moLotus.common.util.extensions.setVisible
import com.sms.moLotus.feature.blocking.BlockingDialog
import com.sms.moLotus.feature.changelog.ChangelogDialog
import com.sms.moLotus.feature.conversations.ConversationsAdapter
import com.sms.moLotus.feature.intro.IntroActivity
import com.sms.moLotus.feature.main.adapter.MyAdapter
import com.sms.moLotus.feature.main.fragment.SMSFragment
import com.sms.moLotus.manager.ChangelogManager
import com.sms.moLotus.repository.SyncRepository
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.layout_more_options.view.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.main_permission_hint.*
import kotlinx.android.synthetic.main.main_syncing.*
import timber.log.Timber
import javax.inject.Inject

class MainActivity : QkThemedActivity(), MainView {

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

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override val onNewIntentIntent: Subject<Intent> = PublishSubject.create()
    override val activityResumedIntent: Subject<Boolean> = PublishSubject.create()
    override val queryChangedIntent by lazy { toolbarSearch.textChanges() }
    override val composeIntent by lazy {
        compose.clicks()
    }
    override val homeIntent: Subject<Unit> = PublishSubject.create()

    override val optionsItemIntent: Subject<Int> = PublishSubject.create()

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

    /*private val toggle by lazy { ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.main_drawer_open_cd, 0) }*/
//    private val itemTouchHelper by lazy { ItemTouchHelper(itemTouchCallback) }
    private val progressAnimator by lazy { ObjectAnimator.ofInt(syncingProgress, "progress", 0, 0) }
    private val changelogDialog by lazy { ChangelogDialog(this) }
    private val snackbar by lazy { findViewById<View>(R.id.snackbar) }
    private val syncing by lazy { findViewById<View>(R.id.syncing) }
    private val backPressedSubject: Subject<NavItem> = PublishSubject.create()
    var isSearch: Boolean? = false

    companion object {
        var toolbarVisible: androidx.appcompat.widget.Toolbar? = null
        var conversationsAdapterNew: ConversationsAdapter? = null
        var newState: MainState? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        viewModel.bindView(this)
        onNewIntentIntent.onNext(intent)
        val settings = getSharedPreferences("appInfo", 0)
        val firstTime = settings.getBoolean("first_time", true)

        if (firstTime) {
            val intent = Intent(this, IntroActivity::class.java);
            startActivity(intent)
        }

        conversationsAdapterNew = conversationsAdapter
        toolbarVisible = toolbar
        initTabView()
        imgSearch.setOnClickListener {
            relSearch?.visibility = View.VISIBLE
        }

        imgClose.setOnClickListener {
            dismissKeyboard()
            toolbarSearch?.setText("")
            relSearch?.visibility = View.GONE
            clearSearch()
            clearSelection()
        }

        imgMenu.setOnClickListener { v ->
            val inflater =
                applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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

        (snackbar as? ViewStub)?.setOnInflateListener { _, _ ->
            snackbarButton.clicks()
                .autoDisposable(scope(Lifecycle.Event.ON_DESTROY))
                .subscribe(snackbarButtonIntent)
        }

        (syncing as? ViewStub)?.setOnInflateListener { _, _ ->
            //syncingProgress?.progressTintList = ColorStateList.valueOf(theme.blockingFirst().theme)
            /*syncingProgress?.indeterminateTintList =
                ColorStateList.valueOf(theme.blockingFirst().theme)*/
        }

        //toggle.syncState()
        toolbar.setNavigationOnClickListener {
            dismissKeyboard()
            homeIntent.onNext(Unit)
        }

        SMSFragment.rv?.let { conversationsAdapter.autoScrollToStart(it) }

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
                        // inboxIcon.imageTintList = tintList
                        // archivedIcon.imageTintList = tintList
                    }
            }
    }

    private fun initTabView() {
        tabLayout?.newTab()?.setText("SMS")?.let { tabLayout?.addTab(it) }
        tabLayout?.newTab()?.setText("MCHAT")?.let { tabLayout?.addTab(it) }
        tabLayout?.tabGravity = TabLayout.GRAVITY_FILL

        val adapter = MyAdapter(this, supportFragmentManager, tabLayout.tabCount)
        viewPager?.adapter = adapter
        viewPager?.adapter?.notifyDataSetChanged()
        viewPager?.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager?.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.run(onNewIntentIntent::onNext)
    }

    override fun render(state: MainState) {
        Timber.e("render :: $state")
        Timber.e("render :: ${state.page}")
        newState = state
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
        toolbar.menu.findItem(R.id.delete)?.isVisible = selectedConversations != 0
        toolbar.menu.findItem(R.id.add)?.isVisible = addContact && selectedConversations != 0
        toolbar.menu.findItem(R.id.read)?.isVisible = markRead && selectedConversations != 0
        toolbar.menu.findItem(R.id.unread)?.isVisible = !markRead && selectedConversations != 0
        compose.setVisible(state.page is Inbox || state.page is Archived)
        /*conversationsAdapter.emptyView =
            SMSFragment.txtEmpty?.takeIf { state.page is Inbox || state.page is Archived }*/
        searchAdapter.emptyView = SMSFragment.txtEmpty?.takeIf { state.page is Searching }

        when (state.page) {
            is Inbox -> {
                showBackButton(state.page.selected > 0)
                title = getString(R.string.main_title_selected, state.page.selected)
                if (isSearch == true) {
                    isSearch = false
                    if (SMSFragment.rv?.adapter !== conversationsAdapter) SMSFragment.rv?.adapter =
                        conversationsAdapter
                    conversationsAdapter.updateData(state.page.data)
                    SMSFragment.txtEmpty?.setText(R.string.inbox_empty_text)
                }
            }

            is Searching -> {
                showBackButton(true)
                if (SMSFragment.rv?.adapter !== searchAdapter) SMSFragment.rv?.adapter =
                    searchAdapter
                searchAdapter.data = state.page.data ?: listOf()
                SMSFragment.txtEmpty?.setText(R.string.inbox_search_empty_text)
                isSearch = true
            }

            is Archived -> {
                showBackButton(state.page.selected > 0)
                title = when (state.page.selected != 0) {
                    true -> getString(R.string.main_title_selected, state.page.selected)
                    false -> getString(R.string.title_archived)
                }

            }
        }

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
    }

    override fun onPause() {
        super.onPause()
        activityResumedIntent.onNext(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
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
        //  SMSFragment.rv?.scrapViews()
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
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        optionsItemIntent.onNext(item.itemId)
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
