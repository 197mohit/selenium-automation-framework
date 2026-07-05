package scripts.Native;

import com.paytm.LocalConfig;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.reporting.Reporter;
import com.paytm.utils.merchant.util.PGPUtil;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static com.paytm.appconstants.Constants.Owner.MONIKA_NAGARIA;

/*
@EditedBy Anju
Class name has been renamed to initiateTransaction from createTransaction
 */

@Owner("Deepak")
public class InitiateTransaction extends PGPBaseTest {

    @Deprecated
    public String getChecksumNative(String mid, String orderId, String amount, User user) throws Exception {
        String test = "\"{\\\"requestType\\\":\\\"Payment\\\",\\\"mid\\\":\\\""+mid+"\\\",\\\"orderId\\\":\\\""+orderId+"\\\",\\\"websiteName\\\":" +
                "\\\"retail\\\",\\\"txnAmount\\\":{\\\"value\\\":\\\""+amount+"\\\",\\\"currency\\\":\\\"INR\\\"},\\\"userInfo\\\":{\\\"custId\\\":\\\""+user.custId()+"\\\",\\\"mobile\\\":\\\""+user.mobNo()+"\\\",\\\"email\\\":\\\"arzoo.batra@paytm.com\\\",\\\"firstName\\\":\\\"arzoo\\\",\\\"lastName\\\":\\\"batra\\\"},\\\"paytmSsoToken\\\":\\\""+user.ssoToken()+"\\\",\\\"disablePaymentMode\\\":[],\\\"enablePaymentMode\\\":[],\\\"promoCode\\\":\\\"\\\",\\\"callbackUrl\\\":\\\"https://pg-staging.paytm.in/MerchantSite/bankResponse\\\",\\\"goods\\\":[{\\\"merchantGoodsId\\\":\\\"0001\\\",\\\"merchantShippingId\\\":\\\"0101\\\",\\\"snapshotUrl\\\":\\\"[http://snap.url.com]\\\",\\\"description\\\":\\\"SummerDress\\\",\\\"category\\\":\\\"travelling\\\",\\\"quantity\\\":\\\"1\\\",\\\"unit\\\":\\\"Kg\\\",\\\"price\\\":{\\\"currency\\\":\\\"INR\\\",\\\"value\\\":\\\"1\\\"},\\\"extendInfo\\\":{\\\"udf1\\\":\\\"U1\\\",\\\"udf2\\\":\\\"U2\\\",\\\"udf3\\\":\\\"U3\\\",\\\"udf4\\\":\\\"U4\\\",\\\"udf5\\\":\\\"U5\\\"}}],\\\"shippingInfo\\\":[{\\\"merchantShippingId\\\":\\\"0001\\\",\\\"trackingNo\\\":\\\"00101\\\",\\\"carrier\\\":\\\"FederalExpress\\\",\\\"chargeAmount\\\":{\\\"currency\\\":\\\"INR\\\",\\\"value\\\":\\\"1\\\"},\\\"countryName\\\":\\\"JP\\\",\\\"stateName\\\":\\\"GA\\\",\\\"cityName\\\":\\\"Atlanta\\\",\\\"address1\\\":\\\"137WSanBernardino\\\",\\\"address2\\\":\\\"4114Sepulveda\\\",\\\"firstName\\\":\\\"Jim\\\",\\\"lastName\\\":\\\"Li\\\",\\\"mobileNo\\\":\\\"8376979170\\\",\\\"zipCode\\\":\\\"310001\\\",\\\"email\\\":\\\"arzoobatra04@gmail.com\\\"}]}\";";
        Reporter.report.info("values used in checksum generation is: "+ test);
        String checkSum = PGPUtil.getChecksum(MerchantType.NATIVE_HYBRID.getKey(), test);
        Reporter.report.info("checksum generated for Native initiate txn is: "+ checkSum);
        return checkSum;
    }

    @BeforeTest
    public void disableScrenShotCapture() {
        DriverManager.setCaptureScreenShot(false);
    }

    @Test(description = "Validate successful Native transaction.", groups = {"smoke", "regression"})
    public void verifySuccessCase() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HYBRID).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).as("TXN Token is null").isNotNull();
    }

    @Test(description = "Validate successful Native transaction for ChannelId WEB.")
    public void verifySuccessCaseWithChannelIdWEB() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HYBRID)
                .setChannelId("WEB")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).as("TXN Token is null").isNotNull();
    }

    @Test(description = "Validate successful Native transaction for ChannelId WAP.")
    public void verifySuccessCaseWithChannelIdWAP() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HYBRID)
                .setChannelId("WAP")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).as("TXN Token is null").isNotNull();
    }

    @Test(description = "Validate unsuccessful Native transaction for blank MID.")
    public void UnsuccessfulTxnWithBlankMID() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HYBRID)
                .setMid(null)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).as("Result Code mismatch").isEqualToIgnoringCase("1007");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).as("Result Msg mismatch").isEqualToIgnoringCase("Mid is mandatory in query parameter");
    }

    @Test(description = "Validate successful Native transaction with duplicate orderID.")
    public void SuccessfulTxnWithDuplicateOrderId() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HYBRID).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).as("TXN Token is null").isNotNull();
        response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).as("TXN Token is null").isNotNull();
    }

    @Test(description = "Validate successful Native transaction with decimal Txn amount.")
    public void SuccessfulTxnWithDecimalTxnAmount() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HYBRID)
                .setTxnValue("1.5")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).as("TXN Token is null").isNotNull();
    }

    @Test(description = "Validate successful Native transaction and authenticated=false when not passing  paytmSsoToken Token.")
    public void TC_CT_047_SuccessfulTxnWhenNotPassingSSOToken() throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.NATIVE_HYBRID).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).as("TXN Token is null").isNotNull();
        Assertions.assertThat(response.jsonPath().getString("body.authenticated")).as("Authenticated is null").isNotNull();
    }

    @Test(description = "Validate unsuccessful Native transaction with invalid SSOToken.")
    public void TC_CT_049_UnsuccessfulTxnWithInvalidSSOToken() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken() + "a", MerchantType.NATIVE_HYBRID)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).as("ResultCode mismatch").isEqualToIgnoringCase("2004");
    }

    @Test(description = "Verify case when not passing txn amt in request")
    public void TC_CT_036() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HYBRID)
                .setTxnValue(null)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath().setRoot("body.resultInfo");
        Assertions.assertThat(jsonPath.getString("resultStatus")).isEqualToIgnoringCase("F");
        Assertions.assertThat(jsonPath.getString("resultCode")).isEqualToIgnoringCase("1007");
        Assertions.assertThat(jsonPath.getString("resultMsg")).isEqualToIgnoringCase("Missing mandatory element");
    }

    @Test(description = "Verify txn token in response when websiteName='retail'")
    public void TC_CT_037() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HYBRID)
                .setWebsiteName("retail")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath().setRoot("body");
        Assertions.assertThat(jsonPath.getString("resultInfo.resultStatus")).isEqualToIgnoringCase("S");
        Assertions.assertThat(jsonPath.getString("resultInfo.resultCode")).isEqualToIgnoringCase("0000");
        Assertions.assertThat(jsonPath.getString("resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
        Assertions.assertThat(jsonPath.getString("txnToken")).isNotNull();
    }
    @Test(description = "Verify txn token in response when emiOption=0CostEMI:8565563_763")
    public void TC_CT_043() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HYBRID)
                .setEmiOption("0CostEMI:8565563_763")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath().setRoot("body");
        Assertions.assertThat(jsonPath.getString("resultInfo.resultStatus")).isEqualToIgnoringCase("S");
        Assertions.assertThat(jsonPath.getString("resultInfo.resultCode")).isEqualToIgnoringCase("0000");
        Assertions.assertThat(jsonPath.getString("resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
        Assertions.assertThat(jsonPath.getString("txnToken")).isNotNull();
    }

    @Test(description = "Verify txn token in response when when callback url is null")
    public void TC_CT_046() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HYBRID)
                .setCallbackUrl(null)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath().setRoot("body");
        Assertions.assertThat(jsonPath.getString("resultInfo.resultStatus")).isEqualToIgnoringCase("S");
        Assertions.assertThat(jsonPath.getString("resultInfo.resultCode")).isEqualToIgnoringCase("0000");
        Assertions.assertThat(jsonPath.getString("resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
        Assertions.assertThat(jsonPath.getString("txnToken")).isNotNull();
    }

    @Test(description = "Validate successful Native transaction and authenticated=true when passing  paytmSsoToken Token.")
    public void TC_CT_048_SuccessfulTxnWhenPassingSSOToken() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.NATIVE_HYBRID)
                .setSsoToken(user.ssoToken()).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.authenticated")).as("Authenticated is null").isNotNull();
    }

    @Test(description = "Validate successful Native transaction when checksum is passed and checksum enabled on merchant")
    public void TC_CT_051_ValidateTxn_ChecksumIsPassedAndEnabledOnMerchant() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.NATIVE_HYBRID)
                .setSsoToken(user.ssoToken()).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("head.signature")).as("signature is null").isNotNull();
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).as("TXN Token is null").isNotNull();
    }

    @Feature("VULN-18527")
    @Owner(MONIKA_NAGARIA)
    @Test(description = "Verify when orderId is <p>This is <b>bold</b> text</p>, it should return 403 if its present in XSS_VALIDATION_URL_REQUEST_BODY_PARAM_MAPPINGS property")
    public void xssVulnerabilityInInitiate() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = MerchantType.EmiInfo_COP;
        String OrderId = "<p>This is <b>bold</b> text</p>";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2")
                .setOrderId(OrderId)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response  = initTxn.execute();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(403);

    }

    @Feature("VULN-18527")
    @Owner(MONIKA_NAGARIA)
    @Test(description = "Verify when orderId is <script>alert('XSS')</script>, it should return 403 if its present in XSS_VALIDATION_URL_REQUEST_BODY_PARAM_MAPPINGS property")
    public void xssVulnerabilityInInitiateOrderId() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = MerchantType.EmiInfo_COP;
        String OrderId = "<script>alert('XSS')</script>";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2")
                .setOrderId(OrderId)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response  = initTxn.execute();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(403);

    }

    @Feature("VULN-18527")
    @Owner(MONIKA_NAGARIA)
    @Test(description = "Verify when mid is alert(document.cookie), it should return 403 if its present in XSS_VALIDATION_URL_REQUEST_BODY_PARAM_MAPPINGS property")
    public void xssVulnerabilityInInitiateMId() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String OrderId =LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = MerchantType.EmiInfo_COP;
//        String merchant = "<script>alert('XSS')</script>";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2")
                .setOrderId(OrderId)
                .setMid("alert(document.cookie)")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response  = initTxn.execute();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(403);

    }

    @Feature("VULN-18527")
    @Owner(MONIKA_NAGARIA)
    @Test(description = "Verify when paymode is alert(document.cookie), it should not return 403 if its not present in XSS_VALIDATION_URL_REQUEST_BODY_PARAM_MAPPINGS property")
    public void xssVulnerabilityInInitiateWebsitename() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String OrderId =LocalConfig.ENV_NAME+"_"+CommonHelpers.generateOrderId();
        Constants.MerchantType merchant = MerchantType.EmiInfo_COP;
//        String merchant = "<script>alert('XSS')</script>";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2")
                .setOrderId(OrderId)
                .setRequestType("alert(document.cookie)>")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response  = initTxn.execute();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);

    }

}
