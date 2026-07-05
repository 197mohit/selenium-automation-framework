
package scripts.contractValidation;

import com.paytm.api.DeveloperPaytmPageAPI;
import com.paytm.framework.api.BaseApi;
import io.qameta.allure.Owner;
import org.testng.annotations.Test;

@Owner("Deepak")
public class OrderStatusTest extends BaseApi {

    private static String ORDER_STATUS_URL="transaction-status-api/";

    @Test
    public void testMendatoryParams(){
        DeveloperPaytmPageAPI developerPaytmPageAPI=new DeveloperPaytmPageAPI(ORDER_STATUS_URL);
        //developerPaytmPageAPI.getParameters("");
    }

}

