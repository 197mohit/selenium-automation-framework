package scripts.api.PLE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.paytm.LocalConfig;
import com.paytm.api.PLE.DealsEmiGetInvoice;
import com.paytm.api.PLE.DealsPaymentCreateStdQr;
import com.paytm.api.PLE.DealsPaymentTransactionHistory;
import com.paytm.api.PLE.DealsShareInvoiceViaSms;
import com.paytm.api.PLE.DealsStoreDetails;
import com.paytm.api.UpiPspProcessor;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.UPIIntentRequestDTO;
import com.paytm.dto.upiIntent.staticQR.Response.StaticQrUpiPSPResponse;
import com.paytm.dto.upiIntent.staticQR.StaticQrUpiPSPRequest;
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
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * PLE dynamic / standard QR happy path:
 * {@code GET /api/store/pgmid/{mid}/details} →
 * POST /api/payment/createStdQr} → UPI PSP + intent acknowledge →share invoice
 * → get invoice → txn history (page * 1, size
 */
@Owner(Constants.Owner.CHAKSHU)
@Feature("PLE-UPI-DQR-Standard")
public class UpiDQRStandardPayment extends PGPBaseTest {

    private static final String PG_MID = "ALI00794701502328614";
    private static final String MERCHANT_DISPLAY_NAME = "Emi Store Testing";
    private static final String PHONE = "9898989898";
    /** Body {@code amount} for createStdQr (sample curl {@code "160"}). */
    private static final String STD_QR_AMOUNT = "200";
    private static final String PAYER_VPA = "9898989898@okicici";
    private static final String PAYEE_VPA = "paytm.ud123649907885@ptys";

    private static String refererStorePage(String dealsBase, String pgmid) {
        return dealsBase + "/pgmid/" + pgmid + "/store?prevScreen=store_page";
    }

    private static String refererCollectPayment(String dealsBase, String pgmid) {
        return dealsBase + "/pgmid/" + pgmid + "/collect-payment?isPaytmLovesEmiFlow=true&prevScreen=store_page";
    }

    /* PGP base URL for { UpiPspProcessor} + instaUrl} */
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
    @Feature("PLE-STD-QR-E2E")
    @Test(description = "PLE std QR ,createStdQr, UPI PSP , intent callback, shareInvoiceViaSms ,getInvoice, txn history")
    public void pleCollectPaymentCreateStdQrFlowEndToEnd() throws Exception {
        long flowTs = System.currentTimeMillis();
        String dealsBase = LocalConfig.PLE_DEALS_BASE_URL;

        // 1) GET /api/store/pgmid/{pgmid}/details
        DealsStoreDetails.Builder storeB = new DealsStoreDetails.Builder()
                .baseUri(dealsBase)
                .pgmid(PG_MID)
                .referer(refererStorePage(dealsBase, PG_MID));

        Response storeRes = storeB.build().execute();
        Assertions.assertThat(storeRes.getStatusCode()).as("/api/store/pgmid/*/details").isEqualTo(200);

        String collectRef = refererCollectPayment(dealsBase, PG_MID);

        // 2) POST /api/payment/createStdQr
        DealsPaymentCreateStdQr.Builder stdB = new DealsPaymentCreateStdQr.Builder()
                .baseUri(dealsBase)
                .referer(collectRef)
                .globalTs(flowTs + 211)
                .amount(STD_QR_AMOUNT)
                .mid(PG_MID)
                .additionalMerchantName(MERCHANT_DISPLAY_NAME)
                .additionalPaytmMerchantId(PG_MID)
                .description("UPI Payment");

        Response stdQrRes = stdB.build().execute();
        Assertions.assertThat(stdQrRes.getStatusCode()).as("/api/payment/createStdQr").isEqualTo(200);

        JsonPath stdJp = stdQrRes.jsonPath();
        // Get qrData string
        String qrData = stdJp.getString("body.qrData");
        // Extract tr value (orderId)
        String orderId = qrData.split("tr=")[1].split("&")[0];

        String displayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM, yyyy"));
        String txAmountDisplay = STD_QR_AMOUNT;

        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(PG_MID, orderId, STD_QR_AMOUNT,
                PAYEE_VPA, PAYER_VPA, "", "");
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "", orderId);
        UpiPspProcessor upiPspProcessor = upiPspProcessorForPle(staticQrUpiPSPRequest);
        Response response = upiPspProcessor.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.then()
                .statusCode(200)
                .body("body.resultCode", Matchers.equalToIgnoringCase("SUCCESS"))
                .body("body.resultCodeId", Matchers.equalToIgnoringCase("001"))
                .body("body.resultMsg", Matchers.equalToIgnoringCase("success"))
                .body("body.mid", Matchers.equalTo(PG_MID))
                .extract().as(StaticQrUpiPSPResponse.class);

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(STD_QR_AMOUNT)
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

        Thread.sleep(5000);

        // 3) POST /api/emi/shareInvoiceViaSms
        Response smsRes = new DealsShareInvoiceViaSms.Builder()
                .baseUri(dealsBase)
                .referer(dealsBase + "/pgmid/" + PG_MID + "/order/" + orderId + "?orderStatus=SUCCESS")
                .globalTs(flowTs + 413)
                .pgmid(PG_MID)
                .orderId(orderId)
                .phoneNo(PHONE)
                .bankName("UPI")
                .transactionAmount(txAmountDisplay)
                .transactionDate(displayDate)
                .paymentMethod("UPI")
                .merchantName(MERCHANT_DISPLAY_NAME)
                .build()
                .execute();
        Assertions.assertThat(smsRes.getStatusCode()).as("shareInvoiceViaSms").isEqualTo(200);
        JsonPath smsJp = smsRes.jsonPath();
        String smsStatus = smsJp.getString("status");
        Assertions.assertThat(smsStatus).isEqualTo("SUCCESS");

        // 4) GET /api/emi/pgmid/{pgmid}/getInvoice/{merchantTransId} — merchantTransId
        // same as Deals order id
        DealsEmiGetInvoice.Builder invB = new DealsEmiGetInvoice.Builder()
                .baseUri(dealsBase)
                .pgmid(PG_MID)
                .merchantTransId(orderId)
                .referer(dealsBase + "/pgmid/" + PG_MID + "/order/" + orderId + "?orderStatus=SUCCESS");

        Response invRes = invB.build().execute();
        Assertions.assertThat(invRes.getStatusCode()).as("getInvoice").isEqualTo(200);
        Assertions.assertThat(invRes.getBody().asString()).as("getInvoice body").isNotBlank();
        JsonPath invResJp = invRes.jsonPath();

        String resultStatus = invResJp.getString("body.resultStatus");
        String resultMsg = invResJp.getString("body.resultMsg");
        Assertions.assertThat(resultStatus).isEqualTo("SUCCESS");
        Assertions.assertThat(resultMsg).isEqualTo("Success");

        // 5) GET /api/payment/transaction/history — today page 1
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        DealsPaymentTransactionHistory.Builder histB = new DealsPaymentTransactionHistory.Builder()
                .baseUri(dealsBase)
                .mid(PG_MID)
                .pageNum(1)
                .nextPageNum(1)
                .size(10)
                .startDate(today)
                .endDate(today)
                .referer(dealsBase + "/pgmid/" + PG_MID + "/emi/transaction-queue?prevScreen=store_page");
        Response hRes = histB.build().execute();
        Assertions.assertThat(hRes.getStatusCode()).as("payment/transaction/history page 1").isEqualTo(200);
        Assertions.assertThat(transactionHistoryResponseContainsOrderId(hRes, orderId))
                .as("transaction history lists orderId %s", orderId)
                .isTrue();
    }
}
