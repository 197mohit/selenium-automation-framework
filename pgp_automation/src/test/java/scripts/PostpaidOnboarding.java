package scripts;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PostpaidHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.PaymentModes;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.json.simple.JSONObject;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Owner("Tarun")
public class PostpaidOnboarding extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();

    private FetchPaymentOptResponseDTO executeFetchPaymentOption(String orderId, String mid, String txnToken, boolean onboardingStatus) {
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.
                Builder(txnToken, onboardingStatus).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(mid, orderId, fetchPaymentOptionsDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = null;
        JsonPath jsonPath = fetchPaymentOption.execute().jsonPath();
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(jsonPath.get());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            fetchPaymentOptResponseDTO = mapper.readValue(jsonObject.toJSONString(), FetchPaymentOptResponseDTO.class);
        } catch (IOException e) {
            Assertions.fail("Exception occured in converting fetchPaymentOptionResp to DTO", e);
        }
        Assertions.assertThat("Success")
                .as("Mismatch in Result Message")
                .isEqualToIgnoringCase(fetchPaymentOptResponseDTO.getBody().getResultInfo().getResultMsg());
        return fetchPaymentOptResponseDTO;
    }

    private PaymentModes fetchPaymentMode(FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO, String paymodes) {
        List<PaymentModes> paymentModesList = fetchPaymentOptResponseDTO.getBody().getMerchantPayOption().getPaymentModes();

        for (PaymentModes paymentMode : paymentModesList) {
            if (paymodes.equalsIgnoreCase(paymentMode.getPaymentMode()))
                return paymentMode;
        }
        Assertions.fail(paymodes + " not found in response of fetchPaymentOptResponse");
        return null;
    }


    @Test(description = "Validate successful native transaction using postpaid while onboarding")
    public void successNativeTxn_postpaidOnboarding() throws Exception {
        User user = userManager.getForWrite(Label.POSTPAIDONBOARDING);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid)
                .setTxnValue("75")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO ftchPymntOptResp = executeFetchPaymentOption(initTxnDTO.getBody().getOrderId(),
                initTxnDTO.getBody().getMid(), txnToken, true);

        PaymentModes paymentMode = fetchPaymentMode(ftchPymntOptResp, "PAYTM_DIGITAL_CREDIT");
        Assertions.assertThat(paymentMode.getOnboarding())
                .as("onboarding is false for PAYTM_DIGITAL_CREDIT in fetchPaymentOptions")
                .isTrue();

        OrderDTO orderDTO = new OrderFactory
                .Native(Constants.MerchantType.Hybrid, initTxnDTO.orderFromBody(), txnToken, PayMethodType.PAYTM_DIGITAL_CREDIT)
                .build();
        WalletHelpers.setZeroBalance(user);
        PostpaidHelpers.updatePostpaidUserAttributes(user, PostpaidHelpers.WHITELISTED);
        PostpaidHelpers.updateBalance("1000");
        checkoutPage.createNativeOrder(orderDTO, false);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful transaction using Postpaid while onboarding")
    public void successTxn_postpaidOnboard(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.POSTPAIDONBOARDING);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("74.0").build();
        WalletHelpers.setZeroBalance(user);
        PostpaidHelpers.updatePostpaidUserAttributes(user, PostpaidHelpers.WHITELISTED);
        PostpaidHelpers.updateBalance("1000");
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.POSTPAID_ONBOARDING);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus =
                new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateGatewayName("PAYTMCC")
                .AssertAll();
    }


    @Parameters({"theme"})
    @Test(description = "Validate successful Hybrid transaction using Postpaid while onboarding")
    public void successHybridTxn_postpaidOnboard(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.POSTPAIDONBOARDING);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.Hybrid, theme, user)
                .setTXN_AMOUNT("74.0").build();
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        PostpaidHelpers.updatePostpaidUserAttributes(user, PostpaidHelpers.WHITELISTED);
        PostpaidHelpers.updateBalance("1000");
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.POSTPAID_ONBOARDING);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus =
                new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());

        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "Paytm Postpaid")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Constants.Gateway.PAYTMCC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();
    }


}
