package scripts.SolidityCheck;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.framework.datareader.DataReaderUtil;
import com.paytm.framework.reporting.listenerDecorators.DefaultExtentListener;
import com.paytm.utils.merchant.util.AuthUtil;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.paytm.framework.reporting.Reporter.report;

/**
 * Created by anjukumari on 20/07/18
 */

@Owner("Gagandeep")
@Listeners(DefaultExtentListener.class)
public class ValidateUser extends PGPBaseTest {
    @DataProvider(name = "getUsers")
    public Object[][] provideUsesr(){
        Object[][] users =  DataReaderUtil.readCSV("users.csv", "users");
        Object[][] data = new Object[users.length][];
        for(int i = 0;i< users.length;i++){
            Object[] user = new Object[3];
            user[0] = users[i][0];
            user[1] = users[i][1];
            user[2] = users[i][2];
            data[i] = user;
        }
        return data;
    }

    @Test()
    public void No_Duplicate_UserInSuite(String user, String password, String lable)
    {
        Object[][] users =  DataReaderUtil.readCSV("users.csv", "users");
        Map<String, String> userTestList = new HashMap<>();
        for(int i =0 ;i < users.length; i++) {
            Assertions.assertThat(userTestList).containsKey(users[i][0].toString()).withFailMessage("Duplicate number found in test data");
        }
    }


    @Test(dataProvider = "getUsers")
    public void validate_Users_label(String user, String password, String lable) {
        try {
            Response response = AuthUtil.fetchUserStrategy(LocalConfig.AUTH_HOST, AuthUtil.getSSOToken(LocalConfig.AUTH_HOST, user, password));
            JsonPath jsonPath = response.jsonPath();
            report.info("Validate user : "+ user+" with lable: "+ lable);
            validate_Postpaid_User(jsonPath, lable);
            validate_Ppbl_User(jsonPath, lable);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void validate_Postpaid_User(JsonPath path, String lable){
        if(lable.contains("postpaid")){
            String digitalCredit = path.getString("userAttributeInfo.CREDIT_CARD");
            Assertions.assertThat(digitalCredit).containsIgnoringCase("true").withFailMessage("Number is not configured for paytm cc");
        }
    }

    private void validate_Ppbl_User(JsonPath path, String lable){
        if(lable.contains("ppbl")){
            String ppbl = path.getString("userTypes.PPB_CUSTOMER");
            Assertions.assertThat(ppbl).containsIgnoringCase("PPB_CUSTOMER").withFailMessage("Number is not configured with ppbl");
        }
    }

//    @Test(enabled = false)
    public void validate_Users() {
        try {
            String user = "7000000002";
             String password = "";//this test is outdated please pass password from DB if required in future
             String lable = "basic_postpaid";
            Response response = AuthUtil.fetchUserStrategy(LocalConfig.AUTH_HOST, AuthUtil.getSSOToken(LocalConfig.AUTH_HOST, user, password));
            JsonPath jsonPath = response.jsonPath();
            //validating digital credit card
            validate_Postpaid_User(jsonPath, lable);
            validate_Ppbl_User(jsonPath, lable);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
