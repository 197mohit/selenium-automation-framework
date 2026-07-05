package scripts.Native;

import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPaymentOptionV5;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.qr.GenerateQR;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.InitTxn.CashierAdditionalInfo;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.dto.saveCard.SaveCardResponseBase;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.ff4j.FF4JFlags;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.InternationalSavedCard;

import java.util.Date;

import static com.paytm.appconstants.Constants.Owner.POONAM;
import static org.codehaus.groovy.runtime.StringGroovyMethods.findAll;
import static org.hamcrest.Matchers.is;

public class NativeFPOV2 extends PGPBaseTest implements InternationalSavedCard {

    public static String generateQRViaWallet(Constants.MerchantType merchantType)
    {
        GenerateQR generateQR = new GenerateQR(merchantType.getId(),"");
        JsonPath generateJson = generateQR.execute().jsonPath();
        Assertions.assertThat(generateJson.getString("statusCode")).isEqualTo("200");
        String qrCodeId = generateJson.getString("response.qrCodeId").replaceAll("\\p{P}", "");
        return qrCodeId;
    }

    @Test(description = "Native : PG side: Non Logged In Flow : Verify that international card is visible in fpo on international supported MID")
    @Feature("PGP-23196")
    @Owner("Tarun")
    @Epic(Constants.Sprint.SPRINT33_2)
    @Description("Automation JIRA : PGP-26960")
    @Override
    public void internationalCardNonLoggedInFlowPGSide(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId = CommonHelpers.generateOrderId();
        Constants.MerchantType internationalMerchant = Constants.MerchantType.ALLPAYMODE;
        SavedCardHelpers.assertStoreCardPrefEnabled(internationalMerchant);

        SavedCardHelpers.disableAllSavedCardFlags();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.INTERNATIONAL_CARD);

        Double txnAmount = 2.0;

        //Deleting for Merchant on PGPDB
        SavedCardHelpers.deleteSavedCard(custId);

        //Adding for MID CustId on PGPDB
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        SaveCardResponseBase saveCardResponseBase = savedCardHelpers.saveCard_custId_mId(paymentDTO.getCreditCardNumber(), custId, internationalMerchant.getId(), paymentDTO.getExpMonth() + paymentDTO.getExpYear());
        String cardId = saveCardResponseBase.getResponse().toString();

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, internationalMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        // FPO V2

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2")
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoWithoutUser = response.jsonPath();

        //Card Id from PGPDB should be visible
        Assertions.assertThat(fpoWithoutUser.getString("body.merchantPayOption.savedInstruments.cardDetails.cardId")).containsOnlyOnce(cardId);

    }


/*    @Test(description = "Native P+ side : Non Logged In Flow  : Verify that international card is visible on fpo on international supported MID", enabled = false)
    @Feature("PGP-23196")
    @Owner("Tarun")
    @Epic(Constants.Sprint.SPRINT33_2)
    @Description("Automation JIRA : PGP-26960")
    @Override */
    public void internationalCardNonLoggedInFlowPPlusSide(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId =CommonHelpers.generateOrderId();
        Constants.MerchantType internationalMerchant = Constants.MerchantType.ALLPAYMODE;
        SavedCardHelpers.assertStoreCardPrefEnabled(internationalMerchant);

       SavedCardHelpers.enableAllSavedCardFlags();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.INTERNATIONAL_CARD);

        Double txnAmount = 2.0;

        //Deleting for Merchant on P+
        SavedCardHelpers.deleteSavedCardsAlipay(internationalMerchant.getId(),custId);

        //Adding for MID CustId on P+
        String cin = SavedCardHelpers.addCardAlipay(internationalMerchant.getId(),custId, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, internationalMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO V2

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoWithoutUser = response.jsonPath();

        //Card Id from P+  should be visible
        Assertions.assertThat(fpoWithoutUser.getString("body.merchantPayOption.savedInstruments.cardDetails.cardId")).containsOnlyOnce(cin);



    }

    @Test(description = "Native : PG side : Logged In Flow : Verify that international card is visible on fpo on international supported MID")
    @Feature("PGP-23196")
    @Owner("Tarun")
    @Epic(Constants.Sprint.SPRINT33_2)
    @Description("Automation JIRA : PGP-26960")
    @Override
    public void internationalCardLoggedInFlowPGSide(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId =CommonHelpers.generateOrderId();
        Constants.MerchantType internationalMerchant = Constants.MerchantType.ALLPAYMODE;
        SavedCardHelpers.assertStoreCardPrefEnabled(internationalMerchant);

        SavedCardHelpers.disableAllSavedCardFlags();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.INTERNATIONAL_CARD);

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(PGPBaseTest.Label.SAVECARDMIGRATION);

        //Deleting for User on PGPDB
        SavedCardHelpers.deleteSavedCard(user);

        //Adding for User on PGPDB
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String cardId = SavedCardHelpers.getSavedCardId(user,0);
        String cardIndex = SavedCardHelpers.getCIN(cardId);
        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), internationalMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO V2 with txnToken

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoWithoutUser = response.jsonPath();

        //Card Id from PGPDB should be visible
        Assertions.assertThat(fpoWithoutUser.getString("body.merchantPayOption.savedInstruments.cardDetails.cardId")).containsOnlyOnce(cardIndex);

        //FPO V2 with SSO

        FetchPaymentOptionsDTO fetchUserDTO = new FetchPaymentOptionsDTO.Builder()
                .setVersion("v2")
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(internationalMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchSSO = new FetchPaymentOption(internationalMerchant.getId(),fetchUserDTO);
        JsonPath fetchPaymentOptResponse = fetchSSO.execute().jsonPath();

        //Card Id from PGPDB should be visible
        Assertions.assertThat(fetchPaymentOptResponse.getString("body.merchantPayOption.savedInstruments.cardDetails.cardId")).containsOnlyOnce(cardIndex);

    }


    @Test(description = "P+ side : Logged In Flow : Verify that international card is visible on cashier page on international supported MID & SUCCESS txn using International Card CIN")
    @Feature("PGP-23196")
    @Owner("Tarun")
    @Epic(Constants.Sprint.SPRINT33_2)
    @Description("Automation JIRA : PGP-26960")
    @Override
    public void internationalCardLoggedInFlowPPlus(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String custId =CommonHelpers.generateOrderId();
        Constants.MerchantType internationalMerchant = Constants.MerchantType.ALLPAYMODE;
        SavedCardHelpers.assertStoreCardPrefEnabled(internationalMerchant);

        SavedCardHelpers.enableAllSavedCardFlags();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.INTERNATIONAL_CARD);

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(PGPBaseTest.Label.SAVECARDMIGRATION);

        //Deleting for User on P +
        SavedCardHelpers.deleteSavedCardsAlipay(user);

        //Adding for User on  P +
        String cin = SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), internationalMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO with txnToken

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoWithoutUser = response.jsonPath();

        //Card Id from PGPDB should be visible
        Assertions.assertThat(fpoWithoutUser.getString("body.merchantPayOption.savedInstruments.cardDetails.cardId")).containsOnlyOnce(cin);

        //FPO V2 with SSO

        FetchPaymentOptionsDTO fetchUserDTO = new FetchPaymentOptionsDTO.Builder()
                .setVersion("v2")
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(internationalMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchSSO = new FetchPaymentOption(internationalMerchant.getId(),fetchUserDTO);
        JsonPath fetchPaymentOptResponse = fetchSSO.execute().jsonPath();

        Assertions.assertThat(fetchPaymentOptResponse.getString("body.merchantPayOption.savedInstruments.cardDetails.cardId")).containsOnlyOnce(cin);

        paymentDTO.setSavedCardId(cin);

        OrderDTO orderDTO = new OrderFactory.Native(internationalMerchant, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.CREDIT_CARD)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();

        //PTC

        checkoutPage.createNativeOrder(orderDTO, true);
        PGPHelpers.validateSuccessResponsePage(orderDTO,internationalMerchant,Constants.Gateway.IHDF.toString(), Constants.Bank.HDFC_ONLY.toString(), "CC");
        PGPHelpers.validateSuccessTxnStatus(orderDTO,"CC", Constants.Bank.HDFC_ONLY.toString(), Constants.Gateway.IHDF.toString());
    }

    // Saved Cards are coming from P+ Side as cards have been migrated and related FF4j Flags are enabled, Hence disabling this Test Case
//    @Test(enabled = false, description = "PG side : Logged In & Non Logged In Flow : Verify that international card is not visible on cashier page on international non supported MID")
//    @Feature("PGP-23196")
//    @Owner("Tarun")
//    @Epic(Constants.Sprint.SPRINT33_2)
//    @Description("Automation JIRA : PGP-26960")
//    @Override
    public void internationalCardNotVisibleLoggedInFlowPGSide(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId =CommonHelpers.generateOrderId();
        Constants.MerchantType internationalMerchant = Constants.MerchantType.PGOnly;
        SavedCardHelpers.assertStoreCardPrefEnabled(internationalMerchant);

        SavedCardHelpers.disableAllSavedCardFlags();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.INTERNATIONAL_CARD);

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(PGPBaseTest.Label.SAVECARDMIGRATION);

        //Deleting for User on PGPDB
        SavedCardHelpers.deleteSavedCard(user);

        //Adding for User on PGPDB
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), internationalMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO with txnToken

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoWithoutUser = response.jsonPath();

        //Card Id from PGPDB should not be visible
        Assertions.assertThat(fpoWithoutUser.getList("body.merchantPayOption.savedInstruments.cardDetails.cardId")).isNullOrEmpty();

        //FPO with SSO

        FetchPaymentOptionsDTO fetchUserDTO = new FetchPaymentOptionsDTO.Builder()
                .setVersion("v2")
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(internationalMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchSSO = new FetchPaymentOption(internationalMerchant.getId(),fetchUserDTO);
        JsonPath fetchPaymentOptResponse = fetchSSO.execute().jsonPath();

        //Card Id from PGPDB should not be visible
        Assertions.assertThat(fetchPaymentOptResponse.getList("body.merchantPayOption.savedInstruments.cardDetails.cardId")).isNullOrEmpty();


    }

    @Test(description = "PG side : Logged In & Non Logged In Flow : Verify that international card is not visible on cashier page on international non supported MID")
    @Feature("PGP-23196")
    @Owner("Tarun")
    @Epic(Constants.Sprint.SPRINT33_2)
    @Description("Automation JIRA : PGP-26960")
    @Override
    public void internationalCardNotVisibleLoggedInFlowPPlus(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId =CommonHelpers.generateOrderId();
        Constants.MerchantType internationalMerchant = Constants.MerchantType.PGOnly;
        SavedCardHelpers.assertStoreCardPrefEnabled(internationalMerchant);

        SavedCardHelpers.enableAllSavedCardFlags();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.INTERNATIONAL_CARD);

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(PGPBaseTest.Label.SAVECARDMIGRATION);

        //Deleting for user on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(user);

        //Adding for user on P+ side
        SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), internationalMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO with txnToken

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setVersion("v2")
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoWithoutUser = response.jsonPath();

        //Card Id from PGPDB should not be visible
        Assertions.assertThat(fpoWithoutUser.getList("body.merchantPayOption.savedInstruments.cardDetails.cardId")).isNullOrEmpty();

        //FPO with SSO

        FetchPaymentOptionsDTO fetchUserDTO = new FetchPaymentOptionsDTO.Builder()
                .setVersion("v2")
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(internationalMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchSSO = new FetchPaymentOption(internationalMerchant.getId(),fetchUserDTO);
        JsonPath fetchPaymentOptResponse = fetchSSO.execute().jsonPath();

        //Card Id from PGPDB should not be visible
        Assertions.assertThat(fetchPaymentOptResponse.getList("body.merchantPayOption.savedInstruments.cardDetails.cardId")).isNullOrEmpty();


    }

//    @Test(description = "P + , PGP side : Recon Success : Bajaj fn card is getting filtered from both the sides",enabled = false)
//    @Feature("PGP-23196")
//    @Owner("Tarun")
//    @Epic(Constants.Sprint.SPRINT33_2)
//    @Description("Automation JIRA : PGP-26960")
//    @Override
    public void bajajFinservFilteringAlipay(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId =CommonHelpers.generateOrderId();
        Constants.MerchantType bajajfinemi = Constants.MerchantType.BAJAJFINEMI;
        SavedCardHelpers.assertStoreCardPrefEnabled(bajajfinemi);

        //MID/CustId
        FF4JFlags.disable("shortCircuitSavedCardServiceReadForMidCustId");
        FF4JFlags.enable("fetchSavedcardFromPlatformForMidCustId");
        FF4JFlags.enable("returnSavedCardsFromPlatformForMidCustId");

        //UserId
        FF4JFlags.disable("shortCircuitSavedCardServiceReadForUserId");
        FF4JFlags.enable("fetchSavedcardFromPlatformForUserId");
        FF4JFlags.enable("returnSavedCardsFromPlatformForUserId");

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.BAJAJ_FINSERV_CREDIT_CARD_NUMBER);

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.SAVECARDMIGRATION);

        //Deleting for user on PG side
        SavedCardHelpers.deleteSavedCard(user);

        //Deleting for user on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(user);

        //Adding for user on PGP Side
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        //Adding for user on P+ side
        SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        //Adding for MID CustId on PGP side
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        savedCardHelpers.saveCard_custId_mId(paymentDTO.getCreditCardNumber(), custId, bajajfinemi.getId(), paymentDTO.getExpMonth() + paymentDTO.getExpYear());

        //Adding for MID CustId on P+
        SavedCardHelpers.addCardAlipay(bajajfinemi.getId(),custId, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), bajajfinemi)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO with txnToken

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setVersion("v2")
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoWithoutUser = response.jsonPath();

        //Bajaj Fn Card should not be visible
        Assertions.assertThat(fpoWithoutUser.getList("body.merchantPayOption.savedInstruments.cardDetails.cardId")).isNullOrEmpty();

        //FPO with SSO

        FetchPaymentOptionsDTO fetchUserDTO = new FetchPaymentOptionsDTO.Builder()
                .setVersion("v2")
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(bajajfinemi.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchSSO = new FetchPaymentOption(bajajfinemi.getId(),fetchUserDTO);
        JsonPath fetchPaymentOptResponse = fetchSSO.execute().jsonPath();

        //Bajaj Fn Card should not be visible
        Assertions.assertThat(fetchPaymentOptResponse.getList("body.merchantPayOption.savedInstruments.cardDetails.cardId")).isNullOrEmpty();


    }

    @Test(description = "PG side: Verify that prepaid card should not be visible on cashier page if mid doesnt supoort it")
    @Feature("PGP-23196")
    @Owner("Tarun")
    @Epic(Constants.Sprint.SPRINT33_2)
    @Description("Automation JIRA : PGP-26960")
    @Override
    public void prepaidCardNotVisibleUnsupportedMidPG(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId =CommonHelpers.generateOrderId();
        Constants.MerchantType nonPrepaidMerchant = Constants.MerchantType.Hybrid;
        SavedCardHelpers.assertStoreCardPrefEnabled(nonPrepaidMerchant);
        PGPHelpers.validate_MerchantPreference(nonPrepaidMerchant.getId(),"PREPAID_CARD","N");

        SavedCardHelpers.disableAllSavedCardFlags();

        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.SAVECARDMIGRATION);

        //Deleting for Merchant on PGPDB
        SavedCardHelpers.deleteSavedCard(custId);

        //Deleting for User on  PGPDB
        SavedCardHelpers.deleteSavedCard(user);

        //Adding for MID CustId on PGPDB
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        SaveCardResponseBase saveCardResponseBase = savedCardHelpers.saveCard_custId_mId(paymentDTO.getDebitCardNumber(), custId, nonPrepaidMerchant.getId(), paymentDTO.getExpMonth() + paymentDTO.getExpYear());
        String cardId = saveCardResponseBase.getResponse().toString();

        //Adding for User on PGPDB
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getDebitCardNumber());

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), nonPrepaidMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO with txnToken

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setVersion("v2")
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoWithoutUser = response.jsonPath();

        //Prepaid Card should not be visible
        Assertions.assertThat(fpoWithoutUser.getList("body.merchantPayOption.savedInstruments.cardDetails.cardId")).isNullOrEmpty();

        //FPO with SSO

        FetchPaymentOptionsDTO fetchUserDTO = new FetchPaymentOptionsDTO.Builder()
                .setVersion("v2")
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(nonPrepaidMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchSSO = new FetchPaymentOption(nonPrepaidMerchant.getId(),fetchUserDTO);
        JsonPath fetchPaymentOptResponse = fetchSSO.execute().jsonPath();

        //Prepaid Card should not be visible
        Assertions.assertThat(fetchPaymentOptResponse.getList("body.merchantPayOption.savedInstruments.cardDetails.cardId")).isNullOrEmpty();


    }


//    @Test(description = "P + : Verify that prepaid card should not be visible on cashier page if mid doesnt supoort it. No matter weather fetched from P+ or PG end.", enabled = false)
//    @Feature("PGP-23196")
//    @Owner("Tarun")
//    @Epic(Constants.Sprint.SPRINT33_2)
//    @Description("Automation JIRA : PGP-26960")
//    @Override
    public void prepaidCardNotVisibleUnsupportedMid(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId =CommonHelpers.generateOrderId();
        Constants.MerchantType nonPrepaidMerchant = Constants.MerchantType.Hybrid;
        SavedCardHelpers.assertStoreCardPrefEnabled(nonPrepaidMerchant);
        PGPHelpers.validate_MerchantPreference(nonPrepaidMerchant.getId(),"PREPAID_CARD","N");

        SavedCardHelpers.enableAllSavedCardFlags();

        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.SAVECARDMIGRATION);

        //Deleting for user on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(user);

        //Adding for user on P+ side
        SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());

        //Adding for MID CustId on P+
        SavedCardHelpers.addCardAlipay(nonPrepaidMerchant.getId(),custId, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), nonPrepaidMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO with txnToken

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setVersion("v2")
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoWithoutUser = response.jsonPath();

        //Prepaid Card should not be visible
        Assertions.assertThat(fpoWithoutUser.getList("body.merchantPayOption.savedInstruments.cardDetails.cardId")).isNullOrEmpty();

        //FPO with SSO

        FetchPaymentOptionsDTO fetchUserDTO = new FetchPaymentOptionsDTO.Builder()
                .setVersion("v2")
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(nonPrepaidMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchSSO = new FetchPaymentOption(nonPrepaidMerchant.getId(),fetchUserDTO);
        JsonPath fetchPaymentOptResponse = fetchSSO.execute().jsonPath();

        //Prepaid Card should not be visible
        Assertions.assertThat(fetchPaymentOptResponse.getList("body.merchantPayOption.savedInstruments.cardDetails.cardId")).isNullOrEmpty();

    }

    @Test(description = "PG side: Verify that prepaid card should  be visible in FPO if mid supoorts it")
    @Feature("PGP-23196")
    @Owner("Tarun")
    @Epic(Constants.Sprint.SPRINT33_2)
    @Description("Automation JIRA : PGP-26960")
    @Override
    public void prepaidCardVisibleSupportedMidPG(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId =CommonHelpers.generateOrderId();
        Constants.MerchantType prepaidMerchant = Constants.MerchantType.MASKED_MOBILE_ENABLED;
        SavedCardHelpers.assertStoreCardPrefEnabled(prepaidMerchant);
        PGPHelpers.validate_MerchantPreference(prepaidMerchant.getId(),"PREPAID_CARD","Y");

        SavedCardHelpers.disableAllSavedCardFlags();

        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.SAVECARDMIGRATION);

        //Deleting for Merchant on PGPDB
        SavedCardHelpers.deleteSavedCard(custId);

        //Deleting for User on  PGPDB
        SavedCardHelpers.deleteSavedCard(user);

        //Adding for MID CustId on PGPDB
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        SaveCardResponseBase saveCardResponseBase = savedCardHelpers.saveCard_custId_mId(paymentDTO.getCreditCardNumber(), custId, prepaidMerchant.getId(), paymentDTO.getExpMonth() + paymentDTO.getExpYear());
        String cardId = saveCardResponseBase.getResponse().toString();

        //Adding for User on PGPDB
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getDebitCardNumber());

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), prepaidMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO with txnToken

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setVersion("v2")
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoWithoutUser = response.jsonPath();

        //Prepaid Card should be visible
        Assertions.assertThat(fpoWithoutUser.getString("body.merchantPayOption.savedInstruments.cardDetails.cardId")).containsOnlyOnce(cardId);


    }

//    @Test(description = "P + : Verify that prepaid card should  be visible on cashier page if mid supoorts it", enabled = false)
//    @Feature("PGP-23196")
//    @Owner("Tarun")
//    @Epic(Constants.Sprint.SPRINT33_2)
//    @Description("Automation JIRA : PGP-26960")
//    @Override
    public void prepaidCardVisibleSupportedMid(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId =CommonHelpers.generateOrderId();
        Constants.MerchantType prepaidMerchant = Constants.MerchantType.MASKED_MOBILE_ENABLED;
        SavedCardHelpers.assertStoreCardPrefEnabled(prepaidMerchant);
        PGPHelpers.validate_MerchantPreference(prepaidMerchant.getId(),"PREPAID_CARD","Y");

        SavedCardHelpers.enableAllSavedCardFlags();

        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.SAVECARDMIGRATION);

        //Deleting for user on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(user);

        //Adding for user on P+ side
        String cinDC1 = SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());

        //Adding for MID CustId on P+
        String cinDC2 = SavedCardHelpers.addCardAlipay(prepaidMerchant.getId(),custId, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), prepaidMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO with txnToken

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setVersion("v2")
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoWithoutUser = response.jsonPath();

        //Prepaid Card should be visible
        Assertions.assertThat(fpoWithoutUser.getString("body.merchantPayOption.savedInstruments.cardDetails.cardId")).contains(cinDC1,cinDC2);

        //FPO with SSO

        FetchPaymentOptionsDTO fetchUserDTO = new FetchPaymentOptionsDTO.Builder()
                .setVersion("v2")
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(prepaidMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchSSO = new FetchPaymentOption(prepaidMerchant.getId(),fetchUserDTO);
        JsonPath fetchPaymentOptResponse = fetchSSO.execute().jsonPath();

        //Prepaid Card should be visible
        Assertions.assertThat(fetchPaymentOptResponse.getString("body.merchantPayOption.savedInstruments.cardDetails.cardId")).contains(cinDC1,cinDC2);


    }

    @AfterSuite
    public void setSavedFlagToItsOriginalValue()
    {
        //As per sheet : https://docs.google.com/spreadsheets/d/1iH6UdZ3Q-0sVLzcgshIKbUMe62wlnGEtqUZU1D3vhi8/edit#gid=0
        //MID/CustId
        FF4JFlags.disable("shortCircuitSavedCardServiceReadForMidCustId");
        FF4JFlags.enable("fetchSavedcardFromPlatformForMidCustId");
        FF4JFlags.disable("returnSavedCardsFromPlatformForMidCustId");

        //UserId
        FF4JFlags.disable("shortCircuitSavedCardServiceReadForUserId");
        FF4JFlags.enable("fetchSavedcardFromPlatformForUserId");
        FF4JFlags.disable("returnSavedCardsFromPlatformForUserId");
    }

    @Owner(POONAM)
    @Test(description = "Verfiy if banklogourl parameter is available in v2/fpo for Net Banking")
    public void VerifyBankLogoforNBNative() throws Exception {
        String custId =CommonHelpers.generateOrderId();
        Double txnAmount = 2.0;
        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO with txnToken

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setVersion("v2")
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoWithoutUser = response.jsonPath();

        //Bank Logo Url should be visible
        Assertions.assertThat(fpoWithoutUser.getString("body.merchantPayOption.paymentModes[2].payChannelOptions[0].bankLogoUrl")).contains("/native/bank/HDFC.png");
    }

    @Owner(POONAM)
    @Test(description = "Verfiy if banklogourl parameter is available in v2/fpo for Saved Cards")
    public void VerifyBankLogoforSavedCardsNative() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        String custId =CommonHelpers.generateOrderId();
        Constants.MerchantType savedCardMerchant = Constants.MerchantType.NATIVE_HYBRID;
        SavedCardHelpers.assertStoreCardPrefEnabled(savedCardMerchant);
       SavedCardHelpers.enableAllSavedCardFlags();

        Double txnAmount = 2.0;

        //Deleting for user on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(user);

        //Adding for user on P+ side
         SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());

        //Adding for MID CustId on P+
        SavedCardHelpers.addCardAlipay(savedCardMerchant.getId(),custId, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), savedCardMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO with txnToken

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setVersion("v2")
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoResponse = response.jsonPath();

        //Bank Logo Url should be visible
        Assertions.assertThat(fpoResponse.getString("body.merchantPayOption.savedInstruments.bankLogoUrl")).contains("/native/bank/HDFC.png");
    }

    @Owner(POONAM)
    @Test(description = "Verfiy if banklogourl parameter is available in v2/fpo for Saved VPA")
    public void VerifyBankLogoforSavedvpaNative() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String custId =CommonHelpers.generateOrderId();
        Double txnAmount = 2.0;
        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PPBLC_ONLY)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO with txnToken

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setVersion("v2")
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoResponse = response.jsonPath();

        //Bank Logo Url should be visible
        response.then()
                .statusCode(200)
                .body("body.merchantPayOption.paymentModes.find{it.paymentMode == 'UPI'}.payChannelOptions.findAll{it.bankLogoUrl.contains('/native/bank/UPI.png')}.size()",is(1));
    }


    @Owner(POONAM)
    @Test(description = "Verfiy if banklogourl parameter is available in v2/fpo for EMI")
    public void VerifyBankLogoforEMINative() throws Exception {
        String custId =CommonHelpers.generateOrderId();
        Double txnAmount = 2.0;
        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.NATIVE_HYBRID)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO with txnToken

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setVersion("v2")
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoResponse = response.jsonPath();

        //Bank Logo Url should be visible
        Assertions.assertThat(fpoResponse.getString("body.merchantPayOption.paymentModes[6].payChannelOptions[0].bankLogoUrl")).contains("/native/bank/HDFC.png");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-49862")
    @Test(description = "Validate Login QR in FPO Client Id from Merchant Center")
    public void validateLoginQRClientIdfromMerchantCenter() throws Exception {
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MID_CLIENT_ID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.loginInfo.midClientId")).isEqualTo("pg-mid-client3-stag");   

         /* TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("PPI")
                .validateGatewayName(Constants.Gateway.WALLET.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .AssertAll(); */
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-49862")
    @Test(description = "Validate Login QR IN FPO Client Id from Cache")
    public void validateLoginQRClientIdfromCache() throws Exception {
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MID_CLIENT_ID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.loginInfo.midClientId")).isEqualTo("pg-mid-client3-stag");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                initTxnDTO.getBody().getMid(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode(Constants.Bank.PPBL.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateBankName(Constants.Bank.PPBL.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("NB")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-49862")
    @Test(description = "Validate Login QR IN FPO Client Id from Cache UPI Push")
    public void validateLoginQRClientIdfromCacheUPI() throws Exception {
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MID_CLIENT_ID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.loginInfo.midClientId")).isEqualTo("pg-mid-client3-stag");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.MID_CLIENT_ID.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin("NPCI,20150822,2.0|IvebqsSA1dDVYs3OBn4Q9\\/cOgJ5RecQQW7WCe4EOJBniwCUqI9ocIE50GMcbA5UPqdQuSO3urywKs47UTc1q1pN51zAeQ0ISxai+Yfii8amtYVeWL67G2lL9RS5NEp29C+7PQc+cL\\/j34mKrtUZvxA\\/GUiAjllwuTnTuud7hMhGNmO8h+fGmctKMrJsWtbULX4EMG\\/bO\\/ayMUpLRynqvR3nM2g8nfblqnukxApr2QJCy3LG0tzaNgVZc8rBmAFVCyweGKijmf0TKSv0dEEmm9js8In1+VH8da13zfwB52zfEPKc6gMtY6QYymGt6Z3Hekcz6gVR+XS8TSIswlReXCA==")
                .setRiskExtendInfo("deviceType:Mobile|timeZone:IST|osType:IOS|osVersion:15.1|platform:APP|terminalType:APP|deviceManufacturer:Apple|channelId:WAP|paymentFlow:NONE|versionCode:5109|screenResolution:750x1334|appVersion:9.21.0|operationType:PAYMENT|userLBSLatitude:32.19|isRooted:false|deviceId:1DCB75C8-1A6F-4A7B-94B6-C497542397D8|businessFlow:DEFER_CHECKOUT|deviceModel:iPhone 6s (iOS 15.1)|userLBSLongitude:75.65|language:en-IN")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getPAYMENTMODE()).isEqualTo("UPI");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPCODE()).isEqualTo("01");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName(Constants.Gateway.PPBEX.toString())
                .validateMid(Constants.MerchantType.MID_CLIENT_ID.getId())
                .AssertAll();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-49862")
    @Test(description = "Validate Login QR IN FPO Client Id when client id is not present on Merchant Center")
    public void validateLoginQRClientIdnotPresentonMerchantCenter() throws Exception {
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.loginInfo.midClientId")).isEqualTo(null);

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-49862")
    @Test(description = "Validate Login QR IN FPO Client Id when client id from cache and is not present on Merchant Center")
    public void validateLoginQRClientIdfromCacheAndnotPresentonMerchantCenter() throws Exception {
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.loginInfo.midClientId")).isEqualTo(null);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-49862")
    @Test(description = "Validate Login QR in FQR Client Id from Merchant Center")
    public void validateLoginQRFQRClientIdfromMerchantCenter() throws Exception {
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MID_CLIENT_ID;
        // generating QR code ID for the merchant
        String qrCodeId = generateQRViaWallet(merchant);
        //FetchQRPaymentDetails and get orderId and other paymethod details
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.loginInfo.midClientId")).isEqualTo("pg-mid-client3-stag");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-49862")
    @Test(description = "Validate Login QR IN FQR Client Id from Cache")
    public void validateLoginQRFQRClientIdfromCache() throws Exception {
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        Constants.MerchantType merchant = Constants.MerchantType.MID_CLIENT_ID;
        // generating QR code ID for the merchant
        String qrCodeId = generateQRViaWallet(merchant);
        //FetchQRPaymentDetails and get orderId and other paymethod details
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.loginInfo.midClientId")).isEqualTo("pg-mid-client3-stag");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-49862")
    @Test(description = "Validate Login QR IN FQR Client Id when client id is not present on Merchant Center")
    public void validateLoginQRFQRClientIdnotPresentonMerchantCenter() throws Exception {
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y;
        // generating QR code ID for the merchant
        String qrCodeId = generateQRViaWallet(merchant);
        //FetchQRPaymentDetails and get orderId and other paymethod details
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.loginInfo.midClientId")).isEqualTo(null);
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-49862")
    @Test(description = "Validate Login QR IN FPO Client Id when client id from cache and is not present on Merchant Center")
    public void validateLoginQRFQRClientIdfromCacheAndnotPresentonMerchantCenter() throws Exception {
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_COP_FULL_TRAFFIC_Y;
        // generating QR code ID for the merchant
        String qrCodeId = generateQRViaWallet(merchant);
        //FetchQRPaymentDetails and get orderId and other paymethod details
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchQRResponse.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchQRResponse.getString("body.paymentOptions.loginInfo.midClientId")).isEqualTo(null);
    }

    @Feature("PGP-49014")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that disabled wallet is returned in v5 FPO response")
    public void InativeWallet_returned_in_v2FPO_response() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_Prenotify_MID;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[2].displayName")).isEqualTo("Paytm Balance");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[2].paymentMode")).isEqualTo("BALANCE");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[2].isDisabled.status")).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[2].isDisabled.msg")).isEqualTo("Please create account");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[2].isDisabled.merchantAccept")).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[2].isDisabled.userAccountExist")).isEqualTo("false");

    }

    @Feature("PGP-49014")
    @Owner(Constants.Owner.AKSHAT)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that disabled wallet is returned in v5 FPO response")
    public void InativeWallet_returned_in_v5FPO_response() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_Prenotify_MID;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[2].displayName")).isEqualTo("Paytm Balance");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[2].paymentMode")).isEqualTo("BALANCE");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[2].isDisabled.status")).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[2].isDisabled.msg")).isEqualTo("Please create account");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[2].isDisabled.merchantAccept")).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[2].isDisabled.userAccountExist")).isEqualTo("false");

    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-52948")
    @Test(description = "Validate AddnPay Limit in FPO when balance is Zero , Flag is ON , CC in add money payment options && merchant pay option")
    public void validateccAddnPayLimitinFPOwhenbalanceisZero() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 0.0);
        Constants.MerchantType merchant = Constants.MerchantType.Static_True_Recent_True;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccAddnPayLimit")).isEqualTo("4000");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-52948")
    @Test(description = "Validate AddnPay Limit in FPO when balance is Non Zero , Flag is ON , CC in add money payment options && merchant pay option")
    public void validateccAddnPayLimitinFPOwhenbalanceisNonZero() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 1.0);
        Constants.MerchantType merchant = Constants.MerchantType.Static_True_Recent_True;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccAddnPayLimit")).isNull();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-52948")
    @Test(description = "Validate AddnPay Limit in FPO when Wallet is Not Allowed on Mid, Flag is ON , CC in add money payment options && merchant pay option")
    public void validateccAddnPayLimitinFPOwhenWalletisNotAllowedonMid() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 0.0);
        Constants.MerchantType merchant = Constants.MerchantType.WALLET_DISABLED_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccAddnPayLimit")).isNull();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-52948")
    @Test(description = "Validate AddnPay Limit in FPO when addnpay And CC Not Allowed on Mid, Flag is ON")
    public void validateccAddnPayLimitinFPOwhenaddnpayAndCCNotAllowedonMid() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 0.0);
        Constants.MerchantType merchant = Constants.MerchantType.ADDANDPAY_AND_CC_DISABLED_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccAddnPayLimit")).isNull();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-52948")
    @Test(description = "Validate AddnPay Limit in FPO when addnpay Not Allowed on Mid, Flag is ON")
    public void validateccAddnPayLimitinFPOwhenaddnpayNotAllowedonMid() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 0.0);
        Constants.MerchantType merchant = Constants.MerchantType.ADDANDPAY_DISABLED_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccAddnPayLimit")).isNull();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-52948")
    @Test(description = "Validate AddnPay Limit in FPO when CC Not Allowed on Mid, Flag is ON")
    public void validateccAddnPayLimitinFPOwhenCCNotAllowedonMid() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 0.0);
        Constants.MerchantType merchant = Constants.MerchantType.ADDANDPAY_AND_CC_DISABLED_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccAddnPayLimit")).isNull();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-52948")
    @Test(description = "Validate AddnPay Limit in FPO V5  when balance is Zero , Flag is ON , CC in add money payment options && merchant pay option")
    public void validateccAddnPayLimitinFPOV5whenbalanceisZero() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 0.0);
        Constants.MerchantType merchant = Constants.MerchantType.Static_True_Recent_True;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccAddnPayLimit")).isEqualTo("4000");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-52948")
    @Test(description = "Validate AddnPay Limit in FPO V5 when balance is Non Zero , Flag is ON , CC in add money payment options && merchant pay option")
    public void validateccAddnPayLimitinFPOV5whenbalanceisNonZero() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 1.0);
        Constants.MerchantType merchant = Constants.MerchantType.Static_True_Recent_True;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccAddnPayLimit")).isNull();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-52948")
    @Test(description = "Validate AddnPay Limit in FPO V5 when Wallet is Not Allowed on Mid, Flag is ON , CC in add money payment options && merchant pay option")
    public void validateccAddnPayLimitinFPOV5whenWalletisNotAllowedonMid() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 0.0);
        Constants.MerchantType merchant = Constants.MerchantType.WALLET_DISABLED_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccAddnPayLimit")).isNull();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-52948")
    @Test(description = "Validate AddnPay Limit in FPO V5 when addnpay And CC Not Allowed on Mid, Flag is ON")
    public void validateccAddnPayLimitinFPOV5whenaddnpayAndCCNotAllowedonMid() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 0.0);
        Constants.MerchantType merchant = Constants.MerchantType.ADDANDPAY_AND_CC_DISABLED_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccAddnPayLimit")).isNull();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-52948")
    @Test(description = "Validate AddnPay Limit in FPO V5 when addnpay Not Allowed on Mid, Flag is ON")
    public void validateccAddnPayLimitinFPOV5whenaddnpayNotAllowedonMid() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 0.0);
        Constants.MerchantType merchant = Constants.MerchantType.ADDANDPAY_DISABLED_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccAddnPayLimit")).isNull();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-52948")
    @Test(description = "Validate AddnPay Limit in FPO V5 when CC Not Allowed on Mid, Flag is ON")
    public void validateccAddnPayLimitinFPOV5whenCCNotAllowedonMid() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        WalletHelpers.modifyBalance(user, 0.0);
        Constants.MerchantType merchant = Constants.MerchantType.ADDANDPAY_AND_CC_DISABLED_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.ccAddnPayLimit")).isNull();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59127")
    @Test(description = "validate Prioritise Settlement Account UPI Profile FPO")
    public void validatePrioritiseSettlementAccountUPIProfileFPO() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.Static_True_Recent_True;
        CashierAdditionalInfo cashierAdditionalInfo = new CashierAdditionalInfo();
        cashierAdditionalInfo.setPriorityUpiAccount("5553");
        cashierAdditionalInfo.setItineraryDisplayURL("https://www.abcd.com");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setCashierAdditionalInfo(cashierAdditionalInfo).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.cashierAdditionalInfo.priorityUpiAccount")).isEqualTo("5553");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.cashierAdditionalInfo.itineraryDisplayURL")).isEqualTo("https://www.abcd.com");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59127")
    @Test(description = "validate Prioritise Settlement Account UPI Profile V5 FPO")
    public void validatePrioritiseSettlementAccountUPIProfileV5FPO() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.Static_True_Recent_True;
        CashierAdditionalInfo cashierAdditionalInfo = new CashierAdditionalInfo();
        cashierAdditionalInfo.setPriorityUpiAccount("5553");
        cashierAdditionalInfo.setItineraryDisplayURL("https://www.abcd.com");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setCashierAdditionalInfo(cashierAdditionalInfo).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.cashierAdditionalInfo.priorityUpiAccount")).isEqualTo("5553");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.cashierAdditionalInfo.itineraryDisplayURL")).isEqualTo("https://www.abcd.com");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59127")
    @Test(description = "validate Prioritise Settlement Account UPI Profile Subscription FPO")
    public void validatePrioritiseSettlementAccountUPIProfileSubscriptionFPO() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        CashierAdditionalInfo cashierAdditionalInfo = new CashierAdditionalInfo();
        cashierAdditionalInfo.setPriorityUpiAccount("5553");
        cashierAdditionalInfo.setItineraryDisplayURL("https://www.abcd.com");

        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setCashierAdditionalInfo(cashierAdditionalInfo)
                .build();
        InitTxnResponseDTO initTxnResponseDTO =  NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.cashierAdditionalInfo.priorityUpiAccount")).isEqualTo("5553");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.cashierAdditionalInfo.itineraryDisplayURL")).isEqualTo("https://www.abcd.com");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59127")
    @Test(description = "validate Prioritise Settlement Account UPI Profile FPO when account is empty string")
    public void validatePrioritiseSettlementAccountUPIProfileFPOwhenaccountisemptystring() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.Static_True_Recent_True;
        CashierAdditionalInfo cashierAdditionalInfo = new CashierAdditionalInfo();
        cashierAdditionalInfo.setPriorityUpiAccount("");
        cashierAdditionalInfo.setItineraryDisplayURL("https://www.abcd.com");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setCashierAdditionalInfo(cashierAdditionalInfo).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.cashierAdditionalInfo.priorityUpiAccount")).isEqualTo("");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.cashierAdditionalInfo.itineraryDisplayURL")).isEqualTo("https://www.abcd.com");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59127")
    @Test(description = "validate Prioritise Settlement Account UPI Profile V5 FPO when account is empty string")
    public void validatePrioritiseSettlementAccountUPIProfileV5FPOwhenaccountisemptystring() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.Static_True_Recent_True;
        CashierAdditionalInfo cashierAdditionalInfo = new CashierAdditionalInfo();
        cashierAdditionalInfo.setPriorityUpiAccount("");
        cashierAdditionalInfo.setItineraryDisplayURL("https://www.abcd.com");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setCashierAdditionalInfo(cashierAdditionalInfo).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.cashierAdditionalInfo.priorityUpiAccount")).isEqualTo("");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.cashierAdditionalInfo.itineraryDisplayURL")).isEqualTo("https://www.abcd.com");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59127")
    @Test(description = "validate Prioritise Settlement Account UPI Profile Subscription FPO when account is empty string")
    public void validatePrioritiseSettlementAccountUPIProfileSubscriptionFPOwhenaccountisemptystring() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        CashierAdditionalInfo cashierAdditionalInfo = new CashierAdditionalInfo();
        cashierAdditionalInfo.setPriorityUpiAccount("");
        cashierAdditionalInfo.setItineraryDisplayURL("https://www.abcd.com");

        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setCashierAdditionalInfo(cashierAdditionalInfo)
                .build();
        InitTxnResponseDTO initTxnResponseDTO =  NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.cashierAdditionalInfo.priorityUpiAccount")).isEqualTo("");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.cashierAdditionalInfo.itineraryDisplayURL")).isEqualTo("https://www.abcd.com");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59127")
    @Test(description = "validate Prioritise Settlement Account UPI Profile FPO when Itinerary Display URL is empty ")
    public void validatePrioritiseSettlementAccountUPIProfileFPOwhenItineraryDisplayURLisempty() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.Static_True_Recent_True;
        CashierAdditionalInfo cashierAdditionalInfo = new CashierAdditionalInfo();
        cashierAdditionalInfo.setPriorityUpiAccount("5553");
        cashierAdditionalInfo.setItineraryDisplayURL("");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setCashierAdditionalInfo(cashierAdditionalInfo).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.cashierAdditionalInfo.priorityUpiAccount")).isEqualTo("5553");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.cashierAdditionalInfo.itineraryDisplayURL")).isEqualTo("");
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59127")
    @Test(description = "validate Prioritise Settlement Account UPI Profile V5 FPO when Itinerary Display URL is null ")
    public void validatePrioritiseSettlementAccountUPIProfileV5FPOwhenItineraryDisplayURLisnull() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.Static_True_Recent_True;
        CashierAdditionalInfo cashierAdditionalInfo = new CashierAdditionalInfo();
        cashierAdditionalInfo.setPriorityUpiAccount("5553");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setCashierAdditionalInfo(cashierAdditionalInfo).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.cashierAdditionalInfo.priorityUpiAccount")).isEqualTo("5553");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.cashierAdditionalInfo.itineraryDisplayURL")).isNull();
    }

    @Owner(Constants.Owner.CHAKSHU)
    @Feature("PGP-59127")
    @Test(description = "validate Prioritise Settlement Account UPI Profile Subscription FPO Priority Upi Account is null")
    public void validatePrioritiseSettlementAccountUPIProfileSubscriptionFPOPriorityUpiAccountisnull() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        CashierAdditionalInfo cashierAdditionalInfo = new CashierAdditionalInfo();
        cashierAdditionalInfo.setItineraryDisplayURL("https://www.abcd.com");

        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setCashierAdditionalInfo(cashierAdditionalInfo)
                .build();
        InitTxnResponseDTO initTxnResponseDTO =  NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.cashierAdditionalInfo.priorityUpiAccount")).isNull();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.cashierAdditionalInfo.itineraryDisplayURL")).isEqualTo("https://www.abcd.com");
    }
}

