package scripts.api.theia;

import com.paytm.api.theia.FetchUserPaymentModeStatus;
import com.paytm.api.FetchUserPaymentModeStatus_withtxnAmount;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.fetchPcfDetail.PayMethod;
import com.paytm.framework.reportportal.annotation.Owner;
import io.qameta.allure.Feature;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;


public class FetchUserPayModeStatusTest extends PGPBaseTest {

    @Owner("Anushka")
    @Feature("PGP-39476")
    @Test(description = "Fetch Payment mode Status using TokenType Checksum, Checksum is created without space")
    public void fetchUserPaymentModeStatusChecksum() throws Exception {
        User user;
        user = userManager.getForWrite(Label.USERPAYMODEPOSTPAID);
        String mid = Constants.MerchantType.FETCH_PAYMODES_STATUS.getId();
        String tokenType= "CHECKSUM";
        String token = "MgB/tyQrLtGu3N1PrpHg3DsWsCGPzy4hgxsFdLYBlmFm1ypevnfdnwbIysF+/yn4Hb9mvITGG7Gbex99/P/ZbAoQJxCm4olYFf/qLF/8qaI=";
        String MobileNo= String.valueOf(user);
        ArrayList<String> paymodes= new ArrayList<>();
        paymodes.add("PAYTM_DIGITAL_CREDIT");
        FetchUserPaymentModeStatus fetchUserPaymentModeStatus= new FetchUserPaymentModeStatus().buildRequest(tokenType, token, mid, MobileNo, paymodes);
        JsonPath withDrawJson = fetchUserPaymentModeStatus.execute().jsonPath();
        Assertions.assertThat(withDrawJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(withDrawJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(withDrawJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
    }

    @Owner("Anushka")
    @Feature("PGP-39476")
    @Test(description = "Fetch Payment mode Status using TokenType Checksum, Checksum is created with space")
    public void fetchUserPaymentModeStatusChecksumWithSpace() throws Exception {
        User user;
        user = userManager.getForWrite(Label.USERPAYMODEPOSTPAID);
        String mid = Constants.MerchantType.FETCH_PAYMODES_STATUS.getId();
        String tokenType= "CHECKSUM";
        String token = "bH3O1+p/Ptf3Exa4f6uqGpVqg6P2IE8/0BxYl7vNVgxtifcD5bpNkXVcJAl6U9aKARMrb0AJVMjK30PRB/2T3DIUAFSRcMsHwWKIK3hxPDI=";
        String MobileNo= String.valueOf(user);
        ArrayList<String> paymodes= new ArrayList<>();
        paymodes.add("PAYTM_DIGITAL_CREDIT");
        FetchUserPaymentModeStatus fetchUserPaymentModeStatus= new FetchUserPaymentModeStatus().buildRequest(tokenType, token, mid, MobileNo, paymodes);
        JsonPath withDrawJson = fetchUserPaymentModeStatus.execute().jsonPath();
        Assertions.assertThat(withDrawJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(withDrawJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(withDrawJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
    }
    @Owner("Vikash verma")
    @Feature("PG2-10936")
    @Test(description = "Fetch user payment mode status with respect to txn amount,when txn amount is less than postpaid balance")
    public void FetchUserPaymentModeStatusTxnAmountLessThenPostpaidBalance() throws Exception {
        User user;
        user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType mid = Constants.MerchantType.FETCH_PAYMODES_STATUS_WITHTXNAMOUNT;
        String MobileNo= String.valueOf(user);
        String TxnAmount= "0";
        FetchUserPaymentModeStatus_withtxnAmount fetchUserPaymentModeStatus_withtxnAmount= new FetchUserPaymentModeStatus_withtxnAmount( mid,MobileNo,TxnAmount);
        JsonPath withDrawJson = fetchUserPaymentModeStatus_withtxnAmount.execute().jsonPath();
        Assertions.assertThat(withDrawJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(withDrawJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(withDrawJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(withDrawJson.getString("body.paymentModeStatus["+ 0 +"].status")).isEqualTo("ACTIVE");
    }

    @Owner("Vikash verma")
    @Feature("PG2-10936")
    @Test(description = "Fetch user payment mode status with respect to txn amount,when txn amount is greater than postpaid balance")
    public void FetchUserPaymentModeStatusTxnAmountGreaterThenPostpaidBalance() throws Exception {
        User user;
        user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType mid = Constants.MerchantType.FETCH_PAYMODES_STATUS_WITHTXNAMOUNT;
        String MobileNo= String.valueOf(user);
        String TxnAmount= "10001";
        FetchUserPaymentModeStatus_withtxnAmount fetchUserPaymentModeStatus_withtxnAmount= new FetchUserPaymentModeStatus_withtxnAmount( mid,MobileNo,TxnAmount);
        JsonPath withDrawJson = fetchUserPaymentModeStatus_withtxnAmount.execute().jsonPath();
        Assertions.assertThat(withDrawJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(withDrawJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(withDrawJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(withDrawJson.getString("body.paymentModeStatus["+ 0 +"].status")).isEqualTo("INSUFFICIENT_LIMIT");
    }

}