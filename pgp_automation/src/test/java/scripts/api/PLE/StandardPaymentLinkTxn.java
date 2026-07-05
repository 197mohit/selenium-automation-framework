package scripts.api.PLE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.paytm.LocalConfig;
import com.paytm.api.PLE.DealsEmiGetInvoice;
import com.paytm.api.PLE.DealsEmiOrderSummary;
import com.paytm.api.PLE.DealsMerchantDetails;
import com.paytm.api.PLE.DealsShareInvoiceViaSms;
import com.paytm.api.PLE.DealsStdLinkCreate;
import com.paytm.api.PLE.DealsPaymentTransactionHistory;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.LinkPaymentLoginPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.RestAssured;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.Header;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import java.util.List;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Locale;
import java.util.UUID;

/**
 * PLE standard payment link E2E: createLink, open payment URL, payBy,
 * orderSummary, shareInvoiceSms, getInvoice
 */
@Owner(Constants.Owner.CHAKSHU)
@Feature("PLE-Standard-Payment-Link-E2E")
public class StandardPaymentLinkTxn extends PGPBaseTest {

        private static final Logger log = LoggerFactory.getLogger(StandardPaymentLinkTxn.class);

        private static final String MERCHANT_DISPLAY_NAME = "Emi Store Testing";
        private static final String CASHIER_PHONE = "9898989898";
        /** {@code STD_LINK} body {@code price} (matches sample curl rupees amount). */
        private static final String LINK_PRICE_RUPEES = "200";
        /** Full-swipe Visa (non-EMI) for STD link CC. */
        private static final String STD_LINK_CREDIT_CARD_NUMBER = PaymentDTO.VISA_COFT_CARD_NUMBER;
        /** Test debit PAN for STD link DC */
        private static final String STD_LINK_DEBIT_CARD_NUMBER = PaymentDTO.ICICI_DEBIT_CARD_NUMBER;
        private static final String ISSUING_BANK_HDFC = "HDFC";
        private static final String Link_Description = "PLE Standard Payment Link";

        private static final Filter PLE_CURL_LOGGER = new Filter() {
                @Override
                public Response filter(
                                FilterableRequestSpecification req,
                                FilterableResponseSpecification res,
                                FilterContext ctx) {
                        if (log.isDebugEnabled()) {
                                StringBuilder curl = new StringBuilder("curl --compressed");
                                String method = req.getMethod();
                                if (!"GET".equalsIgnoreCase(method)) {
                                        curl.append(" -X ").append(method);
                                }
                                for (Header h : req.getHeaders()) {
                                        curl.append(" \\\n  -H '")
                                                        .append(h.getName())
                                                        .append(": ")
                                                        .append(h.getValue())
                                                        .append("'");
                                }
                                String body = req.getBody() != null ? req.getBody().toString() : null;
                                if (body != null && !body.isEmpty()) {
                                        curl.append(" \\\n  --data '").append(body).append("'");
                                }
                                curl.append(" \\\n  '").append(req.getURI()).append("'");
                                log.debug("[CURL ↗]\n{}", curl);
                        }
                        return ctx.next(req, res);
                }
        };

        private static String refererCollectPayment(String dealsBase, String pgmid) {
                return dealsBase + "/pgmid/" + pgmid
                                + "/collect-payment?isPaytmLovesEmiFlow=true&prevScreen=store_page";
        }

        private boolean transactionHistoryResponseContainsOrderId(Response response, String orderId) {
                List<String> merchantTransIds = response.jsonPath()
                                .getList("orderList.merchantTransId", String.class);

                List<String> pleIds = response.jsonPath()
                                .getList("orderList.pleId", String.class);

                return (merchantTransIds != null && merchantTransIds.contains(orderId)) ||
                                (pleIds != null && pleIds.contains(orderId));
        }

        /**
         * Shared STD link lifecycle: Deals APIs → {
         * CashierPage#payBy(Constants.PayMode, PaymentDTO)} → post‑txn invoice APIs.
         */
        private void executePleStdLinkInstrumentE2e(
                        String theme,
                        Constants.PayMode checkoutPayMode,
                        PaymentDTO checkoutInstrumentDto,
                        String ISSUING_BANK_HDFC)
                        throws Exception {
                RestAssured.filters(PLE_CURL_LOGGER);
                try {
                        String pgMid = MerchantType.PLE_DEALS_MID.getId();
                        String dealsBase = LocalConfig.PLE_DEALS_BASE_URL;
                        long flowTs = System.currentTimeMillis();

                        String refererCp = refererCollectPayment(dealsBase, pgMid);

                        Response merchantRes = new DealsMerchantDetails.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .referer(refererCp)
                                        .build()
                                        .execute();
                        Assertions.assertThat(merchantRes.getStatusCode()).as("/api/merchant/details").isEqualTo(200);

                        String invoiceNumber = "INV-" + UUID.randomUUID().toString().substring(0, 10);

                        DealsStdLinkCreate.Builder stdLinkB = new DealsStdLinkCreate.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .globalTs(flowTs + 211)
                                        .referer(refererCp)
                                        .price(LINK_PRICE_RUPEES)
                                        .mobileNumber(CASHIER_PHONE)
                                        .linkDescription(Link_Description)
                                        .invoiceNumber(invoiceNumber)
                                        .cashierMobileNumber(CASHIER_PHONE)
                                        .description(Link_Description);

                        Response createLinkRes = stdLinkB.build().execute();
                        Assertions.assertThat(createLinkRes.getStatusCode())
                                        .as("/api/link/pgmid/*/createLink flowType="
                                                        + DealsStdLinkCreate.FLOW_TYPE_STD_LINK)
                                        .isEqualTo(200);

                        JsonPath clJp = createLinkRes.jsonPath();
                        String paymentUrl = clJp.getString("shortUrl");
                        String linkId = clJp.getString("linkId");

                        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
                        linkPaymentLoginPage.OpenEdcLinkNew(paymentUrl);
                        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
                        cashierPage.waitUntilLoads();
                        cashierPage.refresh();
                        cashierPage.payBy(checkoutPayMode, checkoutInstrumentDto);

                        Thread.sleep(15000);

                        DealsEmiOrderSummary.Builder orderSumB = new DealsEmiOrderSummary.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .orderId(linkId)
                                        .flowType(DealsStdLinkCreate.FLOW_TYPE_STD_LINK)
                                        .referer(dealsBase + "/pgmid/" + pgMid + "/order/" + linkId
                                                        + "?isPaytmLovesEmiFlow=true");

                        Response orderSummaryRes = orderSumB.build().execute();
                        Assertions.assertThat(orderSummaryRes.getStatusCode()).as("/api/emi/pgmid/*/orderSummary/*")
                                        .isEqualTo(200);

                        JsonPath osJp = orderSummaryRes.jsonPath();
                        String orderId = osJp.getString("successDetails.orderId");
                        String displayAmount = osJp.getString("successDetails.txnAmount");
                        String displayDate = osJp.getString("successDetails.time");
                        String merchantTransId = orderId;
                        ;

                        DealsShareInvoiceViaSms.Builder smsB = new DealsShareInvoiceViaSms.Builder()
                                        .baseUri(dealsBase)
                                        .referer(dealsBase + "/pgmid/" + pgMid + "/order/" + linkId
                                                        + "?orderStatus=SUCCESS")
                                        .pgmid(pgMid)
                                        .orderId(orderId)
                                        .phoneNo(CASHIER_PHONE)
                                        .bankName(ISSUING_BANK_HDFC + " Bank")
                                        .transactionAmount(LINK_PRICE_RUPEES)
                                        .transactionDate(displayDate)
                                        .paymentMethod("LINK")
                                        .merchantName(MERCHANT_DISPLAY_NAME)
                                        .utmSource("login-via-username")
                                        .globalTs(flowTs + 413);

                        Response smsRes = smsB.build().execute();
                        Assertions.assertThat(smsRes.getStatusCode()).as("shareInvoiceViaSms").isEqualTo(200);
                        JsonPath smsJp = smsRes.jsonPath();
                        String smsStatus = smsJp.getString("status");
                        Assertions.assertThat(smsStatus).isEqualTo("SUCCESS");

                        DealsEmiGetInvoice.Builder invB = new DealsEmiGetInvoice.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .merchantTransId(merchantTransId)
                                        .referer(dealsBase + "/pgmid/" + pgMid + "/order/" + linkId + "?linkId="
                                                        + linkId);

                        Response invRes = invB.build().execute();
                        JsonPath invResJp = invRes.jsonPath();

                        Assertions.assertThat(invRes.getStatusCode()).as("getInvoice").isEqualTo(200);
                        String resultStatus = invResJp.getString("body.resultStatus");
                        String resultMsg = invResJp.getString("body.resultMsg");
                        Assertions.assertThat(resultStatus).isEqualTo("SUCCESS");
                        Assertions.assertThat(resultMsg).isEqualTo("Success");

                        // 12) Transaction history — today, page 1 only, page size 10
                        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
                        Response hRes = new DealsPaymentTransactionHistory.Builder()
                                        .baseUri(dealsBase)
                                        .mid(pgMid)
                                        .pageNum(1)
                                        .nextPageNum(1)
                                        .size(10)
                                        .startDate(today)
                                        .endDate(today)
                                        .referer(dealsBase + "/pgmid/" + pgMid
                                                        + "/emi/transaction-queue?prevScreen=store_page")
                                        .build()
                                        .execute();
                        Assertions.assertThat(hRes.getStatusCode()).as("payment/transaction/history page ")
                                        .isEqualTo(200);
                        Assertions.assertThat(transactionHistoryResponseContainsOrderId(hRes, orderId))
                                        .as("payment/transaction/history response contains orderId : ", orderId)
                                        .isTrue();
                } finally {
                        RestAssured.replaceFiltersWith(Collections.emptyList());
                }
        }

        @Parameters({ "theme" })
        @Test(description = "PLE standard link E2E, STD_LINK, createLink, checkout CC , orderSummary, shareInvoiceSms, getInvoice")
        public void pleStandardPaymentLinkCreditCardlow(@Optional("checkoutjs_web_revamp_2") String theme)
                        throws Exception {
                PaymentDTO dto = new PaymentDTO().setCreditCardNumber(STD_LINK_CREDIT_CARD_NUMBER);
                executePleStdLinkInstrumentE2e(theme, Constants.PayMode.CC, dto, ISSUING_BANK_HDFC);
        }

        @Parameters({ "theme" })
        @Test(description = "PLE standard link E2E, STD_LINK, createLink, checkout EMI , orderSummary, shareInvoiceSms, getInvoice, txn history")
        public void pleStandardPaymentLinkDebitCardFlow(@Optional("checkoutjs_web_revamp_2") String theme)
                        throws Exception {
                PaymentDTO dto = new PaymentDTO().setDebitCardNumber(STD_LINK_DEBIT_CARD_NUMBER);
                executePleStdLinkInstrumentE2e(theme, Constants.PayMode.DC, dto, ISSUING_BANK_HDFC);
        }

        @Parameters({ "theme" })
        @Test(description = "PLE standard link E2E, STD_LINK, createLink, checkout EMI , orderSummary, shareInvoiceSms, getInvoice")
        public void pleStandardPaymentLinkEMIFlow(@Optional("checkoutjs_web_revamp_2") String theme) throws Exception {
                PaymentDTO dto = new PaymentDTO().setCreditCardNumber(STD_LINK_CREDIT_CARD_NUMBER);
                executePleStdLinkInstrumentE2e(theme, Constants.PayMode.EMI, dto, ISSUING_BANK_HDFC);
        }
}
