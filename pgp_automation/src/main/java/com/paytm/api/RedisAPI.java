package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;

public class RedisAPI extends BaseApi {

    private RedisAPI(String redisKey) {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingService.REMOVE_REDIS_KEY.replace("{redisKey}",redisKey));
        getRequestSpecBuilder().setContentType("*/*");
    }

    public static void deleteKey(String redisKey)
    {
        Response response =  new RedisAPI(redisKey).execute();
        Assertions.assertThat(response.getStatusCode()).as(" Mapping service - redisKey remove API not working correctly").isEqualTo(200);
        Assertions.assertThat(response.asString()).as("Mapping service - redisKey remove API not working correctly").isEqualTo("SUCCESS");


    }
}
