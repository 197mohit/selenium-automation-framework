package com.paytm.utils.merchant.api.MappingService;

import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;

/**
 * @author ankuragarwal
 * Date: 10/09/18
 */
public class GetLookupPaymode extends BaseApi{

    private String lookUpPaymodeUrl = Constants.MappingService.LOOK_UP_PAYMENT_MODE;

    private GetLookupPaymode(String paymodeType) {
        lookUpPaymodeUrl = lookUpPaymodeUrl.replace("{paymodeType}", paymodeType);
        setMethod(MethodType.GET);
        RequestSpecBuilder requestSpecBuilder = getRequestSpecBuilder();
        requestSpecBuilder.setContentType(ContentType.JSON);
        requestSpecBuilder.setAccept(ContentType.JSON);
        requestSpecBuilder.setBaseUri(Constants.PGP_HOST);
        requestSpecBuilder.setBasePath(lookUpPaymodeUrl);
    }

    public static Response GetLookupPaymode(String paymodeType) {
        GetLookupPaymode getLookupPaymode = new GetLookupPaymode(paymodeType);
        Response response = getLookupPaymode.execute();
        Assertions.assertThat(response.getStatusCode()).as("Status Code is not success").isEqualTo(200);
        return response;
    }
}
