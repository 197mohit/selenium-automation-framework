package com.paytm.utils.merchant.merchant;

import com.paytm.utils.merchant.api.GetMerchantApi;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.fest.assertions.api.Assertions;

/**
 * Created by deepakkumar on 26/11/17.
 */
public final class ExistingMerchantContract extends AbstractMerchantContract {

    private boolean isConfigModified = false;
    private JsonPath jsonPath;

    public ExistingMerchantContract(final String mid) {
        this.mid = mid;
    }

    public void apply(Configuration config) {
        config.modify(mid);
        new SingleEventMigration(mid).waitTillCompletion();
        isConfigModified = true;
    }

    public void apply(Configuration... configs) {
        for (Configuration config : configs) {
            config.modify(mid);
        }
        new MultiEventMigration(mid, configs.length).waitTillCompletion();
        isConfigModified = true;
    }

    public boolean isAddnPay() {
        setMerchantConfig();
        String value = jsonPath.get("MERCHANT-PREFERENCE-INFO.merchantPreferenceInfos[2].prefValue");
        return value.equalsIgnoreCase("Y") ? true : false;
    }

    public boolean isHybrid() {
        setMerchantConfig();
        String value = jsonPath.get("MERCHANT-PREFERENCE-INFO.merchantPreferenceInfos[3].prefValue");
        return value.equalsIgnoreCase("Y") ? true : false;
    }

    public int noOfRetry() {
        setMerchantConfig();
        return jsonPath.getInt("MERCHANT-EXTENDED-INFO.extendedInfo.numberOfRetry");
    }

    private void setMerchantConfig() {
        if ((jsonPath == null) || (isConfigModified == true)) {
            getLatestMerchantConfigFromServer();
            isConfigModified = false;
        }
    }

    private void getLatestMerchantConfigFromServer() {
        Response response = new GetMerchantApi(mid).execute();
        Assertions.assertThat(response.statusCode()).isEqualTo(200);
        jsonPath = response.jsonPath();
    }

}
