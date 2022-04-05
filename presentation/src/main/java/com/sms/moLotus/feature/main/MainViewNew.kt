package com.sms.moLotus.feature.main

import android.content.Intent
import com.sms.moLotus.common.base.QkView
import com.sms.moLotus.manager.ChangelogManager
import io.reactivex.Observable
import io.reactivex.subjects.Subject

interface MainViewNew : QkView<MainState> {

    fun clearSearch()
    fun clearSelection()
    fun themeChanged()
    fun showBlockingDialog(conversations: List<Long>, block: Boolean)
    fun showDeleteDialog(conversations: List<Long>)
    fun showChangelog(changelog: ChangelogManager.CumulativeChangelog)
    fun showArchivedSnackbar()

}
