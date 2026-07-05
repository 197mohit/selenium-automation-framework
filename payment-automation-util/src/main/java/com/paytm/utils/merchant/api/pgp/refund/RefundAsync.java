package com.paytm.utils.merchant.api.pgp.refund;

import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.dto.refund.AsyncRefundDTO;
import io.restassured.http.ContentType;

public class RefundAsync extends BaseApi {


    public RefundAsync()
    {}

    public RefundAsync(String pgpUrl, String body) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setBaseUri(pgpUrl)
                .setBasePath("/refund/api/v1/async/refund");
        getRequestSpecBuilder().setBody(body);
    }
    
    public RefundAsync(String pgpUrl, AsyncRefundDTO asyncRefundDTO) {
    	setMethod(BaseApi.MethodType.POST);
    	getRequestSpecBuilder()
    			.setContentType(ContentType.JSON)
    			.setAccept(ContentType.JSON)
    			.setBaseUri(pgpUrl)
    			.setBasePath("/refund/api/v1/async/refund")
    			.setBody(asyncRefundDTO);
    }

    public RefundAsync getSyncRefund(String pgpUrl, String body) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setBaseUri(pgpUrl)
                .setBasePath("/refund/api/v1/refund/apply/sync")
                .setBody(body);
        return this;
    }


}
