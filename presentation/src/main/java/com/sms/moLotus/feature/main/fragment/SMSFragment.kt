package com.sms.moLotus.feature.main.fragment

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.sms.moLotus.R
import com.sms.moLotus.common.util.extensions.autoScrollToStart
import com.sms.moLotus.feature.conversations.ConversationsAdapter
import com.sms.moLotus.feature.main.Archived
import com.sms.moLotus.feature.main.Inbox
import com.sms.moLotus.feature.main.MainState
import com.sms.moLotus.feature.main.MainViewNew
import com.sms.moLotus.manager.ChangelogManager
import com.sms.moLotus.repository.SyncRepository
import kotlinx.android.synthetic.main.fragment_sms.recyclerView
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.main_syncing.*
import javax.inject.Inject

class SMSFragment : Fragment(), MainViewNew {
    @Inject
    lateinit var conversationsAdapter: ConversationsAdapter
    var layout: View? = null
    private val progressAnimator by lazy { ObjectAnimator.ofInt(syncingProgress, "progress", 0, 0) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_sms, container, false)
        conversationsAdapter.autoScrollToStart(recyclerView)
        return layout
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SMSFragment().apply {
                /*arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }*/
            }
    }

    override fun clearSearch() {
    }

    override fun clearSelection() {
    }

    override fun themeChanged() {
    }

    override fun showBlockingDialog(conversations: List<Long>, block: Boolean) {
    }

    override fun showDeleteDialog(conversations: List<Long>) {
    }

    override fun showChangelog(changelog: ChangelogManager.CumulativeChangelog) {
    }

    override fun showArchivedSnackbar() {
    }

    override fun render(state: MainState) {
        conversationsAdapter.emptyView =
            empty.takeIf { state.page is Inbox || state.page is Archived }
        when (state.page) {

            is Inbox -> {
                if (recyclerView.adapter !== conversationsAdapter) recyclerView.adapter =
                    conversationsAdapter
                conversationsAdapter.updateData(state.page.data)
//                itemTouchHelper.attachToRecyclerView(recyclerView)
                empty.setText(R.string.inbox_empty_text)
            }
            else -> {
                if (recyclerView.adapter !== conversationsAdapter) recyclerView.adapter =
                    conversationsAdapter
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
    }
}