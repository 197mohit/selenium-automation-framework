package scripts.UPISubsMandate;

import org.testng.annotations.Test;

public interface UPIEnhancedTests {



    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=DAY")
    public void TC_001_Enhanced_Subs_FreqUnitDay(String theme) throws Exception;

    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=MONTH")
    public void TC_002_Enhanced_Subs_FreqUnitMonth(String theme) throws Exception;


    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=YEAR")
    public void TC_003_Enhanced_Subs_FreqUnitYear(String theme) throws Exception;


    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=QUARTER")
    public void TC_004_Enhanced_Subs_FreqUnitQuater(String theme) throws Exception;


    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=BI_MONTHLY")
    public void TC_005_Enhanced_Subs_FreqUnitBiMonthly(String theme) throws Exception;


    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=SEMI_ANNUALLY")
    public void TC_006_Enhanced_Subs_FreqUnitSemiAnually(String theme) throws Exception;


    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=ONDEMAND")
    public void TC_007_Enhanced_Subs_FreqUnitOnDemand(String theme) throws Exception;

    /*Frequency Related TestCases*/

    @Test(description = "Verify Invalid GraceDays when Freq>1 " +
            "requestType=NATIVE_SUBS")
    public void TC_001_Enhanced_Subs_FreqGreaterThn1(String theme) throws Exception;

    @Test(description = "Verify Freq>0 ,SubsPaymode =Unknown " +
            "requestType=NATIVE_SUBS UPI Paymode is filtered in FPO")
    public void TC_002_Enhanced_Subs_FreqGreaterthan1_PaymodeBlank(String theme) throws Exception;


    @Test(description = "Verify Freq=0 Subscription Validation Fails" +
            "requestType=NATIVE_SUBS freq is Mandatory")
    public void TC_003_Enhanced_Subs_FreqEqualToZero(String theme) throws Exception;

    @Test(description = "Verify Freq is Blank By Default it will consider it 1" +
            "requestType=NATIVE_SUBS")
    public void TC_004_Enhanced_Subs_FreqIsBlank(String theme) throws Exception;



    /*Start Date Related TestCases*/



    @Test(description = "Verify StartDate is Null not Supported in UPI " +
            "requestType=NATIVE_SUBS")
    public void TC_001_Enhanced_Subs_StartDateIsNull(String theme) throws Exception;


    @Test(description = "Verify StartDate is equal to Future Date Supported in UPI " +
            "requestType=NATIVE_SUBS")
    public void TC_002_Enhanced_Subs_StartDateIsFuture(String theme) throws Exception;


    @Test(description = "Verify subscriptionGraceDays: subscriptionStartDate: ,both are Blank UPI filtered in FPO" +
            "PTC fails")
    public void TC_003_Enhanced_Subs_StartDateGraceDayNull(String theme) throws Exception;


    @Test(description = "Verify subscriptionGraceDays:1 subscriptionStartDate: , invalid Subscription start date" +
            "requestType=NATIVE_SUBS")
    public void TC_004_Enhanced_Subs_StartDateIsOnlyBlank(String theme) throws Exception;



    /*Subscription Max Amount TestCases*/


    @Test(description = "If subscriptionMaxAmount>5000 and txn amount>5000, and paymode=null," +
            "Error: BHIM UPI is not allowed for this transaction,kindly use some other payment mode requestType=NATIVE_SUBS")
    public void TC_001_Enhanced_Subs_MaxAmountGreaterThan5000PaymodeNull(String theme) throws Exception;




    @Test(description = "If subscriptionMaxAmount>5000 and txn amount>5000, and paymode=UPI," +
            "resultMsg: Subscription Amount Limit For UPI Breached requestType=NATIVE_SUBS")
    public void TC_002_Enhanced_Subs_MaxAmountGreaterThan5000PaymodeUPI(String theme) throws Exception;




    @Test(description = "If txn amount>Subscription amount, and paymode=UPI," +
            "requestType=NATIVE_SUBS")
    public void TC_003_Enhanced_Subs_TxnAmountGreaterThanMaxAmount(String theme) throws Exception;



    @Test(description = "If subscriptionMaxAmount>5000 and txn amount<5000, and paymode=null," +
            "requestType=NATIVE_MF_SIP")
    public void TC_003_Enhanced_Subs_OnlyMaxAmountGreaterThanMaxAmount(String theme)  throws Exception;



    /*Amount Type TestCases*/


    @Test(description = "For FIX amount type,if  equal amount is not passed for txn amount & max amount if payment mode=UPI," +
            " resultMsg : Transaction amount is not equal to max amount set against the subscription" )
    public void TC_001_Enhanced_Subs_FixAmountEqualAmountNotPassedUPI(String theme) throws Exception;



    @Test(description = "For FIX amount type,if  equal amount is not passed for txn amount & max amount if payment mode=null," +
            " then it should fail at the time of process txn " )
    public void TC_002_Enhanced_Subs_FixAmountEqualAmountNotPassed(String theme) throws Exception;



    @Test(description = "For the Variable amount, the subscription txn amount should be" +
            " less than or equal to the subscription max amount" )
    public void TC_003_Enhanced_Subs_VariableAmountEqualOrLessMaxAmount(String theme) throws Exception;



    /*----------------------------------------------------------------------------*/
    /*          Test cases For Enhanced MF SIP Subs RequestType                      */
    /*----------------------------------------------------------------------------*/


    /*Frequency Unit related testcases*/

    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, freq=DAY")
    public void TC_001_Enhanced_MF_Subs_FreqUnitDay(String theme) throws Exception;

    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, freq=MONTH")
    public void TC_002_Enhanced_MF_Subs_FreqUnitMonth(String theme) throws Exception;


    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, freq=YEAR")
    public void TC_003_Enhanced_MF_Subs_FreqUnitYear(String theme) throws Exception;


    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, freq=QUARTER")
    public void TC_004_Enhanced_MF_Subs_FreqUnitQuater(String theme) throws Exception;


    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, freq=BI_MONTHLY")
    public void TC_005_Native_MF_Subs_FreqUnitBiMonthly(String theme) throws Exception;


    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, freq=SEMI_ANNUALLY")
    public void TC_006_Enhanced_MF_Subs_FreqUnitSemiAnually(String theme) throws Exception;


    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_MF_SIP, freq=ONDEMAND")
    public void TC_007_Enhanced_MF_Subs_FreqUnitOnDemand(String theme) throws Exception;

    /*Frequency Related TestCases*/

    @Test(description = "Verify Invalid GraceDays when Freq>1 and Frequency Unit Days" +
            "requestType=NATIVE_MF_SIP")
    public void TC_001_Enhanced_MF_Subs_FreqGreaterThn1(String theme) throws Exception;

    @Test(description = "Verify Freq>0 ,SubsPaymode =Unknown " +
            "requestType=NATIVE_MF_SIP UPI Paymode is filtered in FPO")
    public void TC_002_Enhanced_MF_Subs_FreqGreaterthan1_PaymodeBlank(String theme) throws Exception;



    @Test(description = "Verify Freq is Blank By Default it will consider it 1" +
            "requestType=NATIVE_MF_SIP")
    public void TC_003_Enhanced_MF_Subs_FreqIsBlank(String theme) throws Exception;



    /*Start Date Related TestCases*/



    @Test(description = "Verify StartDate is equal to Future Date Supported in UPI " +
            "requestType=NATIVE_MF_SIP")
    public void TC_001_Enhanced_MF_Subs_StartDateIsFuture(String theme) throws Exception;


    @Test(description = "Verify subscriptionGraceDays: subscriptionStartDate: ,both are Blank UPI filtered in FPO" +
            "PTC fails")
    public void TC_002_Enhanced_MF_Subs_StartDateGraceDayNull(String theme) throws Exception;



    /*Enable Retry TestCases*/


    @Test(description = "Verify when Paymode=UPI SubsRetry >2 Paymode=null , BHIM UPI is not allowed for this transaction, " +
            "kindly use some other payment mode requestType=NATIVE_MF_SIP")
    public void TC_001_Enhanced_MF_Subs_EnableRetryGreaterThan2PaymodeBlank(String theme) throws Exception;



    /*Subscription Max Amount TestCases*/

    @Test(description = "If subscriptionMaxAmount>5000 and txn amount>5000, and paymode=UPI," +
            "resultMsg: Subscription Amount Limit For UPI Breached requestType=NATIVE_MF_SIP")
    public void TC_001_Enhanced_MF_Subs_MaxAmountGreaterThan5000PaymodeNull(String theme) throws Exception;


    @Test(description = "If subscriptionMaxAmount>5000 and txn amount<5000, and paymode=null," +
            "requestType=NATIVE_MF_SIP")
    public void TC_002_Enhanced_MF_Subs_OnlyMaxAmountGreaterThanMaxAmount(String theme) throws Exception;



    /*Amount Type TestCases*/


    @Test(description = "For FIX amount type,if  equal amount is not passed for txn amount & max amount if payment mode=null," +
            " then it should fail at the time of process txn " )
    public void TC_001_Enhanced_MF_Subs_FixAmountEqualAmountNotPassed(String theme) throws Exception;

    @Test(description = "For the Variable amount, the subscription txn amount should be" +
            " less than or equal to the subscription max amount" )
    public void TC_002_Enhanced_MF_Subs_VariableAmountEqualOrLessMaxAmount(String theme) throws Exception;


    /*Validate Account Number For MF_SIP*/


    @Test(description = "validateAccountNumber:true, accountNumber:valid " +
            " allow Unverified account false txn is successful")
    public void TC_001_Enhanced_MF_Subs_AccountNumTrueAndValid(String theme) throws Exception;



    @Test(description = "validateAccountNumber:true, accountNumber:invalid,"+
            " allow Unverified account : false , then it should fail at the time of process txn " )
    public void TC_002_Enhanced_MF_Subs_AccountNumTrueAndInvalid(String theme) throws Exception;



    @Test(description = "validateAccountNumber:true, accountNumber:invalid " +
            " allow Unverified account : true , Txn is Successful " )
    public void TC_003_Enhanced_MF_Subs_AccountNumTrueAndInValidUnverifiedFalse(String theme) throws Exception;



    @Test(description = "validateAccountNumber:false, accountNumber:invalid,"+
            "  Txn is Successful " )
    public void TC_004_Enhanced_MF_Subs_AccountNumFalseAndValid(String theme) throws Exception;


    /* ONDEMAND VALIDATIONS PGP-26500*/
    @Test(description = "Verify that Frequency, StartDate, GracePeriod, RetryAllowed & RetryCount are ignored when" +
            "requestType=NATIVE_SUBS, freqUnit=ONDEMAND")
    public void TC_001_Enhanced_Subs_OnDemandValidations(String theme) throws Exception;

    @Test(description = "Verify that Frequency, StartDate, GracePeriod, RetryAllowed & RetryCount are ignored when" +
            "requestType=NATIVE_MF_SIP, freqUnit=ONDEMAND")
    public void TC_001_Enhanced_MF_Subs_OnDemandValidations(String theme) throws Exception;

    /* Sub Error Code PGP-32799*/
    @Test(description = "Verify that sub-error code is displayed in transaction Status respones")
    public void TC_001_Enhanced_Subs_Failure_ErrorCode(String theme) throws Exception;

}

