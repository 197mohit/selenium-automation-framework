package scripts.api.theia.fetchPcfDetails;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.api.nativeAPI.FetchPcfDetail;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.NativeDTO.fetchPcfDetail.FetchPcfRequest;
import com.paytm.dto.NativeDTO.fetchPcfDetail.FetchPcfRequestWithSSO;
import com.paytm.dto.NativeDTO.fetchPcfDetail.PayMethod;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;
import java.util.ArrayList;
import java.util.List;
import static com.paytm.appconstants.Constants.Owner.VIKASH_VERMA;


public class fetchpcfdetail extends PGPBaseTest {

    public String validatePCFChargeAmount(String txnAmount, Double percentCommsion, Double flatCommission) throws InterruptedException {
        double expectedChargeFeeAmt = convenienceFeeCalculator(Double.parseDouble(txnAmount), percentCommsion, flatCommission, "");
        return String.valueOf(expectedChargeFeeAmt);
    }


    @Owner(VIKASH_VERMA)
    @Feature("PGP-47958")
    @Test(description = "Validate UPI_LITE paymode supported in fetchpcfdetailAPI  with txn token")
    public void validatePCFDetailApiForUPI_litePaymodewithtxntoken() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "100";
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly_Pcf;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String payMethod = "UPI_LITE";
        String expectedChargeAmount = validatePCFChargeAmount(txnamount, 5.00, 0.00);
        String expectedTotalAmount = String.valueOf(Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount));

        //Creating fetchPcf request
        List<PayMethod> payMethods = new ArrayList<>();
        payMethods.add(new PayMethod().setInstId("").setPayMethod(payMethod));
        FetchPcfRequest fetchPcfRequest = new FetchPcfRequest()
                .setHead(new com.paytm.dto.NativeDTO.fetchPcfDetail.Head().setTxnToken(txnToken))
                .setBody(new com.paytm.dto.NativeDTO.fetchPcfDetail.Body().setPayMethods(payMethods));
        FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(fetchPcfRequest, initTxnDTO);
        JsonPath FJsonPath = fetchPcfDetail.execute().jsonPath();
        Assertions.assertThat(FJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(FJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(FJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(FJsonPath.getString("body.consultDetails.UPI_LITE.payMethod")).isEqualTo("UPI_LITE");
        Assertions.assertThat(FJsonPath.getString("body.consultDetails." + payMethod + ".totalTransactionAmount.value")).contains(expectedTotalAmount);

    }

    @Owner(VIKASH_VERMA)
    @Feature("PGP-47958")
    @Test(description = "Validate UPI paymode supported in fetchpcfdetailAPI with txn token")
    public void validatePCFDetailApiForUPIPaymodewithtxntoken() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "100";
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly_Pcf;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String payMethod = "UPI";
        String expectedChargeAmount = validatePCFChargeAmount(txnamount, 5.00, 0.00);
        String expectedTotalAmount = String.valueOf(Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount));

        //Creating fetchPcf request
        List<PayMethod> payMethods = new ArrayList<>();
        payMethods.add(new PayMethod().setInstId("").setPayMethod(payMethod));
        FetchPcfRequest fetchPcfRequest = new FetchPcfRequest()
                .setHead(new com.paytm.dto.NativeDTO.fetchPcfDetail.Head().setTxnToken(txnToken))
                .setBody(new com.paytm.dto.NativeDTO.fetchPcfDetail.Body().setPayMethods(payMethods));
        FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(fetchPcfRequest, initTxnDTO);
        JsonPath FJsonPath = fetchPcfDetail.execute().jsonPath();
        Assertions.assertThat(FJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(FJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(FJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(FJsonPath.getString("body.consultDetails.UPI.payMethod")).isEqualTo("UPI");
        Assertions.assertThat(FJsonPath.getString("body.consultDetails." + payMethod + ".totalTransactionAmount.value")).contains(expectedTotalAmount);

    }

    @Owner(VIKASH_VERMA)
    @Feature("PGP-47958")
    @Test(description = "Validate Balaance paymode supported in fetchpcfdetailAPI with txn token")
    public void validatePCFDetailApiForBalancePaymodewithtxntoken() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "100";
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly_Pcf;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String payMethod = "BALANCE";
        String expectedChargeAmount = validatePCFChargeAmount(txnamount, 6.00, 0.00);
        String expectedTotalAmount = String.valueOf(Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount));

        //Creating fetchPcf request
        List<PayMethod> payMethods = new ArrayList<>();
        payMethods.add(new PayMethod().setInstId("").setPayMethod(payMethod));
        FetchPcfRequest fetchPcfRequest = new FetchPcfRequest()
                .setHead(new com.paytm.dto.NativeDTO.fetchPcfDetail.Head().setTxnToken(txnToken))
                .setBody(new com.paytm.dto.NativeDTO.fetchPcfDetail.Body().setPayMethods(payMethods));
        FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(fetchPcfRequest, initTxnDTO);
        JsonPath FJsonPath = fetchPcfDetail.execute().jsonPath();
        Assertions.assertThat(FJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(FJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(FJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(FJsonPath.getString("body.consultDetails.BALANCE.payMethod")).isEqualTo("BALANCE");
        Assertions.assertThat(FJsonPath.getString("body.consultDetails." + payMethod + ".totalTransactionAmount.value")).contains(expectedTotalAmount);

    }


    @Owner(VIKASH_VERMA)
    @Feature("PGP-47958")
    @Test(description = "Validate UPI and UPI_LITE both paymode supported in fetchpcfdetailAPI with txn token")
    public void validatePCFDetailApiForUPI_LITEANDUPIPaymodewithtxntoken() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "100";
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly_Pcf;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue(txnamount).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String payMethod = "UPI_LITE";
        String payMethod1 = "UPI";
        String expectedChargeAmount = validatePCFChargeAmount(txnamount, 5.00, 0.00);
        String expectedTotalAmount = String.valueOf(Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount));

        //Creating fetchPcf request
        List<PayMethod> payMethods = new ArrayList<>();
        payMethods.add(new PayMethod().setInstId("").setPayMethod(payMethod));
        payMethods.add(new PayMethod().setInstId("").setPayMethod(payMethod1));
        FetchPcfRequest fetchPcfRequest = new FetchPcfRequest()
                .setHead(new com.paytm.dto.NativeDTO.fetchPcfDetail.Head().setTxnToken(txnToken))
                .setBody(new com.paytm.dto.NativeDTO.fetchPcfDetail.Body().setPayMethods(payMethods));
        FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(fetchPcfRequest, initTxnDTO);
        JsonPath FJsonPath = fetchPcfDetail.execute().jsonPath();
        Assertions.assertThat(FJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(FJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(FJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(FJsonPath.getString("body.consultDetails.UPI_LITE.payMethod")).isEqualTo("UPI_LITE");
        Assertions.assertThat(FJsonPath.getString("body.consultDetails.UPI.payMethod")).isEqualTo("UPI");
        Assertions.assertThat(FJsonPath.getString("body.consultDetails." + payMethod + ".totalTransactionAmount.value")).contains(expectedTotalAmount);

    }


    @Owner(VIKASH_VERMA)
    @Feature("PGP-47958")
    @Test(description = "Validate UPI and UPI_LITE both paymode supported in fetchpcfdetailAPI with sso token")
    public void validatePCFDetailApiForUPI_LITEANDUPIPaymodewithssotoken() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "100";
        String payMethod = "UPI_LITE";
        String payMethod1 = "UPI";
        String mid= Constants.MerchantType.PGOnly_Pcf.getId();
        String expectedChargeAmount = validatePCFChargeAmount(txnamount, 5.00, 0.00);
        String expectedTotalAmount = String.valueOf(Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount));

        //Creating fetchPcf request
        List<PayMethod> payMethods = new ArrayList<>();
        payMethods.add(new PayMethod().setInstId("").setPayMethod(payMethod));
        payMethods.add(new PayMethod().setInstId("").setPayMethod(payMethod1));
        FetchPcfRequestWithSSO fetchPcfRequest = new FetchPcfRequestWithSSO()
                .setHead(new com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.Head("WEB").setVersion("v1").setRequestTimestamp("Time").setTokenType("SSO").setToken(user.ssoToken()))
                .setBody(new com.paytm.dto.NativeDTO.fetchPcfDetail.Body().setPayMethods(payMethods).setMid(mid).setTxnAmount(txnamount));
        FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(mid,fetchPcfRequest);
        JsonPath jsonPath = fetchPcfDetail.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.consultDetails.UPI_LITE.payMethod")).isEqualTo("UPI_LITE");
        Assertions.assertThat(jsonPath.getString("body.consultDetails.UPI.payMethod")).isEqualTo("UPI");
        Assertions.assertThat(jsonPath.getString("body.consultDetails." + payMethod + ".totalTransactionAmount.value")).contains(expectedTotalAmount);

    }

    @Owner(VIKASH_VERMA)
    @Feature("PGP-47958")
    @Test(description = "Validate UPI_LITE paymode supported in fetchpcfdetailAPI with sso token  ")
    public void validatePCFDetailApiForUPI_litePaymodewithssotoken() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "100";
        String payMethod = "UPI_LITE";
        String mid= Constants.MerchantType.PGOnly_Pcf.getId();
        String expectedChargeAmount = validatePCFChargeAmount(txnamount, 5.00, 0.00);
        String expectedTotalAmount = String.valueOf(Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount));

        //Creating fetchPcf request
        List<PayMethod> payMethods = new ArrayList<>();
        payMethods.add(new PayMethod().setInstId("").setPayMethod(payMethod));
        FetchPcfRequestWithSSO fetchPcfRequest = new FetchPcfRequestWithSSO()
                .setHead(new com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.Head("WEB").setVersion("v1").setRequestTimestamp("Time").setTokenType("SSO").setToken(user.ssoToken()))
                .setBody(new com.paytm.dto.NativeDTO.fetchPcfDetail.Body().setPayMethods(payMethods).setMid(mid).setTxnAmount(txnamount));
        FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(mid,fetchPcfRequest);
        JsonPath FJsonPath = fetchPcfDetail.execute().jsonPath();
        Assertions.assertThat(FJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(FJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(FJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(FJsonPath.getString("body.consultDetails.UPI_LITE.payMethod")).isEqualTo("UPI_LITE");
        Assertions.assertThat(FJsonPath.getString("body.consultDetails." + payMethod + ".totalTransactionAmount.value")).contains(expectedTotalAmount);

    }

    @Owner(VIKASH_VERMA)
    @Feature("PGP-47958")
    @Test(description = "Validate UPI paymode supported in fetchpcfdetailAPI with sso token")
    public void validatePCFDetailApiForUPIPaymodewithssotoken() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "100";
        String mid= Constants.MerchantType.PGOnly_Pcf.getId();
        String payMethod = "UPI";
        String expectedChargeAmount = validatePCFChargeAmount(txnamount, 5.00, 0.00);
        String expectedTotalAmount = String.valueOf(Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount));

        //Creating fetchPcf request
        List<PayMethod> payMethods = new ArrayList<>();
        payMethods.add(new PayMethod().setInstId("").setPayMethod(payMethod));
        FetchPcfRequestWithSSO fetchPcfRequest = new FetchPcfRequestWithSSO()
                .setHead(new com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.Head("WEB").setVersion("v1").setRequestTimestamp("Time").setTokenType("SSO").setToken(user.ssoToken()))
                .setBody(new com.paytm.dto.NativeDTO.fetchPcfDetail.Body().setPayMethods(payMethods).setMid(mid).setTxnAmount(txnamount));
        FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(mid,fetchPcfRequest);
        JsonPath FJsonPath = fetchPcfDetail.execute().jsonPath();
        Assertions.assertThat(FJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(FJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(FJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(FJsonPath.getString("body.consultDetails.UPI.payMethod")).isEqualTo("UPI");
        Assertions.assertThat(FJsonPath.getString("body.consultDetails." + payMethod + ".totalTransactionAmount.value")).contains(expectedTotalAmount);

    }

    @Owner(VIKASH_VERMA)
    @Feature("PGP-47958")
    @Test(description = "Validate Balaance paymode supported in fetchpcfdetailAPI with sso token")
    public void validatePCFDetailApiForBalancePaymodewithssotoken() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String txnamount = "100";
        String mid= Constants.MerchantType.PGOnly_Pcf.getId();
        String payMethod = "BALANCE";
        String expectedChargeAmount = validatePCFChargeAmount(txnamount, 6.00, 0.00);
        String expectedTotalAmount = String.valueOf(Double.parseDouble(txnamount) + Double.parseDouble(expectedChargeAmount));

        //Creating fetchPcf request
        List<PayMethod> payMethods = new ArrayList<>();
        payMethods.add(new PayMethod().setInstId("").setPayMethod(payMethod));
        FetchPcfRequestWithSSO fetchPcfRequest = new FetchPcfRequestWithSSO()
                .setHead(new com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.Head("WEB").setVersion("v1").setRequestTimestamp("Time").setTokenType("SSO").setToken(user.ssoToken()))
                .setBody(new com.paytm.dto.NativeDTO.fetchPcfDetail.Body().setPayMethods(payMethods).setMid(mid).setTxnAmount(txnamount));
        FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(mid,fetchPcfRequest);
        JsonPath FJsonPath = fetchPcfDetail.execute().jsonPath();
        Assertions.assertThat(FJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(FJsonPath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(FJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(FJsonPath.getString("body.consultDetails.BALANCE.payMethod")).isEqualTo("BALANCE");
        Assertions.assertThat(FJsonPath.getString("body.consultDetails." + payMethod + ".totalTransactionAmount.value")).contains(expectedTotalAmount);

    }

}