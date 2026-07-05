package scripts.api.CardsControlConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.paytm.ServerConfigProvider;
import com.paytm.api.ProcessTransaction;
import com.paytm.api.ProcessTransactionV1;
import com.paytm.api.nativeAPI.FetchBinDetail;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.user.card.bin.query.BinModifyApi;
import com.paytm.api.user.card.bin.query.BinQueryApi;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.NativeDTO.fetchBinDetails.FetchBinDetailsRequest;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.time.Instant;

import static com.paytm.appconstants.Constants.Owner.HIMANSHU;
import static com.paytm.apphelpers.LogsValidationHelper.verifyLogsOnPod;
import static com.paytm.dto.PaymentDTO.*;


public class CardsControlConfig extends PGPBaseTest
{
    private final String onlineOfflineBlockedBin="444433332";//DEBIT_CARD_NUMBER
    private final String onlineBlockedBin="463917001";//VISA_COFT_CARD
    private final String offlineBlockedBin="516640003";//MASTER_ICICI_DEBIT_CARD_NUMBER
    private final String unblockedBin="476136007";//VISA_COFT_CARD_NUMBER


    @Owner(HIMANSHU)
    @Feature("PGP-56830")
    @Test(description = "For online mid, fetch bin shouldn't return bin info if isOnlineBlocked true and isOfflineBlocked true and ff4j is on")
    public void onlineMid_UnsavedCard_BothBlockedBin_ff4jON() throws Exception
    {
        Constants.MerchantType merchant = Constants.MerchantType.UI_TEXTMSG_LOGINQR_SavedCard;

        InitTxnDTO initTxnDTO= new InitTxnDTO.Builder(null, merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        fetchPaymentOption.execute();

        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, onlineOfflineBlockedBin).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode").contains("1003"));
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg").contains("This card is not supported for the transaction, please try with another card"));

    }

    @Owner(HIMANSHU)
    @Feature("PGP-56830")
    @Test(description = "For online mid, fetch bin shouldn't return bin info if isOnlineBlocked true and isOfflineBlocked false and ff4j is on")
    public void onlineMid_UnsavedCard_OnlineBlockedBin_ff4jON() throws Exception
    {
        Constants.MerchantType merchant = Constants.MerchantType.UI_TEXTMSG_LOGINQR_SavedCard;
        InitTxnDTO initTxnDTO= new InitTxnDTO.Builder(null, merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        fetchPaymentOption.execute();

        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, onlineBlockedBin).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode").contains("1003"));
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg").contains("This card is not supported for the transaction, please try with another card"));

    }
    @Owner(HIMANSHU)
    @Feature("PGP-56830")
    @Test(description = "For online mid, fetch bin should return bin info if isOnlineBlocked false and isOfflineBlocked true and ff4j is on")
    public void onlineMid_UnsavedCard_OfflineBlockedBin_ff4jON() throws Exception
    {
        Constants.MerchantType merchant = Constants.MerchantType.UI_TEXTMSG_LOGINQR_SavedCard;
        InitTxnDTO initTxnDTO= new InitTxnDTO.Builder(null, merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        fetchPaymentOption.execute();

        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, offlineBlockedBin).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode").contains("0000"));
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg").contains("Success"));

    }
    @Owner(HIMANSHU)
    @Feature("PGP-56830")
    @Test(description = "For online mid, fetch bin should return bin info if isOnlineBlocked false and isOfflineBlocked false and ff4j is on")
    public void onlineMid_UnsavedCard_UnblockedBin_ff4jON() throws Exception
    {
        Constants.MerchantType merchant = Constants.MerchantType.UI_TEXTMSG_LOGINQR_SavedCard;
        InitTxnDTO initTxnDTO= new InitTxnDTO.Builder(null, merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        fetchPaymentOption.execute();

        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, unblockedBin).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode").contains("0000"));
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg").contains("Success"));

    }

    @Owner(HIMANSHU)
    @Feature("PGP-56830")
    @Test(description = "For offline mid, fetch bin shouldn't return bin info if isOnlineBlocked true and isOfflineBlocked true and ff4j is on")
    public void offlineMid_UnsavedCard_BothBlockedBin_ff4jON() throws Exception
    {
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_WHITELISTED_OFF;
        InitTxnDTO initTxnDTO= new InitTxnDTO.Builder(null, merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        fetchPaymentOption.execute();

        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, onlineOfflineBlockedBin).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode").contains("1003"));
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg").contains("This card is not supported for the transaction, please try with another card"));

    }
    @Owner(HIMANSHU)
    @Feature("PGP-56830")
    @Test(description = "For offline mid, fetch bin should return bin info if isOnlineBlocked true and isOfflineBlocked false and ff4j is on")
    public void offlineMid_UnsavedCard_OnlineBlockedBin_ff4jON() throws Exception
    {
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_WHITELISTED_OFF;
        InitTxnDTO initTxnDTO= new InitTxnDTO.Builder(null, merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        fetchPaymentOption.execute();

        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, onlineBlockedBin).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode").contains("0000"));
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg").contains("Success"));

    }
    @Owner(HIMANSHU)
    @Feature("PGP-56830")
    @Test(description = "For offline mid, fetch bin shouldn't return bin info if isOnlineBlocked false and isOfflineBlocked true and ff4j is on")
    public void offlineMid_UnsavedCard_OfflineBlockedBin_ff4jON() throws Exception
    {
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_WHITELISTED_OFF;
        InitTxnDTO initTxnDTO= new InitTxnDTO.Builder(null, merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        fetchPaymentOption.execute();

        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, offlineBlockedBin).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode").contains("1003"));
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg").contains("This card is not supported for the transaction, please try with another card"));

    }

    @Owner(HIMANSHU)
    @Feature("PGP-56830")
    @Test(description = "For offline mid, fetch bin should return bin info if isOnlineBlocked false and isOfflineBlocked false and ff4j is on")
    public void offlineMid_UnsavedCard_UnblockedBin_ff4jON() throws Exception
    {
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_WHITELISTED_OFF;
        InitTxnDTO initTxnDTO= new InitTxnDTO.Builder(null, merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        fetchPaymentOption.execute();

        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, unblockedBin).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode").contains("0000"));
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg").contains("Success"));

    }
    @Owner(HIMANSHU)
    @Feature("PGP-56830")
    @Test(description = "For online mid, saved card shouldn't come if isOnlineBlocked true and isOfflineBlocked true and ff4j is on")
    public void onlineMid_SavedCard_BothBlockedBin_ff4jON() throws Exception
    {
        Constants.MerchantType merchant = Constants.MerchantType.COFT_THEIA_OFFUS;
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);

        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        SavedCardHelpers.addCardOnMidCustId(merchant,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR, DEBIT_CARD_NUMBER);
        String orderID= CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant,user.ssoToken(), orderID)
                .setCustId(custId)
                .setTxnValue("10")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();

        String txnToken = response.jsonPath().get("body.txnToken").toString();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");

        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, initTxnDTO.getBody().getOrderId(), "GET_TOKENIZED_CARDS_IN_FPO");
        Assertions.assertThat((theia_facade).contains("\"cardInfos\":[]"));
        System.out.println("logs:"+theia_facade);

    }

    @Owner(HIMANSHU)
    @Feature("PGP-56830")
    @Test(description = "For online mid, saved card shouldn't come if isOnlineBlocked true and isOfflineBlocked false and ff4j is on")
    public void onlineMid_SavedCard_OnlineBlockedBin_ff4jON() throws Exception
    {
        Constants.MerchantType merchant = Constants.MerchantType.COFT_THEIA_OFFUS;
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);

        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        SavedCardHelpers.addCardOnMidCustId(merchant,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR, VISA_COFT_CARD);
        String orderID= CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant,user.ssoToken(), orderID)
                .setCustId(custId)
                .setTxnValue("10")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();

        String txnToken = response.jsonPath().get("body.txnToken").toString();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");

        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, initTxnDTO.getBody().getOrderId(), "GET_TOKENIZED_CARDS_IN_FPO");
        Assertions.assertThat((theia_facade).contains("\"cardInfos\":[]"));

    }
    @Owner(HIMANSHU)
    @Feature("PGP-56830")
    @Test(description = "For online mid, saved card should come if isOnlineBlocked false and isOfflineBlocked true and ff4j is on")
    public void onlineMid_SavedCard_OfflineBlockedBin_ff4jON() throws Exception
    {
        Constants.MerchantType merchant = Constants.MerchantType.COFT_THEIA_OFFUS;

        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);

        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        SavedCardHelpers.addCardOnMidCustId(merchant,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR, MASTER_ICICI_DEBIT_CARD_NUMBER);
        String tin= SavedCardHelpers.getTin();
        String orderID= CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant,user.ssoToken(), orderID)
                .setCustId(custId)
                .setTxnValue("10")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();

        String txnToken = response.jsonPath().get("body.txnToken").toString();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");

        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, initTxnDTO.getBody().getOrderId(), "GET_TOKENIZED_CARDS_IN_FPO");
        Assertions.assertThat((theia_facade).contains(tin));


    }
    @Owner(HIMANSHU)
    @Feature("PGP-56830")
    @Test(description = "For online mid, saved card should come if isOnlineBlocked false and isOfflineBlocked false and ff4j is on")
    public void onlineMid_SavedCard_UnblockedBin_ff4jON() throws Exception
    {
        Constants.MerchantType merchant = Constants.MerchantType.COFT_THEIA_OFFUS;
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);

        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        SavedCardHelpers.addCardOnMidCustId(merchant,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR, VISA_COFT_CARD_NUMBER);
        String tin= SavedCardHelpers.getTin();
        String orderID= CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant,user.ssoToken(), orderID)
                .setCustId(custId)
                .setTxnValue("10")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();

        String txnToken = response.jsonPath().get("body.txnToken").toString();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");

        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, initTxnDTO.getBody().getOrderId(), "GET_TOKENIZED_CARDS_IN_FPO");
        Assertions.assertThat((theia_facade).contains(tin));


    }

    @Owner(HIMANSHU)
    @Feature("PGP-56830")
    @Test(description = "For offline mid, saved card shouldn't come if isOnlineBlocked true and isOfflineBlocked true and ff4j is on")
    public void offlineMid_SavedCard_BothBlockedBin_ff4jON() throws Exception
    {
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_WHITELISTED_OFF;
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);

        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCardOnMidCustId(merchant,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.VISA_COFT_CARD_NUMBER);
        String tin= SavedCardHelpers.getTin();
        String orderID= CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant,user.ssoToken(), orderID)
                .setCustId(custId)
                .setTxnValue("10")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();

        String txnToken = response.jsonPath().get("body.txnToken").toString();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");

        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, initTxnDTO.getBody().getOrderId(), "GET_TOKENIZED_CARDS_IN_FPO");
        System.out.println("logs"+theia_facade);
        Assertions.assertThat((theia_facade).contains("\"cardInfos\":[]"));
    }

    @Owner(HIMANSHU)
    @Feature("PGP-56830")
    @Test(description = "For offline mid, saved card should come if isOnlineBlocked true and isOfflineBlocked false and ff4j is on")
    public void offlineMid_SavedCard_OnlineBlockedBin_ff4jON() throws Exception
    {
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_WHITELISTED_OFF;
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);

        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        SavedCardHelpers.addCardOnMidCustId(merchant,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR, VISA_COFT_CARD);
        String tin= SavedCardHelpers.getTin();
        String orderID= CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant,user.ssoToken(), orderID)
                .setCustId(custId)
                .setTxnValue("10")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();

        String txnToken = response.jsonPath().get("body.txnToken").toString();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");

        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, initTxnDTO.getBody().getOrderId(), "GET_TOKENIZED_CARDS_IN_FPO");
        Assertions.assertThat((theia_facade).contains(tin));

    }
    @Owner(HIMANSHU)
    @Feature("PGP-56830")
    @Test(description = "For offline mid, saved card shouldn't come if isOnlineBlocked false and isOfflineBlocked true and ff4j is on")
    public void offlineMid_SavedCard_OfflineBlockedBin_ff4jON() throws Exception
    {
        Constants.MerchantType merchant = Constants.MerchantType.COFT_MERCHANT;
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);

        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        SavedCardHelpers.addCardOnMidCustId(merchant,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR, MASTER_ICICI_DEBIT_CARD_NUMBER);
        String tin= SavedCardHelpers.getTin();
        String orderID= CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant,user.ssoToken(), orderID)
                .setCustId(custId)
                .setTxnValue("10")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();

        String txnToken = response.jsonPath().get("body.txnToken").toString();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");

        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, initTxnDTO.getBody().getOrderId(), "GET_TOKENIZED_CARDS_IN_FPO");
        Assertions.assertThat((theia_facade).contains("\"cardInfos\":[]"));

    }

    @Owner(HIMANSHU)
    @Feature("PGP-56830")
    @Test(description = "For offline mid, saved card should come if isOnlineBlocked false and isOfflineBlocked false and ff4j is on")
    public void offlineMid_SavedCard_UnblockedBin_ff4jON() throws Exception
    {
        Constants.MerchantType merchant = Constants.MerchantType.OFFLINE_WHITELISTED_OFF;
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);

        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        SavedCardHelpers.addCardOnMidCustId(merchant,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.VISA_COFT_CARD_NUMBER);
        String tin= SavedCardHelpers.getTin();
        String orderID= CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant,user.ssoToken(), orderID)
                .setCustId(custId)
                .setTxnValue("10")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();

        String txnToken = response.jsonPath().get("body.txnToken").toString();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");

        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, initTxnDTO.getBody().getOrderId(), "GET_TOKENIZED_CARDS_IN_FPO");
        Assertions.assertThat((theia_facade).contains(tin));

    }

    @Owner(HIMANSHU)
    @Feature("PGP-56830")
    @Test(description = "For online mid, fetch bin should return bin info if isOnlineBlocked true and isOfflineBlocked true and ff4j is off")
    public void onlineMid_UnsavedCard_BothBlockedBin_ff4jOFF() throws Exception
    {
        Constants.MerchantType merchant = Constants.MerchantType.COFT_THEIA_OFFUS;

        InitTxnDTO initTxnDTO= new InitTxnDTO.Builder(null, merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        fetchPaymentOption.execute();

        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, onlineOfflineBlockedBin).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode").contains("0000"));
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg").contains("Success"));

    }

    @Owner(HIMANSHU)
    @Parameters({"theme"})
    @Feature("PGP-56830")
    @Test(description = "For offline mid, fetch bin should return bin info if isOnlineBlocked true and isOfflineBlocked true and ff4j is off")
    public void offlineMid_UnsavedCard_BothBlockedBin_ff4jOFF() throws Exception
    {
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CC_DC;
        InitTxnDTO initTxnDTO= new InitTxnDTO.Builder(null, merchant).build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        fetchPaymentOption.execute();

        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, onlineOfflineBlockedBin).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode").contains("0000"));
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg").contains("Success"));

    }

    @Owner(HIMANSHU)
    @Parameters({"theme"})
    @Feature("PGP-56830")
    @Test(description = "For offline mid, saved card should come if isOnlineBlocked true and isOfflineBlocked true and ff4j is off")
    public void offlineMid_SavedCard_BothBlockedBin_ff4jOFF() throws Exception
    {
        Constants.MerchantType merchant = Constants.MerchantType.PG2_CC_DC;
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);

        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCardOnMidCustId(merchant,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR, DEBIT_CARD_NUMBER);
        String tin= SavedCardHelpers.getTin();
        String orderID= CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant,user.ssoToken(), orderID)
                .setCustId(custId)
                .setTxnValue("10")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();

        String txnToken = response.jsonPath().get("body.txnToken").toString();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");

        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, initTxnDTO.getBody().getOrderId(), "GET_TOKENIZED_CARDS_IN_FPO");
        Assertions.assertThat((theia_facade).contains(tin));

    }

    @Owner(HIMANSHU)
    @Feature("PGP-56830")
    @Parameters({"theme"})
    @Test(description = "For online mid, saved card should come if isOnlineBlocked true and isOfflineBlocked true and ff4j is off")
    public void onlineMid_SavedCard_BothBlockedBin_ff4jOFF(@Optional("false") Boolean isNativePlus) throws Exception
    {
        Constants.MerchantType merchant = Constants.MerchantType.COFT_THEIA_OFFUS;
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);

        String custId = RandomStringUtils.randomAlphabetic(10)+ Instant.now().toEpochMilli();
        SavedCardHelpers.addCardOnMidCustId(merchant,custId,PaymentDTO.EXP_MONTH,PaymentDTO.EXP_YEAR,PaymentDTO.VISA_COFT_CARD_NUMBER);
        String tin= SavedCardHelpers.getTin();
        String orderID= CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(merchant,user.ssoToken(), orderID)
                .setCustId(custId)
                .setTxnValue("10")
                .setSsoToken("")
                .setOrderId(orderID)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();

        String txnToken = response.jsonPath().get("body.txnToken").toString();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");

        String theia_facade = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade, initTxnDTO.getBody().getOrderId(), "GET_TOKENIZED_CARDS_IN_FPO");
        Assertions.assertThat((theia_facade).contains(tin));
    }
}
