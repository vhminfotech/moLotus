package com.sms.moLotus.feature.compose

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.sms.moLotus.R
import com.sms.moLotus.common.base.QkAdapter
import com.sms.moLotus.common.base.QkViewHolder
import com.sms.moLotus.extensions.mapNotNull
import com.sms.moLotus.model.Attachment
import ezvcard.Ezvcard
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.attachment_contact_list_item.*
import kotlinx.android.synthetic.main.attachment_image_list_item.*
import kotlinx.android.synthetic.main.attachment_image_list_item.view.*
import javax.inject.Inject

class AttachmentAdapter @Inject constructor(
    private val context: Context
) : QkAdapter<Attachment>() {

    companion object {
        private const val VIEW_TYPE_IMAGE = 0
        private const val VIEW_TYPE_CONTACT = 1
    }

    val attachmentDeleted: Subject<Attachment> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = when (viewType) {
            VIEW_TYPE_IMAGE -> inflater.inflate(R.layout.attachment_image_list_item, parent, false)
                .apply { thumbnailBounds.clipToOutline = true }

            VIEW_TYPE_CONTACT -> inflater.inflate(
                R.layout.attachment_contact_list_item,
                parent,
                false
            )

            else -> inflater.inflate(R.layout.attachment_image_list_item, parent, false)
                .apply { thumbnailBounds.clipToOutline = true }
        }

        return QkViewHolder(view).apply {
            view.setOnClickListener {
                val attachment = getItem(adapterPosition)
                attachmentDeleted.onNext(attachment)
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {

        when (val attachment = getItem(position)) {
            is Attachment.Image -> {
                if (attachment.isAudio(context)) {
                    Glide.with(context)
                        .load(R.drawable.ic_baseline_mic_24)
                        .into(holder.thumbnail)

                } else if (attachment.isDoc(context) || attachment.isXlDoc(context) || attachment.isWordDoc(
                        context
                    )
                ) {
                    Glide.with(context)
                        .load(R.drawable.ic_outline_file_copy_24)
                        .into(holder.thumbnail)
                } else {
                    Glide.with(context)
                        .load(attachment.getUri())
                        .into(holder.thumbnail)
                }
            }

            is Attachment.Contact -> Observable.just(attachment.vCard)
                .mapNotNull { vCard -> Ezvcard.parse(vCard).first() }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { vcard -> holder.name?.text = vcard.formattedName.value }

        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is Attachment.Image -> VIEW_TYPE_IMAGE
        is Attachment.Contact -> VIEW_TYPE_CONTACT
    }

}