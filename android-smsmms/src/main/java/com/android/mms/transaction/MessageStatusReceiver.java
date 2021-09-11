package com.android.mms.transaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MessageStatusReceiver extends BroadcastReceiver {
    public static final String MESSAGE_STATUS_RECEIVED_ACTION =
            "com.android.mms.transaction.MessageStatusReceiver.MESSAGE_STATUS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (MESSAGE_STATUS_RECEIVED_ACTION.equals(intent.getAction())) {
            intent.setClass(context, MessageStatusService.class);
            context.startService(intent);
       }
    }
}
