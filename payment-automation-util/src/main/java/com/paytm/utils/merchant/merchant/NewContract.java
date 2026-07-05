package com.paytm.utils.merchant.merchant;

import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.utils.merchant.Constants;
import com.paytm.utils.merchant.DbQueries;
import com.paytm.utils.merchant.api.CreateMerchantApi;
import com.paytm.utils.merchant.api.CreateMerchantV2Api;
import com.paytm.utils.merchant.dto.ConfigVelocity;
import com.paytm.utils.merchant.dto.ConfigureMBID;
import com.paytm.utils.merchant.dto.CreateMerRequest;
import com.paytm.utils.merchant.dto.CreateMerchant;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang.RandomStringUtils;
import org.fest.assertions.api.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by deepakkumar on 11/12/17.
 */
public final class NewContract extends AbstractMerchantContract implements MerchantContract {

    private static final int MERCHANT_CREATION_TIMEOUT_IN_MS = 120000;

    public NewContract(Configuration... configurations) {
        this(RandomStringUtils.randomNumeric(10), configurations);
    }

    public NewContract(String custId, Configuration... configurations) {
        this.merchantConfig = new CreateMerchant()
                .setCustid(custId)
                .setSfCallbackUrl("/services/apexrest/Merchant/sdMidInfo")
                .setIpAddr("127.0.0.1")
                .setCreateMerRequest(
                        new CreateMerRequest()
                                .setAction("Submit for Approval")
                                .setCreatedBy("sobeer_sales")
                                .setMerchantDetails(null)
                                .setUrlDetails(
                                        new ArrayList<>())
                                .setDocsDetails(null))
                .setConfigureMbid(
                        new ConfigureMBID()
                                .setBank(
                                        new ArrayList<>()))
                .setConfigVelocity(
                        new ConfigVelocity()
                                .setVelocities(
                                        new ArrayList<>()))
                .setConfigureMerchantCommission(null);

        for (Configuration configuration : configurations) {
            configuration.apply(this.merchantConfig);
        }
    }

    @Override
    public String create() {
        executeCreateMerchantAPI();
        lookUpMidFromDB();
        return mid;
    }

    @Override
    public String createWithoutDBcheck() {
        executeCreateMerchantAPI();
        return mid;
    }

    public CreateMerchant getMerchantConfig() {
        return this.merchantConfig;
    }

    public String createV2()
    {
        executeCreateMerchantV2Api();
        lookUpMidFromDB();
        return mid;
    }
    private  void executeCreateMerchantV2Api()
    {
        Response response=new CreateMerchantV2Api(this.merchantConfig).execute();
        Assertions.assertThat(response.statusCode()).as("Bad Status Code received from Create IMerchant API").isEqualTo(200);
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat(jsonPath.getString("STATUS")).isEqualToIgnoringCase("MID generation is in progress");
        Assertions.assertThat(jsonPath.getString("errorMessage")).isEqualToIgnoringCase("");

    }
    private void executeCreateMerchantAPI() {
        Response response = new CreateMerchantApi(this.merchantConfig).execute();
        Assertions.assertThat(response.statusCode()).as("Bad Status Code received from Create IMerchant API").isEqualTo(200);
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat(jsonPath.getString("STATUS")).isEqualToIgnoringCase("MID generation is in progress");
        Assertions.assertThat(jsonPath.getString("errorMessage")).isEqualToIgnoringCase("");
    }

    public String getEntityIdFromMid(String mid) {
        String query = DbQueries.ENTITY_ID_FROM_ENTITY_INFO(mid);
        String dbUrl = Constants.PG_DB_CONNECTION;
        Long startTimeInMilliSeconds = System.currentTimeMillis();
        List<Map<String, Object>> result;
        String entity_id = "";
        Reporter.report.info("Executing DB query: "+query);
        while ((System.currentTimeMillis() - startTimeInMilliSeconds) < MERCHANT_CREATION_TIMEOUT_IN_MS) {
            result = DatabaseUtil.getInstance().executeSelectQuery(dbUrl, query);
            if (result.size() != 0) {
                entity_id = result.get(0).get("ID").toString();
                Reporter.report.info("entity_id is: "+entity_id);
                return entity_id;
            }
            waitFor(3);
        }
        if (entity_id == "" || entity_id == null) {
            throw new RuntimeException("Entity Id not found with in specified time");
        }
        return entity_id;
    }

    public String lookUpMidFromDB(CreateMerchant merchantConfig) {
        String requestId = merchantConfig.getCreateMerRequest().getMerchantDetails().getrEQUESTID();
        String sourceId = merchantConfig.getCreateMerRequest().getMerchantDetails().getsOURCEID();
        String query = DbQueries.MID_FROM_REQUESTID(sourceId, requestId);
        String dbUrl = Constants.PG_DB_CONNECTION;
        Long startTimeInMilliSeconds = System.currentTimeMillis();
        List<Map<String, Object>> result;
        String mid = "";
        Reporter.report.info("Executing DB query "+query);

        while ((System.currentTimeMillis() - startTimeInMilliSeconds) < MERCHANT_CREATION_TIMEOUT_IN_MS) {
            result = DatabaseUtil.getInstance().executeSelectQuery(dbUrl, query);
            if (result.size() != 0) {
                mid = result.get(0).get("MID").toString();
                Reporter.report.info("Merchant MID is: "+mid);
                return mid;
            }
            waitFor(3);
        }

        if (this.mid == null) {
            throw new RuntimeException("Merchant not created within specified time");
        }
        return mid;
    }

    private void lookUpMidFromDB() {
        String requestId = this.merchantConfig.getCreateMerRequest().getMerchantDetails().getrEQUESTID();
        String sourceId = this.merchantConfig.getCreateMerRequest().getMerchantDetails().getsOURCEID();
        String query = "select MID from ENTITY_INFO where REQUEST_ID = '" + sourceId + requestId + "'";
        String dbUrl = Constants.PG_DB_CONNECTION;
        Long startTimeInMilliSeconds = System.currentTimeMillis();
        List<Map<String, Object>> result;
        Reporter.report.info("Executing DB query "+query);

        while ((System.currentTimeMillis() - startTimeInMilliSeconds) < MERCHANT_CREATION_TIMEOUT_IN_MS) {
            result = DatabaseUtil.getInstance().executeSelectQuery(dbUrl, query);
            if (result.size() != 0) {
                this.mid = result.get(0).get("MID").toString();
                Reporter.report.info("Merchant MID is: "+this.mid);
                break;
            }
            waitFor(3);
        }

        if (this.mid == null) {
            throw new RuntimeException("Merchant not created within specified time");
        }
    }

    private void waitFor(int timeInSeconds) {
        try {
            Thread.sleep(timeInSeconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}