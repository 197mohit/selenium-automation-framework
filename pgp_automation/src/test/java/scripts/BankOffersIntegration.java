package scripts;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.RedisAPI;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.wallet.transitWallet.AddFundsToSubWalletTransit;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.*;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedPaymentOffers;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.reporting.Owners;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.pages.*;
import com.paytm.utils.merchant.Peon;
import io.qameta.allure.Owner;
import io.qameta.allure.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.SkipException;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.base.test.Group.Status;


@Owner(Constants.Owner.ROHIT)
public class BankOffersIntegration extends PGPBaseTest {
    @Feature("PGP-24608")

    @Parameters({"isNativePlus"})
    @Test(description = "verify simplifiedpaymentoffers should come in response of fpo when minimal_promo_merchant pref is set as Y ")
    public void simplifiedPaymentOffersVisibleFPO(@Optional("true") Boolean isNativePlus) throws Exception {

        MerchantType merchantType = MerchantType.MINIMAL_PROMO_MERCHANT_Y;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        fetchPaymentOptionsDTO.getHead().setWorkFlow("checkout");
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        fetchPaymentOption.setContext("body.applyPaymentOffer", "true");
        fetchPaymentOption.setContext("body.fetchAllPaymentOffers", "true");
        Response response = fetchPaymentOption.execute();
        JsonPath fpoResponse = response.jsonPath();
        Assertions.assertThat(fpoResponse.getString("body.paymentOffers")).isNotNull();
        Assertions.assertThat(fpoResponse.getString("body")).contains("simplifiedPaymentOffers");
        Assertions.assertThat(fpoResponse.getString("body.simplifiedPaymentOffers")).contains("promoCode", "applyAvailablePromo", "validatePromo");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "verify simplifiedpaymentoffers should not come in response of fpo when minimal_promo_merchant pref is set as N ")
    public void simplifiedPaymentOffersNotVisibleFPO(@Optional("true") Boolean isNativePlus) throws Exception {
        MerchantType merchantType = MerchantType.MINIMAL_PROMO_MERCHANT_N;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        fetchPaymentOptionsDTO.getHead().setWorkFlow("checkout");
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        fetchPaymentOption.setContext("body.applyPaymentOffer","true");
        fetchPaymentOption.setContext("body.fetchAllPaymentOffers","true");
        Response response = fetchPaymentOption.execute();
        JsonPath fpoResponse = response.jsonPath();
        Assertions.assertThat(fpoResponse.getString("body.paymentOffers")).isNotNull();
        Assertions.assertThat(fpoResponse.getString("body")).doesNotContain("simplifiedPaymentOffers");
    }
}