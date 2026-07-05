package scripts.api.savedcardservice.getsavedcardbyssotoken

import com.paytm.LocalConfig
import com.paytm.api.RedisAPI
import com.paytm.appconstants.Constants
import com.paytm.apphelpers.SavedCardHelpers
import com.paytm.base.test.User
import com.paytm.framework.utils.DatabaseUtil
import com.paytm.utils.merchant.user.alipay.AlipayUser
import com.paytm.utils.merchant.user.annotations.AUser
import io.qameta.allure.Owner
import org.testng.SkipException
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import static org.hamcrest.Matchers.*

@Owner('Deepak')
class GetSavedCardBySSOTokenFlagOnTest extends GetSavedCardBySSOTokenTest {

    @BeforeClass
    void beforeClass() {
        String queryForExp = "UPDATE FF4J_FEATURES SET EXPRESSION = 'weight=1', ENABLE = 1 WHERE FEAT_UID = '$SC_FETCH_FROM_PLATFORM_PERCENTAGE_FEATURE';";
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, queryForExp);
        RedisAPI.deleteKey("FF4J_FEATURE_$SC_FETCH_FROM_PLATFORM_PERCENTAGE_FEATURE")
        Thread.sleep(Constants.FF4J_INTERNAL_CACHE_TIMEOUT)
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test no saved cards are returned when no cards are saved on userId"() {
        (user() as AlipayUser).savedCards.clear()
        req().get().then()
                .spec(noContentSchema)
    }

//    @Override
//    @AUser(edit = true)
//    @Test(enabled = false)
    //TODO need to write code to update expiry of card on alipay
    void "test unable to fetch expired saved card(s) when expired cards are saved on userId"() {
        (user() as AlipayUser).savedCards.clear()
        def card = cards.find()
        assert (user() as AlipayUser).savedCards.add(card)
        SavedCardHelpers.updateCardExpiry_withExpiredExpiry(user().savedCards[0].id, user().with { new User(it.mobile, it.password) })
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test able to fetch saved card when VISA card is saved on userId"() {
        (user() as AlipayUser).savedCards.clear()
        assert (user() as AlipayUser).savedCards.add(cards.find { it.scheme == 'visa' })
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test able to fetch saved card when MASTER card is saved on userId"() {
        (user() as AlipayUser).savedCards.clear()
        assert (user() as AlipayUser).savedCards.add(cards.find { it.scheme == 'master' })
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test able to fetch saved card when AMEX card is saved on userId"() {
        (user() as AlipayUser).savedCards.clear()
        assert (user() as AlipayUser).savedCards.add(cards.find { it.scheme == 'amex' })
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test able to fetch saved card when DINERS card is saved on userId"() {
        (user() as AlipayUser).savedCards.clear()
        assert (user() as AlipayUser).savedCards.add(cards.find { it.scheme == 'diners' })
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test able to fetch saved card when MAESTRO card is saved on userId"() {
        (user() as AlipayUser).savedCards.clear()
        assert (user() as AlipayUser).savedCards.add(cards.find { it.scheme == 'maestro' })
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test able to fetch saved card when RUPAY card is saved on userId"() {
        (user() as AlipayUser).savedCards.clear()
        assert (user() as AlipayUser).savedCards.add(cards.find { it.scheme == 'rupay' })
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test able to fetch saved card when DISCOVER card is saved on userId"() {
        (user() as AlipayUser).savedCards.clear()
        assert (user() as AlipayUser).savedCards.add(cards.find { it.scheme == 'discover' })
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test able to fetch saved card when BAJAJFN card is saved on userId"() {
        (user() as AlipayUser).savedCards.clear()
        assert (user() as AlipayUser).savedCards.add(cards.find { it.scheme == 'bajajfn' })
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test able to fetch saved card when credit card is saved on userId"() {
        (user() as AlipayUser).savedCards.clear()
        assert (user() as AlipayUser).savedCards.add(cards.find { it.type == 'credit' })
        req().get().then()
                .spec(successSchema)
                .root('response')
                .body('cardScheme', everyItem(equalTo('CC')))
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test able to fetch saved card when debit card is saved on userId"() {
        (user() as AlipayUser).savedCards.clear()
        assert (user() as AlipayUser).savedCards.add(cards.find { it.type == 'debit' })
        req().get().then()
                .spec(successSchema)
                .root('response')
                .body('cardScheme', everyItem(equalTo('DC')))
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test able to fetch max 10 saved card(s) when greater than 10 cards are saved on userId"() {
        (user() as AlipayUser).savedCards.clear()
        if (!(user() as AlipayUser).savedCards.addAll(cards[0..10])) throw new SkipException('unable to save more than 10 cards')
        req().get().then()
                .spec(successSchema)
                .body('response', hasSize(10))
    }

    @Override
    @Test
    void "test unable to fetch saved card(s) when ssoToken equals random value"() {
        req(UUID.randomUUID().toString()).get().then()
                .spec(errorSchema)
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test able to fetch saved card(s) when ssoToken = paytm token"() {
        (user() as AlipayUser).savedCards.clear()
        assert (user() as AlipayUser).savedCards.add(cards.find())
        req(user().tokens['paytm'].id).get().then()
                .spec(successSchema)
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test able to fetch saved card(s) when ssoToken = txn token"() {
        (user() as AlipayUser).savedCards.clear()
        assert (user() as AlipayUser).savedCards.add(cards.find())
        req(user().tokens['txn'].id).get().then()
                .spec(successSchema)
    }

    @Override
    @AUser(edit = true)
    @Test
    void "test able to fetch saved card(s) when ssoToken = wallet token"() {
        (user() as AlipayUser).savedCards.clear()
        assert (user() as AlipayUser).savedCards.add(cards.find())
        req(user().tokens['wallet'].id).get().then()
                .spec(successSchema)
    }
}
