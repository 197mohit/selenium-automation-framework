package scripts;

import com.paytm.CreateToken;
import com.paytm.LocalConfig;
import com.paytm.api.Deals.InitiateTransaction;
import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.nativeAPI.RenewSubscription;
import com.paytm.api.theia.emiSubvention.ApiV1Tenure;
import com.paytm.api.theia.emiSubvention.ApiV1Validate;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.*;
import com.paytm.apphelpers.*;
import com.paytm.base.test.Group.Theme;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.base.test.UserManager;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchUnifiedOffers;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.fetchAllItemOffers;
import com.paytm.dto.NativeDTO.InitTxn.*;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.dto.emiSubvention.ApiV1Tenure.request.ApiV1TenureRequest;
import com.paytm.dto.emiSubvention.ApiV1Tenure.request.Filters;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.ApiV1ValidateRequest;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.OfferDetails;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.PaymentDetails;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.pages.*;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.emi.MerchantEMI;
import com.paytm.utils.merchant.helpers.GetMerchantHelper;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import com.paytm.utils.merchant.util.DbQueriesUtil;
import com.paytm.utils.merchant.util.PGPUtil;
import com.paytm.utils.merchant.util.PayMethodType;
import com.paytm.utils.merchant.util.exception.pgpException.PGPException;
import io.qameta.allure.*;
import io.qameta.allure.Owner;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;


import static com.paytm.appconstants.Constants.MerchantType.NATIVE_HYBRID;
import static com.paytm.appconstants.Constants.Owner.*;
import static com.paytm.apphelpers.NativeHelpers.submitProcessTxnResponseFromReq;
import static com.paytm.base.test.Group.Status;
import static com.paytm.dto.PaymentDTO.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasKey;

@Owner("Deepak")
public class EMI extends PGPBaseTest {
    private static final String BAJAJ_FINSERV_BANK_NAME = "Bajaj Finserv Ltd.";
    private static final String BAJAJ_FINSERV_GATEWAY_NAME = "BAJAJFN";
    private static final String EMI_PAY_MODE = "EMI";
    private static final int BAJAJ_FINSERV_EMI_DURATION = 6;
    private static final String SUCCESS_RESPONSE_CODE = "01";
    private static final String VALID_OTP = "123456";
    private static final String AMEX_DROPDOWN_BANK_NAME = "American Express";
    private static final String BAJAJ_FINSERV_DROPDOWN_BANK_NAME = "BAJAJ FINSERV EMI CARD";
    private static final String HDFC_DROPDOWN_BANK_NAME = "HDFC Bank";
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final CheckoutJsCheckoutPage checkoutjsPage = new CheckoutJsCheckoutPage();
    private final BajajFinservBankPage bajajFinservBankPage = new BajajFinservBankPage();

    public int countOfSubstring(String logs, String substring){
        int count = 0;
        int index = 0;

        while ((index = logs.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }

        return count;
    }




    @Step("Validate emi avail for mid: {0}")
    private MerchantEMI validateEmiAvail(String mid) {
        MerchantEMI merchantEMI = new GetMerchantHelper(mid).getMerchantEmiInfo();
        String resultCode = merchantEMI.getResultInfo().getResultCode();
        if (!resultCode.equalsIgnoreCase("SUCCESS")) {
            throw new SkipException("EMI not available on mid");
        }
        return merchantEMI;
    }

    public void validatev2ordercheckoutrequest (String MID,String theialogs, String OrderId, String amount, String month)
    {
        int amount1 = Integer.parseInt(amount) * 100;
        String txn_amount = new Integer(amount1).toString();
        String str = txn_amount + ".0";
        Assertions.assertThat(theialogs).contains("\"merchantInfo\":{\"mid\":\""+MID+"\",\"oldPgMid\":\""+MID+"\"");
        Assertions.assertThat(theialogs).contains("\"orderId\":\""+OrderId+"\"");
        Assertions.assertThat(theialogs).contains("\"orderType\":\"RETAIL_EMI_ORDER\"");
        Assertions.assertThat(theialogs).contains("\"productCode\":\"51051000100000000001\"");
        Assertions.assertThat(theialogs).contains("\"billAmount\":{\"currency\":\"INR\",\"value\":\""+txn_amount+"\"}");
        Assertions.assertThat(theialogs).contains("\"checkoutOrderAmount\":{\"currency\":\"INR\",\"value\":\""+txn_amount+"\"}");
        Assertions.assertThat(theialogs).contains("\"orderAmount\":{\"currency\":\"INR\",\"value\":\""+txn_amount+"\"}");
        Assertions.assertThat(theialogs).contains("\"isOfferCheckoutRequired\":false");
        Assertions.assertThat(theialogs).contains("\"paymentInfo\":{\"paymentOptions\":[{\"payAmount\":{\"currency\":\"INR\",\"value\":\""+txn_amount+"\"}");
        Assertions.assertThat(theialogs).contains("\"payMode\":\"EMI\"");
        Assertions.assertThat(theialogs).contains("\"paymentDetails\":{\"bin\":\"203040\"");
        Assertions.assertThat(theialogs).contains("\"interest\":0.0,\"emiType\":\"SUBVENTION\",\"emiSubType\":\"STANDARD\",\"subventionType\":\"ZERO_COST\"");
        Assertions.assertThat(theialogs).contains("\"effectiveAmount\":"+str+",\"loanAmount\":"+str+",\"payableAmount\":"+str+",\"totalPayableAmount\":"+str+",\"emi\":");
        Assertions.assertThat(theialogs).contains("\"merchantSolutionType\":\"ONLINE\"");
        Assertions.assertThat(theialogs).contains("\"emiPlanDetails\":{\"planId\":\"BAJAJFN&"+month+"\",\"emiId\":");
        Assertions.assertThat(theialogs).contains("\"tenure\":{\"value\":"+month+",\"unit\":\"MONTH\"},\"roi\":0.0");
    }

    public void validatev2ordercheckoutresponse (String MID,String theialogs, String OrderId, String amount)
    {
        int amount1 = Integer.parseInt(amount) * 100;
        String txn_amount = new Integer(amount1).toString();
        String str = txn_amount + ".0";
        Assertions.assertThat(theialogs).contains("\"status\":\"S\",\"message\":\"Success\"");
        Assertions.assertThat(theialogs).contains("\"merchantInfo\":{\"mid\":\""+MID+"\"");
        Assertions.assertThat(theialogs).contains("\"orderId\":\""+OrderId+"\"");
        Assertions.assertThat(theialogs).contains("\"orderType\":\"RETAIL_EMI_ORDER\"");
        Assertions.assertThat(theialogs).contains("\"productCode\":\"51051000100000000001\"");
        Assertions.assertThat(theialogs).contains("\"billAmount\":{\"value\":"+txn_amount+",\"currency\":\"INR\"}");
        Assertions.assertThat(theialogs).contains("\"checkoutOrderAmount\":{\"value\":"+txn_amount+",\"currency\":\"INR\"}");
        Assertions.assertThat(theialogs).contains("\"orderAmount\":{\"value\":"+txn_amount+",\"currency\":\"INR\"}");
        Assertions.assertThat(theialogs).contains("\"isOfferCheckoutRequired\":false");
        Assertions.assertThat(theialogs).contains("\"paymentInfo\":{\"paymentOptions\":[{\"payAmount\":{\"value\":"+txn_amount+",\"currency\":\"INR\"},\"payMode\":\"EMI\",\"paymentDetails\":{\"bin\":\"203040\"");
        Assertions.assertThat(theialogs).contains("\"checkoutExtendInfo\":{\"affordabilityInfo\":{\"affordabilityAcquirementId\":");
    }

    public void validateInstaOTPRequest (String instalogs,String amount ,String CARDNO)
    {
        Assertions.assertThat(instalogs).contains("ASSETID");
        Assertions.assertThat(instalogs).contains("PRODDESC");
        Assertions.assertThat(instalogs).contains("RequestID");
        Assertions.assertThat(instalogs).contains("\"TncACCEPT\":\"Y\"");
        Assertions.assertThat(instalogs).contains("DEALERID");
        Assertions.assertThat(instalogs).contains("VALIDATIONKEY");
        Assertions.assertThat(instalogs).contains("OTPNO");
        Assertions.assertThat(instalogs).contains("IPADDR");
        Assertions.assertThat(instalogs).contains("SCHEMEID");
        Assertions.assertThat(instalogs).contains("Tenure");
        Assertions.assertThat(instalogs).contains("PIN");
        Assertions.assertThat(instalogs).contains("\"CARDNUMBER\":\""+CARDNO+"\"");
        Assertions.assertThat(instalogs).contains("ORDERNO");
        Assertions.assertThat(instalogs).contains("REQUESTTEXT2");
        Assertions.assertThat(instalogs).contains("Manufacturer");
        Assertions.assertThat(instalogs).contains("LOANAMT\":\""+amount+"\"");
        Assertions.assertThat(instalogs).contains("NAMEONCARD");
        Assertions.assertThat(instalogs).contains("REQUESTTEXT1");
        Assertions.assertThat(instalogs).contains("Manufacturer");
        Assertions.assertThat(instalogs).contains("SALETYPE");
        Assertions.assertThat(instalogs).contains("REQUESTDATE2");
        Assertions.assertThat(instalogs).contains("REQUESTDATE1");
    }
    public void validateInstaOTPResponse (String instalogs)
    {
        Assertions.assertThat(instalogs).contains("\"RESPDESC\": \"Success\"");
        Assertions.assertThat(instalogs).contains("\"RSPCODE\": \"0\"");
        Assertions.assertThat(instalogs).contains("\"MobileNo\": \"6618\"");
        Assertions.assertThat(instalogs).contains("Key");
        Assertions.assertThat(instalogs).contains("RequestID");
    }

    public void validateInstaAUTHRequest (String instalogs,String amount ,String CARDNO)
    {
        Assertions.assertThat(instalogs).contains("ASSETID");
        Assertions.assertThat(instalogs).contains("PRODDESC");
        Assertions.assertThat(instalogs).contains("REQUESTID");
        Assertions.assertThat(instalogs).contains("\"TncACCEPT\":\"Y\"");
        Assertions.assertThat(instalogs).contains("DEALERID");
        Assertions.assertThat(instalogs).contains("VALIDATIONKEY");
        Assertions.assertThat(instalogs).contains("\"OTPNO\":\"111111\"");
        Assertions.assertThat(instalogs).contains("IPADDR");
        Assertions.assertThat(instalogs).contains("SCHEMEID");
        Assertions.assertThat(instalogs).contains("Tenure");
        Assertions.assertThat(instalogs).contains("PIN");
        Assertions.assertThat(instalogs).contains("\"CARDNUMBER\":\""+CARDNO+"\"");
        Assertions.assertThat(instalogs).contains("ORDERNO");
        Assertions.assertThat(instalogs).contains("REQUESTTEXT2");
        Assertions.assertThat(instalogs).contains("LOANAMT\":\""+amount+"\"");
        Assertions.assertThat(instalogs).contains("NAMEONCARD");
        Assertions.assertThat(instalogs).contains("REQUESTTEXT1");
        Assertions.assertThat(instalogs).contains("MANFID");
        Assertions.assertThat(instalogs).contains("SALETYPE");
        Assertions.assertThat(instalogs).contains("REQUESTDATE2");
        Assertions.assertThat(instalogs).contains("REQUESTDATE1");
    }
    public void validateInstaAUTHResponse (String instalogs,String amount)
    {
        Assertions.assertThat(instalogs).contains("\"RESPDESC\": \"Success\"");
        Assertions.assertThat(instalogs).contains("\"RSPCODE\": \"0\"");
        Assertions.assertThat(instalogs).contains("ORDERNO");
        Assertions.assertThat(instalogs).contains("LOANAMT\": \""+amount+"\"");
        Assertions.assertThat(instalogs).contains("Tenure");
        Assertions.assertThat(instalogs).contains("Key");
        Assertions.assertThat(instalogs).contains("DEALID");
        Assertions.assertThat(instalogs).contains("RequestID");
    }

    public void validateInstaSTATUSQUERYRequest (String instalogs)
    {
        Assertions.assertThat(instalogs).contains("REQUERYID");
        Assertions.assertThat(instalogs).contains("VALKEY");
        Assertions.assertThat(instalogs).contains("ACQCHNLID");
        Assertions.assertThat(instalogs).contains("DEALERID");
        Assertions.assertThat(instalogs).contains("REQID");
    }
    public void validateInstaSTATUSQUERYResponse (String instalogs)
    {
        Assertions.assertThat(instalogs).contains("\"RESPONSECODE\": \"0\"");
        Assertions.assertThat(instalogs).contains("\"ERRORDESCRIPTION\": \"Success\"");
        Assertions.assertThat(instalogs).contains("ORDERNO");
        Assertions.assertThat(instalogs).contains("REQUESTID");
        Assertions.assertThat(instalogs).contains("\"RQTYPE\": \"AUTH\"");
        Assertions.assertThat(instalogs).contains("REQUERYID");
        Assertions.assertThat(instalogs).contains("REQID");
        Assertions.assertThat(instalogs).contains("VALKEY");
        Assertions.assertThat(instalogs).contains("\"ERRDESC\": \"SUCCESS\"");
        Assertions.assertThat(instalogs).contains("\"RESCODE\": \"00\"");
        Assertions.assertThat(instalogs).contains("Key");
        Assertions.assertThat(instalogs).contains("DEALID");

    }

    @Parameters({"theme"})
    @Test(description = "fail transaction with EMI option availble under saved card from PGOnly merchant")
    public void validateFailTxnSavedEmi_Pgonly_LoggedIn(@Optional("enhancedwap") String theme) throws Exception {
        prerequisite:
        {
            validateEmiAvail(MerchantType.EMI.getId());
        }
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.EMI, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("99.98")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.EMI_SAVED_CARD, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode(ResponseCode.BANK_TXN_FAILURE.getRespCode()).assertAll();
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateFailureResponse(ResponseCode.BANK_TXN_FAILURE.getRespCode(), ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate Saved EMI txn from saved cards for PGOnly merchant and user is logged in")
    public void validateSavedEmiTxn_PgOnly_LoggedIn(@Optional("enhancedwap") String theme) throws Exception {
        prerequisite:
        {
            validateEmiAvail(MerchantType.PGONLY_EMI_MIN_MAX.getId());
        }
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGONLY_EMI_MIN_MAX, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("15")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.EMI_SAVED_CARD, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode(SUCCESS_RESPONSE_CODE).assertAll();
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateSuccessResponse()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate failed EMI txn for PGOnly merchant and user is logged in")
    public void validateFailedEmiTxn_PgOnly_LoggedIn(@Optional("enhancedWap") String theme) throws Exception {
        prerequisite:
        {
            validateEmiAvail(MerchantType.PGONLY_EMI_MIN_MAX.getId());
        }
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO()
                .setBankName(HDFC_DROPDOWN_BANK_NAME);
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGONLY_EMI_MIN_MAX, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("99.98")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.EMI, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("227").assertAll();
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateFailureResponse(ResponseCode.BANK_TXN_FAILURE.getRespCode(), ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate failed EMI txn for PGOnly merchant and user is not logged in")
    public void validateFailedEmiTxn_PgOnly_nonLoggedIn(@Optional("enhancedWap") String theme) {
        prerequisite:
        {
            validateEmiAvail(MerchantType.PGONLY_EMI_MIN_MAX.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGONLY_EMI_MIN_MAX, theme)
                .setTXN_AMOUNT("99.98")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO()
                .setBankName(HDFC_DROPDOWN_BANK_NAME);
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(PayMode.EMI, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("227").assertAll();
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateFailureResponse(ResponseCode.BANK_TXN_FAILURE.getRespCode(), ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate EMI option not displayed when TXN amount is More than EMI MAX amount for non logged in user")
    public void validateEmiNotDisplayed_txnAmountMorethan_MaxAmount_nonLoggedIn(@Optional("enhancedwap_revamp") String theme) throws Exception {
        prerequisite:
        {
            validateEmiAvail(MerchantType.EMI_LIMIT.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.EMI_LIMIT, theme)
                .setTXN_AMOUNT("250")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabEMI().assertNotVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Validate EMI option not displayed when TXN amount is more than EMI MAX amount")
    public void validateEmiNotDisplayed_txnAmountMorethan_maxAmount(@Optional("enhancedwap_revamp") String theme) throws Exception {
        prerequisite:
        {
            validateEmiAvail(MerchantType.EMI_LIMIT.getId());
        }
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.EMI_LIMIT, theme)
                .setTXN_AMOUNT("250")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabEMI().assertNotVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Validate EMI option not displayed when TXN amount is less than EMI MIN amount for non logged in user")
    public void validateEmiNotDisplayed_txnAmountLessthan_minAmount_nonLoggedIn(@Optional("enhancedwap_revamp") String theme) throws Exception {
        prerequisite:
        {
            validateEmiAvail(MerchantType.EMI_LIMIT.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.EMI_LIMIT, theme)
                .setTXN_AMOUNT("50")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabEMI().assertNotVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Validate EMI option not displayed when TXN amount is less than EMI MIN amount")
    public void validateEmiNotDisplayed_txnAmountLessthan_minAmount(@Optional("enhancedwap_revamp") String theme) throws Exception {
        prerequisite:
        {
            validateEmiAvail(MerchantType.EMI_LIMIT.getId());
        }
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.EMI_LIMIT, theme)
                .setTXN_AMOUNT("50")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabEMI().assertNotVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful Onus EMI transaction from PGOnly merchant and user is logged in")
    public void validateSuccessOnusEMITxn_PgOnlyMerc_loggedInUser(@Optional("enhancedwap_revamp") String theme) throws Exception {
        prerequisite:
        {
            validateEmiAvail(MerchantType.EMI.getId());
        }
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.EMI, theme)
                .setCUST_ID(CommonHelpers.generateOrderId())
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO()
                .setBankName(HDFC_DROPDOWN_BANK_NAME);
        paymentDTO.setMonth(3);
        //     cashierPage.checkBoxPPI().unCheck();
        cashierPage.payBy(PayMode.EMI, paymentDTO);
        new ResponsePage().waitUntilLoads();
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode(SUCCESS_RESPONSE_CODE)
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }


    @Parameters({"theme"})
    @Test(description = "Validate successful Onus EMI transaction from PGOnly merchant")
    public void validateSuccessOnusEMITxn_PgOnlyMerc(@Optional("enhancedwap_revamp") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.EMI, theme)
                .setTXN_AMOUNT("1")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER).setMonth(3);
        cashierPage.payBy(PayMode.EMI,paymentDTO);
        new ResponsePage().waitUntilLoads();
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode(SUCCESS_RESPONSE_CODE)
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(groups = {Theme.MERCHANT4, Theme.ENHANCEDWAP, Theme.ENHANCEDWEB, "smoke"},
            description = "Validate successful Onus EMI transaction.")
    public void PGP_178_successfulOnusEMITxn(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.EMI, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO()
                .setBankName(HDFC_DROPDOWN_BANK_NAME)
                .setMonth(3);
        cashierPage.payBy(PayMode.EMI, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Bank.HDFC.toString())
                .validateCheckSum(orderDTO.getMerchantKey())
                .validateResponsePageParameters()
                .assertAll();

        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode(SUCCESS_RESPONSE_CODE)
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "PG Only Anonymous | Validate the EMI for all the plans against a bank")
    public void PGP_179_validateBankAllPlans(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.EMI, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.tabEMI().click();
        cashierPage.dropdownEmiBanks().selectByVisibleText(HDFC_DROPDOWN_BANK_NAME);
        cashierPage.verifyEMIOptions(paymentDTO.getBankName(), Double.valueOf(orderDTO.getTXN_AMOUNT()));
    }
    //    @Parameters({"theme"})
//    @Test(description = "Validate successful Offus EMI transaction.",enabled = false)
    //For invalid card bin,fetchBinDetail api will send a flag isEmiAvailable is false and UI show error there only
    public void SuccessfulOffusEMITxn(@Optional("enhancedweb_revamp") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.EMI, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //4207399104106768
        PaymentDTO paymentDTO = new PaymentDTO()
                .setBankName(HDFC_DROPDOWN_BANK_NAME)
                .setMonth(1)
                .setCreditCardNumber(VISA_CC_BILL_PAYMENT)
                .setEmiCard(VISA_CC_BILL_PAYMENT);
        cashierPage.payBy(PayMode.EMI, paymentDTO);
        assertion.apply(cashierPage.emiAnotherCard().content().equals("Please enter a valid card number to use this EMI plan"));
    }

    @Parameters({"theme"})
    @Test(groups = {Theme.MERCHANT4, Theme.ENHANCEDWAP, Theme.ENHANCEDWEB},
            description = "perform a EMI transaction with BAJAJ FN card, without entering expiry date and CVV")
    public void SuccessfulEBajajFinMITxn(@Optional("enhancedweb_revamp") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.Hybrid, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //4207399104106768
        PaymentDTO paymentDTO = new PaymentDTO()
                .setCreditCardNumber(BAJAJ_FINSERV_CREDIT_CARD_NUMBER)
                .setEmiCard(BAJAJ_FINSERV_CREDIT_CARD_NUMBER)
                .setBankName(BAJAJ_FINSERV_DROPDOWN_BANK_NAME)
                .setExpMonth(null)
                .setExpYear(null)
                .setCvvNumber(null)
                .setMonth(6);
        cashierPage.payBy(PayMode.EMI, paymentDTO);
        bajajFinservBankPage.waitUntilLoads();
        bajajFinservBankPage.inputOtp(VALID_OTP);
        bajajFinservBankPage.clickSubmit();
        new ResponsePage().waitUntilLoads();
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(BAJAJ_FINSERV_GATEWAY_NAME)
                .validateRespCode(SUCCESS_RESPONSE_CODE)
                .validateRespMsg("Txn Successful.")
                .validateBankName(BAJAJ_FINSERV_BANK_NAME)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(groups = {Theme.ENHANCEDWAP, Theme.ENHANCEDWEB},
            description = "perform a EMI transaction with BAJAJ FN card, entering expiry date")
    public void verifyEMITxnIsSuccessfulWithBajajFinservCardWhenExpiryDateIsEntered(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.EMI, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO()
                .setCreditCardNumber(BAJAJ_FINSERV_CREDIT_CARD_NUMBER)
                .setEmiCard(BAJAJ_FINSERV_CREDIT_CARD_NUMBER)
                .setBankName(BAJAJ_FINSERV_DROPDOWN_BANK_NAME)
                .setCvvNumber(null);
        cashierPage.payBy(PayMode.EMI, paymentDTO);
        bajajFinservBankPage.inputOtp(VALID_OTP);
        bajajFinservBankPage.clickSubmit();
        new ResponsePage().waitUntilLoads();
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(BAJAJ_FINSERV_GATEWAY_NAME)
                .validateRespCode(SUCCESS_RESPONSE_CODE)
                .validateRespMsg("Txn Successful.")
                .validateBankName(BAJAJ_FINSERV_DROPDOWN_BANK_NAME)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(EMI_PAY_MODE)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(groups = {Theme.ENHANCEDWAP, Theme.ENHANCEDWEB},
            description = "check for UI message, 'Expiry date and CVV not required for BAJAJ FN card' (only enhanced and default)")
    public void verifyCorrectInfoMsgIsShownForEMIPaymodeWhenBajajFinservIsSelected(@Optional("enhancedweb_revamp") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.EMI, theme)
                .setTXN_AMOUNT("10")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.scrollToElement(cashierPage.tabEMI());
        cashierPage.tabEMI().click();
        cashierPage.dropdownEmiBanks().selectByVisibleText(BAJAJ_FINSERV_DROPDOWN_BANK_NAME);
        if ("enhancedwap".equalsIgnoreCase(theme)) {
            cashierPage.selectEMIPlan().click();
        }
        if(theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WEB) || theme.equalsIgnoreCase(Constants.Theme.ENHANCEDWAP))
        {
            cashierPage.emiDurationOption(BAJAJ_FINSERV_EMI_DURATION).click();}
        cashierPage.textBoxCardNumber().clearAndType(BAJAJ_FINSERV_CREDIT_CARD_NUMBER);
        cashierPage.labelPaymodeInfoMsg().assertText("Expiry and CVV not required if not mentioned on your card");
    }

    @Parameters({"theme"})
    @Test(groups = {Theme.MERCHANT4, Theme.ENHANCEDWAP, Theme.ENHANCEDWEB},
            description = "Perform a EMI only transaction with ADD n Pay merchant")
    public void verifyEMITxnIsSuccessfulWithBajajFinservCardForAddNPayMerchant(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 0.0);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.EMI, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO()
                .setCreditCardNumber(BAJAJ_FINSERV_CREDIT_CARD_NUMBER)
                .setEmiCard(BAJAJ_FINSERV_CREDIT_CARD_NUMBER)
                .setBankName(BAJAJ_FINSERV_DROPDOWN_BANK_NAME)
                .setCvvNumber(null);
        cashierPage.payBy(PayMode.EMI, paymentDTO);
        bajajFinservBankPage.inputOtp(VALID_OTP);
        bajajFinservBankPage.clickSubmit();
        new ResponsePage().waitUntilLoads();
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(BAJAJ_FINSERV_GATEWAY_NAME)
                .validateRespCode(SUCCESS_RESPONSE_CODE)
                .validateRespMsg("Txn Successful.")
                .validateBankName(BAJAJ_FINSERV_DROPDOWN_BANK_NAME)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(EMI_PAY_MODE)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Issue("PGP-14025")
    @Parameters({"theme"})
    @Test(groups = {Theme.ENHANCEDWAP, Theme.ENHANCEDWEB, Status.BUG},
            description = "perform a EMI transaction with a saved BAJAJ FN card, without entering CVV")
    public void verifyEMITxnIsSuccessfulWithSavedBajajFinservCardWhenCVV_IsNotEntered(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO()
                .setCreditCardNumber(BAJAJ_FINSERV_CREDIT_CARD_NUMBER)
                .setBankName(BAJAJ_FINSERV_DROPDOWN_BANK_NAME)
                .setCvvNumber(null)
                .setMonth(6);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.Hybrid, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.EMI_SAVED_CARD, paymentDTO);
        bajajFinservBankPage.inputOtp(VALID_OTP);
        bajajFinservBankPage.clickSubmit();
        new ResponsePage().waitUntilLoads();
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(BAJAJ_FINSERV_GATEWAY_NAME)
                .validateRespCode(SUCCESS_RESPONSE_CODE)
                .validateRespMsg("Txn Successful.")
                .validateBankName(BAJAJ_FINSERV_DROPDOWN_BANK_NAME)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(EMI_PAY_MODE)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }
    //    @Issue("PGP-14025")
//    @Parameters({"theme"})
//    @Test(groups = {Theme.ENHANCEDWAP, Theme.ENHANCEDWEB},
//            description = "perform a EMI transaction with a saved BAJAJ FN card, entering cvv", enabled = false)
//disabling it as now cvv option is not shown for bajaj finserv as it doesn't support a cvv
    public void verifyEMITxnIsSuccessfulWithSavedBajajFinservCardWhenCVVIsEntered(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO()
                .setCreditCardNumber(BAJAJ_FINSERV_CREDIT_CARD_NUMBER)
                .setBankName(BAJAJ_FINSERV_DROPDOWN_BANK_NAME)
                .setMonth(6);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.Hybrid, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.EMI_SAVED_CARD, paymentDTO);
        bajajFinservBankPage.inputOtp(VALID_OTP);
        bajajFinservBankPage.clickSubmit();
        new ResponsePage().waitUntilLoads();
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(BAJAJ_FINSERV_GATEWAY_NAME)
                .validateRespCode(SUCCESS_RESPONSE_CODE)
                .validateRespMsg("Txn Successful.")
                .validateBankName(BAJAJ_FINSERV_BANK_NAME)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(EMI_PAY_MODE)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(groups = {Theme.MERCHANT4, Theme.ENHANCEDWAP, Theme.ENHANCEDWEB},
            description = "Verify EMI txn gets failed when cancelled on BajajFinserv authentication page")
    public void verifyEMITxnGetsFailedWhenCancelledOnBajajFinservAuthenticationPage(@Optional("enhancedweb_revamp") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.Hybrid, theme)
                .setTXN_AMOUNT("10")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO()
                .setCreditCardNumber(BAJAJ_FINSERV_CREDIT_CARD_NUMBER)
                .setEmiCard(BAJAJ_FINSERV_CREDIT_CARD_NUMBER)
                .setBankName(BAJAJ_FINSERV_DROPDOWN_BANK_NAME)
                .setCvvNumber(null)
                .setMonth(6);
        cashierPage.payBy(PayMode.EMI, paymentDTO);
        bajajFinservBankPage.buttonCancel().click();
        bajajFinservBankPage.modalCancelPayment().accept();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode(ResponseCode.FGW_USER_CANCEL_PAYTM_PAGE.getRespCode()).assertAll();
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateFailureResponse(ResponseCode.FGW_USER_CANCEL_PAYTM_PAGE.getRespCode(), ResponseCode.FGW_USER_CANCEL_PAYTM_PAGE.getRespMsg())
                .AssertAll();
    }

    @Issue("PGP-14825")
    @Parameters({"theme"})
    @Test(groups = {Theme.MERCHANT4, Theme.ENHANCEDWAP, Theme.ENHANCEDWEB},
            description = "Perform a EMI transaction with BAJAJ FN card , redirect to bank page, enter incorrect OTP , should be redirected back to theia response page")
    public void verifyRedirectionToBankPageWhenIncorrectOTP_IsEnteredForEMI_TxnWIthBajajFinservCard(@Optional("enhancedweb_revamp") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.Hybrid, theme)
                .setTXN_AMOUNT("10")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO()
                .setCreditCardNumber(BAJAJ_FINSERV_CREDIT_CARD_NUMBER)
                .setEmiCard(BAJAJ_FINSERV_CREDIT_CARD_NUMBER)
                .setBankName(BAJAJ_FINSERV_DROPDOWN_BANK_NAME)
                .setCvvNumber(null)
                .setMonth(6);
        cashierPage.payBy(PayMode.EMI, paymentDTO);
        bajajFinservBankPage.inputOtp("000035");
        bajajFinservBankPage.clickSubmit();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode(ResponseCode.FGW_OTP_VALIDATION_FAILED.getRespCode()).assertAll();
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateFailureResponse(ResponseCode.FGW_OTP_VALIDATION_FAILED.getRespCode(), ResponseCode.FGW_OTP_VALIDATION_FAILED.getRespMsg())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(groups = {Theme.MERCHANT4, Theme.ENHANCEDWAP, Theme.ENHANCEDWEB},
            description = "Check user redirected to bank when payment initiated using AMEX card")
    public void checkUserRedirectedToBankWhenPaymentInitiatedUsingAMEXCard(@Optional("enhancedweb_revamp") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.EMI, theme)
                .setTXN_AMOUNT("10")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO()
                .setCreditCardNumber(PaymentDTO.AMEX_CARD_NUMBER)
                .setEmiCard(PaymentDTO.AMEX_CARD_NUMBER)
                .setBankName(AMEX_DROPDOWN_BANK_NAME)
                .setCvvNumber("1111")
                .setMonth(3);
        cashierPage.payBy(PayMode.EMI, paymentDTO);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("AMEX")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(AMEX_DROPDOWN_BANK_NAME)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(groups = {Theme.MERCHANT4, Theme.ENHANCEDWAP, Theme.ENHANCEDWEB},
            description = "Check zero cost emi txn")
//TODO ask charu why interest in applied on zero cost emi
    public void checkZeroCostEmiTxn(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.EMI, theme)
                .setEMI_OPTIONS("0CostEMI:8565560_" + LocalConfig.ZERO_COST_EMI)
                .setTXN_AMOUNT("250")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO emiDetails = new PaymentDTO();
        cashierPage.tabEMI().click();
        cashierPage.textBoxCardNumber().clearAndType(emiDetails.getCreditCardNumber());
        cashierPage.fillExpiryMonth(emiDetails.getExpMonth());
        cashierPage.fillExpiryYear(emiDetails.getExpYear().substring(2));
        cashierPage.textBoxCVVNumber().clearAndType(emiDetails.getCvvNumber());
        cashierPage.buttonPGPayNow().click();
        new ResponsePage().waitUntilLoads();
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode(SUCCESS_RESPONSE_CODE)
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful EMI_DC transaction")
    public void validateSuccessEMI_DC(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.RETRYPAYMODE);
        PaymentDTO Emi_Dc = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.EMI, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("12")
                .build();
        Emi_Dc.setEmiCard(ICICI_DEBIT_CARD_NUMBER).setBankName("ICICI Bank Debit Card").setMonth(3);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI, Emi_Dc);
        new ResponsePage().waitUntilLoads();
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.ICIE.toString())
                .validateRespCode(SUCCESS_RESPONSE_CODE)
                .validateRespMsg("Txn Successful.")
                .validateBankName("ICICI Bank")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-30168")
    @Test(description = "Initiate request via initiate txn api with a mobile number on which EMI_DC is configured and verify response-Standard EMI and perform successful transacion")
    public void verifyEmiDcVisibleInFPOAndPerformSuccessfulTransaction(){
        String mobNoWithEmiDcConfigured = "5616539291";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.EMI_DC_MERCHANT)
                .setMobile(mobNoWithEmiDcConfigured)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String txnToken = iniJsonPath.getString("body.txnToken");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find{it.displayName  == 'EMI'}.payChannelOptions.emiType")).as("EMI_DC not found").contains("DEBIT_CARD");
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" + ICICI_DEBIT_CARD_NUMBER + "|" + paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.EMI_DC_MERCHANT, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setCardInfo(cardInfo)
                .setEMI_TYPE("DEBIT_CARD")
                .setPlanId("ICICI|3")
                .setChannelCode("ICICI")
                .setPaymentFlow("NONE")
                .build();
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validatePaymentMode("EMI_DC")
                .validateGatewayName("ICIE")
                .validateBankName("ICICI Bank")
                .assertAll();
    }

    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-30168")
    @Test(description = "Initiate request via initiate txn api with a mobile number on which EMI_DC is not configured and verify EMI_DC should not be available as a paymode in response of FPO")
    public void verifyEmiDcNotVisibleInFPOformobNoWithEmiDcNotConfigured(){
        String mobNoWithEmiDcNotConfigured = "5988976543";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.EMI_DC_MERCHANT)
                .setMobile(mobNoWithEmiDcNotConfigured)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String txnToken = iniJsonPath.getString("body.txnToken");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find{it.displayName  == 'EMI'}.payChannelOptions.emiType")).as("EMI_DC should not be visible").doesNotContain("DEBIT_CARD");
    }

    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-30168")
    @Test(description = "Initiate request via initiate txn api with no mobile number and verify EMI_DC not visible in FPO response")
    public void verifyEmiDcNotVisibleInFPOWithoutMobNoInInitiateApi(){
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.EMI_DC_MERCHANT)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String txnToken = iniJsonPath.getString("body.txnToken");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find{it.displayName  == 'EMI'}.payChannelOptions.emiType")).as("EMI_DC should not be visible").doesNotContain("DEBIT_CARD");

    }

    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-30168")
    @Test(description = "Initiate request via create token api with a mobile number on which EMI_DC is configured and verify response-Standard EMI and perform successful transacion")
    public void verifyEmiDcVisibleInFPOPerformSuccessfulTransactionWithAccessToken(){
        String mobNoWithEmiDcConfigured = "9818686101";
        String refId = UUID.randomUUID().toString().substring(0, 18);
        CreateToken createToken = new CreateToken(MerchantType.EMI_DC_MERCHANT, "", refId);
        UserInfo userInfo = new UserInfo();
        userInfo.setMobile(mobNoWithEmiDcConfigured);
        userInfo.setEmail("");
        userInfo.setFirstName("");
        userInfo.setLastName("");
        userInfo.setCustId("");
        createToken.setContext("body.userInfo",userInfo);
        String body = "{" + "\"mid\":\"{MID}\"," + "\"referenceId\":\"{REFID}\"," + "\"paytmSsoToken\":\"{SSOTOKEN}\",\"userInfo\":{\"firstName\":\"\",\"lastName\":\"\",\"custId\":\"\",\"mobile\":\"{mobno}\",\"email\":\"\"}}";
        body = body.replace("{MID}", MerchantType.EMI_DC_MERCHANT.getId())
                .replace("{REFID}", refId)
                .replace("{SSOTOKEN}","")
                .replace("{mobno}",mobNoWithEmiDcConfigured);
        String Checksum = PGPUtil.getChecksum(MerchantType.EMI_DC_MERCHANT.getKey(), body);
        createToken.setContext("head.token",Checksum);
        createToken.setContext("body.referenceId",refId);
        JsonPath jsonPath = createToken.execute().jsonPath();
        String accessToken = jsonPath.getString("body.accessToken");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("ACCESS", accessToken)
                .setMid(MerchantType.EMI_DC_MERCHANT.getId())
                .setGenerateOrderId("true")
                .build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(MerchantType.EMI_DC_MERCHANT.getId(), fetchPaymentOptionsDTO);
        fetchPaymentOption.getRequestSpecBuilder().addQueryParam("referenceId", refId);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find{it.displayName  == 'EMI'}.payChannelOptions.emiType")).as("EMI_DC not found").contains("DEBIT_CARD");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.EMI_DC_MERCHANT)
                .setMobile(mobNoWithEmiDcConfigured)
                .setOrderId(fetchPaymentOptionsJson.getString("body.orderId"))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String txnToken = iniJsonPath.getString("body.txnToken");
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" + ICICI_DEBIT_CARD_NUMBER_EMI + "|" + paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.EMI_DC_MERCHANT,fetchPaymentOptionsJson.getString("body.orderId") , txnToken, PayMethodType.EMI)
                .setCardInfo(cardInfo)
                .setEMI_TYPE("DEBIT_CARD")
                .setPlanId("ICICI|3")
                .setChannelCode("ICICI")
                .setPaymentFlow("NONE")
                .build();
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validatePaymentMode("EMI_DC")
                .validateGatewayName("ICIE")
                .validateBankName("ICICI Bank")
                .assertAll();

    }

    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-30168")
    @Test(description = "Initiate request via create token api with a mobile number on which EMI_DC is not configured and verify EMI_DC should not be available as a paymode in response of FPO ")
    public void verifyEmiDcNotVisibleInFPOWithAccessTokenformobNoWithEmiDcNotConfigured() {
        String mobNoWithEmiDcNotConfigured = "5988976543";
        String refId = UUID.randomUUID().toString().substring(0, 18);
        CreateToken createToken = new CreateToken(MerchantType.EMI_DC_MERCHANT, "", refId);
        UserInfo userInfo = new UserInfo();
        userInfo.setMobile(mobNoWithEmiDcNotConfigured);
        userInfo.setEmail("");
        userInfo.setFirstName("");
        userInfo.setLastName("");
        userInfo.setCustId("");
        createToken.setContext("body.userInfo", userInfo);
        String body = "{" + "\"mid\":\"{MID}\"," + "\"referenceId\":\"{REFID}\"," + "\"paytmSsoToken\":\"{SSOTOKEN}\",\"userInfo\":{\"firstName\":\"\",\"lastName\":\"\",\"custId\":\"\",\"mobile\":\"{mobno}\",\"email\":\"\"}}";
        body = body.replace("{MID}", MerchantType.EMI_DC_MERCHANT.getId())
                .replace("{REFID}", refId)
                .replace("{SSOTOKEN}", "")
                .replace("{mobno}", mobNoWithEmiDcNotConfigured);
        String Checksum = PGPUtil.getChecksum(MerchantType.EMI_DC_MERCHANT.getKey(), body);
        createToken.setContext("head.token", Checksum);
        createToken.setContext("body.referenceId", refId);
        JsonPath jsonPath = createToken.execute().jsonPath();
        String accessToken = jsonPath.getString("body.accessToken");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("ACCESS", accessToken)
                .setMid(MerchantType.EMI_DC_MERCHANT.getId())
                .setGenerateOrderId("true")
                .build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(MerchantType.EMI_DC_MERCHANT.getId(), fetchPaymentOptionsDTO);
        fetchPaymentOption.getRequestSpecBuilder().addQueryParam("referenceId", refId);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.body.merchantPayOption.paymentModes.find{it.displayName  == 'EMI'}.payChannelOptions.emiType")).as("EMI_DC should not be visible").isNotEqualTo("DEBIT_CARD");
    }

    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-30168")
    @Test(description = "Initiate request via create token api with no mobile number and verify EMI_DC should not be visivle in FPO response ")
    public void verifyEmiDcNotVisibleInFPOWithAccessTokenWithoutMobNoInCreateTokenApi() {
        String refId = UUID.randomUUID().toString().substring(0, 18);
        CreateToken createToken = new CreateToken(MerchantType.EMI_DC_MERCHANT, "", refId);
        UserInfo userInfo = new UserInfo();
        userInfo.setMobile("");
        userInfo.setEmail("");
        userInfo.setFirstName("");
        userInfo.setLastName("");
        userInfo.setCustId("");
        createToken.setContext("body.userInfo", userInfo);
        String body = "{" + "\"mid\":\"{MID}\"," + "\"referenceId\":\"{REFID}\"," + "\"paytmSsoToken\":\"{SSOTOKEN}\",\"userInfo\":{\"firstName\":\"\",\"lastName\":\"\",\"custId\":\"\",\"mobile\":\"{mobno}\",\"email\":\"\"}}";
        body = body.replace("{MID}", MerchantType.EMI_DC_MERCHANT.getId())
                .replace("{REFID}", refId)
                .replace("{SSOTOKEN}", "")
                .replace("{mobno}", "");
        String Checksum = PGPUtil.getChecksum(MerchantType.EMI_DC_MERCHANT.getKey(), body);
        createToken.setContext("head.token", Checksum);
        createToken.setContext("body.referenceId", refId);
        JsonPath jsonPath = createToken.execute().jsonPath();
        String accessToken = jsonPath.getString("body.accessToken");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("ACCESS", accessToken)
                .setMid(MerchantType.EMI_DC_MERCHANT.getId())
                .setGenerateOrderId("true")
                .build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(MerchantType.EMI_DC_MERCHANT.getId(), fetchPaymentOptionsDTO);
        fetchPaymentOption.getRequestSpecBuilder().addQueryParam("referenceId", refId);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find{it.displayName  == 'EMI'}.payChannelOptions.emiType")).as("EMI_DC should not be visible").doesNotContain("DEBIT_CARD");
    }

    @Feature("PGP-45709")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA : PGP-46645")
    @Test(description = "Validate mapping-service/get/fetchValidBins/planId is returning bins corresponding to the EMI PlanId(HDFC BankId)")
    public void ValidateFetchValidBinsWithEMIPlanIdAndIsOfusNull() throws Exception{
        //String EMIPLANID="8565560";    Please Note:- EMIPlanId is same as BANK_ID
        String bankCode = Bank.HDFC.toString();

        String DBQuery = "SELECT * from PAYTMPGDB.BANK_MASTER bm where BANK_CODE= '"+bankCode+"';";
        String bankId = DbQueriesUtil.selectFromPaytmPGDB(DBQuery, "BANK_ID");

        FetchValidBinsAPI fetchValidBinsAPI = new FetchValidBinsAPI();
        fetchValidBinsAPI.FetchValidBinsAPIHelper(bankId);
        JsonPath jsonPath = fetchValidBinsAPI.execute().jsonPath();

        Assertions.assertThat(jsonPath.getString("planID")).isEqualTo(bankId);
        Assertions.assertThat(jsonPath.getString("isOfus")).isEqualTo(null);
    }

    @Feature("PGP-45709")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA : PGP-46645")
    @Test(description = "Validate mapping-service/get/fetchValidBins/planId/{0} is returning bins corresponding to the EMI PlanId[FallBack Logic]")
    public void ValidateFetchValidBinsWithEMIPlanIdAndIsOfusZero() throws Exception{
        String bankCode = Bank.HDFC.toString();

        String DBQuery = "SELECT * from PAYTMPGDB.BANK_MASTER bm where BANK_CODE= '"+bankCode+"';";
        String bankId = DbQueriesUtil.selectFromPaytmPGDB(DBQuery, "BANK_ID");

        String ISOFUS = "0";
        FetchValidBinsAPI fetchValidBinsAPI = new FetchValidBinsAPI();
        fetchValidBinsAPI.FetchValidBinAPIWithIsOfus(bankId,ISOFUS);
        JsonPath jsonPath = fetchValidBinsAPI.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("planID")).isEqualTo(bankId);
        Assertions.assertThat(jsonPath.getString("isOfus")).isEqualTo(ISOFUS);
    }

    @Feature("PGP-45709")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA : PGP-46645")
    @Test(description = "Validate mapping-service/get/fetchValidBins/planId/{1} is returning bins corresponding to the EMI PlanId[FallBack Logic]")
    public void ValidateFetchValidBinsWithEMIPlanIdAndIsOfusOne() throws Exception{
        String bankCode = Bank.HDFC.toString();

        String DBQuery = "SELECT * from PAYTMPGDB.BANK_MASTER bm where BANK_CODE= '"+bankCode+"';";
        String bankId = DbQueriesUtil.selectFromPaytmPGDB(DBQuery, "BANK_ID");

        String ISOFUS = "1";
        FetchValidBinsAPI fetchValidBinsAPI = new FetchValidBinsAPI();
        fetchValidBinsAPI.FetchValidBinAPIWithIsOfus(bankId,ISOFUS);
        JsonPath jsonPath = fetchValidBinsAPI.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("planID")).isEqualTo(bankId);
        Assertions.assertThat(jsonPath.getString("isOfus")).isEqualTo(ISOFUS);
    }

    @Feature("PGP-45709")
    @Owner(Constants.Owner.ROUNAK)
    @Description("Automation JIRA : PGP-46645")
    @Test(description = "Validate mapping-service/get/fetchValidBins/planId/{2} is returning bins corresponding to the EMI PlanId[FallBack Logic]")
    public void ValidateFetchValidBinsWithEMIPlanIdAndIsOfusTwo() throws Exception{
        String bankCode = Bank.HDFC.toString();

        String DBQuery = "SELECT * from PAYTMPGDB.BANK_MASTER bm where BANK_CODE= '"+bankCode+"';";
        String bankId = DbQueriesUtil.selectFromPaytmPGDB(DBQuery, "BANK_ID");

        String ISOFUS = "2";
        FetchValidBinsAPI fetchValidBinsAPI = new FetchValidBinsAPI();
        fetchValidBinsAPI.FetchValidBinAPIWithIsOfus(bankId,ISOFUS);
        JsonPath jsonPath = fetchValidBinsAPI.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("planID")).isEqualTo(bankId);
        Assertions.assertThat(jsonPath.getString("isOfus")).isEqualTo(ISOFUS);
    }


    @Feature("PGP-42854")
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Validate Loan amount in Create order and Pay in theia facade")
    public void validateLoanAmountInCreateOrderAndPay(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String txnAmount="200";
        String pcfAmount="4.7";
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER).setMonth(3);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_PCE_MID, theme)
                .setTXN_AMOUNT(txnAmount)
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.EMI, paymentDTO);
        String theia_facade_logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_CREATE_ORDER_PAY");
        String finalValue=PG2LogsValidationHelper.getKeyParameterValueFromLogs("loanAmount",theia_facade_logs);
        double amt=Double.parseDouble(finalValue);
        double finalTxnAmt=Double.parseDouble(txnAmount)+Double.parseDouble(pcfAmount);
        Assert.assertEquals(amt,finalTxnAmt);

    }

    @Parameters({"theme"})
    @Test(description = "Validate Loan amount in Create order and Pay in theia facade in checkoutjs")
    public void ValidateLoanAmountInCOP(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        String txnAmount="200";
        String pcfAmount="4.70";
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        Constants.MerchantType merchantType = MerchantType.PGOnly_PCE_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutjsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutjsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());

        cashierPage.payBy(PayMode.EMI, paymentDTO);

        String theia_facade_logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_PAY");
        String finalValue=PG2LogsValidationHelper.getKeyParameterValueFromLogs("loanAmount",theia_facade_logs);
        double amt=Double.parseDouble(finalValue);
        double finalTxnAmt=Double.parseDouble(txnAmount)+Double.parseDouble(pcfAmount);
        Assert.assertEquals(amt,finalTxnAmt);

    }



    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Validate loanAmount  with EMI + promo + PCF case in ACQUIRING_CREATE_ORDER_AND_PAY api in theia_facade logs" )
    public void validateLoanAmountWithSpecificPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String txnAmount="200";
        String pcfAmount="7.08";
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        Constants.MerchantType merchantType = Constants.MerchantType.PCEM_PCE_MID;
        Promo promo = new Promo();
        for (int i = 0; i < 2; i++) {
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("TESTEMI90").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchantType, simplifiedPaymentOffers)
                .setTxnValue("200.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutjsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutjsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();


        cashierPage.payBy(PayMode.EMI, paymentDTO);
        String theia_facade_logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_PAY");
        String finalValue=PG2LogsValidationHelper.getKeyParameterValueFromLogs("loanAmount",theia_facade_logs);
        double amt=Double.parseDouble(finalValue);
        String DA="10.00";
        double finalTxnAmt=Double.parseDouble(txnAmount)+Double.parseDouble(pcfAmount)-Double.parseDouble(DA);
        Assert.assertEquals(amt,finalTxnAmt);

    }


    @Parameters({"theme"})
    @Test(description = "Verify router parameter in status query request and formatter for CC transaction")
    public void successfulCCStatusQuery(@Optional("enhancedweb_revamp") String theme) throws PGPException, InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.EMI_STATUSQUERY, theme)
                .setTXN_AMOUNT("99.82")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("402")
                .validateStatus("PENDING")
                .assertAll();
        Thread .sleep(60000);
        String instalogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"pg.router.paytm.bankcard.query.request");

        String splitString = instalogs.split("ExtSN=")[1];
        String ESN = splitString.split("}")[0];
        System.out.println(ESN);
        Assertions.assertThat(instalogs).contains("\"isEmi\":N\"");
        Assertions.assertThat(instalogs).contains("\"emiONUS\":null\"");
        Assertions.assertThat(instalogs).contains("\"payerAccountType\":CC\"");
        Assertions.assertThat(instalogs).contains("\"payMethod\":CREDIT_CARD\"");
        String instaUPIrequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,ESN,"c.p.p.b.s.HDFCService.prepareStatusQryPacket");
        Assertions.assertThat(instaUPIrequestlogs).contains("HDFCService.prepareStatusQryPacket()");
    }

    @Parameters({"theme"})
    @Test(description = "Verify router parameter in status query request and formatter for DC transaction")
    public void successfulDCStatusQuery(@Optional("enhancedweb_revamp") String theme) throws PGPException, InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.EMI_STATUSQUERY, theme)
                .setTXN_AMOUNT("99.82")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("402")
                .validateStatus("PENDING")
                .assertAll();
        Thread .sleep(60000);
        String instalogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"pg.router.paytm.bankcard.query.request");

        String splitString = instalogs.split("ExtSN=")[1];
        String ESN = splitString.split("}")[0];
        System.out.println(ESN);
        Assertions.assertThat(instalogs).contains("\"isEmi\":N\"");
        Assertions.assertThat(instalogs).contains("\"emiONUS\":null\"");
        Assertions.assertThat(instalogs).contains("\"payerAccountType\":DC\"");
        Assertions.assertThat(instalogs).contains("\"payMethod\":DEBIT_CARD\"");
        String instaUPIrequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,ESN,"c.p.p.b.s.HDFCService.prepareStatusQryPacket");
        Assertions.assertThat(instaUPIrequestlogs).contains("HDFCService.prepareStatusQryPacket()");
    }

    @Parameters({"theme"})
    @Test(description = "Verify router parameter in status query request and formatter for EMI transaction")
    public void successfulEMIStatusQuery(@Optional("enhancedweb_revamp") String theme) throws PGPException, InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.EMI_STATUSQUERY, theme)
                .setTXN_AMOUNT("99.82")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.EMI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("EMI")
                .validateRespCode("402")
                .validateStatus("PENDING")
                .assertAll();
        Thread .sleep(60000);
        String instalogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"pg.router.paytm.bankcard.query.request");

        String splitString = instalogs.split("ExtSN=")[1];
        String ESN = splitString.split("}")[0];
        System.out.println(ESN);
        Assertions.assertThat(instalogs).contains("\"isEmi\":Y\"");
        Assertions.assertThat(instalogs).contains("\"emiONUS\":Y\"");
        Assertions.assertThat(instalogs).contains("\"payerAccountType\":CC\"");
        Assertions.assertThat(instalogs).contains("\"payMethod\":EMI\"");
        String instaUPIrequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,ESN,"c.p.p.b.s.HDFCService.prepareStatusQryPacket");
        Assertions.assertThat(instaUPIrequestlogs).contains("HDFCService.prepareStatusQryPacket()");
    }

    @Parameters({"theme"})
    @Test(description = "Verify router parameter in status query request and formatter for EMI_DC transaction")
    public void successfulEMIDCStatusQuery(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.ZEROWALLET);
        PaymentDTO Emi_Dc = new PaymentDTO();
        Emi_Dc.setEmiCard(ICICI_DEBIT_CARD_NUMBER).setBankName("ICICI Bank Debit Card").setMonth(3);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.EMI_STATUSQUERY, theme)
                .setTXN_AMOUNT("99.82")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI, Emi_Dc);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("EMI_DC")
                .validateRespCode("402")
                .validateStatus("PENDING")
                .assertAll();
        Thread .sleep(60000);
        String instalogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"pg.router.paytm.bankcard.query.request");

        String splitString = instalogs.split("ExtSN=")[1];
        String ESN = splitString.split("}")[0];
        System.out.println(ESN);
        Assertions.assertThat(instalogs).contains("\"isEmi\":Y\"");
        Assertions.assertThat(instalogs).contains("\"emiONUS\":Y\"");
        Assertions.assertThat(instalogs).contains("\"payerAccountType\":DC\"");
        Assertions.assertThat(instalogs).contains("\"payMethod\":EMI_DC\"");
        String instaUPIrequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,ESN,"c.p.p.b.s.HDFCService.prepareStatusQryPacket");
        Assertions.assertThat(instaUPIrequestlogs).contains("HDFCService.prepareStatusQryPacket()");
    }


    @Parameters({"theme"})
    @Test(description = "Verify router parameter in status query request and formatter for BAJAJFN EMI transaction")
    public void SuccessfulEBajajFNStatusQuery(@Optional("enhancedweb_revamp") String theme) throws InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.BAJAJFN_MID, theme)
                .setTXN_AMOUNT("8000") .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO()
                .setCreditCardNumber(BAJAJ_FINSERV_CREDIT_CARD_NUMBER1)
                .setEmiCard(BAJAJ_FINSERV_CREDIT_CARD_NUMBER1)
                .setBankName(BAJAJ_FINSERV_DROPDOWN_BANK_NAME)
                .setExpMonth("05")
                .setExpYear("2025")
                .setCvvNumber(null)
                .setMonth(3);
        cashierPage.payBy(PayMode.EMI, paymentDTO);
        Response response = PGPHelpers.getTxnStatusResponse(orderDTO.getMID(), orderDTO.getORDER_ID());
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat(jsonPath.getString("STATUS")).isEqualTo("PENDING");
        Assertions.assertThat(jsonPath.getString("RESPCODE")).isEqualTo("402");
        Thread .sleep(60000);
        String instalogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"pg.router.paytm.bankcard.query.request");
        String splitString = instalogs.split("ExtSN=")[1];
        String ESN = splitString.split("}")[0];
        System.out.println(ESN);
        Assertions.assertThat(instalogs).contains("\"isEmi\":Y\"");
        Assertions.assertThat(instalogs).contains("\"emiONUS\":Y\"");
        Assertions.assertThat(instalogs).contains("\"payerAccountType\":CC\"");
        Assertions.assertThat(instalogs).contains("\"payMethod\":EMI\"");
        String instaUPIrequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,ESN,"c.p.p.b.s.BajajFinservEMIService.statusQry");
        Assertions.assertThat(instaUPIrequestlogs).contains("BajajFinservEMIService.statusQry()");
    }



    @Feature("PGP-46012")
    @Owner("Shubham Soni")
    @Parameters({"theme"})
    @Test(description = "Verify v2/ordercehck affordability and Insta api  for BajajFNNONDBDABSOLUTEZERO transaction")
    public void SuccessfulEBajajFNNONDBDABSOLUTEZERO(@Optional("enhancedweb_revamp") String theme) throws InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.BAJAJFN_MID, theme)
                .setTXN_AMOUNT("8000") .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO()
                .setCreditCardNumber(BAJAJ_FINSERV_CREDIT_CARD_NUMBER1)
                .setEmiCard(BAJAJ_FINSERV_CREDIT_CARD_NUMBER1)
                .setBankName(BAJAJ_FINSERV_DROPDOWN_BANK_NAME)
                .setExpMonth("05")
                .setExpYear("2525")
                .setCvvNumber(null)
                .setMonth(3);
        cashierPage.payBy(PayMode.EMI, paymentDTO);
        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
        directBankOTPPage.submitOtp("111111");
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validatePaymentMode("EMI")
                .validateGatewayName("BAJAJFN")
                .validateBankName("BAJAJ FINSERV EMI CARD")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        Response response = PGPHelpers.getTxnStatusResponse(orderDTO.getMID(), orderDTO.getORDER_ID());
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat(jsonPath.getString("STATUS")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(jsonPath.getString("RESPCODE")).isEqualTo("01");
        Assertions.assertThat(jsonPath.getString("RESPMSG")).isEqualTo("Txn Success");
        Assertions.assertThat(jsonPath.getString("PAYMENTMODE")).isEqualTo("EMI");
        Assertions.assertThat(jsonPath.getString("BANKNAME")).isEqualTo("BAJAJ FINSERV EMI CARD");
        Assertions.assertThat(jsonPath.getString("GATEWAYNAME")).isEqualTo("BAJAJFN");
        String affordabilityrequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"v2/order/checkout\", \"TYPE\" : \"REQUEST\"");
        String affordabilityresponselogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"v2/order/checkout\", \"TYPE\" : \"RESPONSE\"");
        String theialogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_ORDER_MODIFY");
        validatev2ordercheckoutrequest(orderDTO.getMID(),affordabilityrequestlogs,orderDTO.getORDER_ID(),orderDTO.getTXN_AMOUNT(), String.valueOf(paymentDTO.getMonth()));
        validatev2ordercheckoutresponse(orderDTO.getMID(),affordabilityresponselogs,orderDTO.getORDER_ID(),orderDTO.getTXN_AMOUNT());
        Assertions.assertThat(theialogs).contains("\"detailExtendInfo\":{\"AFFORDABILITY_INFO\":\"{\"affordabilityAcquirementId\":");
        String OTPRequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"OTP req before encryption");
        String OTPResponselogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"Decrypted response");
        validateInstaOTPRequest (OTPRequestlogs,orderDTO.getTXN_AMOUNT() ,BAJAJ_FINSERV_CREDIT_CARD_NUMBER1);
        validateInstaOTPResponse (OTPResponselogs);
        String AuthRequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"Auth req before encryption");
        String AuthResponselogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"Decrypted Auth response");
        validateInstaAUTHRequest (AuthRequestlogs,orderDTO.getTXN_AMOUNT() ,BAJAJ_FINSERV_CREDIT_CARD_NUMBER1);
        validateInstaAUTHResponse (AuthResponselogs,orderDTO.getTXN_AMOUNT());
        String STATUSQUERYRequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"StatusQry req before encryption");
        String STATUSQUERYResponselogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"Decrypted response");
        validateInstaSTATUSQUERYRequest (STATUSQUERYRequestlogs);
        validateInstaSTATUSQUERYResponse (STATUSQUERYResponselogs);
    }

    @Feature("PGP-46012")
    @Owner("Shubham Soni")
    @Parameters({"theme"})
    @Test(description = "NATIVE :Verify v2/ordercehck affordability and Insta api  for BajajFNNONDBDABSOLUTEZERO transaction")
    public void SuccessfulEBajajFNNONDBDPERCENTAGEZERO() throws InterruptedException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.BAJAJFN_MID)
                .setTxnValue("8000")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String txnToken = iniJsonPath.getString("body.txnToken");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        fetchPaymentOption.execute();
        PaymentDTO paymentDTO = new PaymentDTO()
                .setCreditCardNumber(BAJAJ_FINSERV_CREDIT_CARD_NUMBER1)
                .setEmiCard(BAJAJ_FINSERV_CREDIT_CARD_NUMBER1)
                .setBankName(BAJAJ_FINSERV_DROPDOWN_BANK_NAME)
                .setExpMonth("05")
                .setExpYear("2025")
                .setCvvNumber("111")
                .setMonth(6);
        String cardInfo = "|" + BAJAJ_FINSERV_CREDIT_CARD_NUMBER1 + "|" + paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(MerchantType.BAJAJFN_MID, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setCardInfo(cardInfo)
                .setPlanId("BAJAJFN|6")
                .setChannelCode("BAJAJFN")
                .setTXN_AMOUNT("8000")
                .build();
        checkoutPage.createNativeOrder(orderDTO, true);
        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
        directBankOTPPage.submitOtp("111111");
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validatePaymentMode("EMI")
                .validateGatewayName("BAJAJFN")
                .validateBankName("BAJAJ FINSERV EMI CARD")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        Response response = PGPHelpers.getTxnStatusResponse(orderDTO.getMID(), orderDTO.getORDER_ID());
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat(jsonPath.getString("STATUS")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(jsonPath.getString("RESPCODE")).isEqualTo("01");
        Assertions.assertThat(jsonPath.getString("RESPMSG")).isEqualTo("Txn Success");
        Assertions.assertThat(jsonPath.getString("PAYMENTMODE")).isEqualTo("EMI");
        Assertions.assertThat(jsonPath.getString("BANKNAME")).isEqualTo("BAJAJ FINSERV EMI CARD");
        Assertions.assertThat(jsonPath.getString("GATEWAYNAME")).isEqualTo("BAJAJFN");
        String affordabilityrequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"v2/order/checkout\", \"TYPE\" : \"REQUEST\"");
        String affordabilityresponselogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"v2/order/checkout\", \"TYPE\" : \"RESPONSE\"");
        String theialogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        validatev2ordercheckoutrequest(orderDTO.getMID(),affordabilityrequestlogs,orderDTO.getORDER_ID(),orderDTO.getTXN_AMOUNT(), String.valueOf(paymentDTO.getMonth()));
        validatev2ordercheckoutresponse(orderDTO.getMID(),affordabilityresponselogs,orderDTO.getORDER_ID(),orderDTO.getTXN_AMOUNT());
        Assertions.assertThat(theialogs).contains("\"detailExtendInfo\":{\"AFFORDABILITY_INFO\":\"{\"affordabilityAcquirementId\":");
        String OTPRequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"OTP req before encryption");
        String OTPResponselogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"Decrypted response");
        validateInstaOTPRequest (OTPRequestlogs,orderDTO.getTXN_AMOUNT() ,BAJAJ_FINSERV_CREDIT_CARD_NUMBER1);
        validateInstaOTPResponse (OTPResponselogs);
        String AuthRequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"Auth req before encryption");
        String AuthResponselogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"Decrypted Auth response");
        validateInstaAUTHRequest (AuthRequestlogs,orderDTO.getTXN_AMOUNT() ,BAJAJ_FINSERV_CREDIT_CARD_NUMBER1);
        validateInstaAUTHResponse (AuthResponselogs,orderDTO.getTXN_AMOUNT());
        String STATUSQUERYRequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"StatusQry req before encryption");
        String STATUSQUERYResponselogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"Decrypted response");
        validateInstaSTATUSQUERYRequest (STATUSQUERYRequestlogs);
        validateInstaSTATUSQUERYResponse (STATUSQUERYResponselogs);
    }


    @Feature("PGP-46012")
    @Owner("Shubham Soni")
    @Parameters({"theme"})
    @Test(description = "CHECKOUTJS:Verify v2/ordercehck affordability and Insta api  for BajajFNNONDBDABSOLUTEZERO transaction")
    public void SuccessfulEBajajFNNONDBDABSOLUTEZEROJS(@Optional("checkoutjs_web_revamp") String theme) throws InterruptedException, IOException {
        PaymentDTO paymentDTO = new PaymentDTO()
                .setCreditCardNumber(BAJAJ_FINSERV_CREDIT_CARD_NUMBER1)
                .setEmiCard(BAJAJ_FINSERV_CREDIT_CARD_NUMBER1)
                .setBankName(BAJAJ_FINSERV_DROPDOWN_BANK_NAME)
                .setExpMonth("05")
                .setExpYear("2525")
                .setCvvNumber("123")
                .setMonth(3);
        Constants.MerchantType merchantType = MerchantType.BAJAJFN_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("8000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutjsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutjsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.EMI, paymentDTO);
        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
        String parent=DriverManager.getDriver().getWindowHandle();
        Set<String> s=DriverManager.getDriver().getWindowHandles();
        Iterator<String> I1= s.iterator();

        while(I1.hasNext())
        {
            String child_window=I1.next();
            if(!parent.equals(child_window)) {
                DriverManager.getDriver().switchTo().window(child_window);
            }}
        directBankOTPPage.submitOtp("111111");
        DriverManager.getDriver().switchTo().window(parent);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(merchantType.getId())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validatePaymentMode("EMI")
                .validateGatewayName("BAJAJFN")
                .validateBankName("BAJAJ FINSERV EMI CARD")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        Response response = PGPHelpers.getTxnStatusResponse(merchantType.getId(),initTxnDTO.orderFromBody());
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat(jsonPath.getString("STATUS")).isEqualTo("TXN_SUCCESS");
        Assertions.assertThat(jsonPath.getString("RESPCODE")).isEqualTo("01");
        Assertions.assertThat(jsonPath.getString("RESPMSG")).isEqualTo("Txn Success");
        Assertions.assertThat(jsonPath.getString("PAYMENTMODE")).isEqualTo("EMI");
        Assertions.assertThat(jsonPath.getString("BANKNAME")).isEqualTo("BAJAJ FINSERV EMI CARD");
        Assertions.assertThat(jsonPath.getString("GATEWAYNAME")).isEqualTo("BAJAJFN");
        String affordabilityrequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"v2/order/checkout\", \"TYPE\" : \"REQUEST\"");
        String affordabilityresponselogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"v2/order/checkout\", \"TYPE\" : \"RESPONSE\"");
        String theialogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        validatev2ordercheckoutrequest(merchantType.getId(),affordabilityrequestlogs,initTxnDTO.orderFromBody(),initTxnDTO.txnAmountFromBody(), String.valueOf(paymentDTO.getMonth()));
        validatev2ordercheckoutresponse(merchantType.getId(),affordabilityresponselogs,initTxnDTO.orderFromBody(),initTxnDTO.txnAmountFromBody());
        Assertions.assertThat(theialogs).contains("\"detailExtendInfo\":{\"AFFORDABILITY_INFO\":\"{\"affordabilityAcquirementId\":");
        String OTPRequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,initTxnDTO.orderFromBody(),"OTP req before encryption");
        String OTPResponselogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,initTxnDTO.orderFromBody(),"Decrypted response");
        validateInstaOTPRequest (OTPRequestlogs,initTxnDTO.txnAmountFromBody(),BAJAJ_FINSERV_CREDIT_CARD_NUMBER1);
        validateInstaOTPResponse (OTPResponselogs);
        String AuthRequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,initTxnDTO.orderFromBody(),"Auth req before encryption");
        String AuthResponselogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,initTxnDTO.orderFromBody(),"Decrypted Auth response");
        validateInstaAUTHRequest (AuthRequestlogs,initTxnDTO.txnAmountFromBody(),BAJAJ_FINSERV_CREDIT_CARD_NUMBER1);
        validateInstaAUTHResponse (AuthResponselogs,initTxnDTO.txnAmountFromBody());
        String STATUSQUERYRequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,initTxnDTO.orderFromBody(),"StatusQry req before encryption");
        String STATUSQUERYResponselogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,initTxnDTO.orderFromBody(),"Decrypted response");
        validateInstaSTATUSQUERYRequest (STATUSQUERYRequestlogs);
        validateInstaSTATUSQUERYResponse (STATUSQUERYResponselogs);
    }

    @Feature("PGP-46012")
    @Owner("Shubham Soni")
    @Parameters({"theme"})
    @Test(description = "Verify v2/ordercehck affordability and Insta api  for BankFormFailureBajajFNRetryDisable transaction")
    public void BankFormFailureBajajFNRetryDisable(@Optional("checkoutjs_web_revamp") String theme) throws InterruptedException, IOException {
        PaymentDTO paymentDTO = new PaymentDTO()
                .setCreditCardNumber(BAJAJ_FINSERV_CREDIT_CARD_NUMBER1)
                .setEmiCard(BAJAJ_FINSERV_CREDIT_CARD_NUMBER1)
                .setBankName(BAJAJ_FINSERV_DROPDOWN_BANK_NAME)
                .setExpMonth("05")
                .setExpYear("2525")
                .setCvvNumber("123")
                .setMonth(3);
        Constants.MerchantType merchantType = MerchantType.BAJAJFN_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("998")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutjsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutjsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.EMI, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(merchantType.getId())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateRespCode("227")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validatePaymentMode("EMI")
                .validateGatewayName("BAJAJFN")
                .validateBankName("BAJAJ FINSERV EMI CARD")
                .validateStatus("TXN_FAILURE")
                .assertAll();
        String affordabilityrequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"v2/order/checkout\", \"TYPE\" : \"REQUEST\"");
        String affordabilityresponselogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"v2/order/checkout\", \"TYPE\" : \"RESPONSE\"");
        String theialogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        validatev2ordercheckoutrequest(merchantType.getId(),affordabilityrequestlogs,initTxnDTO.orderFromBody(),initTxnDTO.txnAmountFromBody(), String.valueOf(paymentDTO.getMonth()));
        validatev2ordercheckoutresponse(merchantType.getId(),affordabilityresponselogs,initTxnDTO.orderFromBody(),initTxnDTO.txnAmountFromBody());
        Assertions.assertThat(theialogs).contains("\"detailExtendInfo\":{\"AFFORDABILITY_INFO\":\"{\"affordabilityAcquirementId\":");
        String OTPRequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,initTxnDTO.orderFromBody(),"OTP req before encryption");
        String OTPResponselogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,initTxnDTO.orderFromBody(),"Decrypted response");
        validateInstaOTPRequest (OTPRequestlogs,initTxnDTO.txnAmountFromBody(),BAJAJ_FINSERV_CREDIT_CARD_NUMBER1);
        Assertions.assertThat(OTPResponselogs).contains("\"RESPDESC\": \"CARD NUMBER DOES NOT EXISTS\"");
        Assertions.assertThat(OTPResponselogs).contains("\"RSPCODE\": \"99\"");
    }

    @Feature("PGP-46012")
    @Owner("Shubham Soni")
    @Parameters({"theme"})
    @Test(description = "Verify retry disable for BAJAJFN EMI transaction")
    public void TXNFailureBajajFNRetryDisable(@Optional("enhancedweb_revamp") String theme) throws InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.BAJAJFN_MID, theme)
                .setTXN_AMOUNT("8000") .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO()
                .setCreditCardNumber(BAJAJ_FINSERV_CREDIT_CARD_NUMBER1)
                .setEmiCard(BAJAJ_FINSERV_CREDIT_CARD_NUMBER1)
                .setBankName(BAJAJ_FINSERV_DROPDOWN_BANK_NAME)
                .setExpMonth("05")
                .setExpYear("2525")
                .setCvvNumber(null)
                .setMonth(3);
        cashierPage.payBy(PayMode.EMI, paymentDTO);
        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
        directBankOTPPage.submitOtp("123456");
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("810")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validatePaymentMode("EMI")
                .validateGatewayName("BAJAJFN")
                .validateBankName("BAJAJ FINSERV EMI CARD")
                .validateStatus("TXN_FAILURE")
                .assertAll();
        String affordabilityrequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"v2/order/checkout\", \"TYPE\" : \"REQUEST\"");
        String affordabilityresponselogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"v2/order/checkout\", \"TYPE\" : \"RESPONSE\"");
        String theialogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_ORDER_MODIFY");
        validatev2ordercheckoutrequest(orderDTO.getMID(),affordabilityrequestlogs,orderDTO.getORDER_ID(),orderDTO.getTXN_AMOUNT(), String.valueOf(paymentDTO.getMonth()));
        validatev2ordercheckoutresponse(orderDTO.getMID(),affordabilityresponselogs,orderDTO.getORDER_ID(),orderDTO.getTXN_AMOUNT());
        Assertions.assertThat(theialogs).contains("\"detailExtendInfo\":{\"AFFORDABILITY_INFO\":\"{\"affordabilityAcquirementId\":");
        String AuthRequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"Auth req before encryption");
        String AuthResponselogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"Decrypted Auth response");
        Assertions.assertThat(AuthResponselogs).contains("\"RESPDESC\": \"INVALID OTP CODE\"");
        Assertions.assertThat(AuthResponselogs).contains("\"RSPCODE\": \"35\"");
        String Responselogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"FGW_OTP_VALIDATION_FAILED");
        Assertions.assertThat(Responselogs).contains("\"resultStatus\":\"F\", \"resultCodeId\":\"23013900\", \"resultCode\":\"FGW_OTP_VALIDATION_FAILED\"");
    }


    @Owner(KARMVIR)
    @Feature("PGP-47723")
    @Parameters("isNativePlus")
    @Test(description ="Test that v1/checkout api should call for standard emi txn")
    public void Testv1CheckoutApiCalledForStdEmiTxn(@Optional("true") Boolean isNativePlus) throws Exception {
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.EMI_V1_CHECKOUT;
        InitTxnDTO initTxnDTO =new InitTxnDTO.Builder(null,merchantType)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("HDFC|3")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM","REQUEST");
        Assertions.assertThat(logs).contains("productInfo").isNotNull();
        Assertions.assertThat(logs).contains("\"productId\":\"001\",");
        Assertions.assertThat(logs).contains("paymentDetails").isNotNull();
        Assertions.assertThat(logs).contains("emiPlanDetails").isNotNull();
        Assertions.assertThat(logs).contains("tenure").isNotNull();
        Assertions.assertThat(logs).contains("FUNCTION","/ats/v1/order/checkout");

    }

    @Owner(KARMVIR)
    @Feature("PGP-49467")
    @Parameters("isNativePlus")
    @Test(description ="Test OrderOfferInfo should not be sent in v1/checkout api request when payment flow is not deal")
    public void TestOrderOfferInfoNotSentForNonDealsTxn(@Optional("true") Boolean isNativePlus) throws Exception {
        String txnAmount="1100";
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchantType= Constants.MerchantType.EMI_V1_CHECKOUT;
        InitiateTransaction initiateTransaction = (InitiateTransaction) new InitiateTransaction().dealsbuildRequest(merchantType.getId(),txnAmount,user.ssoToken())
                .setContext("body.affordabilityInfo.paymentFlow","Payment");
        JsonPath initTxnResponse = initiateTransaction.execute().jsonPath();
        String txnToken = initTxnResponse.getString("body.txnToken");
//        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initiateTransaction.getOrderId(), txnToken, PayMethodType.EMI)
                .setPlanId("HDFC|3")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initiateTransaction.getOrderId(),"AFFORDABILITY_PLATFORM","REQUEST");
        Assertions.assertThat(logs).contains("productInfo").isNotNull();
        Assertions.assertThat(logs).doesNotContain("orderOfferInfo");
        Assertions.assertThat(logs).contains("\"productId\":\"001\",");
        Assertions.assertThat(logs).contains("paymentDetails").isNotNull();
        Assertions.assertThat(logs).contains("emiPlanDetails").isNotNull();
        Assertions.assertThat(logs).contains("tenure").isNotNull();
        Assertions.assertThat(logs).contains("FUNCTION","/ats/v1/order/checkout");
    }

    @Owner(KARMVIR)
    @Feature("PGP-49459")
    @Parameters("isNativePlus")
    @Test(description ="Test that v1/checkout api should not call for redemptiontype= Paytm_cashback")
    public void Testv1CheckoutShouldNotCallForPaytm_CashbackTxn(@Optional("true") Boolean isNativePlus) throws Exception {
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.EMI_V1_CHECKOUT;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("PROMO03232423").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO =new InitTxnDTO.Builder(null,merchantType,simplifiedPaymentOffers)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("HDFC|3")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody());
        Assertions.assertThat(logs).contains("paymentpromo/checkout");
        Assertions.assertThat(logs).doesNotContain("AFFORDABILITY_PLATFORM");
    }

    @Owner(KARMVIR)
    @Feature("PGP-47723")
    @Parameters("isNativePlus")
    @Test(description ="Test that v1/checkout api should not call for NB+Promo txn")
    public void Testv1CheckoutShouldNotCallForNBPromoTxn(@Optional("true") Boolean isNativePlus) throws Exception {
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.EMI_V1_CHECKOUT;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO =new InitTxnDTO.Builder(null,merchantType,simplifiedPaymentOffers)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody());
        Assertions.assertThat(logs).contains("paymentpromo/checkout");
        Assertions.assertThat(logs).doesNotContain("AFFORDABILITY_PLATFORM");
    }

    @Owner(KARMVIR)
    @Feature("PGP-49451")
    @Parameters("isNativePlus")
    @Test(description ="Test that v1/checkout api should call for standard emi with Promo txn amount based")
    public void Testv1CheckoutApiCalledForStdEmiPromoTxn(@Optional("true") Boolean isNativePlus) throws Exception {
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.EMI_V1_CHECKOUT;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO =new InitTxnDTO.Builder(null,merchantType,simplifiedPaymentOffers)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("HDFC|3")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM","REQUEST");
        Assertions.assertThat(logs).contains("productInfo").isNotNull();
        Assertions.assertThat(logs).contains("promoCheckoutInfo").isNotNull();
        Assertions.assertThat(logs).contains("\"productId\":\"001\",");
        Assertions.assertThat(logs).contains("paymentDetails").isNotNull();
        Assertions.assertThat(logs).contains("emiPlanDetails").isNotNull();
        Assertions.assertThat(logs).contains("tenure").isNotNull();
        Assertions.assertThat(logs).contains("FUNCTION","/ats/v1/order/checkout");

    }

    @Owner(KARMVIR)
    @Feature("PGP-49451")
    @Parameters("isNativePlus")
    @Test(description ="Test that v1/checkout api should call for Debit card  with Promo txn amount based")
    public void Testv1CheckoutApiCalledForDCPromoTxn(@Optional("true") Boolean isNativePlus) throws Exception {
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.EMI_V1_CHECKOUT;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO =new InitTxnDTO.Builder(null,merchantType,simplifiedPaymentOffers)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM","REQUEST");
        Assertions.assertThat(logs).contains("productInfo").isNotNull();
        Assertions.assertThat(logs).contains("promoCheckoutInfo").isNotNull();
        Assertions.assertThat(logs).contains("\"productId\":\"001\",");
        Assertions.assertThat(logs).contains("paymentDetails").isNotNull();
        Assertions.assertThat(logs).doesNotContain("emiPlanDetails");
        Assertions.assertThat(logs).contains("FUNCTION","/ats/v1/order/checkout");

    }

    @Owner(KARMVIR)
    @Feature("PGP-49451")
    @Parameters("isNativePlus")
    @Test(description ="Test that v1/checkout api should call for standard emi with Promo txn Item based")
    public void Testv1CheckoutApiCalledForStdEmiPromoTxnItembased(@Optional("true") Boolean isNativePlus) throws Exception {

        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.EMI_V1_CHECKOUT;
        List<String> categoryIds= new ArrayList<>();
        categoryIds.add("521");
        SimplifiedPaymentOffers.ProductDetail productDetail= new SimplifiedPaymentOffers.ProductDetail("123400232343411113e331", null, "112866",categoryIds);
        SimplifiedPaymentOffers.Items items= new SimplifiedPaymentOffers.Items("113","","1100",productDetail);
        List<SimplifiedPaymentOffers.Items> itemsList= new ArrayList<>();
        itemsList.add(items);
        SimplifiedPaymentOffers.CartDetails cartDetails= new SimplifiedPaymentOffers.CartDetails(itemsList);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("","true","true",cartDetails);
        InitTxnDTO initTxnDTO =new InitTxnDTO.Builder(null,merchantType,simplifiedPaymentOffers)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("HDFC|3")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM","REQUEST");
        Assertions.assertThat(logs).contains("productInfo").isNotNull();
        Assertions.assertThat(logs).contains("promoCheckoutInfo").isNotNull();
        Assertions.assertThat(logs).contains("\"productId\":\"123400232343411113e331\",");
        Assertions.assertThat(logs).contains("paymentDetails").isNotNull();
        Assertions.assertThat(logs).contains("emiPlanDetails").isNotNull();
        Assertions.assertThat(logs).contains("tenure").isNotNull();
        Assertions.assertThat(logs).contains("FUNCTION","/ats/v1/order/checkout");

    }

    @Owner(KARMVIR)
    @Feature("PGP-49451")
    @Test(description = "Test that v1/checkout api should be called for EMI+subvention txn for custom checkout flow")
    public void Testv1CheckoutApiCalledForSubEmiTxnAmountbased() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI_V1_CHECKOUT;
        PaymentDTO paymentDTO = new PaymentDTO();
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="1100";
        String subventionAmount = "1100";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setMerchantKey(emiMerchant.getKey())
                .setCustomerId(orderId)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", paymentDTO)))
                .setGenerateTokenForIntent(true)
                .setOfferDetails(new OfferDetails().setOfferId("2252327"))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        JsonPath validateResponse =  r2.then()
                .extract().jsonPath();
        String finalTxnAmount = validateResponse.getString("body.finalTransactionAmount");
        String emiSubventionToken = validateResponse.getString("body.emiSubventionToken");
        String pgPlanId = validateResponse.getString("body.pgPlanId");

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setPayableAmount(new TxnAmount(finalTxnAmount))
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        OrderDTO orderDTO = new OrderFactory.Native(emiMerchant, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.EMI)
                .setChannelCode("HDFC")
                .setPlanId(pgPlanId)
                .setTXN_AMOUNT(price)
                .setAUTH_MODE("otp")
                .setINDUSTRY_TYPE_ID("Retail").build();
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM","REQUEST");
        Assertions.assertThat(logs).contains("productInfo").isNotNull();
        Assertions.assertThat(logs).contains("subventionCheckoutInfo").isNotNull();
        Assertions.assertThat(logs).contains("productId").isNotNull();
        Assertions.assertThat(logs).contains("paymentDetails").isNotNull();
        Assertions.assertThat(logs).contains("emiPlanDetails").isNotNull();
        Assertions.assertThat(logs).contains("tenure").isNotNull();
        Assertions.assertThat(logs).contains("FUNCTION","/ats/v1/order/checkout");
    }

    @Owner(KARMVIR)
    @Feature("PGP-49466")
    @Test(description = "Test that v1/checkout api should be called but 2 object should not be sent in v1/checkout request for partial amount subvention")
    public void Testv1CheckoutApiCalledForSubEmiTxnPartialAmountbased() throws Exception {
        Constants.MerchantType emiMerchant = MerchantType.EMI_V1_CHECKOUT;
        PaymentDTO paymentDTO = new PaymentDTO();
        String orderId = CommonHelpers.generateOrderId();
        User user = userManager.getForRead(Label.BASIC);
        String price ="1100";
        String subventionAmount = "500";

        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        ApiV1Tenure api = new ApiV1Tenure(emiMerchant.getId(), req);
        Response r = api.execute();
        JsonPath tenureResp = r.then()
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath();

        String planId = tenureResp.getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(price,subventionAmount)
                .setMid(emiMerchant.getId())
                .setMerchantKey(emiMerchant.getKey())
                .setCustomerId(orderId)
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setPlanId(intplanid)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount(price)
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", paymentDTO)))
                .setGenerateTokenForIntent(true)
                .setOfferDetails(new OfferDetails().setOfferId("2252327"))
                .build();
        ApiV1Validate api2 = new ApiV1Validate(emiMerchant.getId(), req2);
        Response r2 = api2.execute();
        JsonPath validateResponse =  r2.then()
                .extract().jsonPath();

        String emiSubventionToken = validateResponse.getString("body.emiSubventionToken");
        String pgPlanId = validateResponse.getString("body.pgPlanId");
        String finalTxnAmount = validateResponse.getString("body.finalTransactionAmount");

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),emiMerchant)
                .setTxnValue(price)
                .setPayableAmount(new TxnAmount(finalTxnAmount))
                .setOrderId(orderId)
                .setEmiSubventionToken(emiSubventionToken)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        OrderDTO orderDTO = new OrderFactory.Native(emiMerchant, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.EMI)
                .setChannelCode("HDFC")
                .setPlanId(pgPlanId)
                .setTXN_AMOUNT(price)
                .setAUTH_MODE("otp")
                .setINDUSTRY_TYPE_ID("Retail").build();
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM","REQUEST");
        String subString="itemDetails";
        Assertions.assertThat(countOfSubstring(logs,subString)).isEqualTo(3);
        Assertions.assertThat(logs).contains("productInfo").isNotNull();
        Assertions.assertThat(logs).contains("subventionCheckoutInfo").isNotNull();
        Assertions.assertThat(logs).contains("productId").isNotNull();
        Assertions.assertThat(logs).contains("paymentDetails").isNotNull();
        Assertions.assertThat(logs).contains("emiPlanDetails").isNotNull();
        Assertions.assertThat(logs).contains("tenure").isNotNull();
        Assertions.assertThat(logs).contains("FUNCTION","/ats/v1/order/checkout");
    }
    @Feature("PGP-50816")
    @Owner(KARMVIR)
    @Parameters("isNativePlus")
    @Test(description = "Perform a EMI_DC only transaction with PCF merchant")
    public void VerifyEMI_DCTxnWithPCF(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.EMIDC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_PCE_MID;
        String txnAmount = "1100";
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" + ICICI_DEBIT_CARD_EMI + "|" + paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("ICICI|3")
                .setCardInfo(cardInfo)
                .setChannelCode("ICICI")
                .setEMI_TYPE("DEBIT_CARD")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("ICIE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.ICICI.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }
    @Feature("PGP-50816")
    @Owner(KARMVIR)
    @Parameters("isNativePlus")
    @Test(description = "Perform a EMI_DC only transaction with PCF merchant with Promo")
    public void VerifyEMI_DCTxnWithPCFWithPromo(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.EMIDC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_PCE_MID;
        String txnAmount = "1100";
        Merchant merchant = new Merchant(merchantType.getId(), true);
        Promo promocode = new Promo(false);
        merchant.getPromos().add(promocode);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType, simplifiedPaymentOffers)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" + ICICI_DEBIT_CARD_EMI + "|" + paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("ICICI|3")
                .setCardInfo(cardInfo)
                .setChannelCode("ICICI")
                .setEMI_TYPE("DEBIT_CARD")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("ICIE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.ICICI.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }
    @Feature("PGP-51420")
    @Owner(KARMVIR)
    @Parameters("isNativePlus")
    @Test(description = "Verify that Error message should be The selected EMI plan is not applicable for this transaction. when Invalid planId provided in PTC for EMI")
    public void VerifyTheResponseMessagewheninvalidPlanIdProvidedForEMI(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        String txnAmount = "1100";
        String mid=merchantType.getId().toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|20")
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("The selected EMI plan is not applicable for this transaction.");
    }
    @Feature("PGP-51420")
    @Owner(KARMVIR)
    @Parameters("isNativePlus")
    @Test(description = "Verify that Error message should be The selected EMI plan is not applicable for this transaction. when Invalid planId provided in PTC for EMI_DC")
    public void VerifyTheResponseMessagewheninvalidPlanIdProvidedForEMIDC(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        User user = userManager.getForRead(Label.EMIDC);
        String sso=user.ssoToken();
        String txnAmount = "1100";
        String mid=merchantType.getId().toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(sso, merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4799320857008816|111|122025")
                .setAuthMode("otp")
                .setChannelCode("ICICI")
                .setEmiType("DEBIT_CARD")
                .setPlanId("ICICI|20")
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("The selected EMI plan is not applicable for this transaction.");
    }
    @Feature("PGP-51420")
    @Owner(KARMVIR)
    @Parameters("isNativePlus")
    @Test(description = "Verify that Error message should be The selected EMI plan is not applicable for this transaction. when planId provided is not configured on merchant in PTC for EMI")
    public void VerifyTheResponseMessagewhenPlanIdSentNotConfiguredOnMIDProvidedForEMI(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        String txnAmount = "1100";
        String mid=merchantType.getId().toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|27")
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("The selected EMI plan is not applicable for this transaction.");
    }
    @Feature("PGP-51420")
    @Owner(KARMVIR)
    @Parameters("isNativePlus")
    @Test(description = "Verify that Error message should be The selected EMI plan is not applicable for this transaction. when planId provided is not configured on merchant in PTC for EMI_DC")
    public void VerifyTheResponseMessagewhenPlanIdSentNotConfiguredOnMIDProvidedForEMIDC(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        User user = userManager.getForRead(Label.EMIDC);
        String sso=user.ssoToken();
        String txnAmount = "1100";
        String mid=merchantType.getId().toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(sso, merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4799320857008816|111|122025")
                .setAuthMode("otp")
                .setChannelCode("ICICI")
                .setEmiType("DEBIT_CARD")
                .setPlanId("ICICI|27")
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("The selected EMI plan is not applicable for this transaction.");
    }

    @Feature("PGP-51420")
    @Owner(KARMVIR)
    @Parameters("isNativePlus")
    @Test(description = "Verify that Error message should be Card number entered is not matching with the selected Bank when paymode is EMI CC and DC card provided")
    public void VerifyTheResponseMessagewhenPayModeIsEMIDCAndCCCardProvided(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        String txnAmount = "1100";
        String mid=merchantType.getId().toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("DEBIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Card number entered does not match with the selected Bank");
    }
    @Feature("PGP-51420")
    @Owner(KARMVIR)
    @Parameters("isNativePlus")
    @Test(description = "Verify that Error message should be Card number entered is not matching with the selected Bank when paymode is EMI CC and DC card provided")
    public void VerifyTheResponseMessagewhenPayModeIsEMIAndDCCardProvided(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        User user = userManager.getForRead(Label.EMIDC);
        String sso=user.ssoToken();
        String txnAmount = "1100";
        String mid=merchantType.getId().toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(sso, merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4799320857008816|111|122025")
                .setAuthMode("otp")
                .setChannelCode("ICICI")
                .setEmiType("CREDIT_CARD")
                .setPlanId("ICICI|3")
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Card number entered does not match with the selected Bank");
    }
    @Feature("PGP-51420")
    @Owner(KARMVIR)
    @Parameters("isNativePlus")
    @Test(description = "Verify that Error message should be Card number entered is not matching with the selected Bank when HDFC Plan is passed and ICICI card number entered")
    public void VerifyTheResponseMessagewhenPlanIdIsofHDFCBankAndCardNumberIsOfICICIEMIDC(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        String txnAmount = "1100";
        String mid=merchantType.getId().toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4799320857008816|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("DEBIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Card number entered does not match with the selected Bank");
    }
    @Feature("PGP-51420")
    @Owner(KARMVIR)
    @Parameters("isNativePlus")
    @Test(description = "Verify that Error message should be Card number entered is not matching with the selected Bank when ICICI Plan is passed and HDFC card number entered")
    public void VerifyTheResponseMessagewhenPlanIdIsofICICIBankAndCardNumberIsOfHDFCEMI(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        User user = userManager.getForRead(Label.EMIDC);
        String sso=user.ssoToken();
        String txnAmount = "1100";
        String mid=merchantType.getId().toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(sso, merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid,txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("ICICI")
                .setEmiType("CREDIT_CARD")//VerifyTheResponseMessagewhenPlanIdIsofHDFCBankAndCardNumberIsOfICICI
                .setPlanId("ICICI|3")
                .build();
        ProcessTxnV1Response ptcResponse =NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Card number entered does not match with the selected Bank");
    }
    @Feature("PGP-50907")
    @Owner(KARMVIR)
    @Parameters("isNativePlus")
    @Test(description = "Verify that minAmount and MaxAmount to be sent in COP for EMI txn")
    public void VerifyThatMinMaxAmountSentInCOPRequest(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        String txnAmount = "1100";
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" + VISA_HDFC_EMI_CREDIT_CARD_NUMBER + "|" + paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("HDFC|3")
                .setCardInfo(cardInfo)
                .setChannelCode("HDFC")
                .setEMI_TYPE("CREDIT_CARD")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_CREATE_ORDER_AND_PAY","REQUEST");
        Assertions.assertThat(logs).contains("emiMinAmount").isNotNull();
        Assertions.assertThat(logs).contains("emiMaxAmount").isNotNull();
    }

    @Feature("PGP-53495")
    @Owner(MAYURI)
    @Parameters("isNativePlus")
    @Test(description = "Verify FPO response should contain HDFC,ICICI bank of EMI_DC when enablePaymentMode contains only EMI_DC")
    public void validateFetchPaymentOptionWhenEMIDCPassedInEnablePaymentMode() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        Double txnAmount = 2000.0;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(null, "EMI_DC", new String[]{"HDFC", "ICICI"});
        User user = userManager.getForWrite(Label.BASICTOKYC);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setEightDigitBinRequired("False")
                .setCardHashRequired("true")
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode})
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String displayName = fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].displayName");
        Assert.assertEquals(displayName, "EMI", "Expected displayName is not EMI");
        if( fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].channelCode").toString()== "ICICI" && displayName=="EMI")
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].emiType").toString()).isEqualTo("DEBIT_CARD");
        if( fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].channelCode").toString()== "HDFC" && displayName=="EMI")
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].emiType").toString()).isEqualTo("DEBIT_CARD");
    }

    @Feature("PGP-53495")
    @Owner(MAYURI)
    @Parameters("isNativePlus")
    @Test(description = "Verify FPO response should contain ICICI bank of EMI_DC when enablePaymentMode contains only EMI_DC,bank=ICICI")
    public void validateFetchPaymentOptionWhenEMIDCWithICICIBankPassedInEnablePaymentMode() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        Double txnAmount = 2000.0;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(null, "EMI_DC", new String[]{"ICICI"});
        User user = userManager.getForWrite(Label.BASICTOKYC);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setEightDigitBinRequired("False")
                .setCardHashRequired("true")
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode})
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String displayName = fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].displayName");
        Assert.assertEquals(displayName, "EMI", "Expected displayName is not EMI");
        if( fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].channelCode").toString()== "ICICI" && displayName=="EMI")
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].emiType").toString()).isEqualTo("DEBIT_CARD");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].channelCode").toString()).isNotEqualTo("HDFC");
    }

    @Feature("PGP-53495")
    @Owner(MAYURI)
    @Parameters("isNativePlus")
    @Test(description = "Verify FPO response should contain all banks of EMI_DC when enablePaymentMode contains only EMI_DC,No bank specified")
    public void validateFetchPaymentOptionWhenEMIDCWithNOBankPassedInEnablePaymentMode() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        Double txnAmount = 2000.0;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(null, "EMI_DC", new String[]{});
        User user = userManager.getForWrite(Label.BASICTOKYC);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setEightDigitBinRequired("False")
                .setCardHashRequired("true")
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode})
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String displayName = fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].displayName");
        Assert.assertEquals(displayName, "EMI", "Expected displayName is not EMI");
        if( fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].channelCode").toString()== "ICICI" && displayName=="EMI")
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].emiType").toString()).isEqualTo("DEBIT_CARD");
        if( fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].channelCode").toString()== "HDFC" && displayName=="EMI")
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].emiType").toString()).isEqualTo("DEBIT_CARD");
    }

    @Feature("PGP-53495")
    @Owner(MAYURI)
    @Parameters("isNativePlus")
    @Test(description = "Verify FPO response should contain all banks of EMI CC and DC when enablePaymentMode contains EMI and EMI_DC,No bank specified")
    public void validateFetchPaymentOptionWhenEMIDCAndEMIPassedInEnablePaymentMode() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        Double txnAmount = 2000.0;
        EnablePaymentMode enableEMIDCPaymentMode = new EnablePaymentMode(null, "EMI_DC", new String[]{});
        EnablePaymentMode enableEMIPaymentMode = new EnablePaymentMode(null, "EMI", new String[]{});
        User user = userManager.getForWrite(Label.BASICTOKYC);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setEightDigitBinRequired("False")
                .setCardHashRequired("true")
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setEnablePaymentMode(new EnablePaymentMode[]{enableEMIDCPaymentMode,enableEMIPaymentMode})
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String displayName = fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].displayName");
        Assert.assertEquals(displayName, "EMI", "Expected displayName is not EMI");
        if( fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].channelCode").toString()== "ICICI" && displayName=="EMI")
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].emiType").toString()).isEqualTo("DEBIT_CARD");
        if( fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].channelCode").toString()== "HDFC" && displayName=="EMI")
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].emiType").toString()).isEqualTo("DEBIT_CARD");
        if( fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].channelCode").toString()== "AMEX" && displayName=="EMI")
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].emiType").toString()).isEqualTo("CREDIT_CARD");
        if( fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].channelCode").toString()== "ICICI" && displayName=="EMI")
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].emiType").toString()).isEqualTo("CREDIT_CARD");
        if( fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].channelCode").toString()== "HDFC" && displayName=="EMI")
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].emiType").toString()).isEqualTo("CREDIT_CARD");
    }

    @Feature("PGP-53495")
    @Owner(MAYURI)
    @Parameters("isNativePlus")
    @Test(description = "Verify FPO response should contain ICICI EMI CC and HDFC EMI_DC when enablePaymentMode contains EMI with bank=ICICI and EMI_DC with bank=HDFC")
    public void validateFetchPaymentOptionWhenEMIDCAndEMIWithBankPassedInEnablePaymentMode() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        Double txnAmount = 2000.0;
        EnablePaymentMode enableEMIDCPaymentMode = new EnablePaymentMode(null, "EMI_DC", new String[]{"HDFC"});
        EnablePaymentMode enableEMIPaymentMode = new EnablePaymentMode(null, "EMI", new String[]{"ICICI"});
        User user = userManager.getForWrite(Label.BASICTOKYC);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setEightDigitBinRequired("False")
                .setCardHashRequired("true")
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setEnablePaymentMode(new EnablePaymentMode[]{enableEMIDCPaymentMode,enableEMIPaymentMode})
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String displayName = fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].displayName");
        Assert.assertEquals(displayName, "EMI", "Expected displayName is not EMI");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions.find { it.emiType == 'DEBIT_CARD' }.channelCode").toString()).isNotEqualTo("ICICI");
        if( fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].channelCode").toString()== "HDFC" && displayName=="EMI")
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].emiType").toString()).isEqualTo("DEBIT_CARD");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions.find { it.emiType == 'CREDIT_CARD' }.channelCode").toString()).isNotEqualTo("AMEX");
        if( fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].channelCode").toString()== "ICICI" && displayName=="EMI")
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].emiType").toString()).isEqualTo("CREDIT_CARD");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions.find { it.emiType == 'CREDIT_CARD' }.channelCode").toString()).isNotEqualTo("HDFC");
    }

    @Feature("PGP-53495")
    @Owner(MAYURI)
    @Parameters("isNativePlus")
    @Test(description = "Verify FPO response should contain ICICI EMI CC and All banks for EMI_DC when enablePaymentMode contains EMI with bank=ICICI and EMI_DC with bank=HDFC")
    public void validateFetchPaymentOptionWhenEMIDCAllBankAndEMIICICIBankPassedInEnablePaymentMode() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        Double txnAmount = 2000.0;
        EnablePaymentMode enableEMIDCPaymentMode = new EnablePaymentMode(null, "EMI_DC", new String[]{});
        EnablePaymentMode enableEMIPaymentMode = new EnablePaymentMode(null, "EMI", new String[]{"ICICI"});
        User user = userManager.getForWrite(Label.BASICTOKYC);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setEightDigitBinRequired("False")
                .setCardHashRequired("true")
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setEnablePaymentMode(new EnablePaymentMode[]{enableEMIDCPaymentMode,enableEMIPaymentMode})
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String displayName = fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].displayName");
        Assert.assertEquals(displayName, "EMI", "Expected displayName is not EMI");
        if( fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].channelCode").toString()== "ICICI" && displayName=="EMI")
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].emiType").toString()).isEqualTo("DEBIT_CARD");
        if( fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].channelCode").toString()== "HDFC" && displayName=="EMI")
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].emiType").toString()).isEqualTo("DEBIT_CARD");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions.find { it.emiType == 'CREDIT_CARD' }.channelCode").toString()).isNotEqualTo("AMEX");
        if( fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].channelCode").toString()== "ICICI" && displayName=="EMI")
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].emiType").toString()).isEqualTo("CREDIT_CARD");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions.find { it.emiType == 'CREDIT_CARD' }.channelCode").toString()).isNotEqualTo("HDFC");
    }

    @Feature("PGP-53495")
    @Owner(MAYURI)
    @Parameters("isNativePlus")
    @Test(description = "Verify FPO response should contain all banks  CC and HDFC EMI_DC when enablePaymentMode contains EMI with bank=ICICI and EMI_DC with bank=HDFC")
    public void validateFetchPaymentOptionWhenEMIDCWithHDFCBankAndEMIWithAllBankPassedInEnablePaymentMode() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        Double txnAmount = 2000.0;
        EnablePaymentMode enableEMIDCPaymentMode = new EnablePaymentMode(null, "EMI_DC", new String[]{"HDFC"});
        EnablePaymentMode enableEMIPaymentMode = new EnablePaymentMode(null, "EMI", new String[]{});
        User user = userManager.getForWrite(Label.BASICTOKYC);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setEightDigitBinRequired("False")
                .setCardHashRequired("true")
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setEnablePaymentMode(new EnablePaymentMode[]{enableEMIDCPaymentMode,enableEMIPaymentMode})
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String displayName = fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].displayName");
        Assert.assertEquals(displayName, "EMI", "Expected displayName is not EMI");
        if( fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].channelCode").toString()== "ICICI" && displayName=="EMI")
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].emiType").toString()).isEqualTo("DEBIT_CARD");
        if( fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].channelCode").toString()== "HDFC" && displayName=="EMI")
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].emiType").toString()).isEqualTo("DEBIT_CARD");
        if( fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].channelCode").toString()== "AMEX" && displayName=="EMI")
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].emiType").toString()).isEqualTo("CREDIT_CARD");
        if( fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].channelCode").toString()== "ICICI" && displayName=="EMI")
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].emiType").toString()).isEqualTo("CREDIT_CARD");
        if( fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].channelCode").toString()== "HDFC" && displayName=="EMI")
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].payChannelOptions[0].emiType").toString()).isEqualTo("CREDIT_CARD");
    }

    @Feature("PGP-53495")
    @Owner(MAYURI)
    @Parameters("isNativePlus")
    @Test(description = "Verify FPO response should contain all banks  CC and HDFC EMI_DC when enablePaymentMode contains EMI with bank=ICICI and EMI_DC with bank=HDFC")
    public void validateFetchPaymentOptionWhenEMIDCAndEMIAndCreditCardPassedInEnablePaymentMode() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        Double txnAmount = 2000.0;
        EnablePaymentMode enableEMIDCPaymentMode = new EnablePaymentMode(null, "EMI_DC", new String[]{"HDFC"});
        EnablePaymentMode enableCreditCardPaymentMode = new EnablePaymentMode(null, "CREDIT_CARD", new String[]{});
        EnablePaymentMode enableEMIPaymentMode = new EnablePaymentMode(null, "EMI", new String[]{"ICICI"});
        User user = userManager.getForWrite(Label.BASICTOKYC);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setEightDigitBinRequired("False")
                .setCardHashRequired("true")
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setEnablePaymentMode(new EnablePaymentMode[]{enableEMIDCPaymentMode,enableEMIPaymentMode,enableCreditCardPaymentMode})
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String displayName = fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[1].displayName");
        Assert.assertEquals(displayName, "EMI", "Expected displayName is not EMI");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[1].payChannelOptions.find { it.emiType == 'DEBIT_CARD' }.channelCode").toString()).isNotEqualTo("ICICI");
        if( fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[1].payChannelOptions[0].channelCode").toString()== "HDFC" && displayName=="EMI")
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[1].payChannelOptions[0].emiType").toString()).isEqualTo("DEBIT_CARD");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[1].payChannelOptions.find { it.emiType == 'CREDIT_CARD' }.channelCode").toString()).isNotEqualTo("AMEX");
        if( fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[1].payChannelOptions[0].channelCode").toString()== "ICICI" && displayName=="EMI")
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[1].payChannelOptions[0].emiType").toString()).isEqualTo("CREDIT_CARD");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[1].payChannelOptions.find { it.emiType == 'CREDIT_CARD' }.channelCode").toString()).isNotEqualTo("HDFC");
        String creditCardDisplayName = fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].displayName");
        Assert.assertEquals(creditCardDisplayName, "Credit Card", "Expected displayName is not Credit Card");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find { it.displayName == 'Credit Card' }.paymentMode").toString()).isEqualTo("CREDIT_CARD");

    }

    @Feature("PGP-53495")
    @Owner(MAYURI)
    @Parameters("isNativePlus")
    @Test(description = "Verify FPO response should contain all banks  CC and HDFC EMI_DC when enablePaymentMode contains EMI with bank=ICICI and EMI_DC with bank=HDFC")
    public void validateFetchPaymentOptionWhenEMIDCPassedInEnablePaymentModeAndTxnAmoutLessThanMinEMIAmount() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        Double txnAmount = 2.0;
        EnablePaymentMode enableEMIDCPaymentMode = new EnablePaymentMode(null, "EMI_DC", new String[]{"HDFC"});
        EnablePaymentMode enableEMIPaymentMode = new EnablePaymentMode(null, "EMI", new String[]{"ICICI"});
        User user = userManager.getForWrite(Label.BASICTOKYC);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setEightDigitBinRequired("False")
                .setCardHashRequired("true")
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setEnablePaymentMode(new EnablePaymentMode[]{enableEMIDCPaymentMode,enableEMIPaymentMode})
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[]")).hasSize(2);
    }

    @Feature("PGP-53495")
    @Owner(MAYURI)
    @Parameters("isNativePlus")
    @Test(description = "Verify FPO response should contain all banks  CC and HDFC EMI_DC when enablePaymentMode contains EMI with bank=ICICI and EMI_DC with bank=HDFC")
    public void validateFetchPaymentOptionWhenEMIDCPassedInEnablePaymentModeAndTxnAmoutGreaterThanMaxEMIAmount() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        Double txnAmount = 1000001.0;
        EnablePaymentMode enableEMIDCPaymentMode = new EnablePaymentMode(null, "EMI_DC", new String[]{"HDFC"});
        EnablePaymentMode enableEMIPaymentMode = new EnablePaymentMode(null, "EMI", new String[]{"ICICI"});
        User user = userManager.getForWrite(Label.BASICTOKYC);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setEightDigitBinRequired("False")
                .setCardHashRequired("true")
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setEnablePaymentMode(new EnablePaymentMode[]{enableEMIDCPaymentMode,enableEMIPaymentMode})
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[]")).hasSize(2);

    }

    @Feature("PGP-52897")
    @Owner(MAYURI)
    @Parameters("isNativePlus")
    @Test(description = "Verify FPO response for Only  Amount based subvention")
    public void validateFPOWhenFetchUnifiedOffersContainsOnlyAmountBasedSubventionForDeferredFlow() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        Double txnAmount = 1500.0;
        User user = userManager.getForWrite(Label.BASICTOKYC);
        FetchUnifiedOffers fetchUnifiedOffers = new FetchUnifiedOffers(true, null, null);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setEightDigitBinRequired("False")
                .setCardHashRequired("true")
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setFetchUnifiedOffers(fetchUnifiedOffers)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        if (fetchPaymentOptionsJson.getList("body.unifiedOffers").get(0) != null) {
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryTitle").toString()).isEqualTo("EMI Linked Offers");
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryType").toString()).isEqualTo("EMI_LINKED_OFFERS");
        }
        if (fetchPaymentOptionsJson.getList("body.unifiedOffers[0].categoryOfferDetails").get(0) != null) {
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].issuingBank").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].payMethod").toString()).isEqualTo("EMI");
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].benefitText").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].type").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].emiType").toString()).isEqualTo("SUBVENTION");
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].subventionType").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].offerId").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].validUpto").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].validFrom").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].tnc").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].offerDescription").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].type")).isNull();
        }
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.subventionDetails.subventionAmount").toString()).isEqualTo("1500.0");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.subventionDetails.amountBasedSubvention").toString()).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiType").toString()).isEqualTo("SUBVENTION");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].issuingBank").toString()).isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].bankLogo").toString()).isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].payMethod").toString()).isEqualTo("EMI");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].benefit").toString()).isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getList("body.emiBankDetails[0].emiTypeDetails[0].subventionTypes").toString()).isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].cardDetails.cardType").toString()).isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].cardDetails.cardLabel").toString()).isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].showKFSLink").toString()).isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].showLendingConsent").toString()).isNotNull();
    }

    @Feature("PGP-52897")
    @Owner(MAYURI)
    @Parameters("isNativePlus")
    @Test(description = "Verify FPO response for Only  Amount based Bank offer")
    public void validateFPOWhenFetchUnifiedOffersContainsOnlyAmountBasedBankOfferForDeferredFlow() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        Double txnAmount = 1500.0;
        User user = userManager.getForWrite(Label.BASICTOKYC);
        FetchUnifiedOffers fetchUnifiedOffers = new FetchUnifiedOffers(null, true, null);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setEightDigitBinRequired("False")
                .setCardHashRequired("true")
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setFetchUnifiedOffers(fetchUnifiedOffers)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        if (fetchPaymentOptionsJson.getList("body.unifiedOffers").get(0) != null) {
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryTitle").toString()).isEqualTo("EMI Linked Offers");
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryType").toString()).isEqualTo("EMI_LINKED_OFFERS");
        }
        if (fetchPaymentOptionsJson.getList("body.unifiedOffers[0].categoryOfferDetails").get(0) != null) {
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].issuingBank").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].payMethod").toString()).isEqualTo("EMI");
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].benefitText").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].gratificationText").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].type").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].offerId").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].promocode").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].tnc").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].offerTitle").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].offerText").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].offerDescription").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].type")).isNull();
        }
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.applyAvailablePromo").toString()).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.validatePromo").toString()).isEqualTo("false");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.amountBasedBankOffer").toString()).isEqualTo("true");
    }

    @Feature("PGP-52897")
    @Owner(MAYURI)
    @Parameters("isNativePlus")
    @Test(description = "Verify FPO response for Amount based Subvention and Bank offer")
    public void validateFPOWhenFetchUnifiedOffersContainsAmountBasedSubventionAndBankOfferForDeferredFlow() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        Double txnAmount = 1500.0;
        User user = userManager.getForWrite(Label.BASICTOKYC);
        FetchUnifiedOffers fetchUnifiedOffers = new FetchUnifiedOffers(true, true, null);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setEightDigitBinRequired("False")
                .setCardHashRequired("true")
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setFetchUnifiedOffers(fetchUnifiedOffers)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        if (fetchPaymentOptionsJson.getList("body.unifiedOffers").get(0) != null) {
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryTitle").toString()).isEqualTo("EMI Linked Offers");
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryType").toString()).isEqualTo("EMI_LINKED_OFFERS");
        }
        if (fetchPaymentOptionsJson.getList("body.unifiedOffers[0].categoryOfferDetails").get(0) != null) {
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].issuingBank").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].payMethod").toString()).isEqualTo("EMI");
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].benefitText").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].gratificationText").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].type").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].offerId").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].promocode").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].tnc").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].offerTitle").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].offerText").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].offerDescription").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].type").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].emiType").toString()).isEqualTo("SUBVENTION");
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].subventionType").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].offerId").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].validUpto").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].validFrom").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].tnc").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].offerDescription").toString()).isNotNull();
        }
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.applyAvailablePromo").toString()).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.validatePromo").toString()).isEqualTo("false");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.amountBasedBankOffer").toString()).isEqualTo("true");
        if (fetchPaymentOptionsJson.getList("body.emiBankDetails[0].emiTypeDetails").get(0) != null) {
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].issuingBank").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].bankLogo").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].payMethod").toString()).isEqualTo("EMI");
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].benefit").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].subventionTypes[0]").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].cardDetails.cardType").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].cardDetails.cardLabel").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].showKFSLink").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].showLendingConsent").toString()).isNotNull();
        }
    }

    @Feature("PGP-52897")
    @Owner(MAYURI)
    @Parameters("isNativePlus")
    @Test(description = "Verify FPO response for FetchUnifiedOffers contains no params")
    public void validateFPOWhenFetchUnifiedOffersContainsNoParamsForDeferredFlow() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        Double txnAmount = 1500.0;
        User user = userManager.getForWrite(Label.BASICTOKYC);
        FetchUnifiedOffers fetchUnifiedOffers = new FetchUnifiedOffers(null, null, null);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setEightDigitBinRequired("False")
                .setCardHashRequired("true")
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setFetchUnifiedOffers(fetchUnifiedOffers)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus").toString()).isEqualTo("F");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode").toString()).isEqualTo("1001");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg").toString()).isEqualTo("Request parameters are not valid");
    }

    @Feature("PGP-52897")
    @Owner(MAYURI)
    @Parameters("isNativePlus")
    @Test(description = "Verify FPO response for FetchUnifiedOffers contains amountBasedSubvention=false,amountBasedBankOffer=false,and no Item object")
    public void validateFPOWhenFetchUnifiedOffersContainsAmountBasedSubventionAndAmountBasedBankOfferFalseForDeferredFlow() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        Double txnAmount = 1500.0;
        User user = userManager.getForWrite(Label.BASICTOKYC);
        FetchUnifiedOffers fetchUnifiedOffers = new FetchUnifiedOffers(false, false, null);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setEightDigitBinRequired("False")
                .setCardHashRequired("true")
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setFetchUnifiedOffers(fetchUnifiedOffers)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus").toString()).isEqualTo("F");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode").toString()).isEqualTo("1001");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg").toString()).isEqualTo("Request parameters are not valid");
    }

    @Feature("PGP-52897")
    @Owner(MAYURI)
    @Parameters("isNativePlus")
    @Test(description = "Verify FPO response for FetchUnifiedOffers contains amountBasedSubvention=false,amountBasedBankOffer=false,and Item object having no value")
    public void validateFPOWhenFetchUnifiedOffersContainsAmountBasedSubventionAndAmountBasedBankOfferFalseAndItemNullForDeferredFlow() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        Double txnAmount = 1500.0;
        User user = userManager.getForWrite(Label.BASICTOKYC);
        List<FetchUnifiedOffers.Item> items = new ArrayList<>();
        FetchUnifiedOffers.Item item = new FetchUnifiedOffers.Item(null,null,null,null,null);
        //FetchUnifiedOffers.Item item = new FetchUnifiedOffers.Item("15036688","15036688","18084","6224","1500.0");
        items.add(item);
        FetchUnifiedOffers fetchUnifiedOffers = new FetchUnifiedOffers(false, false, items);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setEightDigitBinRequired("False")
                .setCardHashRequired("true")
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setFetchUnifiedOffers(fetchUnifiedOffers)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus").toString()).isEqualTo("F");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode").toString()).isEqualTo("1001");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg").toString()).isEqualTo("Request parameters are not valid");
    }

    @Feature("PGP-52897")
    @Owner(MAYURI)
    @Parameters("isNativePlus")
    @Test(description = "Verify FPO response for FetchUnifiedOffers contains only Item object having")
    public void validateFPOWhenFetchUnifiedOffersContainsOnlyItemObjectForDeferredFlow() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        Double txnAmount = 1500.0;
        User user = userManager.getForWrite(Label.BASICTOKYC);
        List<FetchUnifiedOffers.Item> items = new ArrayList<>();
        FetchUnifiedOffers.Item item = new FetchUnifiedOffers.Item("15036688","15036688","18084","6224","1500.0");
        items.add(item);
        FetchUnifiedOffers fetchUnifiedOffers = new FetchUnifiedOffers(null, null, items);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setEightDigitBinRequired("False")
                .setCardHashRequired("true")
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setFetchUnifiedOffers(fetchUnifiedOffers)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus").toString()).isEqualTo("F");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode").toString()).isEqualTo("1001");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg").toString()).isEqualTo("Request parameters are not valid");
    }

    @Feature("PGP-52897")
    @Owner(MAYURI)
    @Parameters("isNativePlus")
    @Test(description = "Verify FPO response when passed FetchUnifiedOffers(new) and FetchAllItemOffers(old) object , API response should be as per new flow")
    public void validateFPOWhenFetchUnifiedOffersAndFetchAllItemOffersObjectForDeferredFlow() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        Double txnAmount = 1500.0;
        User user = userManager.getForWrite(Label.BASICTOKYC);
        List<FetchUnifiedOffers.Item> items = new ArrayList<>();
        FetchUnifiedOffers.Item item = new FetchUnifiedOffers.Item("15036688","15036688","18084","6224","1500.0");
        items.add(item);
        FetchUnifiedOffers fetchUnifiedOffers = new FetchUnifiedOffers(false, false, items);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setEightDigitBinRequired("False")
                .setCardHashRequired("true")
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setFetchUnifiedOffers(fetchUnifiedOffers)
                .setFetchAllItemOffers(new fetchAllItemOffers() {{
                    setItems(Collections.singletonList(new SimplifiedSubvention.Item(
                            "15036688", "15036688", "18084", Collections.singletonList("6224"), "1",
                            "1500")));
                }})
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        if (fetchPaymentOptionsJson.getList("body.unifiedOffers").get(0) != null) {
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryTitle").toString()).isEqualTo("EMI Linked Offers");
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryType").toString()).isEqualTo("EMI_LINKED_OFFERS");
        }
        if (fetchPaymentOptionsJson.getList("body.unifiedOffers[0].categoryOfferDetails").get(0) != null) {
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].issuingBank").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].payMethod").toString()).isEqualTo("EMI");
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].benefitText").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].gratificationText").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].type").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].offerId").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].promocode").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].tnc").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].offerTitle").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].offerText").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].offerDescription").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].type").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].emiType").toString()).isEqualTo("SUBVENTION");
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].subventionType").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].offerId").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].validUpto").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].validFrom").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].tnc").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].offerDescription").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].gratificationText").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].type").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].offerId").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].promocode").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].tnc").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].offerTitle").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].offerText").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].offerDescription").toString()).isNotNull();
        }
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.applyAvailablePromo").toString()).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.validatePromo").toString()).isEqualTo("false");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.amountBasedBankOffer").toString()).isEqualTo("false");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.subventionDetails.subventionAmount").toString()).isEqualTo("1500.0");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.subventionDetails.amountBasedSubvention").toString()).isEqualTo("false");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.items[0].id").toString()).isEqualTo("15036688");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.items[0].productId").toString()).isEqualTo("15036688");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.items[0].brandId").toString()).isEqualTo("18084");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.items[0].categoryId").toString()).isEqualTo("6224");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.items[0].price").toString()).isEqualTo("1500.0");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiType").toString()).isEqualTo("SUBVENTION");

        if (fetchPaymentOptionsJson.getList("body.emiBankDetails[0].emiTypeDetails").get(0) != null) {
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].issuingBank").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].bankName").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].bankLogo").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].payMethod").toString()).isEqualTo("EMI");
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].benefit").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].subventionTypes[0]").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].cardDetails.cardType").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].cardDetails.cardLabel").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].showKFSLink").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].showLendingConsent").toString()).isNotNull();
        }


    }

    @Feature("PGP-52897")
    @Owner(MAYURI)
    @Parameters("isNativePlus")
    @Test(description = "Verify FPO response when passed FetchUnifiedOffers contains item based subvention + amount based promo\n")
    public void validateFPOWhenFetchUnifiedOffersObjectContainsItemBasedSubventionAndAmountBasedPromoForDeferredFlow() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        Double txnAmount = 1500.0;
        User user = userManager.getForWrite(Label.BASICTOKYC);
        List<FetchUnifiedOffers.Item> items = new ArrayList<>();
        FetchUnifiedOffers.Item item = new FetchUnifiedOffers.Item("15036688","15036688","18084","6224","1500.0");
        items.add(item);
        FetchUnifiedOffers fetchUnifiedOffers = new FetchUnifiedOffers(false, true, items);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setEightDigitBinRequired("False")
                .setCardHashRequired("true")
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setFetchUnifiedOffers(fetchUnifiedOffers)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        if (fetchPaymentOptionsJson.getList("body.unifiedOffers").get(0) != null) {
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryTitle").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryType").toString()).isNotNull();
        }
        if (fetchPaymentOptionsJson.getList("body.unifiedOffers[0].categoryOfferDetails").get(0) != null) {
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].issuingBank").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].payMethod").toString()).isEqualTo("EMI");
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].benefitText").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].gratificationText").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].type").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].offerId").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].promocode").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].tnc").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].offerTitle").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].offerText").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].offerDescription").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].type").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].emiType").toString()).isEqualTo("SUBVENTION");
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].subventionType").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].offerId").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].validUpto").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].validFrom").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].tnc").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[0].offerDescription").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].gratificationText").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].type").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].offerId").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].promocode").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].tnc").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].offerTitle").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].offerText").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].offerDescription").toString()).isNotNull();
        }
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.applyAvailablePromo").toString()).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.validatePromo").toString()).isEqualTo("false");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.amountBasedBankOffer").toString()).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.subventionDetails.subventionAmount").toString()).isEqualTo("1500.0");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.subventionDetails.amountBasedSubvention").toString()).isEqualTo("false");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.items[0].id").toString()).isEqualTo("15036688");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.items[0].productId").toString()).isEqualTo("15036688");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.items[0].brandId").toString()).isEqualTo("18084");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.items[0].categoryId").toString()).isEqualTo("6224");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.items[0].price").toString()).isEqualTo("1500.0");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiType").toString()).isEqualTo("SUBVENTION");

        if (fetchPaymentOptionsJson.getList("body.emiBankDetails[0].emiTypeDetails").get(0) != null) {
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].issuingBank").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].bankName").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].bankLogo").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].payMethod").toString()).isEqualTo("EMI");
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].benefit").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].subventionTypes[0]").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].cardDetails.cardType").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].cardDetails.cardLabel").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].showKFSLink").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiBankDetails[0].emiTypeDetails[0].showLendingConsent").toString()).isNotNull();
        }


    }

    @Feature("PGP-52897")
    @Owner(MAYURI)
    @Parameters("isNativePlus")
    @Test(description = "Verify FPO response when passed FetchUnifiedOffers contains 1.applyPaymentOffer=false\n" +
            "2.NOT passing amountBasedSubvention\n")
    public void validateFPOWhenFetchUnifiedOffersObjectContainsItemObjectAndAmountBasedBankOfferAndApplyPaymentOfferIsFalseForDeferredFlow() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        Double txnAmount = 1500.0;
        User user = userManager.getForWrite(Label.BASICTOKYC);
        List<FetchUnifiedOffers.Item> items = new ArrayList<>();
        FetchUnifiedOffers.Item item = new FetchUnifiedOffers.Item("15036688","15036688","18084","6224","1500.0");
        items.add(item);
        FetchUnifiedOffers fetchUnifiedOffers = new FetchUnifiedOffers(null, false, items);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setEightDigitBinRequired("False")
                .setCardHashRequired("true")
                .setFetchAllPaymentOffers("true")
                .setFetchUnifiedOffers(fetchUnifiedOffers)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        if (fetchPaymentOptionsJson.getList("body.unifiedOffers").get(0) != null) {
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryTitle").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryType").toString()).isNotNull();
        }
        if (fetchPaymentOptionsJson.getList("body.unifiedOffers[0].categoryOfferDetails").get(0) != null) {
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].issuingBank").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].payMethod").toString()).isEqualTo("EMI");
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].benefitText").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].gratificationText").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].type").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].offerId").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].promocode").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].tnc").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].offerTitle").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].offerText").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.bankOfferDetails[0].offerDescription").toString()).isNotNull();
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers[0].categoryOfferDetails[0].offerDetails.emiOfferDetails[]").toString()).hasSize(2);
        }
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.applyAvailablePromo").toString()).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.validatePromo").toString()).isEqualTo("false");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.amountBasedBankOffer").toString()).isEqualTo("false");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.items[0].id").toString()).isEqualTo("15036688");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.items[0].productId").toString()).isEqualTo("15036688");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.items[0].brandId").toString()).isEqualTo("18084");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.items[0].categoryId").toString()).isEqualTo("6224");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.items[0].price").toString()).isEqualTo("1500.0");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body").toString()).doesNotContain("emiBankDetails");
    }
    @Feature("PGP-54974")
    @Owner(PUSPA)
    @Parameters("isNativePlus")
    @Test(description = "Verify FPO response for saved card having unifiedoffersToken")
    public void validateUnifiedOfferToken_AmountBasedOffer() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        Double txnAmount = 200.0;
        User user = userManager.getForWrite(Label.BASICTOKYC);
        FetchUnifiedOffers fetchUnifiedOffers = new FetchUnifiedOffers(true, true, null);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setFetchUnifiedOffers(fetchUnifiedOffers)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        if (fetchPaymentOptionsJson.getList("body.merchantPayOption.savedInstruments").get(0) != null ) {
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments[0].unifiedOffers[0]").toString()).contains("unifiedOffersToken");
        }
    }
    @Feature("PGP-54974")
    @Owner(PUSPA)
    @Parameters("isNativePlus")
    @Test(description = "Verify txn from saved card for unifiedoffersToken")
    public void validateUnifiedOfferTokenE2ETxn_AmountBasedOffer() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        Double txnAmountt = 200.0;
        User user = userManager.getForRead(Label.ADVANCEDEPOSITLOWBALANCEUSER);
        String custId= user.custId();
        FetchUnifiedOffers fetchUnifiedOffers = new FetchUnifiedOffers(true, true, null);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmountt.toString())
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setFetchUnifiedOffers(fetchUnifiedOffers)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String unifiedOffersToken = fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments[0].unifiedOffers[0].unifiedOffersToken");
        String originalAmount = fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments[0].unifiedOffers[0].offerDetails[0].originalAmount");
        String payableAmount = fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments[0].unifiedOffers[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, unifiedOffersToken)
                .setTxnValue(originalAmount)
                .setPayableAmount(txnAmount)
                .setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId().toString(),txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("DEBIT_CARD")
                .setCardInfo("655f16081f966c656a0e05b4||123|")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        int paymentPromoCheckoutDataIdx=logs.indexOf("paymentPromoCheckoutData");
        String paymentPromoCheckoutDataLogs=logs.substring(paymentPromoCheckoutDataIdx);
        String status = PG2LogsValidationHelper.getKeyParameterValueFromLogs("status",paymentPromoCheckoutDataLogs);
        String promocode = PG2LogsValidationHelper.getKeyParameterValueFromLogs("promocode",paymentPromoCheckoutDataLogs);
        String promotext = PG2LogsValidationHelper.getKeyParameterValueFromLogs("promotext",paymentPromoCheckoutDataLogs);
        Assert.assertEquals(status,"1");
        Assert.assertEquals(promocode,"PROMO6U8498");
        Assert.assertEquals(promotext,"₹100.0 discount applied successfully.");
    }

    @Feature("PGP-54974")
    @Owner(PUSPA)
    @Parameters("isNativePlus")
    @Test(description = "Verify FPO response for saved card having unifiedoffersToken")
    public void validateUnifiedOfferToken_ItemBasedOffer() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        Double txnAmount = 200.0;
        User user = userManager.getForWrite(Label.BASICTOKYC);
        List<FetchUnifiedOffers.Item> items = new ArrayList<>();
        FetchUnifiedOffers.Item item = new FetchUnifiedOffers.Item("15036688","15036688","18084","6224","200.0");
        items.add(item);
        FetchUnifiedOffers fetchUnifiedOffers = new FetchUnifiedOffers(true, false, items);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setFetchUnifiedOffers(fetchUnifiedOffers)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        if (fetchPaymentOptionsJson.getList("body.merchantPayOption.savedInstruments").get(0) != null ) {
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments[0].unifiedOffers[0]").toString()).contains("unifiedOffersToken");
        }
    }
    @Feature("PGP-54974")
    @Owner(PUSPA)
    @Parameters("isNativePlus")
    @Test(description = "Verify txn from saved card for unifiedoffersToken")
    public void validateUnifiedOfferTokenE2ETxn_ItemBasedOffer() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        Double txnAmountt = 200.0;
        User user = userManager.getForRead(Label.ADVANCEDEPOSITLOWBALANCEUSER);
        String custId = user.custId();
        List<FetchUnifiedOffers.Item> items = new ArrayList<>();
        FetchUnifiedOffers.Item item = new FetchUnifiedOffers.Item("15036688", "15036688", "18084", "6224", "200.0");
        items.add(item);
        FetchUnifiedOffers fetchUnifiedOffers = new FetchUnifiedOffers(true, false, items);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmountt.toString())
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setFetchUnifiedOffers(fetchUnifiedOffers)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String unifiedOffersToken = fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments[0].unifiedOffers[0].unifiedOffersToken");
        String originalAmount = fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments[0].unifiedOffers[0].offerDetails[0].originalAmount");
        String payableAmount = fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments[0].unifiedOffers[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, unifiedOffersToken)
                .setTxnValue(originalAmount)
                .setPayableAmount(txnAmount)
                .setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId().toString(), txnToken, initTxnDTO.orderFromBody().toString()).
                setPaymentMode("DEBIT_CARD")
                .setCardInfo("655f16081f966c656a0e05b4||123|")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody().toString(), "ACQUIRING_CREATE_ORDER_AND_PAY");
        int paymentPromoCheckoutDataIdx = logs.indexOf("paymentPromoCheckoutData");
        String paymentPromoCheckoutDataLogs = logs.substring(paymentPromoCheckoutDataIdx);
        String status = PG2LogsValidationHelper.getKeyParameterValueFromLogs("status", paymentPromoCheckoutDataLogs);
        String promocode = PG2LogsValidationHelper.getKeyParameterValueFromLogs("promocode", paymentPromoCheckoutDataLogs);
        String promotext = PG2LogsValidationHelper.getKeyParameterValueFromLogs("promotext", paymentPromoCheckoutDataLogs);
        Assert.assertEquals(status, "1");
        Assert.assertEquals(promocode, "PROMO6U8498");
        Assert.assertEquals(promotext, "₹100.0 discount applied successfully.");
    }
    @Feature("PGP-55241")
    @Owner(PUSPA)
    @Parameters("isNativePlus")
    @Test(description = "Verify FPO response for all-offers true with Access Token")
    public void validateUnifiedOfferforAllOffers() throws Exception {
        Constants.MerchantType merchantType = MerchantType.EMI_DISCOVERY;
        String referenceId = String.valueOf(CommonHelpers.getRandomWithSize(15)) + "123";
        com.paytm.api.CreateToken createToken = new com.paytm.api.CreateToken(merchantType, referenceId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("ACCESS")
                .setMid(merchantType.getId())
                .setToken(AccessToken)
                .setReferenceId(referenceId)
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        fetchPaymentOption.getRequestSpecBuilder().addQueryParam("referenceId", referenceId);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, "AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).contains("FUNCTION", "ads/v2/offer/discovery?all-offers=true");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers").toString()).isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.amountBasedBankOffer").toString()).contains("true");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.subventionDetails.amountBasedSubvention").toString()).contains("true");

    }

    @Feature("PGP-55241")
    @Owner(PUSPA)
    @Parameters("isNativePlus")
    @Test(description = "Verify FPO response for all-offers true with SSO Token")
    public void validateUnifiedOfferforAllOffersSSO() throws Exception {
        Constants.MerchantType merchantType = MerchantType.EMI_DISCOVERY;
        User user = userManager.getForWrite(Label.BASICTOKYC);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, "AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).contains("FUNCTION", "ads/v2/offer/discovery?all-offers=true");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.unifiedOffers").toString()).isNotNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.promoDetails.amountBasedBankOffer").toString()).contains("true");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.subventionDetails.amountBasedSubvention").toString()).contains("true");

    }

    @Feature("PPSL-534")
    @Owner(PUSPA)
    @Parameters("isNativePlus")
    @Test(description = "Verify bin param with 9 digit in bulk apply req")
    public void validateBin9Digit() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        Double txnAmount = 200.0;
        User user = userManager.getForWrite(Label.SAVEDVPA);
        FetchUnifiedOffers fetchUnifiedOffers = new FetchUnifiedOffers(true, true, null);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setFetchUnifiedOffers(fetchUnifiedOffers)
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        if (fetchPaymentOptionsJson.getList("body.merchantPayOption.savedInstruments").get(0) != null) {
            String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,merchantType.getId(),"ads/v2/offer/bulkApply", "REQUEST");
            Assertions.assertThat(logs).contains("\"bin\":\"461015181\"");
        }
    }



    @Feature("PGP-58532")
    @Owner(LOKESH_SAXENA)
    @Parameters("isNativePlus")
    @Test(description = "Verify subvention amount should not come for multi item in FPO response under simplifiedUnifiedOffers")
    public void verifySubventionAmountNotPresentForMultiItem() throws Exception {
        Constants.MerchantType merchantType = MerchantType.PG2_AMEX_EMI;
        Double txnAmount = 400.0;
        User user = userManager.getForWrite(Label.SAVEDVPA);
        List<FetchUnifiedOffers.Item> items = new ArrayList<>();
        FetchUnifiedOffers.Item item1 = new FetchUnifiedOffers.Item("15036688","15036688","18084","6224","200.0");
        FetchUnifiedOffers.Item item2 = new FetchUnifiedOffers.Item("15036689","15036688","18084","6224","200.0");
        items.add(item1);
        items.add(item2);
        FetchUnifiedOffers fetchUnifiedOffers = new FetchUnifiedOffers(true, false, items);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setFetchUnifiedOffers(fetchUnifiedOffers)
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.subventionDetails").toString()).doesNotContain("subventionAmount");
    }
    @Feature("PGP-58532")
    @Owner(LOKESH_SAXENA)
    @Parameters("isNativePlus")
    @Test(description = "Verify subvention amount should not come for single item in FPO response under simplifiedUnifiedOffers")
    public void verifySubventionAmountNotPresentForSingleItem() throws Exception {
        Constants.MerchantType merchantType = MerchantType.PG2_AMEX_EMI;
        Double txnAmount = 400.0;
        User user = userManager.getForWrite(Label.SAVEDVPA);
        List<FetchUnifiedOffers.Item> items = new ArrayList<>();
        FetchUnifiedOffers.Item item1 = new FetchUnifiedOffers.Item("15036688","15036688","18084","6224","200.0");
        items.add(item1);
        FetchUnifiedOffers fetchUnifiedOffers = new FetchUnifiedOffers(true, false, items);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setFetchUnifiedOffers(fetchUnifiedOffers)
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedUnifiedOffers.subventionDetails").toString()).doesNotContain("subventionAmount");
    }

    @Feature("PGP-58574")
    @Owner(PUSPA)
    @Parameters("isNativePlus")
    @Test(description = "Verify FPO response for saved card do not have unifiedOfferToken if offer is not present. Flag should be enable:theia.skipUnifiedOffersTokenGeneration")
    public void validateUnifiedOfferTokenNotPresent_singleItemBasedOffer() throws Exception {
        Constants.MerchantType merchantType = MerchantType.UI_TEXTMSG_LOGINQR_SavedCard;
        Double txnAmount = 200.0;
        User user = userManager.getForWrite(Label.SAVEDVPA);
        List<FetchUnifiedOffers.Item> items = new ArrayList<>();
        FetchUnifiedOffers.Item item = new FetchUnifiedOffers.Item("15036688","15036688","18084","6224","200.0");
        items.add(item);
        FetchUnifiedOffers fetchUnifiedOffers = new FetchUnifiedOffers(true, false, items);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setFetchUnifiedOffers(fetchUnifiedOffers)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        if (fetchPaymentOptionsJson.getList("body.merchantPayOption.savedInstruments").get(0) != null ) {
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments[0].unifiedOffers[0]").toString()).doesNotContain("unifiedOffersToken");
        }
    }

    @Feature("PGP-58574")
    @Owner(PUSPA)
    @Parameters("isNativePlus")
    @Test(description = "Verify FPO response for saved card do not have unifiedOfferToken if offer is not present. Flag should be enable:theia.skipUnifiedOffersTokenGeneration")
    public void validateUnifiedOfferTokenNotPresent_MultiItemBasedOffer() throws Exception {
        Constants.MerchantType merchantType = MerchantType.UI_TEXTMSG_LOGINQR_SavedCard;
        Double txnAmount = 400.0;
        User user = userManager.getForWrite(Label.SAVEDVPA);
        List<FetchUnifiedOffers.Item> items = new ArrayList<>();
        FetchUnifiedOffers.Item item1 = new FetchUnifiedOffers.Item("15036688","15036688","18084","6224","200.0");
        FetchUnifiedOffers.Item item2 = new FetchUnifiedOffers.Item("15036689","15036688","18084","6224","200.0");
        items.add(item1);
        items.add(item2);
        FetchUnifiedOffers fetchUnifiedOffers = new FetchUnifiedOffers(true, false, items);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setFetchUnifiedOffers(fetchUnifiedOffers)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        if (fetchPaymentOptionsJson.getList("body.merchantPayOption.savedInstruments").get(0) != null ) {
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments[0].unifiedOffers[0]").toString()).doesNotContain("unifiedOffersToken");
        }
    }
}