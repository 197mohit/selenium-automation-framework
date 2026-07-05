package scripts.api.theia.fetchPcfDetails

import org.testng.annotations.Test
import scripts.api.theia.HeadTest

interface FetchPCFDetailsTest extends HeadTest {

    @Test
    void "test when mid in query params is not provided"()

    @Test
    void "test when mid in query params is different from mid sent in request body"()

    @Test
    void "test when orderId in query params is not provided"()

    @Test
    void 'test when body mid is not provided'()

    @Test
    void 'test when body mid = null'()

    @Test
    void 'test when body mid =\'\''()

    @Test
    void 'test when body mid equals random value'()

    @Test
    void 'test when body txnAmount is not provided'()

    @Test
    void 'test when body txnAmount = null'()

    @Test
    void 'test when body txnAmount =\'\''()

    @Test
    void 'test when body txnAmount equals integer value'()

    @Test
    void 'test when body txnAmount equals decimal value with 1 significant digit after decimal'()

    @Test
    void 'test when body txnAmount equals decimal value with 2 significant digits after decimal'()

    @Test
    void 'test when body txnAmount equals 0'()

    @Test
    void 'test when body payMethods is not provided'()

    @Test
    void 'test when body payMethods = null'()

    @Test
    void 'test when body payMethods payMethod equals random value'()

    @Test
    void 'test when body payMethods equals empty list'()

    @Test
    void "test when merchant's commission type not equals post-convenience"()

    @Test
    void 'test when body payMethods items payMethod equals pay method not configured on merchant'()

    @Test
    void 'test when body payMethods items payMethod = NET_BANKING && body payMethods items instId != PPBL'()

    @Test
    void 'test when body payMethods items payMethod = NET_BANKING && body payMethods items instId = PPBL'()

    @Test
    void 'test when body payMethods items payMethod = CREDIT_CARD'()

    @Test
    void 'test when body payMethods items payMethod = DEBIT_CARD'()

    @Test
    void 'test when body payMethods items payMethod = EMI'()

    @Test
    void 'test when body payMethods items payMethod = EMI_DC'()

    @Test
    void 'test when body payMethods items payMethod = UPI'()

    @Test
    void 'test when body payMethods items payMethod = PAYTM_DIGITAL_CREDIT'()

    @Test
    void 'test when body payMethods items payMethod = PPBL'()

    @Test
    void 'test when body payMethods items payMethod = WALLET'()

    @Test
    void 'test when body payMethods items payMethod = BALANCE'()

    @Test
    void 'test when body payMethods items payMethod = ADVANCE_DEPOSIT_ACCOUNT'()

    @Test
    void 'test when multiple pay methods are provided'()
}