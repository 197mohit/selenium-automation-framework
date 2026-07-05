package scripts.Native.checkoutjs;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.RedisAPI;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedPaymentOffers;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedSubvention;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutJsCheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.ff4j.FF4JFlags;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

//HDFC pe txn karne k liye : theia server pe project-theia.properties file me ye property honi chahiye : MERCHANT_ONLY_CONTRIBUTION_EMI_BANKS=HDFC

public class CheckoutJsICB extends CheckoutJsBase {

    //only merchant contri is configured for amount based subvention on prod

    //modify brand id values in our DB for pref : ICB_AUTO_SETTLEMENT_BRAND on mid
    private final CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();


    @Owner("'Mayuri")
    @Feature("PG2-8901")
    @Parameters({"theme"})
    @Test(description = "only subvention,HDFC, brand contri")
    public void EMISubCheckoutJsItembased01(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGONLY_EMI_MIN_MAX;
        List<SimplifiedSubvention.Item> items = new ArrayList<SimplifiedSubvention.Item>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("1", "321067334", "33688", Collections.singletonList("66781"), "1", "2000", "51", true, false, null, true, "offline", "G531BT-BQ002T", "1152435");
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("2000")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setMonth(3);
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
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
                .validateTxnAmount("1993.0")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateResponsePageParameters()
                .assertAll();
        String checkoutWithOrderLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "checkoutWithOrder", "RESPONSE");
        Assertions.assertThat(checkoutWithOrderLogs).contains("{\"status\":\"SUCCESS\",\"message\":null,\"bankId\":\"HDFC-CREDIT_CARD\",\"bankName\":\"*********************\",\"bankCode\":\"HDFC\",\"cardType\":\"***********\",\"bankLogoUrl\":\"https://staticgw.paytm.in/native/bank/HDFC.png\",\"planId\":\"302023434206761984\",\"pgPlanId\":\"HDFC&3\",\"rate\":2.0,\"interval\":3,\"emi\":666.55,\"interest\":7.0,\"emiType\":\"SUBVENTION\",\"emiLabel\":\"Subvention\",\"gratifications\":[{\"value\":7.0,\"type\":\"DISCOUNT\",\"label\":\"Discount\"}],\"itemBreakUp\":[{\"id\":\"1\",\"interest\":7.0,\"offerId\":\"2220349\",\"subventionType\":\"ZERO_COST\",\"gratifications\":[{\"value\":7.0,\"type\":\"DISCOUNT\",\"label\":\"Emi Discount\"}],\"amountBearer\":{\"brand\":7.0,\"merchant\":0.0,\"platform\":0.0}}]}");

        String affordabilityPlatformRequestLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "AFFORDABILITY_PLATFORM", "REQUEST");
        Assertions.assertThat(affordabilityPlatformRequestLogs).contains("\"orderType\":\"BRAND_EMI_ORDER\"")
                .contains("subventionCheckoutInfo").isNotNull()
                .contains("\"emiType\":\"SUBVENTION\"")
                .contains("gratifications").isNotNull()
                .contains("\"brand\":7.0")
                .contains("itemDetails").isNotNull()
                .contains("orderInfo").isNotNull()
                .contains("\"orderAmount\":{\"currency\":\"INR\",\"value\":\"199300\"}")
                .contains("\"checkoutOrderAmount\":{\"currency\":\"INR\",\"value\":\"200000\"}")
                .contains("\"productInfo\":[{\"productId\":\"321067334\",\"brandId\":\"33688\",\"model\":\"G531BT-BQ002T\",\"price\":\"200000\"}]}")
                .contains("{\"status\":\"SUCCESS\",\"bankId\":\"HDFC-CREDIT_CARD\",\"bankName\":\"*********************\",\"bankCode\":\"HDFC\",\"cardType\":\"***********\",\"bankLogoUrl\":\"https://staticgw.paytm.in/native/bank/HDFC.png\",\"planId\":\"302023434206761984\",\"pgPlanId\":\"HDFC&3\",\"rate\":2.0,\"interval\":3,\"emi\":666.55,\"interest\":7.0,\"emiType\":\"SUBVENTION\",\"emiLabel\":\"Subvention\",\"gratifications\":[{\"value\":7.0,\"type\":\"DISCOUNT\",\"label\":\"Discount\"}],\"itemBreakUp\":[{\"id\":\"1\",\"orderItemId\":\"1\",\"interest\":7.0,\"offerId\":\"2220349\",\"subventionType\":\"ZERO_COST\",\"gratifications\":[{\"value\":7.0,\"type\":\"DISCOUNT\",\"label\":\"Emi Discount\"}],\"amountBearer\":{\"brand\":7.0,\"merchant\":0.0,\"platform\":0.0},\"itemDetails\":{\"id\":\"1\",\"productId\":\"321067334\",\"brandId\":\"33688\",\"categoryList\":[\"66781\"],\"merchantId\":\"1152435\",\"model\":\"G531BT-BQ002T\",\"price\":2000.0,\"quantity\":1,\"discoverability\":\"offline\",\"verticalId\":\"51\",\"isPhysical\":true,\"isEmiEnabled\":true,\"offerDetails\":{\"offerId\":\"2220349\"},\"isStandardEmi\":false,\"originalPrice\":0.0}}]}}");

        String affordabilityPlatformResponseLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "AFFORDABILITY_PLATFORM", "RESPONSE");
        Assertions.assertThat(affordabilityPlatformResponseLogs).contains("\"orderType\":\"BRAND_EMI_ORDER\"")
                .contains("checkoutExtendInfo").isNotNull()
                .contains("affordabilityAcquirementId").isNotNull()
                .contains("\"amountType\":\"BRAND_EMI_INSTANT_CASHBACK\"")
                .contains("\"payMode\":\"EMI\"")
                .contains("\"merchantType\":\"ONUS\"")
                .contains("\"category\":\"SUBVENTION\"")
                .contains("\"offerType\":\"DISCOUNT\"")
                .contains("\"type\":\"BRAND\"")
                .contains("\"orderAmount\":{\"value\":199300,\"currency\":\"INR\"}")
                .contains("\"checkoutOrderAmount\":{\"value\":199300,\"currency\":\"INR\"}")
                .contains("\"billAmount\":{\"value\":199300,\"currency\":\"INR\"}")
                .contains("\"productInfo\":[{\"skuIdentifier\":null,\"productName\":null,\"productId\":\"321067334\",\"brandId\":\"33688\",\"brandName\":null,\"categoryId\":null,\"categoryName\":null,\"sellerId\":null,\"model\":\"G531BT-BQ002T\",\"modelName\":null,\"price\":200000,\"discoverability\":null,\"verticalId\":null,\"logo\":null}]");

        String acquiringOrderModifyRequestLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "ACQUIRING_ORDER_MODIFY", "REQUEST");
        Assertions.assertThat(acquiringOrderModifyRequestLogs).contains("AFFORDABILITY_INFO").isNotNull()
                .contains("extendInfo").isNotNull()
                .contains("detailExtendInfo").isNotNull()
                .contains("affordabilityAcquirementId").isNotNull()
                .contains("\"emiCategory\":\"brandEmi\"")
                .contains("\"billAmount\":\"199300\"")
                .contains("\"orderAmount\":{\"currency\":\"INR\",\"value\":199300}")
                .contains("\"orderPricingInfo\":{\"pricingAmountInfoList\":[{\"pricingAmount\":{\"currency\":\"INR\",\"value\":700},\"amountType\":\"BRAND_EMI_INSTANT_CASHBACK\",\"contriDetail\":{\"brandContri\":\"7.0\"}}]}");

        String acquiringOrderModifyResponseLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.getBody().getOrderId(), "ACQUIRING_ORDER_MODIFY", "RESPONSE");
        Assertions.assertThat(acquiringOrderModifyResponseLogs).contains("acquirementId").isNotNull();
    }
}
