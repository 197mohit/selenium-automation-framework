package scripts.UPISubsMandate;

import com.paytm.ServerConfigProvider;
import com.paytm.api.ProcessTransactionV1;
import com.paytm.appconstants.Constants;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.utils.merchant.merchant.util.QRCode;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;
import io.qameta.allure.Owner;

import static com.paytm.ServerConfigProvider.SERVICE.INSTAPROXY;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@Feature("PGP-21646")
@Owner(Constants.Owner.PRAGYA_KURELE)
public class UPICheckDeviceDetail extends PGPBaseTest {

    @Owner(Constants.Owner.PRAGYA_KURELE)
    @Test(description = "For UPI transaction device detail is passed in validate address and merchant status")

    public void validatedevicedetail() throws Exception{
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType= Constants.MerchantType.UPI_INTENT;

        //Initiate Transaction
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPI_INTENT)
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

        //V1 PTC
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPaymentFlow("NONE")
                .setPayerAccount("arsh.test2@paytm")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request,"samsung","SM-A750F","12");
        Response response = processTransactionV1.execute();

        // Validate-Address Instaproxy Logs

        String grepcmd = "grep \"" + initTxnDTO.getBody().getOrderId() + "\" /paytm/logs/instaproxy.log | " +
                "grep \"Auth Request\"";
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcmd);
        Assertions.assertThat(logs).contains("URL:https://pgp-automation.paytm.in/mockbank/PPBL_UPI/upi/validate-address?mid="+merchantType.getId()+"&orderId="+initTxnDTO.getBody().getOrderId());

        // Merchant-Request Instaproxy Logs
        String grepcmd1 = "grep \"" + initTxnDTO.getBody().getOrderId() + "\" /paytm/logs/instaproxy.log | " +
                "grep \"Payment Request\"";
        String logs1 = getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcmd1);
        Assertions.assertThat(logs1).contains("URL:https://pgp-automation.paytm.in/mockbank/PPBL_UPI/upi/collect-merchant-request?mid="+merchantType.getId()+"&orderId="+initTxnDTO.getBody().getOrderId());
    }

}
