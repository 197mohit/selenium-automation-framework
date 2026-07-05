package com.paytm.utils.merchant.util.exception.pgpException;

/**
 * Created by deepakkumar on 3/4/18.
 */
public class NoResultFoundException extends PGPException {

    private static final long serialVersionUID = -589868288886829641L;

    public NoResultFoundException() {
    }

    public NoResultFoundException(String message) {
        super(message);
    }

    public NoResultFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoResultFoundException(Throwable cause) {
        super(cause);
    }

    public NoResultFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
