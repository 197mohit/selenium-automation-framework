package com.paytm.utils.merchant.merchant;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.api.pgp.admin.ConfigureMBId;
import com.paytm.utils.merchant.dto.BankConfig;
import com.paytm.utils.merchant.dto.CreateMerchant;
import com.paytm.utils.merchant.dto.PAY_MODES;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.fest.assertions.api.Assertions;

import java.util.List;

/**
 * Created by rahulkumar on Apr,2018
 */
public class BankConfiguration extends Configuration {

    private static final String[] CHANNELS = {"WEB", "WAP"};

    private final String action;
    private final String bankName;
    private final String industry;
    private final String payMode;
    private final String[] channel;
    private final String authMode;
    private final String mbid;
    private final String bankKey;
    private final boolean escrow;
    private final String params;

    public static Configuration HdfcCC() {
        return new BankConfiguration("Save", "HDFC", "Retail", "CC", CHANNELS,
                "3D", "70007981", "70007981", false, null);
    }

    public static Configuration HdfcDC() {
        return new BankConfiguration("Save", "HDFC", "Retail", "DC", CHANNELS,
                "3D", "70007981", "70007981", false, null);
    }

    public static Configuration HdfcCCSubs() {
        return new BankConfiguration("Save", "HDFC", "Retail", "CC", CHANNELS,
                "SUBS", "11000197", null, false, null);
    }

    public static Configuration HdfcNB() {
        return new BankConfiguration("Save", "HDFC", "Retail", "NB", CHANNELS,
                "USRPWD", "70007981", "70007981", false, null);
    }

    public static Configuration IciciNB() {
        return new BankConfiguration("Save", "ICICI", "Retail", "NB", CHANNELS,
                "USRPWD", "000000001141", null, false, "SPID=100000061642");
    }

    public static Configuration IciciUPI() {
        return new BankConfiguration("Save", "ICICI", "Retail", "UPI", CHANNELS,
                "USRPWD", "125004", null, false, null);
    }

    public static Configuration COD() {
        return new BankConfiguration("Save", "COD", "Retail", "COD", CHANNELS,
                "3D", "125004", null, false, null);
    }

    public static Configuration Wallet() {
        return new BankConfiguration("CreateAndSave", "WALLET", "Retail", "PPI", CHANNELS,
                "USRPWD", null, null, false, null);
    }

    public static Configuration PostPaid() {
        return new BankConfiguration("Save", "PAYTMCC", "Retail", "PAYTM_DIGITAL_CREDIT", CHANNELS,
                "USRPWD", "1234", null, false, null);
    }

    public static Configuration PPBL() {
        return new BankConfiguration("Save", "PPBL", "Retail", "NB", CHANNELS,
                "USRPWD", "pg", null, false, null);
    }

    private BankConfiguration(String action, String bankName, String industry, String payMode, String[] channel, String authMode, String mbid, String bankKey, boolean escrow, String params) {
        this.action = action;
        this.bankName = bankName;
        this.industry = industry;
        this.payMode = payMode;
        this.channel = channel;
        this.authMode = authMode;
        this.mbid = mbid;
        this.bankKey = bankKey;
        this.escrow = escrow;
        this.params = params;
    }

    @Override
    void apply(CreateMerchant merchantConfig) {
        List<PAY_MODES> pay_modesList = merchantConfig.getConfigureMbidAndInstrument().getPayModes();
        for(PAY_MODES payModes: pay_modesList){
            if(payModes.getPayMode().equalsIgnoreCase(this.payMode)){
                payModes.getBanks().add(
                        new BankConfig()
                                .setAction(action)
                                .setBank(bankName)
                                .setIndustry(industry)
                                .setChannel(channel)
                                .setAuthMode(authMode)
                                .setMbid(mbid)
                                .setBankKey(bankKey)
                                .setEscrow(escrow)
                                .setParams(params)
                );
            }
        }
    }

    @Override
    void modify(String mid) {
        JsonObject mbidDetails = new JsonObject();
        JsonArray payModeArray = new JsonArray();
        payModeArray.add(payMode);
        JsonArray channelArray = new JsonArray();
        for (String channel : channel) {
            channelArray.add(channel);
        }

        mbidDetails.addProperty("MID", mid);
        mbidDetails.addProperty("BANK", bankName);
        mbidDetails.addProperty("INDUSTRY", industry);
        mbidDetails.add("PAY_MODE", payModeArray);
        mbidDetails.add("CHANNEL", channelArray);
        mbidDetails.addProperty("MBID", mbid);
        mbidDetails.addProperty("AUTH", authMode);
        mbidDetails.addProperty("BANK_KEY", bankKey);
        mbidDetails.addProperty("PARAMS", params);
        mbidDetails.addProperty("ESCROW_FLAG", escrow);

        JsonObject config = new JsonObject();
        config.addProperty("ACTION", action);
        config.add("MBID_DETAILS", mbidDetails);

        BaseApi request = new ConfigureMBId(config.toString());
        Response response = request.execute();
        Assertions.assertThat(response.statusCode()).isEqualTo(200);
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat(jsonPath.getString("STATUS")).isEqualToIgnoringCase("SUCCESS");
    }
}
