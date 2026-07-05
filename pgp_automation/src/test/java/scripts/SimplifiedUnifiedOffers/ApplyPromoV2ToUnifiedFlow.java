package scripts.SimplifiedUnifiedOffers;

import com.paytm.api.TxnStatus;
import com.paytm.api.theia.ApplyPromoV2Api;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.ApplyPromoV2DTO.*;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.PaymentOffersApplied;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedPaymentOffers;
import com.paytm.dto.NativeDTO.InitTxn.TxnAmount;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import static com.paytm.appconstants.Constants.Owner.KARMVIR;


public class ApplyPromoV2ToUnifiedFlow extends PGPBaseTest{
    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-49045")
    @Test(description="Test that applyOffer should be hit to Affordability when theia.enable.migration.to.unifiedOffers.applyPromo enabled on merchant with access token+" +
            "and we hit applyPromo api")
    public void testApplyPromoV2WithAccessToken(@Optional("true") boolean isNativePlus) throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        String custid=user.custId();
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        PaymentOption paymentOption= new PaymentOption("1000","CREDIT_CARD","","","4718650100010336","");
        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
                .setChannelId("WEB")
                .setToken(user.ssoToken())
                .setTokenType("ACCESS")
                .setCustId(custid)
                .setPaymentOptions(Arrays.asList(paymentOption))
                .setMid(mid.getId())
                .setTotalTransactionAmount("1000")
                .build();
        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid, "");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
    }
    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-49045")
    @Test(description="Test that applyOffer should be hit to Affordability when theia.enable.migration.to.unifiedOffers.applyPromo enabled on merchant with SSO token+" +
            "and we hit applyPromo api")
    public void testApplyPromoV2WithSSOToken(@Optional("true") boolean isNativePlus) throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        String custid=user.custId();
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        PaymentOption paymentOption= new PaymentOption("1000","NET_BANKING","","ICICI","","");
        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
                .setChannelId("WEB")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustId(custid)
                .setPaymentOptions(Arrays.asList(paymentOption))
                .setMid(mid.getId())
                .setTotalTransactionAmount("1000")
                .build();
        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
    }
    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-49045")
    @Test(description="Test that applyOffer should be hit to Affordability when theia.enable.migration.to.unifiedOffers.applyPromo enabled on merchant with Txn token+" +
            "and we hit applyPromo api")
    public void testApplyPromoV2WithTxnToken(@Optional("true") boolean isNativePlus) throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        String custid=user.custId();
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        SimplifiedPaymentOffers simplifiedPaymentOffers= new SimplifiedPaymentOffers("","true","true","");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue("1000")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentOption paymentOption= new PaymentOption("1000","EMI","","","4718650100010336","3");
        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
                .setChannelId("WEB")
                .setToken(txnToken)
                .setTokenType("TXN_TOKEN")
                .setCustId(custid)
                .setPaymentOptions(Arrays.asList(paymentOption))
                .setMid(mid.getId())
                .setOrderId(initTxnDTO.orderFromBody())
                .setTotalTransactionAmount("1000")
                .build();
        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,initTxnDTO.orderFromBody());
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
    }
    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-49045")
    @Test(description="Test that applyOffer should be hit to Affordability when theia.enable.migration.to.unifiedOffers.applyPromo enabled on merchant with SSO token+" +
            "and we hit applyPromo api for UPI Paymode")
    public void testApplyPromoV2WithSSOTokenForUPIPaymode(@Optional("true") boolean isNativePlus) throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        String custid=user.custId();
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        PaymentOption paymentOption= new PaymentOption("1000","UPI","arsh.test2@ptaxis","","","");
        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
                .setChannelId("WEB")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustId(custid)
                .setPaymentOptions(Arrays.asList(paymentOption))
                .setMid(mid.getId())
                .setTotalTransactionAmount("1000")
                .build();
        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
    }
    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-49045")
    @Test(description="Test that applyOffer should be hit to Affordability when theia.enable.migration.to.unifiedOffers.applyPromo enabled on merchant with SSO token+" +
            "and we hit applyPromo api for EMI_DC Paymode")
    public void testApplyPromoV2WithSSOTokenForEMI_DCPaymode(@Optional("true") boolean isNativePlus) throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        String custid=user.custId();
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        PaymentOption paymentOption= new PaymentOption("1000","EMI_DC","","","4799320857008816","3");
        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
                .setChannelId("WEB")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustId(custid)
                .setPaymentOptions(Arrays.asList(paymentOption))
                .setMid(mid.getId())
                .setTotalTransactionAmount("1000")
                .build();
        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
    }
    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-49045")
    @Test(description="Test that applyOffer should be hit to Affordability when theia.enable.migration.to.unifiedOffers.applyPromo enabled on merchant with SSO token+" +
            "and we hit applyPromo api for Item Based")
    public void testApplyPromoV2WithSSOTokenForItemBased(@Optional("true") boolean isNativePlus) throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        String custid=user.custId();
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        PaymentOption paymentOption= new PaymentOption("1000","EMI","","","4718650100010336","3");
        ArrayList categoryIds = new ArrayList();
        categoryIds.add("6224");
        ProductDetail productDetail= new ProductDetail("321067334","18084",categoryIds);
        Item item= new Item("1",1000,productDetail);
        CartDetails cartDetails= new CartDetails(Arrays.asList(item));
        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
                .setChannelId("WEB")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustId(custid)
                .setPaymentOptions(Arrays.asList(paymentOption))
                .setMid(mid.getId())
                .setTotalTransactionAmount("1000")
                .setCartDetails(cartDetails)
                .build();
        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.cartOfferDetail")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.cartDetails")).isNotNull();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
    }
    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-49045")
    @Test(description="Test that applyOffer should be hit to Affordability when theia.enable.migration.to.unifiedOffers.applyPromo enabled on merchant with SSO token+" +
            "and we hit applyPromo api for Item Based when promo code provided in request")
    public void testApplyPromoV2WithSSOTokenForItemBasedPromoCodeProvided(@Optional("true") boolean isNativePlus) throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        String custid=user.custId();
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        PaymentOption paymentOption= new PaymentOption("1000","EMI","","","4718650100010336","3");
        ArrayList categoryIds = new ArrayList();
        categoryIds.add("6224");
        ProductDetail productDetail= new ProductDetail("321067334","18084",categoryIds);
        Item item= new Item("1",1000,productDetail);
        CartDetails cartDetails= new CartDetails(Arrays.asList(item));
        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
                .setChannelId("WEB")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustId(custid)
                .setPaymentOptions(Arrays.asList(paymentOption))
                .setMid(mid.getId())
                .setPromocode("PROMO1234")
                .setTotalTransactionAmount("1000")
                .setCartDetails(cartDetails)
                .build();
        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.cartOfferDetail")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.cartDetails")).isNotNull();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        System.out.println(logs);
        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
        Assertions.assertThat(logs).contains("\"promocode\":[\"PROMO1234\"]");
    }
    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-49045")
    @Test(description="Test that applyOffer should be hit to Affordability when theia.enable.migration.to.unifiedOffers.applyPromo enabled on merchant with SSO token+" +
            "and we hit applyPromo api when promocode provided in request")
    public void testApplyPromoV2WithSSOTokenPromoCodeProvied(@Optional("true") boolean isNativePlus) throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        String custid=user.custId();
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        PaymentOption paymentOption= new PaymentOption("1000","NET_BANKING","","ICICI","","");
        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
                .setChannelId("WEB")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustId(custid)
                .setPromocode("PROMO1234")
                .setPaymentOptions(Arrays.asList(paymentOption))
                .setMid(mid.getId())
                .setTotalTransactionAmount("1000")
                .build();
        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
        Assertions.assertThat(logs).contains("\"promocode\":[\"PROMO1234\"]");
    }
    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-49045")
    @Test(description="Test that E2e success txn for EMI when theia.enable.migration.to.unifiedOffers.applyPromo enabled on merchant with SSO token+" +
            "and we hit applyPromo api")
    public void E2EEmiTxnWithapplyPromoApi(@Optional("true") boolean isNativePlus) throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        String custid=user.custId();
        String  transactionAmount="1000";
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        PaymentOption paymentOption= new PaymentOption(transactionAmount,"EMI","","","4718650100010336","3");
        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
                .setChannelId("WEB")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustId(custid)
                .setPaymentOptions(Arrays.asList(paymentOption))
                .setMid(mid.getId())
                .setTotalTransactionAmount("1000")
                .build();
        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
        HashMap<String, Object> paymentOffersObject= jsonPath.get("body.paymentOffer");
        PaymentOffersApplied paymentOffersApplied= new PaymentOffersApplied(paymentOffersObject);
        String discount = jsonPath.getString("body.paymentOffer.totalInstantDiscount");
        double payableAmount = 0;
        if (transactionAmount != null && discount != null) {
            try {
                payableAmount = Double.valueOf(transactionAmount) - Double.valueOf(discount);
            } catch (NumberFormatException e) {
                System.out.println("Error parsing integer values: " + e.getMessage());
            }
        } else {
            System.out.println("One or both values are null: transactionAmount = " + transactionAmount + ", discount = " + discount);
        }
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setPayableAmount(txnAmount)
                .setPaymentOffersApplied(paymentOffersApplied)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("HDFC|3")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        String ApplyLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(ApplyLogs).contains("/ads/v2/offer/apply");
    }
    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-49045")
    @Test(description="Test that E2e success txn for EMI_DC when theia.enable.migration.to.unifiedOffers.applyPromo enabled on merchant with SSO token+" +
            "and we hit applyPromo api")
    public void E2EEmiDCTxnWithapplyPromoApi(@Optional("true") boolean isNativePlus) throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        String custid=user.custId();
        String  transactionAmount="1000";
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        PaymentOption paymentOption= new PaymentOption(transactionAmount,"EMI_DC","","","4799320857008816","6");
        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
                .setChannelId("WEB")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustId(custid)
                .setPaymentOptions(Arrays.asList(paymentOption))
                .setMid(mid.getId())
                .setTotalTransactionAmount("1000")
                .build();
        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
        HashMap<String, Object> paymentOffersObject= jsonPath.get("body.paymentOffer");
        PaymentOffersApplied paymentOffersApplied= new PaymentOffersApplied(paymentOffersObject);
        String discount = jsonPath.getString("body.paymentOffer.totalInstantDiscount");
        double payableAmount = 0;
        if (transactionAmount != null && discount != null) {
            try {
                payableAmount = Double.valueOf(transactionAmount) - Double.valueOf(discount);
            } catch (NumberFormatException e) {
                System.out.println("Error parsing integer values: " + e.getMessage());
            }
        } else {
            System.out.println("One or both values are null: transactionAmount = " + transactionAmount + ", discount = " + discount);
        }
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setPayableAmount(txnAmount)
                .setPaymentOffersApplied(paymentOffersApplied)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("ICICI|6")
                .setCardInfo("|4799320857008816|618|12"+PaymentDTO.Tokenization_Year)
                .setEMI_TYPE("DEBIT_CARD")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        String ApplyLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(ApplyLogs).contains("/ads/v2/offer/apply");
    }
    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-49045")
    @Test(description="Test that E2e success txn for Credit card when theia.enable.migration.to.unifiedOffers.applyPromo enabled on merchant with SSO token+" +
            "and we hit applyPromo api")
    public void E2ECCTxnWithapplyPromoApi(@Optional("true") boolean isNativePlus) throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        String custid=user.custId();
        String  transactionAmount="1000";
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        PaymentOption paymentOption= new PaymentOption(transactionAmount,"CREDIT_CARD","","","4718650100010336","");
        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
                .setChannelId("WEB")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustId(custid)
                .setPaymentOptions(Arrays.asList(paymentOption))
                .setMid(mid.getId())
                .setTotalTransactionAmount("1000")
                .build();
        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
        HashMap<String, Object> paymentOffersObject= jsonPath.get("body.paymentOffer");
        PaymentOffersApplied paymentOffersApplied= new PaymentOffersApplied(paymentOffersObject);
        String discount = jsonPath.getString("body.paymentOffer.totalInstantDiscount");
        double payableAmount = 0;
        if (transactionAmount != null && discount != null) {
            try {
                payableAmount = Double.valueOf(transactionAmount) - Double.valueOf(discount);
            } catch (NumberFormatException e) {
                System.out.println("Error parsing integer values: " + e.getMessage());
            }
        } else {
            System.out.println("One or both values are null: transactionAmount = " + transactionAmount + ", discount = " + discount);
        }
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setPayableAmount(txnAmount)
                .setPaymentOffersApplied(paymentOffersApplied)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setCardInfo("|4718650100010336|618|12"+PaymentDTO.Tokenization_Year)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        String ApplyLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(ApplyLogs).contains("/ads/v2/offer/apply");
    }

    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-49045")
    @Test(description="Test that E2e success txn for Net banking when theia.enable.migration.to.unifiedOffers.applyPromo enabled on merchant with SSO token+" +
            "and we hit applyPromo api")
    public void E2ENBTxnWithapplyPromoApi(@Optional("true") boolean isNativePlus) throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        String custid=user.custId();
        String  transactionAmount="1000";
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        PaymentOption paymentOption= new PaymentOption(transactionAmount,"NET_BANKING","","ICICI","","");
        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
                .setChannelId("WEB")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustId(custid)
                .setPaymentOptions(Arrays.asList(paymentOption))
                .setMid(mid.getId())
                .setTotalTransactionAmount("1000")
                .build();
        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
        HashMap<String, Object> paymentOffersObject= jsonPath.get("body.paymentOffer");
        PaymentOffersApplied paymentOffersApplied= new PaymentOffersApplied(paymentOffersObject);
        String discount = jsonPath.getString("body.paymentOffer.totalInstantDiscount");
        double payableAmount = 0;
        if (transactionAmount != null && discount != null) {
            try {
                payableAmount = Double.valueOf(transactionAmount) - Double.valueOf(discount);
                System.out.println("payableAmount: "+payableAmount);
            } catch (NumberFormatException e) {
                System.out.println("Error parsing integer values: " + e.getMessage());
            }
        } else {
            System.out.println("One or both values are null: transactionAmount = " + transactionAmount + ", discount = " + discount);
        }
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setPayableAmount(txnAmount)
                .setPaymentOffersApplied(paymentOffersApplied)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        String ApplyLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(ApplyLogs).contains("/ads/v2/offer/apply");
    }

    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-49045")
    @Test(description="Test that E2e success txn for DEBIT card when theia.enable.migration.to.unifiedOffers.applyPromo enabled on merchant with SSO token+" +
            "and we hit applyPromo api")
    public void E2EDCTxnWithapplyPromoApi(@Optional("true") boolean isNativePlus) throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        String custid=user.custId();
        String  transactionAmount="1000";
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        PaymentOption paymentOption= new PaymentOption(transactionAmount,"DEBIT_CARD","","","4444333322221111","");
        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
                .setChannelId("WEB")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustId(custid)
                .setPaymentOptions(Arrays.asList(paymentOption))
                .setMid(mid.getId())
                .setTotalTransactionAmount("1000")
                .build();
        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
        HashMap<String, Object> paymentOffersObject= jsonPath.get("body.paymentOffer");
        PaymentOffersApplied paymentOffersApplied= new PaymentOffersApplied(paymentOffersObject);
        String discount = jsonPath.getString("body.paymentOffer.totalInstantDiscount");
        double payableAmount = 0;
        if (transactionAmount != null && discount != null) {
            try {
                payableAmount = Double.valueOf(transactionAmount) - Double.valueOf(discount);
            } catch (NumberFormatException e) {
                System.out.println("Error parsing integer values: " + e.getMessage());
            }
        } else {
            System.out.println("One or both values are null: transactionAmount = " + transactionAmount + ", discount = " + discount);
        }
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setPayableAmount(txnAmount)
                .setPaymentOffersApplied(paymentOffersApplied)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setCardInfo("|4444333322221111|618|12"+PaymentDTO.Tokenization_Year)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        String ApplyLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(ApplyLogs).contains("/ads/v2/offer/apply");
    }
    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-49045")
    @Test(description="Test that E2e success txn for UPI when theia.enable.migration.to.unifiedOffers.applyPromo enabled on merchant with SSO token+" +
            "and we hit applyPromo api")
    public void E2EUPITxnWithapplyPromoApi(@Optional("true") boolean isNativePlus) throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        String custid=user.custId();
        String  transactionAmount="1000";
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        PaymentOption paymentOption= new PaymentOption(transactionAmount,"UPI","arsh.test2@ptaxis","","","");
        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
                .setChannelId("WEB")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustId(custid)
                .setPaymentOptions(Arrays.asList(paymentOption))
                .setMid(mid.getId())
                .setTotalTransactionAmount("1000")
                .build();
        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
        HashMap<String, Object> paymentOffersObject= jsonPath.get("body.paymentOffer");
        PaymentOffersApplied paymentOffersApplied= new PaymentOffersApplied(paymentOffersObject);
        String discount = jsonPath.getString("body.paymentOffer.totalInstantDiscount");
        double payableAmount = 0;
        if (transactionAmount != null && discount != null) {
            try {
                payableAmount = Double.valueOf(transactionAmount) - Double.valueOf(discount);
            } catch (NumberFormatException e) {
                System.out.println("Error parsing integer values: " + e.getMessage());
            }
        } else {
            System.out.println("One or both values are null: transactionAmount = " + transactionAmount + ", discount = " + discount);
        }
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setPayableAmount(txnAmount)
                .setPaymentOffersApplied(paymentOffersApplied)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount("arsh.test2@ptaxis")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        String ApplyLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(ApplyLogs).contains("/ads/v2/offer/apply");
    }
    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-49045")
    @Test(description="Test that E2e success txn for EMI when theia.enable.migration.to.unifiedOffers.applyPromo enabled on merchant with SSO token+" +
            "and we hit applyPromo api for Item Based")
    public void E2EEmiTxnWithapplyPromoApiItemBased(@Optional("true") boolean isNativePlus) throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        String custid=user.custId();
        String transactionAmount="1000";
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        PaymentOption paymentOption= new PaymentOption("1000","EMI","","","4718650100010336","3");
        ArrayList categoryIds = new ArrayList();
        categoryIds.add("6224");
        ProductDetail productDetail= new ProductDetail("321067334","18084",categoryIds);
        Item item= new Item("1",1000,productDetail);
        CartDetails cartDetails= new CartDetails(Arrays.asList(item));
        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
                .setChannelId("WEB")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustId(custid)
                .setPaymentOptions(Arrays.asList(paymentOption))
                .setMid(mid.getId())
                .setTotalTransactionAmount("1000")
                .setCartDetails(cartDetails)
                .build();
        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
        HashMap<String, Object> paymentOffersObject= jsonPath.get("body.paymentOffer");
        PaymentOffersApplied paymentOffersApplied= new PaymentOffersApplied(paymentOffersObject);
        String discount = jsonPath.getString("body.paymentOffer.totalInstantDiscount");
        double payableAmount = 0;
        if (transactionAmount != null && discount != null) {
            try {
                payableAmount = Double.valueOf(transactionAmount) - Double.valueOf(discount);
            } catch (NumberFormatException e) {
                System.out.println("Error parsing integer values: " + e.getMessage());
            }
        } else {
            System.out.println("One or both values are null: transactionAmount = " + transactionAmount + ", discount = " + discount);
        }
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setPayableAmount(txnAmount)
                .setPaymentOffersApplied(paymentOffersApplied)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("HDFC|3")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        String ApplyLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(ApplyLogs).contains("/ads/v2/offer/apply");
    }

    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-49045")
    @Test(description="Test that E2e success txn for Net Banking when theia.enable.migration.to.unifiedOffers.applyPromo enabled on merchant with SSO token+" +
            "and we hit applyPromo api for Item Based")
    public void E2ENBTxnWithapplyPromoApiItemBased(@Optional("true") boolean isNativePlus) throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        String custid=user.custId();
        String transactionAmount="1000";
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        PaymentOption paymentOption= new PaymentOption("1000","NET_BANKING","","ICICI","","");
        ArrayList categoryIds = new ArrayList();
        categoryIds.add("6224");
        ProductDetail productDetail= new ProductDetail("321067334","18084",categoryIds);
        Item item= new Item("1",1000,productDetail);
        CartDetails cartDetails= new CartDetails(Arrays.asList(item));
        ApplyPromoV2DTO applyPromoV2DTO = new ApplyPromoV2DTO.Builder()
                .setChannelId("WEB")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustId(custid)
                .setPaymentOptions(Arrays.asList(paymentOption))
                .setMid(mid.getId())
                .setTotalTransactionAmount("1000")
                .setCartDetails(cartDetails)
                .build();
        JsonPath jsonPath= ApplyPromoV2Api.applyPromoV2(applyPromoV2DTO, mid,"");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.paymentOffer.offerBreakup[0].promocodeApplied")).isNotNull();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(logs).contains("/ads/v2/offer/apply");
        HashMap<String, Object> paymentOffersObject= jsonPath.get("body.paymentOffer");
        PaymentOffersApplied paymentOffersApplied= new PaymentOffersApplied(paymentOffersObject);
        String discount = jsonPath.getString("body.paymentOffer.totalInstantDiscount");
        double payableAmount = 0;
        if (transactionAmount != null && discount != null) {
            try {
                payableAmount = Double.valueOf(transactionAmount) - Double.valueOf(discount);
            } catch (NumberFormatException e) {
                System.out.println("Error parsing integer values: " + e.getMessage());
            }
        } else {
            System.out.println("One or both values are null: transactionAmount = " + transactionAmount + ", discount = " + discount);
        }
        TxnAmount txnAmount = new TxnAmount(String.valueOf(payableAmount));
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .setPayableAmount(txnAmount)
                .setPaymentOffersApplied(paymentOffersApplied)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        String ApplyLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(ApplyLogs).contains("/ads/v2/offer/apply");
    }
    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-49045")
    @Test(description = "Test the E2e Txn for NB paymode when Minimal_Promo_merchant is enabled on merchant and theia.enable.migration.to.unifiedOffers " +
            "ff4j flag is enabled on merchant")
    public void TestE2ENBTxnForMinimalPromoMerchant(@Optional ("true") boolean isNativePlus) throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        String transactionAmount="1000";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_MINIMAL_PROMO;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_REG_MINIMAL_PROMO, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        String ApplyLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(ApplyLogs).contains("/ads/v2/offer/apply");
    }
    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-49045")
    @Test(description = "Test the E2e Txn for EMI paymode when Minimal_Promo_merchant is enabled on merchant and theia.enable.migration.to.unifiedOffers " +
            "ff4j flag is enabled on merchant")
    public void TestE2EEMITxnForMinimalPromoMerchant(@Optional ("true") boolean isNativePlus) throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        String transactionAmount="1000";
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("HDFC|3")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        String ApplyLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(ApplyLogs).contains("/ads/v2/offer/apply");
    }
    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-49045")
    @Test(description = "Test the E2e Txn for EMIDC paymode when Minimal_Promo_merchant is enabled on merchant and theia.enable.migration.to.unifiedOffers " +
            "ff4j flag is enabled on merchant")
    public void TestE2EEMIDCTxnForMinimalPromoMerchant(@Optional ("true") boolean isNativePlus) throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        String transactionAmount="1000";
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId("ICICI|6")
                .setEMI_TYPE("DEBIT_CARD")
                .setCardInfo("|4799320857008816|618|12"+PaymentDTO.Tokenization_Year)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        String ApplyLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(ApplyLogs).contains("/ads/v2/offer/apply");
    }
    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-49045")
    @Test(description = "Test the E2e Txn for CC paymode when Minimal_Promo_merchant is enabled on merchant and theia.enable.migration.to.unifiedOffers " +
            "ff4j flag is enabled on merchant")
    public void TestE2ECCTxnForMinimalPromoMerchant(@Optional ("true") boolean isNativePlus) throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        String transactionAmount="1000";
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_MINIMAL_PROMO;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_REG_MINIMAL_PROMO, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setCardInfo("|4718650100010336|618|12"+PaymentDTO.Tokenization_Year)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        String ApplyLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(ApplyLogs).contains("/ads/v2/offer/apply");
    }

    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-49045")
    @Test(description = "Test the E2e Txn for UPI paymode when Minimal_Promo_merchant is enabled on merchant and theia.enable.migration.to.unifiedOffers " +
            "ff4j flag is enabled on merchant")
    public void TestE2EUPITxnForMinimalPromoMerchant(@Optional ("true") boolean isNativePlus) throws Exception {
        User user=userManager.getForRead(Label.BASIC);
        String transactionAmount="1000";
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(transactionAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount("arsh.test2@paytm")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        String ApplyLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_PLATFORM_DISCOVERY");
        Assertions.assertThat(ApplyLogs).contains("/ads/v2/offer/apply");
    }
}
