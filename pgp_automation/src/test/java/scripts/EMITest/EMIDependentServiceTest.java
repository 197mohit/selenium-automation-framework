package scripts.EMITest;

import com.paytm.api.nativeAPI.FetchPaymentOptionV5;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.applyItemOffers;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.promoContext;
import com.paytm.dto.NativeDTO.InitTxn.DisablePaymentMode;
import com.paytm.dto.NativeDTO.InitTxn.EnablePaymentMode;
import com.paytm.dto.NativeDTO.InitTxn.UltimateBeneficiaryDetails;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedUnifiedOffers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.PaymentDTO;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import com.paytm.pages.*;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.util.Collections;

import static com.paytm.appconstants.Constants.Owner.MEHUL_GUPTA;
import static com.paytm.appconstants.Constants.Owner.RONIKA;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import io.restassured.response.Response;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.TxnAmount;
import com.paytm.api.refund.SyncRefund;
import com.paytm.apphelpers.*;
import com.paytm.LocalConfig;
import com.paytm.api.TxnStatus;
import com.paytm.pages.NativePlusHoldpayPage;
import com.paytm.utils.merchant.util.PGPUtil;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.NativeDTO.OfferApply.*;
import static com.paytm.dto.PaymentDTO.*;
import static com.paytm.appconstants.Constants.Source;
import org.assertj.core.api.SoftAssertions;

public class EMIDependentServiceTest extends PGPBaseTest{


    private final NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();
    private static final String JSON_POST_URL = "/checkoutpage/new_nplus_page.jsp?env=" + LocalConfig.ENV_NAME;
    private static final String expiry = "122027";

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-57665")
    @Test(description = "Verify FPO Response when enablePaymentMode is sent in request with mode UPI and subTypes as UPI_LITE")
    public void validateDisabledSubPaymentOptionsInFPOWhenSubTypeIsSentAsUPI_LITE() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.EMI_REG_FPO_UPI_CC_LITE;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String requestId = CommonHelpers.generateOrderId();
        String GoodsId = CommonHelpers.getRandomWithSize(10)+"1";
        com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.Good goodId = new com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.Good();
        goodId.setMerchantGoodsId(GoodsId);
        EnablePaymentMode enableUPIPaymentMode = new EnablePaymentMode(null, "UPI", new String[]{},new String[]{"UPI_LITE"});
        UltimateBeneficiaryDetails ultimateBeneficiaryDetailsFPO = new UltimateBeneficiaryDetails("Electricity Bill");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setVersion("v5")
                .setChannelId("WAP")
                .setRequestId(requestId)
                .setTokenType("SSO")
                .setMid(merchant.getId())
                .setToken(user.ssoToken())
                .setGoods(Collections.singletonList(goodId))
                .setEnablePaymentMode(new EnablePaymentMode[]{enableUPIPaymentMode})
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode("BANK_MANDATE"),new DisablePaymentMode().setMode("PAY_AT_COUNTER"),new DisablePaymentMode().setMode("ADVANCE_ACCOUNT"),new DisablePaymentMode().setMode("COD"),new DisablePaymentMode().setMode("ESCROW")})
                .setCardHashRequired("true")
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setOrderAmount("100")
                .setApplyItemOffers(new applyItemOffers() {{
                    setPromoContext(new promoContext() {{
                        setCart("123456789", "18084", "6224", "10000");
                    }});
                }})
                .setTxnAmount(new com.paytm.dto.processTransactionV1.TxnAmount().setValue("100").setCurrency("INR"))
                .setUltimateBeneficiaryDetails(ultimateBeneficiaryDetailsFPO)
                .build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(merchant.getId(),fetchPaymentOptionsDTO);
        fetchPaymentOption.getRequestSpecBuilder().addQueryParam("mid",merchant.getId());
        fetchPaymentOption.getRequestSpecBuilder().addHeader("X-PGP-Unique-ID",requestId);
        JsonPath response = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(response.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(response.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(response.getString("body.merchantPayOption.paymentModes.paymentMode").replace("[","").replace("]","")).isEqualTo("UPI");
        Assertions.assertThat(response.getString("body.merchantPayOption.paymentModes[0].disabledSubPaymentOptions")).contains("UPI_CREDITLINE").contains("UPI_PPIWALLET").contains("UPI_3P_INTENT").contains("UPI_SAVINGS").contains("UPI_CC").contains("CURRENT").contains("NRO").contains("SOD").contains("NRE").contains("DEFAULT").contains("UOD").doesNotContain("UPI_LITE");
        Assertions.assertThat(response.getString("body.groupedMerchantPayOption.other_options[0].disabledSubPaymentOptions")).contains("UPI_CREDITLINE").contains("UPI_PPIWALLET").contains("UPI_3P_INTENT").contains("UPI_SAVINGS").contains("UPI_CC").contains("CURRENT").contains("NRO").contains("SOD").contains("NRE").contains("DEFAULT").contains("UOD").doesNotContain("UPI_LITE");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-57665")
    @Test(description = "Verify FPO Response when enablePaymentMode is sent in request with mode UPI and subTypes as UPI_CC")
    public void validateDisabledSubPaymentOptionsInFPOWhenSubTypeIsSentAsUPI_CC() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.EMI_REG_FPO_UPI_CC_LITE;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String requestId = CommonHelpers.generateOrderId();
        String GoodsId = CommonHelpers.getRandomWithSize(10)+"1";
        com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.Good goodId = new com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.Good();
        goodId.setMerchantGoodsId(GoodsId);
        EnablePaymentMode enableUPIPaymentMode = new EnablePaymentMode(null, "UPI", new String[]{},new String[]{"UPI_CC"});
        UltimateBeneficiaryDetails ultimateBeneficiaryDetailsFPO = new UltimateBeneficiaryDetails("Electricity Bill");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setVersion("v5")
                .setChannelId("WAP")
                .setRequestId(requestId)
                .setTokenType("SSO")
                .setMid(merchant.getId())
                .setToken(user.ssoToken())
                .setGoods(Collections.singletonList(goodId))
                .setEnablePaymentMode(new EnablePaymentMode[]{enableUPIPaymentMode})
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode("BANK_MANDATE"),new DisablePaymentMode().setMode("PAY_AT_COUNTER"),new DisablePaymentMode().setMode("ADVANCE_ACCOUNT"),new DisablePaymentMode().setMode("COD"),new DisablePaymentMode().setMode("ESCROW")})
                .setCardHashRequired("true")
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setOrderAmount("100")
                .setApplyItemOffers(new applyItemOffers() {{
                    setPromoContext(new promoContext() {{
                        setCart("123456789", "18084", "6224", "10000");
                    }});
                }})
                .setTxnAmount(new com.paytm.dto.processTransactionV1.TxnAmount().setValue("100").setCurrency("INR"))
                .setUltimateBeneficiaryDetails(ultimateBeneficiaryDetailsFPO)
                .build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(merchant.getId(),fetchPaymentOptionsDTO);
        fetchPaymentOption.getRequestSpecBuilder().addQueryParam("mid",merchant.getId());
        fetchPaymentOption.getRequestSpecBuilder().addHeader("X-PGP-Unique-ID",requestId);
        JsonPath response = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(response.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(response.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(response.getString("body.merchantPayOption.paymentModes.paymentMode").replace("[","").replace("]","")).isEqualTo("UPI");
        Assertions.assertThat(response.getString("body.merchantPayOption.paymentModes[0].disabledSubPaymentOptions")).contains("UPI_CREDITLINE").contains("UPI_PPIWALLET").contains("UPI_3P_INTENT").contains("UPI_SAVINGS").contains("UPI_LITE").contains("CURRENT").contains("NRO").contains("SOD").contains("NRE").contains("DEFAULT").contains("UOD").doesNotContain("UPI_CC");
        Assertions.assertThat(response.getString("body.groupedMerchantPayOption.other_options[0].disabledSubPaymentOptions")).contains("UPI_CREDITLINE").contains("UPI_PPIWALLET").contains("UPI_3P_INTENT").contains("UPI_SAVINGS").contains("UPI_LITE").contains("CURRENT").contains("NRO").contains("SOD").contains("NRE").contains("DEFAULT").contains("UOD").doesNotContain("UPI_CC");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-57665")
    @Test(description = "Verify FPO Response when enablePaymentMode is sent in request with mode UPI and subTypes as UPI_CREDITLINE")
    public void validateDisabledSubPaymentOptionsInFPOWhenSubTypeIsSentAsUPI_CREDITLINE() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.EMI_REG_FPO_UPI_CC_LITE;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String requestId = CommonHelpers.generateOrderId();
        String GoodsId = CommonHelpers.getRandomWithSize(10)+"1";
        com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.Good goodId = new com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.Good();
        goodId.setMerchantGoodsId(GoodsId);
        EnablePaymentMode enableUPIPaymentMode = new EnablePaymentMode(null, "UPI", new String[]{},new String[]{"UPI_CREDITLINE"});
        UltimateBeneficiaryDetails ultimateBeneficiaryDetailsFPO = new UltimateBeneficiaryDetails("Electricity Bill");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setVersion("v5")
                .setChannelId("WAP")
                .setRequestId(requestId)
                .setTokenType("SSO")
                .setMid(merchant.getId())
                .setToken(user.ssoToken())
                .setGoods(Collections.singletonList(goodId))
                .setEnablePaymentMode(new EnablePaymentMode[]{enableUPIPaymentMode})
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode("BANK_MANDATE"),new DisablePaymentMode().setMode("PAY_AT_COUNTER"),new DisablePaymentMode().setMode("ADVANCE_ACCOUNT"),new DisablePaymentMode().setMode("COD"),new DisablePaymentMode().setMode("ESCROW")})
                .setCardHashRequired("true")
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setOrderAmount("100")
                .setApplyItemOffers(new applyItemOffers() {{
                    setPromoContext(new promoContext() {{
                        setCart("123456789", "18084", "6224", "10000");
                    }});
                }})
                .setTxnAmount(new com.paytm.dto.processTransactionV1.TxnAmount().setValue("100").setCurrency("INR"))
                .setUltimateBeneficiaryDetails(ultimateBeneficiaryDetailsFPO)
                .build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(merchant.getId(),fetchPaymentOptionsDTO);
        fetchPaymentOption.getRequestSpecBuilder().addQueryParam("mid",merchant.getId());
        fetchPaymentOption.getRequestSpecBuilder().addHeader("X-PGP-Unique-ID",requestId);
        JsonPath response = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(response.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(response.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(response.getString("body.merchantPayOption.paymentModes.paymentMode").replace("[","").replace("]","")).isEqualTo("UPI");
        Assertions.assertThat(response.getString("body.merchantPayOption.paymentModes[0].disabledSubPaymentOptions")).contains("UPI_LITE").contains("UPI_PPIWALLET").contains("UPI_3P_INTENT").contains("UPI_SAVINGS").contains("UPI_CC").contains("CURRENT").contains("NRO").contains("SOD").contains("NRE").contains("DEFAULT").contains("UOD").doesNotContain("UPI_CREDITLINE");
        Assertions.assertThat(response.getString("body.groupedMerchantPayOption.other_options[0].disabledSubPaymentOptions")).contains("UPI_LITE").contains("UPI_PPIWALLET").contains("UPI_3P_INTENT").contains("UPI_SAVINGS").contains("UPI_CC").contains("CURRENT").contains("NRO").contains("SOD").contains("NRE").contains("DEFAULT").contains("UOD").doesNotContain("UPI_CREDITLINE");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-57665")
    @Test(description = "Verify FPO Response when enablePaymentMode is sent in request with mode UPI and subTypes as UPI_LITE and UPI_CC")
    public void validateDisabledSubPaymentOptionsInFPOWhenSubTypeIsSentAsUPI_LITEAndUPI_CC() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.EMI_REG_FPO_UPI_CC_LITE;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String requestId = CommonHelpers.generateOrderId();
        String GoodsId = CommonHelpers.getRandomWithSize(10)+"1";
        com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.Good goodId = new com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.Good();
        goodId.setMerchantGoodsId(GoodsId);
        EnablePaymentMode enableUPIPaymentMode = new EnablePaymentMode(null, "UPI", new String[]{},new String[]{"UPI_LITE","UPI_CC"});
        UltimateBeneficiaryDetails ultimateBeneficiaryDetailsFPO = new UltimateBeneficiaryDetails("Electricity Bill");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setVersion("v5")
                .setChannelId("WAP")
                .setRequestId(requestId)
                .setTokenType("SSO")
                .setMid(merchant.getId())
                .setToken(user.ssoToken())
                .setGoods(Collections.singletonList(goodId))
                .setEnablePaymentMode(new EnablePaymentMode[]{enableUPIPaymentMode})
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode("BANK_MANDATE"),new DisablePaymentMode().setMode("PAY_AT_COUNTER"),new DisablePaymentMode().setMode("ADVANCE_ACCOUNT"),new DisablePaymentMode().setMode("COD"),new DisablePaymentMode().setMode("ESCROW")})
                .setCardHashRequired("true")
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setOrderAmount("100")
                .setApplyItemOffers(new applyItemOffers() {{
                    setPromoContext(new promoContext() {{
                        setCart("123456789", "18084", "6224", "10000");
                    }});
                }})
                .setTxnAmount(new com.paytm.dto.processTransactionV1.TxnAmount().setValue("100").setCurrency("INR"))
                .setUltimateBeneficiaryDetails(ultimateBeneficiaryDetailsFPO)
                .build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(merchant.getId(),fetchPaymentOptionsDTO);
        fetchPaymentOption.getRequestSpecBuilder().addQueryParam("mid",merchant.getId());
        fetchPaymentOption.getRequestSpecBuilder().addHeader("X-PGP-Unique-ID",requestId);
        JsonPath response = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(response.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(response.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(response.getString("body.merchantPayOption.paymentModes.paymentMode").replace("[","").replace("]","")).isEqualTo("UPI");
        Assertions.assertThat(response.getString("body.merchantPayOption.paymentModes[0].disabledSubPaymentOptions")).contains("UPI_CREDITLINE").contains("UPI_PPIWALLET").contains("UPI_3P_INTENT").contains("UPI_SAVINGS").contains("CURRENT").contains("NRO").contains("SOD").contains("NRE").contains("DEFAULT").contains("UOD").doesNotContain("UPI_CC").doesNotContain("UPI_LITE");
        Assertions.assertThat(response.getString("body.groupedMerchantPayOption.other_options[0].disabledSubPaymentOptions")).contains("UPI_CREDITLINE").contains("UPI_PPIWALLET").contains("UPI_3P_INTENT").contains("UPI_SAVINGS").contains("CURRENT").contains("NRO").contains("SOD").contains("NRE").contains("DEFAULT").contains("UOD").doesNotContain("UPI_CC").doesNotContain("UPI_LITE");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-57665")
    @Test(description = "Verify FPO Response when enablePaymentMode is sent in request with mode UPI and subTypes as empty List")
    public void validateDisabledSubPaymentOptionsInFPOWhenSubTypeIsSentAsEmptyList() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.EMI_REG_FPO_UPI_CC_LITE;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String requestId = CommonHelpers.generateOrderId();
        String GoodsId = CommonHelpers.getRandomWithSize(10)+"1";
        com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.Good goodId = new com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.Good();
        goodId.setMerchantGoodsId(GoodsId);
        EnablePaymentMode enableUPIPaymentMode = new EnablePaymentMode(null, "UPI", new String[]{},new String[]{});
        UltimateBeneficiaryDetails ultimateBeneficiaryDetailsFPO = new UltimateBeneficiaryDetails("Electricity Bill");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setVersion("v5")
                .setChannelId("WAP")
                .setRequestId(requestId)
                .setTokenType("SSO")
                .setMid(merchant.getId())
                .setToken(user.ssoToken())
                .setGoods(Collections.singletonList(goodId))
                .setEnablePaymentMode(new EnablePaymentMode[]{enableUPIPaymentMode})
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode("BANK_MANDATE"),new DisablePaymentMode().setMode("PAY_AT_COUNTER"),new DisablePaymentMode().setMode("ADVANCE_ACCOUNT"),new DisablePaymentMode().setMode("COD"),new DisablePaymentMode().setMode("ESCROW")})
                .setCardHashRequired("true")
                .setApplyPaymentOffers("true")
                .setFetchAllPaymentOffers("true")
                .setOrderAmount("100")
                .setApplyItemOffers(new applyItemOffers() {{
                    setPromoContext(new promoContext() {{
                        setCart("123456789", "18084", "6224", "10000");
                    }});
                }})
                .setTxnAmount(new com.paytm.dto.processTransactionV1.TxnAmount().setValue("100").setCurrency("INR"))
                .setUltimateBeneficiaryDetails(ultimateBeneficiaryDetailsFPO)
                .build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(merchant.getId(),fetchPaymentOptionsDTO);
        fetchPaymentOption.getRequestSpecBuilder().addQueryParam("mid",merchant.getId());
        fetchPaymentOption.getRequestSpecBuilder().addHeader("X-PGP-Unique-ID",requestId);
        JsonPath response = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(response.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(response.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(response.getString("body.merchantPayOption.paymentModes.paymentMode").replace("[","").replace("]","")).isEqualTo("UPI");
        Assertions.assertThat(response.getString("body.merchantPayOption.paymentModes[0].disabledSubPaymentOptions")).isNull();
        Assertions.assertThat(response.getString("body.groupedMerchantPayOption.other_options[0].disabledSubPaymentOptions")).isNull();
    }


    @Owner(RONIKA)
    @Feature("PGP-58214")
    @Test(description = "Verify error code for cases : 1. Refund amount is invalid, 2. Item Info is missing, 3. Invalid Order Id")
    public void verifyErrorCodeForRefundAmountIsInvalid_ItemInfoMissing_InvalidOrderId() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        //String custId = "MOCKMULTI0001";
        String orderId = CommonHelpers.generateOrderId();
        List<Item> items = new ArrayList<>();
        Item item1 = new Item("Item001_" + orderId, "18084", "6226", "123047", 1100.0);
        Item item2 = new Item("Item002_" + orderId, "18084", "6226", "123047", 1100.0);
        Item item3 = new Item("Item003_" + orderId, "18084", "6226", "123047", 1100.0);
        items.add(item1);
        items.add(item2);
        items.add(item3);
        OfferApplyDTO offerApply = new OfferApplyDTO.Builder(ssoToken, mid.getId())
                .setCustId(custId)
                .setItems(items)
                .setPaymentDetails(new PaymentDetails(2200.0,
                        Collections.singletonList(new PaymentOption(true, true, "HDFC", 0.0, "HDFC", "EMI", 0.0, "2200.0", null, null, null, null, AlternateID_VISA_CARD, Collections.singletonList(new Tenure(3, "MONTH"))))))
                .build();
        Response response = NativeHelpers.Validate_OfferApply(offerApply);

        JsonPath jsonPath = response.jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);


        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + AlternateID_VISA_CARD + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
        // Invalid Refund Amount
        String refundItems1 = "[{\"itemId\":\"Item001_"+orderId+"\",\"productId\":\"123047\",\"itemRefundAmount\":\"3000\"}]";
        String refId1 = CommonHelpers.generateOrderId();
        String bodyforChecksum1 = SyncRefund.bodyWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", "3000").replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId1).replace("{refund_items}",refundItems1);
        String checksum1= PGPUtil.getChecksum(mid.getKey(),bodyforChecksum1);
        String body1 = SyncRefund.requestWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", "3000").replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{SIGNATURE}", checksum1).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId1).replace("{refund_items}",refundItems1);
        SyncRefund syncRefund1 =  new SyncRefund(body1);
        JsonPath asyncRefundResp1 = syncRefund1.execute().jsonPath();
        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultCode")).isEqualTo("674");
        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultMsg")).isEqualTo("Refund amount is invalid or greater than transaction amount");
        Assertions.assertThat(asyncRefundResp1.getString("body.refundAmount")).isEqualTo("3000");
        
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.REFUND_FACADE_LOGS,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM", "RESPONSE");
        Assertions.assertThat(logs).contains("{\"resultInfo\":{\"code\":\"613\",\"status\":\"F\",\"message\":\"Business Validations Failed\",\"description\":\"Invalid Amount for Refund request\"}}");
        
        Thread.sleep(10000);
        
        // Invalid Order Id
        Double payableAmountItem2 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.payableAmount");
        String refundItems2 = "[{\"itemId\":\"Item001_"+orderId+"\",\"productId\":\"123047\",\"itemRefundAmount\":\"" + payableAmountItem2 +"\"}]";
        String refId2 = CommonHelpers.generateOrderId();
        String bodyforChecksum2 = SyncRefund.bodyWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem2)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()+"123").replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId2).replace("{refund_items}",refundItems2);
        String checksum2= PGPUtil.getChecksum(mid.getKey(),bodyforChecksum2);
        String body2 = SyncRefund.requestWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem2)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()+"123").replace("{SIGNATURE}", checksum2).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId2).replace("{refund_items}",refundItems2);
        SyncRefund syncRefund2 =  new SyncRefund(body2);
        JsonPath asyncRefundResp2 = syncRefund2.execute().jsonPath();
        Assertions.assertThat(asyncRefundResp2.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(asyncRefundResp2.getString("body.resultInfo.resultCode")).isEqualTo("627");
        Assertions.assertThat(asyncRefundResp2.getString("body.resultInfo.resultMsg")).isEqualTo("Order Details Mismatch");
        Assertions.assertThat(asyncRefundResp2.getString("body.refundAmount")).isEqualTo(Double.toString(payableAmountItem2));
        

        Thread.sleep(10000);

        // Item Info is missing
        Double payableAmountItem3 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.payableAmount");
        String refundItems3 = "[]";
        String refId3 = CommonHelpers.generateOrderId();
        String bodyforChecksum3 = SyncRefund.bodyWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem3)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId3).replace("{refund_items}",refundItems3);
        String checksum3= PGPUtil.getChecksum(mid.getKey(),bodyforChecksum3);
        String body3 = SyncRefund.requestWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem3)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{SIGNATURE}", checksum3).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId3).replace("{refund_items}",refundItems3);
        SyncRefund syncRefund3 =  new SyncRefund(body3);
        JsonPath asyncRefundResp3 = syncRefund3.execute().jsonPath();
        Assertions.assertThat(asyncRefundResp3.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(asyncRefundResp3.getString("body.resultInfo.resultCode")).isEqualTo("725");
        Assertions.assertThat(asyncRefundResp3.getString("body.resultInfo.resultMsg")).isEqualTo("Item information was passed in the parent request. Please check and pass them");
        Assertions.assertThat(asyncRefundResp3.getString("body.refundAmount")).isEqualTo(Double.toString(payableAmountItem3));
        
        String logs2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.REFUND_FACADE_LOGS,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM", "RESPONSE");
        Assertions.assertThat(logs2).contains("{\"resultInfo\":{\"code\":\"615\",\"status\":\"F\",\"message\":\"Business Validations Failed\",\"description\":\"Item information missing in request\"}}");
        

    }
    @Owner(RONIKA)
    @Feature("PGP-58214")
    @Test(description = "Verify error code when item is already refunded")
    public void verifyErrorCodeWhenItemIsAlreadyRefunded() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        String orderId = CommonHelpers.generateOrderId();
        List<Item> items = new ArrayList<>();
        Item item1 = new Item("Item001_" + orderId, "18084", "6226", "123047", 1100.0);
        Item item2 = new Item("Item002_" + orderId, "18084", "6226", "123047", 1100.0);
        items.add(item1);
        items.add(item2);
        OfferApplyDTO offerApply = new OfferApplyDTO.Builder(ssoToken, mid.getId())
                .setCustId(custId)
                .setItems(items)
                .setPaymentDetails(new PaymentDetails(2200.0,
                        Collections.singletonList(new PaymentOption(true, true, "HDFC", 0.0, "HDFC", "EMI", 0.0, "2200.0", null, null, null, null, AlternateID_VISA_CARD, Collections.singletonList(new Tenure(3, "MONTH"))))))
                .build();
        Response response = NativeHelpers.Validate_OfferApply(offerApply);

        JsonPath jsonPath = response.jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + AlternateID_VISA_CARD + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");

        Double payableAmountItem1 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.payableAmount");
        String refundItems1 = "[{\"itemId\":\"Item001_"+orderId+"\",\"productId\":\"123047\",\"itemRefundAmount\":\"" + payableAmountItem1 +"\"}]";
        String refId1 = CommonHelpers.generateOrderId();
        String bodyforChecksum1 = SyncRefund.bodyWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId1).replace("{refund_items}",refundItems1);
        String checksum1= PGPUtil.getChecksum(mid.getKey(),bodyforChecksum1);
        String body1 = SyncRefund.requestWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{SIGNATURE}", checksum1).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId1).replace("{refund_items}",refundItems1);
        SyncRefund syncRefund1 =  new SyncRefund(body1);
        JsonPath asyncRefundResp1 = syncRefund1.execute().jsonPath();
        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultStatus")).isEqualTo("PENDING");
        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultCode")).isEqualTo("601");
        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultMsg")).isEqualTo("Refund request was raised for this transaction. But it is pending state");
        Assertions.assertThat(asyncRefundResp1.getString("body.refundAmount")).isEqualTo(Double.toString(payableAmountItem1));

        String refId2 = CommonHelpers.generateOrderId();
        String bodyforChecksum2 = SyncRefund.bodyWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId2).replace("{refund_items}",refundItems1);
        String checksum2= PGPUtil.getChecksum(mid.getKey(),bodyforChecksum2);
        String body2 = SyncRefund.requestWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{SIGNATURE}", checksum2).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId2).replace("{refund_items}",refundItems1);
        SyncRefund syncRefund2 =  new SyncRefund(body2);
        JsonPath asyncRefundResp2 = syncRefund2.execute().jsonPath();
        Assertions.assertThat(asyncRefundResp2.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(asyncRefundResp2.getString("body.resultInfo.resultCode")).isEqualTo("1401");
        Assertions.assertThat(asyncRefundResp2.getString("body.resultInfo.resultMsg")).isEqualTo("Already refunded - " + "Item001_" + orderId);
        Assertions.assertThat(asyncRefundResp2.getString("body.refundAmount")).isEqualTo(Double.toString(payableAmountItem1));

    }

    @Owner(RONIKA)
    @Feature("PGP-58214")
    @Test(description = "Verify error code when mid is invalid")
    public void verifyErrorCodeWhenMidIsInvalid() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String refId1 = CommonHelpers.generateOrderId();
        String orderId = CommonHelpers.generateOrderId();
        String txnId = "TXNID" + CommonHelpers.getRandomWithSize(30);
        String invalidMid = "qa12ps693827320623042";
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        
        
        // Create the body for checksum calculation
        String bodyforChecksum1 = new SyncRefund().body.replace("{MID}", invalidMid).replace("{TRANSACTION_AMOUNT}", "2000").replace("{ORDER_ID}", orderId).replace("{TRANSACTION_ID}", txnId).replace("{ref_id}", refId1);
        
        String checksum1 = PGPUtil.getChecksum(mid.getKey(), bodyforChecksum1);
        
        // Create the full request body
        String body1 = new SyncRefund().request.replace("{MID}", invalidMid).replace("{TRANSACTION_AMOUNT}", "2000").replace("{ORDER_ID}", orderId).replace("{SIGNATURE}", checksum1).replace("{TRANSACTION_ID}", txnId).replace("{ref_id}", refId1);
        
        // Pass to constructor
        SyncRefund syncRefund1 = new SyncRefund(body1);
        JsonPath asyncRefundResp1 = syncRefund1.execute().jsonPath();
        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultCode")).isEqualTo("335");
        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultMsg")).isEqualTo("Invalid merchant Id.");

    }
    @Owner(RONIKA)
    @Feature("PGP-58214")
    @Test(description = "Verify error code when order is not successful")
    public void verifyErrorCodeWhenOrderIsNotSuccessful() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        //String custId = "MOCKMULTI0001";
        String orderId = CommonHelpers.generateOrderId();
        List<Item> items = new ArrayList<>();
        Item item1 = new Item("Item001_" + orderId, "18084", "6226", "123047", 339.29);
        items.add(item1);
        OfferApplyDTO offerApply = new OfferApplyDTO.Builder(ssoToken, mid.getId())
                .setCustId(custId)
                .setItems(items)
                .setPaymentDetails(new PaymentDetails(339.29,
                        Collections.singletonList(new PaymentOption(true, true, "HDFC", 0.0, "HDFC", "EMI", 0.0, "2200.0", null, null, null, null, AlternateID_VISA_CARD, Collections.singletonList(new Tenure(3, "MONTH"))))))
                .build();
        Response response = NativeHelpers.Validate_OfferApply(offerApply);

        JsonPath jsonPath = response.jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);


        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + AlternateID_VISA_CARD + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();

        Double payableAmountItem1 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.payableAmount");
        String refundItems1 = "[{\"itemId\":\"Item001_"+orderId+"\",\"productId\":\"123047\",\"itemRefundAmount\":\"" + payableAmountItem1 +"\"}]";
        String refId1 = CommonHelpers.generateOrderId();
        String bodyforChecksum1 = SyncRefund.bodyWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId1).replace("{refund_items}",refundItems1);
        String checksum1= PGPUtil.getChecksum(mid.getKey(),bodyforChecksum1);
        String body1 = SyncRefund.requestWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{SIGNATURE}", checksum1).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId1).replace("{refund_items}",refundItems1);
        SyncRefund syncRefund1 =  new SyncRefund(body1);
        JsonPath asyncRefundResp1 = syncRefund1.execute().jsonPath();
        
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.REFUND_FACADE_LOGS,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM", "RESPONSE");
        Assertions.assertThat(logs).contains("{\"resultInfo\":{\"code\":\"612\",\"status\":\"F\",\"message\":\"Business Validations Failed\",\"description\":\"Requested Order is not successful\"}}");
    }
    @Owner(RONIKA)
    @Feature("PGP-58214")
    @Test(description = "Verify error code when no successful checkout is found")
    public void verifyErrorCodeWhenNoSuccessfulCheckoutIsFound() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        //String custId = "MOCKMULTI0001";
        String orderId = CommonHelpers.generateOrderId();
        List<Item> items = new ArrayList<>();
        Item item1 = new Item("Item001_" + orderId, "18084", "6226", "123047", 1989.39);
        items.add(item1);
        OfferApplyDTO offerApply = new OfferApplyDTO.Builder(ssoToken, mid.getId())
                .setCustId(custId)
                .setItems(items)
                .setPaymentDetails(new PaymentDetails(1989.39,
                        Collections.singletonList(new PaymentOption(true, true, "HDFC", 0.0, "HDFC", "EMI", 0.0, "2200.0", null, null, null, null, AlternateID_VISA_CARD, Collections.singletonList(new Tenure(3, "MONTH"))))))
                .build();
        Response response = NativeHelpers.Validate_OfferApply(offerApply);

        JsonPath jsonPath = response.jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);


        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + AlternateID_VISA_CARD + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");

        Double payableAmountItem1 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.payableAmount");
        String refundItems1 = "[{\"itemId\":\"Item001_"+orderId+"\",\"productId\":\"123047\",\"itemRefundAmount\":\"" + payableAmountItem1 +"\"}]";
        String refId1 = CommonHelpers.generateOrderId();
        String bodyforChecksum1 = SyncRefund.bodyWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId1).replace("{refund_items}",refundItems1);
        String checksum1= PGPUtil.getChecksum(mid.getKey(),bodyforChecksum1);
        String body1 = SyncRefund.requestWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{SIGNATURE}", checksum1).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId1).replace("{refund_items}",refundItems1);
        SyncRefund syncRefund1 =  new SyncRefund(body1);
        JsonPath asyncRefundResp1 = syncRefund1.execute().jsonPath();
        
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.REFUND_FACADE_LOGS,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM", "RESPONSE");
        Assertions.assertThat(logs).contains("{\"resultInfo\":{\"code\":\"614\",\"status\":\"F\",\"message\":\"Business Validations Failed\",\"description\":\"No successful checkout found\"}}");
    }

    @Owner(RONIKA)
    @Feature("PGP-58214")
    @Test(description = "Verify error code when child order is not successful")
    public void verifyErrorCodeWhenChildOrderIsNotSuccessful() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        //String custId = "MOCKMULTI0001";
        String orderId = CommonHelpers.generateOrderId();
        List<Item> items = new ArrayList<>();
        Item item1 = new Item("Item001_" + orderId, "18084", "6226", "123047", 929.89);
        Item item2 = new Item("Item002_" + orderId, "18084", "6226", "123047", 1989.00);

        items.add(item1);
        items.add(item2);

        OfferApplyDTO offerApply = new OfferApplyDTO.Builder(ssoToken, mid.getId())
                .setCustId(custId)
                .setItems(items)
                .setPaymentDetails(new PaymentDetails(2918.89,
                        Collections.singletonList(new PaymentOption(true, true, "HDFC", 0.0, "HDFC", "EMI", 0.0, "2918.89", null, null, null, null, AlternateID_VISA_CARD, Collections.singletonList(new Tenure(3, "MONTH"))))))
                .build();
        Response response = NativeHelpers.Validate_OfferApply(offerApply);

        JsonPath jsonPath = response.jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);


        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + AlternateID_VISA_CARD + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");

        Double payableAmountItem1 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.payableAmount");
        String refundItems1 = "[{\"itemId\":\"Item001_"+orderId+"\",\"productId\":\"123047\",\"itemRefundAmount\":\"" + payableAmountItem1 +"\"}]";
        String refId1 = CommonHelpers.generateOrderId();
        String bodyforChecksum1 = SyncRefund.bodyWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId1).replace("{refund_items}",refundItems1);
        String checksum1= PGPUtil.getChecksum(mid.getKey(),bodyforChecksum1);
        String body1 = SyncRefund.requestWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{SIGNATURE}", checksum1).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId1).replace("{refund_items}",refundItems1);
        SyncRefund syncRefund1 =  new SyncRefund(body1);
        JsonPath asyncRefundResp1 = syncRefund1.execute().jsonPath();
        
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.REFUND_FACADE_LOGS,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM", "RESPONSE");
        Assertions.assertThat(logs).contains("{\"itemInfo\":[{\"id\":\"Item001_8566aebffee934f27dd0623cdeb5f7aa\",\"productId\":\"123047\",\"itemRefundAmount\":929.89,\"error\":{\"code\":\"1404\",\"message\":\"Child order of this item has failed\"}}],\"resultInfo\":{\"code\":\"602\",\"status\":\"F\",\"message\":\"Business Validations Failed\",\"description\":\"Some of the Requested Items are not eligible for refund\"}}");
    }
    @Owner(RONIKA)
    @Feature("PGP-60033")
    @Feature("PGP-59292")
    @Test(description = "when deviceImei and source is sent in request then it should populate in offerApply and Checkout response")
    public void verifyDeviceImeiAndSourceInOfferApplyAndCheckoutResponse(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String orderId = CommonHelpers.generateOrderId();
        String txnAmount = "1700";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_RESTRICT_OFFER;
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(null, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", txnAmount, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setDeviceIMEI("1234567890");
        simplifiedUnifiedOffers.setSource(Source.PG_LINK.getValue());
        String jwtToken = PGPHelpers.createTokenForDeviceImeiAndSource(mid.getId(),orderId,txnAmount);

        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(mid,orderId, null)
                .setTxnValue(txnAmount)
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .buildWithJwt(Constants.PAYTM_LOVES_EMI_THEIA_CLIENT_ID,jwtToken);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = PGPHelpers.openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(AlternateID_VISA_CARD);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ads/v2/offer/apply","REQUEST");
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(logs).contains("source\u003d[PG_LINK]");
        String logs1 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ats/v2/order/checkout","REQUEST");
        softAssertions.assertThat(logs1).contains("X-Channel=[PG_LINK]");
        softAssertions.assertThat(logs1).contains("\"deviceIMEI\":\"1234567890\"");
        softAssertions.assertAll();
    }
    @Owner(RONIKA)
    @Feature("PGP-60383")
    @Test(description = "Verify modelID is not getting overriden by productID when modelID is sent in request for offline merchant")
    public void verifyModelIDisNotGettingOverridenByProductIDWhenModelIDIsSentInRequest(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String orderId = CommonHelpers.generateOrderId();
        String txnAmount = "2700";
        String id = "item" + CommonHelpers.getRandomWithSize(3);
        Constants.MerchantType mid = Constants.MerchantType.EMI_EDC_LINK_MID_NEW;
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(null, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", txnAmount, null, null);
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item = new SimplifiedUnifiedOffers.Items(
            id, "12345678", "1707", "66781", "56002", "offline", 2700.0
        );
        items.add(item);
        
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(promoDetails, subventionDetails, items);
        simplifiedUnifiedOffers.setDeviceIMEI("1234567890");
        simplifiedUnifiedOffers.setSource(Source.PG_LINK.getValue());
        String jwtToken = PGPHelpers.createTokenForDeviceImeiAndSource(mid.getId(),orderId,txnAmount);

        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(mid,orderId, null)
                .setTxnValue(txnAmount)
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .buildWithJwt(Constants.PAYTM_LOVES_EMI_THEIA_CLIENT_ID,jwtToken);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = PGPHelpers.openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(AlternateID_VISA_CARD);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ads/v2/offer/apply","REQUEST");
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(logs).contains("\"model\":\"56002\"");
        String logs2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ads/v2/offer/apply","RESPONSE");
        softAssertions.assertThat(logs2).contains("\"offerId\":\"2478081\"");
        softAssertions.assertThat(logs2).contains("\"offerId\":\"2437219\"");
        softAssertions.assertThat(logs2).contains("\"offerId\":\"2513358\"");
        softAssertions.assertAll();

    }
    @Owner(RONIKA)
    @Feature("PGP-60383")
    @Test(description = "Verify modelID is getting overriden by productID when modelID is not sent in request for offline merchant")
    public void verifyModelIDisGettingOverridenByProductIDWhenModelIDIsNotSentInRequest(@Optional("checkoutjs") String JsType) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.EMIDCELIGIBLE);
        String orderId = CommonHelpers.generateOrderId();
        String id = "item" + CommonHelpers.getRandomWithSize(3);
        String txnAmount = "2700";
        Constants.MerchantType mid = Constants.MerchantType.EMI_EDC_LINK_MID_NEW;
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(null, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", txnAmount, null, null);
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item = new SimplifiedUnifiedOffers.Items(
            id, "56002", "1707", "66781", null, "offline", 2700.0
        );
        items.add(item);
        
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(promoDetails, subventionDetails, items);
        simplifiedUnifiedOffers.setDeviceIMEI("1234567890");
        simplifiedUnifiedOffers.setSource(Source.PG_LINK.getValue());
        String jwtToken = PGPHelpers.createTokenForDeviceImeiAndSource(mid.getId(),orderId,txnAmount);

        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(mid,orderId, null)
                .setTxnValue(txnAmount)
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .buildWithJwt(Constants.PAYTM_LOVES_EMI_THEIA_CLIENT_ID,jwtToken);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String theme = PGPHelpers.openJs(JsType, initTxnDTO, txnToken,mid,"EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(AlternateID_VISA_CARD);
        cashierPage.payByEMI(cashierPage, paymentDTO,false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();

        String logs2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ads/v2/offer/apply","RESPONSE");
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(logs2).contains("\"offerId\":\"2478081\"");
        softAssertions.assertThat(logs2).contains("\"offerId\":\"2437219\"");
        softAssertions.assertThat(logs2).contains("\"offerId\":\"2513358\"");
        softAssertions.assertAll();
    }
        
}
