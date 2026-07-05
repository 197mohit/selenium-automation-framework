package scripts;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class FetchValidBinsAPI extends BaseApi {

    public void FetchValidBinsAPIHelper(String bankId) {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingService.FETCH_VALID_BINS.replace("{bankId}",bankId));
    }

    public void FetchValidBinAPIWithIsOfus(String bankId,String ISOFUS){
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingService.FETCH_VALID_BINS_WITH_ISOFFUS.replace("{bankId}",bankId).replace("{ISOFUS}",ISOFUS));
    }
}
