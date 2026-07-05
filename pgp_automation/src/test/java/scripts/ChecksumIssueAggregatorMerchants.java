package scripts;

import com.paytm.CreateToken;
import com.paytm.api.user.card.bin.query.BinModifyApi;
import com.paytm.api.user.card.bin.query.BinQueryApi;
import com.paytm.appconstants.Constants;
import com.paytm.dto.NativeDTO.InitTxn.ChecksumInitTxnApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static com.paytm.appconstants.Constants.MerchantType.MUTUALFUND;
import static com.paytm.appconstants.Constants.MerchantType.MUTUALFUND_AGGR;
import static com.paytm.appconstants.Constants.Owner.HIMANSHU;
import static com.paytm.apphelpers.CommonHelpers.generateOrderId;
import static org.fest.util.Arrays.format;

@Owner(HIMANSHU)
@Feature("PGP-30179")
public class ChecksumIssueAggregatorMerchants {

    SoftAssertions softly = new SoftAssertions();

    @Owner(HIMANSHU)
    @Feature("PGP-30179")
    @Test(description="Checksum Issue in response of Initiate transaction for Aggregator merchants")
    public void checksumIssue()
    {
        // get checksum for child Mutual Fund MID
        String orderId=generateOrderId();
        //Checking if transaction can be initiated with checksum created with Mutual Fund (Child) MID (It should not be initiated)
        ChecksumInitTxnApi api = new ChecksumInitTxnApi(MUTUALFUND.getKey(),MUTUALFUND.getId(),MUTUALFUND_AGGR.getId(),orderId);
        Response response = api.execute();
        softly.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Checksum provided is invalid");
        //get checksum for Aggr Mutual Fund MID
        orderId=generateOrderId();
        //Checking if transaction can be initiated with Aggr Mutual Fund checksum (It should be initiated)
        // transaction token should be generated
        api  = new ChecksumInitTxnApi(MUTUALFUND_AGGR.getKey(),MUTUALFUND.getId(),MUTUALFUND_AGGR.getId(),orderId);
        response = api.execute();

        softly.assertThat(response.jsonPath().getString("body.txnToken").length()!=0);
        softly.assertAll();
    }
}
