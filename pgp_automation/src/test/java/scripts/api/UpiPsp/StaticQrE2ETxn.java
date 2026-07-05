package scripts.api.UpiPsp;

import com.paytm.api.AxisBankMockApi;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.fest.assertions.api.Assertions;
import org.testng.annotations.Test;
import org.testng.asserts.Assertion;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Random;

public class StaticQrE2ETxn extends PGPBaseTest {

    private static final String REQUEST = "Request";
    private static final String RESPONSE = "Response";
    private static final String Create_Order_And_PAY = "ACQUIRING_CREATE_ORDER_PAY_V2";
    private static final String THEIA_QR_REQUEST = "c.p.p.i.c.i.UPIQRFlowCommand.notifyTheiaQRTxn";
    public static String bankTxnId(){
        Random rand = new Random();
        long result = (long)(rand.nextDouble() * 900000000000000000L) + 100000000000000000L;
        String rrn = String.valueOf(result);
        return rrn;
    }

    @Owner("Karmvir")
    @Feature("PGP-54518")
    @Test(description = "Verify the E2E success txn for static QR when Axis Bank callback received from Bank")
    public void testE2ESuccessTxnForStaticQrAxisBank() throws UnsupportedEncodingException, InterruptedException {
        String callBackUrl= com.paytm.utils.merchant.Constants.PGP_HOST+"/instaproxy/bankresponse/AXIF/UPI/RESPONSE";
        String Esn= bankTxnId();
        Constants.MerchantType merchantType = Constants.MerchantType.AXIS_BANK_SQR;
        String mid= merchantType.getId();
        JsonPath Path=AxisBankMockApi.axisBankMockApi(callBackUrl,"paytms.1000db@axis",Esn,"1.00");
        Assertions.assertThat(Path.getString("orderStatus")).isEqualTo("Success");
        String logsRequest = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,Esn,THEIA_QR_REQUEST ,REQUEST);
        System.out.println(logsRequest);
        Assertions.assertThat(logsRequest).contains("\"bankCode\":\"AXIF\"");
        Assertions.assertThat(logsRequest).contains("\"payeeVpa\":\"paytms.1000db@axis\"");
        Assertions.assertThat(logsRequest).contains("\"orderId\":\""+Esn+"\"");
        Assertions.assertThat(logsRequest).contains("\"bankErrorCode\":\"00\"");
        Assertions.assertThat(logsRequest).contains("\"mid\":\""+mid+"\"");
        Assertions.assertThat(logsRequest).contains("\"payerVpa\":\"test@axis\"");
        Assertions.assertThat(logsRequest).contains("\"txnStatus\":\"SUCCESS\"");
        Assertions.assertThat(logsRequest).contains("\"txnAmount\":\"1.00\"");
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,Esn,THEIA_QR_REQUEST ,RESPONSE);
        System.out.println(logsResponse);
        Assertions.assertThat(logsResponse).contains("\"resultCode\":\"ACCEPTED_SUCCESS\"");
        Assertions.assertThat(logsResponse).contains("\"resultCodeId\":\"001\"");
        Assertions.assertThat(logsResponse).contains("\"resultMsg\":\"accepted success\"");
        String UpiPspLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.DOWN_STEAM_REQUEST_RESPONSE,Esn, Create_Order_And_PAY, REQUEST);
        org.assertj.core.api.Assertions.assertThat(UpiPspLogs).contains("deemedTxnInfo");
        org.assertj.core.api.Assertions.assertThat(UpiPspLogs).contains("\"txnStatus\":\"SUCCESS\"");
        org.assertj.core.api.Assertions.assertThat(UpiPspLogs).contains("\"fundBackTrigger\":false");
        org.assertj.core.api.Assertions.assertThat(UpiPspLogs).contains("\"flowType\":\"3P_SQR\"");
        TxnStatus txnStatus = new TxnStatus(mid, Esn);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(Esn)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.AXIF.toString())
                .validateRespCode("01")
                .validateTxnAmount("1.00")
                .validateRespMsg("Txn Successful.")
                .validateMid(mid)
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }


}
