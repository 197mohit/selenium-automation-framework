package scripts;

import com.paytm.ServerConfigProvider;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.saveCard.BinDetails;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Owner;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

/*
* Account Range Bin Modification: Here there are two API endpoints
* 1. {MAPPING_SERVICE_URL}/get/bankcard/v1/bin/{cardbinId}
* 2. {MAPPING_SERVICE_URL}/get/bankcard/bin/{cardbinId}
*
* When request is hit to the endpoints with cardBinId (6 to 9 digits)
* 1. In response returns card bin details which includes "cardBin (6 digits)", not more than 6 digits
* 2. In logs first 6 digit should come then should be masked with '*' character
* More Info JIRA: PGP-30458
* */

@Owner(Constants.Owner.RITIK)
public class AccountRanges extends PGPBaseTest {

    public static String createString(int stringLength){
        //create new string from char array of required size
        String str = new String(new char[stringLength]);
        str = str.replace('\0', '*');
        return str;
    }

    // V1 API - mapping-service/get/bankcard/v1/bin/{binID}
    @Test(description = "Verify CardBin returned from BankCardBin API when Bin sent is of more than 9 digits")
    public void validateCardBinForMoreThanNineDigitsV1Api() throws Exception{
        String cardNumber = PaymentDTO.ICICI_DEBIT_CARD_NUMBER;
        BinDetails binDetails = PGPHelpers.getBinDetails(cardNumber);
        String receivedCardBin = binDetails.getBin();
        Assert.assertEquals(cardNumber.substring(0, 6), receivedCardBin);
        String cardBinInLogs = receivedCardBin + createString(10);
        String grepcmd = "grep \"" + receivedCardBin + "\" /paytm/logs/mapping-service.log | " + "grep 'Call received getCardBinInfo method cardBin'";
        String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        System.out.println(mappingServiceLogs);
        Assertions.assertThat(mappingServiceLogs).contains("Call received getCardBinInfo method cardBin " + cardBinInLogs);
    }

    @Test(description = "Verify CardBin returned from BankCardBin API when Bin sent is of 9 digits")
    public void validateCardBinForNineDigitsV1Api() throws Exception{
        String cardNumber = PaymentDTO.ICICI_DEBIT_CARD_NUMBER;
        BinDetails binDetails = PGPHelpers.getBinDetails(cardNumber.substring(0, 9));
        String receivedCardBin = binDetails.getBin();
        Assert.assertEquals(cardNumber.substring(0, 6), receivedCardBin);
        String cardBinInLogs = receivedCardBin + createString(3);
        System.out.println(cardBinInLogs);
        String grepcmd = "grep \"" + receivedCardBin + "\" /paytm/logs/mapping-service.log | " + "grep 'Call received getCardBinInfo method cardBin'";
        String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        System.out.println(mappingServiceLogs);
        Assertions.assertThat(mappingServiceLogs).contains("Call received getCardBinInfo method cardBin " + cardBinInLogs);
    }

    @Test(description = "Verify CardBin returned from BankCardBin API when Bin sent is of 6 digits")
    public void validateCardBinForSixDigitsV1Api() throws Exception{
        String cardNumber = PaymentDTO.ICICI_DEBIT_CARD_NUMBER;
        BinDetails binDetails = PGPHelpers.getBinDetails(cardNumber.substring(0, 6));
        String receivedCardBin = binDetails.getBin();
        Assert.assertEquals(cardNumber.substring(0, 6), receivedCardBin);
        String cardBinInLogs = receivedCardBin;
        System.out.println(cardBinInLogs);
        String grepcmd = "grep \"" + receivedCardBin + "\" /paytm/logs/mapping-service.log | " + "grep 'Call received getCardBinInfo method cardBin'";
        String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        System.out.println(mappingServiceLogs);
        Assertions.assertThat(mappingServiceLogs).contains("Call received getCardBinInfo method cardBin " + cardBinInLogs);
    }

    @Test(description = "Verify CardBin returned from BankCardBin API when Bin sent is of less than 6 digits")
    public void validateCardBinForLessThanSixDigitsV1Api() throws Exception{
        String cardNumber = PaymentDTO.ICICI_DEBIT_CARD_NUMBER;
        BinDetails binDetails = PGPHelpers.getBinDetails(cardNumber.substring(0, 5));
        String receivedCardBin = binDetails.getBin();
        Assert.assertEquals(cardNumber.substring(0, 5), receivedCardBin);
        String cardBinInLogs = receivedCardBin;
        System.out.println(cardBinInLogs);
        String grepcmd = "grep \"" + receivedCardBin + "\" /paytm/logs/mapping-service.log | " + "grep 'Call received getCardBinInfo method cardBin'";
        String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        System.out.println(mappingServiceLogs);
        Assertions.assertThat(mappingServiceLogs).contains("Call received getCardBinInfo method cardBin " + cardBinInLogs);
    }

    @Test(description = "Verify CardBin returned from BankCardBin API when Bin sent of 7 digits")
    public void validateCardBinForSevenDigitsV1Api() throws Exception{
        String cardNumber = PaymentDTO.ICICI_DEBIT_CARD_NUMBER;
        BinDetails binDetails = PGPHelpers.getBinDetails(cardNumber.substring(0, 7));
        String receivedCardBin = binDetails.getBin();
        Assert.assertEquals(cardNumber.substring(0, 6), receivedCardBin);
        String cardBinInLogs = receivedCardBin + createString(1);
        System.out.println(cardBinInLogs);
        String grepcmd = "grep \"" + receivedCardBin + "\" /paytm/logs/mapping-service.log | " + "grep 'Call received getCardBinInfo method cardBin'";
        String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        System.out.println(mappingServiceLogs);
        Assertions.assertThat(mappingServiceLogs).contains("Call received getCardBinInfo method cardBin " + cardBinInLogs);
    }

    // API - mapping-service/get/bankcard/bin/{binID}
    @Test(description = "Verify CardBin returned from BankCardBin API when Bin sent is of more than 9 digits")
    public void validateCardBinForMoreThanNineDigits() throws Exception{
        String cardNumber = PaymentDTO.ICICI_DEBIT_CARD_NUMBER;
        BinDetails binDetails = PGPHelpers.getBinDetailsApi(cardNumber);
        String receivedCardBin = binDetails.getBin();
        Assert.assertEquals(cardNumber.substring(0, 6), receivedCardBin);
        String cardBinInLogs = receivedCardBin + createString(10);
        String grepcmd = "grep \"" + receivedCardBin + "\" /paytm/logs/mapping-service.log | " + "grep 'Call received getCardBinInfo method cardBin'";
        String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        System.out.println(mappingServiceLogs);
        Assertions.assertThat(mappingServiceLogs).contains("Call received getCardBinInfo method cardBin " + cardBinInLogs);
    }

    @Test(description = "Verify CardBin returned from BankCardBin API when Bin sent is of 9 digits")
    public void validateCardBinForNineDigits() throws Exception{
        String cardNumber = PaymentDTO.ICICI_DEBIT_CARD_NUMBER;
        BinDetails binDetails = PGPHelpers.getBinDetailsApi(cardNumber.substring(0, 9));
        String receivedCardBin = binDetails.getBin();
        Assert.assertEquals(cardNumber.substring(0, 6), receivedCardBin);
        String cardBinInLogs = receivedCardBin + createString(3);
        System.out.println(cardBinInLogs);
        String grepcmd = "grep \"" + receivedCardBin + "\" /paytm/logs/mapping-service.log | " + "grep 'Call received getCardBinInfo method cardBin'";
        String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        System.out.println(mappingServiceLogs);
        Assertions.assertThat(mappingServiceLogs).contains("Call received getCardBinInfo method cardBin " + cardBinInLogs);
    }

    @Test(description = "Verify CardBin returned from BankCardBin API when Bin sent is of 6 digits")
    public void validateCardBinForSixDigits() throws Exception{
        String cardNumber = PaymentDTO.ICICI_DEBIT_CARD_NUMBER;
        BinDetails binDetails = PGPHelpers.getBinDetailsApi(cardNumber.substring(0, 6));
        String receivedCardBin = binDetails.getBin();
        Assert.assertEquals(cardNumber.substring(0, 6), receivedCardBin);
        String cardBinInLogs = receivedCardBin;
        System.out.println(cardBinInLogs);
        String grepcmd = "grep \"" + receivedCardBin + "\" /paytm/logs/mapping-service.log | " + "grep 'Call received getCardBinInfo method cardBin'";
        String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        System.out.println(mappingServiceLogs);
        Assertions.assertThat(mappingServiceLogs).contains("Call received getCardBinInfo method cardBin " + cardBinInLogs);
    }

    @Test(description = "Verify CardBin returned from BankCardBin API when Bin sent is of less than 6 digits")
    public void validateCardBinForLessThanSixDigits() throws Exception{
        String cardNumber = PaymentDTO.ICICI_DEBIT_CARD_NUMBER;
        BinDetails binDetails = PGPHelpers.getBinDetailsApi(cardNumber.substring(0, 5));
        String receivedCardBin = binDetails.getBin();
        Assert.assertEquals(cardNumber.substring(0, 5), receivedCardBin);
        String cardBinInLogs = receivedCardBin;
        System.out.println(cardBinInLogs);
        String grepcmd = "grep \"" + receivedCardBin + "\" /paytm/logs/mapping-service.log | " + "grep 'Call received getCardBinInfo method cardBin'";
        String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        System.out.println(mappingServiceLogs);
        Assertions.assertThat(mappingServiceLogs).contains("Call received getCardBinInfo method cardBin " + cardBinInLogs);
    }

    @Test(description = "Verify CardBin returned from BankCardBin API when Bin sent of 7 digits")
    public void validateCardBinForSevenDigits() throws Exception{
        String cardNumber = PaymentDTO.ICICI_DEBIT_CARD_NUMBER;
        BinDetails binDetails = PGPHelpers.getBinDetailsApi(cardNumber.substring(0, 7));
        String receivedCardBin = binDetails.getBin();
        Assert.assertEquals(cardNumber.substring(0, 6), receivedCardBin);
        String cardBinInLogs = receivedCardBin + createString(1);
        System.out.println(cardBinInLogs);
        String grepcmd = "grep \"" + receivedCardBin + "\" /paytm/logs/mapping-service.log | " + "grep 'Call received getCardBinInfo method cardBin'";
        String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        System.out.println(mappingServiceLogs);
        Assertions.assertThat(mappingServiceLogs).contains("Call received getCardBinInfo method cardBin " + cardBinInLogs);
    }
}
