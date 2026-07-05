package scripts;

import com.paytm.api.FetchPaymentOptionsLite;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

@Owner(Constants.Owner.CHAKSHU)
@Feature("PG2-12875")

public class JusPay extends PGPBaseTest {

    @Test(description = "check for different paymethods for DEFAULT request type with valid checksum")
    public void jusPayValidationforDEFAULTReqestType(){
        Constants.MerchantType mid = Constants.MerchantType.JUSPAY_PAYMODE;
        String requestType = "DEFAULT";
        FetchPaymentOptionsLite paymentoptionlist = new FetchPaymentOptionsLite(mid,requestType);
        JsonPath jsonpath=paymentoptionlist.execute().jsonPath();
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonpath.getString("body.netBanking")).isEqualTo("true");
        Assertions.assertThat(jsonpath.getString("body.creditCard")).isEqualTo("true");
        Assertions.assertThat(jsonpath.getString("body.debitCard")).isEqualTo("true");
        Assertions.assertThat(jsonpath.getString("body.paytmWallet")).isEqualTo("true");
        Assertions.assertThat(jsonpath.getString("body.paytmPostpaid")).isEqualTo("true");
        Assertions.assertThat(jsonpath.getString("body.emiCc")).isEqualTo("true");
        Assertions.assertThat(jsonpath.getString("body.emiDc")).isEqualTo("true");
        Assertions.assertThat(jsonpath.getString("body.upi")).isEqualTo("true");
        Assertions.assertThat(jsonpath.getString("body.cod")).isEqualTo("true");
        Assertions.assertThat(jsonpath.getString("body.advanceAccount")).isEqualTo("true");
        Assertions.assertThat(jsonpath.getString("body.bankTransfer")).isEqualTo("true");
        Assertions.assertThat(jsonpath.getString("body.netBankingOptions")).contains("ICICI","ICICI Bank","PPBL","Paytm Payments Bank");
        Assertions.assertThat(jsonpath.getString("body.upiOptions")).contains("UPI_PUSH","UPI_INTENT","UPI_COLLECT");
        Assertions.assertThat(jsonpath.getString("body.emiDcOptions")).contains("HDFC Bank");
        Assertions.assertThat(jsonpath.getString("body.emiCcOptions")).contains("HDFC Bank");
        Assertions.assertThat(jsonpath.getString("body.debitCardOptions.cardNetwork")).contains("MASTER","VISA","RUPAY","DINERS");
        Assertions.assertThat(jsonpath.getString("body.debitCardOptions.cardNetwork.MASTER")).contains("domestic","corporate","prepaid","savedCards");
        Assertions.assertThat(jsonpath.getString("body.debitCardOptions.cardNetwork.VISA")).contains("domestic","corporate","prepaid","savedCards");
        Assertions.assertThat(jsonpath.getString("body.creditCardOptions.cardNetwork")).contains("AMEX","DISCOVER","MAESTRO","MASTER","VISA","RUPAY","DINERS","domestic","international","corporate","savedCards");
        Assertions.assertThat(jsonpath.getString("body.creditCardOptions.cardNetwork.MASTER")).contains("domestic","international","corporate","savedCards");
        Assertions.assertThat(jsonpath.getString("body.creditCardOptions.cardNetwork.VISA")).contains("domestic","international","corporate","savedCards");
        Assertions.assertThat(jsonpath.getString("body.creditCardOptions.cardNetwork.AMEX")).contains("domestic","corporate","savedCards");
        Assertions.assertThat(jsonpath.getString("body.creditCardOptions.cardNetwork.MAESTRO")).contains("international","corporate");
    }

    @Test(description = "check for different paymethods for NATIVE_MF_PAY request type with valid checksum")
    public void jusPayValidationforNATIVE_MF_PAYReqestType(){
        Constants.MerchantType mid = Constants.MerchantType.MUTUAL_FUND;
        String requestType = "NATIVE_MF_PAY";
        FetchPaymentOptionsLite paymentoptionlist = new FetchPaymentOptionsLite(mid,requestType);
        JsonPath jsonpath=paymentoptionlist.execute().jsonPath();
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonpath.getString("body.netBanking")).isEqualTo("true");
        Assertions.assertThat(jsonpath.getString("body.debitCard")).isEqualTo("true");
        Assertions.assertThat(jsonpath.getString("body.upi")).isEqualTo("true");
        Assertions.assertThat(jsonpath.getString("body.netBankingOptions")).contains("ICICI","ICICI Bank");
        Assertions.assertThat(jsonpath.getString("body.upiOptions")).contains("UPI_PUSH","UPI_INTENT","UPI_COLLECT");
        Assertions.assertThat(jsonpath.getString("body.debitCardOptions.cardNetwork")).contains("MASTER","VISA","RUPAY","DINERS");
    }

    @Test(description = "check for bankMandate paymethods for NATIVE_SUBSCRIPTION_PAY request type with valid checksum")
    public void jusPayValidationforNATIVE_SUBSCRIPTION_PAYReqestType(){
        Constants.MerchantType mid = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        String requestType = "NATIVE_SUBSCRIPTION_PAY";
        FetchPaymentOptionsLite paymentoptionlist = new FetchPaymentOptionsLite(mid,requestType);
        JsonPath jsonpath=paymentoptionlist.execute().jsonPath();
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonpath.getString("body.bankMandate")).isEqualTo("true");
        Assertions.assertThat(jsonpath.getString("body.creditCard")).isEqualTo("true");
        Assertions.assertThat(jsonpath.getString("body.debitCard")).isEqualTo("true");
    }

    @Test(description = "check for different paymethods for NATIVE_PAY request type with valid checksum")
    public void jusPayValidationforNATIVE_PAYReqestType(){
        Constants.MerchantType mid = Constants.MerchantType.JUSPAY_PAYMODE;
        String requestType = "NATIVE_PAY";
        FetchPaymentOptionsLite paymentoptionlist = new FetchPaymentOptionsLite(mid,requestType);
        JsonPath jsonpath=paymentoptionlist.execute().jsonPath();
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonpath.getString("body.netBanking")).isEqualTo("true");
        Assertions.assertThat(jsonpath.getString("body.creditCard")).isEqualTo("true");
        Assertions.assertThat(jsonpath.getString("body.debitCard")).isEqualTo("true");
        Assertions.assertThat(jsonpath.getString("body.paytmWallet")).isEqualTo("true");
        Assertions.assertThat(jsonpath.getString("body.paytmPostpaid")).isEqualTo("true");
        Assertions.assertThat(jsonpath.getString("body.emiCc")).isEqualTo("true");
        Assertions.assertThat(jsonpath.getString("body.emiDc")).isEqualTo("true");
        Assertions.assertThat(jsonpath.getString("body.upi")).isEqualTo("true");
        Assertions.assertThat(jsonpath.getString("body.cod")).isEqualTo("true");
        Assertions.assertThat(jsonpath.getString("body.advanceAccount")).isEqualTo("true");
        Assertions.assertThat(jsonpath.getString("body.bankTransfer")).isEqualTo("true");
    }

    @Test(description = "check for different paymethods for PREAUTH request type with valid checksum")
    public void jusPayValidationforPREAUTHReqestType(){
        Constants.MerchantType mid = Constants.MerchantType.pushCloseNotify;
        String requestType = "PREAUTH";
        FetchPaymentOptionsLite paymentoptionlist = new FetchPaymentOptionsLite(mid,requestType);
        JsonPath jsonpath=paymentoptionlist.execute().jsonPath();
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonpath.getString("body.upi")).isEqualTo("true");
        Assertions.assertThat(jsonpath.getString("body.upiOptions")).contains("UPI_PUSH","UPI_INTENT","UPI_COLLECT");
    }

    @Test(description = "check for different paymethods for default request type with valid checksum")
    public void jusPayValidationfordefaultReqestType(){
        Constants.MerchantType mid = Constants.MerchantType.JUSPAY_PAYMODE;
        String requestType = "default";
        FetchPaymentOptionsLite paymentoptionlist = new FetchPaymentOptionsLite(mid,requestType);
        JsonPath jsonpath=paymentoptionlist.execute().jsonPath();
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultStatus")).isEqualTo("U");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultCode")).isEqualTo("00000900");
        Assertions.assertThat(jsonpath.getString("body.resultInfo.resultMsg")).isEqualTo("System error");
    }
}
