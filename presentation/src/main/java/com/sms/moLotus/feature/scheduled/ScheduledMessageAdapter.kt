package com.sms.moLotus.feature.scheduled

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.sms.moLotus.R
import com.sms.moLotus.common.base.QkRealmAdapter
import com.sms.moLotus.common.base.QkViewHolder
import com.sms.moLotus.common.util.DateFormatter
import com.sms.moLotus.model.Contact
import com.sms.moLotus.model.Recipient
import com.sms.moLotus.model.ScheduledMessage
import com.sms.moLotus.repository.ContactRepository
import com.sms.moLotus.util.PhoneNumberUtils
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.scheduled_message_list_item.*
import kotlinx.android.synthetic.main.scheduled_message_list_item.view.*
import javax.inject.Inject

class ScheduledMessageAdapter @Inject constructor(
    private val context: Context,
    private val contactRepo: ContactRepository,
    private val dateFormatter: DateFormatter,
    private val phoneNumberUtils: PhoneNumberUtils
) : QkRealmAdapter<ScheduledMessage>() {

    private val contacts by lazy { contactRepo.getContacts() }
    private val contactCache = ContactCache()
    private val imagesViewPool = RecyclerView.RecycledViewPool()

    val clicks: Subject<Long> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.scheduled_message_list_item, parent, false)

        view.attachments.adapter = ScheduledMessageAttachmentAdapter(context)
        view.attachments.setRecycledViewPool(imagesViewPool)

        return QkViewHolder(view).apply {
            view.setOnClickListener {
                val message = getItem(adapterPosition) ?: return@setOnClickListener
                clicks.onNext(message.id)
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val message = getItem(position) ?: return

        // GroupAvatarView only accepts recipients, so map the phone numbers to recipients
        holder.avatars.recipients = message.recipients.map { address -> Recipient(address = address) }

        holder.recipients.text = message.recipients.joinToString(",") { address ->
            contactCache[address]?.name?.takeIf { it.isNotBlank() } ?: address
        }

        holder.date.text = dateFormatter.getScheduledTimestamp(message.date)
        holder.body.text = message.body

        val adapter = holder.attachments.adapter as ScheduledMessageAttachmentAdapter
        adapter.data = message.attachments.map(Uri::parse)
        holder.attachments.isVisible = message.attachments.isNotEmpty()
    }

    /**
     * Cache the contacts in a map by the address, because the messages we're binding don't have
     * a reference to the contact.
     */
    private inner class ContactCache : HashMap<String, Contact?>() {

        override fun get(key: String): Contact? {
            if (super.get(key)?.isValid != true) {
                set(key, contacts.firstOrNull { contact ->
                    contact.numbers.any {
                        phoneNumberUtils.compare(it.address, key)
                    }
                })
            }

            return super.get(key)?.takeIf { it.isValid }
        }

    }

}
