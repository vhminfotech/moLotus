package com.sms.moLotus.util

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class VideoCompressor internal constructor(
    private val context: Context,
    private val uri: Uri) {
    var newUri: Uri? = null
    var isComplete: Boolean? = false

    fun compress(): Uri? {
        GlobalScope.launch(Dispatchers.IO) {

            VideoCompressor.start(
                context = context, // => This is required
                uris = listOf(uri), // => Source can be provided as content uris
                isStreamable = true,
                saveAt = Environment.DIRECTORY_MOVIES + "/VideoCompress", // => the directory to save the compressed video(s)
                listener = object : CompressionListener {
                    override fun onProgress(index: Int, percent: Float) {
                        // Update UI with progress value
                        Log.e("ImageUtils", "percent:: $percent")
                    }

                    override fun onStart(index: Int) {
                        // Compression start
                        Log.e("ImageUtils", "onStart:: $index")

                    }

                    override fun onSuccess(
                        index: Int,
                        size: Long,
                        path: String?
                    ) {
                        // On Compression success
                        Log.e("ImageUtils", "onSuccess:: $size:: path:: $path")

                        isComplete = true
                        MediaScannerConnection.scanFile(
                            context, arrayOf<String>(File(path).absolutePath), null
                        ) { _, uri ->
                            Log.e("ImageUtils", uri.path)
                            newUri = uri
                        }
                    }

                    override fun onFailure(index: Int, failureMessage: String) {
                        // On Failure
                        Log.e("ImageUtils", "failureMessage:: $failureMessage")

                    }

                    override fun onCancelled(index: Int) {
                        // On Cancelled
                        Log.e("ImageUtils", "onCancelled:")

                    }

                },
                configureWith = Configuration(
                    quality = VideoQuality.MEDIUM,
                    frameRate = 24, /*Int, ignore, or null*/
                    isMinBitrateCheckEnabled = false,
                    videoBitrate = 3677198, /*Int, ignore, or null*/
                    disableAudio = false, /*Boolean, or ignore*/
                    keepOriginalResolution = false, /*Boolean, or ignore*/
                    videoWidth = 360.0, /*Double, ignore, or null*/
                    videoHeight = 480.0 /*Double, ignore, or null*/
                )
            )
        }
        return newUri
    }

    fun getContentUri(file: File, context: Context): Uri? {
        var getContentUri: Uri? = null
        MediaScannerConnection.scanFile(
            context, arrayOf<String>(file.absolutePath), null
        ) { _, uri ->
            Log.e("ImageUtils", uri.path)
            getContentUri = uri
        }

        return getContentUri
    }
}