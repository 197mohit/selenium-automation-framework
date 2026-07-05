package scripts.api.theia.validateRefId

import org.testng.annotations.Test

//TODO
// 1. add case where redis key has expired, then check how the API responds; check how then txn status and txn status list api's will behave
interface ValidateRefIdTest {

    @Test
    void 'test when refId is not provided'()

    @Test
    void 'test when refId = null'()

    @Test
    void 'test when refId = \'\''()

    @Test
    void 'test when refId equals random value'()

    @Test
    void 'test when ssoToken is not provided'()

    @Test
    void 'test when ssoToken = null'()

    @Test
    void 'test when ssoToken = \'\''()

    @Test
    void 'test when ssoToken equals random value'()

    @Test
    void 'test when ssoToken equals SSO token'()

    @Test
    void 'test when ssoToken equals WALLET token given SSO token is used in PTC'()

    @Test
    void 'test when ssoToken equals WALLET token given WALLET token is used in PTC'()

    @Test
    void 'test when ssoToken equals TXN token given SSO token is used in PTC'()

    @Test
    void 'test when ssoToken equals expired token'()

    @Test
    void 'test when mid is not provided'()

    @Test
    void 'test when mid = null'()

    @Test
    void 'test when mid = \'\''()

    @Test
    void 'test when mid equals random value'()

    @Test
    void 'test when mid passed is different from one provided in PTC'()

    @Test
    void 'test when order is successful'()

    @Test
    void 'test when order is failure'()

    @Test
    void 'test when order is pending'()
}