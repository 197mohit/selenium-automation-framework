package com.paytm.apphelpers;
import com.paytm.encryptdecrypt.Aes256EncryptionDecryption;
import com.paytm.utils.merchant.util.exception.pgpException.PGPException;
import java.util.HashMap;

public class IrctcHelper {
    public static HashMap<String, String> getDecryptedResponse(String encryptedString, String Key) {
        String decryptedResponse = Aes256EncryptionDecryption.decrypt(encryptedString, Key);
        HashMap<String, String> respMap = new HashMap<>();
        try {
            for (String s : decryptedResponse.split("\\|")) {
                String k = s.substring(0, s.indexOf("="));
                String v = s.substring(s.indexOf("=") + 1);
                respMap.put(k, v);
            }
        } catch (Exception e) {
            throw new PGPException("Exception occurred while decrypting request");
        }
        return respMap;
    }
}
