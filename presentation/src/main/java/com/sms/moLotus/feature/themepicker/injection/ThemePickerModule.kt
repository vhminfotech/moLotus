package com.sms.moLotus.feature.themepicker.injection

import com.sms.moLotus.feature.themepicker.ThemePickerController
import com.sms.moLotus.injection.scope.ControllerScope
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class ThemePickerModule(private val controller: ThemePickerController) {

    @Provides
    @ControllerScope
    @Named("recipientId")
    fun provideThreadId(): Long = controller.recipientId

}