package scripts.api.theia.applyPromo

import org.testng.annotations.Test

interface TxnTokenApplyPromoTest extends ApplyPromoTest {
    @Test
    void "test when promo code is passed neither in Init Txn API nor in Apply Promo API"()

    @Test
    void "test when promo code is passed in Init Txn API but is not passed in Apply Promo API"()

    @Test
    void "test when promo code is not passed in Init Txn API but is passed in Apply Promo API"()

    @Test
    void "test when same promo code is passed in both Init Txn API and in Apply Promo API"()

    @Test
    void "test when promo code passed in Init Txn API is different from one passed in Apply Promo API"()

    @Test
    void 'test when body simplifiedPaymentOffers is not passed in Init Txn API'()
}