package com.paytm.api.theia;

import com.paytm.CreateToken;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.utils.merchant.Results;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;

import java.util.UUID;

import static com.paytm.appconstants.Constants.NativeAPIResourcePath.APPLY_PROMO_V2;

public class ApiV2ApplyPromo extends ApiV1ApplyPromo {

    public ApiV2ApplyPromo(Constants.MerchantType mid, String sso_token) {
        super(mid.getId());
        String refId = UUID.randomUUID().toString().substring(0, 18);

        CreateToken createToken = new CreateToken(mid, sso_token, refId);
        Response ctR = createToken.execute()
                .then()
                .spec((ResponseSpecification) new Results().getSuccess())
                .extract().response();

        getRequestSpecBuilder()
                .setBasePath(APPLY_PROMO_V2)
                .addQueryParam("mid", "?")
                .addQueryParam("orderId", UUID.randomUUID().toString())
                .addQueryParam("referenceId", refId)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        setMethod(MethodType.POST);
        setContext("body.mid", mid.getId())
                .setContext("head.tokenType", "ACCESS")
                .setContext("head.token", ctR.jsonPath().getString("body.accessToken"));

    }
}
