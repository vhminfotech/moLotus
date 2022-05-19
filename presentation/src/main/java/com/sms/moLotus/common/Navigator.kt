package com.sms.moLotus.common

import android.app.Activity
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.provider.Telephony
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import com.sms.moLotus.feature.backup.BackupActivity
import com.sms.moLotus.feature.blocking.BlockingActivity
import com.sms.moLotus.feature.compose.ComposeActivity
import com.sms.moLotus.feature.conversationinfo.ConversationInfoActivity
import com.sms.moLotus.feature.gallery.GalleryActivity
import com.sms.moLotus.feature.intro.IntroActivity2
import com.sms.moLotus.feature.notificationprefs.NotificationPrefsActivity
import com.sms.moLotus.feature.plus.PlusActivity
import com.sms.moLotus.feature.scheduled.ScheduledActivity
import com.sms.moLotus.feature.settings.AppSettingsActivity
import com.sms.moLotus.feature.settings.SettingsActivity
import com.sms.moLotus.manager.AnalyticsManager
import com.sms.moLotus.manager.BillingManager
import com.sms.moLotus.manager.NotificationManager
import com.sms.moLotus.manager.PermissionManager
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton
import com.sms.moLotus.BuildConfig


@Singleton
class Navigator @Inject constructor(
    private val context: Context,
    private val analyticsManager: AnalyticsManager,
    private val billingManager: BillingManager,
    private val notificationManager: NotificationManager,
    private val permissions: PermissionManager
) {

    private fun startActivity(intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun startActivityExternal(intent: Intent) {
        if (intent.resolveActivity(context.packageManager) != null) {
            startActivity(intent)
        } else {
            startActivity(Intent.createChooser(intent, null))
        }
    }

    /**
     * @param source String to indicate where this QKSMS+ screen was launched from. This should be
     * one of [main_menu, compose_schedule, settings_night, settings_theme]
     */
    fun showQksmsPlusActivity(source: String) {
        analyticsManager.track("Viewed QKSMS+", Pair("source", source))
        val intent = Intent(context, PlusActivity::class.java)
        startActivity(intent)
    }

    /**
     * This won't work unless we use startActivityForResult
     */
    fun showDefaultSmsDialog(context: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java) as RoleManager
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
            context.startActivityForResult(intent, 42389)
        } else {
            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
            context.startActivity(intent)
        }
    }

    fun showCompose(sub: String? = null, body: String? = null, images: List<Uri>? = null) {
        val intent = Intent(context, ComposeActivity::class.java)
        intent.putExtra(Intent.EXTRA_TEXT, body)
        intent.putExtra(Intent.EXTRA_SUBJECT, sub)
        Timber.e("==========", "images:: $images")
        images?.takeIf { it.isNotEmpty() }?.let {
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(images))
        }
        startActivity(intent)
    }

    fun shareToOtherApps(body: String? = null) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(Intent.EXTRA_TEXT, body)
        }
        startActivity(Intent.createChooser(shareIntent, "Share"))
    }

    fun sendVideoOrAudio(body: String? = null, images: List<Uri>? = null) {
        val sendIntent = Intent(context, ComposeActivity::class.java)
        sendIntent.putExtra(Intent.EXTRA_TEXT, body)
        sendIntent.putExtra(Intent.EXTRA_STREAM, images.toString())
        sendIntent.type = "video/mp4"
        startActivity(sendIntent)
    }

    fun getRealPathFromURI(context: Context, contentUri: Uri?): String? {
        var cursor: Cursor? = null
        return try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri!!, proj, null, null, null)
            val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } finally {
            cursor?.close()
        }
    }

    private fun onClickApp(pack: String?, bitmap: Bitmap) {
        val pm: PackageManager = context.packageManager
        try {
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val path: String =
                MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
            val imageUri = Uri.parse(path)
            val info: PackageInfo? =
                pack?.let { pm.getPackageInfo(it, PackageManager.GET_META_DATA) }
            val waIntent = Intent(Intent.ACTION_SEND)
            waIntent.type = "image/jpg"
            waIntent.setPackage(pack)
            waIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
            waIntent.putExtra(Intent.EXTRA_TEXT, pack)
            context.startActivity(Intent.createChooser(waIntent, "Share with"))
        } catch (e: Exception) {
            Log.e("Error on sharing", "$e ")
            Toast.makeText(context, "App not Installed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getMmsImage(_id: String): Bitmap? {
        val partURI = Uri.parse("content://mms/part/$_id")
        var `is`: InputStream? = null
        var bitmap: Bitmap? = null
        try {
            `is` = context.contentResolver.openInputStream(partURI)
            bitmap = BitmapFactory.decodeStream(`is`)
        } catch (e: IOException) {
        } finally {
            if (`is` != null) {
                try {
                    `is`.close()
                } catch (e: IOException) {
                }
            }
        }
        return bitmap
    }

    fun showConversation(threadId: Long, query: String? = null) {
        val intent = Intent(context, ComposeActivity::class.java)
            .putExtra("threadId", threadId)
            .putExtra("query", query)
        startActivity(intent)
    }

    fun showConversationInfo(threadId: Long) {
        val intent = Intent(context, ConversationInfoActivity::class.java)
        intent.putExtra("threadId", threadId)
        startActivity(intent)
    }

    fun showMedia(partId: Long) {
        val intent = Intent(context, GalleryActivity::class.java)
        intent.putExtra("partId", partId)
        startActivity(intent)
    }

    fun showAudioMedia(uri: Uri) {
        val viewMediaIntent = Intent()
        viewMediaIntent.action = Intent.ACTION_VIEW
        viewMediaIntent.setDataAndType(uri, "audio/aac")
        viewMediaIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(viewMediaIntent)
    }

    fun showBackup() {
        analyticsManager.track("Viewed Backup")
        startActivity(Intent(context, BackupActivity::class.java))
    }

    fun showAPNsetting() {
        analyticsManager.track("Viewed APN Setting")
        startActivity(Intent(context, IntroActivity2::class.java))
    }

    fun showAppsetting() {
        analyticsManager.track("Viewed Settings")
        startActivity(Intent(context, AppSettingsActivity::class.java))
    }

    fun showScheduled() {
        analyticsManager.track("Viewed Scheduled")
        val intent = Intent(context, ScheduledActivity::class.java)
        startActivity(intent)
    }

    fun showSettings() {
        val intent = Intent(context, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun showDeveloper() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/moezbhatti"))
        startActivityExternal(intent)
    }

    fun showSourceCode() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/moezbhatti/qksms"))
        startActivityExternal(intent)
    }

    fun showChangelog() {
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/moezbhatti/qksms/releases"))
        startActivityExternal(intent)
    }

    fun showLicense() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://github.com/moezbhatti/qksms/blob/master/LICENSE")
        )
        startActivityExternal(intent)
    }

    fun showBlockedConversations() {
        val intent = Intent(context, BlockingActivity::class.java)
        startActivity(intent)
    }

    fun makePhoneCall(address: String) {
        val action = if (permissions.hasCalling()) Intent.ACTION_CALL else Intent.ACTION_DIAL
        val intent = Intent(action, Uri.parse("tel:$address"))
        startActivityExternal(intent)
    }

    fun showDonation() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://bit.ly/QKSMSDonation"))
        startActivityExternal(intent)
    }

    fun showRating() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.moez.QKSMS"))
            .addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY
                        or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                        or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            )

        try {
            startActivityExternal(intent)
        } catch (e: ActivityNotFoundException) {
            val url = "http://play.google.com/store/apps/details?id=com.moez.QKSMS"
            startActivityExternal(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    /**
     * Launch the Play Store and display the Call Control listing
     */
    fun installCallControl() {
        val url =
            "https://play.google.com/store/apps/details?id=com.flexaspect.android.everycallcontrol"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivityExternal(intent)
    }

    /**
     * Launch the Play Store and display the Should I Answer? listing
     */
    fun installSia() {
        val url = "https://play.google.com/store/apps/details?id=org.mistergroup.shouldianswer"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivityExternal(intent)
    }

    fun showSupport() {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("moez@qklabs.com"))
        intent.putExtra(Intent.EXTRA_SUBJECT, "QKSMS Support")
        intent.putExtra(Intent.EXTRA_TEXT, StringBuilder("\n\n")
            .append("\n\n--- Please write your message above this line ---\n\n")
            .append("Package: ${context.packageName}\n")
            .append("Version: ${BuildConfig.VERSION_NAME}\n")
            .append("Device: ${Build.BRAND} ${Build.MODEL}\n")
            .append("SDK: ${Build.VERSION.SDK_INT}\n")
            .append("Upgraded"
                .takeIf { BuildConfig.FLAVOR != "noAnalytics" }
                .takeIf { billingManager.upgradeStatus.blockingFirst() } ?: "")
            .toString())
        startActivityExternal(intent)
    }

    fun showInvite() {
        analyticsManager.track("Clicked Invite")
        Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, "http://qklabs.com/download")
            .let { Intent.createChooser(it, null) }
            .let(::startActivityExternal)
    }

    fun addContact(address: String) {
        val intent = Intent(Intent.ACTION_INSERT)
            .setType(ContactsContract.Contacts.CONTENT_TYPE)
            .putExtra(ContactsContract.Intents.Insert.PHONE, address)

        startActivityExternal(intent)
    }

    fun showContact(lookupKey: String) {
        val intent = Intent(Intent.ACTION_VIEW)
            .setData(Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey))

        startActivityExternal(intent)
    }

    fun viewFile(file: File) {
        val data = FileProvider.getUriForFile(context, "com.sms.moLotus.fileprovider", file)
        val type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.name.split(".").last())
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(data, type)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        startActivityExternal(intent)
    }

    fun shareFile(body: String?, file: File) {
        val data = FileProvider.getUriForFile(
            context, context.packageName + ".fileprovider", file
        )
//        val type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.name.split(".").last())
        /* val intent = Intent(Intent.ACTION_SEND)
             .setType(type)
             .putExtra(Intent.EXTRA_STREAM, data)
             .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)*/
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.name.split(".").last())
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            flags = Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(Intent.EXTRA_TEXT, body)
            putExtra(Intent.EXTRA_STREAM, data)
        }
        startActivity(Intent.createChooser(shareIntent, "Share"))
        //startActivityExternal(intent)
    }

    fun showNotificationSettings(threadId: Long = 0) {
        val intent = Intent(context, NotificationPrefsActivity::class.java)
        intent.putExtra("threadId", threadId)
        startActivity(intent)
    }

    fun showNotificationChannel(threadId: Long = 0) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (threadId != 0L) {
                notificationManager.createNotificationChannel(threadId)
            }

            val channelId = notificationManager.buildNotificationChannelId(threadId)
            val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            startActivity(intent)
        }
    }

}
