package com.paytm.utils.merchant.api.pgp.bo;

import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.dto.bo.PGPlusBODTO;
import io.restassured.http.ContentType;

public class PGPlusBO extends BaseApi{


        public PGPlusBO(String pgpUrl, String body) {
            setMethod(BaseApi.MethodType.POST);
            getRequestSpecBuilder()
                    .setContentType(ContentType.JSON)
                    .setAccept(ContentType.JSON)
                    .setBaseUri(pgpUrl)
                    .setBasePath("pg-plus-bo/search/refund")
                    .setBody(body);
        }

        public PGPlusBO(String pgpUrl, PGPlusBODTO pgPlusBODTO) {
            setMethod(BaseApi.MethodType.POST);
            getRequestSpecBuilder()
                    .setContentType(ContentType.JSON)
                    .setAccept(ContentType.JSON)
                    .setBaseUri(pgpUrl)
                    .setBasePath("pg-plus-bo/search/refund")
                    .setBody(pgPlusBODTO);
        }


    }



