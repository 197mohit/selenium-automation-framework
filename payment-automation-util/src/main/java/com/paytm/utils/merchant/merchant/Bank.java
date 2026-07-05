package com.paytm.utils.merchant.merchant;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.UtilConstants;
import com.paytm.utils.merchant.api.pgp.admin.ConfigureMBId;
import com.paytm.utils.merchant.dto.BankConfig;
import com.paytm.utils.merchant.dto.CreateMerchant;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.fest.assertions.api.Assertions;

public class Bank extends Configuration {

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
        return new Bank("Save", UtilConstants.BankName.HDFC.toString(), "Retail", "CC", CHANNELS,
                "3D", "70007981", "70007981", false, null);
    }

    public static Configuration HdfcDC() {
        return new Bank("Save", UtilConstants.BankName.HDFC.toString(), "Retail", "DC", CHANNELS,
                "3D", "70007981", "70007981", false, null);
    }

    public static Configuration HdfcCCSubs() {
        return new Bank("Save", UtilConstants.BankName.HDFC.toString(), "Retail", "CC", CHANNELS,
                "SUBS", "11000197", null, false, null);
    }

    public static Configuration HdfcNB() {
        return new Bank("Save", UtilConstants.BankName.HDFC.toString(), "Retail", "NB", CHANNELS,
                "USRPWD", "70007981", "70007981", false, null);
    }

    public static Configuration IciciNB() {
        return new Bank("Save", UtilConstants.BankName.ICICI.toString(), "Retail", "NB", CHANNELS,
                "USRPWD", "000000001141", null, false, "SPID=100000061642");
    }

    public static Configuration IciciUPI() {
        return new Bank("Save", UtilConstants.BankName.ICICI.toString(), "Retail", "UPI", CHANNELS,
                "USRPWD", "125004", null, false, null);
    }

    public static Configuration COD() {
        return new Bank("Save", UtilConstants.BankName.COD.toString(), "Retail", "COD", CHANNELS,
                "3D", "125004", null, false, null);
    }

    public static Configuration Wallet() {
        return new Bank("CreateAndSave", UtilConstants.BankName.WALLET.toString(), "Retail", "PPI", CHANNELS,
                "USRPWD", null, null, false, null);
    }

    public static Configuration PostPaid() {
        return new Bank("Save", UtilConstants.BankName.PAYTMCC.toString(), "Retail", "PAYTM_DIGITAL_CREDIT", CHANNELS,
                "USRPWD", "1234", null, false, null);
    }

    public static Configuration PPBL() {
        return new Bank("Save", UtilConstants.BankName.PPBL.toString(), "Retail", "NB", CHANNELS,
                "USRPWD", "pg1234", "DT0jfXEOYMqkHZtYrKi7o4qjj1GmZN09", false, "MCC=1234;");
    }

    public static Configuration UPI_PUSH() {
        return  new Bank("Save", UtilConstants.BankName.PPBL.toString(), "Retail", "UPI", CHANNELS, "USRPWD",
                    "1234", null, false, "MCC=7221;MERCHANT_VPA=vpaMerchant@paytm");
    }

    public static Configuration Icici_Direct_CC_DC() {
        return new Bank("Save", UtilConstants.BankName.ICICI_DIRECT.toString(), "Retail", "CC/DC", CHANNELS, "3D",
                "44361207", null, false, null);
    }

    @Deprecated
    /**
     * Bank configuration not working
     */
    public static Configuration Icici_CC_DC() {
        return new Bank("Save", UtilConstants.BankName.ICICI.toString(), "Retail", "CC/DC", CHANNELS, "3D",
                "00005344", null, false, null);
    }

    public static Configuration SBI_NB() {
        return new Bank("Save", UtilConstants.BankName.SBI.toString(), "Retail", "NB", CHANNELS, "USRPWD",
                "PAYTM_SHOP97", null, false, null);
    }

    public static Configuration SBI_CC() {
        return new Bank("Save", UtilConstants.BankName.SBI.toString(), "Retail", "CC", CHANNELS, "3D",
                "00003332", "000000000240754", false, null);
    }

    public static Configuration HDFC_NB() {
        return new Bank("Save", UtilConstants.BankName.HDFC.toString(), "Retail", "NB", CHANNELS, "USRPWD",
            "PaytmMobileS", "123456", false, null);
    }

    public static Configuration Bajaj_Finserv_EMI() {
        return new Bank("Save", UtilConstants.BankName.HDFC.toString(), "Retail", "EMI", CHANNELS, "3D",
                null, null, false, "DealerID=320221");
    }

    public static Configuration ICICI_EMI() {
        return new Bank("Save", UtilConstants.BankName.HDFC.toString(), "Retail", "EMI_DC", CHANNELS, "3D",
                null, null, false, "MPID=5536");
    }

    public static Configuration AMEX_EMI() {
        return new Bank("Save", UtilConstants.BankName.HDFC.toString(), "Retail", "EMI", CHANNELS, "3D",
                null, null, false, "DealerID=320221");
    }

    public static Configuration HDFC_EMI() {
        return new Bank("Save", UtilConstants.BankName.HDFC.toString(), "Retail", "EMI", CHANNELS, "3D",
                null, null, false, null);
    }

    private Bank(String action, String bankName, String industry, String payMode, String[] channel, String authMode, String mbid, String bankKey, boolean escrow, String params) {
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
        merchantConfig
                .getConfigureMbid()
                .getBank()
                .add(new BankConfig()
                        .setAction(action)
                        .setBank(bankName)
                        .setIndustry(industry)
                        .setPayMode(payMode)
                        .setChannel(channel)
                        .setAuthMode(authMode)
                        .setMbid(mbid)
                        .setBankKey(bankKey)
                        .setEscrow(escrow)
                        .setParams(params));
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
