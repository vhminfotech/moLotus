package com.sms.moLotus.feature.qkreply

import androidx.lifecycle.ViewModel
import com.sms.moLotus.injection.ViewModelKey
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import javax.inject.Named

@Module
class QkReplyActivityModule {

    @Provides
    @Named("threadId")
    fun provideThreadId(activity: QkReplyActivity): Long = activity.intent.extras?.getLong("threadId") ?: 0L

    @Provides
    @IntoMap
    @ViewModelKey(QkReplyViewModel::class)
    fun provideQkReplyViewModel(viewModel: QkReplyViewModel): ViewModel = viewModel

}