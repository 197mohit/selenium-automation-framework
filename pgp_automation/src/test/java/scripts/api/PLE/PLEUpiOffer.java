package scripts.api.PLE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.paytm.LocalConfig;
import com.paytm.api.PLE.DealsEmiAll;
import com.paytm.api.PLE.DealsEmiGetInvoice;
import com.paytm.api.PLE.DealsEmiOfferApply;
import com.paytm.api.PLE.DealsPaymentCreateQr;
import com.paytm.api.PLE.DealsPaymentTransactionHistory;
import com.paytm.api.PLE.DealsPaymentUpiQrEchoTxnReversal;
import com.paytm.api.PLE.DealsPaymentUpiQrTxnDetails;
import com.paytm.api.PLE.DealsShareInvoiceViaSms;
import com.paytm.api.PLE.DealsStoreDetails;
import com.paytm.api.PLE.DealsStoreProductDetails;
import com.paytm.api.UpiPspProcessor;
import com.paytm.dto.UPIIntentRequestDTO;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.QRHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.upiIntent.staticQR.Response.StaticQrUpiPSPResponse;
import com.paytm.dto.upiIntent.staticQR.StaticQrUpiPSPRequest;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.appconstants.Constants.MerchantType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import java.util.List;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * PLE branded UPI offer E2E (API-only): store product → store details → emi/all
 * → emi/offerApply → createQr →
 * → createQr → {@link UpiPspProcessor} → {@link DealsPaymentUpiQrTxnDetails} →
 * {@link DealsPaymentUpiQrEchoTxnReversal} → share invoice SMS → get invoice →
 * transaction history
 */
public class PLEUpiOffer extends PGPBaseTest {

        private static final Logger log = LoggerFactory.getLogger(PLEUpiOffer.class);

        private static final String PRODUCT_ID = "1235528546";
        /** Staging MID from store-attendant UPI curls. */
        private static final String PHONE = "9898989898";
        private static final String PAYER_VPA = "9898989898@okicici";
        private static final String PAYEE_VPA = "paytm.ud123649907885@ptys";
        private static final String MERCHANT_DISPLAY_NAME = "Emi Store Testing";
        private static final String BUILD_VERSION = "S26.01.07";

        /**
         * Fixed 8-digit {@code tid} on createQr and downstream UPI stubs (matches
         * staging curl).
         */
        private static final String CREATE_QR_TID = "97005459";

        private static String refererProductsPage(String dealsBase, String pgmid, String productId) {
                return String.format(
                                "%s/pgmid/%s/products/%s?isPaytmLovesEmiFlow=true&prevScreen=product_search_page&utm_source="
                                                + "login-via-username",
                                dealsBase, pgmid, productId);
        }

        /**
         * Plan-summary referer aligned with staging UPI + offer query params (dynamic
         * price / payable / brand).
         */
        private static String refererPlanSummaryUpi(
                        String dealsBase, String pgmid, String productId, int brandId, long listPricePaise,
                        long payablePaise) {
                return String.format(
                                Locale.US,
                                "%s/pgmid/%s/plan-summary?isPaytmLovesEmiFlow=true&prevScreen=product_details_page&issuingBank=UPI&"
                                                + "payMethod=UPI&price=%d&tenure=0&brandId=%d&isOfferApplied=true&totalPayableAmount=%d&productId="
                                                + "%s",
                                dealsBase, pgmid, listPricePaise, brandId, payablePaise, productId);
        }

        private static String refererStorePage(String dealsBase, String pgmid) {
                return dealsBase + "/pgmid/" + pgmid + "/store?prevScreen=store_page";
        }

        private static String composeExtendOrderRef(String yyyy, String mmdd, String hhmmss, String invoiceNo,
                        String tid) {
                return yyyy + mmdd + hhmmss + invoiceNo + tid;
        }

        /**
         * Six-digit numeric {@code invoiceNumber} / IMEI prefix for createQr (staging
         * uses short numeric invoice). Override
         * with {@code -Dple.upi.invoice} (digits only,
         * {@code floorMod(..., 1_000_000)}).
         */
        private static String sixDigitInvoiceNo(long flowTs) {
                String prop = System.getProperty("ple.upi.invoice");
                if (prop != null && !prop.isEmpty() && prop.matches("\\d+")) {
                        return String.format(Locale.US, "%06d", Math.floorMod(Long.parseLong(prop), 1_000_000L));
                }
                return String.format(Locale.US, "%06d", Math.floorMod(flowTs, 1_000_000L));
        }

        /**
         * PGP base URL for {@link UpiPspProcessor} + {@code instaUrl} in
         * {@link UPIIntentRequestDTO}.
         */
        private static String plePgpHostForUpiFlow() {
                if (Boolean.parseBoolean(System.getProperty("ple.upi.useQa14Pgp", "true"))) {
                        return PleQa14UpiPspProcessor.QA14_PGP_HOST;
                }
                return LocalConfig.PGP_HOST;
        }

        private static UpiPspProcessor upiPspProcessorForPle(StaticQrUpiPSPRequest staticQrUpiPSPRequest) {
                if (Boolean.parseBoolean(System.getProperty("ple.upi.useQa14Pgp", "true"))) {
                        return new PleQa14UpiPspProcessor(staticQrUpiPSPRequest);
                }
                return new UpiPspProcessor(staticQrUpiPSPRequest);
        }

        private boolean transactionHistoryResponseContainsOrderId(Response response, String orderId) {
                List<String> merchantTransIds = response.jsonPath()
                                .getList("orderList.merchantTransId", String.class);

                List<String> pleIds = response.jsonPath()
                                .getList("orderList.pleId", String.class);

                return (merchantTransIds != null && merchantTransIds.contains(orderId)) ||
                                (pleIds != null && pleIds.contains(orderId));
        }

        @Owner(Constants.Owner.CHAKSHU)
        @Feature("PLE-UPI-Offer-QR-E2E")
        @Test(description = "PLE UPI offer CreateQr , UpiPspProcessor , txnDetails and echo txnReversal then Share invoice SMS, Get invoice, Transaction history")
        public void pleUpiBrandOfferQrFlowEndToEnd() throws Exception {

                long flowTs = System.currentTimeMillis();
                String PG_MID = MerchantType.PLE_DEALS_MID.getId();

                /*
                 * createQr (and txnDetails / echo reversal) clock: +2 min vs wall clock — per
                 * product requirement.
                 */
                LocalDateTime createQrTxnClock = LocalDateTime.now().plusMinutes(2);
                LocalDate ld = createQrTxnClock.toLocalDate();
                String yearStr = String.valueOf(ld.getYear());
                String dateMmdd = DateTimeFormatter.ofPattern("MMdd").format(createQrTxnClock);
                String timeHhmmss = DateTimeFormatter.ofPattern("HHmmss").format(createQrTxnClock);
                String tid = CREATE_QR_TID;
                String invoiceNo = sixDigitInvoiceNo(flowTs);

                String compositeExtendOrderId = composeExtendOrderRef(yearStr, dateMmdd, timeHhmmss, invoiceNo, tid);
                log.info("[PLE UPI] tid={} invoice={} compositeExtendOrderRef={}", tid, invoiceNo,
                                compositeExtendOrderId);

                String dealsBase = LocalConfig.PLE_DEALS_BASE_URL;
                String refProduct = refererProductsPage(dealsBase, PG_MID, PRODUCT_ID);

                // 1) GET /api/store/products/{productId}
                DealsStoreProductDetails.Builder prodBuilder = new DealsStoreProductDetails.Builder()
                                .baseUri(dealsBase)
                                .productId(PRODUCT_ID)
                                .globalTs(flowTs)
                                .buildVersion(BUILD_VERSION)
                                .referer(refProduct);

                Response productRes = prodBuilder.build().execute();
                Assertions.assertThat(productRes.getStatusCode()).as("store/products").isEqualTo(200);
                JsonPath productJp = productRes.jsonPath();

                int brandId = productJp.getInt("brandId");
                int categoryId = productJp.getInt("categoryId");
                String model = productJp.getString("model");
                String brandNameStr = productJp.getString("brandName");
                long priceFromProduct = productJp.getLong("price");

                Assertions.assertThat(brandId).as("brandId from product").isNotNull();
                Assertions.assertThat(categoryId).as("categoryId from product").isNotNull();
                Assertions.assertThat(model).as("model/sku from product").isNotNull();
                long pricePaise = priceFromProduct * 100;

                // 2) GET /api/store/pgmid/{mid}/details
                DealsStoreDetails.Builder storeDetailBuilder = new DealsStoreDetails.Builder()
                                .baseUri(dealsBase)
                                .pgmid(PG_MID)
                                .globalTs(flowTs + 13)
                                .buildVersion(BUILD_VERSION)
                                .referer(refererStorePage(dealsBase, PG_MID));

                Response storeRes = storeDetailBuilder.build().execute();
                Assertions.assertThat(storeRes.getStatusCode()).as("/api/store/pgmid/*/details").isEqualTo(200);

                // 3) POST /api/emi/all
                DealsEmiAll.Builder emiBuilder = new DealsEmiAll.Builder()
                                .baseUri(dealsBase)
                                .productId(PRODUCT_ID)
                                .brandId(brandId)
                                .categoryId(categoryId)
                                .model(model)
                                .price(pricePaise)
                                .quantity(1)
                                .merchantId(PG_MID)
                                .transactionAmount(pricePaise)
                                .globalTs(flowTs + 137)
                                .buildVersion(BUILD_VERSION)
                                .referer(refProduct);

                Response emiAllRes = emiBuilder.build().execute();
                Assertions.assertThat(emiAllRes.getStatusCode()).as("/api/emi/all").isEqualTo(200);

                // 4) POST /api/emi/offerApply — warms EMI context (payload shape matches
                // createQr)
                DealsEmiOfferApply.Builder offerBuilder = new DealsEmiOfferApply.Builder()
                                .baseUri(dealsBase)
                                .referer(refProduct)
                                .globalTs(flowTs + 181)
                                .buildVersion(BUILD_VERSION)
                                .merchantType("OFFUS")
                                .phoneNo(PHONE)
                                .merchantId(PG_MID)
                                .tid(tid)
                                .paymentDetailTransactionAmount(pricePaise)
                                .addLineItem(
                                                new DealsEmiOfferApply.OfferLineItem(
                                                                PRODUCT_ID, brandId, categoryId, model, pricePaise, 1));
                Response offerApplyRes = offerBuilder.build().execute();
                Assertions.assertThat(offerApplyRes.getStatusCode()).as("/api/emi/offerApply").isEqualTo(200);
                JsonPath offerRes = offerApplyRes.jsonPath();

                String payableAmount = offerRes.getString("response.paymentDetails[0].offerDetails[0].payableAmount");
                long payablePaise = Long.parseLong(payableAmount);

                String planSummaryReferer = refererPlanSummaryUpi(dealsBase, PG_MID, PRODUCT_ID, brandId, pricePaise,
                                payablePaise);

                Response createQrRes = new DealsPaymentCreateQr.Builder()
                                .baseUri(dealsBase)
                                .referer(planSummaryReferer)
                                .globalTs(flowTs + 389)
                                .buildVersion(BUILD_VERSION)
                                .mid(PG_MID)
                                .tid(tid)
                                .time(timeHhmmss)
                                .date(dateMmdd)
                                .year(yearStr)
                                .amount(Long.toString(payablePaise))
                                .invoiceNumber(invoiceNo)
                                .imeiNo(invoiceNo + " ")
                                .flowType("BRAND")
                                .merchantId(PG_MID)
                                .phoneNo(PHONE)
                                .paymentDetailTransactionAmount(pricePaise)
                                .merchantRequestId("T" + flowTs)
                                .addLineItem(new DealsPaymentCreateQr.QrLineItem(
                                                PRODUCT_ID, brandId, categoryId, model, pricePaise, 1))
                                .productInfo(
                                                String.valueOf(brandId),
                                                brandNameStr,
                                                PRODUCT_ID,
                                                model,
                                                invoiceNo)
                                .bankInvoiceNumber(invoiceNo)
                                .affordabilityTotalPayableAmount(Long.toString(payablePaise))
                                .affordabilityCustomerMobileNumber(PHONE)
                                .build()
                                .execute();
                Assertions.assertThat(createQrRes.getStatusCode()).as("/api/payment/createQr").isEqualTo(200);
                JsonPath createJp = createQrRes.jsonPath();

                // Extract values
                String txnAmount = createJp.getString("body.amount");
                String orderId = createJp.getString("body.orderId");
                String acquirementId = createJp.getString("body.acquirementId");

                StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(PG_MID, orderId, txnAmount,
                                PAYEE_VPA, PAYER_VPA, "", "");
                StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "", orderId);
                UpiPspProcessor upiPspProcessor = upiPspProcessorForPle(staticQrUpiPSPRequest);
                Response response = upiPspProcessor.execute();
                StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                                .statusCode(200)
                                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                                .body("body.txnAmount", Matchers.equalTo(txnAmount))
                                .body("body.mid", Matchers.equalTo(PG_MID))
                                .extract().as(StaticQrUpiPSPResponse.class);

                UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
                upiIntentRequestDTO.setAmount(txnAmount)
                                .setInstaUrl(plePgpHostForUpiFlow())
                                .setOrderId(orderId)
                                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                                .setMid(PG_MID);
                Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
                response1.then()
                                .statusCode(200)
                                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"));

                Response txnDetailsResp = new DealsPaymentUpiQrTxnDetails.Builder()
                                .baseUri(dealsBase)
                                .referer(planSummaryReferer)
                                .globalTs(flowTs + 501)
                                .buildVersion(BUILD_VERSION)
                                .mid(PG_MID)
                                .tid(tid)
                                .invoiceNumber(invoiceNo)
                                .time(timeHhmmss)
                                .date(dateMmdd)
                                .year(yearStr)
                                .extendInfoOrderId(compositeExtendOrderId)
                                .build()
                                .execute();
                Assertions.assertThat(txnDetailsResp.getStatusCode()).as("/api/payment/upiQr/txnDetails")
                                .isEqualTo(200);

                Response echoRevResp = new DealsPaymentUpiQrEchoTxnReversal.Builder()
                                .baseUri(dealsBase)
                                .referer(planSummaryReferer)
                                .globalTs(flowTs + 617)
                                .buildVersion(BUILD_VERSION)
                                .mid(PG_MID)
                                .tid(tid)
                                .stan(invoiceNo)
                                .invoiceNumber(invoiceNo)
                                .time(timeHhmmss)
                                .date(dateMmdd)
                                .year(yearStr)
                                .acquirementId(acquirementId)
                                .orderId(orderId)
                                .extendInfoOrderId(compositeExtendOrderId)
                                .riskTimestamp(Long.toString(flowTs + 713))
                                .build()
                                .execute();
                Assertions.assertThat(echoRevResp.getStatusCode()).as("/api/payment/upiQr/echo/txnReversal")
                                .isEqualTo(200);

                String payableAmtSms = String.format(Locale.US, "%,d", payablePaise / 100);
                String displayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM, yyyy"));
                Response smsRes = new DealsShareInvoiceViaSms.Builder()
                                .baseUri(dealsBase)
                                .referer(dealsBase + "/pgmid/" + PG_MID + "/order/" + orderId + "?orderStatus=SUCCESS")
                                .globalTs(flowTs + 823)
                                .buildVersion(BUILD_VERSION)
                                .pgmid(PG_MID)
                                .orderId(orderId)
                                .phoneNo(PHONE)
                                .bankName("UPI")
                                .transactionAmount(payableAmtSms)
                                .transactionDate(displayDate)
                                .paymentMethod("UPI")
                                .merchantName(MERCHANT_DISPLAY_NAME)
                                .build()
                                .execute();
                Assertions.assertThat(smsRes.getStatusCode()).as("shareInvoiceViaSms").isEqualTo(200);

                Response invRes = new DealsEmiGetInvoice.Builder()
                                .baseUri(dealsBase)
                                .referer(dealsBase + "/pgmid/" + PG_MID + "/order/" + orderId + "?linkId=" + orderId)
                                .pgmid(PG_MID)
                                .merchantTransId(orderId)
                                .build()
                                .execute();
                Assertions.assertThat(invRes.getStatusCode()).as("getInvoice").isEqualTo(200);
                Assertions.assertThat(invRes.getBody().asString()).isNotBlank();

                String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
                Response hRes = new DealsPaymentTransactionHistory.Builder()
                                .baseUri(dealsBase)
                                .mid(PG_MID)
                                .pageNum(1)
                                .nextPageNum(1)
                                .size(10)
                                .startDate(today)
                                .endDate(today)
                                .referer(dealsBase + "/pgmid/" + PG_MID
                                                + "/emi/transaction-queue?prevScreen=store_page")
                                .build()
                                .execute();
                Assertions.assertThat(hRes.getStatusCode()).as("payment/transaction/history page ").isEqualTo(200);
                Assertions.assertThat(transactionHistoryResponseContainsOrderId(hRes, orderId))
                                .as("payment/transaction/history response contains orderId : ", orderId)
                                .isTrue();
        }
}
