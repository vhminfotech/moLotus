package com.sms.moLotus.injection

import com.sms.moLotus.common.QKApplication
import com.sms.moLotus.common.QkDialog
import com.sms.moLotus.common.util.QkChooserTargetService
import com.sms.moLotus.common.widget.AvatarView
import com.sms.moLotus.common.widget.PagerTitleView
import com.sms.moLotus.common.widget.PreferenceView
import com.sms.moLotus.common.widget.QkEditText
import com.sms.moLotus.common.widget.QkSwitch
import com.sms.moLotus.common.widget.QkTextView
import com.sms.moLotus.common.widget.RadioPreferenceView
import com.sms.moLotus.feature.backup.BackupController
import com.sms.moLotus.feature.blocking.BlockingController
import com.sms.moLotus.feature.blocking.manager.BlockingManagerController
import com.sms.moLotus.feature.blocking.messages.BlockedMessagesController
import com.sms.moLotus.feature.blocking.numbers.BlockedNumbersController
import com.sms.moLotus.feature.compose.editing.DetailedChipView
import com.sms.moLotus.feature.conversationinfo.injection.ConversationInfoComponent
import com.sms.moLotus.feature.settings.SettingsController
import com.sms.moLotus.feature.settings.about.AboutController
import com.sms.moLotus.feature.settings.swipe.SwipeActionsController
import com.sms.moLotus.feature.themepicker.injection.ThemePickerComponent
import com.sms.moLotus.feature.widget.WidgetAdapter
import com.sms.moLotus.injection.android.ActivityBuilderModule
import com.sms.moLotus.injection.android.BroadcastReceiverBuilderModule
import com.sms.moLotus.injection.android.ServiceBuilderModule
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    AppModule::class,
    ActivityBuilderModule::class,
    BroadcastReceiverBuilderModule::class,
    ServiceBuilderModule::class])
interface AppComponent {

    fun conversationInfoBuilder(): ConversationInfoComponent.Builder
    fun themePickerBuilder(): ThemePickerComponent.Builder

    fun inject(application: QKApplication)

    fun inject(controller: AboutController)
    fun inject(controller: BackupController)
    fun inject(controller: BlockedMessagesController)
    fun inject(controller: BlockedNumbersController)
    fun inject(controller: BlockingController)
    fun inject(controller: BlockingManagerController)
    fun inject(controller: SettingsController)
    fun inject(controller: SwipeActionsController)

    fun inject(dialog: QkDialog)

    fun inject(service: WidgetAdapter)

    /**
     * This can't use AndroidInjection, or else it will crash on pre-marshmallow devices
     */
    fun inject(service: QkChooserTargetService)

    fun inject(view: AvatarView)
    fun inject(view: DetailedChipView)
    fun inject(view: PagerTitleView)
    fun inject(view: PreferenceView)
    fun inject(view: RadioPreferenceView)
    fun inject(view: QkEditText)
    fun inject(view: QkSwitch)
    fun inject(view: QkTextView)

}
