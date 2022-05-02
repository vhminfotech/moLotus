package com.sms.moLotus.feature.main.fragment

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.sms.moLotus.PreferenceHelper
import com.sms.moLotus.R
import com.sms.moLotus.feature.Constants
import com.sms.moLotus.feature.chat.ChatActivity
import com.sms.moLotus.feature.chat.adapter.ChatListAdapter
import com.sms.moLotus.feature.main.listener.OnItemClickListener
import com.sms.moLotus.feature.model.ChatList
import com.sms.moLotus.feature.retrofit.MainRepository
import com.sms.moLotus.feature.retrofit.MainViewModel
import com.sms.moLotus.feature.retrofit.MyViewModelFactory
import com.sms.moLotus.feature.retrofit.RetrofitService
import kotlinx.android.synthetic.main.fragment_mchat.view.*

class ChatFragment : Fragment(), OnItemClickListener {
    var layout: View? = null
    lateinit var viewModel: MainViewModel
    private val retrofitService = RetrofitService.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_mchat, container, false)
        viewModel =
            ViewModelProvider(this, MyViewModelFactory(MainRepository(retrofitService))).get(
                MainViewModel::class.java
            )
        getChatList()
        return layout
    }

    private fun initRecyclerView(chatList: ArrayList<ChatList>) {
        layout?.rvChatRecyclerView?.layoutManager = LinearLayoutManager(requireActivity())

        // This will pass the ArrayList to our Adapter
        val adapter = ChatListAdapter(requireActivity(), chatList, this)

        // Setting the Adapter with the recyclerview
        layout?.rvChatRecyclerView?.adapter = adapter
        layout?.rvChatRecyclerView?.adapter?.notifyDataSetChanged()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatFragment().apply {
                /* arguments = Bundle().apply {
                     putString(ARG_PARAM1, param1)
                     putString(ARG_PARAM2, param2)
                 }*/
            }
    }

    private fun getChatList() {
        viewModel.chatList.observe(viewLifecycleOwner, {
            Log.e("=====", "response:: $it")

            if (it.isNullOrEmpty()) {
                layout?.rvChatRecyclerView?.visibility = View.GONE
                layout?.txtNoChat?.visibility = View.VISIBLE

            } else {
                layout?.rvChatRecyclerView?.visibility = View.VISIBLE
                layout?.txtNoChat?.visibility = View.GONE
                initRecyclerView(it)
            }
        })
        viewModel.errorMessage.observe(viewLifecycleOwner, {
            Log.e("=====", "errorMessage:: $it")
            val conMgr =
                requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = conMgr.activeNetworkInfo
            if (netInfo == null) {
                //No internet
                Snackbar.make(
                    requireActivity().findViewById(R.id.chatFrag),
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
               // requireActivity().toast(it.toString(), Toast.LENGTH_SHORT)
            }
        })
        viewModel.getChatList(
            "Bearer ${
                PreferenceHelper.getStringPreference(
                    requireActivity(),
                    Constants.TOKEN
                ).toString()
            }"
        )
    }

    override fun onItemClick(item: ChatList?) {
        Toast.makeText(requireActivity(), item?.id.toString(), Toast.LENGTH_SHORT).show()
        val intent = Intent(requireActivity(), ChatActivity::class.java)
            .putExtra("currentUser", item?.current_user)
            .putExtra("threadId", item?.id)
            .putExtra("userName", item?.recipient_user?.get(0)?.name.toString())
        startActivity(intent)
        requireActivity().overridePendingTransition(0, 0)
    }


}