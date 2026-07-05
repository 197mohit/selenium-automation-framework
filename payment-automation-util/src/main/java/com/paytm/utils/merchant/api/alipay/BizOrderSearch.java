package com.paytm.utils.merchant.api.alipay;

import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import com.paytm.utils.merchant.dto.alipay.BizOrderSearchReqDTO;
import io.restassured.http.ContentType;

/**
 * Created by anjukumari on 17/04/19
 */
public class BizOrderSearch extends BaseApi {
    private static final String BASE_URI = Constants.ALIPAY;
    private static final String BASE_PATH = "alipayplus/dataservice/bizorder/search.htm";

    public BizOrderSearch(String txnId, String searchType) {
        BizOrderSearchReqDTO bizOrderSearchReqDTO = new BizOrderSearchReqDTO(searchType, txnId);
        setMethod(MethodType.POST);
        getRequestSpecBuilder()
                .setBaseUri(BASE_URI)
                .setBasePath(BASE_PATH)
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .setBody(bizOrderSearchReqDTO);
    }


    public BizOrderSearch(String txnId) {
        BizOrderSearchReqDTO bizOrderSearchReqDTO = new BizOrderSearchReqDTO(txnId);
        setMethod(MethodType.POST);
        getRequestSpecBuilder()
                .setBaseUri(BASE_URI)
                .setBasePath(BASE_PATH)
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .setBody(bizOrderSearchReqDTO);
    }


}
