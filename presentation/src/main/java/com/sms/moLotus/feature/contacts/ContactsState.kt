package com.sms.moLotus.feature.contacts

import com.sms.moLotus.feature.compose.editing.ComposeItem
import com.sms.moLotus.model.Contact

data class ContactsState(
    val query: String = "",
    val composeItems: List<ComposeItem> = ArrayList(),
    val selectedContact: Contact? = null // For phone number picker
)
