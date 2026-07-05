package scripts.notification;
import com.paytm.api.notification.CaptureNotify;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Test;


public class CaptureNotifyTest extends PGPBaseTest{

    @Test(description = "CaptureNotify Success Case")
    public void Test_001_CaptureNotify() throws Exception {
        String orderID = CommonHelpers.generateOrderId();
        CaptureNotify captureNotify =new CaptureNotify();
        Constants.MerchantType mid = Constants.MerchantType.pushCloseNotify;
        JsonPath response = captureNotify
                .setCaptureID("20230530010870000870292049625381573")
                .setMerchantTransID(orderID)
                .setAcquirementID("20230607010890000873236257407818167")
                .setMID(mid.getId())
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
