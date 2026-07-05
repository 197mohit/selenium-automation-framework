package com.paytm.utils.merchant.util.addMoneyExpressEncryp;


import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES-CTR encryptor.
 *
 * Encrypts data using the AES symmetric cipher in counter (CTR) mode with no padding.
 *
 * The IV input to the AES-CTR algorithm is 128-bits wide and is formatted as shown below:
 * ---------------------------------
 * | 64-bit nonce | 64-bit counter |
 * ---------------------------------
 *
 * The most significant 64-bits is allocated as the nonce. This is the number that is transmitted in the payload.
 * The least significant 64-bits is allocated to the counter (internal). The initial value for the counter is 1.
 *
 * For example, if the 64-bit nonce is:
 * 0x15B7369DDD955828
 *
 * The IV used with the AES-CTR algorithm on first invocation is:
 * 0x15B7369DDD9558280000000000000001
 */
public final class AesEncryptor implements Encryptor<byte[], byte[]> {

    private static final String ALGORITHM_AES = "AES";
    private static final String CIPHER_AES_CTR_NO_PADDING = "AES/CTR/NoPadding";

     private final byte[] mAesKey;
     private final byte[] mNonce;

    /**
     * Default constructor.
     *
     * @param aesKey 256-bit AES key.
     * @param nonce 8-byte (64-bit) sequence that forms the MSB of the IV.
     */
    public AesEncryptor( byte[] aesKey,  byte[] nonce) {
        mAesKey = aesKey.clone();
        mNonce = nonce.clone();
    }

    public byte[] encrypt( byte[] data) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(mAesKey, ALGORITHM_AES);
        IvParameterSpec iv = createIv(mNonce);
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_AES_CTR_NO_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);
            return cipher.doFinal(data);
        } catch (Exception ignored) {
            return null;
        }
    }

    
    private static IvParameterSpec createIv( byte[] nonce) {
        byte[] counter = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01 };
        byte[] output = new byte[nonce.length + counter.length];
        for (int i = 0; i < nonce.length; ++i) {
            output[i] = nonce[i];
        }
        for (int j = 0; j < counter.length; ++j) {
            output[nonce.length + j] = counter[j];
        }
        return new IvParameterSpec(output);
    }
}