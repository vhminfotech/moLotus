package com.sms.moLotus.injection

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.lifecycle.ViewModelProvider
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.sms.moLotus.blocking.BlockingClient
import com.sms.moLotus.blocking.BlockingManager
import com.sms.moLotus.common.ViewModelFactory
import com.sms.moLotus.common.util.BillingManagerImpl
import com.sms.moLotus.common.util.NotificationManagerImpl
import com.sms.moLotus.common.util.ShortcutManagerImpl
import com.sms.moLotus.feature.conversationinfo.injection.ConversationInfoComponent
import com.sms.moLotus.feature.themepicker.injection.ThemePickerComponent
import com.sms.moLotus.listener.ContactAddedListener
import com.sms.moLotus.listener.ContactAddedListenerImpl
import com.sms.moLotus.manager.ActiveConversationManager
import com.sms.moLotus.manager.ActiveConversationManagerImpl
import com.sms.moLotus.manager.AlarmManager
import com.sms.moLotus.manager.AlarmManagerImpl
import com.sms.moLotus.manager.AnalyticsManager
import com.sms.moLotus.manager.AnalyticsManagerImpl
import com.sms.moLotus.manager.BillingManager
import com.sms.moLotus.manager.ChangelogManager
import com.sms.moLotus.manager.ChangelogManagerImpl
import com.sms.moLotus.manager.KeyManager
import com.sms.moLotus.manager.KeyManagerImpl
import com.sms.moLotus.manager.NotificationManager
import com.sms.moLotus.manager.PermissionManager
import com.sms.moLotus.manager.PermissionManagerImpl
import com.sms.moLotus.manager.RatingManager
import com.sms.moLotus.manager.ReferralManager
import com.sms.moLotus.manager.ReferralManagerImpl
import com.sms.moLotus.manager.ShortcutManager
import com.sms.moLotus.manager.WidgetManager
import com.sms.moLotus.manager.WidgetManagerImpl
import com.sms.moLotus.mapper.CursorToContact
import com.sms.moLotus.mapper.CursorToContactGroup
import com.sms.moLotus.mapper.CursorToContactGroupImpl
import com.sms.moLotus.mapper.CursorToContactGroupMember
import com.sms.moLotus.mapper.CursorToContactGroupMemberImpl
import com.sms.moLotus.mapper.CursorToContactImpl
import com.sms.moLotus.mapper.CursorToConversation
import com.sms.moLotus.mapper.CursorToConversationImpl
import com.sms.moLotus.mapper.CursorToMessage
import com.sms.moLotus.mapper.CursorToMessageImpl
import com.sms.moLotus.mapper.CursorToPart
import com.sms.moLotus.mapper.CursorToPartImpl
import com.sms.moLotus.mapper.CursorToRecipient
import com.sms.moLotus.mapper.CursorToRecipientImpl
import com.sms.moLotus.mapper.RatingManagerImpl
import com.sms.moLotus.repository.BackupRepository
import com.sms.moLotus.repository.BackupRepositoryImpl
import com.sms.moLotus.repository.BlockingRepository
import com.sms.moLotus.repository.BlockingRepositoryImpl
import com.sms.moLotus.repository.ContactRepository
import com.sms.moLotus.repository.ContactRepositoryImpl
import com.sms.moLotus.repository.ConversationRepository
import com.sms.moLotus.repository.ConversationRepositoryImpl
import com.sms.moLotus.repository.MessageRepository
import com.sms.moLotus.repository.MessageRepositoryImpl
import com.sms.moLotus.repository.ScheduledMessageRepository
import com.sms.moLotus.repository.ScheduledMessageRepositoryImpl
import com.sms.moLotus.repository.SyncRepository
import com.sms.moLotus.repository.SyncRepositoryImpl
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(subcomponents = [
    ConversationInfoComponent::class,
    ThemePickerComponent::class])
class AppModule(private var application: Application) {

    @Provides
    @Singleton
    fun provideContext(): Context = application

    @Provides
    fun provideContentResolver(context: Context): ContentResolver = context.contentResolver

    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    @Provides
    @Singleton
    fun provideRxPreferences(preferences: SharedPreferences): RxSharedPreferences {
        return RxSharedPreferences.create(preferences)
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
    }

    @Provides
    fun provideViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory = factory

    // Listener

    @Provides
    fun provideContactAddedListener(listener: ContactAddedListenerImpl): ContactAddedListener = listener

    // Manager

    @Provides
    fun provideBillingManager(manager: BillingManagerImpl): BillingManager = manager

    @Provides
    fun provideActiveConversationManager(manager: ActiveConversationManagerImpl): ActiveConversationManager = manager

    @Provides
    fun provideAlarmManager(manager: AlarmManagerImpl): AlarmManager = manager

    @Provides
    fun provideAnalyticsManager(manager: AnalyticsManagerImpl): AnalyticsManager = manager

    @Provides
    fun blockingClient(manager: BlockingManager): BlockingClient = manager

    @Provides
    fun changelogManager(manager: ChangelogManagerImpl): ChangelogManager = manager

    @Provides
    fun provideKeyManager(manager: KeyManagerImpl): KeyManager = manager

    @Provides
    fun provideNotificationsManager(manager: NotificationManagerImpl): NotificationManager = manager

    @Provides
    fun providePermissionsManager(manager: PermissionManagerImpl): PermissionManager = manager

    @Provides
    fun provideRatingManager(manager: RatingManagerImpl): RatingManager = manager

    @Provides
    fun provideShortcutManager(manager: ShortcutManagerImpl): ShortcutManager = manager

    @Provides
    fun provideReferralManager(manager: ReferralManagerImpl): ReferralManager = manager

    @Provides
    fun provideWidgetManager(manager: WidgetManagerImpl): WidgetManager = manager

    // Mapper

    @Provides
    fun provideCursorToContact(mapper: CursorToContactImpl): CursorToContact = mapper

    @Provides
    fun provideCursorToContactGroup(mapper: CursorToContactGroupImpl): CursorToContactGroup = mapper

    @Provides
    fun provideCursorToContactGroupMember(mapper: CursorToContactGroupMemberImpl): CursorToContactGroupMember = mapper

    @Provides
    fun provideCursorToConversation(mapper: CursorToConversationImpl): CursorToConversation = mapper

    @Provides
    fun provideCursorToMessage(mapper: CursorToMessageImpl): CursorToMessage = mapper

    @Provides
    fun provideCursorToPart(mapper: CursorToPartImpl): CursorToPart = mapper

    @Provides
    fun provideCursorToRecipient(mapper: CursorToRecipientImpl): CursorToRecipient = mapper

    // Repository

    @Provides
    fun provideBackupRepository(repository: BackupRepositoryImpl): BackupRepository = repository

    @Provides
    fun provideBlockingRepository(repository: BlockingRepositoryImpl): BlockingRepository = repository

    @Provides
    fun provideContactRepository(repository: ContactRepositoryImpl): ContactRepository = repository

    @Provides
    fun provideConversationRepository(repository: ConversationRepositoryImpl): ConversationRepository = repository

    @Provides
    fun provideMessageRepository(repository: MessageRepositoryImpl): MessageRepository = repository

    @Provides
    fun provideScheduledMessagesRepository(repository: ScheduledMessageRepositoryImpl): ScheduledMessageRepository = repository

    @Provides
    fun provideSyncRepository(repository: SyncRepositoryImpl): SyncRepository = repository

}