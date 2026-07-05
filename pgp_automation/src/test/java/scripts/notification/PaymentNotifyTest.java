package scripts.notification;

import com.paytm.api.notification.PaymentNotify;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.response.Response;

import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PaymentNotifyTest extends PGPBaseTest {

    @Owner("Vidhi")
    @Feature("PGP-60429")
    @Test(description = "Verify UMP_NOTIFY payload when custom EDC_ID and merchantUniqueReference are passed in paymentNotify payload")
    public void Test_001_PaymentNotify_CustomParameters() throws Exception {
        String orderID = CommonHelpers.generateOrderId();
        String customEdcId = "EDC_TEST_" + System.currentTimeMillis();
        String customMerchantUniqueRef = "UNIQ_REF_" + System.currentTimeMillis();
        
        PaymentNotify paymentNotify = new PaymentNotify(customEdcId, customMerchantUniqueRef)
        .setMerchantTransId(orderID);
    
        Response response = paymentNotify.execute();
        System.out.println("Payment Notify Response: " + response.asString());

        String grepcmd="Data UMP_NOTIFY:";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification, orderID, grepcmd);
        Assertions.assertThat(logs).contains("\"merchantUniqueReference\":\""+customMerchantUniqueRef+"\"");
        Assertions.assertThat(logs).contains("\"edcId\":\""+customEdcId+"\"");
    }

    @Owner("Vidhi")
    @Feature("PGP-60429")
    @Test(description = "Verify UMP_NOTIFY payload when custom EDC_ID and merchantUniqueReference as emptyare passed in paymentNotify payload")
    public void Test_002_PaymentNotify_CustomParameters_EDC_ID_Empty() throws Exception {
        String orderID = CommonHelpers.generateOrderId();
        String customEdcId = "";
        String customMerchantUniqueRef = "UNIQ_REF_" + System.currentTimeMillis();
        
        PaymentNotify paymentNotify = new PaymentNotify(customEdcId, customMerchantUniqueRef)
        .setMerchantTransId(orderID);

        Response response = paymentNotify.execute();
         System.out.println("Payment Notify Response: " + response.asString());


         String grepcmd="Data UMP_NOTIFY:";
         String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification, orderID, grepcmd);
         Assertions.assertThat(logs).contains("\"merchantUniqueReference\":\""+customMerchantUniqueRef+"\"");
         Assertions.assertThat(logs).doesNotContain("\"edcId\"");
           
    }

    @Owner("Vidhi")
    @Feature("PGP-60429")
    @Test(description = "")
    public void Test_003_PaymentNotify_CustomParameters_MerchantUniqueReference_Empty() throws Exception {
        String orderID = CommonHelpers.generateOrderId();
        String customEdcId = "EDC_TEST_" + System.currentTimeMillis();
        String customMerchantUniqueRef = "";
        
        PaymentNotify paymentNotify = new PaymentNotify(customEdcId, customMerchantUniqueRef)
        .setMerchantTransId(orderID);
    
        Response response = paymentNotify.execute();
        System.out.println("Payment Notify Response: " + response.asString());

        String grepcmd="Data UMP_NOTIFY:";
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.pgproxy_notification, orderID, grepcmd);
        Assertions.assertThat(logs).doesNotContain("\"merchantUniqueReference\"");
        Assertions.assertThat(logs).contains("\"edcId\":\""+customEdcId+"\"");
    }

}
