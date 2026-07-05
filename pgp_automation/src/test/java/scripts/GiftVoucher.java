package scripts;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.pages.*;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO.*;

@Owner("Gagandeep")
public class GiftVoucher extends PGPBaseTest {

    @Parameters({"theme"})
    @Test(description = "Gift voucher Add_Money")
    public void gvAddMoney(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        String walletType = WalletHelpers.getWalletType(user);
        if (walletType.equalsIgnoreCase("Min Kyc Expired")) {
            WalletHelpers.updateGVBalance(user, 0.0);
            OrderDTO orderDTO = new OrderFactory.AddMoney(Constants.MerchantType.AddnPay, theme, user).build();
            CheckoutPage checkoutPage = new CheckoutPage();
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.CC);
            ResponsePage responsePage= new ResponsePage();
            responsePage.waitUntilLoads();
            TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(orderDTO.getORDER_ID())
                    .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("ADDMONEY")
                    .validateGatewayName(Constants.Gateway.HDFC.toString())
                    .validateRespCode("01")
                    .validateRespMsg("Txn Success")
                    .validateBankName(Constants.Bank.HDFC.toString())
                    .validateMid(orderDTO.getMID())
                    .validatePaymentMode("CC")
                    .validateRefundAmnt("0.0")
                    .validateTxnDate(new Date())
                    .validateStatusAPIParameters()
                    .AssertAll();

            Assertions.assertThat(WalletHelpers.getGVBalance(user)).as("GV Balance is not getting updated").isEqualTo(2.00);
        } else {
            throw new RuntimeException("User is not Min Kyc Expired");
        }

    }

    @Parameters({"theme"})
    @Test(description = "Gift voucher ADD_PAY")
    public void gvAddNPay(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        String walletType = WalletHelpers.getWalletType(user);
        if (walletType.equalsIgnoreCase("Min Kyc Expired")) {
           WalletHelpers.setZeroBalance(user);
            WalletHelpers.updateGVBalance(user, 2.00);
            OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.AddnPay, theme, user).setTXN_AMOUNT("4").build();
            CheckoutPage checkoutPage = new CheckoutPage();
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            Assertions.assertThat(cashierPage.wallet_dropdown()).as("Gift Balance is not equal to Wallet balance").isTrue();
            cashierPage.payBy(Constants.PayMode.CC);
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
                    .validateRefundAmnt("2.0")
                    .validateTxnDate(new Date())
                    .validateStatusAPIParameters()
                    .AssertAll();
            Assertions.assertThat(WalletHelpers.getGVBalance(user)).as("GV Balance is not used").isEqualTo(0.0);
        } else {
            throw new RuntimeException("User is not Min Kyc Expired");
        }
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Gift voucher ADD_Money Native/Native plus")
    public void gvAddMoneyNative (@Optional("false") boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        String walletType = WalletHelpers.getWalletType(user);
        if (walletType.equalsIgnoreCase("Min Kyc Expired")) {
            WalletHelpers.updateGVBalance(user, 0.0);
            InitTxnDTO initTxnDTO = new Builder(user.ssoToken(), Constants.MerchantType.NATIVE_ADDNPAY).setIsNativeAddMoney("true").setTxnValue("2").build();
            String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
            OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.NATIVE_ADDNPAY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
            CheckoutPage checkoutPage= new CheckoutPage();
            GVConsentPage gvConsentPage=new GVConsentPage();
            ResponsePage responsePage= new ResponsePage();
            checkoutPage.createNativeOrder(orderDTO, isNativePlus);
            gvConsentPage.proceedToBuyGiftVoucher();
            responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(orderDTO.getORDER_ID())
                    .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("ADDMONEY")
                    .validateGatewayName(Constants.Gateway.HDFC.toString())
                    .validateRespCode("01")
                    .validateRespMsg("Txn Success")
                    .validateBankName(Constants.Bank.HDFC.toString())
                    .validateMid(orderDTO.getMID())
                    .validatePaymentMode("CC")
                    .validateRefundAmnt("2.0")
                    .validateTxnDate(new Date())
                    .validateStatusAPIParameters()
                    .AssertAll();
            Assertions.assertThat(WalletHelpers.getGVBalance(user)).as("GV Balance is not getting updated").isEqualTo(2.00);
        } else {
            throw new RuntimeException("User is not Min Kyc Expired");
        }

    }
        @Parameters({"isNativePlus"})
        @Test(description = "Gift voucher ADD_PAY Native/Native plus")
        public void gvAddNPayNative (@Optional("true") boolean isNativePlus) throws Exception {
            User user = userManager.getForWrite(Label.MINKYCEXPIRED);
            String walletType = WalletHelpers.getWalletType(user);
            if (walletType.equalsIgnoreCase("Min Kyc Expired"))
            {
                    WalletHelpers.updateGVBalance(user, 2.00);
                InitTxnDTO initTxnDTO = new Builder(user.ssoToken(), Constants.MerchantType.NATIVE_ADDNPAY).setTxnValue("3.0").build();
                String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
                OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.NATIVE_ADDNPAY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).
                        setPaymentFlow("ADDANDPAY").build();
                CheckoutPage checkoutPage= new CheckoutPage();
                ResponsePage responsePage= new ResponsePage();
                checkoutPage.createNativeOrder(orderDTO, isNativePlus);
                responsePage = new ResponsePage();
                responsePage.waitUntilLoads();
                TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
                txnStatus.executeUntilNotPending();
                txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                        .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                        .validateOrderid(orderDTO.getORDER_ID())
                        .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                        .validateStatus("TXN_SUCCESS")
                        .validateTxnType("SALE")
                        .validateGatewayName("WALLET")
                        .validateRespCode("01")
                        .validateRespMsg("Txn Success")
                        .validateBankName("WALLET")
                        .validateMid(orderDTO.getMID())
                        .validatePaymentMode("PPI")
                        .validateRefundAmnt("0.0")
                        .validateTxnDate(new Date())
                        .validateStatusAPIParameters()
                        .AssertAll();
                Assertions.assertThat(WalletHelpers.getGVBalance(user)).as("GV Balance is not used").isEqualTo(0.0);

            } else {
                throw new RuntimeException("User is not Min Kyc Expired");
            }

        }
    @Parameters({"theme"})
    @Test(description = "Gift voucher Hybrid")
    public void gvHybrid(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        String walletType = WalletHelpers.getWalletType(user);
        if (walletType.equalsIgnoreCase("Min Kyc Expired")) {
            WalletHelpers.setZeroBalance(user);
            WalletHelpers.updateGVBalance(user, 2.00);
            OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.Hybrid, theme, user).setTXN_AMOUNT("3").build();
            CheckoutPage checkoutPage = new CheckoutPage();
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
                Assertions.assertThat(cashierPage.wallet_dropdown()).as("Gift Balance is not equal to Wallet balance").isTrue();
            cashierPage.payBy(Constants.PayMode.CC);
            TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(orderDTO.getORDER_ID())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateRespCode("01")
                    .validateRespMsg("Txn Success")
                    .validateMid(orderDTO.getMID())
                    .validatePaymentMode("HYBRID")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .validateChildTxnsPresent();
            txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                    .validateTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                    .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                    .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(1.00d))
                    .validateGatewayName(TxnStatus.ChildTxnType.BANK, Constants.Gateway.HDFC.toString())
                    .validateBankTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                    .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");
            txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                    .validateTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                    .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                    .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(2.00d))
                    .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                    .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                    .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                    .AssertAll();
            Assertions.assertThat(WalletHelpers.getGVBalance(user)).as("GV Balance is not used").isEqualTo(0.0);
        } else {
            throw new RuntimeException("User is not Min Kyc Expired");
        }
    }
    @Parameters({"isNativePlus"})
    @Test(description = "Gift voucher Hybrid Native/Native plus")
    public void gvHybridNative (@Optional("true") boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        String walletType = WalletHelpers.getWalletType(user);
        if (walletType.equalsIgnoreCase("Min Kyc Expired")) {
            WalletHelpers.updateGVBalance(user, 2.00);

            InitTxnDTO initTxnDTO = new Builder(user.ssoToken(), Constants.MerchantType.NATIVE_HYBRID).setTxnValue("3.0").build();
            String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
            OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.NATIVE_HYBRID, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).
                    setPaymentFlow("HYBRID").build();
            CheckoutPage checkoutPage= new CheckoutPage();
            ResponsePage responsePage= new ResponsePage();
            checkoutPage.createNativeOrder(orderDTO, isNativePlus);
            responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(orderDTO.getORDER_ID())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateRespCode("01")
                    .validateRespMsg("Txn Success")
                    .validateMid(orderDTO.getMID())
                    .validatePaymentMode("HYBRID")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .validateChildTxnsPresent();

            txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                    .validateTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                    .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                    .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(1.00d))
                    .validateGatewayName(TxnStatus.ChildTxnType.BANK, Constants.Gateway.HDFC.toString())
                    .validateBankTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                    .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

            txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                    .validateTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                    .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                    .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(2.00d))
                    .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                    .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                    .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                    .AssertAll();
            Assertions.assertThat(WalletHelpers.getGVBalance(user)).as("GV Balance is not used").isEqualTo(0.0);
        } else {
            throw new RuntimeException("User is not Min Kyc Expired");
        }

    }

    }

