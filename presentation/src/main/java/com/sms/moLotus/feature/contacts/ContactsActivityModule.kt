package com.sms.moLotus.feature.contacts

import androidx.lifecycle.ViewModel
import com.sms.moLotus.injection.ViewModelKey
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
class ContactsActivityModule {

    @Provides
    fun provideIsSharing(activity: ContactsActivity): Boolean {
        return activity.intent.extras?.getBoolean(ContactsActivity.SharingKey, false) ?: false
    }

    @Provides
    fun provideChips(activity: ContactsActivity): HashMap<String, String?> {
        return activity.intent.extras?.getSerializable(ContactsActivity.ChipsKey)
                ?.let { serializable -> serializable as? HashMap<String, String?> }
                ?: hashMapOf()
    }

    @Provides
    @IntoMap
    @ViewModelKey(ContactsViewModel::class)
    fun provideContactsViewModel(viewModel: ContactsViewModel): ViewModel = viewModel

}
