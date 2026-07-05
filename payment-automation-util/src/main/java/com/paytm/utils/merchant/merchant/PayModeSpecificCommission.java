package com.paytm.utils.merchant.merchant;

import com.google.gson.JsonObject;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.api.pgp.admin.ConfigureMerchantInstrumentCommission;
import com.paytm.utils.merchant.dto.CommissionConfig;
import com.paytm.utils.merchant.dto.CreateMerchant;
import com.paytm.utils.merchant.dto.PAY_MODES;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.fest.assertions.api.Assertions;

import java.util.List;

/**
 * Created by deepakkumar on 8/12/17.
 */
public class PayModeSpecificCommission extends Configuration {

    private static final String SIMPLE = "simple";
    private static final String SLAB = "TransAmountslap";
    private static final String EDIT = "EDIT";
    private static final String RETAIL = "345678920";
    private static final String CARD_CATEGORY_OTHERS_OTHERS = "3457777413";
    private final String mid;
    private final String action;
    private final Double percentCommission;
    private final Double flatCommission;
    private final Boolean commissionTypeBoth;
    private final Double slab1StartRange;
    private final Double slab1EndRange;
    private final Double slab2StartRange;
    private final Double slab2EndRange;
    private final Double slab3StartRange;
    private final Double slab3EndRange;
    private final Boolean slab1CommissionTypeBoth;
    private final Boolean slab2CommissionTypeBoth;
    private final Boolean slab3CommissionTypeBoth;
    private final Double slab1PercentCommission;
    private final Double slab1FlatCommission;
    private final Double slab2PercentCommission;
    private final Double slab2FlatCommission;
    private final Double slab3PercentCommission;
    private final Double slab3FlatCommission;
    private final String feeType;
    private final String industry;
    private final String payMode;
    private final String dcOnusOffus;
    private final String ccOnusOffus;
    private final String cardCategory;



    private PayModeSpecificCommission(String mid, String action, Double percentCommission, Double flatCommission,
                                      Boolean commissionTypeBoth, String feeType, String industry, String payMode,
                                      String dcOnusOffus, String ccOnusOffus, String cardCategory) {
        this(mid, action, percentCommission, flatCommission, commissionTypeBoth, null, null,
                null, null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                feeType, industry, payMode, dcOnusOffus, ccOnusOffus, cardCategory);
    }

    private PayModeSpecificCommission(String mid, String action, Double percentCommission, Double flatCommission,
                                      Boolean commissionTypeBoth, Double slab1StartRange, Double slab1EndRange,
                                      Double slab2StartRange, Double slab2EndRange, Double slab3StartRange, Double slab3EndRange,
                                      Boolean slab1CommissionTypeBoth, Boolean slab2CommissionTypeBoth, Boolean slab3CommissionTypeBoth,
                                      Double slab1PercentCommission, Double slab1FlatCommission, Double slab2PercentCommission,
                                      Double slab2FlatCommission, Double slab3PercentCommission, Double slab3FlatCommission,
                                      String feeType, String industry, String payMode, String dcOnusOffus, String ccOnusOffus, String cardCategory) {
        this.mid = mid;
        this.action = action;
        this.percentCommission = percentCommission;
        this.flatCommission = flatCommission;
        this.commissionTypeBoth = commissionTypeBoth;
        this.slab1StartRange = slab1StartRange;
        this.slab1EndRange = slab1EndRange;
        this.slab2StartRange = slab2StartRange;
        this.slab2EndRange = slab2EndRange;
        this.slab3StartRange = slab3StartRange;
        this.slab3EndRange = slab3EndRange;
        this.slab1CommissionTypeBoth = slab1CommissionTypeBoth;
        this.slab2CommissionTypeBoth = slab2CommissionTypeBoth;
        this.slab3CommissionTypeBoth = slab3CommissionTypeBoth;
        this.slab1PercentCommission = slab1PercentCommission;
        this.slab1FlatCommission = slab1FlatCommission;
        this.slab2PercentCommission = slab2PercentCommission;
        this.slab2FlatCommission = slab2FlatCommission;
        this.slab3PercentCommission = slab3PercentCommission;
        this.slab3FlatCommission = slab3FlatCommission;
        this.feeType = feeType;
        this.industry = industry;
        this.payMode = payMode;
        this.dcOnusOffus = dcOnusOffus;
        this.ccOnusOffus = ccOnusOffus;
        this.cardCategory = cardCategory;



    }

    @Deprecated
    public static Configuration SimpleDC(Double percentCommission, Double flatCommission, boolean isOnus, boolean isOffus) {
        String dcOnusOffus = calculateOnusOffus(isOnus, isOffus);
        boolean commissionTypeBoth = calculateCommissionTypeBoth(percentCommission, flatCommission);
        return new PayModeSpecificCommission(null, EDIT, percentCommission, flatCommission,
                commissionTypeBoth, SIMPLE, RETAIL, "DC", dcOnusOffus, null, CARD_CATEGORY_OTHERS_OTHERS);
    }

    public static Configuration SimpleDC(Double percentCommission, Double flatCommission) {
        boolean commissionTypeBoth = calculateCommissionTypeBoth(percentCommission, flatCommission);
        return new PayModeSpecificCommission(null, EDIT, percentCommission, flatCommission,
                commissionTypeBoth, SIMPLE, RETAIL, "DC", "OFFUS", null, CARD_CATEGORY_OTHERS_OTHERS);
    }

    @Deprecated
    public static Configuration SimpleCC(Double percentCommission, Double flatCommission, boolean isOnus, boolean isOffus) {
        String ccOnusOffus = calculateOnusOffus(isOnus, isOffus);
        boolean commissionTypeBoth = calculateCommissionTypeBoth(percentCommission, flatCommission);
        return new PayModeSpecificCommission(null, EDIT, percentCommission, flatCommission,
                commissionTypeBoth, SIMPLE, RETAIL, "CC", null, ccOnusOffus, CARD_CATEGORY_OTHERS_OTHERS);
    }

    public static Configuration SimpleCC(Double percentCommission, Double flatCommission) {
        boolean commissionTypeBoth = calculateCommissionTypeBoth(percentCommission, flatCommission);
        return new PayModeSpecificCommission(null, EDIT, percentCommission, flatCommission,
                commissionTypeBoth, SIMPLE, RETAIL, "CC", null, "OFFUS", CARD_CATEGORY_OTHERS_OTHERS);
    }

    public static Configuration SimpleNB(Double percentCommission, Double flatCommission) {
        boolean commissionTypeBoth = calculateCommissionTypeBoth(percentCommission, flatCommission);
        return new PayModeSpecificCommission(null, EDIT, percentCommission, flatCommission,
                commissionTypeBoth, SIMPLE, RETAIL, "NB", null, null, null);
    }

    public static Configuration SimpleUPI(Double percentCommission, Double flatCommission) {
        boolean commissionTypeBoth = calculateCommissionTypeBoth(percentCommission, flatCommission);
        return new PayModeSpecificCommission(null, EDIT, percentCommission, flatCommission,
                commissionTypeBoth, SIMPLE, RETAIL, "UPI", null, null, null);
    }

    public static Configuration SimplePPI(Double percentCommission, Double flatCommission) {
        boolean commissionTypeBoth = calculateCommissionTypeBoth(percentCommission, flatCommission);
        return new PayModeSpecificCommission(null, EDIT, percentCommission, flatCommission,
                commissionTypeBoth, SIMPLE, RETAIL, "PPI", null, null, null);
    }

    public static Configuration SlabDC(boolean isOnus, boolean isOffus, Slab slab1Commission,
                                       Slab slab2Commission, Slab slab3Commission) {
        String dcOnusOffus = calculateOnusOffus(isOnus, isOffus);
        return new PayModeSpecificCommission(null, EDIT, null, null,
                null, slab1Commission.slabStartRange, slab1Commission.slabEndRange,
                slab2Commission.slabStartRange, slab2Commission.slabEndRange, slab3Commission.slabStartRange,
                slab3Commission.slabEndRange, slab1Commission.commissionTypeBoth, slab2Commission.commissionTypeBoth,
                slab3Commission.commissionTypeBoth, slab1Commission.percentCommission, slab1Commission.flatCommission,
                slab2Commission.percentCommission, slab2Commission.flatCommission
                , slab3Commission.percentCommission, slab3Commission.flatCommission, SLAB, RETAIL, "DC",
                dcOnusOffus, null, CARD_CATEGORY_OTHERS_OTHERS);
    }

    public static Configuration SlabCC(boolean isOnus, boolean isOffus, Slab slab1Commission,
                                       Slab slab2Commission, Slab slab3Commission) {
        String ccOnusOffus = calculateOnusOffus(isOnus, isOffus);
        return new PayModeSpecificCommission(null, EDIT, null, null,
                null, slab1Commission.slabStartRange, slab1Commission.slabEndRange,
                slab2Commission.slabStartRange, slab2Commission.slabEndRange, slab3Commission.slabStartRange,
                slab3Commission.slabEndRange, slab1Commission.commissionTypeBoth, slab2Commission.commissionTypeBoth,
                slab3Commission.commissionTypeBoth, slab1Commission.percentCommission, slab1Commission.flatCommission,
                slab2Commission.percentCommission, slab2Commission.flatCommission
                , slab3Commission.percentCommission, slab3Commission.flatCommission, SLAB, RETAIL, "CC",
                null, ccOnusOffus, CARD_CATEGORY_OTHERS_OTHERS);
    }

    public static Configuration SlabNB(Slab slab1Commission,
                                       Slab slab2Commission, Slab slab3Commission) {
        return new PayModeSpecificCommission(null, EDIT, null, null,
                null, slab1Commission.slabStartRange, slab1Commission.slabEndRange,
                slab2Commission.slabStartRange, slab2Commission.slabEndRange, slab3Commission.slabStartRange,
                slab3Commission.slabEndRange, slab1Commission.commissionTypeBoth, slab2Commission.commissionTypeBoth,
                slab3Commission.commissionTypeBoth, slab1Commission.percentCommission, slab1Commission.flatCommission,
                slab2Commission.percentCommission, slab2Commission.flatCommission
                , slab3Commission.percentCommission, slab3Commission.flatCommission, SLAB, RETAIL, "NB",
                null, null, null);
    }

    private static String calculateOnusOffus(boolean isOnus, boolean isOffus) {
        String value = null;
        if (!(isOnus || isOffus)) {
            throw new IllegalArgumentException("either of isOnus and isOffus should be true");
        } else if (isOnus && isOffus) {
            value = "BOTHONOFF";
        } else if (isOnus) {
            value = "ONUS";
        } else if (isOffus) {
            value = "OFFUS";
        }
        return value;
    }

    private static boolean calculateCommissionTypeBoth(Double percentCommission, Double flatCommission) {
        boolean value = false;
        if (!((percentCommission == null) || (flatCommission == null))) {
            value = true;
        } else if ((percentCommission == null) && (flatCommission == null)) {
            throw new IllegalArgumentException("either of percentCommission and flatCommission should be provided");
        }
        return value;
    }

    @Override
    void apply(CreateMerchant merchantConfig) {
        List<PAY_MODES> pay_modesList = merchantConfig.getConfigureMbidAndInstrument().getPayModes();
        for (PAY_MODES payModes : pay_modesList) {
            if (payModes.getPayMode().equalsIgnoreCase(this.payMode)) {
                payModes.setCommission(
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
                                .setFeeType(feeType)
                );
            }

        }
    }

    @Override
    void modify(String mid) {

        JsonObject commission = new JsonObject();
        JsonObject commissionBody = new JsonObject();
        commission.addProperty("PERCENT_COMMISSION", percentCommission);
        commission.addProperty("FLAT_COMMISSION", flatCommission);
        commission.addProperty("COMMISSION_TYPE_BOTH", commissionTypeBoth);
        commission.addProperty("SLAB_1_START_RANGE", slab1StartRange);
        commission.addProperty("SLAB_1_END_RANGE", slab1EndRange);
        commission.addProperty("SLAB_2_START_RANGE", slab2StartRange);
        commission.addProperty("SLAB_2_END_RANGE", slab2EndRange);
        commission.addProperty("SLAB_3_START_RANGE", slab3StartRange);
        commission.addProperty("SLAB_3_END_RANGE", slab3EndRange);
        commission.addProperty("SLAB_1_COMMISSION_TYPE_BOTH", slab1CommissionTypeBoth);
        commission.addProperty("SLAB_2_COMMISSION_TYPE_BOTH", slab2CommissionTypeBoth);
        commission.addProperty("SLAB_3_COMMISSION_TYPE_BOTH", slab3CommissionTypeBoth);
        commission.addProperty("SLAB_1_PERCENT_COMMISSION", slab1PercentCommission);
        commission.addProperty("SLAB_1_FLAT_COMMISSION", slab1FlatCommission);
        commission.addProperty("SLAB_2_PERCENT_COMMISSION", slab2PercentCommission);
        commission.addProperty("SLAB_2_FLAT_COMMISSION", slab2FlatCommission);
        commission.addProperty("SLAB_3_PERCENT_COMMISSION", slab3PercentCommission);
        commission.addProperty("SLAB_3_FLAT_COMMISSION", slab3FlatCommission);
        commission.addProperty("FEE_TYPE", feeType);

        commissionBody.addProperty("MID", mid);
        commissionBody.addProperty("ACTION", action);
        commissionBody.addProperty("INDUSTRY", industry);
        commissionBody.addProperty("PAY_OPTION", payMode);
        commissionBody.addProperty("DC_ONUS_OFFUS", dcOnusOffus);
        commissionBody.addProperty("CC_ONUS_OFFUS", ccOnusOffus);
        commissionBody.addProperty("CARD_CATEGORY", cardCategory);
        commissionBody.add("COMMISSION", commission);

        commissionBody.addProperty("MID", mid);
        BaseApi request = new ConfigureMerchantInstrumentCommission(commissionBody.toString());
        Response response = request.execute();
        Assertions.assertThat(response.statusCode()).isEqualTo(200);
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat(jsonPath.getString("STATUS")).isEqualToIgnoringCase("SUCCESS");
    }

    public static final class Slab {

        private final Double flatCommission;
        private final Double percentCommission;
        private final Double slabStartRange;
        private final Double slabEndRange;
        private final Boolean commissionTypeBoth;

        private Slab(Double flatCommission, Double percentCommission, Double slabStartRange, Double slabEndRange,
                     Boolean commissionTypeBoth) {
            this.flatCommission = flatCommission;
            this.percentCommission = percentCommission;
            this.slabStartRange = slabStartRange;
            this.slabEndRange = slabEndRange;
            this.commissionTypeBoth = commissionTypeBoth;
        }

        public static Slab Flat(double flatCommission, double slabStartRange, double slabEndRange) {
            return new Slab(flatCommission, null, slabStartRange, slabEndRange, false);
        }

        public static Slab Percent(double percentCommission, double slabStartRange, double slabEndRange) {
            return new Slab(null, percentCommission, slabStartRange, slabEndRange, false);
        }

        public static Slab PercentFlatBoth(double percentCommission, double flatCommission, double slabStartRange,
                                           double slabEndRange) {
            return new Slab(percentCommission, flatCommission, slabStartRange, slabEndRange, true);
        }

    }


}
