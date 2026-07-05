package com.paytm.utils.merchant.merchant;

import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.utils.merchant.Constants;
import com.paytm.utils.merchant.api.CreateMerchantV2Api;
import com.paytm.utils.merchant.dto.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.fest.assertions.api.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by rahulkumar on Apr,2018
 */
public class NewV2Contract extends AbstractMerchantContract implements MerchantContract {
    private static final int MERCHANT_CREATION_TIMEOUT_IN_MS = 120000;

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

    public NewV2Contract(String custId, Configuration... configurations) {
        this.merchantConfig = new CreateMerchant()
                .setCustid(custId)
                .setCreateMerRequest(
                        new CreateMerRequest()
                                .setAction("Submit for Approval")
                                .setCreatedBy("sobeer_sales")
                                .setMerchantDetails(null)
                                .setUrlDetails(
                                        new ArrayList<>())
                                .setDocsDetails(null))
                .setConfigureMbidAndInstrument(
                        new ConfigureMbidAndInstrument()
                        .setPayModes(
                                new PAY_MODES().setPayMode("PPI"),
                                new PAY_MODES().setPayMode("CC"),
                                new PAY_MODES().setPayMode("DC"),
                                new PAY_MODES().setPayMode("NB"),
                                new PAY_MODES().setPayMode("UPI"),
                                new PAY_MODES().setPayMode("COD"),
                                new PAY_MODES().setPayMode("PAYTM_DIGITAL_CREDIT")
                        ))
                .setConfigVelocity(
                        new ConfigVelocity()
                                .setVelocities(
                                        new ArrayList<>()))
                .setConfigureMerchantCommission(null);

        for (Configuration configuration : configurations) {
            configuration.apply(this.merchantConfig);
        }
    }


    private void executeCreateMerchantAPI() {
        Response response = new CreateMerchantV2Api(this.merchantConfig).execute();
        Assertions.assertThat(response.statusCode()).as("Bad Status Code received from Create IMerchant API").isEqualTo(200);
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat(jsonPath.getString("STATUS")).isEqualToIgnoringCase("MID generation is in progress");
        Assertions.assertThat(jsonPath.getString("errorMessage")).isEqualToIgnoringCase("");
    }
    private void lookUpMidFromDB() {
        String requestId = this.merchantConfig.getCreateMerRequest().getMerchantDetails().getrEQUESTID();
        String sourceId = this.merchantConfig.getCreateMerRequest().getMerchantDetails().getsOURCEID();
        String query = "select MID from ENTITY_INFO where REQUEST_ID = '" + sourceId + requestId + "'";
        String dbUrl = Constants.PG_DB_CONNECTION;
        Long startTimeInMilliSeconds = System.currentTimeMillis();
        List<Map<String, Object>> result;

        while ((System.currentTimeMillis() - startTimeInMilliSeconds) < MERCHANT_CREATION_TIMEOUT_IN_MS) {
            result = DatabaseUtil.getInstance().executeSelectQuery(dbUrl, query);
            if (result.size() != 0) {
                this.mid = result.get(0).get("MID").toString();
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

