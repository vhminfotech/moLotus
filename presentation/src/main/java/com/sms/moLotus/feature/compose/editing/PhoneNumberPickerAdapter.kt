package com.sms.moLotus.feature.compose.editing

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.sms.moLotus.R
import com.sms.moLotus.common.base.QkAdapter
import com.sms.moLotus.common.base.QkViewHolder
import com.sms.moLotus.common.util.extensions.forwardTouches
import com.sms.moLotus.extensions.Optional
import com.sms.moLotus.model.PhoneNumber
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.phone_number_list_item.*
import kotlinx.android.synthetic.main.radio_preference_view.*
import kotlinx.android.synthetic.main.radio_preference_view.view.*
import javax.inject.Inject

class PhoneNumberPickerAdapter @Inject constructor(
    private val context: Context
) : QkAdapter<PhoneNumber>() {

    val selectedItemChanges: Subject<Optional<Long>> = BehaviorSubject.create()

    private var selectedItem: Long? = null
        set(value) {
            data.indexOfFirst { number -> number.id == field }.takeIf { it != -1 }?.run(::notifyItemChanged)
            field = value
            data.indexOfFirst { number -> number.id == field }.takeIf { it != -1 }?.run(::notifyItemChanged)
            selectedItemChanges.onNext(Optional(value))
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.phone_number_list_item, parent, false)
        return QkViewHolder(view).apply {
            radioButton.forwardTouches(itemView)

            view.setOnClickListener {
                val phoneNumber = getItem(adapterPosition)
                selectedItem = phoneNumber.id
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val phoneNumber = getItem(position)

        holder.number.radioButton.isChecked = phoneNumber.id == selectedItem
        holder.number.titleView.text = phoneNumber.address
        holder.number.summaryView.text = when (phoneNumber.isDefault) {
            true -> context.getString(R.string.compose_number_picker_default, phoneNumber.type)
            false -> phoneNumber.type
        }
    }

    override fun onDatasetChanged() {
        super.onDatasetChanged()
        selectedItem = data.find { number -> number.isDefault }?.id ?: data.firstOrNull()?.id
    }

}
