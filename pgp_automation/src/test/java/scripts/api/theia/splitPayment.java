package scripts.api.theia;

import com.paytm.api.MappingService.VendorDetails;
import com.paytm.api.TxnStatus;
import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.api.nativeAPI.InitTxn;

import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.Amount;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SplitInfo;
import com.paytm.dto.NativeDTO.InitTxn.SplitSettlementInfo;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.pages.*;
import com.paytm.utils.merchant.Peons;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Arrays;


public class splitPayment extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();
    Constants.MerchantType mutualFundMerchant = Constants.MerchantType.ONLINE_SETTLEMENT;
    com.paytm.appconstants.Constants.MerchantType aggMerchant = Constants.MerchantType.ONLINE_SETTLEMENT_AGG;


    @Parameters({"isNativePlus"})
    @Owner("Nirottam")
    @Test(description = "Verify a Successfull Txn  and splitInfo on Response Page for Spilt payment ")
    public void SPLIT_Payment_001(@Optional("false") Boolean isNativePlus) throws Exception {

        String txnAmount = "10";

        VendorDetails vendorDetails = new VendorDetails(mutualFundMerchant.getId());
        String vendorId = vendorDetails.execute().jsonPath().getString("resultResp.paytmMerchantId");

        SplitInfo splitInfo = new SplitInfo().setMid(vendorId).setAmount(new Amount().setValue("8").setPercentage(""));
        SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo().setSplitMethod("AMOUNT").setSplitInfo(new SplitInfo[]{splitInfo});

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mutualFundMerchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(aggMerchant.getId())
                .setMerchantKey(aggMerchant.getKey())
                .setSplitSettlementInfo(splitSettlementInfo)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(mutualFundMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(txnAmount)
                .setAggMid(aggMerchant.getId())
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateSplitSettlementInfo(splitSettlementInfo.getSplitMethod())
                .assertAll();
    }

}
