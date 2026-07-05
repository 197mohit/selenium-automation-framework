package scripts.EMITest.CustomCheckout;

import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static com.paytm.appconstants.Constants.*;
import static com.paytm.dto.PaymentDTO.*;
public class FullSwipeOffers extends PGPBaseTest {


    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-6982")
    @Parameters("isNativePlus")
    @Test(description = "CC txn with FULL_SWIPE_OFFER_MID — verify AFFORDABILITY_PLATFORM RESPONSE logs contain feeRateFactors isFullSwipeOfferTxn")
    public void testFullSwipeOfferTxn_CC_AffordabilityLogs(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String txnAmount = "100";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_DISCOVERY;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" + VISA_HDFC_EMI_CREDIT_CARD_NUMBER + "|" + paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth() + paymentDTO.getExpYear();

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setCardInfo(cardInfo)
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();

        String AFFORDABILITY_PLATFORM_LOGGER = "/ats/v2/order/checkout";
        String expectedFullSwipeFeeRateFactors =
                "\"feeRateFactors\":[{\"key\":\"isFullSwipeOfferTxn\",\"value\":\"TRUE\"}]";

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), AFFORDABILITY_PLATFORM_LOGGER, "RESPONSE");
        Assertions.assertThat(logs).as("AFFORDABILITY_PLATFORM logs from theia_facade should not be empty").isNotEmpty();
        Assertions.assertThat(logs).as("Logs should contain AFFORDABILITY_PLATFORM logger").contains(AFFORDABILITY_PLATFORM_LOGGER);
        Assertions.assertThat(logs)
                .as("Logs should contain feeRateFactors with isFullSwipeOfferTxn TRUE for full swipe offer txn")
                .contains(expectedFullSwipeFeeRateFactors);
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-6982")
    @Parameters("isNativePlus")
    @Test(description = "CC txn with FULL_SWIPE_OFFER_MID — verify ACQUIRING_PAY_ORDER REQUEST logs contain isFullSwipeOfferTxn TRUE")
    public void testFullSwipeOfferTxn_CC_AcquiringLogs(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String txnAmount = "100";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_DISCOVERY;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" + VISA_HDFC_EMI_CREDIT_CARD_NUMBER + "|" + paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth() + paymentDTO.getExpYear();

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setCardInfo(cardInfo)
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .AssertAll();

        String acquiringPayOrderRequestLogs = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.theia_facade,
                initTxnDTO.orderFromBody(),
                "ACQUIRING_PAY_ORDER",
                "REQUEST");
        Assertions.assertThat(acquiringPayOrderRequestLogs)
                .as("ACQUIRING_PAY_ORDER REQUEST logs should not be empty")
                .isNotEmpty();
        Assertions.assertThat(acquiringPayOrderRequestLogs)
                .as("ACQUIRING_PAY_ORDER REQUEST should contain isFullSwipeOfferTxn TRUE")
                .contains("\"isFullSwipeOfferTxn\":\"TRUE\"");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-6982")
    @Parameters("isNativePlus")
    @Test(description = "DC txn with FULL_SWIPE_OFFER_MID — verify AFFORDABILITY_PLATFORM RESPONSE and ACQUIRING_PAY_ORDER REQUEST (isFullSwipeOfferTxn) in one case")
    public void testFullSwipeOfferTxn_DC_AffordabilityAndAcquiringPayOrderLogs(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String txnAmount = "100";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_DISCOVERY;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" + DEBIT_CARD_NUMBER + "|" + paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth() + paymentDTO.getExpYear();

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setCardInfo(cardInfo)
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .AssertAll();

        String AFFORDABILITY_PLATFORM_LOGGER = "AFFORDABILITY_PLATFORM";
        String expectedFullSwipeFeeRateFactors =
                "\"feeRateFactors\":[{\"key\":\"isFullSwipeOfferTxn\",\"value\":\"TRUE\"}]";

        String affordabilityLogs = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.theia_facade,
                initTxnDTO.orderFromBody(),
                AFFORDABILITY_PLATFORM_LOGGER,
                "RESPONSE");
        Assertions.assertThat(affordabilityLogs).as("AFFORDABILITY_PLATFORM logs from theia_facade should not be empty").isNotEmpty();
        Assertions.assertThat(affordabilityLogs).as("Logs should contain AFFORDABILITY_PLATFORM logger").contains(AFFORDABILITY_PLATFORM_LOGGER);
        Assertions.assertThat(affordabilityLogs)
                .as("Logs should contain feeRateFactors with isFullSwipeOfferTxn TRUE for full swipe offer txn")
                .contains(expectedFullSwipeFeeRateFactors);

        String acquiringPayOrderRequestLogs = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.theia_facade,
                initTxnDTO.orderFromBody(),
                "ACQUIRING_PAY_ORDER",
                "REQUEST");
        Assertions.assertThat(acquiringPayOrderRequestLogs)
                .as("ACQUIRING_PAY_ORDER REQUEST logs should not be empty")
                .isNotEmpty();
        Assertions.assertThat(acquiringPayOrderRequestLogs)
                .as("ACQUIRING_PAY_ORDER REQUEST should contain isFullSwipeOfferTxn TRUE")
                .contains("\"isFullSwipeOfferTxn\":\"TRUE\"");
    }

    @Owner(Constants.Owner.VIDHI)
    @Feature("PAPR-6982")
    @Parameters("isNativePlus")
    @Test(description = "HDFC EMI (CREDIT_CARD) txn with bank offers — verify AFFORDABILITY_PLATFORM RESPONSE and ACQUIRING_PAY_ORDER REQUEST (isFullSwipeOfferTxn - FALSE)")
    public void testFullSwipeOfferTxn_HDFC_EMI_AffordabilityAndAcquiringPayOrderLogs(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String txnAmount = "100";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_DISCOVERY;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        String cardInfo = "|" + VISA_HDFC_EMI_CREDIT_CARD_NUMBER + "|" + paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth() + paymentDTO.getExpYear();

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("HDFC|9")
                .setChannelCode("HDFC")
                .setCardInfo(cardInfo)
                .setEMI_TYPE("CREDIT_CARD")
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();

        String AFFORDABILITY_PLATFORM_LOGGER = "/ats/v2/order/checkout";
        String expectedFullSwipeFeeRateFactors =
                "\"feeRateFactors\":[{\"key\":\"isFullSwipeOfferTxn\",\"value\":\"FALSE\"}]";

        String affordabilityLogs = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.theia_facade,
                initTxnDTO.orderFromBody(),
                AFFORDABILITY_PLATFORM_LOGGER,
                "RESPONSE");
                System.out.println("affordabilityLogs: " + affordabilityLogs);
        Assertions.assertThat(affordabilityLogs).as("Logs should contain AFFORDABILITY_PLATFORM logger").contains(AFFORDABILITY_PLATFORM_LOGGER);
        Assertions.assertThat(affordabilityLogs).contains(expectedFullSwipeFeeRateFactors);

        String acquiringPayOrderRequestLogs = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.theia_facade,
                initTxnDTO.orderFromBody(),
                "ACQUIRING_PAY_ORDER",
                "REQUEST");

        Assertions.assertThat(acquiringPayOrderRequestLogs).doesNotContain("\"isFullSwipeOfferTxn\":\"TRUE\"");
    }
}
