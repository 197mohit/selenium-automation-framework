package scripts.api.theia.fetchPayOptions

import org.testng.annotations.Test

interface TxnTokenFetchPayOptionsTest {
    @Test
    void 'test body simplifiedPaymentOffers is returned in response when body simplifiedPaymentOffers is passed in Init Txn API'()

    @Test
    void 'test body simplifiedPaymentOffers is not returned in response when body simplifiedPaymentOffers is not passed in Init Txn API'()

    @Test
    void 'test when body fetchAllPaymentOffers is not provided'()

    @Test
    void 'test when body fetchAllPaymentOffers = null'()

    @Test
    void 'test when body fetchAllPaymentOffers = \'\''()

    @Test
    void 'test when body fetchAllPaymentOffers equals random value'()

    @Test
    void 'test when body fetchAllPaymentOffers = true given merchant does not have promos configured'()

    @Test
    void 'test when body fetchAllPaymentOffers = true given merchant has promos configured and body simplifiedPaymentOffers promoCode is not passed in Initiate Txn API'()

    @Test
    void 'test when body fetchAllPaymentOffers = true given merchant has promos configured and body simplifiedPaymentOffers promoCode equals valid promo in Initiate Txn API'()

    @Test
    void 'test when body fetchAllPaymentOffers = true given merchant has promos configured and body simplifiedPaymentOffers promoCode equals invalid promo in Initiate Txn API'()

    @Test
    void 'test when body fetchAllPaymentOffers = false given merchant has promos configured'()

    @Test
    void 'test when body applyPaymentOffer is not provided'()

    @Test
    void 'test when body applyPaymentOffer = null'()

    @Test
    void 'test when body applyPaymentOffer = \'\''()

    @Test
    void 'test when body applyPaymentOffer equals random value'()

    @Test
    void 'test when body simplifiedPaymentOffers applyAvailablePromo = true in Initiate Txn API and body applyPaymentOffer = true in FetchPayOptions API'()

    @Test
    void 'test when body simplifiedPaymentOffers applyAvailablePromo = false in Initiate Txn API and body applyPaymentOffer = true in FetchPayOptions API'()

    @Test
    void 'test when body simplifiedPaymentOffers applyAvailablePromo = true in Initiate Txn API and body applyPaymentOffer = false in FetchPayOptions API'()

    @Test
    void 'test when body simplifiedPaymentOffers applyAvailablePromo = false in Initiate Txn API and body applyPaymentOffer = false in FetchPayOptions API'()

    @Test
    void 'test when body simplifiedPaymentOffers promoCode is not passed in Initiate Txn API'()

    @Test
    void 'test when body simplifiedPaymentOffers promoCode equals valid promo code in Initiate Txn API'()

    @Test
    void 'test when body simplifiedPaymentOffers promoCode equals invalid promo code in Initiate Txn API'()

    @Test
    void "test that Bulk Promo API is applied successfully when user has saved cards"()

    @Test
    void "test that Bulk Promo API is applied successfully when merchant has saved cards"()
}