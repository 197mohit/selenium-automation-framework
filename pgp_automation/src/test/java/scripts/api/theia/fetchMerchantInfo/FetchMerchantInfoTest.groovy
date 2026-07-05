package scripts.api.theia.fetchMerchantInfo

import org.testng.annotations.Test

interface FetchMerchantInfoTest {
    @Test
    void 'test when query params are not provided'()

    @Test
    void 'test when mid in query params is not provided'()

    @Test
    void 'test when orderId in query params is not provided'()

    @Test
    void 'test when mid provided in query params is different from mid provided in request body'()

    @Test
    void 'test when orderId provided in query params is different from orderId provided in request body'()

    @Test
    void 'test when body mid is not provided'()

    @Test
    void 'test when body mid = null'()

    @Test
    void 'test when body mid = \'\''()

    @Test
    void 'test when body mid equals random value'()

    @Test
    void 'test when body orderId is not provided'()

    @Test
    void 'test when body orderId = null'()

    @Test
    void 'test when body orderId = \'\''()

    @Test
    void 'test when body orderId equals random value'()

    @Test
    void 'test when head txnToken is not provided'()

    @Test
    void 'test when head txnToken = null'()

    @Test
    void 'test when head txnToken = \'\''()

    @Test
    void 'test when head txnToken equals random value'()

    @Test
    void 'test when head txnToken equals txn token generated using different mid'()

    @Test
    void 'test when head txnToken equals txn token generated using different orderId'()

    @Test
    void 'test when head txnToken equals txn token generated for OFFUS user'()

    @Test
    void 'test when head ssoToken is not provided'()

    @Test
    void 'test when head ssoToken = null'()

    @Test
    void 'test when head ssoToken = \'\''()

    @Test
    void 'test when head ssoToken equals random value'()

    @Test
    void 'test when head ssoToken equals SSO token'()

    @Test
    void 'test when head ssoToken equals WALLET token'()

    @Test
    void 'test when head ssoToken equals TXN token'()

    @Test
    void 'test when head ssoToken equals expired token'()

    @Test
    void 'test when SSO token used in head ssoToken is different from SSO token used to create txnToken'()

    @Test
    void 'test when WALLET token used in head ssoToken is different from WALLET token used to create txnToken'()

    @Test
    void 'test when TXN token used in head ssoToken is different from TXN token used to create txnToken'()

    @Test
    void 'test when SSO token is used in head ssoToken but WALLET token is used to create txnToken'()

    @Test
    void 'test body promoCodeApplied = true when promo is passed in InitiateTxn API while creating txnToken'()

    @Test
    void 'test body promoCodeApplied = false when promo is not passed in InitiateTxn API while creating txnToken'()

    @Test
    void 'test body txnAmount value when txnAmount having integer value'()

    @Test
    void 'test body txnAmount value when txnAmount having decimal value with one significant digit after decimal which is 0'()

    @Test
    void 'test body txnAmount value when txnAmount having decimal value with two significant digits after decimal with both as 0'()

    @Test
    void 'test for a subscription order having txn amt = 0'()

    @Test
    void 'test for a subscription order having txn amt greater than or equal to 1'()
}