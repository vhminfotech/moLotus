package com.android.mms.transaction;

import android.content.Context;
import android.util.Config;
import timber.log.Timber;

/**
 * Default retry scheme, based on specs.
 */
public class DefaultRetryScheme extends AbstractRetryScheme {
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = DEBUG ? Config.LOGD : Config.LOGV;

    private static final int[] sDefaultRetryScheme = {
        0, 1 * 60 * 1000, 5 * 60 * 1000, 10 * 60 * 1000, 30 * 60 * 1000};

    public DefaultRetryScheme(Context context, int retriedTimes) {
        super(retriedTimes);

        mRetriedTimes = mRetriedTimes < 0 ? 0 : mRetriedTimes;
        mRetriedTimes = mRetriedTimes >= sDefaultRetryScheme.length
                ? sDefaultRetryScheme.length - 1 : mRetriedTimes;

        // TODO Get retry scheme from preference.
    }

    @Override
    public int getRetryLimit() {
        return sDefaultRetryScheme.length;
    }

    @Override
    public long getWaitingInterval() {
        if (LOCAL_LOGV) {
            Timber.v("Next int: " + sDefaultRetryScheme[mRetriedTimes]);
        }
        return sDefaultRetryScheme[mRetriedTimes];
    }
}
