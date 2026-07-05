package scripts.notification;
import com.paytm.api.notification.DeductionNotify;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;


public class DeductionNotifyTest extends PGPBaseTest {

    @Owner("Ajeesh")
    @Test(description = "DeductionNotify for INIT status")
    public void Test_001_DeductionNotify() throws Exception {
        String orderID = CommonHelpers.generateOrderId();
        DeductionNotify deductionNotify =new DeductionNotify();
        Constants.MerchantType mid = Constants.MerchantType.SUBS_UI_TEXT;
        JsonPath response = deductionNotify
                .setPaymentId("20230530010860000870342334708692442")
                .setDeductiondetailID("20230530010368703423344731832327265")
                .setMID(mid.getId())
                .setOrderID(orderID)
                .setDeductionStatus("INIT")
                .execute().jsonPath();
        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");

    }

    @Owner("Akshat")
    @Test(description = "DeductionNotify for Terminated status")
    public void Test_002_DeductionNotify() throws Exception {
        String orderID = CommonHelpers.generateOrderId();
        DeductionNotify deductionNotify =new DeductionNotify();
        Constants.MerchantType mid = Constants.MerchantType.SUBS_UI_TEXT;
        JsonPath response = deductionNotify
                .setPaymentId("20230530010860000870342334708692441")
                .setDeductiondetailID("20230530010368703423344731832327264")
                .setMID(mid.getId())
                .setOrderID(orderID)
                .setDeductionStatus("TERMINATED")
                .execute().jsonPath();
        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");

    }

    @Owner("Akshat")
    @Test(description = "DeductionNotify for Terminated status")
    public void Test_003_DeductionNotify() throws Exception {
        String orderID = CommonHelpers.generateOrderId();
        DeductionNotify deductionNotify =new DeductionNotify();
        Constants.MerchantType mid = Constants.MerchantType.SUBS_UI_TEXT;
        JsonPath response = deductionNotify
                .setPaymentId("20230530010860000870342334708692441")
                .setDeductiondetailID("20230530010368703423344731832327264")
                .setMID(mid.getId())
                .setOrderID(orderID)
                .setDeductionStatus("DEBITED")
                .execute().jsonPath();
        Assert.assertEquals(response.getString("response.body.resultInfo.resultMsg"),"Success");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCode"),"SUCCESS");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultCodeId"),"00000000");
        Assert.assertEquals(response.getString("response.body.resultInfo.resultStatus"),"S");

    }

}
