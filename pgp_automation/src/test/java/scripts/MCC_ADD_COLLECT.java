package scripts;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
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
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.RestAssured;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

public class MCC_ADD_COLLECT extends PGPBaseTest{

    private static final String FULLKYC_PC = "4814";
    private static final String MINKYC_PC = "7013";

    @Owner("Shubham Soni")
    @Feature("PGP-46976 & PGP-46374")
    @Parameters({"theme"})
    @Test(description = "For UPI Collect ADDNAPAY transaction validate MCC is passed as Payee-code in collect-merchant-request , when user having FULL KYC")
    public void addnPay_UPI_Collect_FULLKYC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        User user = userManager.getForWrite(Label.ZEROWALLET);
        String CustId = user.custId();
        String walletState = "PAYTM_PRIME_WALLET";
        WalletHelpers.setWalletType(CustId,walletState);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.ADDNPAY_MCC_ADDMONEY, theme, user).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.SAVED_UPI);
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
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .assertAll();

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
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.00);
        String WalletLimitLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"wallet-web/walletLimits");
        Assertions.assertThat(WalletLimitLogs).contains("\"isWalletCategoryRequired\":true");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_PAY_ORDER");
        String passThroughExtendInfologs = logs.substring(logs.indexOf("passThroughExtendInfo")+24,logs.indexOf("\",\"virtualPaymentAddr\""));
        String decrypted= PGPHelpers.Base64Decode(passThroughExtendInfologs);
        Assertions.assertThat(decrypted).contains("\"walletRbiType\":\"FULL KYC\"");
        String instalogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"pg.router.paytm.upi.payment.request");
        String extendedInfo = instalogs.substring(instalogs.indexOf("\"extendInfo\"")+14,instalogs.indexOf("\", \"settleType\""));
        String decryptedInstaEI=PGPHelpers.Base64Decode(extendedInfo);
        String passThroughExtendInfoInstalogs = decryptedInstaEI.substring(decryptedInstaEI.indexOf("\"passThroughExtendInfo\"")+24,decryptedInstaEI.indexOf("\",\"directPassThroughInfo"));
        String decryptedInsta=PGPHelpers.Base64Decode(passThroughExtendInfoInstalogs);
        Assertions.assertThat(decryptedInsta).contains("\"walletRbiType\":\"FULL KYC\"");
        String instaUPIrequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"PPBL_UPI/upi/collect-merchant-request");
        Assertions.assertThat(instaUPIrequestlogs).contains("payee-code="+FULLKYC_PC);
    }

    @Owner("Shubham Soni")
    @Feature("PGP-46976 & PGP-46374")
    @Parameters({"theme"})
    @Test(description = "For UPI Collect ADDNAPAY transaction validate MINMCC is passed as Payee-code in collect-merchant-request , when user having MIN KYC")
    public void addnPay_UPI_Collect_MINKYC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.ZEROWALLET);
        String CustId = user.custId();
        String walletState = "PAYTM_BASIC_PLUS";
        WalletHelpers.setWalletType(CustId,walletState);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.ADDNPAY_MCC_ADDMONEY, theme, user).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.SAVED_UPI);
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
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .assertAll();

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
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.00);
        String WalletLimitLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"wallet-web/walletLimits");
        Assertions.assertThat(WalletLimitLogs).contains("\"isWalletCategoryRequired\":true");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_PAY_ORDER");
        String passThroughExtendInfologs = logs.substring(logs.indexOf("passThroughExtendInfo")+24,logs.indexOf("\",\"virtualPaymentAddr\""));
        String decrypted=PGPHelpers.Base64Decode(passThroughExtendInfologs);
        Assertions.assertThat(decrypted).contains("\"walletRbiType\":\"MIN KYC\"");

        String instalogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"pg.router.paytm.upi.payment.request");
        String extendedInfo = instalogs.substring(instalogs.indexOf("\"extendInfo\"")+14,instalogs.indexOf("\", \"settleType\""));
        String decryptedInstaEI=PGPHelpers.Base64Decode(extendedInfo);
        String passThroughExtendInfoInstalogs = decryptedInstaEI.substring(decryptedInstaEI.indexOf("\"passThroughExtendInfo\"")+24,decryptedInstaEI.indexOf("\",\"directPassThroughInfo"));
        String decryptedInsta=PGPHelpers.Base64Decode(passThroughExtendInfoInstalogs);
        Assertions.assertThat(decryptedInsta).contains("\"walletRbiType\":\"MIN KYC\"");
        String instaUPIrequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"/upi/collect-merchant-request");
        Assertions.assertThat(instaUPIrequestlogs).contains("payee-code="+MINKYC_PC);
    }



    @Owner("Shubham Soni")
    @Feature("PGP-46976 & PGP-46374")
    @Parameters({"theme"})
    @Test(description = "For UPI Collect ADDMONEY transaction validate MCC is passed as Payee-code in collect-merchant-request , when user having FULL KYC")
    public void addMoney_UPI_Collect_FULLKYC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.ZEROWALLET);
        String CustId = user.custId();
        String walletState = "PAYTM_PRIME_WALLET";
        WalletHelpers.setWalletType(CustId,walletState);
        WalletHelpers.modifyBalance(user, 0.00);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.ADDNPAY_MCC_ADDMONEY, theme, user)
                .setREQUEST_TYPE("ADD_MONEY")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.SAVED_UPI);
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
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName("PPBLC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
        WalletHelpers.validateBalance(user, 2.00);
        String WalletLimitLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"wallet-web/walletLimits");
        Assertions.assertThat(WalletLimitLogs).contains("\"isWalletCategoryRequired\":true");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"FUND_ORDER_PAY");
        String passThroughExtendInfologs = logs.substring(logs.indexOf("passThroughExtendInfo")+24,logs.indexOf("\",\"virtualPaymentAddr\""));
        String decrypted=PGPHelpers.Base64Decode(passThroughExtendInfologs);
        Assertions.assertThat(decrypted).contains("\"walletRbiType\":\"FULL KYC\"");

        String instalogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"pg.router.paytm.upi.payment.request");
        String extendedInfo = instalogs.substring(instalogs.indexOf("\"extendInfo\"")+14,instalogs.indexOf("\", \"settleType\""));
        String decryptedInstaEI=PGPHelpers.Base64Decode(extendedInfo);
        String passThroughExtendInfoInstalogs = decryptedInstaEI.substring(decryptedInstaEI.indexOf("\"passThroughExtendInfo\"")+24,decryptedInstaEI.indexOf("\",\"directPassThroughInfo"));
        String decryptedInsta=PGPHelpers.Base64Decode(passThroughExtendInfoInstalogs);
        Assertions.assertThat(decryptedInsta).contains("\"walletRbiType\":\"FULL KYC\"");
        String instaUPIrequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"/upi/collect-merchant-request");
        Assertions.assertThat(instaUPIrequestlogs).contains("payee-code="+FULLKYC_PC);
    }

    @Owner("Shubham Soni")
    @Feature("PGP-46976 & PGP-46374")
    @Parameters({"theme"})
    @Test(description = "For UPI Collect ADDMONEY transaction validate MINMCC is passed as Payee-code in collect-merchant-request , when user having MIN KYC")
    public void addMoney_UPI_Collect_MINKYC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.ZEROWALLET);
        String CustId = user.custId();
        String walletState = "PAYTM_BASIC_PLUS";
        WalletHelpers.setWalletType(CustId,walletState);
        WalletHelpers.modifyBalance(user, 0.00);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.ADDNPAY_MCC_ADDMONEY, theme, user)
                .setREQUEST_TYPE("ADD_MONEY")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.SAVED_UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String walletStateFinal = "PAYTM_PRIME_WALLET";
        WalletHelpers.setWalletType(CustId,walletStateFinal);
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName("PPBLC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
        WalletHelpers.validateBalance(user, 2.00);
        String WalletLimitLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"wallet-web/walletLimits");
        Assertions.assertThat(WalletLimitLogs).contains("\"isWalletCategoryRequired\":true");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"FUND_ORDER_PAY");
        String passThroughExtendInfologs = logs.substring(logs.indexOf("passThroughExtendInfo")+24,logs.indexOf("\",\"virtualPaymentAddr\""));
        String decrypted=PGPHelpers.Base64Decode(passThroughExtendInfologs);
        Assertions.assertThat(decrypted).contains("\"walletRbiType\":\"MIN KYC\"");

        String instalogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"pg.router.paytm.upi.payment.request");
        String extendedInfo = instalogs.substring(instalogs.indexOf("\"extendInfo\"")+14,instalogs.indexOf("\", \"settleType\""));
        String decryptedInstaEI=PGPHelpers.Base64Decode(extendedInfo);
        String passThroughExtendInfoInstalogs = decryptedInstaEI.substring(decryptedInstaEI.indexOf("\"passThroughExtendInfo\"")+24,decryptedInstaEI.indexOf("\",\"directPassThroughInfo"));
        String decryptedInsta=PGPHelpers.Base64Decode(passThroughExtendInfoInstalogs);
        Assertions.assertThat(decryptedInsta).contains("\"walletRbiType\":\"MIN KYC\"");
        String instaUPIrequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"/upi/collect-merchant-request");
        Assertions.assertThat(instaUPIrequestlogs).contains("payee-code="+MINKYC_PC);
    }

    @Owner("Shubham Soni")
    @Feature("PGP-46976 & PGP-46374")
    @Parameters({"isNativePlus"})
    @Test(description = "NATIVE : For UPI Collect ADDNAPAY transaction validate MCC is passed as Payee-code in collect-merchant-request , when user having FULL KYC")
    public void validateupiCollectToAddPayFullKyc(@Optional("false") Boolean isNativePlus) throws Exception {
        CheckoutPage checkoutPage = new CheckoutPage();
        String txmAmount =  "2.00";
        User user = userManager.getForRead(Label.ZEROWALLET);
        String CustId = user.custId();
        String walletState = "PAYTM_PRIME_WALLET";
        WalletHelpers.setWalletType(CustId,walletState);
        WalletHelpers.modifyBalance(user, Double.valueOf(txmAmount) - 1.00);
        Constants.MerchantType merchantType= Constants.MerchantType.ADDNPAY_MCC_ADDMONEY;
        PaymentDTO paymentDTO = new PaymentDTO();

        InitTxnDTO initTxnDTO1 = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txmAmount).
                        build();
        String txnToken1 = NativeHelpers.Validate_InitTxn(initTxnDTO1);

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO1.orderFromBody(), txnToken1,paymentDTO, PayMethodType.UPI)
                .setPaymentFlow("ADDANDPAY")
                .setPaymentMode("UPI")
                .setAUTH_MODE("USRPWD")
                .setPayerAccount("test@paytm")
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);
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
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.00);
        String WalletLimitLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"wallet-web/walletLimits");
        Assertions.assertThat(WalletLimitLogs).contains("\"isWalletCategoryRequired\":true");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        String passThroughExtendInfologs = logs.substring(logs.indexOf("passThroughExtendInfo")+24,logs.indexOf("\",\"virtualPaymentAddr\""));
        String decrypted=PGPHelpers.Base64Decode(passThroughExtendInfologs);
        Assertions.assertThat(decrypted).contains("\"walletRbiType\":\"FULL KYC\"");

        String instalogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"pg.router.paytm.upi.payment.request");
        String extendedInfo = instalogs.substring(instalogs.indexOf("\"extendInfo\"")+14,instalogs.indexOf("\", \"settleType\""));
        String decryptedInstaEI=PGPHelpers.Base64Decode(extendedInfo);
        String passThroughExtendInfoInstalogs = decryptedInstaEI.substring(decryptedInstaEI.indexOf("\"passThroughExtendInfo\"")+24,decryptedInstaEI.indexOf("\",\"directPassThroughInfo"));
        String decryptedInsta=PGPHelpers.Base64Decode(passThroughExtendInfoInstalogs);
        Assertions.assertThat(decryptedInsta).contains("\"walletRbiType\":\"FULL KYC\"");
        String instaUPIrequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"/upi/collect-merchant-request");
        Assertions.assertThat(instaUPIrequestlogs).contains("payee-code="+FULLKYC_PC);
    }

    @Owner("Shubham Soni")
    @Feature("PGP-46976 & PGP-46374")
    @Parameters({"isNativePlus"})
    @Test(description = "NATIVE : For UPI Collect ADDNAPAY transaction validate MINMCC is passed as Payee-code in collect-merchant-request , when user having MIN KYC")
    public void validateupiCollectToAddPayMinKyc(@Optional("false") Boolean isNativePlus) throws Exception {
        CheckoutPage checkoutPage = new CheckoutPage();
        String txmAmount =  "2.00";
        User user = userManager.getForRead(Label.MINKYC);
        WalletHelpers.modifyBalance(user, Double.valueOf(txmAmount) - 1.00);
        Constants.MerchantType merchantType= Constants.MerchantType.ADDNPAY_MCC_ADDMONEY;
        PaymentDTO paymentDTO = new PaymentDTO();

        InitTxnDTO initTxnDTO1 = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txmAmount).
                        build();
        String txnToken1 = NativeHelpers.Validate_InitTxn(initTxnDTO1);

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO1.orderFromBody(), txnToken1,paymentDTO, PayMethodType.UPI)
                .setPaymentFlow("ADDANDPAY")
                .setPaymentMode("UPI")
                .setAUTH_MODE("USRPWD")
                .setPayerAccount("test@paytm")
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);
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
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.00);
        String WalletLimitLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"wallet-web/walletLimits");
        Assertions.assertThat(WalletLimitLogs).contains("\"isWalletCategoryRequired\":true");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        String passThroughExtendInfologs = logs.substring(logs.indexOf("passThroughExtendInfo")+24,logs.indexOf("\",\"virtualPaymentAddr\""));
        String decrypted=PGPHelpers.Base64Decode(passThroughExtendInfologs);
        Assertions.assertThat(decrypted).contains("\"walletRbiType\":\"MIN KYC\"");

        String instalogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"pg.router.paytm.upi.payment.request");
        String extendedInfo = instalogs.substring(instalogs.indexOf("\"extendInfo\"")+14,instalogs.indexOf("\", \"settleType\""));
        String decryptedInstaEI=PGPHelpers.Base64Decode(extendedInfo);
        String passThroughExtendInfoInstalogs = decryptedInstaEI.substring(decryptedInstaEI.indexOf("\"passThroughExtendInfo\"")+24,decryptedInstaEI.indexOf("\",\"directPassThroughInfo"));
        String decryptedInsta=PGPHelpers.Base64Decode(passThroughExtendInfoInstalogs);
        Assertions.assertThat(decryptedInsta).contains("\"walletRbiType\":\"MIN KYC\"");
        String instaUPIrequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,orderDTO.getORDER_ID(),"/upi/collect-merchant-request");
        Assertions.assertThat(instaUPIrequestlogs).contains("payee-code="+MINKYC_PC);
    }

}
