package scripts.AccountBinRange;


import com.paytm.ServerConfigProvider;
import com.paytm.api.ProcessTransactionV1;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.SavedInstruments;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;


public class AccountBinRanges extends PGPBaseTest {

    Constants.MerchantType coftMerchant = Constants.MerchantType.COFT_MERCHANT_3P;

    @Owner(Constants.Owner.PRAGYA_KURELE)
    @Feature("PGP-30257")
    @Test(description = "Verify successful savedcard txn when bin detail for saved asset from platform is BinDetail is received.")

    public void binDetailForSavedAsset() throws Exception{


        User user = userManager.getForRead(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);

        //Card data- CIN
        String cardId = PGPHelpers.saveCardAtAlipayUserBind(user.custId(), PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.ICICI_CC_CARD);

        //Initiate transaction
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, coftMerchant)
                .setTxnValue("2000")
                .setSsoToken(user.ssoToken())
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

        // FPO
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(initTxnResponse.getBody().getTxnToken()).build();
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = PGPHelpers.executeFetchPaymentOpt(coftMerchant.getId(), initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO, false);
        SavedInstruments savedInstruments= fetchPaymentOptResponse.getBody().getMerchantPayOption().getSavedInstruments().get(0);
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(savedInstruments.getCardDetails().getCardId()).isEqualTo(cardId);
        softAssertions.assertAll();

        // v1 PTC

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                coftMerchant.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId())
                .setPaymentMode("CREDIT_CARD")
                .setChannelId("WEB")
                .setAuthMode("otp")
                .setCardInfo(cardId+"||"+paymentDTO.getCvvNumber()+"|")
                .build();


       //JSON POST
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        // THEIA_LOG
        String grepcmd = "grep \"" + initTxnDTO.getBody().getOrderId() + "\" /paytm/logs/theia.log | " +
                "grep \"bin detail for saved asset from platform is BinDetail\"";

        String logs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

       Assertions.assertThat(logs).contains("bin detail for saved asset from platform is BinDetail");

    }

}
