package com.sms.moLotus.common

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.provider.FontRequest
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import com.google.firebase.FirebaseApp
import com.sms.moLotus.R
import com.sms.moLotus.common.util.CrashlyticsTree
import com.sms.moLotus.common.util.FileLoggingTree
import com.sms.moLotus.injection.AppComponentManager
import com.sms.moLotus.injection.appComponent
import com.sms.moLotus.manager.AnalyticsManager
import com.sms.moLotus.manager.BillingManager
import com.sms.moLotus.manager.ReferralManager
import com.sms.moLotus.migration.QkMigration
import com.sms.moLotus.migration.QkRealmMigration
import com.sms.moLotus.util.NightModeManager
import com.uber.rxdogtag.RxDogTag
import com.uber.rxdogtag.autodispose.AutoDisposeConfigurer
import dagger.android.*
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class QKApplication : Application(), HasActivityInjector, HasBroadcastReceiverInjector,
    HasServiceInjector {

    /**
     * Inject these so that they are forced to initialize
     */
    @Suppress("unused")
    @Inject
    lateinit var analyticsManager: AnalyticsManager

    @Suppress("unused")
    @Inject
    lateinit var qkMigration: QkMigration

    @Inject
    lateinit var billingManager: BillingManager

    @Inject
    lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var dispatchingBroadcastReceiverInjector: DispatchingAndroidInjector<BroadcastReceiver>

    @Inject
    lateinit var dispatchingServiceInjector: DispatchingAndroidInjector<Service>

    @Inject
    lateinit var fileLoggingTree: FileLoggingTree

    @Inject
    lateinit var nightModeManager: NightModeManager

    @Inject
    lateinit var realmMigration: QkRealmMigration

    @Inject
    lateinit var referralManager: ReferralManager


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        AppComponentManager.init(this)
        appComponent.inject(this)

        Realm.init(this)
        Realm.setDefaultConfiguration(
            RealmConfiguration.Builder()
                .compactOnLaunch()
                .migration(realmMigration)
                .schemaVersion(QkRealmMigration.SchemaVersion)
                .build()
        )

        qkMigration.performMigration()

        GlobalScope.launch(Dispatchers.IO) {
            referralManager.trackReferrer()
            billingManager.checkForPurchases()
            billingManager.queryProducts()
        }

        nightModeManager.updateCurrentTheme()

        val fontRequest = FontRequest(
            "com.google.android.gms.fonts",
            "com.google.android.gms",
            "Noto Color Emoji Compat",
            R.array.com_google_android_gms_fonts_certs
        )

        EmojiCompat.init(FontRequestEmojiCompatConfig(this, fontRequest))

        Timber.plant(Timber.DebugTree(), CrashlyticsTree(), fileLoggingTree)

        RxDogTag.builder()
            .configureWith(AutoDisposeConfigurer::configure)
            .install()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                extractLogToFileAndWeb()
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data =
                    Uri.parse(String.format("package:%s", applicationContext?.packageName))
                intent.flags = FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        } else {
            //if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
            extractLogToFileAndWeb()
            /* } else {
                 ActivityCompat.requestPermissions(
                     applicationContext as Activity,
                     arrayOf(
                         Manifest.permission.WRITE_EXTERNAL_STORAGE),
                     0
                 )
             }*/
        }

        Log.e("=========", "log file:: ${extractLogToFileAndWeb()}")
    }

    override fun activityInjector(): AndroidInjector<Activity> {
        return dispatchingActivityInjector
    }

    override fun broadcastReceiverInjector(): AndroidInjector<BroadcastReceiver> {
        return dispatchingBroadcastReceiverInjector
    }

    override fun serviceInjector(): AndroidInjector<Service> {
        return dispatchingServiceInjector
    }

    private fun extractLogToFileAndWeb(): File? {
        //set a file
        val datum = Date()
        val df = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val fullName: String = "mChatApp${df.format(datum)}.txt"
        val file = File(Environment.getExternalStorageDirectory(), fullName)

        //clears a file
        if (file.exists()) {
            file.delete()
        }
        try {
            Runtime.getRuntime().exec("logcat -c")
            Runtime.getRuntime().exec("logcat -f $file")
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file
    }
}