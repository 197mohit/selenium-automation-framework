package scripts.flows

import io.qameta.allure.Owner
import org.testng.annotations.Test

@Owner('Deepak | Somesh')
interface FlipkartPromoTest {

    @Test
    void 'test when order payment is done by ppi given paytm_cashback promo is applied'()

    @Test
    void 'test when order payment is done by cc given paytm_cashback promo is applied'()

    @Test
    void 'test when order payment is done by saved card CIN given paytm_cashback promo is applied'()

    @Test
    void 'test when order payment is done by saved card id given paytm_cashback promo is applied'()

    @Test
    void 'test when order payment is done by add n pay cc given paytm_cashback promo is applied'()

    @Test
    void 'test when order payment is done by add n pay cc given cashback promo is applied'()

    @Test
    void 'test when order payment is done by add n pay cc given discount promo is applied'()

    @Test
    void 'test when paytm_cashback promo is applied but discount promo details are passed while initiating order'()

    @Test
    void 'test when promo amt is greater than txn amt'()

    @Test
    void 'test when promo amt is lesser than txn amt'()

}