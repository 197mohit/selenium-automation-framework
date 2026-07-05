package com.paytm.api.theia;

import com.paytm.CreateToken;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.ApplyPromoV2DTO.ApplyPromoV2DTO;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Results;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;

import java.util.UUID;

import static com.paytm.appconstants.Constants.NativeAPIResourcePath.APPLY_PROMO_V2;

public class ApplyPromoV2Api extends BaseApi {
    public  ApplyPromoV2Api(ApplyPromoV2DTO applyPromoV2DTO, Constants.MerchantType mid, String orderId) {

        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(com.paytm.utils.merchant.Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath(APPLY_PROMO_V2);
        if (applyPromoV2DTO.getHead().getTokenType().equalsIgnoreCase("ACCESS")) {
            String refId = UUID.randomUUID().toString().substring(0, 18);
            CreateToken createToken = new CreateToken(mid, applyPromoV2DTO.getHead().getToken(), refId);
            Response ctR = createToken.execute()
                    .then().extract().response();

            getRequestSpecBuilder()
                    .addQueryParam("mid", mid.getId())
                    .addQueryParam("referenceId", refId);
            applyPromoV2DTO.getHead().setToken(ctR.jsonPath().getString("body.accessToken"));
            getRequestSpecBuilder().setBody(applyPromoV2DTO);
        }
        else if (applyPromoV2DTO.getHead().getTokenType().equalsIgnoreCase("CHECKSUM")) {

            getRequestSpecBuilder().addQueryParam("mid", mid.getId());
            String token = PGPHelpers.getNativeChecksum(mid.getKey(), applyPromoV2DTO.getBody());
            System.out.println("Checksum=" + token);
            applyPromoV2DTO.getHead().setToken(token);
            getRequestSpecBuilder().setBody(applyPromoV2DTO);
        }
        else if (applyPromoV2DTO.getHead().getTokenType().equalsIgnoreCase("TXN_TOKEN")){
            getRequestSpecBuilder()
                    .addQueryParam("mid", mid.getId())
                    .addQueryParam("orderId",orderId);

            getRequestSpecBuilder().setBody(applyPromoV2DTO);
        } else {
            getRequestSpecBuilder()
                    .addQueryParam("mid", mid.getId());
            getRequestSpecBuilder().setBody(applyPromoV2DTO);
        }
    }
    public  static JsonPath applyPromoV2(ApplyPromoV2DTO applyPromoV2DTO, com.paytm.appconstants.Constants.MerchantType mid, String orderId) {
        return new ApplyPromoV2Api(applyPromoV2DTO, mid, orderId).execute().jsonPath();
    }
}
