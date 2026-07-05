package com.paytm.utils.merchant.merchant;

import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.utils.merchant.Constants;
import com.paytm.utils.merchant.api.ConfigureMerchantCommission;
import com.paytm.utils.merchant.dto.CommissionConfig;
import com.paytm.utils.merchant.dto.CreateMerchant;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.fest.assertions.api.Assertions;

/**
 * Created by deepakkumar on 26/11/17.
 */
public final class DefaultCommission extends Configuration {

    private static final String SIMPLE = "simple";
    private static final String SLAB = "TransAmountslap";
    private static final String EDIT = "EDIT";
    private com.paytm.utils.merchant.dto.ConfigureMerchantCommission config;


    public static DefaultCommission SimpleFlat(double flatCommission) {
        return new DefaultCommission(null, EDIT, null, flatCommission, false, SIMPLE);
    }

    public static DefaultCommission SimplePercent(double percentCommission) {
        return new DefaultCommission(null, EDIT, percentCommission, null, false, SIMPLE);
    }

    public static DefaultCommission SimplePercentFlatBoth(double percentCommission, double flatCommission) {
        return new DefaultCommission(null, EDIT, percentCommission, flatCommission, true, SIMPLE);
    }

    public static DefaultCommission SlabCommission(Slab slab1Commission, Slab slab2Commission, Slab slab3Commission) {
        return new DefaultCommission(null, EDIT, null, null, null,
                slab1Commission.slabStartRange, slab1Commission.slabEndRange, slab2Commission.slabStartRange,
                slab2Commission.slabEndRange, slab3Commission.slabStartRange, slab3Commission.slabEndRange,
                slab1Commission.commissionTypeBoth, slab2Commission.commissionTypeBoth, slab3Commission.commissionTypeBoth,
                slab1Commission.percentCommission, slab1Commission.flatCommission, slab2Commission.percentCommission,
                slab2Commission.flatCommission, slab3Commission.percentCommission, slab3Commission.flatCommission, SLAB);
    }


    private DefaultCommission(String mid, String action, Double percentCommission, Double flatCommission,
                              Boolean commissionTypeBoth, String feeType) {
        this(mid, action, percentCommission, flatCommission, commissionTypeBoth, null, null,
                null, null, null, null, null,
                null, null, null, null,
                null, null, null, null, feeType);
    }

    private DefaultCommission(String mid, String action, Double percentCommission, Double flatCommission,
                              Boolean commissionTypeBoth, Double slab1StartRange, Double slab1EndRange,
                              Double slab2StartRange, Double slab2EndRange, Double slab3StartRange, Double slab3EndRange,
                              Boolean slab1CommissionTypeBoth, Boolean slab2CommissionTypeBoth, Boolean slab3CommissionTypeBoth,
                              Double slab1PercentCommission, Double slab1FlatCommission, Double slab2PercentCommission,
                              Double slab2FlatCommission, Double slab3PercentCommission, Double slab3FlatCommission,
                              String feeType) {
        this.config =
                new com.paytm.utils.merchant.dto.ConfigureMerchantCommission()
                        .setMid(mid)
                        .setAction(action)
                        .setCommission(
                                new CommissionConfig()
                                        .setPercentCommission(percentCommission)
                                        .setFlatCommission(flatCommission)
                                        .setCommissionTypeBoth(commissionTypeBoth)
                                        .setSlab1StartRange(slab1StartRange)
                                        .setSlab1EndRange(slab1EndRange)
                                        .setSlab2StartRange(slab2StartRange)
                                        .setSlab2EndRange(slab2EndRange)
                                        .setSlab3StartRange(slab3StartRange)
                                        .setSlab3EndRange(slab3EndRange)
                                        .setSlab1CommissionTypeBoth(slab1CommissionTypeBoth)
                                        .setSlab2CommissionTypeBoth(slab2CommissionTypeBoth)
                                        .setSlab3CommissionTypeBoth(slab3CommissionTypeBoth)
                                        .setSlab1PercentCommission(slab1PercentCommission)
                                        .setSlab1FlatCommission(slab1FlatCommission)
                                        .setSlab2PercentCommission(slab2PercentCommission)
                                        .setSlab2FlatCommission(slab2FlatCommission)
                                        .setSlab3PercentCommission(slab3PercentCommission)
                                        .setSlab3FlatCommission(slab3FlatCommission)
                                        .setFeeType(feeType));
    }

    @Override
    void apply(CreateMerchant merchantConfig) {
        merchantConfig
                .setConfigureMerchantCommission(this.config);
    }

    @Override
    void modify(final String mid) {
        removeOldConfigs(mid);
        this.config.setMid(mid);
        ConfigureMerchantCommission request = new ConfigureMerchantCommission(this.config);
        Response response = request.execute();
        Assertions.assertThat(response.statusCode()).isEqualTo(200);
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat(jsonPath.getString("STATUS")).isEqualToIgnoringCase("SUCCESS");
    }

    private void removeOldConfigs(String mid) {
        String query1 = "UPDATE ENTITY_PAYTM_COMMISSION SET STATUS = '9376504', MODIFIED_BY = '8894181', MODIFIED_ON = CURRENT_TIMESTAMP " +
                "WHERE STATUS = '9376503' AND MID = (SELECT ID FROM ENTITY_INFO WHERE MID = '" + mid + "') " +
                "AND IS_DEFAULT_COMMISSION = 0; ";
        String query2 = "UPDATE SLAB_FEE_RANGES SET STATUS = '9376504', MODIFIED_BY = '8894181', MODIFIED_DATE = CURRENT_TIMESTAMP WHERE RANGE_ID IN ( " +
                "SELECT SLAB_RANGE_ID FROM ENTITY_PAYTM_COMMISSION WHERE MID = (SELECT ID FROM ENTITY_INFO WHERE MID = '" + mid + "') " +
                "AND IS_DEFAULT_COMMISSION = 0 " +
                ") AND STATUS='9376503'; ";
        DatabaseUtil.getInstance().executeUpdateQuery(Constants.PG_DB_CONNECTION, query1);
        DatabaseUtil.getInstance().executeUpdateQuery(Constants.PG_DB_CONNECTION, query2);
    }

    public static final class Slab {

        private final Double flatCommission;
        private final Double percentCommission;
        private final Double slabStartRange;
        private final Double slabEndRange;
        private final Boolean commissionTypeBoth;

        public static Slab Flat(double flatCommission, double slabStartRange, double slabEndRange) {
            return new Slab(flatCommission, null, slabStartRange, slabEndRange, false);
        }

        public static Slab Percent(double percentCommission, double slabStartRange, double slabEndRange) {
            return new Slab(null, percentCommission, slabStartRange, slabEndRange, false);
        }

        public static Slab PercentFlatBoth(double percentCommission, double flatCommission, double slabStartRange,
                                           double slabEndRange) {
            return new Slab(flatCommission, percentCommission, slabStartRange, slabEndRange, true);
        }

        private Slab(Double flatCommission, Double percentCommission, Double slabStartRange, Double slabEndRange,
                     Boolean commissionTypeBoth) {
            this.flatCommission = flatCommission;
            this.percentCommission = percentCommission;
            this.slabStartRange = slabStartRange;
            this.slabEndRange = slabEndRange;
            this.commissionTypeBoth = commissionTypeBoth;
        }

    }
}

