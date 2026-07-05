package com.paytm.apphelpers.supercashhelpers;

import com.paytm.api.MappingService.MigrationDetails;
import com.paytm.framework.api.BaseApi;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;


public class superCashHelper  extends BaseApi {
    public static final String balance="BALANCE";
    public static final String postpid="PAYTM_DIGITAL_CREDIT";
    public static final String UPI="UPI";
    public static final String UPI_LITE="UPI_LITE";
    public static final String UPI_CC="UPI_CC";
    public static final String PPBL="PPBL";

    public static final String POSTPAID_Campaign="AUTOMATE_PGPROJECTCASE4";
    public static final String UPI_CC_Campaign="UPI_CC_INLINE_SANITY";
    public static final String PPBL_Campaign="PPBL_INLINE_SANITY";
    public static final String BALANCE_Campaign="AUTOMATE_PGPROJECTCASE2";
   // public static final String BALANCE_Campaign="PROJECTIONPGBALANCE01";
    public static final String UPI_LITE_Campaign="UPI_LITE_INLINE_SANITY";
    public static final String UPI_Campaign="UPI_INLINE_SANITY";


    public static String getSource(String mid)
    {
        String source=null;
        //to check whether merchant is offus or not
        Boolean isOnus = new MigrationDetails(mid).execute().jsonPath().getBoolean("MERCHANT-EXTENDED-INFO.extendedInfo.ONPAYTM");

        if (isOnus==true)
            return "ORDER";
        else if(isOnus==false)
            return "PG";
        else return source;
    }

    public static void Validate_Paymode(JsonPath applysupercashResponse, String paymode) {
        if (paymode.equals(superCashHelper.UPI_CC) )
        {
            Assertions.assertThat(applysupercashResponse.getString("body.supercashPayModes.find {it.paymode == '"+superCashHelper.UPI+"'}.subOffers.UPI_CC")).as("UPI CC is not found in supercash offer object").isNotEmpty();
            System.out.println("Paymode UPI CC Found in Response");
        }
        else {
            switch (paymode) {
                case "BALANCE":
                    Assertions.assertThat(applysupercashResponse.getString("body.supercashPayModes.find {it.paymode == '" + paymode + "'}.paymode")).as("Paymode Postpaid is not found in supercash API response").isEqualTo("BALANCE");
                    System.out.println("Paymode Balance Found in Response");
                    break;
                case "PAYTM_DIGITAL_CREDIT":
                    Assertions.assertThat(applysupercashResponse.getString("body.supercashPayModes.find {it.paymode == '" + paymode + "'}.paymode")).as("Paymode Postpaid is not found in supercash API response").isEqualTo("PAYTM_DIGITAL_CREDIT");
                    System.out.println("Paymode Paytm Digital Credit Found in Response");
                    break;
                case "PPBL":
                    Assertions.assertThat(applysupercashResponse.getString("body.supercashPayModes.find {it.paymode == '" + paymode + "'}.paymode")).as("Paymode Postpaid is not found in supercash API response").isEqualTo("PPBL");
                    System.out.println("Paymode PPBL Found in Response");
                    break;
                case "UPI_LITE":
                    Assertions.assertThat(applysupercashResponse.getString("body.supercashPayModes.find {it.paymode == '" + paymode + "'}.paymode")).as("Paymode Postpaid is not found in supercash API response").isEqualTo("UPI_LITE");
                    System.out.println("Paymode UPI LITE Found in Response");
                    break;
            }
        }
    }
    public static void validate_Campaign(JsonPath applysupercashResponse, String paymode) {


                Assertions.assertThat(applysupercashResponse.getString("body.supercashPayModes.find {it.paymode == '"+paymode+"'}.offers.campaign").replaceAll("\\[|\\]", "")).as("Offer Not Found for "+paymode).isEqualTo((paymode=="BALANCE")?BALANCE_Campaign:(paymode=="PAYTM_DIGITAL_CREDIT")?POSTPAID_Campaign:"");
                System.out.println("Campaign for "+paymode+" Found in Response");

    }
    public static void validate_Display_Text(JsonPath applysupercashResponse, String paymode) {
        String balanceDisplayText="Get ₹45 cashback";
        String postpaidDisplayText="Get ₹45 cashback";
        Assertions.assertThat(applysupercashResponse.getString("body.supercashPayModes.find {it.paymode == '"+paymode+"'}.offers.display_text").replaceAll("\\[|\\]", "")).as("Offer Not Found for "+paymode).isEqualTo((paymode=="BALANCE")?balanceDisplayText:(paymode=="PAYTM_DIGITAL_CREDIT")?postpaidDisplayText:"");
        System.out.println("Display Test for "+paymode+" Found in Response");
    }

    public static void validate_Failed_Response(JsonPath applysupercashResponse) {
        Assertions.assertThat(applysupercashResponse.getString("error")).as("expected illegal parameters in API response").isEqualTo("illegal parameters");
        Assertions.assertThat(applysupercashResponse.getString("body")).as("body should be null").isEqualTo(null);
        Assertions.assertThat(applysupercashResponse.getString("status")).as("status not found").isEqualTo("false");

    }

    public static void Validate_PaymodeIn_FPO(JsonPath fetchPaymentOptionsJson, String paymode) {
        if (paymode.equals(superCashHelper.UPI_CC) )
        {
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.superCashOffers.supercashPayModes.find {it.paymode == '"+superCashHelper.UPI+"'}.subOffers.UPI_CC")).as("UPI CC is not found in supercash offer object").isNotEmpty();
            System.out.println("Paymode UPI CC Found in Response");
        }
        else{
        switch (paymode) {
            case "BALANCE":
                Assertions.assertThat(fetchPaymentOptionsJson.getString("body.superCashOffers.supercashPayModes.find {it.paymode == '" + paymode + "'}.paymode")).as("Balance is not found in supercash offer object").isEqualTo("BALANCE");
                System.out.println("Paymode Balance Found in Response");
                break;
            case "PAYTM_DIGITAL_CREDIT":
                Assertions.assertThat(fetchPaymentOptionsJson.getString("body.superCashOffers.supercashPayModes.find {it.paymode == '" + paymode + "'}.paymode")).as("Paymode Postpaid is not found in supercash offer object").isEqualTo("PAYTM_DIGITAL_CREDIT");
                System.out.println("Paymode Paytm Digital Credit Found in Response");
                break;
            case "PPBL":
                Assertions.assertThat(fetchPaymentOptionsJson.getString("body.superCashOffers.supercashPayModes.find {it.paymode == '" + paymode + "'}.paymode")).isEqualTo("PPBL");
                System.out.println("Paymode PPBL Found in Response");
                break;
            case "UPI_LITE":
                Assertions.assertThat(fetchPaymentOptionsJson.getString("body.superCashOffers.supercashPayModes.find {it.paymode == '" + paymode + "'}.paymode")).as("UPI LITE is not found in supercash offer object").isEqualTo("UPI_LITE");
                System.out.println("Paymode UPI LITE Found in Response");
                break;
            case "UPI":
                Assertions.assertThat(fetchPaymentOptionsJson.getString("body.superCashOffers.supercashPayModes.find {it.paymode == '" + paymode + "'}.paymode")).as("UPI is not found in supercash offer object").isEqualTo("UPI");
                System.out.println("Paymode UPI Found in Response");
                break;
        }
        }
    }

    public static void validate_CampaignIn_FPO(JsonPath fetchPaymentOptionsJson, String paymode) {

        if (paymode.equals(superCashHelper.UPI_CC))
        {
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.superCashOffers.supercashPayModes.find {it.paymode == '"+superCashHelper.UPI+"'}.subOffers.UPI_CC.campaign").replaceAll("\\[|\\]", "")).isEqualTo(UPI_CC_Campaign);
            System.out.println("Campaign for " + paymode + " Found in Response");
        }
        else {
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.superCashOffers.supercashPayModes.find {it.paymode == '" + paymode + "'}.offers.campaign").replaceAll("\\[|\\]", ""))
                    .as("Offer Not Found for " + paymode).isEqualTo(
                            (paymode == superCashHelper.balance) ? BALANCE_Campaign :
                            (paymode == superCashHelper.PPBL) ?  PPBL_Campaign :
                            (paymode==superCashHelper.postpid)?POSTPAID_Campaign:
                            (paymode==superCashHelper.UPI_LITE)?UPI_LITE_Campaign :
                            (paymode==superCashHelper.UPI)?UPI_Campaign:"");
            System.out.println("Campaign for " + paymode + " Found in Response");
        }
    }

    public static void Validate_PaymodeIn_FQR(JsonPath fetchPaymentOptionsJson, String paymode) {
        if (paymode.equals(superCashHelper.UPI_CC) )
        {
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.paymentOptions.superCashOffers.supercashPayModes.find {it.paymode == '"+superCashHelper.UPI+"'}.subOffers.UPI_CC")).as("UPI CC is not found in supercash offer object").isNotEmpty();
            System.out.println("Paymode UPI CC Found in Response");
        }
        else{
            switch (paymode) {
                case "BALANCE":
                    Assertions.assertThat(fetchPaymentOptionsJson.getString("body.paymentOptions.superCashOffers.supercashPayModes.find {it.paymode == '" + paymode + "'}.paymode")).as("Balance is not found in supercash offer object").isEqualTo("BALANCE");
                    System.out.println("Paymode Balance Found in Response");
                    break;
                case "PAYTM_DIGITAL_CREDIT":
                    Assertions.assertThat(fetchPaymentOptionsJson.getString("body.paymentOptions.superCashOffers.supercashPayModes.find {it.paymode == '" + paymode + "'}.paymode")).as("Paymode Postpaid is not found in supercash offer object").isEqualTo("PAYTM_DIGITAL_CREDIT");
                    System.out.println("Paymode Paytm Digital Credit Found in Response");
                    break;
                case "PPBL":
                    Assertions.assertThat(fetchPaymentOptionsJson.getString("body.paymentOptions.superCashOffers.supercashPayModes.find {it.paymode == '" + paymode + "'}.paymode")).isEqualTo("PPBL");
                    System.out.println("Paymode PPBL Found in Response");
                    break;
                case "UPI_LITE":
                    Assertions.assertThat(fetchPaymentOptionsJson.getString("body.paymentOptions.superCashOffers.supercashPayModes.find {it.paymode == '" + paymode + "'}.paymode")).as("UPI LITE is not found in supercash offer object").isEqualTo("UPI_LITE");
                    System.out.println("Paymode UPI LITE Found in Response");
                    break;
                case "UPI":
                    Assertions.assertThat(fetchPaymentOptionsJson.getString("body.paymentOptions.superCashOffers.supercashPayModes.find {it.paymode == '" + paymode + "'}.paymode")).as("UPI is not found in supercash offer object").isEqualTo("UPI");
                    System.out.println("Paymode UPI Found in Response");
                    break;
            }
        }
    }

    public static void validate_CampaignIn_FQR(JsonPath fetchPaymentOptionsJson, String paymode) {

        if (paymode.equals(superCashHelper.UPI_CC))
        {
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.paymentOptions.superCashOffers.supercashPayModes.find {it.paymode == '"+superCashHelper.UPI+"'}.subOffers.UPI_CC.campaign").replaceAll("\\[|\\]", "")).isEqualTo(UPI_CC_Campaign);
            System.out.println("Campaign for " + paymode + " Found in Response");
        }
        else {
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.paymentOptions.superCashOffers.supercashPayModes.find {it.paymode == '" + paymode + "'}.offers.campaign").replaceAll("\\[|\\]", ""))
                    .as("Offer Not Found for " + paymode).isEqualTo(
                            (paymode == superCashHelper.balance) ? BALANCE_Campaign :
                                    (paymode == superCashHelper.PPBL) ?  PPBL_Campaign :
                                            (paymode==superCashHelper.postpid)?POSTPAID_Campaign:
                                                    (paymode==superCashHelper.UPI_LITE)?UPI_LITE_Campaign :
                                                            (paymode==superCashHelper.UPI)?UPI_Campaign:"");
            System.out.println("Campaign for " + paymode + " Found in Response");
        }
    }

    public static String getCampaignID_FQR(JsonPath fetchPaymentOptionsJson, String paymode) {

        if (paymode.equals(superCashHelper.UPI_CC))
        {
            return fetchPaymentOptionsJson.getString("body.paymentOptions.superCashOffers.supercashPayModes.find {it.paymode == '"+superCashHelper.UPI+"'}.subOffers.UPI_CC.id").replaceAll("\\[|\\]", "");
            }
        else {
            return  fetchPaymentOptionsJson.getString("body.paymentOptions.superCashOffers.supercashPayModes.find {it.paymode == '" + paymode + "'}.offers.id").replaceAll("\\[|\\]", "");
        }
    }

    public static String getCampaignID_FPO(JsonPath fetchPaymentOptionsJson, String paymode) {

        if (paymode.equals(superCashHelper.UPI_CC))
        {
            return fetchPaymentOptionsJson.getString("body.superCashOffers.supercashPayModes.find {it.paymode == '"+superCashHelper.UPI+"'}.subOffers.UPI_CC.id").replaceAll("\\[|\\]", "");
        }
        else {
            return  fetchPaymentOptionsJson.getString("body.superCashOffers.supercashPayModes.find {it.paymode == '" + paymode + "'}.offers.id").replaceAll("\\[|\\]", "");
        }
    }

}
