package scripts.Promo.ORRFlow;


import com.paytm.LocalConfig;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.EnablePaymentMode;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.pages.NativePlusHoldpayPage;
import com.paytm.utils.merchant.api.MappingService.GetMerchantExtendedInfo;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.extendedInfo.MerchExtendedInfo;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.testng.SkipException;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

@Owner("Bharat Gandhi")
@Feature("PGP-29683")
public class ORFPOSCM extends PGPBaseTest {
    @Parameters({"isNativePlus"})
    @Test
    public void validateORFlowFPOandInitiate_using_CIN(@Optional("true") Boolean isNativePlus) throws Exception {

        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        String SSotoken = user.ssoToken();
        Response res = AuthHelpers.getUserTokens(LocalConfig.AUTH_HOST, SSotoken);
        String token = res.jsonPath().getString("tokens.access_token[0]");
        PaymentDTO paymentDTO = new PaymentDTO();

        SavedCardHelpers.deleteSavedCard(user);
        String cin = SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.PROMO_CC_CARD_ICICI);
        Response bin8hashres = SavedCardHelpers.fetchCardsAlipay(user);
        String bin8hash = bin8hashres.jsonPath().get("assetInfos.CC[0].extendInfo.eightDigitBinHash");

        Constants.MerchantType promoMerchant = Constants.MerchantType.Hybrid;

        FetchPaymentOptionsDTO fpo_DTO = new FetchPaymentOptionsDTO.Builder("SSO", token)
                .setMid(promoMerchant.getId()).setAmount(100.0).setGenerateOrderId(null)
                .setCardHashRequired("true")
                .setFetchAllPaymentOffers("true")
                .setEightDigitBinRequired("true")
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode()
                        .setMode("CREDIT_CARD")
                        .setChannels(new Object[]{"MASTER", "VISA"})
                        .setBanks(new String[]{"ICICI"})})
                .build();
        FetchPaymentOption fpo = new FetchPaymentOption(promoMerchant.getId(), fpo_DTO);

        fpo.execute()
                .then()
                .body("body.resultInfo.resultStatus", Matchers.equalTo("S"))
                .body("body.resultInfo.resultMsg", Matchers.equalTo("Success"))
                .body("body.merchantPayOption.savedInstruments.cardDetails.cardId", Matchers.hasItem(cin))
                .body("body.merchantPayOption.savedInstruments.cardDetails.cardHash", Matchers.hasItem(cin))
                .body("body.merchantPayOption.savedInstruments.cardDetails.firstEightDigit", Matchers.hasItem(bin8hash));


        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(token, promoMerchant)
                .setCardHash(cin)
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode()
                        .setMode("CREDIT_CARD")
                        .setChannels(new Object[]{"MASTER", "VISA"})
                        .setBanks(new String[]{"ICICI"})})
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response initresponse = initTxn.execute();
        Assertions.assertThat(initresponse.jsonPath().get("body.txnToken").toString()).isNotNull();
        String orderId = initTxnDTO.orderFromBody();
        String txnToken = initresponse.jsonPath().get("body.txnToken").toString();


        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(promoMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cin + "||123|")
                .setExtendInfoOrderAlreadyCreated(true)
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();
        String JSON_POST_URL = LocalConfig.JSON_POST_URL;
        nativePlusHoldpayPage.launch(LocalConfig.MOCK_HOST + JSON_POST_URL)
                .fillAndSubmitJsonForm(processTxnV1Response.toString());
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(promoMerchant.getId())
                .validateTxnDate(new Date())
                .AssertAll();


    }

    @Owner("Shubham Soni")
    @Feature("PGP-41548")
    @Test(description = "Payment Flow is HYBRID when txn amount is greater than wallet amount for Hybrid Merchant")
    public void validatePaymentFlowinFPOHybrid() throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        WalletHelpers.modifyBalance(user, 5.00);
        String SSotoken = user.ssoToken();
        Constants.MerchantType promoMerchant = Constants.MerchantType.Hybrid_Retry;
        prerequisite:
        {
            MerchExtendedInfo merchExtendedInfo = GetMerchantExtendedInfo.executeMercExtendedInfo(promoMerchant.getId());
            boolean onPaytm = merchExtendedInfo.getExtendedInfo().getONPAYTM();
            if (onPaytm != true)
                throw new SkipException("isonPaytm is " + onPaytm + " for mid: " + promoMerchant.getId());
        }
        FetchPaymentOptionsDTO fpo_DTO = new FetchPaymentOptionsDTO.Builder("SSO", SSotoken)
                .setMid(promoMerchant.getId()).setAmount(10.0).setGenerateOrderId(null)
                .setCardHashRequired("true")
                .setFetchAllPaymentOffers("true")
                .setEightDigitBinRequired("true")
                .setOrderAmount("10")
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode()
                        .setMode("CREDIT_CARD")
                        .setChannels(new Object[]{"MASTER", "VISA"})
                        .setBanks(new String[]{"HDFC"}),new EnablePaymentMode().setMode("BALANCE")})
                .build();
        FetchPaymentOption fpo = new FetchPaymentOption(promoMerchant.getId(), fpo_DTO);
        Test:
        {
            fpo.execute()
                    .then()
                    .body("body.resultInfo.resultStatus", Matchers.equalTo("S"))
                    .body("body.resultInfo.resultMsg", Matchers.equalTo("Success"))
                    .body("body.paymentFlow", Matchers.equalTo("HYBRID"));
        }
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(SSotoken, promoMerchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode()
                        .setMode("CREDIT_CARD")
                        .setChannels(new Object[]{"MASTER", "VISA"})
                        .setBanks(new String[]{"HDFC"}),new EnablePaymentMode().setMode("BALANCE")})
                .setTxnValue("10")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response initresponse = initTxn.execute();
        Assertions.assertThat(initresponse.jsonPath().get("body.txnToken").toString()).isNotNull();
        String orderId = initTxnDTO.orderFromBody();
        String txnToken = initresponse.jsonPath().get("body.txnToken").toString();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(promoMerchant.getId(), txnToken, orderId)
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo("|4761360075860360|123|122024")
                .setPaymentFlow("HYBRID")
                .setAuthMode("otp")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();
        String JSON_POST_URL = LocalConfig.JSON_POST_URL;
        nativePlusHoldpayPage.launch(LocalConfig.MOCK_HOST + JSON_POST_URL)
                .fillAndSubmitJsonForm(processTxnV1Response.toString());
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(promoMerchant.getId())
                .validateTxnDate(new Date())
                .validatePaymentMode("HYBRID")
                .AssertAll();
    }

    @Owner("Shubham Soni")
    @Feature("PGP-41548")
    @Test(description = "Payment Flow is NONE when txn amount is less than wallet amount for Hybrid merchant")
    public void validatePaymentFlowinFPONONE() throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        WalletHelpers.modifyBalance(user, 5.00);
        String SSotoken = user.ssoToken();
        Constants.MerchantType promoMerchant = Constants.MerchantType.Hybrid_Retry;
        prerequisite:
        {
            MerchExtendedInfo merchExtendedInfo = GetMerchantExtendedInfo.executeMercExtendedInfo(promoMerchant.getId());
            boolean onPaytm = merchExtendedInfo.getExtendedInfo().getONPAYTM();
            if (onPaytm != true)
                throw new SkipException("isonPaytm is " + onPaytm + " for mid: " + promoMerchant.getId());
        }
        FetchPaymentOptionsDTO fpo_DTO = new FetchPaymentOptionsDTO.Builder("SSO", SSotoken)
                .setMid(promoMerchant.getId()).setAmount(2.0).setGenerateOrderId(null)
                .setCardHashRequired("true")
                .setFetchAllPaymentOffers("true")
                .setEightDigitBinRequired("true").setOrderAmount("2")
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode()
                        .setMode("CREDIT_CARD")
                        .setChannels(new Object[]{"MASTER", "VISA"})
                        .setBanks(new String[]{"HDFC"}),new EnablePaymentMode().setMode("BALANCE")})
                .build();
        FetchPaymentOption fpo = new FetchPaymentOption(promoMerchant.getId(), fpo_DTO);
        Test:
        {
            fpo.execute()
                    .then()
                    .body("body.resultInfo.resultStatus", Matchers.equalTo("S"))
                    .body("body.resultInfo.resultMsg", Matchers.equalTo("Success"))
                    .body("body.paymentFlow", Matchers.equalTo("NONE"));
        }
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(SSotoken, promoMerchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode()
                        .setMode("CREDIT_CARD")
                        .setChannels(new Object[]{"MASTER", "VISA"})
                        .setBanks(new String[]{"HDFC"}),new EnablePaymentMode().setMode("BALANCE")})
                .setTxnValue("2")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response initresponse = initTxn.execute();
        Assertions.assertThat(initresponse.jsonPath().get("body.txnToken").toString()).isNotNull();
        String orderId = initTxnDTO.orderFromBody();
        String txnToken = initresponse.jsonPath().get("body.txnToken").toString();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(promoMerchant.getId(), txnToken, orderId)
                .setPaymentMode("BALANCE")
                .setAuthMode("USRPWD")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();
        String JSON_POST_URL = LocalConfig.JSON_POST_URL;
        nativePlusHoldpayPage.launch(LocalConfig.MOCK_HOST + JSON_POST_URL)
                .fillAndSubmitJsonForm(processTxnV1Response.toString());
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(promoMerchant.getId())
                .validateTxnDate(new Date())
                .validatePaymentMode("PPI")
                .AssertAll();
    }

    @Owner("Shubham Soni")
    @Feature("PGP-41548")
    @Test(description = "Payment Flow is ADDNPAY when txn amount is more than wallet amount for ADDNPAYMerchant")
    public void validatePaymentFlowinFPOADDNPAY() throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        WalletHelpers.modifyBalance(user, 5.00);
        String SSotoken = user.ssoToken();
        Constants.MerchantType promoMerchant = Constants.MerchantType.WALLET_OFFER_ONUS;
        prerequisite:
        {
            MerchExtendedInfo merchExtendedInfo = GetMerchantExtendedInfo.executeMercExtendedInfo(promoMerchant.getId());
            boolean onPaytm = merchExtendedInfo.getExtendedInfo().getONPAYTM();
            if (onPaytm != true)
                throw new SkipException("isonPaytm is " + onPaytm + " for mid: " + promoMerchant.getId());
        }
        FetchPaymentOptionsDTO fpo_DTO = new FetchPaymentOptionsDTO.Builder("SSO", SSotoken)
                .setMid(promoMerchant.getId()).setAmount(10.0).setGenerateOrderId(null)
                .setCardHashRequired("true")
                .setFetchAllPaymentOffers("true")
                .setEightDigitBinRequired("true").setOrderAmount("10")
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode()
                        .setMode("CREDIT_CARD")
                        .setChannels(new Object[]{"MASTER", "VISA"})
                        .setBanks(new String[]{"HDFC"}),new EnablePaymentMode().setMode("BALANCE")})
                .build();
        FetchPaymentOption fpo = new FetchPaymentOption(promoMerchant.getId(), fpo_DTO);
       Test:
       {
           fpo.execute()
                   .then()
                   .body("body.resultInfo.resultStatus", Matchers.equalTo("S"))
                   .body("body.resultInfo.resultMsg", Matchers.equalTo("Success"))
                   .body("body.paymentFlow", Matchers.equalTo("ADDANDPAY"));
       }
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(SSotoken, promoMerchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode()
                        .setMode("CREDIT_CARD")
                        .setChannels(new Object[]{"MASTER", "VISA"})
                        .setBanks(new String[]{"HDFC"}),new EnablePaymentMode().setMode("BALANCE")})
                .setTxnValue("10")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response initresponse = initTxn.execute();
        Assertions.assertThat(initresponse.jsonPath().get("body.txnToken").toString()).isNotNull();
        String orderId = initTxnDTO.orderFromBody();
        String txnToken = initresponse.jsonPath().get("body.txnToken").toString();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(promoMerchant.getId(), txnToken, orderId)
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo("|4761360075860360|123|122024")
                .setPaymentFlow("ADDANDPAY")
                .setAuthMode("otp")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();
        String JSON_POST_URL = LocalConfig.JSON_POST_URL;
        nativePlusHoldpayPage.launch(LocalConfig.MOCK_HOST + JSON_POST_URL)
                .fillAndSubmitJsonForm(processTxnV1Response.toString());
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(promoMerchant.getId())
                .validateTxnDate(new Date())
                .validatePaymentMode("PPI")
                .AssertAll();


    }

    @Owner("Shubham Soni")
    @Feature("PGP-41548")
    @Test(description = "Payment Flow is NONE when txn amount is less than wallet amount for ADDNPAY merchant")
    public void validatePaymentFlowinFPONONEADDNPAY() throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        WalletHelpers.modifyBalance(user, 5.00);
        String SSotoken = user.ssoToken();
        Constants.MerchantType promoMerchant = Constants.MerchantType.WALLET_OFFER_ONUS;
        prerequisite:
        {
            MerchExtendedInfo merchExtendedInfo = GetMerchantExtendedInfo.executeMercExtendedInfo(promoMerchant.getId());
            boolean onPaytm = merchExtendedInfo.getExtendedInfo().getONPAYTM();
            if (onPaytm != true)
                throw new SkipException("isonPaytm is " + onPaytm + " for mid: " + promoMerchant.getId());
        }
        FetchPaymentOptionsDTO fpo_DTO = new FetchPaymentOptionsDTO.Builder("SSO", SSotoken)
                .setMid(promoMerchant.getId()).setAmount(2.0).setGenerateOrderId(null)
                .setCardHashRequired("true")
                .setFetchAllPaymentOffers("true")
                .setEightDigitBinRequired("true").setOrderAmount("2")
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode()
                        .setMode("CREDIT_CARD")
                        .setChannels(new Object[]{"MASTER", "VISA"})
                        .setBanks(new String[]{"HDFC"}),new EnablePaymentMode().setMode("BALANCE")})
                .build();
        FetchPaymentOption fpo = new FetchPaymentOption(promoMerchant.getId(), fpo_DTO);
        Test:
        {
            fpo.execute()
                    .then()
                    .body("body.resultInfo.resultStatus", Matchers.equalTo("S"))
                    .body("body.resultInfo.resultMsg", Matchers.equalTo("Success"))
                    .body("body.paymentFlow", Matchers.equalTo("NONE"));
        }
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(SSotoken, promoMerchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode()
                        .setMode("CREDIT_CARD")
                        .setChannels(new Object[]{"MASTER", "VISA"})
                        .setBanks(new String[]{"HDFC"}),new EnablePaymentMode().setMode("BALANCE")})
                .setTxnValue("2")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response initresponse = initTxn.execute();
        Assertions.assertThat(initresponse.jsonPath().get("body.txnToken").toString()).isNotNull();
        String orderId = initTxnDTO.orderFromBody();
        String txnToken = initresponse.jsonPath().get("body.txnToken").toString();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(promoMerchant.getId(), txnToken, orderId)
                .setPaymentMode("BALANCE")
                .setAuthMode("USRPWD")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();
        String JSON_POST_URL = LocalConfig.JSON_POST_URL;
        nativePlusHoldpayPage.launch(LocalConfig.MOCK_HOST + JSON_POST_URL)
                .fillAndSubmitJsonForm(processTxnV1Response.toString());
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(promoMerchant.getId())
                .validateTxnDate(new Date())
                .validatePaymentMode("PPI")
                .AssertAll();
    }

}
