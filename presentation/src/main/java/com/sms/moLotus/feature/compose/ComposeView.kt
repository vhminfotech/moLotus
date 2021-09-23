package com.sms.moLotus.feature.compose

import android.net.Uri
import androidx.annotation.StringRes
import androidx.core.view.inputmethod.InputContentInfoCompat
import com.sms.moLotus.common.base.QkView
import com.sms.moLotus.model.Attachment
import com.sms.moLotus.model.Recipient
import io.reactivex.Observable
import io.reactivex.subjects.Subject

interface ComposeView : QkView<ComposeState> {

    val activityVisibleIntent: Observable<Boolean>
    val chipsSelectedIntent: Subject<HashMap<String, String?>>
    val chipDeletedIntent: Subject<Recipient>
    val menuReadyIntent: Observable<Unit>
    val optionsItemIntent: Observable<Int>
    val sendAsGroupIntent: Observable<*>
    val messageClickIntent: Subject<Long>
    val messagePartClickIntent: Subject<Long>
    val messagesSelectedIntent: Observable<List<Long>>
    val cancelSendingIntent: Subject<Long>
    val attachmentDeletedIntent: Subject<Attachment>
    val textChangedIntent: Observable<CharSequence>
    val attachIntent: Observable<Unit>
    val cameraIntent: Observable<*>
    val galleryIntent: Observable<*>
    val scheduleIntent: Unit
    val attachContactIntent: Observable<*>
    val attachmentSelectedIntent: Observable<Uri>
    val contactSelectedIntent: Observable<Uri>
    val inputContentIntent: Observable<InputContentInfoCompat>
    val scheduleSelectedIntent: Observable<Long>
    val scheduleCancelIntent: Observable<*>
    val changeSimIntent: Observable<*>
    val sendIntent: Observable<Unit>
    val viewQksmsPlusIntent: Subject<Unit>
    val backPressedIntent: Observable<Unit>

    fun clearSelection()
    fun showDetails(details: String)
    fun requestDefaultSms()
    fun requestStoragePermission()
    fun requestSmsPermission()
    fun showContacts(sharing: Boolean, chips: List<Recipient>)
    fun themeChanged()
    fun showKeyboard()
    fun requestCamera()
    fun requestGallery()
    fun requestDatePicker()
    fun requestContact()
    fun setDraft(draft: String)
    fun scrollToMessage(id: Long)
    fun showQksmsPlusSnackbar(@StringRes message: Int)

}