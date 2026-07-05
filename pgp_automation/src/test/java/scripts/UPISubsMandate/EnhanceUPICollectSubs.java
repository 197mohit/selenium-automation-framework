package scripts.UPISubsMandate;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.GetPaymentStatus;
import com.paytm.api.PreNotify;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.framework.utils.CommonUtils;
import com.paytm.pages.*;
import com.paytm.utils.merchant.Peon;
import io.qameta.allure.Issue;
import io.qameta.allure.Step;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import io.qameta.allure.*;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;

import static com.paytm.appconstants.Constants.MerchantType.*;
import static com.paytm.appconstants.Constants.Owner.GAURAV;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

public class EnhanceUPICollectSubs extends PGPBaseTest implements UPIEnhancedTests {

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


    private List<String> getListOfPayModesOnCashierPage(CashierPage cashierPage) {
        List<UIElement> PaymodesOnPage = cashierPage.ListOfPayModsOnCashier();
        List<String> paymethodList = new ArrayList<>();
        for (int i = 0; i < PaymodesOnPage.size(); i++) {
            paymethodList.add(PaymodesOnPage.get(i).getText().split("\n")[0]);
        }
        return paymethodList;
    }
    @Override
    @Parameters({"theme"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=DAY")
    public void TC_001_Enhanced_Subs_FreqUnitDay(@Optional("enhancedweb") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("DAY")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT("10")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBLC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals("").not()

        );
        sAssert.eval();


    }

    @Override
    @Parameters({"theme"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=MONTH")
    public void TC_002_Enhanced_Subs_FreqUnitMonth(@Optional("enhancedweb") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("10")
                .setSUBS_MAX_AMOUNT("10")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("10")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("FIX")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PTYBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PTYBLC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PTYBLC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals("").not()

        );
        sAssert.eval();


    }

    @Override
    @Parameters({"theme"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=YEAR")
    public void TC_003_Enhanced_Subs_FreqUnitYear(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("YEAR")
                .setTXN_AMOUNT("10")
                .setSUBS_MAX_AMOUNT("10")
                .setSUBS_START_DATE(CommonHelpers.addYears(date, "yyyy-MM-dd", 1))
                .setSUBS_GRACE_DAYS("325")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("FIX")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PTYBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PTYBLC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PTYBLC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals("").not()

        );
        sAssert.eval();


    }

    @Override
    @Parameters({"theme"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=QUARTER")
    public void TC_004_Enhanced_Subs_FreqUnitQuater(@Optional("enhancedweb") String theme) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("QUARTER")
                .setTXN_AMOUNT("10")
                .setSUBS_MAX_AMOUNT("10")
                .setSUBS_START_DATE(CommonHelpers.addMonths(date, "yyyy-MM-dd", 3))
                .setSUBS_GRACE_DAYS("2")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("FIX")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBLC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals("").not()

        );
        sAssert.eval();


    }

    @Override
    @Parameters({"theme"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=BI_MONTHLY")
    public void TC_005_Enhanced_Subs_FreqUnitBiMonthly(@Optional("enhancedweb") String theme) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("BI_MONTHLY")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT("10")
                .setSUBS_START_DATE(CommonHelpers.addMonths(date, "yyyy-MM-dd", 2))
                .setSUBS_GRACE_DAYS("12")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PTYBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PTYBLC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PTYBLC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals("").not()

        );
        sAssert.eval();


    }


    @Override
    @Parameters({"theme"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=SEMI_ANNUALLY")
    public void TC_006_Enhanced_Subs_FreqUnitSemiAnually(@Optional("enhancedweb") String theme) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("SEMI_ANNUALLY")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT("10")
                .setSUBS_START_DATE(CommonHelpers.addMonths(date, "yyyy-MM-dd", 6))
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBLC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals("").not()

        );
        sAssert.eval();


    }

//Ondemand is now allowed in UPI PGP-26500

    @Override
    @Parameters({"theme"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=ONDEMAND")
    public void TC_007_Enhanced_Subs_FreqUnitOnDemand(@Optional("enhancedweb") String theme) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT("10")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PTYBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PTYBLC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PTYBLC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals("").not()

        );
        sAssert.eval();

    }



    /*Frequency Related TestCases*/


    @Override
    @Parameters({"theme"})
    @Test(description = "Verify Invalid GraceDays when Freq>1 " +
            "requestType=NATIVE_SUBS")
    public void TC_001_Enhanced_Subs_FreqGreaterThn1(@Optional("enhancedweb") String theme) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("DAY")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT("10")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("2")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("810")
                .validateRespMsg("Invalid Subscription Frequency")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateCheckSum(merchant.getKey())
                .assertAll();


    }


    @Override
    @Parameters({"theme"})
    @Test(description = "Verify Freq>1 ,SubsPaymode =Unknown " +
            "requestType=NATIVE_SUBS UPI Paymode on cashier Page")
    public void TC_002_Enhanced_Subs_FreqGreaterthan1_PaymodeBlank(@Optional("enhancedweb") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("1")
                .setSUBS_PAYMENT_MODE("")
                .setSUBS_MAX_AMOUNT("10")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("2")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        List<String> paymethodList = getListOfPayModesOnCashierPage(cashierPage);
        Assert.assertFalse(paymethodList.contains("BHIM UPI"));
    }


    @Override
    @Parameters({"theme"})
    @Test(description = "Verify Freq=0 Subscription Validation Fails" +
            "requestType=NATIVE_SUBS freq is Mandatory")
    public void TC_003_Enhanced_Subs_FreqEqualToZero(@Optional("enhancedweb") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT("10")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("0")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("810")
                .validateRespMsg("Invalid Subscription Frequency")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateCheckSum(merchant.getKey())
                .assertAll();


    }


    @Override
    @Parameters({"theme"})
    @Test(description = "Verify Freq is Blank By Default it will consider it 1" +
            "requestType=NATIVE_SUBS")
    public void TC_004_Enhanced_Subs_FreqIsBlank(@Optional("enhancedweb") String theme) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("10")
                .setSUBS_MAX_AMOUNT("10")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("")                  //Frequency is Blank
                .setSUBS_AMOUNT_TYPE("FIX")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PTYBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey()).assertAll();

    }



    /*Start Date Related TestCases*/


    @Override
    @Parameters({"theme"})
    @Test(description = "Verify StartDate is Null not Supported in UPI " +
            "requestType=NATIVE_SUBS")
    public void TC_001_Enhanced_Subs_StartDateIsNull(@Optional("enhancedweb") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("YEAR")
                .setTXN_AMOUNT("10")
                .setSUBS_MAX_AMOUNT("10")
                .setSUBS_START_DATE("")
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);


        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("810")
                .validateRespMsg("Invalid Subscription start date")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateCheckSum(merchant.getKey())
                .assertAll();
    }

    @Override
    @Parameters({"theme"})
    @Test(description = "Verify StartDate is equal to Future Date Supported in UPI " +
            "requestType=NATIVE_SUBS")
    public void TC_002_Enhanced_Subs_StartDateIsFuture(@Optional("enhancedweb") String theme) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT("10")
                .setSUBS_START_DATE(CommonHelpers.addMonths(date, "yyyy-MM-dd", 1))
                .setSUBS_GRACE_DAYS("29")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();
    }


//////////////////////////////////


    @Override
    @Parameters({"theme"})
    @Test(description = "Verify subscriptionGraceDays: subscriptionStartDate: ,both are Blank UPI filtered in FPO" +
            "PTC fails")
    public void TC_003_Enhanced_Subs_StartDateGraceDayNull(@Optional("enhancedweb") String theme) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT("10")
                .setSUBS_START_DATE("")
                .setSUBS_GRACE_DAYS("")
                .setSUBS_PAYMENT_MODE("")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);


        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        List<String> paymethodList = getListOfPayModesOnCashierPage(cashierPage);
        Assert.assertFalse(paymethodList.contains("BHIM UPI"));

    }


    @Override
    @Parameters({"theme"})
    @Test(description = "Verify subscriptionGraceDays:1 subscriptionStartDate: , invalid Subscription start date" +
            "requestType=NATIVE_SUBS")
    public void TC_004_Enhanced_Subs_StartDateIsOnlyBlank(@Optional("enhancedweb") String theme) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT("10")
                .setSUBS_START_DATE("")
                .setSUBS_GRACE_DAYS("1")
                .setSUBS_PAYMENT_MODE("")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("810")
                .validateRespMsg("Invalid Subscription start date")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateCheckSum(merchant.getKey())
                .assertAll();


    }


    /*Subscription Max Amount TestCases*/

    @Override
    @Parameters({"theme"})
    @Test(description = "If subscriptionMaxAmount>5000 and txn amount>5000, and paymode=null," +
            "UPI Paymode is not Available for Payment requestType=NATIVE_SUBS")
    public void TC_001_Enhanced_Subs_MaxAmountGreaterThan5000PaymodeNull(@Optional("enhancedweb") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("5500")
                .setSUBS_MAX_AMOUNT("5500")
                .setSUBS_PAYMENT_MODE("")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("29")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        List<String> paymethodList = getListOfPayModesOnCashierPage(cashierPage);
        Assert.assertFalse(paymethodList.contains("BHIM UPI"));

    }


    //UPI Limit for Subs has been increased to 25k - PGP-36177
    @Override
    @Parameters({"theme"})
    @Test(description = "If subscriptionMaxAmount>25000 and txn amount>25000, and paymode=UPI," +
            "resultMsg: Subscription Amount Limit For UPI Breached requestType=NATIVE_SUBS")
    public void TC_002_Enhanced_Subs_MaxAmountGreaterThan5000PaymodeUPI(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("100500")
                .setSUBS_MAX_AMOUNT("100500")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("29")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("810")
                .validateRespMsg("Subscription Amount Limit For UPI Breached")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateCheckSum(merchant.getKey())
                .assertAll();

    }


    @Override
    @Parameters({"theme"})
    @Test(description = "If txn amount>Subscription amount, and paymode=UPI," +
            "requestType=NATIVE_SUBS")
    public void TC_003_Enhanced_Subs_TxnAmountGreaterThanMaxAmount(@Optional("enhancedweb") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("YEAR")
                .setTXN_AMOUNT("10")
                .setSUBS_MAX_AMOUNT("1")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("324")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("2022")
                .validateRespMsg("Paymode selected is not applicable when txn amount is greater than the renewal amount")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateCheckSum(merchant.getKey())
                .assertAll();

    }

    @Issue("PGP-26277")
    @Test(description = "If subscriptionMaxAmount>5000 and txn amount<5000, and paymode=null," +
            "requestType=NATIVE_MF_SIP", groups = Group.Status.BUG)
    public void TC_003_Enhanced_Subs_OnlyMaxAmountGreaterThanMaxAmount(@Optional("enhancedweb") String theme) throws Exception{

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("4500")
                .setSUBS_MAX_AMOUNT("5500")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("21")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        List<String> paymethodList = getListOfPayModesOnCashierPage(cashierPage);
        Assert.assertFalse(paymethodList.contains("BHIM UPI"));


    }

    /*Amount Type TestCases*/

    //As Discuss with Srishti MAX Amount check has been removed from subs sides so need to disble testcase ---- Auto REnewal flow

    @Override
    @Feature("PGP-33945")
    @Parameters({"theme"})
    @Test(description = "For FIX amount type,if  equal amount is not passed for txn amount & max amount if payment mode=UPI," +
            " resultMsg : Transaction amount is not equal to max amount set against the subscription",enabled = true)
    public void TC_001_Enhanced_Subs_FixAmountEqualAmountNotPassedUPI(@Optional("enhancedweb_revamp") String theme) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("10")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("29")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("FIX")
                .build();

        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("2022")
                .validateRespMsg("Paymode selected is not applicable when txn amount is less than the renewal amount")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateCheckSum(merchant.getKey())
                .assertAll();


    }

//As Discuss with Srishti MAX Amount check has been removed from subs sides so need to disble testcase ---- Auto REnewal flow

    @Override
    @Feature("PGP-33945")
    @Parameters({"theme"})
    @Test(description = "For FIX amount type,if  equal amount is not passed for txn amount & max amount if payment mode=null",enabled = true)
    public void TC_002_Enhanced_Subs_FixAmountEqualAmountNotPassed(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("110")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("29")
                .setSUBS_PAYMENT_MODE("")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("FIX")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        List<String> paymethodList = getListOfPayModesOnCashierPage(cashierPage);
        Assert.assertFalse(paymethodList.contains("BHIM UPI"));
    }


    @Override
    @Parameters({"theme"})
    @Test(description = "For the Variable amount, the subscription txn amount should be" +
            " less than or equal to the subscription max amount")
    public void TC_003_Enhanced_Subs_VariableAmountEqualOrLessMaxAmount(@Optional("enhancedweb") String theme) throws Exception {


        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("YEAR")
                .setTXN_AMOUNT("100")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.addYears(date, "yyyy-MM-dd", 1))
                .setSUBS_GRACE_DAYS("324")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PTYBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();

    }

    /*----------------------------------------------------------------------------*/
    /*          Test cases For Enhanced MF SIP Subs RequestType
    *
    *                           APP INVOKE FLOW                                   */
    /*----------------------------------------------------------------------------*/


    /*Frequency Unit related testcases*/

    @Override
    @Parameters({"theme"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, freq=DAY")
    public void TC_001_Enhanced_MF_Subs_FreqUnitDay(@Optional("enhancedweb") String theme) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addDays(date, "yyyy-MM-dd", 1))
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.orderFromBody(),txnToken)
                .build();

        checkoutPage.createAppInvokeOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();



        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBLC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals("").not()

        );
        sAssert.eval();


    }
    @Override
    @Parameters({"theme"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, freq=MONTH")
    public void TC_002_Enhanced_MF_Subs_FreqUnitMonth(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("100")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("101")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("10")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addMonths(date, "yyyy-MM-dd", 1))
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.orderFromBody(),txnToken)
                .build();

        checkoutPage.createAppInvokeOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();



        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBLC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals("").not()

        );
        sAssert.eval();


    }

    @Override
    @Parameters({"theme"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, freq=YEAR")
    public void TC_003_Enhanced_MF_Subs_FreqUnitYear(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1900")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("1900")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("360")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addYears(date, "yyyy-MM-dd", 1))
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.orderFromBody(),txnToken)
                .build();

        checkoutPage.createAppInvokeOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();



        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBLC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals("").not()

        );
        sAssert.eval();

    }

    @Override
    @Parameters({"theme"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, freq=QUARTER")
    public void TC_004_Enhanced_MF_Subs_FreqUnitQuater(@Optional("enhancedweb") String theme) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("QUARTER")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addMonths(date, "yyyy-MM-dd", 3))
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.orderFromBody(),txnToken)
                .build();

        checkoutPage.createAppInvokeOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();



        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBLC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals("").not()

        );
        sAssert.eval();

    }

    @Override
    @Parameters({"theme"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, freq=BI_MONTHLY")
    public void TC_005_Native_MF_Subs_FreqUnitBiMonthly(@Optional("enhancedweb_revamp") String theme) throws Exception{


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("BI_MONTHLY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.orderFromBody(),txnToken)
                .build();

        checkoutPage.createAppInvokeOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();



        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBLC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals("").not()

        );
        sAssert.eval();

    }

    @Override
    @Parameters({"theme"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, freq=SEMI_ANNUALLY")
    public void TC_006_Enhanced_MF_Subs_FreqUnitSemiAnually(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SEMI_ANNUALLY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addMonths(date, "yyyy-MM-dd", 6))
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.orderFromBody(),txnToken)
                .build();

        checkoutPage.createAppInvokeOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();



        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBLC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals("").not()

        );
        sAssert.eval();

    }

    @Override
    @Parameters({"theme"})
    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, freq=ONDEMAND")
    public void TC_007_Enhanced_MF_Subs_FreqUnitOnDemand(@Optional("enhancedweb_revamp") String theme) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.orderFromBody(),txnToken)
                .build();

        checkoutPage.createAppInvokeOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();



        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "SUBS_ID"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBLC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.subsId().equals("").not()

        );
        sAssert.eval();


    }
    /*Frequency Related TestCases*/
    @Override
    @Parameters({"theme"})
    @Test(description = "Verify Invalid GraceDays when Freq>1 and Frequency Unit Days" +
            "requestType=NATIVE_MF_SIP")
    public void TC_001_Enhanced_MF_Subs_FreqGreaterThn1(@Optional("enhancedweb") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("BI_MONTHLY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
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
                .isEqualToIgnoringCase("Invalid Subscription Frequency");

    }

    @Override
    @Parameters({"theme"})
    @Test(description = "Verify Freq>1 ,SubsPaymode =Unknown " +
            "requestType=NATIVE_MF_SIP UPI Paymode is filtered in FPO")
    public void TC_002_Enhanced_MF_Subs_FreqGreaterthan1_PaymodeBlank(@Optional("enhancedweb")  String theme) throws Exception{

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("3")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.orderFromBody(),txnToken)
                .build();

        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        List<String> paymethodList = getListOfPayModesOnCashierPage(cashierPage);
        Assert.assertFalse(paymethodList.contains("BHIM UPI"));
    }


    @Override
    @Parameters({"theme"})
    @Test(description = "Verify Freq is Blank By Default it will consider it 1" +
            "requestType=NATIVE_MF_SIP")
    public void TC_003_Enhanced_MF_Subs_FreqIsBlank(@Optional("enhancedweb") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.orderFromBody(),txnToken)
                .build();

        checkoutPage.createAppInvokeOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();
    }



    /*Start Date Related TestCases*/


    @Override
    @Parameters({"theme"})
    @Test(description = "Verify StartDate is equal to Future Date Supported in UPI " +
            "requestType=NATIVE_MF_SIP")
    public void TC_001_Enhanced_MF_Subs_StartDateIsFuture(@Optional("enhancedweb") String theme) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addYears(date, "yyyy-MM-dd", 1))
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.orderFromBody(),txnToken)
                .build();

        checkoutPage.createAppInvokeOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();
    }

    @Override
    @Parameters({"theme"})
    @Test(description = "Verify blank subscriptionGraceDays are not supported")
    public void TC_002_Enhanced_MF_Subs_StartDateGraceDayNull(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("")
                .setSubscriptionRetryCount("1")
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
                .isEqualToIgnoringCase("Grace days value is mandatory");

    /*    InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.orderFromBody(),txnToken)
                .build();

        checkoutPage.createAppInvokeOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        List<String> paymethodList = getListOfPayModesOnCashierPage(cashierPage);
        Assert.assertFalse(paymethodList.contains("BHIM UPI"));*/

    }


    /*Enable Retry TestCases*/


    @Override
    @Parameters({"theme"})
    @Test(description = "Verify when Paymode=UPI SubsRetry >2 Paymode=null , BHIM UPI is not allowed for this transaction, " +
            "kindly use some other payment mode requestType=NATIVE_MF_SIP")
    public void TC_001_Enhanced_MF_Subs_EnableRetryGreaterThan2PaymodeBlank(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("3")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.orderFromBody(),txnToken)
                .build();


        checkoutPage.createAppInvokeOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        List<String> paymethodList = getListOfPayModesOnCashierPage(cashierPage);
        Assert.assertFalse(paymethodList.contains("BHIM UPI"));
    }



    /*Subscription Max Amount TestCases*/



    //Ideally it should fail incase subs max amount is greater than 2500


    @Override
    @Parameters({"theme"})
    @Test(description = "If subscriptionMaxAmount>5000 and txn amount>5000, and paymode=Null," +
            "resultMsg: Subscription Amount Limit For UPI Breached requestType=NATIVE_MF_SIP")
    public void TC_001_Enhanced_MF_Subs_MaxAmountGreaterThan5000PaymodeNull(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("2100")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("2100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.orderFromBody(),txnToken)
                .build();

        checkoutPage.createAppInvokeOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        List<String> paymethodList = getListOfPayModesOnCashierPage(cashierPage);
        Assert.assertFalse(paymethodList.contains("BHIM UPI"));

    }


    @Issue("PGP-26277")
    @Test(description = "If subscriptionMaxAmount>5000 and txn amount<5000, and paymode=null," +
            "requestType=NATIVE_MF_SIP")
    public void TC_002_Enhanced_MF_Subs_OnlyMaxAmountGreaterThanMaxAmount(@Optional("enhancedweb") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("DAY")
                .setTXN_AMOUNT("1900")
                .setSUBS_MAX_AMOUNT("2500")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        List<String> paymethodList = getListOfPayModesOnCashierPage(cashierPage);
        Assert.assertFalse(paymethodList.contains("BHIM UPI"));
    }

    /*Amount Type TestCases*/

    @Override
    @Parameters({"theme"})
    @Test(description = "For FIX amount type,if  equal amount is not passed for txn amount & max amount if payment mode=null," +
            " then it should fail at the time of process txn " )
    public void TC_001_Enhanced_MF_Subs_FixAmountEqualAmountNotPassed(@Optional("enhancedweb_revamp") String theme) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.orderFromBody(),txnToken)
                .build();

        checkoutPage.createAppInvokeOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
       // cashierPage.payBy(Constants.PayMode.UPI);
        cashierPage.tabUPI().assertNotVisible();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.execute();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("PENDING")
                .validateTxnType("SALE")
                .validateMid(orderDTO.getMID())
                .validateRespCode("402")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();
    }


    @Override
    @Parameters({"theme"})
    @Test(description = "For the Variable amount, the subscription txn amount should be" +
            " less than or equal to the subscription max amount" )
    public void TC_002_Enhanced_MF_Subs_VariableAmountEqualOrLessMaxAmount(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.orderFromBody(),txnToken)
                .build();

        checkoutPage.createAppInvokeOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();
    }


    /*Validate Account Number For MF_SIP*/

    @Override
    @Parameters({"theme"})
    @Test(description = "validateAccountNumber:true, accountNumber:valid " +
            " allow Unverified account false txn is successful")
    public void TC_001_Enhanced_MF_Subs_AccountNumTrueAndValid(@Optional("enhancedweb_revamp") String theme) throws Exception {


        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setValidateAccountNumber("true")
                .setSubscriptionFrequency("1")
                .setAccountNumber("112343132122")
                .setAllowUnverifiedAccount("false")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("23")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addMonths(date, "yyyy-MM-dd", 1))
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.orderFromBody(),txnToken)
                .build();

        checkoutPage.createAppInvokeOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(subsId)
                .validateCheckSum(Constants.MerchantType.UPI_MUTUAL_MF.getKey())
                .assertAll();
    }


    @Override
    @Parameters({"theme"})
    @Test(description = "validateAccountNumber:true, accountNumber:invalid,"+
            " allow Unverified account : false , then it should fail at the time of process txn " )
    public void TC_002_Enhanced_MF_Subs_AccountNumTrueAndInvalid(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setValidateAccountNumber("true")
                .setSubscriptionFrequency("1")
                .setAccountNumber("7777777")
                .setAllowUnverifiedAccount("false")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("235")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addYears(date, "yyyy-MM-dd", 1))
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.orderFromBody(),txnToken)
                .build();

        checkoutPage.createAppInvokeOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("227")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(subsId)
                .validateCheckSum(merchant.getKey())
                .assertAll();
    }


    @Override
    @Parameters({"theme"})
    @Test(description = "validateAccountNumber:true, accountNumber:invalid " +
            " allow Unverified account : true , Txn is Successful " )
    public void TC_003_Enhanced_MF_Subs_AccountNumTrueAndInValidUnverifiedFalse(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5000")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("5000")
                .setValidateAccountNumber("true")
                .setSubscriptionFrequency("1")
                .setAccountNumber("7777777")
                .setAllowUnverifiedAccount("true")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("10")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addMonths(date, "yyyy-MM-dd", 1))
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.orderFromBody(),txnToken)
                .build();

        checkoutPage.createAppInvokeOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(subsId)
                .validateCheckSum(merchant.getKey())
                .assertAll();
    }


    @Override
    @Parameters({"theme"})
    @Test(description = "validateAccountNumber:false, accountNumber:invalid,"+
            "  Txn is Successful " )
    public void TC_004_Enhanced_MF_Subs_AccountNumFalseAndValid(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String date = CommonUtils.getdate("yyyy-MM-dd");
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setValidateAccountNumber("false")
                .setSubscriptionFrequency("1")
                .setAccountNumber("7777777")
                .setAllowUnverifiedAccount("false")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("10")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.addMonths(date, "yyyy-MM-dd", 1))
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.orderFromBody(),txnToken)
                .build();

        checkoutPage.createAppInvokeOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(subsId)
                .validateCheckSum(merchant.getKey())
                .assertAll();
    }

    /* ONDEMAND VALIDATIONS PGP-26500*/

    @Override
    @Parameters({"theme"})
    @Test(description = "Verify that Frequency, StartDate, GracePeriod, RetryAllowed & RetryCount are ignored when " +
            "requestType=NATIVE_SUBS, freqUnit=ONDEMAND")
    public void TC_001_Enhanced_Subs_OnDemandValidations(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT("10")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("")
                .setSUBS_FREQUENCY("")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .setSUBS_RETRY_COUNT("")
                .setSUBS_ENABLE_RETRY("")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PTYBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();
    }
    @Override
    @Parameters({"theme"})
    @Test(description = "Verify that Frequency, StartDate, GracePeriod, RetryAllowed & RetryCount are ignored when" +
            "requestType=NATIVE_MF_SIP, freqUnit=ONDEMAND")
    public void TC_001_Enhanced_MF_Subs_OnDemandValidations(@Optional("enhancedweb") String theme) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("")
                .setSubscriptionRetryCount("")
                .setSubscriptionEnableRetry("")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_MF_SIP)
                .build();

        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.orderFromBody(),txnToken)
                .build();

        checkoutPage.createAppInvokeOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();
    }
    @Feature("PGP-32799")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"theme"})
    @Test(description = "Verify that sub-error code is displayed in transaction Status respones")
    public void TC_001_Enhanced_Subs_Failure_ErrorCode(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("17")
                .setSUBS_MAX_AMOUNT("20")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("1")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("227")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();

        GetPaymentStatusDTO getPaymentStatusDTOFail = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getMID(), merchant.getKey())
                .build();
        GetPaymentStatus getPaymentStatusFail = new GetPaymentStatus(getPaymentStatusDTOFail);
        Response responseFail = getPaymentStatusFail.execute();
        JsonPath jsonPathFail = responseFail.jsonPath();
        Assertions.assertThat(jsonPathFail.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(jsonPathFail.getString("body.resultInfo.resultCode")).isEqualTo("227");
        Assertions.assertThat(jsonPathFail.getString("body.subResultInfo.resultCode")).isEqualTo("SE110");
        Assertions.assertThat(jsonPathFail.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        Assertions.assertThat(jsonPathFail.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(jsonPathFail.getString("body.txnAmount")).isEqualToIgnoringCase("17.00");

    }

    @Owner(GAURAV)
    @Feature("PGP-36177")
    @Parameters({"theme"})
    @Test(description = "Verify Subs transaction when txn amt =25k and max amt =25k")
    public void verifySubsFixTo25k(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        Constants.MerchantType merchantType = SUBSCRIPTION_PG2_LATEST_ALL;
        Random random = new Random();
        Integer randomNumber = random.nextInt();
        OrderDTO orderDTO = new OrderFactory.SubscriptionUPI(merchantType, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setCUST_ID(randomNumber.toString())
                .setTXN_AMOUNT("25000")
                .setSUBS_AMOUNT_TYPE("FIX")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        String subsId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID);
        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(1).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        UPIRenewSubs.modifySubsDatesInDB(subsId, PreviousDate);

        PreNotify preNotify = new PreNotify(merchantType, "25000", subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        UPIRenewSubs.modifyNotifyDatesInDB(paytmRefId);
        LocalDateTime frequencyReqDate = LocalDateTime.now().minusMonths(1);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());
        UPIRenewSubs.modifySubsDatesInDB(subsId, frequencyReqDate);

        String orderId = UPIRenewSubs.executeRenewalAndFetchOrderId(merchantType, subsId, orderDTO.getTXN_AMOUNT(), "");
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
    }

    @Owner(GAURAV)
    @Feature("PGP-36177")
    @Parameters({"theme"})
    @Test(description = "Verify Subs transaction when txn amt >5k and max amt =25k")
    public void verifySubsGreaterThan5k(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        Constants.MerchantType merchantType = SUBSCRIPTION_PG2_LATEST_ALL;
        Random random = new Random();
        Integer randomNumber = random.nextInt();
        OrderDTO orderDTO = new OrderFactory.SubscriptionUPI(merchantType, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setCUST_ID(randomNumber.toString())
                .setTXN_AMOUNT("6000")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        String subsId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID);
        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(1).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        UPIRenewSubs.modifySubsDatesInDB(subsId, PreviousDate);

        PreNotify preNotify = new PreNotify(merchantType, "2000", subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        UPIRenewSubs.modifyNotifyDatesInDB(paytmRefId);
        LocalDateTime frequencyReqDate = LocalDateTime.now().minusMonths(1);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());
        UPIRenewSubs.modifySubsDatesInDB(subsId, frequencyReqDate);

        String orderId = UPIRenewSubs.executeRenewalAndFetchOrderId(merchantType, subsId, "2000", "");
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
    }

    @Owner(GAURAV)
    @Feature("PGP-36177")
    @Parameters({"theme"})
    @Test(description = "Verify Subs transaction when txn amt <5k and max amt =25k")
    public void verifySubsLessThan5k(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        Constants.MerchantType merchantType = SUBSCRIPTION_PG2_LATEST_ALL;
        Random random = new Random();
        Integer randomNumber = random.nextInt();
        OrderDTO orderDTO = new OrderFactory.SubscriptionUPI(merchantType, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setCUST_ID(randomNumber.toString())
                .setTXN_AMOUNT("100")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        String subsId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID);
        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(1).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        UPIRenewSubs.modifySubsDatesInDB(subsId, PreviousDate);

        PreNotify preNotify = new PreNotify(merchantType, "25000", subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        UPIRenewSubs.modifyNotifyDatesInDB(paytmRefId);
        LocalDateTime frequencyReqDate = LocalDateTime.now().minusMonths(1);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());
        UPIRenewSubs.modifySubsDatesInDB(subsId, frequencyReqDate);

        String orderId = UPIRenewSubs.executeRenewalAndFetchOrderId(merchantType, subsId, "25000", "");
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");
    }
    @Owner(Constants.Owner.Amanpreet)
    @Feature("PGP-39609")
    @Parameters({"theme"})
    @Test(description = "Verify the gateway name in subscription notify bean for UPI renew subscription")
    public void TC_02_UPISubsRenewPPP(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        Constants.MerchantType merchantType = SUBSCRIPTION_PG2_LATEST_ALL;
        OrderDTO orderDTO = new OrderFactory.SubscriptionUPI(merchantType, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setCUST_ID("Test105")
                .setTXN_AMOUNT("10")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        String subsId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID);
        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(1).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        UPIRenewSubs.modifySubsDatesInDB(subsId, PreviousDate);

        PreNotify preNotify = new PreNotify(merchantType, "10", subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        UPIRenewSubs.modifyNotifyDatesInDB(paytmRefId);
        LocalDateTime frequencyReqDate = LocalDateTime.now().minusMonths(1);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());
        UPIRenewSubs.modifySubsDatesInDB(subsId, frequencyReqDate);

        String orderId = UPIRenewSubs.executeRenewalAndFetchOrderId(merchantType, subsId, "10", "");
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");

        //PPP logs verify
        String SubsMerchantNotifyBean = "grep \"" + subsId + "\"  /paytm/logs/paymentPostProcessor.log |grep \"SubscriptionMerchantNotifyBean\"";
        String subsBean = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.PAYMENTPOSTPROCESSOR, SubsMerchantNotifyBean);
        String gateway = subsBean.substring(subsBean.indexOf("gateway="), subsBean.indexOf(", txnDate=")).replace("gateway=", "");
        Assert.assertEquals(gateway,"PPBS");

    }


    @Parameters({"theme"})
    @Feature("PGP-41399")
    @Owner("Himanshu Arora")
    @Test(description = "validate correct Upi Vpa case in case of enhanced flow with respcode 0 in theia facade logs.")
    public void UpiVpaCases_01(@Optional("enhancedweb_revamp") String theme) throws Exception {
        double txn_amount = 20;
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL, theme)
                .setTXN_AMOUNT(toString().valueOf(txn_amount))
                .build();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        checkoutPage.createOrder(orderDTO);
        Thread.sleep(100);
        cashierPage.tabUPI().click();
        cashierPage.textBoxVPA().sendKeys("test@paytm");
        cashierPage.buttonPGPayNow().click();
        Thread.sleep(100);
        cashierPage.verifiedVpaID().isDisplayed();

        String theiaFaacadeLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"UPI_SECURE");
        //String grepcmd = "grep \"" + "\" /paytm/logs/theia_facade.log  | " +"grep \"UPI_SECURE\"  ";
       // String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFaacadeLogs).contains("\"status\":\"SUCCESS\"");
        Assertions.assertThat(theiaFaacadeLogs).contains("\"respCode\":\"0\"");
    }

    @Parameters({"theme"})
    @Feature("PGP-41399")
    @Owner("Himanshu Arora")
    @Test(description = "validate invalid Upi Vpa case in case of enhanced flow with respcode 37 in theia facade logs.")
    public void UpiVpaCases_02(@Optional("enhancedweb_revamp") String theme) throws Exception {
        double txn_amount = 20;
//        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(SUBSCRIPTION_PG2_LATEST_ALL, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().click();
        cashierPage.textBoxVPA().sendKeys("get@paytm");
        cashierPage.buttonPGPayNow().click();
        Thread.sleep(100);
        String msg=cashierPage.errorTextsInUPIFlow().getText();
        Assert.assertEquals(msg,Constants.MessageAssert.INVALID_VPA.toString());

        String grepcmd = "grep \"" + "\" /paytm/logs/theia_facade.log  | " +"grep \"UPI_SECURE\"  ";
        //String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        String theiaFaacadeLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"UPI_SECURE");
        Assertions.assertThat(theiaFaacadeLogs).contains("\"status\":\"FAILURE\"");
        Assertions.assertThat(theiaFaacadeLogs).contains("\"respCode\":\"37\"");
        Assert.assertTrue(cashierPage.invalidVpaText().isElementPresent());
    }

    @Parameters({"theme"})
    @Feature("PGP-41455")
    @Owner("Himanshu Arora")
    @Test(description = "validate only Upi Vpa is enabled on cashier page when DISABLE_UPI_COLLECT_NUMERIC_ID is true on MID in case of enhanced flow.")
    public void UpiVpaCases_05(@Optional("enhancedweb_revamp") String theme) throws Exception {
        double txn_amount = 20;
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL, theme)
                .setTXN_AMOUNT(toString().valueOf(txn_amount))
                .build();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        checkoutPage.createOrder(orderDTO);
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.textBoxVPA().clearAndType("8512005349@paytm");
        cashierPage.verifyVPALinkText().click();
        Assert.assertFalse(cashierPage.verifyUpiNumericID().isElementPresent());
    }

    @Owner("Himanshu Arora")
    @Feature("PGP-42048")
    @Parameters({"theme"})
    @Test(description = "Verify only vpa is displayed for subcription for correct vpa")
    public void UpiVpaCases_07(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String TxnMaxAmount = "1";
        String SubscriptionPurpose = "Loan Payments";
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL, theme)
                .setSUBS_PAYMENT_MODE("UPI")
                .setCHANNEL_ID("WEB")
                .setREQUEST_TYPE("SUBSCRIBE")
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("1")
                .setSUBS_FREQUENCY("1")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        cashierPage.textBoxVPA().clearAndType("8512005349@paytm");
        cashierPage.verifyVPALinkText().click();
        Assert.assertFalse(cashierPage.verifyUpiNumericID().isElementPresent());
    }

    @Owner("Himanshu Arora")
    @Feature("PGP-42048")
    @Parameters({"theme"})
    @Test(description = "Verify only vpa is displayed for subcription for incorrect vpa")
    public void UpiVpaCases_08(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String TxnMaxAmount = "1";
        String SubscriptionPurpose = "Loan Payments";
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL, theme)
                .setSUBS_PAYMENT_MODE("UPI")
                .setCHANNEL_ID("WEB")
                .setREQUEST_TYPE("SUBSCRIBE")
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("1")
                .setSUBS_FREQUENCY("1")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        cashierPage.textBoxVPA().clearAndType("85120053497@paytm");
        cashierPage.verifyVPALinkText().click();
        Assert.assertTrue(cashierPage.invalidVpaText().isElementPresent());
    }

    @Parameters({"theme"})
    @Feature("PGP-42069")
    @Owner("Himanshu Arora")
    @Test(description = "validate correct Upi numericid case in case of enhanced flow with respcode 0 in theia facade logs.")
    public void UpiNumericId_01(@Optional("enhancedweb_revamp") String theme) throws Exception {
        double txn_amount = 20;
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL, theme)
                .setTXN_AMOUNT(toString().valueOf(txn_amount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().click();
        cashierPage.UpiNumericId().sendKeys("8006006993");
        cashierPage.buttonPGPayNow().click();
        cashierPage.verifyUpiNumericID().isDisplayed();
        //String grepcmd = "grep \"" + "\" /paytm/logs/theia_facade.log  | " +"grep \"UPI_SECURE\"  ";
       // String theiaFaacadeLogs= theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
       String  theiaFaacadeLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"UPI_SECURE");
        Assertions.assertThat(theiaFaacadeLogs).contains("\"status\":\"SUCCESS\"");
        Assertions.assertThat(theiaFaacadeLogs).contains("\"respCode\":\"0\"");
    }
    @Parameters({"theme"})
    @Feature("PGP-42069")
    @Owner("Himanshu Arora")
    @Test(description = "validate invalid Upi numericid case in case of enhanced flow with respcode INT-1766 in theia facade logs.")
    public void UpiNumericId_02(@Optional("enhancedweb_revamp") String theme) throws Exception {
        double txn_amount = 20;
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL, theme)
                .setTXN_AMOUNT(toString().valueOf(txn_amount))
                .build();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        checkoutPage.createOrder(orderDTO);
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.UpiNumericId().sendKeys("8512005349");
        cashierPage.verifyUpiNumericID().click();
        String grepcmd = "grep \"" + "\" /paytm/logs/theia_facade.log  | " +"grep \"UPI_SECURE\"  ";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFaacadeLogs).contains("\"status\":\"FAILURE\"");
        Assertions.assertThat(theiaFaacadeLogs).contains("\"respCode\":\"INT-1766\"");
    }

    @Parameters({"theme"})
    @Feature("PGP-41075")
    @Owner("Abhishek Gupta")
    @Test(description = "validate correct Upi numericid case in case of enhanced flow with respcode 0 and validate payerCmid in  api in theia facade logs.")
    public void UpiNumericIdCmid_01(@Optional("enhancedweb_revamp") String theme) throws Exception {
        double txn_amount = 20;
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL, theme)
                .setTXN_AMOUNT(toString().valueOf(txn_amount))
                .build();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        checkoutPage.createOrder(orderDTO);
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.UpiNumericId().sendKeys("8006006993");
        cashierPage.verifyUpiNumericID().click();
        cashierPage.payBy(Constants.PayMode.UPI);
        String grepcmd = "grep \"" + "\" /paytm/logs/theia_facade.log  | " +"grep \"UPI_SECURE\"  ";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFaacadeLogs).contains("\"status\":\"SUCCESS\"");
        Assertions.assertThat(theiaFaacadeLogs).contains("\"respCode\":\"0\"");
        Assertions.assertThat(theiaFaacadeLogs).contains("\"cmId\":\"8006006993\"");
        Assertions.assertThat(theiaFaacadeLogs).contains("\"vpa\":\"srivastavaprateek@paytm\"");
        String grepcmd1 = "grep \"" + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getORDER_ID() + "\" | grep \"ACQUIRING_PAY_ORDER\" ";
        String theiaFacade = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd1);
        PGPHelpers pgpHelpers=new PGPHelpers();
        String passThroughExtendInfo=pgpHelpers.getPassThroughExtendedInfo(theiaFacade);
        String decrpted=PGPHelpers.Base64Decode(passThroughExtendInfo);
        String getPayerCmid=pgpHelpers.getPayerCmid(decrpted);
        Assert.assertEquals(getPayerCmid,"8006006993");
    }

    
    @Parameters({"theme"})
    @Feature("PGP-41075")
    @Owner("Abhishek Gupta")
    @Test(description = "validate correct Upi numericid case in case of enhanced flow with status failure and cmid not coming in theia facade logs.")
    public void UpiNumericIdCmid_02(@Optional("enhancedweb_revamp") String theme) throws Exception {
        double txn_amount = 20;
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL, theme)
                .setTXN_AMOUNT(toString().valueOf(txn_amount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().click();
        cashierPage.UpiNumericId().sendKeys("8006006994");
        cashierPage.buttonPGPayNow().click();
        String msg=cashierPage.errorTextsInUPIFlow().getText();
        Assert.assertEquals(msg,"UPI Number does not exist");
        //String grepcmd = "grep \"" + "\" /paytm/logs/theia_facade.log  | " +"grep \"UPI_SECURE\"  ";
        //String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        String theiaFaacadeLogs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"UPI_SECURE");
        Assertions.assertThat(theiaFaacadeLogs).contains("\"status\":\"FAILURE\"");
        Assertions.assertThat(theiaFaacadeLogs).contains("\"respCode\":\"INT-1766\"");
        Assertions.assertThat(theiaFaacadeLogs).contains("\"respMessage\":\"This UPI Number does not exist.\"");
    }


    @Owner(Constants.Owner.AKSHAT)
    @Feature("PGP-55339")
    @Parameters({"theme"})
    @Test(description = "Verify that create subscription logs are printed in theia facade logs")
    public void TC_001_subscriptionClientRemoval_createCollectFlow(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT("10")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("")
                .setSUBS_FREQUENCY("")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .setSUBS_RETRY_COUNT("")
                .setSUBS_ENABLE_RETRY("")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID());
        Assertions.assertThat(logs).contains("\"COMPONENT\": \"SUBSCRIPTION_SERVICE\"");
        Assertions.assertThat(logs).contains(LocalConfig.PGP_HOST +"/subscription/subscription/create");

    }

    @Owner(Constants.Owner.AKSHAT)
    @Feature("PGP-58496")
    @Parameters({"theme"})
    @Test(description = "Verify that expiry time is passed in create_order request for Enhanced subscription")
    public void validateExpiryTime_Enhanced(@Optional("enhancedweb_revamp") String theme) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.P4B_NOTIFICATION_MID;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT("10")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("0")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderDTO.getORDER_ID(), "ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("\"timeoutConfig\":[{\"timeoutType\":\"EXPIRY_TIMEOUT\",\"disabled\":false,\"timeoutInSeconds\":\"900\"}]");


    }

    @Owner(Constants.Owner.AKSHAT)
    @Feature("PGP-58835")
    @Parameters({"theme"})
    @Test(description = "Verify order is created on Product Code 51051000100000000004 for PCF & Subs enabled MID")
    public void validateEnhancedOrderCreateon_51051000100000000004(@Optional("enhancedweb_revamp") String theme) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.Subs_PCF_fix;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT("10")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderDTO.getORDER_ID(), "ACQUIRING_CREATE_ORDER");
        Assertions.assertThat(logs).contains("\"productCode\":\"51051000100000000004\"");


    }

    @Owner(Constants.Owner.AKSHAT)
    @Feature("PGP-59241")
    @Parameters({"theme"})
    @Test(description = "Verify order is created on Product Code 51051000100000000052 for PCF & Subs enabled MID")
    public void validateEnhancedOrderCreateon_51051000100000000052(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_Pg2_MID2;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("0")
                .setSUBS_MAX_AMOUNT("50")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_RETRY_COUNT("0")
                .setSUBS_ENABLE_RETRY("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID());
        Assertions.assertThat(logs).contains("\"API_NAME\": \"ACQUIRING_PAY_ORDER\"");
        Assertions.assertThat(logs).contains("\"productCode\":\"51051000100000000052\"");
        Assertions.assertThat(logs).contains("{\"resultInfo\":{\"resultCode\":\"SUCCESS\",\"resultCodeId\":\"00000000\",\"resultStatus\":\"S\",\"resultMsg\":\"success\"}");


    }

    @Owner(Constants.Owner.AKSHAT)
    @Feature("PG-5982")
    @Parameters({"theme"})
    @Test(description = "Verify that retry is enabled on subscription transactions")
    public void verifyRetryEnabled_onSubscriptionTransaction(@Optional("enhancedweb_revamp") String theme) throws Exception {

        // FF4j : theia.enableRetryForNativeSubscription  //

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.Subs_PCF_fix;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("17")
                .setSUBS_MAX_AMOUNT("50")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_RETRY_COUNT("0")
                .setSUBS_ENABLE_RETRY("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        cashierPage.waitUntilLoads();
        cashierPage.ErrorRetryButton().assertVisible();

    }


    @Owner(Constants.Owner.AKSHAT)
    @Feature("PG-5982")
    @Parameters({"theme"})
    @Test(description = "Verify that retry is successfully attempted (either pass/fail)")
    public void verifyRetry_isSuccessfullyAttempted(@Optional("enhancedweb_revamp") String theme) throws Exception {

        // FF4j : theia.enableRetryForNativeSubscription  //

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.Subs_PCF_fix;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("17")
                .setSUBS_MAX_AMOUNT("50")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_RETRY_COUNT("0")
                .setSUBS_ENABLE_RETRY("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        cashierPage.waitUntilLoads();
        cashierPage.ErrorRetryButton().assertVisible();

        cashierPage.ErrorRetryButton().click();
        cashierPage.payBy(Constants.PayMode.UPI);
        cashierPage.waitUntilLoads();
        cashierPage.ErrorRetryButton().assertVisible();

    }

    @Owner(Constants.Owner.AKSHAT)
    @Feature("PG-5982")
    @Parameters({"theme"})
    @Test(description = "Verify that for failure transaction retry is not attempted when retry=disabled")
    public void verifyRetryDisabled_forSubscriptionTransaction(@Optional("enhancedweb_revamp") String theme) throws Exception {

        // FF4j : theia.enableRetryForNativeSubscription  //

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_Pg2_MID2;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("17")
                .setSUBS_MAX_AMOUNT("50")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_RETRY_COUNT("0")
                .setSUBS_ENABLE_RETRY("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();


    }

}
