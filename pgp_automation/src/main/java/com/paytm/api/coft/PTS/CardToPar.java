package com.paytm.api.coft.PTS;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CoftHelper;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.coft.CardData;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * @Author mayankbharshiv
 * @Date 05/07/22 05:02 PM
 * @Version 1.0
 */
public class CardToPar extends BaseApi {

    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"version\": \"v1\",\n" +
            "        \"requestTimestamp\": \"1544614590000\",\n" +
            "        \"requestId\": \""+UUID.randomUUID().toString()+"\",\n" +
            "        \"signature\": \"AQ8THu3k8dP4dJI0tCiJLnX1J1qFWKPGMND5chRcsi0DkzHbar0XdYarWVkC+PPlWEsZG31C4Y4dOeqyE4NvYe+b4NwFz/+PfkcDxlXiNwY=\",\n" +
            "        \"signatureType\": \"CHECKSUM\",\n" +
            "        \"clientId\":\"c\"\n" +
            "    },\n" +
            "    \"body\": {\"mid\":\"hdiTdG75687156533737\",\"requestType\":\"GCIN\",\"requestValue\":\"af6b61516f624f52ff7a06f32573830b5e614ebeb6467e12eb7fd0606d82795d\"}\n" +
            "}";

    public CardToPar(boolean callNetwork) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.COFT_CENTER);
        getRequestSpecBuilder().setBasePath(Constants.coftCenter.CARD_TO_PAR);
        getRequestSpecBuilder().addQueryParam("disableNetworkRefresh", callNetwork);
        getRequestSpecBuilder().setBody(getRequest());
    }

    public String getRequest() {
        return request;
    }

    public CardToPar buildRequest(String mid, String requestType, String requestValue,String checksum) {
        setContext("body.mid", mid);
        setContext("body.requestType", requestType);
        setContext("body.requestValue", requestValue);
        setContext("head.signature", checksum);
        return this;
    }

    public CardToPar encryptCardData(String cardNumber)
            throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        CardData cardDataObject = new CardData();
        cardDataObject.setCardNumber(cardNumber);
        String encryptedCardData = CoftHelper.encryptCardData(cardDataObject);
        setContext("body.requestValue", encryptedCardData);
        return this;
    }
}
