package scripts.Native;

import com.paytm.api.FetchUserPaymentOffers;
import com.paytm.api.GetPaymentStatus;
import com.paytm.api.Peon;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchEMIDetail;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPromoCodeDetail;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.theia.ApiV1ApplyPromo;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.PaymentOffersApplied;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedPaymentOffers;
import com.paytm.dto.NativeDTO.fetchEMIDetail.FetchEMIDetailRequest;
import com.paytm.dto.NativeDTO.fetchPromoCode.FetchPromoCodeDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.PeonResponse;
import com.paytm.framework.reporting.Owners;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.ff4j.FF4JFlags;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import scripts.api.theia.applyPromo.SSOTokenApplyPromoV1Test;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.appconstants.Constants.Owner.ESHANI;

@Owner("Deepak")
@Owners(author = "Deepak", qa = "Tarun")
public class FetchPromoCodeDetails extends PGPBaseTest {

    protected InitTxnDTO initiateTrxUsingPromo(String ssoToken, Constants.MerchantType merchantType, Constants.promoCode promoCode) {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, merchantType).
                setPromoCode(promoCode.toString()).build();
        return initTxnDTO;
    }

    protected String Validate_EMIDetails(String txnToken, InitTxnDTO initTxnDTO, String channelCode) {
        FetchEMIDetailRequest fetchEMIDetailRequest = new FetchEMIDetailRequest(txnToken, channelCode);
        Response res = new FetchEMIDetail(fetchEMIDetailRequest, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody()).execute();
        JsonPath path = res.jsonPath();
        return path.get("body.emiDetail.emiChannelInfos[0].planId");
    }


    @Test(description = "Verify User is able to search BALANCE promo code Details")
    public void TC_FPC_001() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = initiateTrxUsingPromo(user.ssoToken(),
                Constants.MerchantType.NATIVE_PROMO_HYBRID, Constants.promoCode.WALLET_PROMO);
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String trxToken = iniJsonPath.getString("body.txnToken");
        FetchPromoCodeDTO fetchPromoCodeDTO = new FetchPromoCodeDTO.Builder(trxToken, "BALANCE")
                .build();
        FetchPromoCodeDetail fetchPromoCodeDetail = new FetchPromoCodeDetail(fetchPromoCodeDTO, initTxnDTO);
        JsonPath FJsonPath = fetchPromoCodeDetail.execute().jsonPath();
        CommonHelpers.assertCheck(FJsonPath, new Object[]{
                "body.resultInfo.resultStatus", "SUCCESS",
                "body.resultInfo.resultCode", "01",
                "body.resultInfo.resultMsg", "Valid payment mode",
                "body.promoCodeDetail.promoCode", "WALLETPROMO",
                "body.promoCodeDetail.promoMsg", "This is WALLET promo"
        });
    }


    @Test(description = "Verify User is able to search Credit Card promo code Details")
    public void TC_FPC_002() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = initiateTrxUsingPromo(user.ssoToken(),
                Constants.MerchantType.NATIVE_PROMO_HYBRID, Constants.promoCode.CC_PROMO);
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String trxToken = iniJsonPath.getString("body.txnToken");
        FetchPromoCodeDTO fetchPromoCodeDTO = new FetchPromoCodeDTO.Builder(trxToken, "CREDIT_CARD").
                setCardNumber(PaymentDTO.PROMO_CC_CARD_HDFC).build();
        FetchPromoCodeDetail fetchPromoCodeDetail = new FetchPromoCodeDetail(fetchPromoCodeDTO, initTxnDTO);
        JsonPath FJsonPath = fetchPromoCodeDetail.execute().jsonPath();
        CommonHelpers.assertCheck(FJsonPath, new Object[]{
                "body.resultInfo.resultStatus", "SUCCESS",
                "body.resultInfo.resultCode", "01",
                "body.resultInfo.resultMsg", "Valid Card",
                "body.promoCodeDetail.promoCode", "CCPROMOAUTO",
                "body.promoCodeDetail.promoMsg", "CC promo for automation"
        });
    }

    @Test(description = "Verify User is able to search Debit Card promo code Details")
    public void TC_FPC_003() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        InitTxnDTO initTxnDTO = initiateTrxUsingPromo(user.ssoToken(),
                Constants.MerchantType.NATIVE_PROMO_HYBRID, Constants.promoCode.DC_PROMO);
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String trxToken = iniJsonPath.getString("body.txnToken");
        FetchPromoCodeDTO fetchPromoCodeDTO = new FetchPromoCodeDTO.Builder(trxToken, "DEBIT_CARD").
                setCardNumber(new PaymentDTO().getDebitCardNumber()).build();
        FetchPromoCodeDetail fetchPromoCodeDetail = new FetchPromoCodeDetail(fetchPromoCodeDTO, initTxnDTO);
        JsonPath FJsonPath = fetchPromoCodeDetail.execute().jsonPath();
        CommonHelpers.assertCheck(FJsonPath, new Object[]{
                "body.resultInfo.resultStatus", "SUCCESS",
                "body.resultInfo.resultCode", "01",
                "body.resultInfo.resultMsg", "Valid Card",
                "body.promoCodeDetail.promoCode", "DCPROMOAUTOM",
                "body.promoCodeDetail.promoMsg", "DC Promo for automation"
        });
    }

    @Test(description = "Verify User is able to search Net Banking promo code Details")
    public void TC_FPC_004() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = initiateTrxUsingPromo(user.ssoToken(),
                Constants.MerchantType.NATIVE_PROMO_HYBRID, Constants.promoCode.NB_PROMO);
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String trxToken = iniJsonPath.getString("body.txnToken");
        FetchPromoCodeDTO fetchPromoCodeDTO = new FetchPromoCodeDTO.Builder(trxToken, "NET_BANKING").
                setBankCode("ICICI").build();
        FetchPromoCodeDetail fetchPromoCodeDetail = new FetchPromoCodeDetail(fetchPromoCodeDTO, initTxnDTO);
        JsonPath FJsonPath = fetchPromoCodeDetail.execute().jsonPath();
        CommonHelpers.assertCheck(FJsonPath, new Object[]{
                "body.resultInfo.resultStatus", "SUCCESS",
                "body.resultInfo.resultCode", "01",
                "body.resultInfo.resultMsg", "Valid netbanking",
                "body.promoCodeDetail.promoCode", "NBPROMOAUTO",
                "body.promoCodeDetail.promoMsg", "This is Net banking promo"
        });
    }


    @Test(description = "Verify User is not able to search BALANCE promo code Details")
    public void TC_FPC_005() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_PROMO_HYBRID).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String trxToken = iniJsonPath.getString("body.txnToken");
        FetchPromoCodeDTO fetchPromoCodeDTO = new FetchPromoCodeDTO.Builder(trxToken, "BALANCE").build();
        FetchPromoCodeDetail fetchPromoCodeDetail = new FetchPromoCodeDetail(fetchPromoCodeDTO, initTxnDTO);
        JsonPath FJsonPath = fetchPromoCodeDetail.execute().jsonPath();
        CommonHelpers.assertCheck(FJsonPath, new Object[]{
                "body.resultInfo.resultStatus", "F",
                "body.resultInfo.resultCode", "1010",
                "body.resultInfo.resultMsg", "promo code is not valid",
        });
    }

    @Test(description = "Verify User is not able to search Credit Card promo code Details")
    public void TC_FPC_006() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_PROMO_HYBRID).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String trxToken = iniJsonPath.getString("body.txnToken");
        FetchPromoCodeDTO fetchPromoCodeDTO = new FetchPromoCodeDTO.Builder(trxToken, "CREDIT_CARD").
                setCardNumber(new PaymentDTO().getCreditCardNumber()).build();
        FetchPromoCodeDetail fetchPromoCodeDetail = new FetchPromoCodeDetail(fetchPromoCodeDTO, initTxnDTO);
        JsonPath FJsonPath = fetchPromoCodeDetail.execute().jsonPath();
        CommonHelpers.assertCheck(FJsonPath, new Object[]{
                "body.resultInfo.resultStatus", "F",
                "body.resultInfo.resultCode", "1010",
                "body.resultInfo.resultMsg", "promo code is not valid",
        });
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify User is able to search EMI promo code and process txn with CC EMI")
    public void TC_FPC_007(@Optional("false") boolean isNativePlus) throws Exception {

        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage;
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(paymentDTO.getEmiCard());
        Constants.MerchantType merchantType = Constants.MerchantType.AMEX_PCF;
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = initiateTrxUsingPromo(user.ssoToken(),
                merchantType, Constants.promoCode.EMI_PROMO_CODE);
        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String trxToken = iniJsonPath.getString("body.txnToken");
        FetchPromoCodeDTO fetchPromoCodeDTO = new FetchPromoCodeDTO.Builder(trxToken, "EMI").
                setCardNumber(paymentDTO.getEmiCard()).build();
        FetchPromoCodeDetail fetchPromoCodeDetail = new FetchPromoCodeDetail(fetchPromoCodeDTO, initTxnDTO);
        JsonPath FJsonPath = fetchPromoCodeDetail.execute().jsonPath();
        CommonHelpers.assertCheck(FJsonPath, new Object[]{
                "body.resultInfo.resultStatus", "SUCCESS",
                "body.resultInfo.resultCode", "01",
                "body.resultInfo.resultMsg", "Promocode Successfully Validated.",
                "body.promoCodeDetail.promoCode", Constants.promoCode.EMI_PROMO_CODE,
                "body.promoCodeDetail.promoMsg", "this is emi promocode"
        });
        String emiPlanId = Validate_EMIDetails(trxToken, initTxnDTO, "HDFC");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, trxToken, paymentDTO, PayMethodType.EMI).
                setPlanId(emiPlanId).setEMI_TYPE("CREDIT_CARD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Success Txn using Specific Promo for Cashback in New Flow")
    public void PGP_27167_ValidateCashbackPromoInPaymentOffersFlow() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        User user = userManager.getForRead(Label.BASIC);

        Promo promo = new Promo();
        merchant.getPromos().add(promo);
        SSOTokenApplyPromoV1Test applyPromo = new SSOTokenApplyPromoV1Test();

        Map<String, Object> paymentOption = new HashMap<>();
        paymentOption.put("transactionAmount", "1");
        paymentOption.put("payMethod", "CREDIT_CARD");
        paymentOption.put("cardNo", PaymentDTO.ICICI_CC_CARD);

        Map<String, Object> root = applyPromo.root();
        ((Map<String, Object>) root.get("body")).put("promocode", promo.getName());
        ((Map<String, Object>) root.get("body")).put("mid", merchant.getId());
        ((Map<String, Object>) root.get("head")).put("token", user.ssoToken());
        ((Map<String, Object>) root.get("body")).put("paymentOptions", Arrays.asList(paymentOption));

        RestAssured.given(applyPromo.reqBldr().removeQueryParam("mid").addQueryParam("mid",merchant.getId()).build()).body(root).post().then()
                .spec(applyPromo.getSuccess());

        Response response =  RestAssured.given(applyPromo.reqBldr().removeQueryParam("mid").addQueryParam("mid",merchant.getId()).build()).body(root).post();
        HashMap<String, Object> paymentOffersAppliedResponse = response.jsonPath().get("body.paymentOffer");

        PaymentOffersApplied paymentOffersApplied = new PaymentOffersApplied(paymentOffersAppliedResponse);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, paymentOffersApplied).setTxnValue("10")
                .build();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO",user.ssoToken())
                .setMid(merchantType.getId())
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String trxToken = iniJsonPath.getString("body.txnToken");
        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, trxToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage;
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(initTxnDTO.getBody().getMid())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateMid(orderDTO.getMID())
                .validateTxnDate(new Date())
                .AssertAll();
        SoftAssert softAssert = new SoftAssert();
        com.paytm.api.Peon peon = new Peon(orderId);
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderId);
        softAssert.assertEquals(peonResponse.getSTATUS(), "TXN_SUCCESS");
        softAssert.assertEquals(peonResponse.getTXNAMOUNT(), "10.00");
        softAssert.assertNotNull(peonResponse.getPaymentPromoCheckoutData());
        softAssert.assertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Success Txn using Discount Promo in Apply Promo Flow for Native plus")
    public void PGP_27167_ValidateDiscountPromoInPaymentOffersFlow() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        User user = userManager.getForRead(Label.BASIC);
        String txnamt = "10";
        Promo promo = new Promo();
        merchant.getPromos().add(promo);
        SSOTokenApplyPromoV1Test applyPromo = new SSOTokenApplyPromoV1Test();

        Map<String, Object> paymentOption = new HashMap<>();
        paymentOption.put("transactionAmount", txnamt);
        paymentOption.put("payMethod", "CREDIT_CARD");
        paymentOption.put("cardNo", PaymentDTO.ICICI_CC_CARD);

        Map<String, Object> root = applyPromo.root();
        ((Map<String, Object>) root.get("body")).put("promocode", "discount");
        ((Map<String, Object>) root.get("body")).put("mid", merchant.getId());
        ((Map<String, Object>) root.get("head")).put("token", user.ssoToken());
        ((Map<String, Object>) root.get("body")).put("paymentOptions", Arrays.asList(paymentOption));

        RestAssured.given(applyPromo.reqBldr().removeQueryParam("mid").addQueryParam("mid",merchant.getId()).build()).body(root).post().then()
                .spec(applyPromo.getSuccess());

        Response response =  RestAssured.given(applyPromo.reqBldr().removeQueryParam("mid").addQueryParam("mid",merchant.getId()).build()).body(root).post();
        HashMap<String, Object> paymentOffersAppliedResponse = response.jsonPath().get("body.paymentOffer");

        PaymentOffersApplied paymentOffersApplied = new PaymentOffersApplied(paymentOffersAppliedResponse);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, paymentOffersApplied)
                .setTxnValue(txnamt)
                .build();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO",user.ssoToken())
                .setMid(merchantType.getId())
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        fetchPaymentOption.execute().jsonPath();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String trxToken = iniJsonPath.getString("body.txnToken");
        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, trxToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage;
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        Double txnAmount = Double.parseDouble(initTxnDTO.txnAmountFromBody());
        responsePage.validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.doubleToTwoDigitAfterDecimalPoint(txnAmount - 0.01 * txnAmount))
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(initTxnDTO.getBody().getMid())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(CommonHelpers.doubleToTwoDigitAfterDecimalPoint(txnAmount - 0.01 * txnAmount))
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateMid(orderDTO.getMID())
                .validateTxnDate(new Date())
                .AssertAll();
        SoftAssert softAssert = new SoftAssert();
        com.paytm.api.Peon peon = new Peon(orderId);
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderId);
        softAssert.assertEquals(peonResponse.getSTATUS(), "TXN_SUCCESS");
        softAssert.assertEquals(peonResponse.getTXNAMOUNT(), CommonHelpers.doubleToTwoDigitAfterDecimalPoint(txnAmount - 0.01 * txnAmount));
        softAssert.assertNotNull(peonResponse.getPaymentPromoCheckoutData());
        softAssert.assertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify When both PaymentOffersApplied and SimplifiedPaymentOffers passed, it results to failure")
    public void ValidateFailureTxnUsingPaymentandSimplifiedoffers(@Optional("false") boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        User user = userManager.getForRead(Label.BASIC);

        Promo promo = new Promo();
        merchant.getPromos().add(promo);
        SSOTokenApplyPromoV1Test applyPromo = new SSOTokenApplyPromoV1Test();

        Map<String, Object> paymentOption = new HashMap<>();
        paymentOption.put("transactionAmount", "1");
        paymentOption.put("payMethod", "CREDIT_CARD");
        paymentOption.put("cardNo", PaymentDTO.ICICI_CC_CARD);

        Map<String, Object> root = applyPromo.root();
        ((Map<String, Object>) root.get("body")).put("promocode", promo.getName());
        ((Map<String, Object>) root.get("body")).put("mid", merchant.getId());
        ((Map<String, Object>) root.get("head")).put("token", user.ssoToken());
        ((Map<String, Object>) root.get("body")).put("paymentOptions", Arrays.asList(paymentOption));

        RestAssured.given(applyPromo.reqBldr().removeQueryParam("mid").addQueryParam("mid",merchant.getId()).build()).body(root).post().then()
        .spec(applyPromo.getSuccess());

        Response response =  RestAssured.given(applyPromo.reqBldr().removeQueryParam("mid").addQueryParam("mid",merchant.getId()).build()).body(root).post();
        HashMap<String, Object> paymentOffersAppliedResponse = response.jsonPath().get("body.paymentOffer");

        PaymentOffersApplied paymentOffersApplied = new PaymentOffersApplied(paymentOffersAppliedResponse);

        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("true");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, paymentOffersApplied,simplifiedPaymentOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String respMsg = iniJsonPath.getString("body.resultInfo.resultMsg");
        Assertions.assertThat(respMsg).isEqualTo("Invalid Promo Param");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Success Txn using Specific Promo for Cashback in New Flow")
    public void ValidateSuccessTxnUsingSpecificCashbackPromoInNewFlow(@Optional("true") boolean isNativePlus) throws Exception {
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage;
        PaymentDTO paymentDTO = new PaymentDTO();
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForRead(Label.BASIC);
        Promo promo = new Promo();
        new Merchant(merchantType.getId(), true).getPromos().add(promo);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String trxToken = iniJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, trxToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validatePayableAmount("1.00")
                .validatePaymentPromoCheckoutDataPresent()
                .validateTxnDate(new Date())
                .AssertAll();
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = merchant.execute();
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase("1.00");
        Assertions.assertThat(jsonPath.getString("body.payableAmount")).isEqualToIgnoringCase("1.00");
        Assertions.assertThat(jsonPath.getString("body.paymentPromoCheckoutData")).isNotNull();

        SoftAssert softAssert = new SoftAssert();
        com.paytm.api.Peon peon = new Peon(orderId);
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderId);
        softAssert.assertEquals(peonResponse.getSTATUS(), "TXN_SUCCESS");
        softAssert.assertEquals(peonResponse.getTXNAMOUNT(), "1.00");
        softAssert.assertEquals(peonResponse.getPAYABLE_AMOUNT(), "1.00");
        softAssert.assertNotNull(peonResponse.getPaymentPromoCheckoutData());
        softAssert.assertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Success Txn using Specific Promo for Discount in New Flow")
    public void ValidateSuccessTxnUsingSpecificDiscountPromoInNewFlow(@Optional("false") boolean isNativePlus) throws Exception {
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage;
        PaymentDTO paymentDTO = new PaymentDTO();
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForRead(Label.BASIC);
        Promo promo = new Promo();
        new Merchant(merchantType.getId(), true).getPromos().add(promo);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String trxToken = iniJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, trxToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("9.50")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validatePayableAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validatePaymentPromoCheckoutDataPresent()
                .validateTxnDate(new Date())
                .AssertAll();
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = merchant.execute();
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase("9.50");
        Assertions.assertThat(jsonPath.getString("body.payableAmount")).isEqualToIgnoringCase("10.00");
        Assertions.assertThat(jsonPath.getString("body.paymentPromoCheckoutData")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());

        SoftAssert softAssert = new SoftAssert();
        com.paytm.api.Peon peon = new Peon(orderId);
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderId);
        softAssert.assertEquals(peonResponse.getSTATUS(), "TXN_SUCCESS");
        softAssert.assertEquals(peonResponse.getTXNAMOUNT(), "9.50");
        softAssert.assertEquals(peonResponse.getPAYABLE_AMOUNT(), "10.00");
        softAssert.assertNotNull(peonResponse.getPaymentPromoCheckoutData());
        softAssert.assertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Success Txn using Any Available Promo in New Flow")
    public void ValidateSuccessTxnUsingAnyAvailablePromoInNewFlow(@Optional("false") boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage;
        PaymentDTO paymentDTO = new PaymentDTO();
        User user = userManager.getForRead(Label.BASIC);
        Promo promo = new Promo();
        new Merchant(merchantType.getId(), true).getPromos().add(promo);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("").setApplyAvailablePromo("true").setValidatePromo("true");
        String ssotoken = user.ssoToken();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String trxToken = iniJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, trxToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validatePayableAmount("1.00")
                .validatePaymentPromoCheckoutDataPresent()
                .validateTxnDate(new Date())
                .AssertAll();
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = merchant.execute();
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase("1.00");
        Assertions.assertThat(jsonPath.getString("body.payableAmount")).isEqualToIgnoringCase("1.00");
        Assertions.assertThat(jsonPath.getString("body.paymentPromoCheckoutData")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());

        SoftAssert softAssert = new SoftAssert();
        com.paytm.api.Peon peon = new Peon(orderId);
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderId);
        softAssert.assertEquals(peonResponse.getSTATUS(), "TXN_SUCCESS");
        softAssert.assertEquals(peonResponse.getTXNAMOUNT(), "1.00");
        softAssert.assertEquals(peonResponse.getPAYABLE_AMOUNT(), "1.00");
        softAssert.assertNotNull(peonResponse.getPaymentPromoCheckoutData());
        softAssert.assertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Merchant Retry is not allowed using Any Available Promo in New Promo Flow and Txn is failed")
    public void ValidateMerchantRetryNotAllowedUsingAnyAvailablePromoInNewFlow(@Optional("false") boolean isNativePlus) throws Exception {
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage;
        PaymentDTO paymentDTO = new PaymentDTO();
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        User user = userManager.getForWrite(Label.BASIC);
        Promo promo = new Promo();
        new Merchant(merchantType.getId(), true).getPromos().add(promo);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String trxToken = iniJsonPath.getString("body.txnToken");
        paymentDTO.setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, trxToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
             //RESPCODE Param not req to be validated as it get picked from DB
        responsePage.validateStatus("TXN_FAILURE")
                .assertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Txn goes to pending when Invalid Promo is used and setValidatePromo is true in New Flow")
    public void ValidateFailureTxnUsingInvalidPromoWithValidatePromoTrueInNewFlow(@Optional("false") boolean isNativePlus) throws Exception {
        CheckoutPage checkoutPage = new CheckoutPage();
        PaymentDTO paymentDTO = new PaymentDTO();
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForWrite(Label.BASIC);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("failure").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String trxToken = iniJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, trxToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.execute();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("PENDING")
                .validateTxnType("SALE")
                .validateRespCode("402")
                .validateMid(orderDTO.getMID())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Success Txn when Invalid Promo is used and setValidatePromo is false as Txn follows normally without promo")
    public void ValidateSuccessTxnUsingInvalidPromoWithValidatePromoFalseInNewFlow(@Optional("false") boolean isNativePlus) throws Exception {
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage;
        PaymentDTO paymentDTO = new PaymentDTO();
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("failure").setApplyAvailablePromo("true").setValidatePromo("false");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String trxToken = iniJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, trxToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = merchant.execute();
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase("1.00");
        Assertions.assertThat(jsonPath.getString("body.paymentPromoCheckoutData")).isNull();
        Assertions.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Failure Txn using Null Promo parameters")
    public void ValidateFailureTxnUsingNullPromoParamsInNewFlow(@Optional("false") boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("").setApplyAvailablePromo("").setValidatePromo("");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        Assertions.assertThat(iniJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(iniJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Invalid Promo Param");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify Failed Txn when validatepromo flag is true for a Non Promo Txn in New Flow")
    public void ValidateFailureTxnWhenValidatePromoisTrueForNonPromoTxnInNewFlow(@Optional("false") boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("").setApplyAvailablePromo("").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        Assertions.assertThat(iniJsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(iniJsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Invalid Promo Param");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify User is able to search EMI promo code and process txn with DC EMI")
    public void TC_FPC_008(@Optional("true") boolean isNativePlus) throws Exception {

        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage;
        PaymentDTO paymentDTO = new PaymentDTO();
        Constants.MerchantType merchantType = Constants.MerchantType.AMEX_PCF;
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = initiateTrxUsingPromo(user.ssoToken(),
                merchantType, Constants.promoCode.EMI_PROMO_CODE);
        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String trxToken = iniJsonPath.getString("body.txnToken");
        FetchPromoCodeDTO fetchPromoCodeDTO = new FetchPromoCodeDTO.Builder(trxToken, "EMI").
                setCardNumber(paymentDTO.getDebitCardNumber()).build();
        FetchPromoCodeDetail fetchPromoCodeDetail = new FetchPromoCodeDetail(fetchPromoCodeDTO, initTxnDTO);
        JsonPath FJsonPath = fetchPromoCodeDetail.execute().jsonPath();
        CommonHelpers.assertCheck(FJsonPath, new Object[]{
                "body.resultInfo.resultStatus", "SUCCESS",
                "body.resultInfo.resultCode", "01",
                "body.resultInfo.resultMsg", "Promocode Successfully Validated.",
                "body.promoCodeDetail.promoCode", Constants.promoCode.EMI_PROMO_CODE,
                "body.promoCodeDetail.promoMsg", "this is emi promocode"
        });
        String emiPlanId = Validate_EMIDetails(trxToken, initTxnDTO, "HDFC");

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, trxToken, paymentDTO, PayMethodType.EMI).
                setPlanId(emiPlanId).setEMI_TYPE("DEBIT_CARD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

    }

    //Disabling this testcase in PGP-27157 in Native as it requires to check in logs which is not possible.
//    @Parameters({"isNativePlus"})
//    @Test(description = "Verify Success Txn using Specific Promo for Cashback in New Flow", enabled = false)
    public void PGP_27157_ValidateBankOffersNotVisiblewithPWPMerchantfterLoggingIn(@Optional("true") boolean isNativePlus) throws Exception {
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage;
        PaymentDTO paymentDTO = new PaymentDTO();
        User user = userManager.getForWrite(Label.LOGIN);
        Constants.MerchantType merchant = Constants.MerchantType.PWP_DEFAULT;
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        for (int i=0; i<2; i++) {
            Promo promo = new Promo();
            new Merchant(merchant.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchant, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String trxToken = initTrxJsonPath.getString("body.txnToken");
        String orderId = initTxnDTO.orderFromBody();
        OrderDTO orderDTO = new OrderFactory.Native(merchant, orderId, trxToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
    }




    @Owner(ESHANI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify tncUrl not present in apply promo(cashback) response when ff4j flag is OFF")
    public void ValidateTncUrlNotPresentInApplyPromoForCashbackPromo() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        User user = userManager.getForWrite(Label.BASIC);
        SoftAssert softAssert = new SoftAssert();
        FF4JFlags.disable("theia.applyPromoSendResponseTncUrl");

        Promo promo = new Promo();
        merchant.getPromos().add(promo);
        SSOTokenApplyPromoV1Test applyPromo = new SSOTokenApplyPromoV1Test();

        Map<String, Object> paymentOption = new HashMap<>();
        paymentOption.put("transactionAmount", "1");
        paymentOption.put("payMethod", "CREDIT_CARD");
        paymentOption.put("cardNo", PaymentDTO.ICICI_CC_CARD);

        Map<String, Object> root = applyPromo.root();
        ((Map<String, Object>) root.get("body")).put("promocode", promo.getName());
        ((Map<String, Object>) root.get("body")).put("mid", merchant.getId());
        ((Map<String, Object>) root.get("head")).put("token", user.ssoToken());
        ((Map<String, Object>) root.get("body")).put("paymentOptions", Arrays.asList(paymentOption));

        RestAssured.given(applyPromo.reqBldr().removeQueryParam("mid").addQueryParam("mid",merchant.getId()).build()).body(root).post().then()
                .spec(applyPromo.getSuccess());

        Response response =  RestAssured.given(applyPromo.reqBldr().removeQueryParam("mid").addQueryParam("mid",merchant.getId()).build()).body(root).post();

        String tncUrl = response.jsonPath().getString("body.paymentOffer.tncUrl");
        Assertions.assertThat(tncUrl)
                .as("TncUrl present in apply promo response despite flag being OFF")
                .isNull();


        HashMap<String, Object> paymentOffersAppliedResponse = response.jsonPath().get("body.paymentOffer");

        PaymentOffersApplied paymentOffersApplied = new PaymentOffersApplied(paymentOffersAppliedResponse);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, paymentOffersApplied).setTxnValue("10")
                .build();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO",user.ssoToken())
                .setMid(merchantType.getId())
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String trxToken = iniJsonPath.getString("body.txnToken");
        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, trxToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage;
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(initTxnDTO.getBody().getMid())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateMid(orderDTO.getMID())
                .validateTxnDate(new Date())
                .AssertAll();

        com.paytm.api.Peon peon = new Peon(orderId);
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderId);
        softAssert.assertEquals(peonResponse.getSTATUS(), "TXN_SUCCESS");
        softAssert.assertEquals(peonResponse.getTXNAMOUNT(), "10.00");
        softAssert.assertNotNull(peonResponse.getPaymentPromoCheckoutData());
        softAssert.assertAll();
    }


    @Owner(ESHANI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify tncUrl present in apply promo (cashback) response when ff4j flag is ON")
    public void ValidateTncUrlPresentInApplyPromoForCashbackPromo() throws Exception {
        try {
            Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
            Merchant merchant = new Merchant(merchantType.getId(), true);
            User user = userManager.getForWrite(Label.BASIC);
            SoftAssert softAssert = new SoftAssert();
            FF4JFlags.enable("theia.applyPromoSendResponseTncUrl");

            Promo promo = new Promo();
            merchant.getPromos().add(promo);
            SSOTokenApplyPromoV1Test applyPromo = new SSOTokenApplyPromoV1Test();

            Map<String, Object> paymentOption = new HashMap<>();
            paymentOption.put("transactionAmount", "1");
            paymentOption.put("payMethod", "CREDIT_CARD");
            paymentOption.put("cardNo", PaymentDTO.ICICI_CC_CARD);

            Map<String, Object> root = applyPromo.root();
            ((Map<String, Object>) root.get("body")).put("promocode", promo.getName());
            ((Map<String, Object>) root.get("body")).put("mid", merchant.getId());
            ((Map<String, Object>) root.get("head")).put("token", user.ssoToken());
            ((Map<String, Object>) root.get("body")).put("paymentOptions", Arrays.asList(paymentOption));

            RestAssured.given(applyPromo.reqBldr().removeQueryParam("mid").addQueryParam("mid", merchant.getId()).build()).body(root).post().then()
                    .spec(applyPromo.getSuccess());

            Response response = RestAssured.given(applyPromo.reqBldr().removeQueryParam("mid").addQueryParam("mid", merchant.getId()).build()).body(root).post();

            String tncUrl = response.jsonPath().getString("body.paymentOffer.tncUrl");
            Assertions.assertThat(tncUrl)
                    .as("TncUrl not present in apply promo response despite flag being ON")
                    .isNotNull();


            HashMap<String, Object> paymentOffersAppliedResponse = response.jsonPath().get("body.paymentOffer");

            PaymentOffersApplied paymentOffersApplied = new PaymentOffersApplied(paymentOffersAppliedResponse);
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, paymentOffersApplied).setTxnValue("10")
                    .build();
            FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                    .setMid(merchantType.getId())
                    .build();
            FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),
                    initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
            JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

            InitTxn initTxn = new InitTxn(initTxnDTO);
            String orderId = initTxnDTO.orderFromBody();
            JsonPath iniJsonPath = initTxn.execute().jsonPath();
            String trxToken = iniJsonPath.getString("body.txnToken");
            PaymentDTO paymentDTO = new PaymentDTO();
            OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, trxToken, paymentDTO, PayMethodType.CREDIT_CARD)
                    .build();
            CheckoutPage checkoutPage = new CheckoutPage();
            checkoutPage.createNativeOrder(orderDTO, true);
            ResponsePage responsePage;
            responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateStatus("TXN_SUCCESS")
                    .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                    .validateOrderId(initTxnDTO.orderFromBody())
                    .validateMid(initTxnDTO.getBody().getMid())
                    .assertAll();
            TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(orderDTO.getORDER_ID())
                    .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateRespCode("01")
                    .validateMid(orderDTO.getMID())
                    .validateTxnDate(new Date())
                    .AssertAll();

            com.paytm.api.Peon peon = new Peon(orderId);
            peon.executeUntilGetResponse();
            PeonResponse peonResponse;
            peonResponse = peon.getPeonData(orderId);
            softAssert.assertEquals(peonResponse.getSTATUS(), "TXN_SUCCESS");
            softAssert.assertEquals(peonResponse.getTXNAMOUNT(), "10.00");
            softAssert.assertNotNull(peonResponse.getPaymentPromoCheckoutData());
            softAssert.assertAll();
        }
        finally {
            FF4JFlags.disable("theia.applyPromoSendResponseTncUrl");
        }
    }





    @Owner(ESHANI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify tncUrl not present in apply promo(discount) response when ff4j flag is OFF")
    public void ValidateTncUrlNotPresentInApplyPromoForDiscountPromo() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        User user = userManager.getForWrite(Label.BASIC);
        FF4JFlags.disable("theia.applyPromoSendResponseTncUrl");
        String txnamt = "10";
        Promo promo = new Promo();
        merchant.getPromos().add(promo);
        SSOTokenApplyPromoV1Test applyPromo = new SSOTokenApplyPromoV1Test();
        SoftAssert softAssert = new SoftAssert();

        Map<String, Object> paymentOption = new HashMap<>();
        paymentOption.put("transactionAmount", txnamt);
        paymentOption.put("payMethod", "CREDIT_CARD");
        paymentOption.put("cardNo", PaymentDTO.ICICI_CC_CARD);

        Map<String, Object> root = applyPromo.root();
        ((Map<String, Object>) root.get("body")).put("promocode", "discount");
        ((Map<String, Object>) root.get("body")).put("mid", merchant.getId());
        ((Map<String, Object>) root.get("head")).put("token", user.ssoToken());
        ((Map<String, Object>) root.get("body")).put("paymentOptions", Arrays.asList(paymentOption));

        RestAssured.given(applyPromo.reqBldr().removeQueryParam("mid").addQueryParam("mid",merchant.getId()).build()).body(root).post().then()
                .spec(applyPromo.getSuccess());

        Response response =  RestAssured.given(applyPromo.reqBldr().removeQueryParam("mid").addQueryParam("mid",merchant.getId()).build()).body(root).post();

        String tncUrl = response.jsonPath().getString("body.paymentOffer.tncUrl");
        Assertions.assertThat(tncUrl)
                .as("TncUrl present in apply promo response despite flag being OFF")
                .isNull();

        HashMap<String, Object> paymentOffersAppliedResponse = response.jsonPath().get("body.paymentOffer");

        PaymentOffersApplied paymentOffersApplied = new PaymentOffersApplied(paymentOffersAppliedResponse);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, paymentOffersApplied)
                .setTxnValue(txnamt)
                .build();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO",user.ssoToken())
                .setMid(merchantType.getId())
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        fetchPaymentOption.execute().jsonPath();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String trxToken = iniJsonPath.getString("body.txnToken");
        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, trxToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage;
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        Double txnAmount = Double.parseDouble(initTxnDTO.txnAmountFromBody());
        responsePage.validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.doubleToTwoDigitAfterDecimalPoint(txnAmount - 0.01 * txnAmount))
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(initTxnDTO.getBody().getMid())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(CommonHelpers.doubleToTwoDigitAfterDecimalPoint(txnAmount - 0.01 * txnAmount))
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateMid(orderDTO.getMID())
                .validateTxnDate(new Date())
                .AssertAll();

        com.paytm.api.Peon peon = new Peon(orderId);
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderId);
        softAssert.assertEquals(peonResponse.getSTATUS(), "TXN_SUCCESS");
        softAssert.assertEquals(peonResponse.getTXNAMOUNT(), CommonHelpers.doubleToTwoDigitAfterDecimalPoint(txnAmount - 0.01 * txnAmount));
        softAssert.assertNotNull(peonResponse.getPaymentPromoCheckoutData());
        softAssert.assertAll();
    }



    @Owner(ESHANI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify tncUrl present in apply promo(discount) response when ff4j flag is ON")
    public void ValidateTncUrlPresentInApplyPromoForDiscountPromo() throws Exception {
        try{
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        User user = userManager.getForWrite(Label.BASIC);
        FF4JFlags.enable("theia.applyPromoSendResponseTncUrl");
        String txnamt = "10";
        Promo promo = new Promo();
        merchant.getPromos().add(promo);
        SSOTokenApplyPromoV1Test applyPromo = new SSOTokenApplyPromoV1Test();
        SoftAssert softAssert = new SoftAssert();

        Map<String, Object> paymentOption = new HashMap<>();
        paymentOption.put("transactionAmount", txnamt);
        paymentOption.put("payMethod", "CREDIT_CARD");
        paymentOption.put("cardNo", PaymentDTO.ICICI_CC_CARD);

        Map<String, Object> root = applyPromo.root();
        ((Map<String, Object>) root.get("body")).put("promocode", "discount");
        ((Map<String, Object>) root.get("body")).put("mid", merchant.getId());
        ((Map<String, Object>) root.get("head")).put("token", user.ssoToken());
        ((Map<String, Object>) root.get("body")).put("paymentOptions", Arrays.asList(paymentOption));

        RestAssured.given(applyPromo.reqBldr().removeQueryParam("mid").addQueryParam("mid", merchant.getId()).build()).body(root).post().then()
                .spec(applyPromo.getSuccess());

        Response response = RestAssured.given(applyPromo.reqBldr().removeQueryParam("mid").addQueryParam("mid", merchant.getId()).build()).body(root).post();

        String tncUrl = response.jsonPath().getString("body.paymentOffer.tncUrl");
        Assertions.assertThat(tncUrl)
                .as("TncUrl not present in apply promo response despite flag being ON")
                .isNotNull();

        HashMap<String, Object> paymentOffersAppliedResponse = response.jsonPath().get("body.paymentOffer");

        PaymentOffersApplied paymentOffersApplied = new PaymentOffersApplied(paymentOffersAppliedResponse);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, paymentOffersApplied)
                .setTxnValue(txnamt)
                .build();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(merchantType.getId())
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        fetchPaymentOption.execute().jsonPath();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String trxToken = iniJsonPath.getString("body.txnToken");
        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, trxToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage;
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        Double txnAmount = Double.parseDouble(initTxnDTO.txnAmountFromBody());
        responsePage.validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.doubleToTwoDigitAfterDecimalPoint(txnAmount - 0.01 * txnAmount))
                .validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(initTxnDTO.getBody().getMid())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(CommonHelpers.doubleToTwoDigitAfterDecimalPoint(txnAmount - 0.01 * txnAmount))
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateMid(orderDTO.getMID())
                .validateTxnDate(new Date())
                .AssertAll();

        com.paytm.api.Peon peon = new Peon(orderId);
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderId);
        softAssert.assertEquals(peonResponse.getSTATUS(), "TXN_SUCCESS");
        softAssert.assertEquals(peonResponse.getTXNAMOUNT(), CommonHelpers.doubleToTwoDigitAfterDecimalPoint(txnAmount - 0.01 * txnAmount));
        softAssert.assertNotNull(peonResponse.getPaymentPromoCheckoutData());
        softAssert.assertAll();
        }
        finally
        {
            FF4JFlags.disable("theia.applyPromoSendResponseTncUrl");
        }
    }


    @Feature("PGP-30167")
    @Epic(Constants.Sprint.SPRINT37_0)
    @Owner("Eshani")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that Payment offers(promo) data is in merchant status response with Flag ON")
    public void PaymentOffersFlow_PromoON() throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.XIAOMI3;
      //  Flags are controlled on the basis of Mid now
      //  FF4JFlags.enableMidBased("theia.promoDataInMerchantStatusService", merchantType.getId());

        Merchant merchant = new Merchant(merchantType.getId(), true);
        User user = userManager.getForWrite(Label.BASIC);

        Promo promo = new Promo();
        merchant.getPromos().add(promo);
        ApiV1ApplyPromo apiV1ApplyPromo = new ApiV1ApplyPromo(merchantType.getId());

        Map<String, Object> paymentOption = new HashMap<>();
        paymentOption.put("transactionAmount", "1");
        paymentOption.put("payMethod", "CREDIT_CARD");
        paymentOption.put("cardNo", new PaymentDTO().getCreditCardNumber());

        apiV1ApplyPromo
                .setContext("body.promocode", promo.getName())
                .setContext("head.token", user.ssoToken())
                .setContext("body.paymentOptions", Arrays.asList(paymentOption));

        Response response = apiV1ApplyPromo.execute()
                .then()
                .spec(ApiV1ApplyPromo.ResultInfo.SUCCESS)
                .extract().response();

        HashMap<String, Object> paymentOffersAppliedResponse = response.jsonPath().get("body.paymentOffer");

        PaymentOffersApplied paymentOffersApplied = new PaymentOffersApplied(paymentOffersAppliedResponse);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, paymentOffersApplied).setTxnValue("10")
                .build();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(merchantType.getId())
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String trxToken = iniJsonPath.getString("body.txnToken");
        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, trxToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, true);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentPromoCheckoutDataPresent()
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateMid(orderDTO.getMID())
                .validateTxnDate(new Date())
                .AssertAll();

        SoftAssert softAssert = new SoftAssert();
        com.paytm.api.Peon peon = new Peon(orderId);
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderId);
        softAssert.assertEquals(peonResponse.getSTATUS(), "TXN_SUCCESS");
        softAssert.assertEquals(peonResponse.getTXNAMOUNT(), "10.00");
        softAssert.assertNotNull(peonResponse.getPaymentPromoCheckoutData());
        softAssert.assertAll();



        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus getPaymentStatus = new GetPaymentStatus(getPaymentStatusDTO);
        Response response1 = getPaymentStatus.execute();
        JsonPath jsonPath = response1.jsonPath();
        SoftAssert softAssert1= new SoftAssert();
        softAssert1.assertEquals(jsonPath.getString("body.resultInfo.resultStatus"),"TXN_SUCCESS");
        softAssert1.assertNotNull(jsonPath.getString("body.paymentPromoCheckoutData"));
        softAssert1.assertAll();

    }


    @Owner("Eshani")
    @Feature("PGP_28691")
    @Test(description = "Verify Bulk Apply api should not hit when BLOCK_BULK_APPLY_PROMO preference is active in show payment page")
    public void PGP_28691_BulkPromoShouldNotHitWhenBLOCK_BULK_APPLY_PROMOisActiveForShowPaymentPage(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SSOToken= user.ssoToken();
        Constants.MerchantType merchantType = Constants.MerchantType.BLOCK_BULK_APPLY;
        Promo promo = null;
        for (int i = 0; i < 2; i++) {
            promo = new Promo();
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        WalletHelpers.modifyBalance(user, 15.00);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setApplyAvailablePromo("true").setValidatePromo("false");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(SSOToken, merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchantType,initTxnDTO.getBody().getOrderId(),txnToken).build();

        CheckoutPage checkoutPage= new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.applyPromoText().assertNotVisible();
        cashierPage.tabSavedCard().click();
        cashierPage.applyPromoText().assertVisible();

    }


    @Owner("Eshani")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that Discount promo gets applied to promoAmount only not whole txnAmount")
    public void ValidateDiscountPromoIsAppliedToPromoAmountOnlyforApiFlow(@Optional("false") boolean isNativePlus) throws Exception {
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage;
        PaymentDTO paymentDTO = new PaymentDTO();
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForWrite(Label.BASIC);
        Promo promo = new Promo();
        new Merchant(merchantType.getId(), true).getPromos().add(promo);

        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true").setPromoAmount("10");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("20.00")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String trxToken = iniJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, trxToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("19.50")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validatePayableAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validatePaymentPromoCheckoutDataPresent()
                .validateTxnDate(new Date())
                .AssertAll();
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = merchant.execute();
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase("19.50");
        Assertions.assertThat(jsonPath.getString("body.payableAmount")).isEqualToIgnoringCase("20.00");
        Assertions.assertThat(jsonPath.getString("body.paymentPromoCheckoutData")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());

        SoftAssert softAssert = new SoftAssert();
        com.paytm.api.Peon peon = new Peon(orderId);
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderId);
        softAssert.assertEquals(peonResponse.getSTATUS(), "TXN_SUCCESS");
        softAssert.assertEquals(peonResponse.getTXNAMOUNT(), "19.50");
        softAssert.assertEquals(peonResponse.getPAYABLE_AMOUNT(), "20.00");
        softAssert.assertNotNull(peonResponse.getPaymentPromoCheckoutData());
        softAssert.assertAll();
    }

    @Owner("Eshani")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that Discount promo gets applied to promoAmount only not whole txnAmount")
    public void ValidateCashbackPromoIsAppliedToPromoAmountOnlyforApiFlow(@Optional("true") boolean isNativePlus) throws Exception {
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage;
        PaymentDTO paymentDTO = new PaymentDTO();
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForWrite(Label.BASIC);
        Promo promo = new Promo();
        new Merchant(merchantType.getId(), true).getPromos().add(promo);

        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("cashback").setApplyAvailablePromo("true").setValidatePromo("true").setPromoAmount("10");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("20.00")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String trxToken = iniJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, trxToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("20")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validatePayableAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validatePaymentPromoCheckoutDataPresent()
                .validateTxnDate(new Date())
                .AssertAll();

        SoftAssert softAssert = new SoftAssert();
        com.paytm.api.Peon peon = new Peon(orderId);
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderId);
        softAssert.assertEquals(peonResponse.getSTATUS(), "TXN_SUCCESS");
        softAssert.assertEquals(peonResponse.getTXNAMOUNT(), "20.00");
        softAssert.assertEquals(peonResponse.getPAYABLE_AMOUNT(), "20.00");
        softAssert.assertNotNull(peonResponse.getPaymentPromoCheckoutData());

        JsonPath jsonPath= new JsonPath(peonResponse.getPaymentPromoCheckoutData());
        softAssert.assertEquals(jsonPath.getString("savings.savings"),"[0.50]" );
        softAssert.assertEquals(jsonPath.getString("savings.redemptionType"),"[cashback]");

        softAssert.assertAll();

    }


    @Parameters({"theme"})
    @Test(description = "Verify that promo gets applied to promoAmount only in bulk apply also in show Payment page")
    public void ValidatePromoIsAppliedToPromoAmountOnlyinBulkApplyforAppInvoke(@Optional("enhancedweb_revamp") String theme) throws Exception {

        ResponsePage responsePage;
        PaymentDTO paymentDTO = new PaymentDTO();
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForWrite(Label.SAVECARDMIGRATION);
        WalletHelpers.modifyBalance(user,Double.parseDouble("100.00"));
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());
        for (int i = 0; i <= 2; i++) {
            Promo promo = new Promo();
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }


        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers
                .setPromoCode("")
                .setApplyAvailablePromo("true")
                .setValidatePromo("true")
                .setPromoAmount("10");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("20.00")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String txnToken = iniJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchantType,initTxnDTO.getBody().getOrderId(),txnToken, "true", "true")
                .setTXN_AMOUNT("20")
                .build();
        CheckoutPage showPaymentPage= new CheckoutPage();
        showPaymentPage.createAppInvokeOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.promoOffersList().assertVisible();
        String promoOfferText= cashierPage.applyPromoText().getText();
        Assert.assertTrue(promoOfferText.contains("0.5"));
        String expectedTxnAmt;
        if(promoOfferText.contains("discount"))
            expectedTxnAmt="19.50";
        else
            expectedTxnAmt="20.00";

        cashierPage.payBy(Constants.PayMode.CC);

        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(expectedTxnAmt)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validatePayableAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validatePaymentPromoCheckoutDataPresent()
                .validateTxnDate(new Date())
                .AssertAll();
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = merchant.execute();
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(expectedTxnAmt);
        Assertions.assertThat(jsonPath.getString("body.payableAmount")).isEqualToIgnoringCase("20.00");
        Assertions.assertThat(jsonPath.getString("body.paymentPromoCheckoutData")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());

        SoftAssert softAssert = new SoftAssert();
        com.paytm.api.Peon peon = new Peon(orderId);
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderId);
        softAssert.assertEquals(peonResponse.getSTATUS(), "TXN_SUCCESS");
        softAssert.assertEquals(peonResponse.getTXNAMOUNT(), expectedTxnAmt);
        softAssert.assertEquals(peonResponse.getPAYABLE_AMOUNT(), "20.00");
        softAssert.assertNotNull(peonResponse.getPaymentPromoCheckoutData());

        if(promoOfferText.contains("cashback")) {
            JsonPath jsonPath1 = new JsonPath(peonResponse.getPaymentPromoCheckoutData());
            softAssert.assertEquals(jsonPath1.getString("savings.savings"), "[0.50]");
            softAssert.assertEquals(jsonPath1.getString("savings.redemptionType"), "[cashback]");
        }

        softAssert.assertAll();
    }


    @Parameters({"theme"})
    @Test(description = "Verify that Discount promo gets applied to promoAmount only for PCF merchant in showPaymentPage")
    public void ValidateDiscountPromoIsAppliedToPromoAmountOnlyforPCF(@Optional("enhancedweb_revamp") String theme) throws Exception {

        ResponsePage responsePage;
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Pcf;
        User user = userManager.getForWrite(Label.BASIC);
        Promo promo = new Promo();
        new Merchant(merchantType.getId(), true).getPromos().add(promo);

        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true").setPromoAmount("10");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("20.00")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String txnToken = iniJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchantType,initTxnDTO.getBody().getOrderId(),txnToken, "true", "true")
                .setTXN_AMOUNT("20")
                .build();
        CheckoutPage showPaymentPage= new CheckoutPage();
        showPaymentPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);

        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount("19.50")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validatePayableAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validatePaymentPromoCheckoutDataPresent()
                .validateTxnDate(new Date())
                .validateChargeAmount("1.32")
                .AssertAll();

    }

    @Owner(Constants.Owner.ABHISHEK_TEWARI)
    @Feature("PGP-31718")
    @Test(description = "Verify user Payment Offers applied on the merchant")
    public void verifyFetchUserPaymentOffers() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.MERCHANT_UPI_PPI_CC_DC_SUBS;
        Promo promocode = new Promo(true);
        Merchant merchant = new Merchant(merchantType.getId(), true);
        merchant.getPromos().add(promocode);
        FetchUserPaymentOffers offers = new FetchUserPaymentOffers(merchantType, user.mobNo());

        Response offersResponse = offers.execute();
        JsonPath jsonPath = offersResponse.jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("00000000");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentOffers[0].promocode")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentOffers[0].offer")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paytmOffersAvailable")).isEqualTo("true");
    }

}
