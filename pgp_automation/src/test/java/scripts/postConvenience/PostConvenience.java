package scripts.postConvenience;

import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.MerchantManager;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.responsePage.ResponsePage;
import com.paytm.utils.merchant.merchant.DefaultCommission;
import com.paytm.utils.merchant.merchant.ExistingMerchantContract;
import com.paytm.utils.merchant.merchant.Merchant;
import com.paytm.utils.merchant.merchant.PayModeSpecificCommission;
import com.paytm.utils.merchant.merchant.util.Preference;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.openqa.selenium.By;
import org.testng.SkipException;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.pages.responsePage.ResponsePage.Attribute.*;

/**
 * Created by deepakkumar on 5/1/18.
 */
@Owner("Tarun")
public class PostConvenience extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final ResponsePage responsePage = new ResponsePage();

    @Parameters("theme")
    @Test(description = "Verify post convenience fee for all payment modes when Default commission(same config) is set")
    public void PGP_486(@Optional("merchant4") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double flatCommission = 1.12;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(Merchant.ConvFeeType.POST_CONVENIENCE),
                    DefaultCommission.SimpleFlat(flatCommission)
            );
        }
        Test:
        {
            User user = userManager.getForWrite(Label.BASIC);
            String mobNo = user.mobNo();
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key).build();
            WalletHelpers.modifyBalance(user, Double.valueOf(order.getTXN_AMOUNT()) - 1);
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            SoftAssertions softAssert = new SoftAssertions();
            cashierPage.tabCreditCard().click();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, flatCommission, "CC");
            cashierPage.tabDebitCard().click();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, flatCommission, "DC");
            cashierPage.tabNetBanking().click();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, flatCommission, "NB");
            cashierPage.tabUPI().click();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, flatCommission, "UPI");
            softAssert.assertAll();
        }
    }

    @Parameters("theme")
    @Test(description = "Verify post convenience fee when different commission is set for different payment modes")
    public void PGP_487(@Optional("merchant3") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        final double ccPercentCommission = 1.54;
        final double dcPercentCommission = 2.54;
        final double nbPercentCommission = 3.54;
        final double upiPercentCommission = 4.54;

        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(Merchant.ConvFeeType.POST_CONVENIENCE),
                    PayModeSpecificCommission.SimpleCC(ccPercentCommission, null),
                    PayModeSpecificCommission.SimpleDC(dcPercentCommission, null),
                    PayModeSpecificCommission.SimpleNB(nbPercentCommission, null),
                    PayModeSpecificCommission.SimpleUPI(upiPercentCommission, null)
            );
        }
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key).build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            SoftAssertions softAssert = new SoftAssertions();
            cashierPage.tabCreditCard().click();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), ccPercentCommission, 0, "CC");
            cashierPage.tabDebitCard().click();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), dcPercentCommission, 0, "DC");
            cashierPage.tabNetBanking().click();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), nbPercentCommission, 0, "NB");
            cashierPage.tabUPI().click();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), upiPercentCommission, 0, "UPI");
            softAssert.assertAll();
        }
    }

    @Parameters("theme")
    @Test(description = "Verify post convenience fee when simple flat commission is configured")
    public void PGP_488(@Optional("merchant3") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double flatCommission = 0.21;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);

            contract.apply(
                    Merchant.Default(Merchant.ConvFeeType.POST_CONVENIENCE),
                    DefaultCommission.SimpleFlat(flatCommission)
            );
        }
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key).build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabCreditCard().click();
            SoftAssertions softAssert = new SoftAssertions();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, flatCommission, "CC");
            softAssert.assertAll();
        }
    }

    @Parameters("theme")
    @Test(description = "Verify post convenience fee when simple percent is configured")
    public void PGP_489(@Optional("merchant3") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double percentCommission = 1.04;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(Merchant.ConvFeeType.POST_CONVENIENCE),
                    DefaultCommission.SimplePercent(percentCommission)
            );
        }
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key)
                    .setTXN_AMOUNT("19")
                    .build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabCreditCard().click();
            SoftAssertions softAssert = new SoftAssertions();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), percentCommission, 0, "CC");
            softAssert.assertAll();
        }
    }

    @Parameters("theme")
    @Test(description = "Verify post convenience fee when both simple percent and simple flat commission is configured")
    public void PGP_490(@Optional("merchant3") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double percentCommission = 1.13;
        double flatCommission = 1;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(Merchant.ConvFeeType.POST_CONVENIENCE),
                    DefaultCommission.SimplePercentFlatBoth(percentCommission, flatCommission)
            );
        }
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key)
                    .setTXN_AMOUNT("19")
                    .build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabCreditCard().click();
            SoftAssertions softAssert = new SoftAssertions();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), percentCommission, flatCommission, "CC");
            softAssert.assertAll();
        }
    }

    @Parameters("theme")
    @Test(description = "Verify post convenience fee when slab based commission is configured")
    public void PGP_491(@Optional("merchant3") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double percentCommission = 1.15;
        double flatCommission = 0.23;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(Merchant.ConvFeeType.POST_CONVENIENCE),
                    DefaultCommission.SlabCommission(
                            DefaultCommission.Slab.Percent(percentCommission, 0, 10),
                            DefaultCommission.Slab.Flat(flatCommission, 10, 18),
                            DefaultCommission.Slab.PercentFlatBoth(percentCommission, flatCommission, 18, -1)
                    )
            );
        }
        SoftAssertions softAssert = new SoftAssertions();
        TestForSlab1:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key)
                    .setTXN_AMOUNT("5")
                    .build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabCreditCard().click();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), percentCommission, 0, "CC");
        }
        TestForSlab2:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setTXN_AMOUNT("14")
                    .setMerchantKey(key)
                    .build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabCreditCard().click();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, flatCommission, "CC");
        }
        TestForSlab3:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setTXN_AMOUNT("25")
                    .setMerchantKey(key)
                    .build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabCreditCard().click();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), percentCommission, flatCommission, "CC");
        }
        softAssert.assertAll();
    }

    @Parameters("theme")
    @Test(description = "Validate commission when txn amount lie at the lower boundary of first slab.")
    public void PGP_492(@Optional("merchant3") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double slab1FlatCommission = 0.21;
        double slab2FlatCommission = 1.25;
        double slab3FlatCommission = 1.64;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(Merchant.ConvFeeType.POST_CONVENIENCE),
                    DefaultCommission.SlabCommission(
                            DefaultCommission.Slab.Flat(slab1FlatCommission, 0, 100),
                            DefaultCommission.Slab.Flat(slab2FlatCommission, 100, 200),
                            DefaultCommission.Slab.Flat(slab3FlatCommission, 200, -1)
                    )
            );
        }
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key)
                    .setTXN_AMOUNT("1")
                    .build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabCreditCard().click();
            SoftAssertions softAssert = new SoftAssertions();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, slab1FlatCommission, "CC");
            softAssert.assertAll();
        }
    }

    @Parameters("theme")
    @Test(description = "Validate boundary value commission when txn amount lie between first slab range")
    public void PGP_493(@Optional("merchant3") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double slab1FlatCommission = 0.21;
        double slab2FlatCommission = 1.25;
        double slab3FlatCommission = 1.64;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(Merchant.ConvFeeType.POST_CONVENIENCE),
                    DefaultCommission.SlabCommission(
                            DefaultCommission.Slab.Flat(slab1FlatCommission, 0, 100),
                            DefaultCommission.Slab.Flat(slab2FlatCommission, 100, 200),
                            DefaultCommission.Slab.Flat(slab3FlatCommission, 200, -1)
                    )
            );
        }
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key)
                    .setTXN_AMOUNT("2")
                    .build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabCreditCard().click();
            SoftAssertions softAssert = new SoftAssertions();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, slab1FlatCommission, "CC");
            softAssert.assertAll();
        }
    }

    @Parameters("theme")
    @Test(description = "Validate boundary value commission when txn amount lie at the upper boundary of first slab")
    public void PGP_494(@Optional("merchant3") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double slab1FlatCommission = 0.21;
        double slab2FlatCommission = 1.25;
        double slab3FlatCommission = 1.64;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(Merchant.ConvFeeType.POST_CONVENIENCE),
                    DefaultCommission.SlabCommission(
                            DefaultCommission.Slab.Flat(slab1FlatCommission, 0, 100),
                            DefaultCommission.Slab.Flat(slab2FlatCommission, 100, 200),
                            DefaultCommission.Slab.Flat(slab3FlatCommission, 200, -1)
                    )
            );
        }
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key)
                    .setTXN_AMOUNT("100")
                    .build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabCreditCard().click();
            SoftAssertions softAssert = new SoftAssertions();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, slab1FlatCommission, "CC");
            softAssert.assertAll();
        }
    }

    @Parameters("theme")
    @Test(description = "Validate boundary value commission when txn amount lie at the lower boundary of second slab range")
    public void PGP_495(@Optional("merchant3") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double slab1FlatCommission = 0.12;
        double slab2FlatCommission = 0.15;
        double slab3FlatCommission = 0.17;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(Merchant.ConvFeeType.POST_CONVENIENCE),
                    DefaultCommission.SlabCommission(
                            DefaultCommission.Slab.Flat(slab1FlatCommission, 0, 100),
                            DefaultCommission.Slab.Flat(slab2FlatCommission, 100, 200),
                            DefaultCommission.Slab.Flat(slab3FlatCommission, 200, -1)
                    )
            );
        }
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key)
                    .setTXN_AMOUNT("100.01")
                    .build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabCreditCard().click();
            SoftAssertions softAssert = new SoftAssertions();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, slab2FlatCommission, "CC");
            softAssert.assertAll();
        }
    }

    @Parameters("theme")
    @Test(description = "Validate boundary value commission when txn amount lie between second slab range")
    public void PGP_496(@Optional("merchant3") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double slab1FlatCommission = 0.21;
        double slab2FlatCommission = 1.25;
        double slab3FlatCommission = 1.64;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(Merchant.ConvFeeType.POST_CONVENIENCE),
                    DefaultCommission.SlabCommission(
                            DefaultCommission.Slab.Flat(slab1FlatCommission, 0, 100),
                            DefaultCommission.Slab.Flat(slab2FlatCommission, 100, 200),
                            DefaultCommission.Slab.Flat(slab3FlatCommission, 200, -1)
                    )
            );
        }
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key)
                    .setTXN_AMOUNT("120")
                    .build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabCreditCard().click();
            SoftAssertions softAssert = new SoftAssertions();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, slab2FlatCommission, "CC");
            softAssert.assertAll();
        }
    }

    @Parameters("theme")
    @Test(description = "Validate boundary value commission when txn amount lie at upper boundary of second slab range")
    public void PGP_497(@Optional("merchant3") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double slab1FlatCommission = 0.21;
        double slab2FlatCommission = 1.25;
        double slab3FlatCommission = 1.64;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(Merchant.ConvFeeType.POST_CONVENIENCE),
                    DefaultCommission.SlabCommission(
                            DefaultCommission.Slab.Flat(slab1FlatCommission, 0, 100),
                            DefaultCommission.Slab.Flat(slab2FlatCommission, 100, 200),
                            DefaultCommission.Slab.Flat(slab3FlatCommission, 200, -1)
                    )
            );
        }
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key)
                    .setTXN_AMOUNT("200")
                    .build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabCreditCard().click();
            SoftAssertions softAssert = new SoftAssertions();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, slab2FlatCommission, "CC");
            softAssert.assertAll();
        }
    }

    @Parameters("theme")
    @Test(description = "Validate boundary value commission when txn amount lie at lower boundary of third slab range")
    public void PGP_498(@Optional("merchant3") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double slab1FlatCommission = 0.21;
        double slab2FlatCommission = 1.25;
        double slab3FlatCommission = 1.64;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(Merchant.ConvFeeType.POST_CONVENIENCE),
                    DefaultCommission.SlabCommission(
                            DefaultCommission.Slab.Flat(slab1FlatCommission, 0, 100),
                            DefaultCommission.Slab.Flat(slab2FlatCommission, 100, 200),
                            DefaultCommission.Slab.Flat(slab3FlatCommission, 200, -1)
                    )
            );
        }
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key)
                    .setTXN_AMOUNT("200.01")
                    .build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabCreditCard().click();
            SoftAssertions softAssert = new SoftAssertions();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, slab3FlatCommission, "CC");
            softAssert.assertAll();
        }
    }

    @Parameters("theme")
    @Test(description = "Validate boundary value commission when txn amount lie in between third slab range")
    public void PGP_499(@Optional("merchant3") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double slab1FlatCommission = 0.21;
        double slab2FlatCommission = 1.25;
        double slab3FlatCommission = 1.64;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(Merchant.ConvFeeType.POST_CONVENIENCE),
                    DefaultCommission.SlabCommission(
                            DefaultCommission.Slab.Flat(slab1FlatCommission, 0, 100),
                            DefaultCommission.Slab.Flat(slab2FlatCommission, 100, 200),
                            DefaultCommission.Slab.Flat(slab3FlatCommission, 200, -1)
                    )
            );
        }
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key)
                    .setTXN_AMOUNT("230")
                    .build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabCreditCard().click();
            SoftAssertions softAssert = new SoftAssertions();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, slab3FlatCommission, "CC");
            softAssert.assertAll();
        }
    }

 //   @Parameters("theme")
//    @Test(description = "Validate post convenience fee and perform end to end Hybrid Txn", enabled = false)
    public void PGP_500(@Optional("merchant4") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double ccFlatCommission = 1.15;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Hybrid(Merchant.ConvFeeType.POST_CONVENIENCE),
                    DefaultCommission.SimpleFlat(ccFlatCommission)
            );
        }
        Test:
        {
            User user = userManager.getForWrite(Label.BASIC);
            String mobNo = user.mobNo();
            OrderDTO order = new OrderFactory.Hybrid(mid, theme, user)
                    .setMerchantKey(key).build();
            WalletHelpers.modifyBalance(user, Double.valueOf(order.getTXN_AMOUNT()) - 1);
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabCreditCard().click();
            SoftAssertions softAssert = new SoftAssertions();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, ccFlatCommission, "CC");
            softAssert.assertAll();
            cashierPage.payBy(Constants.PayMode.CC);
            TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(order.getORDER_ID())
                    .validateTxnAmount(order.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateMid(order.getMID())
                    .validatePaymentMode("HYBRID")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .validateChildTxnsPresent()
                    .AssertAll();
        }
    }

    @Parameters("theme")
    @Test(description = "Validate post convenience fee and perform end to end WalletOnly Txn")
    public void PGP_501(@Optional("merchant4") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double ccFlatCommission = 1.15;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Hybrid(Merchant.ConvFeeType.POST_CONVENIENCE),
                    DefaultCommission.SimpleFlat(ccFlatCommission)
            );
            if (!new com.paytm.utils.merchant.merchant.util.Merchant(mid, true).getPreferences().add(new Preference("pcf-fee-info"))) throw new SkipException("not able to enable merchant's pcf-fee-info preference");
        }
        Test:
        {
            User user = userManager.getForWrite(Label.BASIC);
            OrderDTO order = new OrderFactory.Hybrid(mid, theme, user)
                    .setMerchantKey(key).build();
            WalletHelpers.modifyBalance(user, Double.valueOf(order.getTXN_AMOUNT()) + 10);
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            SoftAssertions softAssert = new SoftAssertions();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, ccFlatCommission, "PPI");
            softAssert.assertAll();
            cashierPage.payBy(Constants.PayMode.WALLET);
            assertion.apply(pageWait.apply(responsePage.hasLoaded()));
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(STATUS).equals("TXN_SUCCESS"),
                    responsePage.get(CHARGEAMOUNT).equals(convenienceFeeCalculator(Double.valueOf(order.getTXN_AMOUNT()), 0, ccFlatCommission, "PPI"))
            );
            sAssert.eval();
            TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(order.getORDER_ID())
                    .validateTxnAmount(order.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateMid(order.getMID())
                    .validatePaymentMode("PPI")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
        }
    }


    @Parameters("theme")
    @Test(description = "Validate post convenience fee and perform end to end PgOnly CC Txn")
    public void PGP_502(@Optional("merchant4") String theme) {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double ccFlatCommission = 1.15;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(Merchant.ConvFeeType.POST_CONVENIENCE),
                    DefaultCommission.SimpleFlat(ccFlatCommission)
            );
            if (!new com.paytm.utils.merchant.merchant.util.Merchant(mid, true).getPreferences().add(new Preference("pcf-fee-info"))) throw new SkipException("not able to enable merchant's pcf-fee-info preference");
        }
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key).build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabCreditCard().click();
            SoftAssertions softAssert = new SoftAssertions();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, ccFlatCommission, "CC");
            softAssert.assertAll();
            cashierPage.payBy(Constants.PayMode.CC);
            assertion.apply(pageWait.apply(responsePage.hasLoaded()));
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(STATUS).equals("TXN_SUCCESS"),
                    responsePage.get(CHARGEAMOUNT).equals(convenienceFeeCalculator(Double.valueOf(order.getTXN_AMOUNT()), 0, ccFlatCommission, "CC"))
            );
            sAssert.eval();
            TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(order.getORDER_ID())
                    .validateTxnAmount(order.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName(Constants.Gateway.HDFC.toString())
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateBankName(Constants.Bank.HDFC.toString())
                    .validateMid(order.getMID())
                    .validatePaymentMode("CC")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
        }
    }

    @Parameters("theme")
    @Test(description = "test Charge Amount Property Not Displayed In Merchant Callback When Not Enabled On Merchant")
    public void testChargeAmountPropertyNotDisplayedInMerchantCallbackWhenNotEnabledOnMerchant(@Optional("merchant4") String theme) {
        Constants.MerchantType merchant = Constants.MerchantType.PCF_WITH_PCF_FEE_INFO_PREF_DISABLED;
        String mid = merchant.getId();
        String key = merchant.getKey();
        double ccFlatCommission = 1.15;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(Merchant.ConvFeeType.POST_CONVENIENCE),
                    DefaultCommission.SimpleFlat(ccFlatCommission)
            );
            if (!new com.paytm.utils.merchant.merchant.util.Merchant(mid, true).getPreferences().remove(new Preference("pcf-fee-info"))) throw new SkipException("not able to disable merchant's pcf-fee-info preference");
        }

        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key).build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.CC);
            assertion.apply(pageWait.apply(responsePage.hasLoaded()));
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(STATUS).equals("TXN_SUCCESS"),
                    responsePage.keys().contains(CHARGEAMOUNT).not()
            );
            sAssert.eval();
        }
    }

    @Parameters("theme")
    @Test(description = "Validate response of failed PgOnly Txn")
    public void testWhenTxnFails(@Optional("merchant4") String theme) {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double ccFlatCommission = 1.15;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(Merchant.ConvFeeType.POST_CONVENIENCE),
                    DefaultCommission.SimpleFlat(ccFlatCommission)
            );
            if (!new com.paytm.utils.merchant.merchant.util.Merchant(mid, true).getPreferences().add(new Preference("pcf-fee-info"))) throw new SkipException("not able to enable merchant's pcf-fee-info preference");
        }
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key).build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber("4718650100030136"));
            cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber("4718650100030136"));
            assertion.apply(pageWait.apply(responsePage.hasLoaded()));
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(STATUS).equals("TXN_FAILURE"),
                    responsePage.get(CHARGEAMOUNT).equals(convenienceFeeCalculator(Double.valueOf(order.getTXN_AMOUNT()), 0, ccFlatCommission, "CC"))
            );
            sAssert.eval();
            TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(order.getORDER_ID())
                    .validateTxnAmount(order.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName(Constants.Gateway.HDFC.toString())
                    .validateRespCode(Constants.ResponseCode.BANK_TXN_FAILURE.getRespCode())
                    .validateRespMsg(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                    .validateBankName(Constants.Bank.HDFC.toString())
                    .validateMid(order.getMID())
                    .validatePaymentMode("CC")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
        }
    }

    @Parameters("theme")
    @Test(description = "Validate post convenience fee and perform end to end NB Txn")
    public void testNBTxn(@Optional("merchant4") String theme) {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double percentCommission = 1.12;
        double flatCommission = 1.15;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(Merchant.ConvFeeType.POST_CONVENIENCE),
                    PayModeSpecificCommission.SimpleNB(percentCommission, flatCommission)
            );
            if (!new com.paytm.utils.merchant.merchant.util.Merchant(mid, true).getPreferences().add(new Preference("pcf-fee-info"))) throw new SkipException("not able to enable merchant's pcf-fee-info preference");
        }
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key).build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
            cashierPage.payBy(Constants.PayMode.NB, paymentDTO);
            assertion.apply(pageWait.apply(responsePage.hasLoaded()));
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(STATUS).equals("TXN_SUCCESS"),
                    responsePage.get(CHARGEAMOUNT).equals(convenienceFeeCalculator(Double.valueOf(order.getTXN_AMOUNT()), percentCommission, flatCommission, "NB"))
            );
            sAssert.eval();
            TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(order.getORDER_ID())
                    .validateTxnAmount(order.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName(Constants.Gateway.ICICI.toString())
                    .validateRespCode("01")
                    .validateRespMsg("Txn Success")
                    .validateBankName(Constants.Bank.ICICI.toString())
                    .validateMid(order.getMID())
                    .validatePaymentMode("NB")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
        }
    }

    @Parameters("theme")
    @Test(description = "Validate post convenience fee and perform end to end DC Txn")
    public void testDCTxn(@Optional("merchant4") String theme) {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double percentCommission = 1.12;
        double flatCommission = 1.15;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(Merchant.ConvFeeType.POST_CONVENIENCE),
                    DefaultCommission.SlabCommission(
                            DefaultCommission.Slab.Percent(percentCommission, 0, 10),
                            DefaultCommission.Slab.Flat(flatCommission, 10, 18),
                            DefaultCommission.Slab.PercentFlatBoth(percentCommission, flatCommission, 18, -1)
                    )
            );
            if (!new com.paytm.utils.merchant.merchant.util.Merchant(mid, true).getPreferences().add(new Preference("pcf-fee-info"))) throw new SkipException("not able to enable merchant's pcf-fee-info preference");
        }
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key).build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.DC);
            assertion.apply(pageWait.apply(responsePage.hasLoaded()));
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(STATUS).equals("TXN_SUCCESS"),
                    responsePage.get(CHARGEAMOUNT).equals(convenienceFeeCalculator(Double.valueOf(order.getTXN_AMOUNT()), percentCommission, flatCommission, "DC"))
            );
            sAssert.eval();
            TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(order.getORDER_ID())
                    .validateTxnAmount(order.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName(Constants.Gateway.HDFC.toString())
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateBankName(Constants.Bank.HDFC.toString())
                    .validateMid(order.getMID())
                    .validatePaymentMode("DC")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
        }
    }


    @Parameters("theme")
    @Test(description = "Validate post convenience fee and perform end to end UPI Txn")
    public void testUPITxn(@Optional("merchant4") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double percentCommission = 1.12;
        double flatCommission = 1.15;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(Merchant.ConvFeeType.POST_CONVENIENCE),
                    PayModeSpecificCommission.SimpleUPI(percentCommission, flatCommission)
            );
            if (!new com.paytm.utils.merchant.merchant.util.Merchant(mid, true).getPreferences().add(new Preference("pcf-fee-info"))) throw new SkipException("not able to enable merchant's pcf-fee-info preference");
        }
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key).build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.UPI);
            assertion.apply(pageWait.apply(responsePage.hasLoaded()));
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(STATUS).equals("TXN_SUCCESS"),
                    responsePage.get(CHARGEAMOUNT).equals(convenienceFeeCalculator(Double.valueOf(order.getTXN_AMOUNT()), percentCommission, flatCommission, "UPI"))
            );
            sAssert.eval();
            TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(order.getORDER_ID())
                    .validateTxnAmount(order.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName(Constants.Gateway.ICICI.toString())
                    .validateRespCode("01")
                    .validateRespMsg("Txn Success")
                    .validateMid(order.getMID())
                    .validatePaymentMode("UPI")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
        }
    }

    @Parameters("theme")
    @Test(description = "Validate post convenience fee and perform end to end Add N Pay Txn")
    public void PGP_503(@Optional("merchant4") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double ccFlatCommission = 1.15;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.AddnPay(Merchant.ConvFeeType.POST_CONVENIENCE),
                    DefaultCommission.SimpleFlat(ccFlatCommission)
            );
            if (!new com.paytm.utils.merchant.merchant.util.Merchant(mid, true).getPreferences().add(new Preference("pcf-fee-info"))) throw new SkipException("not able to enable merchant's pcf-fee-info preference");
        }
        Test:
        {
            User user = userManager.getForWrite(Label.BASIC);
            OrderDTO order = new OrderFactory.AddnPay(mid, theme)
                    .setMerchantKey(key)
                    .build();
            WalletHelpers.modifyBalance(user, Double.valueOf(order.getTXN_AMOUNT()) - 1);
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.login(user);
            cashierPage.tabCreditCard().click();
            SoftAssertions softAssert = new SoftAssertions();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, ccFlatCommission, "CC");
            softAssert.assertAll();
            cashierPage.payBy(Constants.PayMode.CC);
            assertion.apply(pageWait.apply(responsePage.hasLoaded()));
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(STATUS).equals("TXN_SUCCESS"),
                    responsePage.get(CHARGEAMOUNT).equals(convenienceFeeCalculator(Double.valueOf(order.getTXN_AMOUNT()), 0, ccFlatCommission, "CC"))
            );
            sAssert.eval();
            TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(order.getORDER_ID())
                    .validateTxnAmount(order.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName("WALLET")
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateBankName("WALLET")
                    .validateMid(order.getMID())
                    .validatePaymentMode("PPI")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
        }
    }

//    @Parameters("theme")
//    @Test(description = "Validate post convenience fee is calculated with wallet preference for a Hybrid merchant", enabled = false)
    public void PGP_504(@Optional("merchant4") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double ppiFlatCommission = 1.5;
        double ccFlatCommission = 2.5;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Hybrid(Merchant.ConvFeeType.POST_CONVENIENCE),
                    PayModeSpecificCommission.SimpleCC(null, ccFlatCommission),
                    PayModeSpecificCommission.SimplePPI(null, ppiFlatCommission)
            );
        }
        Test:
        {
            User user = userManager.getForWrite(Label.BASIC);
            String mobNo = user.mobNo();
            OrderDTO order = new OrderFactory.Hybrid(mid, theme, user)
                    .setMerchantKey(key).build();
            WalletHelpers.modifyBalance(user, Double.valueOf(order.getTXN_AMOUNT()) - 1);
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabCreditCard().click();
            SoftAssertions softAssert = new SoftAssertions();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, ppiFlatCommission, "CC");
            softAssert.assertAll();
        }
    }

    @Parameters("theme")
    @Test(description = "Validate post convenience fee is calculated with wallet preference for an Add N Pay merchant")
    public void PGP_505(@Optional("merchant4") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double ppiFlatCommission = 1.5;
        double ccFlatCommission = 2.2;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.AddnPay(Merchant.ConvFeeType.POST_CONVENIENCE),
                    PayModeSpecificCommission.SimpleCC(null, ccFlatCommission),
                    PayModeSpecificCommission.SimplePPI(null, ppiFlatCommission)
            );

        }
        Test:
        {
            User user = userManager.getForWrite(Label.BASIC);
            String mobNo = user.mobNo();
            OrderDTO order = new OrderFactory.AddnPay(mid, theme)
                    .setMerchantKey(key).setSSO_TOKEN(user.ssoToken()).build();
            WalletHelpers.modifyBalance(user, Double.valueOf(order.getTXN_AMOUNT()) - 1);
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            //cashierPage.login(mobNo);
            cashierPage.tabCreditCard().click();
            try {
            	  System.out.println("----------------------------"+DriverManager.getDriver().findElement(By.xpath("//*[@id='baseAmt']")).getText());

            }catch (Exception e) {
				e.printStackTrace();
			}

            SoftAssertions softAssert = new SoftAssertions();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, ppiFlatCommission, "CC");
            softAssert.assertAll();
        }
    }

//    @Parameters("theme")
//    @Test(description = "Verify successful Hybrid txn where txn amt is equal to wallet balance and evaluated conv. fee is < 1", enabled = false)
    public void PGP_506(@Optional("merchant4") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double flatCommission = 0.12;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Hybrid(Merchant.ConvFeeType.POST_CONVENIENCE),
                    DefaultCommission.SimpleFlat(flatCommission)
            );
        }
        Test:
        {
            User user = userManager.getForWrite(Label.BASIC);
            String mobNo = user.mobNo();
            OrderDTO order = new OrderFactory.Hybrid(mid, theme, user)
                    .setMerchantKey(key).build();
            WalletHelpers.modifyBalance(user, Double.valueOf(order.getTXN_AMOUNT()));
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabCreditCard().click();
            SoftAssertions softAssert = new SoftAssertions();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, flatCommission, "CC");
            softAssert.assertAll();
            cashierPage.payBy(Constants.PayMode.CC);
            TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(order.getORDER_ID())
                    .validateTxnAmount(order.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName(Constants.Gateway.HDFC.toString())
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateBankName(Constants.Bank.HDFC.toString())
                    .validateMid(order.getMID())
                    .validatePaymentMode("CC")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
        }
    }

    @Parameters("theme")
    @Test(description = "Verify successful AddnPay txn where txn amt is equal to wallet balance and evaluated conv fee is < 1")
    public void PGP_507(@Optional("merchant3") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double flatCommission = 0.12;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.AddnPay(Merchant.ConvFeeType.POST_CONVENIENCE),
                    DefaultCommission.SimpleFlat(flatCommission)
            );
            if (!new com.paytm.utils.merchant.merchant.util.Merchant(mid, true).getPreferences().add(new Preference("pcf-fee-info"))) throw new SkipException("not able to enable merchant's pcf-fee-info preference");
        }
        Test:
        {
            User user = userManager.getForWrite(Label.BASIC);
            String mobNo = user.mobNo();
            OrderDTO order = new OrderFactory.AddnPay(mid, theme)
                    .setMerchantKey(key).build();
            WalletHelpers.modifyBalance(user, Double.valueOf(order.getTXN_AMOUNT()));
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabCreditCard().click();
            SoftAssertions softAssert = new SoftAssertions();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, flatCommission, "CC");
            softAssert.assertAll();
            cashierPage.payBy(Constants.PayMode.CC);
            assertion.apply(pageWait.apply(responsePage.hasLoaded()));
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(STATUS).equals("TXN_SUCCESS"),
                    responsePage.get(CHARGEAMOUNT).equals(convenienceFeeCalculator(Double.valueOf(order.getTXN_AMOUNT()), 0, flatCommission, "CC"))
            );
            sAssert.eval();
            TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(order.getORDER_ID())
                    .validateTxnAmount(order.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName("WALLET")
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateBankName("WALLET")
                    .validateMid(order.getMID())
                    .validatePaymentMode("PPI")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
        }
    }

//    @Parameters("theme")
//    @Test(description = "Verify wallet only txn when commission fee is breaching wallet limit", enabled = false)
    public void PGP_508(@Optional("merchant3") String theme) throws Exception {//TODO invalid TC
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        String walletLimitTxnAmt = "set apt value";
        double ccFlatCommission = 1.15;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Hybrid(Merchant.ConvFeeType.POST_CONVENIENCE),
                    DefaultCommission.SimpleFlat(ccFlatCommission)
            );
        }
        Test:
        {
            User user = userManager.getForWrite(Label.BASIC);
            String mobNo = user.mobNo();
            OrderDTO order = new OrderFactory.Hybrid(mid, theme, user)
                    .setMerchantKey(key)
                    .setTXN_AMOUNT(walletLimitTxnAmt)
                    .build();
            WalletHelpers.modifyBalance(user, Double.valueOf(order.getTXN_AMOUNT()) + 1);
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            SoftAssertions softAssert = new SoftAssertions();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, ccFlatCommission, "CC");
            softAssert.assertAll();
            cashierPage.payBy(Constants.PayMode.WALLET);
            TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(order.getORDER_ID())
                    .validateTxnAmount(order.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateMid(order.getMID())
                    .validatePaymentMode("HYBRID")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .validateChildTxnsPresent()
                    .AssertAll();

        }
    }

//    @Parameters("theme")
//    @Test(description = "Verify Add N Pay txn when commission fee is breaching wallet limit", enabled = false)
    public void PGP_509(@Optional("merchant3") String theme) throws Exception {//TODO invalid TC
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        String walletLimitTxnAmt = "set apt value";
        double ccFlatCommission = 1.15;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.AddnPay(Merchant.ConvFeeType.POST_CONVENIENCE),
                    DefaultCommission.SimpleFlat(ccFlatCommission)
            );
        }
        Test:
        {
            User user = userManager.getForWrite(Label.BASIC);
            String mobNo = user.mobNo();
            OrderDTO order = new OrderFactory.AddnPay(mid, theme)
                    .setMerchantKey(key)
                    .setSSO_TOKEN(user.ssoToken())
                    .setTXN_AMOUNT(walletLimitTxnAmt)
                    .build();
            WalletHelpers.modifyBalance(user, Double.valueOf(order.getTXN_AMOUNT()) - 1);
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabCreditCard().click();
            SoftAssertions softAssert = new SoftAssertions();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, ccFlatCommission, "CC");
            softAssert.assertAll();
            cashierPage.payBy(Constants.PayMode.CC);
            TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(order.getORDER_ID())
                    .validateTxnAmount(order.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName("WALLET")
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateBankName("WALLET")
                    .validateMid(order.getMID())
                    .validatePaymentMode("PPI")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
        }
    }

    @Parameters("theme")
    @Test(description = "Verify commission fee when CC number is enterd with DC pay tab")
    public void PGP_510(@Optional("merchant3") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double ccFlatCommission = 0.12;
        double dcFlatCommission = 0.16;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(Merchant.ConvFeeType.POST_CONVENIENCE),
                    PayModeSpecificCommission.SimpleCC(null, ccFlatCommission),
                    PayModeSpecificCommission.SimpleDC(null, dcFlatCommission)
            );
        }
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key).build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabDebitCard().click();
            PaymentDTO paymentDTO = new PaymentDTO();
            cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());
            Thread.sleep(1000);
            SoftAssertions softAssert = new SoftAssertions();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, dcFlatCommission, "DC");
            softAssert.assertAll();
        }
    }

    @Parameters("theme")
    @Test(description = "Verify commission fee when DC number is enterd with CC pay tab")
    public void PGP_511(@Optional("merchant3") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double ccFlatCommission = 0.12;
        double dcFlatCommission = 0.16;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(Merchant.ConvFeeType.POST_CONVENIENCE),
                    PayModeSpecificCommission.SimpleCC(null, ccFlatCommission),
                    PayModeSpecificCommission.SimpleDC(null, dcFlatCommission)
            );
        }
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key).build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabCreditCard().click();
            PaymentDTO paymentDTO = new PaymentDTO();
            cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getDebitCardNumber());
            Thread.sleep(1000);
            SoftAssertions softAssert = new SoftAssertions();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, ccFlatCommission, "CC");
            softAssert.assertAll();
        }
    }

    @Parameters("theme")
    @Test(description = "Verify no conv. fee is applied when evaluated conv. fee is less than 0.01")
    public void PGP_512(@Optional("merchant3") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double percentCommission = 0.30;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(Merchant.ConvFeeType.POST_CONVENIENCE),
                    DefaultCommission.SimplePercent(percentCommission)
            );
        }
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key).build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabCreditCard().click();
            String expectedTotalTxnAmt = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(order.getTXN_AMOUNT());
            String actualTotalTxnAmt = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.totalAmtPG().getText());
            Assertions.assertThat(actualTotalTxnAmt).isEqualTo(expectedTotalTxnAmt);
        }
    }

    @Parameters("theme")
    @Test(description = "Verify commission is allowed to be updated")
    public void PGP_513(@Optional("merchant3") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double flatCommissionBefore = 0.12;
        double flatCommissionAfter = 0.15;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(Merchant.ConvFeeType.POST_CONVENIENCE),
                    DefaultCommission.SimpleFlat(flatCommissionBefore),
                    DefaultCommission.SimpleFlat(flatCommissionAfter)
            );
        }
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key).build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabCreditCard().click();
            SoftAssertions softAssert = new SoftAssertions();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, flatCommissionAfter, "CC");
            softAssert.assertAll();
        }
    }

    @Parameters("theme")
    @Test(description = "Verify post convenience fee shown for all pay modes when different commission is configured on different pay modes is as expected in case of txn retry")
    public void PGP_519(@Optional("merchant4") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        final double ccPercentCommission = 1.54;
        final double dcPercentCommission = 2.54;
        final double nbPercentCommission = 3.54;
        final double upiPercentCommission = 4.54;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(1, Merchant.ConvFeeType.POST_CONVENIENCE),
                    PayModeSpecificCommission.SimpleCC(ccPercentCommission, null),
                    PayModeSpecificCommission.SimpleDC(dcPercentCommission, null),
                    PayModeSpecificCommission.SimpleNB(nbPercentCommission, null),
                    PayModeSpecificCommission.SimpleUPI(upiPercentCommission, null)
            );
        }
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key).build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber("4718650100030136"));
            SoftAssertions softAssert = new SoftAssertions();
            cashierPage.tabCreditCard().click();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), ccPercentCommission, 0, "CC");
            cashierPage.tabDebitCard().click();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), dcPercentCommission, 0, "DC");
            cashierPage.tabNetBanking().click();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), nbPercentCommission, 0, "NB");
            cashierPage.tabUPI().click();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), upiPercentCommission, 0, "UPI");
            softAssert.assertAll();
        }
    }

    @Parameters("theme")
    @Test(description = "Validate post convenience fee and perform end to end PgOnly Txn in case of txn retry")
    public void PGP_520(@Optional("merchant4") String theme) throws Exception {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        double ccFlatCommission = 1.15;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(1, Merchant.ConvFeeType.POST_CONVENIENCE),
                    DefaultCommission.SimpleFlat(ccFlatCommission)
            );
            if (!new com.paytm.utils.merchant.merchant.util.Merchant(mid, true).getPreferences().add(new Preference("pcf-fee-info"))) throw new SkipException("not able to enable merchant's pcf-fee-info preference");
        }
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key).build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber("4718650100030136"));
            cashierPage.tabCreditCard().click();
            SoftAssertions softAssert = new SoftAssertions();
            validateCommission(softAssert, cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, ccFlatCommission, "CC");
            softAssert.assertAll();
            cashierPage.payBy(Constants.PayMode.CC);
            assertion.apply(pageWait.apply(responsePage.hasLoaded()));
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(STATUS).equals("TXN_SUCCESS"),
                    responsePage.get(CHARGEAMOUNT).equals(convenienceFeeCalculator(Double.valueOf(order.getTXN_AMOUNT()), 0, ccFlatCommission, "CC"))
            );
            sAssert.eval();
            TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(order.getORDER_ID())
                    .validateTxnAmount(order.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName(Constants.Gateway.HDFC.toString())
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateBankName(Constants.Bank.HDFC.toString())
                    .validateMid(order.getMID())
                    .validatePaymentMode("CC")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
        }
    }

    private void validateCommission(SoftAssertions softAssert, CashierPage cashierPage, double baseAmount, double percentCommission, double flatCommission, String paymentMode) {
        double actualBaseAmt;
        double actualChargeFeeAmt;
        double actualTotalAmt;

        if (paymentMode.equalsIgnoreCase("PPI")) {
            actualBaseAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.baseAmtPPI().getText()));
            actualChargeFeeAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.chargeFeeAmtPPI().getText()));
            actualTotalAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.totalAmtPPI().getText()));
        } else {
            actualBaseAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.baseAmtPG().getAttribute("innerText")));
            actualChargeFeeAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.chargeFeeAmtPG().getAttribute("innerText")));
            actualTotalAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.totalAmtPG().getAttribute("innerText")));
        }

        double expectedBaseAmt = baseAmount;
        double expectedChargeFeeAmt = convenienceFeeCalculator(baseAmount, percentCommission, flatCommission, paymentMode);
        double expectedTotalAmt = CommonHelpers.doubleHalfUpConvertor(expectedBaseAmt + expectedChargeFeeAmt);

        softAssert.assertThat(actualBaseAmt).as(paymentMode).isEqualTo(expectedBaseAmt);
        softAssert.assertThat(actualChargeFeeAmt).as(paymentMode).isEqualTo(expectedChargeFeeAmt);
        softAssert.assertThat(actualTotalAmt).as(paymentMode).isEqualTo(expectedTotalAmt);
    }

}

