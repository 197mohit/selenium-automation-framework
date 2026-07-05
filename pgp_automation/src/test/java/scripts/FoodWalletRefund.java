package scripts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.api.wallet.WalletPaymentConfirmation;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.RefundStatusHelper;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.dto.refund.SubWalletAmount;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Map;


/**
 * Created by anjukumari on 22/01/19
 */
@Owner("Tarun")
public class FoodWalletRefund extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();

    public String getSubWalletParameter(SubWalletAmount subwalletAmount) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String subWalletDetail = mapper.writeValueAsString(subwalletAmount).replaceAll("\"", "\\\\\"");
            return subWalletDetail;
        } catch (JsonProcessingException e) {
            Reporter.report.info("Invalid subwallet object" + e);
            return null;

        }
    }

    public OrderDTO successfulFoodWalletTxn(String theme, SubWalletAmount subwalletAmount) throws Exception {
        User user = userManager.getForWrite(Label.FOODWALLET);
        WalletHelpers.updateFoodWalletBalance(user, Double.parseDouble(subwalletAmount.getFood()));
        OrderDTO orderDTO = new OrderFactory.SubWallet(Constants.MerchantType.FOOD_MERCHANT_HYB, theme, user)
                .setSubwallet_Details(getSubWalletParameter(subwalletAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);
        new ResponsePage().waitUntilLoads();
        WalletHelpers.getFoodWalletBalance(user);
        String txnId = new TxnStatusValidations().validateWalletTxnStatus(orderDTO);
        orderDTO.setTxnId(txnId);
        WalletPaymentConfirmation paymentConfirmations = new WalletPaymentConfirmation().confirmation(txnId);
        JsonPath js = paymentConfirmations.execute().jsonPath();

        return orderDTO;
    }

    @Parameters({"theme"})
    @Test(description = "Validate Successfull refund txn from Food wallet when food wallet=txn amount")
    public void PGP_successfulFoodWalletRefund(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String foodwalletAmount = "2";
        SubWalletAmount subwalletAmount = new SubWalletAmount();
        subwalletAmount.setFood(foodwalletAmount);
        OrderDTO orderDTOTxn = successfulFoodWalletTxn(theme, subwalletAmount);
        Response response = PGPHelpers.masterRefund_FoodWallet_CheckSum(orderDTOTxn.getMID(), orderDTOTxn.getMerchantKey(), orderDTOTxn.getORDER_ID(), String.valueOf(CommonHelpers.getRandomWithSize(8)), "2", orderDTOTxn.getTxnId(), null, subwalletAmount);
        Assertions.assertThat(response.jsonPath().get("extraParamsMap.subwalletWithdrawMaxAmountDetails.FOOD").toString()).isEqualToIgnoringCase(foodwalletAmount);
        validateRefundStatus:
        {
            RefundStatusHelper refundStatusHelper = PGPHelpers.getRefundStatus(orderDTOTxn.getMID(), orderDTOTxn.getMerchantKey(), response.jsonPath().getString("REFID"), true);
            Map map = refundStatusHelper.getRefundBy("PAYMENTMODE", "PPI");
            refundStatusHelper.validate(map, "GATEWAY", "WALLET");
            refundStatusHelper.validate(map, "STATUS", "TXN_SUCCESS");
        }

    }


    @Parameters({"theme"})
    @Test(description = "Validate failed refund txn from Food wallet when food wallet parameter is not passed in checksum")
    public void PGP_failedRefund_WhenFoodNotPassedInCheckSum(@Optional("merchant4") String theme) throws Exception {
        String foodwalletAmount = "2";
        SubWalletAmount subwalletAmount = new SubWalletAmount();
        subwalletAmount.setFood(foodwalletAmount);
        OrderDTO orderDTOTxn = successfulFoodWalletTxn(theme, subwalletAmount);
        Response response = PGPHelpers.masterRefund_FoodWallet_CheckSum(orderDTOTxn.getMID(), orderDTOTxn.getMerchantKey(), orderDTOTxn.getORDER_ID(), String.valueOf(CommonHelpers.getRandomWithSize(8)), orderDTOTxn.getTXN_AMOUNT(), orderDTOTxn.getTxnId(), null, null);
        Assertions.assertThat(response.jsonPath().getString("STATUS")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(response.jsonPath().getString("RESPCODE")).isEqualTo("606");
    }

    @Issue("PGP-15179")
    @Parameters({"theme"})
    @Test(groups = Group.Status.BUG,description = "Validate success refund txn from Food wallet when txn_amount > foodWalletBalance and mainBalance = 0")
    public void PGP_SuccessFoodRefund_Hybrid_CC(@Optional("merchant4") String theme) throws Exception {
        String foodwalletAmount = "1";
        Double foodWalletAmtDouble = Double.parseDouble(foodwalletAmount);
        SubWalletAmount subwalletAmount = new SubWalletAmount();
        subwalletAmount.setFood(foodwalletAmount);
        User user = userManager.getForWrite(Label.FOODWALLET);
        WalletHelpers.updateFoodWalletBalance(user, Integer.parseInt(foodwalletAmount));
        Double mainWalletBalance = WalletHelpers.getWalletBalance(user);
        WalletHelpers.withdrawOnlyFromMainWallet(user, mainWalletBalance);
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(foodWalletAmtDouble);
        OrderDTO orderDTO = new OrderFactory.SubWallet(Constants.MerchantType.FOOD_MERCHANT_HYB, theme, user)
                .setSubwallet_Details(getSubWalletParameter(subwalletAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(Constants.PayMode.CC);
        new ResponsePage().waitUntilLoads();
        String txnId = new TxnStatusValidations().validateHybridTxnStatus(orderDTO, foodWalletAmtDouble, 0.0);
        orderDTO.setTxnId(txnId);
        WalletPaymentConfirmation paymentConfirmations = new WalletPaymentConfirmation().confirmation(txnId);
        JsonPath js = paymentConfirmations.execute().jsonPath();

        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(0.00);
        Response response = PGPHelpers.masterRefund_FoodWallet_CheckSum(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), String.valueOf(CommonHelpers.getRandomWithSize(8)), "2", orderDTO.getTxnId(), null, subwalletAmount);
        validateRefundStatus:
        {
            RefundStatusHelper refundStatusHelper = PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), response.jsonPath().getString("REFID"), true);
            Map map = refundStatusHelper.getRefundBy("PAYMENTMODE", "PPI");
            refundStatusHelper.validate(map, "GATEWAY", "WALLET");
            refundStatusHelper.validate(map, "STATUS", "TXN_SUCCESS");
            map = refundStatusHelper.getRefundBy("PAYMENTMODE", "CC");
            refundStatusHelper.validate(map, "GATEWAY", "HDFC");
            refundStatusHelper.validate(map, "STATUS", "TXN_SUCCESS");
        }
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(Double.parseDouble(foodwalletAmount));
    }


    @Parameters({"theme"})
    @Test(description = "Validate failure refund txn, when incorrect food parameter is passed in refund request and checksum", enabled = true)
    public void PGP_FailedFoodRefund_InvalidFoodParamInChecksum(@Optional("merchant4") String theme) throws Exception {
        String foodwalletAmount = "2";
        SubWalletAmount subwalletAmount = new SubWalletAmount();
        subwalletAmount.setFood(foodwalletAmount);
        //Invalid food wallet parameter for checksum
        OrderDTO orderDTOTxn = successfulFoodWalletTxn(theme, subwalletAmount);
        SubWalletAmount subwalletAmountInvalid = new SubWalletAmount();
        subwalletAmountInvalid.setFood("A");
        Response response = PGPHelpers.masterRefund_FoodWallet_CheckSum(orderDTOTxn.getMID(), orderDTOTxn.getMerchantKey(), orderDTOTxn.getORDER_ID(), String.valueOf(CommonHelpers.getRandomWithSize(8)), "2", orderDTOTxn.getTxnId(), null, subwalletAmountInvalid);
        Assertions.assertThat(response.jsonPath().getString("RESPMSG")).isEqualToIgnoringCase("System Error.");
    }


    @Parameters({"theme"})
    @Test(description = "Validate success refund txn, when Txn from CC=6, food=2 and main wallet =12")
    public void PGP_SuccessFoodRefund_Hybrid_FoodCCMainWallet(@Optional("merchant4") String theme) throws Exception {
        String foodwalletAmount = "2";
        String mainWalletBalance = "12";
        Double foodWalletAmtDouble = Double.parseDouble(foodwalletAmount);
        SubWalletAmount subwalletAmount = new SubWalletAmount();
        subwalletAmount.setFood(foodwalletAmount);
        User user = userManager.getForWrite(Label.FOODWALLET);
        WalletHelpers.modifyBalance(user, 12.0);
        WalletHelpers.updateFoodWalletBalance(user, Integer.parseInt(foodwalletAmount));
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(foodWalletAmtDouble);
        OrderDTO orderDTO = new OrderFactory.SubWallet(Constants.MerchantType.FOOD_MERCHANT_HYB, theme, user)
                .setTXN_AMOUNT("20")
                .setSubwallet_Details(getSubWalletParameter(subwalletAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(Constants.PayMode.CC);
        new ResponsePage().waitUntilLoads();
        String txnId = new TxnStatusValidations().validateHybridTxnStatus(orderDTO, Double.parseDouble(mainWalletBalance), Double.parseDouble(foodwalletAmount));
        orderDTO.setTxnId(txnId);
        WalletPaymentConfirmation paymentConfirmations = new WalletPaymentConfirmation().confirmation(txnId);
        JsonPath js = paymentConfirmations.execute().jsonPath();
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(0.00);
        Assertions.assertThat(WalletHelpers.getWalletBalance(user)).isEqualTo(0.00);
        Response response = PGPHelpers.masterRefund_FoodWallet_CheckSum(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), String.valueOf(CommonHelpers.getRandomWithSize(8)), "20", orderDTO.getTxnId(), null, subwalletAmount);
        validateRefundStatus:
        {
            RefundStatusHelper refundStatusHelper = PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), response.jsonPath().getString("REFID"), true);
            Map map = refundStatusHelper.getRefundBy("PAYMENTMODE", "PPI");
            refundStatusHelper.validate(map, "GATEWAY", "WALLET");
            refundStatusHelper.validate(map, "STATUS", "TXN_SUCCESS");
            map = refundStatusHelper.getRefundBy("PAYMENTMODE", "CC");
            refundStatusHelper.validate(map, "GATEWAY", "HDFC");
            refundStatusHelper.validate(map, "STATUS", "TXN_SUCCESS");
        }
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(Double.parseDouble(foodwalletAmount));
        Assertions.assertThat(WalletHelpers.getWalletBalance(user)).isEqualTo(Double.parseDouble(mainWalletBalance));
    }

    @Parameters({"theme"})
    @Test(description = "Validate success refund txn, when Txn from CC=0, food=2 and main wallet =3 and refund = 1.2 and food refund=1.2")
    public void PGP_SuccessFoodPartialRefund_Hybrid_FoodMainWallet(@Optional("merchant4") String theme) throws Exception {
        String foodwalletAmount = "2";
        String mainWalletBalance = "3";
        Double foodWalletAmtDouble = Double.parseDouble(foodwalletAmount);
        SubWalletAmount subwalletAmount = new SubWalletAmount();
        subwalletAmount.setFood(foodwalletAmount);
        User user = userManager.getForWrite(Label.FOODWALLET);
        WalletHelpers.modifyBalance(user, Double.parseDouble(mainWalletBalance));
        WalletHelpers.updateFoodWalletBalance(user, Integer.parseInt(foodwalletAmount));
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(foodWalletAmtDouble);
        OrderDTO orderDTO = new OrderFactory.SubWallet(Constants.MerchantType.FOOD_MERCHANT_HYB, theme, user)
                .setTXN_AMOUNT("5")
                .setSubwallet_Details(getSubWalletParameter(subwalletAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);
        new ResponsePage().waitUntilLoads();
        String txnId = new TxnStatusValidations().validateWalletTxnStatus(orderDTO);
        orderDTO.setTxnId(txnId);
        WalletPaymentConfirmation paymentConfirmations = new WalletPaymentConfirmation().confirmation(txnId);
        JsonPath js = paymentConfirmations.execute().jsonPath();

        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(0.00);
        Assertions.assertThat(WalletHelpers.getWalletBalance(user)).isEqualTo(0.00);
        SubWalletAmount refundSubWaletAmount = new SubWalletAmount();
        refundSubWaletAmount.setFood("1.2");
        Response response = PGPHelpers.masterRefund_FoodWallet_CheckSum(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), String.valueOf(CommonHelpers.getRandomWithSize(8)), "1.2", orderDTO.getTxnId(), null, refundSubWaletAmount);
        validateRefundStatus:
        {
            RefundStatusHelper refundStatusHelper = PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), response.jsonPath().getString("REFID"), true);
            Map map = refundStatusHelper.getRefundBy("PAYMENTMODE", "PPI");
            refundStatusHelper.validate(map, "GATEWAY", "WALLET");
            refundStatusHelper.validate(map, "STATUS", "TXN_SUCCESS");
        }
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(Double.parseDouble(refundSubWaletAmount.getFood()));
    }


    @Parameters({"theme"})
    @Test(description = "Validate success refund txn, when Txn from CC=0, food=2 and main wallet =3 and total refundAmount = 3 and foodRefundAmount=2.1")
    public void PGP_SuccessFoodRefund_Hybrid_PartialFoodMainWallet(@Optional("merchant4") String theme) throws Exception {
        String foodwalletAmount = "2";
        String mainWalletBalance = "3";
        Double foodWalletAmtDouble = Double.parseDouble(foodwalletAmount);
        SubWalletAmount subwalletAmount = new SubWalletAmount();
        subwalletAmount.setFood(foodwalletAmount);
        User user = userManager.getForWrite(Label.FOODWALLET);
        WalletHelpers.modifyBalance(user, Double.parseDouble(mainWalletBalance));
        WalletHelpers.updateFoodWalletBalance(user, Integer.parseInt(foodwalletAmount));
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(foodWalletAmtDouble);
        OrderDTO orderDTO = new OrderFactory.SubWallet(Constants.MerchantType.FOOD_MERCHANT_HYB, theme, user)
                .setTXN_AMOUNT("5")
                .setSubwallet_Details(getSubWalletParameter(subwalletAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);
        new ResponsePage().waitUntilLoads();
        String txnId = new TxnStatusValidations().validateWalletTxnStatus(orderDTO);
        orderDTO.setTxnId(txnId);
        WalletPaymentConfirmation paymentConfirmations = new WalletPaymentConfirmation().confirmation(txnId);
        JsonPath js = paymentConfirmations.execute().jsonPath();
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(0.00);
        Assertions.assertThat(WalletHelpers.getWalletBalance(user)).isEqualTo(0.00);
        SubWalletAmount refundSubWaletAmount = new SubWalletAmount();
        refundSubWaletAmount.setFood("2.1");
        Response response = PGPHelpers.masterRefund_FoodWallet_CheckSum(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), String.valueOf(CommonHelpers.getRandomWithSize(8)), "3", orderDTO.getTxnId(), null, refundSubWaletAmount);
        validateRefundStatus:
        {
            RefundStatusHelper refundStatusHelper = PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), response.jsonPath().getString("REFID"), true);
            Map map = refundStatusHelper.getRefundBy("PAYMENTMODE", "PPI");
            refundStatusHelper.validate(map, "GATEWAY", "WALLET");
            refundStatusHelper.validate(map, "STATUS", "TXN_SUCCESS");
            refundStatusHelper.assertAll();
        }
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(Double.parseDouble(foodwalletAmount));
        Assertions.assertThat(WalletHelpers.getWalletBalance(user)).isEqualTo(Double.parseDouble("1.0"));
    }


    @Parameters({"theme"})
    @Test(description = "Validate failed refund due to invalid refund amount,  when Txn is(total = 5, CC=0, Food = 2, Main = 3) and refundAmount is(total = 6, food = 2)")
    public void PGP_FailedFoodRefund_Hybrid(@Optional("enhancedweb") String theme) throws Exception {
        String foodwalletAmount = "2";
        String mainWalletBalance = "3";
        Double foodWalletAmtDouble = Double.parseDouble(foodwalletAmount);
        SubWalletAmount subwalletAmount = new SubWalletAmount();
        subwalletAmount.setFood(foodwalletAmount);
        User user = userManager.getForWrite(Label.FOODWALLET);
        WalletHelpers.modifyBalance(user, Double.parseDouble(mainWalletBalance));
        WalletHelpers.updateFoodWalletBalance(user, Integer.parseInt(foodwalletAmount));
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(foodWalletAmtDouble);
        OrderDTO orderDTO = new OrderFactory.SubWallet(Constants.MerchantType.FOOD_MERCHANT_HYB, theme, user)
                .setTXN_AMOUNT("5")
                .setSubwallet_Details(getSubWalletParameter(subwalletAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(Constants.PayMode.WALLET);
        new ResponsePage().waitUntilLoads();
        String txnId = new TxnStatusValidations().validateWalletTxnStatus(orderDTO);
        orderDTO.setTxnId(txnId);
        WalletPaymentConfirmation paymentConfirmations = new WalletPaymentConfirmation().confirmation(txnId);
        JsonPath js = paymentConfirmations.execute().jsonPath();

        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(0.00);
        Assertions.assertThat(WalletHelpers.getWalletBalance(user)).isEqualTo(0.00);
        SubWalletAmount refundSubWaletAmount = new SubWalletAmount();
        refundSubWaletAmount.setFood("2");
        Response response = PGPHelpers.masterRefund_FoodWallet_CheckSum(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), String.valueOf(CommonHelpers.getRandomWithSize(8)), "6", orderDTO.getTxnId(), null, refundSubWaletAmount);
        Assertions.assertThat(response.jsonPath().getString("RESPMSG")).isEqualTo("Invalid refund amount.");
        Assertions.assertThat(response.jsonPath().getString("STATUS")).isEqualTo("TXN_FAILURE");
    }

    @Parameters({"theme"})
    @Test(description = "Validate Success refund,  when Txn is(total=5, CC = 0, Food = 2, Main = 3) and refundAmount is(total = 4, foodRefundAmount=4)")
    public void PGP_SuccessFoodRefund_Hybrid_FoodWalletRefundAmtExceed(@Optional("merchant4") String theme) throws Exception {
        String foodwalletAmount = "2";
        String mainWalletBalance = "3";
        String refundAmount = "4";
        String refundFromFood = "4";
        Double foodWalletAmtDouble = Double.parseDouble(foodwalletAmount);
        SubWalletAmount subwalletAmount = new SubWalletAmount();
        subwalletAmount.setFood(foodwalletAmount);
        User user = userManager.getForWrite(Label.FOODWALLET);
        WalletHelpers.modifyBalance(user, Double.parseDouble(mainWalletBalance));
        WalletHelpers.updateFoodWalletBalance(user, Integer.parseInt(foodwalletAmount));
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(foodWalletAmtDouble);
        OrderDTO orderDTO = new OrderFactory.SubWallet(Constants.MerchantType.FOOD_MERCHANT_HYB, theme, user)
                .setTXN_AMOUNT("5")
                .setSubwallet_Details(getSubWalletParameter(subwalletAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);
        new ResponsePage().waitUntilLoads();
        String txnId = new TxnStatusValidations().validateWalletTxnStatus(orderDTO);
        orderDTO.setTxnId(txnId);
        WalletPaymentConfirmation paymentConfirmations = new WalletPaymentConfirmation().confirmation(txnId);
        JsonPath js = paymentConfirmations.execute().jsonPath();
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(0.00);
        Assertions.assertThat(WalletHelpers.getWalletBalance(user)).isEqualTo(0.00);
        SubWalletAmount refundSubWaletAmount = new SubWalletAmount();
        refundSubWaletAmount.setFood(refundFromFood);
        Response response = PGPHelpers.masterRefund_FoodWallet_CheckSum(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), String.valueOf(CommonHelpers.getRandomWithSize(8)), refundAmount, orderDTO.getTxnId(), null, refundSubWaletAmount);
        validateRefundStatus:
        {
            RefundStatusHelper refundStatusHelper = PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), response.jsonPath().getString("REFID"), true);
            Map map = refundStatusHelper.getRefundBy("PAYMENTMODE", "PPI");
            refundStatusHelper.validate(map, "GATEWAY", "WALLET");
            refundStatusHelper.validate(map, "STATUS", "TXN_SUCCESS");
        }
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(Double.parseDouble(foodwalletAmount));
        Assertions.assertThat(WalletHelpers.getWalletBalance(user)).isEqualTo(Double.parseDouble("2.0"));

    }


    @Parameters({"theme"})
    @Test(description = "Validate Success refund, when Txn amount is (total=5, CC=0, Food=2, Main = 3) and refundAmount is(total = 2, food = 2)")
    public void PGP_SuccessFoodRefund_Hybrid_notExceedsFoodWalletAmount(@Optional("merchant4") String theme) throws Exception {
        String foodwalletAmount = "2";
        String mainWalletBalance = "3";
        String refundAmount = "2";
        String refundFromFood = "2";
        Double foodWalletAmtDouble = Double.parseDouble(foodwalletAmount);
        SubWalletAmount subwalletAmount = new SubWalletAmount();
        subwalletAmount.setFood(foodwalletAmount);
        User user = userManager.getForWrite(Label.FOODWALLET);
        WalletHelpers.modifyBalance(user, Double.parseDouble(mainWalletBalance));
        WalletHelpers.updateFoodWalletBalance(user, Integer.parseInt(foodwalletAmount));
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(foodWalletAmtDouble);
        OrderDTO orderDTO = new OrderFactory.SubWallet(Constants.MerchantType.FOOD_MERCHANT_HYB, theme, user)
                .setTXN_AMOUNT("5")
                .setSubwallet_Details(getSubWalletParameter(subwalletAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);
        new ResponsePage().waitUntilLoads();
        String txnId = new TxnStatusValidations().validateWalletTxnStatus(orderDTO);
        orderDTO.setTxnId(txnId);
        WalletPaymentConfirmation paymentConfirmations = new WalletPaymentConfirmation().confirmation(txnId);
        JsonPath js = paymentConfirmations.execute().jsonPath();
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(0.00);
        Assertions.assertThat(WalletHelpers.getWalletBalance(user)).isEqualTo(0.00);
        SubWalletAmount refundSubWaletAmount = new SubWalletAmount();
        refundSubWaletAmount.setFood(refundFromFood);
        Response response = PGPHelpers.masterRefund_FoodWallet_CheckSum(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), String.valueOf(CommonHelpers.getRandomWithSize(8)), refundAmount, orderDTO.getTxnId(), null, refundSubWaletAmount);
        validateRefundStatus:
        {
            RefundStatusHelper refundStatusHelper = PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), response.jsonPath().getString("REFID"), true);
            Map map = refundStatusHelper.getRefundBy("PAYMENTMODE", "PPI");
            refundStatusHelper.validate(map, "GATEWAY", "WALLET");
            refundStatusHelper.validate(map, "STATUS", "TXN_SUCCESS");
        }
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(Double.parseDouble(refundFromFood));
        Assertions.assertThat(WalletHelpers.getWalletBalance(user)).isEqualTo(Double.parseDouble("0.0"));
    }

    @Parameters({"theme"})
    @Test(description = "Validate success add n pay refund txn, when Txn is (Total = 25, CC=7, food=18, mainWallet =0) and refund amount is( refund=25, wallet=7, food=18)")
    public void PGP_SuccessRefund_FOOD_MERCHANT_ADDNPAY(@Optional("merchant4") String theme) throws Exception {
        String foodwalletAmount = "18";
        Double ccAmount = 7.00;
        Double foodWalletAmtDouble = Double.parseDouble(foodwalletAmount);
        SubWalletAmount subwalletAmount = new SubWalletAmount();
        subwalletAmount.setFood(foodwalletAmount);
        User user = userManager.getForWrite(Label.FOODWALLET);
        WalletHelpers.updateFoodWalletBalance(user, Integer.parseInt(foodwalletAmount));
        Double mainWalletBalance = WalletHelpers.getWalletBalance(user);
        WalletHelpers.withdrawOnlyFromMainWallet(user, mainWalletBalance);
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(foodWalletAmtDouble);
        OrderDTO orderDTO = new OrderFactory.SubWallet(Constants.MerchantType.FOOD_MERCHANT_ADDNPAY, theme, user)
                .setTXN_AMOUNT("25")
                .setSubwallet_Details(getSubWalletParameter(subwalletAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        new ResponsePage().waitUntilLoads();
        String txnId = new TxnStatusValidations().validateWalletTxnStatus(orderDTO);
        orderDTO.setTxnId(txnId);
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(0.00);
        Assertions.assertThat(WalletHelpers.getWalletBalance(user)).isEqualTo(0.00);
        Response response = PGPHelpers.masterRefund_FoodWallet_CheckSum(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), String.valueOf(CommonHelpers.getRandomWithSize(8)), "25", orderDTO.getTxnId(), null, subwalletAmount);
        validateRefundStatus:
        {
            RefundStatusHelper refundStatusHelper = PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), response.jsonPath().getString("REFID"), true);
            Map map = refundStatusHelper.getRefundBy("PAYMENTMODE", "PPI");
            refundStatusHelper.validate(map, "GATEWAY", "WALLET");
            refundStatusHelper.validate(map, "STATUS", "TXN_SUCCESS");
            refundStatusHelper.validate(map, "REFUNDAMOUNT", String.valueOf(Double.parseDouble(foodwalletAmount)));
            map = refundStatusHelper.getRefundBy("PAYMENTMODE", "CC");
            refundStatusHelper.validate(map, "GATEWAY", "HDFC");
            refundStatusHelper.validate(map, "STATUS", "TXN_SUCCESS");
            refundStatusHelper.validate(map, "REFUNDAMOUNT", ccAmount);
        }
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(Double.parseDouble(foodwalletAmount));
        Assertions.assertThat(Double.compare(WalletHelpers.getWalletBalance(user), ccAmount)).isEqualTo(0);
    }


    @Parameters({"theme"})
    @Test(description = "Validate success add n pay partial refund txn, when Txn is (Total = 4, CC=0, food=2, mainWallet =2) and refund amount is( refund=2, wallet=1, food=1, CC=0)")
    public void PGP_SuccessRefund_Partial_AddNPay(@Optional("merchant4") String theme) throws Exception {
        String foodwalletAmount = "2";
        String foodWalletRefund = "1";
        Double foodWalletAmtDouble = Double.parseDouble(foodwalletAmount);
        //set food wallet txn amount
        SubWalletAmount subwalletAmount = new SubWalletAmount();
        subwalletAmount.setFood(foodwalletAmount);
        //set food wallet refund amount
        SubWalletAmount subwalletAmountRefund = new SubWalletAmount();
        subwalletAmountRefund.setFood(foodWalletRefund);
        User user = userManager.getForWrite(Label.FOODWALLET);
        WalletHelpers.modifyBalance(user, 2.0);
        WalletHelpers.updateFoodWalletBalance(user, Integer.parseInt(foodwalletAmount));
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(foodWalletAmtDouble);
        OrderDTO orderDTO = new OrderFactory.SubWallet(Constants.MerchantType.FOOD_MERCHANT_ADDNPAY, theme, user)
                .setTXN_AMOUNT("4")
                .setSubwallet_Details(getSubWalletParameter(subwalletAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);
        new ResponsePage().waitUntilLoads();
        String txnId = new TxnStatusValidations().validateWalletTxnStatus(orderDTO);
        orderDTO.setTxnId(txnId);
        WalletPaymentConfirmation paymentConfirmations = new WalletPaymentConfirmation().confirmation(txnId);
        JsonPath js = paymentConfirmations.execute().jsonPath();

        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(0.00);
        Assertions.assertThat(WalletHelpers.getWalletBalance(user)).isEqualTo(0.00);
        Response response = PGPHelpers.masterRefund_FoodWallet_CheckSum(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), String.valueOf(CommonHelpers.getRandomWithSize(8)), "2", orderDTO.getTxnId(), null, subwalletAmountRefund);
        validateRefundStatus:
        {
            RefundStatusHelper refundStatusHelper = PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), response.jsonPath().getString("REFID"), true);
            Map map = refundStatusHelper.getRefundBy("PAYMENTMODE", "PPI");
            refundStatusHelper.validate(map, "GATEWAY", "WALLET");
            refundStatusHelper.validate(map, "STATUS", "TXN_SUCCESS");
            refundStatusHelper.validate(map, "REFUNDAMOUNT", "2.0");
        }
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(Double.parseDouble(foodWalletRefund));
        Assertions.assertThat(WalletHelpers.getWalletBalance(user)).isEqualTo(1.0);
    }


    @Parameters({"theme"})
    @Test(description = "Validate Success refund of add n pay txn when add money limit is breached on user before refund", groups = Group.Status.TO_BE_FIXED)
    public void PGP_SuccessAdNPayRefund_WhenLimitBreached(@Optional("merchant4") String theme) throws Exception {
        String foodwalletAmount = "2";
        Double foodWalletAmtDouble = Double.parseDouble(foodwalletAmount);
        //set food wallet txn amount
        SubWalletAmount subwalletAmount = new SubWalletAmount();
        subwalletAmount.setFood(foodwalletAmount);
        User user = userManager.getForWrite(Label.FOODWALLET);
        WalletHelpers.updateFoodWalletBalance(user, Integer.parseInt(foodwalletAmount));
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(foodWalletAmtDouble);
        OrderDTO orderDTO = new OrderFactory.SubWallet(Constants.MerchantType.FOOD_MERCHANT_ADDNPAY, theme, user)

                .setTXN_AMOUNT("2")
                .setSubwallet_Details(getSubWalletParameter(subwalletAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);
        new ResponsePage().waitUntilLoads();
        String txnId = new TxnStatusValidations().validateWalletTxnStatus(orderDTO);
        orderDTO.setTxnId(txnId);
        Assertions.assertThat(WalletHelpers.getFoodWalletBalance(user)).isEqualTo(0.00);
        try {
            WalletHelpers.breachAddMoneyLimit(user);
            Response response = PGPHelpers.masterRefund_FoodWallet_CheckSum(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), String.valueOf(CommonHelpers.getRandomWithSize(8)), "2", orderDTO.getTxnId(), null, subwalletAmount);
            validateRefundStatus:
            {
                RefundStatusHelper refundStatusHelper = PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), response.jsonPath().getString("REFID"), true);
                Map map = refundStatusHelper.getRefundBy("PAYMENTMODE", "PPI");
                refundStatusHelper.validate(map, "GATEWAY", "WALLET");
                refundStatusHelper.validate(map, "STATUS", "TXN_SUCCESS");
                refundStatusHelper.validate(map, "REFUNDAMOUNT", "2.0");
            }
        } catch (Exception e) {

        } finally {
            WalletHelpers.setLimitAuditInfoDefault(user);
        }

    }
}
