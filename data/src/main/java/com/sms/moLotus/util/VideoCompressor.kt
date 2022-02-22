package com.sms.moLotus.util

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import kotlinx.coroutines.runBlocking
import java.io.File

object VideoCompressor {

    var isComplete: Boolean? = false
    var newUri: Uri ?=null

    fun compress(context: Context, uri: Uri): Uri? = runBlocking {

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
                    newUri = getContentUri(File(path), context)!!

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
                quality = VideoQuality.MEDIUM,
                frameRate = 24, /*Int, ignore, or null*/
                isMinBitrateCheckEnabled = false,
                videoBitrate = 3677198, /*Int, ignore, or null*/
                disableAudio = false, /*Boolean, or ignore*/
                keepOriginalResolution = false, /*Boolean, or ignore*/
                null, /*Double, ignore, or null*/
                null /*Double, ignore, or null*/
            )
        )


        newUri
    }

    fun getContentUri(file: File, context: Context): Uri? {
        val filePath = file.absolutePath
        val cursor: Cursor? = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Video.Media._ID),
            MediaStore.Video.Media.DATA + "=? ", arrayOf(filePath), null
        )
        return if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
            cursor.close()
            Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "" + id)
        } else {
            if (file.exists()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver: ContentResolver = context.contentResolver
                    val picCollection = MediaStore.Video.Media
                        .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    val picDetail = ContentValues()
                    picDetail.put(MediaStore.Video.Media.DISPLAY_NAME, file?.name)
                    picDetail.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    picDetail.put(
                        MediaStore.Video.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_MOVIES
                    )
                    picDetail.put(MediaStore.Video.Media.IS_PENDING, 0)
                    val finalUri: Uri? = resolver.insert(picCollection, picDetail)
                    finalUri

                } else {
                    val values = ContentValues()
                    values.put(MediaStore.Video.Media.DATA, filePath)
                    context.contentResolver.insert(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values
                    )
                }
            } else {
                null
            }
        }
    }
}