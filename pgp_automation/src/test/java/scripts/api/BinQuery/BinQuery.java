package scripts.api.BinQuery;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import io.restassured.http.ContentType;

public class BinQuery extends BaseApi{

    public BinQuery(String binNumber) {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.STS_HOST);
        getRequestSpecBuilder().setBasePath(Constants.BinCenter.CARD_BIN);
        getRequestSpecBuilder().addPathParams("bin", binNumber);
        getRequestSpecBuilder().setBasePath(Constants.BinCenter.CARD_BIN);

    }
}
