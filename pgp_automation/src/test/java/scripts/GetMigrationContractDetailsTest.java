package scripts;

import com.paytm.api.GetMigrationContractDetail;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GetMigrationContractDetailsTest extends PGPBaseTest {
    GetMigrationContractDetail getMigrationContractDetail = new GetMigrationContractDetail();
    @Owner("Vaibhav Tyagi")
    @Feature("PGP-38742")
    @Test(description = "Verify all enabled contract must be returned in mapping-service/query/merchant/migration/contract/details/{mid} API response")
    public void fetchMerchantContractDetail() {
        Constants.MerchantType merchantType = Constants.MerchantType.PCF_MERCHANT;
        getMigrationContractDetail.getMerchantContractDetail(merchantType.getId());
        JsonPath jsonPath = getMigrationContractDetail.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("contractDetailList[0].resultInfo.resultStatus").isEmpty());
    }
}