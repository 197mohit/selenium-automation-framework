package scripts.api.theia.applyPromo

import org.testng.annotations.Test

interface ChecksumApplyPromoTest extends ApplyPromoTest {

    @Test
    void "test unable to apply promo when checksum is created using different merchant's key"()
}