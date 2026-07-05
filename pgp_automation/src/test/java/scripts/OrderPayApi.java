package scripts;

import com.paytm.api.theia.OrderPay;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

@Feature("PGP-49850")
public class OrderPayApi extends PGPBaseTest
{
    @Test(description = "Verify 2xx response code in payorder api response for Limit failure when payment option is NB")
    public void validatePayOrderApiFailureWithNB()
    {
        Constants.MerchantType mid = Constants.MerchantType.MERCHANT_WITH_LIMIS;
        String orderId="payorder" + RandomStringUtils.randomNumeric(6);
        String txnAmount="200000";
        String payMode="NB";
        String bankCode="ICICI";
        OrderPay payOrder=new OrderPay(mid,orderId,txnAmount,payMode,bankCode);
        Response response= payOrder.execute();
        JsonPath jsonpath=response.jsonPath();
        Assertions.assertThat(response.statusCode()).isEqualTo(200);
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultCodeId")).isEqualTo("RC-00018");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultCode")).isEqualTo("MERCHANT_VELOCITY_LIMIT_BREACH");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultMsg")).isEqualTo("Merchant velocity limit breached");
    }


    @Test(description = "Verify 2xx response code in payorder api response for Limit failure when payment option is UPI Intent")
    public void validatePayOrderApiFailureWithUpiIntent()
    {
        Constants.MerchantType mid = Constants.MerchantType.MERCHANT_WITH_LIMIS;
        String orderId="payorder" + RandomStringUtils.randomNumeric(6);
        String txnAmount="200000";
        String payMode="UPI_INTENT";
        String bankCode="";
        OrderPay payOrder=new OrderPay(mid,orderId,txnAmount,payMode,bankCode);
        Response response= payOrder.execute();
        JsonPath jsonpath=response.jsonPath();
        Assertions.assertThat(response.statusCode()).isEqualTo(200);
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultCodeId")).isEqualTo("RC-00018");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultCode")).isEqualTo("MERCHANT_VELOCITY_LIMIT_BREACH");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultMsg")).isEqualTo("Merchant velocity limit breached");
    }

    @Test(description = "Verify 2xx response code in payorder api response for Success when payment option is NB")
    public void validatePayOrderApiSuccessWithNB()
    {
        Constants.MerchantType mid = Constants.MerchantType.MERCHANT_WITH_LIMIS;
        String orderId="payorder" + RandomStringUtils.randomNumeric(6);
        String txnAmount="1";
        String payMode="NB";
        String bankCode="ICICI";
        OrderPay payOrder=new OrderPay(mid,orderId,txnAmount,payMode,bankCode);
        Response response= payOrder.execute();
        JsonPath jsonpath=response.jsonPath();
        Assertions.assertThat(response.statusCode()).isEqualTo(200);
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultCodeId")).isEqualTo("RC-00001");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonpath.getString("body.orderId")).isEqualTo(orderId);
        Assertions.assertThat(jsonpath.getString("body.paymentMode")).isEqualTo(payMode);
    }


    @Test(description = "Verify 2xx response code in payorder api response for Success when payment option is UPI Intent")
    public void validatePayOrderApiSuccessWithUpiIntent()
    {
        Constants.MerchantType mid = Constants.MerchantType.MERCHANT_WITH_LIMIS;
        String orderId="payorder" + RandomStringUtils.randomNumeric(6);
        String txnAmount="1";
        String payMode="UPI_INTENT";
        String bankCode="";
        OrderPay payOrder=new OrderPay(mid,orderId,txnAmount,payMode,bankCode);
        Response response= payOrder.execute();
        JsonPath jsonpath=response.jsonPath();
        Assertions.assertThat(response.statusCode()).isEqualTo(200);
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultCodeId")).isEqualTo("RC-00001");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonpath.getString("body.orderId")).isEqualTo(orderId);
        Assertions.assertThat(jsonpath.getString("body.paymentMode")).isEqualTo("UPI");}

}
