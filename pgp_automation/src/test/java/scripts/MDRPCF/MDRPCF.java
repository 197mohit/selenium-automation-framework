package scripts.MDRPCF;

import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PostpaidHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.Native.PcfHelpher;

import java.util.Date;

import static com.paytm.appconstants.Constants.Owner.GAGANDEEP;

@Feature("PGP-21945")
@Owner("Tarun")
public class MDRPCF extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private double txnAmount = 2.00;
    private String theme= "enhancedweb";

    //Boss Panel http://10.142.51.67 >> Manage Merchants >>Commission >> Merchant ID >> Add specific commission to payMode

    Constants.MerchantType mdrPCF = Constants.MerchantType.MDR_PCF;
    Constants.MerchantType mdrPCF_PG2_RTDD = Constants.MerchantType.MDR_PCF_PG2_RTDD;

    private void pause(int seconds)
    {
        try {
            Thread.sleep((seconds * 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

///////////////   Test Cases   ////////////////
    
    
    @Parameters({"theme"})
    @Test(description = "Verify a successful CC txn on MDR merchant applied with PCF commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulCCTxnForMDRPlusPCF(@Optional("enhancedweb_revamp") String theme) throws Exception {
        PcfHelpher.assertDynamicChargeTarget(mdrPCF_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(mdrPCF_PG2_RTDD,"N");// MDR Flow
        String payMode = "CC";

        User user = userManager.getForRead(Label.BASIC);

        OrderDTO orderDTO = new OrderFactory.PGOnly(mdrPCF_PG2_RTDD, theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setSSO_TOKEN(user.ssoToken())
                .build();

        JsonPath fetchPCFResponse = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO,"",user,"CREDIT_CARD");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(fetchPCFResponse,"CREDIT_CARD");
        Double ccTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(fetchPCFResponse,"CREDIT_CARD");

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabCreditCard().click();
        if(theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WEB) ||theme.equalsIgnoreCase(Constants.Theme.ENHANCEDWAP))
        { PcfHelpher.validateCashierPageAmount(cashierPage,ccTxnAmount);}

        cashierPage.payBy(Constants.PayMode.CC);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);


        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("CREDIT_CARD", "VISA", "HDFC", "API");



        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }

    @Parameters({"theme"})
    @Test(description = "Verify a successful CC txn with MASTER on MDR merchant applied with PCF commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulCCWithMasterTxnForMDRPlusPCF(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType mdrPCF = Constants.MerchantType.SEAMLESS_MDR_PCF_PG2_RTDD;
        PcfHelpher.assertDynamicChargeTarget(mdrPCF,"Y");
        PcfHelpher.assertConvFee(mdrPCF,"N");// MDR Flow
        String payMode = "CC";
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD);

        User user = userManager.getForRead(Label.BASIC);

        OrderDTO orderDTO = new OrderFactory.PGOnly(mdrPCF, theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setSSO_TOKEN(user.ssoToken())
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO,"MASTER",user,"CREDIT_CARD");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"CREDIT_CARD");
        Double ccTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(jsonPath,"CREDIT_CARD");

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabCreditCard().waitUntilClickable();
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());

        PcfHelpher.validateCashierPageAmount(cashierPage,ccTxnAmount);

        cashierPage.fillExpiryMonth(paymentDTO.getExpMonth());
        cashierPage.fillExpiryYear(paymentDTO.getExpYear().substring(2));
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());

        cashierPage.buttonPGPayNow().click();

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("MASTER");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("CREDIT_CARD", "MASTER", "HDFC", "API");


        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }


    @Parameters({"theme"})
    @Test(description = "Verify a successful DC txn(applied with PCF commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulDCTxnForMDRPlusPCF(@Optional("enhancedweb_revamp") String theme) throws Exception {
        PcfHelpher.assertDynamicChargeTarget(mdrPCF_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(mdrPCF_PG2_RTDD,"N");// MDR Flow
        String payMode = "DC";

        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(mdrPCF_PG2_RTDD, theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO,"",user,"DEBIT_CARD");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"DEBIT_CARD");
        Double dcTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(jsonPath,"DEBIT_CARD");

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabDebitCard().click();
        pause(1);
        PcfHelpher.validateCashierPageAmount(cashierPage,dcTxnAmount);

        cashierPage.payBy(Constants.PayMode.DC);


        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("DEBIT_CARD", "VISA", "HDFC", "API");


        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }

    @Parameters({"theme"})
    @Test(description = "Verify a successful DC txn with MASTER on MDR merchant applied with PCF commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulDCWithMasterTxnForMDRPlusPCF(@Optional("enhancedweb") String theme) throws Exception {
        Constants.MerchantType mdrPCF = Constants.MerchantType.SEAMLESS_MDR_PCF_PG2_RTDD;
        PcfHelpher.assertDynamicChargeTarget(mdrPCF,"Y");
        PcfHelpher.assertConvFee(mdrPCF,"N");// MDR Flow
        String payMode = "DC";
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER);

        User user = userManager.getForWrite(Label.BASIC);

        OrderDTO orderDTO = new OrderFactory.PGOnly(mdrPCF, theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setSSO_TOKEN(user.ssoToken())
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO,"MASTER",user,"DEBIT_CARD");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"DEBIT_CARD");
        Double ccTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(jsonPath,"DEBIT_CARD");

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabDebitCard().waitUntilClickable();
        cashierPage.tabDebitCard().click();
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getDebitCardNumber());

         PcfHelpher.validateCashierPageAmount(cashierPage,ccTxnAmount);

        cashierPage.fillExpiryMonth(paymentDTO.getExpMonth());
        cashierPage.fillExpiryYear(paymentDTO.getExpYear().substring(2));
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());

        cashierPage.buttonPGPayNow().click();

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus =PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("MASTER");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("DEBIT_CARD", "MASTER", "HDFC", "API");


        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }



    @Parameters({"theme"})
    @Test(description = "Verify a successful saved CC txn on MDR merchant applied with PCF commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulSavedCCTxnForMDRPlusPCF(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());
        PcfHelpher.assertDynamicChargeTarget(mdrPCF,"Y");
        PcfHelpher.assertConvFee(mdrPCF,"N");// MDR Flow
        String payMode = "CC";

        OrderDTO orderDTO = new OrderFactory.PGOnly(mdrPCF, theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setSSO_TOKEN(user.ssoToken())
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO,"",user,"CREDIT_CARD");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"CREDIT_CARD");
        Double ccTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(jsonPath,"CREDIT_CARD");

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabSavedCard().waitUntilClickable();
        cashierPage.tabSavedCard().click();

        PcfHelpher.validateCashierPageAmount(cashierPage,ccTxnAmount);

        cashierPage.payBy(Constants.PayMode.SAVED_CARD);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("CREDIT_CARD", "VISA", "HDFC", "API");



        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }

    @Parameters({"theme"})
    @Test(description = "Verify a successful NB txn with ICICI(applied with PCF commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulNBTxnForMDRPlusPCF(@Optional("enhancedweb_revamp") String theme) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(mdrPCF_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(mdrPCF_PG2_RTDD,"N");// MDR Flow
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        String payMode = "NB";

        User user  = userManager.getForWrite(Label.BASIC);

        OrderDTO orderDTO = new OrderFactory.PGOnly(mdrPCF_PG2_RTDD, theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO,"ICICI",user,"NET_BANKING");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"NET_BANKING");
        Double nbTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(jsonPath,"NET_BANKING");

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabNetBanking().click();
        PcfHelpher.validateCashierPageAmount(cashierPage,nbTxnAmount);
        cashierPage.payBy(Constants.PayMode.NB,paymentDTO);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("NET_BANKING", "ICICI", "ICICI", "API");

        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }


    @Parameters({"theme"})
    @Test(description = "Verify a successful PPI txn(applied with PCF commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulPPITxnForMDRPlusPCF(@Optional("enhancedweb") String theme) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(mdrPCF_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(mdrPCF_PG2_RTDD,"N");// MDR Flow
        String payMode = "PPI";

        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.MDR_PCF_PG2_RTDD, theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setSSO_TOKEN(user.ssoToken())
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO,"",user,"BALANCE");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"BALANCE");
        Double walletTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(jsonPath,"BALANCE");

        WalletHelpers.modifyBalance(user,Double.valueOf(walletTxnAmount));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PcfHelpher.validateCashierPageAmount(cashierPage,walletTxnAmount);
        cashierPage.buttonWalletPayNow().waitUntilClickable();
        cashierPage.buttonWalletPayNow().click();

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);

        //MerchantStatus
        PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);

        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }

    @Parameters({"theme"})
    @Test(description = "Verify a successful PPBL txn(applied with PCF commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulPPBLTxnForMDRPlusPCF(@Optional("enhancedweb") String theme) throws Exception {

         PcfHelpher.assertDynamicChargeTarget(mdrPCF_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(mdrPCF,"N");// MDR Flow
        String payMode = "PPBL";
        User user = userManager.getForWrite(Label.PPBL);

        OrderDTO orderDTO = new OrderFactory.PGOnly(mdrPCF_PG2_RTDD, theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setSSO_TOKEN(user.ssoToken())
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO,"",user,payMode);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,payMode);
        Double ppblTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(jsonPath,payMode);

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.checkboxPPBL().check();
        PcfHelpher.validateCashierPageAmount(cashierPage,ppblTxnAmount);
        cashierPage.payBy(Constants.PayMode.PPBL);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF_PG2_RTDD,"NB",chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus =  PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,"NB",chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,"NB",chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF_PG2_RTDD,"NB",chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,"NB",chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,"NB",chargeAmount);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("NET_BANKING", "PPBL", "PPBL", "API");


        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,"NB");
    }

    @Parameters({"theme"})
    @Test(description = "Verify a successful UPI Collect txn(applied with PCF commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulUPITxnForMDRPlusPCF(@Optional("enhancedweb") String theme) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(mdrPCF_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(mdrPCF,"N");// MDR Flow
        String payMode = "UPI";

        User user  = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(mdrPCF_PG2_RTDD, theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO,"",user,payMode);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,payMode);
        Double upiTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(jsonPath,payMode);

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        PcfHelpher.validateCashierPageAmount(cashierPage,upiTxnAmount);
        cashierPage.payBy(Constants.PayMode.UPI);
        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("UPI", "UPI", "ICICI", "API");



        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }

    @Parameters({"theme"})
    @Test(description = "Verify a successful EMI txn(applied with PCF commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulEMITxnForMDRPlusPCF(@Optional("enhancedweb_revamp") String theme) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(mdrPCF_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(mdrPCF,"N");// MDR Flow
        String payMode = "EMI";

        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(mdrPCF_PG2_RTDD, theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO,"",user,payMode);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,payMode);
        Double emiTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(jsonPath,payMode);

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabEMI().click();
        PcfHelpher.validateCashierPageAmount(cashierPage,emiTxnAmount);
        cashierPage.payBy(Constants.PayMode.EMI);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);


        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("EMI", "HDFC", "HDFC", "API");



        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }


    //Not in scope of Phase 1, but will be a part of next phase
//    @Parameters({"theme"})
//    @Test(description = "Verify a successful EMI DC txn(applied with PCF commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB",enabled = false)
    public void successfulEMIDCTxnForMDRPlusPCF(@Optional("enhancedweb") String theme) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(mdrPCF,"Y");
        PcfHelpher.assertConvFee(mdrPCF,"N");// MDR Flow
        String payMode = "EMI_DC";
        PaymentDTO paymentDTO  = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER).setBankName("ICICI Bank Debit Card").setMonth(3);

        User user = userManager.getForWrite(Label.EMIDC);
        SavedCardHelpers.deleteSavedCard(user);

        OrderDTO orderDTO = new OrderFactory.PGOnly(mdrPCF, theme)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setSSO_TOKEN(user.ssoToken())
                .build();
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO,"",user,payMode);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,payMode);
        Double emiTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(jsonPath,payMode);

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabEMI().click();
        cashierPage.dropdownEmiBanks().selectByVisibleText(paymentDTO.getBankName());
        PcfHelpher.validateCashierPageAmount(cashierPage,emiTxnAmount);
        cashierPage.emiDurationOption(paymentDTO.getMonth()).click();
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());
        cashierPage.fillExpiryMonth(paymentDTO.getExpMonth());
        cashierPage.fillExpiryYear(paymentDTO.getExpYear().substring(2));
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        cashierPage.buttonPGPayNow().click();

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF,payMode,chargeAmount);

        //MerchantStatus
        PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);

        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }

    @Parameters({"theme"})
    @Test(description = "Verify a successful Paytm Postpaid txn(applied with PCF commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulPaytmCCTxnForMDRPlusPCF(@Optional("enhancedweb_revamp") String theme) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(mdrPCF_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(mdrPCF_PG2_RTDD,"N");// MDR Flow
        String payMode = "Paytm Postpaid";
        User user = userManager.getForWrite(Label.POSTPAID);
        OrderDTO orderDTO = new OrderFactory.PGOnly(mdrPCF_PG2_RTDD, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO,"",user,"PAYTM_DIGITAL_CREDIT");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"PAYTM_DIGITAL_CREDIT");
        Double paytmCCTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(jsonPath,"PAYTM_DIGITAL_CREDIT");

        PostpaidHelpers.updateBalance(String.valueOf(Math.ceil(paytmCCTxnAmount)));

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.radioButtonPaytmPostpaid().click();
        PcfHelpher.validateCashierPageAmount(cashierPage,paytmCCTxnAmount);
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus =PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);
        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("PAYTM_DIGITAL_CREDIT", "PAYTMCC", "PAYTMCC", "API");


        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }

    // App Invoke Flow
    @Parameters({"theme"})
    @Test(description = "Verify a successful CC txn(applied with PCF commission) with Show Payment Page Flow when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulPaytmCCTxnForMDRPlusPCFAppInvoke(@Optional("enhancedweb") String theme) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(mdrPCF_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(mdrPCF_PG2_RTDD,"N");// MDR Flow
        String payMode = "CC";

        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mdrPCF_PG2_RTDD)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(mdrPCF_PG2_RTDD,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO,"",user,"CREDIT_CARD");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"CREDIT_CARD");
        Double ccTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(jsonPath,"CREDIT_CARD");

        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabCreditCard().click();
        if (theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WEB) || theme.equalsIgnoreCase(Constants.Theme.ENHANCEDWAP))
        PcfHelpher.validateCashierPageAmount(cashierPage,ccTxnAmount);

        cashierPage.payBy(Constants.PayMode.CC);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);


        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);
        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("CREDIT_CARD", "VISA", "HDFC", "API");



        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }

    @Parameters({"theme"})
    @Test(description = "Verify a successful DC txn(applied with PCF commission) with Show Payment Page Flow when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulPaytmDCTxnForMDRPlusPCFAppInvoke(@Optional("enhancedweb") String theme) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(mdrPCF_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(mdrPCF_PG2_RTDD,"N");// MDR Flow
        String payMode = "DC";

        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mdrPCF)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(mdrPCF_PG2_RTDD,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO,"",user,"DEBIT_CARD");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"DEBIT_CARD");
        Double dcTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(jsonPath,"DEBIT_CARD");

        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabDebitCard().click();
        PcfHelpher.validateCashierPageAmount(cashierPage,dcTxnAmount);

        cashierPage.payBy(Constants.PayMode.DC);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);


        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("DEBIT_CARD", "VISA", "HDFC", "API");

        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }

    @Parameters({"theme"})
    @Test(description = "Verify a successful saved DC txn(applied with PCF commission) with Show Payment Page Flow when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulsavedCCTxnForMDRPlusPCFAppInvoke(@Optional("enhancedweb_revamp") String theme) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(mdrPCF,"Y");
        PcfHelpher.assertConvFee(mdrPCF,"N");// MDR Flow
        String payMode = "DC";
        User user =userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getDebitCardNumber());

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mdrPCF)
                .setTxnValue(String.valueOf(txnAmount))
                .setSsoToken(user.ssoToken())
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(mdrPCF,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO,"",user,"DEBIT_CARD");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"DEBIT_CARD");
        Double dcTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(jsonPath,"DEBIT_CARD");


        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabSavedCard().waitUntilClickable();
        cashierPage.tabSavedCard().click();
        PcfHelpher.validateCashierPageAmount(cashierPage,dcTxnAmount);
        cashierPage.payBy(Constants.PayMode.SAVED_CARD);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus =PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);



        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("DEBIT_CARD", "VISA", "HDFC", "API");


        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
        
    }

    @Parameters({"theme"})
    @Test(description = "Verify a successful NB txn(applied with PCF commission) with Show Payment Page Flow when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulPaytmNBTxnForMDRPlusPCFAppInvoke(@Optional("enhancedweb") String theme) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(mdrPCF_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(mdrPCF,"N");// MDR Flow
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        String payMode = "NB";

        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mdrPCF_PG2_RTDD)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(mdrPCF_PG2_RTDD,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO,"",user,"NET_BANKING");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"NET_BANKING");
        Double nbTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(jsonPath,"NET_BANKING");

        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabNetBanking().click();
        PcfHelpher.validateCashierPageAmount(cashierPage,nbTxnAmount);

        cashierPage.payBy(Constants.PayMode.NB,paymentDTO);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus =PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("NET_BANKING", "ICICI", "ICICI", "API");

        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }

    @Parameters({"theme"})
    @Test(description = "Verify a successful PPI txn(applied with PCF commission) with Show Payment Page Flow when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulPaytmPPITxnForMDRPlusPCFAppInvoke(@Optional("enhancedweb") String theme) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(mdrPCF_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(mdrPCF_PG2_RTDD,"N");// MDR Flow
        String payMode = "PPI";

        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mdrPCF_PG2_RTDD)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(mdrPCF_PG2_RTDD,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO,"",user,"BALANCE");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"BALANCE");
        Double walletTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(jsonPath,"BALANCE");

        WalletHelpers.modifyBalance(user,Double.valueOf(walletTxnAmount));
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.buttonWalletPayNow().waitUntilClickable();
        cashierPage.buttonWalletPayNow().click();

        PcfHelpher.validateCashierPageAmount(cashierPage,walletTxnAmount);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);

        //MerchantStatus
        PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);

        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }

    @Parameters({"theme"})
    @Test(description = "Verify a successful PPBL txn(applied with PCF commission) with Show Payment Page Flow when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulPaytmPPBLTxnForMDRPlusPCFAppInvoke(@Optional("enhancedweb") String theme) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(mdrPCF_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(mdrPCF_PG2_RTDD,"N");// MDR Flow
        String payMode = "PPBL";

        User user = userManager.getForWrite(Label.PPBL);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mdrPCF_PG2_RTDD)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(mdrPCF_PG2_RTDD,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO,"",user,payMode);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,payMode);
        Double ppblTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(jsonPath,payMode);

        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.checkboxPPBL().check();
        PcfHelpher.validateCashierPageAmount(cashierPage,ppblTxnAmount);
        cashierPage.payBy(Constants.PayMode.PPBL);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF_PG2_RTDD,"NB",chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus =PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,"NB",chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,"NB",chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF_PG2_RTDD,"NB",chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,"NB",chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,"NB",chargeAmount);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("NET_BANKING", "PPBL", "PPBL", "API");


        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,"NB");
    }

    @Parameters({"theme"})
    @Test(description = "Verify a successful UPI Collect txn(applied with PCF commission) with Show Payment Page Flow when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulPaytmUPICollectTxnForMDRPlusPCFAppInvoke(@Optional("enhancedweb") String theme) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(mdrPCF_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(mdrPCF_PG2_RTDD,"N");// MDR Flow
        String payMode = "UPI";

        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mdrPCF_PG2_RTDD)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(mdrPCF_PG2_RTDD,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO,"",user,"UPI");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"UPI");
        Double upiTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(jsonPath,"UPI");

        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        PcfHelpher.validateCashierPageAmount(cashierPage,upiTxnAmount);
        cashierPage.payBy(Constants.PayMode.UPI);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus =PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);
        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("UPI", "UPI", "ICICI", "API");

        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }

    @Parameters({"theme"})
    @Test(description = "Verify a successful EMI txn(applied with PCF commission) with Show Payment Page Flow when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulPaytmEMITxnForMDRPlusPCFAppInvoke(@Optional("enhancedweb") String theme) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(mdrPCF_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(mdrPCF,"N");// MDR Flow
        String payMode = "EMI";

        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mdrPCF_PG2_RTDD)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(mdrPCF_PG2_RTDD,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO,"",user,"EMI");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"EMI");
        Double emiTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(jsonPath,"EMI");

        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabEMI().click();
        PcfHelpher.validateCashierPageAmount(cashierPage,emiTxnAmount);
        cashierPage.payBy(Constants.PayMode.EMI);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("HDFC");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("EMI", "HDFC", "HDFC", "API");

        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }

    @Test(description = "Validate Successful Seamless Txn with CC paymode as PCF for MDR merchant")
    public void successfulSeamlessTxnWithCC() throws Exception {
        Constants.MerchantType seamlessMDRPCF = Constants.MerchantType.SEAMLESS_MDR_PCF_PG2_RTDD;

        PcfHelpher.assertDynamicChargeTarget(seamlessMDRPCF,"Y");
        PcfHelpher.assertConvFee(seamlessMDRPCF,"N");// MDR Flow
        String payMode = "CC";
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.Seamless(seamlessMDRPCF, payMode, user)
                .setTHEME(theme)
                //.setSSO_TOKEN(user.ssoToken())
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO,"",user,"CREDIT_CARD");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"CREDIT_CARD");
        checkoutPage.createOrder(orderDTO);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,seamlessMDRPCF,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus =  PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,seamlessMDRPCF,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("CREDIT_CARD", "VISA", "HDFC", "API");

        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);

    }

    @Test(description = "Validate Successful Seamless Txn with DC paymode as PCF for MDR merchant")
    public void successfulSeamlessTxnWithDC() throws Exception {
        Constants.MerchantType seamlessMDRPCF = Constants.MerchantType.SEAMLESS_MDR_PCF_PG2_RTDD;

        PcfHelpher.assertDynamicChargeTarget(seamlessMDRPCF,"Y");
        PcfHelpher.assertConvFee(seamlessMDRPCF,"N");// MDR Flow
        String payMode = "DC";
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.Seamless(seamlessMDRPCF, payMode, user)
                .setTHEME(theme)
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO,"",user,"DEBIT_CARD");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"DEBIT_CARD");
        checkoutPage.createOrder(orderDTO);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,seamlessMDRPCF,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus =  PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,seamlessMDRPCF,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);
        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("DEBIT_CARD", "VISA", "HDFC", "API");

        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);


    }

    @Test(description = "Validate Successful Seamless Txn with NB paymode as PCF for MDR merchant")
    public void successfulSeamlessTxnWithNB() throws Exception {
        Constants.MerchantType seamlessMDRPCF = Constants.MerchantType.SEAMLESS_MDR_PCF_PG2_RTDD;

        PcfHelpher.assertDynamicChargeTarget(seamlessMDRPCF,"Y");
        PcfHelpher.assertConvFee(seamlessMDRPCF,"N");// MDR Flow
        String payMode = "NB";
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.Seamless(seamlessMDRPCF, payMode, user)
                .setBANK_CODE("ICICI")
                .setTHEME(theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO,"ICICI",user,"NET_BANKING");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"NET_BANKING");
        checkoutPage.createOrder(orderDTO);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,seamlessMDRPCF,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus =PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,seamlessMDRPCF,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("NET_BANKING", "ICICI", "ICICI", "API");


        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);

    }
    //TODO
//    @Test(description = "Validate Successful Seamless Txn with PPBL paymode as PCF for MDR merchant",enabled = false)
    public void successfulPPBLSeamlessTxnWithNB() throws Exception {
        Constants.MerchantType seamlessMDRPCF = Constants.MerchantType.SEAMLESS_MDR_PCF;

        PcfHelpher.assertDynamicChargeTarget(seamlessMDRPCF,"Y");
        PcfHelpher.assertConvFee(seamlessMDRPCF,"N");// MDR Flow
        String payMode = "NB";
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.Seamless(seamlessMDRPCF, payMode, user)
                .setBANK_CODE("PPBL")
                .setTHEME(theme)
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO,"PPBL",user,"NET_BANKING");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"NET_BANKING");
        checkoutPage.createOrder(orderDTO);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,seamlessMDRPCF,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus =PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,seamlessMDRPCF,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(response.jsonPath().getString("result.feeFactor[0]")).isEqualTo("NET_BANKING|PPBL|PPBL")
                .as("FEE Factor is Not coming from PGPLUS BO");



        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);

    }

    //Seamless NB
    @Feature("PGP-25168")
    @Parameters({"theme"})
    @Test(description = "Validate Successful Seamless NB flow Txn with NB paymode as PCF for MDR merchant")
    public void successfulSeamlessTxnWithSeamlessNB(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType seamlessMDRPCF = Constants.MerchantType.SEAMLESS_MDR_PCF_PG2_RTDD;

        PcfHelpher.assertDynamicChargeTarget(seamlessMDRPCF,"Y");
        PcfHelpher.assertConvFee(seamlessMDRPCF,"N");// MDR Flow
        String payMode = "NB";
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(seamlessMDRPCF, theme)
                .setPAYMENT_TYPE_ID(payMode)
                .setPAYMENT_MODE_ONLY("YES")
                .setBANK_CODE("ICICI")
                .setAUTH_MODE("USRPWD")
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO,"ICICI",user,"NET_BANKING");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"NET_BANKING");
        checkoutPage.createOrder(orderDTO);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,seamlessMDRPCF,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,seamlessMDRPCF,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);
        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("NET_BANKING", "ICICI", "ICICI", "API");


        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);

    }

//    @Feature("PGP-25168")
//    @Parameters({"theme"})
//    @Test(description = "Validate Successful Seamless NB  PPBL flow Txn with PPBL paymode as PCF for MDR merchant",enabled = false)
    public void successfulSeamlessTxnWithSeamlessNBPPBL(@Optional("enhancedweb") String theme) throws Exception {

        Constants.MerchantType seamlessMDRPCF = Constants.MerchantType.SEAMLESS_MDR_PCF;
        PcfHelpher.assertDynamicChargeTarget(seamlessMDRPCF,"Y");
        PcfHelpher.assertConvFee(seamlessMDRPCF,"N");// MDR Flow
        String payMode = "NB";
        PaymentDTO paymentDTO = new PaymentDTO();
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(seamlessMDRPCF, theme)
                .setPAYMENT_TYPE_ID(payMode)
                .setPAYMENT_MODE_ONLY("YES")
                .setBANK_CODE("PPBL")
                .setMpin(paymentDTO.getPasscode())
                .setAUTH_MODE("USRPWD")
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO,"PPBL",user,"NET_BANKING");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"NET_BANKING");
        checkoutPage.createOrder(orderDTO);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,seamlessMDRPCF,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus =PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,seamlessMDRPCF,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(response.jsonPath().getString("result.feeFactor[0]")).isEqualTo("NET_BANKING|PPBL|PPBL")
                .as("FEE Factor is Not coming from PGPLUS BO");


        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);

    }

    @Feature("PGP-29616")
    @Owner(GAGANDEEP)
    @Test(description = "Validate Successful Seamless Txn with new UDF_2 field for CC paymode")
    public void successfulSeamlessWithNewFieldUDFTxnWithCC() throws Exception {
        Constants.MerchantType seamlessMDRPCF = Constants.MerchantType.SEAMLESS_MDR_PCF_PG2_RTDD;
        String UDF_2 = "default_udf2";
        String payMode = "CC";
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.Seamless(seamlessMDRPCF, payMode, user)
                .setTHEME(theme)
                .setUDF_2(UDF_2)
                .build();

        checkoutPage.createOrder(orderDTO);
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
                .validateUDF(UDF_2)
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(Constants.MerchantType.SEAMLESS_MDR_PCF_PG2_RTDD.getKey())
                .assertAll();

    }




    @Feature("PGP-29611")
    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description = "Verify a successful NB txn(applied with PCF commission) with Show Payment Page Flow when For Retry")
    public void successfulPaytmNBTxnForMDRPlusPCFAppInvokeUsingRetry(@Optional("enhancedweb_revamp") String theme) throws Exception {

        PcfHelpher.assertDynamicChargeTarget(mdrPCF,"Y");
        PcfHelpher.assertConvFee(mdrPCF,"N");// MDR Flow
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        String payMode = "NB";

        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mdrPCF)
                .setOrderId(CommonHelpers.generateOrderId() + "RETRY")
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(mdrPCF,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO,"",user,"NET_BANKING");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"NET_BANKING");
        Double nbTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(jsonPath,"NET_BANKING");

        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabNetBanking().click();
        PcfHelpher.validateCashierPageAmount(cashierPage,nbTxnAmount);

        cashierPage.payBy(Constants.PayMode.NB,paymentDTO);
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
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
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(mdrPCF.getKey())
                .assertAll();

    }

}

