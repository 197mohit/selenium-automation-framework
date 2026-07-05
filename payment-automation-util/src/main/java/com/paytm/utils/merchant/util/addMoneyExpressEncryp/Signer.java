package com.paytm.utils.merchant.util.addMoneyExpressEncryp;


/**
 * This indicates that the current object is capable of signing data.
 *
 * @param <T> The type of data to sign.
 */
public interface Signer<T> {

    /**
     * Sign the data input.
     *
     * @param data input.
     * @return signed data.
     */
    T sign(T data);
}