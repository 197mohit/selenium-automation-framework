package scripts.api.theia.merchantDetails;

import com.paytm.LocalConfig;
import com.paytm.api.PaymentService;
import com.paytm.api.theia.liteMerchantDetails.V1LiteMerchantDetails;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;
import java.util.HashMap;

public class v1LiteMerchantDetailsTest extends PGPBaseTest {

    String APIBody=V1LiteMerchantDetails.bodyWithAll;
    private SoftAssertions softly = new SoftAssertions();
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-56108")
    @Test(description = "Verify getting success response for MID")
    public void LiteMerchantDetails_for_MID() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.LITE_DETAIL_MID;
        HashMap<String,String> map = new HashMap<>();
        String jwttoken = "";
        map.put("mid", mid.getId());
        jwttoken = PGPHelpers.createTokenForLiteMerchantDetails(map);

        V1LiteMerchantDetails v1LiteMerchantDetails=(V1LiteMerchantDetails) new V1LiteMerchantDetails(APIBody)
                .setContext("body.mid",mid.getId())
                .deleteContext("body.orderId")
                .deleteContext("body.merchantVpa")
                .setContext("head.token",jwttoken);
        Response viLiteMerchantDetailResponse= v1LiteMerchantDetails.execute();
        validateResponse(viLiteMerchantDetailResponse,mid.getId(),false);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-56108")
    @Test(description = "Verify posId is getting in response when merchantVpa is present in request")
    public void checkPosId_for_VPA() throws Exception {
        Constants.MerchantType WalletOfferOnus = Constants.MerchantType.LITE_DETAIL_MID;
        HashMap<String,String> map = new HashMap<>();
        String jwttoken = "";
        String vpa="paytmqr10nsbb@paytm";
        map.put("merchantVpa", vpa);
        jwttoken = PGPHelpers.createTokenForLiteMerchantDetails(map);
        V1LiteMerchantDetails v1LiteMerchantDetails=(V1LiteMerchantDetails) new V1LiteMerchantDetails(APIBody)
                .setContext("body.merchantVpa",vpa)
                .deleteContext("body.orderId")
                .deleteContext("body.mid")
                .setContext("head.token",jwttoken);

        Response viLiteMerchantDetailResponse= v1LiteMerchantDetails.execute();
        JsonPath jsonpath=viLiteMerchantDetailResponse.jsonPath();
        softly.assertThat(jsonpath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(jsonpath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        softly.assertThat(jsonpath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        verifyPosId(vpa,jsonpath.getString("body.posId"));
        softly.assertAll();
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-56108")
    @Test(description = "Verify getting isDqrExpired is true in response when incorrect orderId passed in request.")
    public void check_isDqrExpired_as_true() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.LITE_DETAIL_MID;
        HashMap<String,String> map = new HashMap<>();
        String jwttoken = "";
        String OrderId = CommonHelpers.generateOrderId();
        String vpa="paytmqr10nsbb@paytm";
        map.put("merchantVpa", vpa);
        map.put("orderId",OrderId);
        jwttoken = PGPHelpers.createTokenForLiteMerchantDetails(map);
        V1LiteMerchantDetails v1LiteMerchantDetails=(V1LiteMerchantDetails) new V1LiteMerchantDetails(APIBody)
                .setContext("body.merchantVpa",vpa)
                .setContext("body.orderId",OrderId)
                .deleteContext("body.mid")
                .setContext("head.token",jwttoken);
        Response viLiteMerchantDetailResponse= v1LiteMerchantDetails.execute();
        JsonPath jsonpath=viLiteMerchantDetailResponse.jsonPath();
        softly.assertThat(jsonpath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(jsonpath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        softly.assertThat(jsonpath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        softly.assertThat(jsonpath.getString("body.isDqrExpired")).isEqualTo("true");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, "ACQUIRING_INQUIRE_WITH_MERCHANT_TRANS_ID","dipadd46834559641560");
        softly.assertThat(logs.contains("\"resultCode\":\"TARGET_NOT_FOUND\",\"resultCodeId\":\"00000020\",\"resultStatus\":\"F\",\"resultMsg\":\"order not exist\"")).isEqualTo(true);
        softly.assertAll();
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-56108")
    @Test(description = "Verify mid or merchantvpa is mandatory in request")
    public void LiteMerchantDetails_without_MId_VPA() throws Exception {
        HashMap<String,String> map = new HashMap<>();
        String jwttoken = "";
        String OrderId = CommonHelpers.generateOrderId();
        map.put("orderId",OrderId);
        jwttoken = PGPHelpers.createTokenForLiteMerchantDetails(map);
        V1LiteMerchantDetails v1LiteMerchantDetails=(V1LiteMerchantDetails) new V1LiteMerchantDetails(APIBody)
                .deleteContext("body.merchantVpa")
                .setContext("body.orderId",OrderId)
                .deleteContext("body.mid")
                .setContext("head.token",jwttoken);
        Response viLiteMerchantDetailResponse= v1LiteMerchantDetails.execute();
        JsonPath jsonpath=viLiteMerchantDetailResponse.jsonPath();
        softly.assertThat(jsonpath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertThat(jsonpath.getString("body.resultInfo.resultCode")).isEqualTo("2071");
        softly.assertThat(jsonpath.getString("body.resultInfo.resultMsg")).isEqualTo("Either Merchant ID (MID) or Merchant VPA must be provided in the request. Please ensure that one of these mandatory elements is included.");
        softly.assertAll();
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-56108")
    @Test(description = "Verify getting success response when valid DQR orderID passed in request")
    public void LiteMerchantDetails_For_DQR() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.LITE_DETAIL_MID;
        HashMap<String,String> map = new HashMap<>();
        String jwttoken = "";
        String vpa="paytmocl.d920991505@axis";
        String OrderId = CommonHelpers.generateOrderId();
        PaymentService paymentService = new PaymentService(mid,"12",OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();
        softly.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        map.put("merchantVpa", vpa);
        map.put("orderId",OrderId);
        jwttoken = PGPHelpers.createTokenForLiteMerchantDetails(map);
        V1LiteMerchantDetails v1LiteMerchantDetails=(V1LiteMerchantDetails) new V1LiteMerchantDetails(APIBody)
                .setContext("body.merchantVpa",vpa)
                .setContext("body.orderId",OrderId)
                .deleteContext("body.mid")
                .setContext("head.token",jwttoken);
        Response viLiteMerchantDetailResponse= v1LiteMerchantDetails.execute();
        JsonPath jsonpath=viLiteMerchantDetailResponse.jsonPath();

        validateResponse(viLiteMerchantDetailResponse,mid.getId(),true);
        verifyPosId(vpa,jsonpath.getString("body.posId"));
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-56108")
    @Test(description = "Verify Product code should be same in DQR createOrder and FPO")
    public void validate_ProductCodeFor_DQR() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.LITE_DETAIL_MID;
        HashMap<String,String> map = new HashMap<>();
        String jwttoken = "";
        String vpa="paytmocl.d920991505@axis";
        String OrderId = CommonHelpers.generateOrderId();
        PaymentService paymentService = new PaymentService(mid,"12",OrderId);
        JsonPath jsonPath = paymentService.execute().jsonPath();
        softly.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
        map.put("merchantVpa", vpa);
        map.put("orderId",OrderId);
        jwttoken = PGPHelpers.createTokenForLiteMerchantDetails(map);
        V1LiteMerchantDetails v1LiteMerchantDetails=(V1LiteMerchantDetails) new V1LiteMerchantDetails(APIBody)
                .setContext("body.merchantVpa",vpa)
                .setContext("body.orderId",OrderId)
                .deleteContext("body.mid")
                .setContext("head.token",jwttoken);

        Response viLiteMerchantDetailResponse= v1LiteMerchantDetails.execute();
        JsonPath jsonpath=viLiteMerchantDetailResponse.jsonPath();

        validateResponse(viLiteMerchantDetailResponse,mid.getId(),true);
        verifyPosId(vpa,jsonpath.getString("body.posId"));
        String INQUIRE_WITH_MERCHANT_TRANS_ID_LOGS=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, "ACQUIRING_INQUIRE_WITH_MERCHANT_TRANS_ID",OrderId);
        String merchantTransIDProductCode=INQUIRE_WITH_MERCHANT_TRANS_ID_LOGS.substring(INQUIRE_WITH_MERCHANT_TRANS_ID_LOGS.indexOf("productCode")+14,INQUIRE_WITH_MERCHANT_TRANS_ID_LOGS.indexOf("productCode")+34);
        String FPO_LOGS=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, "CHECKOUT_LITE_PAYVIEW_CONSULT",mid.getId());
        String FPOProductCode=FPO_LOGS.substring(FPO_LOGS.indexOf("productCode")+13,FPO_LOGS.indexOf("productCode")+33);
        softly.assertThat(merchantTransIDProductCode).isEqualTo(FPOProductCode);
        softly.assertAll();
    }

    public void validateResponse(Response viLiteMerchantDetailResponse,String mid,boolean isDQROrder) throws InterruptedException {
        JsonPath jsonpath=viLiteMerchantDetailResponse.jsonPath();
        // verify resultInfo
        softly.assertThat(jsonpath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(jsonpath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        softly.assertThat(jsonpath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

        softly.assertThat(jsonpath.getString("body.mid")).isEqualTo(mid);
        softly.assertThat(jsonpath.getString("body.merchantVpa"));

        getVPAFromLogs(mid,jsonpath.getString("body.merchantVpa"));
        getMerchantDisplayName(mid,jsonpath.getString("body.merchantDisplayName"));
        //getPcfEnabled(mid);
        softly.assertThat(jsonpath.getString("body.pcfEnabled")).isEqualTo(isPcfEnabled(mid));
        softly.assertThat(jsonpath.getInt("body.payMethods.size")).isNotEqualTo(0);
        if(isDQROrder==false)
        {
            softly.assertThat(jsonpath.getString("body.mcc")).isEqualTo(getMCCFromLogs(mid));
            softly.assertThat(jsonpath.getString("body.tipDetails.tipAmounts")).contains("[amount:10.0, mostTipped:false]");
            softly.assertThat(jsonpath.getString("body.tipDetails.tipAmounts")).contains("[amount:30.0, mostTipped:true]");
            softly.assertThat(jsonpath.getString("body.tipDetails.tipAmounts")).contains("[amount:50.0, mostTipped:false]");
            softly.assertThat(jsonpath.getString("body.tipDetails.tipMov")).isEqualTo("500.0");
            softly.assertThat(jsonpath.getString("body.merchantLimitInfo.remainingLimit")).isEqualTo("10000.00");
        }
softly.assertAll();
    }
    public String getMCCFromLogs(String mid) throws InterruptedException {
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, "/pgmc-adapter/api/v1/details/merchant/additional/"+mid,"RESPONSE");
        return logs.substring(logs.indexOf("mccCode")+10,logs.indexOf("mccCode")+14);
    }
    public void getVPAFromLogs(String mid,String vpa) throws InterruptedException {
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, "/pgmc-adapter/api/v2/get/vpa/"+mid,"RESPONSE");
        softly.assertThat(logs.contains(vpa)).isEqualTo(true);
    }

    public void getMerchantDisplayName(String mid,String merchantDisplayName) throws InterruptedException {
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option, "PG2MerchantInfo :: MerchantInfo",mid);
        softly.assertThat(logs.contains(merchantDisplayName)).isEqualTo(true);
        softly.assertAll();
    }

    public String isPcfEnabled(String mid) throws InterruptedException {
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, "PG2Merch\n" +
                "antExtendedInfoV3Response",mid);
        if (logs.contains("51051000100000000002")) return "true";
        else return "false";
    }

    public void verifyPosId(String vpa,String posid) throws InterruptedException {
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, "/pgmc-adapter/api/v2/details/merchant/vpa/"+vpa,"RESPONSE");
        softly.assertThat(logs.contains("\"posId\":\""+posid+"\"")).isEqualTo(true);
        softly.assertAll();
    }
}
