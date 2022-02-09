/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.sms.moLotus.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.R.attr.path
import java.io.*


object ImageUtils {

    fun getScaledGif(context: Context, uri: Uri, maxWidth: Int, maxHeight: Int, quality: Int = 90): ByteArray {
        val gif = GlideApp
                .with(context)
                .asGif()
                .load(uri)
                .centerInside()
                .encodeQuality(quality)
                .submit(maxWidth, maxHeight)
                .get()

        val outputStream = ByteArrayOutputStream()
        GifEncoder(context, GlideApp.get(context).bitmapPool).encodeTransformedToStream(gif, outputStream)
        return outputStream.toByteArray()
    }



    fun getScaledVideo(context: Context, uri: Uri, maxWidth: Int, maxHeight: Int, quality: Int = 90): ByteArray {
        /*Transcoder.into("")
            .addDataSource(context, uri)
            .setListener(object: TranscoderListener {
                override fun onTranscodeProgress(progress: Double) {
                    Log.e("ImageUtils","progress::: $progress")
                }

                override fun onTranscodeCompleted(successCode: Int) {
                    Log.e("ImageUtils","successCode::: $successCode")

                }

                override fun onTranscodeCanceled() {
                    Log.e("ImageUtils","onTranscodeCanceled")
                }

                override fun onTranscodeFailed(exception: Throwable) {
                    Log.e("ImageUtils","exception:: ${exception.message}")

                }
            }).transcode()*/
        /*val file: File = File(path)
        val size = file.length().toInt()
        val bytes = ByteArray(size)
        val buf = BufferedInputStream(FileInputStream(file))
        buf.read(bytes, 0, bytes.size)
        buf.close()*/

        val iStream: InputStream? = context.contentResolver.openInputStream(uri)
        val byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)

        var len = 0
        while (iStream?.read(buffer).also {
                if (it != null) {
                    len = it
                }
            } != -1) {
            byteBuffer.write(buffer, 0, len)
        }

        Log.e("ImageUtils","uri:: $uri==== byteBuffer:: $byteBuffer")

       // GifEncoder(context, GlideApp.get(context).bitmapPool).encodeTransformedToStream(gif, outputStream)
        return byteBuffer.toByteArray()
    }

    fun getScaledImage(context: Context, uri: Uri, maxWidth: Int, maxHeight: Int, quality: Int = 90): ByteArray {
        return GlideApp
                .with(context)
                .`as`(ByteArray::class.java)
                .load(uri)
                .centerInside()
                .encodeQuality(quality)
                .submit(maxWidth, maxHeight)
                .get()
    }

}