package com.sms.moLotus.feature.blocking.messages

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.sms.moLotus.R
import com.sms.moLotus.common.base.QkRealmAdapter
import com.sms.moLotus.common.base.QkViewHolder
import com.sms.moLotus.common.util.DateFormatter
import com.sms.moLotus.common.util.extensions.resolveThemeColor
import com.sms.moLotus.model.Conversation
import com.sms.moLotus.util.Preferences
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.blocked_list_item.*
import kotlinx.android.synthetic.main.blocked_list_item.view.*
import javax.inject.Inject

class BlockedMessagesAdapter @Inject constructor(
    private val context: Context,
    private val dateFormatter: DateFormatter
) : QkRealmAdapter<Conversation>() {

    val clicks: PublishSubject<Long> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.blocked_list_item, parent, false)

        if (viewType == 0) {
            view.title.setTypeface(view.title.typeface, Typeface.BOLD)
            view.date.setTypeface(view.date.typeface, Typeface.BOLD)
            view.date.setTextColor(view.context.resolveThemeColor(android.R.attr.textColorPrimary))
        }

        return QkViewHolder(view).apply {
            view.setOnClickListener {
                val conversation = getItem(adapterPosition) ?: return@setOnClickListener
                when (toggleSelection(conversation.id, false)) {
                    true -> view.isActivated = isSelected(conversation.id)
                    false -> clicks.onNext(conversation.id)
                }
            }
            view.setOnLongClickListener {
                val conversation = getItem(adapterPosition) ?: return@setOnLongClickListener true
                toggleSelection(conversation.id)
                view.isActivated = isSelected(conversation.id)
                true
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val conversation = getItem(position) ?: return

        holder.containerView.isActivated = isSelected(conversation.id)

        holder.avatars.recipients = conversation.recipients
        holder.title.collapseEnabled = conversation.recipients.size > 1
        holder.title.text = conversation.getTitle()
        holder.date.text = dateFormatter.getConversationTimestamp(conversation.date)

        holder.blocker.text = when (conversation.blockingClient) {
            Preferences.BLOCKING_MANAGER_CC -> context.getString(R.string.blocking_manager_call_control_title)
            Preferences.BLOCKING_MANAGER_SIA -> context.getString(R.string.blocking_manager_sia_title)
            else -> null
        }

        holder.reason.text = conversation.blockReason
        holder.blocker.isVisible = holder.blocker.text.isNotEmpty()
        holder.reason.isVisible = holder.blocker.text.isNotEmpty()
    }

    override fun getItemViewType(position: Int): Int {
        val conversation = getItem(position)
        return if (conversation?.unread == false) 1 else 0
    }

}
