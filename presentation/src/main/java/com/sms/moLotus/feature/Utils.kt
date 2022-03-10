package com.sms.moLotus.feature

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception

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
                    picDetail.put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
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

    fun getImageContentUri(file: File, context: Context): Uri? {
        val builder: StrictMode.VmPolicy.Builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val filePath = file.absolutePath
        val cursor: Cursor? = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Images.Media._ID),
            MediaStore.Images.Media.DATA + "=? ", arrayOf(filePath), null
        )
        return if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
            cursor.close()
            Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id)
        } else {
            if (file.exists()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver: ContentResolver = context.contentResolver
                    val picCollection = MediaStore.Images.Media
                        .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    val picDetail = ContentValues()
                    picDetail.put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
                    picDetail.put(MediaStore.Images.Media.MIME_TYPE, "images/jpg")
                    picDetail.put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_DCIM
                    )
                    picDetail.put(MediaStore.Images.Media.IS_PENDING, 0)
                    return resolver.insert(picCollection, picDetail)

                } else {
                    val values = ContentValues()
                    values.put(MediaStore.Images.Media.DATA, filePath)
                    values.put(MediaStore.Images.Media.MIME_TYPE, "images/jpg")
                    context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                    )
                }
            } else {
                null
            }
        }
    }

    fun compressImage(file: File, context: Context): Uri? {
        return try {
            // BitmapFactory options to downsize the image
            val o: BitmapFactory.Options = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            o.inSampleSize = 6
            // factor of downsizing the image
            var inputStream = FileInputStream(file)
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o)
            inputStream.close()

            // The new size we want to scale to
            val requiredSize = 75

            // Find the correct scale value. It should be the power of 2.
            var scale = 1
            while (o.outWidth / scale / 2 >= requiredSize &&
                o.outHeight / scale / 2 >= requiredSize
            ) {
                scale *= 2
            }
            val o2: BitmapFactory.Options = BitmapFactory.Options()
            o2.inSampleSize = scale
            inputStream = FileInputStream(file)
            val selectedBitmap: Bitmap? = BitmapFactory.decodeStream(inputStream, null, o2)
            inputStream.close()

            val compressedFile =
                File(Environment.getExternalStorageDirectory().absolutePath + "/" + Environment.DIRECTORY_PICTURES + "/imageCompress.jpg")
            if (compressedFile.exists()) {
                compressedFile.delete()
                compressedFile.createNewFile()
            } else {
                compressedFile.createNewFile()
            }
            val outputStream = FileOutputStream(compressedFile)
            selectedBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            getImageContentUri(compressedFile, context)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("======", "error ::: ${e.message}")
            getImageContentUri(file, context)
        }
    }

    fun getVideoDuration(uri: String, context: Context): Int {
        val mp: MediaPlayer = MediaPlayer.create(context, Uri.parse(uri))
        val duration: Int = mp.duration
        mp.release()
        return duration
    }
}