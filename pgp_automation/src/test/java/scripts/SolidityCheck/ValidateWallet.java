package scripts.SolidityCheck;

import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.framework.datareader.DataReaderUtil;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Created by anjukumari on 25/07/18
 */
@Owner("Gagandeep")
public class ValidateWallet extends PGPBaseTest {



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

    @Test(dataProvider = "getUsers", description = "Validate wallet APIs for")
    public void validate_User_At_Wallet(String user, String password) {
        try {
            User user1 = new User(user, password);
            WalletHelpers.setZeroBalance(user1);
            Assertions.assertThat(WalletHelpers.getWalletBalance(user1)).isEqualTo(0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
