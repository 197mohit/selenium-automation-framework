package com.paytm.utils.merchant.util.addMoneyExpressEncryp;


import jodd.io.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;

public final class NewEncrypt  {

    private static final String DELIMITER = "$";
    private static final String VERSION = "ue-1_0_1";

    private static final int AES_KEY_SIZE_IN_BITS = 256;
    private static final int HMAC_SECRET_SIZE_IN_BITS = 256;
    private static final int NONCE_SIZE_IN_BITS = 64;

    private byte[] mPublicKey;

    /**
     * Default constructor.
     *
     * @param bs RSA public key as string.
     */
    public NewEncrypt( byte[] bs) {
        mPublicKey = bs;
    }

    public NewEncrypt() {
    }

    public static PublicKey LoadKey(String path, String algorithm)
            throws IOException, NoSuchAlgorithmException,
            InvalidKeySpecException {
        ClassLoader classLoader = new NewEncrypt().getClass().getClassLoader();
        InputStream fis = classLoader.getResourceAsStream(path);//ClassLoader.getSystemResourceAsStream("public.key");
        File filePublicKey = new File(classLoader.getResource(path).getFile());
        byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
        fis.read(encodedPublicKey);
        fis.close();
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
                encodedPublicKey);
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

        return publicKey;
    }



    public static List<Key> generateNewKey(String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        Key publicKey = LoadKey(filename, "RSA");
        List<Key> returnable= new ArrayList<Key>();
        returnable.add(publicKey);
        return returnable;
    }

    public String encrypt( String text) {
        byte[] data = text.getBytes(Charset.forName("UTF-8"));
        byte[] nonce = generateRandomBytes(NONCE_SIZE_IN_BITS / 8);
        byte[] aesKey = generateRandomBytes(AES_KEY_SIZE_IN_BITS / 8);
        byte[] hmacSecret = generateRandomBytes(HMAC_SECRET_SIZE_IN_BITS / 8);

        AesEncryptor aesEncryptor = new AesEncryptor(aesKey, nonce);
        byte[] cipherText = aesEncryptor.encrypt(data);
        byte[] message = createMessage(nonce, cipherText);

        HmacSigner hmacSigner = new HmacSigner(hmacSecret);
        byte[] signature = hmacSigner.sign(message);

        RsaEncryptor rsaEncryptor = new RsaEncryptor(mPublicKey, RsaEncryptor.CIPHER_RSA_ECB_OAEP);
        byte[] encryptedAesKey = rsaEncryptor.encrypt(aesKey);
        byte[] encryptedHmacSecret = rsaEncryptor.encrypt(hmacSecret);

        return wrap(VERSION, encryptedHmacSecret, encryptedAesKey, nonce, cipherText, signature);
    }


    private static byte[] createMessage( byte[] nonce,  byte[] cipherText) {
        ArrayList<String> items = new ArrayList<String>();
        items.add(DatatypeConverter.printBase64Binary(nonce));
        items.add(DatatypeConverter.printBase64Binary(cipherText));

        StringBuilder sb= new StringBuilder();
        for(String item:items){
            sb.append(new String(item)).append(DELIMITER);
        }
        String returnable = sb.toString();
        return returnable.substring(0, returnable.length()-DELIMITER.length()).getBytes(Charset.forName("UTF-8"));
    }


    private static byte[] generateRandomBytes(int sizeInBits) {
        byte[] bytes = new byte[sizeInBits];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }


    private static String wrap( String version, byte[]... byteArrays) {
        ArrayList<String> items = new ArrayList<String>();
        items.add(version);

        for (byte[] byteArray : byteArrays) {
            items.add(DatatypeConverter.printBase64Binary(byteArray));
        }

        StringBuilder sb= new StringBuilder();
        for(String item:items){
            sb.append(new String(item)).append(DELIMITER);
        }
        String returnable = sb.toString();
        return returnable.substring(0, returnable.length()-DELIMITER.length());

    }
}