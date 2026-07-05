package scripts.Sprint28;

import com.paytm.api.CreateSubscription;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.RenewSubscription;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.RenewSubscription.RenewSubscriptionDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.saveCard.SaveCardResponseBase;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Story;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

@Owner("Tarun")
@Epic("Sprint-28.2")
@Feature("PGP-17114")
public class SubscriptionStoreCardNo extends PGPBaseTest {

   // Store Card Details : False
    private final Constants.MerchantType merchantType = Constants.MerchantType.SUBSCRIPTION_PGONLY_PEON_DISABLED;
    private final CheckoutPage checkoutPage = new CheckoutPage();

    private void assertStoreCardPreferenceDisabled(Constants.MerchantType merchantType)
    {
        //Merchant should have store card details NO
        prerequisite:
        { PGPHelpers.validate_MerchantPreference(merchantType.getId(), "STORE CARD DETAILS", "NO"); }
    }

    private void assertSavedCardIDNotStored(String subsId)
    {
        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).as("Saved card id in 'subscription_contract_v2' is not null for subscription ID: " +subsId)
                .isNull();
    }

    private void assertSavedCardIDStored(String subsId)
    {

        Assertions.assertThat(PGPHelpers.getSavedCardId(subsId)).as("Saved card id in 'subscription_contract_v2' is null for subscription ID: " +subsId)
                .isNotNull().isNotEmpty();
    }

    //isSuccessRenewal should be true if success renewal needs to be validated
    private void assertRenewalStatus(String subsId,String txnAmount,boolean isSuccessRenewal)
    {
        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchantType.getId(), subsId, txnAmount)
                .setMerchantKey(merchantType.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("S");
        String orderId = renewSubscriptionDTO.getBody().getOrderId();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("payment_type", subsId, orderId))
                .as("payment_type mismatch").isEqualToIgnoringCase("RENEWAL");

        if(isSuccessRenewal) {
            softAssertions
                    .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                    .as("Status is not 'ACTIVE' in subscription_payment_details for payment type : renewal for subsid: " + subsId).isEqualToIgnoringCase("ACTIVE");
            softAssertions
                    .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderId))
                    .as("Acquirement Id is 'NULL' in subscription_payment_details for payment type : renewal for subsid: " + subsId).isNotNull();
            softAssertions.assertAll();
        }
        else
        {

            softAssertions
                    .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                    .as("Status is not 'INIT' in subscription_payment_details for payment type : renewal for subsid: "+subsId) .isEqualToIgnoringCase("FAIL");
            softAssertions
                    .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderId))
                    .as("Acquirement Id is not'NULL' in subscription_payment_details for payment type : renewal for subsid: "+subsId).isNull();
            softAssertions.assertAll();
        }



    }


    //-------------------------------- Native Flow---------------------------------------------
    //Deprecated CC/DC Flow after SI Hub Release
//    @Test(enabled = false, groups = {"smoke"},description = "Verify for CC,subscription is created and savedCardId is generated and renewal is successful with generated savedCardId")
    public void savedCardIdGeneratedCC() throws Exception {
        assertStoreCardPreferenceDisabled(merchantType);
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchantType)
                .paymethodType(PayMethodType.CREDIT_CARD)
                .pay();
         String subsId = subscription.getSubsId();

        assertSavedCardIDStored(subsId);
        assertRenewalStatus(subsId,initTxnDTO.txnAmountFromBody(),true);


    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Test(enabled = false, description = "Verify for DC,subscription is created and savedCardId is generated and renewal is successful with generated savedCardId")
    public void savedCardIdGeneratedDC() throws Exception {
        assertStoreCardPreferenceDisabled(merchantType);
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchantType)
                .paymethodType(PayMethodType.DEBIT_CARD)
                .pay();
        String subsId = subscription.getSubsId();

        assertSavedCardIDStored(subsId);

        assertRenewalStatus(subsId,initTxnDTO.txnAmountFromBody(),true);

    }

    @Test(description = "Verify that subscription is created succesfully with AddandPay method but cardId should not save in subscription_contract_v2 and renew is not successful")
    public void savedCardIdGeneratedPPI() throws Exception {
        double txnAmount = 5.0d;
        Constants.MerchantType merchantType = Constants.MerchantType.SUBSCRIPTION_ADDNPAY;
        assertStoreCardPreferenceDisabled(merchantType);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,txnAmount-1.0);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(String.valueOf(txnAmount))
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchantType)
                .paymethodType(PayMethodType.ADDANDPAY)
                .pay();
        String subsId = subscription.getSubsId();

        assertSavedCardIDNotStored(subsId);

        WalletHelpers.setZeroBalance(user);//to make sure that balance is zero before renewal

        assertRenewalStatus(subsId,initTxnDTO.txnAmountFromBody(),false);


    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Story("PGP-17703")
//    @Test(enabled = false, description = "Verify in fetchPayOptions api that card is not saved on basis of MID & CustId")
    public void fetchPayOptionsOnTheBasisOfMIDAndCustId() throws Exception {
        assertStoreCardPreferenceDisabled(merchantType);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("",merchantType)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchantType)
                .paymethodType(PayMethodType.CREDIT_CARD)
                .pay();

        String subsId = subscription.getSubsId();

        assertSavedCardIDStored(subsId);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(subscription.getTxnToken()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).as("Fetch Pay option failed").isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments[0]")).as("Saved Card instrument is not blank")
                .isBlank();


    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Story("PGP-17703")
//    @Test(enabled = false, description = "Verify in fetchPayOptions api that card is saved on basis on SSO token")
    public void fetchPayOptionsOnTheBasisOfSSOToken() throws Exception {
        assertStoreCardPreferenceDisabled(merchantType);
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchantType)
                .paymethodType(PayMethodType.CREDIT_CARD)
                .pay();
        String subsId = subscription.getSubsId();
        String saveCardId = PGPHelpers.getSavedCardId(subsId);

        assertSavedCardIDStored(subsId);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(subscription.getTxnToken()).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).as("Fetch Pay option failed").isEqualTo("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments[0].cardDetails.cardId")).as("Card id doesn't match with save card from DB")
                .isEqualTo(saveCardId);
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments[0].cardDetails.firstSixDigit")).as("First six digits are not getting matched")
                .isEqualTo(paymentDTO.getCreditCardNumber().substring(0,6));
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Test(enabled = false, description = "Verify that if a subscription is created on previously saved card on combo of MID-CustId ,old id is saved in contract_v2 and renewal is successful with that id")
    public void subsCreatedOnAlreadySavedCard() throws Exception {
        assertStoreCardPreferenceDisabled(merchantType);
        PaymentDTO paymentDTO = new PaymentDTO();
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        String custId = CommonHelpers.generateOrderId();
        SavedCardHelpers helper = new SavedCardHelpers();
        SaveCardResponseBase resp = helper.saveCard_custId_mId(paymentDTO.getCreditCardNumber(), custId, merchantType.getId(), paymentDTO.getExpMonth() + paymentDTO.getExpYear());

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("",merchantType)
                .setCustId(custId)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchantType)
                .paymethodType(PayMethodType.CREDIT_CARD)
                .pay();

        String subsId = subscription.getSubsId();
        String savedCardId = PGPHelpers.getSavedCardId(subsId);
        Assertions.assertThat(savedCardId).as("New saved card is getting stored in subscription_contract_v2").isEqualTo(resp.getResponse().toString());
        assertRenewalStatus(subsId,initTxnDTO.txnAmountFromBody(),true);

    }

    //-------------------------------- Enhanced Flow---------------------------------------------
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters("theme")
//    @Test(enabled = false, description = "Verify that subscription is created succesfully and savecardId is generated and saved in subscription_contract_v2 for CC and renew is also successful")
    public void assertSubsCreatedForEnhancedFlowCC(@Optional("enhancedweb") String theme) throws Exception {
        assertStoreCardPreferenceDisabled(merchantType);
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(merchantType, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
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
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        assertSavedCardIDStored(subsId);
        assertRenewalStatus(subsId,orderDTO.getTXN_AMOUNT(),true);

    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters("theme")
//    @Test(enabled = false, description = "Verify that subscription is created succesfully and savecardId is generated and saved in subscription_contract_v2 for DC and renew is also success")
    public void assertSubsCreatedForEnhancedFlowDC(@Optional("enhancedweb") String theme) throws Exception {
        assertStoreCardPreferenceDisabled(merchantType);
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(merchantType, theme, user)
                .setSUBS_PAYMENT_MODE("DC")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
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
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();

        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        assertSavedCardIDStored(subsId);
        assertRenewalStatus(subsId,orderDTO.getTXN_AMOUNT(),true);

    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters("theme")
//    @Test(enabled = false, description = "Verify that subscription is created succesfully and savecardId is generated & saved in subscription_contract_v2 for PPI - N [CC] and renewal is also success")
    public void assertSubsCreatedForEnhancedFlowPPIN(@Optional("enhancedweb") String theme) throws Exception {
       Constants.MerchantType merchantType = Constants.MerchantType.SUBSCRIPTION_PPI;
        assertStoreCardPreferenceDisabled(merchantType);
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(merchantType,theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user,Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
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
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        assertSavedCardIDStored(subsId);

        WalletHelpers.setZeroBalance(user);
        assertRenewalStatus(subsId,orderDTO.getTXN_AMOUNT(),true);

    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters("theme")
//    @Test(enabled = false, description = "Verify that subscription is created succesfully and savecardId is generated & saved in subscription_contract_v2 for PPI - N [DC] and renewal is also success")
    public void assertSubsCreatedForEnhancedFlowPPINWithDC(@Optional("enhancedweb") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.SUBSCRIPTION_PPI;
        assertStoreCardPreferenceDisabled(merchantType);
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionPPI(merchantType,theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user,Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
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
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        assertSavedCardIDStored(subsId);

        WalletHelpers.setZeroBalance(user);
        assertRenewalStatus(subsId,orderDTO.getTXN_AMOUNT(),true);

    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters("theme")
//    @Test(enabled = false, description = "Verify that subscription is created succesfully with AddandPay method and renewal is not successful")
    public void assertSubsWithAddNPay(@Optional("enhancedweb") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.SUBSCRIPTION_ADDNPAY;
        assertStoreCardPreferenceDisabled(merchantType);
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(merchantType,theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user,Double.valueOf(orderDTO.getTXN_AMOUNT())-1.0);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
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
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();

        WalletHelpers.setZeroBalance(user);
        assertRenewalStatus(subsId,orderDTO.getTXN_AMOUNT(),false);

    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Story("PGP-17703")
//    @Parameters("theme")
//    @Test(enabled = false, description = "Enhanced - Verify in fetchPayOptions api that card is not fetched on basis of MID & CustId")
    public void cardNotFetchedMIDCUSID(@Optional("enhancedweb") String theme)
    {
        assertStoreCardPreferenceDisabled(merchantType);
        String custId = CommonHelpers.generateOrderId();
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(merchantType,theme)
                .setCUST_ID(custId)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
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
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        assertSavedCardIDStored(subsId);
        OrderDTO orderDTO2 = new OrderFactory.SubscriptionCC_DC(merchantType,theme)
                .setCUST_ID(custId)
                .build();
        checkoutPage.createOrder(orderDTO2);
        cashierPage.assertSavedCardNotVisible();

    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Story("PGP-17703")
//    @Parameters("theme")
//    @Test(enabled = false, description = "Enhanced - Verify in fetchPayOptions api that card is fetched on basis on SSO token")
    public void cardNotFetchedSSOToken(@Optional("enhancedweb") String theme) throws Exception {
        assertStoreCardPreferenceDisabled(merchantType);
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(merchantType,theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
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
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
        String subsId = txnStatus.getResponse().getSUBS_ID();
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderDTO.getORDER_ID())).isNotNull().isNotEmpty();
        assertSavedCardIDStored(subsId);
        OrderDTO orderDTO2 = new OrderFactory.SubscriptionCC_DC(merchantType,theme)
            .setSSO_TOKEN(user.ssoToken())
            .build();
        checkoutPage.createOrder(orderDTO2);
        cashierPage.assertSavedCardVisibility();

    }

}
