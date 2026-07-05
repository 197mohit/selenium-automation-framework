package com.paytm.apphelpers;

/**
 * Created by sourav singh on 19/07/21
 * To Manage Risk Reject TCs
 * Currently there is an issue related to Risk Reject
 * Risk Panel for Config is not accessible by Risk Team hence Risk Reject cannot be configured based on Risk Amount
 * Issue Jira ID - SMP1-6007
 * Risk Reject Amount Configration - https://wiki.mypaytm.com/pages/viewpage.action?spaceKey=PGP&title=QA-Risk+Reject+Amount+Configuration
 */

public class RiskRejectHelper{

    //General TCs
    public final String riskAmount = "1.88";
    public final String riskRejectRespCode = "501";
    public final String riskRejectRespMsg = "Sorry, this transaction is declined as the system surveillance bot has detected some suspicious activity. We advise you to retry this transaction later OR with a lower amount.";


    //TopUpExpress TCs
    public final String riskRespCode = "505";
}
