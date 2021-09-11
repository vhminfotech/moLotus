package com.sms.moLotus.feature.compose.editing

import android.view.LayoutInflater
import android.view.ViewGroup
import com.sms.moLotus.R
import com.sms.moLotus.common.base.QkAdapter
import com.sms.moLotus.common.base.QkViewHolder
import com.sms.moLotus.model.PhoneNumber
import kotlinx.android.synthetic.main.contact_number_list_item.*

class PhoneNumberAdapter : QkAdapter<PhoneNumber>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.contact_number_list_item, parent, false)
        return QkViewHolder(view)
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val number = getItem(position)

        holder.address.text = number.address
        holder.type.text = number.type
    }

    override fun areItemsTheSame(old: PhoneNumber, new: PhoneNumber): Boolean {
        return old.type == new.type && old.address == new.address
    }

    override fun areContentsTheSame(old: PhoneNumber, new: PhoneNumber): Boolean {
        return old.type == new.type && old.address == new.address
    }

}