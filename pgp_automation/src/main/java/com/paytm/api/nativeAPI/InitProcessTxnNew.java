package com.paytm.api.nativeAPI;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.OrderDTO;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import java.util.HashMap;
import java.util.Map;

public class InitProcessTxnNew extends BaseApi {


    public InitProcessTxnNew(OrderDTO orderDTO, String senturl) {
        Map<String, Object> formData= prepareFormBody(orderDTO);

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.URLENC);
        getRequestSpecBuilder().setAccept("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        getRequestSpecBuilder().addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36");
        getRequestSpecBuilder().addHeader("Referer", "http://localhost:8080/theia/merchantcheckout");
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.NEW_INIT_TXN_INTENT_URL);
        getRequestSpecBuilder().addFormParams(formData);
        getRequestSpecBuilder().addFormParam("appCallbackUrl", senturl);
        getRequestSpecBuilder().addFormParam("browserName", "googlechrome");

    }

    private Map<String,Object> prepareFormBody(OrderDTO orderDTO){
        ObjectMapper oMapper = new ObjectMapper();
        oMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        oMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        Map<String, Object> formData = oMapper.convertValue(orderDTO, HashMap.class);

        return formData;
    }
}
