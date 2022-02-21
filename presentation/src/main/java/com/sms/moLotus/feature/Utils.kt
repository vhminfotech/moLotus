package com.sms.moLotus.feature

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import java.io.File

object Utils {
    fun getVideoContentUri(file: File, context: Context): Uri? {
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
                    values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    context.contentResolver.insert(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values
                    )
                }
            } else {
                null
            }
        }
    }

    fun getAudioContentUri(file: File, context: Context): Uri? {
        val builder: StrictMode.VmPolicy.Builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val filePath = file.absolutePath
        val cursor: Cursor? = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Audio.Media._ID),
            MediaStore.Audio.Media.DATA + "=? ", arrayOf(filePath), null
        )
        return if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
            cursor.close()
            Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + id)
        } else {
            if (file.exists()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver: ContentResolver = context.contentResolver
                    val picCollection = MediaStore.Audio.Media
                        .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    val picDetail = ContentValues()
                    picDetail.put(MediaStore.Audio.Media.DISPLAY_NAME, file.name)
                    picDetail.put(MediaStore.Audio.Media.IS_ALARM, true)
                    picDetail.put(MediaStore.Audio.Media.MIME_TYPE, "audio/aac")
                    picDetail.put(
                        MediaStore.Audio.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_ALARMS
                    )
                    picDetail.put(MediaStore.Audio.Media.IS_PENDING, 0)
                    //val uri = resolver.insert(picCollection, picDetail)
                    //Log.e("===========", "uri::: $uri")
                    return resolver.insert(picCollection, picDetail)

                } else {
                    val values = ContentValues()
                    values.put(MediaStore.Audio.Media.DATA, filePath)
                    values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/aac")
                    context.contentResolver.insert(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values
                    )
                }
            } else {
                null
            }
        }
    }

    fun getVideoDuration(uri: String, context: Context): Int {
        val mp: MediaPlayer = MediaPlayer.create(context, Uri.parse(uri))
        val duration: Int = mp.duration
        mp.release()
        return duration
    }
}