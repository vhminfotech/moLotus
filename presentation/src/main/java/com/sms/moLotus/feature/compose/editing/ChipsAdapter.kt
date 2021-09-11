package com.sms.moLotus.feature.compose.editing

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.sms.moLotus.R
import com.sms.moLotus.common.base.QkAdapter
import com.sms.moLotus.common.base.QkViewHolder
import com.sms.moLotus.common.util.extensions.dpToPx
import com.sms.moLotus.common.util.extensions.resolveThemeColor
import com.sms.moLotus.common.util.extensions.setBackgroundTint
import com.sms.moLotus.model.Recipient
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.contact_chip.*
import javax.inject.Inject

class ChipsAdapter @Inject constructor() : QkAdapter<Recipient>() {

    var view: RecyclerView? = null
    val chipDeleted: PublishSubject<Recipient> = PublishSubject.create<Recipient>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.contact_chip, parent, false)
        return QkViewHolder(view).apply {
            // These theme attributes don't apply themselves on API 21
            if (Build.VERSION.SDK_INT <= 22) {
                content.setBackgroundTint(view.context.resolveThemeColor(R.attr.bubbleColor))
            }

            view.setOnClickListener {
                val chip = getItem(adapterPosition)
                showDetailedChip(view.context, chip)
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val recipient = getItem(position)

        holder.avatar.setRecipient(recipient)
        holder.name.text = recipient.contact?.name?.takeIf { it.isNotBlank() } ?: recipient.address
    }

    /**
     * The [context] has to come from a view, because we're inflating a view that used themed attrs
     */
    private fun showDetailedChip(context: Context, recipient: Recipient) {
        val detailedChipView = DetailedChipView(context)
        detailedChipView.setRecipient(recipient)

        val rootView = view?.rootView as ViewGroup

        val layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)

        layoutParams.topMargin = 24.dpToPx(context)
        layoutParams.marginStart = 56.dpToPx(context)

        rootView.addView(detailedChipView, layoutParams)
        detailedChipView.show()

        detailedChipView.setOnDeleteListener {
            chipDeleted.onNext(recipient)
            detailedChipView.hide()
        }
    }
}
