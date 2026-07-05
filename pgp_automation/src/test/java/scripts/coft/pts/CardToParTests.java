package scripts.coft.pts;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.paytm.api.coft.PTS.CardToPar;
import com.paytm.api.coft.PTS.TokenizeDirectCard;
import com.paytm.appconstants.Constants;
import com.paytm.framework.ui.base.test.BaseTest;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.path.json.JsonPath;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @Author mayankbharshiv
 * @Date 05/07/22 1:35 PM
 * @Version 1.0
 */
public class CardToParTests extends BaseTest {
    Constants.MerchantType COFT_MERCHANT_3P = Constants.MerchantType.COFT_MERCHANT_3P;
    public static final String CUST_ID = RandomStringUtils.randomAlphabetic(10);
    public static String TIN = null;
    public static String PAR = null;
    public static final String MASTERCARD_CARD_DATA = "b2dWv0hyUh1Q2tyRvKfm3d3NvPr9LcXHa63u9nLcGEFCBbhsxfQiZEbp8UKnNQJPV9D6F5QRa81EuD7WfJRvf0zN3/aC1ZmRmIHcUQ9l1vJjuOVpzTpROyOgsCr2A7xCF0RRUlcFy7Q98slfwi0kvLJaDASlrA+zMdufpnYXR8DMb/0UgWYGZGqyjfAWiN80M48B8BYiqGBwwtKJNz4KsjltBqk6V59yXWuhRdAnG2N+rF9mYEsPJ6lBZZeX1fXdS3e9ssNVojduq25ejN/EeSL/DdAPHDmjtd9WUK5DZplgorhn+88y2mst77jvJ8xV5ELwfm2M2Mh53KapOWZR9A==";
    public static final String AUTH_REF_ID = "501123338";
    public static final String PAN = "DZT2auUi1tbMz1yENfOTnT+x9uL8uL+AvMZ/7KP+K4AwzmQ0D6kvfCXPssHLBTX51Gh3XlBmxkZFQ1E+c5riywV/ablel9fOMoiNJ23Q5t5lnoAKEYL996IT/kpzGHiZozKZ+laOwNHLr/cqd4t6tmf910hE+ymDIX387914/FuuowAyuvHpDyhOHv2DMqRe+G6ccn+HZi2L59XfN8QRfzFMPB6tOxb2eg2NfY2gITdMuXZMlt5LCb/VNhs5nHLMDanYwk6oou8FZ+KTZDt3lGF/nFUW0/r/AozH611jfbNf6jA2lv3Wq5iZmtXiqT6R6Ya0lw+p3XpbodN7sGxzHQ==";
    public static final String FAILURE_PAN = "BDZt+dKRIXuAU2lFvOfE4tbVXCpGJFYEdh/aF+CCiqFggGJAmliCJs2OtDKXC27bV5GHnz/HOd80U45K0WjFr0SNWUMNPzfrysxmiZJgxCwyXBXX+OcrubTRbFh61O/tArOAjT6jt/giSdxjKHn4U4tAKRyu52KG2KWg3LDF4XnCg7Y/SAxEGl4VjDCQWRhz0GUx+CCm/3IvX3efMpYJ44cihfzuRLnrN/tFUSSGXWgSEaAlBbdmxch6fdTmqFZ3/14L2jAdCf3AeTK8UiBq2uNP6kXzRj9Q/f6u6ZIn1B80DV1p62oLyRx2jY+eKlinbAq1gcozDVU9V8r3jXcd6Q==";
    public static String CIN = null;
    public static String GCIN = null;


    @BeforeClass
    public void init() {
        TokenizeDirectCard TokenizeDirectCard = new TokenizeDirectCard(COFT_MERCHANT_3P.getId())
                .buildRequest(MASTERCARD_CARD_DATA, CUST_ID, Constants.TokenizationConsent.YES.get(), AUTH_REF_ID, Constants.CardSource.CARD_ON_FILE.get(), "")
                .generateChecksum(COFT_MERCHANT_3P.getKey());
        JsonPath tokenizeCardResponse = TokenizeDirectCard.execute().jsonPath();
        TIN = tokenizeCardResponse.getString("body.tokenInfo.tokenIndexNumber");
        PAR = tokenizeCardResponse.getString("body.tokenInfo.panUniqueReference");
    }

    @Test(description = "Verify CIN and GCIN is returned for TIN")
    public void verifyCorrectResponseForTIN() {
        String checksum = checksum(COFT_MERCHANT_3P.getKey(), COFT_MERCHANT_3P.getId(), Constants.CardToParRequestType.TIN.get(), TIN, null);
        CardToPar cardToPar = new CardToPar(true).buildRequest(COFT_MERCHANT_3P.getId(), Constants.CardToParRequestType.TIN.get(), TIN, checksum);
        JsonPath cardToParResponse = cardToPar.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultCode")).isEqualTo("00");
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(cardToParResponse.getString("body.cardScheme")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.cardSuffix")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.cardType")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.displayName")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.issuingBankName")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.issuingBankCode")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.panUniqueReference")).isEqualTo(PAR);
        softly.assertThat(cardToParResponse.getString("body.cardIndexNumber")).isNull();
        softly.assertThat(cardToParResponse.getString("body.globalPanIndex")).isNotEmpty();
        softly.assertAll();
    }

    @Test(description = "Verify CIN and GCIN is returned for PAR", priority = -1)
    public void verifyCorrectResponseForPAR() {
        String checksum = checksum(COFT_MERCHANT_3P.getKey(), COFT_MERCHANT_3P.getId(), Constants.CardToParRequestType.PAR.get(), PAR, null);
        CardToPar cardToPar = new CardToPar(true).buildRequest(COFT_MERCHANT_3P.getId(), Constants.CardToParRequestType.PAR.get(), PAR, checksum);
        JsonPath cardToParResponse = cardToPar.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultCode")).isEqualTo("00");
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(cardToParResponse.getString("body.cardScheme")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.cardSuffix")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.cardType")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.displayName")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.issuingBankName")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.issuingBankCode")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.panUniqueReference")).isEqualTo(PAR);
        softly.assertThat(cardToParResponse.getString("body.cardIndexNumber")).isNull();
        softly.assertThat(cardToParResponse.getString("body.globalPanIndex")).isNotEmpty();
        softly.assertAll();
        CIN = cardToParResponse.getString("body.cardIndexNumber");
        GCIN = cardToParResponse.getString("body.globalPanIndex");
    }

    @Test(description = "Verify CIN and PAR is returned for GCIN")
    public void verifyCorrectResponseForGCIN() {
        String checksum = checksum(COFT_MERCHANT_3P.getKey(), COFT_MERCHANT_3P.getId(), Constants.CardToParRequestType.GCIN.get(), GCIN, null);
        CardToPar cardToPar = new CardToPar(true).buildRequest(COFT_MERCHANT_3P.getId(), Constants.CardToParRequestType.GCIN.get(), GCIN, checksum);
        JsonPath cardToParResponse = cardToPar.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultCode")).isEqualTo("00");
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(cardToParResponse.getString("body.cardScheme")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.cardSuffix")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.cardType")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.displayName")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.issuingBankName")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.issuingBankCode")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.panUniqueReference")).isEqualTo(PAR);
        softly.assertThat(cardToParResponse.getString("body.cardIndexNumber")).isNull();
        softly.assertThat(cardToParResponse.getString("body.globalPanIndex")).isNotEmpty();
        softly.assertAll();
    }

//    @Test(description = "Verify PAR and GCIN is returned for CIN", enabled = false)
    public void verifyCorrectResponseForCIN() {
        String checksum = checksum(COFT_MERCHANT_3P.getKey(), COFT_MERCHANT_3P.getId(), Constants.CardToParRequestType.CIN.get(), CIN, null);
        CardToPar cardToPar = new CardToPar(true).buildRequest(COFT_MERCHANT_3P.getId(), Constants.CardToParRequestType.CIN.get(), CIN, checksum);
        JsonPath cardToParResponse = cardToPar.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultCode")).isEqualTo("00");
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(cardToParResponse.getString("body.cardScheme")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.cardSuffix")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.cardType")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.displayName")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.issuingBankName")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.issuingBankCode")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.panUniqueReference")).isEqualTo(PAR);
        softly.assertThat(cardToParResponse.getString("body.cardIndexNumber")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.globalPanIndex")).isNotEmpty();
        softly.assertAll();
    }

    @Test(description = "Verify PAR is returned for PAN")
    public void verifyCorrectResponseForPAN() {
        String checksum = checksum(COFT_MERCHANT_3P.getKey(), COFT_MERCHANT_3P.getId(), Constants.CardToParRequestType.PAN.get(), PAN, null);
        CardToPar cardToPar = new CardToPar(true).buildRequest(COFT_MERCHANT_3P.getId(), Constants.CardToParRequestType.PAN.get(), PAN, checksum);
        JsonPath cardToParResponse = cardToPar.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultCode")).isEqualTo("00");
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(cardToParResponse.getString("body.panUniqueReference")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.cardIndexNumber")).isNull();
        softly.assertThat(cardToParResponse.getString("body.globalPanIndex")).isNotEmpty();
        softly.assertAll();
    }

    @Test(description = "Verify PAR is returned for PAN with call to Network is enabled")
    public void verifyCorrectResponseForPANWithNetwork() {
        String checksum = checksum(COFT_MERCHANT_3P.getKey(), COFT_MERCHANT_3P.getId(), Constants.CardToParRequestType.PAN.get(), PAN, null);
        CardToPar cardToPar = new CardToPar(false).buildRequest(COFT_MERCHANT_3P.getId(), Constants.CardToParRequestType.PAN.get(), PAN, checksum);
        JsonPath cardToParResponse = cardToPar.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultMsg")).isEqualTo("SUCCESS");
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultCode")).isEqualTo("00");
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        softly.assertThat(cardToParResponse.getString("body.panUniqueReference")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.cardIndexNumber")).isNull();
        softly.assertThat(cardToParResponse.getString("body.globalPanIndex")).isNotEmpty();
        softly.assertAll();
    }

    @Test(description = "verify Request Type Is Required")
    public void verifyRequestTypeIsRequired() {
        String checksum = checksum(COFT_MERCHANT_3P.getKey(), COFT_MERCHANT_3P.getId(), Constants.CardToParRequestType.PAN.get(), PAN, "requestType");
        CardToPar cardToPar = (CardToPar) new CardToPar(false).buildRequest(COFT_MERCHANT_3P.getId(), Constants.CardToParRequestType.PAN.get(), PAN, checksum).deleteContext("body.requestType");
        JsonPath cardToParResponse = cardToPar.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultMsg")).containsIgnoringCase("requestBody.requestType : must not be null");
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "verify Request Value Is Required")
    public void verifyRequestTypeValueIsRequired() {
        String checksum = checksum(COFT_MERCHANT_3P.getKey(), COFT_MERCHANT_3P.getId(), Constants.CardToParRequestType.PAN.get(), PAN, "requestValue");
        CardToPar cardToPar = (CardToPar) new CardToPar(false).buildRequest(COFT_MERCHANT_3P.getId(), Constants.CardToParRequestType.PAN.get(), PAN, checksum).deleteContext("body.requestValue");
        ;
        JsonPath cardToParResponse = cardToPar.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultMsg")).containsIgnoringCase("requestBody.requestValue : must not be null");
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "verify Request Type Is Valid")
    public void verifyRequestTypeIsValid() {
        String checksum = checksum(COFT_MERCHANT_3P.getKey(), COFT_MERCHANT_3P.getId(), "JaneDoe", PAN, null);
        CardToPar cardToPar = new CardToPar(false).buildRequest(COFT_MERCHANT_3P.getId(), "JaneDoe", PAN, checksum);
        JsonPath cardToParResponse = cardToPar.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultMsg")).containsIgnoringCase("Malformed Json Request");
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultCode")).isEqualTo("400");
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

    @Test(description = "Verify PAR is not returned for PAN with call to Network is not enabled")
    public void verifyFailureResponseForPANWithNetwork() {
        String checksum = checksum(COFT_MERCHANT_3P.getKey(), COFT_MERCHANT_3P.getId(), Constants.CardToParRequestType.PAN.get(), PAN, null);
        CardToPar cardToPar = new CardToPar(false).buildRequest(COFT_MERCHANT_3P.getId(), Constants.CardToParRequestType.PAN.get(), FAILURE_PAN, checksum);
        JsonPath cardToParResponse = cardToPar.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultMsg")).isEqualTo("PAR not found by CARD NETWORK");
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultCode")).isEqualTo("760");
        softly.assertThat(cardToParResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertThat(cardToParResponse.getString("body.cardScheme")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.cardSuffix")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.cardType")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.displayName")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.issuingBankName")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.issuingBankCode")).isNotEmpty();
        softly.assertThat(cardToParResponse.getString("body.panUniqueReference")).isNull();
        softly.assertThat(cardToParResponse.getString("body.cardIndexNumber")).isNull();
        softly.assertThat(cardToParResponse.getString("body.globalPanIndex")).isNotEmpty();
        softly.assertAll();
    }

    public static String checksum(String key, String mid, String requestType, String requestValue, String removeObject) {
        Gson gson = new Gson();
        JsonObject bodyForChecksum = gson.fromJson("{\"mid\":\"" + mid + "\",\"requestType\":\"" + requestType + "\",\"requestValue\":\"" + requestValue + "\"}", JsonObject.class);
        if (removeObject != null) {
            String s[] = removeObject.split("[.]");
            if (s.length > 1) {
                bodyForChecksum.getAsJsonObject(s[0]).remove(s[1]);
            } else {
                bodyForChecksum.remove(removeObject);
            }
        }
        String checksum = PGPUtil.getChecksum(key, bodyForChecksum.toString());
        return checksum;
    }
}
