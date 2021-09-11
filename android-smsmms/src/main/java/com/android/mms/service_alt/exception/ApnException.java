package com.android.mms.service_alt.exception;

/**
 * APN exception
 */
public class ApnException extends Exception {

    public ApnException() {
        super();
    }

    public ApnException(String message) {
        super(message);
    }

    public ApnException(Throwable cause) {
        super(cause);
    }

    public ApnException(String message, Throwable cause) {
        super(message, cause);
    }
}
