package com.paytm.api.MappingService;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import java.util.List;

public class MerchantIdListDetail extends BaseApi {
    String request ="{\"merchantIdList\": [\"{merchantIdLists}\"]}";

    public MerchantIdListDetail() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingService.MERCHANT_LIST);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }
    public String getRequest() {
        return request;
    }

    public void setRequest(List<String> merchantIdList) {
        this.request = request.replace("{merchantIdLists}",merchantIdList.toString());
    }

    public MerchantIdListDetail buildRequest(List<String> merchantIdList) {
        setContext("merchantIdList", merchantIdList);
        return this;
    }
}
