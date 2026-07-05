package scripts.otpInject;

import com.paytm.LocalConfig;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.pages.*;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.fest.assertions.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.appconstants.Constants.Owner.GAGANDEEP;
import static com.paytm.appconstants.Constants.PayMode.CC;
import static com.paytm.appconstants.Constants.PayMode.DC;
import static com.paytm.appconstants.Constants.ValidationType.NON_EMPTY;

@Owner("Tarun")
@Epic(Constants.Sprint.OTP_INJECTION)
@Feature("PGP-21753")


//TODO debugging with Abhishek P + Bin Issue
public class OTPInjection extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
    private final Constants.MerchantType otpInjectMerchant = Constants.MerchantType.OTP_INJECT;
    private final IntermediateOTPPage intermediateOTPPage = new IntermediateOTPPage("int-otp-page");
    private final String INVALID_OTP = "111111";
    private void validateRedirectFlowEnabled(Constants.MerchantType merchantType) {

        pre_requisite:
        {
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "MERCHANT_REDIRECT_FLOW_ENABLED", "Y");
        }

    }

    private void validateRedirectFlowDisabled(Constants.MerchantType merchantType) {

        pre_requisite:
        {
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "MERCHANT_REDIRECT_FLOW_ENABLED", "N");
        }

    }

    private String fetchFromRedirectChannelConfig(String acquiringBankCode, String cardSchema, String cardType, String issuingBankCode,String column)
    {
        String query = "SELECT * FROM PGPDB.redirect_channel_config WHERE acq_bank_code = '" +acquiringBankCode+ "' and card_schema = '" +cardSchema+"'  and card_type='" + cardType + "' and issuing_bank_code  =  '"+issuingBankCode+"' ;";
        return  DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, query).get(0).get(column).toString();
    }

    private String fetchFromIssuingBankConfig(String issuingBankCode,String payMode,String cardType, String column)
    {
        String query = "SELECT * FROM PGPDB.issuing_bank_config WHERE issuing_bank_code = '" +issuingBankCode+ "' and payment_mode = '" +payMode+"'  and card_type='" + cardType + "';";
        return  DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, query).get(0).get(column).toString();
    }

    @Test(description = "Verify the success txn using SBI VISA card from direct page after sucessfull redirection at instaredirect end")
    @Parameters({"theme"})
    public void txnUsingSBIVISA(@Optional("enhancedweb") String theme)
    {
        validateRedirectFlowEnabled(otpInjectMerchant);
        OrderDTO orderDTO = new OrderFactory.PGOnly(otpInjectMerchant, theme)
                .build();
        Assertions.assertThat(fetchFromRedirectChannelConfig(Constants.Gateway.KOTAK.toString(), "VISA", "DC", Constants.Bank.SBI.toString(),"status")).isEqualTo("true");
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setDebitCardNumber(PaymentDTO.SBI_DEBIT_CARD);
        cashierPage.payBy(DC,paymentDTO);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.submitOtp(PaymentDTO.OTP);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.KOTAK.toString())
                .validateBankName(Constants.Bank.SBI_FULL.toString())
                .validateCheckSum(otpInjectMerchant.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(NON_EMPTY)
                .validateBankTxnId(NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.KOTAK.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.SBI_FULL.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }


    @Test(description = "Verify that transaction fails if response to bankFormReq from bank end is fail.")
    @Parameters({"theme"})
    public void txnFailIfRespFromBankFail(@Optional("enhancedweb_revamp") String theme)
    {
        Constants.MerchantType merchantWithIncorrectURL = Constants.MerchantType.INVALID_WEBPAY_URL;
        validateRedirectFlowEnabled(merchantWithIncorrectURL);
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantWithIncorrectURL, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(CC);
        cashierPage.waitUntilContainsText("Failed to retrieve Bank Form");

        Assertions.assertThat(cashierPage.bankFormErrorMsg()).as("Error message displayed on UI is incorrect")
                        .isEqualTo("Failed to retrieve Bank Form");

    }

    @Test(description = "Verify transaction should get success directly(with mock) with value of 'MERCHANT_REDIRECT_FLOW_ENABLED' is set to 'N' in merchant_preference_info table in PGPDB")
    @Parameters({"theme"})
    public void successIfPrefN(@Optional("enhancedweb") String theme)
    {
        Constants.MerchantType redirectFlowDisabled = Constants.MerchantType.PGOnly;
        validateRedirectFlowDisabled(redirectFlowDisabled);
        OrderDTO orderDTO = new OrderFactory.PGOnly(redirectFlowDisabled, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(Constants.MerchantType.PGOnly.getKey())
                .validateResponsePageParameters()
                .assertAll();

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

    @Test(description = "Verify bank otp page is visible(auto submit from browser should fail on insta redirect) if 'MERCHANT_REDIRECT_FLOW_ENABLED' is set to 'Y' in merchant_preference_info table in PGPDB but NKMB/MASTER/DC/ICICI combination does not exists in 'redirect_channel_config' table in PGPDB")
    @Parameters({"theme"})
    public void successTxnIfCombinationDoesntExist(@Optional("enhancedweb") String theme)
    {
        Constants.MerchantType otpInject = Constants.MerchantType.OTP_INJECT;
        validateRedirectFlowEnabled(otpInject);

        //Query should result empty set
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> {
            fetchFromRedirectChannelConfig(Constants.Gateway.KOTAK.toString(), "MASTER", "DC", Constants.Bank.ICICI.toString(),"status");})
                .isInstanceOf(IndexOutOfBoundsException.class)
                .hasMessageContaining("Index: 0, Size: 0");

        OrderDTO orderDTO = new OrderFactory.PGOnly(otpInject, theme)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_DEBIT_CARD_NUMBER);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(CC,paymentDTO);

        intermediateOTPPage.otpPageTextbox(PaymentDTO.OTP);
        intermediateOTPPage.otpPageSubmitButton().click();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateBankTxnId(NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.KOTAK.toString())
                .validateBankName(Constants.Gateway.ICICO.toString())//ICICI Bank
                .validateCheckSum(otpInject.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.KOTAK.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Gateway.ICICO.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }

    @Test(description = "Verify success txn without direct page that if pref is enabled on merchant and entry exist in redirect channel config table but status is marked as 0(Inactive)")
    @Parameters({"theme"})
    public void successTxnIfStatusIsInactive(@Optional("enhancedweb") String theme)
    {
        Constants.MerchantType otpInject = Constants.MerchantType.OTP_INJECT;
        validateRedirectFlowEnabled(otpInject);

        Assertions.assertThat(fetchFromRedirectChannelConfig(Constants.Gateway.KOTAK.toString(), "VISA", "CC", "HDFC","status")).isEqualTo("false");
        OrderDTO orderDTO = new OrderFactory.PGOnly(otpInject, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(CC);

        intermediateOTPPage.otpPageTextbox(PaymentDTO.OTP);
        intermediateOTPPage.otpPageSubmitButton().click();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.KOTAK.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(otpInject.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(NON_EMPTY)
                .validateBankTxnId(NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.KOTAK.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }

    @Test(description = "Verify if everything is set correctly, on direct page issuing bank ,bank logo , is resend should come from issuing_bank_config and txn should get succeed after one resend")
    @Parameters({"theme"})
    public void issuingBankConfig(@Optional("enhancedweb_revamp") String theme)
    {
        Constants.MerchantType otpInject = Constants.MerchantType.OTP_INJECT;
        validateRedirectFlowEnabled(otpInject);

        Assertions.assertThat(fetchFromRedirectChannelConfig(Constants.Gateway.KOTAK.toString(), "VISA", "DC", Constants.Bank.SBI.toString(),"status")).isEqualTo("true");

        OrderDTO orderDTO = new OrderFactory.PGOnly(otpInject, theme)
                .build();

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setDebitCardNumber(PaymentDTO.SBI_DEBIT_CARD);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(DC,paymentDTO);

        directBankOTPPage.waitUntilLoads();
        String bankLogoURL = fetchFromIssuingBankConfig(Constants.Bank.SBI.toString(),"DC","DC","image_url");
        String issuingBank = fetchFromIssuingBankConfig(Constants.Bank.SBI.toString(),"DC","DC","bank_name");
        String isResendAllowed = fetchFromIssuingBankConfig(Constants.Bank.SBI.toString(),"DC","DC","is_resend");

        Assertions.assertThat(directBankOTPPage.BankLogo().getAttribute("src")).as("Bank LOGO URL not matching from DB").isEqualTo(bankLogoURL);
        Assertions.assertThat(issuingBank).as("Issuing bank mismatch").isEqualTo("SBI Debit Card");
        Assertions.assertThat(isResendAllowed).as("Resend not allowed on this configuration").isEqualTo("1");
        directBankOTPPage.ResendOTPLink().assertVisible();
        directBankOTPPage.ResendOTPLink().click();
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.submitOtp(PaymentDTO.OTP);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.KOTAK.toString())
                .validateBankName(Constants.Bank.SBI_FULL.toString())
                .validateCheckSum(otpInject.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(NON_EMPTY)
                .validateBankTxnId(NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.KOTAK.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.SBI_FULL.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }

    @Test(description = "Verify if everything is set correctly, on direct page issuing bank ,bank logo ,is resend should come from issuing_bank_config")
    @Parameters({"theme"})
    public void issuingBankConfigResendNo(@Optional("enhancedweb") String theme)
    {
        Constants.MerchantType otpInject = Constants.MerchantType.OTP_INJECT;
        validateRedirectFlowEnabled(otpInject);

        Assertions.assertThat(fetchFromRedirectChannelConfig(Constants.Gateway.KOTAK.toString(), "VISA", "CC", Constants.Bank.ICICI.toString(),"status")).isEqualTo("true");

        OrderDTO orderDTO = new OrderFactory.PGOnly(otpInject, theme)
                .build();

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CREDIT_CARD_NUMBER);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(CC,paymentDTO);

        directBankOTPPage.waitUntilLoads();
        String isResendAllowed = fetchFromIssuingBankConfig(Constants.Bank.ICICI.toString(),"CC","CC","is_resend");

        Assertions.assertThat(isResendAllowed).as("Resend not allowed on this configuration").isEqualTo("false");
        directBankOTPPage.ResendOTPLink().assertNotVisible();

    }

    @Test(description = "Validate cancel button should be visible , user is not able to see ResendOTP if count exhaust the max_OTP_RETRY limit ")
    @Parameters({"theme"})
    public void maxResendOTPLimit(@Optional("enhancedweb") String theme) {
        Constants.MerchantType otpInject = Constants.MerchantType.OTP_INJECT;
        validateRedirectFlowEnabled(otpInject);

        Assertions.assertThat(fetchFromRedirectChannelConfig(Constants.Gateway.KOTAK.toString(), "VISA", "DC", Constants.Bank.SBI.toString(), "status")).isEqualTo("true");

        OrderDTO orderDTO = new OrderFactory.PGOnly(otpInject, theme)
                .build();

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setDebitCardNumber(PaymentDTO.SBI_DEBIT_CARD);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(DC, paymentDTO);

        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.cancel().assertVisible();
        int maxResendCount = Integer.parseInt(fetchFromIssuingBankConfig(Constants.Bank.SBI.toString(), "DC", "DC", "max_resend_otp_count"));

        for (int i = 0; i < maxResendCount; i++) {
            directBankOTPPage.ResendOTPLink().click();
            directBankOTPPage.VerifyRequestMsg("OTP has been sent to your registered mobile number");
        }

        directBankOTPPage.ResendOTPLink().assertNotVisible();
        directBankOTPPage.submitOtp(PaymentDTO.OTP);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.KOTAK.toString())
                .validateBankName(Constants.Bank.SBI_FULL.toString())
                .validateCheckSum(otpInject.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(NON_EMPTY)
                .validateBankTxnId(NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.KOTAK.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.SBI_FULL.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }

        // TC failing , raised observation to functional team
    @Test(description = "Verify that expiry alert is displayed to user after time mentioned in 'page_expiry_time' column for that issuing bank in 'issuing_bank_config' table in PGPDB")
    @Parameters({"theme"})
    public void expiryTimeValidation(@Optional("enhancedweb") String theme)
    {
        Constants.MerchantType otpInject = Constants.MerchantType.OTP_INJECT_RETRY;
        validateRedirectFlowEnabled(otpInject);

        Assertions.assertThat(fetchFromRedirectChannelConfig(Constants.Gateway.KOTAK.toString(), "VISA", "CC", Constants.Bank.ICICI.toString(),"status")).isEqualTo("true");

        OrderDTO orderDTO = new OrderFactory.PGOnly(otpInject, theme)
                .build();
        int pageExpiryTime = Integer.valueOf(fetchFromIssuingBankConfig(Constants.Bank.ICICI.toString(),"CC","CC","page_expiry_time"));

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CREDIT_CARD_NUMBER);
        paymentDTO.setDebitCardNumber(PaymentDTO.SBI_DEBIT_CARD);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(CC,paymentDTO);
        try {
            Thread.sleep(pageExpiryTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Waiting completed....");

        cashierPage.waitUntilLoads();
        cashierPage.payBy(DC,paymentDTO);

        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.submitOtp(PaymentDTO.OTP);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.KOTAK.toString())
                .validateBankName(Constants.Bank.SBI_FULL.toString())
                .validateCheckSum(otpInject.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(NON_EMPTY)
                .validateBankTxnId(NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.KOTAK.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.SBI_FULL.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }

    @Test(description = "Verify after invalid otp , txn get succesful after entering correct otp")
    @Parameters({"theme"})
    public void invalidOTPScenario(@Optional("enhancedweb") String theme)
    {
        Constants.MerchantType otpInject = Constants.MerchantType.OTP_INJECT;
        validateRedirectFlowEnabled(otpInject);

        Assertions.assertThat(fetchFromRedirectChannelConfig(Constants.Gateway.KOTAK.toString(), "VISA", "DC", Constants.Bank.SBI.toString(),"status")).isEqualTo("true");

        OrderDTO orderDTO = new OrderFactory.PGOnly(otpInject, theme)
                .build();
        int retryCount = Integer.valueOf(fetchFromIssuingBankConfig(Constants.Bank.SBI.toString(),"DC","DC","max_retry_count"));
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setDebitCardNumber(PaymentDTO.SBI_DEBIT_CARD);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(DC,paymentDTO);
        directBankOTPPage.waitUntilLoads();

        for(int i = 0 ; i<retryCount;i++) {
            directBankOTPPage.submitOtp("111111");//Invalid OTP
            directBankOTPPage.VerifyRequestMsg("Incorrect OTP entered. Kindly enter the new OTP sent");
        }
        directBankOTPPage.submitOtp(PaymentDTO.OTP);//Correct OTP
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.KOTAK.toString())
                .validateBankName(Constants.Bank.SBI_FULL.toString())
                .validateCheckSum(otpInject.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(NON_EMPTY)
                .validateBankTxnId(NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.KOTAK.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.SBI_FULL.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }

    @Test(description = "Validate invalid otp exhaust scenario")
    @Parameters({"theme"})
    public void invalidOTPExhaust(@Optional("enhancedweb") String theme)
    {
        Constants.MerchantType otpInject = Constants.MerchantType.OTP_INJECT;
        validateRedirectFlowEnabled(otpInject);

        Assertions.assertThat(fetchFromRedirectChannelConfig(Constants.Gateway.KOTAK.toString(), "VISA", "DC", Constants.Bank.SBI.toString(),"status")).isEqualTo("true");

        OrderDTO orderDTO = new OrderFactory.PGOnly(otpInject, theme)
                .build();
        int retryCount = Integer.valueOf(fetchFromIssuingBankConfig(Constants.Bank.SBI.toString(),"DC","DC","max_retry_count"));

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setDebitCardNumber(PaymentDTO.SBI_DEBIT_CARD);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(DC,paymentDTO);
        directBankOTPPage.waitUntilLoads();

        for(int i = 0 ; i<=retryCount;i++) {
            directBankOTPPage.submitOtp("111111");//Invalid OTP
            directBankOTPPage.pause(2);
        }

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("246")
                .validateStatus("TXN_FAILURE")
                .validateRespMsg("OTP validation attempts exceeded.")
                .validateBankName(Constants.Bank.SBI_FULL.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .assertAll();

    }

    @Owner(GAGANDEEP)
    @Feature("PGP-28978")
    @Parameters({"theme"})
    @Test(description = "Verify Go to bank page link is visible for otp inject flow")
    public void verifyGoToBankPagelinkAvailable(@Optional("enhancedweb") String theme)
    {
        validateRedirectFlowEnabled(otpInjectMerchant);
        OrderDTO orderDTO = new OrderFactory.PGOnly(otpInjectMerchant, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setDebitCardNumber(PaymentDTO.DEBIT_CARD_NUMBER);
        cashierPage.payBy(DC,paymentDTO);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.GoToBankWebsiteLink().assertVisible();

    }

    @Owner(GAGANDEEP)
    @Feature("PGP-28978")
    @Parameters({"theme"})
    @Test(description = "Verify go to website link will redirect to actual bank")
    public void verifyGoToBankPageLinkRedirectToActualBank(@Optional("enhancedweb") String theme)
    {
        validateRedirectFlowEnabled(otpInjectMerchant);
        OrderDTO orderDTO = new OrderFactory.PGOnly(otpInjectMerchant, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setDebitCardNumber(PaymentDTO.DEBIT_CARD_NUMBER);
        cashierPage.payBy(DC,paymentDTO);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.GoToBankWebsiteLink().click();
        String url = DriverManager.getDriver().getCurrentUrl();
        Assert.assertTrue(url.contains("/mockbank/otpInject/createFormForInstaRedirect"));
    }


    @Owner(GAGANDEEP)
    @Feature("PGP-28978")
    @Parameters({"theme"})
    @Test(description = "Verify go to website link will redirect to actual bank")
    public void verifySuccessTransactionActualBankTransaction(@Optional("enhancedweb") String theme)
    {
        validateRedirectFlowEnabled(otpInjectMerchant);
        OrderDTO orderDTO = new OrderFactory.PGOnly(otpInjectMerchant, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setDebitCardNumber(PaymentDTO.DEBIT_CARD_NUMBER);
        cashierPage.payBy(DC,paymentDTO);
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.GoToBankWebsiteLink().click();
        intermediateOTPPage.otpPageTextbox(PaymentDTO.OTP);
        intermediateOTPPage.otpPageSubmitButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .assertAll();
    }


    @Owner(GAGANDEEP)
    @Feature("PGP-28978")
    @Parameters({"theme"})
    @Test(description = "Verify go to website link will redirect to actual bank")
    public void verifyGoToBankRedirectionalLinkForRetryTransaction(@Optional("enhancedweb") String theme)
    {
        validateRedirectFlowEnabled(otpInjectMerchant);
        OrderDTO orderDTO = new OrderFactory.PGOnly(otpInjectMerchant, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setDebitCardNumber(PaymentDTO.DEBIT_CARD_NUMBER);
        cashierPage.payBy(DC,paymentDTO);
        directBankOTPPage.submitOtp(INVALID_OTP);
        directBankOTPPage.VerifyRequestMsg("Incorrect OTP entered. Kindly enter the new OTP sent");
        directBankOTPPage.waitUntilLoads();
        directBankOTPPage.GoToBankWebsiteLink().click();
        String url = DriverManager.getDriver().getCurrentUrl();
        Assert.assertTrue(url.contains("/mockbank/otpInject/createFormForInstaRedirect"));
    }

}
