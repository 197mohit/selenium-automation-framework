package scripts;

import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Owner;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.Native.checkoutjs.CheckoutJsBase;

import java.io.IOException;
import java.util.Date;

public class CheckoutjsRevamp extends CheckoutJsBase {

    @Owner("Ashwani")
    @Parameters({"theme"})
    @Test(description = "Verify the card number field accepts a complete 16-digit card number and the CC txn completes successfully via Checkout JS flow")
    public void ValidateCardFieldAcceptsComplete16DigitCardNumber(@Optional("checkoutjs_web_revamp_2") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());

        // Complete 16-digit Visa card number (HDFC) used to verify the field accepts the full number
        String complete16DigitCardNumber = "4718650100010336";
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(complete16DigitCardNumber);

        cashierPage.tabCreditCard().waitUntilClickable();
        cashierPage.tabCreditCard().click();
        cashierPage.waitUntilLoads();

        // Card fields render inside a dedicated iframe, so switch into it before typing
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().waitUntilVisible();
        cashierPage.textBoxCardNumber().clearAndType(complete16DigitCardNumber);
        // Give the field a moment to apply its group-of-4 formatting before reading it back
        cashierPage.pause(1);

        // Field formats the value in groups of 4 (e.g. "4718 6501 0001 0336"); strip non-digits to compare
        String enteredValue = cashierPage.textBoxCardNumber().getAttribute("value");
        String enteredDigits = enteredValue == null ? "" : enteredValue.replaceAll("\\D", "");
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(enteredDigits)
                .as("Card number field should accept the complete 16-digit card number")
                .hasSize(16)
                .isEqualTo(complete16DigitCardNumber);
        softly.assertAll();

        cashierPage.fillExpiryMonth(paymentDTO.getExpMonth());
        cashierPage.fillExpiryYear(paymentDTO.getExpYear().substring(2));
        cashierPage.textBoxCVVNumber().waitUntilVisible();
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.pause(2);
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();

        ResponsePage responsePage = new ResponsePage();
        try {
            responsePage.waitUntilLoads("120");
        } catch (AssertionError firstAttemptFailure) {
            // One guarded retry for intermittent missed-click/navigation races on revamp cashier.
            cashierPage.clickPgOverlay();
            if (cashierPage.buttonPGPayNow().isElementPresent()) {
                cashierPage.buttonPGPayNow().waitUntilClickable();
                cashierPage.buttonPGPayNow().click();
            }
            responsePage.waitUntilLoads("120");
        }
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }

    @Owner("Ashwani")
    @Parameters({"theme"})
    @Test(description = "Verify an expired card expiry year does not allow successful CC txn via Checkout JS flow")
    public void ValidateCardWithExpiredYearDoesNotCompleteSuccessTxn(@Optional("checkoutjs_web_revamp_2") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());

        String complete16DigitCardNumber = "4718650100010336";
        // Explicitly expired expiry (Nov 2011) to validate negative behaviour.
        PaymentDTO paymentDTO = new PaymentDTO()
                .setCreditCardNumber(complete16DigitCardNumber)
                .setExpMonth("11")
                .setExpYear("2011");

        cashierPage.payByCC(cashierPage, paymentDTO);
        cashierPage.pause(3);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(DriverManager.getDriver().getCurrentUrl())
                .as("Expired card expiry should not navigate to success response page")
                .doesNotContain("/MerchantSite/bankResponse");
        softly.assertAll();
    }
}
