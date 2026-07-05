package scripts.GuestCheckout;

import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.reportportal.annotation.Owner;
import com.paytm.pages.*;
import io.qameta.allure.Feature;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import java.util.Date;


public class GuestCheckoutUI extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();
    Constants.MerchantType alternateID_offus = Constants.MerchantType.Alternate_ID_Offus;
    Constants.MerchantType alternateID_onus = Constants.MerchantType.Alternate_ID_Onus;


    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-45606")
    @Parameters("{theme}")
    @Test(description = "Verifying alternate id e-2-e successful Guest Checkout txn for offus")
    public void verifyingGenerateAltIDforGuestCheckout(@Optional("enhancedweb_revamp") String theme) throws Exception {

        OrderDTO orderDTO = new OrderFactory.PGOnly(alternateID_offus, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4895380115392363");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
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
                .validateBankName("HDFC Bank")
                .validateCheckSum(alternateID_offus.getKey())
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
                .validateBankName("")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderDTO.getORDER_ID(), "/token/gc/generateTokenData");
        Assertions.assertThat(logs).contains("encryptedCardData");
        Assertions.assertThat(logs).contains("\"cardToken\":\"4895380115392364\"");
        Assertions.assertThat(logs).contains("tavv");
        Assertions.assertThat(logs).contains("\"tokenBin\":\"489538011\"");
        Assertions.assertThat(logs).contains("\"tokenSuffix\":\"2364\"");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderDTO.getORDER_ID(), "ASSET_CENTER_SERVICE");
        Assertions.assertThat(logs).contains("\"assetType\":\"ALT_TOKEN\"");
        Assertions.assertThat(logs).contains("assetNo");
        Assertions.assertThat(logs).contains("cacheAssetId");
        Assertions.assertThat(logs).contains("\"assetBin\":\"489538011\"");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderDTO.getORDER_ID(), "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"transactionVia\":\"ALT_ID\"");
        Assertions.assertThat(logs).contains("\"cardBin\":\"489538\"");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, orderDTO.getORDER_ID());
        Assertions.assertThat(logs).contains("assetType=ALT_TOKEN");
        Assertions.assertThat(logs).contains("assetNo");
        Assertions.assertThat(logs).contains("maskedAssetNo");
        Assertions.assertThat(logs).contains("01|COFT:true|ALT:true");
        Assertions.assertThat(logs).contains("Authentication & Authorization response for Enrolled Card: <result>CAPTURED</result>");
        Assertions.assertThat(logs).contains("70007981|HDFC|CC|PAYMENT|S|");

    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-45606")
    @Parameters("{theme}")
    @Test(description = "Verifying alternate id e-2-e successful Guest Checkout txn for onus")
    public void verifyingGenerateAltIDforGuestCheckoutOnus(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(alternateID_onus, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4895380115392363");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
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
                .validateBankName("HDFC Bank")
                .validateChargeAmount("0.01")
                .validateCheckSum(alternateID_onus.getKey())
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
                .validateBankName("")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChargeAmount("0.01")
                .validateStatusAPIParameters()
                .AssertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderDTO.getORDER_ID(), "/token/gc/generateTokenData");
        Assertions.assertThat(logs).contains("encryptedCardData");
        Assertions.assertThat(logs).contains("\"cardToken\":\"4895380115392364\"");
        Assertions.assertThat(logs).contains("tavv");
        Assertions.assertThat(logs).contains("\"tokenBin\":\"489538011\"");
        Assertions.assertThat(logs).contains("\"tokenSuffix\":\"2364\"");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderDTO.getORDER_ID(), "ASSET_CENTER_SERVICE");
        Assertions.assertThat(logs).contains("\"assetType\":\"ALT_TOKEN\"");
        Assertions.assertThat(logs).contains("assetNo");
        Assertions.assertThat(logs).contains("cacheAssetId");
        Assertions.assertThat(logs).contains("\"assetBin\":\"489538011\"");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderDTO.getORDER_ID(), "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"transactionVia\":\"ALT_ID\"");
        Assertions.assertThat(logs).contains("\"cardBin\":\"489538\"");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, orderDTO.getORDER_ID());
        Assertions.assertThat(logs).contains("assetType=ALT_TOKEN");
        Assertions.assertThat(logs).contains("assetNo");
        Assertions.assertThat(logs).contains("maskedAssetNo");
        Assertions.assertThat(logs).contains("01|COFT:true|ALT:true");
        Assertions.assertThat(logs).contains("Authentication & Authorization response for Enrolled Card: <result>CAPTURED</result>");
        Assertions.assertThat(logs).contains("70007981|HDFC|CC|PAYMENT|S|");

    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-45606")
    @Parameters("{theme}")
    @Test(description = "Verifying alternate id e-2-e successful RUPAY Txn & verify transactionVia param for offus")
    public void verifyingRupayTxnforAltID(@Optional("enhancedweb_revamp") String theme) throws Exception {

        OrderDTO orderDTO = new OrderFactory.PGOnly(alternateID_offus, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber("6080410000000001");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC, paymentDTO);
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
                .validateBankName("HDFC Bank")
                .validateCheckSum(alternateID_offus.getKey()) 
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
                .validateBankName("")
                .validateMid(orderDTO.getMID())
               .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderDTO.getORDER_ID());
        Assertions.assertThat(logs).doesNotContain("/token/gc/generateTokenData");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderDTO.getORDER_ID(), "ASSET_CENTER_SERVICE");
        Assertions.assertThat(logs).contains("\"assetType\":\"ISO_CARD\"");
        Assertions.assertThat(logs).contains("\"cardBin\":\"608041000\"");
        Assertions.assertThat(logs).contains("\"assetBin\":\"608041\"");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(), "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"transactionVia\":\"ALT_ID\"");

    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-45606")
    @Parameters("{theme}")
    @Test(description = "Verifying alternate id e-2-e successful RUPAY Txn & verify transactionVia param for onus")
    public void verifyingRupayTxnforAltIDOnus(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(alternateID_onus, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber("6080410000000001");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC, paymentDTO);
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
                .validateBankName("HDFC Bank")
                .validateCheckSum(alternateID_onus.getKey()) 
                .validateChargeAmount("0.01")
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
                .validateBankName("")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChargeAmount("0.01")
                .validateStatusAPIParameters()
                .AssertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderDTO.getORDER_ID());
        Assertions.assertThat(logs).doesNotContain("/token/gc/generateTokenData");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderDTO.getORDER_ID(), "ASSET_CENTER_SERVICE");
        Assertions.assertThat(logs).contains("\"assetType\":\"ISO_CARD\"");
        Assertions.assertThat(logs).contains("\"cardBin\":\"608041000\"");
        Assertions.assertThat(logs).contains("\"assetBin\":\"608041\"");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(), "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"transactionVia\":\"ALT_ID\"");

    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-45606")
    @Parameters("{theme}")
    @Test(description = "Verifying alternate id e-2-e successful Guest Checkout txn for saved cards on onus")
    public void verifyingGenerateAltIDforSavedGuestCheckoutOnus(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(alternateID_onus, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4895380115392363");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC_WITH_SAVECARD, paymentDTO);

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
                .validateBankName("HDFC Bank")
                .validateChargeAmount("0.01")
                .validateCheckSum(alternateID_onus.getKey())
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
                .validateBankName("")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChargeAmount("0.01")
                .validateStatusAPIParameters()
                .AssertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderDTO.getORDER_ID(), "/token/gc/generateTokenData");
        Assertions.assertThat(logs).contains("encryptedCardData");
        Assertions.assertThat(logs).contains("\"cardToken\":\"4895380115392364\"");
        Assertions.assertThat(logs).contains("tavv");
        Assertions.assertThat(logs).contains("\"tokenBin\":\"489538011\"");
        Assertions.assertThat(logs).contains("\"tokenSuffix\":\"2364\"");
        Assertions.assertThat(logs).contains("\"tokenizationConsent\":{\"userConsent\":1");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderDTO.getORDER_ID(), "ASSET_CENTER_SERVICE");
        Assertions.assertThat(logs).contains("\"assetType\":\"ALT_TOKEN\"");
        Assertions.assertThat(logs).contains("assetNo");
        Assertions.assertThat(logs).contains("cacheAssetId");
        Assertions.assertThat(logs).contains("\"assetBin\":\"489538011\"");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderDTO.getORDER_ID(), "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"transactionVia\":\"ALT_ID\"");
        Assertions.assertThat(logs).contains("\"cardBin\":\"489538\"");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, orderDTO.getORDER_ID());
        Assertions.assertThat(logs).contains("assetType=ALT_TOKEN");
        Assertions.assertThat(logs).contains("assetNo");
        Assertions.assertThat(logs).contains("maskedAssetNo");
        Assertions.assertThat(logs).contains("01|COFT:true|ALT:true");
        Assertions.assertThat(logs).contains("Authentication & Authorization response for Enrolled Card: <result>CAPTURED</result>");
        Assertions.assertThat(logs).contains("70007981|HDFC|CC|PAYMENT|S|");

        //for checking encrypted card data present or not in tokenization req
        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, orderDTO.getORDER_ID());
        Assertions.assertThat(logs).doesNotContain("encryptedCardData");
        Assertions.assertThat(logs).contains("\"clientId\":\"PAYTM_INSTAPROXY\"");
        Assertions.assertThat(logs).contains("tokenizationConsent");
        Assertions.assertThat(logs).contains("\"isSaveAssetForUser\":true,\"cardSource\":\"MANUAL_ENTERED\",\"isSaveAssetForMerchant\":false,\"providerTypes\":[\"CARD_NETWORK\"],\"altToken\":\"4895XXXXXXXX2364\"");
        /*Assertions.assertThat(logs).contains("tokenIndexNumber");
        Assertions.assertThat(logs).contains("tokenStatus");*/


    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-45606")
    @Parameters("{theme}")
    @Test(description = "Verifying alternate id e-2-e successful Guest Checkout txn for saved cards on offus")
    public void verifyingGenerateAltIDforSavedGuestCheckoutOffus(@Optional("enhancedweb_revamp") String theme) throws Exception {

        OrderDTO orderDTO = new OrderFactory.PGOnly(alternateID_offus, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4895380115392363");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC_WITH_SAVECARD, paymentDTO);
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
                .validateBankName("HDFC Bank")
                .validateCheckSum(alternateID_offus.getKey())
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
                .validateBankName("")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderDTO.getORDER_ID(), "/token/gc/generateTokenData");
        Assertions.assertThat(logs).contains("encryptedCardData");
        Assertions.assertThat(logs).contains("\"cardToken\":\"4895380115392364\"");
        Assertions.assertThat(logs).contains("tavv");
        Assertions.assertThat(logs).contains("\"tokenBin\":\"489538011\"");
        Assertions.assertThat(logs).contains("\"tokenSuffix\":\"2364\"");
        Assertions.assertThat(logs).contains("\"tokenizationConsent\":{\"userConsent\":1");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderDTO.getORDER_ID(), "ASSET_CENTER_SERVICE");
        Assertions.assertThat(logs).contains("\"assetType\":\"ALT_TOKEN\"");
        Assertions.assertThat(logs).contains("assetNo");
        Assertions.assertThat(logs).contains("cacheAssetId");
        Assertions.assertThat(logs).contains("\"assetBin\":\"489538011\"");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderDTO.getORDER_ID(), "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"transactionVia\":\"ALT_ID\"");
        Assertions.assertThat(logs).contains("\"cardBin\":\"489538\"");

        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, orderDTO.getORDER_ID());
        Assertions.assertThat(logs).contains("assetType=ALT_TOKEN");
        Assertions.assertThat(logs).contains("assetNo");
        Assertions.assertThat(logs).contains("maskedAssetNo");
        Assertions.assertThat(logs).contains("01|COFT:true|ALT:true");
        Assertions.assertThat(logs).contains("Authentication & Authorization response for Enrolled Card: <result>CAPTURED</result>");
        Assertions.assertThat(logs).contains("70007981|HDFC|CC|PAYMENT|S|");

        //for checking encrypted card data present or not in tokenization req
        logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, orderDTO.getORDER_ID());
        Assertions.assertThat(logs).doesNotContain("encryptedCardData");
        Assertions.assertThat(logs).contains("\"clientId\":\"PAYTM_INSTAPROXY\"");
        Assertions.assertThat(logs).contains("tokenizationConsent");
        Assertions.assertThat(logs).contains("\"userConsent\":1,\"platform\":\"web\"},\"isSaveAssetForUser\":false,\"cardSource\":\"MANUAL_ENTERED\",\"isSaveAssetForMerchant\":true,\"providerTypes\":[\"CARD_NETWORK\"],\"altToken\":\"4895XXXXXXXX2364\"");

/*        Assertions.assertThat(logs).contains("tokenIndexNumber");
        Assertions.assertThat(logs).contains("tokenStatus");*/
    }
    @Owner(Constants.Owner.Amanpreet)
    @Feature("PGP-45606")
    @Parameters("{theme}")
    @Test(description = "Verify TAVV is passed in PaymentCashier directPassThroughInfo for fresh card Onus transaction\"")
    public void verifyTAVVforCOPinAlternateIdOnusVISA(@Optional("enhancedweb_revamp") String theme) throws Exception {

        OrderDTO orderDTO = new OrderFactory.PGOnly(alternateID_onus, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4895380115392363");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
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
                .validateBankName("HDFC Bank")
                .validateCheckSum(alternateID_onus.getKey())
                .validateChargeAmount("0.01")
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
                .validateBankName("")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChargeAmount("0.01")
                .validateStatusAPIParameters()
                .AssertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderDTO.getORDER_ID(), "ACQUIRING_PAY_ORDER");
        String [] ar = logs.split("directPassThroughInfo");
        String [] ar2 = ar[1].split("\"");
        String decodedString = PGPHelpers.Base64Decode(ar2[2]);
        Assertions.assertThat(decodedString).contains("tavv");
    }
    @Owner(Constants.Owner.Amanpreet)
    @Feature("PGP-45606")
    @Parameters("{theme}")
    @Test(description = "Verify TAVV is passed in PaymentCashier directPassThroughInfo for fresh card Offus transaction")
    public void verifyTAVVforPaymemtCashierinAlternateIdOffusVISA(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(alternateID_offus, theme)
              //  .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4895380115392363");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
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
                .validateBankName("HDFC Bank")
                .validateCheckSum(alternateID_offus.getKey())
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
                .validateBankName("")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderDTO.getORDER_ID(), "ACQUIRING_PAY_ORDER");
        String [] ar = logs.split("directPassThroughInfo");
        String [] ar2 = ar[1].split("\"");
        String decodedString = PGPHelpers.Base64Decode(ar2[2]);
        Assertions.assertThat(decodedString).contains("tavv");

    }
}