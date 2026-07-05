package scripts;

import com.paytm.ServerConfigProvider;
import com.paytm.api.GetPaymentStatus;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.Gateway;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.ValidationType;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import groovy.json.JsonSlurper;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.Map;

@Owner("Deepak")
public class Seamless extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Test(description = "Validate Successful Seamless Txn with CC for Non-Existing User.", groups = {"smoke"})
    public void PGP_288_successfulSeamlessCC() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.Seamless(MerchantType.Seamless_Hybrid_Onus, "CC", user).build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(MerchantType.Seamless_Hybrid_Onus.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

    }

    @Test(description = "Validate Successful Seamless Txn with DC for Non-Existing User.")
    public void PGP_289_successfulSeamlessDC() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.Seamless(MerchantType.Seamless_Hybrid_Onus, "DC", user).build();
        checkoutPage.createOrder(orderDTO);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Test(description = "Validate successful Txn using Credit Card for Existing User without saving card.")
    public void PGP_290_SeamlessCCWithoutSavedCard() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SSOToken = user.ssoToken();
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.Seamless(MerchantType.Seamless_Hybrid_Onus, "CC", user)
                .setSSO_TOKEN(SSOToken)
                .setSTORE_CARD("0")
                .build();
        checkoutPage.createOrder(orderDTO);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        SavedCardHelpers.validateSavedCardAbsence(user);
    }

    @Test(description = "Validate Card is getting saved for using credit card for Existing user.")
    public void PGP_291_SeamlessCCAndSavingCard() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SSOToken = user.ssoToken();
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.Seamless(MerchantType.Seamless_Hybrid_Onus, "CC", user)
                .setSSO_TOKEN(SSOToken)
                .setSTORE_CARD("1")
                .build();
        checkoutPage.createOrder(orderDTO);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        SavedCardHelpers.validateSavedCardPresence(user);
    }

    @Test(description = "Validate Successful Seamless Txn with Saved Card for Existing User.")
    public void PGP_292_SeamlessCCUsingSavedCard() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SSOToken = user.ssoToken();
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        paymentDTO.setSavedCardId(SavedCardHelpers.getSavedCardId(user, 0));
        OrderDTO orderDTO = new OrderFactory.Seamless(MerchantType.Seamless_Hybrid_Onus, "CC", paymentDTO, user)
                .setSSO_TOKEN(SSOToken)
                .build();
        checkoutPage.createOrder(orderDTO);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Owner("Deepak | Karmvir")
    @Feature("PGP_288 | PGP-27192")
    @Test(description = "Validate Successful Seamless Txn with NB for Existing User and get payment status")
    public void PGP_293_successfulSeamlessNB() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SSOToken = user.ssoToken();
        OrderDTO orderDTO = new OrderFactory.Seamless(MerchantType.Seamless_Hybrid_Onus, "NB", user)
                .setSSO_TOKEN(SSOToken)
                .setBANK_CODE("ICICI")
                .build();
        checkoutPage.createOrder(orderDTO);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), MerchantType.Seamless_Hybrid_Onus.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        JsonPath getPaymentStatus = merchant.execute().jsonPath();
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Txn Success");
        Assertions.assertThat(getPaymentStatus.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(getPaymentStatus.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        Assertions.assertThat(getPaymentStatus.getString("body.gatewayName")).isEqualToIgnoringCase(Gateway.ICICI.toString());
        Assertions.assertThat(getPaymentStatus.getString("body.paymentMode")).isEqualToIgnoringCase("NB");
        Assertions.assertThat(getPaymentStatus.getString("body.promoStatus")).isNullOrEmpty();
    }


    @Owner("Tarun | Karmvir")
    @Feature("PGP-18539 | PGP-27192")
    @Test(description = "Verify the seamless flow txn with the paymode CC and promocode of CC and get payment status")
    public void PGP_294_successfulSeamlessCCPromoTxn() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String promoCode = Constants.promoCode.CC_PROMO.toString();
        OrderDTO orderDTO = new OrderFactory.Seamless(MerchantType.Seamless_Hybrid_Onus, "CC", user)
                .setPROMO_CAMP_ID(promoCode)
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(MerchantType.Seamless_Hybrid_Onus.getKey())
                .assertAll();

        Assertions.assertThat(responsePage.textPromoCampId().getText()).isEqualTo(promoCode);
        Assertions.assertThat(responsePage.textPromoRespcode().getText()).isEqualTo("700");
        Assertions.assertThat(responsePage.textPromoStatus().getText()).isEqualTo("PROMO_SUCCESS");

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), MerchantType.Seamless_Hybrid_Onus.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        JsonPath getPaymentStatus = merchant.execute().jsonPath();
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Txn Success");
        Assertions.assertThat(getPaymentStatus.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(getPaymentStatus.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        Assertions.assertThat(getPaymentStatus.getString("body.gatewayName")).isEqualToIgnoringCase(Gateway.HDFC.toString());
        Assertions.assertThat(getPaymentStatus.getString("body.paymentMode")).isEqualToIgnoringCase("CC");
        Assertions.assertThat(getPaymentStatus.getString("body.promoStatus")).isEqualToIgnoringCase("PROMO_SUCCESS");
    }


    @Owner("Tarun | Karmvir")
    @Feature("PGP-18539 | PGP-27192")
    @Test(description = "Verify the seamless flow txn with the paymode NB and promocode of NB and get payment status")
    public void PGP_295_successfulSeamlessNBPromoTxn() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String promoCode = Constants.promoCode.NB_PROMO.toString();

        OrderDTO orderDTO = new OrderFactory.Seamless(MerchantType.Seamless_Hybrid_Onus, "NB", user)
                .setPROMO_CAMP_ID(promoCode)
                .setBANK_CODE("ICICI")
                .build();

        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.ICICI.toString())
                .validateBankName(Constants.Bank.ICICI.toString())
                .validateCheckSum(MerchantType.Seamless_Hybrid_Onus.getKey())
                .assertAll();

        Assertions.assertThat(responsePage.textPromoCampId().getText()).isEqualTo(promoCode);
        Assertions.assertThat(responsePage.textPromoRespcode().getText()).isEqualTo("700");
        Assertions.assertThat(responsePage.textPromoStatus().getText()).isEqualTo("PROMO_SUCCESS");

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.ICICI.toString())
                .validateBankName(Constants.Bank.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), MerchantType.Seamless_Hybrid_Onus.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        JsonPath getPaymentStatus = merchant.execute().jsonPath();
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Txn Success");
        Assertions.assertThat(getPaymentStatus.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(getPaymentStatus.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());

        Assertions.assertThat(getPaymentStatus.getString("body.gatewayName")).isEqualToIgnoringCase(Gateway.ICICI.toString());
        Assertions.assertThat(getPaymentStatus.getString("body.paymentMode")).isEqualToIgnoringCase("NB");
        Assertions.assertThat(getPaymentStatus.getString("body.promoStatus")).isEqualToIgnoringCase("PROMO_SUCCESS");

    }

    @Owner("Tarun | Karmvir")
    @Feature("PGP-18539 | PGP-27192")
    @Test(description = "Verify the seamless flow txn with the paymode CC and promocode of NB and cashback type and get payment status")
    public void PGP_296_successfulSeamlessNBPromoTxnDiscountType() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String promoCode = Constants.promoCode.NB_PROMO.toString();

        OrderDTO orderDTO = new OrderFactory.Seamless(MerchantType.Seamless_Hybrid_Onus, "CC", user)
                .setPROMO_CAMP_ID(promoCode)
                .build();

        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(MerchantType.Seamless_Hybrid_Onus.getKey())
                .assertAll();

        Assertions.assertThat(responsePage.textPromoCampId().getText()).isEqualTo(promoCode);
        Assertions.assertThat(responsePage.textPromoRespcode().getText()).isEqualTo("701");
        Assertions.assertThat(responsePage.textPromoStatus().getText()).isEqualTo("FAILURE");

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), MerchantType.Seamless_Hybrid_Onus.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        JsonPath getPaymentStatus = merchant.execute().jsonPath();
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Txn Success");
        Assertions.assertThat(getPaymentStatus.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(getPaymentStatus.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());

        Assertions.assertThat(getPaymentStatus.getString("body.gatewayName")).isEqualToIgnoringCase(Gateway.HDFC.toString());
        Assertions.assertThat(getPaymentStatus.getString("body.paymentMode")).isEqualToIgnoringCase("CC");
        Assertions.assertThat(getPaymentStatus.getString("body.promoStatus")).isEqualToIgnoringCase("FAILURE");

    }


    @Owner("Tarun | Karmvir")
    @Feature("PGP-18539 | PGP-27192")
    @Test(description = "Verify the seamless flow txn with the paymode NB and promocode of CC and cashback type and get payment status")
    public void PGP_297_successfulSeamlessNBPromoTxn() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String promoCode = Constants.promoCode.CC_PROMO.toString();

        OrderDTO orderDTO = new OrderFactory.Seamless(MerchantType.Seamless_Hybrid_Onus, "NB", user)
                .setPROMO_CAMP_ID(promoCode)
                .setBANK_CODE("ICICI")
                .build();

        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.ICICI.toString())
                .validateBankName(Constants.Bank.ICICI.toString())
                .validateCheckSum(MerchantType.Seamless_Hybrid_Onus.getKey())
                .assertAll();

        Assertions.assertThat(responsePage.textPromoCampId().getText()).isEqualTo(promoCode);
        Assertions.assertThat(responsePage.textPromoRespcode().getText()).isEqualTo("701");
        Assertions.assertThat(responsePage.textPromoStatus().getText()).isEqualTo("FAILURE");

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.ICICI.toString())
                .validateBankName(Constants.Bank.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), MerchantType.Seamless_Hybrid_Onus.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        JsonPath getPaymentStatus = merchant.execute().jsonPath();
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Txn Success");
        Assertions.assertThat(getPaymentStatus.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(getPaymentStatus.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());

        Assertions.assertThat(getPaymentStatus.getString("body.gatewayName")).isEqualToIgnoringCase(Gateway.ICICI.toString());
        Assertions.assertThat(getPaymentStatus.getString("body.paymentMode")).isEqualToIgnoringCase("NB");
        Assertions.assertThat(getPaymentStatus.getString("body.promoStatus")).isEqualToIgnoringCase("FAILURE");
    }


    @Owner("Tarun | Karmvir")
    @Feature("PGP-18539 | PGP-27192")
    @Test(description = "Verify the seamless flow txn with the paymode CC and promocode of CC and discount type and get payment status")
    public void PGP_298_successfulSeamlessCCPromoTxn() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String promoCode = Constants.promoCode.RESTRICTED_CC_PROMO.toString();

        OrderDTO orderDTO = new OrderFactory.Seamless(MerchantType.Seamless_Hybrid_Onus, "CC", user)
                .setPROMO_CAMP_ID(promoCode)
                .build();

        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(MerchantType.Seamless_Hybrid_Onus.getKey())
                .assertAll();

        Assertions.assertThat(responsePage.textPromoCampId().getText()).isEqualTo(promoCode);
        Assertions.assertThat(responsePage.textPromoRespcode().getText()).isEqualTo("700");
        Assertions.assertThat(responsePage.textPromoStatus().getText()).isEqualTo("PROMO_SUCCESS");

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), MerchantType.Seamless_Hybrid_Onus.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        JsonPath getPaymentStatus = merchant.execute().jsonPath();
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Txn Success");
        Assertions.assertThat(getPaymentStatus.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(getPaymentStatus.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());

        Assertions.assertThat(getPaymentStatus.getString("body.gatewayName")).isEqualToIgnoringCase(Gateway.HDFC.toString());
        Assertions.assertThat(getPaymentStatus.getString("body.paymentMode")).isEqualToIgnoringCase("CC");
        Assertions.assertThat(getPaymentStatus.getString("body.promoStatus")).isEqualToIgnoringCase("PROMO_SUCCESS");

    }

    @Owner("Tarun")
    @Feature("PGP-18539")
    @Test(description = "Verify the seamless flow txn with the paymode NB and promocode of CC and discount type")
    public void PGP_299_successfulSeamlessCCPromoTxn() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String promoCode = Constants.promoCode.RESTRICTED_CC_PROMO.toString();
        OrderDTO orderDTO = new OrderFactory.Seamless(MerchantType.Seamless_Hybrid_Onus, "NB", user)
                .setPROMO_CAMP_ID(promoCode)
                .setBANK_CODE("ICICI")
                .build();

        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("810")
                .validateRespMsg("Payment failed due to a technical error. Please try after some time.")
                .validateStatus("TXN_FAILURE")
                .assertAll();


    }

    @Owner("Tarun")
    @Feature("PGP-24136")
    @Test(description = "Validate Success seamless corporate card bin txn with CC on Corporate Card merchant")
    public void successfulSeamlessCorporateCC() throws Exception {
        MerchantType corporateMerchant = MerchantType.CORPORATE_CARD_ONLY;
        CorporateHelpers.assertCorporateCardCC(corporateMerchant.getId());

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CORPORATE_INDIAN_CC);
        String bin = paymentDTO.getCreditCardNumber().substring(0,6);
        Assertions.assertThat(CorporateHelpers.isBinCorporate(bin)).isEqualTo(true);
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.Seamless(corporateMerchant, "CC",paymentDTO, user).build();
        checkoutPage.createOrder(orderDTO);

        CorporateHelpers.validateSuccessResponse(orderDTO,corporateMerchant,"CC", Constants.Bank.HDFC.toString());

        CorporateHelpers.validateSuccessTxnStatusCorporate(orderDTO,"CC", Constants.Bank.HDFC.toString());

        CorporateHelpers.validateSuccessPeonCorporate(orderDTO,"CC", Constants.Bank.HDFC.toString(),Gateway.HDFC.toString());
    }

    @Owner("Tarun")
    @Feature("PGP-24136")
    @Test(description = "Validate Success seamless corporate card bin txn with DC on Corporate Card merchant")
    public void successfulSeamlessCorporateDC() throws Exception {
        MerchantType corporateMerchant = MerchantType.CORPORATE_CARD_ONLY;
        CorporateHelpers.assertCorporateCardDC(corporateMerchant.getId());

        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.CORPORATE_INDIAN_DC);
        String bin = paymentDTO.getDebitCardNumber().substring(0,6);
        Assertions.assertThat(CorporateHelpers.isBinCorporate(bin)).isEqualTo(true);
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.Seamless(corporateMerchant, "DC",paymentDTO, user).build();
        checkoutPage.createOrder(orderDTO);

        CorporateHelpers.validateSuccessResponse(orderDTO,corporateMerchant,"DC", Constants.Bank.AXIS.toString());

        CorporateHelpers.validateSuccessTxnStatusCorporate(orderDTO,"DC", Constants.Bank.AXIS.toString());

        CorporateHelpers.validateSuccessPeonCorporate(orderDTO,"DC", Constants.Bank.AXIS.toString(),Gateway.HDFC.toString());
    }

    @Owner("AJEESH")
    @Feature("PGP-35628")
    @Test(description = "Validate Prepaid Card param is not shown in extendInfo while doing Seamless with Prepaid Card")
    public void TC001_VerifyPrepaidCardisnotshowninExtendinfo() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        MerchantType prepaidMerchant = MerchantType.SEAMLESS_PREPAID;
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);
        String bin = paymentDTO.getDebitCardNumber().substring(0,6);
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnAlipay(bin)).isEqualTo(true);
        Assertions.assertThat(PrepaidHelpers.isBinPrepaidOnPaytm(bin)).isEqualTo(true);

        OrderDTO orderDTO = new OrderFactory.Seamless(prepaidMerchant, "DC",paymentDTO, user).build();
        checkoutPage.createOrder(orderDTO);
        PrepaidHelpers.validateSuccessTxnStatusPrepaidCard(orderDTO);
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() +"\" | grep \"ACQUIRING_CREATE_ORDER_AND_PAY\" | grep \"REQUEST\" | grep \"extendInfo\"";
        String theiafacadelogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);

        Map map = (Map) new JsonSlurper().parseText(theiafacadelogs);
        String extendInfo = ((String) ((Map) ((Map) ((Map) map.get("REQUEST")).get("request")).get("body")).get("extendInfo"));
        Assertions.assertThat(extendInfo).as("Prepaid Card is not present in extendInfo").doesNotContain("\"prepaidCard\"");
    }

    @Owner("AJEESH")
    @Feature("PGP-35628")
    @Test(description = "Validate Prepaid Card param is not shown in extendInfo while doing Seamless without Prepaid Card")
    public void TC002_VerifyPrepaidCardisnotshowninExtendinfo() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.Seamless(MerchantType.SEAMLESS_PREPAID, "DC", user).build();
        checkoutPage.createOrder(orderDTO);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() +"\" | grep \"ACQUIRING_CREATE_ORDER_AND_PAY\" | grep \"REQUEST\" | grep \"extendInfo\"";
        String theiafacadelogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);

        Map map = (Map) new JsonSlurper().parseText(theiafacadelogs);
        String extendInfo = ((String) ((Map) ((Map) ((Map) map.get("REQUEST")).get("request")).get("body")).get("extendInfo"));
        Assertions.assertThat(extendInfo).as("Prepaid Card is not present in extendInfo").doesNotContain("\"prepaidCard\"");
    }
}
