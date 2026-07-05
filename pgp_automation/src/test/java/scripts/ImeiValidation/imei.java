package scripts.ImeiValidation;

import com.paytm.api.ImeiValidation.ImeiValidation;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedPaymentOffers;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedSubvention;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.pages.*;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;


import java.util.ArrayList;
import java.util.List;

import static com.paytm.appconstants.Constants.MerchantType.*;
import static com.paytm.appconstants.Constants.Owner.POOJA;
import static com.paytm.appconstants.Constants.Owner.ROHIT_SHARMA;

public class imei extends PGPBaseTest {
    private final CheckoutJsCheckoutPage checkoutJsPage =new CheckoutJsCheckoutPage();
    private static CheckoutPage checkoutPage = new CheckoutPage();
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46453")
    @Parameters({"theme"})
    @Test(description = "Verify that Imei validation api should give error if brandId is not passed")
    public void brandIdErrorCase(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(EmiInfo_COP, theme).setTXN_AMOUNT("100").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("HDFC Bank").setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("EMI")
                .validateMid(orderDTO.getMID());
        ImeiValidation imeiValidation = new ImeiValidation(EmiInfo_COP,orderDTO.getORDER_ID(),"BLOCK","123456","Sales","1234","","667811","12345678988");
        JsonPath jsonPath = imeiValidation.execute().jsonPath();
        String resultCode = jsonPath.getString("resultInfo.resultCode");
        Assertions.assertThat(resultCode).isEqualTo("MISSING_MANDATORY_ELEMENT");
        String resultMsg = jsonPath.getString("resultInfo.resultMsg");
        Assertions.assertThat(resultMsg).isEqualTo("brandId can't be blank");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46453")
    @Parameters({"theme"})
    @Test(description = "Verify that Imei validation api should give error if action type apart from BLOCK and UNBLOCK is passed")
    public void invalidActionErrorCase(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(EmiInfo_COP, theme).setTXN_AMOUNT("100").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("HDFC Bank").setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("EMI")
                .validateMid(orderDTO.getMID());
        ImeiValidation imeiValidation = new ImeiValidation(EmiInfo_COP,orderDTO.getORDER_ID(),"BLOCKED","123456","Sales","1234","1707","667811","12345678988");
        JsonPath jsonPath = imeiValidation.execute().jsonPath();
        String resultCode = jsonPath.getString("resultInfo.resultCode");
        Assertions.assertThat(resultCode).isEqualTo("INVALID_ACTION");
        String resultMsg = jsonPath.getString("resultInfo.resultMsg");
        Assertions.assertThat(resultMsg).isEqualTo("Invalid action sent in request");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46453")
    @Parameters({"theme"})
    @Test(description = "Verify that Imei validation api should give error if skuCode is not passed")
    public void skuCodeErrorCase(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(EmiInfo_COP, theme).setTXN_AMOUNT("100").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("HDFC Bank").setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("EMI")
                .validateMid(orderDTO.getMID());
        ImeiValidation imeiValidation = new ImeiValidation(EmiInfo_COP,orderDTO.getORDER_ID(),"BLOCK","","Sales","1234","1707","667811","12345678988");
        JsonPath jsonPath = imeiValidation.execute().jsonPath();
        String resultCode = jsonPath.getString("resultInfo.resultCode");
        Assertions.assertThat(resultCode).isEqualTo("MISSING_MANDATORY_ELEMENT");
        String resultMsg = jsonPath.getString("resultInfo.resultMsg");
        Assertions.assertThat(resultMsg).isEqualTo("skuCode can't be blank");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46453")
    @Parameters({"theme"})
    @Test(description = "Verify that Imei validation api should give error if imei is not passed")
    public void imeiErrorCase(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(EmiInfo_COP, theme).setTXN_AMOUNT("100").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("HDFC Bank").setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("EMI")
                .validateMid(orderDTO.getMID());
        ImeiValidation imeiValidation = new ImeiValidation(EmiInfo_COP,orderDTO.getORDER_ID(),"BLOCK","1234","Sales","1234","1707","667811","");
        JsonPath jsonPath = imeiValidation.execute().jsonPath();
        String resultCode = jsonPath.getString("resultInfo.resultCode");
        Assertions.assertThat(resultCode).isEqualTo("MISSING_MANDATORY_ELEMENT");
        String resultMsg = jsonPath.getString("resultInfo.resultMsg");
        Assertions.assertThat(resultMsg).isEqualTo("imei can't be blank");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46453")
    @Parameters({"theme"})
    @Test(description = "Verify that Imei validation api should give error if dealerCode is not passed for brandId configured in validation.service.brandId.dealerCode in theia project-validation.properties")
    public void dealerCodeErrorCase(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(EmiInfo_COP, theme).setTXN_AMOUNT("100").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("HDFC Bank").setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("EMI")
                .validateMid(orderDTO.getMID());
        ImeiValidation imeiValidation = new ImeiValidation(EmiInfo_COP,orderDTO.getORDER_ID(),"BLOCK","1234","","DL05#","18260","667811","123456");
        JsonPath jsonPath = imeiValidation.execute().jsonPath();
        String resultCode = jsonPath.getString("resultInfo.resultCode");
        Assertions.assertThat(resultCode).isEqualTo("MISSING_MANDATORY_ELEMENT");
        String resultMsg = jsonPath.getString("resultInfo.resultMsg");
        Assertions.assertThat(resultMsg).isEqualTo("dealerCode can't be blank");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46453")
    @Parameters({"theme"})
    @Test(description = "Verify that Imei validation api should give error if stateCode is not passed for brandId configured in validation.service.brandId.stateCode in theia project-validation.properties")
    public void stateCodeErrorCase(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(EmiInfo_COP, theme).setTXN_AMOUNT("100").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("HDFC Bank").setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("EMI")
                .validateMid(orderDTO.getMID());
        ImeiValidation imeiValidation = new ImeiValidation(EmiInfo_COP,orderDTO.getORDER_ID(),"BLOCK","1234","#rohit29#","","18084","667811","123456");
        JsonPath jsonPath = imeiValidation.execute().jsonPath();
        String resultCode = jsonPath.getString("resultInfo.resultCode");
        Assertions.assertThat(resultCode).isEqualTo("MISSING_MANDATORY_ELEMENT");
        String resultMsg = jsonPath.getString("resultInfo.resultMsg");
        Assertions.assertThat(resultMsg).isEqualTo("stateCode can't be blank");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46453")
    @Parameters({"theme"})
    @Test(description = "Verify that Imei validation api should give error if both stateCode and dealerCode is not passed for brandId configured in validation.service.brandId.stateCode and validation.service.brandId.dealerCodein in theia project-validation.properties")
    public void stateCodeAndDealreCodeMandatoryCase(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(EmiInfo_COP, theme).setTXN_AMOUNT("100").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("HDFC Bank").setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("EMI")
                .validateMid(orderDTO.getMID());
        ImeiValidation imeiValidation = new ImeiValidation(EmiInfo_COP,orderDTO.getORDER_ID(),"BLOCK","1234","","","1707","667811","123456");
        JsonPath jsonPath = imeiValidation.execute().jsonPath();
        String resultCode = jsonPath.getString("resultInfo.resultCode");
        Assertions.assertThat(resultCode).isEqualTo("MISSING_MANDATORY_ELEMENT");
        String resultMsg = jsonPath.getString("resultInfo.resultMsg");
        Assertions.assertThat(resultMsg).isEqualTo("stateCode can't be blank");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46453")
    @Parameters({"theme"})
    @Test(description = "Verify that Imei validation api should not give error if both stateCode and dealerCode is not passed for brandId not configured in validation.service.brandId.stateCode and validation.service.brandId.dealerCodein in theia project-validation.properties")
    public void stateCodeAndDealreCodeNonMandatoryCase(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(EmiInfo_COP, theme).setTXN_AMOUNT("100").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("HDFC Bank").setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("EMI")
                .validateMid(orderDTO.getMID());
        ImeiValidation imeiValidation = new ImeiValidation(EmiInfo_COP,orderDTO.getORDER_ID(),"BLOCK","1234","","","17071","667811","123456");
        JsonPath jsonPath = imeiValidation.execute().jsonPath();
        String resultMsg = jsonPath.getString("resultInfo.resultMsg");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"VALIDATION_MODEL");
        Assertions.assertThat(logs).contains("\"brandId\":\"17071\"");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46453")
    @Parameters({"theme"})
    @Test(description = "Verify that Imei validation api should not give error if both stateCode and dealerCode is  passed for brandId not configured in validation.service.brandId.stateCode and validation.service.brandId.dealerCodein in theia project-validation.properties")
    public void stateCodeAndDealreCodeNonMandatoryPassedCase(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(EmiInfo_COP, theme).setTXN_AMOUNT("100").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("HDFC Bank").setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("EMI")
                .validateMid(orderDTO.getMID());
        ImeiValidation imeiValidation = new ImeiValidation(EmiInfo_COP,orderDTO.getORDER_ID(),"BLOCK","1234","rohit","2929","17071","667811","123456");
        JsonPath jsonPath = imeiValidation.execute().jsonPath();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"VALIDATION_MODEL");
        Assertions.assertThat(logs).contains("\"dealerCode\":\"rohit\"");

    }

    @Owner(POOJA)
    @Feature("PGP-49239")
    @Parameters({"theme"})
    @Test(description = "verify imei pre-validate request is sent to affordability validation service for Action = BLOCK and FF4J flag theia.routeToAVS  = true on MID")
    public void AVSpreValidateRequestForrouteToAVSIsTrue_PaymentOffers (@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EmiInfo_COP;
        List<String> categoryIds = new ArrayList<>();
        categoryIds.add("521");
        SimplifiedPaymentOffers.ProductDetail productDetail = new SimplifiedPaymentOffers.ProductDetail("123400232343411113e331", "6002665", "112866", categoryIds);
        SimplifiedPaymentOffers.Items items = new SimplifiedPaymentOffers.Items("113", "PROMO00187", "2000", productDetail);
        List<SimplifiedPaymentOffers.Items> itemsList = new ArrayList<>();
        itemsList.add(items);
        SimplifiedPaymentOffers.CartDetails cartDetails = new SimplifiedPaymentOffers.CartDetails(itemsList);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("PROMO00187", "true", "true", cartDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1100")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard("4761360075860428");
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("EMI")
                .assertAll();

        ImeiValidation imeiValidation = new ImeiValidation(EmiInfo_COP,orderid,"BLOCK","1234","pooja","2929","18084","667811","123456");
        JsonPath jsonPath = imeiValidation.execute().jsonPath();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderid);
        Assertions.assertThat(logs).contains("\"COMPONENT\": \"AFFORDABILITY_VALIDATION_SERVICE\"");

    }

    @Owner(POOJA)
    @Feature("PGP-49239")
    @Parameters({"theme"})
    @Test(description = "verify imei pre-validate request is sent to affordability validation service for Action = BLOCK and FF4J flag theia.routeToAVS  = true on MID")
    public void AVSpreValidateRequestForrouteToAVSIsTrue_Subvention (@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EmiInfo_COP;
        List<String> categoryList = new ArrayList<>();
        categoryList.add("6224");
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("120","321067334","18084",categoryList,"1","1100","51",true, false, null);
        List<SimplifiedSubvention.Item> itemss = new ArrayList<>();
        itemss.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, itemss );
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1100")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard("4761360075860428");
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("EMI")
                .assertAll();

        ImeiValidation imeiValidation = new ImeiValidation(EmiInfo_COP,orderid,"BLOCK","1234","pooja","2929","18084","667811","123456");
        JsonPath jsonPath = imeiValidation.execute().jsonPath();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderid);
        Assertions.assertThat(logs).contains("\"COMPONENT\": \"AFFORDABILITY_VALIDATION_SERVICE\"");

    }
    @Owner(POOJA)
    @Feature("PGP-49239")
    @Parameters({"theme"})
    @Test(description = "verify for BLOCK action Theia is sending avs/v2/pre-validate AVS request")
    public void preValidateRequestToAVSforBLOCK_PaymentOffers (@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EmiInfo_COP;
        List<String> categoryIds = new ArrayList<>();
        categoryIds.add("521");
        SimplifiedPaymentOffers.ProductDetail productDetail = new SimplifiedPaymentOffers.ProductDetail("123400232343411113e331", "6002665", "112866", categoryIds);
        SimplifiedPaymentOffers.Items items = new SimplifiedPaymentOffers.Items("113", "PROMO00187", "2000", productDetail);
        List<SimplifiedPaymentOffers.Items> itemsList = new ArrayList<>();
        itemsList.add(items);
        SimplifiedPaymentOffers.CartDetails cartDetails = new SimplifiedPaymentOffers.CartDetails(itemsList);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("PROMO00187", "true", "true", cartDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1100")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard("4761360075860428");
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("EMI")
                .assertAll();

        ImeiValidation imeiValidation = new ImeiValidation(EmiInfo_COP,orderid,"BLOCK","1234","pooja","2929","18084","667811","123456");
        JsonPath jsonPath = imeiValidation.execute().jsonPath();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "AFFORDABILITY_VALIDATION_SERVICE");
        Assertions.assertThat(logs).contains("avs/v2/post-transaction/block");

    }

    @Owner(POOJA)
    @Feature("PGP-49239")
    @Parameters({"theme"})
    @Test(description = "verify for BLOCK action Theia is sending avs/v2/pre-validate AVS request")
    public void preValidateRequestToAVSforBLOCK_Subvention (@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EmiInfo_COP;
        List<String> categoryList = new ArrayList<>();
        categoryList.add("6224");
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("120","321067334","18084",categoryList,"1","1100","51",true, false, null);
        List<SimplifiedSubvention.Item> itemss = new ArrayList<>();
        itemss.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, itemss );
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1100")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard("4761360075860428");
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("EMI")
                .assertAll();

        ImeiValidation imeiValidation = new ImeiValidation(EmiInfo_COP,orderid,"BLOCK","1234","pooja","2929","18084","667811","123456");
        JsonPath jsonPath = imeiValidation.execute().jsonPath();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "AFFORDABILITY_VALIDATION_SERVICE");
        Assertions.assertThat(logs).contains("avs/v2/post-transaction/block");

    }
    @Owner(POOJA)
    @Feature("PGP-49239")
    @Parameters({"theme"})
    @Test(description = "verify in imei pre-validate request productID value same as skuCode is sent for Action = BLOCK ")
    public void preValidateRequestToAVSforBLOCKProductIDAsSkuCode_PaymentOffers (@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EmiInfo_COP;
        List<String> categoryIds = new ArrayList<>();
        categoryIds.add("521");
        SimplifiedPaymentOffers.ProductDetail productDetail = new SimplifiedPaymentOffers.ProductDetail("123400232343411113e331", "6002665", "112866", categoryIds);
        SimplifiedPaymentOffers.Items items = new SimplifiedPaymentOffers.Items("113", "PROMO00187", "2000", productDetail);
        List<SimplifiedPaymentOffers.Items> itemsList = new ArrayList<>();
        itemsList.add(items);
        SimplifiedPaymentOffers.CartDetails cartDetails = new SimplifiedPaymentOffers.CartDetails(itemsList);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("PROMO00187", "true", "true", cartDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1100")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard("4761360075860428");
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("EMI")
                .assertAll();

        ImeiValidation imeiValidation = new ImeiValidation(EmiInfo_COP,orderid,"BLOCK","1234","pooja","2929","18084","667811","123456");
        JsonPath jsonPath = imeiValidation.execute().jsonPath();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderid, "AFFORDABILITY_VALIDATION_SERVICE");
        Assertions.assertThat(logs).contains("skuCode\":\"1234");
        Assertions.assertThat(logs).contains("productId\":\"1234");

    }

    @Owner(POOJA)
    @Feature("PGP-49239")
    @Parameters({"theme"})
    @Test(description = "verify in imei pre-validate request productID value same as skuCode is sent for Action = BLOCK ")
    public void preValidateRequestToAVSforBLOCKProductIDAsSkuCode_Subvention (@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EmiInfo_COP;
        List<String> categoryList = new ArrayList<>();
        categoryList.add("6224");
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("120","321067334","18084",categoryList,"1","1100","51",true, false, null);
        List<SimplifiedSubvention.Item> itemss = new ArrayList<>();
        itemss.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, itemss );
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1100")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard("4761360075860428");
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("EMI")
                .assertAll();

        ImeiValidation imeiValidation = new ImeiValidation(EmiInfo_COP,orderid,"BLOCK","1234","pooja","2929","18084","667811","123456");
        JsonPath jsonPath = imeiValidation.execute().jsonPath();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderid, "AFFORDABILITY_VALIDATION_SERVICE");
        Assertions.assertThat(logs).contains("skuCode\":\"1234");
        Assertions.assertThat(logs).contains("productId\":\"1234");

    }

    @Owner(POOJA)
    @Feature("PGP-49239")
    @Parameters({"theme"})
    @Test(description = "verify imei pre-validate success response from AVS ")
    public void preValidateSuccessFromAVS_PaymentOffers(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EmiInfo_COP;
        List<String> categoryIds = new ArrayList<>();
        categoryIds.add("521");
        SimplifiedPaymentOffers.ProductDetail productDetail = new SimplifiedPaymentOffers.ProductDetail("123400232343411113e331", "6002665", "112866", categoryIds);
        SimplifiedPaymentOffers.Items items = new SimplifiedPaymentOffers.Items("113", "PROMO00187", "2000", productDetail);
        List<SimplifiedPaymentOffers.Items> itemsList = new ArrayList<>();
        itemsList.add(items);
        SimplifiedPaymentOffers.CartDetails cartDetails = new SimplifiedPaymentOffers.CartDetails(itemsList);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("PROMO00187", "true", "true", cartDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1100")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard("4761360075860428");
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("EMI")
                .assertAll();

        ImeiValidation imeiValidation = new ImeiValidation(EmiInfo_COP,orderid,"BLOCK","1234","pooja","2929","18084","667811","123456");
        JsonPath jsonPath = imeiValidation.execute().jsonPath();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_VALIDATION_SERVICE");
        Assertions.assertThat(logs).contains("\"TYPE\" : \"RESPONSE\"");
        Assertions.assertThat(logs).contains("\"resultStatus\":\"SUCCESS\"");

    }

    @Owner(POOJA)
    @Feature("PGP-49239")
    @Parameters({"theme"})
    @Test(description = "verify imei pre-validate success response from AVS ")
    public void preValidateSuccessFromAVS_Subvention(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EmiInfo_COP;
        List<String> categoryList = new ArrayList<>();
        categoryList.add("6224");
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("120","321067334","18084",categoryList,"1","1100","51",true, false, null);
        List<SimplifiedSubvention.Item> itemss = new ArrayList<>();
        itemss.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, itemss );
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1100")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard("4761360075860428");
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("EMI")
                .assertAll();

        ImeiValidation imeiValidation = new ImeiValidation(EmiInfo_COP,orderid,"BLOCK","1234","pooja","2929","18084","667811","123456");
        JsonPath jsonPath = imeiValidation.execute().jsonPath();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_VALIDATION_SERVICE");
        Assertions.assertThat(logs).contains("\"TYPE\" : \"RESPONSE\"");
        Assertions.assertThat(logs).contains("\"resultStatus\":\"SUCCESS\"");

    }

    @Owner(POOJA)
    @Feature("PGP-49239")
    @Parameters({"theme"})
    @Test(description = "verify imei post-validate request is sent to affordability validation service for Action = UNBLOCK and FF4J flag theia.routeToAVS  = true on MID")
    public void postValidateRequestToAVSForActionUNBLOCK_PaymentOffers(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EmiInfo_COP;
        List<String> categoryIds = new ArrayList<>();
        categoryIds.add("521");
        SimplifiedPaymentOffers.ProductDetail productDetail = new SimplifiedPaymentOffers.ProductDetail("123400232343411113e331", "6002665", "112866", categoryIds);
        SimplifiedPaymentOffers.Items items = new SimplifiedPaymentOffers.Items("113", "PROMO00187", "2000", productDetail);
        List<SimplifiedPaymentOffers.Items> itemsList = new ArrayList<>();
        itemsList.add(items);
        SimplifiedPaymentOffers.CartDetails cartDetails = new SimplifiedPaymentOffers.CartDetails(itemsList);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("PROMO00187", "true", "true", cartDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1100")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard("4761360075860428");
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("EMI")
                .assertAll();

        ImeiValidation imeiValidation = new ImeiValidation(EmiInfo_COP,orderid,"BLOCK","1234","pooja","2929","18084","667811","123456");
        JsonPath jsonPath = imeiValidation.execute().jsonPath();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_VALIDATION_SERVICE");
        Assertions.assertThat(logs).contains("\"TYPE\" : \"RESPONSE\"");
        Assertions.assertThat(logs).contains("\"resultStatus\":\"SUCCESS\"");

        ImeiValidation imeiValidationUnblock = new ImeiValidation(EmiInfo_COP,orderid,"UNBLOCK","1234","pooja","2929","18084","667811","123456");
        JsonPath jsonPath2 = imeiValidationUnblock.execute().jsonPath();

        String logsAVS = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderid,"AFFORDABILITY_VALIDATION_SERVICE" );
        Assertions.assertThat(logsAVS).contains("avs/v2/post-validate");
        Assertions.assertThat(logsAVS).contains("\"TYPE\" : \"REQUEST\"");
    }

    @Owner(POOJA)
    @Feature("PGP-49239")
    @Parameters({"theme"})
    @Test(description = "verify imei post-validate request is sent to affordability validation service for Action = UNBLOCK and FF4J flag theia.routeToAVS  = true on MID")
    public void postValidateRequestToAVSForActionUNBLOCK_Subvention(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EmiInfo_COP;
        List<String> categoryList = new ArrayList<>();
        categoryList.add("6224");
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("120","321067334","18084",categoryList,"1","1100","51",true, false, null);
        List<SimplifiedSubvention.Item> itemss = new ArrayList<>();
        itemss.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, itemss );
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1100")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard("4761360075860428");
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("EMI")
                .assertAll();

        ImeiValidation imeiValidation = new ImeiValidation(EmiInfo_COP,orderid,"BLOCK","1234","pooja","2929","18084","667811","123456");
        JsonPath jsonPath = imeiValidation.execute().jsonPath();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_VALIDATION_SERVICE");
        Assertions.assertThat(logs).contains("\"TYPE\" : \"RESPONSE\"");
        Assertions.assertThat(logs).contains("\"resultStatus\":\"SUCCESS\"");

        ImeiValidation imeiValidationUnblock = new ImeiValidation(EmiInfo_COP,orderid,"UNBLOCK","1234","pooja","2929","18084","667811","123456");
        JsonPath jsonPath2 = imeiValidationUnblock.execute().jsonPath();

        String logsAVS = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderid,"AFFORDABILITY_VALIDATION_SERVICE" );
        Assertions.assertThat(logsAVS).contains("avs/v2/post-validate");
        Assertions.assertThat(logsAVS).contains("\"TYPE\" : \"REQUEST\"");


    }

    @Owner(POOJA)
    @Feature("PGP-49239")
    @Parameters({"theme"})
    @Test(description = "verify in imei post-validate request productID value same as skuCode is sent for Action = UNBLOCK ")
    public void postValidateRequestToAVSforUNBLOCKProductIDAsSkuCode_PaymentOffers (@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EmiInfo_COP;
        List<String> categoryIds = new ArrayList<>();
        categoryIds.add("521");
        SimplifiedPaymentOffers.ProductDetail productDetail = new SimplifiedPaymentOffers.ProductDetail("123400232343411113e331", "6002665", "112866", categoryIds);
        SimplifiedPaymentOffers.Items items = new SimplifiedPaymentOffers.Items("113", "PROMO00187", "2000", productDetail);
        List<SimplifiedPaymentOffers.Items> itemsList = new ArrayList<>();
        itemsList.add(items);
        SimplifiedPaymentOffers.CartDetails cartDetails = new SimplifiedPaymentOffers.CartDetails(itemsList);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("PROMO00187", "true", "true", cartDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1100")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard("4761360075860428");
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("EMI")
                .assertAll();

        ImeiValidation imeiValidation = new ImeiValidation(EmiInfo_COP,orderid,"BLOCK","1234","pooja","2929","18084","667811","123456");
        JsonPath jsonPath = imeiValidation.execute().jsonPath();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_VALIDATION_SERVICE");
        Assertions.assertThat(logs).contains("\"TYPE\" : \"RESPONSE\"");
        Assertions.assertThat(logs).contains("\"resultStatus\":\"SUCCESS\"");

        ImeiValidation imeiValidationUnblock = new ImeiValidation(EmiInfo_COP,orderid,"UNBLOCK","1234","pooja","2929","18084","667811","123456");
        JsonPath jsonPath2 = imeiValidationUnblock.execute().jsonPath();

        String logsAVS = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderid,"AFFORDABILITY_VALIDATION_SERVICE" );
        Assertions.assertThat(logsAVS).contains("avs/v2/post-validate");
        Assertions.assertThat(logsAVS).contains("productId\":\"1234");
    }

    @Owner(POOJA)
    @Feature("PGP-49239")
    @Parameters({"theme"})
    @Test(description = "verify in imei post-validate request productID value same as skuCode is sent for Action = UNBLOCK ")
    public void postValidateRequestToAVSforUNBLOCKProductIDAsSkuCode_Subvention (@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EmiInfo_COP;
        List<String> categoryList = new ArrayList<>();
        categoryList.add("6224");
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("120","321067334","18084",categoryList,"1","1100","51",true, false, null);
        List<SimplifiedSubvention.Item> itemss = new ArrayList<>();
        itemss.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, itemss );
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1100")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard("4761360075860428");
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("EMI")
                .assertAll();

        ImeiValidation imeiValidation = new ImeiValidation(EmiInfo_COP,orderid,"BLOCK","1234","pooja","2929","18084","667811","123456");
        JsonPath jsonPath = imeiValidation.execute().jsonPath();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_VALIDATION_SERVICE");
        Assertions.assertThat(logs).contains("\"TYPE\" : \"RESPONSE\"");
        Assertions.assertThat(logs).contains("\"resultStatus\":\"SUCCESS\"");

        ImeiValidation imeiValidationUnblock = new ImeiValidation(EmiInfo_COP,orderid,"UNBLOCK","1234","pooja","2929","18084","667811","123456");
        JsonPath jsonPath2 = imeiValidationUnblock.execute().jsonPath();

        String logsAVS = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderid,"AFFORDABILITY_VALIDATION_SERVICE" );
        Assertions.assertThat(logsAVS).contains("avs/v2/post-validate");
        Assertions.assertThat(logsAVS).contains("productId\":\"1234");
    }

    @Owner(POOJA)
    @Feature("PGP-49239")
    @Parameters({"theme"})
    @Test(description = "verify imei post-validate success response from AVS ")
    public void postValidateSuccessFromAVS_PaymentOffers(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EmiInfo_COP;
        List<String> categoryIds = new ArrayList<>();
        categoryIds.add("521");
        SimplifiedPaymentOffers.ProductDetail productDetail = new SimplifiedPaymentOffers.ProductDetail("123400232343411113e331", "6002665", "112866", categoryIds);
        SimplifiedPaymentOffers.Items items = new SimplifiedPaymentOffers.Items("113", "PROMO00187", "2000", productDetail);
        List<SimplifiedPaymentOffers.Items> itemsList = new ArrayList<>();
        itemsList.add(items);
        SimplifiedPaymentOffers.CartDetails cartDetails = new SimplifiedPaymentOffers.CartDetails(itemsList);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("PROMO00187", "true", "true", cartDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1100")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard("4761360075860428");
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("EMI")
                .assertAll();

        ImeiValidation imeiValidation = new ImeiValidation(EmiInfo_COP,orderid,"BLOCK","1234","pooja","2929","18084","667811","123456");
        JsonPath jsonPath = imeiValidation.execute().jsonPath();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_VALIDATION_SERVICE");
        Assertions.assertThat(logs).contains("\"TYPE\" : \"RESPONSE\"");
        Assertions.assertThat(logs).contains("\"resultStatus\":\"SUCCESS\"");

        ImeiValidation imeiValidationUnblock = new ImeiValidation(EmiInfo_COP,orderid,"UNBLOCK","1234","pooja","2929","18084","667811","123456");
        JsonPath jsonPath2 = imeiValidationUnblock.execute().jsonPath();

        String logsAVS = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_VALIDATION_SERVICE" );
        Assertions.assertThat(logsAVS).contains("avs/v2/post-validate");
        Assertions.assertThat(logsAVS).contains("\"TYPE\" : \"RESPONSE\"");

    }

    @Owner(POOJA)
    @Feature("PGP-49239")
    @Parameters({"theme"})
    @Test(description = "verify imei post-validate success response from AVS ")
    public void postValidateSuccessFromAVS_Subvention(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EmiInfo_COP;
        List<String> categoryList = new ArrayList<>();
        categoryList.add("6224");
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("120","321067334","18084",categoryList,"1","1100","51",true, false, null);
        List<SimplifiedSubvention.Item> itemss = new ArrayList<>();
        itemss.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, itemss );
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1100")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard("4761360075860428");
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("EMI")
                .assertAll();

        ImeiValidation imeiValidation = new ImeiValidation(EmiInfo_COP,orderid,"BLOCK","1234","pooja","2929","18084","667811","123456");
        JsonPath jsonPath = imeiValidation.execute().jsonPath();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_VALIDATION_SERVICE");
        Assertions.assertThat(logs).contains("\"TYPE\" : \"RESPONSE\"");
        Assertions.assertThat(logs).contains("\"resultStatus\":\"SUCCESS\"");

        ImeiValidation imeiValidationUnblock = new ImeiValidation(EmiInfo_COP,orderid,"UNBLOCK","1234","pooja","2929","18084","667811","123456");
        JsonPath jsonPath2 = imeiValidationUnblock.execute().jsonPath();

        String logsAVS = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_VALIDATION_SERVICE" );
        Assertions.assertThat(logsAVS).contains("avs/v2/post-validate");
        Assertions.assertThat(logsAVS).contains("\"TYPE\" : \"RESPONSE\"");

    }

    @Owner(POOJA)
    @Feature("PGP-49239")
    @Parameters({"theme"})
    @Test(description = "verify imei UNBLOCK sent without BLOCK Action should give error from AVS ")
    public void unBlockWithoutBlockError_PaymentOffers(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EmiInfo_COP;
        List<String> categoryIds = new ArrayList<>();
        categoryIds.add("521");
        SimplifiedPaymentOffers.ProductDetail productDetail = new SimplifiedPaymentOffers.ProductDetail("123400232343411113e331", "6002665", "112866", categoryIds);
        SimplifiedPaymentOffers.Items items = new SimplifiedPaymentOffers.Items("113", "PROMO00187", "2000", productDetail);
        List<SimplifiedPaymentOffers.Items> itemsList = new ArrayList<>();
        itemsList.add(items);
        SimplifiedPaymentOffers.CartDetails cartDetails = new SimplifiedPaymentOffers.CartDetails(itemsList);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("PROMO00187", "true", "true", cartDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1100")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard("4761360075860428");
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("EMI")
                .assertAll();

        ImeiValidation imeiValidationUnblock = new ImeiValidation(EmiInfo_COP,orderid,"UNBLOCK","1234","pooja","2929","18084","667811","123456");
        JsonPath jsonPath2 = imeiValidationUnblock.execute().jsonPath();


        String logsAVS = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_VALIDATION_SERVICE" );
        Assertions.assertThat(logsAVS).contains("avs/v2/post-validate");
        Assertions.assertThat(logsAVS).contains("\"TYPE\" : \"RESPONSE\"");
        Assertions.assertThat(logsAVS).contains("\"resultStatus\":\"FAILURE\"");
        Assertions.assertThat(logsAVS).contains("Pre validation not called for given transactionId");
    }

    @Owner(POOJA)
    @Feature("PGP-49239")
    @Parameters({"theme"})
    @Test(description = "verify imei UNBLOCK sent without BLOCK Action should give error from AVS ")
    public void unBlockWithoutBlockError_Subvention(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EmiInfo_COP;
        List<String> categoryList = new ArrayList<>();
        categoryList.add("6224");
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("120","321067334","18084",categoryList,"1","1100","51",true, false, null);
        List<SimplifiedSubvention.Item> itemss = new ArrayList<>();
        itemss.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, itemss );
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1100")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard("4761360075860428");
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("EMI")
                .assertAll();


        ImeiValidation imeiValidationUnblock = new ImeiValidation(EmiInfo_COP,orderid,"UNBLOCK","1234","pooja","2929","18084","667811","123456");
        JsonPath jsonPath2 = imeiValidationUnblock.execute().jsonPath();

        String logsAVS = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"AFFORDABILITY_VALIDATION_SERVICE" );
        Assertions.assertThat(logsAVS).contains("avs/v2/post-validate");
        Assertions.assertThat(logsAVS).contains("\"TYPE\" : \"RESPONSE\"");
        Assertions.assertThat(logsAVS).contains("\"resultStatus\":\"FAILURE\"");
        Assertions.assertThat(logsAVS).contains("Pre validation not called for given transactionId");
    }
}
