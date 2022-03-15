package com.sms.moLotus.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.sms.moLotus.interactor.MarkDelivered
import com.sms.moLotus.interactor.MarkDeliveryFailed
import dagger.android.AndroidInjection
import javax.inject.Inject

class SmsDeliveredReceiver : BroadcastReceiver() {

    @Inject lateinit var markDelivered: MarkDelivered
    @Inject lateinit var markDeliveryFailed: MarkDeliveryFailed

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)

        val id = intent.getLongExtra("id", 0L)

        when (resultCode) {
            // TODO notify about delivery
            Activity.RESULT_OK -> {
                val pendingResult = goAsync()
                markDelivered.execute(id) { pendingResult.finish() }
            }

            // TODO notify about delivery failure
            Activity.RESULT_CANCELED -> {
                val pendingResult = goAsync()
                markDeliveryFailed.execute(MarkDeliveryFailed.Params(id, resultCode)) { pendingResult.finish() }
            }
        }
    }

}
