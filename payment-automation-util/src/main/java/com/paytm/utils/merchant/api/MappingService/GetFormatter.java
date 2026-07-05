package com.paytm.utils.merchant.api.MappingService;

import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;

/**
 * Created by ankuragarwal on 27/9/18
 */
public class GetFormatter extends BaseApi {
    private String getFormatterUrl = Constants.MappingService.GET_FORMATTER;

    private GetFormatter(String bankCode, String payMode) {

        getFormatterUrl = getFormatterUrl.replace("{bankCode}", bankCode)
                .replace("{payMethod}", payMode);
        setMethod(MethodType.GET);
        RequestSpecBuilder requestSpecBuilder = getRequestSpecBuilder();
        requestSpecBuilder.setContentType(ContentType.JSON);
        requestSpecBuilder.setAccept(ContentType.JSON);
        requestSpecBuilder.setBaseUri(Constants.PGP_HOST);
        requestSpecBuilder.setBasePath(getFormatterUrl);
    }

    public static Response executeGetFormatter(String bankCode, String payMode) {
        GetFormatter getFormatter = new GetFormatter(bankCode, payMode);
        Response response = getFormatter.execute();
        Assertions.assertThat(response.getStatusCode()).as("Status Code is not success").isEqualTo(200);
        return response;
    }
}
