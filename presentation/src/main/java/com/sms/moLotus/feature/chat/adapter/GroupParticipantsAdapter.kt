package com.sms.moLotus.feature.chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.sms.moLotus.GetGroupDetailsQuery
import com.sms.moLotus.R
import com.sms.moLotus.common.widget.QkTextView
import com.sms.moLotus.feature.chat.listener.OnGroupItemClickListener

class GroupParticipantsAdapter(
    private val context: Context,
    private val mList: List<GetGroupDetailsQuery.ParticipantsOfGroup?>?,
    private val listener: OnGroupItemClickListener,
) :
    RecyclerView.Adapter<GroupParticipantsAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group_participants, parent, false)
        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txtUserName.text = mList?.get(position)?.name.toString()
        holder.txtPhoneNo.text = mList?.get(position)?.mobile.toString()
        if (mList?.get(position)?.isAdmin == true) {
            holder.txtGroupAdmin.visibility = View.VISIBLE

        } else {
            holder.txtGroupAdmin.visibility = View.GONE
        }


        holder.llItem.setOnLongClickListener {
            listener.onGroupItemClick(
                mList?.get(position)?.id.toString(),
                holder.txtGroupAdmin,
                holder.llItem
            )
            return@setOnLongClickListener true
        }

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList?.size ?: 0
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val txtUserName: QkTextView = itemView.findViewById(R.id.txtUserName)
        val txtPhoneNo: QkTextView = itemView.findViewById(R.id.txtPhoneNo)
        val txtGroupAdmin: QkTextView = itemView.findViewById(R.id.txtGroupAdmin)
        val llItem: LinearLayout = itemView.findViewById(R.id.llItem)
    }
}