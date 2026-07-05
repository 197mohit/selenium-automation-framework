package scripts;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

// International , Prepaid, Bajaj Finserv & Corporate

//P+ and PGP

//PGP-26960
public interface InternationalSavedCard {

    @Test(description = "PG side: Non Logged In Flow : Verify that international card is visible on cashier page on international supported MID")
    @Parameters({"theme"})
    abstract void internationalCardNonLoggedInFlowPGSide(String theme) throws Exception;

    @Test(description = "P+ side : Non Logged In Flow  : Verify that international card is visible on cashier page on international supported MID")
    @Parameters({"theme"})
    abstract void internationalCardNonLoggedInFlowPPlusSide(String theme) throws Exception;

    @Test(description = "PG side : Logged In Flow : Verify that international card is visible on cashier page on international supported MID")
    @Parameters({"theme"})
    abstract void internationalCardLoggedInFlowPGSide(String theme) throws Exception;

    @Test(description = "P+ side : Logged In Flow : Verify that international card is visible on cashier page on international supported MID & SUCCESS txn using International Card CIN")
    @Parameters({"theme"})
    abstract void internationalCardLoggedInFlowPPlus(String theme) throws Exception;

    @Test(description = "PG side : Logged In & Non Logged In Flow : Verify that international card is not visible on cashier page on international non supported MID")
    @Parameters({"theme"})
    abstract void internationalCardNotVisibleLoggedInFlowPGSide(String theme) throws Exception;

    @Test(description = "P+ side : Logged in & non logged in flow : Verify that international card is not visible on cashier page on international non supported MID ")
    @Parameters({"theme"})
    abstract void internationalCardNotVisibleLoggedInFlowPPlus(String theme) throws Exception;

    @Test(description = "P +,PGP side : Recon Success : Bajaj fn card is getting filtered from both the sides")
    @Parameters({"theme"})
    abstract void bajajFinservFilteringAlipay(String theme) throws Exception;

    @Test(description = "PG side: Verify that prepaid card should not be visible on cashier page if mid doesnt supoort it")
    @Parameters({"theme"})
    abstract void prepaidCardNotVisibleUnsupportedMidPG(String theme) throws Exception;

    @Test(description = "P + : Verify that prepaid card should not be visible on cashier page if mid doesnt supoort it")
    @Parameters({"theme"})
    abstract void prepaidCardNotVisibleUnsupportedMid(String theme) throws Exception;

    @Test(description = "PG side: Verify that prepaid card should  be visible on cashier page if mid supoorts it")
    @Parameters({"theme"})
    abstract void prepaidCardVisibleSupportedMidPG(String theme) throws Exception;

    @Test(description = "P + : Verify that prepaid card should  be visible on cashier page if mid supoorts it")
    @Parameters({"theme"})
    abstract void prepaidCardVisibleSupportedMid(String theme) throws Exception;

}
