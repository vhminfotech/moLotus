package com.sms.moLotus.feature.main

import android.content.Intent
import com.sms.moLotus.common.base.QkView
import com.sms.moLotus.manager.ChangelogManager
import io.reactivex.Observable
import io.reactivex.subjects.Subject

interface MainView : QkView<MainState> {

    val onNewIntentIntent: Observable<Intent>
    val activityResumedIntent: Observable<Boolean>
    val queryChangedIntent: Observable<CharSequence>
    val composeIntent: Unit
    val drawerOpenIntent: Observable<Boolean>
    val homeIntent: Observable<*>
    val navigationIntent: Observable<NavItem>
    val optionsItemIntent: Observable<Int>
    val plusBannerIntent: Observable<*>
    val dismissRatingIntent: Observable<*>
    val rateIntent: Observable<*>
    val conversationsSelectedIntent: Observable<List<Long>>
    val confirmDeleteIntent: Observable<List<Long>>
    val swipeConversationIntent: Unit
    val changelogMoreIntent: Observable<*>
    val undoArchiveIntent: Observable<Unit>
    val snackbarButtonIntent: Observable<Unit>

    fun requestDefaultSms()
    fun requestPermissions()
    fun clearSearch()
    fun clearSelection()
    fun themeChanged()
    fun showBlockingDialog(conversations: List<Long>, block: Boolean)
    fun showDeleteDialog(conversations: List<Long>)
    fun showChangelog(changelog: ChangelogManager.CumulativeChangelog)
    fun showArchivedSnackbar()

}

enum class NavItem { BACK, INBOX, ARCHIVED, BACKUP, SCHEDULED, BLOCKING, SETTINGS, PLUS, HELP, INVITE }
