package com.paytm.utils.merchant.api.pgp.refund;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.dto.cachecardtoken.request.CacheCardDTO;
import com.paytm.utils.merchant.util.exception.pgpException.PGPException;
import io.restassured.http.ContentType;

public class CacheCardToken extends BaseApi {


    public CacheCardToken(String pgpUrl, String body) {
        setMethod(BaseApi.MethodType.POST);
        ObjectMapper objectMapper=new ObjectMapper();
        CacheCardDTO cacheCardDTO=null;
        try {
             cacheCardDTO = objectMapper.readValue(body, CacheCardDTO.class);
        }
        catch (Exception e) {
            throw new PGPException("Exception Occured while converting CacheCardDTO to String", e);
        }
        getRequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setBaseUri(pgpUrl)
                .setBasePath("/refund/api/v1/account/validate")
                .addQueryParam("mid",cacheCardDTO.getBody().getMid())
                .addQueryParam("requestId","qwerty")
                .setBody(body);
    }

    public CacheCardToken(String pgpUrl, CacheCardDTO cacheCardDTO) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setBaseUri(pgpUrl)
                .setBasePath("refund/api/v1/account/validate")
                .addQueryParam("mid",cacheCardDTO.getBody().getMid())
                .addQueryParam("requestId","qwerty")
                .setBody(cacheCardDTO);
    }

}
