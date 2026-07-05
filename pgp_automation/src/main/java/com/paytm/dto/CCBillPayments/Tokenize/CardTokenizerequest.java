package com.paytm.dto.CCBillPayments.Tokenize;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.User;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.http.ContentType;

import java.util.HashMap;

public class CardTokenizerequest extends BaseApi {
    public  CardTokenizerequest(CardTokenize body,CardTokenize.TokenizeType type,User user){
        HashMap<String, String> map = new HashMap<>();
        String ssoToken=user.ssoToken();
        map.put("SsoToken",ssoToken);
        setMethod(BaseApi.MethodType.POST);
        setBodyOmitNullValueAttributes(body);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.PGP_HOST);
        getRequestSpecBuilder().addHeaders(map);
        if(type.toString().equals(CardTokenize.TokenizeType.ccNumber.toString())){
            getRequestSpecBuilder().setBasePath("billproxy/api/v2/cardNumber/cc/cardTokenize/request");
        }
        else if(type.toString().equals(CardTokenize.TokenizeType.savedCardId.toString())){
            getRequestSpecBuilder().setBasePath("billproxy/api/v2/savedCardId/cc/cardTokenize/request");
        }
        else if (type.toString().equals(CardTokenize.TokenizeType.creditCardId.toString())){
            getRequestSpecBuilder().setBasePath("billproxy/api/v2/creditCardId/cc/cardTokenize/request");
        }
    }
}
