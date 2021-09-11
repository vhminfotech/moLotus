package com.sms.moLotus.injection.android

import com.sms.moLotus.feature.backup.BackupActivity
import com.sms.moLotus.feature.blocking.BlockingActivity
import com.sms.moLotus.feature.compose.ComposeActivity
import com.sms.moLotus.feature.compose.ComposeActivityModule
import com.sms.moLotus.feature.contacts.ContactsActivity
import com.sms.moLotus.feature.contacts.ContactsActivityModule
import com.sms.moLotus.feature.conversationinfo.ConversationInfoActivity
import com.sms.moLotus.feature.gallery.GalleryActivity
import com.sms.moLotus.feature.gallery.GalleryActivityModule
import com.sms.moLotus.feature.main.MainActivity
import com.sms.moLotus.feature.main.MainActivityModule
import com.sms.moLotus.feature.notificationprefs.NotificationPrefsActivity
import com.sms.moLotus.feature.notificationprefs.NotificationPrefsActivityModule
import com.sms.moLotus.feature.plus.PlusActivity
import com.sms.moLotus.feature.plus.PlusActivityModule
import com.sms.moLotus.feature.qkreply.QkReplyActivity
import com.sms.moLotus.feature.qkreply.QkReplyActivityModule
import com.sms.moLotus.feature.scheduled.ScheduledActivity
import com.sms.moLotus.feature.scheduled.ScheduledActivityModule
import com.sms.moLotus.feature.settings.SettingsActivity
import com.sms.moLotus.injection.scope.ActivityScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilderModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    abstract fun bindMainActivity(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [PlusActivityModule::class])
    abstract fun bindPlusActivity(): PlusActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindBackupActivity(): BackupActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ComposeActivityModule::class])
    abstract fun bindComposeActivity(): ComposeActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ContactsActivityModule::class])
    abstract fun bindContactsActivity(): ContactsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindConversationInfoActivity(): ConversationInfoActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [GalleryActivityModule::class])
    abstract fun bindGalleryActivity(): GalleryActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [NotificationPrefsActivityModule::class])
    abstract fun bindNotificationPrefsActivity(): NotificationPrefsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [QkReplyActivityModule::class])
    abstract fun bindQkReplyActivity(): QkReplyActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ScheduledActivityModule::class])
    abstract fun bindScheduledActivity(): ScheduledActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindSettingsActivity(): SettingsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindBlockingActivity(): BlockingActivity

}
