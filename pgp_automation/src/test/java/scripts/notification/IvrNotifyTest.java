package scripts.notification;

import com.jayway.jsonpath.JsonPathException;
import com.paytm.api.SMSPrimary;
import com.paytm.api.notification.IvrNotify;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.Amount;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.fest.assertions.api.Assertions;
import org.testng.ITestResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.NoSuchElementException;

@Owner(Constants.Owner.PRIYANSHI)
@Feature("PGP-26899")

public class IvrNotifyTest  extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();
    @BeforeMethod
    public void merchantTxn(@Optional("enhancedweb_revamp") String theme){
            Constants.MerchantType ivr = Constants.MerchantType.IVR;
            OrderDTO orderDTO = new OrderFactory.PGOnly(ivr, theme)
                    .build();
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CREDIT_CARD_NUMBER);
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateRespMsg("Txn Success")
                    .validateStatus("TXN_SUCCESS")
                    .assertAll();
        }
    @Test(description = "Verify that in api response we are getting error if phone number is not sent in API request header")
    public void mobileNumberIsNotSendTC_01(){

        IvrNotify ivrnotify = new IvrNotify("");
        JsonPath withDrawJson = ivrnotify.execute().jsonPath();
        Assertions.assertThat(withDrawJson.getString("status")).isEqualTo("ERROR");
        Assertions.assertThat(withDrawJson.getString("statusCode")).isEqualTo("PGP_02");
        Assertions.assertThat(withDrawJson.getString("statusMessage")).isEqualTo("Request failed");
        Assertions.assertThat(withDrawJson.getString("response.status")).isEqualTo("ERROR");
        Assertions.assertThat(withDrawJson.getString("response.statusCode")).isEqualTo(null);

    }

    @Test(description = "Verify that in api response we are getting error if clientid is not sent in API request header")
    public void clientIdIsNotSendTC_02() throws Exception {
        User user = userManager.getForRead(Label.IVR);
        IvrNotify ivrnotify = new IvrNotify(user.mobNo());
        ivrnotify.getRequestSpecBuilder().removeParam("CLIENTID");
        JsonPath withDrawJson = ivrnotify.execute().jsonPath();
        Assertions.assertThat(withDrawJson.getString("status")).isEqualTo(null);
        Assertions.assertThat(withDrawJson.getString("statusMessage")).isEqualTo("Request Successfully fullfilled.");
        Assertions.assertThat(withDrawJson.getString("response.status")).isEqualTo("SUCCESS");
        Assertions.assertThat(withDrawJson.getString("response.statusCode")).isEqualTo(null);
    }



    @Test(description = "Verify that in api response we are getting error if hash is not sent in API request header")
    public void hashIsNotSend_TC03(){
        IvrNotify ivrnotify = new IvrNotify("");
        ivrnotify.getRequestSpecBuilder().removeParam("HASH");
        JsonPath withDrawJson = ivrnotify.execute().jsonPath();
        Assertions.assertThat(withDrawJson.getString("status")).isEqualTo("ERROR");
        Assertions.assertThat(withDrawJson.getString("statusCode")).isEqualTo("PGP_02");
        Assertions.assertThat(withDrawJson.getString("statusMessage")).isEqualTo("Request failed");
        Assertions.assertThat(withDrawJson.getString("response.status")).isEqualTo("ERROR");
        Assertions.assertThat(withDrawJson.getString("response.statusCode")).isEqualTo(null);

    }


    @Test(description = "Verify that no txn related data is returned in API if incorrect Phone number is sent in api request header")
    public void phoneNumberIncorrect_TC04(){
        IvrNotify ivrnotify = new IvrNotify("8787878787");
        JsonPath withDrawJson = ivrnotify.execute().jsonPath();
        Assertions.assertThat(withDrawJson.getString("response.txnHistorySmsResp")).isEqualTo(null);
        Assertions.assertThat(withDrawJson.getString("response.balanceSmsResp")).isEqualTo(null);

    }




    @Test(description = "Verify that in api response we are getting error if incorrect clientid  is sent in API request header")
    public void clientIdIsIncorrect_TC06(){
        IvrNotify ivrnotify = new IvrNotify("");
        ivrnotify.getRequestSpecBuilder().removeParam("CLIENTID");
        ivrnotify.getRequestSpecBuilder().addHeader("CLENTID","xyz");
        JsonPath withDrawJson = ivrnotify.execute().jsonPath();
        Assertions.assertThat(withDrawJson.getString("status")).isEqualTo("ERROR");
        Assertions.assertThat(withDrawJson.getString("statusCode")).isEqualTo("PGP_02");
        Assertions.assertThat(withDrawJson.getString("statusMessage")).isEqualTo("Request failed");
        Assertions.assertThat(withDrawJson.getString("response.status")).isEqualTo("ERROR");


    }

    @Test(description = "Verify that in api response we are getting error if incorrect HASH  is sent in API request header")
    public void hashIsIncorrect_TC07(){
        IvrNotify ivrnotify = new IvrNotify("");
        ivrnotify.getRequestSpecBuilder().removeParam("HASH");
        ivrnotify.getRequestSpecBuilder().addHeader("HASH","acde");
        JsonPath withDrawJson = ivrnotify.execute().jsonPath();
        Assertions.assertThat(withDrawJson.getString("status")).isEqualTo("ERROR");
        Assertions.assertThat(withDrawJson.getString("statusCode")).isEqualTo("PGP_02");
        Assertions.assertThat(withDrawJson.getString("statusMessage")).isEqualTo("Request failed");
        Assertions.assertThat(withDrawJson.getString("response.status")).isEqualTo("ERROR");

    }



    @Test(description = "Verify that we are not getting any txnrelated data if we sent any other action type except for 'TXN_HISTORY_SMS','BALANCE_SMS' and 'SETTLEMENT_SMS'")
    public void TC_09() throws Exception {
        User user = userManager.getForRead(Label.IVR);
        IvrNotify ivrnotify = new IvrNotify(user.mobNo());
        JsonPath withDrawJson = ivrnotify.execute().jsonPath();
        Assertions.assertThat(withDrawJson.getString("status")).isEqualTo(null);
        Assertions.assertThat(withDrawJson.getString("statusMessage")).isEqualTo("Request Successfully fullfilled.");
        Assertions.assertThat(withDrawJson.getString("response.status")).isEqualTo("SUCCESS");
        Assertions.assertThat(withDrawJson.getString("response.statusCode")).isEqualTo(null);
    }

    @Test(description = "Verify that we are getting following parameters in API response:Status,statusCode,statusMessage,response,posSummaryResponse")
    public void TC_16() throws Exception {
        User user = userManager.getForRead(Label.IVR);
        IvrNotify ivrnotify = new IvrNotify(user.mobNo());

        JsonPath withDrawJson = ivrnotify.execute().jsonPath();

        Assertions.assertThat(withDrawJson.getString("status")).isEqualTo(null);
        Assertions.assertThat(withDrawJson.getString("statusCode")).isNotNull();
        Assertions.assertThat(withDrawJson.getString("statusMessage")).isEqualTo("Request Successfully fullfilled.");
        Assertions.assertThat(withDrawJson.getString("response.posSummaryResponse")).isEqualTo(null);


    }

    @Test(description = "VVerify that merchantid returned is alipay merchant id of merchant for which txn data is returned")
    public void TC_20() throws Exception {
        User user = userManager.getForRead(Label.IVR);
        IvrNotify ivrnotify = new IvrNotify(user.mobNo());

        JsonPath withDrawJson = ivrnotify.execute().jsonPath();
        Assertions.assertThat(withDrawJson.getString("status")).isEqualTo(null);
        Assertions.assertThat(withDrawJson.getString("statusMessage")).isEqualTo("Request Successfully fullfilled.");
        Assertions.assertThat(withDrawJson.getString("response.status")).isEqualTo("SUCCESS");
        Assertions.assertThat(withDrawJson.getString("response.statusCode")).isEqualTo(null);
        Assertions.assertThat(withDrawJson.getString("response.merchantId")).isEqualTo(withDrawJson.getString("response.balanceSmsResp.merchantId"));

    }


    @Test(description = "Verify that follwoing paramters are returned in 'summarySmsResp' body: settledSummary,unSettledSummary")
    public void TC_35() throws Exception {
        User user = userManager.getForRead(Label.IVR);
        IvrNotify ivrnotify = new IvrNotify(user.mobNo());
        JsonPath withDrawJson = ivrnotify.execute().jsonPath();

        Assertions.assertThat(withDrawJson.getString("status")).isEqualTo(null);
        Assertions.assertThat(withDrawJson.getString("statusMessage")).isEqualTo("Request Successfully fullfilled.");
        Assertions.assertThat(withDrawJson.getString("response.status")).isEqualTo("SUCCESS");
        Assertions.assertThat(withDrawJson.getString("response.statusCode")).isEqualTo(null);
        Assertions.assertThat(withDrawJson.getString("response")).isNotEmpty();
        Assertions.assertThat(withDrawJson.getString("response.summarySmsResp.settledSummary.status")).isEqualTo("SUCCESS");
        Assertions.assertThat(withDrawJson.getString("response.summarySmsResp.unSettledSummary.status")).isEqualTo("SUCCESS");

    }

}
