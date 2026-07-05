package scripts;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.Peon;
import com.paytm.api.PreAuthAPI;
import com.paytm.api.PreAuthCaptureAPI;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.AuthHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PeonResponse;
import com.paytm.dto.PreAuth.PreAuthDTO;
import com.paytm.dto.PreAuthCapture.PreAuthCaptureDTO;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

public class PreAuth extends PGPBaseTest {

    @Owner(Constants.Owner.JAI)
    @Test(description = "Verify OnPaytm=true in promo payload in queue handler service after Successful PreAuth Txn with ONUS merchant")
    public void PGP_28980_verifyNotificationQueueHandleraferSuccesfulPreAuthTxnONUS() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        User user = userManager.getForWrite(Label.FOODWALLET,Label.MGV,Label.EMIDC);
        String SSotoken = user.ssoToken();
        Response res = AuthHelpers.getUserTokens(LocalConfig.AUTH_HOST, SSotoken);
        String token = res.jsonPath().getString("tokens.access_token[0]");
        OrderDTO orderDTO = new OrderFactory.WalletOnly(merchantType, "",user)
                .setTXN_AMOUNT("2")
                .build();
        WalletHelpers.modifyBalance(user, 20.00);
        PreAuthDTO preAuthDTO = new PreAuthDTO(orderDTO.getORDER_ID(),merchantType,"2",token);
        PreAuthAPI preAuthAPI = new PreAuthAPI(preAuthDTO);
        Response response = preAuthAPI.execute();
        String pREAUTH_ID = response.jsonPath().getString("PREAUTH_ID");
        PreAuthCaptureDTO preAuthCaptureDTO = new PreAuthCaptureDTO(orderDTO,pREAUTH_ID,merchantType, token);
        PreAuthCaptureAPI preAuthCaptureAPI = new PreAuthCaptureAPI(preAuthCaptureDTO);
        preAuthCaptureAPI.execute();
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/notificationQueueHandler.log | " +
                "grep \"CapturePromoServiceImpl.pushPayloadInKafka()\" | grep \"CAPTURE\"";
        String merchantstatusfacadelogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER,grepcmd);
        Assertions.assertThat(merchantstatusfacadelogs).contains("\"onPaytm\":true");
        SoftAssert softAssert = new SoftAssert();
        com.paytm.api.Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());
        softAssert.assertEquals(peonResponse.getSTATUS(), "TXN_SUCCESS");
        softAssert.assertAll();
    }

    @Owner(Constants.Owner.JAI)
    @Test(description = "Verify OnPaytm=false in promo payload in queue handler service after Successful PreAuth Txn with OFFUS merchant")
    public void PGP_28980_verifyNotificationQueueHandleraferSuccesfulPreAuthTxnOFFUS() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.WalletOnly;
        //   User user = userManager.getForWrite(Label.FOODWALLET,Label.MGV,Label.EMIDC);
        User user = userManager.getForWrite(Label.BASIC);
        String SSotoken = user.ssoToken();
        Response res = AuthHelpers.getUserTokens(LocalConfig.AUTH_HOST, SSotoken);
        String token = res.jsonPath().getString("tokens.access_token[0]");
        OrderDTO orderDTO = new OrderFactory.WalletOnly(merchantType, "",user)
                .setTXN_AMOUNT("2")
                .build();
        WalletHelpers.modifyBalance(user, 20.00);
        PreAuthDTO preAuthDTO = new PreAuthDTO(orderDTO.getORDER_ID(),merchantType,"2",token);
        PreAuthAPI preAuthAPI = new PreAuthAPI(preAuthDTO);
        Response response = preAuthAPI.execute();
        String pREAUTH_ID = response.jsonPath().getString("PREAUTH_ID");
        PreAuthCaptureDTO preAuthCaptureDTO = new PreAuthCaptureDTO(orderDTO,pREAUTH_ID,merchantType, token);
        PreAuthCaptureAPI preAuthCaptureAPI = new PreAuthCaptureAPI(preAuthCaptureDTO);
        preAuthCaptureAPI.execute();
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/notificationQueueHandler.log | " +
                "grep \"CapturePromoServiceImpl.pushPayloadInKafka()\" | grep \"CAPTURE\"";
        System.out.println(grepcmd);
        String merchantstatusfacadelogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER,grepcmd);
        Assertions.assertThat(merchantstatusfacadelogs).contains("\"onPaytm\":false");
        SoftAssert softAssert = new SoftAssert();
        com.paytm.api.Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());
        softAssert.assertEquals(peonResponse.getSTATUS(), "TXN_SUCCESS");
        softAssert.assertAll();
    }
}
