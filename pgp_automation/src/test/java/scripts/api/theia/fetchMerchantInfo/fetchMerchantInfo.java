package scripts.api.theia.fetchMerchantInfo;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.CloseOrderAPI;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.refund.SyncRefund;
import com.paytm.api.theia.FetchMerchantInfo;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.CloseOrder.CloseOrderDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.*;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.paytm.appconstants.Constants.MerchantType.*;
import static com.paytm.appconstants.Constants.Owner.HIMANSHU;
import static com.paytm.apphelpers.PGPHelpers.assertRefundSuccessNotifyPresence;
import static com.paytm.dto.PaymentDTO.CORPORATE_INDIAN_DC;
import static com.paytm.dto.PaymentDTO.PREPAID_CARD;
import static io.restassured.RestAssured.given;

public class fetchMerchantInfo extends PGPBaseTest {

    @Owner(HIMANSHU)
    @Feature("PGP-58139")
    @Test(description = "Validate MCC and subs identifier are coming in resp of fetchMerchantInfo api for one time txn")
    public void validateFetchMerchantInfoResp_oneTimeTxn() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CC_DC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        String mid = merchant.getId();
        FetchMerchantInfo fetchInfo=new FetchMerchantInfo(mid,orderID,txnToken,user.ssoToken());
        Response fetchMerchantInfoResponse = fetchInfo.execute();
        Assertions.assertThat(fetchMerchantInfoResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchMerchantInfoResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchMerchantInfoResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchMerchantInfoResponse.jsonPath().getString("body.mccCode")).isNotNull();
        Assertions.assertThat(fetchMerchantInfoResponse.jsonPath().getString("body.isSubscription")).isEqualTo("false");
    }

    @Owner(HIMANSHU)
    @Feature("PGP-58139")
    @Test(description = "Validate MCC and subs identifier are coming in resp of fetchMerchantInfo api for subs txn")
    public void validateFetchMerchantInfoResp_subsTxn() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = SUBSCRIPTION_UPI;
        String subscriptionStartDate = CommonHelpers.getDate().toString();
        String orderId = CommonHelpers.generateOrderId();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchant)
                .setTxnValue("399")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("500")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("0")
                .setSubscriptionStartDate(subscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setOrderId(orderId)
                .build();
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken= responseDTO.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        String mid = merchant.getId();
        FetchMerchantInfo fetchInfo=new FetchMerchantInfo(mid,orderID,txnToken,user.ssoToken());
        Response fetchMerchantInfoResponse = fetchInfo.execute();
        Assertions.assertThat(fetchMerchantInfoResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchMerchantInfoResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchMerchantInfoResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchMerchantInfoResponse.jsonPath().getString("body.mccCode")).isNotNull();
        Assertions.assertThat(fetchMerchantInfoResponse.jsonPath().getString("body.isSubscription")).isEqualTo("true");
    }
}