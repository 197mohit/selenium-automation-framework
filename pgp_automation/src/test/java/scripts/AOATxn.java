package scripts;

import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.pages.CheckoutPage;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

@Owner("Gagandeep")
public class AOATxn extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();

    public static class PaymentType {
        public static final String CC = "CREDIT_CARD";
        public static final String DC = "DEBIT_CARD";
        public static final String NB = "NET_BANKING";
        public static final String PPI = "WALLET";

    }

    @Test(description = "Verify CC/DC transaction when SSo token is not passed in request.")
    public void aoa_CC_S_withoutToken() throws Exception {

        String mid = "216820000000386640820";
        String txnAmount = "1.00";
        String paymentType = PaymentType.PPI;
        String cardInfo = "|5326760507590433|912|112024";
        String channelCode = "ICICI";

        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.NATIVE_HYBRID).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();

        String orderId=initTxnDTO.getBody().getOrderId();
        String txnToken=response.jsonPath().get("body.txnToken").toString();

        if (paymentType.equalsIgnoreCase(PaymentType.CC)) {
            OrderDTO orderDTO = new OrderFactory.AOA(
                    mid,
                    orderId,
                    txnToken,
                    paymentType)
                    .setNative_cardInfo(cardInfo)
                    .build();
            checkoutPage.createOrder(orderDTO);
        }
        else if(paymentType.equalsIgnoreCase(PaymentType.DC)){
            OrderDTO orderDTO = new OrderFactory.AOA(
                    mid,
                    orderId,
                    txnToken,
                    paymentType)
                    .setNative_cardInfo(cardInfo)
                    .build();
            checkoutPage.createOrder(orderDTO);
        }
        else if(paymentType.equalsIgnoreCase(PaymentType.NB)){
            OrderDTO orderDTO = new OrderFactory.AOA(
                    mid,
                    orderId,
                    txnToken,
                    paymentType)
                    .setNative_channelCode(channelCode)
                    .build();
            checkoutPage.createOrder(orderDTO);
        }
        else if(paymentType.equalsIgnoreCase(PaymentType.PPI)){
            OrderDTO orderDTO = new OrderFactory.AOA(
                    mid,
                    orderId,
                    txnToken,
                    paymentType)
                    .build();
            checkoutPage.createOrder(orderDTO);
        }
        else{
            System.out.println("Please select a valid payment mode");
        }



    }
}
