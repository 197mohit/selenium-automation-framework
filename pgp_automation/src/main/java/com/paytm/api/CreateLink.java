package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.CreateLinkDTO;
import com.paytm.framework.api.BaseApi;
import com.paytm.framework.utils.CommonUtils;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;

public class CreateLink extends BaseApi {

    Response response;
    CreateLinkDTO createLinkDTO;

    public CreateLink(String mid, String linkName, Double amount) {
        String date = CommonUtils.getdate("dd/MM/yyyy");
        createLinkDTO = new CreateLinkDTO()
                .setMid(mid)
                .setLinkName(linkName)
                .setLinkType("INVOICE")
                .setDueDate(CommonHelpers.subtractDays(date, "dd/MM/yyyy", 1))
                .setExpiryDate(CommonHelpers.addDays(date, "dd/MM/yyyy", 5))
                .setPenaltyFee(0.00)
                .setAmount(amount)
                .setInvoiceId("1");
        generateRequest(createLinkDTO);
    }

    public CreateLink(CreateLinkDTO createLinkDTO) {
        generateRequest(createLinkDTO);
    }

    public void generateRequest(CreateLinkDTO createLinkDTO) {
        super.setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.GET_LINK);
        getRequestSpecBuilder().setBody(createLinkDTO);
    }

    public String getPaymentLink() {
        JsonPath path = response.jsonPath();
        Assertions.assertThat(path.get("longUrl").toString()).isNotNull().withFailMessage("Long URL is not available in response");
        String paymentLink = path.get("longUrl").toString();
        return paymentLink;
    }

    @Override
    public Response execute() {
        response = super.execute();
        return response;
    }
}
