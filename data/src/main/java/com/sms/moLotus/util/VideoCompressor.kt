package com.sms.moLotus.util

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import kotlinx.coroutines.runBlocking

object VideoCompressor {
    var isComplete: Boolean? = false
    var newUri: Uri ?=null
    fun compress(context: Context, uri: Uri): Uri? = runBlocking {
        VideoCompressor.start(
            context = context, // => This is required
            uris = listOf(uri), // => Source can be provided as content uris
            isStreamable = true,
            saveAt = context.externalCacheDir?.absolutePath /*Environment.DIRECTORY_MOVIES + "/VideoCompress"*/, // => the directory to save the compressed video(s)
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
                    newUri = path?.let { getVideoContentUri( context, it) }
                    Log.e("ImageUtils", "onSuccess:: $newUri")
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
                quality = VideoQuality.LOW,
                frameRate = 24, /*Int, ignore, or null*/
                isMinBitrateCheckEnabled = false,
                videoBitrate = 160000, /*Int, ignore, or null*/
                disableAudio = false, /*Boolean, or ignore*/
                keepOriginalResolution = false, /*Boolean, or ignore*/
                null, /*Double, ignore, or null*/
                null /*Double, ignore, or null*/
            )
        )
        newUri
    }

    fun getVideoContentUri(context: Context, absPath: String): Uri? {
        val cursor: Cursor? = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Video.Media._ID),
            MediaStore.Video.Media.DATA + "=? ",
            arrayOf(absPath),
            null
        )
        return if (cursor != null && cursor.moveToFirst()) {
            val id: Int = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
            Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id.toString())
        } else if (absPath.isNotEmpty()) {
            val values = ContentValues()
            values.put(MediaStore.Video.Media.DATA, absPath)
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            context.contentResolver.insert(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values
            )
        } else {
            null
        }
    }
}