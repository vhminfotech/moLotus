package com.sms.moLotus.feature.contacts

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.editorActions
import com.jakewharton.rxbinding2.widget.textChanges
import com.sms.moLotus.R
import com.sms.moLotus.common.ViewModelFactory
import com.sms.moLotus.common.base.QkThemedActivity
import com.sms.moLotus.common.util.extensions.hideKeyboard
import com.sms.moLotus.common.util.extensions.resolveThemeColor
import com.sms.moLotus.common.util.extensions.setBackgroundTint
import com.sms.moLotus.common.util.extensions.showKeyboard
import com.sms.moLotus.common.widget.QkDialog
import com.sms.moLotus.extensions.Optional
import com.sms.moLotus.feature.compose.editing.ComposeItem
import com.sms.moLotus.feature.compose.editing.ComposeItemAdapter
import com.sms.moLotus.feature.compose.editing.PhoneNumberAction
import com.sms.moLotus.feature.compose.editing.PhoneNumberPickerAdapter
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.contacts_activity.*
import timber.log.Timber
import javax.inject.Inject

class ContactsActivity : QkThemedActivity(), ContactsContract {

    companion object {
        const val SharingKey = "sharing"
        const val ChipsKey = "chips"
        const val DraftKey = "draft"
    }

    @Inject lateinit var contactsAdapter: ComposeItemAdapter
    @Inject lateinit var phoneNumberAdapter: PhoneNumberPickerAdapter
    @Inject lateinit var viewModelFactory: ViewModelFactory

    override val queryChangedIntent: Observable<CharSequence> by lazy { search.textChanges() }
    override val queryClearedIntent: Observable<*> by lazy { cancel.clicks() }
    override val queryEditorActionIntent: Observable<Int> by lazy { search.editorActions() }
    override val composeItemPressedIntent: Subject<ComposeItem> by lazy { contactsAdapter.clicks }
    override val composeItemLongPressedIntent: Subject<ComposeItem> by lazy { contactsAdapter.longClicks }
    override val phoneNumberSelectedIntent: Subject<Optional<Long>> by lazy { phoneNumberAdapter.selectedItemChanges }
    override val phoneNumberActionIntent: Subject<PhoneNumberAction> = PublishSubject.create()

    private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory)[ContactsViewModel::class.java] }

    private val phoneNumberDialog by lazy {
        QkDialog(this).apply {
            titleRes = R.string.compose_number_picker_title
            adapter = phoneNumberAdapter
            positiveButton = R.string.compose_number_picker_always
            positiveButtonListener = { phoneNumberActionIntent.onNext(PhoneNumberAction.ALWAYS) }
            negativeButton = R.string.compose_number_picker_once
            negativeButtonListener = { phoneNumberActionIntent.onNext(PhoneNumberAction.JUST_ONCE) }
            cancelListener = { phoneNumberActionIntent.onNext(PhoneNumberAction.CANCEL) }
        }
    }

    private var draftData : String?= ""
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.contacts_activity)
        showBackButton(true)
        viewModel.bindView(this)


        draftData = intent?.getStringExtra(DraftKey)
        Timber.e("")
        contacts.adapter = contactsAdapter

        // These theme attributes don't apply themselves on API 21
        if (Build.VERSION.SDK_INT <= 22) {
            search.setBackgroundTint(resolveThemeColor(R.attr.bubbleColor))
        }
    }

    override fun render(state: ContactsState) {
        cancel.isVisible = state.query.length > 1

        contactsAdapter.data = state.composeItems

        if (state.selectedContact != null && !phoneNumberDialog.isShowing) {
            phoneNumberAdapter.data = state.selectedContact.numbers
            phoneNumberDialog.subtitle = state.selectedContact.name
            phoneNumberDialog.show()
        } else if (state.selectedContact == null && phoneNumberDialog.isShowing) {
            phoneNumberDialog.dismiss()
        }
    }

    override fun clearQuery() {
        search.text = null
    }

    override fun openKeyboard() {
        search.postDelayed({
            search.showKeyboard()
        }, 200)
    }

    override fun finish(result: HashMap<String, String?>) {
        search.hideKeyboard()
        val intent = Intent().putExtra(ChipsKey, result).putExtra(DraftKey,draftData)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

}
