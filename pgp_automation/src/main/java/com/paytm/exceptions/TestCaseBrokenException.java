package com.paytm.exceptions;

public class TestCaseBrokenException extends RuntimeException {

    public TestCaseBrokenException(Throwable cause) {
        super(cause);
    }

    public TestCaseBrokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public TestCaseBrokenException(String message) {
        super(message);
    }
}
