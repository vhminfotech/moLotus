package com.sms.moLotus.feature.chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.sms.moLotus.R
import com.sms.moLotus.common.widget.QkTextView
import com.sms.moLotus.feature.chat.listener.OnChatContactClickListener
import com.sms.moLotus.feature.chat.model.Users

class ChatContactListAdapter(
    private val context: Context,
    private val mList: List<Users>,
    private val listener: OnChatContactClickListener,
    private val isBottomSheet: Boolean
) :
    RecyclerView.Adapter<ChatContactListAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_contacts_list, parent, false)
        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contactList = mList[position]
        holder.txtUserName.text = contactList.name
        holder.txtPhoneNo.text = contactList.msisdn
        if (isBottomSheet){
            holder.checkbox.visibility = View.VISIBLE

            holder.checkbox.setOnCheckedChangeListener { _, p1 ->
                if (p1) {
                    listener.onCheckClick(contactList, contactList)
                }else{
                    listener.onCheckClick(null,contactList)
                }
            }
        }else{
            holder.checkbox.visibility = View.GONE
            holder.constraintLayout.setOnClickListener {
                listener.onChatContactClick(contactList)
            }
        }
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val txtUserName: QkTextView = itemView.findViewById(R.id.txtUserName)
        val txtPhoneNo: QkTextView = itemView.findViewById(R.id.txtPhoneNo)
        val constraintLayout: ConstraintLayout = itemView.findViewById(R.id.constraintLayout)
        val checkbox: CheckBox = itemView.findViewById(R.id.checkbox)
    }
}