package scripts;

import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.RiskExtendInfo;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.*;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.appconstants.Constants.Owner.GAGANDEEP;

@Owner("Nikunj")
public class RiskReject extends PGPBaseTest{
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final RiskVerificationPage riskVerificationPage = new RiskVerificationPage();
    //private static String CCPaymentDetails = "4718650100010336|882|052026";
    private final String riskUserMailAddress="ankit2.arora@paytm.com";

    private String getPaymentDetails(PaymentDTO paymentDTO){
        return paymentDTO.getCreditCardNumber()+"|"+paymentDTO.getCvvNumber()+"|"+paymentDTO.getExpMonth()+paymentDTO.getExpYear();
    }


    protected RiskExtendInfo riskExtendInfoDTO() {

        return new RiskExtendInfo()
                .setOperationOrigin("APP").setOperationType("rent payment").setRentMonthYear("Dec2019").setRentPerMonth("100000")
                .setUserMerchant("216810000016528000000").setIsRentalsPayment("True").setPaytmMerchantId("amanja38238280372187").setIFSC("SBIN000322")
                .setPanCard("CPGPK1767M").setSelfAccount("TRUE").setPanNameMatchFlag("TRUE").setBankAccountNameMatchFlag("TRUE").setIsHighRiskBankAccount("TRUE").setCpId("216810000016528000000")
                .setCpFirstName("A").setCpMiddleName("B").setCpLastName("C").setCpName("C").setCpEmail("abc@gmail.com").setCpMobile("8989898989").setCpIdentityType("ID_CARD").setCpIdentityNo("5bb910960d8a026159d331777b7a9ba7233351e68561485d17a3bc65faff1c2f")
                .setCpCountry("IN").setCpState("UP").setCpCity("Ghaziabad").setCpArea("indirapuram").setCpPostalCode("201010").setCpStreet1("windsor street").setCpStreet2("windsor street")
                .setCpAddress("plot 211 , Abhay khand 1").setCpPaytmUserId("1107223757").setCpAccountNo("9199939333999939").setCpIFSC("SBIN2022202")
                .setCpglobalCardIndex("20200605666006118312bd3b9f606c4879ede6b9fa5d4").setCpVPA("1107223757").setCpVpaName("xyz");
    }


    @Parameters({"theme"})
    @Test(description = "To verify successful PG txn using CC when user is logged in and Risk amount is passed(Alert Accept)")
    public void SuccessfulPGOnlyCCTxnWhenUserLoggedInRiskAlertAccept(@Optional("enhancedwap") String theme) throws Exception {
        User user=userManager.getForRead(Label.RISKVERIFY);
        String riskAmount = "1.8";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.NATIVE_RISK, theme)
                .setTXN_AMOUNT(riskAmount)
                .setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        riskVerificationPage.clickAlert();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "To verify successful Native txn using DC when user is logged in and Risk amount is passed(Alert Accept)")
    public void SuccessfulNativeDCTxnWhenUserLoggedInRiskAlertAccept(@Optional("false") Boolean isNativePlus) throws Exception {
        User user=userManager.getForRead(Label.RISKVERIFY);
        String riskAmount = "1.8";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_RISK).
                setTxnValue(riskAmount)
                .setRiskExtendInfo(riskExtendInfoDTO()).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO=new PaymentDTO();
        paymentDTO.setCreditCardNumber(paymentDTO.getDebitCardNumber());
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.NATIVE_RISK, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setPAYMENT_DETAILS(getPaymentDetails(paymentDTO))
                .setTXN_AMOUNT(riskAmount).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        riskVerificationPage.clickAlert();
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters("theme")
    @Test(description = "To verify offline PG txn using DC when user is logged in and Risk amount is passed(Alert Accept)")
    public void SuccessfulOfflineDCTxnWhenUserLoggedInRiskAlertAccept(@Optional("merchant4") String theme) throws Exception {
        User user=userManager.getForRead(Label.RISKVERIFY);
        String riskAmount = "1.8";
        PaymentDTO paymentDTO=new PaymentDTO();
        paymentDTO.setCreditCardNumber(paymentDTO.getDebitCardNumber());
        OrderDTO orderDTO = new OrderFactory.OffLineTxn(Constants.MerchantType.NATIVE_RISK, theme, user)
                .setPAYMENT_DETAILS(getPaymentDetails(paymentDTO))
                .setPAYMENT_TYPE_ID("DC")
                .setTXN_AMOUNT(riskAmount)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        riskVerificationPage.clickAlert();
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "To verify failure PG txn using CC when user is logged in and Risk amount is passed(Alert Back button clicked)")
    public void VerifyFailureWhenUserLoggedInAlertBack(@Optional("enhancedwap") String theme) throws Exception {
        User user=userManager.getForRead(Label.RISKVERIFY);
        String riskAmount = "1.8";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.NATIVE_RISK, theme)
                .setTXN_AMOUNT(riskAmount)
                .setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        riskVerificationPage.cancelAlert();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(((String) null))
                .validateRespCode("810")
                .validateRespMsg("Payment failed due to a technical error. Please try after some time.")
                .validateMid(orderDTO.getMID())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "To verify successful PG txn using DC when user is logged in and Risk amount is passed(Paytm Password)")
    public void SuccessfulPGOnlyDCTxnWhenUserLoggedInRiskPaytmPwd(@Optional("merchant4") String theme) throws Exception {
        User user=userManager.getForRead(Label.RISKVERIFY);
        String riskAmount = "1.4";
        PaymentDTO paymentDTO=new PaymentDTO();
        paymentDTO.setCreditCardNumber(paymentDTO.getDebitCardNumber());
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setTXN_AMOUNT(riskAmount)
                .setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        riskVerificationPage.fillPwd("paytm@123");
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "To verify successful Native txn using DC when user is logged in and Risk amount is passed(Paytm Password)")
    public void SuccessfulNativeDCTxnWhenUserLoggedInRiskPaytmPwd(@Optional("false") Boolean isNativePlus) throws Exception {
        User user=userManager.getForRead(Label.RISKVERIFY);
        String riskAmount="1.4";
        PaymentDTO paymentDTO=new PaymentDTO();
        paymentDTO.setCreditCardNumber(paymentDTO.getDebitCardNumber());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_RISK)
                .setTxnValue(riskAmount).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.NATIVE_RISK, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setPAYMENT_DETAILS(getPaymentDetails(paymentDTO))
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        riskVerificationPage.fillPwd("paytm@123");
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "To verify successful Native txn using DC when user is logged in and Risk amount is passed(OTP SMS)")
    public void SuccessfulNativeDCTxnWhenUserLoggedInRiskOTPSMS(@Optional("false") Boolean isNativePlus) throws Exception {
        User user=userManager.getForRead(Label.RISKVERIFY);
        String riskAmount="8.3";
        PaymentDTO paymentDTO=new PaymentDTO();
        paymentDTO.setCreditCardNumber(paymentDTO.getDebitCardNumber());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_RISK)
                .setTxnValue(riskAmount).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.NATIVE_RISK, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setPAYMENT_DETAILS(getPaymentDetails(paymentDTO))
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        riskVerificationPage.enterSmsOtp(user.mobNo());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "To verify successful PG txn using DC when user is logged in and Risk amount is passed(OTP SMS)")
    public void SuccessfulPGOnlyDCTxnWhenUserLoggedInRiskOTPSMS(@Optional("enhancedweb") String theme) throws Exception {
        User user=userManager.getForRead(Label.RISKVERIFY);
        PaymentDTO paymentDTO=new PaymentDTO();
        paymentDTO.setCreditCardNumber(paymentDTO.getDebitCardNumber());
        String riskAmount="8.3";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.NATIVE_RISK, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT(riskAmount).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        riskVerificationPage.enterSmsOtp(user.mobNo());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "To verify successful PG txn using DC when user is logged in and Risk amount is passed(OTP mail)")
    public void SuccessfulPGOnlyDCTxnWhenUserLoggedInRiskOTPEmail(@Optional("merchant4") String theme) throws Exception {
        User user=userManager.getForRead(Label.RISKVERIFY);
        PaymentDTO paymentDTO=new PaymentDTO();
        paymentDTO.setCreditCardNumber(paymentDTO.getDebitCardNumber());
        String riskAmount="1.2";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.NATIVE_RISK, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT(riskAmount).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        riskVerificationPage.enterMailOtp(riskUserMailAddress);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }


    @Owner("Gagandeep")
    @Feature("PGP-28964")
    @Description("Headers in UIMicroservices api")
    @Parameters({"theme"})
    @Test(description = "To verify successful Native txn using CC when user is logged in and Risk amount is passed(OTP Mail)")
    public void SuccessfulNativeCCTxnWhenUserLoggedInRiskOTPEmail(@Optional("false") Boolean isNativePlus) throws Exception {
        User user=userManager.getForRead(Label.RISKVERIFY);
        String riskAmount="1.2";
        PaymentDTO paymentDTO=new PaymentDTO();
        paymentDTO.setCreditCardNumber(paymentDTO.getDebitCardNumber());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_RISK)
                .setTxnValue(riskAmount).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.NATIVE_RISK, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setPAYMENT_DETAILS(getPaymentDetails(paymentDTO))
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        riskVerificationPage.enterMailOtp(riskUserMailAddress);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }


    @Owner(GAGANDEEP)
    @Feature("PGP-28964")
    @Description("Headers in UIMicroservices api")
    @Parameters({"theme"})
    @Test(description = "To verify when user is logged in and Risk amount is passed(Paytm Password)Data Is Encoded By UImicroservice that is MID is present in ff4J flag(theia.uimicroservice.riskflow.feature)")
    public void UImicroserviceEncodedWhenMidPresentInFF4JFlag(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.RISKVERIFY);
        String riskAmount = "1.4";
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(paymentDTO.getDebitCardNumber());
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setTXN_AMOUNT(riskAmount)
                .setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        riskVerificationPage.fillPwd("paytm@123");
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateOrderId(orderDTO.getORDER_ID()).assertAll();
    }




    @Owner(GAGANDEEP)
    @Feature("PGP-28964")
    @Description("Headers in UIMicroservices api")
    @Parameters({"theme"})
    @Test(description = "To verify when user is logged in and Risk amount is passed(Paytm Password)Data Is Encoded By UImicroservice that is Not MID is present in ff4J flag(theia.uimicroservice.riskflow.feature)")
    public void UImicroserviceEncodedWhenMidNotPresentInFF4JFlag(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.RISKVERIFY);
        String riskAmount = "1.4";
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(paymentDTO.getDebitCardNumber());
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.Hybrid, theme)
                .setTXN_AMOUNT(riskAmount)
                .setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        riskVerificationPage.fillPwd("paytm@123");
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateOrderId(orderDTO.getORDER_ID()).assertAll();
    }





}

