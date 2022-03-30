package com.sms.moLotus.feature.settings.autodownloadapk

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class UpdateApp : AsyncTask<String?, Void?, Void?>() {
    private var context: Context? = null
    private var outputFile: String? = null
    var progressDialog: ProgressDialog? = null

    fun setContext(contextf: Context?,progress: ProgressDialog?) {
        context = contextf
        progressDialog = progress
    }
    override fun onPreExecute() {
        progressDialog?.setMessage("Please Wait...")
        progressDialog?.setCancelable(false)
        progressDialog?.show()
    }
    override fun onPostExecute(aVoid: Void?) {
        Toast.makeText(context, "App Installed Successfully!!!!", Toast.LENGTH_LONG).show()
        Timber.e("outputFile:: ${outputFile}")
        progressDialog?.dismiss()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val fileUri =
                context?.let {
                    FileProvider.getUriForFile(
                        it, "${context?.packageName}.fileprovider",
                        File(outputFile)
                    )
                }
            val install = Intent(Intent.ACTION_VIEW)
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            install.data = fileUri
            context?.startActivity(install)
        } else {
            val uri = Uri.parse("file://$outputFile")

            val install = Intent(Intent.ACTION_VIEW)
            install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            install.setDataAndType(
                uri,
                "application/vnd.android.package-archive"
            )
            context?.startActivity(install)
        }
        Timber.e("Installing apk!!!!!!")

    }

    override fun doInBackground(vararg arg0: String?): Void? {
        Timber.e("do in background called:::")
        try {
            val myDir = File(Environment.getExternalStorageDirectory(), "MCHAT/Media")
            myDir.mkdirs()
            outputFile = File(myDir, "mChat.apk").absolutePath
            val url = URL(arg0[0])
            val c = url.openConnection() as HttpURLConnection
            c.requestMethod = "GET"
            c.doOutput = true
            c.connect()
            Timber.e("connected:::")

            Timber.e("outputFile:::$outputFile")
            if (File(outputFile).exists()) {
                File(outputFile).delete()
                Timber.e("deleteddddddd:::")
            }
            val fos = FileOutputStream(outputFile)
            val `is` = c.inputStream
            val buffer = ByteArray(1024)
            var len1 = 0
            while (`is`.read(buffer).also { len1 = it } != -1) {
                fos.write(buffer, 0, len1)
            }
            fos.close()
            `is`.close()

        } catch (e: Exception) {
            Timber.e("Update error!!!!! " + e.message)
            e.printStackTrace()
        }
        return null
    }
}