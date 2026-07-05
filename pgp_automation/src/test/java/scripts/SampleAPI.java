package scripts;

import com.paytm.api.AddMoney;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.AddMoneyDTO;
import com.paytm.dto.AddMoneyRequestDTO;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SampleAPI extends PGPBaseTest {


    @Test(description = "Validate successful response")
    public void testIfAPIIsSuccess() throws Exception {
        User user=userManager.getForWrite(Label.BASIC);
        String txnAmt = "1.0";
        AddMoneyRequestDTO addMoneyRequestDTO = new AddMoneyRequestDTO.AddMoneyRequestDTOBuilder()
                .setPayeePhoneNumber(user.mobNo())
                .setAmount(txnAmt)
                .build();

        AddMoneyDTO addMoneyDTO = new AddMoneyDTO.AddMoneyDTOBuilder()
                .setRequest(addMoneyRequestDTO)
                .build();

        AddMoney addMoney = new AddMoney(user.ssoToken());
        addMoney.getRequestSpecBuilder().setBody(addMoneyDTO);
        Response response = addMoney.execute();
        JsonPath jsonPath = response.jsonPath();

        assertThat(response.getStatusCode(), is(200));
        assertThat(jsonPath.get("statusCode"), is("SUCCESS"));
    }

    @Test(description = "Validate successful balance update")
    public void testIdAmountIsAddedToWallet() throws Exception {
        User user=userManager.getForWrite(Label.BASIC);
        String txnAmt = "1.0";

        double currentBalance = WalletHelpers.getWalletBalance(user);
        double expectedBalance = currentBalance + Double.parseDouble(txnAmt);

        AddMoneyRequestDTO addMoneyRequestDTO = new AddMoneyRequestDTO.AddMoneyRequestDTOBuilder()
                .setPayeePhoneNumber(user.mobNo())
                .setAmount(txnAmt)
                .build();

        AddMoneyDTO addMoneyDTO = new AddMoneyDTO.AddMoneyDTOBuilder()
                .setRequest(addMoneyRequestDTO)
                .build();

        AddMoney addMoney = new AddMoney(user.ssoToken());
        addMoney.getRequestSpecBuilder().setBody(addMoneyDTO);
        addMoney.execute();
        double actualBalance = WalletHelpers.getWalletBalance(user);
        assertThat(actualBalance, is(expectedBalance));
    }

}
