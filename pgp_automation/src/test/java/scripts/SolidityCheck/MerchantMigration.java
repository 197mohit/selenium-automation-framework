package scripts.SolidityCheck;

import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.utils.merchant.UtilConstants;
import com.paytm.utils.merchant.dto.CreateMerchant;
import com.paytm.utils.merchant.helpers.GetMerchantHelper;
import com.paytm.utils.merchant.merchant.*;
import com.paytm.utils.merchant.util.Acquiring;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

@Owner("Gagandeep")
public class MerchantMigration extends PGPBaseTest {

    CreateMerchant merchantConfig;
    NewContract contract;
    GetMerchantHelper merchantHelper;
    Acquiring acquiring;
    private static final int MERCHANT_CREATION_TIMEOUT_IN_MS = 120000;
    String mid;

    @Test (description = "Verify Creation of merchant and applied acquiring in ALIPAY")
    public void validateMerchantCreation() {
        hybrid(0);
        verifyMidInDB();
        pause(60);
        verifyMidInAlipay();
        verifyMerchantAcquirings();
    }

    private void fetchMerchantFromAlipay(Constants.MerchantType merchantType) {
        String mid = merchantType.getId();
        merchantHelper = new GetMerchantHelper();
        merchantHelper.fetchMetchantDetails(mid, 10, 15);
        acquiring = new Acquiring(merchantHelper, true);
    }

    private void verifyMerchantAcquirings() {
        Acquiring acquiring = new Acquiring(merchantHelper, true);
        acquiring.verifyAcquiring(PayMethodType.CREDIT_CARD, UtilConstants.BankName.HDFC)
                .verifyAcquiring(PayMethodType.DEBIT_CARD, UtilConstants.BankName.HDFC)
                .verifyAcquiring(PayMethodType.NET_BANKING, UtilConstants.BankName.ICICI)
                .verifyAcquiring(PayMethodType.UPI, UtilConstants.BankName.ICICI)
                .verifyAcquiring(PayMethodType.COD, UtilConstants.BankName.CODMOCK)
                .AssertAll();
    }

    private void verifyMidInAlipay() {
        merchantHelper = new GetMerchantHelper();
        merchantHelper.fetchMetchantDetails(mid, 10, 15);
        merchantHelper.verifyMrchntAcquiringStatus();
    }

    private void verifyMidInDB() {
        mid = contract.lookUpMidFromDB(merchantConfig);
        Assertions.assertThat(mid.isEmpty()).as("MID is returned blank when fetching from DB").isFalse();
    }

    private void hybrid(int retry) {
        contract = new NewContract(
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
       //         UtilConstants.Wallet(),
                Bank.HdfcCC(),
                Bank.HdfcDC(),
                Bank.IciciNB(),
                Bank.IciciUPI(),
                Bank.COD(),
                DefaultCommission.SimplePercent(1),
                Velocity.Overall()
        );
        contract.createWithoutDBcheck();
        merchantConfig = contract.getMerchantConfig();
    }

    public void pause(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
