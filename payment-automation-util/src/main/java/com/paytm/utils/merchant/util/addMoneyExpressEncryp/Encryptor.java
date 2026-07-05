package com.paytm.utils.merchant.util.addMoneyExpressEncryp;

/**
 * This indicates that the current object is capable of encrypting data.
 *
 * @param <T> The type of data to encrypt.
 * @param <K> The type of encrypted data.
 */
public interface Encryptor<T, K> {

    /**
     * Encrypts the data input.
     *
     * @param data input.
     * @return encrypted data.
     */
    K encrypt(T data);
}