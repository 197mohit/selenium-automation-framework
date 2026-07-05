package scripts.Native.Khata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.paytm.api.GetPaymentStatus;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.GetPaymentStatusResponse.GetPaymentStatusResponseDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.Peon;
import com.paytm.utils.merchant.util.PayMethodType;
import groovy.json.JsonOutput;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Date;
import static com.paytm.base.test.Group.Status.BUG;

@Owner("Gagandeep")
public class KhataBook extends PGPBaseTest {


    private final CheckoutPage checkoutPage = new CheckoutPage();

    private JsonPath Validate_FetchPayInstrument(String txnToken, InitTxnDTO initTxnDTO, String payMethod, String status) {
        Reporter.report.info("Validating fetch pay options for the merchant and txn token");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", status).getList(
                "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains(payMethod);
        return fetchPaymentOptionsJson;
    }


    private String GetJSONStringBody(Response response) throws IOException {
        ObjectMapper object = new ObjectMapper();
        GetPaymentStatusResponseDTO getPaymentStatusResponseDTO = object.readValue(response.getBody().asString(), GetPaymentStatusResponseDTO.class);
        Gson gson = new Gson();
        return gson.toJson(getPaymentStatusResponseDTO.getBody());
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify that NB txn successful for khatabook merchant", enabled = true)
    public void TC_001_KhataBookTxnNB(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.ADVANCE_DEPOSIT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly)
                .setTxnValue("2.00")
                .setMerchantKey(Constants.MerchantType.PGOnly.getKey())
                .setpeonUrl("https://pgp-automation.paytm.in/mockbank/peon")
                .setAggrMid(Constants.MerchantType.ADVANCE_DEPOSIT.getId())
                .build(merchantType.getKey());
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.PGOnly, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setAggMid(merchantType.getId())
                .setChannelCode("ICICI")
                .build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "NET_BANKING", "false");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateBankName(Constants.Bank.ICICINB.toString())
                .validateCheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey())
                .assertAll();
        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().equals("ICICI"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("ICICI"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("NB"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid(Constants.MerchantType.ADVANCE_DEPOSIT.getKey())

        );
        sAssert.eval();

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = merchant.execute();
        JsonPath jsonPath = response.jsonPath();
        String MerchantResBody = GetJSONStringBody(response);
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(initTxnDTO.txnAmountFromBody());
        Assertions.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.ICICI.toString());
        Assertions.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        Assertions.assertThat(jsonPath.getString("body.paymentMode")).isEqualToIgnoringCase("NB");
        Boolean isValidChecksum = PGPHelpers.verifyKhataMerchantcheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey(), MerchantResBody, jsonPath.getString("head.signature"));
        Assertions.assertThat(isValidChecksum).as("Checksum Validation failed in GetPaymentStatus Response").isTrue();

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify that CC collect txn successful for khatabook merchant.", enabled = true)
    public void TC_002_KhataBookTxnCC(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.ADVANCE_DEPOSIT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly)
                .setTxnValue("2.00")
                .setpeonUrl("https://pgp-automation.paytm.in/mockbank/peon")
                .setAggrMid(Constants.MerchantType.ADVANCE_DEPOSIT.getId())
                .build(merchantType.getKey());
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setAggMid(merchantType.getId())
                .setMID(Constants.MerchantType.PGOnly.getId())
                .build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey()).assertAll();
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
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().equals("HDFC Bank"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("CC"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid(Constants.MerchantType.ADVANCE_DEPOSIT.getKey())

        );
        sAssert.eval();
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = merchant.execute();
        JsonPath jsonPath = response.jsonPath();
        String MerchantResBody = GetJSONStringBody(response);
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(initTxnDTO.txnAmountFromBody());
        Assertions.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());
        Assertions.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        Assertions.assertThat(jsonPath.getString("body.paymentMode")).isEqualToIgnoringCase("CC");
        Boolean isValidChecksum = PGPHelpers.verifyKhataMerchantcheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey(), MerchantResBody, jsonPath.getString("head.signature"));
        Assertions.assertThat(isValidChecksum).as("Checksum Validation failed in GetPaymentStatus Response").isTrue();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify that DC collect txn successful for khatabook merchant.", enabled = true)
    public void TC_003_KhataBookTxnDC(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.ADVANCE_DEPOSIT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly)
                .setTxnValue("2.00")
                .setpeonUrl("https://pgp-automation.paytm.in/mockbank/peon")
                .setAggrMid(Constants.MerchantType.ADVANCE_DEPOSIT.getId())
                .build(merchantType.getKey());
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.PGOnly, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setAggMid(merchantType.getId())
                .build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey()).assertAll();
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
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().equals("HDFC Bank"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("DC"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid(Constants.MerchantType.ADVANCE_DEPOSIT.getKey())

        );
        sAssert.eval();
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = merchant.execute();
        JsonPath jsonPath = response.jsonPath();
        String MerchantResBody = GetJSONStringBody(response);
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(initTxnDTO.txnAmountFromBody());
        Assertions.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());
        Assertions.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        Boolean isValidChecksum = PGPHelpers.verifyKhataMerchantcheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey(), MerchantResBody, jsonPath.getString("head.signature"));
        Assertions.assertThat(isValidChecksum).as("Checksum Validation failed in GetPaymentStatus Response").isTrue();

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify that Wallet collect txn successful for khatabook merchant.", enabled = true)
    public void TC_004_KhataBookTxnWallet(@Optional("false") Boolean isNativePlus) throws Exception {
        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, txnAmount);
        Constants.MerchantType merchantType = Constants.MerchantType.ADVANCE_DEPOSIT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.WALLETPEON)
                .setTxnValue("2.00")
                .setpeonUrl("https://pgp-automation.paytm.in/mockbank/peon")
                .setAggrMid(Constants.MerchantType.ADVANCE_DEPOSIT.getId())
                .build(merchantType.getKey());
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.WALLETPEON, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .setAggMid(merchantType.getId())
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey()).assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().equals("WALLET"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("WALLET"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("PPI"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid(Constants.MerchantType.ADVANCE_DEPOSIT.getKey())

        );
        sAssert.eval();
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = merchant.execute();
        JsonPath jsonPath = response.jsonPath();
        String MerchantResBody = GetJSONStringBody(response);
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(initTxnDTO.txnAmountFromBody());
        Assertions.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase("WALLET");
        Assertions.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        Assertions.assertThat(jsonPath.getString("body.paymentMode")).isEqualToIgnoringCase("PPI");
        Boolean isValidChecksum = PGPHelpers.verifyKhataMerchantcheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey(), MerchantResBody, jsonPath.getString("head.signature"));
        Assertions.assertThat(isValidChecksum).as("Checksum Validation failed in GetPaymentStatus Response").isTrue();

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify that ADD N PAY collect txn successful for khatabook merchant.", enabled = true)
    public void TC_005_KhataBookTxnADDNPAY(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.ADVANCE_DEPOSIT;
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,1.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.AddnPay)
                .setTxnValue("2.00")
                .setpeonUrl("https://pgp-automation.paytm.in/mockbank/peon")
                .setAggrMid(Constants.MerchantType.ADVANCE_DEPOSIT.getId())
                .build(merchantType.getKey());
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setAggMid(merchantType.getId())
                .setPaymentFlow("ADDANDPAY")
                .setMID(Constants.MerchantType.AddnPay.getId())
                .build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey()).assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().equals("WALLET"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("WALLET"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("PPI"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid(Constants.MerchantType.ADVANCE_DEPOSIT.getKey())

        );
        sAssert.eval();
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = merchant.execute();
        JsonPath jsonPath = response.jsonPath();
        String MerchantResBody = GetJSONStringBody(response);
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(initTxnDTO.txnAmountFromBody());
        Assertions.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase("WALLET");
        Assertions.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        Assertions.assertThat(jsonPath.getString("body.paymentMode")).isEqualToIgnoringCase("PPI");
        Boolean isValidChecksum = PGPHelpers.verifyKhataMerchantcheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey(), MerchantResBody, jsonPath.getString("head.signature"));
        Assertions.assertThat(isValidChecksum).as("Checksum Validation failed in GetPaymentStatus Response").isTrue();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify that UPI collect txn successful for khatabook merchant.", enabled = true)
    public void TC_006_KhataBookTxnUPI(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.ADVANCE_DEPOSIT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly)
                .setTxnValue("2.00")
                .setpeonUrl("https://pgp-automation.paytm.in/mockbank/peon")
                .setAggrMid(Constants.MerchantType.ADVANCE_DEPOSIT.getId())
                .build(merchantType.getKey());
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount("test@paytm")
                .setAggMid(merchantType.getId())
                .setMID(Constants.MerchantType.PGOnly.getId())
                .build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey()).assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("ICICI"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid(Constants.MerchantType.ADVANCE_DEPOSIT.getKey())

        );
        sAssert.eval();
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = merchant.execute();
        JsonPath jsonPath = response.jsonPath();
        String MerchantResBody = GetJSONStringBody(response);
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(initTxnDTO.txnAmountFromBody());
        Assertions.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.ICICI.toString());
        Assertions.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        Boolean isValidChecksum = PGPHelpers.verifyKhataMerchantcheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey(), MerchantResBody, jsonPath.getString("head.signature"));
        Assertions.assertThat(isValidChecksum).as("Checksum Validation failed in GetPaymentStatus Response").isTrue();

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify that HYBRID collect txn successful for khatabook merchant.")/*, enabled = true,groups = BUG)*/
    public void TC_007_KhataBookTxnHYBRID(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.ADVANCE_DEPOSIT;
        WalletHelpers.modifyBalance(user, 1.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_HYBRID)
                .setTxnValue("2.00")
                .setpeonUrl("https://pgp-automation.paytm.in/mockbank/peon")
                .setAggrMid(Constants.MerchantType.ADVANCE_DEPOSIT.getId())
                .setCallbackUrl("https://pgp-automation.paytm.in/mockbank/MerchantSite/bankResponse")
                .build(merchantType.getKey());

        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.NATIVE_HYBRID, orderId, txnToken, PayMethodType.CREDIT_CARD)
                .setAggMid(merchantType.getId())
                .setPaymentFlow("HYBRID")
                .build();


        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey()).assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateChildTxnsPresent()
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE","CHILDTXNLIST","TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("HYBRID"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
              peon.isChecksumValid(Constants.MerchantType.ADVANCE_DEPOSIT.getKey())

        );
        sAssert.eval();
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = merchant.execute();
        JsonPath jsonPath = response.jsonPath();
        ObjectMapper object = new ObjectMapper();
        //jsonPath.getMap("body").remove("childTransaction");
        GetPaymentStatusResponseDTO getPaymentStatusResponseDTO =
                object.readValue(JsonOutput.toJson(jsonPath.getMap(".")), GetPaymentStatusResponseDTO.class);
        Gson gson = new Gson();
        String MerchantResBody= gson.toJson(getPaymentStatusResponseDTO.getBody());
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(initTxnDTO.txnAmountFromBody());
        Assertions.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        Boolean isValidChecksum = PGPHelpers.verifyKhataMerchantcheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey(), MerchantResBody, jsonPath.getString("head.signature"));
        Assertions.assertThat(isValidChecksum).as("Checksum Validation failed in GetPaymentStatus Response").isTrue();

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify that Checksum provided is invalid when different checksum initiate and process txn", enabled = true)
    public void TC_008_KhataBookTxnDifferentMID(@Optional("false") Boolean isNativePlus) throws Exception {


        Constants.MerchantType merchantType = Constants.MerchantType.ADVANCE_DEPOSIT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly)
                .setTxnValue("2.00")
                .setpeonUrl("https://pgp-automation.paytm.in/mockbank/peon")
                .setAggrMid(Constants.MerchantType.ADVANCE_DEPOSIT.getId())
                .build(Constants.MerchantType.PGOnly.getKey());
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Checksum provided is invalid");
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify that CC collect txn failed for khatabook merchant.", enabled = true)
    public void TC_009_KhataBookTxnCCFailed(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.ADVANCE_DEPOSIT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly)
                .setTxnValue("2.00")
                .setMerchantKey(Constants.MerchantType.PGOnly.getKey())
                .setpeonUrl("https://pgp-automation.paytm.in/mockbank/peon")
                .setAggrMid(Constants.MerchantType.ADVANCE_DEPOSIT.getId())
                .build(merchantType.getKey());
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .setAggMid(merchantType.getId())
                .setMID(Constants.MerchantType.PGOnly.getId())
                .build();
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey()).assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("227")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().equals("HDFC Bank"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("CC"),
                peon.respCode().equals("227"),
                peon.status().equals("TXN_FAILURE"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid(Constants.MerchantType.ADVANCE_DEPOSIT.getKey())

        );
        sAssert.eval();
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = merchant.execute();
        JsonPath jsonPath = response.jsonPath();
        String MerchantResBody = GetJSONStringBody(response);
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(initTxnDTO.txnAmountFromBody());
        Assertions.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());
        Assertions.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        Assertions.assertThat(jsonPath.getString("body.paymentMode")).isEqualToIgnoringCase("CC");
        Boolean isValidChecksum = PGPHelpers.verifyKhataMerchantcheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey(), MerchantResBody, jsonPath.getString("head.signature"));
        Assertions.assertThat(isValidChecksum).as("Checksum Validation failed in GetPaymentStatus Response").isTrue();

    }


////////////////////////////////////////////////////////////////////////


    @Parameters({"theme"})
    @Test(description = "Khatabook CC App Invoke Transaction")
    public void TC001_KhatabookAppInvokeCC(@Optional("enhancedweb") String theme) throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.ADVANCE_DEPOSIT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly)
                .setTxnValue("2.00")
                .setpeonUrl("https://pgp-automation.paytm.in/mockbank/peon")
                .setAggrMid(Constants.MerchantType.ADVANCE_DEPOSIT.getId())
                .build(merchantType.getKey());
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.PGOnly, initTxnDTO.getBody().getOrderId(), txnToken)
                .setAggMid(Constants.MerchantType.ADVANCE_DEPOSIT.getId())
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey()).assertAll();
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
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().equals("HDFC Bank"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("CC"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid(Constants.MerchantType.ADVANCE_DEPOSIT.getKey())

        );
        sAssert.eval();

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = merchant.execute();
        JsonPath jsonPath = response.jsonPath();
        String MerchantResBody = GetJSONStringBody(response);
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(initTxnDTO.txnAmountFromBody());
        Assertions.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());
        Assertions.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        Assertions.assertThat(jsonPath.getString("body.paymentMode")).isEqualToIgnoringCase("CC");
        Boolean isValidChecksum = PGPHelpers.verifyKhataMerchantcheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey(), MerchantResBody, jsonPath.getString("head.signature"));
        Assertions.assertThat(isValidChecksum).as("Checksum Validation failed in GetPaymentStatus Response").isTrue();

    }


    @Parameters({"theme"})
    @Test(description = "Khatabook DC App Invoke Transaction")
    public void TC002_KhatabookAppInvokeDC(@Optional("enhancedweb") String theme) throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.ADVANCE_DEPOSIT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly)
                .setTxnValue("2.00")
                .setpeonUrl("https://pgp-automation.paytm.in/mockbank/peon")
                .setAggrMid(Constants.MerchantType.ADVANCE_DEPOSIT.getId())
                .build(merchantType.getKey());
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.PGOnly, initTxnDTO.getBody().getOrderId(), txnToken)
                .setAggMid(Constants.MerchantType.ADVANCE_DEPOSIT.getId())
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey()).assertAll();
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
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().equals("HDFC Bank"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("DC"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid(Constants.MerchantType.ADVANCE_DEPOSIT.getKey())

        );
        sAssert.eval();
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = merchant.execute();
        JsonPath jsonPath = response.jsonPath();
        String MerchantResBody = GetJSONStringBody(response);
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(initTxnDTO.txnAmountFromBody());
        Assertions.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());
        Assertions.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        Assertions.assertThat(jsonPath.getString("body.paymentMode")).isEqualToIgnoringCase("DC");
        Boolean isValidChecksum = PGPHelpers.verifyKhataMerchantcheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey(), MerchantResBody, jsonPath.getString("head.signature"));
        Assertions.assertThat(isValidChecksum).as("Checksum Validation failed in GetPaymentStatus Response").isTrue();
    }


    @Parameters({"theme"})
    @Test(description = "Khatabook UPI App Invoke Transaction")
    public void TC003_KhatabookAppInvokeUPI(@Optional("enhancedweb") String theme) throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.ADVANCE_DEPOSIT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly)
                .setTxnValue("2.00")
                .setpeonUrl("https://pgp-automation.paytm.in/mockbank/peon")
                .setAggrMid(Constants.MerchantType.ADVANCE_DEPOSIT.getId())
                .build(merchantType.getKey());
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.PGOnly, initTxnDTO.getBody().getOrderId(), txnToken)
                .setAggMid(Constants.MerchantType.ADVANCE_DEPOSIT.getId())
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey()).assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("ICICI"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid(Constants.MerchantType.ADVANCE_DEPOSIT.getKey())

        );
        sAssert.eval();
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = merchant.execute();
        JsonPath jsonPath = response.jsonPath();
        String MerchantResBody = GetJSONStringBody(response);
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(initTxnDTO.txnAmountFromBody());
        Assertions.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.ICICI.toString());
        Assertions.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        Assertions.assertThat(jsonPath.getString("body.paymentMode")).isEqualToIgnoringCase("UPI");
        PGPHelpers.verifyKhataMerchantcheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey(), MerchantResBody, jsonPath.getString("head.signature"));

    }


    @Parameters({"theme"})
    @Test(description = "Khatabook NB App Invoke Transaction")
    public void TC004_KhatabookAppInvokeNB(@Optional("enhancedweb") String theme) throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.ADVANCE_DEPOSIT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly)
                .setTxnValue("2.00")
                .setpeonUrl("https://pgp-automation.paytm.in/mockbank/peon")
                .setAggrMid(Constants.MerchantType.ADVANCE_DEPOSIT.getId())
                .build(merchantType.getKey());
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.PGOnly, initTxnDTO.getBody().getOrderId(), txnToken)
                .setAggMid(Constants.MerchantType.ADVANCE_DEPOSIT.getId())
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setBankName("ICICI");
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateBankName(Constants.Bank.ICICINB.toString())
                .validateCheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey())
                .assertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().equals("ICICI"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("ICICI"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("NB"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid(Constants.MerchantType.ADVANCE_DEPOSIT.getKey())

        );
        sAssert.eval();
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = merchant.execute();
        JsonPath jsonPath = response.jsonPath();
        String MerchantResBody = GetJSONStringBody(response);
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(initTxnDTO.txnAmountFromBody());
        Assertions.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.ICICI.toString());
        Assertions.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        Assertions.assertThat(jsonPath.getString("body.paymentMode")).isEqualToIgnoringCase("NB");
        Boolean isValidChecksum = PGPHelpers.verifyKhataMerchantcheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey(), MerchantResBody, jsonPath.getString("head.signature"));
        Assertions.assertThat(isValidChecksum).as("Checksum Validation failed in GetPaymentStatus Response").isTrue();
    }

    @Parameters({"theme"})
    @Test(description = "Khatabook ADDNPAY App Invoke Transaction")
    public void TC005_KhatabookAppInvokeADDNPAY(@Optional("enhancedweb") String theme) throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.ADVANCE_DEPOSIT;
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user,1.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.AddnPay)
                .setTxnValue("2.00")
                .setpeonUrl("https://pgp-automation.paytm.in/mockbank/peon")
                .setAggrMid(Constants.MerchantType.ADVANCE_DEPOSIT.getId())
                .build(merchantType.getKey());
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.AddnPay, initTxnDTO.getBody().getOrderId(), txnToken)
                .setAggMid(Constants.MerchantType.ADVANCE_DEPOSIT.getId())
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey()).assertAll();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey())
                .assertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().equals("WALLET"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("WALLET"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("PPI"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid(Constants.MerchantType.ADVANCE_DEPOSIT.getKey())

        );
        sAssert.eval();
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = merchant.execute();
        JsonPath jsonPath = response.jsonPath();
        String MerchantResBody = GetJSONStringBody(response);
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(initTxnDTO.txnAmountFromBody());
        Assertions.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase("WALLET");
        Assertions.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        Assertions.assertThat(jsonPath.getString("body.paymentMode")).isEqualToIgnoringCase("PPI");
        Boolean isValidChecksum = PGPHelpers.verifyKhataMerchantcheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey(), MerchantResBody, jsonPath.getString("head.signature"));
        Assertions.assertThat(isValidChecksum).as("Checksum Validation failed in GetPaymentStatus Response").isTrue();

    }

    @Parameters({"theme"})
    @Test(description = "Khatabook HYBRID App Invoke Transaction")
    public void TC006_KhatabookAppInvokeHYBRID(@Optional("enhancedweb") String theme) throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.ADVANCE_DEPOSIT;
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user, 1.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid)
                .setTxnValue("2.00")
                .setMerchantKey(Constants.MerchantType.Hybrid.getKey())
                .setpeonUrl("https://pgp-automation.paytm.in/mockbank/peon")
                .setAggrMid(Constants.MerchantType.ADVANCE_DEPOSIT.getId())
                .build(merchantType.getKey());
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        Validate_FetchPayInstrument(txnToken, initTxnDTO, "CREDIT_CARD", "false");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.Hybrid, initTxnDTO.getBody().getOrderId(), txnToken)
                .setAggMid(Constants.MerchantType.ADVANCE_DEPOSIT.getId())
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey()).assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateChildTxnsPresent()
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "CHILDTXNLIST","TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("HYBRID"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid(Constants.MerchantType.ADVANCE_DEPOSIT.getKey())

        );
        sAssert.eval();
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = merchant.execute();
        JsonPath jsonPath = response.jsonPath();
        ObjectMapper object = new ObjectMapper();
        //jsonPath.getMap("body").remove("childTransaction");
        GetPaymentStatusResponseDTO getPaymentStatusResponseDTO =
                object.readValue(JsonOutput.toJson(jsonPath.getMap(".")), GetPaymentStatusResponseDTO.class);
        Gson gson = new Gson();
        String MerchantResBody= gson.toJson(getPaymentStatusResponseDTO.getBody());
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(initTxnDTO.txnAmountFromBody());
        Assertions.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        Assertions.assertThat(jsonPath.getString("body.paymentMode")).isEqualToIgnoringCase("HYBRID");
        Boolean isValidChecksum = PGPHelpers.verifyKhataMerchantcheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey(), MerchantResBody, jsonPath.getString("head.signature"));
        Assertions.assertThat(isValidChecksum).as("Checksum Validation failed in GetPaymentStatus Response").isTrue();
    }



    @Parameters({"theme"})
    @Test(description = "Khatabook App Invoke Transaction Wallet")
    public void TC007_KhatabookAppInvokeWallet(@Optional("enhancedweb") String theme) throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.ADVANCE_DEPOSIT;
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user, 2.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.WALLETPEON)
                .setTxnValue("2.00")
                .setpeonUrl("https://pgp-automation.paytm.in/mockbank/peon")
                .setAggrMid(Constants.MerchantType.ADVANCE_DEPOSIT.getId())
                .build(merchantType.getKey());
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.WALLETPEON, initTxnDTO.getBody().getOrderId(), txnToken)
                .setAggMid(Constants.MerchantType.ADVANCE_DEPOSIT.getId())
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey())
                .assertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().equals("WALLET"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("WALLET"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("PPI"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid(Constants.MerchantType.ADVANCE_DEPOSIT.getKey())

        );
        sAssert.eval();
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = merchant.execute();
        JsonPath jsonPath = response.jsonPath();
        String MerchantResBody = GetJSONStringBody(response);
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(initTxnDTO.txnAmountFromBody());
        Assertions.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase("WALLET");
        Assertions.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        Assertions.assertThat(jsonPath.getString("body.paymentMode")).isEqualToIgnoringCase("PPI");
        Boolean isValidChecksum = PGPHelpers.verifyKhataMerchantcheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey(), MerchantResBody, jsonPath.getString("head.signature"));
        Assertions.assertThat(isValidChecksum).as("Checksum Validation failed in GetPaymentStatus Response").isTrue();

    }

    @Parameters({"theme"})
    @Test(description = "Khatabook CC App Invoke Transaction for failed txn")
    public void TC008_KhatabookAppInvokeCCforFailedTxn(@Optional("enhancedweb") String theme) throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.ADVANCE_DEPOSIT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly)
                .setTxnValue("2.00")
                .setpeonUrl("https://pgp-automation.paytm.in/mockbank/peon")
                .setAggrMid(Constants.MerchantType.ADVANCE_DEPOSIT.getId())
                .build(merchantType.getKey());
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.PGOnly, initTxnDTO.getBody().getOrderId(), txnToken)
                .setAggMid(merchantType.getId())
                .build();


        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("227")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey())
                .assertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().equals("HDFC Bank"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("CC"),
                peon.respCode().equals("227"),
                peon.respMsg().equals("Looks like OTP entered was incorrect. Please try again."),
                peon.status().equals("TXN_FAILURE"),
                peon.txnAmt().equals(initTxnDTO.txnAmountFromBody()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid(Constants.MerchantType.ADVANCE_DEPOSIT.getKey())

        );
        sAssert.eval();
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = merchant.execute();
        JsonPath jsonPath = response.jsonPath();
        String MerchantResBody = GetJSONStringBody(response);
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_Failure");
        Assertions.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(initTxnDTO.txnAmountFromBody());
        Assertions.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());
        Assertions.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        Assertions.assertThat(jsonPath.getString("body.paymentMode")).isEqualToIgnoringCase("CC");
        Boolean isValidChecksum = PGPHelpers.verifyKhataMerchantcheckSum(Constants.MerchantType.ADVANCE_DEPOSIT.getKey(), MerchantResBody, jsonPath.getString("head.signature"));
        Assertions.assertThat(isValidChecksum).as("Checksum Validation failed in GetPaymentStatus Response").isTrue();


    }
}

