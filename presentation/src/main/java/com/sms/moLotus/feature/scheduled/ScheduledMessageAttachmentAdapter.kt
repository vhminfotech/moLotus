package com.sms.moLotus.feature.scheduled

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import com.sms.moLotus.R
import com.sms.moLotus.common.base.QkAdapter
import com.sms.moLotus.common.base.QkViewHolder
import com.sms.moLotus.util.GlideApp
import kotlinx.android.synthetic.main.attachment_image_list_item.view.*
import kotlinx.android.synthetic.main.scheduled_message_image_list_item.*
import javax.inject.Inject

class ScheduledMessageAttachmentAdapter @Inject constructor(
    private val context: Context
) : QkAdapter<Uri>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.scheduled_message_image_list_item, parent, false)
        view.thumbnail.clipToOutline = true

        return QkViewHolder(view)
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val attachment = getItem(position)

        GlideApp.with(context).load(attachment).into(holder.thumbnail)
    }

}
