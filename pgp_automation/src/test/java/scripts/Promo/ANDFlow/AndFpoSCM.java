package scripts.Promo.ANDFlow;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.api.nativeAPI.FetchPaymentOptionV5;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.AuthHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.*;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedSubvention;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import scripts.LogValidationRetryAnalyser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Owner("Bharat Gandhi")
@Feature("PGP-29683")
public class AndFpoSCM extends PGPBaseTest {
    @Test(retryAnalyzer = LogValidationRetryAnalyser.class)
    public void Validate_PROMO_WITH_CIN_AND_BIN8HASH_IN_FPO() throws Exception {

        Constants.MerchantType promoMerchant = Constants.MerchantType.Hybrid;
        Merchant merchant = new Merchant(promoMerchant.getId(), true);
        User user = userManager.getForWrite(Label.BASIC);
        String SSotoken = user.ssoToken();
        Response res = AuthHelpers.getUserTokens(LocalConfig.AUTH_HOST, SSotoken);
        String token = res.jsonPath().getString("tokens.access_token[0]");

        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        String cin = SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.PROMO_CC_CARD_ICICI);

        Response bin8hashres = SavedCardHelpers.fetchCardsAlipay(user);
        String bin8hash = bin8hashres.jsonPath().getString("assetInfos.CC.extendInfo.eightDigitBinHash");

//        MerchantAddPreferenceInfoReq addpref = new MerchantAddPreferenceInfoReq.Builder(promoMerchant.getId(), "BLOCK_BULK_APPLY_PROMO", "ACTIVE", "N")
//                .build();
//        List bulkpref = addpref.getMerchantPreferenceInfos();
//        System.out.println("List of Pref" + bulkpref);

        Promo promocode = new Promo(true);
        merchant.getPromos().add(promocode);

        FetchPaymentOptionsDTO fpo_DTO = new FetchPaymentOptionsDTO.Builder("SSO", token)
                .setMid(promoMerchant.getId())
                .setAmount(100.0)
                .setGenerateOrderId(null)
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setEightDigitBinRequired("true")
                .build();
        FetchPaymentOption fpo = new FetchPaymentOption(promoMerchant.getId(), fpo_DTO);

        fpo.execute()
                .then()
                .body("body.paymentOffers.promocode", Matchers.containsString(promocode.getName()),
                        "body.merchantPayOption.savedInstruments.cardDetails.cardId", Matchers.equalTo(cin),
                        "body.merchantPayOption.savedInstruments.cardDetails.firstEightDigit", Matchers.equalTo(bin8hash),
                        "body.merchantPayOption.savedInstruments.cardDetails.paymentOfferDetails.promocodeApplied", Matchers.equalTo(promocode.getName()),
                        "body.merchantPayOption.savedInstruments.cardDetails.paymentOfferDetails.promotext", Matchers.containsString("applied successfully."));

        String grepcmdoffersearchres = "grep \"" + promoMerchant.getId() + "\"  /paytm/logs/theia_facade.log |grep \"PAYMENT_PROMO_SERVICE\" | grep \"v1/promosearch/payment/offers\"|grep \"RESPONSE\"| grep \"is8DigitBin\" |grep " + promocode.getName() + "\n";
        String theiafacadelogspromosearchresponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdoffersearchres);
        Assertions.assertThat(theiafacadelogspromosearchresponse).contains("is8DigitBin");

        String grepcmdbulkapplyreq = "grep /paytm/logs/theia_facade.log |grep \"PAYMENT_PROMO_SERVICE\" | grep \"v1/paymentpromo/applybulk\"|grep \"REQUEST\"| grep \"cardIndexNo\" \n";
        String theiafacadelogsbulkapplyreq = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdbulkapplyreq);
        Assertions.assertThat(theiafacadelogsbulkapplyreq).contains("cardIndexNo", cin);

        String grepcmdbulkapplyres = "grep /paytm/logs/theia_facade.log |grep \"PAYMENT_PROMO_SERVICE\" | grep \"v1/paymentpromo/applybulk\"|grep \"RESPONSE\"| grep \"promocode\" \n";
        String theiafacadelogsbulkapplyres = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdbulkapplyres);
        Assertions.assertThat(theiafacadelogsbulkapplyres).contains("cardIndexNo", "applied successfully.");


    }

    @Owner("Shubham Soni")
    @Feature("PGP-53224")
    @Test(description = "V2FPOSSO :Validate CustId is passed in Promo Request when Item details is passed.")
    public void validateCUSTIDPassedV2FPOSSO() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        String custId = user.custId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(Constants.MerchantType.CUST_ID_PROMO.getId())
                .setGenerateOrderId("true")
                .setFetchAllItemOffers(new fetchAllItemOffers() {{
                    setItems(Collections.singletonList(new SimplifiedSubvention.Item(
                            "123456789", "123456789", "18084", Collections.singletonList("6224"), "1",
                            "200", "51", true, false, null, true,
                            "offline", "G531BT-BQ002T", "1152435")));
                }})
                .setApplyItemOffers(new applyItemOffers() {{
                    setPromoContext(new promoContext() {{
                        setCart("123456789", "18084", "6224", "20000");
                    }});
                }})
                .build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(Constants.MerchantType.CUST_ID_PROMO.getId(), fetchPaymentOptionsDTO);
        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        String orderId = jsonPath.getString("body.orderId");

        String theiaFacadeLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,orderId,"PAYMENT_PROMO_SERVICE");
        Assertions.assertThat(theiaFacadeLogs).contains("v2/promosearch/payment/offers?X-CLIENT=PG&paytm-user-id="+custId+"&X-CLIENT-ID="+Constants.MerchantType.CUST_ID_PROMO.getId());

    }

    @Owner("Shubham Soni")
    @Feature("PGP-53224")
    @Test(description = "V1FPOSSO :Validate CustId is passed in Promo Request when Item details is passed.")
    public void validateCUSTIDPassedV1FPOSSO() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        String custId = user.custId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(Constants.MerchantType.CUST_ID_PROMO.getId())
                .setGenerateOrderId("true")
                .setFetchAllItemOffers(new fetchAllItemOffers() {{
                    setItems(Collections.singletonList(new SimplifiedSubvention.Item(
                            "123456789", "123456789", "18084", Collections.singletonList("6224"), "1",
                            "200", "51", true, false, null, true,
                            "offline", "G531BT-BQ002T", "1152435")));
                }})
                .setApplyItemOffers(new applyItemOffers() {{
                    setPromoContext(new promoContext() {{
                        setCart("123456789", "18084", "6224", "20000");
                    }});
                }})
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(Constants.MerchantType.CUST_ID_PROMO.getId(), fetchPaymentOptionsDTO);
        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        String orderId = jsonPath.getString("body.orderId");
        String theiaFacadeLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,orderId,"PAYMENT_PROMO_SERVICE");
        Assertions.assertThat(theiaFacadeLogs).contains("v2/promosearch/payment/offers?X-CLIENT=PG&paytm-user-id="+custId+"&X-CLIENT-ID="+Constants.MerchantType.CUST_ID_PROMO.getId());

    }

    @Owner("Shubham Soni")
    @Feature("PGP-53224")
    @Test(description = "V5FPOSSO :Validate CustId is passed in Promo Request when Item details is passed.")
    public void validateCUSTIDPassedV5FPOSSO() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        String custId = user.custId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(Constants.MerchantType.CUST_ID_PROMO.getId())
                .setGenerateOrderId("true")
                .setFetchAllItemOffers(new fetchAllItemOffers() {{
                    setItems(Collections.singletonList(new SimplifiedSubvention.Item(
                            "123456789", "123456789", "18084", Collections.singletonList("6224"), "1",
                            "200", "51", true, false, null, true,
                            "offline", "G531BT-BQ002T", "1152435")));
                }})
                .setApplyItemOffers(new applyItemOffers() {{
                    setPromoContext(new promoContext() {{
                        setCart("123456789", "18084", "6224", "20000");
                    }});
                }})
                .build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(Constants.MerchantType.CUST_ID_PROMO.getId(), fetchPaymentOptionsDTO);
        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        String orderId = jsonPath.getString("body.orderId");
        String theiaFacadeLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,orderId,"PAYMENT_PROMO_SERVICE");
        Assertions.assertThat(theiaFacadeLogs).contains("v2/promosearch/payment/offers?X-CLIENT=PG&paytm-user-id="+custId+"&X-CLIENT-ID="+Constants.MerchantType.CUST_ID_PROMO.getId());

    }

    @Owner("Shubham Soni")
    @Feature("PGP-53224")
    @Test(description = "V2FPOTxnToken :Validate CustId is passed in Promo Request when Item details is passed.")
    public void validateCUSTIDPassedV2FPOTxnToken() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        String custId = user.custId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.CUST_ID_PROMO)
                .setTxnValue("1.00")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setMid(Constants.MerchantType.CUST_ID_PROMO.getId())
                .setFetchAllItemOffers(new fetchAllItemOffers() {{
                    setItems(Collections.singletonList(new SimplifiedSubvention.Item(
                            "123456789", "123456789", "18084", Collections.singletonList("6224"), "1",
                            "200", "51", true, false, null, true,
                            "offline", "G531BT-BQ002T", "1152435")));
                }})
                .setApplyItemOffers(new applyItemOffers() {{
                    setPromoContext(new promoContext() {{
                        setCart("123456789", "18084", "6224", "20000");
                    }});
                }})
                .build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(Constants.MerchantType.CUST_ID_PROMO.getId(),initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response resposne = fetchPaymentOption.execute();

        String theiaFacadeLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,initTxnDTO.orderFromBody(),"PAYMENT_PROMO_SERVICE");
        Assertions.assertThat(theiaFacadeLogs).contains("v2/promosearch/payment/offers?X-CLIENT=PG&paytm-user-id="+custId+"&X-CLIENT-ID="+Constants.MerchantType.CUST_ID_PROMO.getId());

    }

    @Owner("Shubham Soni")
    @Feature("PGP-53224")
    @Test(description = "V5FPOTxnToken :Validate CustId is passed in Promo Request when Item details is passed.")
    public void validateCUSTIDPassedV5FPOTxnToken() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        String custId = user.custId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.CUST_ID_PROMO)
                .setTxnValue("1.00")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setMid(Constants.MerchantType.CUST_ID_PROMO.getId())
                .setFetchAllItemOffers(new fetchAllItemOffers() {{
                    setItems(Collections.singletonList(new SimplifiedSubvention.Item(
                            "123456789", "123456789", "18084", Collections.singletonList("6224"), "1",
                            "200", "51", true, false, null, true,
                            "offline", "G531BT-BQ002T", "1152435")));
                }})
                .setApplyItemOffers(new applyItemOffers() {{
                    setPromoContext(new promoContext() {{
                        setCart("123456789", "18084", "6224", "20000");
                    }});
                }})
                .build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(Constants.MerchantType.CUST_ID_PROMO.getId(),initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response resposne = fetchPaymentOption.execute();

        String theiaFacadeLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,initTxnDTO.orderFromBody(),"PAYMENT_PROMO_SERVICE");
        Assertions.assertThat(theiaFacadeLogs).contains("v2/promosearch/payment/offers?X-CLIENT=PG&paytm-user-id="+custId+"&X-CLIENT-ID="+Constants.MerchantType.CUST_ID_PROMO.getId());

    }

    @Owner("Shubham Soni")
    @Feature("PGP-53224")
    @Test(description = "V1FPOTxnToken :Validate CustId is passed in Promo Request when Item details is passed.")
    public void validateCUSTIDPassedV1FPOTxnToken() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        String custId = user.custId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.CUST_ID_PROMO)
                .setTxnValue("1.00")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setMid(Constants.MerchantType.CUST_ID_PROMO.getId())
                .setFetchAllItemOffers(new fetchAllItemOffers() {{
                    setItems(Collections.singletonList(new SimplifiedSubvention.Item(
                            "123456789", "123456789", "18084", Collections.singletonList("6224"), "1",
                            "200", "51", true, false, null, true,
                            "offline", "G531BT-BQ002T", "1152435")));
                }})
                .setApplyItemOffers(new applyItemOffers() {{
                    setPromoContext(new promoContext() {{
                        setCart("123456789", "18084", "6224", "20000");
                    }});
                }})
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(Constants.MerchantType.CUST_ID_PROMO.getId(),initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response resposne = fetchPaymentOption.execute();

        String theiaFacadeLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,initTxnDTO.orderFromBody(),"PAYMENT_PROMO_SERVICE");
        Assertions.assertThat(theiaFacadeLogs).contains("v2/promosearch/payment/offers?X-CLIENT=PG&paytm-user-id="+custId+"&X-CLIENT-ID="+Constants.MerchantType.CUST_ID_PROMO.getId());

    }

    @Owner("Shubham Soni")
    @Feature("PGP-53224")
    @Test(description = "V2FPOTxnToken :Validate CustId is not passed in Promo Request when Item details is passed without sso token")
    public void validateCUSTIDPassedV2FPOTxnTokenwithoutSSO() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        String custId = user.custId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", Constants.MerchantType.CUST_ID_PROMO)
                .setTxnValue("1.00")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setMid(Constants.MerchantType.CUST_ID_PROMO.getId())
                .setFetchAllItemOffers(new fetchAllItemOffers() {{
                    setItems(Collections.singletonList(new SimplifiedSubvention.Item(
                            "123456789", "123456789", "18084", Collections.singletonList("6224"), "1",
                            "200", "51", true, false, null, true,
                            "offline", "G531BT-BQ002T", "1152435")));
                }})
                .setApplyItemOffers(new applyItemOffers() {{
                    setPromoContext(new promoContext() {{
                        setCart("123456789", "18084", "6224", "20000");
                    }});
                }})
                .build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(Constants.MerchantType.CUST_ID_PROMO.getId(),initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response resposne = fetchPaymentOption.execute();

        String theiaFacadeLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,initTxnDTO.orderFromBody(),"PAYMENT_PROMO_SERVICE");
        Assertions.assertThat(theiaFacadeLogs).contains("v2/promosearch/payment/offers?X-CLIENT=PG&X-CLIENT-ID="+Constants.MerchantType.CUST_ID_PROMO.getId());

    }

    @Owner("Shubham Soni")
    @Feature("PGP-53224")
    @Test(description = "V5FPOTxnToken :Validate CustId is not passed in Promo Request when Item details is passed without sso token")
    public void validateCUSTIDPassedV5FPOTxnTokenwithoutSSO() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        String custId = user.custId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", Constants.MerchantType.CUST_ID_PROMO)
                .setTxnValue("1.00")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setMid(Constants.MerchantType.CUST_ID_PROMO.getId())
                .setFetchAllItemOffers(new fetchAllItemOffers() {{
                    setItems(Collections.singletonList(new SimplifiedSubvention.Item(
                            "123456789", "123456789", "18084", Collections.singletonList("6224"), "1",
                            "200", "51", true, false, null, true,
                            "offline", "G531BT-BQ002T", "1152435")));
                }})
                .setApplyItemOffers(new applyItemOffers() {{
                    setPromoContext(new promoContext() {{
                        setCart("123456789", "18084", "6224", "20000");
                    }});
                }})
                .build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(Constants.MerchantType.CUST_ID_PROMO.getId(),initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response resposne = fetchPaymentOption.execute();

        String theiaFacadeLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,initTxnDTO.orderFromBody(),"PAYMENT_PROMO_SERVICE");
        Assertions.assertThat(theiaFacadeLogs).contains("v2/promosearch/payment/offers?X-CLIENT=PG&X-CLIENT-ID="+Constants.MerchantType.CUST_ID_PROMO.getId());

    }

    @Owner("Shubham Soni")
    @Feature("PGP-53224")
    @Test(description = "V1FPOTxnToken :Validate CustId is not passed in Promo Request when Item details is passed without sso token")
    public void validateCUSTIDPassedV1FPOTxnTokenwithoutSSO() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        String custId = user.custId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", Constants.MerchantType.CUST_ID_PROMO)
                .setTxnValue("1.00")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setMid(Constants.MerchantType.CUST_ID_PROMO.getId())
                .setFetchAllItemOffers(new fetchAllItemOffers() {{
                    setItems(Collections.singletonList(new SimplifiedSubvention.Item(
                            "123456789", "123456789", "18084", Collections.singletonList("6224"), "1",
                            "200", "51", true, false, null, true,
                            "offline", "G531BT-BQ002T", "1152435")));
                }})
                .setApplyItemOffers(new applyItemOffers() {{
                    setPromoContext(new promoContext() {{
                        setCart("123456789", "18084", "6224", "20000");
                    }});
                }})
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(Constants.MerchantType.CUST_ID_PROMO.getId(),initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response resposne = fetchPaymentOption.execute();

        String theiaFacadeLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,initTxnDTO.orderFromBody(),"PAYMENT_PROMO_SERVICE");
        Assertions.assertThat(theiaFacadeLogs).contains("v2/promosearch/payment/offers?X-CLIENT=PG&X-CLIENT-ID="+Constants.MerchantType.CUST_ID_PROMO.getId());

    }
}


