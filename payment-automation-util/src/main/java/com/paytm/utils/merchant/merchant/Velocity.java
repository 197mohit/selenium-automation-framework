package com.paytm.utils.merchant.merchant;

import com.google.gson.JsonObject;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.api.pgp.admin.ConfigVelocity;
import com.paytm.utils.merchant.dto.CreateMerchant;
import com.paytm.utils.merchant.dto.Velocities;
import com.paytm.utils.merchant.dto.VelocityDetails;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.fest.assertions.api.Assertions;

/**
 * Created by deepakkumar on 26/11/17.
 */
public final class Velocity extends Configuration {

    private String velocityType;
    private Integer maxAmtPerTxn;
    private Integer maxAmtPerDay;
    private Integer maxAmtPerWeek;
    private Integer maxAmtPerMonth;
    private Integer maxTxnPerDay;
    private Integer maxTxnPerWeek;
    private Integer maxTxnPerMonth;


    private Velocity(String velocityType, Integer maxAmtPerTxn, Integer maxAmtPerDay, Integer maxAmtPerWeek,
                     Integer maxAmtPerMonth, Integer maxTxnPerDay, Integer maxTxnPerWeek, Integer maxTxnPerMonth) {
        this.velocityType = velocityType;
        this.maxAmtPerTxn = maxAmtPerTxn;
        this.maxAmtPerDay = maxAmtPerDay;
        this.maxAmtPerWeek = maxAmtPerWeek;
        this.maxAmtPerMonth = maxAmtPerMonth;
        this.maxTxnPerDay = maxTxnPerDay;
        this.maxTxnPerWeek = maxTxnPerWeek;
        this.maxTxnPerMonth = maxTxnPerMonth;
    }

    public static Velocity Overall() {
        return new Velocity("OVERALL", 1000, 10000, 50000,
                100000, 1000, 1000, 1000);
    }

    public static Velocity NB() {
        return new Velocity("NB", 1000, 10000, null,
                null, 1000, 1000, null);
    }

    public static Velocity Card() {
        return new Velocity("CARD", 1000, 10000, 50000,
                100000, 1000, 1000, 1000);
    }

    public static Velocity Sso() {
        return new Velocity("SSO", 1000, 10000, 50000,
                100000, 1000, 1000, 1000);
    }

    public static Velocity AddMoney() {

        return new Velocity("ADDMONEY", 1000, 10000, 50000,
                100000, 1000, 1000, 1000);
    }

    public Velocity setMaxAmtPerTxn(Integer maxAmtPerTxn) {
        this.maxAmtPerTxn = maxAmtPerTxn;
        return this;
    }

    public Velocity setMaxAmtPerDay(Integer maxAmtPerDay) {
        this.maxAmtPerDay = maxAmtPerDay;
        return this;
    }

    public Velocity setMaxAmtPerWeek(Integer maxAmtPerWeek) {
        this.maxAmtPerWeek = maxAmtPerWeek;
        return this;
    }

    public Velocity setMaxAmtPerMonth(Integer maxAmtPerMonth) {
        this.maxAmtPerMonth = maxAmtPerMonth;
        return this;
    }

    public Velocity setMaxTxnPerDay(Integer maxTxnPerDay) {
        this.maxTxnPerDay = maxTxnPerDay;
        return this;
    }

    public Velocity setMaxTxnPerWeek(Integer maxTxnPerWeek) {
        this.maxTxnPerWeek = maxTxnPerWeek;
        return this;
    }

    public Velocity setMaxTxnPerMonth(Integer maxTxnPerMonth) {
        this.maxTxnPerMonth = maxTxnPerMonth;
        return this;
    }

    @Override
    void apply(final CreateMerchant merchantConfig) {
        Velocities config =
                new Velocities()
                        .setVelocityType(velocityType)
                        .setVelocityDetails(
                                new VelocityDetails()
                                        .setMaxAmtPerTxn(maxAmtPerTxn)
                                        .setMaxAmtPerDay(maxAmtPerDay)
                                        .setMaxAmtPerWeek(maxAmtPerWeek)
                                        .setMaxAmtPerMonth(maxAmtPerMonth)
                                        .setMaxTxnPerDay(maxTxnPerDay)
                                        .setMaxTxnPerWeek(maxTxnPerWeek)
                                        .setMaxTxnPerMonth(maxTxnPerMonth));

        merchantConfig
                .getConfigVelocity()
                .getVelocities()
                .add(config);
    }

    @Override
    void modify(final String mid) {
        JsonObject velocities = new JsonObject();
        JsonObject velocityDetails = new JsonObject();
        velocities.addProperty("VELOCITY_TYPE", velocityType);
        velocities.add("VELOCITY_DETAILS", velocityDetails);
        velocityDetails.addProperty("MAX_AMT_PER_TXN", maxAmtPerTxn);
        velocityDetails.addProperty("MAX_AMT_PER_DAY", maxAmtPerDay);
        velocityDetails.addProperty("MAX_AMT_PER_WEEK", maxAmtPerWeek);
        velocityDetails.addProperty("MAX_AMT_PER_MONTH", maxAmtPerMonth);
        velocityDetails.addProperty("MAX_TXN_PER_DAY", maxTxnPerDay);
        velocityDetails.addProperty("MAX_TXN_PER_WEEK", maxTxnPerWeek);
        velocityDetails.addProperty("MAX_TXN_PER_MONTH", maxTxnPerMonth);

        velocities.addProperty("MID", mid);
        BaseApi request = new ConfigVelocity(velocities.toString());
        Response response = request.execute();
        Assertions.assertThat(response.statusCode()).isEqualTo(200);
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat(jsonPath.getString("STATUS")).isEqualToIgnoringCase("SUCCESS");
    }

}

