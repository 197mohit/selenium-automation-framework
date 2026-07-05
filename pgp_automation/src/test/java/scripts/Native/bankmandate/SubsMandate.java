package scripts.Native.bankmandate;

import com.paytm.api.CreateSubscription;
import com.paytm.api.Peon;
import com.paytm.api.nativeAPI.RenewSubscription;
import com.paytm.api.nativeAPI.SubsMandateCallback;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.ExtendInfo;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.OrderAdditionalInfo;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.NativeDTO.RenewSubscription.RenewSubscriptionDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.framework.reporting.Owners;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.LostInSpacePage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.*;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.SkipException;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.base.test.Group.Status.BUG;

@Owner("Gagandeep")
@Owners(author = "Gagandeep", qa = "Ankur")
public class SubsMandate extends PGPBaseTest {

    private final static String NATIVE_SUBSCRIPTION = "NATIVE_SUBSCRIPTION";
    private final static String NATIVE_MF_SIP = "NATIVE_MF_SIP";

    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Step()
    private InitTxnResponseDTO validateSuccessInitiateSubscription(InitTxnDTO initTxnDTO) {
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("S");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("0000");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Success");
        return responseDTO;

    }

    @Parameters({"isNativePlus"})
    @Severity(SeverityLevel.CRITICAL)
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, Paymentmode=BANK_MANDATE, freq=ONDEMAND", priority = 0)
    public void TC_SM001_ValidateTknGnrtd_NMS_BM(@Optional("false") Boolean isNativePlus) throws Exception {
       // User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)       //Initiate subs request
                .setTxnValue("1")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();
        validateSuccessInitiateSubscription(initTxnDTO);
    }

    @Epic(Constants.Sprint.SPRINT31_1)
    @Feature("PGP-19896")
    @Owner("Tarun")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBSCRIPTION, Paymentmode=BANK_MANDATE, freq=ONDEMAND")
    public void TC_SM001_ValidateTknGnrtd_NS_BM(@Optional("false") Boolean isNativePlus) throws Exception {
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setOrderAdditionalInfo(new OrderAdditionalInfo()
                .setMName("Automation")
                .setMID(Constants.MerchantType.Subscription_PGOnly.getId())
                .setMcc("1234")
                .setMLogo("Paytm"));
     //   User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)       //Initiate subs request
                .setTxnValue("1")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .setExtendInfo(extendInfo)
                .build();
        validateSuccessInitiateSubscription(initTxnDTO);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id not generated when " +
            "requestType=NATIVE_MF_SIP, Paymentmode=BANK_MANDATE, freq=ONDEMAND, amount=null")
    public void TC_SM002_ValidateTknNotGnrtd_NMS_amountNull(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue(null)
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("1007");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify txn token and subs id not generated when " +
            "requestType=NATIVE_SUBSCRIPTION, Paymentmode=BANK_MANDATE, freq=ONDEMAND, amount=null")
    public void TC_SM002_ValidateTknNotGnrtd_NS_amountNull(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue(null)
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("1007");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify mandate not created when txmAmount>0 and " +
            "requestType=NATIVE_MF_SIP, Paymentmode=BANK_MANDATE, freq=ONDEMAND")
    public void TC_SM003_ValidateMndtNotCreated_NMS_amountGreat0(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("1")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("4001");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("validation failed as invalid txnAmount is Passed");

        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();
        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setChannelCode("HDFC")
                .setCHANNEL_ID("WEB")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        LostInSpacePage lostInSpacePage = new LostInSpacePage();
        lostInSpacePage.imgLostInSpace().assertVisible();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify mandate not created when txmAmount>0 and " +
            "requestType=NATIVE_SUBSCRIPTION, Paymentmode=BANK_MANDATE, freq=ONDEMAND")
    public void TC_SM003_ValidateMndtNotCreated_NS_amountGreat0(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("1")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("4001");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("validation failed as invalid txnAmount is Passed");

        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();
        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setChannelCode("HDFC")
                .setCHANNEL_ID("WEB")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        LostInSpacePage lostInSpacePage = new LostInSpacePage();
        lostInSpacePage.imgLostInSpace().assertVisible();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify that status is INIT and sub status is NULL on create subs mandate in table subscription_contract_v2 and subscription_payment_details when " +
            "RequestType=NATIVE_MF_SIP")
    public void TC_SM004_ValidateStatusAndSubstatus_NMS_SubsInitiated(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String subsId = responseDTO.getBody().getSubscriptionId();
        String status = PGPHelpers.executeUntilSubsContractNotFound("status", subsId, initTxnDTO.orderFromBody());
        String sub_status = PGPHelpers.executeUntilSubsContractNotFound("sub_status", subsId, initTxnDTO.orderFromBody());
        Assertions.assertThat(status).as("status mismatch").isEqualToIgnoringCase("INIT");
        Assertions.assertThat(sub_status).as("sub_status is not null").isNull();

        //  validating subscription_payment_details
        status = PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, initTxnDTO.orderFromBody());
        String payment_type = PGPHelpers.executeUntilSubsPaymentInfoNotFound("payment_type", subsId, initTxnDTO.orderFromBody());
        Assertions.assertThat(status).as("status mismatch").isEqualToIgnoringCase("INIT");
        Assertions.assertThat(payment_type).as("payment_type mismatch").isEqualToIgnoringCase("FIRST_REQUEST");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify that status is INIT and sub status is NULL on create subs mandate in table subscription_contract_v2 and subscription_payment_details when " +
            "RequestType=NATIVE_SUBSCRIPTION")
    public void TC_SM004_ValidateStatusAndSubstatus_NS_SubsInitiated(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String subsId = responseDTO.getBody().getSubscriptionId();
        String status = PGPHelpers.executeUntilSubsContractNotFound("status", subsId, initTxnDTO.orderFromBody());
        String sub_status = PGPHelpers.executeUntilSubsContractNotFound("sub_status", subsId, initTxnDTO.orderFromBody());
        Assertions.assertThat(status).as("status mismatch").isEqualToIgnoringCase("INIT");
        Assertions.assertThat(sub_status).as("sub_status is not null").isNull();

        //  validating subscription_payment_details
        status = PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, initTxnDTO.orderFromBody());
        String payment_type = PGPHelpers.executeUntilSubsPaymentInfoNotFound("payment_type", subsId, initTxnDTO.orderFromBody());
        Assertions.assertThat(status).as("status mismatch").isEqualToIgnoringCase("INIT");
        Assertions.assertThat(payment_type).as("payment_type mismatch").isEqualToIgnoringCase("FIRST_REQUEST");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify subs failing when freqType=ONDEMAND and merchant not supported" +
            "RequestType=NATIVE_MF_SIP")
    public void TC_SM005_ValidateSubs_Fail_NMS_ONDEMAND_merchNotSupp(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE_WO_ONDEMAND;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMessage mismatch")
                .isEqualToIgnoringCase("OnDemand Subscriptions are not allowed on merchant");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify subs failing when freqType=ONDEMAND and merchant not supported" +
            "RequestType=NATIVE_SUBSCRIPTION")
    public void TC_SM005_ValidateSubs_Fail_NS_ONDEMAND_merchNotSupp(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE_WO_ONDEMAND;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMessage mismatch")
                .isEqualToIgnoringCase("OnDemand Subscriptions are not allowed on merchant");
    }

    //TODO: need to confirm this case
    @Parameters({"isNativePlus"})
    @Test(description = "Verify subs success when amountType=FIX and" +
            "RequestType=NATIVE_MF_SIP", groups = {Group.Status.TO_BE_FIXED})
    public void TC_SM006_ValidateSubs_Success_NMS_FIX(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify subs create success when paymentMode=DC and " +
            "RequestType=NATIVE_MF_SIP, amountType=VARIABLE")
    public void TC_SM007_ValidateSubs_Success_NMS_PayMode_BM_DC(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "DEBIT_CARD", false);
        Assertions.assertThat(paymodeStatus).as("DEBIT_CARD paymode status mismatch").isTrue();

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId, "DEBIT_CARD")
                .setChannelCode("HDFC")
                .setCHANNEL_ID("WEB")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage mandateResponsePage = new ResponsePage();
        mandateResponsePage.validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validatePaymentMode("DC")
                .validateSubsId(subsId)
                .validateTxnAmount("1.0")
                .assertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify subs create unsuccess when paymentMode=other than BANK_MANDATE/DC and " +
            "RequestType=NATIVE_MF_SIP, amountType=VARIABLE")
    public void TC_SM007_ValidateSubs_Fail_NMS_PayMode_CC(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "CREDIT_CARD", false);
        Assertions.assertThat(paymodeStatus).as("CREDIT_CARD paymode is enabled in fetchPaymentOptions").isFalse();

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId, "CREDIT_CARD")
                .setChannelCode("HDFC")
                .setCHANNEL_ID("WEB")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage mandateResponsePage = new ResponsePage();
        mandateResponsePage.validateStatus("TXN_FAILURE")
                .validateRespCode("317")
                .validateRespMsg("Invalid payment mode")
                .assertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify subs create unsuccess when maxAmount > configured amount " +
            "RequestType=NATIVE_MF_SIP, amountType=VARIABLE")
    public void TC_SM008_ValidateSubs_Fail_NMS_maxAmountGrtrConfigAmt(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("5010")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();
        InitTxnResponseDTO initTxnResponseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(initTxnResponseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(initTxnResponseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Maximum Subscription Amount limit breached");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify subs create unsuccess when maxAmount > configured amount " +
            "RequestType=NATIVE_SUBSCRIPTION, amountType=VARIABLE")
    public void TC_SM008_ValidateSubs_Fail_NS_maxAmountGrtrConfigAmt(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("5010")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO initTxnResponseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(initTxnResponseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(initTxnResponseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Maximum Subscription Amount limit breached");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify subs create Fail when active subscriptions on merchant and cust id are more than the max subscriptions configured on merchant" +
            "RequestType=NATIVE_MF_SIP, amountType=VARIABLE")
    public void TC_SM009_ValidateSubs_Fail_NMS_activeSubsCountCompleted(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE_LIMIT_1;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();
        String custId = initTxnDTO.getBody().getUserInfo().getCustId();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setChannelCode("HDFC")
                .setCHANNEL_ID("WEB")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateRespMsg("SUCCESS")
                .validateRespCode("3006")
                .assertAll();

        // Sending mandate callback
        JsonPath jsonPath = new SubsMandateCallback(subsId, "ACTIVE").execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("resultInfo.code"))
                .as("resultCOde mismtach")
                .isEqualToIgnoringCase("3006");

        initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .setCustId(custId)
                .build();
        InitTxnResponseDTO initTxnResponseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(initTxnResponseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(initTxnResponseDTO.getBody().getResultInfo().getResultCode().trim())
                .as("resultCode mismatch")
                .isEqualToIgnoringCase("937");

        Assertions.assertThat(initTxnResponseDTO.getBody().getResultInfo().getResultMsg().trim())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Subscription Count limit breached");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify subs create fail when active subscriptions on merchant and cust id are more than the max subscriptions configured on merchant" +
            "RequestType=NATIVE_SUBSCRIPTION, amountType=VARIABLE")
    public void TC_SM009_ValidateSubs_Fail_NS_activeSubsCountCompleted(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE_LIMIT_1;
        InitTxnDTO initTxnDTO = null;
        String custId = "";
        pre_requisite:
        {
            try {
                initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                        .setTxnValue("0")
                        .setSubscriptionPaymentMode("BANK_MANDATE")
                        .setSubscriptionAmountType("VARIABLE")
                        .setSubscriptionMaxAmount("10")
                        .setSubscriptionFrequency("1")
                        .setSubscriptionFrequencyUnit("ONDEMAND")
                        .setSubscriptionGraceDays("0")
                        .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                        .setRequestType(NATIVE_SUBSCRIPTION)
                        .build();
                custId = initTxnDTO.getBody().getUserInfo().getCustId();
                InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
                String txnToken = responseDTO.getBody().getTxnToken();
                String subsId = responseDTO.getBody().getSubscriptionId();

                OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                        .setChannelCode("HDFC")
                        .setCHANNEL_ID("WEB")
                        .build();
                checkoutPage.createNativeOrder(orderDTO, isNativePlus);
                ResponsePage responsePage = new ResponsePage();
                responsePage.validateStatus("TXN_SUCCESS")
                        .validateRespMsg("SUCCESS")
                        .validateRespCode("3006")
                        .assertAll();

                // Sending mandate callback
                JsonPath jsonPath = new SubsMandateCallback(subsId, "ACTIVE").execute().jsonPath();
                Assertions.assertThat(jsonPath.getString("resultInfo.code"))
                        .as("resultCode mismtach")
                        .isEqualToIgnoringCase("3006");
            } catch (Throwable ex) {
                throw new SkipException("Exeception occuered when creating subscription");
            }
        }

        initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .setCustId(custId)
                .build();
        InitTxnResponseDTO initTxnResponseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(initTxnResponseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(initTxnResponseDTO.getBody().getResultInfo().getResultCode().trim())
                .as("resultCode mismatch")
                .isEqualToIgnoringCase("937");
        Assertions.assertThat(initTxnResponseDTO.getBody().getResultInfo().getResultMsg().trim())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Subscription Count limit breached");
    }

    @Issue("PGP-19623")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify subs create fail When subscriptionExpiryDate is before today's date " +
            "RequestType=NATIVE_MF_SIP, amountType=VARIABLE", groups = BUG)
    public void TC_SM010_ValidateSubs_Fail_NMS_expDateLess_todayDate(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .setSubscriptionExpiryDate(CommonHelpers.getDate().minusMonths(4L).toString())
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Invalid subscription start date.");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify subs create success When subscriptionStartDate is before today's date " +
            "RequestType=NATIVE_MF_SIP, amountType=VARIABLE")
    public void TC_SM011_ValidateSubs_Success_NMS_startDateLess_todayDate(@Optional("false") Boolean isNativePlus) throws Exception {
     //   User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)       //Initiate subs request
                .setTxnValue("1")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().minusMonths(4L).toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();
        validateSuccessInitiateSubscription(initTxnDTO);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify subs create success When Empty subscriptionRetryCount " +
            "RequestType=NATIVE_MF_SIP, amountType=VARIABLE")
    public void TC_SM012_ValidateSubs_Success_NMS_emptyRetryCount(@Optional("false") Boolean isNativePlus) throws Exception {
     //   User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)       //Initiate subs request
                .setTxnValue("1")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .setSubscriptionRetryCount(null)
                .build();
        validateSuccessInitiateSubscription(initTxnDTO);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify subs create success When Empty subscriptionGraceDays " +
            "RequestType=NATIVE_MF_SIP, amountType=VARIABLE")
    public void TC_SM013_ValidateSubs_Success_NMS_emptyGraceDays(@Optional("false") Boolean isNativePlus) throws Exception {
       // User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)       //Initiate subs request
                .setTxnValue("1")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays(null)
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();
        validateSuccessInitiateSubscription(initTxnDTO);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify subs create success When subscriptionEnableRetry: 0 " +
            "RequestType=NATIVE_MF_SIP, amountType=VARIABLE")
    public void TC_SM014_ValidateSubs_Success_NMS_enblRetryis0(@Optional("false") Boolean isNativePlus) throws Exception {
     //   User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)       //Initiate subs request
                .setTxnValue("1")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionEnableRetry("0")
                .setRequestType(NATIVE_MF_SIP)
                .build();
        validateSuccessInitiateSubscription(initTxnDTO);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify subs create fail When subscriptionEnableRetry: null " +
            "RequestType=NATIVE_MF_SIP, amountType=VARIABLE")
    public void TC_SM015_ValidateSubs_Fail_NMS_enblRetryisNULL(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setSubscriptionEnableRetry(null)
                .setRequestType(NATIVE_MF_SIP)
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Validation failed");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify subs create Fail When Empty subscriptionStartDate " +
            "RequestType=NATIVE_MF_SIP, amountType=VARIABLE", groups = {Group.Status.TO_BE_FIXED})
    public void TC_SM016_ValidateSubs_Fail_NMS_emptyStartDate(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("0")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate("")
                .setRequestType(NATIVE_MF_SIP)
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualToIgnoringCase("TXN_FAILURE");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify subs create Fail When Empty subscriptionFrequencyUnit " +
            "RequestType=NATIVE_MF_SIP, amountType=VARIABLE")
    public void TC_SM017_ValidateSubs_Fail_NMS_emptyFreqUnit(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit(null)
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualToIgnoringCase("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Invalid Frequency Unit");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify subs create success when subscriptionFrequencyUnit: ONDEMAND, no subscriptionFrequency is needed " +
            "RequestType=NATIVE_MF_SIP, amountType=VARIABLE")
    public void TC_SM018_ValidateSubs_Success_NMS_emptySubsFreq(@Optional("false") Boolean isNativePlus) throws Exception {
       // User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)       //Initiate subs request
                .setTxnValue("1")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
              //  .setSubscriptionFrequency("0")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();
        validateSuccessInitiateSubscription(initTxnDTO);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify subs create success When subscriptionPaymentMode is passed \"BANK_MANDATE\" in request and frequency='0' " +
            "RequestType=NATIVE_MF_SIP, amountType=VARIABLE")
    public void TC_SM019_ValidateSubs_Success_NMS_ZeroSubsFreq(@Optional("false") Boolean isNativePlus) throws Exception {
       // User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)       //Initiate subs request
                .setTxnValue("1")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("0")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();
        validateSuccessInitiateSubscription(initTxnDTO);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify subs create fail When Passing invalid txn amount (eg: -11) " +
            "RequestType=NATIVE_MF_SIP, amountType=VARIABLE")
    public void TC_SM020_ValidateSubs_Fail_NMS_invalidTxnAmount(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("-11")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualToIgnoringCase("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Txn amount is invalid");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify If subs is created with CC paymode and request type Native MF sip, ptc should fail with error: Invalid payment mode " +
            "RequestType=NATIVE_MF_SIP, amountType=VARIABLE")
    public void TC_SM021_ValidatePTC_Fail_NMS_invalidPayMode(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnId = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        //Executing ProcessTransactionController
        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, txnId, initTxnDTO.orderFromBody(), subsId, "CREDIT_CARD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_FAILURE")
                .validateRespMsg("Invalid payment mode")
                .validateRespCode("317")
                .assertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify that the payop supported for respective product code are visible in response " +
            "RequestType=NATIVE_MF_SIP, amountType=VARIABLE, PaymentMode=BANK_MANDATE")
    public void TC_022_ValidateFPO_BM_PaymodeSupported(@Optional("false") Boolean isNativePlus) throws Exception {
     //   User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)       //Initiate subs request
                .setTxnValue("1")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnId = responseDTO.getBody().getTxnToken();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.
                fetchPaymentOptionResponse(txnId, merchant.getId(), initTxnDTO.orderFromBody());
        Assertions.assertThat(NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "BANK_MANDATE", false))
                .as("BANK_MANDATE isDisabled status mismatchedor not found in FPO")
                .isTrue();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify that the payop supported for respective product code are visible in response " +
            "RequestType=NATIVE_MF_SIP, amountType=VARIABLE, PaymentMode=DEBIT_CARD")
    public void TC_022_ValidateFPO_DC_PaymodeSupported(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnId = responseDTO.getBody().getTxnToken();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.
                fetchPaymentOptionResponse(txnId, merchant.getId(), initTxnDTO.orderFromBody());
        Assertions.assertThat(NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "DEBIT_CARD", false))
                .as("DEBIT_CARD isDisabled status mismatched or not found in FPO")
                .isTrue();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify that the payop supported for respective product code are visible in response " +
            "RequestType=NATIVE_MF_SIP, amountType=VARIABLE, PaymentMode=")
    public void TC_023_ValidateFPO_blank_PaymodeSupported(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode(null)
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnId = responseDTO.getBody().getTxnToken();


        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.
                fetchPaymentOptionResponse(txnId, merchant.getId(), initTxnDTO.orderFromBody());
        Assertions.assertThat(NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "BANK_MANDATE", false))
                .as("BANK_MANDATE isDisabled status mismatched or not found in FPO")
                .isTrue();
        Assertions.assertThat(NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "DEBIT_CARD", false))
                .as("DEBIT_CARD isDisabled status mismatched or not found in FPO")
                .isTrue();

    }

    //Not Tested
    @Owner("Tarun")
    @Description("Debit on Pre Defined Date | Theia")
    @Story("PGP-22549")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify renewal when debitDate= future date, Paymode: Bank_Mandate with Request Type NATIVE_MF_SIP")
    public void TC_SM024_bankMandateDebitDateIsEqualToFutureDate(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("2")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "BANK_MANDATE", false);
        Assertions.assertThat(paymodeStatus).as("BANK_MANDATE paymode status mismatch").isTrue();

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setChannelCode("HDFC")
                .setCHANNEL_ID("WEB")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage mandateResponsePage = new ResponsePage();
        mandateResponsePage.validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validatePaymentMode("BANK_MANDATE")
                .validateSubsId(subsId)
                .validateTxnAmount("1.0")
                .assertAll();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, initTxnDTO.txnAmountFromBody(), CommonHelpers.getDate(new Date(), "yyyy-MM-dd hh:mm:ss"))
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath renewResponse = renewSubscription.execute().jsonPath();
        Assertions.assertThat(renewResponse.getString("body.resultMsg")).isEqualTo("Subscription Txn accepted.");
    }

    @Owner("Tarun")
    @Description("Debit on Pre Defined Date | Theia")
    @Story("PGP-22549")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify renewal when debitDate= future date, Paymode: Bank_Mandate with Request Type NATIVE_SUBSCRIPTION")
    public void TC_SM025_bankMandateDebitDateIsEqualToFutureDate(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("2")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "BANK_MANDATE", false);
        Assertions.assertThat(paymodeStatus).as("BANK_MANDATE paymode status mismatch").isTrue();

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId, "BANK_MANDATE")
                .setChannelCode("HDFC")
                .setCHANNEL_ID("WEB")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage mandateResponsePage = new ResponsePage();
        mandateResponsePage.validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validatePaymentMode("BANK_MANDATE")
                .validateSubsId(subsId)
                .validateTxnAmount("1.0")
                .assertAll();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, initTxnDTO.txnAmountFromBody(), CommonHelpers.getDate(new Date(), "yyyy-MM-dd hh:mm:ss"))
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath renewResponse = renewSubscription.execute().jsonPath();
        Assertions.assertThat(renewResponse.getString("body.resultMsg")).isEqualTo("Subscription Txn accepted.");
    }

    @Owner("Tarun")
    @Description("Debit on Pre Defined Date | Theia")
    @Story("PGP-22549")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify renewal when debitDate= future date, Paymode is not Bank_Mandate with Request Type NATIVE_SUBSCRIPTION ")
    public void TC_SM027_nonbankMandateDebitDateIsEqualToFutureDateNative(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("BI_MONTHLY")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.CREDIT_CARD)
                .pay();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subscription.getSubsId(), initTxnDTO.txnAmountFromBody(), CommonHelpers.getDate(new Date(), "yyyy-MM-dd hh:mm:ss"))
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath renewResponse = renewSubscription.execute().jsonPath();
        Assertions.assertThat(renewResponse.getString("body.resultMsg")).isEqualTo("Invalid Pre Debit Date param");
    }


    @Owner("Tarun")
    @Description("Debit on Pre Defined Date | Theia")
    @Story("PGP-22549")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify renewal when debitDate= future date, Paymode is not Bank_Mandate with Request Type NATIVE_MF_SIP ")
    public void TC_SM028_nonbankMandateDebitDateIsEqualToFutureDateMUTUALFUND(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("2")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = NativeHelpers.fetchPaymentOptionResponse(txnToken, merchant.getId(), initTxnDTO.orderFromBody());
        boolean paymodeStatus = NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponseDTO, "DEBIT_CARD", false);
        Assertions.assertThat(paymodeStatus).as("DEBIT_CARD paymode status mismatch").isTrue();

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId, "DEBIT_CARD")
                .setChannelCode("HDFC")
                .setCHANNEL_ID("WEB")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage mandateResponsePage = new ResponsePage();
        mandateResponsePage.validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validatePaymentMode("DC")
                .validateSubsId(subsId)
                .validateTxnAmount("1.0")
                .assertAll();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, initTxnDTO.txnAmountFromBody(), CommonHelpers.getDate(new Date(), "yyyy-MM-dd hh:mm:ss"))
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath renewResponse = renewSubscription.execute().jsonPath();
        Assertions.assertThat(renewResponse.getString("body.resultMsg")).isEqualTo("Invalid Pre Debit Date param");
    }


    @Parameters({"isNativePlus"})
    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-26968")
    @Description("Automation JIRA : PGP-27164")
    @Test(description = "Verify that peon is not sent for 0 amount transaction in case of closer order when payMode=BANK_MANDATE & requestType =NATIVE_SUBSCRIPTION")
    public void peonNotSentForZeroBMNativeSubs(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)      //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String subsId = responseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, responseDTO.getBody().getTxnToken(), initTxnDTO.orderFromBody(), subsId, "BANK_MANDATE")
                .setChannelCode("HDFC")
                .setCHANNEL_ID("WEB")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage mandateResponsePage = new ResponsePage();
        mandateResponsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateMid(merchant.getId())
                .validatePaymentMode(Constants.PayMode.BANK_MANDATE.toString())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateGatewayName("PPBL")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateMandateType("E_MANDATE")
                .assertAll();

        com.paytm.api.Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeToGetNoResponse();

    }



    @Parameters({"isNativePlus"})
    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-26968")
    @Description("Automation JIRA : PGP-27164")
    @Test(description = "Verify that peon is not sent for 0 amount transaction in case of closer order when payMode=BANK_MANDATE & requestType =NATIVE_MF_SIP")
    public void peonNotSentForZeroBMNativeMF(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)      //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String subsId = responseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, responseDTO.getBody().getTxnToken(), initTxnDTO.orderFromBody(), subsId, "BANK_MANDATE")
                .setChannelCode("HDFC")
                .setCHANNEL_ID("WEB")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage mandateResponsePage = new ResponsePage();
        mandateResponsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateMid(merchant.getId())
                .validatePaymentMode(Constants.PayMode.BANK_MANDATE.toString())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateGatewayName("PPBL")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateMandateType("E_MANDATE")
                .assertAll();


        com.paytm.api.Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeToGetNoResponse();

    }

}
