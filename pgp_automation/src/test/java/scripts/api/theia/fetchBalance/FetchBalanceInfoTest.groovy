package scripts.api.theia.fetchBalance

import org.testng.annotations.Test

interface FetchBalanceInfoTest {

    @Test
    void 'test when query params are not provided'()

    @Test
    void 'test when mid is not provided in query params'()

    @Test
    void 'test when orderId is not provided in query params'()

    @Test
    void 'test when mid provided in query params is different from mid provided in request body'()

    @Test
    void 'test when head token is not provided'()

    @Test
    void 'test when head token = null'()

    @Test
    void 'test when head token = \'\''()

    @Test
    void 'test when head token equals random value'()

    @Test
    void 'test when body mid is not provided'()

    @Test
    void 'test when body mid = null'()

    @Test
    void 'test when body mid = \'\''()

    @Test
    void 'test when body mid equals random value'()

    @Test
    void 'test when body paymentMode is not provided'()

    @Test
    void 'test when body paymentMode = null'()

    @Test
    void 'test when body paymentMode = \'\''()

    @Test
    void 'test when body paymentMode equals random value'()

    @Test
    void 'test when body paymentMode equals BALANCE given merchant and user both have PPI configured'()

    @Test
    void 'test when body paymentMode equals BALANCE given merchant and user both do not have PPI configured'()

    @Test
    void 'test when body paymentMode equals BALANCE given merchant has PPI configured but user does not'()

    @Test
    void 'test when body paymentMode equals BALANCE given user has PPI configured but merchant does not'()

    @Test
    void 'test when body paymentMode equals PPBL given merchant and user both have PPBL configured'()

    @Test
    void 'test when body paymentMode equals PPBL given merchant and user both do not have PPBL configured'()

    @Test
    void 'test when body paymentMode equals PPBL given merchant has PPBL configured but user does not'()

    @Test
    void 'test when body paymentMode equals PPBL given user has PPBL configured but merchant does not'()

    @Test
    void 'test when body paymentMode equals PAYTM_DIGITAL_CREDIT given merchant and user both have PAYTM_DIGITAL_CREDIT configured'()

    @Test
    void 'test when body paymentMode equals PAYTM_DIGITAL_CREDIT given merchant and user both do not have PAYTM_DIGITAL_CREDIT configured'()

    @Test
    void 'test when body paymentMode equals PAYTM_DIGITAL_CREDIT given merchant has PAYTM_DIGITAL_CREDIT configured but user does not'()

    @Test
    void 'test when body paymentMode equals PAYTM_DIGITAL_CREDIT given user has PAYTM_DIGITAL_CREDIT configured but merchant does not'()
}