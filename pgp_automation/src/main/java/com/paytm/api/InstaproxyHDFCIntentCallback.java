package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.framework.utils.DatabaseUtil;
import io.restassured.http.ContentType;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class InstaproxyHDFCIntentCallback extends BaseApi {

    private String meRes;
    private String pgMerchantId = "HDFC000010425377";
    private String enc_key = "2ca02bad0275e75b9688f7f7d4463a52";

    public  InstaproxyHDFCIntentCallback(String callbackResponse , String esn) throws Exception {
        // Set method and headers
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.URLENC);
        getRequestSpecBuilder().setAccept(ContentType.ANY);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.INSTAPROXY_SECURERESPONSE_HDFC_UPI_RESP);

        String encryptedCallbackMessage = encrypt(callbackResponse,enc_key);
        // Form-data params
        this.meRes = encryptedCallbackMessage;
        this.pgMerchantId = pgMerchantId;

        addStatusQueryResponseInDB(encryptedCallbackMessage, esn);

        Thread.sleep(2000);

        // Add form params (same as --data-urlencode in curl)
        getRequestSpecBuilder().addFormParam("meRes", this.meRes);
        getRequestSpecBuilder().addFormParam("pgMerchantId", this.pgMerchantId);

    }

    public static String encrypt(String message, String enc_key ) {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(HexfromString(enc_key), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encstr = cipher.doFinal(message.getBytes());
            return HextoString(encstr);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Helper to convert HEX -> bytes
    public static byte[] HexfromString(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }

    // Helper to convert bytes -> HEX
    public static String HextoString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    public static void addStatusQueryResponseInDB( String encryptedCallbackMessage , String esn ){

        try {
            String query = "UPDATE PGP_QA_UTILS.hdfcMandateHelper SET callbackResponse=" + "'" + encryptedCallbackMessage + "'" + ", reccurrResponse=NULL, revokeFlag='N' WHERE pspRefNo=" + esn + ";";
            System.out.println("DB Query " + query);
            DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.MOCK_DB, query);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
