package com.sms.moLotus.feature.contacts

import com.sms.moLotus.common.base.QkView
import com.sms.moLotus.extensions.Optional
import com.sms.moLotus.feature.compose.editing.ComposeItem
import com.sms.moLotus.feature.compose.editing.PhoneNumberAction
import io.reactivex.Observable
import io.reactivex.subjects.Subject

interface ContactsContract : QkView<ContactsState> {

    val queryChangedIntent: Observable<CharSequence>
    val queryClearedIntent: Observable<*>
    val queryEditorActionIntent: Observable<Int>
    val composeItemPressedIntent: Subject<ComposeItem>
    val composeItemLongPressedIntent: Subject<ComposeItem>
    val phoneNumberSelectedIntent: Subject<Optional<Long>>
    val phoneNumberActionIntent: Subject<PhoneNumberAction>

    fun clearQuery()
    fun openKeyboard()
    fun finish(result: HashMap<String, String?>)

}
