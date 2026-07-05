package com.paytm.api.PgPlusBO;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class CreateDispute extends BaseApi{
    private String body = "{\n" +
            "  \"txnId\":\"{esn}\",\n" +
            "   \"cbAmount\":10,\n" +
            "   \"reason\":\"chargeback from bank\",\n" +
            "   \"referenceNo\":\"{ref_num}\",\n" +
            "   \"disputeSource\":\"BANK_NOTIFY\",\n" +
            "   \"accuserType\":\"USER\",\n" +
            "   \"accusedType\":\"MERCHANT\",\n" +
            "   \"contactInfo\":\"samar.aswal@paytm.com\",\n" +
            "   \"boOperatorName\":\"create_dispute\",\n" +
            "   \"currency\":\"INR\",\n" +
            "   \"description\":\"DISPUTE CREATION\",\n" +
            "   \"transType\":\"ACQUIRING\",\n" +
            "   \"disputeExtendInfo\":{ \n" +
            "      \"cbType\":\"1\",\n" +
            "    \"cbDueDate\":\"31/12/2021\"\n" +
            "   }\n" +
            "}";

    public CreateDispute(String esn, String referenceId){
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PgPlusBo.CRERATE_DISPUTE);
        setBody(esn, referenceId);
        getRequestSpecBuilder().setBody(body);
    }

    private void setBody(String esn, String referenceId){
        this.body = body.replace("{esn}", esn)
                .replace("{ref_num}", referenceId);
    }
}
