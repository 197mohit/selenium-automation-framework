package scripts.api.theia

import org.testng.annotations.Test

interface SSOTokenAuthenticationTest {
    @Test
    void 'test when head token equals SSO token'()

    @Test
    void 'test when head token equals WALLET token'()

    @Test
    void 'test when head token equals TXN token'()

    @Test
    void 'test when head token equals expired token'()
}