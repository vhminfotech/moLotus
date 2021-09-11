package com.android.mms.transaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import timber.log.Timber;

/**
 * MmsPushOutboxMessages listens for MMS_SEND_OUTBOX_MSG intent .
 * {@link android.intent.action.MMS_SEND_OUTBOX_MSG},
 * and wakes up the mms service when it receives it.
 * This will tricker the mms service to send any messages stored
 * in the outbox.
 */
public class MmsPushOutboxMessages extends BroadcastReceiver {
    private static final String INTENT_MMS_SEND_OUTBOX_MSG = "android.intent.action.MMS_SEND_OUTBOX_MSG";

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.v("Received the MMS_SEND_OUTBOX_MSG intent: " + intent);
        String action = intent.getAction();
        if(action.equalsIgnoreCase(INTENT_MMS_SEND_OUTBOX_MSG)){
            Timber.d("Now waking up the MMS service");
            context.startService(new Intent(context, TransactionService.class));
        }
    }

}
