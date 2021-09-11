package com.sms.moLotus.feature.changelog

import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.sms.moLotus.BuildConfig
import com.sms.moLotus.R
import com.sms.moLotus.feature.main.MainActivity
import com.sms.moLotus.manager.ChangelogManager
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.changelog_dialog.view.*

class ChangelogDialog(activity: MainActivity) {

    val moreClicks: Subject<Unit> = PublishSubject.create()

    private val dialog: AlertDialog
    private val adapter = ChangelogAdapter(activity)

    init {
        val layout = LayoutInflater.from(activity).inflate(R.layout.changelog_dialog, null)

        dialog = AlertDialog.Builder(activity)
                .setCancelable(true)
                .setView(layout)
                .create()

        layout.version.text = activity.getString(R.string.changelog_version, BuildConfig.VERSION_NAME)
        layout.changelog.adapter = adapter
        layout.more.setOnClickListener { dialog.dismiss(); moreClicks.onNext(Unit) }
        layout.dismiss.setOnClickListener { dialog.dismiss() }
    }

    fun show(changelog: ChangelogManager.CumulativeChangelog) {
        adapter.setChangelog(changelog)
        dialog.show()
    }

}
