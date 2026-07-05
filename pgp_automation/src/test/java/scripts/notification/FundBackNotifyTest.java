package scripts.notification;

import com.paytm.api.notification.FundBackNotify;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Test;


public class FundBackNotifyTest extends PGPBaseTest {

    @Owner("Ajeesh")
    @Test(description = "FundBackNotify CC ORDER_IS_CLOSED")
    public void Test_001_FundBackNotify_CC_closed() throws Exception {
        String orderID = CommonHelpers.generateOrderId();
        Constants.MerchantType mid = Constants.MerchantType.Notification_RTDD;
        FundBackNotify fundBackNotify =new FundBackNotify();
        JsonPath response = fundBackNotify
                .setAcquirementID("20230530011010000870276684811968810")
                .setResultStatus("S")
                .setResultCode("SUCCESS")
                .setResultCodeID("00000000")
                .setResultMsg("SUCCESS")
                .setFundBackReason("ORDER_IS_CLOSED")
                .setMerchantID(mid.getId())
                .setMerchantTransId(orderID)
                .setIssuingBank("ICICI Bank")
                .setCardScheme("VISA")
                .setPayMethod("CREDIT_CARD")
                .execute().jsonPath();

        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");

    }

    @Owner("Akshat")
    @Test(description = "FundBackNotify CC ORDER_IS_EXPIRED")
    public void Test_002_FundBackNotify_CC_expired() throws Exception {
        String orderID = CommonHelpers.generateOrderId();
        Constants.MerchantType mid = Constants.MerchantType.Notification_RTDD;
        FundBackNotify fundBackNotify =new FundBackNotify();
        JsonPath response = fundBackNotify
                .setAcquirementID("20230530011010000870276684811968811")
                .setResultStatus("S")
                .setResultCode("SUCCESS")
                .setResultCodeID("00000000")
                .setResultMsg("SUCCESS")
                .setFundBackReason("ORDER_IS_EXPIRED")
                .setMerchantID(mid.getId())
                .setMerchantTransId(orderID)
                .setIssuingBank("ICICI Bank")
                .setCardScheme("VISA")
                .setPayMethod("CREDIT_CARD")
                .execute().jsonPath();

        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");

    }

    @Owner("Akshat")
    @Test(description = "FundBackNotify CC REPEAT_PAY")
    public void Test_003_FundBackNotify_CC_repeatPay() throws Exception {
        String orderID = CommonHelpers.generateOrderId();
        Constants.MerchantType mid = Constants.MerchantType.Notification_RTDD;
        FundBackNotify fundBackNotify =new FundBackNotify();
        JsonPath response = fundBackNotify
                .setAcquirementID("20230530011010000870276684811968812")
                .setResultStatus("S")
                .setResultCode("SUCCESS")
                .setResultCodeID("00000000")
                .setResultMsg("SUCCESS")
                .setFundBackReason("REPEAT_PAY")
                .setMerchantID(mid.getId())
                .setMerchantTransId(orderID)
                .setIssuingBank("ICICI Bank")
                .setCardScheme("VISA")
                .setPayMethod("CREDIT_CARD")
                .execute().jsonPath();

        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");


    }

    @Owner("Akshat")
    @Test(description = "FundBackNotify UPI ORDER_IS_CLOSED")
    public void Test_004_FundBackNotify_UPI_closed() throws Exception {
        String orderID = CommonHelpers.generateOrderId();
        Constants.MerchantType mid = Constants.MerchantType.Notification_RTDD;
        FundBackNotify fundBackNotify =new FundBackNotify();
        JsonPath response = fundBackNotify
                .setAcquirementID("20230530011010000870276684811968813")
                .setResultStatus("S")
                .setResultCode("SUCCESS")
                .setResultCodeID("00000000")
                .setResultMsg("SUCCESS")
                .setFundBackReason("ORDER_IS_CLOSED")
                .setMerchantID(mid.getId())
                .setMerchantTransId(orderID)
                .setPayMethod("UPI")
                .setVirtualPaymentAddress("9972953111@paytm")
                .execute().jsonPath();

        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");

    }

    @Owner("Akshat")
    @Test(description = "FundBackNotify UPI ORDER_IS_EXPIRED")
    public void Test_005_FundBackNotify_UPI_expired() throws Exception {
        String orderID = CommonHelpers.generateOrderId();
        Constants.MerchantType mid = Constants.MerchantType.Notification_RTDD;
        FundBackNotify fundBackNotify =new FundBackNotify();
        JsonPath response = fundBackNotify
                .setAcquirementID("20230530011010000870276684811968814")
                .setResultStatus("S")
                .setResultCode("SUCCESS")
                .setResultCodeID("00000000")
                .setResultMsg("SUCCESS")
                .setFundBackReason("ORDER_IS_EXPIRED")
                .setMerchantID(mid.getId())
                .setMerchantTransId(orderID)
                .setPayMethod("UPI")
                .setVirtualPaymentAddress("9972953222@paytm")
                .execute().jsonPath();

        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");

    }

    @Owner("Akshat")
    @Test(description = "FundBackNotify UPI REPEAT_PAY")
    public void Test_006_FundBackNotify_UPI_repeatPay() throws Exception {
        String orderID = CommonHelpers.generateOrderId();
        Constants.MerchantType mid = Constants.MerchantType.Notification_RTDD;
        FundBackNotify fundBackNotify =new FundBackNotify();
        JsonPath response = fundBackNotify
                .setAcquirementID("20230530011010000870276684811968815")
                .setResultStatus("S")
                .setResultCode("SUCCESS")
                .setResultCodeID("00000000")
                .setResultMsg("SUCCESS")
                .setFundBackReason("REPEAT_PAY")
                .setMerchantID(mid.getId())
                .setMerchantTransId(orderID)
                .setPayMethod("UPI")
                .setVirtualPaymentAddress("9972953333@paytm")
                .execute().jsonPath();

        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");

    }

    @Owner("Akshat")
    @Test(description = "FundBackNotify DC ORDER_IS_CLOSED")
    public void Test_007_FundBackNotify_DC_closed() throws Exception {
        String orderID = CommonHelpers.generateOrderId();
        Constants.MerchantType mid = Constants.MerchantType.Notification_RTDD;
        FundBackNotify fundBackNotify =new FundBackNotify();
        JsonPath response = fundBackNotify
                .setAcquirementID("20230530011010000870276684811968816")
                .setResultStatus("S")
                .setResultCode("SUCCESS")
                .setResultCodeID("00000000")
                .setResultMsg("SUCCESS")
                .setFundBackReason("ORDER_IS_CLOSED")
                .setMerchantID(mid.getId())
                .setMerchantTransId(orderID)
                .setIssuingBank("HDFC Bank")
                .setCardScheme("MASTER")
                .setPayMethod("DEBIT_CARD")
                .execute().jsonPath();

        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");

    }

    @Owner("Akshat")
    @Test(description = "FundBackNotify DC ORDER_IS_EXPIRED")
    public void Test_008_FundBackNotify_DC_expired() throws Exception {
        String orderID = CommonHelpers.generateOrderId();
        Constants.MerchantType mid = Constants.MerchantType.Notification_RTDD;
        FundBackNotify fundBackNotify =new FundBackNotify();
        JsonPath response = fundBackNotify
                .setAcquirementID("20230530011010000870276684811968817")
                .setResultStatus("S")
                .setResultCode("SUCCESS")
                .setResultCodeID("00000000")
                .setResultMsg("SUCCESS")
                .setFundBackReason("ORDER_IS_EXPIRED")
                .setMerchantID(mid.getId())
                .setMerchantTransId(orderID)
                .setIssuingBank("HDFC Bank")
                .setCardScheme("MASTER")
                .setPayMethod("DEBIT_CARD")
                .execute().jsonPath();

        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");

    }

    @Owner("Akshat")
    @Test(description = "FundBackNotify DC REPEAT_PAY")
    public void Test_009_FundBackNotify_DC_repeatPay() throws Exception {
        String orderID = CommonHelpers.generateOrderId();
        Constants.MerchantType mid = Constants.MerchantType.Notification_RTDD;
        FundBackNotify fundBackNotify =new FundBackNotify();
        JsonPath response = fundBackNotify
                .setAcquirementID("20230530011010000870276684811968812")
                .setResultStatus("S")
                .setResultCode("SUCCESS")
                .setResultCodeID("00000000")
                .setResultMsg("SUCCESS")
                .setFundBackReason("REPEAT_PAY")
                .setMerchantID(mid.getId())
                .setMerchantTransId(orderID)
                .setIssuingBank("HDFC Bank")
                .setCardScheme("MASTER")
                .setPayMethod("DEBIT_CARD")
                .execute().jsonPath();

        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");

    }

    @Owner("Akshat")
    @Test(description = "FundBackNotify Balance ORDER_IS_CLOSED")
    public void Test_010_FundBackNotify_Balance_closed() throws Exception {
        String orderID = CommonHelpers.generateOrderId();
        Constants.MerchantType mid = Constants.MerchantType.Notification_RTDD;
        FundBackNotify fundBackNotify =new FundBackNotify();
        JsonPath response = fundBackNotify
                .setAcquirementID("20230530011010000870276684811968816")
                .setResultStatus("S")
                .setResultCode("SUCCESS")
                .setResultCodeID("00000000")
                .setResultMsg("SUCCESS")
                .setFundBackReason("ORDER_IS_CLOSED")
                .setMerchantID(mid.getId())
                .setMerchantTransId(orderID)
                .setPayMethod("BALANCE")
                .execute().jsonPath();

        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");

    }

    @Owner("Akshat")
    @Test(description = "FundBackNotify BALANCE ORDER_IS_EXPIRED")
    public void Test_011_FundBackNotify_BALANCE_expired() throws Exception {
        String orderID = CommonHelpers.generateOrderId();
        Constants.MerchantType mid = Constants.MerchantType.Notification_RTDD;
        FundBackNotify fundBackNotify =new FundBackNotify();
        JsonPath response = fundBackNotify
                .setAcquirementID("20230530011010000870276684811968817")
                .setResultStatus("S")
                .setResultCode("SUCCESS")
                .setResultCodeID("00000000")
                .setResultMsg("SUCCESS")
                .setFundBackReason("ORDER_IS_EXPIRED")
                .setMerchantID(mid.getId())
                .setMerchantTransId(orderID)
                .setPayMethod("BALANCE")
                .execute().jsonPath();

        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");

    }

    @Owner("Akshat")
    @Test(description = "FundBackNotify BALANCE REPEAT_PAY")
    public void Test_012_FundBackNotify_BALANCE_repeatPay() throws Exception {
        String orderID = CommonHelpers.generateOrderId();
        Constants.MerchantType mid = Constants.MerchantType.Notification_RTDD;
        FundBackNotify fundBackNotify =new FundBackNotify();
        JsonPath response = fundBackNotify
                .setAcquirementID("20230530011010000870276684811968812")
                .setResultStatus("S")
                .setResultCode("SUCCESS")
                .setResultCodeID("00000000")
                .setResultMsg("SUCCESS")
                .setFundBackReason("REPEAT_PAY")
                .setMerchantID(mid.getId())
                .setMerchantTransId(orderID)
                .setPayMethod("BALANCE")
                .execute().jsonPath();

        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");

    }
}
