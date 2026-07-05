package scripts.subscription.subscriptionFirstRequest.defaultFlows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.api.ProcessTransaction;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.TransactionType;
import com.paytm.apphelpers.AuthHelpers;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.*;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.*;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;

import static com.paytm.apphelpers.CommonHelpers.*;

@Owner("Tarun")
public class SubscriptionFieldValidations extends PGPBaseTest {
    private static final String CASHIER_PAGE_NOT_DISPLAYED = "Cashier page is not displayed";
    private static final String CASHIER_PAGE = "Paytm Secure Online Payment Gateway";
    private static final String SOMETHING_WENT_WRONG = "Something went wrong";
    private static final String OOPS_PAGE_NOT_DISPLAYED = "Oops page is not displayed";
    private String transactionType = null;
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private synchronized OrderDTO.Builder getOrderDTOBuilder(String transactionType, String theme) {
        if (transactionType.toString().toLowerCase().equals(TransactionType.PGOnly.toString().toLowerCase())) {
            return new OrderFactory.SubscriptionCC_DC(MerchantType.Subscription_PGOnly, theme);
        } else if (transactionType.toString().toLowerCase().equals(TransactionType.AddnPay.toString().toLowerCase())) {
            return new OrderFactory.SubscriptionPPI(MerchantType.SUBSCRIPTION_PPI, theme);
        } else if (transactionType.toString().toLowerCase().equals(TransactionType.WalletOnly.toString().toLowerCase())) {
            return new OrderFactory.SubscriptionWalletOnly(MerchantType.SUBSCRIPTION_WALLET_ONLY, theme);
        } else {
            throw new RuntimeException("Invalid Transaction Type: " + transactionType);
        }
    }

    @Parameters({"transactionType"})
    @BeforeClass
    public void setTransactionType(@Optional("PGOnly") String transactionType) {
        this.transactionType = transactionType;
    }

    @BeforeMethod
    public void disableScreenShotCapture(Method method, ITestResult testResult) {
        try {
            DriverManager.setCaptureScreenShot(false);
        } catch(Throwable e) {
            testResult.setStatus(ITestResult.SKIP);
            testResult.setThrowable(new SkipException(method.getName(), e));
        }
    }

    @Parameters({"theme"})
    @Test(description = "Validate that Cashier page should open 'Request Type' with blank value")
    public void PGP_10_validateCashierPageOnRequestTypeBlank(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).
                setREQUEST_TYPE("")
                .build();
        String response = PGPHelpers.executeProcessTransaction(orderDTO).asString();
        Assertions.assertThat(response).containsIgnoringCase(CASHIER_PAGE);
    }

    @Issue("PGP-10776")
    @Parameters({"theme"})
    @Test(description = "Validate OOPs page should be displayed for 'Request Type' with Invalid value")
    public void PGP_38_validateOopsPageOnRequestTypeInvalid(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).
                setREQUEST_TYPE("abcd").
                build();
        checkoutPage.createOrder(orderDTO);
        LostInSpacePage lostInSpacePage = new LostInSpacePage();
        lostInSpacePage.imgLostInSpace().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Validate TXN failure for passing 'Order Id' with Blank value")
    public void PGP_39_validateTxnFailureOnOrderIDBlank(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).
                setORDER_ID("").
                build();
        String response = PGPHelpers.executeProcessTransaction(orderDTO).asString();
        Assertions.assertThat(response).containsIgnoringCase("name=\'STATUS\' value=\'TXN_FAILURE\'");
        Assertions.assertThat(response).containsIgnoringCase("name=\'RESPMSG\' value=\'Invalid order id.\'");
        Assertions.assertThat(response).containsIgnoringCase("name=\'RESPCODE\' value=\'309\'");
    }

    @Parameters({"theme"})
    @Test(description = "Validate OOPS page for passing duplicate value in ORDER_ID Field.")
    public void PGP_40_validateOopsPageOnDuplicateOrderID(@Optional("merchant4") String theme) {
        String orderId = CommonHelpers.generateOrderId();
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme)
                .setORDER_ID(orderId)
                .build();
        OrderDTO orderDTO1 = getOrderDTOBuilder(transactionType, theme)
                .setORDER_ID(orderId)
                .build();
        PGPHelpers.executeProcessTransaction(orderDTO).asString();
        checkoutPage.createOrder(orderDTO1);
        ResponsePage responsePage = new ResponsePage();
        Assertions.assertThat(responsePage.textRespMsg().getText()).containsIgnoringCase("Duplicate order id");
        Assertions.assertThat(responsePage.textRespCode().getText()).containsIgnoringCase("325");
        Assertions.assertThat(responsePage.textStatus().getText()).containsIgnoringCase("TXN_FAILURE");
    }

    @Issue("PGP-10776")
    @Parameters({"theme"})
    @Test(description = "Validate OOPS page should be displayed for passing blank value in MID.")
    public void PGP_41_validateOopsPageForBlankMID(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).
                setMID("").
                build();
        checkoutPage.createOrder(orderDTO);
        OopsPage oopsPage = new OopsPage();
        oopsPage.imgOops().assertVisible();
    }

    @Issue("PGP-10776")
    @Parameters({"theme"})
    @Test(description = "Validate OOPS page should be displayed with expired licenced 'MID' value.")
    public void PGP_42_validateOopsPageForexpiredLicencedMid(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).
                setMID(PGPHelpers.getExpiredMID()).
                build();
        checkoutPage.createOrder(orderDTO);
        LostInSpacePage lostInSpacePage = new LostInSpacePage();
        lostInSpacePage.imgLostInSpace().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Validate cashier page displayed for invalid 'INDUSTRY_TYPE_ID'")
    public void PGP_43_validateCashierPageForIndustryTypeIdInvalid(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).
                setINDUSTRY_TYPE_ID("abcd").
                build();
        checkoutPage.createOrder(orderDTO);
        CashierPageFactory.getCashierPage(theme).assertContainsText("Payment Schedule");
    }

//    @Parameters({"theme"})
//    @Test(description = "Validate cashier page should be displayed for 'INDUSTRY_TYPE_ID' with blank value.", enabled = false)
    public void PGP_44_validateOopsPageForIndustryTypeIdBlank(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setINDUSTRY_TYPE_ID("").build();
        Response response = PGPHelpers.executeProcessTransaction(orderDTO);
        Assertions.assertThat(response.asString()).withFailMessage("Abondoned Transaction message not displayed.").contains("Abondoned Transaction.");
    }

    @Parameters({"theme"})
    @Test(description = "Validate 'Invalid CustID' error is displayed for 'CUST_ID' with blank value.")
    public void PGP_45_validateErrorForCustIdBlank(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setCUST_ID("").build();
        String response = PGPHelpers.executeProcessTransaction(orderDTO).asString();
        Assertions.assertThat(response).withFailMessage("Expecting \"Invalid CustID\" msg to be displayed in response but is not").containsIgnoringCase("Invalid CustID");
    }

    @Issue("PGP-10776")
    @Parameters({"theme"})
    @Test(description = "Validate Oops page should be displayed for 'WEB_SITE ' with blank value.")
    public void PGP_46_validateOopsPageForWebsiteBlank(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setWEBSITE("").setREQUEST_TYPE("DEFAULT").build();
        checkoutPage.createOrder(orderDTO);
        LostInSpacePage lostInSpacePage = new LostInSpacePage();
        lostInSpacePage.imgLostInSpace().assertVisible();
    }

    @Issue("PGP-10776")
    @Parameters({"theme"})
    @Test(description = "Validate Oops page should be displayed for 'WEB_SITE ' with Invalid value.")
    public void PGP_47_validateOopsPageForWebsiteInvalid(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setWEBSITE("abcd").build();
        checkoutPage.createOrder(orderDTO);
        LostInSpacePage lostInSpacePage = new LostInSpacePage();
        lostInSpacePage.imgLostInSpace().assertVisible();
    }

    @Issue("PGP-10776")
    @Parameters({"theme"})
    @Test(description = "Validate Oops page should be displayed for 'CHANNEL_ID' with blank value.")
    public void PGP_463_validateOopsPageForChannelIdBlank(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setCHANNEL_ID("").build();
        String response = PGPHelpers.executeProcessTransaction(orderDTO).asString();
        Assertions.assertThat(response).withFailMessage(OOPS_PAGE_NOT_DISPLAYED).containsIgnoringCase(SOMETHING_WENT_WRONG);
    }

    @Issue("PGP-10776")
    @Parameters({"theme"})
    @Test(description = "Validate Oops page should be displayed for 'TXN_AMOUNT' with blank value.")
    public void PGP_462_validateOopsPageForTransactionAmountBlank(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setTXN_AMOUNT("").build();
        checkoutPage.createOrder(orderDTO);
        LostInSpacePage lostInSpacePage = new LostInSpacePage();
        lostInSpacePage.imgLostInSpace().assertVisible();
    }

    @Issue("PGP-10776")
    @Parameters({"theme"})
    @Test(description = "Validate Oops page should be displayed for 'TXN_AMOUNT' with non-numeric value.")
    public void PGP_48_validateOopsPageForTransactionAmountNonNumeric(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).
                setTXN_AMOUNT("abcd").
                build();
        ObjectMapper oMapper = new ObjectMapper();
        HashMap<String, String> formData = oMapper.convertValue(orderDTO, HashMap.class);
        ProcessTransaction processTransaction = new ProcessTransaction();
        processTransaction.getRequestSpecBuilder().addFormParams(formData);
        checkoutPage.createOrder(orderDTO);
        LostInSpacePage lostInSpacePage = new LostInSpacePage();
        lostInSpacePage.imgLostInSpace().assertVisible();
    }

//    @Parameters({"theme"})
//    @Test(description = "Validate Oops page should be displayed for 'Subscription Service ID' with blank value.", enabled = false)
    public void PGP_11_validateOopsPageForSubsIdBlank(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setSUBS_SERVICE_ID("").build();
        ObjectMapper oMapper = new ObjectMapper();
        HashMap<String, String> formData = oMapper.convertValue(orderDTO, HashMap.class);
        ProcessTransaction processTransaction = new ProcessTransaction();
        processTransaction.getRequestSpecBuilder().addFormParams(formData);
        String response = processTransaction.execute().asString();
        Assertions.assertThat(response).withFailMessage(OOPS_PAGE_NOT_DISPLAYED).containsIgnoringCase(SOMETHING_WENT_WRONG);
    }

    @Issue("PGP-10776")
    @Parameters({"theme"})
    @Test(description = "Validate Oops page should be displayed for 'Subscription Amount' with blank value")
    public void PGP_12_validateOopsPageForSubsAmountBlank(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setTXN_AMOUNT("").build();
        checkoutPage.createOrder(orderDTO);
        LostInSpacePage lostInSpacePage = new LostInSpacePage();
        lostInSpacePage.imgLostInSpace().assertVisible();
    }

    @Issue("PGP-10776")
    @Parameters({"theme"})
    @Test(description = "Validate Oops page should be displayed for 'Subscription Amount' with invalid value")
    public void PGP_51_validateOopsPageForSubsAmountInvalid(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setTXN_AMOUNT("abcd").build();
        checkoutPage.createOrder(orderDTO);
        LostInSpacePage lostInSpacePage = new LostInSpacePage();
        lostInSpacePage.imgLostInSpace().assertVisible();
    }

    @Issue("PGP-10776")
    @Parameters({"theme"})
    @Test(description = "Validate Oops page should be displayed for 'Subscription Amount' set to constant value 'FIX'.")
    public void PGP_52_validateOopsPageForSubsAmountFix(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setTXN_AMOUNT("FIX").build();
        checkoutPage.createOrder(orderDTO);
        LostInSpacePage lostInSpacePage = new LostInSpacePage();
        lostInSpacePage.imgLostInSpace().assertVisible();
    }

    @Issue("PGP-10776")
    @Parameters({"theme"})
    @Test(description = "Validate Oops page should be displayed for 'Subscription Frequency' with blank values.")
    public void PGP_14_validateOopsPageForSubsFrequencyBlank(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setSUBS_FREQUENCY("").build();
        checkoutPage.createOrder(orderDTO);
        LostInSpacePage lostInSpacePage = new LostInSpacePage();
        lostInSpacePage.imgLostInSpace().assertVisible();
    }

    @Issue("PGP-10776")
    @Parameters({"theme"})
    @Test(description = "Validate Oops page should be displayed for 'Subscription Frequency Unit' with Invalid values.")
    public void PGP_54_validateOopsPageForSubsFrequencyUnitInvalid(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setSUBS_FREQUENCY_UNIT("abcd").build();
        checkoutPage.createOrder(orderDTO);
        OopsPage oopsPage = new OopsPage();
        oopsPage.imgOops().assertVisible();
    }

    @Issue("PGP-10776")
    @Parameters({"theme"})
    @Test(description = "Validate Oops page should be displayed for 'Subscription Frequency Unit' with blank values.")
    public void PGP_15_validateOopsPageForSubsFrequencyUnitBlank(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setSUBS_FREQUENCY_UNIT("").build();
        checkoutPage.createOrder(orderDTO);
        OopsPage oopsPage = new OopsPage();
        oopsPage.imgOops().assertVisible();
    }

    @Issue("PGP-10776")
    @Parameters({"theme"})
    @Test(description = "Validate Oops page should be displayed for 'Subscription Frequency' set with Invalid values.")
    public void PGP_53_validateOopsPageForSubsFrequencyInvalid(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setSUBS_FREQUENCY("abcd").build();
        checkoutPage.createOrder(orderDTO);
        LostInSpacePage lostInSpacePage = new LostInSpacePage();
        lostInSpacePage.imgLostInSpace().assertVisible();
    }

    @Issue("PGP-10776")
    @Parameters({"theme"})
    @Test(description = "Validate Oops page should be displayed for 'SUBS_ENABLE_RETRY' with blank values.")
    public void PGP_55_validateOopsPageForSubsEnableRetryBlank(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setSUBS_ENABLE_RETRY("").build();
        checkoutPage.createOrder(orderDTO);
        LostInSpacePage lostInSpacePage = new LostInSpacePage();
        lostInSpacePage.imgLostInSpace().assertVisible();
    }

    @Issue("PGP-10776")
    @Parameters({"theme"})
    @Test(description = "Validate Oops page should be displayed for 'Subscription Start Date' with blank value.")
    public void PGP_16_validateOopsPageForSubsStartDateBlank(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setSUBS_START_DATE("").build();
        checkoutPage.createOrder(orderDTO);
        LostInSpacePage lostInSpacePage = new LostInSpacePage();
        lostInSpacePage.imgLostInSpace().assertVisible();
    }

    @Issue("PGP-10776")
    @Parameters({"theme"})
    @Test(description = "Validate Oops page should be displayed when 'Subscription Start Date' > 'Subscription Expiry Date'.")
    public void PGP_56_validateOopsPageWhenSubsStartDateGTSubsExpiryDate(@Optional("merchant4") String theme) {
        String endDate = getDate(addDays(new Date(), 1), "yyyy-MM-dd");
        String startDate = getDate(addDays(new Date(), 2), "yyyy-MM-dd");
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).
                setSUBS_START_DATE(startDate).
                setSUBS_EXPIRY_DATE(endDate).
                build();
        checkoutPage.createOrder(orderDTO);
        LostInSpacePage lostInSpacePage = new LostInSpacePage();
        lostInSpacePage.imgLostInSpace().assertVisible();
    }

    @Issue("PGP-10776")
    @Parameters({"theme"})
    @Test(description = "Validate Oops page should be displayed when 'Subscription Start Date' = 'Subscription Expiry Date'.")
    public void PGP_57_validateOopsPageWhenSubsStartDateEqualsSubsExpiryDate(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).
                setSUBS_EXPIRY_DATE(getDate(addDays(new Date(), 1), "yyyy-MM-dd")).
                setSUBS_START_DATE(getDate(addDays(new Date(), 1), "yyyy-MM-dd"))
                .build();
        String response = PGPHelpers.executeProcessTransaction(orderDTO).asString();
        Assertions.assertThat(response).withFailMessage(OOPS_PAGE_NOT_DISPLAYED).containsIgnoringCase(SOMETHING_WENT_WRONG);
    }

    @Parameters({"theme"})
    @Test(description = "Validate \"Invalid Subs Start date\" be displayed when 'Subscription Start Date' < Current Date.")
    public void PGP_58_validateOopsPageForSubsStartDateLTCurrentDate(@Optional("merchant4") String theme) {
        String startDate = getDate(subtractDays(new Date(), 1), "yyyy-MM-dd");
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).
                setSUBS_START_DATE(startDate).
                build();
        String response = PGPHelpers.executeProcessTransaction(orderDTO).asString();
        Assertions.assertThat(response).withFailMessage("Expecting \"Invalid Subs Start date\" msg to be displayed but is not ").containsIgnoringCase("Invalid Subs Start date");
    }

    @Issue("PGP-10776")
    @Parameters({"theme"})
    @Test(description = "Validate Oops page should be displayed when 'Subscription Expiry Date' = 'Subscription Start Date'.")
    public void PGP_59_validateOopsPageForSubsExpiryDateEqualsStartDate(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).
                setSUBS_EXPIRY_DATE(getDate(new Date(), "yyyy-MM-dd")).
                build();
        String response = PGPHelpers.executeProcessTransaction(orderDTO).asString();
        Assertions.assertThat(response).withFailMessage(OOPS_PAGE_NOT_DISPLAYED).containsIgnoringCase(SOMETHING_WENT_WRONG);
    }

    @Parameters({"theme"})
    @Test(description = "Validate Cashier page should be displayed when SAVECARD_ID = Blank and Call type =Blank.")
    public void PGP_61_validateCashierPageForSaveCardIdBlankAndCallTypeBlank(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).
                setSTORE_CARD("").setCONNECTION_TYPE("").build();
        String response = PGPHelpers.executeProcessTransaction(orderDTO).asString();
        Assertions.assertThat(response).withFailMessage(CASHIER_PAGE_NOT_DISPLAYED).containsIgnoringCase(CASHIER_PAGE);
    }

    @Parameters({"theme"})
    @Test(description = "Validate Cashier page should be displayed for 'SSO TOKEN set with blank value.")
    public void PGP_64_validateCashierPageForSsoTokenBlank(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setSSO_TOKEN("").build();
        String response = PGPHelpers.executeProcessTransaction(orderDTO).asString();
        Assertions.assertThat(response).withFailMessage(CASHIER_PAGE_NOT_DISPLAYED).containsIgnoringCase(CASHIER_PAGE);
    }

    @Parameters({"theme"})
    @Test(description = "Validate Oops page should be displayed for 'SSO TOKEN set with Invalid value.")
    public void PGP_65_validateOopsPageForSsoTokenInvalid(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setSSO_TOKEN("abcd").build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateStatus("TXN_FAILURE")
                .validateRespMsg(Constants.ResponseCode.INVALID_SSO_TOKEN.getRespMsg())
                .validateRespCode(Constants.ResponseCode.INVALID_SSO_TOKEN.getRespCode())
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate OOPS page displayed for 'SSO TOKEN set with expired value.")
    public void PGP_66_validateCashierPageForSsoTokenExpiredValue(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String ssoToken = user.ssoToken();
        try {
            AuthHelpers.logout(ssoToken);
        } finally {
            user.purge();
        }
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme)
                .setSSO_TOKEN(ssoToken)
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage
                .validateRespCode(Constants.ResponseCode.INVALID_SSO_TOKEN.getRespCode())
                .validateRespMsg(Constants.ResponseCode.INVALID_SSO_TOKEN.getRespMsg())
                .validateStatus("TXN_FAILURE")
                .assertAll();
    }

    @Issue("PGP-10776")
    @Parameters({"theme"})
    @Test(description = "Validate Oops page should be displayed for 'Subscription Max Amount' set with blank value.")
    public void PGP_13_validateOopsPageForSubsMaxAmountBlank(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setSUBS_MAX_AMOUNT("").build();
        checkoutPage.createOrder(orderDTO);
        LostInSpacePage lostInSpacePage = new LostInSpacePage();
        lostInSpacePage.imgLostInSpace().assertVisible();
    }

    @Issue("PGP-10776")
    @Parameters({"theme"})
    @Test(description = "Validate Oops page should be displayed for 'Subscription Max Amount' set with invalid value.")
    public void PGP_68_validateOopsPageForSubsMaxAmountInvalid(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setSUBS_MAX_AMOUNT("abcd").build();
        checkoutPage.createOrder(orderDTO);
        LostInSpacePage lostInSpacePage = new LostInSpacePage();
        lostInSpacePage.imgLostInSpace().assertVisible();
    }

    @Issue("PGP-10776")
    @Parameters({"theme"})
    @Test(description = "Validate Oops page should be displayed for 'Subscription Grace Days' with blank value.")
    public void PGP_17_validateOopsPageForSubsGraceDaysBlank(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setSUBS_GRACE_DAYS("").build();
        checkoutPage.createOrder(orderDTO);
        LostInSpacePage lostInSpacePage = new LostInSpacePage();
        lostInSpacePage.imgLostInSpace().assertVisible();
    }

    @Issue("PGP-10776")
    @Parameters({"theme"})
    @Test(description = "Validate Oops page should be displayed for 'Subscription Grace Days' set with invalid value.")
    public void PGP_69_validateOopsPageForSubsGraceDaysInvalid(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setSUBS_GRACE_DAYS("abcd").build();
        checkoutPage.createOrder(orderDTO);
        LostInSpacePage lostInSpacePage = new LostInSpacePage();
        lostInSpacePage.imgLostInSpace().assertVisible();
    }

    @Issue("PGP-10776")
    @Parameters({"theme"})
    @Test(description = "Validate Oops page should be displayed for 'Subscription Retry Enabled' set with blank values")
    public void PGP_18_validateOopsPageForSubsRetryEnabledBlank(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setSUBS_ENABLE_RETRY("").build();
        checkoutPage.createOrder(orderDTO);
        LostInSpacePage lostInSpacePage = new LostInSpacePage();
        lostInSpacePage.imgLostInSpace().assertVisible();
    }

    @Issue("PGP-10776")
    @Parameters({"theme"})
    @Test(description = "Validate Oops page should be displayed for 'Subscription Retry Enabled' set with Invalid values")
    public void PGP_97_validateOopsPageForSubsRetryEnabledInvalid(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setSUBS_ENABLE_RETRY("abcd").build();
        checkoutPage.createOrder(orderDTO);
        LostInSpacePage lostInSpacePage = new LostInSpacePage();
        lostInSpacePage.imgLostInSpace().assertVisible();
    }

    @Issue("PGP-10776")
    @Parameters({"theme"})
    @Test(description = "Validate Oops page should be displayed for 'Subscription Expiry Date' set with blank value.")
    public void PGP_19_validateOopsPageForSubsExpiryDateBlank(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setSUBS_EXPIRY_DATE("").build();
        checkoutPage.createOrder(orderDTO);
        LostInSpacePage lostInSpacePage = new LostInSpacePage();
        lostInSpacePage.imgLostInSpace().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Validate Cashier page should be displayed for 'SUBS_PAYMENT_MODE' set with blank value.")
    public void PGP_20_validateCashierPageForSubsPaymentModeBlank(@Optional("merchant4") String theme) throws Exception {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setSUBS_PAYMENT_MODE("")
                .build();
        checkoutPage.createOrder(orderDTO);
        checkoutPage.waitUntilLoads();
        Assertions.assertThat(DriverManager.getDriver().getTitle())
                .as("CashierPage title mismatch")
                .isEqualToIgnoringCase("Paytm Secure Online Payment Gateway");
    }

    @Issue("PGP-10776")
    @Parameters({"theme"})
    @Test(description = "Validate Oops page should be displayed for 'SUBS_PAYMENT_MODE' set with invalid value.")
    public void PGP_98_validateOopsPageForSubsPaymentModeInvalid(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setSUBS_PAYMENT_MODE("abcd").build();
        checkoutPage.createOrder(orderDTO);
        LostInSpacePage lostInSpacePage = new LostInSpacePage();
        lostInSpacePage.imgLostInSpace().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Validate Cashier page should be displayed for 'SUBS_PPI_ONLY' set with blank value.")
    public void PGP_21_validateCashierPageForSubsPPIOnlyBlank(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setSUBS_PPI_ONLY("").build();
        String response = PGPHelpers.executeProcessTransaction(orderDTO).asString();
        Assertions.assertThat(response).withFailMessage(CASHIER_PAGE_NOT_DISPLAYED).containsIgnoringCase(CASHIER_PAGE);
    }

    @Parameters({"theme"})
    @Test(description = "Validate Cashier page should be displayed for 'SUBS_PPI_ONLY' set with invalid value.")
    public void PGP_99_validateCashierPageForSubsPPIOnlyInvalid(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = getOrderDTOBuilder(transactionType, theme).setSUBS_PPI_ONLY("abcd").build();
        String response = PGPHelpers.executeProcessTransaction(orderDTO).asString();
        Assertions.assertThat(response).withFailMessage(CASHIER_PAGE_NOT_DISPLAYED).containsIgnoringCase(CASHIER_PAGE);
    }
}

