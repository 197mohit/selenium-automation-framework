package com.paytm.utils.merchant.api.MappingService;

import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;

/**
 * Created by ankuragarwal on 12/10/18
 */
public class GetPaytmId extends BaseApi {

    private String getPaytmIdUrl = Constants.MappingService.GET_PAYTM_ID;

    private GetPaytmId(String alipayId) {
        getPaytmIdUrl = getPaytmIdUrl.replace("{alipayId}", alipayId);
        setMethod(MethodType.GET);
        RequestSpecBuilder requestSpecBuilder = getRequestSpecBuilder();
        requestSpecBuilder.setContentType(ContentType.JSON);
        requestSpecBuilder.setAccept(ContentType.JSON);
        requestSpecBuilder.setBaseUri(Constants.PGP_HOST);
        requestSpecBuilder.setBasePath(getPaytmIdUrl);
    }

    public static Response executeGetPaytmId(String alipayId) {
        GetPaytmId getPaytmId = new GetPaytmId(alipayId);
        Response response = getPaytmId.execute();
        Assertions.assertThat(response.getStatusCode()).as("Status Code is not success").isEqualTo(200);
        return response;
    }
}
