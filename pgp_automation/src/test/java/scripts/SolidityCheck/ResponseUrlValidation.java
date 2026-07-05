package scripts.SolidityCheck;

import com.paytm.LocalConfig;
import com.paytm.framework.utils.DatabaseUtil;
import io.qameta.allure.Owner;
import org.assertj.core.api.SoftAssertions;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.Map;

import static com.paytm.framework.reporting.Reporter.report;

/**
 * Created by anjukumari on 10/07/18
 */

@Owner("Gagandeep")
public class ResponseUrlValidation extends BankDetailsQueries{

    @Test(description = "Validate bank ResponseUrl in DB for HDFC")
    public void Validate_HDFC_ResponseUrl(){
        SoftAssertions softAssert = new SoftAssertions();
        String channels = "WAP,WEB";
        String payMethods = "CC,DC";
        bankName = "HDFC";
        payMethodList = payMethods.split(",");
        channelList = channels.split(",");
        //check web response url
        for (int i = 0; i < payMethodList.length; i++) {
            for (int j = 0; j < channelList.length; j++) {
                setBankDetails(channelList[j], bankName, payMethodList[i]);
                System.out.println("Response : ");
                String sql_query_web = getUpdatedStaring(sql_query_web_response, bankId, payMethodId, channelId);
                String webResponseUrl = LocalConfig.PGP_HOST+"/instaproxy/bankresponse/"+bankName+"/"+payMethodList[i]+"/RESP";
                    Map<String, Object> urlMap = DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, sql_query_web).get(0);
                    report.info("SQL qury for WEB_RESPONSE_URL: "+sql_query_web);
                    String webResponseUrl_DB;
                    webResponseUrl_DB = urlMap.get("WEB_RESPONSE_URL").toString();
                    report.info("WEB RESPONSE URL IN DB: "+webResponseUrl_DB);
                    softAssert.assertThat(webResponseUrl_DB)
                            .isEqualTo(webResponseUrl);
            }
        }
        softAssert.assertAll();
    }

    @Test(description = "Validate bank ResponseUrl in DB for ICICI")
    public void Validate_ICICI_ResponseUrl(){
        SoftAssertions softAssert = new SoftAssertions();
        String channels = "WEB";
        String payMethods = "CC,DC,NB,UPI";
        bankName = "ICICI";
        payMethodList = payMethods.split(",");
        channelList = channels.split(",");
        //check web response url
        for (int i = 0; i < payMethodList.length; i++) {
            for (int j = 0; j < channelList.length; j++) {
                setBankDetails(channelList[j], bankName, payMethodList[i]);
                String sql_query_web = getUpdatedStaring(sql_query_web_response, bankId, payMethodId, channelId);
                String webResponseUrl = LocalConfig.PGP_HOST + "/instaproxy/bankresponse/" + bankName + "/" + payMethodList[i] + "/RESP";
                    Map<String, Object> urlMap = DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, sql_query_web).get(0);
                    report.info("SQL query for WEB_RESPONSE_URL: "+sql_query_web);
                    String webResponseUrl_DB = urlMap.get("WEB_RESPONSE_URL").toString();
                    report.info("WEB RESPONSE URL IN DB: "+webResponseUrl_DB);
                    softAssert.assertThat(webResponseUrl_DB)
                            .isEqualTo(webResponseUrl);
            }
        }
        softAssert.assertAll();
    }


    @Test(description = "Validate bank ResponseUrl in DB for PAYTMCC")
    public void Validate_PAYTMCC_ResponseUrl(){
        SoftAssertions softAssert = new SoftAssertions();
        String channels = "WAP,WEB";
        String payMethods = "PAYTM_DIGITAL_CREDIT";
        bankName = "PAYTMCC";
        payMethodList = payMethods.split(",");
        channelList = channels.split(",");
        //check web response url
        for (int i = 0; i < payMethodList.length; i++) {
            for (int j = 0; j < channelList.length; j++) {
                setBankDetails(channelList[j], bankName, payMethodList[i]);
                String sql_query_web = getUpdatedStaring(sql_query_web_response, bankId, payMethodId, channelId);
                String webResponseUrl = LocalConfig.PGP_HOST+"/instaproxy/bankresponse/"+bankName+"/PAYTM_DIGITAL_CREDIT/RESP";
                    Map<String, Object> urlMap = DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, sql_query_web).get(0);
                    report.info("SQL query for WEB_RESPONSE_URL: "+sql_query_web);
                    String webResponseUrl_DB = urlMap.get("WEB_RESPONSE_URL").toString();
                    report.info("WEB RESPONSE URL IN DB: "+webResponseUrl_DB);
                    softAssert.assertThat(webResponseUrl_DB)
                            .isEqualTo(webResponseUrl);
            }
        }
        softAssert.assertAll();
    }


    @Test(description = "Validate bank ResponseUrl in DB for AMEX")
    public void Validate_AMEX_ResponseUrl(){
        SoftAssertions softAssert = new SoftAssertions();
        String channels = "WAP,WEB";
        String payMethods = "CC";
        bankName = "AMEX";
        payMethodList = payMethods.split(",");
        channelList = channels.split(",");
        //check web response url
        for (int i = 0; i < payMethodList.length; i++) {
            for (int j = 0; j < channelList.length; j++) {
                setBankDetails(channelList[j], bankName, payMethodList[i]);
                String sql_query_web = getUpdatedStaring(sql_query_web_response, bankId, payMethodId, channelId);
                String webResponseUrl = LocalConfig.PGP_HOST+"/instaproxy/bankresponse/"+bankName+"/"+payMethodList[i]+"/RESP";
                    Map<String, Object> urlMap = DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, sql_query_web).get(0);
                    report.info("SQL query for WEB_RESPONSE_URL: "+sql_query_web);
                    String webResponseUrl_DB = urlMap.get("WEB_RESPONSE_URL").toString();
                    report.info("WEB RESPONSE URL IN DB: "+webResponseUrl_DB);
                    softAssert.assertThat(webResponseUrl_DB)
                            .isEqualTo(webResponseUrl);
            }
        }
        softAssert.assertAll();
    }


}
