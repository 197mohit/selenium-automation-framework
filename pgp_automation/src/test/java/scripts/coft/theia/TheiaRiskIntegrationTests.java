package scripts.coft.theia;

import com.paytm.ServerConfigProvider;
import com.paytm.api.coft.saveCard.HandlerInternalDeleteBinUser;
import com.paytm.api.coft.saveCard.HanlerInternalBinInfo;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.CardTokenInfo;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
import static com.paytm.apphelpers.SavedCardHelpersNew.tokenizeCard;


public class TheiaRiskIntegrationTests extends PGPBaseTest {

    Constants.MerchantType coftMerchantPay = Constants.MerchantType.COFT_MERCHANT;
    Constants.MerchantType coftMerchantCop = Constants.MerchantType.COFT_ONUS_MERCHANT;

    @Owner(Constants.Owner.MAYANK_BHARSHIV)
    @Feature("PGP-40009")
    @Test(description = "Verify CIN, PAR, Last4 digits and cardBin is set in Pay request for Token and tavv transaction")
    public void verifyTheiaRiskIntegrationForTokenTavvPay() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();

        //Initiate Transaction
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), coftMerchantPay)
                .setTxnValue("2000")
                .setSsoToken(user.ssoToken())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setCardToken(PaymentDTO.CARDTOKEN);
        cardTokenInfo.setTokenExpiry(PaymentDTO.TOKENEXPIRY);
        cardTokenInfo.setTavv(PaymentDTO.Tavv);
        cardTokenInfo.setCardSuffix(PaymentDTO.CARDSUFFIX);
        cardTokenInfo.setPanUniqueReference(PaymentDTO.PAR);

        //V1 PTC
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                coftMerchantPay.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId())
                .setPaymentMode("CREDIT_CARD")
                .setAuthMode("otp")
                .setCardInfo("||" + paymentDTO.getCvvNumber() + "|")
                .setcardTokenInfo(cardTokenInfo)
                .build();

        NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        String grepcmd = "grep \"" + initTxnDTO.getBody().getOrderId() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + coftMerchantPay.getId() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        String verifyLogs = theiaFacadeLogs.substring(theiaFacadeLogs.indexOf("riskExtendInfo"), theiaFacadeLogs.indexOf("isContact"));
        Assertions.assertThat(verifyLogs).contains("\\\"par\\\":\\\"" + PaymentDTO.PAR + "\\\"").contains("\\\"lastFourDigits\\\":\\\"" + PaymentDTO.CARDSUFFIX + "\\\"").contains("\\\"cardBin\\\":\\\"" + PaymentDTO.COFTTHEIARISKINTBIN + "\\\"").contains("\\\"cin\\\":\\\"" + PaymentDTO.COFTTHEIARISKINTCIN + "\\\"");
    }

    @Owner(Constants.Owner.MAYANK_BHARSHIV)
    @Feature("PGP-40009")
    @Test(description = "Verify PAR is sent in Pay request for Fresh Card")
    public void verifyTheiaRiskIntegrationForFreshCardPay() throws Exception {
        User user = userManager.getForRead(Label.BASIC);

        //Initiate Transaction
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), coftMerchantPay)
                .setTxnValue("2000")
                .setSsoToken(user.ssoToken())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

        //V1 PTC
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                coftMerchantPay.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId())
                .setPaymentMode("CREDIT_CARD")
                .setAuthMode("otp")
                .setCardInfo("|5506900480000008|111|082022")
                .build();

        NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        String grepcmd = "grep \"" + initTxnDTO.getBody().getOrderId() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + coftMerchantPay.getId() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        String verifyLogs = theiaFacadeLogs.substring(theiaFacadeLogs.indexOf("riskExtendInfo"), theiaFacadeLogs.indexOf("isContact"));
        Assertions.assertThat(verifyLogs).contains("\\\"par\\\":\\\"" + PaymentDTO.PAR + "\\\"");
    }

    @Owner(Constants.Owner.MAYANK_BHARSHIV)
    @Feature("PGP-40009")
    @Test(description = "Verify CIN, PAR, Last4 digits and cardBin is set in COP request for Token and tavv transaction")
    public void verifyTheiaRiskIntegrationForTokenTavvCOP() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();

        //Initiate Transaction
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), coftMerchantCop)
                .setTxnValue("2000")
                .setSsoToken(user.ssoToken())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setCardToken(PaymentDTO.CARDTOKEN);
        cardTokenInfo.setTokenExpiry(PaymentDTO.TOKENEXPIRY);
        cardTokenInfo.setTavv(PaymentDTO.Tavv);
        cardTokenInfo.setCardSuffix(PaymentDTO.CARDSUFFIX);
        cardTokenInfo.setPanUniqueReference(PaymentDTO.PAR);

        //V1 PTC
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                coftMerchantCop.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId())
                .setPaymentMode("CREDIT_CARD")
                .setAuthMode("otp")
                .setCardInfo("||" + paymentDTO.getCvvNumber() + "|")
                .setcardTokenInfo(cardTokenInfo)
                .build();

        NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        String grepcmd = "grep \"" + initTxnDTO.getBody().getOrderId() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + coftMerchantCop.getId() + "\" | grep \"ACQUIRING_CREATE_ORDER_AND_PAY\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        String verifyLogs = theiaFacadeLogs.substring(theiaFacadeLogs.indexOf("riskExtendInfo"), theiaFacadeLogs.indexOf("isContact"));
        Assertions.assertThat(verifyLogs).contains("\\\"par\\\":\\\"" + PaymentDTO.PAR + "\\\"").contains("\\\"lastFourDigits\\\":\\\"" + PaymentDTO.CARDSUFFIX + "\\\"").contains("\\\"cardBin\\\":\\\"" + PaymentDTO.COFTTHEIARISKINTBIN + "\\\"").contains("\\\"cin\\\":\\\"" + PaymentDTO.COFTTHEIARISKINTCIN + "\\\"");
    }

    @Owner(Constants.Owner.MAYANK_BHARSHIV)
    @Feature("PGP-40009")
    @Test(description = "Verify PAR is sent in COP request for Fresh Card")
    public void verifyTheiaRiskIntegrationForFreshCardCOP() throws Exception {
        User user = userManager.getForRead(Label.BASIC);

        //Initiate Transaction
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), coftMerchantCop)
                .setTxnValue("2000")
                .setSsoToken(user.ssoToken())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

        //V1 PTC
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                coftMerchantCop.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId())
                .setPaymentMode("CREDIT_CARD")
                .setAuthMode("otp")
                .setCardInfo("|5506900480000008|111|082022")
                .build();

        NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        String grepcmd = "grep \"" + initTxnDTO.getBody().getOrderId() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + coftMerchantCop.getId() + "\" | grep \"ACQUIRING_CREATE_ORDER_AND_PAY\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        String verifyLogs = theiaFacadeLogs.substring(theiaFacadeLogs.indexOf("riskExtendInfo"), theiaFacadeLogs.indexOf("isContact"));
        Assertions.assertThat(verifyLogs).contains("\\\"par\\\":\\\"" + PaymentDTO.PAR + "\\\"");
    }

    @Owner(Constants.Owner.MAYANK_BHARSHIV)
    @Feature("PGP-40009")
    @Test(description = "Verify PAR is sent in Pay request for saved token")
    public void verifyTheiaRiskIntegrationForTINPay() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String sso = user.ssoToken();

        //deleting existing cards on user
        HandlerInternalDeleteBinUser.deleteAllCards(sso);
        HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(sso, true);
        JsonPath binInfoResponse = binInfo.execute().jsonPath();
        int size = Integer.parseInt(binInfoResponse.getString("SIZE"));
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(size).isEqualTo(0);

        //adding coft card
        tokenizeCard(coftMerchantPay, user, "VISA");

        binInfo = new HanlerInternalBinInfo(sso, true);
        binInfoResponse = binInfo.execute().jsonPath();

        //Initiate Transaction
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), coftMerchantPay)
                .setTxnValue("2000")
                .setSsoToken(user.ssoToken())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

        //V1 PTC
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                coftMerchantPay.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId())
                .setPaymentMode("CREDIT_CARD")
                .setAuthMode("otp")
                .setCardInfo(binInfoResponse.getString("BIN_DETAILS[0].SAVE_CARD_ID") + "||" + "|123")
                .build();

        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String grepcmd = "grep \"" + initTxnDTO.getBody().getOrderId() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + coftMerchantPay.getId() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        String verifyLogs = theiaFacadeLogs.substring(theiaFacadeLogs.indexOf("riskExtendInfo"), theiaFacadeLogs.indexOf("isContact"));
        Assertions.assertThat(verifyLogs).contains("\\\"par\\\":\\\"V0010013021363276718988547940\\\"").contains("\\\"lastFourDigits\\\":\\\"0436\\\"").contains("\\\"cardBin\\\":\\\"448968\\\"").contains("\\\"cin\\\":\\\"2021120756220cd76cdc11d3d89f59c17eb926bb88b22\\\"");
    }

    @Owner(Constants.Owner.MAYANK_BHARSHIV)
    @Feature("PGP-40009")
    @Test(description = "Verify PAR is sent in COP request for saved token")
    public void verifyTheiaRiskIntegrationForTINCOP() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String sso = user.ssoToken();

        //deleting existing cards on user
        HandlerInternalDeleteBinUser.deleteAllCards(sso);
        HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(sso, true);
        JsonPath binInfoResponse = binInfo.execute().jsonPath();
        int size = Integer.parseInt(binInfoResponse.getString("SIZE"));
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(size).isEqualTo(0);

        //adding coft card
        tokenizeCard(coftMerchantPay, user, "VISA");

        binInfo = new HanlerInternalBinInfo(sso, true);
        binInfoResponse = binInfo.execute().jsonPath();

        //Initiate Transaction
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), coftMerchantCop)
                .setTxnValue("2000")
                .setSsoToken(user.ssoToken())
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

        //V1 PTC
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                coftMerchantCop.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId())
                .setPaymentMode("CREDIT_CARD")
                .setAuthMode("otp")
                .setCardInfo(binInfoResponse.getString("BIN_DETAILS[0].SAVE_CARD_ID") + "||" + "|123")
                .build();

        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String grepcmd = "grep \"" + initTxnDTO.getBody().getOrderId() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + coftMerchantCop.getId() + "\" | grep \"ACQUIRING_CREATE_ORDER_AND_PAY\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        String verifyLogs = theiaFacadeLogs.substring(theiaFacadeLogs.indexOf("riskExtendInfo"), theiaFacadeLogs.indexOf("isContact"));
        Assertions.assertThat(verifyLogs).contains("\\\"par\\\":\\\"V0010013021363276718988547940\\\"").contains("\\\"lastFourDigits\\\":\\\"0436\\\"").contains("\\\"cardBin\\\":\\\"448968\\\"").contains("\\\"cin\\\":\\\"2021120756220cd76cdc11d3d89f59c17eb926bb88b22\\\"");
    }

}



