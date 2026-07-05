package scripts.notification;
import com.paytm.api.notification.VoidNotify;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;


public class VoidNotifyTest extends PGPBaseTest{

    @Owner("Ajeesh")
    @Test(description = "VoidNotify Closed Case")
    public void Test_001_VoidNotify() throws Exception {
        String orderID = CommonHelpers.generateOrderId();
        VoidNotify voidNotify =new VoidNotify();
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

        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");

    }

    @Owner("Akshat")
    @Test(description = "VoidNotify Success Case")
    public void Test_002_VoidNotify() throws Exception {
        String orderID = CommonHelpers.generateOrderId();
        VoidNotify voidNotify =new VoidNotify();
        Constants.MerchantType mid = Constants.MerchantType.pushCloseNotify;
        JsonPath response = voidNotify
                .setVoidId("20230607010890000873236257407818167")
                .setMerchantTransID(orderID)
                .setAcquirementID("20230607010890000873236257407818167")
                .setMID(mid.getId())
                .setvoidSource("LAST_CONFIRM")
                .setvoidReason("void from last confirm")
                .setResultStatus("S")
                .setResultCode("SUCCESS")
                .setResultCodeID("00000000")
                .setResultMsg("SUCCESS")
                .setOrderStatus("SUCCESS")
                .execute().jsonPath();

        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");

    }
}
