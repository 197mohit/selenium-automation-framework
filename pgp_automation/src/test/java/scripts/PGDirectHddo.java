package scripts;

import com.paytm.api.GetPaymentStatus;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.*;
import com.paytm.base.test.User;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedPaymentOffers;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.*;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.appconstants.Constants.PayMode.CC;
import static com.paytm.appconstants.Constants.ValidationType.NON_EMPTY;
import static com.paytm.base.test.Group.Status;
import static com.paytm.base.test.Group.Theme;

@Owner("Jai")
public class PGDirectHddo extends PGPBaseTest {

    private CheckoutPage checkoutPage = new CheckoutPage();
    private ResponsePage responsePage;
    private final DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();

    @Parameters("theme")
    @Test(description = "Verify successful transaction using HDFC CC in HDDO Flow", groups = {Theme.MERCHANT4, Theme.ENHANCEDWAP, Theme.ENHANCEDWEB})
    public void verifySuccessfulPGOnlyTxnHDFCCard(@Optional("enhancedweb_revamp") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.HDDOMERCHANT, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
//        cashierPage.assertVisible();
        cashierPage.payBy(CC);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.assertVisible();
        directBankOTPPage.submitOtp(PaymentDTO.OTP);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(NON_EMPTY)
                .validateBankTxnId(NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDDO.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Test(description = "Verify successful Promo transaction in Direct Bank Flow")
    public void verifySuccessfulPromoTxn() throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        CheckoutPage checkoutPage = new CheckoutPage();
        Constants.MerchantType merchantType = MerchantType.HDDOMERCHANT;
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        Promo promo = new Promo();
        new Merchant(merchantType.getId(), true).getPromos().add(promo);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String trxToken = iniJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, trxToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.assertVisible();
        directBankOTPPage.submitOtp(PaymentDTO.OTP);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(NON_EMPTY)
                .validateBankTxnId(NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDDO.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validatePaymentPromoCheckoutDataPresent()
                .validatePayableAmount("1.00")
                .validateTxnDate(new Date())
                .AssertAll();

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = merchant.execute();
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase("1.00");
        Assertions.assertThat(jsonPath.getString("body.payableAmount")).isEqualToIgnoringCase("1.00");
        Assertions.assertThat(jsonPath.getString("body.paymentPromoCheckoutData")).isNotNull();
    }

    @Parameters("theme")
    @Test(description = "Verify successful transaction using ICICI CC in HDDO Flow", groups = {Theme.MERCHANT4, Theme.ENHANCEDWAP, Theme.ENHANCEDWEB})
    public void verifySuccessfulPGOnlyTxnICICICard(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.HDDOMERCHANT, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        cashierPage.payBy(CC,paymentDTO);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.assertVisible();
        directBankOTPPage.submitOtp(PaymentDTO.OTP);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(NON_EMPTY)
                .validateBankTxnId(NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDDO.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("ICICI Bank")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters("theme")
    @Test(description = " Validate same bank logo and name should be visible as the Card Used", groups = {"regression"})
    public void visibilityOfBankLogo(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.HDDOMERCHANT, theme).build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.assertBankLogo("HDFC");
    }

    @Parameters("theme")
    @Test(description = " Validate Resend OTP Button and Go to Bank Website Link is not present in Web", groups = {"regression"})
    public void ValidateResendOTPButtonNotPresent(@Optional("enhancedweb") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.HDDOMERCHANT, theme).build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.ResendOTPLink().assertNotVisible();
        directBankOTPPage.GoToBankWebsiteLink().assertNotVisible();
    }

    @Parameters("theme")
    @Test(description = "After entering invalid otp, direct bank page should not appear", groups = {"regression"})
    public void ValidatewithInvalidOTP(@Optional("enhancedweb_revamp") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.HDDOMERCHANT, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(CC);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.submitOtp("808080");
        cashierPage.waitUntilLoads();
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        if(theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WEB_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WAP_REVAMP))
        {cashierPage.closeCcDcDetailBtn().click();}
        cashierPage.assertVisible();
    }

    @Parameters("theme")
    @Test(description = "Verify Cashier page will appear till merchant retry count is breached after entering " +
            "invalid otp at direct bank page", groups = {"regression"})
    public void ValidateMerchantRetryBreach(@Optional("enhancedweb_revamp") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.HDDOMERCHANT, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(CC);
        directBankOTPPage.waitUntilLoads();
        //Merchant retry count is 2
        directBankOTPPage.assertVisible();
        directBankOTPPage.submitOtp("808080");
        cashierPage.waitUntilLoads();
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        if(theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WEB_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WAP_REVAMP))
        {cashierPage.closeCcDcDetailBtn().click();}
        cashierPage.assertVisible();
//        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        cashierPage.payBy(CC);
        directBankOTPPage.waitUntilLoads();

        directBankOTPPage.assertVisible();
        directBankOTPPage.submitOtp("808080");
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        if(theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WEB_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WAP_REVAMP))
        {cashierPage.closeCcDcDetailBtn().click();}
        cashierPage.assertVisible();

//        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        cashierPage.payBy(CC);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.assertVisible();
        directBankOTPPage.submitOtp("808080");
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE")
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateGatewayName(Gateway.HDDO.toString())
                .validateRespCode("227")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Test(description = "Verify resend OTP button is not visible after 15s and redirectToBankPage will not appear after 30 s", groups = {"regression"})
    public void verifyGotoBankWebsiteLinkNotPresent(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.HDDOMERCHANT, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        DirectBankOTPPage directBankOTPPage = DirectBankPageFactory.getDirectBankPage(theme);
        directBankOTPPage.waitUntilLoads();
        //OTP Time expires
        directBankOTPPage.assertVisible();
        directBankOTPPage.pause(15);
        directBankOTPPage.ResendOTPLink().assertNotVisible();
        directBankOTPPage.pause(15);
        directBankOTPPage.GoToBankWebsiteLink().assertNotVisible();
    }


}
