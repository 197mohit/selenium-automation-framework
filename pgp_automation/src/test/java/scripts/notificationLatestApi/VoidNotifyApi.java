package scripts.notificationLatestApi;
import com.paytm.api.notification.VoidNotify;
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
import org.testng.asserts.Assertion;

public class VoidNotifyApi extends  PGPBaseTest{

    @Owner("Akshat")
    @Test(description = "VoidNotify order Closed")
    public void Test_001_VoidNotify_orderClosed() throws Exception {
        String orderID = CommonHelpers.generateOrderId();
        com.paytm.api.notification.VoidNotify voidNotify =new VoidNotify();
        Constants.MerchantType mid = Constants.MerchantType.pushCloseNotify;
        JsonPath response = voidNotify
                .setVoidId("20230607010890000873236257407818166")
                .setMerchantTransID(orderID)
                .setAcquirementID("20230607010890000873236257407818166")
                .setMID(mid.getId())
                .setvoidSource("MERCHANT")
                .setvoidReason("edc-update-rec-void")
                .setResultStatus("S")
                .setResultCode("SUCCESS")
                .setResultCodeID("00000000")
                .setResultMsg("SUCCESS")
                .setOrderStatus("CLOSED")
                .execute().jsonPath();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification, orderID);
        Assertions.assertThat(logs).contains("\"function\":\"oldpg.acquiring.order.voidNotify\"");
        Assertions.assertThat(logs).contains("\"orderStatus\":\"CLOSED\"");
        Assertions.assertThat(logs).contains("BUSINESS_FLOW_NAME=ACQUIRING_VOID");
        Assertions.assertThat(logs).contains("KAFKA_TOPIC_NAME=TP_S_1212_EC_EVENTLOG_2007");

    }


    @Owner("Akshat")
    @Test(description = "VoidNotify order Success")
    public void Test_002_VoidNotify_orderSuccess() throws Exception {
        String orderID = CommonHelpers.generateOrderId();
        com.paytm.api.notification.VoidNotify voidNotify =new VoidNotify();
        Constants.MerchantType mid = Constants.MerchantType.pushCloseNotify;
        JsonPath response = voidNotify
                .setVoidId("20230607010890000873236257407818167")
                .setMerchantTransID(orderID)
                .setAcquirementID("20230607010890000873236257407818167")
                .setMID(mid.getId())
                .setvoidSource("MERCHANT")
                .setvoidReason("edc-update-rec-void")
                .setResultStatus("S")
                .setResultCode("SUCCESS")
                .setResultCodeID("00000000")
                .setResultMsg("SUCCESS")
                .setOrderStatus("SUCCESS")
                .execute().jsonPath();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification, orderID);
        Assertions.assertThat(logs).contains("\"function\":\"oldpg.acquiring.order.voidNotify\"");
        Assertions.assertThat(logs).contains("\"orderStatus\":\"SUCCESS\"");
        Assertions.assertThat(logs).contains("BUSINESS_FLOW_NAME=ACQUIRING_VOID");
        Assertions.assertThat(logs).contains("KAFKA_TOPIC_NAME=TP_S_1212_EC_EVENTLOG_2007");

    }


}
