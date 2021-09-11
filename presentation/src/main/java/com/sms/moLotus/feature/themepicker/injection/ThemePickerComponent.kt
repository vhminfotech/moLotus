package com.sms.moLotus.feature.themepicker.injection

import com.sms.moLotus.feature.themepicker.ThemePickerController
import com.sms.moLotus.injection.scope.ControllerScope
import dagger.Subcomponent

@ControllerScope
@Subcomponent(modules = [ThemePickerModule::class])
interface ThemePickerComponent {

    fun inject(controller: ThemePickerController)

    @Subcomponent.Builder
    interface Builder {
        fun themePickerModule(module: ThemePickerModule): Builder
        fun build(): ThemePickerComponent
    }

}