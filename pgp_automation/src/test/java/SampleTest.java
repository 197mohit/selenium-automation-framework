import com.paytm.api.billproxy.CardTokenizeCardNumberV1API;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.FF4JFeatures;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.PaymentDTO;
import com.paytm.utils.ff4j.FF4JFlags;
import org.testng.annotations.Test;

import java.util.Arrays;

public class SampleTest extends PGPBaseTest {


    @Test
    public void test1() throws Exception {

        Constants.MerchantType m = Constants.MerchantType.EDC_PAY_CONFIRM;
        FF4JFlags.disableMidBased(FF4JFeatures.EXEMPT_MIDLIST_FROM_CLOSEORDER_WITH_PENDING_STATUS, m.getId());

//        CardTokenizeCardNumberV1API cardTokenizeCardNumberV1API =
//                new CardTokenizeCardNumberV1API(new PaymentDTO().getCreditCardNumber(), "ssoToken");
//        cardTokenizeCardNumberV1API.setContext("head.clientId", "IOS")
//                .setContext("body.testFiels", Arrays.asList("ankur", "sdfsdf"));
//        cardTokenizeCardNumberV1API.execute();


    }

}
