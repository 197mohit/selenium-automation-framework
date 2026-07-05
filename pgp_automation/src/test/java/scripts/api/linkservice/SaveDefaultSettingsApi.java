package scripts.api.linkservice;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import com.paytm.api.linkAPI.*;
import java.util.*;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;


public class SaveDefaultSettingsApi extends PGPBaseTest{

        User user;
        String mid;

        void setMId(String merchant) throws Exception {
            mid = merchant;
        }

    @Owner("Shashank Gupta")
    @Feature("PGP-39628")
    @Test(description = "Removing default reminder API to be created")
    public void TrueAndRemoveChannelsAndDaysBeforeExpiry() throws Exception {
        ArrayList<Integer> daysAfterIssueDate = new ArrayList<Integer>();
        ArrayList<Integer> daysBeforeExpiry = new ArrayList<Integer>();
        ArrayList<String> channels = new ArrayList<String>();
        daysAfterIssueDate.add(1);

        setMId(Constants.MerchantType.SAVE_DEFAULT_SETTINGS.getId().toString());
        SaveDefaultSettings saveDefaultSettings = new SaveDefaultSettings();
        saveDefaultSettings.buildRequest(mid, "true",daysAfterIssueDate,daysBeforeExpiry,channels);
        JsonPath withDrawJson1 = saveDefaultSettings.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");

    }

    @Owner("Shashank Gupta")
    @Feature("PGP-39628")
    @Test(description = "Removing default reminder API to be created")
    public void TrueAndRemoveDaysBeforeExpiryAndDaysAfterIssueDate() throws Exception {
        ArrayList<Integer> daysAfterIssueDate = new ArrayList<Integer>();
        ArrayList<Integer> daysBeforeExpiry = new ArrayList<Integer>();
        ArrayList<String> channels = new ArrayList<String>();
        channels.add("PUSH");

        setMId(Constants.MerchantType.SAVE_DEFAULT_SETTINGS.getId().toString());
        SaveDefaultSettings saveDefaultSettings = new SaveDefaultSettings();
        saveDefaultSettings.buildRequest(mid, "true",daysAfterIssueDate,daysBeforeExpiry,channels);
        JsonPath withDrawJson1 = saveDefaultSettings.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
    }
    @Owner("Shashank Gupta")
    @Feature("PGP-39628")
    @Test(description = "Removing default reminder API to be created")
    public void TrueAndRemoveChannels() throws Exception {
        ArrayList<Integer> daysAfterIssueDate = new ArrayList<Integer>();
        ArrayList<Integer> daysBeforeExpiry = new ArrayList<Integer>();
        ArrayList<String> channels = new ArrayList<String>();

        daysAfterIssueDate.add(1);
        daysAfterIssueDate.add(2);
        daysBeforeExpiry.add(4);
        daysBeforeExpiry.add(3);

        setMId(Constants.MerchantType.SAVE_DEFAULT_SETTINGS.getId().toString());
        SaveDefaultSettings saveDefaultSettings = new SaveDefaultSettings();
        saveDefaultSettings.buildRequest(mid, "true",daysAfterIssueDate,daysBeforeExpiry,channels);
        JsonPath withDrawJson1 = saveDefaultSettings.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
    }

    @Owner("Shashank Gupta")
    @Feature("PGP-39628")
    @Test(description = "Removing default reminder API to be created")
    public void TrueAndChangeChannels() throws Exception {
        ArrayList<Integer> daysAfterIssueDate = new ArrayList<Integer>();
        ArrayList<Integer> daysBeforeExpiry = new ArrayList<Integer>();
        ArrayList<String> channels = new ArrayList<String>();

        daysAfterIssueDate.add(1);
        daysAfterIssueDate.add(2);
        daysBeforeExpiry.add(4);
        daysBeforeExpiry.add(3);
        channels.add("abcd");
        setMId(Constants.MerchantType.SAVE_DEFAULT_SETTINGS.getId().toString());
        SaveDefaultSettings saveDefaultSettings = new SaveDefaultSettings();
        saveDefaultSettings.buildRequest(mid, "true",daysAfterIssueDate,daysBeforeExpiry,channels);
        JsonPath withDrawJson1 = saveDefaultSettings.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
    }

    @Owner("Shashank Gupta")
    @Feature("PGP-39628")
    @Test(description = "Removing default reminder API to be created")
    public void FalseAndRemoveDaysAfterIssueDateAndDaysBeforeExpiry() throws Exception {
        ArrayList<Integer> daysAfterIssueDate = new ArrayList<Integer>();
        ArrayList<Integer> daysBeforeExpiry = new ArrayList<Integer>();
        ArrayList<String> channels = new ArrayList<String>();

        channels.add("PUSH");

        setMId(Constants.MerchantType.SAVE_DEFAULT_SETTINGS.getId().toString());
        SaveDefaultSettings saveDefaultSettings = new SaveDefaultSettings();
        saveDefaultSettings.buildRequest(mid, "false",daysAfterIssueDate,daysBeforeExpiry,channels);
        JsonPath withDrawJson1 = saveDefaultSettings.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultStatus")).isEqualTo("FAILED");
    }
    @Owner("Shashank Gupta")
    @Feature("PGP-39628")
    @Test(description = "Removing default reminder API to be created")
    public void FalseAndAddMoreThan5Reminders() throws Exception {
        ArrayList<Integer> daysAfterIssueDate = new ArrayList<Integer>();
        ArrayList<Integer> daysBeforeExpiry = new ArrayList<Integer>();
        ArrayList<String> channels = new ArrayList<String>();

        daysAfterIssueDate.add(1);
        daysAfterIssueDate.add(2);
        daysBeforeExpiry.add(4);
        daysBeforeExpiry.add(3);
        daysBeforeExpiry.add(5);
        daysBeforeExpiry.add(6);
        channels.add("PUSH");

        setMId(Constants.MerchantType.SAVE_DEFAULT_SETTINGS.getId().toString());
        SaveDefaultSettings saveDefaultSettings = new SaveDefaultSettings();
        saveDefaultSettings.buildRequest(mid, "false",daysAfterIssueDate,daysBeforeExpiry,channels);
        JsonPath withDrawJson1 = saveDefaultSettings.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultStatus")).isEqualTo("FAILED");
    }

    @Owner("Shashank Gupta")
    @Feature("PGP-39628")
    @Test(description = "Removing default reminder API to be created")
    public void FalseAndAddDuplicateForDaysBeforeExpiry() throws Exception {
        ArrayList<Integer> daysAfterIssueDate = new ArrayList<Integer>();
        ArrayList<Integer> daysBeforeExpiry = new ArrayList<Integer>();
        ArrayList<String> channels = new ArrayList<String>();
        daysAfterIssueDate.add(1);
        daysAfterIssueDate.add(2);
        daysBeforeExpiry.add(4);
        daysBeforeExpiry.add(4);
        channels.add("PUSH");

        setMId(Constants.MerchantType.SAVE_DEFAULT_SETTINGS.getId().toString());
        SaveDefaultSettings saveDefaultSettings = new SaveDefaultSettings();
        saveDefaultSettings.buildRequest(mid, "false",daysAfterIssueDate,daysBeforeExpiry,channels);
        JsonPath withDrawJson1 = saveDefaultSettings.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultStatus")).isEqualTo("FAILED");
    }
    @Owner("Shashank Gupta")
    @Feature("PGP-39628")
    @Test(description = "Removing default reminder API to be created")
    public void FalseAndChangeValueOfChannels() throws Exception {
        ArrayList<Integer> daysAfterIssueDate = new ArrayList<Integer>();
        ArrayList<Integer> daysBeforeExpiry = new ArrayList<Integer>();
        ArrayList<String> channels = new ArrayList<String>();
        daysAfterIssueDate.add(1);
        daysAfterIssueDate.add(2);
        daysBeforeExpiry.add(4);
        daysBeforeExpiry.add(3);
        channels.add("UIP");

        setMId(Constants.MerchantType.SAVE_DEFAULT_SETTINGS.getId().toString());
        SaveDefaultSettings saveDefaultSettings = new SaveDefaultSettings();
        saveDefaultSettings.buildRequest(mid, "false",daysAfterIssueDate,daysBeforeExpiry,channels);
        JsonPath withDrawJson1 = saveDefaultSettings.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultStatus")).isEqualTo("FAILED");
    }
    @Owner("Shashank Gupta")
    @Feature("PGP-39628")
    @Test(description = "Removing default reminder API to be created")
    public void FalseAndChangeDaysBeforeExpiryToNegative() throws Exception {
        ArrayList<Integer> daysAfterIssueDate = new ArrayList<Integer>();
        ArrayList<Integer> daysBeforeExpiry = new ArrayList<Integer>();
        ArrayList<String> channels = new ArrayList<String>();
        daysAfterIssueDate.add(1);
        daysAfterIssueDate.add(2);
        daysBeforeExpiry.add(4);
        daysBeforeExpiry.add(-3);
        channels.add("PUSH");

        setMId(Constants.MerchantType.SAVE_DEFAULT_SETTINGS.getId().toString());
        SaveDefaultSettings saveDefaultSettings = new SaveDefaultSettings();
        saveDefaultSettings.buildRequest(mid, "false",daysAfterIssueDate,daysBeforeExpiry,channels);
        JsonPath withDrawJson1 = saveDefaultSettings.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultStatus")).isEqualTo("FAILED");
    }
    @Owner("Shashank Gupta")
    @Feature("PGP-39628")
    @Test(description = "Removing default reminder API to be created")
    public void FalseAndChangeDaysBeforeExpiryAsNull() throws Exception {
        ArrayList<Integer> daysAfterIssueDate = new ArrayList<Integer>();
        ArrayList<Integer> daysBeforeExpiry = new ArrayList<Integer>();
        ArrayList<String> channels = new ArrayList<String>();
        daysAfterIssueDate.add(1);
        daysAfterIssueDate.add(2);
        channels.add("PUSH");

        setMId(Constants.MerchantType.SAVE_DEFAULT_SETTINGS.getId().toString());
        SaveDefaultSettings saveDefaultSettings = new SaveDefaultSettings();
        saveDefaultSettings.buildRequest(mid, "false",daysAfterIssueDate,daysBeforeExpiry,channels);
        JsonPath withDrawJson1 = saveDefaultSettings.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultStatus")).isEqualTo("SUCCESS");
    }

    @Owner("Shashank Gupta")
    @Feature("PGP-39628")
    @Test(description = "Removing default reminder API to be created")
    public void FalseAndDaysBeforeExpiryAndDaysBeforeExpiryAsNull() throws Exception {
        ArrayList<Integer> daysAfterIssueDate = new ArrayList<Integer>();
        ArrayList<Integer> daysBeforeExpiry = new ArrayList<Integer>();
        ArrayList<String> channels = new ArrayList<String>();
        channels.add("PUSH");

        setMId(Constants.MerchantType.SAVE_DEFAULT_SETTINGS.getId().toString());
        SaveDefaultSettings saveDefaultSettings = new SaveDefaultSettings();
        saveDefaultSettings.buildRequest(mid, "false",daysAfterIssueDate,daysBeforeExpiry,channels);
        JsonPath withDrawJson1 = saveDefaultSettings.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultStatus")).isEqualTo("FAILED");
    }
    @Owner("Shashank Gupta")
    @Feature("PGP-39628")
    @Test(description = "Removing default reminder API to be created")
    public void FalseAndChangeChannelsToNull() throws Exception {
        ArrayList<Integer> daysAfterIssueDate = new ArrayList<Integer>();
        ArrayList<Integer> daysBeforeExpiry = new ArrayList<Integer>();
        ArrayList<String> channels = new ArrayList<String>();
        daysAfterIssueDate.add(1);
        daysAfterIssueDate.add(2);
        daysBeforeExpiry.add(4);
        daysBeforeExpiry.add(3);

        setMId(Constants.MerchantType.SAVE_DEFAULT_SETTINGS.getId().toString());
        SaveDefaultSettings saveDefaultSettings = new SaveDefaultSettings();
        saveDefaultSettings.buildRequest(mid, "false",daysAfterIssueDate,daysBeforeExpiry,channels);
        JsonPath withDrawJson1 = saveDefaultSettings.execute().jsonPath();
        Assertions.assertThat(withDrawJson1.getString("body.resultInfo.resultStatus")).isEqualTo("FAILED");
    }

}
