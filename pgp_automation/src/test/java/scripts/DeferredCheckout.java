package scripts;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.theia.FetchCardDetails;
import com.paytm.api.theia.FetchCardIndexNumber;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchCardDetailsDTO.FetchCardDetailsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.*;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.Peon;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Story;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.asserts.Assertion;


import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.paytm.appconstants.Constants.Owner.MEHUL_GUPTA;

@Owner("Gagandeep")
public class DeferredCheckout extends PGPBaseTest implements IAddMoney{

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final RiskRejectHelper riskRejectHelper = new RiskRejectHelper();

    private FetchPaymentOptResponseDTO fetchPaymentOpt(User user, String paymentMode, String amount, String mid) throws IOException {
        FetchPaymentOptResponseDTO fetchPaymentOptResponse;
        if (amount == "")
        {
            fetchPaymentOptResponse = fetchPaymentOptionResponse(user,true, mid);
        }
         else {
             fetchPaymentOptResponse = fetchPaymentOptionResponse(user, true, Double.valueOf(amount), mid);
        }
        System.out.println(fetchPaymentOptResponse.getBody().getOrderId());
        boolean status = QRHelper.validatePaymentModeEnabled(fetchPaymentOptResponse, paymentMode, false);
        Assertions.assertThat(status).as(paymentMode + " is not enabled on fetchPaymentOption API").isTrue();
        return fetchPaymentOptResponse;
    }

    private static FetchPaymentOptResponseDTO fetchPaymentOptionResponse(User user, boolean generateOrderId, Double amount, String mid) throws IOException {
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(generateOrderId))
                .setMid(mid)
                .setToken(user.ssoToken())
                .setAmount(amount)
                .build();
        return PGPHelpers.executeFetchPaymentOpt(mid, "", fetchPaymentOptionsDTO, generateOrderId);
    }

        //override
        private static FetchPaymentOptResponseDTO fetchPaymentOptionResponse(User user, boolean generateOrderId, String mid) throws IOException {
            FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                    .setTokenType("SSO")
                    .setGenerateOrderId(Boolean.toString(generateOrderId))
                    .setMid(mid)
                    .setToken(user.ssoToken())
                    .build();
            return PGPHelpers.executeFetchPaymentOpt(mid, "", fetchPaymentOptionsDTO, generateOrderId);
    }

    protected RiskExtendInfo riskExtendInfoDTO() {

        return new RiskExtendInfo()
                .setOperationOrigin("APP").setOperationType("rent payment").setRentMonthYear("Dec2019").setRentPerMonth("100000")
                .setUserMerchant("216810000016528000000").setIsRentalsPayment("True").setPaytmMerchantId("amanja38238280372187").setIFSC("SBIN000322")
                .setPanCard("CPGPK1767M").setSelfAccount("TRUE").setPanNameMatchFlag("TRUE").setBankAccountNameMatchFlag("TRUE").setIsHighRiskBankAccount("TRUE").setCpId("216810000016528000000")
                .setCpFirstName("A").setCpMiddleName("B").setCpLastName("C").setCpName("C").setCpEmail("abc@gmail.com").setCpMobile("8989898989").setCpIdentityType("ID_CARD").setCpIdentityNo("5bb910960d8a026159d331777b7a9ba7233351e68561485d17a3bc65faff1c2f")
                .setCpCountry("IN").setCpState("UP").setCpCity("Ghaziabad").setCpArea("indirapuram").setCpPostalCode("201010").setCpStreet1("windsor street").setCpStreet2("windsor street")
                .setCpAddress("plot 211 , Abhay khand 1").setCpPaytmUserId("1107223757").setCpAccountNo("9199939333999939").setCpIFSC("SBIN2022202")
                .setCpglobalCardIndex("20200605666006118312bd3b9f606c4879ede6b9fa5d4").setCpVPA("1107223757").setCpVpaName("xyz");
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Validate Success CC transaction using Deferred flow where amount is passed in Fetch Pay options")
    public void validateCCTxn_DeferredCheckout_WithAmountInFetchPayOption(@Optional("false") boolean isNativePlus) throws Exception {
        String paymentMode = "CREDIT_CARD";
        String txnAmount = "2.00";

        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user,paymentMode,txnAmount,merchantType.getId());
        String orderId= fetchPaymentOptResponse.getBody().getOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setOrderId(orderId)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentMode).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(merchantType.getId())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Validate Success CC add money transaction using Deferred flow where amount is not passed in Fetch Pay options")
    public void validateCCAddMoneyTxn_DeferredCheckout_WithoutAmountInFetchPayOption(@Optional("false") boolean isNativePlus) throws Exception {
        String paymentMode = "CREDIT_CARD";

        Constants.MerchantType merchantType = Constants.MerchantType.AddMoneyMP;
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user,paymentMode,"",merchantType.getId());
        String orderId= fetchPaymentOptResponse.getBody().getOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setOrderId(orderId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentMode).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(merchantType.getId())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate Success CC git Add money transaction using Deferred flow where amount is passed in Fetch Pay options")
    public void validateCCAddMoneyTxn_DeferredCheckout_WithAmountInFetchPayOption(@Optional("false") boolean isNativePlus) throws Exception {
        String paymentMode = "CREDIT_CARD";
        String txnAmount = "2.00";

        Constants.MerchantType merchantType = Constants.MerchantType.AddMoneyMP;
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user,paymentMode,txnAmount,merchantType.getId());
        String orderId= fetchPaymentOptResponse.getBody().getOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setOrderId(orderId)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentMode).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(merchantType.getId())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate Success DC transaction using Deferred flow where amount is passed in Fetch Pay options")
    public void validateDCTxn_DeferredCheckout_WithAmountInFetchPayOption(@Optional("false") boolean isNativePlus) throws Exception {
        String paymentMode = "DEBIT_CARD";
        String txnAmount = "2.00";

        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user,paymentMode,txnAmount,merchantType.getId());
        String orderId= fetchPaymentOptResponse.getBody().getOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setOrderId(orderId)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentMode).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(merchantType.getId())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate Success NB transaction using Deferred flow where amount is passed in Fetch Pay options")
    public void validateNBTxn_DeferredCheckout_WithAmountInFetchPayOption(@Optional("false") boolean isNativePlus) throws Exception {
        String txnAmount = "2.00";

        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user,"NET_BANKING",txnAmount,merchantType.getId());
        String orderId= fetchPaymentOptResponse.getBody().getOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setOrderId(orderId)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI").build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.ICICINB.toString())
                .validateMid(merchantType.getId())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate Success wallet transaction using Deferred flow where amount is passed in Fetch Pay options")
    public void validatePPITxn_DeferredCheckout_WithAmountInFetchPayOption(@Optional("false") boolean isNativePlus) throws Exception {
        String txnAmount = "2.00";
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        WalletHelpers.modifyBalance(user,Double.valueOf(txnAmount));
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user,"BALANCE",txnAmount,merchantType.getId());
        String orderId= fetchPaymentOptResponse.getBody().getOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setOrderId(orderId)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.BALANCE).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(merchantType.getId())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user,0.0);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate Success Add and pay transaction using Deferred flow where amount is passed in Fetch Pay options")
    public void validateADDANDPAYTxn_DeferredCheckout_WithAmountInFetchPayOption(@Optional("false") boolean isNativePlus) throws Exception {
        String txnAmount = "2.00";
        Constants.MerchantType merchantType = Constants.MerchantType.AddnPay;
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        WalletHelpers.modifyBalance(user,Double.valueOf(txnAmount)-1.0);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user,"BALANCE",txnAmount,merchantType.getId());
        String orderId= fetchPaymentOptResponse.getBody().getOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setOrderId(orderId)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.CREDIT_CARD)
                .setPaymentFlow("ADDANDPAY").build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(merchantType.getId())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user,0.0);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate Success UPI transaction using Deferred flow where amount is passed in Fetch Pay options")
    public void validateUPITxn_DeferredCheckout_WithAmountInFetchPayOption(@Optional("false") boolean isNativePlus) throws Exception {
        String txnAmount = "2.00";
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user,"UPI",txnAmount,merchantType.getId());
        String orderId= fetchPaymentOptResponse.getBody().getOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setOrderId(orderId)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.UPI)
                .setPayerAccount("test@paytm").build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(merchantType.getId())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate Success SAVED CARD transaction using Deferred flow where amount is passed in Fetch Pay options")
    public void validateSAVEDCARDYTxn_DeferredCheckout_WithAmountInFetchPayOption(@Optional("false") boolean isNativePlus) throws Exception {
        String txnAmount = "2.00";
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        paymentDTO.setSavedCardId(SavedCardHelpers.getSavedCardId(user, 0)).setCvvNumber("123");
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user,"CREDIT_CARD",txnAmount,merchantType.getId());
        String orderId= fetchPaymentOptResponse.getBody().getOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setOrderId(orderId)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken,paymentDTO, PayMethodType.CREDIT_CARD).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(merchantType.getId())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate Success Hybrid transaction using Deferred flow where amount is passed in Fetch Pay options")
    public void validateHybridTxn_DeferredCheckout_WithAmountInFetchPayOption(@Optional("false") boolean isNativePlus) throws Exception {
        String txnAmount = "2.00";
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        WalletHelpers.modifyBalance(user,Double.valueOf(txnAmount)-1.0);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user,"CREDIT_CARD",txnAmount,merchantType.getId());
        String orderId= fetchPaymentOptResponse.getBody().getOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setOrderId(orderId)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.CREDIT_CARD)
                .setPaymentFlow("HYBRID").build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(merchantType.getId())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent()
                .validateStatusAPIParameters();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, "1.00")
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Constants.Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Constants.Bank.HDFC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET,"1.00")
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();

        WalletHelpers.validateBalance(user,0.0);
    }


    @Epic(Constants.Sprint.SPRINT30_1)
    @Feature("PGP-19081")
    @Parameters({"isNativePlus"})
    @Test(description = "Validate Success DC transaction using Deferred flow where amount is passed in Fetch Pay options for PCF merchant")
    public void validateDCTxn_DeferredCheckout_WithAmountInFetchPayOptionForPCFMerchant(@Optional("false") boolean isNativePlus) throws Exception {
        String paymentMode = "DEBIT_CARD";
        String txnAmount = "2.00";

        Constants.MerchantType merchantType = Constants.MerchantType.NETBANK_PCF            ;
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        double WalletBalance = WalletHelpers.getWalletBalance(user);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user,paymentMode,txnAmount,merchantType.getId());
        String orderId= fetchPaymentOptResponse.getBody().getOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setOrderId(orderId)
                .setTxnValue(txnAmount)
                .setIsNativeAddMoney("true")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentMode).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(merchantType.getId())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, WalletBalance+Double.valueOf(initTxnDTO.txnAmountFromBody()));
    }

    @Epic(Constants.Sprint.SPRINT31_1)
    @Feature("PGP-20043")
    @Owner("Tarun")
    @Parameters({"isNativePlus"})
    @Test(description = "Validate Success CC transaction using Deferred flow where amount is passed in Fetch Pay options")
    public void deferredCheckoutRiskReject(@Optional("false") boolean isNativePlus) throws Exception {
        String paymentMode = "CREDIT_CARD";
        String txnAmount = riskRejectHelper.riskAmount;

        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user,paymentMode,txnAmount,merchantType.getId());
        String orderId= fetchPaymentOptResponse.getBody().getOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setOrderId(orderId)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentMode).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateRespMsg("Txn Success")
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateTxnAmount(txnAmount)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode(Constants.PayMode.CC.toString())
                .validateCurrency("INR")
                .validateTxnDate(new Date())
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .assertAll();
    }


    @Epic(Constants.Sprint.SPRINT31_2)
    @Story("PGP-20549")
    @Parameters({"isNativePlus"})
    @Test(description = "Validate Success txn when init api contains % symbol in Shipping and Goods using Deferred flow")
    public void succesfulOrderwhenApercentagesSignInInitForDeferred(@Optional("false") boolean isNativePlus) throws Exception {
        String paymentMode = "CREDIT_CARD";
        String txnAmount = "2.00";

        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user,paymentMode,txnAmount,merchantType.getId());
        String orderId= fetchPaymentOptResponse.getBody().getOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setOrderId(orderId)
                .build();

        ShippingInfo shippingInfo = new ShippingInfo();
        shippingInfo.setAddress1("137WSanBer%nard%ino");

        Good goodInfo = new Good();
        goodInfo.setDescription("Women%Sum%mer%Dress");

        initTxnDTO.getBody().setGoods(new Good[]{goodInfo});

        initTxnDTO.getBody().setShippingInfo(new ShippingInfo[]{shippingInfo});
        String checksum = PGPHelpers.getNativeChecksum(merchantType.getKey(), initTxnDTO.getBody());
        initTxnDTO.getHead().setSignature(checksum);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentMode).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(merchantType.getId())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }


    //////////////////////// AddMoney Cases //////////////////////////////////////////


    //Deferred Native Plus Flow (used in Adding Money through APP)

    @Override
    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    public void validateFullKYCWalletCC(@Optional("enhancedweb_revamp") String theme) throws Exception {

        Constants.MerchantType scwMerchant = Constants.MerchantType.AddMoneyMP;
        PaymentDTO paymentDTO = new PaymentDTO();
        Double txnAmount = 10001.00;
        User user = userManager.getForWrite(Label.LOGIN);

        //Fetch Payment Options
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(scwMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(scwMerchant.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();

        Double feePercent = fetchPaymentOptResponse.getDouble("body.riskConvenienceFee[0].feePercent");
        Double feeAmount = txnAmount*feePercent/100;
        Double totalAmount = txnAmount + feeAmount;

        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();

        //Fetch CIN
        FetchCardIndexNumber fetchCardIndexNumber = new FetchCardIndexNumber(scwMerchant,paymentDTO.getCreditCardNumber(),paymentDTO.getExpMonth(),paymentDTO.getExpYear());
        JsonPath fetchCINResponse = fetchCardIndexNumber.execute().jsonPath();
        String cin = fetchCINResponse.getString("body.cardIndexNumber");

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(scwMerchant.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,orderId);
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),scwMerchant)
                .setTxnValue(totalAmount.toString())
                .setOrderId(orderId)
                .setCardHash(cardHash)
                .setRiskFeeDetails(new RiskFeeDetails().setFeeAmount(feeAmount.toString()).setInitialAmount(txnAmount.toString()))
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setCardInfo(cin+"||"+paymentDTO.getCvvNumber()+"|") // Txn through CIN
                .setRiskExtendInfo("feeApplied:true")
                .build();

        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(scwMerchant.getId(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(totalAmount.toString())
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFCSC.toString())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(scwMerchant.getId())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateCardHash(Constants.ValidationType.NON_EMPTY)
                .AssertAll();

    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateFullKYCWalletSavedCC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType scwMerchant = Constants.MerchantType.AddMoneyMP;
        Double txnAmount = 10001.00;
        User user = userManager.getForWrite(Label.LOGIN);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        paymentDTO.setSavedCardId(SavedCardHelpers.getSavedCardId(user, 0)).setCvvNumber(paymentDTO.getCvvNumber());

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(scwMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(scwMerchant.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();

        Double feePercent = fetchPaymentOptResponse.getDouble("body.riskConvenienceFee[0].feePercent");
        Double feeAmount = txnAmount*feePercent/100;
        Double totalAmount = txnAmount + feeAmount;

        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(scwMerchant.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,orderId);
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),scwMerchant)
                .setTxnValue(totalAmount.toString())
                .setOrderId(orderId)
                .setCardHash(cardHash)
                .setRiskFeeDetails(new RiskFeeDetails().setFeeAmount(feeAmount.toString()).setInitialAmount(txnAmount.toString()))
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.CREDIT_CARD)
                .setRiskExtendInfo("feeApplied:true")
                .build();

        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(scwMerchant.getId(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(totalAmount.toString())
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFCBANK.toString())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(scwMerchant.getId())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateCardHash(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateMinKYCWalletLimitNotBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {

        Constants.MerchantType scwMerchant = Constants.MerchantType.AddMoneyMP;
        PaymentDTO paymentDTO = new PaymentDTO();
        Double txnAmount = 7777.0;
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(scwMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(scwMerchant.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();

        //Fetch CIN
        FetchCardIndexNumber fetchCardIndexNumber = new FetchCardIndexNumber(scwMerchant,paymentDTO.getCreditCardNumber(),paymentDTO.getExpMonth(),paymentDTO.getExpYear());
        JsonPath fetchCINResponse = fetchCardIndexNumber.execute().jsonPath();
        String cin = fetchCINResponse.getString("body.cardIndexNumber");

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(scwMerchant.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,orderId);
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        Response response = WalletHelpers.checkWalletLimit(user,String.valueOf(txnAmount),"MAIN",cardHash,cin,"CREDIT_CARD");

        Assertions.assertThat(response.jsonPath().getList("response.walletRbiType").get(0).toString()).isEqualTo("Min Kyc Expired");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),scwMerchant)
                .setTxnValue(txnAmount.toString())
                .setOrderId(orderId)
                .setCardHash(cardHash)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setCardInfo(cin+"||"+paymentDTO.getCvvNumber()+"|")
                .build();

        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(scwMerchant.getId(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount.toString())
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(scwMerchant.getId())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateCardHash(Constants.ValidationType.NON_EMPTY)
                .AssertAll();

    }

    @Override
    @Test(description = "MIN KYC Flow - To validate when more than 10000 Rs txn is done by CC for adding money to Wallet")
    @Owner("Tarun")
    @Feature("PGP-19696")
    public void validateMinKYCWalletLimitBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {

        Constants.MerchantType scwMerchant = Constants.MerchantType.AddMoneyMP;
        PaymentDTO paymentDTO = new PaymentDTO();
        Double txnAmount = 10001.0;
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(scwMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(scwMerchant.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(scwMerchant.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,orderId);
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        Response response = WalletHelpers.checkWalletLimit(user,String.valueOf(txnAmount),"MAIN",cardHash,"","CREDIT_CARD");

        Assertions.assertThat(response.jsonPath().getList("response.walletRbiType").get(0).toString()).isEqualTo("Min Kyc Expired");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),scwMerchant)
                .setTxnValue(txnAmount.toString())
                .setOrderId(orderId)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("You can buy up to Rs. 10,000 worth of Gift voucher per month using credit card. To continue, please use UPI or Debit card payment option")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();

    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateNoKYCWalletLimitNotBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.AddMoneyMP;
        Double txnAmount = 7777.0;
        User user = userManager.getForWrite(Label.BASICTOKYC);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();

        Response response = WalletHelpers.checkWalletLimit(user,String.valueOf(txnAmount),"MAIN","","","CREDIT_CARD");

        Assertions.assertThat(response.jsonPath().getList("response.walletRbiType").get(0).toString()).isEqualTo("Basic");


        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue(txnAmount.toString())
                .setOrderId(orderId)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, true);


    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateNoKYCWalletLimitBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.AddMoneyMP;
        Double txnAmount = 100001.0;
        User user = userManager.getForWrite(Label.BASICTOKYC);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();

        Response response = WalletHelpers.checkWalletLimit(user,String.valueOf(txnAmount),"MAIN","","","CREDIT_CARD");

        Assertions.assertThat(response.jsonPath().getList("response.walletRbiType").get(0).toString()).isEqualTo("Basic");


        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue(txnAmount.toString())
                .setOrderId(orderId)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("You can buy up to Rs. 10,000 worth of Gift voucher per month using credit card. To continue, please use UPI or Debit card payment option")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();

    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateFullKYCGV(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType scwMerchant = Constants.MerchantType.AddMoneyMP;
        PaymentDTO paymentDTO = new PaymentDTO();
        Double txnAmount = 1111.0;
        User user = userManager.getForWrite(Label.LOGIN);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(scwMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(scwMerchant.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();

        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();

        //Fetch CIN
        FetchCardIndexNumber fetchCardIndexNumber = new FetchCardIndexNumber(scwMerchant,paymentDTO.getCreditCardNumber(),paymentDTO.getExpMonth(),paymentDTO.getExpYear());
        JsonPath fetchCINResponse = fetchCardIndexNumber.execute().jsonPath();
        String cin = fetchCINResponse.getString("body.cardIndexNumber");

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(scwMerchant.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,orderId);
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        Response response = WalletHelpers.checkWalletLimit(user,String.valueOf(txnAmount),"GIFT_VOUCHER","","","CREDIT_CARD");

        Assertions.assertThat(response.jsonPath().getList("response.walletRbiType").get(0).toString()).isEqualTo("Premium");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),scwMerchant)
                .setTxnValue(txnAmount.toString())
                .setOrderId(orderId)
                .setCardHash(cardHash)
                .build();

        Good goodInfo = new Good();
        goodInfo.setMerchantGoodsId("154435058"); //Adding money in GV

        initTxnDTO.getBody().setGoods(new Good[]{goodInfo});

        String checksum = PGPHelpers.getNativeChecksum(scwMerchant.getKey(), initTxnDTO.getBody());
        initTxnDTO.getHead().setSignature(checksum);

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setCardInfo(cin+"||"+paymentDTO.getCvvNumber()+"|") // Txn through CIN
                .setRiskExtendInfo("feeApplied:true")
                .build();

        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(scwMerchant.getId(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount.toString())
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFCBANK.toString())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(scwMerchant.getId())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateCardHash(Constants.ValidationType.NON_EMPTY)
                .AssertAll();

    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateFullKYCGVSavedCC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.AddMoneyMP;
        Double txnAmount = 1111.0;
        User user = userManager.getForWrite(Label.LOGIN);

        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        paymentDTO.setSavedCardId(SavedCardHelpers.getSavedCardId(user, 0)).setCvvNumber(paymentDTO.getCvvNumber());

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();

        Response response = WalletHelpers.checkWalletLimit(user,String.valueOf(txnAmount),"GIFT_VOUCHER","","","CREDIT_CARD");

        Assertions.assertThat(response.jsonPath().getList("response.walletRbiType").get(0).toString()).isEqualTo("Premium");

        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue(txnAmount.toString())
                .setOrderId(orderId)
                .build();

        Good goodInfo = new Good();
        goodInfo.setMerchantGoodsId("154435058"); //Adding money in GV

        initTxnDTO.getBody().setGoods(new Good[]{goodInfo});

        String checksum = PGPHelpers.getNativeChecksum(merchantType.getKey(), initTxnDTO.getBody());
        initTxnDTO.getHead().setSignature(checksum);

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.CREDIT_CARD)
                .setRiskExtendInfo("feeApplied:true")
                .build();

        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount.toString())
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFCBANK.toString())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(merchantType.getId())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateCardHash(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
    }



    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateMinKYCLimitNotBreachedGV(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType scwMerchant = Constants.MerchantType.AddMoneyMP;
        PaymentDTO paymentDTO = new PaymentDTO();
        Double txnAmount = 1111.0;
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(scwMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(scwMerchant.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();

        Response response = WalletHelpers.checkWalletLimit(user,String.valueOf(txnAmount),"GIFT_VOUCHER","","","CREDIT_CARD");

        Assertions.assertThat(response.jsonPath().getList("response.walletRbiType").get(0).toString()).isEqualTo("Min Kyc Expired");

        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(scwMerchant.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,orderId);
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),scwMerchant)
                .setTxnValue(txnAmount.toString())
                .setOrderId(orderId)
                .setCardHash(cardHash)
                .build();

        Good goodInfo = new Good();
        goodInfo.setMerchantGoodsId("154435058"); //Adding money in GV

        initTxnDTO.getBody().setGoods(new Good[]{goodInfo});

        String checksum = PGPHelpers.getNativeChecksum(scwMerchant.getKey(), initTxnDTO.getBody());
        initTxnDTO.getHead().setSignature(checksum);

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.CREDIT_CARD)
                .setRiskExtendInfo("feeApplied:true")
                .build();

        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(scwMerchant.getId(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount.toString())
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFCBANK.toString())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(scwMerchant.getId())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateCardHash(Constants.ValidationType.NON_EMPTY)
                .AssertAll();


    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateMinKYCLimitBreachedGV(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType scwMerchant = Constants.MerchantType.AddMoneyMP;
        PaymentDTO paymentDTO = new PaymentDTO();
        Double txnAmount = 10001.0;
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(scwMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(scwMerchant.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();

        Response response = WalletHelpers.checkWalletLimit(user,String.valueOf(txnAmount),"GIFT_VOUCHER","","","CREDIT_CARD");

        Assertions.assertThat(response.jsonPath().getList("response.walletRbiType").get(0).toString()).isEqualTo("Min Kyc Expired");

        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(scwMerchant.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,orderId);
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),scwMerchant)
                .setTxnValue(txnAmount.toString())
                .setOrderId(orderId)
                .setCardHash(cardHash)
                .build();

        Good goodInfo = new Good();
        goodInfo.setMerchantGoodsId("154435058"); //Adding money in GV

        initTxnDTO.getBody().setGoods(new Good[]{goodInfo});

        String checksum = PGPHelpers.getNativeChecksum(scwMerchant.getKey(), initTxnDTO.getBody());
        initTxnDTO.getHead().setSignature(checksum);

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.CREDIT_CARD)
                .setRiskExtendInfo("feeApplied:true")
                .build();

        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("You can buy up to Rs. 10,000 worth of Gift voucher per month using credit card. To continue, please use UPI or Debit card payment option")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();

    }


    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateNoKYCGV(@Optional("enhancedweb_revamp") String theme) throws Exception {

        Constants.MerchantType scwMerchant = Constants.MerchantType.AddMoneyMP;
        Double txnAmount = 1111.0;
        User user = userManager.getForWrite(Label.BASICTOKYC);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(scwMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(scwMerchant.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();

        Response response = WalletHelpers.checkWalletLimit(user,String.valueOf(txnAmount),"GIFT_VOUCHER","","","CREDIT_CARD");

        Assertions.assertThat(response.jsonPath().getList("response.walletRbiType").get(0).toString()).isEqualTo("Basic");

        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),scwMerchant)
                .setTxnValue(txnAmount.toString())
                .setOrderId(orderId)
                .build();

        Good goodInfo = new Good();
        goodInfo.setMerchantGoodsId("154435058"); //Adding money in GV

        initTxnDTO.getBody().setGoods(new Good[]{goodInfo});

        String checksum = PGPHelpers.getNativeChecksum(scwMerchant.getKey(), initTxnDTO.getBody());
        initTxnDTO.getHead().setSignature(checksum);

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(scwMerchant.getId(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount.toString())
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFCBANK.toString())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(scwMerchant.getId())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateCardHash(Constants.ValidationType.NON_EMPTY)
                .AssertAll();

    }

    //New Flow - Wallet is not getting called

    @Test(description = "New App New Flow : To test txn should get success if feeApplied is true in PTC for txnAmount even greater than 10000")
    @Owner("Tarun")
    @Feature("PGP-19696")
    public void validateFullKYCWalletNewFlow() throws Exception {

        Constants.MerchantType scwMerchant = Constants.MerchantType.AddMoneyMP;
        PaymentDTO paymentDTO = new PaymentDTO();
        Double txnAmount = 10001.00;
        User user = userManager.getForWrite(Label.LOGIN);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(scwMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setAddMoneyFeeAppliedOnWallet(true) //New flow
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(scwMerchant.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();

        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();

        //Fetch CIN
        FetchCardIndexNumber fetchCardIndexNumber = new FetchCardIndexNumber(scwMerchant,paymentDTO.getCreditCardNumber(),paymentDTO.getExpMonth(),paymentDTO.getExpYear());
        JsonPath fetchCINResponse = fetchCardIndexNumber.execute().jsonPath();
        String cin = fetchCINResponse.getString("body.cardIndexNumber");

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(scwMerchant.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,orderId);
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),scwMerchant)
                .setTxnValue(txnAmount.toString())
                .setOrderId(orderId)
                .setCardHash(cardHash)
                .setAddMoneyFeeAppliedOnWallet(true) //New flow
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setRiskExtendInfo("feeApplied:true")
                .setCardInfo(cin+"||"+paymentDTO.getCvvNumber()+"|") // Txn through CIN
                .build();

        checkoutPage.createNativeOrder(orderDTO, true);


    }

    @Test(description = "New App New Flow : To test txn should get success with savedCard if feeApplied is true in PTC for txnAmount even greater than 10000")
    @Owner("Tarun")
    @Feature("PGP-19696")
    public void validateFullKYCWalletSavedCCNewFlow() throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.AddMoneyMP;
        Double txnAmount = 10001.00;
        User user = userManager.getForWrite(Label.LOGIN);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        paymentDTO.setSavedCardId(SavedCardHelpers.getSavedCardId(user, 0)).setCvvNumber(paymentDTO.getCvvNumber());

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setAddMoneyFeeAppliedOnWallet(true)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();

        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue(txnAmount.toString())
                .setOrderId(orderId)
                .setAddMoneyFeeAppliedOnWallet(true)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.CREDIT_CARD)
                .setRiskExtendInfo("feeApplied:true")
                .build();

        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount.toString())
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFCBANK.toString())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(merchantType.getId())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateCardHash(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
    }

    @Test(description = "New APP New Flow - To validate when more than 10000 Rs txn is done by CC for adding money to Wallet")
    @Owner("Tarun")
    @Feature("PGP-19696")
    public void validateMinKYCWalletWalletNewApp() throws Exception {

        Constants.MerchantType scwMerchant = Constants.MerchantType.AddMoneyMP;
        PaymentDTO paymentDTO = new PaymentDTO();
        Double txnAmount = 10001.0;
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(scwMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setAddMoneyFeeAppliedOnWallet(true)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(scwMerchant.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(scwMerchant.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,orderId);
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        Response response = WalletHelpers.checkWalletLimit(user,String.valueOf(txnAmount),"MAIN",cardHash,"","CREDIT_CARD");

        Assertions.assertThat(response.jsonPath().getList("response.walletRbiType").get(0).toString()).isEqualTo("Min Kyc Expired");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),scwMerchant)
                .setTxnValue(txnAmount.toString())
                .setOrderId(orderId)
                .setAddMoneyFeeAppliedOnWallet(true)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(scwMerchant.getId(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount.toString())
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFCBANK.toString())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(scwMerchant.getId())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateCardHash(Constants.ValidationType.NON_EMPTY)
                .AssertAll();

    }

    @Test(description = "New APP New Flow : To validate when No KYC user is adding money to wallet")
    @Owner("Tarun")
    @Feature("PGP-19696")
    public void validateNoKYCWalletMorePNewFlow() throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.AddMoneyMP;
        Double txnAmount = 10001.0;
        User user = userManager.getForWrite(Label.BASICTOKYC);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setAddMoneyFeeAppliedOnWallet(true)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();

        Response response = WalletHelpers.checkWalletLimit(user,String.valueOf(txnAmount),"MAIN","","","CREDIT_CARD");

        Assertions.assertThat(response.jsonPath().getList("response.walletRbiType").get(0).toString()).isEqualTo("Basic");


        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue(txnAmount.toString())
                .setOrderId(orderId)
                .setAddMoneyFeeAppliedOnWallet(true)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount.toString())
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFCBANK.toString())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(merchantType.getId())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateCardHash(Constants.ValidationType.NON_EMPTY)
                .AssertAll();

    }

    @Test(description = "New APP New Flow : To validate full KYC user is adding money to GV")
    @Owner("Tarun")
    @Feature("PGP-19696")
    public void validateFullKYCGVSavedCCNewAPP() throws Exception {
        Constants.MerchantType scwMerchant = Constants.MerchantType.AddMoneyMP;
        Double txnAmount = 1111.0;
        User user = userManager.getForWrite(Label.LOGIN);

        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        paymentDTO.setSavedCardId(SavedCardHelpers.getSavedCardId(user, 0)).setCvvNumber(paymentDTO.getCvvNumber());

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(scwMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setAddMoneyFeeAppliedOnWallet(true)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(scwMerchant.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();

        Response response = WalletHelpers.checkWalletLimit(user,String.valueOf(txnAmount),"GIFT_VOUCHER","","","CREDIT_CARD");

        Assertions.assertThat(response.jsonPath().getList("response.walletRbiType").get(0).toString()).isEqualTo("Premium");

        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(scwMerchant.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,orderId);
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),scwMerchant)
                .setTxnValue(txnAmount.toString())
                .setOrderId(orderId)
                .setCardHash(cardHash)
                .setAddMoneyFeeAppliedOnWallet(true)
                .build();

        Good goodInfo = new Good();
        goodInfo.setMerchantGoodsId("154435058"); //Adding money in GV

        initTxnDTO.getBody().setGoods(new Good[]{goodInfo});

        String checksum = PGPHelpers.getNativeChecksum(scwMerchant.getKey(), initTxnDTO.getBody());
        initTxnDTO.getHead().setSignature(checksum);

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.CREDIT_CARD)
                .setRiskExtendInfo("feeApplied:true")
                .build();

        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(scwMerchant.getId(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount.toString())
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFCBANK.toString())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(scwMerchant.getId())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateCardHash(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
    }

    @Test(description = "New APP New Flow: To validate when No KYC user is adding money to GV")
    @Owner("Tarun")
    @Feature("PGP-19696")
    public void validateNoKYCGVNewFlow() throws Exception {

        Constants.MerchantType scwMerchant = Constants.MerchantType.AddMoneyMP;
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        Double txnAmount = 10001.0;
        User user = userManager.getForWrite(Label.BASICTOKYC);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(scwMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .setAddMoneyFeeAppliedOnWallet(true)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(scwMerchant.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();

        Response response = WalletHelpers.checkWalletLimit(user,String.valueOf(txnAmount),"GIFT_VOUCHER","","","CREDIT_CARD");

        Assertions.assertThat(response.jsonPath().getList("response.walletRbiType").get(0).toString()).isEqualTo("Basic");

        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(scwMerchant.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,orderId);
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");


        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),scwMerchant)
                .setTxnValue(txnAmount.toString())
                .setOrderId(orderId)
                .setCardHash(cardHash)
                .setAddMoneyFeeAppliedOnWallet(true)
                .build();

        Good goodInfo = new Good();
        goodInfo.setMerchantGoodsId("154435058"); //Adding money in GV

        initTxnDTO.getBody().setGoods(new Good[]{goodInfo});

        String checksum = PGPHelpers.getNativeChecksum(scwMerchant.getKey(), initTxnDTO.getBody());
        initTxnDTO.getHead().setSignature(checksum);

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(scwMerchant.getId(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount.toString())
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFCBANK.toString())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(scwMerchant.getId())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateCardHash(Constants.ValidationType.NON_EMPTY)
                .AssertAll();

    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateFullKYCDC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType scwMerchant = Constants.MerchantType.AddMoneyMP;
        PaymentDTO paymentDTO = new PaymentDTO();
        Double txnAmount = 10001.00;
        User user = userManager.getForWrite(Label.LOGIN);

        //Fetch Payment Options
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(scwMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(scwMerchant.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();

        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();

        //Fetch CIN
        FetchCardIndexNumber fetchCardIndexNumber = new FetchCardIndexNumber(scwMerchant,paymentDTO.getDebitCardNumber(),paymentDTO.getExpMonth(),paymentDTO.getExpYear());
        JsonPath fetchCINResponse = fetchCardIndexNumber.execute().jsonPath();
        String cin = fetchCINResponse.getString("body.cardIndexNumber");

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(scwMerchant.getId())
                .setCardNumber(paymentDTO.getDebitCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,orderId);
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),scwMerchant)
                .setTxnValue(txnAmount.toString())
                .setOrderId(orderId)
                .setCardHash(cardHash)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setCardInfo(cin+"||"+paymentDTO.getCvvNumber()+"|") // Txn through CIN
                .build();

        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(scwMerchant.getId(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount.toString())
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC_ONLY.toString())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(scwMerchant.getId())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateCardHash(Constants.ValidationType.NON_EMPTY)
                .AssertAll();

    }
    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateMinKycNB(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType scwMerchant = Constants.MerchantType.AddMoneyMP;
        Double txnAmount = 10001.00;
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);

        //Fetch Payment Options
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(scwMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(scwMerchant.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();

        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),scwMerchant)
                .setTxnValue(txnAmount.toString())
                .setOrderId(orderId)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI").build();

        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(scwMerchant.getId(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount.toString())
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateBankName(Constants.Bank.ICICINB.toString())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(scwMerchant.getId())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .AssertAll();
    }
    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateNoKYCDC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType scwMerchant = Constants.MerchantType.AddMoneyMP;
        PaymentDTO paymentDTO = new PaymentDTO();
        Double txnAmount = 10001.00;
        User user = userManager.getForWrite(Label.BASICTOKYC);

        //Fetch Payment Options
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(scwMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(scwMerchant.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();

        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();

        //Fetch CIN
        FetchCardIndexNumber fetchCardIndexNumber = new FetchCardIndexNumber(scwMerchant,paymentDTO.getDebitCardNumber(),paymentDTO.getExpMonth(),paymentDTO.getExpYear());
        JsonPath fetchCINResponse = fetchCardIndexNumber.execute().jsonPath();
        String cin = fetchCINResponse.getString("body.cardIndexNumber");

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(scwMerchant.getId())
                .setCardNumber(paymentDTO.getDebitCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,orderId);
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        //Initiate Txn
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),scwMerchant)
                .setTxnValue(txnAmount.toString())
                .setOrderId(orderId)
                .setCardHash(cardHash)
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(scwMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setCardInfo(cin+"||"+paymentDTO.getCvvNumber()+"|") // Txn through CIN
                .build();

        checkoutPage.createNativeOrder(orderDTO, true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(scwMerchant.getId(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(txnAmount.toString())
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFCBANK.toString())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(scwMerchant.getId())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateCardHash(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
    }





    @Feature("PGP-26956")
    @Owner("Gagandeep")
    @Parameters({"isNativePlus"})
    @Test(description = "Native : Verify on Onus Payment Risk_Accept , Risk_info not null for CC transaction with merchant status and Peon ")
    public void validateRiskAcceptNotNullForCC(@Optional("false") Boolean isNativePlus) throws Exception {

        Constants.MerchantType rentMerchant = Constants.MerchantType.PGOnly;

        User user = userManager.getForRead(Label.LOGIN);
        String riskAmount = "5.5";


        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user, "CREDIT_CARD", riskAmount, rentMerchant.getId());
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), rentMerchant)
                .setRiskExtendInfo(riskExtendInfoDTO())
                .setTxnValue(riskAmount)
                .setOrderId(orderId)
                .build();

        String token = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(rentMerchant, initTxnDTO.orderFromBody(), token, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateRiskInfo(Constants.ValidationType.NON_EMPTY)
                .assertAll();


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
                .validateRiskInfo(Constants.ValidationType.NON_EMPTY)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "cardScheme","RESPMSG", "riskInfo", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("CC"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.getBody().getTxnAmount().getValue()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.riskInfo().equals("").not()
        );
        sAssert.eval();
    }

    @Feature("PGP-26956")
    @Owner("Gagandeep")
    @Parameters({"isNativePlus"})
    @Test(description = "Native : Verify on Onus Payment Risk_Accept , Risk_info not null with Length exceeded 100 characters for DC transaction with merchant status and Peon ")
    public void validateRiskAcceptNotNullForDC(@Optional("false") Boolean isNativePlus) throws Exception {

        Constants.MerchantType rentMerchant = Constants.MerchantType.PGOnly;

        User user = userManager.getForRead(Label.LOGIN);
        String riskAmount = "5.6";


        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user, "DEBIT_CARD", riskAmount, rentMerchant.getId());
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), rentMerchant)
                .setRiskExtendInfo(riskExtendInfoDTO())
                .setOrderId(orderId)
                .setTxnValue(riskAmount)
                .build();

        String token = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(rentMerchant, initTxnDTO.orderFromBody(), token, PayMethodType.DEBIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateRiskInfo(Constants.ValidationType.NON_EMPTY)
                .assertAll();


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
                .validateRiskInfo(Constants.ValidationType.NON_EMPTY)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "riskInfo", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME","TXNDATE", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("DC"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.getBody().getTxnAmount().getValue()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.riskInfo().equals("").not()
        );
        sAssert.eval();
    }


    @Feature("PGP-26956")
    @Owner("Gagandeep")
    @Parameters({"isNativePlus"})
    @Test(description = "Native : Verify on Onus Payment Risk_Accept , Risk_info not null for UPI transaction with merchant status and Peon ")
    public void validateRiskAcceptNotNullForUPI(@Optional("false") Boolean isNativePlus) throws Exception {

        Constants.MerchantType rentMerchant = Constants.MerchantType.PGOnly;

        User user = userManager.getForRead(Label.LOGIN);
        String riskAmount = "5.5";


        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user, "UPI", riskAmount, rentMerchant.getId());
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), rentMerchant)
                .setRiskExtendInfo(riskExtendInfoDTO())
                .setOrderId(orderId)
                .setTxnValue(riskAmount)
                .build();

        String token = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(rentMerchant, initTxnDTO.orderFromBody(), token, PayMethodType.UPI)
                .setPayerAccount("test@paytm")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateRiskInfo(Constants.ValidationType.NON_EMPTY)
                .assertAll();


        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("ICICI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRiskInfo(Constants.ValidationType.NON_EMPTY)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "riskInfo", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("ICICI"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.getBody().getTxnAmount().getValue()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.riskInfo().equals("").not()
        );
        sAssert.eval();
    }


    @Feature("PGP-26956")
    @Owner("Gagandeep")
    @Parameters({"isNativePlus"})
    @Test(description = "Native : Verify on Onus Payment Risk_Accept , Risk_info not null for HYBRID transaction with merchant status and Peon ")
    public void validateRiskAcceptNotNullForHybrid(@Optional("false") Boolean isNativePlus) throws Exception {

        Constants.MerchantType rentMerchant = Constants.MerchantType.Hybrid;

        User user = userManager.getForRead(Label.LOGIN);
        WalletHelpers.modifyBalance(user, 1.00);

        String riskAmount = "5.5";

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user, "CREDIT_CARD", riskAmount, rentMerchant.getId());
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();


        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), rentMerchant)
                .setRiskExtendInfo(riskExtendInfoDTO())
                .setOrderId(orderId)
                .setTxnValue(riskAmount)
                .build();

        String token = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(rentMerchant, initTxnDTO.orderFromBody(), token, PayMethodType.CREDIT_CARD)
                .setPaymentFlow("HYBRID")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateRiskInfo(Constants.ValidationType.NON_EMPTY)
                .assertAll();


        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRiskInfo(Constants.ValidationType.NON_EMPTY)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "riskInfo", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "CHILDTXNLIST", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("HYBRID"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.getBody().getTxnAmount().getValue()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.riskInfo().equals("").not()
        );
        sAssert.eval();
    }


    @Feature("PGP-26956")
    @Owner("Gagandeep")
    @Parameters({"isNativePlus"})
    @Test(description = "Native : Verify on Onus Payment Risk_Accept , Risk_info not null for ADDNPAY transaction with merchant status and Peon ")
    public void validateRiskAcceptNotNullForADDNPAY(@Optional("false") Boolean isNativePlus) throws Exception {

        Constants.MerchantType rentMerchant = Constants.MerchantType.ADDNPAYPEON;

        User user = userManager.getForRead(Label.LOGIN);
        WalletHelpers.modifyBalance(user, 1.00);

        String riskAmount = "5.5";


        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user, "CREDIT_CARD", riskAmount, rentMerchant.getId());
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), rentMerchant)
                .setRiskExtendInfo(riskExtendInfoDTO())
                .setOrderId(orderId)
                .setTxnValue(riskAmount)
                .build();

        String token = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(rentMerchant, initTxnDTO.orderFromBody(), token, PayMethodType.CREDIT_CARD)
                .setPaymentFlow("ADDANDPAY")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateRiskInfo(Constants.ValidationType.NON_EMPTY)
                .assertAll();


        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRiskInfo(Constants.ValidationType.NON_EMPTY)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "riskInfo", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("WALLET"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("PPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.getBody().getTxnAmount().getValue()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.riskInfo().equals("").not()
        );
        sAssert.eval();
    }


    @Feature("PGP-26956")
    @Owner("Gagandeep")
    @Parameters({"isNativePlus"})
    @Test(description = "Native : Verify on Onus Payment Risk_Accept , Risk_info not null with Length exceeded 100 characters for PPBL transaction with merchant status and Peon ")
    public void validateRiskAcceptNotNullForPPBL(@Optional("false") Boolean isNativePlus) throws Exception {

        Constants.MerchantType rentMerchant = Constants.MerchantType.PGOnly;

        User user = userManager.getForWrite(Label.PPBL);
        String riskAmount = "5.6";

        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user, "CREDIT_CARD", riskAmount, rentMerchant.getId());
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), rentMerchant)
                .setRiskExtendInfo(riskExtendInfoDTO())
                .setOrderId(orderId)
                .setTxnValue(riskAmount)
                .build();

        String token = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderDTO.Builder()
                .setORDER_ID(initTxnDTO.orderFromBody())
                .setMID(rentMerchant.getId())
                .setCHANNEL_ID("WEB")
                .setTXN_TOKEN(token)
                .setPAYMENT_TYPE_ID("PPBL")
                .setAUTH_MODE("USRPWD")
                .setTXN_AMOUNT(riskAmount)
                .setMpin(new PaymentDTO().getPasscode())
                .build();


        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
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
                .validateRiskInfo(Constants.ValidationType.NON_EMPTY)
                .assertAll();


        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.PPBL.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRiskInfo(Constants.ValidationType.NON_EMPTY)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "riskInfo", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBL"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("NB"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(initTxnDTO.getBody().getTxnAmount().getValue()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.riskInfo().equals("").not()
        );
        sAssert.eval();
    }





    @Feature("PGP-28519")
    @Parameters({"isNativePlus"})
    @Owner("Gagandeep")
    @Test(description = "Verify the transaction when goods sections is passed and matches property for deferred FPO request")
    public void validateDeferedFPOWhenGoodIDMatchesInProperty(@Optional("true") Boolean isNativePlus) throws Exception {

        Constants.MerchantType addMoneyOnly = Constants.MerchantType.AddMoneyMP;
        User user = userManager.getForRead(Label.LOGIN);
        boolean generateOrderId = true;
        String amount = "1";
        com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.Good goodId = new com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.Good();
        goodId.setMerchantGoodsId("154435058"); //this id is present in project-theia.propertiesFile
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(generateOrderId))
                .setMid(addMoneyOnly.getId())
                .setAmount(Double.valueOf(amount))
                .setToken(user.ssoToken())
                .setGoods(Collections.singletonList(goodId))
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(addMoneyOnly.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        Assert.assertFalse(fetchPaymentOptResponse.get("body").toString().contains("riskConvenienceFee"));
        String orderId = fetchPaymentOptResponse.getString("body.orderId");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), addMoneyOnly)
                .setOrderId(orderId)
                .setTxnValue(amount)
                .build();

        Good goodInfo = new Good();
        goodInfo.setMerchantGoodsId("154435058"); //Adding money in GV
        initTxnDTO.getBody().setGoods(new Good[]{goodInfo});
        String checksum = PGPHelpers.getNativeChecksum(addMoneyOnly.getKey(), initTxnDTO.getBody());
        initTxnDTO.getHead().setSignature(checksum);
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String token = initTxnResponse.getBody().getTxnToken();

        OrderDTO orderDTO = new OrderFactory.Native(addMoneyOnly, initTxnDTO.orderFromBody(), token,new PaymentDTO().setDebitCardNumber(PaymentDTO.DEBIT_CARD_NUMBER), PayMethodType.DEBIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(addMoneyOnly.getId(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(amount)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFCBANK.toString())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(addMoneyOnly.getId())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateCardHash(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
    }


    @Feature("PGP-28519")
    @Owner("Gagandeep")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify the transaction when goods sections is passed but doesn't match in deferred FPO request")
    public void validateDeferedFPOWhenGoodsIdNotMatchesInProp() throws Exception {

        Constants.MerchantType addMoneyOnly = Constants.MerchantType.AddMoneyMP;
        User user = userManager.getForRead(Label.LOGIN);
        String GoodsId = CommonHelpers.getRandomWithSize(10)+"1";
        boolean generateOrderId = true;
        com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.Good goodId = new com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.Good();
        goodId.setMerchantGoodsId(GoodsId); //this id is present in project-theia.propertiesFile
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(generateOrderId))
                .setMid(addMoneyOnly.getId())
                .setToken(user.ssoToken())
                .setGoods(Collections.singletonList(goodId))
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(addMoneyOnly.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        Assert.assertTrue(fetchPaymentOptResponse.get("body").toString().contains("riskConvenienceFee"));


    }


    @Feature("PGP-28519")
    @Owner("Gagandeep")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify the transaction when goods sections is not passed in deferred FPO request , riskConvienceFee wont come")
    public void validateDeferedFPOWhenGoodsIdNotPassed() throws Exception {

        Constants.MerchantType addMoneyOnly = Constants.MerchantType.AddMoneyMP;
        User user = userManager.getForRead(Label.LOGIN);
        boolean generateOrderId = true;
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(generateOrderId))
                .setMid(addMoneyOnly.getId())
                .setToken(user.ssoToken())
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(addMoneyOnly.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        Assert.assertTrue(fetchPaymentOptResponse.get("body").toString().contains("riskConvenienceFee"));

    }

    @Feature("PGP-31223")
    @Owner("Sourav")
    @Test(description = "Verify the FPO Response when workflow checkout is passed in Request header")
    public void validateDeferredCheckoutFPOResponse() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.NATIVE_HYBRID;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .build();
        String txntoken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        boolean generateOrderId = true;
        boolean fetchAllPaymentOffers = false;
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txntoken)
                .setVersion("v2")
                .setRequestId("r1")
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(generateOrderId))
                .setMid(merchant.getId())
                .setToken(user.ssoToken())
                .setWorkFlow("checkout")
                .setFetchAllPaymentOffers(Boolean.toString(fetchAllPaymentOffers))
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchant.getId(),orderId,fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptResponse.get("body.resultInfo.resultMsg").toString().contains("Success"));

    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-56040")
    @Test(description = "Verify that No call going to mapping service to check the DC emi eligibility of the user and EMI_DC banks being returned if configured on mid")
    public void validate_EmiEligibility_CallNotHappening_inFPO(@Optional("false") boolean isNativePlus) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String requestId = CommonHelpers.generateOrderId();
        String GoodsId = CommonHelpers.getRandomWithSize(10)+"1";
        com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.Good goodId = new com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.Good();
        goodId.setMerchantGoodsId(GoodsId);
        UltimateBeneficiaryDetails ultimateBeneficiaryDetailsFPO = new UltimateBeneficiaryDetails("Indane");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setVersion("v2")
                .setRequestId(requestId)
                .setTokenType("SSO")
                .setMid(merchant.getId())
                .setToken(user.ssoToken())
                .setGoods(Collections.singletonList(goodId))
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode("BANK_MANDATE"),new DisablePaymentMode().setMode("PAY_AT_COUNTER"),new DisablePaymentMode().setMode("ADVANCE_ACCOUNT"),new DisablePaymentMode().setMode("COD"),new DisablePaymentMode().setMode("ESCROW")})
                .setCardHashRequired("true")
                .setOrderAmount("1000")
                .setTxnAmount(new com.paytm.dto.processTransactionV1.TxnAmount().setValue("1000").setCurrency("INR"))
                .setUltimateBeneficiaryDetails(ultimateBeneficiaryDetailsFPO)
                .build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(merchant.getId(),fetchPaymentOptionsDTO);
        fetchPaymentOption.getRequestSpecBuilder().addQueryParam("channel","web");
        fetchPaymentOption.getRequestSpecBuilder().addQueryParam("client","WEB");
        fetchPaymentOption.getRequestSpecBuilder().addHeader("X-PGP-Unique-ID",requestId);
        JsonPath response = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(response.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(response.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(response.getString("body.merchantPayOption.paymentModes")).contains("emiType:DEBIT_CARD");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option,requestId);
        String pgpId = logs.substring(logs.indexOf("PGP_ID=")+7,logs.indexOf(", version=v2} - Native request received for API"));
        String logs2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option,pgpId);
        Assertions.assertThat(logs2).isNotEmpty();
        Assertions.assertThat(logs2).doesNotContain("Mapping response - EmiOnDcResponse");
    }

}