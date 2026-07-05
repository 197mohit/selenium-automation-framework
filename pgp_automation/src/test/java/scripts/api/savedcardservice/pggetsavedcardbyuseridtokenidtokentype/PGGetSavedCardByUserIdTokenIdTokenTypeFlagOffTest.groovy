package scripts.api.savedcardservice.pggetsavedcardbyuseridtokenidtokentype

import com.paytm.LocalConfig
import com.paytm.api.RedisAPI
import com.paytm.appconstants.Constants
import com.paytm.apphelpers.SavedCardHelpers
import com.paytm.base.test.User
import com.paytm.framework.utils.DatabaseUtil
import com.paytm.utils.merchant.user.annotations.AUser
import io.qameta.allure.Owner
import org.testng.SkipException
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import static org.hamcrest.Matchers.*

@Owner('Deepak')
class PGGetSavedCardByUserIdTokenIdTokenTypeFlagOffTest extends PGGetSavedCardByUserIdTokenIdTokenTypeTest {

    @BeforeClass
    void beforeClass() {
        String queryForExp = "UPDATE FF4J_FEATURES SET ENABLE = 0 WHERE FEAT_UID = '$SC_FETCH_FROM_PLATFORM_FOR_USER_ID';";
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, queryForExp);
        RedisAPI.deleteKey("FF4J_FEATURE_$SC_FETCH_FROM_PLATFORM_FOR_USER_ID")
        Thread.sleep(Constants.FF4J_INTERNAL_CACHE_TIMEOUT)
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test no saved cards are returned when no cards are saved on userId"() {
        user().savedCards.clear()
        assert user().savedCards.add(cards.find())
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test unable to fetch expired saved card(s) when expired cards are saved on userId"() {
        user().savedCards.clear()
        def card = cards.find()
        assert user().savedCards.add(card)
        SavedCardHelpers.updateCardExpiry_withExpiredExpiry(user().savedCards[0].id, user().with { new User(it.mobile, it.password) })
        req().get().then()
                .spec(noContentSchema)
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test able to fetch saved card when VISA card is saved on userId"() {
        user().savedCards.clear()
        assert user().savedCards.add(cards.find { it.scheme == 'visa' })
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test able to fetch saved card when MASTER card is saved on userId"() {
        user().savedCards.clear()
        assert user().savedCards.add(cards.find { it.scheme == 'master' })
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test able to fetch saved card when AMEX card is saved on userId"() {
        user().savedCards.clear()
        assert user().savedCards.add(cards.find { it.scheme == 'amex' })
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test able to fetch saved card when DINERS card is saved on userId"() {
        user().savedCards.clear()
        assert user().savedCards.add(cards.find { it.scheme == 'diners' })
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test able to fetch saved card when MAESTRO card is saved on userId"() {
        user().savedCards.clear()
        assert user().savedCards.add(cards.find { it.scheme == 'maestro' })
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test able to fetch saved card when RUPAY card is saved on userId"() {
        user().savedCards.clear()
        assert user().savedCards.add(cards.find { it.scheme == 'rupay' })
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test able to fetch saved card when DISCOVER card is saved on userId"() {
        user().savedCards.clear()
        assert user().savedCards.add(cards.find { it.scheme == 'discover' })
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test able to fetch saved card when BAJAJFN card is saved on userId"() {
        user().savedCards.clear()
        assert user().savedCards.add(cards.find { it.scheme == 'bajajfn' })
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test able to fetch saved card when credit card is saved on userId"() {
        user().savedCards.clear()
        assert user().savedCards.add(cards.find { it.type == 'credit' })
        req().get().then()
                .spec(successSchema)
                .root('response')
                .body('cardScheme', everyItem(equalTo('CREDIT_CARD')))
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test able to fetch saved card when debit card is saved on userId"() {
        user().savedCards.clear()
        assert user().savedCards.add(cards.find { it.type == 'debit' })
        req().get().then()
                .spec(successSchema)
                .root('response')
                .body('cardScheme', everyItem(equalTo('DEBIT_CARD')))
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test able to fetch max 10 saved card(s) when greater than 10 cards are saved on userId"() {
        user().savedCards.clear()
        if (!user().savedCards.addAll(cards[0..10])) throw new SkipException('unable to save more than 10 cards')
        req().get().then()
                .spec(successSchema)
                .body('response', hasSize(10))
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test unable to fetch saved card(s) when userId equals random value"() {
        user().savedCards.clear()
        assert user().savedCards.add(cards.find())
        req(userId: UUID.randomUUID().toString(), tokenId: user().tokens['sso'].id, tokenType: TokenType.OAUTH).get().then()
                .spec(headersResSpec)
                .statusCode(200)
                .body(
                        'responseStatus', equalTo('FAILURE'),
                        'httpCode', equalTo('400'),
                        'httpSubCode', equalTo('412'),
                        'codeDetail', equalTo('User Id is invalid'))
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test unable to fetch saved card(s) when tokenId equals random value"() {
        user().savedCards.clear()
        assert user().savedCards.add(cards.find())
        req(userId: user().id, tokenId: UUID.randomUUID().toString(), tokenType: TokenType.OAUTH).get().then()
                .spec(headersResSpec)
                .statusCode(200)
                .body(
                        'responseStatus', equalTo('FAILURE'),
                        'httpCode', equalTo('400'),
                        'httpSubCode', equalTo('413'),
                        'codeDetail', equalTo('user token is invalid'))
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test unable to fetch saved card(s) when tokenType equals random value"() {
        user().savedCards.clear()
        assert user().savedCards.add(cards.find())
        req(userId: user().id, tokenId: user().tokens['sso'].id, tokenType: UUID.randomUUID().toString()).get().then()
                .spec(headersResSpec)
                .statusCode(200)
                .body(
                        'responseStatus', equalTo('FAILURE'),
                        'httpCode', equalTo('400'),
                        'httpSubCode', equalTo('406'),
                        'codeDetail', equalTo('Invalid data entered by user'))
    }
}
