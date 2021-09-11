package com.sms.moLotus.feature.main

import androidx.lifecycle.ViewModel
import com.sms.moLotus.injection.ViewModelKey
import com.sms.moLotus.injection.scope.ActivityScope
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import io.reactivex.disposables.CompositeDisposable

@Module
class MainActivityModule {

    @Provides
    @ActivityScope
    fun provideCompositeDiposableLifecycle(): CompositeDisposable = CompositeDisposable()

    @Provides
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    fun provideMainViewModel(viewModel: MainViewModel): ViewModel = viewModel

}