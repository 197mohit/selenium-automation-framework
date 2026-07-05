package com.paytm.utils.merchant.util.addMoneyExpressEncryp;


import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;

/**
 * Used to encrypt data with a public key.
 */
public class RsaEncryptor implements Encryptor<byte[], byte[]> {

    public static final String CIPHER_RSA_ECB_OAEP = "RSA/ECB/OAEPWithSHA1AndMGF1Padding";
    public static final String CIPHER_RSA_PKCS1_PADDING = "RSA/ECB/PKCS1Padding";

    private static final String ALGORITHM_RSA = "RSA";

    private final Cipher mCipher;

    /**
     * Default constructor.
     *
     * @param mPublicKey the public key as text.
     * @param transformation The name of the transformation to create a cipher for.
     */
    public RsaEncryptor( byte[] mPublicKey,  String transformation) {
        mCipher = createCipher(mPublicKey, transformation);
    }

    public byte[] encrypt( byte[] data) {
        if (mCipher != null) {
            try {
                return mCipher.doFinal(data);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    /**
     * Encrypts text with public key.
     *
     * @param text the UTF-8 input text.
     * @return Base64 encoded string.
     */
    public String encrypt( String text) {
        try {
            byte[] inputBytes = text.getBytes(Charset.forName("UTF-8"));
            byte[] outputBytes = encrypt(inputBytes);
            return DatatypeConverter.printBase64Binary(outputBytes);
//            return Base64.encodeToString(outputBytes, Base64.DEFAULT);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Creates an RSA cipher.
     *
     * @param mPublicKey A public key as a base64 string.
     * @param transformation The name of the transformation to create a cipher for.
     * @return RSA cipher for encrypting card data.
     */
    private static Cipher createCipher( byte[] mPublicKey,  String transformation) {
        Cipher cipher;
        try {
      	  byte[] publicKeyBytes =mPublicKey;
//      	  byte[] publicKeyBytes =DatatypeConverter.parseBase64Binary(mPublicKey);
//            byte[] publicKeyBytes = Base64.decode(publicKeyString, Base64.DEFAULT);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
            cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        } catch (Exception ignored) {
            cipher = null;
        }
        return cipher;
    }
}