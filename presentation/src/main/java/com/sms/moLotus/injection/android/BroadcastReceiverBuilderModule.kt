package com.sms.moLotus.injection.android

import com.sms.moLotus.feature.widget.WidgetProvider
import com.sms.moLotus.injection.scope.ActivityScope
import com.sms.moLotus.receiver.BlockThreadReceiver
import com.sms.moLotus.receiver.BootReceiver
import com.sms.moLotus.receiver.DefaultSmsChangedReceiver
import com.sms.moLotus.receiver.DeleteMessagesReceiver
import com.sms.moLotus.receiver.MarkArchivedReceiver
import com.sms.moLotus.receiver.MarkReadReceiver
import com.sms.moLotus.receiver.MarkSeenReceiver
import com.sms.moLotus.receiver.MmsReceivedReceiver
import com.sms.moLotus.receiver.MmsReceiver
import com.sms.moLotus.receiver.MmsSentReceiver
import com.sms.moLotus.receiver.MmsUpdatedReceiver
import com.sms.moLotus.receiver.NightModeReceiver
import com.sms.moLotus.receiver.RemoteMessagingReceiver
import com.sms.moLotus.receiver.SendScheduledMessageReceiver
import com.sms.moLotus.receiver.SmsDeliveredReceiver
import com.sms.moLotus.receiver.SmsProviderChangedReceiver
import com.sms.moLotus.receiver.SmsReceiver
import com.sms.moLotus.receiver.SmsSentReceiver
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BroadcastReceiverBuilderModule {

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindBlockThreadReceiver(): BlockThreadReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindBootReceiver(): BootReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindDefaultSmsChangedReceiver(): DefaultSmsChangedReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindDeleteMessagesReceiver(): DeleteMessagesReceiver

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindMarkArchivedReceiver(): MarkArchivedReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindMarkReadReceiver(): MarkReadReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindMarkSeenReceiver(): MarkSeenReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindMmsReceivedReceiver(): MmsReceivedReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindMmsReceiver(): MmsReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindMmsSentReceiver(): MmsSentReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindMmsUpdatedReceiver(): MmsUpdatedReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindNightModeReceiver(): NightModeReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindRemoteMessagingReceiver(): RemoteMessagingReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindSendScheduledMessageReceiver(): SendScheduledMessageReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindSmsDeliveredReceiver(): SmsDeliveredReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindSmsProviderChangedReceiver(): SmsProviderChangedReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindSmsReceiver(): SmsReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindSmsSentReceiver(): SmsSentReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindWidgetProvider(): WidgetProvider

}