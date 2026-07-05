package scripts.api.theia;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class BankMandateListAPI extends BaseApi {

    public BankMandateListAPI() {
        APIBuilder:
        {
            setMethod(MethodType.GET);
            getRequestSpecBuilder().setContentType(ContentType.JSON);
            getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
            getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
            getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.BANK_MANDATE_LIST);

        }
    }
}