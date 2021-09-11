package com.sms.moLotus.feature.notificationprefs

import android.os.Build
import com.sms.moLotus.util.Preferences

data class NotificationPrefsState(
    val threadId: Long = 0,
    val conversationTitle: String = "",
    val notificationsEnabled: Boolean = true,
    val previewSummary: String = "",
    val previewId: Int = Preferences.NOTIFICATION_PREVIEWS_ALL,
    val wakeEnabled: Boolean = false,
    val action1Summary: String = "",
    val action2Summary: String = "",
    val action3Summary: String = "",
    val vibrationEnabled: Boolean = true,
    val ringtoneName: String = "",
    val qkReplyEnabled: Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.N,
    val qkReplyTapDismiss: Boolean = true
)
