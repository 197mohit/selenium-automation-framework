package com.paytm.utils.merchant.api;

import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;

public class GetMerchantKey extends BaseApi {

    private static final String BASE_URI = Constants.ADMIN_SERVER_ADDRESS;
    private static final String BASE_PATH = "/admin/app/v1/merchants/{entityId}/key";
    private static Response response;
    private static JsonPath jsonPath;

    private GetMerchantKey(String entityId) {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setBaseUri(BASE_URI);
        getRequestSpecBuilder().setBasePath(BASE_PATH.replace("{entityId}", entityId));
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("x-auth-ump", "zxcs-9098-kls-qw90-xcd");
    }

    public static String getKey(String entityId) {
        GetMerchantKey getMerchantKey = new GetMerchantKey(entityId);
        response = getMerchantKey.execute();
        jsonPath = response.jsonPath();
        Assertions.assertThat(response.getStatusCode()).as("status code").isEqualTo(200);
        String key = jsonPath.getString("result");

        return key;
    }

}
