package com.paytm.api.billproxy;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import static com.paytm.LocalConfig.PGP_HOST;
import static com.paytm.appconstants.Constants.BillProxy.CARD_TOKENIZE_CARD_NO_V1;
import static com.paytm.framework.api.BaseApi.MethodType.POST;

public class CardTokenizeCardNumberV1API extends BaseApi {

    public CardTokenizeCardNumberV1API(String cardNo, String ssoToken) {
        this.setMethod(POST);
        this.getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        this.getRequestSpecBuilder().setBaseUri(PGP_HOST);
        this.getRequestSpecBuilder().setBasePath(CARD_TOKENIZE_CARD_NO_V1);
        this.getRequestSpecBuilder().setBody("{\"head\":{\"clientId\":\"ANDROID\",\"version\":\"v1\",\"requestTimeStamp\":\"1519736590323\",\"channelId\":\"APP\",\"signature\":\"\"},\"body\":{\"cardNumber\":\"" + cardNo + "\",\"ssoToken\":\"" + ssoToken + "\"}}");
    }
}