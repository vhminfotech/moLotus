package com.sms.moLotus.feature.compose

import android.content.ContentResolver
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.mms.ContentType
import com.sms.moLotus.injection.ViewModelKey
import com.sms.moLotus.model.Attachment
import com.sms.moLotus.model.Attachments
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.net.URLDecoder
import javax.inject.Named

@Module
class ComposeActivityModule {

    @Provides
    @Named("query")
    fun provideQuery(activity: ComposeActivity): String =
        activity.intent.extras?.getString("query") ?: ""

    @Provides
    @Named("threadId")
    fun provideThreadId(activity: ComposeActivity): Long =
        activity.intent.extras?.getLong("threadId") ?: 0L

    @Provides
    @Named("addresses")
    fun provideAddresses(activity: ComposeActivity): List<String> {
        return activity.intent
            ?.decodedDataString()
            ?.substringAfter(':') // Remove scheme
            ?.substringBefore("?") // Remove query
            ?.split(",", ";")
            ?.filter { number -> number.isNotEmpty() }
            ?: listOf()
    }

    @Provides
    @Named("text")
    fun provideSharedText(activity: ComposeActivity): String {
        var subject = activity.intent.getStringExtra(Intent.EXTRA_SUBJECT) ?: "";
        if (subject != "") {
            subject += "\n"
        }
        val sb = SpannableStringBuilder(subject)
        sb.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            subject.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return sb.append(activity.intent.extras?.getString(Intent.EXTRA_TEXT)
            ?: activity.intent.extras?.getString("sms_body")
            ?: activity.intent?.decodedDataString()
                ?.substringAfter('?') // Query string
                ?.takeIf { it.startsWith("body") }
                ?.substringAfter('=')
            ?: "").toString()
    }

    @Provides
    @Named("subject")
    fun provideSubjectText(activity: ComposeActivity): String {
        var subject = activity.intent.getStringExtra(Intent.EXTRA_SUBJECT) ?: "";
        if (subject != "") {
            subject += "\n"
        }
        val sb = SpannableStringBuilder(subject)
        sb.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            subject.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return sb.toString()
    }

    @Provides
    @Named("attachments")
    fun provideSharedAttachments(activity: ComposeActivity): Attachments {
        val uris = mutableListOf<Uri>()
        // activity.intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.run(uris::add)
        activity.intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.run(uris::addAll)


        return Attachments(uris.map { uri ->
            Log.e("==========", "uris:: $uri")

            val mimeType = activity.contentResolver.getType(uri)
            Log.e("==========", "mimeType:: $mimeType")

            when {
                ContentType.isImageType(mimeType) -> {
                    Attachment.Image(uri)
                }

                ContentType.isVideoType(mimeType) -> {
                    Attachment.Image(uri)
                }

                ContentType.AUDIO_AAC.equals(mimeType, true) || ContentType.AUDIO_MP3.equals(
                    mimeType,
                    true
                ) -> {
                    Attachment.Image(uri)
                }

                ContentType.TEXT_VCARD.equals(mimeType, true) -> {
                    /*val inputStream = activity.contentResolver.openInputStream(uri)
                    val text = inputStream?.reader(Charset.forName("utf-8"))?.readText()
                    text?.let(Attachment::Contact)*/

                    val cr: ContentResolver = activity.contentResolver
                    var stream: InputStream? = null
                    try {
                        stream = cr.openInputStream(uri)
                    } catch (e: FileNotFoundException) {
                        // TODO Auto-generated catch block
                        e.printStackTrace()
                    }

                    val fileContent = StringBuffer("")
                    var ch: Int = 0
                    try {
                        while (stream?.read()
                                .also {
                                    if (it != null) {
                                        ch = it
                                    }
                                } != -1
                        ) fileContent.append(ch.toChar())
                    } catch (e: IOException) {
                        // TODO Auto-generated catch block
                        e.printStackTrace()
                    }
                    val data = String(fileContent)
                    Log.e("==============", "data: $data")
                    data?.let(Attachment::Contact)
                }

                else -> Attachment.Image(uri)
            }
        })
    }

    @Provides
    @IntoMap
    @ViewModelKey(ComposeViewModel::class)
    fun provideComposeViewModel(viewModel: ComposeViewModel): ViewModel = viewModel

    // The dialer app on Oreo sends a URL encoded string, make sure to decode it
    private fun Intent.decodedDataString(): String? {
        val data = data?.toString()
        if (data?.contains('%') == true) {
            return URLDecoder.decode(data, "UTF-8")
        }
        return data
    }

}