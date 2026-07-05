package scripts.UPI.OnlineUpi;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.convenienceFeeElements;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.RedisHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.OrderFactory.PGOnly;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutJsCheckoutPage;
import com.paytm.pages.CheckoutPage;
import java.util.HashMap;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class ConvenienceFeeUpiIntentUITest extends PGPBaseTest {
    MerchantType convFeeAndPlatformFeeMerchant = MerchantType.CONVENIENCE_AND_PLATFORM_FEE_ON_UPI_SUBTYPE;
    MerchantType platformFeeMerchant = MerchantType.PLATFORM_FEE_ON_UPI_SUBTYPE;
    MerchantType convFeeMerchant = MerchantType.CONVENIENCE_FEE_ON_UPI_SUBTYPE;

    //if fee related assertions are failing, check if commission value and type i.e. flat/percentage is set as mentioned in this method
    public HashMap<String, Double> calculateExpectedFees(double transactionAmount, MerchantType merchantType) {
        HashMap<String, Double> feeAmounts = new HashMap<>();


        double convFeeCC = 0.0;
        double convFeeCL = 0.0;
        double convFeePPI = 0.0;
        double platformFee = 0.0;
        
        switch (merchantType) {
            case CONVENIENCE_AND_PLATFORM_FEE_ON_UPI_SUBTYPE:
                convFeeCC = 5.00;
                convFeeCL = 4.00;
                convFeePPI = 2.00;
                platformFee = 5.00;
                break;
                
            case PLATFORM_FEE_ON_UPI_SUBTYPE:
                platformFee = 5.00;
                break;
                
            case CONVENIENCE_FEE_ON_UPI_SUBTYPE:
                convFeeCC = 2.00;
                convFeeCL = 2.00;
                convFeePPI = 3.00;
                break;
                
            default:
                throw new IllegalArgumentException("Invalid merchant type: " + merchantType);
        }
        
        feeAmounts.put("expectedConvFeeAmtUpiCC", convenienceFeeCalculator(transactionAmount, convFeeCC, 0, "UPI"));
        feeAmounts.put("expectedConvFeeAmtUpiCL", convenienceFeeCalculator(transactionAmount, convFeeCL, 0, "UPI"));
        feeAmounts.put("expectedConvFeeAmtUpiPPI", convenienceFeeCalculator(transactionAmount, convFeePPI, 0, "UPI"));
        feeAmounts.put("expectedPlatformFeeAmt", convenienceFeeCalculator(transactionAmount, 0, platformFee, "UPI"));
        
        return feeAmounts;
    }

    @Test(description = "Verify UPI Intent Convenience Fee for DQR when both Platform and Conv. fee are present on merchant")
    @Parameters({"JsType"})
    public void testCase_01(@Optional("redirection") String JsType) throws Exception {
        InitTxnDTO initTxnDTO=null;
        String txnToken=null;
        String txnAmount ="20";
        String theme=null;
        CheckoutPage checkoutPage = new CheckoutPage();
        // Create order and navigate to cashier page
        if(!JsType.equalsIgnoreCase("redirection")){
            initTxnDTO = new InitTxnDTO.Builder(null, convFeeAndPlatformFeeMerchant)
            .setTxnValue(txnAmount)
            .build();
            txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
            theme = PGPHelpers.openJs(JsType, initTxnDTO, txnToken,convFeeAndPlatformFeeMerchant,"UPI");
        }

        else {
            theme = PGPHelpers.openJs(JsType, initTxnDTO, txnToken,convFeeAndPlatformFeeMerchant,"UPI");
            OrderDTO orderDTO = new OrderFactory.PGOnly(convFeeAndPlatformFeeMerchant, theme)
                .setTXN_AMOUNT(txnAmount)
                .build();
            checkoutPage.createOrder(orderDTO);
        }

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(cashierPage.FeeTextForUpiIntentQr().getText()).isEqualTo(convenienceFeeElements.convFeeHeading2.toString());
        
        cashierPage.pcfConvenienceInfoIconNew().click();
        cashierPage.pcfConvenienceInfoHeaderNew().isDisplayed();
        HashMap<String, Double> expectedFees = calculateExpectedFees(Double.valueOf(txnAmount), convFeeAndPlatformFeeMerchant);
        //convenience fee checks
        softAssertions.assertThat(cashierPage.isConvenienceFeeWrapperPresent()).isEqualTo(true);
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.convFeeHeading.name()).getText()).isEqualTo(convenienceFeeElements.convFeeHeading.toString());
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.convFeeText.name()).getText()).isEqualTo(convenienceFeeElements.convFeeText.toString());
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.bankAccountTitle.name()).getText()).contains(convenienceFeeElements.bankAccountTitle.toString());
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.bankAccountSubTitle.name()).getText()).isEqualTo(convenienceFeeElements.bankAccountSubTitle.toString());
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.bankAccountFee.name()).getText()).isEqualTo("₹0");
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.subTypeCreditCardTitle.name()).getText()).isEqualTo(convenienceFeeElements.subTypeCreditCardTitle.toString());
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.creditCardFee.name()).getText()).isEqualTo("₹"+expectedFees.get("expectedConvFeeAmtUpiCC"));

        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.subTypePPIWalletTitle.name()).getText()).isEqualTo(convenienceFeeElements.subTypePPIWalletTitle.toString());
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.ppiWalletFee.name()).getText()).isEqualTo("₹"+expectedFees.get("expectedConvFeeAmtUpiPPI"));
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.subTypeCreditLineTitle.name()).getText()).isEqualTo(convenienceFeeElements.subTypeCreditLineTitle.toString());
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.creditLineFee.name()).getText()).isEqualTo("₹"+expectedFees.get("expectedConvFeeAmtUpiCL"));


        //platform fee checks
        softAssertions.assertThat(cashierPage.isPlatformFeeWrapperPresent()).isEqualTo(true);

        softAssertions.assertThat(cashierPage.getPlatformFeeWrapperElements().get(convenienceFeeElements.platformFeeHeading.name()).getText()).isEqualTo(convenienceFeeElements.platformFeeHeading.toString());
        softAssertions.assertThat(cashierPage.getPlatformFeeWrapperElements().get(convenienceFeeElements.platformFeeText.name()).getText()).isEqualTo(convenienceFeeElements.platformFeeText.toString());
        softAssertions.assertThat(cashierPage.getPlatformFeeWrapperElements().get(convenienceFeeElements.platformFeeAmount.name()).getText()).isEqualTo("₹"+expectedFees.get("expectedPlatformFeeAmt"));
        softAssertions.assertAll();
    }

    @Test(description = "Verify UPI Intent Convenience Fee for DQR when only Conv. fee is present on merchant")
    @Parameters({"JsType"})
    public void testCase_02(@Optional("checkoutjs") String JsType) throws Exception {

        InitTxnDTO initTxnDTO=null;
        String txnToken=null;
        String txnAmount ="20";
        String theme=null;
        CheckoutPage checkoutPage = new CheckoutPage();
        if(!JsType.equalsIgnoreCase("redirection")){
            initTxnDTO = new InitTxnDTO.Builder(null, convFeeMerchant)
                .setTxnValue(txnAmount)
                .build();
            txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
            theme = PGPHelpers.openJs(JsType, initTxnDTO, txnToken,convFeeMerchant,"UPI");
        }

        else {
            theme = PGPHelpers.openJs(JsType, initTxnDTO, txnToken,convFeeMerchant,"UPI");
            OrderDTO orderDTO = new OrderFactory.PGOnly(convFeeMerchant, theme)
                .setTXN_AMOUNT(txnAmount)
                .build();
            checkoutPage.createOrder(orderDTO);
        }
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(cashierPage.FeeTextForUpiIntentQr().getText()).isEqualTo(convenienceFeeElements.convFeeHeading3.toString());

        cashierPage.pcfConvenienceInfoIconNew().click();
        cashierPage.pcfConvenienceInfoHeaderNew().isDisplayed();
        HashMap<String, Double> expectedFees = calculateExpectedFees(Double.valueOf(txnAmount), convFeeMerchant);

        //convenience fee checks
        softAssertions.assertThat(cashierPage.isConvenienceFeeWrapperPresent()).isEqualTo(true);
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.convFeeHeading.name()).getText()).isEqualTo(convenienceFeeElements.convFeeHeading.toString());
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.convFeeText.name()).getText()).isEqualTo(convenienceFeeElements.convFeeText.toString());
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.bankAccountTitle.name()).getText()).contains(convenienceFeeElements.bankAccountTitle.toString());
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.bankAccountSubTitle.name()).getText()).isEqualTo(convenienceFeeElements.bankAccountSubTitle.toString());
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.bankAccountFee.name()).getText()).isEqualTo("₹0");
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.subTypeCreditCardTitle.name()).getText()).isEqualTo(convenienceFeeElements.subTypeCreditCardTitle.toString());
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.creditCardFee.name()).getText()).isEqualTo("₹"+expectedFees.get("expectedConvFeeAmtUpiCC"));

        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.subTypePPIWalletTitle.name()).getText()).isEqualTo(convenienceFeeElements.subTypePPIWalletTitle.toString());
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.ppiWalletFee.name()).getText()).isEqualTo("₹"+expectedFees.get("expectedConvFeeAmtUpiPPI"));
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.subTypeCreditLineTitle.name()).getText()).isEqualTo(convenienceFeeElements.subTypeCreditLineTitle.toString());
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.creditLineFee.name()).getText()).isEqualTo("₹"+expectedFees.get("expectedConvFeeAmtUpiCL"));


        //platform fee checks
        softAssertions.assertThat(cashierPage.isPlatformFeeWrapperPresent()).isEqualTo(false);
        softAssertions.assertAll();
    }

    @Test(description = "Verify UPI Intent Convenience Fee for DQR when only Platform fee is present on merchant")
    @Parameters({"JsType"})
    public void testCase_03(@Optional("checkoutjs") String JsType) throws Exception {
        InitTxnDTO initTxnDTO=null;
        String txnToken=null;
        String txnAmount ="20";
        String theme=null;
        CheckoutPage checkoutPage = new CheckoutPage();
        if(!JsType.equalsIgnoreCase("redirection")){
            initTxnDTO = new InitTxnDTO.Builder(null, platformFeeMerchant)
                .setTxnValue(txnAmount)
                .build();
            txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
            theme = PGPHelpers.openJs(JsType, initTxnDTO, txnToken,platformFeeMerchant,"UPI");
        }

        else {
            theme = PGPHelpers.openJs(JsType, initTxnDTO, txnToken,platformFeeMerchant,"UPI");
            OrderDTO orderDTO = new OrderFactory.PGOnly(platformFeeMerchant, theme)
                .setTXN_AMOUNT(txnAmount)
                .build();
            checkoutPage.createOrder(orderDTO);
        }
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(cashierPage.FeeTextForUpiIntentQr().getText()).isEqualTo(convenienceFeeElements.convFeeHeading2.toString());

        cashierPage.pcfConvenienceInfoIconNew().click();
        cashierPage.pcfConvenienceInfoHeaderNew().isDisplayed();
        HashMap<String, Double> expectedFees = calculateExpectedFees(Double.valueOf(txnAmount), platformFeeMerchant);

        //convenience fee checks
        softAssertions.assertThat(cashierPage.isConvenienceFeeWrapperPresent()).isEqualTo(false);


        //platform fee checks
        softAssertions.assertThat(cashierPage.isPlatformFeeWrapperPresent()).isEqualTo(true);
        softAssertions.assertThat(cashierPage.getPlatformFeeWrapperElements().get(convenienceFeeElements.platformFeeHeading.name()).getText()).isEqualTo(convenienceFeeElements.platformFeeHeading.toString());
        softAssertions.assertThat(cashierPage.getPlatformFeeWrapperElements().get(convenienceFeeElements.platformFeeText.name()).getText()).isEqualTo(convenienceFeeElements.platformFeeText.toString());
        softAssertions.assertThat(cashierPage.getPlatformFeeWrapperElements().get(convenienceFeeElements.platformFeeAmount.name()).getText()).isEqualTo("₹"+expectedFees.get("expectedPlatformFeeAmt"));
        softAssertions.assertAll();
    }

    @Test(description = "Verify UPI Intent Convenience Fee for online intent when both Platform and Conv. fee are present on merchant")
    @Parameters({"JsType"})
    public void testCase_04(@Optional("checkoutjs") String JsType) throws Exception {
        InitTxnDTO initTxnDTO=null;
        String txnToken=null;
        String txnAmount ="20";
        String theme=null;
        CheckoutPage checkoutPage = new CheckoutPage();
        if(!JsType.equalsIgnoreCase("redirection")){
            initTxnDTO = new InitTxnDTO.Builder(null, convFeeAndPlatformFeeMerchant)
                .setTxnValue(txnAmount)
                .build();
            txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
            theme = PGPHelpers.openJs(JsType, initTxnDTO, txnToken,convFeeAndPlatformFeeMerchant,"UPI");
        }

        else {
            theme = PGPHelpers.openJs(JsType, initTxnDTO, txnToken,convFeeAndPlatformFeeMerchant,"UPI");
            OrderDTO orderDTO = new OrderFactory.PGOnly(convFeeAndPlatformFeeMerchant, theme)
                .setTXN_AMOUNT(txnAmount)
                .build();
            checkoutPage.createOrder(orderDTO);
        }

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        SoftAssertions softAssertions = new SoftAssertions();
        cashierPage.tabUPI().click();
        cashierPage.upiIntentTabTick();
        softAssertions.assertThat(cashierPage.upiIntentPcfInfoText().getText()).isEqualTo(convenienceFeeElements.convFeeHeading2.toString());
        cashierPage.upiIntentPcfInfoIcon().click();
        cashierPage.pcfConvenienceInfoHeaderNew().isDisplayed();
        HashMap<String, Double> expectedFees = calculateExpectedFees(Double.valueOf(txnAmount), convFeeAndPlatformFeeMerchant);

        //convenience fee checks
        softAssertions.assertThat(cashierPage.isConvenienceFeeWrapperPresent()).isEqualTo(true);
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.convFeeHeading.name()).getText()).isEqualTo(convenienceFeeElements.convFeeHeading.toString());
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.convFeeText.name()).getText()).isEqualTo(convenienceFeeElements.convFeeText.toString());
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.bankAccountTitle.name()).getText()).contains(convenienceFeeElements.bankAccountTitle.toString());
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.bankAccountSubTitle.name()).getText()).isEqualTo(convenienceFeeElements.bankAccountSubTitle.toString());
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.bankAccountFee.name()).getText()).isEqualTo("₹0");
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.subTypeCreditCardTitle.name()).getText()).isEqualTo(convenienceFeeElements.subTypeCreditCardTitle.toString());
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.creditCardFee.name()).getText()).isEqualTo("₹"+expectedFees.get("expectedConvFeeAmtUpiCC"));

        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.subTypePPIWalletTitle.name()).getText()).isEqualTo(convenienceFeeElements.subTypePPIWalletTitle.toString());
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.ppiWalletFee.name()).getText()).isEqualTo("₹"+expectedFees.get("expectedConvFeeAmtUpiPPI"));
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.subTypeCreditLineTitle.name()).getText()).isEqualTo(convenienceFeeElements.subTypeCreditLineTitle.toString());
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.creditLineFee.name()).getText()).isEqualTo("₹"+expectedFees.get("expectedConvFeeAmtUpiCL"));


        //platform fee checks
        softAssertions.assertThat(cashierPage.isPlatformFeeWrapperPresent()).isEqualTo(true);

        softAssertions.assertThat(cashierPage.getPlatformFeeWrapperElements().get(convenienceFeeElements.platformFeeHeading.name()).getText()).isEqualTo(convenienceFeeElements.platformFeeHeading.toString());
        softAssertions.assertThat(cashierPage.getPlatformFeeWrapperElements().get(convenienceFeeElements.platformFeeText.name()).getText()).isEqualTo(convenienceFeeElements.platformFeeText.toString());
        softAssertions.assertThat(cashierPage.getPlatformFeeWrapperElements().get(convenienceFeeElements.platformFeeAmount.name()).getText()).isEqualTo("₹"+expectedFees.get("expectedPlatformFeeAmt"));
        softAssertions.assertAll();
    }

    @Test(description = "Verify UPI Intent Convenience Fee for UPI Intent when only Conv. fee is present on merchant")
    @Parameters({"JsType"})
    public void testCase_05(@Optional("checkoutjs") String JsType) throws Exception {
        InitTxnDTO initTxnDTO=null;
        String txnToken=null;
        String txnAmount ="20";
        String theme=null;
        CheckoutPage checkoutPage = new CheckoutPage();
        if(!JsType.equalsIgnoreCase("redirection")){
            initTxnDTO = new InitTxnDTO.Builder(null, convFeeMerchant)
                .setTxnValue(txnAmount)
                .build();
            txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
            theme = PGPHelpers.openJs(JsType, initTxnDTO, txnToken,convFeeMerchant,"UPI");
        }

        else {
            theme = PGPHelpers.openJs(JsType, initTxnDTO, txnToken,convFeeMerchant,"UPI");
            OrderDTO orderDTO = new OrderFactory.PGOnly(convFeeMerchant, theme)
                .setTXN_AMOUNT(txnAmount)
                .build();
            checkoutPage.createOrder(orderDTO);
        }

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        SoftAssertions softAssertions = new SoftAssertions();
        cashierPage.tabUPI().click();
        cashierPage.upiIntentTabTick();
        softAssertions.assertThat(cashierPage.upiIntentPcfInfoText().getText()).isEqualTo(convenienceFeeElements.convFeeHeading3.toString());
        cashierPage.upiIntentPcfInfoIcon().click();
        cashierPage.pcfConvenienceInfoHeaderNew().isDisplayed();
        HashMap<String, Double> expectedFees = calculateExpectedFees(Double.valueOf(txnAmount), convFeeMerchant);

        //convenience fee checks
        softAssertions.assertThat(cashierPage.isConvenienceFeeWrapperPresent()).isEqualTo(true);
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.convFeeHeading.name()).getText()).isEqualTo(convenienceFeeElements.convFeeHeading.toString());
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.convFeeText.name()).getText()).isEqualTo(convenienceFeeElements.convFeeText.toString());
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.bankAccountTitle.name()).getText()).contains(convenienceFeeElements.bankAccountTitle.toString());
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.bankAccountSubTitle.name()).getText()).isEqualTo(convenienceFeeElements.bankAccountSubTitle.toString());
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.bankAccountFee.name()).getText()).isEqualTo("₹0");
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.subTypeCreditCardTitle.name()).getText()).isEqualTo(convenienceFeeElements.subTypeCreditCardTitle.toString());
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.creditCardFee.name()).getText()).isEqualTo("₹"+expectedFees.get("expectedConvFeeAmtUpiCC"));

        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.subTypePPIWalletTitle.name()).getText()).isEqualTo(convenienceFeeElements.subTypePPIWalletTitle.toString());
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.ppiWalletFee.name()).getText()).isEqualTo("₹"+expectedFees.get("expectedConvFeeAmtUpiPPI"));
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.subTypeCreditLineTitle.name()).getText()).isEqualTo(convenienceFeeElements.subTypeCreditLineTitle.toString());
        softAssertions.assertThat(cashierPage.getConvenienceFeeWrapperElements().get(convenienceFeeElements.creditLineFee.name()).getText()).isEqualTo("₹"+expectedFees.get("expectedConvFeeAmtUpiCL"));


        //platform fee checks
        softAssertions.assertThat(cashierPage.isPlatformFeeWrapperPresent()).isEqualTo(false);
        softAssertions.assertAll();
    }

    @Test(description = "Verify UPI Intent Convenience Fee for UPI Intent when only Platform fee is present on merchant")
    @Parameters({"JsType"})
    public void testCase_06(@Optional("jsonredirection") String JsType) throws Exception {
        InitTxnDTO initTxnDTO=null;
        String txnToken=null;
        String txnAmount ="20";
        String theme=null;
        CheckoutPage checkoutPage = new CheckoutPage();
        if(!JsType.equalsIgnoreCase("redirection")){
            initTxnDTO = new InitTxnDTO.Builder(null, platformFeeMerchant)
                .setTxnValue(txnAmount)
                .build();
            txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
            theme = PGPHelpers.openJs(JsType, initTxnDTO, txnToken,platformFeeMerchant,"UPI");
        }

        else {
            theme = PGPHelpers.openJs(JsType, initTxnDTO, txnToken,platformFeeMerchant,"UPI");
            OrderDTO orderDTO = new OrderFactory.PGOnly(platformFeeMerchant, theme)
                .setTXN_AMOUNT(txnAmount)
                .build();
            checkoutPage.createOrder(orderDTO);
        }
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        SoftAssertions softAssertions = new SoftAssertions();
        cashierPage.tabUPI().click();
        cashierPage.upiIntentTabTick();
        softAssertions.assertThat(cashierPage.upiIntentPcfInfoText().getText()).isEqualTo(convenienceFeeElements.convFeeHeading2.toString());
        cashierPage.upiIntentPcfInfoIcon().click();
        cashierPage.pcfConvenienceInfoHeaderNew().isDisplayed();
        HashMap<String, Double> expectedFees = calculateExpectedFees(Double.valueOf(txnAmount), platformFeeMerchant);

        //convenience fee checks
        softAssertions.assertThat(cashierPage.isConvenienceFeeWrapperPresent()).isEqualTo(false);


        //platform fee checks
        softAssertions.assertThat(cashierPage.isPlatformFeeWrapperPresent()).isEqualTo(true);
        softAssertions.assertThat(cashierPage.getPlatformFeeWrapperElements().get(convenienceFeeElements.platformFeeHeading.name()).getText()).isEqualTo(convenienceFeeElements.platformFeeHeading.toString());
        softAssertions.assertThat(cashierPage.getPlatformFeeWrapperElements().get(convenienceFeeElements.platformFeeText.name()).getText()).isEqualTo(convenienceFeeElements.platformFeeText.toString());
        softAssertions.assertThat(cashierPage.getPlatformFeeWrapperElements().get(convenienceFeeElements.platformFeeAmount.name()).getText()).isEqualTo("₹"+expectedFees.get("expectedPlatformFeeAmt"));
        softAssertions.assertAll();
    }
}
