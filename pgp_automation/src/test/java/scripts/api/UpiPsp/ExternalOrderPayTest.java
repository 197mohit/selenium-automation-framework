package scripts.api.UpiPsp;

import com.paytm.api.TxnStatus;
import com.paytm.api.upipsp.externalOrderPayUPIPspDTO.OrderPayUpiPsp;
import com.paytm.api.upipsp.externalOrderPayUPIPspDTO.OrderPayUpiPspRequest;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import io.jsonwebtoken.JwtBuilder;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.time.LocalTime;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

public class ExternalOrderPayTest extends PGPBaseTest {

    private static final String Create_Order_And_PAY = "ACQUIRING_CREATE_ORDER_PAY_V2";
    private static final String RESPONSE = "RESPONSE";
    private static final String REQUEST = "REQUEST";

    public static String bankTxnId(){
        Random rand = new Random();
        long result = (long)(rand.nextDouble() * 900000000000L) + 100000000000L;
        String rrn = String.valueOf(result);
        return rrn;
    }

    @Owner("Karmvir")
    @Feature("PGP-54518")
    @Test(description = "Verify the success response of UPI PSP API and Success txn status")
    public void testTheSuccessTxn() throws UnsupportedEncodingException, InterruptedException {
        String Txndate= String.valueOf(System.currentTimeMillis());
        String orderId= RandomStringUtils.randomAlphanumeric(30);;
        Constants.MerchantType merchantType = Constants.MerchantType.YES_BANK_SQR;
        String mid= merchantType.getId();
        String bankTxnId=bankTxnId();
        OrderPayUpiPsp orderPayUpiPsp= new OrderPayUpiPsp.Builder().setRequestTimeStamp(Txndate).setBankCode("YESF")
                .setTxnStatus("SUCCESS").setTxnDate(Txndate).setOrderId(orderId).setPayeeVpa("jptest.psqs10u092@yestransact").setMid(mid)
                .setBankTxnId(orderId).setRefId(bankTxnId).setPayerVpa("9007750080@ypay").setTxnAmount("1.00").build();
        JsonPath jsonPath= OrderPayUpiPspRequest.orderPayUpiPsp(orderPayUpiPsp);
        Assertions.assertThat(jsonPath.getString("body.resultCode")).isEqualTo("ACCEPTED_SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(jsonPath.getString("body.resultMsg")).isEqualTo("accepted success");
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.DOWN_STEAM_REQUEST_RESPONSE,orderPayUpiPsp.getBody().getOrderId(), Create_Order_And_PAY, REQUEST);
        Assertions.assertThat(logsResponse).contains("deemedTxnInfo");
        Assertions.assertThat(logsResponse).contains("\"txnStatus\":\"SUCCESS\"");
        Assertions.assertThat(logsResponse).contains("\"fundBackTrigger\":false");
        Assertions.assertThat(logsResponse).contains("\"flowType\":\"3P_SQR\"");
        TxnStatus txnStatus = new TxnStatus(orderPayUpiPsp.getBody().getMid(), orderPayUpiPsp.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderPayUpiPsp.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.YESSQR.toString())
                .validateRespCode("01")
                .validateTxnAmount(orderPayUpiPsp.getBody().getTxnAmount())
                .validateRespMsg("Txn Successful.")
                .validateMid(orderPayUpiPsp.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }
    @Owner("Karmvir")
    @Feature("PGP-54518")
    @Test(description = "Verify the success response of UPI PSP API and Failure txn status")
    public void testTheFailureTxn() throws UnsupportedEncodingException, InterruptedException {
        String Txndate= String.valueOf(System.currentTimeMillis());
        String orderId= RandomStringUtils.randomAlphanumeric(30);;
        Constants.MerchantType merchantType = Constants.MerchantType.YES_BANK_SQR;
        String mid= merchantType.getId();
        String bankTxnId=bankTxnId();
        OrderPayUpiPsp orderPayUpiPsp= new OrderPayUpiPsp.Builder().setRequestTimeStamp(Txndate).setBankCode("YESF")
                .setTxnStatus("FAILURE").setTxnDate(Txndate).setOrderId(orderId).setPayeeVpa("jptest.psqs10u092@yestransact").setMid(mid)
                .setBankTxnId(orderId).setRefId(bankTxnId).setPayerVpa("9007750080@ypay").setTxnAmount("1.00").build();
        JsonPath jsonPath= OrderPayUpiPspRequest.orderPayUpiPsp(orderPayUpiPsp);
        Assertions.assertThat(jsonPath.getString("body.resultCode")).isEqualTo("ACCEPTED_SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(jsonPath.getString("body.resultMsg")).isEqualTo("accepted success");
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.DOWN_STEAM_REQUEST_RESPONSE,orderPayUpiPsp.getBody().getOrderId(), Create_Order_And_PAY, REQUEST);
        Assertions.assertThat(logsResponse).contains("deemedTxnInfo");
        Assertions.assertThat(logsResponse).contains("\"txnStatus\":\"FAILURE\"");
        Assertions.assertThat(logsResponse).contains("\"fundBackTrigger\":false");
        Assertions.assertThat(logsResponse).contains("\"flowType\":\"3P_SQR\"");
        TxnStatus txnStatus = new TxnStatus(orderPayUpiPsp.getBody().getMid(), orderPayUpiPsp.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderPayUpiPsp.getBody().getOrderId())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.YESSQR.toString())
                .validateRespCode("810")
                .validateTxnAmount(orderPayUpiPsp.getBody().getTxnAmount())
                .validateRespMsg("Payment failed due to a technical error. Please try after some time.")
                .validateMid(orderPayUpiPsp.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }
    @Owner("Karmvir")
    @Feature("PGP-54518")
    @Test(description = "Verify the success response of UPI PSP API when txndate is less the  1 hr of current time and Failure txn status")
    public void testTheE2ETxnWhenTxnReceived1HrDelay() throws UnsupportedEncodingException, InterruptedException {
        String Txndate= String.valueOf(System.currentTimeMillis()-360000000);
        String orderId= RandomStringUtils.randomAlphanumeric(30);;
        Constants.MerchantType merchantType = Constants.MerchantType.YES_BANK_SQR;
        String mid= merchantType.getId();
        String bankTxnId=bankTxnId();
        OrderPayUpiPsp orderPayUpiPsp= new OrderPayUpiPsp.Builder().setRequestTimeStamp(Txndate).setBankCode("YESF")
                .setTxnStatus("SUCCESS").setTxnDate(Txndate).setOrderId(orderId).setPayeeVpa("jptest.psqs10u092@yestransact").setMid(mid)
                .setBankTxnId(orderId).setRefId(bankTxnId).setPayerVpa("9007750080@ypay").setTxnAmount("1.00").build();
        JsonPath jsonPath= OrderPayUpiPspRequest.orderPayUpiPsp(orderPayUpiPsp);
        Assertions.assertThat(jsonPath.getString("body.resultCode")).isEqualTo("ACCEPTED_SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.resultCodeId")).isEqualTo("001");
        Assertions.assertThat(jsonPath.getString("body.resultMsg")).isEqualTo("accepted success");
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.DOWN_STEAM_REQUEST_RESPONSE,orderPayUpiPsp.getBody().getOrderId(), Create_Order_And_PAY, REQUEST);
        System.out.println(logsResponse);
        Assertions.assertThat(logsResponse).contains("deemedTxnInfo");
        Assertions.assertThat(logsResponse).contains("\"txnStatus\":\"SUCCESS\"");
        Assertions.assertThat(logsResponse).contains("\"fundBackTrigger\":true");
        Assertions.assertThat(logsResponse).contains("\"reason\":\"delayed callback from bank \"");
        Assertions.assertThat(logsResponse).contains("\"flowType\":\"3P_SQR\"");
        TxnStatus txnStatus = new TxnStatus(orderPayUpiPsp.getBody().getMid(), orderPayUpiPsp.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderPayUpiPsp.getBody().getOrderId())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.YESSQR.toString())
                .validateRespCode("810")
                .validateTxnAmount(orderPayUpiPsp.getBody().getTxnAmount())
                .validateMid(orderPayUpiPsp.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }
}
