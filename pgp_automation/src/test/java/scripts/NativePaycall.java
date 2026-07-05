package scripts;

import com.paytm.ServerConfigProvider;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.IntermediateOTPPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

public class NativePaycall extends PGPBaseTest {

    @Owner(Constants.Owner.ROHIT)
    @Feature("PGP-31411")
    @Test(description = "validating paytmMerchantId is passed in ACQUIRING_PAY_ORDER with request when createOrderinIntTxn is enable, in Native+ txn")
    @Parameters("isNativePlus")
    public void cashierPayResponse(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        User user = userManager.getForRead(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("20")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setTXN_AMOUNT("20")
                .build();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(),fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
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
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFacadeLogs).contains("\"paytmMerchantId\":\""+initTxnDTO.getBody().getMid()+"\"");
    }

    @Owner(Constants.Owner.ROHIT)
    @Feature("PGP-31411")
    @Test(description = "validating paytmMerchantId is passed in ACQUIRING_CREATE_ORDER_AND_PAY request with blacklistCreateOrderInIntTxn ff4j enable on merchant, in Native+ txn")
    @Parameters("isNativePlus")
    public void copResponse1(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Pcf;
        User user = userManager.getForRead(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("20")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setTXN_AMOUNT("20")
                .build();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(),fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
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
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() + "\" | grep \"ACQUIRING_CREATE_ORDER_AND_PAY\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFacadeLogs).contains("\"paytmMerchantId\":\""+initTxnDTO.getBody().getMid()+"\"");
    }

    @Owner(Constants.Owner.ABHISHEK_TEWARI)
    @Feature("PGP-33776")
    @Test(description = "Validate pay on bank otp page for native transaction via CC.")
    @Parameters("isNativePlus")
    public void payOnBankForNativeTxnCCPayMode(@Optional("false") Boolean isNativePlus) {
        Constants.MerchantType merchantType = Constants.MerchantType.OTP_INJECT;
        IntermediateOTPPage otpPage = new IntermediateOTPPage("int-otp-page");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.OTP_INJECT)
                .setTxnValue("10")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setTXN_AMOUNT("10")
                .build();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(),fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        otpPage.waitUntilLoads();
        otpPage.otpPageTextbox("123456");
        otpPage.otpPageSubmitButton().click();
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
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.KOTAK.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .assertAll();
    }
}
