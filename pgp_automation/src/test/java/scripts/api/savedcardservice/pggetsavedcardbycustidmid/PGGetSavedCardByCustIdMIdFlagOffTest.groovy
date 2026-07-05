package scripts.api.savedcardservice.pggetsavedcardbycustidmid

import com.paytm.LocalConfig
import com.paytm.api.RedisAPI
import com.paytm.appconstants.Constants
import com.paytm.apphelpers.SavedCardHelpers
import com.paytm.framework.utils.DatabaseUtil
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import io.qameta.allure.Owner
import org.testng.SkipException
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import static org.hamcrest.Matchers.hasSize

@Owner('Deepak')
class PGGetSavedCardByCustIdMIdFlagOffTest extends PGGetSavedCardByCustIdMIdTest {

    @BeforeClass
    void beforeClass() {
        String queryForExp = "UPDATE FF4J_FEATURES SET ENABLE = 0 WHERE FEAT_UID = '$SC_FETCH_FROM_PLATFORM_FOR_MID_CUSTID';";
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, queryForExp);
        RedisAPI.deleteKey("FF4J_FEATURE_$SC_FETCH_FROM_PLATFORM_FOR_MID_CUSTID")
        Thread.sleep(Constants.FF4J_INTERNAL_CACHE_TIMEOUT)
    }

    @Override
    @Merchant(edit = true)
    @Test
    void 'test able to fetch saved card when VISA card is saved on custId and mId'() {
        m().users[0].savedCards.clear()
        assert m().users[0].savedCards.add(cards.find { it.scheme == 'visa' })
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @Merchant(edit = true)
    @Test
    void 'test able to fetch saved card when MASTER card is saved on custId and mId'() {
        m().users[0].savedCards.clear()
        assert m().users[0].savedCards.add(cards.find { it.scheme == 'master' })
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @Merchant(edit = true)
    @Test
    void 'test able to fetch saved card when AMEX card is saved on custId and mId'() {
        m().users[0].savedCards.clear()
        assert m().users[0].savedCards.add(cards.find { it.scheme == 'amex' })
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @Merchant(edit = true)
    @Test
    void 'test able to fetch saved card when DINERS card is saved on custId and mId'() {
        m().users[0].savedCards.clear()
        assert m().users[0].savedCards.add(cards.find { it.scheme == 'diners' })
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @Merchant(edit = true)
    @Test
    void 'test able to fetch saved card when MAESTRO card is saved on custId and mId'() {
        m().users[0].savedCards.clear()
        assert m().users[0].savedCards.add(cards.find { it.scheme == 'maestro' })
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @Merchant(edit = true)
    @Test
    void 'test able to fetch saved card when RUPAY card is saved on custId and mId'() {
        m().users[0].savedCards.clear()
        assert m().users[0].savedCards.add(cards.find { it.scheme == 'rupay' })
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @Merchant(edit = true)
    @Test
    void 'test able to fetch saved card when DISCOVER card is saved on custId and mId'() {
        m().users[0].savedCards.clear()
        assert m().users[0].savedCards.add(cards.find { it.scheme == 'discover' })
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @Merchant(edit = true)
    @Test
    void 'test able to fetch saved card when BAJAJFN card is saved on custId and mId'() {
        m().users[0].savedCards.clear()
        assert m().users[0].savedCards.add(cards.find { it.scheme == 'bajajfn' })
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @Merchant(edit = true)
    @Test
    void "test no saved cards are returned when no cards are saved on custId and mId"() {
        m().users[0].savedCards.clear()
        req().get().then()
                .spec(noContentSchema)
    }

    @Override
    @Merchant(edit = true)
    @Test
    void "test unable to fetch expired saved card(s) when expired cards are saved on custId and mId"() {
        m().users[0].savedCards.clear()
        def card = cards.find()
        assert m().users[0].savedCards.add(card)
        SavedCardHelpers.updateCardExpiry_withExpiredExpiry(m().users[0].savedCards[0].id, m().id, m().users[0].id)
        req().get().then()
                .spec(noContentSchema)
    }

    @Override
    @Merchant(edit = true)
    @Test
    void "test able to fetch saved card when credit card is saved on custId and mId"() {
        m().users[0].savedCards.clear()
        assert m().users[0].savedCards.add(cards.find { it.type == 'credit' })
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @Merchant(edit = true)
    @Test
    void "test able to fetch saved card when debit card is saved on custId and mId"() {
        m().users[0].savedCards.clear()
        assert m().users[0].savedCards.add(cards.find { it.type == 'debit' })
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @Merchant(edit = true)
    @Test
    void "test able to fetch max 10 saved card(s) when greater than 10 cards are saved on custId and mId"() {
        m().users[0].savedCards.clear()
        if (!m().users[0].savedCards.addAll(cards[0..10])) throw new SkipException('unable to save more than 10 cards')
        req().get().then()
                .spec(successSchema)
                .body('response', hasSize(10))
    }

    @Override
    @Merchant(value = { !it.acquirings.any { it.bank == 'amex' } }, edit = true)
    @Test
    void "test unable to fetch saved card(s) when card is saved on custId & mId given corresponding acquiring is not configured on merchant"() {
        m().users[0].savedCards.clear()
        if (!m().users[0].savedCards.add(cards.find { it.scheme == 'amex' })) throw new SkipException('unable to save card')
        req().get().then()
                .spec(successSchema)
    }

    @Override
    @Merchant(edit = true)
    @Test
    void 'test unable to fetch saved card(s) when mId equals random value'() {
        m().users[0].savedCards.clear()
        if (!m().users[0].savedCards.add(cards.find())) throw new SkipException('unable to save card')
        req(m().users[0].id, UUID.randomUUID().toString()).get().then()
                .spec(noContentSchema)
    }

    @Override
    @Merchant
    @Test
    void 'test unable to fetch saved card(s) when custId equals random value'() {
        req(UUID.randomUUID().toString(), m().id).get().then()
                .spec(noContentSchema)
    }
}
