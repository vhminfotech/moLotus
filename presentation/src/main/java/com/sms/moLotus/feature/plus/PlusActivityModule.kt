package com.sms.moLotus.feature.plus

import androidx.lifecycle.ViewModel
import com.sms.moLotus.injection.ViewModelKey
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
class PlusActivityModule {

    @Provides
    @IntoMap
    @ViewModelKey(PlusViewModel::class)
    fun providePlusViewModel(viewModel: PlusViewModel): ViewModel = viewModel

}