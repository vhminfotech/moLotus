package com.sms.moLotus.feature.conversationinfo.injection

import com.sms.moLotus.feature.conversationinfo.ConversationInfoController
import com.sms.moLotus.injection.scope.ControllerScope
import dagger.Subcomponent

@ControllerScope
@Subcomponent(modules = [ConversationInfoModule::class])
interface ConversationInfoComponent {

    fun inject(controller: ConversationInfoController)

    @Subcomponent.Builder
    interface Builder {
        fun conversationInfoModule(module: ConversationInfoModule): Builder
        fun build(): ConversationInfoComponent
    }

}