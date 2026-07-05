package scripts.BOBEncryption;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.PayMode;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.encryptdecrypt.Aes256EncryptionDecryption;
import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.reporting.Owners;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.reporting.filters.RequestResponseLoggingFilter;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.peon.Peon;
import com.paytm.utils.merchant.peon.Peons;
import com.paytm.utils.merchant.util.PGPUtil;
import com.paytm.utils.merchant.util.exception.pgpException.PGPException;
import io.qameta.allure.Epic;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.qameta.allure.Step;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.annotations.Optional;
import org.testng.annotations.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.paytm.LocalConfig.PGP_HOST;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * NOTES FOR ENCRYPTION REQUESTS
 * property should be:
 * csrf.validation.enabled=0 in projec-theia.properties
 * YyOth167411827868949.ENC.DEC.KEY=YyOth167411827868949.256bit.enc.dec.key in encryption-flow-merchant.properties
 * YyOth167411827868949.CKSUM.KEY=YyOth167411827868949.256bit.cksumkey in encryption-flow-merchant.properties
 * PEON_SERVICE_NAME=AES256PeonSentServiceImpl in merchant_extended_info table
 * AES256_ENC_PARAMS_ENABLED=Y in merchant_preference_info table
 * /mockbank/encpeon will be used for encrypted peon request
 */
@Owner("Tarun")
@Owners(author = "Tarun", qa = "Ankur")
public class BOBEncryptionTests extends PGPBaseTest {

    private final ThreadLocal<Map<String, String>> merchantKeyResp = new ThreadLocal<>();
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private static final Map<String, OrderDTO> orderDTOMap = new HashMap<>();
    private static final Map<String, TxnStatus> txnStatusMap = new HashMap<>();

    @Parameters("theme")
    @Test(description = "Successful transaction using wallet through BOB merchant", priority = 1)
    public void t1(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BOB_ENCRYPTED;
        OrderDTO orderDTO = new OrderFactory.WalletOnly(merchantType, theme)
                .setMerchantKey("")
                .setTXN_AMOUNT("5.00")
                .setSSO_TOKEN(user.ssoToken())
                .build();

        orderDTOMap.put("t1", orderDTO);

        WalletHelpers.modifyBalance(user, 5.00);
        String encrytedText = generateEncryptedRequest(orderDTO);

        OrderDTO encyrptedOrder = new OrderDTO.Builder()
                .setMID(merchantType.getId())
                .setENC_DATA(encrytedText)
                .setORDER_ID(orderDTO.getORDER_ID())
                .build();

        checkoutPage.createEncryptedOrder(encyrptedOrder);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.WALLET);

        TreeMap<String, String> t;
        SoftAssertions softly;

        responsePage:
        {
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateMid(encyrptedOrder.getMID())
                    .validateENC_DATA(Constants.ValidationType.NON_EMPTY)
                    .assertAll();
            String encryptedResponse = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.ENC_DATA)
                    .replace(" ", "+");
            t = getDecryptedResponse(encryptedResponse);
            softly = new SoftAssertions();
            softly.assertThat(t.get("CURRENCY")).isEqualTo("INR");
            softly.assertThat(t.get("CUST_ID")).isEqualTo(orderDTO.getCUST_ID());
            softly.assertThat(t.get("GATEWAYNAME")).isEqualTo("WALLET");
            softly.assertThat(t.get("RESPMSG")).isEqualTo("Txn Success");
            softly.assertThat(t.get("MID")).isEqualTo(orderDTO.getMID());
            softly.assertThat(t.get("RESPCODE")).isEqualTo("01");
            softly.assertThat(t.get("TXNID")).isNotEmpty();
            softly.assertThat(t.get("TXNAMOUNT")).isEqualTo(orderDTO.getTXN_AMOUNT());
            softly.assertThat(t.get("ORDERID")).isEqualTo(orderDTO.getORDER_ID());
            softly.assertThat(t.get("STATUS")).isEqualTo("TXN_SUCCESS");
            softly.assertThat(t.get("BANKTXNID")).isNotEmpty();
            softly.assertThat(t.get("TXNDATE")).isNotEmpty();
            softly.assertThat(t.get("CHECKSUMHASH")).isNotEmpty();
            String checksumHash = t.get("CHECKSUMHASH");
            t.remove("CHECKSUMHASH");
            softly.assertThat(PGPUtil.isChecksumValid(merchantKeyResp.get().get("checksum_key"), t, checksumHash)).as("CHECKSUMHASH mismatch").isTrue();
            softly.assertAll();
        }
        Reporter.report.info("Decrypted Response: " + t.toString());
    }

    @Test(description = "Validate txn status for transaction using wallet through BOB merchant", dependsOnMethods = "t1")
    public void t1_1() {
        OrderDTO orderDTO = orderDTOMap.get("t1");
        TreeMap<String, String> t;
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID(), merchantKeyResp.get().get("checksum_key"), true)
                .executeEncrypted(merchantKeyResp.get().get("encrypt_key"))
                .validateBankName("WALLET")
                .validateGatewayName("WALLET")
                .validateMid(orderDTO.getMID())
                .validateOrderid(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        txnStatusMap.put("t1", txnStatus);
        t = getDecryptedResponse(txnStatus.getApiResponse().jsonPath().getString("encParams"));
        String checksumHash = t.get("CHECKSUMHASH");
        t.remove("CHECKSUMHASH");
        Assertions.assertThat(PGPUtil.isChecksumValid(merchantKeyResp.get().get("checksum_key"), t, checksumHash)).as("CHECKSUMHASH mismatch").isTrue();
    }

    /**
     * Possible failures is mockPeon is not able to fetch merchantKey from BO panel, so peon can not be saved
     */
    @Test(description = "Verify peon for transaction using wallet through BOB merchant", dependsOnMethods = "t1")
    public void t1_2() {
        OrderDTO orderDTO = orderDTOMap.get("t1");
        Peons plist = new Peons(orderDTO.getORDER_ID());
        com.paytm.utils.merchant.peon.Peon p = plist.getAt("encpeon").get();
        p.response().then()
                .body("", Matchers.allOf(hasKey("CURRENCY"),
                        hasKey("GATEWAYNAME"),
                        hasKey("RESPMSG"),
                        hasKey("BANKNAME"),
                        hasKey("PAYMENTMODE"),
                        hasKey("CUSTID"),
                        hasKey("MID"),
                        hasKey("RESPCODE"),
                        hasKey("TXNID"),
                        hasKey("TXNAMOUNT"),
                        hasKey("ORDERID"),
                        hasKey("STATUS"),
                        hasKey("BANKTXNID"),
                        hasKey("TXNDATE"),
                        hasKey("CHECKSUMHASH")))
                .body("CURRENCY", equalToIgnoringCase("INR"),
                        "GATEWAYNAME", equalToIgnoringCase("WALLET"),
                        "RESPMSG", equalToIgnoringCase("Txn Success"),
                        "BANKNAME", equalToIgnoringCase("WALLET"),
                        "PAYMENTMODE", equalToIgnoringCase("PPI"),
                        "CUSTID", not(empty()),
                        "MID", equalToIgnoringCase(orderDTO.getMID()),
                        "RESPCODE", equalToIgnoringCase("01"),
                        "TXNID", not(empty()),
                        "TXNAMOUNT", equalToIgnoringCase(orderDTO.getTXN_AMOUNT()),
                        "ORDERID", equalToIgnoringCase(orderDTO.getORDER_ID()),
                        "STATUS", equalToIgnoringCase("TXN_SUCCESS"),
                        "BANKTXNID", not(empty()),
                        "TXNDATE", not(empty())
                );

        //TODO: getting checksum false as discussed with Srishti raised JIRA: PGP-19965
//        validateChecksum: {
//            String checksumHash = p.map().get("CHECKSUMHASH").toString();
//            TreeMap<String, String> t = new TreeMap<>();
//            t.putAll(p.map());
//            t.remove("CHECKSUMHASH");
//            Assertions.assertThat(PGPUtil.isChecksumValid(merchantKeyResp.get().get("checksum_key"), t, checksumHash))
//                    .as("Checksum invalid").isTrue();
//        }


        plist = new Peons(orderDTO.getORDER_ID() + "_enc");
        plist.getAt("encpeon").get().response()
                .then()
                .body("", allOf(hasKey("MID"),
                        hasKey("ENC_DATA")))
                .body("MID", equalToIgnoringCase(orderDTO.getMID()),
                        "ENC_DATA", not(empty()));
    }

    @Test(description = "Verify refund for transaction using wallet through BOB merchant", dependsOnMethods = {"t1", "t1_1"})
    public void t1_3() {
        TxnStatus txnStatus = txnStatusMap.get("t1");
        Response r = PGPHelpers.executeMasterRefund(txnStatus.txnStatusResponse.MID, merchantKeyResp.get().get("checksum_key"),
                txnStatus.txnStatusResponse.getORDERID(), txnStatus.txnStatusResponse.getORDERID(),
                txnStatus.txnStatusResponse.getTXNAMOUNT().replace(".00", ""),
                txnStatus.txnStatusResponse.TXNID, "")
                .then()
                .body("", allOf(hasKey("mid"), hasKey("encParams")))
                .body("mid", equalToIgnoringCase(txnStatus.txnStatusResponse.MID))
                .extract().response();

        TreeMap<String, String> t = getDecryptedResponse(r.jsonPath().getString("encParams"));
        Reporter.report.info("Refund api response: " + t);
        System.out.println(t);
        SoftAssertions s = new SoftAssertions();

        s.assertThat(t.get("CHECKSUM")).as("CHECKSUM not available").isNotEmpty();
        s.assertThat(t.get("MID")).as("MID mismatch").isEqualTo(txnStatus.txnStatusResponse.MID);
        s.assertThat(t.get("ORDERID")).as("ORDERID mismatch").isEqualTo(txnStatus.txnStatusResponse.getORDERID());
        s.assertThat(t.get("REFID")).as("REFID mismatch").isEqualTo(txnStatus.txnStatusResponse.getORDERID());
        s.assertThat(t.get("REFUNDAMOUNT")).as("REFUNDAMOUNT mismatch").isEqualTo(txnStatus.txnStatusResponse.TXNAMOUNT);
        s.assertThat(t.get("RESPCODE")).as("RESPCODE mismatch").isEqualTo("10");
        s.assertThat(t.get("RESPMSG")).as("RESPMSG mismatch").isEqualTo("Refund Successfull");
        s.assertThat(t.get("STATUS")).as("STATUS mismatch").isEqualTo("TXN_SUCCESS");
        s.assertThat(t.get("TXNAMOUNT")).as("TXNAMOUNT mismatch").isEqualTo(txnStatus.txnStatusResponse.TXNAMOUNT);
        s.assertThat(t.get("TXNDATE")).as("TXNDATE mismatch").isNotEmpty();
        s.assertThat(t.get("TXNID")).as("TXNID mismatch").isEqualTo(txnStatus.txnStatusResponse.TXNID);
        s.assertAll();

        t = new TreeMap<>();
        t.put("MID", txnStatus.txnStatusResponse.MID);
        t.put("REFID", txnStatus.txnStatusResponse.getORDERID());

        String checksum = PGPUtil.getChecksum(merchantKeyResp.get().get("checksum_key"), t);
        t.put("CHECKSUMHASH", checksum);
        JSONObject obj = new JSONObject(t);

        r = given().spec(reqSpec1)
                .basePath("/refund/HANDLER_INTERNAL/getMasterRefundStatus")
                .body(obj.toString())
                .post()
                .then()
                .body("", allOf(hasKey("mid"), hasKey("encParams")))
                .body("mid", equalToIgnoringCase(txnStatus.txnStatusResponse.MID))
                .extract().response();
        s = new SoftAssertions();
        t = getDecryptedResponse(r.jsonPath().getString("encParams"));

        s.assertThat(t.get("CHECKSUM")).as("CHECKSUM not available").isNotEmpty();
        s.assertThat(t.get("MID")).as("MID mismatch").isEqualTo(txnStatus.txnStatusResponse.MID);
        s.assertThat(t.get("ORDERID")).as("ORDERID mismatch").isEqualTo(txnStatus.txnStatusResponse.getORDERID());
        s.assertThat(t.get("REFID")).as("REFID mismatch").isEqualTo(txnStatus.txnStatusResponse.getORDERID());
        s.assertThat(t.get("REFUNDAMOUNT")).as("REFUNDAMOUNT mismatch").isEqualTo(txnStatus.txnStatusResponse.TXNAMOUNT);
        s.assertThat(t.get("RESPCODE")).as("RESPCODE mismatch").isEqualTo("10");
        s.assertThat(t.get("RESPMSG")).as("RESPMSG mismatch").isEqualTo("Refund Successfull");
        s.assertThat(t.get("STATUS")).as("STATUS mismatch").isEqualTo("TXN_SUCCESS");
        s.assertThat(t.get("TXNAMOUNT")).as("TXNAMOUNT mismatch").isEqualTo(txnStatus.txnStatusResponse.TXNAMOUNT);
        s.assertThat(t.get("TXNDATE")).as("TXNDATE mismatch").isNotEmpty();
        s.assertThat(t.get("TXNID")).as("TXNID mismatch").isEqualTo(txnStatus.txnStatusResponse.TXNID);
        s.assertAll();
    }

    @Parameters("theme")
    @Test(description = "Successful transaction using CC through BOB merchant", priority = 1)
    public void t2(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.BOB_ENCRYPTED;
        OrderDTO orderDTO = new OrderFactory.WalletOnly(merchantType, theme)
                .setMerchantKey("")
                .setTXN_AMOUNT("2.00")
                .build();
        orderDTOMap.put("t2", orderDTO);

        PGPHelpers.getMerchantKey(merchantType.getId(), "checksum_key");

        String encrytedText = generateEncryptedRequest(orderDTO);

        OrderDTO encyrptedOrder = new OrderDTO.Builder()
                .setMID(merchantType.getId())
                .setENC_DATA(encrytedText)
                .build();

        checkoutPage.createEncryptedOrder(encyrptedOrder);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO= new PaymentDTO();
        cashierPage.payBy(PayMode.CC, paymentDTO.setCreditCardNumber(paymentDTO.ICICI_CREDIT_CARD_NUMBER));

        TreeMap<String, String> t;
        SoftAssertions softly;

        responsePage:
        {
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateMid(encyrptedOrder.getMID())
                    .validateENC_DATA(Constants.ValidationType.NON_EMPTY)
                    .assertAll();
            String encryptedResponse = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.ENC_DATA)
                    .replace(" ", "+");
            t = getDecryptedResponse(encryptedResponse);
            softly = new SoftAssertions();
            softly.assertThat(t.get("CURRENCY")).isEqualTo("INR");
            softly.assertThat(t.get("PAYMENTMODE")).isEqualTo("CC");
            softly.assertThat(t.get("CUST_ID")).isEqualTo(orderDTO.getCUST_ID());
            softly.assertThat(t.get("GATEWAYNAME")).isEqualTo("HDFC");
            softly.assertThat(t.get("RESPMSG")).isEqualTo("Txn Success");
            softly.assertThat(t.get("MID")).isEqualTo(orderDTO.getMID());
            softly.assertThat(t.get("RESPCODE")).isEqualTo("01");
            softly.assertThat(t.get("TXNID")).isNotEmpty();
            softly.assertThat(t.get("TXNAMOUNT")).isEqualTo(orderDTO.getTXN_AMOUNT());
            softly.assertThat(t.get("ORDERID")).isEqualTo(orderDTO.getORDER_ID());
            softly.assertThat(t.get("STATUS")).isEqualTo("TXN_SUCCESS");
            softly.assertThat(t.get("BANKTXNID")).isNotEmpty();
            softly.assertThat(t.get("TXNDATE")).isNotEmpty();
            softly.assertThat(t.get("CHECKSUMHASH")).isNotEmpty();
            String checksumHash = t.get("CHECKSUMHASH");
            t.remove("CHECKSUMHASH");
            softly.assertThat(PGPUtil.isChecksumValid(merchantKeyResp.get().get("checksum_key"), t, checksumHash)).as("CHECKSUMHASH mismatch").isTrue();
            softly.assertAll();
        }
        Reporter.report.info("Decrypted Response: " + t.toString());
    }

    @Parameters("orderDTO")
    @Test(description = "Validate txn status for transaction using CC through BOB merchant", dependsOnMethods = "t2")
    public void t2_1() {
        OrderDTO orderDTO = orderDTOMap.get("t2");
        TreeMap<String, String> t;
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID(), merchantKeyResp.get().get("checksum_key"), true)
                .executeEncrypted(merchantKeyResp.get().get("encrypt_key"))
                .validateBankName("ICICI Bank")
                .validateGatewayName("HDFC")
                .validateMid(orderDTO.getMID())
                .validateOrderid(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        txnStatusMap.put("t2", txnStatus);

        t = getDecryptedResponse(txnStatus.getApiResponse().jsonPath().getString("encParams"));
        String checksumHash = t.get("CHECKSUMHASH");
        t.remove("CHECKSUMHASH");
        Assertions.assertThat(PGPUtil.isChecksumValid(merchantKeyResp.get().get("checksum_key"), t, checksumHash)).as("CHECKSUMHASH mismatch").isTrue();
    }

    @Test(description = "Verify peon for transaction using CC through BOB merchant", dependsOnMethods = "t2")
    public void t2_2() {
        OrderDTO orderDTO = orderDTOMap.get("t2");
        Peons plist = new Peons(orderDTO.getORDER_ID());
        com.paytm.utils.merchant.peon.Peon p = plist.getAt("encpeon").get();
        p.response().then()
                .body("", Matchers.allOf(hasKey("CURRENCY"),
                        hasKey("GATEWAYNAME"),
                        hasKey("RESPMSG"),
                        hasKey("BANKNAME"),
                        hasKey("PAYMENTMODE"),
                        hasKey("CUSTID"),
                        hasKey("MID"),
                        hasKey("RESPCODE"),
                        hasKey("TXNID"),
                        hasKey("TXNAMOUNT"),
                        hasKey("ORDERID"),
                        hasKey("STATUS"),
                        hasKey("BANKTXNID"),
                        hasKey("TXNDATE"),
                        hasKey("CHECKSUMHASH")))
                .body("CURRENCY", equalToIgnoringCase("INR"),
                        "GATEWAYNAME", equalToIgnoringCase("HDFC"),
                        "RESPMSG", equalToIgnoringCase("Txn Success"),
                        "BANKNAME", equalToIgnoringCase("ICICI Bank"),
                        "PAYMENTMODE", equalToIgnoringCase("CC"),
                        "CUSTID", not(empty()),
                        "MID", equalToIgnoringCase(orderDTO.getMID()),
                        "RESPCODE", equalToIgnoringCase("01"),
                        "TXNID", not(empty()),
                        "TXNAMOUNT", equalToIgnoringCase(orderDTO.getTXN_AMOUNT()),
                        "ORDERID", equalToIgnoringCase(orderDTO.getORDER_ID()),
                        "STATUS", equalToIgnoringCase("TXN_SUCCESS"),
                        "BANKTXNID", not(empty()),
                        "TXNDATE", not(empty())
                );

        //TODO: getting checksum false as discussed with Srishti raised JIRA: PGP-19965
//        validateChecksum: {
//            String checksumHash = p.map().get("CHECKSUMHASH").toString();
//            TreeMap<String, String> t = new TreeMap<>();
//            t.putAll(p.map());
//            t.remove("CHECKSUMHASH");
//            Assertions.assertThat(PGPUtil.isChecksumValid(merchantKeyResp.get().get("checksum_key"), t, checksumHash))
//                    .as("Checksum invalid").isTrue();
//        }


        plist = new Peons(orderDTO.getORDER_ID() + "_enc");
        plist.getAt("encpeon").get().response()
                .then()
                .body("", allOf(hasKey("MID"),
                        hasKey("ENC_DATA")))
                .body("MID", equalToIgnoringCase(orderDTO.getMID()),
                        "ENC_DATA", not(empty()));
    }

    @Test(description = "Verify refund for transaction using CC through BOB merchant", dependsOnMethods = {"t2", "t2_1"})
    public void t2_3() {
        TxnStatus txnStatus = txnStatusMap.get("t2");
        Response r = PGPHelpers.executeMasterRefund(txnStatus.txnStatusResponse.MID, merchantKeyResp.get().get("checksum_key"),
                txnStatus.txnStatusResponse.getORDERID(), txnStatus.txnStatusResponse.getORDERID(),
                txnStatus.txnStatusResponse.getTXNAMOUNT().replace(".00", ""),
                txnStatus.txnStatusResponse.TXNID, "")
                .then()
                .body("", allOf(hasKey("mid"), hasKey("encParams")))
                .body("mid", equalToIgnoringCase(txnStatus.txnStatusResponse.MID))
                .extract().response();

        TreeMap<String, String> t = getDecryptedResponse(r.jsonPath().getString("encParams"));
        Reporter.report.info("Refund api response: " + t);
        System.out.println(t);
        SoftAssertions s = new SoftAssertions();

        s.assertThat(t.get("CHECKSUM")).as("CHECKSUM not available").isNotEmpty();
        s.assertThat(t.get("MID")).as("MID mismatch").isEqualTo(txnStatus.txnStatusResponse.MID);
        s.assertThat(t.get("ORDERID")).as("ORDERID mismatch").isEqualTo(txnStatus.txnStatusResponse.getORDERID());
        s.assertThat(t.get("REFID")).as("REFID mismatch").isEqualTo(txnStatus.txnStatusResponse.getORDERID());
        s.assertThat(t.get("REFUNDAMOUNT")).as("REFUNDAMOUNT mismatch").isEqualTo(txnStatus.txnStatusResponse.TXNAMOUNT);
        s.assertThat(t.get("RESPCODE")).as("RESPCODE mismatch").isEqualTo("10");
        s.assertThat(t.get("RESPMSG")).as("RESPMSG mismatch").isEqualTo("Refund Successfull");
        s.assertThat(t.get("STATUS")).as("STATUS mismatch").isEqualTo("TXN_SUCCESS");
        s.assertThat(t.get("TXNAMOUNT")).as("TXNAMOUNT mismatch").isEqualTo(txnStatus.txnStatusResponse.TXNAMOUNT);
        s.assertThat(t.get("TXNDATE")).as("TXNDATE mismatch").isNotEmpty();
        s.assertThat(t.get("TXNID")).as("TXNID mismatch").isEqualTo(txnStatus.txnStatusResponse.TXNID);
        s.assertAll();

        t = new TreeMap<>();
        t.put("MID", txnStatus.txnStatusResponse.MID);
        t.put("REFID", txnStatus.txnStatusResponse.getORDERID());

        String checksum = PGPUtil.getChecksum(merchantKeyResp.get().get("checksum_key"), t);
        t.put("CHECKSUMHASH", checksum);
        JSONObject obj = new JSONObject(t);

        r = given().spec(reqSpec1)
                .basePath("/refund/HANDLER_INTERNAL/getMasterRefundStatus")
                .body(obj.toString())
                .post()
                .then()
                .body("", allOf(hasKey("mid"), hasKey("encParams")))
                .body("mid", equalToIgnoringCase(txnStatus.txnStatusResponse.MID))
                .extract().response();
        s = new SoftAssertions();
        t = getDecryptedResponse(r.jsonPath().getString("encParams"));

        s.assertThat(t.get("CHECKSUM")).as("CHECKSUM not available").isNotEmpty();
        s.assertThat(t.get("MID")).as("MID mismatch").isEqualTo(txnStatus.txnStatusResponse.MID);
        s.assertThat(t.get("ORDERID")).as("ORDERID mismatch").isEqualTo(txnStatus.txnStatusResponse.getORDERID());
        s.assertThat(t.get("REFID")).as("REFID mismatch").isEqualTo(txnStatus.txnStatusResponse.getORDERID());
        s.assertThat(t.get("REFUNDAMOUNT")).as("REFUNDAMOUNT mismatch").isEqualTo(txnStatus.txnStatusResponse.TXNAMOUNT);
        s.assertThat(t.get("RESPCODE")).as("RESPCODE mismatch").isEqualTo("10");
        s.assertThat(t.get("RESPMSG")).as("RESPMSG mismatch").isEqualTo("Refund Successfull");
        s.assertThat(t.get("STATUS")).as("STATUS mismatch").isEqualTo("TXN_SUCCESS");
        s.assertThat(t.get("TXNAMOUNT")).as("TXNAMOUNT mismatch").isEqualTo(txnStatus.txnStatusResponse.TXNAMOUNT);
        s.assertThat(t.get("TXNDATE")).as("TXNDATE mismatch").isNotEmpty();
        s.assertThat(t.get("TXNID")).as("TXNID mismatch").isEqualTo(txnStatus.txnStatusResponse.TXNID);
        s.assertAll();
    }


    @Parameters("theme")
    @Test(description = "Successful hybrid transaction  through BOB merchant", priority = 1)
    public void t3(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BOB_ENCRYPTED;
        OrderDTO orderDTO = new OrderFactory.WalletOnly(merchantType, theme)
                .setMerchantKey("")
                .setTXN_AMOUNT("2.00")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        orderDTOMap.put("t3", orderDTO);

        WalletHelpers.modifyBalance(user, 1.00);
        String encrytedText = generateEncryptedRequest(orderDTO);

        OrderDTO encyrptedOrder = new OrderDTO.Builder()
                .setMID(merchantType.getId())
                .setENC_DATA(encrytedText)
                .build();

        checkoutPage.createEncryptedOrder(encyrptedOrder);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);

        TreeMap<String, String> t;
        SoftAssertions softly;

        responsePage:
        {
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateMid(encyrptedOrder.getMID())
                    .validateENC_DATA(Constants.ValidationType.NON_EMPTY)
                    .assertAll();
            String encryptedResponse = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.ENC_DATA)
                    .replace(" ", "+");
            t = getDecryptedResponse(encryptedResponse);
            softly = new SoftAssertions();
            softly.assertThat(t.get("CURRENCY")).isEqualTo("INR");
            softly.assertThat(t.get("PAYMENTMODE")).isEqualTo("HYBRID");
            softly.assertThat(t.get("CUST_ID")).isEqualTo(orderDTO.getCUST_ID());
//            softly.assertThat(t.get("GATEWAYNAME")).isEqualTo("HDFC");
            softly.assertThat(t.get("RESPMSG")).isEqualTo("Txn Success");
            softly.assertThat(t.get("MID")).isEqualTo(orderDTO.getMID());
            softly.assertThat(t.get("RESPCODE")).isEqualTo("01");
            softly.assertThat(t.get("TXNID")).isNotEmpty();
            softly.assertThat(t.get("TXNAMOUNT")).isEqualTo(orderDTO.getTXN_AMOUNT());
            softly.assertThat(t.get("ORDERID")).isEqualTo(orderDTO.getORDER_ID());
            softly.assertThat(t.get("STATUS")).isEqualTo("TXN_SUCCESS");
//            softly.assertThat(t.get("BANKTXNID")).isNotEmpty();
            softly.assertThat(t.get("TXNDATE")).isNotEmpty();
            softly.assertThat(t.get("CHECKSUMHASH")).isNotEmpty();
            String checksumHash = t.get("CHECKSUMHASH");
            t.remove("CHECKSUMHASH");
            softly.assertThat(PGPUtil.isChecksumValid(merchantKeyResp.get().get("checksum_key"), t, checksumHash)).as("CHECKSUMHASH mismatch").isTrue();
            softly.assertAll();
        }
        Reporter.report.info("Decrypted Response: " + t.toString());
    }

    @Parameters("orderDTO")
    @Test(description = "Validate txn status for hybrid transaction  through BOB merchant", dependsOnMethods = "t3")
    public void t3_1() {
        OrderDTO orderDTO = orderDTOMap.get("t3");
        TreeMap<String, String> t;
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID(), merchantKeyResp.get().get("checksum_key"), true)
                .executeEncrypted(merchantKeyResp.get().get("encrypt_key"));
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
//                    .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateStatus("TXN_SUCCESS")
//                    .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
//                    .validateStatusAPIParameters();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Constants.Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Constants.Bank.HDFC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(1.00))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();
        txnStatusMap.put("t3", txnStatus);

        t = getDecryptedResponse(txnStatus.getApiResponse().jsonPath().getString("encParams"));
        String checksumHash = t.get("CHECKSUMHASH");
        t.remove("CHECKSUMHASH");
        Assertions.assertThat(PGPUtil.isChecksumValid(merchantKeyResp.get().get("checksum_key"), t, checksumHash)).as("CHECKSUMHASH mismatch").isTrue();
    }

    @Test(description = "Verify peon for hybrid transaction  through BOB merchant", dependsOnMethods = {"t3", "t3_1"})
    public void t3_2() {
        OrderDTO orderDTO = orderDTOMap.get("t3");
        Peons plist = new Peons(orderDTO.getORDER_ID());
        Peon p = plist.getAt("encpeon").get();
        p.response().then()
                .body("", Matchers.allOf(hasKey("CURRENCY"),
                        hasKey("GATEWAYNAME"),
                        hasKey("RESPMSG"),
                        hasKey("BANKNAME"),
                        hasKey("PAYMENTMODE"),
                        hasKey("CUSTID"),
                        hasKey("MID"),
                        hasKey("RESPCODE"),
                        hasKey("TXNID"),
                        hasKey("TXNAMOUNT"),
                        hasKey("ORDERID"),
                        hasKey("STATUS"),
                        hasKey("BANKTXNID"),
                        hasKey("TXNDATE"),
                        hasKey("CHECKSUMHASH")))
                .body("CURRENCY", equalToIgnoringCase("INR"),
                        "GATEWAYNAME", equalToIgnoringCase("HDFC"),
                        "RESPMSG", equalToIgnoringCase("Txn Success"),
                        "BANKNAME", equalToIgnoringCase("HDFC"),
                        "PAYMENTMODE", equalToIgnoringCase("HYBRID"),
                        "CUSTID", not(empty()),
                        "MID", equalToIgnoringCase(orderDTO.getMID()),
                        "RESPCODE", equalToIgnoringCase("01"),
                        "TXNID", not(empty()),
                        "TXNAMOUNT", equalToIgnoringCase(orderDTO.getTXN_AMOUNT()),
                        "ORDERID", equalToIgnoringCase(orderDTO.getORDER_ID()),
                        "STATUS", equalToIgnoringCase("TXN_SUCCESS"),
                        "BANKTXNID", not(empty()),
                        "TXNDATE", not(empty())
                );

        //TODO: getting checksum false as discussed with Srishti raised JIRA: PGP-19965
//        validateChecksum: {
//            String checksumHash = p.map().get("CHECKSUMHASH").toString();
//            TreeMap<String, String> t = new TreeMap<>();
//            t.putAll(p.map());
//            t.remove("CHECKSUMHASH");
//            Assertions.assertThat(PGPUtil.isChecksumValid(merchantKeyResp.get().get("checksum_key"), t, checksumHash))
//                    .as("Checksum invalid").isTrue();
//        }


        plist = new Peons(orderDTO.getORDER_ID() + "_enc");
        plist.getAt("encpeon").get().response()
                .then()
                .body("", allOf(hasKey("MID"),
                        hasKey("ENC_DATA")))
                .body("MID", equalToIgnoringCase(orderDTO.getMID()),
                        "ENC_DATA", not(empty()));
    }

    @Test(description = "Verify refund for transaction using hybrid through BOB merchant", dependsOnMethods = {"t3", "t3_1"})
    public void t3_3() {
        TxnStatus txnStatus = txnStatusMap.get("t3");
        Response r = PGPHelpers.executeMasterRefund(txnStatus.txnStatusResponse.MID, merchantKeyResp.get().get("checksum_key"),
                txnStatus.txnStatusResponse.getORDERID(), txnStatus.txnStatusResponse.getORDERID(),
                txnStatus.txnStatusResponse.getTXNAMOUNT().replace(".00", ""),
                txnStatus.txnStatusResponse.TXNID, "")
                .then()
                .body("", allOf(hasKey("mid"), hasKey("encParams")))
                .body("mid", equalToIgnoringCase(txnStatus.txnStatusResponse.MID))
                .extract().response();

        TreeMap<String, String> t = getDecryptedResponse(r.jsonPath().getString("encParams"));
        Reporter.report.info("Refund api response: " + t);
        System.out.println(t);
        SoftAssertions s = new SoftAssertions();

        s.assertThat(t.get("CHECKSUM")).as("CHECKSUM not available").isNotEmpty();
        s.assertThat(t.get("MID")).as("MID mismatch").isEqualTo(txnStatus.txnStatusResponse.MID);
        s.assertThat(t.get("ORDERID")).as("ORDERID mismatch").isEqualTo(txnStatus.txnStatusResponse.getORDERID());
        s.assertThat(t.get("REFID")).as("REFID mismatch").isEqualTo(txnStatus.txnStatusResponse.getORDERID());
        s.assertThat(t.get("REFUNDAMOUNT")).as("REFUNDAMOUNT mismatch").isEqualTo(txnStatus.txnStatusResponse.TXNAMOUNT);
        s.assertThat(t.get("RESPCODE")).as("RESPCODE mismatch").isEqualTo("10");
        s.assertThat(t.get("RESPMSG")).as("RESPMSG mismatch").isEqualTo("Refund Successfull");
        s.assertThat(t.get("STATUS")).as("STATUS mismatch").isEqualTo("TXN_SUCCESS");
        s.assertThat(t.get("TXNAMOUNT")).as("TXNAMOUNT mismatch").isEqualTo(txnStatus.txnStatusResponse.TXNAMOUNT);
        s.assertThat(t.get("TXNDATE")).as("TXNDATE mismatch").isNotEmpty();
        s.assertThat(t.get("TXNID")).as("TXNID mismatch").isEqualTo(txnStatus.txnStatusResponse.TXNID);
        s.assertAll();

        t = new TreeMap<>();
        t.put("MID", txnStatus.txnStatusResponse.MID);
        t.put("REFID", txnStatus.txnStatusResponse.getORDERID());

        String checksum = PGPUtil.getChecksum(merchantKeyResp.get().get("checksum_key"), t);
        t.put("CHECKSUMHASH", checksum);
        JSONObject obj = new JSONObject(t);

        r = given().spec(reqSpec1)
                .basePath("/refund/HANDLER_INTERNAL/getMasterRefundStatus")
                .body(obj.toString())
                .post()
                .then()
                .body("", allOf(hasKey("mid"), hasKey("encParams")))
                .body("mid", equalToIgnoringCase(txnStatus.txnStatusResponse.MID))
                .extract().response();
        s = new SoftAssertions();
        t = getDecryptedResponse(r.jsonPath().getString("encParams"));

        Reporter.report.info("Refund Status api response: " + t);

        s.assertThat(t.get("CHECKSUM")).as("CHECKSUM not available").isNotEmpty();
        s.assertThat(t.get("MID")).as("MID mismatch").isEqualTo(txnStatus.txnStatusResponse.MID);
        s.assertThat(t.get("ORDERID")).as("ORDERID mismatch").isEqualTo(txnStatus.txnStatusResponse.getORDERID());
        s.assertThat(t.get("REFID")).as("REFID mismatch").isEqualTo(txnStatus.txnStatusResponse.getORDERID());
        s.assertThat(t.get("REFUNDAMOUNT")).as("REFUNDAMOUNT mismatch").isEqualTo(txnStatus.txnStatusResponse.TXNAMOUNT);
        s.assertThat(t.get("RESPCODE")).as("RESPCODE mismatch").isEqualTo("10");
        s.assertThat(t.get("RESPMSG")).as("RESPMSG mismatch").isEqualTo("Refund Successfull");
        s.assertThat(t.get("STATUS")).as("STATUS mismatch").isEqualTo("TXN_SUCCESS");
        s.assertThat(t.get("TXNAMOUNT")).as("TXNAMOUNT mismatch").isEqualTo(txnStatus.txnStatusResponse.TXNAMOUNT);
        s.assertThat(t.get("TXNDATE")).as("TXNDATE mismatch").isNotEmpty();
        s.assertThat(t.get("TXNID")).as("TXNID mismatch").isEqualTo(txnStatus.txnStatusResponse.TXNID);
        s.assertAll();
    }

    @Epic("PGP-24847")
    @Owner("Tarun")
    @Parameters("theme")
    @Test(description = "Successful transaction using CC through BOB merchant from vault")
    public void t5(@Optional("enhancedweb_revamp") String theme) {
        Constants.MerchantType merchantType = Constants.MerchantType.BOB_ENCRYPTED_VAULT;
        OrderDTO orderDTO = new OrderFactory.WalletOnly(merchantType, theme)
                .setMerchantKey("")
                .setTXN_AMOUNT("2.00")
                .build();

        PGPHelpers.getMerchantKey(merchantType.getId(), "checksum_key");

        String encrytedText = generateEncryptedRequest(orderDTO);

        OrderDTO encyrptedOrder = new OrderDTO.Builder()
                .setMID(merchantType.getId())
                .setENC_DATA(encrytedText)
                .build();

        checkoutPage.createEncryptedOrder(encyrptedOrder);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);

        TreeMap<String, String> t;
        SoftAssertions softly;

        responsePage:
        {
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateMid(encyrptedOrder.getMID())
                    .validateENC_DATA(Constants.ValidationType.NON_EMPTY)
                    .assertAll();
            String encryptedResponse = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.ENC_DATA)
                    .replace(" ", "+");
            t = getDecryptedResponse(encryptedResponse);
            softly = new SoftAssertions();
            softly.assertThat(t.get("CURRENCY")).isEqualTo("INR");
            softly.assertThat(t.get("PAYMENTMODE")).isEqualTo("CC");
            softly.assertThat(t.get("CUST_ID")).isEqualTo(orderDTO.getCUST_ID());
            softly.assertThat(t.get("GATEWAYNAME")).isEqualTo("HDFC");
            softly.assertThat(t.get("RESPMSG")).isEqualTo("Txn Success");
            softly.assertThat(t.get("MID")).isEqualTo(orderDTO.getMID());
            softly.assertThat(t.get("RESPCODE")).isEqualTo("01");
            softly.assertThat(t.get("TXNID")).isNotEmpty();
            softly.assertThat(t.get("TXNAMOUNT")).isEqualTo(orderDTO.getTXN_AMOUNT());
            softly.assertThat(t.get("ORDERID")).isEqualTo(orderDTO.getORDER_ID());
            softly.assertThat(t.get("STATUS")).isEqualTo("TXN_SUCCESS");
            softly.assertThat(t.get("BANKTXNID")).isNotEmpty();
            softly.assertThat(t.get("TXNDATE")).isNotEmpty();
//            softly.assertThat(t.get("BIN")).isNotEmpty();
//            softly.assertThat(t.get("LASTFOURDIGITS")).isNotEmpty();
//            softly.assertThat(t.get("cardScheme")).isNotEmpty();
            softly.assertThat(t.get("CHECKSUMHASH")).isNotEmpty();
            String checksumHash = t.get("CHECKSUMHASH");
            t.remove("CHECKSUMHASH");
            softly.assertThat(PGPUtil.isChecksumValid(merchantKeyResp.get().get("checksum_key"), t, checksumHash)).as("CHECKSUMHASH mismatch").isTrue();
            softly.assertAll();
        }
        Reporter.report.info("Decrypted Response: " + t.toString());
    }

    @Parameters("theme")
    @Test(description = "Successful transaction using wallet through BOB merchant from vault")
    public void t6(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BOB_ENCRYPTED_VAULT;
        OrderDTO orderDTO = new OrderFactory.WalletOnly(merchantType, theme)
                .setMerchantKey("")
                .setTXN_AMOUNT("5.00")
                .setSSO_TOKEN(user.ssoToken())
                .build();


        WalletHelpers.modifyBalance(user, 5.00);
        String encrytedText = generateEncryptedRequest(orderDTO);

        OrderDTO encyrptedOrder = new OrderDTO.Builder()
                .setMID(merchantType.getId())
                .setENC_DATA(encrytedText)
                .setORDER_ID(orderDTO.getORDER_ID())
                .build();

        checkoutPage.createEncryptedOrder(encyrptedOrder);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.WALLET);

        TreeMap<String, String> t;
        SoftAssertions softly;

        responsePage:
        {
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateMid(encyrptedOrder.getMID())
                    .validateENC_DATA(Constants.ValidationType.NON_EMPTY)
                    .assertAll();
            String encryptedResponse = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.ENC_DATA)
                    .replace(" ", "+");
            t = getDecryptedResponse(encryptedResponse);
            softly = new SoftAssertions();
            softly.assertThat(t.get("CURRENCY")).isEqualTo("INR");
            softly.assertThat(t.get("CUST_ID")).isEqualTo(orderDTO.getCUST_ID());
            softly.assertThat(t.get("GATEWAYNAME")).isEqualTo("WALLET");
            softly.assertThat(t.get("RESPMSG")).isEqualTo("Txn Success");
            softly.assertThat(t.get("MID")).isEqualTo(orderDTO.getMID());
            softly.assertThat(t.get("RESPCODE")).isEqualTo("01");
            softly.assertThat(t.get("TXNID")).isNotEmpty();
            softly.assertThat(t.get("TXNAMOUNT")).isEqualTo(orderDTO.getTXN_AMOUNT());
            softly.assertThat(t.get("ORDERID")).isEqualTo(orderDTO.getORDER_ID());
            softly.assertThat(t.get("STATUS")).isEqualTo("TXN_SUCCESS");
            softly.assertThat(t.get("BANKTXNID")).isNotEmpty();
            softly.assertThat(t.get("TXNDATE")).isNotEmpty();
            softly.assertThat(t.get("CHECKSUMHASH")).isNotEmpty();
            String checksumHash = t.get("CHECKSUMHASH");
            t.remove("CHECKSUMHASH");
            softly.assertThat(PGPUtil.isChecksumValid(merchantKeyResp.get().get("checksum_key"), t, checksumHash)).as("CHECKSUMHASH mismatch").isTrue();
            softly.assertAll();
        }
        Reporter.report.info("Decrypted Response: " + t.toString());
    }

    @Parameters("theme")
    @Test(description = "Successful hybrid transaction  through BOB merchant from vault")
    public void t7(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BOB_ENCRYPTED;
        OrderDTO orderDTO = new OrderFactory.WalletOnly(merchantType, theme)
                .setMerchantKey("")
                .setTXN_AMOUNT("2.00")
                .setSSO_TOKEN(user.ssoToken())
                .build();

        WalletHelpers.modifyBalance(user, 1.00);
        String encrytedText = generateEncryptedRequest(orderDTO);

        OrderDTO encyrptedOrder = new OrderDTO.Builder()
                .setMID(merchantType.getId())
                .setENC_DATA(encrytedText)
                .build();

        checkoutPage.createEncryptedOrder(encyrptedOrder);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);

        TreeMap<String, String> t;
        SoftAssertions softly;

        responsePage:
        {
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateMid(encyrptedOrder.getMID())
                    .validateENC_DATA(Constants.ValidationType.NON_EMPTY)
                    .assertAll();
            String encryptedResponse = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.ENC_DATA)
                    .replace(" ", "+");
            t = getDecryptedResponse(encryptedResponse);
            softly = new SoftAssertions();
            softly.assertThat(t.get("CURRENCY")).isEqualTo("INR");
            softly.assertThat(t.get("PAYMENTMODE")).isEqualTo("HYBRID");
            softly.assertThat(t.get("CUST_ID")).isEqualTo(orderDTO.getCUST_ID());
//            softly.assertThat(t.get("GATEWAYNAME")).isEqualTo("HDFC");
            softly.assertThat(t.get("RESPMSG")).isEqualTo("Txn Success");
            softly.assertThat(t.get("MID")).isEqualTo(orderDTO.getMID());
            softly.assertThat(t.get("RESPCODE")).isEqualTo("01");
            softly.assertThat(t.get("TXNID")).isNotEmpty();
            softly.assertThat(t.get("TXNAMOUNT")).isEqualTo(orderDTO.getTXN_AMOUNT());
            softly.assertThat(t.get("ORDERID")).isEqualTo(orderDTO.getORDER_ID());
            softly.assertThat(t.get("STATUS")).isEqualTo("TXN_SUCCESS");
//            softly.assertThat(t.get("BANKTXNID")).isNotEmpty();
            softly.assertThat(t.get("TXNDATE")).isNotEmpty();
            softly.assertThat(t.get("CHECKSUMHASH")).isNotEmpty();
            String checksumHash = t.get("CHECKSUMHASH");
            t.remove("CHECKSUMHASH");
            softly.assertThat(PGPUtil.isChecksumValid(merchantKeyResp.get().get("checksum_key"), t, checksumHash)).as("CHECKSUMHASH mismatch").isTrue();
            softly.assertAll();
        }
        Reporter.report.info("Decrypted Response: " + t.toString());
    }


    private static class PAYLOAD_DATA {
        private String PAYMENT_TYPE_ID;
        private String PAYMENT_MODE_ONLY;
        private String PAYMENT_MODE_DISABLE;
        private PayMode[] VISIBLE_PAYMODES;
        private PayMode[] NON_VISIBLE_PAYMODES;
        private String[] SAVEDCARDS;
        private String[] VISIBLE_SAVEDCARDS;
        private String[] NON_VISIBLE_SAVEDCARDS;
        private boolean ssoToken;

        public PAYLOAD_DATA(String PAYMENT_TYPE_ID, String PAYMENT_MODE_ONLY, String PAYMENT_MODE_DISABLE, PayMode[] VISIBLE_PAYMODES, PayMode[] NON_VISIBLE_PAYMODES, boolean ssoToken) {
            this.PAYMENT_TYPE_ID = PAYMENT_TYPE_ID;
            this.PAYMENT_MODE_ONLY = PAYMENT_MODE_ONLY;
            this.PAYMENT_MODE_DISABLE = PAYMENT_MODE_DISABLE;
            this.VISIBLE_PAYMODES = VISIBLE_PAYMODES;
            this.NON_VISIBLE_PAYMODES = NON_VISIBLE_PAYMODES;
            this.ssoToken = ssoToken;
        }

        public String[] getNON_VISIBLE_SAVEDCARDS() {
            return NON_VISIBLE_SAVEDCARDS;
        }

        public PAYLOAD_DATA setNON_VISIBLE_SAVEDCARDS(String[] NON_VISIBLE_SAVEDCARDS) {
            this.NON_VISIBLE_SAVEDCARDS = NON_VISIBLE_SAVEDCARDS;
            return this;
        }

        public String[] getVISIBLE_SAVEDCARDS() {
            return VISIBLE_SAVEDCARDS;
        }

        public PAYLOAD_DATA setVISIBLE_SAVEDCARDS(String[] VISIBLE_SAVEDCARDS) {
            this.VISIBLE_SAVEDCARDS = VISIBLE_SAVEDCARDS;
            return this;
        }

        public String[] getSAVEDCARDS() {
            return SAVEDCARDS;
        }

        public PAYLOAD_DATA setSAVEDCARDS(String[] SAVEDCARDS) {
            this.SAVEDCARDS = SAVEDCARDS;
            return this;
        }

        public String getPAYMENT_TYPE_ID() {
            return PAYMENT_TYPE_ID;
        }

        public PAYLOAD_DATA setPAYMENT_TYPE_ID(String PAYMENT_TYPE_ID) {
            this.PAYMENT_TYPE_ID = PAYMENT_TYPE_ID;
            return this;
        }

        public String getPAYMENT_MODE_ONLY() {
            return PAYMENT_MODE_ONLY;
        }

        public PAYLOAD_DATA setPAYMENT_MODE_ONLY(String PAYMENT_MODE_ONLY) {
            this.PAYMENT_MODE_ONLY = PAYMENT_MODE_ONLY;
            return this;
        }

        public String getPAYMENT_MODE_DISABLE() {
            return PAYMENT_MODE_DISABLE;
        }

        public PAYLOAD_DATA setPAYMENT_MODE_DISABLE(String PAYMENT_MODE_DISABLE) {
            this.PAYMENT_MODE_DISABLE = PAYMENT_MODE_DISABLE;
            return this;
        }

        public PayMode[] getVISIBLE_PAYMODES() {
            return VISIBLE_PAYMODES;
        }

        public PAYLOAD_DATA setVISIBLE_PAYMODES(PayMode[] VISIBLE_PAYMODES) {
            this.VISIBLE_PAYMODES = VISIBLE_PAYMODES;
            return this;
        }

        public PayMode[] getNON_VISIBLE_PAYMODES() {
            return NON_VISIBLE_PAYMODES;
        }

        public PAYLOAD_DATA setNON_VISIBLE_PAYMODES(PayMode[] NON_VISIBLE_PAYMODES) {
            this.NON_VISIBLE_PAYMODES = NON_VISIBLE_PAYMODES;
            return this;
        }

        public boolean getSsoToken() {
            return ssoToken;
        }

        public PAYLOAD_DATA setSsoToken(boolean ssoToken) {
            this.ssoToken = ssoToken;
            return this;
        }

        @Override
        public String toString() {
            return "PAYLOAD_DATA{" +
                    "PAYMENT_TYPE_ID='" + PAYMENT_TYPE_ID + '\'' +
                    ", PAYMENT_MODE_ONLY='" + PAYMENT_MODE_ONLY + '\'' +
                    ", PAYMENT_MODE_DISABLE='" + PAYMENT_MODE_DISABLE + '\'' +
                    ", VISIBLE_PAYMODES=" + Arrays.toString(VISIBLE_PAYMODES) +
                    ", NON_VISIBLE_PAYMODES=" + Arrays.toString(NON_VISIBLE_PAYMODES) +
                    ", SAVEDCARDS=" + Arrays.toString(SAVEDCARDS) +
                    ", VISIBLE_SAVEDCARDS=" + Arrays.toString(VISIBLE_SAVEDCARDS) +
                    ", NON_VISIBLE_SAVEDCARDS=" + Arrays.toString(NON_VISIBLE_SAVEDCARDS) +
                    ", ssoToken='" + ssoToken + '\'' +
                    '}';
        }
    }

    @DataProvider(name = "PaymodeData")
    public Object[] dataProvider() throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        Object[][] objects = new Object[33][];

        /**
         * "PAYMENT_TYPE_ID" , "PAYMENT_MODE_ONLY", "PAYMENT_MODE_DISABLE", "VISIBLE_PAYMODES", "NON_VISIBLE_PAYMODES", "ssoToken"
         */

        objects[0] =
                new Object[]{new PAYLOAD_DATA("DC", "YES", "", new PayMode[]{PayMode.DC}, new PayMode[]{PayMode.CC}, false), "enhancedweb_revamp"};
        objects[1] =
                new Object[]{new PAYLOAD_DATA("CC", "YES", "", new PayMode[]{PayMode.CC}, new PayMode[]{PayMode.DC}, false), "enhancedweb_revamp"};
        objects[2] =
                new Object[]{new PAYLOAD_DATA("DEBIT_CARD", "YES", "", new PayMode[]{PayMode.DC}, new PayMode[]{PayMode.CC}, false), "enhancedweb_revamp"};
        objects[3] =
                new Object[]{new PAYLOAD_DATA("CREDIT_CARD", "YES", "", new PayMode[]{PayMode.CC}, new PayMode[]{PayMode.DC}, false), "enhancedweb_revamp"};
        objects[4] =
                new Object[]{new PAYLOAD_DATA("CC,DEBIT_CARD", "YES", "", new PayMode[]{PayMode.CC, PayMode.DC}, new PayMode[]{}, false), "enhancedweb_revamp"};
        objects[5] =
                new Object[]{new PAYLOAD_DATA("DC,CREDIT_CARD", "YES", "", new PayMode[]{PayMode.CC, PayMode.DC}, new PayMode[]{}, false), "enhancedweb_revamp"};
        objects[6] =
                new Object[]{new PAYLOAD_DATA("DC,CC,NB,COD", "YES", "", new PayMode[]{PayMode.CC, PayMode.DC, PayMode.NB, PayMode.COD}, new PayMode[]{}, true), "enhancedweb_revamp"};
        objects[7] =
                new Object[]{new PAYLOAD_DATA("DEBIT_CARD,CREDIT_CARD,NB", "YES", "", new PayMode[]{PayMode.CC, PayMode.DC, PayMode.NB}, new PayMode[]{}, false), "enhancedweb_revamp"};
        objects[8] =
                new Object[]{new PAYLOAD_DATA("", "", "DC", new PayMode[]{PayMode.CC}, new PayMode[]{PayMode.DC}, false), "enhancedweb_revamp"};
        objects[9] =
                new Object[]{new PAYLOAD_DATA("", "", "CC", new PayMode[]{PayMode.DC}, new PayMode[]{PayMode.CC}, false), "enhancedweb_revamp"};
        objects[10] =
                new Object[]{new PAYLOAD_DATA("", "", "DEBIT_CARD", new PayMode[]{PayMode.CC}, new PayMode[]{PayMode.DC}, false), "enhancedweb_revamp"};
        objects[11] =
                new Object[]{new PAYLOAD_DATA("", "", "CREDIT_CARD", new PayMode[]{PayMode.DC}, new PayMode[]{PayMode.CC}, false), "enhancedweb_revamp"};
        objects[12] =
                new Object[]{new PAYLOAD_DATA("", "", "CC,DEBIT_CARD", new PayMode[]{}, new PayMode[]{PayMode.DC, PayMode.CC}, false), "enhancedweb_revamp"};
        objects[13] =
                new Object[]{new PAYLOAD_DATA("", "", "DC,CREDIT_CARD", new PayMode[]{}, new PayMode[]{PayMode.DC, PayMode.CC}, false), "enhancedweb_revamp"};
        objects[14] =
                new Object[]{new PAYLOAD_DATA("", "", "DC,CC,NB,COD", new PayMode[]{}, new PayMode[]{PayMode.DC, PayMode.CC, PayMode.NB, PayMode.COD}, false), "enhancedweb_revamp"};
        objects[15] =
                new Object[]{new PAYLOAD_DATA("", "", "DEBIT_CARD,CREDIT_CARD,NB", new PayMode[]{}, new PayMode[]{PayMode.DC, PayMode.CC, PayMode.NB}, false), "enhancedweb_revamp"};
        objects[16] =
                new Object[]{new PAYLOAD_DATA("DC", "YES", "", new PayMode[]{PayMode.DC}, new PayMode[]{PayMode.CC}, true)
                        .setVISIBLE_SAVEDCARDS(new String[]{paymentDTO.getDebitCardNumber()})
                        .setNON_VISIBLE_SAVEDCARDS(new String[]{paymentDTO.getEmiCard()}), "enhancedweb_revamp"};
        objects[17] =
                new Object[]{new PAYLOAD_DATA("CC", "YES", "", new PayMode[]{PayMode.CC}, new PayMode[]{PayMode.DC}, true)
                        .setVISIBLE_SAVEDCARDS(new String[]{paymentDTO.getEmiCard()})
                        .setNON_VISIBLE_SAVEDCARDS(new String[]{paymentDTO.getDebitCardNumber()}), "enhancedweb_revamp"};
        objects[18] =
                new Object[]{new PAYLOAD_DATA("DEBIT_CARD", "YES", "", new PayMode[]{PayMode.DC}, new PayMode[]{PayMode.CC}, true)
                        .setVISIBLE_SAVEDCARDS(new String[]{paymentDTO.getDebitCardNumber()})
                        .setNON_VISIBLE_SAVEDCARDS(new String[]{paymentDTO.getEmiCard()}), "enhancedweb_revamp"};
        objects[19] =
                new Object[]{new PAYLOAD_DATA("CREDIT_CARD", "YES", "", new PayMode[]{PayMode.CC}, new PayMode[]{PayMode.DC}, true)
                        .setVISIBLE_SAVEDCARDS(new String[]{paymentDTO.getEmiCard()})
                        .setNON_VISIBLE_SAVEDCARDS(new String[]{paymentDTO.getDebitCardNumber()}), "enhancedweb_revamp"};
        objects[20] =
                new Object[]{new PAYLOAD_DATA("CC,DEBIT_CARD", "YES", "", new PayMode[]{PayMode.CC, PayMode.DC}, new PayMode[]{}, true)
                        .setVISIBLE_SAVEDCARDS(new String[]{paymentDTO.getDebitCardNumber(), paymentDTO.getEmiCard()})
                        .setNON_VISIBLE_SAVEDCARDS(new String[]{}), "enhancedweb_revamp"};
        objects[21] =
                new Object[]{new PAYLOAD_DATA("DC,CREDIT_CARD", "YES", "", new PayMode[]{PayMode.CC, PayMode.DC}, new PayMode[]{}, true)
                        .setVISIBLE_SAVEDCARDS(new String[]{paymentDTO.getDebitCardNumber(), paymentDTO.getEmiCard()})
                        .setNON_VISIBLE_SAVEDCARDS(new String[]{}), "enhancedweb_revamp"};
        objects[22] =
                new Object[]{new PAYLOAD_DATA("DC,CC,NB", "YES", "", new PayMode[]{PayMode.CC, PayMode.DC, PayMode.NB}, new PayMode[]{}, true)
                        .setVISIBLE_SAVEDCARDS(new String[]{paymentDTO.getDebitCardNumber(), paymentDTO.getEmiCard()})
                        .setNON_VISIBLE_SAVEDCARDS(new String[]{}), "enhancedweb_revamp"};
        objects[23] =
                new Object[]{new PAYLOAD_DATA("DEBIT_CARD,CREDIT_CARD,NB", "YES", "", new PayMode[]{PayMode.CC, PayMode.DC, PayMode.NB}, new PayMode[]{}, true)
                        .setVISIBLE_SAVEDCARDS(new String[]{paymentDTO.getDebitCardNumber(), paymentDTO.getEmiCard()})
                        .setNON_VISIBLE_SAVEDCARDS(new String[]{}), "enhancedweb_revamp"};
        objects[24] =
                new Object[]{new PAYLOAD_DATA("", "", "DC", new PayMode[]{PayMode.CC}, new PayMode[]{PayMode.DC}, true)
                        .setVISIBLE_SAVEDCARDS(new String[]{paymentDTO.getEmiCard()})
                        .setNON_VISIBLE_SAVEDCARDS(new String[]{paymentDTO.getDebitCardNumber()}), "enhancedweb_revamp"};
        objects[25] =
                new Object[]{new PAYLOAD_DATA("", "", "CC", new PayMode[]{PayMode.DC}, new PayMode[]{PayMode.CC}, true)
                        .setVISIBLE_SAVEDCARDS(new String[]{paymentDTO.getDebitCardNumber()})
                        .setNON_VISIBLE_SAVEDCARDS(new String[]{paymentDTO.getEmiCard()}), "enhancedweb_revamp"};
        objects[26] =
                new Object[]{new PAYLOAD_DATA("", "", "DEBIT_CARD", new PayMode[]{PayMode.CC}, new PayMode[]{PayMode.DC}, true)
                        .setVISIBLE_SAVEDCARDS(new String[]{paymentDTO.getEmiCard()})
                        .setNON_VISIBLE_SAVEDCARDS(new String[]{paymentDTO.getDebitCardNumber()}), "enhancedweb_revamp"};
        objects[27] =
                new Object[]{new PAYLOAD_DATA("", "", "CREDIT_CARD", new PayMode[]{PayMode.DC}, new PayMode[]{PayMode.CC}, true)
                        .setVISIBLE_SAVEDCARDS(new String[]{paymentDTO.getDebitCardNumber()})
                        .setNON_VISIBLE_SAVEDCARDS(new String[]{paymentDTO.getEmiCard()}), "enhancedweb_revamp"};
        objects[28] =
                new Object[]{new PAYLOAD_DATA("", "", "CC,DEBIT_CARD", new PayMode[]{}, new PayMode[]{PayMode.DC, PayMode.CC}, true)
                        .setVISIBLE_SAVEDCARDS(new String[]{})
                        .setNON_VISIBLE_SAVEDCARDS(new String[]{paymentDTO.getEmiCard(), paymentDTO.getDebitCardNumber()}), "enhancedweb_revamp"};
        objects[29] =
                new Object[]{new PAYLOAD_DATA("", "", "DC,CREDIT_CARD", new PayMode[]{}, new PayMode[]{PayMode.DC, PayMode.CC}, true)
                        .setVISIBLE_SAVEDCARDS(new String[]{})
                        .setNON_VISIBLE_SAVEDCARDS(new String[]{paymentDTO.getEmiCard(), paymentDTO.getDebitCardNumber()}), "enhancedweb_revamp"};
        objects[30] =
                new Object[]{new PAYLOAD_DATA("", "", "DC,CC,NB", new PayMode[]{}, new PayMode[]{PayMode.DC, PayMode.CC, PayMode.NB}, true)
                        .setVISIBLE_SAVEDCARDS(new String[]{})
                        .setNON_VISIBLE_SAVEDCARDS(new String[]{paymentDTO.getEmiCard(), paymentDTO.getDebitCardNumber()}), "enhancedweb_revamp"};
        objects[31] =
                new Object[]{new PAYLOAD_DATA("", "", "DEBIT_CARD,CREDIT_CARD,NB", new PayMode[]{}, new PayMode[]{PayMode.DC, PayMode.CC, PayMode.NB}, true)
                        .setVISIBLE_SAVEDCARDS(new String[]{})
                        .setNON_VISIBLE_SAVEDCARDS(new String[]{paymentDTO.getEmiCard(), paymentDTO.getDebitCardNumber()}), "enhancedweb_revamp"};
        objects[32] =
                new Object[]{new PAYLOAD_DATA("COD", "YES", "", new PayMode[]{PayMode.COD}, new PayMode[]{}, true), "enhancedweb_revamp"};

        return objects;
    }

    @Test(description = "Verify paymentMode  on cashierPage when different Paymodes are passed in request", dataProvider = "PaymodeData", priority = 0)
    public void t4(PAYLOAD_DATA payload_data, @Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = null;
        PaymentDTO paymentDTO = new PaymentDTO();
        if(payload_data.ssoToken) {
            user = userManager.getForWrite(Label.BASIC);
            SavedCardHelpers.deleteSavedCard(user);
            SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());
            SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getEmiCard());
        }
        Constants.MerchantType merchantType = Constants.MerchantType.BOB_ENCRYPTED;
        OrderDTO orderDTO = new OrderFactory.WalletOnly(merchantType, theme)
                .setMerchantKey("")
                .setTXN_AMOUNT("12.00")
                .setPAYMENT_TYPE_ID(payload_data.PAYMENT_TYPE_ID)
                .setPAYMENT_MODE_ONLY(payload_data.PAYMENT_MODE_ONLY)
                .setPAYMENT_MODE_DISABLE(payload_data.PAYMENT_MODE_DISABLE)
                .setSSO_TOKEN( user == null ? "" : user.ssoToken() )
                .build();
        String encrytedText = generateEncryptedRequest(orderDTO);

        OrderDTO encyrptedOrder = new OrderDTO.Builder()
                .setMID(merchantType.getId())
                .setENC_DATA(encrytedText)
                .setORDER_ID(orderDTO.getORDER_ID())
                .build();

        checkoutPage.createEncryptedOrder(encyrptedOrder);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        assertVisiblePaymentModes(cashierPage, payload_data.VISIBLE_PAYMODES);
        assertNotVisiblePaymentModes(cashierPage, payload_data.NON_VISIBLE_PAYMODES);
        assertVisibleSavedCardsPaymentModes(cashierPage, payload_data.VISIBLE_SAVEDCARDS);
        assertNonVisibleSavedCardsPaymentModes(cashierPage, payload_data.NON_VISIBLE_SAVEDCARDS);
    }

    private void assertNonVisibleSavedCardsPaymentModes(CashierPage cashierPage, String[] saveCards) {
        SoftAssertions softly = new SoftAssertions();
        if (saveCards != null)
            for (String card : saveCards) {
                softly.assertThat(cashierPage.savedCard(card).isElementPresent())
                        .as("Saved card tab for card number: '" + card + "' is visible")
                        .isFalse();
            }
        softly.assertAll();
    }

    private void assertVisibleSavedCardsPaymentModes(CashierPage cashierPage, String[] saveCards) {
        SoftAssertions softly = new SoftAssertions();
        if (saveCards != null)
            for (String card : saveCards) {
                softly.assertThat(cashierPage.verifyPaymentModeDisplayed(PayMode.SAVED_CARD))
                        .as("Saved Card tab is not visible")
                        .isTrue();
                softly.assertThat(cashierPage.savedCard(card).isElementPresent())
                        .as("Saved card tab for card number: '" + card + "' is not visible")
                        .isTrue();
            }
        softly.assertAll();
    }

    private void assertNotVisiblePaymentModes(CashierPage cashierPage, PayMode[] VISIBLE_PAYMODES) {
        SoftAssertions softly = new SoftAssertions();
        if (VISIBLE_PAYMODES != null)
            for (PayMode payMode : VISIBLE_PAYMODES) {
                softly.assertThat(cashierPage.verifyPaymentModeDisplayed(payMode))
                        .as("Paymode : '" + payMode.toString() + "' is not visible")
                        .isFalse();
            }
        softly.assertAll();
    }

    private void assertVisiblePaymentModes(CashierPage cashierPage, PayMode[] VISIBLE_PAYMODES) {
        SoftAssertions softly = new SoftAssertions();
        for (PayMode payMode : VISIBLE_PAYMODES) {
            softly.assertThat(cashierPage.verifyPaymentModeDisplayed(payMode))
                    .as("Paymode : '" + payMode.toString() + "' is not visible")
                    .isTrue();
        }
        softly.assertAll();
    }

    @BeforeClass
    public void setImplicitWait() {
        DriverManager.getDriver().manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    }

    @AfterClass
    public void resetImplicitWait() {
        DriverManager.getDriver().manage().timeouts().implicitlyWait(Integer.parseInt(System.getProperty("MAX_ELEMENT_LOAD_WAIT_TIME", "60")), TimeUnit.SECONDS);
    }

//=========================

    @Step("fetch key from BO")
    private void fetchMerchantKey(String mid) {
        /**
         * TO AVOID THIS API CALL WE WILL BE FETCHING THIS FROM merchant.yaml
         *
         * User user = new User("9891497839", "paytm@123");
         *         Response response = new FetchKey(mid, user.ssoToken()).execute().then()
         *                 .statusCode(200)
         *                 .body("", allOf(hasKey("checksum_key"), hasKey("default"), hasKey("encrypt_key")))
         *                 .extract().response();
         */

        Map<String, String> m = new HashMap<>();
        m.put("checksum_key", PGPHelpers.getMerchantKey(mid, "checksum_key"));
        m.put("default", PGPHelpers.getMerchantKey(mid, "default"));
        m.put("encrypt_key", PGPHelpers.getMerchantKey(mid, "encrypt_key"));
        merchantKeyResp.set(m);
    }

    private String generateEncryptedRequest(OrderDTO orderDTO) {
        fetchMerchantKey(orderDTO.getMID());
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        TreeMap<String, String> t = mapper.convertValue(orderDTO, TreeMap.class);
        String checksum = PGPUtil.getChecksum(merchantKeyResp.get().get("checksum_key"), t);
        String encrptionString = "";
        for (Map.Entry<String, String> entry : t.entrySet()) {
            encrptionString = encrptionString + entry.getKey() + "=" + entry.getValue() + "|";
        }
        encrptionString = encrptionString + "CHECKSUMHASH=" + checksum;
        String encrytedText = Aes256EncryptionDecryption.encrypt(encrptionString, merchantKeyResp.get().get("encrypt_key"));
        return encrytedText;
    }

    private TreeMap<String, String> getDecryptedResponse(String encryptedString) {
        String decryptedResponse = Aes256EncryptionDecryption.decrypt(encryptedString, merchantKeyResp.get().get("encrypt_key"));

        TreeMap<String, String> respMap = new TreeMap<>();
        try {
            for (String s : decryptedResponse.split("\\|")) {
                String k = s.substring(0, s.indexOf("="));
                String v = s.substring(s.indexOf("=") + 1);

                respMap.put(k, v);
            }
        } catch (Exception e) {
            throw new PGPException("Exception occurred while decrypting request");
        }
        return respMap;
    }

    private RequestSpecification reqSpec1 = new RequestSpecBuilder()
            .addFilter(new RequestResponseLoggingFilter())
            .setConfig(new CurlLoggingRestAssuredConfigBuilder().build())
            .setContentType(ContentType.JSON)
            .setBaseUri(PGP_HOST)
            .build();

}
