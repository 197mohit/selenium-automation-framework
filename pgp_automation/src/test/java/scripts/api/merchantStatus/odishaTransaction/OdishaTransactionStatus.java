package scripts.api.merchantStatus.odishaTransaction;

import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class OdishaTransactionStatus extends BaseApi {

    String msg;
    String payload = "{MID}|{ORDER_ID}|{AMOUNT}";


    public OdishaTransactionStatus(MerchantType merchant, String orderId, String amount) throws Exception {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath(com.paytm.appconstants.Constants.PGPAPIResourcePath.TXNSTATUS);
        msg = encryptMsg(merchant, orderId, amount);
        getRequestSpecBuilder().addQueryParam("msg", msg);
        getRequestSpecBuilder().addQueryParam("merchantCode", merchant.getId());
    }

    private static byte[] genHmac(final byte[] data, final byte[] key) throws Exception {
        final Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        final SecretKeySpec secretKey = new SecretKeySpec(key, "HmacSHA256");
        sha256_HMAC.init(secretKey);
        return sha256_HMAC.doFinal(data);
    }

    private static String getBase64String(final byte[] byteArray) throws Exception {
        return Base64.getEncoder().encodeToString(byteArray);
    }

    private static byte[] encrypt(final byte[] plainText, final byte[] secret) throws Exception {
        final SecretKeySpec secretKey = new SecretKeySpec(secret, "AES");
        final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(plainText);
    }

    public String encryptMsg(MerchantType merchant, String orderId, String amount) throws Exception {
        String checksumString = payload.replace("{MID}", merchant.getId()).replace("{ORDER_ID}", orderId).replace("{AMOUNT}", amount);
        byte[] dataArr = checksumString.getBytes();
        byte[] keyData = merchant.getKey().getBytes();
        byte[] response = genHmac(dataArr, keyData);
        String datachkPlain = checksumString + "|" + getBase64String(response);
        byte[] datachkPlainArr = datachkPlain.getBytes();
        byte[] cypherText = encrypt(datachkPlainArr, keyData);
        return getBase64String(cypherText);
    }


    public String decryptMsg(MerchantType merchant, String msg) throws Exception {
        byte[] decodedBase64String = decodeBase64String(msg);
        byte[] keyData = merchant.getKey().getBytes();
        final SecretKeySpec secretKey = new SecretKeySpec(keyData, "AES");
        final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] base64Decoded= cipher.doFinal(decodedBase64String);
        return new String(base64Decoded);
    }


    public static byte[] decodeBase64String(String encodedString) {
        return Base64.getDecoder().decode(encodedString);
    }

}
