package scripts;

import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.refund.SyncRefund;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.Good;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

import static io.restassured.RestAssured.given;


@Owner(Constants.Owner.ROHIT)
@Feature("PGP-31951")
public class GoodsInfoCheck extends PGPBaseTest {

    @Test(description = "Verify successful txn where % symbol is passed in description of goods info. native+")
    @Parameters("isNativePlus")
    public void nativePlus01(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        Good goodsInfo = new Good();
        goodsInfo.setDescription("%Women Summer Dress%%%% New White Lace Sleeveless%%");

        User user = userManager.getForRead(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("20")
                .build();
        initTxnDTO.getBody().setGoods(new Good[]{goodsInfo});
        String checksum = PGPHelpers.getNativeChecksum(merchantType.getKey(), initTxnDTO.getBody());
        initTxnDTO.getHead().setSignature(checksum);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setTXN_AMOUNT("20")
                .build();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(),fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
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

    }
    @Parameters({"theme"})
    @Test(description = "Verify successful txn where % symbol is passed in description of goods info. Enhanced")
    public void enhanced01(@Optional("enhancedweb") String theme) {
        String goodsInfo = "%20%5B%0A%20%20%20%20%20%20%20%20%20%20%20%20%7B%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22merchantGoodsId%22%3A%20%2224525635625623%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22merchantShippingId%22%3A%20%22564314314574327545%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22snapshotUrl%22%3A%20%22%5Bhttp%3A%2F%2Fsnap.url.com%5D%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22description%22%3A%20%22%25WomenSummerDressNewWhiteLaceSleeveless%25%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22category%22%3A%20%22%25%25travelling%2Fsubway%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22quantity%22%3A%20%223.2%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22unit%22%3A%20%22Kg%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22price%22%3A%20%7B%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22currency%22%3A%20%22INR%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22value%22%3A%20%221%22%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%7D%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22extendInfo%22%3A%20%7B%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22udf1%22%3A%20%22test%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22udf2%22%3A%20%22test%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22udf3%22%3A%20%22test%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22udf4%22%3A%20%22test%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22udf5%22%3A%20%22test%22%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%7D%0A%20%20%20%20%20%20%20%20%20%20%20%20%7D%0A%20%20%20%20%20%20%20%20%5D";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setGoodsInfo(goodsInfo)
                .setTXN_AMOUNT("20")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
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

    }
    @Parameters({"theme"})
    @Test(description = "Verify successful txn where % symbol is passed in description of goods info. App_invoke")
    public void showPaymentPage01(@Optional("enhancedwap") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        Good goodsInfo = new Good();
        goodsInfo.setDescription("%Women Summer Dress%%%% New White Lace Sleeveless%%");
        User user = userManager.getForRead(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("20")
                .build();
        initTxnDTO.getBody().setGoods(new Good[]{goodsInfo});
        String checksum = PGPHelpers.getNativeChecksum(merchantType.getKey(), initTxnDTO.getBody());
        initTxnDTO.getHead().setSignature(checksum);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.PGOnly_Retry,initTxnDTO.getBody().getOrderId(),txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        CommonHelpers.validateTxnStatus(orderDTO,initTxnDTO,txnStatus,Constants.Gateway.HDFC.toString(),
                Constants.Bank.HDFC.toString(),"CC");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-13085")
    @Test(description = "Validate itemProductName in COP Request ")
    public void validateitemProductNameinCOP() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Good goodsInfo = new Good();
        goodsInfo.setDescription("Business Loan Dues");
        String mid = Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y).build();
        initTxnDTO.getBody().setGoods(new Good[]{goodsInfo});
        String checksum = PGPHelpers.getNativeChecksum(Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y.getKey(), initTxnDTO.getBody());
        initTxnDTO.getHead().setSignature(checksum);
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("PAYTM_DIGITAL_CREDIT")
                .setAuthMode("3D")
                .setChannelId("WEB")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("Paytm Postpaid");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("\"itemProductName\":\"Business Loan Dues\"");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-13085")
    @Test(description = "Validate itemProductName with special character in COP Request ")
    public void validateitemProductNamewithspecialcharacterinCOP() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Good goodsInfo = new Good();
        goodsInfo.setDescription("%Business @ Loan %Dues%$%");
        Constants.MerchantType merchant = Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y).build();
        initTxnDTO.getBody().setGoods(new Good[]{goodsInfo});
        String checksum = PGPHelpers.getNativeChecksum(Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y.getKey(), initTxnDTO.getBody());
        initTxnDTO.getHead().setSignature(checksum);
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        String mid = merchant.getId();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("PAYTM_DIGITAL_CREDIT")
                .setAuthMode("3D")
                .setChannelId("WEB")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("Paytm Postpaid");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("\"itemProductName\":\"%Business @ Loan %Dues%$%\"");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PG2-13085")
    @Parameters({"theme"})
    @Test(description = "Validate itemProductName in Cashier Pay Request in CO then Pay flow Enhanced Flow")
    public void validateitemProductNameinEnhancedFlow(@Optional("enhancedweb") String theme) throws Exception{
        String goodsInfo = "%20%5B%0A%20%20%20%20%20%20%20%20%20%20%20%20%7B%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22merchantGoodsId%22%3A%20%2224525635625623%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22merchantShippingId%22%3A%20%22564314314574327545%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22snapshotUrl%22%3A%20%22%5Bhttp%3A%2F%2Fsnap.url.com%20%5D%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22description%22%3A%20%22Amazon%20Pay%20Gift%20Card%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22category%22%3A%20%22travelling%2Fsubway%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22quantity%22%3A%20%223.2%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22unit%22%3A%20%22Kg%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22price%22%3A%20%7B%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22currency%22%3A%20%22INR%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22value%22%3A%20%221%22%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%7D%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22extendInfo%22%3A%20%7B%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22udf1%22%3A%20%22ajay%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22udf2%22%3A%20%22ajay%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22udf3%22%3A%20%22ajay%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22udf4%22%3A%20%22ajay%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22udf5%22%3A%20%22ajay%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%22comments%22%3A%20%22Txn%20for%20Comment%20Peon1%22%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%7D%0A%20%20%20%20%20%20%20%20%20%20%20%20%7D%0A%20%20%20%20%20%20%20%20%5D";
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG2_CO_THEN_PAY_FULL_TRAFFIC_Y, theme)
                .setGoodsInfo(goodsInfo)
                .setTXN_AMOUNT("1")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("Paytm Postpaid")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PAYTMCC.toString())
                .validateBankName(Constants.Gateway.PAYTMCC.toString())
                .assertAll();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"itemProductName\":\"Amazon Pay Gift Card\"");
    }
}
