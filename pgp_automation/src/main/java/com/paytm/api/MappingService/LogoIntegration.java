/**
 * @author : Samar Aswal
 * @desc : This class is used to automate all the Api of logo integration
 */

package com.paytm.api.MappingService;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.logoIntegeration.UpdateLogoBody;
import com.paytm.framework.api.BaseApi;
import com.paytm.framework.datareader.DataReaderUtil;
import com.paytm.framework.utils.DatabaseUtil;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.Reporter;

import java.util.HashMap;
import java.util.Map;

public class LogoIntegration extends BaseApi {

    /**
     * @author : Samar Aswal
     * @desc : This is a object of PGPHelpers class
     */
    private PGPHelpers pgpHelpers;

    /**
     * @author : Samar Aswal
     * @desc : This is a object of UpdateLogoBody class
     */
    private UpdateLogoBody updateLogoBody;

    /**
     * @author : Samar Aswal
     * @return : Map
     * @desc : This function is used to get JWT payload
     */
    private Map<String, String> logoClaim(){
        Map<String, String> map = new HashMap<>();
        map.put("clientId", LocalConfig.JWT_LOGO_CLIENTID);
        map.put("iss", PGPHelpers.ISSUER.ts.toString());
        return map;
    }


    /**
     * @author : Samar Aswal
     * @param logoType : This variable is used to store logoType which you want to upload
     * @param identifier : This variable is used to store identifier which you want to upload
     * @param subIdentifier : This variable is used to store sub identifier which you want to upload
     * @desc : This function is used to hit upload logo Api
     */
    public void uploadLogo(String logoType, String identifier, String subIdentifier){
        pgpHelpers = new PGPHelpers();
        updateLogoBody = new UpdateLogoBody();
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("x-jwt-token", pgpHelpers.createJsonWebToken(logoClaim(),
                PGPHelpers.ISSUER.ts, LocalConfig.JWT_KEY_LOGO));
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        //getMerchantCategoryDetails();
        String endPoint = Constants.logoUrl.uploadLogo + logoType + "/" + identifier;
        getRequestSpecBuilder().setBasePath(endPoint);
        if(subIdentifier != null && !subIdentifier.isEmpty()){
            updateLogoBody.setSubId(subIdentifier);
            updateLogoBody.setFileName(subIdentifier + String.valueOf(System.currentTimeMillis()) + ".png");
        }
        else {
            updateLogoBody.setFileName(identifier + String.valueOf(System.currentTimeMillis()) + ".png");
        }
        getRequestSpecBuilder().setBody(updateLogoBody);
        Response response = execute();
        int statusCode = response.statusCode();
        Reporter.log("Actual status = " + statusCode, true);
        Assert.assertEquals(statusCode, 200,"Actual status code = " + statusCode);
        String responseMessage =response.getBody().jsonPath().getString("response.messaage");
        Assert.assertEquals(responseMessage, "Success");
        int logoStatus = pgpHelpers.getLogoStatus(LocalConfig.PG_DB_CONNECTION_URL, identifier, subIdentifier);
        Assert.assertEquals(logoStatus, 1,"Actual status of logo = " + logoStatus);
    }



    /**
     * @author : Samar Aswal
     * @param logoType : This variable is used to store logoType which you want to fetch/check
     * @param identifier : This variable is used to store identifier which you want to fetch/check
     * @param subIdentifier : This variable is used to store sub identifier which you want to fetch/check
     * @desc : This function is used to hit fetch and check Api
     */
    public void getLogo(String logoType, String identifier, String subIdentifier, String apiName){
        pgpHelpers = new PGPHelpers();
        setMethod(MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().addHeader("x-jwt-token", pgpHelpers.createJsonWebToken(logoClaim(),
                PGPHelpers.ISSUER.ts, LocalConfig.JWT_KEY_LOGO));
        if(subIdentifier != null && !subIdentifier.isEmpty()){
            getRequestSpecBuilder().addParam("subId", subIdentifier);
        }
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String endPoint = null;
        if(!apiName.isEmpty() && apiName != null) {
            if(apiName.equalsIgnoreCase("check")) {
                endPoint = Constants.logoUrl.checkLogo + logoType + "/" + identifier;
            } else{
                endPoint = Constants.logoUrl.fetchLogo + logoType + "/" + identifier;
            }
        }
        if(endPoint != null && !endPoint.isEmpty()) {
            getRequestSpecBuilder().setBasePath(endPoint);
        }
        else {
            Reporter.log("End point is empty or null", true);
        }
        Response response = execute();
        int statusCode = response.statusCode();
        Assert.assertEquals(statusCode, 200,"Actual status code = " + statusCode);
        String responseMessage =response.getBody().jsonPath().getString("response.messaage");
        Assert.assertEquals(responseMessage, "Success");
    }


    /**
     * @author : Samar Aswal
     * @param logoType : This variable is used to store logoType which you want to delete
     * @param identifier : This variable is used to store identifier which you want to delete
     * @param subIdentifier : This variable is used to store sub identifier which you want to delete
     * @desc : This function is used to hit delete logo Api
     */
    public void deleteLogo(String logoType, String identifier, String subIdentifier){
        setMethod(MethodType.DELETE);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().addHeader("x-jwt-token", pgpHelpers.createJsonWebToken(logoClaim(),
                PGPHelpers.ISSUER.ts, LocalConfig.JWT_KEY_LOGO));
        if(subIdentifier != null && !subIdentifier.isEmpty()){
            getRequestSpecBuilder().addParam("subId", subIdentifier);
        }
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String endPoint = Constants.logoUrl.deleteLogo + logoType + "/" + identifier;
        getRequestSpecBuilder().setBasePath(endPoint);
        Response response = execute();
        int statusCode = response.statusCode();
        Reporter.log("Actual status = " + statusCode, true);
        Assert.assertEquals(statusCode, 200,"Actual status code = " + statusCode);
        String responseMessage =response.getBody().jsonPath().getString("response.messaage");
        Assert.assertEquals(responseMessage, "Success");

        //Asserting null because the record is deleted after the delete api is called
        try {
            int logoStatus = pgpHelpers.getLogoStatus(LocalConfig.PG_DB_CONNECTION_URL, identifier, subIdentifier);
        }
        catch (NullPointerException nullPointerException){
            Assertions.assertThatNullPointerException();
        }
    }

    /**
     * @author : Samar Aswal
     * @param mid : Merchant Id
     * @param PGPDBUrl : Db URl
     * @return String
     * @desc : This function is used to get Entity Id
     */
    private String getEntityId(String mid, String PGPDBUrl){
        String entityId = "";
        String query = "Select ENTITY_ID from PAYTMPGDB.MIGRATION_MID WHERE MID = '" + mid + "'";
        entityId = DatabaseUtil.getInstance().executeSelectQuery(PGPDBUrl, query).toString();
        return entityId;
    }

    /*
    private String merchantCategory;
    private String merchatSubCategory;

    private void getMerchantCategoryDetails(){
        String entityId = getEntityId(LocalConfig.MERCHANT_LOGO_MID, LocalConfig.PGP_DB_CONNECTION_URL);
        String getCategoryQuery = "SELECT CATEGORY from PAYTMPGDB.ENTITY_DEMOGRAPHICS WHERE ENTITY_ID = '" + entityId + "'";
        String getSubCategoryQuery = "SELECT SUB_CATEGORY from PAYTMPGDB.ENTITY_DEMOGRAPHICS WHERE ENTITY_ID = '" + entityId + "'";
        merchantCategory = DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL,
                getCategoryQuery).toString();
        merchatSubCategory = DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL,
                getSubCategoryQuery).toString();
    }
    */

    public void deletelogoDbEntry(){

    }
}
