package com.sms.moLotus.feature.main.fragment

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.jakewharton.rxbinding2.view.clicks
import com.sms.moLotus.R
import com.sms.moLotus.common.util.Colors
import com.sms.moLotus.common.util.extensions.autoScrollToStart
import com.sms.moLotus.feature.conversations.ConversationsAdapter
import com.sms.moLotus.feature.main.*
import com.sms.moLotus.repository.SyncRepository
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_sms.view.*
import kotlinx.android.synthetic.main.main_permission_hint.*
import kotlinx.android.synthetic.main.main_syncing.*
import timber.log.Timber

class SMSFragment : Fragment() {
    lateinit var conversationsAdapter: ConversationsAdapter

//    private lateinit var conversationsAdapter: ConversationsAdapter
    private lateinit var searchAdapter: SearchAdapter
    var layout: View? = null
    var state : MainState ?= null
    private val progressAnimator by lazy { ObjectAnimator.ofInt(syncingProgress, "progress", 0, 0) }
    var theme : Observable<Colors.Theme>?= null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_sms, container, false)

        Timber.e("onCreateView")
        conversationsAdapter = MainActivity.conversationsAdapterNew!!
        searchAdapter = MainActivity.searchAdapterNew!!
        theme = MainActivity.themeNew
        state= MainActivity.newState


        layout?.snackbar?.setOnInflateListener { _, _ ->
            try {
                snackbarButton.clicks()
                    .autoDisposable(scope(Lifecycle.Event.ON_DESTROY))
                    .subscribe(MainActivity.snackbarButtonIntentNew)
            }catch (e:Exception){
                e.printStackTrace()
            }

        }

        layout?.syncing?.setOnInflateListener { _, _ ->
            syncingProgress?.progressTintList = theme?.blockingFirst()?.theme?.let {
                ColorStateList.valueOf(
                    it
                )
            }
            syncingProgress?.indeterminateTintList =
                theme?.blockingFirst()?.theme?.let { ColorStateList.valueOf(it) }
        }
        Timber.e("state:: $state")

        layout?.recyclerView?.let { conversationsAdapter.autoScrollToStart(it) }


        initRecyclerView()








        return layout
    }


    private fun initRecyclerView(){
        Timber.e("render:: $state")

        conversationsAdapter.emptyView =
            layout?.empty.takeIf { state?.page is Inbox || state?.page is Archived }
        searchAdapter.emptyView = layout?.empty.takeIf { state?.page is Searching }

        when (state?.page) {

            is Inbox -> {
                if (layout?.recyclerView?.adapter !== conversationsAdapter) layout?.recyclerView?.adapter =
                    conversationsAdapter
                conversationsAdapter.updateData((state?.page as Inbox).data)
                layout?.empty?.setText(R.string.inbox_empty_text)
            }
            is Searching -> {
                MainActivity.qcIntent
                Timber.e("Searching:: ${state?.page}")

                if (layout?.recyclerView?.adapter !== searchAdapter) layout?.recyclerView?.adapter = searchAdapter
                searchAdapter.data = (state?.page as Searching).data ?: listOf()
                layout?.empty?.setText(R.string.inbox_search_empty_text)
            }
            is Archived ->{
                if (layout?.recyclerView?.adapter !== conversationsAdapter) layout?.recyclerView?.adapter =
                    conversationsAdapter
                conversationsAdapter.updateData((state?.page as Archived).data)
                layout?.empty?.setText(R.string.archived_empty_text)
            }
            else -> {
                /*if (layout?.recyclerView?.adapter !== conversationsAdapter) layout?.recyclerView?.adapter =
                    conversationsAdapter*/
            }
        }


        when (state?.syncing) {
            is SyncRepository.SyncProgress.Idle -> {
                layout?.syncing?.isVisible = false
                layout?.snackbar?.isVisible =
                    !state?.defaultSms!! || !state?.smsPermission!! || !state?.contactPermission!!
            }

            is SyncRepository.SyncProgress.Running -> {
                layout?.syncing?.isVisible = true
                try {
                    syncingProgress.max = (state?.syncing as SyncRepository.SyncProgress.Running).max
                    progressAnimator.apply {
                        setIntValues(
                            syncingProgress.progress,
                            (state?.syncing as SyncRepository.SyncProgress.Running).progress
                        )
                    }.start()
                    syncingProgress.isIndeterminate = (state?.syncing as SyncRepository.SyncProgress.Running).indeterminate
                    layout?.snackbar?.isVisible = false

                }catch (e: Exception){
                    e.printStackTrace()
                }

            }
        }
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
}