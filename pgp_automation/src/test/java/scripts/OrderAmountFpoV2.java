package scripts;

import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutJsCheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Date;

@Owner("Akshat")
@Feature("PGP-32214")
public class OrderAmountFpoV2 extends PGPBaseTest {
    private final CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();

    private InitTxnResponseDTO validateSuccessInitiateSubscription(InitTxnDTO initTxnDTO) {
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("S");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("0000");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Success");
        return responseDTO;

    }

    @Test(description = "Verify that order amount is displayed in FPOv2")
    public void TC_001_OrderAmount_In_FPOv2() throws Exception {

        Double txnAmount = 1.0;
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CONSENT_PG;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue(String.valueOf(txnAmount))
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(merchant.getId(), initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoResponse = response.jsonPath();
        Assertions.assertThat(fpoResponse.getString("body.orderAmount"))
                .as("orderAmount is empty")
                .isNotEmpty();

    }

    @Parameters({"theme"})
    @Test(description = "Verify the successful transaction with amount passed in initiateTransaction request")
    public void TC_002_Validate_Success_withInitiateAmt(@Optional("checkoutjs_web") String theme) throws IOException {
        String txnAmount = "10";
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setAmount("20");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
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

    @Parameters({"theme"})
    @Test(description = "Verify the successful subscription transaction with amount passed in initiateTransaction request")
    public void TC_003_Validate_successSubs_withInitiateAmt(@Optional("checkoutjs_web") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_UI_TEXT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionGraceDays("1")
                .setSubscriptionRetryCount("1")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setAmount("20");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());

        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateCheckSum(merchant.getKey())
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify the successful transaction with amount passed in initiateTransaction request for logged in flow")
    public void TC_004_Validate_Success_withInitiateAmt_loggedInFlow(@Optional("checkoutjs_web") String theme) throws Exception {

        String txnAmount = "10";
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setAmount("20");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
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
}