package scripts.CCBillPayments;

import com.paytm.FetchBin;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

@Owner("Deepak")
public class FetchBinTest extends PGPBaseTest {
    @Test(description = "Fetch bin if valid bin is passed", groups = {"regression"})
    public static void fetchValidBin() throws Exception {
        User user = userManager.getForRead(Label.BASIC);

        FetchBin fetchBin = new FetchBin(user.ssoToken());
        fetchBin.setContext("body.binNumber", "504492");

        JsonPath jsonPath = fetchBin.execute().jsonPath();
        String CardScheme = null;
        String Responsemsg = null;
        if (!(jsonPath == null)) {
            CardScheme = jsonPath.get("body.cardScheme");
            Responsemsg = jsonPath.get("body.responseMessage");
        }
        Assertions.assertThat(CardScheme).isEqualToIgnoringCase("MASTER");
        Assertions.assertThat(Responsemsg).isEqualToIgnoringCase("Success");
    }

    @Test(description = "To check response when invalid bin is passed in request", groups = {"regression"})
    public static void InvalidBinrequest() throws Exception {

        User user = userManager.getForRead(Label.BASIC);

        FetchBin fetchBin = new FetchBin(user.ssoToken());
        fetchBin.setContext("body.binNumber", "999");
        JsonPath jsonPath = fetchBin.execute().jsonPath();
        String CardScheme = null;
        String Responsemsg = null;
        if (!(jsonPath == null)) {
            Responsemsg = jsonPath.get("body.responseMessage");
        }
        Assertions.assertThat(Responsemsg).isEqualToIgnoringCase("PARAM_ILLEGAL");
    }

    @Test(description = "To check response when bin length less than 6 is passed in request", groups = {"regression"})
    public static void InvalidBinLength() throws Exception {
        User user = userManager.getForRead(Label.BASIC);

        FetchBin fetchBin = new FetchBin(user.ssoToken());
        fetchBin.setContext("body.binNumber", "9999");
        JsonPath jsonPath = fetchBin.execute().jsonPath();
        String CardScheme = null;
        String Responsemsg = null;
        if (!(jsonPath == null)) {
            Responsemsg = jsonPath.get("body.responseMessage");
        }
        Assertions.assertThat(Responsemsg).isEqualToIgnoringCase("PARAM_ILLEGAL");
    }


}

