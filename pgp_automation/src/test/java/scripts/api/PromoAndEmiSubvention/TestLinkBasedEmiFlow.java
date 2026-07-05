package scripts.api.PromoAndEmiSubvention;

import com.paytm.api.theia.PromoAndEmiSubvention.LinkCreateEmi;

import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.pages.LinkPaymentLoginPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.paytm.appconstants.Constants.Owner.SATWIK_SHARMA;

public class TestLinkBasedEmiFlow extends PGPBaseTest {


    String linkBasedEmi = "{\"head\":{\"tokenType\":\"AES\",\"timestamp\":1667561189,\"signature\":\"Rl+3WQ2tYw/Rj5lXkcqehJLAb576rlxkLsdgzzwqis9RBuUO6kpYKl1cBmb77PBiq3SzpLpOFoTMYlXkJ58x1vxhDmORt7XrMIq3ZbgSyqI=\"},\"body\":{\"mid\":\"qa12FU97229952596781\",\"merchantRequestId\":\"{{$guid}}\",\"linkType\":\"FIXED\",\"linkDescription\":\"TestLinkforFIXEDtype\",\"linkName\":\"FIXED\",\"amount\":\"800\",\"simplifiedPaymentOffers\":{\"applyAvailablePromo\":\"true\",\"validatePromo\":false},\"simplifiedSubvention\":{\"subventionAmount\":\"800\",\"offerId\":\"\"}}}";



    /**
     * The Flow is for Old Flow EMI so make sure MID used is not migrated to new flow
     *
     */

    @Test(description = " OfferStrip Bar Is Not Overflowing  In LinkPaymentPage")
    @Owner(SATWIK_SHARMA)
    @Feature("PGP-54959")
    public void testOfferStripBarIsNotOverflowingInLinkPaymentPage() {

        String requestId = String.valueOf(CommonHelpers.getRandomWithSize(15))+"123";

        String mid = "qa12AG10986723061992";

        LinkCreateEmi linkCreate = setupLinkApi(mid,requestId);

        JsonPath jsonpath = linkCreate.execute().jsonPath();
        String paymentLink = jsonpath.getString("body.longUrl");

        System.out.println("payment link generated is"+paymentLink);

        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();

        linkPaymentLoginPage.launchLoginPage(paymentLink);
        linkPaymentLoginPage.waitUntilAllAJAXCallsFinish();
        linkPaymentLoginPage.takeScreenshot("LinkPaymentLoginPage");
        Assert.assertTrue(linkPaymentLoginPage.paymentOffers().isElementPresent());

    }

    private LinkCreateEmi setupLinkApi(String mid ,String requestId) {
        return (LinkCreateEmi) new LinkCreateEmi(linkBasedEmi)
                .setContext("body.merchantRequestId", requestId)
                .setContext("body.mid", mid);
    }

}
