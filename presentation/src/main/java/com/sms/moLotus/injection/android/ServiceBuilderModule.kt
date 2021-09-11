package com.sms.moLotus.injection.android

import com.sms.moLotus.feature.backup.RestoreBackupService
import com.sms.moLotus.injection.scope.ActivityScope
import com.sms.moLotus.service.HeadlessSmsSendService
import com.sms.moLotus.receiver.SendSmsReceiver
import com.sms.moLotus.service.AutoDeleteService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBuilderModule {

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindAutoDeleteService(): AutoDeleteService

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindHeadlessSmsSendService(): HeadlessSmsSendService

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindRestoreBackupService(): RestoreBackupService

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindSendSmsReceiver(): SendSmsReceiver

}
