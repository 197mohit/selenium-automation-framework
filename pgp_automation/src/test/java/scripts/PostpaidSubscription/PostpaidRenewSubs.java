package scripts.PostpaidSubscription;

import com.paytm.api.nativeAPI.RenewSubscription;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.NativeDTO.RenewSubscription.RenewSubscriptionDTO;
import com.paytm.framework.utils.DatabaseUtil;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class PostpaidRenewSubs {


    public static void modifyNotifyDatesInDB(String paytmRefId) {

        PGPHelpers.modifySubscriptionPreNotifyStatus(Long.valueOf(paytmRefId), "1");
        PGPHelpers.modifySubscriptionPreNotifyDate(Long.valueOf(paytmRefId), LocalDateTime.now().minusDays(2));
        PGPHelpers.modifySubscriptionPreNotifyTxnDate(Long.valueOf(paytmRefId), LocalDateTime.now().with(LocalTime.MIDNIGHT));

    }

    public static String executeRenewalAndFetchOrderId(Constants.MerchantType merchantType, String subsId, String txnAmount, String requestType) {
        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchantType.getId(), subsId, txnAmount)
                .setRequestType(requestType)
                .setMerchantKey(merchantType.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg"))
                .as("Result Message mismatch").isEqualTo("Subscription Txn accepted.");

        return renewSubscriptionDTO.getBody().getOrderId();
    }
}
