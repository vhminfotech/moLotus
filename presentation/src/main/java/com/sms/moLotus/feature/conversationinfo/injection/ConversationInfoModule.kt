package com.sms.moLotus.feature.conversationinfo.injection

import com.sms.moLotus.feature.conversationinfo.ConversationInfoController
import com.sms.moLotus.injection.scope.ControllerScope
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class ConversationInfoModule(private val controller: ConversationInfoController) {

    @Provides
    @ControllerScope
    @Named("threadId")
    fun provideThreadId(): Long = controller.threadId

}