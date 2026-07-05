package com.paytm.utils.merchant.util.exception.pgpException;

/**
 * Created by deepakkumar on 3/4/18.
 */
public class PGPException extends RuntimeException {

    private static final long serialVersionUID = 148545499027578959L;

    public PGPException() {
    }

    public PGPException(String message) {
        super(message);
    }

    public PGPException(String message, Throwable cause) {
        super(message, cause);
    }

    public PGPException(Throwable cause) {
        super(cause);
    }

    public PGPException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
