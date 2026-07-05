package scripts.api.theia.applyPromo

import org.testng.annotations.Test

interface ApplyPromoTest {

    @Test
    void 'test when body mid is not provided'()

    @Test
    void 'test when body mid = null'()

    @Test
    void 'test when body mid = \'\''()

    @Test
    void 'test when body mid equals random value'()

    @Test
    void 'test when mid in query params is not provided'()

    @Test
    void 'test when mid in query params is not equals to mid in request body'()

    @Test
    void 'test when body promocode is not provided given merchant has no promos'()

    @Test
    void 'test when body promocode = null given merchant has no promos'()

    @Test
    void 'test when body promocode = \'\' given merchant has no promos'()

    @Test
    void 'test when body promocode equals random value given merchant has no promos'()

    @Test
    void 'test when body promocode is not provided given merchant has applicable promos'()

    @Test
    void 'test when body promocode = null given merchant has applicable promos'()

    @Test
    void 'test when body promocode = \'\' given merchant has applicable promos'()

    @Test
    void 'test when body totalTransactionAmount is not provided'()

    @Test
    void 'test when body totalTransactionAmount = null'()

    @Test
    void 'test when body totalTransactionAmount = \'\''()

    @Test
    void 'test when body totalTransactionAmount equals random value'()

    @Test
    void 'test when body custId is not provided'()

    @Test
    void 'test when body custId = null'()

    @Test
    void 'test when body custId = \'\''()

    @Test
    void 'test when body custId equals random value'()

    @Test
    void 'test when body paymentOptions is not provided'()

    @Test
    void 'test when body paymentOptions = null'()

    @Test
    void 'test when body paymentOptions = \'\''()

    @Test
    void 'test when body paymentOptions equals empty list'()

    @Test
    void 'test when body paymentOptions payMethod = CREDIT_CARD given merchant has applicable promos'()

    @Test
    void 'test when body paymentOptions payMethod = CREDIT_CARD and body paymentOptions savedCardId equals saved card id of CC given merchant has applicable promos'()

    @Test
    void 'test when body paymentOptions payMethod = CREDIT_CARD and body paymentOptions savedCardId equals CIN of CC given merchant has applicable promos'()

    @Test
    void 'test when body paymentOptions payMethod = CREDIT_CARD and body paymentOptions savedCardId equals saved card id of DC given merchant has applicable promos'()

    @Test
    void 'test when body paymentOptions payMethod = CREDIT_CARD and body paymentOptions savedCardId equals CIN of DC given merchant has applicable promos'()

    @Test
    void 'test when body paymentOptions payMethod = DEBIT_CARD given merchant has applicable promos'()

    @Test
    void 'test when body paymentOptions payMethod = DEBIT_CARD and body paymentOptions savedCardId equals saved card id of DC given merchant has applicable promos'()

    @Test
    void 'test when body paymentOptions payMethod = DEBIT_CARD and body paymentOptions savedCardId equals CIN of DC given merchant has applicable promos'()

    @Test
    void 'test when body paymentOptions payMethod = DEBIT_CARD and body paymentOptions savedCardId equals saved card id of CC given merchant has applicable promos'()

    @Test
    void 'test when body paymentOptions payMethod = DEBIT_CARD and body paymentOptions savedCardId equals CIN of CC given merchant has applicable promos'()

    @Test
    void 'test when body paymentOptions payMethod = NET_BANKING given merchant has applicable promos'()

    @Test
    void 'test when body paymentOptions payMethod = BALANCE given merchant has applicable promos'()

    @Test
    void 'test when body paymentOptions payMethod = WALLET given merchant has applicable promos'()

    @Test
    void 'test when body paymentOptions payMethod = PPBL given merchant has applicable promos'()

    @Test
    void 'test when body paymentOptions payMethod = PAYTM_DIGITAL_CREDIT given merchant has applicable promos'()

    @Test
    void 'test when body paymentOptions payMethod = UPI given merchant has applicable promos'()

    @Test
    void 'test when body paymentOptions payMethod = EMI given merchant has applicable promos'()

    @Test
    void 'test when body paymentOptions payMethod = EMI_DC given merchant has applicable promos'()

    @Test
    void 'test when body paymentOptions payMethod = COD given merchant has applicable promos'()

    @Test
    void 'test when body paymentOptions payMethod = MP_COD given merchant has applicable promos'()

    @Test
    void 'test when body paymentOptions payMethod = HYBRID_PAYMENT given merchant has applicable promos'()

    @Test
    void 'test when body promocode equals expired promocode'()

    @Test
    void 'test when promo is not valid for payMethod'()

    @Test
    void 'test when promo is not valid for mid'()

    @Test
    void 'test when promo is not applied by promo engine'()

    @Test
    void 'test when promo is provided for different bank for which promo is nor created'()

    @Test
    void "test when promo is of cashback type"()

    @Test
    void "test when promo is of discount type"()

    @Test
    void 'test when head token is not provided'()

    @Test
    void 'test when head token = null'()

    @Test
    void 'test when head token = \'\''()

    @Test
    void 'test when head token equals random value'()

    @Test
    void 'test when body paymentOptions payMethod = BALANCE but user info not passed in request'()

    @Test
    void 'test when min txn amt constraint is violated'()

    @Test
    void 'test when max txn amt constraint is violated'()
}