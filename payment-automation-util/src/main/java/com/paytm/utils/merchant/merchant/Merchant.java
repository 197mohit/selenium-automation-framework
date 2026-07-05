package com.paytm.utils.merchant.merchant;

import com.paytm.utils.merchant.api.EditMerchantApi;
import com.paytm.utils.merchant.dto.CreateMerchant;
import com.paytm.utils.merchant.dto.EditMerchant;
import com.paytm.utils.merchant.dto.MerchantDetails;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.fest.assertions.api.Assertions;

import java.util.Arrays;
import java.util.UUID;


public final class Merchant extends Configuration {

    private static final String DEFAULT = "34577774244";
    private static final String RETRY = "34577774255";
    public static final String SUBSCRIBE = "34577774242";
    public static final String RENEW_SUBSCRIPTION = "34577774241";
    public static final String SEAMLESS = "34577774249";
    public static final String SEAMLESS_NATIVE = "34577774314";
    public static final String ADD_MONEY = "34577774259";
    public static final String PAYTM_EXPRESS = "34577774436";
    public static final String SD_MERCHANT="34577774668";
    private String accountFor ="PGMerchantPanel";
    private String merchantType="NonSD";
    private String mobileNo="9876543210";
    private String emailId="automationmerchantp@mailinator.com";
    private String ppiLimited="0";
    private String accountPrimary="TRUE";

    public enum ConvFeeType {
        DEFAULT("0"),
        PRE_CONVENIENCE("1"),
        POST_CONVENIENCE("2");

        private String value;

        ConvFeeType(String value) {
            this.value = value;
        }
    }

    @Deprecated
    public enum MerchantType {
        DEFAULT(false, false, false),
        HYBRID(false, true, false),
        ADD_N_PAY(true, false, false),
        WALLET_ONLY(false, false, true);

        private Boolean isAddnPay;
        private Boolean isHybrid;
        private Boolean isWalletOnly;


        MerchantType(Boolean isAddnPay, Boolean isHybrid, Boolean isWalletOnly) {
            this.isAddnPay = isAddnPay;
            this.isHybrid = isHybrid;
            this.isWalletOnly = isWalletOnly;
        }
    }

    private String[] requestType;
    private Boolean isAddnPay;
    private Boolean isHybrid;
    private Boolean isWalletOnly;
    private int numberOfRetry;
    private String convFeeType;

    public static Configuration Default(String... requestType) {
        return new Merchant(requestType, false, false, false, 0, ConvFeeType.DEFAULT);
    }

    public static Configuration Default(int numberOfRetry, String... requestType) {
        return new Merchant(requestType, false, false, false, numberOfRetry, ConvFeeType.DEFAULT);
    }

    public static Configuration Default(ConvFeeType convFeeType, String... requestType) {
        return new Merchant(requestType, false, false, false, 0, convFeeType);
    }

    public static Configuration Default(int numberOfRetry, ConvFeeType convFeeType, String... requestType) {
        return new Merchant(requestType, false, false, false, numberOfRetry, convFeeType);
    }

    public static Configuration Hybrid(String... requestType) {
        return new Merchant(requestType, false, true, false, 0, ConvFeeType.DEFAULT);
    }

    public static Configuration Hybrid(int numberOfRetry, String... requestType) {
        return new Merchant(requestType, false, true, false, numberOfRetry, ConvFeeType.DEFAULT);
    }

    public static Configuration Hybrid(ConvFeeType convFeeType, String... requestType) {
        return new Merchant(requestType, false, true, false, 0, convFeeType);
    }

    public static Configuration Hybrid(int numberOfRetry, ConvFeeType convFeeType, String... requestType) {
        return new Merchant(requestType, false, true, false, numberOfRetry, convFeeType);
    }

    public static Configuration AddnPay(String... requestType) {
        return new Merchant(requestType, true, false, false, 0, ConvFeeType.DEFAULT);
    }

    public static Configuration AddnPay(int numberOfRetry, String... requestType) {
        return new Merchant(requestType, true, false, false, numberOfRetry, ConvFeeType.DEFAULT);
    }

    public static Configuration AddnPay(ConvFeeType convFeeType, String... requestType) {
        return new Merchant(requestType, true, false, false, 0, convFeeType);
    }

    public static Merchant AddnPay(int numberOfRetry, ConvFeeType convFeeType, String... requestType) {
        return new Merchant(requestType, true, false, false, numberOfRetry, convFeeType);
    }

    public static Configuration WalletOnly(String... requestType) {
        return new Merchant(requestType, false, false, true, 0, ConvFeeType.DEFAULT);
    }

    public static Configuration WalletOnly(int numberOfRetry, String... requestType) {
        return new Merchant(requestType, false, false, true, numberOfRetry, ConvFeeType.DEFAULT);
    }

    public static Configuration WalletOnly(ConvFeeType convFeeType, String... requestType) {
        return new Merchant(requestType, false, false, true, 0, convFeeType);
    }

    public static Configuration WalletOnly(int numberOfRetry, ConvFeeType convFeeType, String... requestType) {
        return new Merchant(requestType, false, false, true, numberOfRetry, convFeeType);
    }

    @Deprecated
    public Merchant(MerchantType merchantType, String... requestType) {
        this(merchantType, requestType, ConvFeeType.DEFAULT, 0);
    }

    @Deprecated
    public Merchant(MerchantType merchantType, int numberOfRetry, String... requestType) {
        this(merchantType, requestType, ConvFeeType.DEFAULT, numberOfRetry);
    }

    @Deprecated
    public Merchant(MerchantType merchantType, ConvFeeType convFeeType, String... requestType) {
        this(merchantType, requestType, convFeeType, 0);
    }

    @Deprecated
    public Merchant(MerchantType merchantType, ConvFeeType convFeeType, int numberOfRetry, String... requestType) {
        this(merchantType, requestType, convFeeType, numberOfRetry);
    }

    @Deprecated
    private Merchant(MerchantType merchantType, String[] requestType, ConvFeeType convFeeType, int numberOfRetry) {
        int lengthOfOldArray = requestType.length;
        String[] requestTypeNew = Arrays.copyOf(requestType, lengthOfOldArray + 2);
        requestTypeNew[lengthOfOldArray] = DEFAULT;
        requestTypeNew[lengthOfOldArray + 1] = RETRY;
        this.requestType = requestTypeNew;
        this.isAddnPay = merchantType.isAddnPay;
        this.isHybrid = merchantType.isHybrid;
        this.isWalletOnly = merchantType.isWalletOnly;
        this.numberOfRetry = numberOfRetry;
        this.convFeeType = convFeeType.value;
    }

    private Merchant(String[] requestType, Boolean isAddnPay, Boolean isHybrid, Boolean isWalletOnly, int numberOfRetry, ConvFeeType convFeeType) {
        int lengthOfOldArray = requestType.length;
        String[] requestTypeNew = Arrays.copyOf(requestType, lengthOfOldArray + 2);
        requestTypeNew[lengthOfOldArray] = DEFAULT;
        requestTypeNew[lengthOfOldArray + 1] = RETRY;
        this.requestType = requestTypeNew;
        this.isAddnPay = isAddnPay;
        this.isHybrid = isHybrid;
        this.isWalletOnly = isWalletOnly;
        this.numberOfRetry = numberOfRetry;
        this.convFeeType = convFeeType.value;
    }

    @Deprecated
    public Merchant() {
    }

    @Deprecated
    public Merchant setRequestType(String... requestType) {
        this.requestType = requestType;
        return this;
    }

    @Deprecated
    public Merchant setAddnPay(Boolean addnPay) {
        this.isAddnPay = addnPay;
        return this;
    }

    @Deprecated
    public Merchant setHybrid(Boolean hybrid) {
        this.isHybrid = hybrid;
        return this;
    }

    @Deprecated
    public Merchant setWalletOnly(Boolean walletOnly) {
        this.isWalletOnly = walletOnly;
        return this;
    }

    @Deprecated
    public Merchant setNumberOfRetry(int numberOfRetry) {
        this.numberOfRetry = numberOfRetry;
        return this;
    }

    @Deprecated
    public Merchant setConvFeeType(ConvFeeType convFeeType) {
        this.convFeeType = convFeeType.value;
        return this;
    }

    public Merchant setAccountFor(String accountFor){
        this.accountFor = accountFor;
        return this;
    }

    public Merchant setMerchantTypes(String merchantType){
        this.merchantType = merchantType;
        return this;
    }
    public Merchant setMobileNo(String mobileNo){
        this.mobileNo = mobileNo;
        return this;
    }
    public Merchant setEmailId(String emailId){
        this.emailId = emailId;
        return this;
    }
    public Merchant setPpiLimited(String ppiLimited){
        this.ppiLimited = ppiLimited;
        return this;
    }
    public Merchant setAccountPrimary(String accountPrimary){
        this.accountPrimary = accountPrimary;
        return this;
    }


    @Override
    void apply(CreateMerchant merchantConfig) {
        merchantConfig
                .getCreateMerRequest()
                .setMerchantDetails(
                        new MerchantDetails()
                                .setmID("")
                                .setrEQUESTID(RandomStringUtils.randomNumeric(18))
                                .setsOURCEID("OE")
                                .setaCCOUNTFOR(accountFor)
                                .setmERCHANTTYPE(merchantType)
                                .setoFFLINEENABLED(false)
                                .setpPILIMITEDMERCHANT(ppiLimited)
                                .setbUSINESSNAME("AutomationMerchant")
                                .setcALLBACKURLENABLED(false)
                                .setcUSTOM("SYSTEM GENERATED")
                                .setmERCHANTNAME("AutomationMerchant001")
                                .setcURRENCY("INR")
                                .setwEBSITENAME("retail")
                                .setrEFUNDTOBANKENABLED(false)
                                .setsTORECARDDETAILS("NO")
                                .setcHECKSUMENABLED(false)
                                .setnUMBEROFRETRY(numberOfRetry)
                                .setcATEGORY("Education")
                                .setsUBCATEGORY("School")
                                .setiNDUSTRYTYPE("Retail")
                                .setsIZEOFKEY(16)
                                .setwALLETRECHARGEOPT("MANUAL_RECHARGE")
                                .setpROFILEID("1")
                                .setwALLETONLYENABLED(false)
                                .seteMAILALERT(true)
                                .setkYBID("1234")
                                .setcONVENIENCEFEETYPE(convFeeType)
                                .setvALIDFROM("12/31/2016")
                                .setvALIDTO("12/31/2020")
                                .setmULTISUPPORT("NO")
                                .setoCP(true)
                                .setcUSTOMNAME("AutomationMerchant")
                                .setrEQUESTNAME("AutomationRequest")
                                .setfIRSTNAME("Automation")
                                .setlASTNAME("MerchantOld")
                                .setuSERNAME(RandomStringUtils.randomAlphabetic(15))
                                .setmOBILENUMBER(mobileNo)
                                .setpHONENUMBER("9876543210")
                                .setpEMAIL(emailId)
                                .setsFIRSTNAME("Automation")
                                .setsEMAIL("automationmerchants@mailinator.com")
                                .setiNVOICEEMAIL("automationmerchanti@mailinator.com")
                                .setaCCOUNTPRIMARY(accountPrimary)
                                .setiSSUBUSER(false)
                                .setaDDRESS1("F-28")
                                .setaDDRESS2("F Block")
                                .setaDDRESS3("Sector 8")
                                .setcOUNTRY("India")
                                .setsTATE("Uttar Pradesh")
                                .setcITY("Noida")
                                .setpIN("201301")
                                .setsAMEASBUSINESSADDR(true)
                                .setcOMMUNICATIONADDRESS1("F-28")
                                .setcOMMUNICATIONADDRESS2("F Block")
                                .setcOMMUNICATIONADDRESS3("Sector 8")
                                .setcOMMUNICATIONCOUNTRY("India")
                                .setcOMMUNICATIONSTATE("Uttar Pradesh")
                                .setcOMMUNICATIONCITY("Noida")
                                .setcOMMUNICATIONPIN("201301")
                                .setkYCBANKNAME("AutoKycBank")
                                .setkYCBANKACCOUNTHOLDERNAME("AutoKycBank")
                                .setkYCBANKACCOUNTNO("607710110000667")
                                .setkYCBUSINESSPANNO("ATGHN1234B")
                                .setkYCBUSINESSGSTIN("22ATGHN1234B1Z5")
                                .setkYCBUSINESSIFSCNO("BKID0006077")
                                .setkYCAUTHORIZEDSIGNATORYPANNO("ASDFG1234B")
                                .setkYCAUTHORIZEDSIGNATORYIDPROOFNO("1234")
                                .setkYCAUTHORIZEDSIGNATORYNAME("Deepankar2")
                                .setkYCAUTHORIZEDSIGNATORYPROOFNO("1234")
                                .setcOMMSTATSELECT("1")
                                .seteMAILMERCHANT(true)
                                .seteMAILCONSUMER(true)
                                .setSapCode("Paytm123")
                                .setMerchUniqRef("")
                                .setEnableQRTag("213123412312")
                                .setrEQUESTTYPE(StringUtils.join(requestType, ","))
                                .setaDDMONEYENABLE(isAddnPay)
                                .setHybridEnabled(isHybrid)
                                .setwALLETONLYENABLED(isWalletOnly));
    }

    @Override
    void modify(String mid) {
        EditMerchant config =
                new EditMerchant()
                        .setAction("Save")
                        .setMid(mid)
                        .setMerchantDetails(
                                new MerchantDetails()
                                        .setrEQUESTTYPE(StringUtils.join(requestType, ","))
                                        .setaDDMONEYENABLE(isAddnPay)
                                        .setHybridEnabled(isHybrid)
                                        .setnUMBEROFRETRY(numberOfRetry)
                                        .setcONVENIENCEFEETYPE(convFeeType));

        Response response = new EditMerchantApi(config).execute();
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat(jsonPath.getString("STATUS")).isEqualToIgnoringCase("SUCCESS");
        Assertions.assertThat(jsonPath.getString("DATA.MSG")).isEqualToIgnoringCase("Merchant successfully edited");
    }

}

