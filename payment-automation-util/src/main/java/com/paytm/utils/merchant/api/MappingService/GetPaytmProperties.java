package com.paytm.utils.merchant.api.MappingService;

import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;

/**
 * Created by ankuragarwal on 13/9/18
 */
public class GetPaytmProperties extends BaseApi {

    private String getPaytmProeprtiesUrl = Constants.MappingService.GET_PAYTM_PROPERTIES;

    private GetPaytmProperties(String propertyName) {
        getPaytmProeprtiesUrl = getPaytmProeprtiesUrl.replace("{propName}", propertyName);
        setMethod(MethodType.GET);
        RequestSpecBuilder requestSpecBuilder = getRequestSpecBuilder();
        requestSpecBuilder.setContentType(ContentType.JSON);
        requestSpecBuilder.setAccept(ContentType.JSON);
        requestSpecBuilder.setBaseUri(Constants.PGP_HOST);
        requestSpecBuilder.setBasePath(getPaytmProeprtiesUrl);
    }

    public static Response executeGetPaytmProperties(String propertyName) {
        GetPaytmProperties getPaytmProperties = new GetPaytmProperties(propertyName);
        Response response = getPaytmProperties.execute();
        Assertions.assertThat(response.getStatusCode()).as("Status Code is not success").isEqualTo(200);
        return response;
    }

}
