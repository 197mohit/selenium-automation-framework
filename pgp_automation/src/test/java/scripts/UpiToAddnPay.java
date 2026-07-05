package scripts;

import com.paytm.ServerConfigProvider;
import com.paytm.api.ProcessTransactionV1;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

@Owner(Constants.Owner.AAYUSH)

public class UpiToAddnPay extends PGPBaseTest {
    @Owner(Constants.Owner.AAYUSH)
    @Test(description = "Validate FPO offer is passed and events.log for flag being passed for UPI to Add&pay txn")
    public void validateupiToAddPay() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType= Constants.MerchantType.AddMoney;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.AddMoney)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionResponse = fetchPaymentOption.execute().jsonPath();
        String upiBlock = fetchPaymentOptionResponse.get("body.convertToAddNPayOfferDetails").toString();
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertNotNull(upiBlock);
        softAssert.assertAll();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setconvertToAddAndPayTxn(true)
                .setMpin("1234")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();

        String grepcmd = "grep \"" + initTxnDTO.getBody().getOrderId() + "\" /paytm/logs/events.log | " +
                "grep \"" + initTxnDTO.getBody().getMid() + "\" | grep \"ONLINE_NATIVE_PAYMENT_REQUEST\" ";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assertions.assertThat(theiaFacadeLogs).contains("Converting UPI transaction to ADDANDPAY for paymentMode");
    }
}