package scripts.SolidityCheck;

import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.AuthHelpers;
import com.paytm.apphelpers.PostpaidHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.framework.datareader.DataReaderUtil;
import com.paytm.framework.reporting.listenerDecorators.DefaultExtentListener;
import com.paytm.utils.merchant.limit.MerchantVelocityLimit;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Created by anjukumari on 12/07/18
 */

@Owner("Gagandeep")
@Listeners({DefaultExtentListener.class})
public class ValidateModules extends PGPBaseTest {


    @DataProvider(name = "getUsers")
    public Object[][] provideUser(){
        Object[][] users =  DataReaderUtil.readCSV("users.csv", "users");
        Object[][] data = new Object[users.length][];
        for(int i = 0;i< users.length;i++){
            Object[] user = new Object[2];
            user[0] = users[i][0];
            user[1] = users[i][1];
            data[i] = user;
        }
        return data;
    }

//    @Test(dataProvider = "getUsers", enabled = false)
    public void validate_Wallet(String user, String password) {
        try {
            User user1 = new User(user, password);
            WalletHelpers.setZeroBalance(user1);
            Assertions.assertThat(WalletHelpers.getWalletBalance(user1)).isEqualTo(0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test(description = "Validate oauth module up and running")
    public void validate_Oauth(){
        try {
            User user1 = userManager.getForRead(Label.BASIC);
            AuthHelpers.getSSOToken(user1.mobNo(), user1.password());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test(description = "Validate mock up and running")
    public void validate_Mock(){
        try{
            PostpaidHelpers.updateBalance("2.0");
        }catch (Exception e){
        }
    }

    @Test(description = "Validate velocity module up and running")
    public void validate_Velocity(){
        MerchantVelocityLimit merchantVelocityLimit = new MerchantVelocityLimit();
        Assertions.assertThat(merchantVelocityLimit.checkLimit(Constants.MerchantType.Hybrid.getId()).getStatusCode()).isEqualTo(200);
    }







}
