package scripts.api.linkservice;

import com.paytm.api.InstaproxyPtybliResponse;
import com.paytm.api.TransactionStatusV1API;
import com.paytm.api.TxnStatus;
import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.theia.FetchMerchantInfoV2;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.apphelpers.QRHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.TransactionStatusV1.Body;
import com.paytm.dto.TransactionStatusV1.Head;
import com.paytm.dto.TransactionStatusV1.TransactionStatusV1DTO;
import com.paytm.dto.theia.FetchMerchantInfoV2DTO;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.LinkPaymentLoginPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.paytm.api.linkAPI.linkConstant.PGPAPIResourcePath.CREATE_LINK_SUCCESS;
import static com.paytm.api.linkAPI.linkConstant.PGPAPIResourcePath.CREATE_LINK_SUCCESS_CODE;
import static com.paytm.api.linkAPI.linkConstant.PGPAPIResourcePath.CREATE_LINK_SUCCESS_STATUS;

public class PaymentLinks extends PGPBaseTest {

        private static final Pattern ORDER_ID_IN_JSON = Pattern.compile("\"ORDER_ID\"\\s*:\\s*\"([^\"]+)\"");
        private static final Pattern ORDER_ID_CAMEL_IN_JSON = Pattern.compile("\"orderId\"\\s*:\\s*\"([^\"]+)\"");
        private static final Pattern TXN_TOKEN_IN_JSON = Pattern.compile("\"txnToken\"\\s*:\\s*\"([^\"]+)\"");
        private static final String EXPECTED_PRODUCT_CODE_IN_LOG = "\"productCode\":\"51051000100000000102\"";
        private static final String EXPECTED_PAYMENT_LINK_TXN_FLAG_IN_LOG = "\"paymentLinkTxn\":\"true\"";

        private static int countOccurrences(String haystack, String needle) {
                int count = 0;
                int idx = 0;
                while ((idx = haystack.indexOf(needle, idx)) != -1) {
                        count++;
                        idx += needle.length();
                }
                return count;
        }

        private static String extractOrderIdFromTheiaLogs(String logs, String contextLabel) {
                Matcher upper = ORDER_ID_IN_JSON.matcher(logs);
                if (upper.find()) {
                        return upper.group(1);
                }
                Matcher camel = ORDER_ID_CAMEL_IN_JSON.matcher(logs);
                Assert.assertTrue(camel.find(), "orderId / ORDER_ID not found in Theia logs: " + contextLabel);
                return camel.group(1);
        }

        /** From initiateTransaction RESPONSE PAYLOAD: {@code "txnToken":"<token>"}. */
        private static String extractTxnTokenFromInitiateTxnResponseLogs(String logs) {
                Matcher m = TXN_TOKEN_IN_JSON.matcher(logs);
                Assert.assertTrue(m.find(), "txnToken not found in /theia/api/v1/initiateTransaction RESPONSE logs");
                return m.group(1);
        }

        @Owner("VIDHI")
        @Feature("PG-1815")
        @Parameters({ "theme" })
        @Test(description = "Verify the e2e PaymentLink txn and verify productcode 102 is passed in COP and LPV request")
        public void verifyPaymentLinkProductcode_FlaginCOP(@Optional("checkoutjs_web_revamp") String theme)
                        throws Exception {
                RestAssured.useRelaxedHTTPSValidation();
                String mid = Constants.MerchantType.Payment_Links_MID.getId();

                CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "2");
                JsonPath createResponse = createNewLink.execute().jsonPath();
                Assert.assertEquals(createResponse.getString("body.resultInfo.resultMessage"), CREATE_LINK_SUCCESS);
                Assert.assertEquals(createResponse.getString("body.resultInfo.resultCode"), CREATE_LINK_SUCCESS_CODE);
                Assert.assertEquals(createResponse.getString("body.resultInfo.resultStatus"),
                                CREATE_LINK_SUCCESS_STATUS);

                String paymentLink = createResponse.getString("body.longUrl");
                String linkId = createResponse.getString("body.linkId");

                LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
                linkPaymentLoginPage.openLink(paymentLink, "None");

                CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
                cashierPage.waitUntilLoads();
                PaymentDTO paymentDTO = new PaymentDTO()
                                .setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
                cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
                cashierPage.LinkTxnSuccessfullScreenMessage().waitUntilVisible();
                Assertions.assertThat(cashierPage.LinkTxnSuccessfullScreenMessage().getText())
                                .contains("Paid Successfully");

                String paymentConsultLogs = LogsValidationHelper.verifyLogsOnPod(
                                PG2LogsValidationHelper.setEnvService.theia_facade,
                                linkId,
                                "link/paymentConsult",
                                "REQUEST");
                Assertions.assertThat(paymentConsultLogs).isNotBlank();

                Matcher orderIdMatcher = ORDER_ID_IN_JSON.matcher(paymentConsultLogs);
                Assert.assertTrue(orderIdMatcher.find(), "ORDER_ID not found in link/paymentConsult REQUEST logs");
                String orderId = orderIdMatcher.group(1);

                String litePayviewLogs = LogsValidationHelper.verifyLogsOnPod(
                                PG2LogsValidationHelper.setEnvService.payment_option_facade,
                                orderId,
                                "CHECKOUT_LITE_PAYVIEW_CONSULT",
                                "REQUEST");
                Assertions.assertThat(litePayviewLogs).isNotBlank();
                Assertions.assertThat(litePayviewLogs).contains("productCode='51051000100000000102'");
                Assertions.assertThat(litePayviewLogs).contains(orderId);

                String acquiringCreateLogs = LogsValidationHelper.verifyLogsOnPod(
                                PG2LogsValidationHelper.setEnvService.theia_facade,
                                orderId,
                                "ACQUIRING_PAY_ORDER",
                                "REQUEST");
                Assertions.assertThat(acquiringCreateLogs).isNotBlank();
                Assertions.assertThat(acquiringCreateLogs).contains("ACQUIRING_PAY_ORDER");
                Assertions.assertThat(acquiringCreateLogs).contains(EXPECTED_PRODUCT_CODE_IN_LOG);
                Assertions.assertThat(countOccurrences(acquiringCreateLogs, EXPECTED_PRODUCT_CODE_IN_LOG))
                                .as("productCode 51051000100000000102 should appear 3 times in ACQUIRING_PAY_ORDER REQUEST log")
                                .isEqualTo(2);

                Assertions.assertThat(acquiringCreateLogs).contains(EXPECTED_PAYMENT_LINK_TXN_FLAG_IN_LOG);
                Assertions.assertThat(countOccurrences(acquiringCreateLogs, EXPECTED_PAYMENT_LINK_TXN_FLAG_IN_LOG))
                                .as("paymentLinkTxn - true should appear 3 times in ACQUIRING_CREATE_ORDER_AND_PAY REQUEST log")
                                .isEqualTo(2);

        }

        /**
         * Order id shown on link / checkout response (page body); used like
         * {@code initTxnDTO.orderFromBody()} in native flows.
         */
        private static String orderIdFromPaymentLinkResponseBody(CashierPage cashierPage) {
                return Awaitility.with()
                                .pollInSameThread()
                                .await()
                                .atMost(2, TimeUnit.MINUTES)
                                .pollInterval(3, TimeUnit.SECONDS)
                                .until(
                                                () -> {
                                                        try {
                                                                if (cashierPage.OrderIdLinkstatus()
                                                                                .isElementPresent()) {
                                                                        String raw = cashierPage.OrderIdLinkstatus()
                                                                                        .getText();
                                                                        String digitsOnly = raw.replaceAll("[^0-9]",
                                                                                        "");
                                                                        if (!digitsOnly.isEmpty()) {
                                                                                return digitsOnly;
                                                                        }
                                                                }
                                                        } catch (Exception ignored) {
                                                                // keep polling until page exposes order id
                                                        }
                                                        WebDriver driver = DriverManager.getDriver();
                                                        if (driver == null) {
                                                                return null;
                                                        }
                                                        String page = driver.getPageSource();
                                                        Matcher orderIdKey = Pattern.compile(
                                                                        "\"orderId\"\\s*:\\s*\"([^\"]+)\"",
                                                                        Pattern.CASE_INSENSITIVE)
                                                                        .matcher(page);
                                                        if (orderIdKey.find()) {
                                                                return orderIdKey.group(1);
                                                        }
                                                        Matcher orderIdField = ORDER_ID_IN_JSON.matcher(page);
                                                        if (orderIdField.find()) {
                                                                return orderIdField.group(1);
                                                        }
                                                        return null;
                                                },
                                                id -> id != null && !id.isEmpty());
        }

        @Owner("VIDHI")
        @Feature("PG-1815")
        @Parameters({ "theme" })
        @Test(description = "Verify the payment link E2E transaction status - product code 102")
        public void verifyPaymentLinkCcTxn(
                        @Optional("checkoutjs_web_revamp") String theme) throws Exception {
                RestAssured.useRelaxedHTTPSValidation();
                String mid = Constants.MerchantType.Payment_Links_MID.getId();

                CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "2");
                JsonPath createResponse = createNewLink.execute().jsonPath();
                Assert.assertEquals(createResponse.getString("body.resultInfo.resultMessage"), CREATE_LINK_SUCCESS);
                Assert.assertEquals(createResponse.getString("body.resultInfo.resultCode"), CREATE_LINK_SUCCESS_CODE);
                Assert.assertEquals(createResponse.getString("body.resultInfo.resultStatus"),
                                CREATE_LINK_SUCCESS_STATUS);

                String paymentLink = createResponse.getString("body.longUrl");
                LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
                linkPaymentLoginPage.openLink(paymentLink, "None");

                CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
                cashierPage.waitUntilLoads();
                PaymentDTO paymentDTO = new PaymentDTO()
                                .setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
                cashierPage.payBy(Constants.PayMode.CC, paymentDTO);

                // cashierPage.LinkTxnSuccessfullScreenMessageNew().waitUntilVisible();
                Assertions.assertThat(cashierPage.LinkTxnSuccessfullScreenMessage().getText())
                                .contains("Paid Successfully");

                String orderFromBody = orderIdFromPaymentLinkResponseBody(cashierPage);

                TxnStatus txnStatus = new TxnStatus(mid, orderFromBody);
                txnStatus.executeUntilNotPending();
                txnStatus
                                .validateSuccessResponse()
                                .validateOrderid(orderFromBody)
                                .validateMid(mid)
                                .validatePaymentMode("CC")
                                .AssertAll();
        }

        @Owner("VIDHI")
        @Feature("PGQA-413")
        @Parameters({ "theme" })
        @Test(description = "Payment link: UPI Intent is not filtering out in Native App FPO response")
        public void verifyUPIIntentNotFilteringOutInNativeAppFPO(
                        @Optional("checkoutjs_web_revamp") String theme) throws Exception {
                RestAssured.useRelaxedHTTPSValidation();
                String mid = Constants.MerchantType.PAYMENT_LINKS_OFFLINE_MID_UPI.getId();
                User user = userManager.getForRead(Label.BASIC);

                CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "2");
                JsonPath createResponse = createNewLink.execute().jsonPath();
                Assert.assertEquals(createResponse.getString("body.resultInfo.resultMessage"), CREATE_LINK_SUCCESS);
                Assert.assertEquals(createResponse.getString("body.resultInfo.resultCode"), CREATE_LINK_SUCCESS_CODE);
                Assert.assertEquals(createResponse.getString("body.resultInfo.resultStatus"),
                                CREATE_LINK_SUCCESS_STATUS);

                String paymentLink = createResponse.getString("body.longUrl");
                String linkId = createResponse.getString("body.linkId");

                LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
                linkPaymentLoginPage.openLink(paymentLink, "None");

                CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
                cashierPage.waitUntilLoads();

                // Single grep on THEIA_REQ_RESP by link id from create link response; parse
                // root ORDER_ID from hit.
                String theiaLogsByLinkId = LogsValidationHelper.verifyLogsOnPod(
                                PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP,
                                linkId);
                Assertions.assertThat(theiaLogsByLinkId).as("THEIA_REQ_RESP logs containing linkId").isNotBlank();

                String orderId = extractOrderIdFromTheiaLogs(
                                theiaLogsByLinkId,
                                "grep by linkId " + linkId);

                String initiateTxnResponseLogs = LogsValidationHelper.verifyLogsOnPod(
                                PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP,
                                orderId,
                                Constants.NativeAPIResourcePath.INIT_TXN,
                                "RESPONSE");
                Assertions.assertThat(initiateTxnResponseLogs).as("initiateTransaction RESPONSE logs").isNotBlank();

                String txnToken = extractTxnTokenFromInitiateTxnResponseLogs(initiateTxnResponseLogs);

                String ssoToken = user.ssoToken();

                FetchMerchantInfoV2DTO fetchMerchantInfoV2Dto = new FetchMerchantInfoV2DTO.Builder(mid, orderId,
                                txnToken,
                                ssoToken).build();
                Response fetchMerchantInfoResp = new FetchMerchantInfoV2(fetchMerchantInfoV2Dto).execute();

                Assertions.assertThat(fetchMerchantInfoResp.getStatusCode()).as("fetchMerchantInfo v2 HTTP status")
                                .isEqualTo(200);
                JsonPath fmiJson = fetchMerchantInfoResp.jsonPath();
                Assertions.assertThat(fmiJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
                Assertions.assertThat(fmiJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");

                FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
                Response fetchPayOptionsResp = new FetchPaymentOption(mid, orderId, fetchPaymentOptionsDTO).execute();
                Assertions.assertThat(fetchPayOptionsResp.getStatusCode()).as("fetchPaymentOptions HTTP status")
                                .isEqualTo(200);
                JsonPath fpoJson = fetchPayOptionsResp.jsonPath();
                Assertions.assertThat(fpoJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
                Assertions.assertThat(fpoJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
                Assertions.assertThat(fpoJson.getString("body.productCode")).isEqualTo("51051000100000000102");
                Assertions.assertThat(fpoJson.getString(
                                "body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions.channelCode"))
                                .contains("UPIPUSH");
        }

        @Owner(Constants.Owner.CHAKSHU)
        @Feature("PGQA-413")
        @Parameters({ "theme" })
        @Test(description = "Payment link: UPI NON ReqAuth DQR in Native App FPO response")
        public void verifyUPINonReqAuthDQRInNativeAppFPO(
                        @Optional("checkoutjs_web_revamp") String theme) throws Exception {
                RestAssured.useRelaxedHTTPSValidation();
                String mid = Constants.MerchantType.PAYMENT_LINKS_OFFLINE_MID_UPI.getId();

                CreateNewLink createNewLink = new CreateNewLink().buildRequest(mid, "FIXED", "2");
                JsonPath createResponse = createNewLink.execute().jsonPath();
                Assert.assertEquals(createResponse.getString("body.resultInfo.resultMessage"), CREATE_LINK_SUCCESS);
                Assert.assertEquals(createResponse.getString("body.resultInfo.resultCode"), CREATE_LINK_SUCCESS_CODE);
                Assert.assertEquals(createResponse.getString("body.resultInfo.resultStatus"),
                                CREATE_LINK_SUCCESS_STATUS);

                String paymentLink = createResponse.getString("body.longUrl");
                String linkId = createResponse.getString("body.linkId");

                LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
                linkPaymentLoginPage.openLink(paymentLink, "None");

                CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
                Thread.sleep(Long.parseLong("25000"));
                cashierPage.waitUntilLoads();

                // Single grep on THEIA_REQ_RESP by link id from create link response; parse
                // root ORDER_ID from hit.
                String theiaLogsByLinkId = LogsValidationHelper.verifyLogsOnPod(
                                PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP,
                                linkId);
                Assertions.assertThat(theiaLogsByLinkId).as("THEIA_REQ_RESP logs containing linkId").isNotBlank();

                String orderId = extractOrderIdFromTheiaLogs(
                                theiaLogsByLinkId,
                                "grep by linkId " + linkId);

                String initiateTxnResponseLogs = LogsValidationHelper.verifyLogsOnPod(
                                PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP,
                                orderId,
                                Constants.NativeAPIResourcePath.INIT_TXN,
                                "RESPONSE");
                Assertions.assertThat(initiateTxnResponseLogs).as("initiateTransaction RESPONSE logs").isNotBlank();

                String txnToken = extractTxnTokenFromInitiateTxnResponseLogs(initiateTxnResponseLogs);

                FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                                .setGenerateOrderId("false")
                                .setDeepLinkRequiedField(true)
                                .setWorkFlow("checkout")
                                .build();
                FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(mid, orderId,
                                fetchPaymentOptionsDTO);
                JsonPath fpoResponse = fetchPaymentOption.execute().jsonPath();

                // 3. Get QR data and decode deeplink
                String base64QrCode = fpoResponse.getString("body.qrDetail.dataUrl");
                String deeplink = QRHelper.getDeeplinkFromQRImage(base64QrCode);
                Assertions.assertThat(deeplink).as("decoded UPI deeplink from QR").isNotBlank();
                System.out.println("Decoded deeplink: " + deeplink);

                Map<String, String> deeplinkInfo = QRHelper.parseDeeplinkInfo(deeplink);
                String tr = deeplinkInfo.get("tr");
                String amount = deeplinkInfo.get("amount");
                String payeeVpa = deeplinkInfo.get("payeeVpa");
                Assertions.assertThat(tr).as("tr from deeplink").isNotBlank();

                String bankRrn = CommonHelpers.generateOrderId();
                JsonPath callbackResponse = new InstaproxyPtybliResponse(
                                amount,
                                tr,
                                bankRrn,
                                "UPI_SAVINGS")
                                .setSecureResponsePath(InstaproxyPtybliResponse.INSTAPROXY_PTYL_UPI_PUSH_RESP)
                                .setContext("body.payeeVpa", payeeVpa)
                                .deleteContext("body.paymentInstrument")
                                .deleteContext("body.creditCardInfo")
                                .execute()
                                .jsonPath();
                Assertions.assertThat(callbackResponse.getString("body.resultCodeId")).isEqualTo("001");
                Assertions.assertThat(callbackResponse.getString("body.resultCode")).isEqualToIgnoringCase("SUCCESS");

                TransactionStatusV1DTO transactionStatusV1DTO = new TransactionStatusV1DTO();
                transactionStatusV1DTO.setBody(new Body()
                                .setIsCallbackUrlRequired(true)
                                .setIsFinalTxnStatusRequired(false)
                                .setMid(mid)
                                .setOrderId(orderId))
                                .setHead(new Head().setTokenType("TXN_TOKEN").setToken(txnToken));
                TransactionStatusV1API transactionStatusV1API = new TransactionStatusV1API(transactionStatusV1DTO);
                JsonPath theiaTxnStatusJson = transactionStatusV1API.executeUntilNotPending().jsonPath();
                Assertions.assertThat(theiaTxnStatusJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
                Assertions.assertThat(theiaTxnStatusJson.getString("body.resultInfo.resultCode"))
                                .isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespCode());
                Assertions.assertThat(theiaTxnStatusJson.getString("body.resultInfo.resultMsg"))
                                .isEqualTo(Constants.ResponseCode.TXN_SUCCESS.getRespMsg());
                Assertions.assertThat(theiaTxnStatusJson.getString("body.txnInfo.STATUS"))
                                .isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
                Assertions.assertThat(theiaTxnStatusJson.getString("body.txnInfo.ORDERID")).isEqualTo(orderId);
                Assertions.assertThat(theiaTxnStatusJson.getString("body.txnInfo.MID")).isEqualTo(mid);
                Assertions.assertThat(theiaTxnStatusJson.getString("body.txnInfo.TXNID")).isNotBlank();

                TxnStatus txnStatus = new TxnStatus(mid, orderId);
                txnStatus.executeUntilNotPending();
                txnStatus
                                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                                .validateSuccessResponse()
                                .validateOrderid(orderId)
                                .validateTxnAmount(amount)
                                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                                .validateRespCode("01")
                                .validatePaymentMode("UPI")
                                .validateMid(mid)
                                .AssertAll();

                String paymentOptionFacadeLogs = LogsValidationHelper.verifyLogsOnPod(
                                PG2LogsValidationHelper.setEnvService.payment_option_facade,
                                orderId);

                Assertions.assertThat(paymentOptionFacadeLogs).contains("ROUTER_CONSULT");
        }

}
