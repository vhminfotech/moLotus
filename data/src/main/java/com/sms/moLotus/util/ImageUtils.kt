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

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Log
import kotlinx.coroutines.*
import java.io.*

object ImageUtils {

    fun getScaledGif(
        context: Context,
        uri: Uri,
        maxWidth: Int,
        maxHeight: Int,
        quality: Int = 90
    ): ByteArray {
        val gif = GlideApp
            .with(context)
            .asGif()
            .load(uri)
            .centerInside()
            .encodeQuality(quality)
            .submit(maxWidth, maxHeight)
            .get()

        val outputStream = ByteArrayOutputStream()
        GifEncoder(context, GlideApp.get(context).bitmapPool).encodeTransformedToStream(
            gif,
            outputStream
        )
        return outputStream.toByteArray()
    }

    fun getScaledVideo(
        context: Context,
        uri: Uri
    ): ByteArray = runBlocking {
        val baos = ByteArrayOutputStream()
        var fis: InputStream? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                fis = context.contentResolver.openAssetFileDescriptor(uri, "r")?.createInputStream()
            } else {
                fis = context.contentResolver.openInputStream(uri)
            }
            val bufferSize = 1024
            val buffer = ByteArray(bufferSize)
            var len = 0
            while (fis?.read(buffer).also {
                    if (it != null) {
                        len = it
                    }
                } != -1) {
                baos.write(buffer, 0, len)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ImageUtils", "error::: ${e.message}")

        }
        return@runBlocking baos.toByteArray()
    }

    @Throws(IOException::class)
    fun readBytes(inputStream: InputStream): ByteArray? {
        // this dynamically extends to take the bytes you read
        val byteBuffer = ByteArrayOutputStream()

        // this is storage overwritten on each iteration with bytes
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)

        // we need to know how may bytes were read to write them to the byteBuffer
        var len = 0
        while (inputStream.read(buffer).also { len = it } != -1) {
            byteBuffer.write(buffer, 0, len)
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray()
    }


    fun getAudio(
        context: Context,
        uri: Uri
    ): ByteArray = runBlocking {
        val baos = ByteArrayOutputStream()
        var fis: InputStream? = null
        try {
            fis = context.contentResolver.openInputStream(uri)
            val bufferSize = 1024
            val buffer = ByteArray(bufferSize)
            var len = 0
            while (fis?.read(buffer).also {
                    if (it != null) {
                        len = it
                    }
                } != -1) {
                baos.write(buffer, 0, len)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ImageUtils", "error::: ${e.message}")

        }
        val bbytes = baos.toByteArray()
        Log.e("ImageUtils", "byteBuffer ::$bbytes")

        return@runBlocking bbytes
    }

    fun getFile(
        context: Context,
        uri: Uri
    ): ByteArray = runBlocking {
        val baos = ByteArrayOutputStream()
        var fis: InputStream? = null
        try {
            fis = context.contentResolver.openInputStream(uri)
            val bufferSize = 1024
            val buffer = ByteArray(bufferSize)
            var len = 0
            while (fis?.read(buffer).also {
                    if (it != null) {
                        len = it
                    }
                } != -1) {
                baos.write(buffer, 0, len)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ImageUtils", "error::: ${e.message}")

        }
        val bbytes = baos.toByteArray()
        Log.e("ImageUtils", "byteBuffer ::$bbytes")

        return@runBlocking bbytes
    }

    fun readData(callback: (ByteArray) -> Unit, uri: Uri, context: Context) {
        val byteBuffer = ByteArrayOutputStream()
        GlobalScope.async {
            val iStream: InputStream? = uri.let { context.contentResolver.openInputStream(it) }
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
            callback(byteBuffer.toByteArray())
        }
    }

    fun getScaledImage(
        context: Context,
        uri: Uri,
        maxWidth: Int,
        maxHeight: Int,
        quality: Int = 90
    ): ByteArray {
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