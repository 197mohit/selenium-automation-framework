package com.paytm.exceptions;


/*
 * Used when desired key not found in Json response
 * */
public class NoSuchKeyException extends Exception {

    public NoSuchKeyException(String message) {
        super(message);
    }
}
