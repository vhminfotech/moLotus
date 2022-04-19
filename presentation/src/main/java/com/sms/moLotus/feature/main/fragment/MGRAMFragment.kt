package com.sms.moLotus.feature.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sms.moLotus.R

class MGRAMFragment : Fragment() {
    var layout: View? = null
    /*private lateinit var conversationsAdapter: ConversationsAdapter
    var state: MainState? = null

    companion object {
        var rv: RecyclerView? = null
        var txtEmpty: QkTextView? = null
    }*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_sms, container, false)

        return layout
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