package scripts.api.theia.applyPromo

import org.testng.annotations.Test

interface SSOTokenApplyPromoTest extends ApplyPromoTest {

    @Test
    void 'test when head token equals SSO token'()

    @Test
    void 'test when head token equals WALLET token'()

    @Test
    void 'test when head token equals TXN token'()
}