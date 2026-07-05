package scripts.api.theia.fetchBinDetail;

import com.paytm.api.CreateSubscription;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.fetchBinDetails.response.FetchBinDetailResponse;
import com.paytm.framework.reportportal.annotation.Owner;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

public class FetchBinDetailSubsTest extends PGPBaseTest {

    private static final String CC_BIN = "559726";
    private static final String INELIGIBLE_CC_BIN = "549683";


    @Owner(Constants.Owner.HIMANSHU)
    @Feature("PG-1626")
    @Test(description = "Verify fetchBinDetail response with txnToken from create subscription")
    public void validateFetchBinDetailWithCreateSubscriptionToken() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PPI;
        String subscriptionStartDate = CommonHelpers.getDate().toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(subscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.CREDIT_CARD);

        String txnToken = subscription.getTxnToken();
        String orderId = initTxnDTO.orderFromBody();

        FetchBinDetailResponse fetchBinDetailResponse = NativeHelpers.fetchBinDetailResponse(
                txnToken, merchant.getId(), orderId, CC_BIN, "NATIVE_SUBSCRIPTION");

        Assertions.assertThat(fetchBinDetailResponse.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("0000");
        Assertions.assertThat(fetchBinDetailResponse.getBody().getAuthModes())
                .as("authModes should contain otp")
                .isNotNull()
                .contains("otp");
    }

    @Owner(Constants.Owner.HIMANSHU)
    @Feature("PG-1626")
    @Test(description = "Verify fetchBinDetail response with txnToken from normal initiate transaction")
    public void validateFetchBinDetailWithInitTxnToken() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();

        FetchBinDetailResponse fetchBinDetailResponse = NativeHelpers.fetchBinDetailResponse(
                txnToken, merchant.getId(), orderId, CC_BIN);

        Assertions.assertThat(fetchBinDetailResponse.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("0000");
        Assertions.assertThat(fetchBinDetailResponse.getBody().getAuthModes())
                .as("authModes should contain otp")
                .isNotNull()
                .contains("otp");
    }

    @Owner(Constants.Owner.HIMANSHU)
    @Feature("PG-1626")
    @Test(description = "Verify authModes not present in fetchBinDetail response for ineligible bin with subscription token")
    public void validateFetchBinDetailIneligibleBinWithCreateSubscriptionToken() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SIHUB_Subs;
        String subscriptionStartDate = CommonHelpers.getDate().toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(subscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.CREDIT_CARD);

        String txnToken = subscription.getTxnToken();
        String orderId = initTxnDTO.orderFromBody();

        FetchBinDetailResponse fetchBinDetailResponse = NativeHelpers.fetchBinDetailResponse(
                txnToken, merchant.getId(), orderId, INELIGIBLE_CC_BIN, "NATIVE_SUBSCRIPTION");

        Assertions.assertThat(fetchBinDetailResponse.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("0000");
                Assertions.assertThat(fetchBinDetailResponse.getBody().getAuthModes())
                .as("authModes should contain otp")
                .isNotNull()
                .contains("otp");
    }

    @Owner(Constants.Owner.HIMANSHU)
    @Feature("PG-1626")
    @Test(description = "Verify authModes present in fetchBinDetail response for ineligible bin with normal initTxn token")
    public void validateFetchBinDetailIneligibleBinWithInitTxnToken() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();

        FetchBinDetailResponse fetchBinDetailResponse = NativeHelpers.fetchBinDetailResponse(
                txnToken, merchant.getId(), orderId, INELIGIBLE_CC_BIN);

        Assertions.assertThat(fetchBinDetailResponse.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("0000");
                Assertions.assertThat(fetchBinDetailResponse.getBody().getAuthModes())
                .as("authModes should contain otp")
                .isNotNull()
                .contains("otp");
    }
}
