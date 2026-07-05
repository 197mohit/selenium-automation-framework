package scripts.crossBorder;

import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.BankAccount;
import com.paytm.dto.NativeDTO.InitTxn.Good;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.Price;
import com.paytm.dto.NativeDTO.InitTxn.ShippingInfo;
import com.paytm.framework.reportportal.annotation.Owner;
import io.qameta.allure.Feature;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

/** Cross-border native initiate (export-style body) — txn token sanity, same flow as {@link scripts.SubWalletDetails#TC_001_paytmWallet_giftVoucher_displayedInFPO}. */
public class Import extends PGPBaseTest {

    /**
     * Nullable fields: {@code null} keeps the sample defaults from {@link #sampleCrossBorderInitPayload(CrossBorderInitOverrides)}.
     * {@code custId} is not part of {@link InitTxnDTO.CrossBorderInitPayload}; when non-null it is applied via {@link InitTxnDTO.Builder#setCustId(String)}.
     */
    public static final class CrossBorderInitOverrides {
        public String custId;
        public String mobile;
        public String email;
        public String firstName;
        public String lastName;
        public String address;
        public String pincode;
        public String city;
        public String state;
        public String countryName;
        public String countryCode;
        public String pan;
        public String ieCode;
        public String bankAccountNumber;
        public String bankName;
        public String bankIfsc;
        public String bankStateCode;
        public String bankCountryCode;
        public String productSku;
        public String productName;
        public String productCode;
        public String hsnCode;
    }


    /** Same defaults as historical export sample; pass {@code null} or {@code new CrossBorderInitOverrides()} with fields unset for defaults only. */
    private static InitTxnDTO.CrossBorderInitPayload sampleCrossBorderInitPayload(CrossBorderInitOverrides overrides) {
        CrossBorderInitOverrides o = overrides == null ? new CrossBorderInitOverrides() : overrides;
        return new InitTxnDTO.CrossBorderInitPayload(
                "100",
                "INR",
                "20260409",
                new Good[]{sampleExportGood(o)},
                new ShippingInfo[]{sampleExportShipping()},
                nz(o.mobile, "7017658313"),
                nz(o.email, "test@paytm.com"),
                nz(o.firstName, "Rahul"),
                nz(o.lastName, "Gupta"),
                "https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse",
                "retail",
                nz(o.address, "Skymark One, Sector 98"),
                nz(o.pincode, "201303"),
                nz(o.city, "Gautam Buddha Nagar"),
                nz(o.state, "Uttar Pradesh"),
                nz(o.countryName, "India"),
                nz(o.countryCode, "IN"),
                nz(o.pan, null),
                null,
                nz(o.ieCode, "ABCDE1234F"),
                sampleExportBankAccount(o));
    }

    private static InitTxnDTO buildCrossBorderInitTxnDTO(
            String merchantId,
            String merchantKey,
            String ssoToken,
            CrossBorderInitOverrides overrides) {
        CrossBorderInitOverrides o = overrides == null ? new CrossBorderInitOverrides() : overrides;
        InitTxnDTO.Builder builder =
                new InitTxnDTO.Builder(merchantId, merchantKey, ssoToken, sampleCrossBorderInitPayload(o));
        if (o.custId != null) {
            builder.setCustId(o.custId);
        }
        return builder.build();
    }

    private static BankAccount sampleExportBankAccount(CrossBorderInitOverrides o) {
        CrossBorderInitOverrides x = o == null ? new CrossBorderInitOverrides() : o;
        BankAccount b = new BankAccount();
        b.setAccountNumber(nz(x.bankAccountNumber, "928827383"));
        b.setName(nz(x.bankName, "ICICI"));
        b.setIfsc(nz(x.bankIfsc, "ICI39OH20"));
        b.setStateCode(nz(x.bankStateCode, "UP"));
        b.setCountryCode(nz(x.bankCountryCode, "IN"));
        return b;
    }

    private static Good sampleExportGood(CrossBorderInitOverrides o) {
        CrossBorderInitOverrides x = o == null ? new CrossBorderInitOverrides() : o;
        Good g = new Good();
        g.setMerchantGoodsId("24525635625623");
        g.setMerchantShippingId("564314314574327545");
        g.setSnapshotUrl("http://snap.url.com");
        g.setDescription("Luggage Bag");
        g.setCategory("travelling/subway");
        g.setQuantity("3.2");
        g.setUnit("Kg");
        Price price = new Price();
        price.setCurrency("INR");
        price.setValue("1000");
        g.setPrice(price);
        g.setProductSku(nz(x.productSku, "LSKD911084"));
        g.setProductName(nz(x.productName, "Luggage Bag"));
        g.setProductCode(nz(x.productCode, "LSKD911084"));
        g.setHsnCode(nz(x.hsnCode, "42021201"));
        g.setExtendInfo(null);
        return g;
    }

    private static ShippingInfo sampleExportShipping() {
        ShippingInfo s = new ShippingInfo();
        s.getChargeAmount().setCurrency("INR");
        s.getChargeAmount().setValue("1");
        s.setLastName("Li");
        s.setTrackingNo("64643143132");
        s.setCountryName("JP");
        s.setMerchantShippingId("564314314574327545");
        s.setCityName("Atlanta");
        s.setAddress1("137 W San Bernardino");
        s.setAddress2("4114 Sepulveda");
        s.setEmail("abc@gmail.com");
        s.setZipCode("310001");
        s.setStateName("GA");
        s.setCarrier("Federal Express");
        s.setFirstName("Jim");
        s.setMobileNo("13765443223");
        s.applyCrossBorderExportShippingOverrides();
        return s;
    }

    private static String nz(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }

    @Owner(Constants.Owner.AKSHAT)
    @Feature("PG-3047")
    @Test(description = "Verify that txn token is successfully generated for crossborder_import")
    public void TC_01_verifyTransactionToken_crossBorderInitTxn() throws Exception {
        User user = userManager.getForRead(Label.PRIORITY);
        Constants.MerchantType merchant = Constants.MerchantType.import_crossborder;

        InitTxnDTO initTxnDTO = buildCrossBorderInitTxnDTO(
                merchant.getId(),
                merchant.getKey(),
                user.ssoToken(),
                null);

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

    }

    @Owner(Constants.Owner.AKSHAT)
    @Feature("PG-3047")
    @Test(description = "Verify txn token is successfully generated when userInfo is passed ")
    public void TC_02_verifyTransactionToken_crossBorderInitTxn() throws Exception {
        User user = userManager.getForRead(Label.PRIORITY);
        Constants.MerchantType merchant = Constants.MerchantType.import_crossborder;

        CrossBorderInitOverrides overrides = new CrossBorderInitOverrides();
        overrides.firstName = "Akshat";
        overrides.lastName = "Sharma";
        overrides.address = "Panchsheel Wellington, Crossing Republik";
        overrides.pincode = "201016";
        overrides.city = "Ghaziabad";

        InitTxnDTO initTxnDTO =
                buildCrossBorderInitTxnDTO(merchant.getId(), merchant.getKey(), user.ssoToken(), overrides);

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

    }

    @Owner(Constants.Owner.AKSHAT)
    @Feature("PG-3047")
    @Test(description = "Verify txn token not generated when invalid HSN code is passed ")
    public void TC_03_verifyTransactionTokenFailed_crossBorderInitTxn() throws Exception {
        User user = userManager.getForRead(Label.PRIORITY);
        Constants.MerchantType merchant = Constants.MerchantType.import_crossborder;

        CrossBorderInitOverrides overrides = new CrossBorderInitOverrides();
        overrides.hsnCode = "4202120";

        InitTxnDTO initTxnDTO =
                buildCrossBorderInitTxnDTO(merchant.getId(), merchant.getKey(), user.ssoToken(), overrides);

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        Assertions.assertThat(txnToken).as("InitTxn should fail for invalid HSN").isEqualTo("1001");
    }

}
