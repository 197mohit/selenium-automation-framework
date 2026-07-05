package scripts.api.savedcardservice

import org.testng.annotations.Test

interface GetSavedCardsOnUserIdTest {

    @Test
    void "test no saved cards are returned when no cards are saved on userId"()

    @Test
    void "test unable to fetch expired saved card(s) when expired cards are saved on userId"()

    @Test
    void 'test able to fetch saved card when VISA card is saved on userId'()

    @Test
    void 'test able to fetch saved card when MASTER card is saved on userId'()

    @Test
    void 'test able to fetch saved card when AMEX card is saved on userId'()

    @Test
    void 'test able to fetch saved card when DINERS card is saved on userId'()

    @Test
    void 'test able to fetch saved card when MAESTRO card is saved on userId'()

    @Test
    void 'test able to fetch saved card when RUPAY card is saved on userId'()

    @Test
    void 'test able to fetch saved card when DISCOVER card is saved on userId'()

    @Test
    void 'test able to fetch saved card when BAJAJFN card is saved on userId'()

    @Test
    void 'test able to fetch saved card when credit card is saved on userId'()

    @Test
    void 'test able to fetch saved card when debit card is saved on userId'()

    @Test
    void 'test able to fetch max 10 saved card(s) when greater than 10 cards are saved on userId'()
}