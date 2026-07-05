package com.paytm.utils.merchant.util.addMoneyExpressEncryp;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * HMAC signer.
 *
 * Signs the hash-based message authentication code (HMAC) using the SHA256 hash function.
 */
public final class HmacSigner implements Signer<byte[]> {

    private static final String ALGORITHM_HMAC = "HmacSHA256";

     private final byte[] mSecretKey;

    /**
     * Default constructor.
     *
     * @param secretKey used to sign.
     */
    public HmacSigner( byte[] secretKey) {
        mSecretKey = secretKey.clone();
    }

    public byte[] sign( byte[] data) {
        try {
            SecretKey secretKey = new SecretKeySpec(mSecretKey, ALGORITHM_HMAC);
            Mac mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
            return mac.doFinal(data);
        } catch (Exception ignored) {
            return null;
        }
    }
}