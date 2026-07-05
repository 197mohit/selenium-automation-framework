package merchantCreationScript;

import com.paytm.utils.merchant.merchant.*;
import org.testng.annotations.Test;

public class merchantCreation {

    @Test
    public void createMerchants() throws Exception {

        /*NewContract newContract = new NewContract(Merchant.Hybrid(), Docs.Default(), Urls.Default(), DefaultCommission.SimpleFlat(1.00),Velocity.Overall(),Bank.HdfcCC(),Bank.HdfcDC(),Bank.IciciNB(),Bank.IciciUPI());
        newContract.create();*/
//        hybrid(0);
       // addNPay(0);
      //  walletOnly(0);

        new NewContract(
                Merchant.Hybrid(Merchant.ConvFeeType.DEFAULT),
                Docs.Default(),
                Urls.Default(),
                Bank.Wallet(),
                Bank.UPI_PUSH(),
                Bank.PPBL(),
                Bank.PostPaid(),
//                Bank.IciciUPI(),
//                Bank.Icici_CC_DC(),
//                Bank.SBI_CC(),
//                Bank.SBI_NB(),
//                Bank.Icici_Direct_CC_DC(),
//                Bank.HDFC_NB(),
                DefaultCommission.SimpleFlat(1.00),
                Velocity.Overall()

        ).createWithoutDBcheck();
//        hybrid(0);
    }

    @Test
    public void withAllpaymode () {
        new NewContract(
                Merchant.Hybrid(Merchant.ConvFeeType.DEFAULT),
                Docs.Default(),
                Urls.Default(),
                Bank.Wallet(),
                Bank.UPI_PUSH(),
                Bank.PPBL(),
                Bank.PostPaid(),
                Bank.IciciUPI(),
                Bank.IciciNB(),
                Bank.HdfcNB(),
                Bank.HdfcDC(),
                Bank.HdfcCC(),
                Bank.COD(),
                DefaultCommission.SimpleFlat(1.00),
                Velocity.Overall()
                ).create();
    }



    public static void hybrid(int retry) {
        new NewContract(
                Merchant.Hybrid(
                        retry,
                        Merchant.ConvFeeType.DEFAULT,
                        Merchant.SUBSCRIBE,
                        Merchant.RENEW_SUBSCRIPTION,
                        Merchant.SEAMLESS,
                        Merchant.SEAMLESS_NATIVE,
                        Merchant.PAYTM_EXPRESS
                ),
                Docs.Default(),
                Urls.Default(),
                Bank.Wallet(),
                Bank.HdfcCC(),
                Bank.HdfcDC(),
                Bank.IciciNB(),
                Bank.IciciUPI(),
                Bank.COD(),
                DefaultCommission.SimplePercent(1),
                Velocity.Overall()
        ).createWithoutDBcheck();
    }

    public static void addNPay(int retry) {
        new NewContract(
                Merchant.AddnPay(
                        retry,
                        Merchant.ConvFeeType.DEFAULT,
                        Merchant.SUBSCRIBE,
                        Merchant.RENEW_SUBSCRIPTION,
                        Merchant.PAYTM_EXPRESS,
                        Merchant.ADD_MONEY
                ),
                Docs.Default(),
                Urls.Default(),
                Bank.Wallet(),
                Bank.HdfcCC(),
                Bank.HdfcDC(),
                Bank.IciciNB(),
                Bank.IciciUPI(),
                Bank.COD(),
                DefaultCommission.SimplePercent(1),
                Velocity.Overall()
        ).create();
    }

    public static void walletOnly(int retry) {
        new NewContract(
                Merchant.WalletOnly(retry, Merchant.ConvFeeType.DEFAULT),
                Docs.Default(),
                Urls.Default(),
                Bank.Wallet(),
                DefaultCommission.SimplePercent(1),
                Velocity.Overall()
        ).create();
    }
}
