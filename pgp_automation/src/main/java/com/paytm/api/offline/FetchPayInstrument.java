package com.paytm.api.offline;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.OfflineDto.FetchPayInstrumentRequest;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * Created by anjukumari on 04/12/18
 */

public class FetchPayInstrument extends BaseApi{
    public FetchPayInstrument(FetchPayInstrumentRequest fetchPayInstrument) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.OfflineTxn.FETCH_PAYMENT_INSTRUMENT);
        getRequestSpecBuilder().setBody(fetchPayInstrument);
    }

    public static Response executeFetchPaymtInstrument(FetchPayInstrumentRequest fetchPayInstrument) {
        Response response =  new FetchPayInstrument(fetchPayInstrument).execute();
        return response;
    }

}
