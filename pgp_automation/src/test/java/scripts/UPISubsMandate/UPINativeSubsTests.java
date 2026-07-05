package scripts.UPISubsMandate;

import org.testng.annotations.Test;

public interface UPINativeSubsTests {


    /*Frequency Unit related testcases*/

    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=DAY")
    public void TC_001_Native_Subs_FreqUnitDay(Boolean isNativePlus) throws Exception;

    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=MONTH")
    public void TC_002_Native_Subs_FreqUnitMonth(Boolean isNativePlus) throws Exception;


    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=YEAR")
    public void TC_003_Native_Subs_FreqUnitYear(Boolean isNativePlus) throws Exception;


    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=QUARTER")
    public void TC_004_Native_Subs_FreqUnitQuater(Boolean isNativePlus) throws Exception;


    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=BI_MONTHLY")
    public void TC_005_Native_Subs_FreqUnitBiMonthly(Boolean isNativePlus) throws Exception;


    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=SEMI_ANNUALLY")
    public void TC_006_Native_Subs_FreqUnitSemiAnually(Boolean isNativePlus) throws Exception;


    @Test(description = "Verify txn token and subs id generated successfully when " +
            "requestType=NATIVE_SUBS, freq=WEEK")
    public void TC_007_Native_Subs_FreqUnitWeek(Boolean isNativePlus) throws Exception;

    @Test(description = "Verify txn token and subs id generated successfully when" +
            "requestType=NATIVE_SUBS, freq=ONDEMAND")
    public void TC_008_Native_Subs_FreqUnitOnDemand(Boolean isNativePlus) throws Exception;

    /*Frequency Related TestCases*/

    @Test(description = "Verify Invalid GraceDays when Freq>1 " +
            "requestType=NATIVE_SUBS")
    public void TC_001_Native_Subs_FreqGreaterThn1(Boolean isNativePlus) throws Exception;

    @Test(description = "Verify Freq>0 ,SubsPaymode =Unknown " +
            "requestType=NATIVE_SUBS UPI Paymode is filtered in FPO")
    public void TC_002_Native_Subs_FreqGreaterthan1_PaymodeBlank(Boolean isNativePlus) throws Exception;


    @Test(description = "Verify Freq=0 Subscription Validation Fails" +
            "requestType=NATIVE_SUBS freq is Mandatory")
    public void TC_003_Native_Subs_FreqEqualToZero(Boolean isNativePlus) throws Exception;

    @Test(description = "Verify Freq is Blank By Default it will consider it 1" +
            "requestType=NATIVE_SUBS")
    public void TC_004_Native_Subs_FreqIsBlank(Boolean isNativePlus) throws Exception;



    /*Start Date Related TestCases*/



    @Test(description = "Verify StartDate is Null not Supported in UPI " +
            "requestType=NATIVE_SUBS")
    public void TC_001_Native_Subs_StartDateIsNull(Boolean isNativePlus) throws Exception;


    @Test(description = "Verify StartDate is equal to Future Date Supported in UPI " +
            "requestType=NATIVE_SUBS")
    public void TC_002_Native_Subs_StartDateIsFuture(Boolean isNativePlus) throws Exception;


    @Test(description = "Verify subscriptionGraceDays: subscriptionStartDate: ,both are Blank UPI filtered in FPO" +
            "PTC fails")
    public void TC_003_Native_Subs_StartDateGraceDayNull(Boolean isNativePlus) throws Exception;


    @Test(description = "Verify subscriptionGraceDays:1 subscriptionStartDate: , invalid Subscription start date" +
            "requestType=NATIVE_SUBS")
    public void TC_004_Native_Subs_StartDateIsOnlyBlank(Boolean isNativePlus) throws Exception;



    /*Enable Retry TestCases*/

    @Test(description = "Verify when Paymode=UPI SubsRetry >2  , invalid Subscription retry count" +
            "requestType=NATIVE_SUBS")
    public void TC_001_Native_Subs_EnableRetryGreaterThan2(Boolean isNativePlus) throws Exception;


    @Test(description = "Verify when Paymode=UPI SubsRetry >2 Paymode=null , BHIM UPI is not allowed for this transaction, " +
            "kindly use some other payment mode requestType=NATIVE_SUBS")
    public void TC_002_Native_Subs_EnableRetryGreaterThan2PaymodeBlank(Boolean isNativePlus) throws Exception;



    /*Subscription Max Amount TestCases*/


    @Test(description = "If subscriptionMaxAmount>5000 and txn amount>5000, and paymode=null," +
            "Error: BHIM UPI is not allowed for this transaction,kindly use some other payment mode requestType=NATIVE_SUBS")
    public void TC_001_Native_Subs_MaxAmountGreaterThan5000PaymodeNull(Boolean isNativePlus) throws Exception;




    @Test(description = "If subscriptionMaxAmount>5000 and txn amount>5000, and paymode=UPI," +
            "resultMsg: Subscription Amount Limit For UPI Breached requestType=NATIVE_SUBS")
    public void TC_002_Native_Subs_MaxAmountGreaterThan5000PaymodeUPI(Boolean isNativePlus) throws Exception;




    @Test(description = "If txn amount>Subscription amount, and paymode=UPI," +
            "requestType=NATIVE_SUBS")
    public void TC_003_Native_Subs_TxnAmountGreaterThanMaxAmount(Boolean isNativePlus) throws Exception;


    @Test(description = "If subscriptionMaxAmount>5000 and txn amount<5000, and paymode=null," +
            "requestType=NATIVE_MF_SIP")
    public void TC_004_Native_Subs_OnlyMaxAmountGreaterThanMaxAmount(Boolean isNativePlus) throws Exception;

    /*Amount Type TestCases*/


    @Test(description = "For FIX amount type,if  equal amount is not passed for txn amount & max amount if payment mode=UPI," +
            " resultMsg : Transaction amount is not equal to max amount set against the subscription" )
    public void TC_001_Native_Subs_FixAmountEqualAmountNotPassedUPI(Boolean isNativePlus) throws Exception;



    @Test(description = "For FIX amount type,if  equal amount is not passed for txn amount & max amount if payment mode=null," +
            " then it should fail at the time of process txn " )
    public void TC_002_Native_Subs_FixAmountEqualAmountNotPassed(Boolean isNativePlus) throws Exception;



    @Test(description = "For the Variable amount, the subscription txn amount should be" +
            " less than or equal to the subscription max amount" )
    public void TC_003_Native_Subs_VariableAmountEqualOrLessMaxAmount(Boolean isNativePlus) throws Exception;



    @Test(description = "For the Variable amount, the subscription txn amount greater" +
            "  than subscription max amount " )
    public void TC_004_Native_Subs_VariableAmountGreater(Boolean isNativePlus) throws Exception;



    @Test(description = "Verify response validation ,txn status for Failure Txn after creation of Subscription" )
    public void TC_001_Native_Subs_FailureTransaction(Boolean isNativePlus) throws Exception;

    /* ONDEMAND VALIDATIONS PGP-26500*/
    @Test(description = "Verify that Frequency, StartDate, GracePeriod, RetryAllowed & RetryCount are ignored when" +
            "requestType=NATIVE_SUBS, freqUnit=ONDEMAND")
    public void TC_001_Native_Subs_OnDemandValidations(Boolean isNativePlus) throws Exception;

    /* Sub Error Code PGP-32799*/
    @Test(description = "Verify that sub-error code is displayed in NATIVE_SUBS transaction Status response")
    public void TC_001_Native_Subs_Failure_ErrorCode(Boolean isNativePlus) throws Exception;

}
