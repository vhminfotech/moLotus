package com.sms.moLotus.feature.main

import androidx.lifecycle.ViewModel
import com.sms.moLotus.injection.ViewModelKey
import com.sms.moLotus.injection.scope.ActivityScope
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Named

@Module
class MainActivityModule {

    @Provides
    @ActivityScope
    fun provideCompositeDiposableLifecycle(): CompositeDisposable = CompositeDisposable()

    @Provides
    @Named("threadId")
    fun provideThreadId(activity: MainActivity): Long =
        activity.intent.extras?.getLong("threadId") ?: 0L

    @Provides
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    fun provideMainViewModel(viewModel: MainViewModel): ViewModel = viewModel

}