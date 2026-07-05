package com.paytm.utils.merchant.util.addMoneyExpressEncryp;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

/**
 * Created by anjukumari on 28/02/19
 */
public class Helpher {


    public static String getEncryptedPayment(String cardDetail, String fileName) throws InvalidKeySpecException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IOException {
        //cardDetail = "36088620723331|457|092018";
        List<Key> keyPair = NewEncrypt.generateNewKey(fileName);
        NewEncrypt encrypt= new NewEncrypt(keyPair.get(0).getEncoded());
        String encVal = encrypt.encrypt(cardDetail);
        return encVal;
    }
}
