package com.sms.moLotus.feature.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sms.moLotus.R
import com.sms.moLotus.common.util.extensions.autoScrollToStart
import com.sms.moLotus.common.widget.QkTextView
import com.sms.moLotus.feature.conversations.ConversationsAdapter
import com.sms.moLotus.feature.main.*
import kotlinx.android.synthetic.main.fragment_sms.view.*
import timber.log.Timber

class SMSFragment : Fragment() {
    var layout: View? = null
    private lateinit var conversationsAdapter: ConversationsAdapter
    var state: MainState? = null

    companion object {
        var rv: RecyclerView? = null
        var txtEmpty: QkTextView? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_sms, container, false)
        conversationsAdapter = MainActivity.conversationsAdapterNew!!
        state = MainActivity.newState

        rv = layout?.recyclerView
        val layoutMgr = LinearLayoutManager(requireActivity())
        rv?.layoutManager = layoutMgr
        txtEmpty = layout?.empty
        Timber.e("onCreateView")


        /*layout?.snackbar?.setOnInflateListener { _, _ ->
            layout?.snackbarButton?.clicks()
                ?.autoDisposable(scope(Lifecycle.Event.ON_DESTROY))
                ?.subscribe(MainActivity.snackbarButtonIntentNew)

        }

        layout?.syncing?.setOnInflateListener { _, _ ->
            layout?.syncingProgress?.progressTintList = theme?.blockingFirst()?.theme?.let {
                ColorStateList.valueOf(
                    it
                )
            }
            layout?.syncingProgress?.indeterminateTintList =
                theme?.blockingFirst()?.theme?.let { ColorStateList.valueOf(it) }
        }*/
        //Timber.e("state:: $state")

        rv?.let { conversationsAdapter.autoScrollToStart(it) }
        initRecyclerView()
        return layout
    }


    private fun initRecyclerView() {

        /*conversationsAdapter.emptyView =
            txtEmpty.takeIf { state?.page is Inbox || state?.page is Archived }*/

        when (state?.page) {

            is Inbox -> {
                if (rv?.adapter !== conversationsAdapter) rv?.adapter =
                    conversationsAdapter
                conversationsAdapter.updateData((state?.page as Inbox).data)
                txtEmpty?.setText(R.string.inbox_empty_text)
            }
            is Searching -> {

               /* if (rv?.adapter !== searchAdapter) rv?.adapter =
                    searchAdapter
                searchAdapter.data = (state?.page as Searching).data ?: listOf()
                txtEmpty?.setText(R.string.inbox_search_empty_text)*/
            }
            is Archived -> {
                if (rv?.adapter !== conversationsAdapter) rv?.adapter =
                    conversationsAdapter
                conversationsAdapter.updateData((state?.page as Archived).data)
                txtEmpty?.setText(R.string.archived_empty_text)
            }
            else -> {
                /*if (rv?.adapter !== conversationsAdapter) rv?.adapter =
                    conversationsAdapter*/
            }
        }


    }


    /*companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SMSFragment().apply {
                *//*arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }*//*
            }
    }*/
}