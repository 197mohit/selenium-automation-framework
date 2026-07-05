package scripts.notification;
import com.paytm.api.notification.CaptureNotify;
import com.paytm.api.notification.MerchantNotify;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.ArrayList;

public class MerchantNotifyTest extends PGPBaseTest{

    @Owner("Akshat")
    @Feature("PGP-46601")
    @Test(description = "Merchant Notify Whatsapp Fail Case001")

    public void Merchant_Notify_Fail_001() throws Exception {

        ArrayList<String> channel = new ArrayList<>();
        channel.add("WHATSAPP");
        MerchantNotify merchantNotify= new MerchantNotify();
        merchantNotify.setNotificationStatus("FAILED");
        merchantNotify.setErrorCode("FGW_PPBL_ACCOUNT_FROZEN");
        merchantNotify.setPreviousTxnStatus("TXN_PENDING");
        merchantNotify.setMerchantId("216820000893549400346");
        merchantNotify.setRequestId("AWSPG20230628000121682000089354940034604");
        merchantNotify.setSettleStrategy("REALTIME_SETTLEMENT");
        merchantNotify.setSettleType("onlineSettlement");
        merchantNotify.setChannel(channel);
        merchantNotify.setReqMsgId();
        JsonPath response=merchantNotify.execute().jsonPath();
        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");
        String merchantNotifyLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,merchantNotify.getReqMsgId(),"MerchantSettlementNotify");
        merchantNotifyLogs.contains("\\\"resultMsg\\\":\\\"Success\\\"");
        merchantNotifyLogs.contains("\\\"resultStatus\\\":\\\"S\\\"");
        merchantNotifyLogs.contains("\\\"resultCode\\\":\\\"SUCCESS\\\"");

    }

    @Owner("Akshat")
    @Feature("PGP-46601")
    @Test(description = "Merchant Notify Whatsapp Fail Case002")
    public void Merchant_Notify_Fail_002() throws Exception {

        ArrayList<String> channel = new ArrayList<>();
        channel.add("WHATSAPP");
        MerchantNotify merchantNotify= new MerchantNotify();
        merchantNotify.setNotificationStatus("FAILED");
        merchantNotify.setErrorCode("FGW_PPBL_ACCOUNT_DORMANT");
        merchantNotify.setPreviousTxnStatus("TXN_PENDING");
        merchantNotify.setMerchantId("216820000893549400346");
        merchantNotify.setRequestId("AWSPG20230628000121682000089354940034604");
        merchantNotify.setSettleStrategy("REALTIME_SETTLEMENT");
        merchantNotify.setSettleType("onlineSettlement");
        merchantNotify.setChannel(channel);
        merchantNotify.setReqMsgId();
        JsonPath response=merchantNotify.execute().jsonPath();
        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");
        String merchantNotifyLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,merchantNotify.getReqMsgId(),"MerchantSettlementNotify");
        merchantNotifyLogs.contains("\\\"resultMsg\\\":\\\"Success\\\"");
        merchantNotifyLogs.contains("\\\"resultStatus\\\":\\\"S\\\"");
        merchantNotifyLogs.contains("\\\"resultCode\\\":\\\"SUCCESS\\\"");

    }

    @Owner("Akshat")
    @Feature("PGP-46601")
    @Test(description = "Merchant Notify Whatsapp Fail Case003")
    public void Merchant_Notify_Fail_003() throws Exception {

        ArrayList<String> channel = new ArrayList<>();
        channel.add("WHATSAPP");
        MerchantNotify merchantNotify= new MerchantNotify();
        merchantNotify.setNotificationStatus("FAILED");
        merchantNotify.setErrorCode("FGW_PPBL_ACCOUNT_CLOSED");
        merchantNotify.setPreviousTxnStatus("TXN_PENDING");
        merchantNotify.setMerchantId("216820000893549400346");
        merchantNotify.setRequestId("AWSPG20230628000121682000089354940034604");
        merchantNotify.setSettleStrategy("REALTIME_SETTLEMENT");
        merchantNotify.setSettleType("onlineSettlement");
        merchantNotify.setChannel(channel);
        merchantNotify.setReqMsgId();
        JsonPath response=merchantNotify.execute().jsonPath();
        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");
        String merchantNotifyLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,merchantNotify.getReqMsgId(),"MerchantSettlementNotify");
        merchantNotifyLogs.contains("\\\"resultMsg\\\":\\\"Success\\\"");
        merchantNotifyLogs.contains("\\\"resultStatus\\\":\\\"S\\\"");
        merchantNotifyLogs.contains("\\\"resultCode\\\":\\\"SUCCESS\\\"");

    }

    @Owner("Akshat")
    @Feature("PGP-46601")
    @Test(description = "Merchant Notify Whatsapp Fail Case004")
    public void Merchant_Notify_Fail_004() throws Exception {

        ArrayList<String> channel = new ArrayList<>();
        channel.add("WHATSAPP");
        MerchantNotify merchantNotify= new MerchantNotify();
        merchantNotify.setNotificationStatus("FAILED");
        merchantNotify.setErrorCode("FGW_PPBL_AMOUNT_LIMIT_NO_PAN");
        merchantNotify.setPreviousTxnStatus("TXN_PENDING");
        merchantNotify.setMerchantId("216820000893549400346");
        merchantNotify.setRequestId("AWSPG20230628000121682000089354940034604");
        merchantNotify.setSettleStrategy("REALTIME_SETTLEMENT");
        merchantNotify.setSettleType("onlineSettlement");
        merchantNotify.setChannel(channel);
        merchantNotify.setReqMsgId();
        JsonPath response=merchantNotify.execute().jsonPath();
        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");
        String merchantNotifyLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,merchantNotify.getReqMsgId(),"MerchantSettlementNotify");
        merchantNotifyLogs.contains("\\\"resultMsg\\\":\\\"Success\\\"");
        merchantNotifyLogs.contains("\\\"resultStatus\\\":\\\"S\\\"");
        merchantNotifyLogs.contains("\\\"resultCode\\\":\\\"SUCCESS\\\"");

    }

    @Owner("Akshat")
    @Feature("PGP-46601")
    @Test(description = "Merchant Notify Whatsapp Fail Case005")
    public void Merchant_Notify_Fail_005() throws Exception {

        ArrayList<String> channel = new ArrayList<>();
        channel.add("WHATSAPP");
        MerchantNotify merchantNotify= new MerchantNotify();
        merchantNotify.setNotificationStatus("FAILED");
        merchantNotify.setErrorCode("FGW_PPBL_AMOUNT_LIMIT_EXCEEDED");
        merchantNotify.setPreviousTxnStatus("TXN_PENDING");
        merchantNotify.setMerchantId("216820000893549400346");
        merchantNotify.setRequestId("AWSPG20230628000121682000089354940034604");
        merchantNotify.setSettleStrategy("REALTIME_SETTLEMENT");
        merchantNotify.setSettleType("onlineSettlement");
        merchantNotify.setChannel(channel);
        merchantNotify.setReqMsgId();
        JsonPath response=merchantNotify.execute().jsonPath();
        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");
        String merchantNotifyLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,merchantNotify.getReqMsgId(),"MerchantSettlementNotify");
        merchantNotifyLogs.contains("\\\"resultMsg\\\":\\\"Success\\\"");
        merchantNotifyLogs.contains("\\\"resultStatus\\\":\\\"S\\\"");
        merchantNotifyLogs.contains("\\\"resultCode\\\":\\\"SUCCESS\\\"");

    }

    @Owner("Akshat")
    @Feature("PGP-46601")
    @Test(description = "Merchant Notify Whatsapp Fail Case006")
    public void Merchant_Notify_Fail_006() throws Exception {

        ArrayList<String> channel = new ArrayList<>();
        channel.add("WHATSAPP");
        MerchantNotify merchantNotify= new MerchantNotify();
        merchantNotify.setNotificationStatus("FAILED");
        merchantNotify.setErrorCode("FGW_PPBL_Risk_Rejected");
        merchantNotify.setPreviousTxnStatus("TXN_PENDING");
        merchantNotify.setMerchantId("216820000893549400346");
        merchantNotify.setRequestId("AWSPG20230628000121682000089354940034604");
        merchantNotify.setSettleStrategy("REALTIME_SETTLEMENT");
        merchantNotify.setSettleType("onlineSettlement");
        merchantNotify.setChannel(channel);
        merchantNotify.setReqMsgId();
        JsonPath response=merchantNotify.execute().jsonPath();
        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");
        String merchantNotifyLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,merchantNotify.getReqMsgId(),"MerchantSettlementNotify");
        merchantNotifyLogs.contains("\\\"resultMsg\\\":\\\"Success\\\"");
        merchantNotifyLogs.contains("\\\"resultStatus\\\":\\\"S\\\"");
        merchantNotifyLogs.contains("\\\"resultCode\\\":\\\"SUCCESS\\\"");

    }

    @Owner("Akshat")
    @Feature("PGP-46601")
    @Test(description = "Merchant Notify Whatsapp Fail Case007")
    public void Merchant_Notify_Fail_007() throws Exception {

        ArrayList<String> channel = new ArrayList<>();
        channel.add("WHATSAPP");
        MerchantNotify merchantNotify= new MerchantNotify();
        merchantNotify.setNotificationStatus("FAILED");
        merchantNotify.setErrorCode("FGW_PPBL_BC_account_Rejection");
        merchantNotify.setPreviousTxnStatus("TXN_PENDING");
        merchantNotify.setMerchantId("216820000893549400346");
        merchantNotify.setRequestId("AWSPG20230628000121682000089354940034604");
        merchantNotify.setSettleStrategy("REALTIME_SETTLEMENT");
        merchantNotify.setSettleType("onlineSettlement");
        merchantNotify.setChannel(channel);
        merchantNotify.setReqMsgId();
        JsonPath response=merchantNotify.execute().jsonPath();
        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");
        String merchantNotifyLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,merchantNotify.getReqMsgId(),"MerchantSettlementNotify");
        merchantNotifyLogs.contains("\\\"resultMsg\\\":\\\"Success\\\"");
        merchantNotifyLogs.contains("\\\"resultStatus\\\":\\\"S\\\"");
        merchantNotifyLogs.contains("\\\"resultCode\\\":\\\"SUCCESS\\\"");

    }

    @Owner("Akshat")
    @Feature("PGP-46601")
    @Test(description = "Merchant Notify Whatsapp Fail Case008")
    public void Merchant_Notify_Fail_008() throws Exception {

        ArrayList<String> channel = new ArrayList<>();
        channel.add("WHATSAPP");
        MerchantNotify merchantNotify= new MerchantNotify();
        merchantNotify.setNotificationStatus("FAILED");
        merchantNotify.setErrorCode("FGW_PPBL_CREDIT_COUNT_LIMIT_EXCEEDED");
        merchantNotify.setPreviousTxnStatus("TXN_PENDING");
        merchantNotify.setMerchantId("216820000893549400346");
        merchantNotify.setRequestId("AWSPG20230628000121682000089354940034604");
        merchantNotify.setSettleStrategy("REALTIME_SETTLEMENT");
        merchantNotify.setSettleType("onlineSettlement");
        merchantNotify.setChannel(channel);
        merchantNotify.setReqMsgId();
        JsonPath response=merchantNotify.execute().jsonPath();
        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");
        String merchantNotifyLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,merchantNotify.getReqMsgId(),"MerchantSettlementNotify");
        merchantNotifyLogs.contains("\\\"resultMsg\\\":\\\"Success\\\"");
        merchantNotifyLogs.contains("\\\"resultStatus\\\":\\\"S\\\"");
        merchantNotifyLogs.contains("\\\"resultCode\\\":\\\"SUCCESS\\\"");

    }

    @Owner("Akshat")
    @Feature("PGP-46601")
    @Test(description = "Merchant Notify Whatsapp Success Case001")
    public void Merchant_Notify_Success_001() throws Exception {

        ArrayList<String> channel = new ArrayList<>();
        channel.add("WHATSAPP");
        MerchantNotify merchantNotify= new MerchantNotify();
        merchantNotify.setNotificationStatus("SUCCESS");
        merchantNotify.setErrorCode("SUCCESS");
        merchantNotify.setPreviousTxnStatus("TXN_PENDING");
        merchantNotify.setMerchantId("216820000893549400346");
        merchantNotify.setRequestId("AWSPG20230628000121682000089354940034604");
        merchantNotify.setSettleStrategy("REALTIME_SETTLEMENT");
        merchantNotify.setSettleType("onlineSettlement");
        merchantNotify.setChannel(channel);
        merchantNotify.setReqMsgId();
/*        JsonPath response=merchantNotify.execute().jsonPath();
        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");*/
        String merchantNotifyLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,merchantNotify.getReqMsgId(),"MerchantSettlementNotify");
        merchantNotifyLogs.contains("\\\"resultMsg\\\":\\\"Success\\\"");
        merchantNotifyLogs.contains("\\\"resultStatus\\\":\\\"S\\\"");
        merchantNotifyLogs.contains("\\\"resultCode\\\":\\\"SUCCESS\\\"");

    }

    @Owner("Akshat")
    @Feature("PGP-50442")
    @Test(description = "Merchant Notify SMS case for earlier allowed retry counts ")
    public void Merchant_Notify_SMS_OS_001() throws Exception {

        /*Earlier allowed Count : sts.settlement.os.sms.retry.intervals=3,19,25,29,30,31,32,33,34,35,36,37,38,39,40*/

        ArrayList<String> channel = new ArrayList<>();
        channel.add("SMS");
        MerchantNotify merchantNotify= new MerchantNotify();
        merchantNotify.setNotificationStatus("FAILED");
        merchantNotify.setErrorCode("FGW_Wallet_FROZEN");
        merchantNotify.setPreviousTxnStatus("TXN_PENDING");
        merchantNotify.setMerchantId("216820000893549400346");
        merchantNotify.setRequestId("AWSPG20230628000121682000089354940034605");
        merchantNotify.setSettleStrategy("ONLINE_SETTLEMENT");
        merchantNotify.setSettleType("onlineSettlement");
        merchantNotify.setbizType("ONLINE_SETTLEMENT");
        merchantNotify.setretryCount("3");
        merchantNotify.setChannel(channel);
        merchantNotify.setReqMsgId();
        JsonPath response=merchantNotify.execute().jsonPath();
        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");
        String merchantNotifyLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,merchantNotify.getReqMsgId(),"MerchantSettlementNotify");
        merchantNotifyLogs.contains("\\\"resultMsg\\\":\\\"Success\\\"");
        merchantNotifyLogs.contains("\\\"resultStatus\\\":\\\"S\\\"");
        merchantNotifyLogs.contains("\\\"resultCode\\\":\\\"SUCCESS\\\"");

    }

    @Owner("Akshat")
    @Feature("PGP-50442")
    @Test(description = "Merchant Notify SMS case for all retry counts ")
    public void Merchant_Notify_SMS_OS_002() throws Exception {

        /*Earlier allowed Count : sts.settlement.os.sms.retry.intervals=3,19,25,29,30,31,32,33,34,35,36,37,38,39,40*/

        ArrayList<String> channel = new ArrayList<>();
        channel.add("SMS");
        MerchantNotify merchantNotify= new MerchantNotify();
        merchantNotify.setNotificationStatus("FAILED");
        merchantNotify.setErrorCode("FGW_Wallet_FROZEN");
        merchantNotify.setPreviousTxnStatus("TXN_PENDING");
        merchantNotify.setMerchantId("216820000893549400346");
        merchantNotify.setRequestId("AWSPG20230628000121682000089354940034615");
        merchantNotify.setSettleStrategy("ONLINE_SETTLEMENT");
        merchantNotify.setSettleType("onlineSettlement");
        merchantNotify.setbizType("ONLINE_SETTLEMENT");
        merchantNotify.setretryCount("41");
        merchantNotify.setChannel(channel);
        merchantNotify.setReqMsgId();
        JsonPath response=merchantNotify.execute().jsonPath();
        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");
        String merchantNotifyLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,merchantNotify.getReqMsgId(),"MerchantSettlementNotify");
        merchantNotifyLogs.contains("\\\"resultMsg\\\":\\\"Success\\\"");
        merchantNotifyLogs.contains("\\\"resultStatus\\\":\\\"S\\\"");
        merchantNotifyLogs.contains("\\\"resultCode\\\":\\\"SUCCESS\\\"");

    }

    @Owner("Akshat")
    @Feature("PGP-50442")
    @Test(description = "Merchant Notify Push case for earlier allowed retry counts ")
    public void Merchant_Notify_Push_OS_001() throws Exception {

        /*Earlier allowed Count : sts.settlement.os.push.retry.intervals=3,19,25,29,30,31,32,33,34,35,36,37,38,39,40*/

        ArrayList<String> channel = new ArrayList<>();
        channel.add("PUSH");
        MerchantNotify merchantNotify= new MerchantNotify();
        merchantNotify.setNotificationStatus("FAILED");
        merchantNotify.setErrorCode("FGW_Wallet_FROZEN");
        merchantNotify.setPreviousTxnStatus("TXN_PENDING");
        merchantNotify.setMerchantId("216820000893549400346");
        merchantNotify.setRequestId("AWSPG20230628000121682000089354940034625");
        merchantNotify.setSettleStrategy("ONLINE_SETTLEMENT");
        merchantNotify.setSettleType("onlineSettlement");
        merchantNotify.setbizType("ONLINE_SETTLEMENT");
        merchantNotify.setretryCount("3");
        merchantNotify.setChannel(channel);
        merchantNotify.setReqMsgId();
        JsonPath response=merchantNotify.execute().jsonPath();
        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");
        String merchantNotifyLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,merchantNotify.getReqMsgId(),"MerchantSettlementNotify");
        merchantNotifyLogs.contains("\\\"resultMsg\\\":\\\"Success\\\"");
        merchantNotifyLogs.contains("\\\"resultStatus\\\":\\\"S\\\"");
        merchantNotifyLogs.contains("\\\"resultCode\\\":\\\"SUCCESS\\\"");

    }

    @Owner("Akshat")
    @Feature("PGP-50442")
    @Test(description = "Merchant Notify Push case for all retry counts ")
    public void Merchant_Notify_Push_OS_002() throws Exception {

        /*Earlier allowed Count : sts.settlement.os.push.retry.intervals=3,19,25,29,30,31,32,33,34,35,36,37,38,39,40*/

        ArrayList<String> channel = new ArrayList<>();
        channel.add("PUSH");
        MerchantNotify merchantNotify= new MerchantNotify();
        merchantNotify.setNotificationStatus("FAILED");
        merchantNotify.setErrorCode("FGW_Wallet_FROZEN");
        merchantNotify.setPreviousTxnStatus("TXN_PENDING");
        merchantNotify.setMerchantId("216820000893549400346");
        merchantNotify.setRequestId("AWSPG20230628000121682000089354940034635");
        merchantNotify.setSettleStrategy("ONLINE_SETTLEMENT");
        merchantNotify.setSettleType("onlineSettlement");
        merchantNotify.setbizType("ONLINE_SETTLEMENT");
        merchantNotify.setretryCount("41");
        merchantNotify.setChannel(channel);
        merchantNotify.setReqMsgId();
        JsonPath response=merchantNotify.execute().jsonPath();
        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");
        String merchantNotifyLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,merchantNotify.getReqMsgId(),"MerchantSettlementNotify");
        merchantNotifyLogs.contains("\\\"resultMsg\\\":\\\"Success\\\"");
        merchantNotifyLogs.contains("\\\"resultStatus\\\":\\\"S\\\"");
        merchantNotifyLogs.contains("\\\"resultCode\\\":\\\"SUCCESS\\\"");

    }

    @Owner("Akshat")
    @Feature("PGP-50442")
    @Test(description = "Merchant Notify SMS case for earlier allowed retry counts ")
    public void Merchant_Notify_SMS_TWS_001() throws Exception {

        /*Earlier allowed Count : sts.settlement.tws.sms.retry.intervals=2,11,14,17,20,21,22,23,24,25*/

        ArrayList<String> channel = new ArrayList<>();
        channel.add("SMS");
        MerchantNotify merchantNotify= new MerchantNotify();
        merchantNotify.setNotificationStatus("FAILED");
        merchantNotify.setErrorCode("FGW_Wallet_FROZEN");
        merchantNotify.setPreviousTxnStatus("TXN_PENDING");
        merchantNotify.setMerchantId("216820000893549400346");
        merchantNotify.setRequestId("AWSPG20230628000121682000089354940034645");
        merchantNotify.setSettleStrategy("TRANSACTION_WISE_SETTLEMENT");
        merchantNotify.setSettleType("onlineSettlement");
        merchantNotify.setbizType("TRANSACTION_WISE_SETTLEMENT");
        merchantNotify.setretryCount("25");
        merchantNotify.setChannel(channel);
        merchantNotify.setReqMsgId();
        JsonPath response=merchantNotify.execute().jsonPath();
        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");
        String merchantNotifyLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,merchantNotify.getReqMsgId(),"MerchantSettlementNotify");
        merchantNotifyLogs.contains("\\\"resultMsg\\\":\\\"Success\\\"");
        merchantNotifyLogs.contains("\\\"resultStatus\\\":\\\"S\\\"");
        merchantNotifyLogs.contains("\\\"resultCode\\\":\\\"SUCCESS\\\"");

    }

    @Owner("Akshat")
    @Feature("PGP-50442")
    @Test(description = "Merchant Notify SMS case for all retry counts ")
    public void Merchant_Notify_SMS_TWS_002() throws Exception {

        /*Earlier allowed Count : sts.settlement.tws.sms.retry.intervals=2,11,14,17,20,21,22,23,24,25*/

        ArrayList<String> channel = new ArrayList<>();
        channel.add("SMS");
        MerchantNotify merchantNotify= new MerchantNotify();
        merchantNotify.setNotificationStatus("FAILED");
        merchantNotify.setErrorCode("FGW_Wallet_FROZEN");
        merchantNotify.setPreviousTxnStatus("TXN_PENDING");
        merchantNotify.setMerchantId("216820000893549400346");
        merchantNotify.setRequestId("AWSPG20230628000121682000089354940034655");
        merchantNotify.setSettleStrategy("TRANSACTION_WISE_SETTLEMENT");
        merchantNotify.setSettleType("onlineSettlement");
        merchantNotify.setbizType("TRANSACTION_WISE_SETTLEMENT");
        merchantNotify.setretryCount("26");
        merchantNotify.setChannel(channel);
        merchantNotify.setReqMsgId();
        JsonPath response=merchantNotify.execute().jsonPath();
        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");
        String merchantNotifyLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,merchantNotify.getReqMsgId(),"MerchantSettlementNotify");
        merchantNotifyLogs.contains("\\\"resultMsg\\\":\\\"Success\\\"");
        merchantNotifyLogs.contains("\\\"resultStatus\\\":\\\"S\\\"");
        merchantNotifyLogs.contains("\\\"resultCode\\\":\\\"SUCCESS\\\"");

    }

    @Owner("Akshat")
    @Feature("PGP-50442")
    @Test(description = "Merchant Notify Push case for earlier allowed retry counts ")
    public void Merchant_Notify_Push_TWS_001() throws Exception {

        /*Earlier allowed Count : sts.settlement.tws.push.retry.intervals=1,11,14,17,20,21,22,23,24,25*/

        ArrayList<String> channel = new ArrayList<>();
        channel.add("PUSH");
        MerchantNotify merchantNotify= new MerchantNotify();
        merchantNotify.setNotificationStatus("FAILED");
        merchantNotify.setErrorCode("FGW_Wallet_FROZEN");
        merchantNotify.setPreviousTxnStatus("TXN_PENDING");
        merchantNotify.setMerchantId("216820000893549400346");
        merchantNotify.setRequestId("AWSPG20230628000121682000089354940034665");
        merchantNotify.setSettleStrategy("TRANSACTION_WISE_SETTLEMENT");
        merchantNotify.setSettleType("onlineSettlement");
        merchantNotify.setbizType("TRANSACTION_WISE_SETTLEMENT");
        merchantNotify.setretryCount("25");
        merchantNotify.setChannel(channel);
        merchantNotify.setReqMsgId();
        JsonPath response=merchantNotify.execute().jsonPath();
        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");
        String merchantNotifyLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,merchantNotify.getReqMsgId(),"MerchantSettlementNotify");
        merchantNotifyLogs.contains("\\\"resultMsg\\\":\\\"Success\\\"");
        merchantNotifyLogs.contains("\\\"resultStatus\\\":\\\"S\\\"");
        merchantNotifyLogs.contains("\\\"resultCode\\\":\\\"SUCCESS\\\"");

    }

    @Owner("Akshat")
    @Feature("PGP-50442")
    @Test(description = "Merchant Notify Push case for all retry counts ")
    public void Merchant_Notify_Push_TWS_002() throws Exception {

        /*Earlier allowed Count : sts.settlement.tws.push.retry.intervals=1,11,14,17,20,21,22,23,24,25*/

        ArrayList<String> channel = new ArrayList<>();
        channel.add("PUSH");
        MerchantNotify merchantNotify= new MerchantNotify();
        merchantNotify.setNotificationStatus("FAILED");
        merchantNotify.setErrorCode("FGW_Wallet_FROZEN");
        merchantNotify.setPreviousTxnStatus("TXN_PENDING");
        merchantNotify.setMerchantId("216820000893549400346");
        merchantNotify.setRequestId("AWSPG20230628000121682000089354940034675");
        merchantNotify.setSettleStrategy("TRANSACTION_WISE_SETTLEMENT");
        merchantNotify.setSettleType("onlineSettlement");
        merchantNotify.setbizType("TRANSACTION_WISE_SETTLEMENT");
        merchantNotify.setretryCount("26");
        merchantNotify.setChannel(channel);
        merchantNotify.setReqMsgId();
        JsonPath response=merchantNotify.execute().jsonPath();
        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");
        String merchantNotifyLogs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification,merchantNotify.getReqMsgId(),"MerchantSettlementNotify");
        merchantNotifyLogs.contains("\\\"resultMsg\\\":\\\"Success\\\"");
        merchantNotifyLogs.contains("\\\"resultStatus\\\":\\\"S\\\"");
        merchantNotifyLogs.contains("\\\"resultCode\\\":\\\"SUCCESS\\\"");

    }

}