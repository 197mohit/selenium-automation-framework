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
public class GetBankDetailsAPCode extends BaseApi {

    private String getBankDetailsApCodeUrl = Constants.MappingService.GET_BANK_DETAILS_APCODE;

    private GetBankDetailsAPCode(String alipayCode) {
        getBankDetailsApCodeUrl = getBankDetailsApCodeUrl.replace("{alipayCode}", alipayCode);
        setMethod(MethodType.GET);
        RequestSpecBuilder requestSpecBuilder = getRequestSpecBuilder();
        requestSpecBuilder.setContentType(ContentType.JSON);
        requestSpecBuilder.setAccept(ContentType.JSON);
        requestSpecBuilder.setBaseUri(Constants.PGP_HOST);
        requestSpecBuilder.setBasePath(getBankDetailsApCodeUrl);
    }

    public static Response executeGetBankDetailsAPCode(String alipayCode) {
        GetBankDetailsAPCode getBankDetailsAPCode = new GetBankDetailsAPCode(alipayCode);
        Response response = getBankDetailsAPCode.execute();
        Assertions.assertThat(response.getStatusCode()).as("Status Code is not success").isEqualTo(200);
        return response;
    }
}
