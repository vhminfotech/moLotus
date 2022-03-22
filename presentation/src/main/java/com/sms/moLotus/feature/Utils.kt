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
import java.io.IOException
import java.lang.Exception
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*

object Utils {

    fun getVideoContentUri(context: Context, absPath: String): Uri? {
        Log.e("=============", "getImageContentUri: $absPath")
        val cursor: Cursor? = context.getContentResolver().query(
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

    fun getAudioContentUri(context: Context, absPath: String): Uri? {
        val cursor: Cursor? = context.getContentResolver().query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Media._ID),
            MediaStore.Audio.Media.DATA + "=? ",
            arrayOf(absPath),
            null
        )
        return if (cursor != null && cursor.moveToFirst()) {
            val id: Int = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
            Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toString())
        } else if (absPath.isNotEmpty()) {
            val values = ContentValues()
            values.put(MediaStore.Audio.Media.DATA, absPath)
            values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/aac")

            context.contentResolver.insert(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values
            )
        } else {
            null
        }
    }




    fun getVideoContentUri1(file: File, context: Context): Uri? {
        val builder: StrictMode.VmPolicy.Builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val filePath = file.absolutePath
        val cursor: Cursor? =
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, arrayOf(
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.SIZE,
                    MediaStore.Video.Media.DATE_TAKEN,
                    MediaStore.Video.Media._ID,
                    MediaStore.MediaColumns.DATA
                ),
                MediaStore.Video.Media.DATA + "=? ", arrayOf(filePath), null
            )
        return if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
            cursor.close()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Uri.withAppendedPath(
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                    "" + id
                )
            } else {
                Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "" + id)
            }
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
                        Environment.DIRECTORY_DCIM
                    )
                    picDetail.put(MediaStore.Video.Media.IS_PENDING, 0)
                    return resolver.insert(picCollection, picDetail)
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
       /* val filePath = imageFile.absolutePath
        val cursor = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Video.Media._ID),
            MediaStore.Video.Media.DATA + "=? ", arrayOf(filePath), null
        )
        return if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
            cursor.close()
            Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "" + id)
        } else {
            if (imageFile.exists()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = context.contentResolver
                    val picCollection = MediaStore.Video.Media
                        .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    val picDetail = ContentValues()
                    picDetail.put(MediaStore.Video.Media.DISPLAY_NAME, imageFile.name)
                    picDetail.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    picDetail.put(
                        MediaStore.Video.Media.RELATIVE_PATH,
                        "Movies/" + UUID.randomUUID().toString() + ".mp4"
                    )
                    picDetail.put(MediaStore.Video.Media.IS_PENDING, 1)
                    val finaluri = resolver.insert(picCollection, picDetail)
                    picDetail.clear()
                    picDetail.put(MediaStore.Video.Media.IS_PENDING, 0)
                    resolver.update(picCollection, picDetail, null, null)
                    finaluri
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
        }*/
    }
    /*fun getVideoContentUri(file: File, context: Context): Uri? {
        *//*val filePath = file.absolutePath
        val cursor: Cursor? = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Video.Media._ID),
            MediaStore.Video.Media.DATA + "=? ", arrayOf(filePath), null
        )
        return if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
            cursor.close()
            Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "" + id)
        } else {*//*
        val builder: StrictMode.VmPolicy.Builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val filePath = file.absolutePath
        val cursor: Cursor? =
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, arrayOf(
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.SIZE,
                    MediaStore.Video.Media.DATE_TAKEN,
                    MediaStore.Video.Media._ID,
                    MediaStore.MediaColumns.DATA
                ),
                MediaStore.Video.Media.DATA + "=? ", arrayOf(filePath), null
            )
        return if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
            cursor.close()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Uri.withAppendedPath(
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                    "" + id
                )
            } else {
                Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "" + id)
            }
        } else {
            if (file.exists()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    *//* val resolver: ContentResolver = context.contentResolver
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
                     finalUri*//*
                    val resolver: ContentResolver = context.contentResolver
                    val picCollection = MediaStore.Video.Media
                        .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    val picDetail = ContentValues()
                    picDetail.put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
                    picDetail.put(MediaStore.Video.Media.TITLE, file.name)
                    picDetail.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    picDetail.put(
                        MediaStore.Video.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_MOVIES
                    )
                    picDetail.put(MediaStore.Video.Media.IS_PENDING, 1)
                    val uri = resolver.insert(picCollection, picDetail)
                    Log.e("===========", "uri:before:: $uri")

                    picDetail.clear()
                    picDetail.put(MediaStore.Video.Media.IS_PENDING, 0)
                    resolver.update(picCollection, picDetail, null, null);
                    Log.e("===========", "uri::: $uri")

                    return uri
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
    }*/

    fun getAudioContentUri1(file: File, context: Context): Uri? {
        /* val builder: StrictMode.VmPolicy.Builder = StrictMode.VmPolicy.Builder()
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
         } else {*/
        val builder: StrictMode.VmPolicy.Builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val filePath = file.absolutePath
        val cursor: Cursor? =
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, arrayOf(
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media._ID,
                    MediaStore.MediaColumns.DATA
                ),
                MediaStore.Audio.Media.DATA + "=? ", arrayOf(filePath), null
            )
        return if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
            cursor.close()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Uri.withAppendedPath(
                    MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                    "" + id
                )
            } else {
                Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + id)
            }
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
                    Log.e(
                        "===========",
                        "audio uri::: ${resolver.insert(picCollection, picDetail)}"
                    )
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
        val cursor: Cursor? =
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, arrayOf(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.SIZE,
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.Media._ID,
                    MediaStore.MediaColumns.DATA
                ),
                MediaStore.Images.Media.DATA + "=? ", arrayOf(filePath), null
            )
        return if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
            cursor.close()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Uri.withAppendedPath(
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                    "" + id
                )
            } else {
                Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id)
            }
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
                File(
                    context.externalCacheDir?.absolutePath + "/" + SimpleDateFormat(
                        "yyyyMMdd_HHmmss",
                        Locale.getDefault()
                    ).format(
                        Date()
                    ) + ".jpg"
                )

            if (compressedFile.exists()) {
                compressedFile.delete()
                compressedFile.createNewFile()
                val outputStream = FileOutputStream(compressedFile)
                selectedBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            } else {
                compressedFile.createNewFile()
                val outputStream = FileOutputStream(compressedFile)
                selectedBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }

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

    fun copyFileOrDirectory(srcDir: String?, dstDir: String?) {
        try {
            val src = File(srcDir)
            val dst = File(dstDir, src.nameWithoutExtension +".aac")
            if (src.isDirectory) {
                val files = src.list()
                val filesLength = files.size
                for (i in 0 until filesLength) {
                    val src1 = File(src, files[i]).path
                    val dst1 = dst.path
                    copyFileOrDirectory(src1, dst1)
                }
            } else {
                copyFile(src, dst)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    fun copyFile(sourceFile: File?, destFile: File) : File{
        if (!destFile.parentFile.exists()) destFile.parentFile.mkdirs()
        if (!destFile.exists()) {
            destFile.createNewFile()
        }
        var source: FileChannel? = null
        var destination: FileChannel? = null
        try {
            source = FileInputStream(sourceFile).channel
            destination = FileOutputStream(destFile).channel
            destination.transferFrom(source, 0, source.size())
        } finally {
            source?.close()
            destination?.close()
        }
       return destFile
    }
}