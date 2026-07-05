package scripts.api.savedcardservice.getcard

import com.paytm.LocalConfig
import com.paytm.api.RedisAPI
import com.paytm.appconstants.Constants
import com.paytm.apphelpers.SavedCardHelpers
import com.paytm.base.test.User
import com.paytm.framework.utils.DatabaseUtil
import com.paytm.utils.merchant.merchant.util.alipay.AlipayMerchant
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.alipay.AlipayUser
import com.paytm.utils.merchant.user.annotations.AUser
import io.qameta.allure.Owner
import org.testng.SkipException
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import static org.hamcrest.Matchers.*

@Owner('Deepak')
class GetCardFlagOnTest extends GetCardTest {

    @BeforeClass
    void beforeClass() {
        String queryForExp = "UPDATE FF4J_FEATURES SET EXPRESSION = 'grantedMids=ALL', ENABLE = 1 WHERE FEAT_UID = '$SC_FETCH_FROM_PLATFORM_FOR_MID';";
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, queryForExp);
        RedisAPI.deleteKey("FF4J_FEATURE_$SC_FETCH_FROM_PLATFORM_FOR_MID")
        Thread.sleep(Constants.FF4J_INTERNAL_CACHE_TIMEOUT)
    }

    @Merchant(edit = true, value = { it.payModes.containsAll(['cc', 'dc']) })
    @AUser(edit = true)
    @Test
    void 'test able to fetch saved card(s) when REQUEST_TYPE is not provided'() {
        def root = root()
        assert (m() as AlipayMerchant).users[0].savedCards.add(cards.find())
        assert (user() as AlipayUser).savedCards.add(cards.find())
        root.remove('REQUEST_TYPE')
        req().body(root).post().then()
                .spec(successSchema)
    }

    @Merchant(edit = true, value = { it.payModes.containsAll(['cc', 'dc']) })
    @AUser(edit = true)
    @Test
    void 'test able to fetch saved card(s) when REQUEST_TYPE = null'() {
        def root = root()
        assert (m() as AlipayMerchant).users[0].savedCards.add(cards.find())
        assert (user() as AlipayUser).savedCards.add(cards.find())
        root.REQUEST_TYPE = null
        req().body(root).post().then()
                .spec(successSchema)
    }

    @Merchant(edit = true, value = { it.preferences.saveCard.enabled == true && it.payModes.containsAll(['cc', 'dc']) })
    @AUser(edit = true)
    @Test
    void "test able to fetch saved card(s) when REQUEST_TYPE = ''"() {
        def root = root()
        assert (m() as AlipayMerchant).users[0].savedCards.add(cards.find())
        assert (user() as AlipayUser).savedCards.add(cards.find())
        root.REQUEST_TYPE = ''
        req().body(root).post().then()
                .spec(successSchema)
    }

    @Merchant(edit = true, value = { it.payModes.containsAll(['cc', 'dc']) })
    @AUser(edit = true)
    @Test
    void 'test unable to fetch saved card(s) when REQUEST_TYPE equals random value'() {
        def root = root()
        assert (m() as AlipayMerchant).users[0].savedCards.add(cards.find())
        assert (user() as AlipayUser).savedCards.add(cards.find())
        root.REQUEST_TYPE = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(emptySchema)
    }

    @Merchant
    @Test
    void 'test unable to fetch saved card(s) when MID is not provided'() {
        def root = root()
        root.remove('MID')
        req().body(root).post().then()
                .spec(errorSchema)
                .body('response', equalTo(INPUT_PARAM_MID_OR_CHECKSUM_MISSING))
    }

    @Merchant
    @Test
    void 'test unable to fetch saved card(s) when MID = null'() {
        def root = root()
        root.MID = null
        req().body(root).post().then()
                .spec(errorSchema)
                .body('response', equalTo(INPUT_PARAM_MID_OR_CHECKSUM_MISSING))
    }

    @Merchant
    @Test
    void "test unable to fetch saved card(s) when MID = ''"() {
        def root = root()
        root.MID = ''
        req().body(root).post().then()
                .spec(errorSchema)
                .body('response', equalTo(INPUT_PARAM_MID_OR_CHECKSUM_MISSING))
    }

    @Merchant
    @Test
    void "test unable to fetch saved card(s) when MID equals random value"() {
        def root = root()
        root.MID = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(errorSchema)
                .body('response', equalTo(INVALID_CHECKSUM))
    }

    @Merchant
    @Test
    void 'test unable to fetch saved card(s) when CHECKSUM is not provided'() {
        def root = root()
        root.remove('CHECKSUM')
        req().body(root).post().then()
                .spec(errorSchema)
                .body('response', equalTo(INPUT_PARAM_MID_OR_CHECKSUM_MISSING))
    }

    @Merchant
    @Test
    void 'test unable to fetch saved card(s) when CHECKSUM = null'() {
        def root = root()
        root.CHECKSUM = null
        req().body(root).post().then()
                .spec(errorSchema)
                .body('response', equalTo(INPUT_PARAM_MID_OR_CHECKSUM_MISSING))
    }

    @Merchant
    @Test
    void "test unable to fetch saved card(s) when CHECKSUM = ''"() {
        def root = root()
        root.CHECKSUM = ''
        req().body(root).post().then()
                .spec(errorSchema)
                .body('response', equalTo(INPUT_PARAM_MID_OR_CHECKSUM_MISSING))
    }

    @Merchant
    @Test
    void "test unable to fetch saved card(s) when CHECKSUM equals random value"() {
        def root = root()
        root.CHECKSUM = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(errorSchema)
                .body('response', equalTo(INVALID_CHECKSUM))
    }

    @Merchant(edit = true)
    @Test
    void 'test no saved cards are returned when CUSTID && SSO_TOKEN are not provided'() {
        def root = root()
        root.remove('CUSTID')
        root.remove('SSO_TOKEN')
        req().body(root).post().then()
                .spec(emptySchema)
    }

    @Merchant(edit = true)
    @Test
    void 'test no saved cards are returned when CUSTID = null && SSO_TOKEN = null'() {
        def root = root()
        root.CUSTID = null
        root.SSO_TOKEN = null
        req().body(root).post().then()
                .spec(emptySchema)
    }

    @Merchant(edit = true)
    @AUser(edit = true)
    @Test
    void 'test no saved cards are returned when CUSTID is not provided and no card is saved on userId'() {
        def root = root()
        (user() as AlipayUser).savedCards.clear()
        root.remove('CUSTID')
        root.SSO_TOKEN = user().tokens['sso'].id
        req().body(root).post().then()
                .spec(emptySchema)
    }

    @Merchant(edit = true)
    @Test
    void 'test no saved cards are returned when SSO_TOKEN is not provided and no card is saved on custId and mId'() {
        def root = root()
        (m() as AlipayMerchant).users[0].savedCards.clear()
        root.CUSTID = m().users[0].id
        root.remove('SSO_TOKEN')
        req().body(root).post().then()
                .spec(emptySchema)
    }

    @Merchant(edit = true)
    @AUser(edit = true)
    @Test
    void 'test no saved cards are returned when CUSTID = null and no card is saved on userId'() {
        def root = root()
        (user() as AlipayUser).savedCards.clear()
        root.CUSTID = null
        root.SSO_TOKEN = user().tokens['sso'].id
        req().body(root).post().then()
                .spec(emptySchema)
    }

    @Merchant(edit = true)
    @Test
    void 'test no saved cards are returned when SSO_TOKEN = null and no card is saved on custId and mId'() {
        def root = root()
        (m() as AlipayMerchant).users[0].savedCards.clear()
        root.CUSTID = m().users[0].id
        root.SSO_TOKEN = null
        req().body(root).post().then()
                .spec(emptySchema)
    }

    @Merchant(edit = true)
    @AUser(edit = true)
    @Test
    void "test no saved cards are returned when CUSTID = '' and no card is saved on userId"() {
        def root = root()
        (user() as AlipayUser).savedCards.clear()
        root.CUSTID = ''
        root.SSO_TOKEN = user().tokens['sso'].id
        req().body(root).post().then()
                .spec(emptySchema)
    }

    @Merchant(edit = true)
    @Test
    void "test no saved cards are returned when SSO_TOKEN = '' and no card is saved on custId and mId"() {
        def root = root()
        (m() as AlipayMerchant).users[0].savedCards.clear()
        root.CUSTID = m().users[0].id
        root.SSO_TOKEN = ''
        req().body(root).post().then()
                .spec(emptySchema)
    }

    @Merchant(edit = true)
    @Test
    void 'test no saved cards are returned when CUSTID equals random value and no card is saved on userId'() {
        def root = root()
        root.CUSTID = (UUID.randomUUID().toString() * 5)
        req().body(root).post().then()
                .spec(emptySchema)
    }

    @Merchant(edit = true)
    @Test
    void 'test no saved cards are returned when SSO_TOKEN equals random value and no card is saved on custId and mId'() {
        def root = root()
        root.SSO_TOKEN = UUID.randomUUID().toString()
        req().body(root).post().then()
                .spec(invalidDataSchema)
    }

    @Override
    @Merchant(edit = true, value = { it.preferences.saveCard.enabled == true })
    @Test
    void "test no saved cards are returned when no cards are saved on custId and mId"() {
        def root = root()
        (m() as AlipayMerchant).users[0].savedCards.clear()
        root.CUSTID = m().users[0].id
        root.SSO_TOKEN = null
        req().body(root).post().then()
                .spec(emptySchema)
    }

    @Override
    @Merchant
    @AUser(edit = true)
    @Test
    void "test no saved cards are returned when no cards are saved on userId"() {
        def root = root()
        (user() as AlipayUser).savedCards.clear()
        root.CUSTID = null
        root.SSO_TOKEN = user().tokens['sso'].id
        req().body(root).post().then()
                .spec(emptySchema)
    }

//    @Override
//    @Merchant(edit = true, value = { it.preferences.saveCard.enabled == true })
//    @Test(enabled = false)
    //TODO need to write code to update expiry of card on alipay
    void "test unable to fetch expired saved card(s) when expired cards are saved on custId and mId"() {
        def root = root()
        (m() as AlipayMerchant).users[0].savedCards.clear()
        def card = cards.find()
        assert (m() as AlipayMerchant).users[0].savedCards.add(card)
        SavedCardHelpers.updateCardExpiry_withExpiredExpiry(m().users[0].savedCards[0].id, m().id, m().users[0].id)
        root.CUSTID = m().users[0].id
        root.SSO_TOKEN = null
        req().body(root).post().then()
                .spec(noContentSchema)
    }

//    @Override
//    @Merchant
//    @AUser(edit = true)
//    @Test(enabled = false)
    //TODO need to write code to update expiry of card on alipay
    void "test unable to fetch expired saved card(s) when expired cards are saved on userId"() {
        def root = root()
        (user() as AlipayUser).savedCards.clear()
        def card = cards.find()
        assert (user() as AlipayUser).savedCards.add(card)
        SavedCardHelpers.updateCardExpiry_withExpiredExpiry(user().savedCards[0].id, user().with { new User(it.mobile, it.password) })
        root.CUSTID = null
        root.SSO_TOKEN = user().tokens['sso'].id
        req().body(root).post().then()
                .spec(noContentSchema)
    }

    @Override
    @Merchant(edit = true, value = { it.preferences.saveCard.enabled == true })
    @Test
    void 'test able to fetch saved card when VISA card is saved on custId and mId'() {
        def root = root()
        (m() as AlipayMerchant).users[0].savedCards.clear()
        assert (m() as AlipayMerchant).users[0].savedCards.add(cards.find { it.scheme == 'visa' })
        root.CUSTID = m().users[0].id
        root.SSO_TOKEN = null
        req().body(root).post().then()
                .spec(successSchema)
                .body('response[0].savedCardId', equalTo(m().users[0].savedCards[0].id))
    }

    @Override
    @Merchant
    @AUser(edit = true)
    @Test
    void 'test able to fetch saved card when VISA card is saved on userId'() {
        def root = root()
        (user() as AlipayUser).savedCards.clear()
        assert (user() as AlipayUser).savedCards.add(cards.find { it.scheme == 'visa' })
        root.CUSTID = null
        root.SSO_TOKEN = user().tokens['sso'].id
        req().body(root).post().then()
                .spec(successSchema)
                .body('response[0].savedCardId', equalTo((user() as AlipayUser).savedCards[0].idxNo))
    }

    @Override
    @Merchant(edit = true, value = { it.preferences.saveCard.enabled == true })
    @Test
    void 'test able to fetch saved card when MASTER card is saved on custId and mId'() {
        def root = root()
        (m() as AlipayMerchant).users[0].savedCards.clear()
        assert (m() as AlipayMerchant).users[0].savedCards.add(cards.find { it.scheme == 'master' })
        root.CUSTID = m().users[0].id
        root.SSO_TOKEN = null
        req().body(root).post().then()
                .spec(successSchema)
                .body('response[0].savedCardId', equalTo(m().users[0].savedCards[0].id))
    }

    @Override
    @Merchant
    @AUser(edit = true)
    @Test
    void 'test able to fetch saved card when MASTER card is saved on userId'() {
        def root = root()
        (user() as AlipayUser).savedCards.clear()
        assert (user() as AlipayUser).savedCards.add(cards.find { it.scheme == 'master' })
        root.CUSTID = null
        root.SSO_TOKEN = user().tokens['sso'].id
        req().body(root).post().then()
                .spec(successSchema)
                .body('response[0].savedCardId', equalTo((user() as AlipayUser).savedCards[0].idxNo))
    }

    @Override
    @Merchant(edit = true, value = { it.preferences.saveCard.enabled == true && it.acquirings.any { it.bank == 'amex' } })
    @Test
    void 'test able to fetch saved card when AMEX card is saved on custId and mId'() {
        def root = root()
        (m() as AlipayMerchant).users[0].savedCards.clear()
        assert (m() as AlipayMerchant).users[0].savedCards.add(cards.find { it.scheme == 'amex' })
        root.CUSTID = m().users[0].id
        root.SSO_TOKEN = null
        req().body(root).post().then()
                .spec(successSchema)
                .body('response[0].savedCardId', equalTo(m().users[0].savedCards[0].id))
    }

    @Override
    @Merchant({ it.acquirings.any { it.bank == 'amex' } && it.preferences.saveCard.enabled == true })
    @AUser(edit = true)
    @Test
    void 'test able to fetch saved card when AMEX card is saved on userId'() {
        def root = root()
        (user() as AlipayUser).savedCards.clear()
        assert (user() as AlipayUser).savedCards.add(cards.find { it.scheme == 'amex' })
        root.CUSTID = null
        root.SSO_TOKEN = user().tokens['sso'].id
        req().body(root).post().then()
                .spec(successSchema)
                .body('response[0].savedCardId', equalTo((user() as AlipayUser).savedCards[0].idxNo))
    }

    @Override
    @Merchant(edit = true, value = { it.preferences.saveCard.enabled == true })
    @Test
    void 'test able to fetch saved card when DINERS card is saved on custId and mId'() {
        def root = root()
        (m() as AlipayMerchant).users[0].savedCards.clear()
        assert (m() as AlipayMerchant).users[0].savedCards.add(cards.find { it.scheme == 'diners' })
        root.CUSTID = m().users[0].id
        root.SSO_TOKEN = null
        req().body(root).post().then()
                .spec(successSchema)
                .body('response[0].savedCardId', equalTo(m().users[0].savedCards[0].id))
    }

    @Override
    @Merchant
    @AUser(edit = true)
    @Test
    void 'test able to fetch saved card when DINERS card is saved on userId'() {
        def root = root()
        (user() as AlipayUser).savedCards.clear()
        assert (user() as AlipayUser).savedCards.add(cards.find { it.scheme == 'diners' })
        root.CUSTID = null
        root.SSO_TOKEN = user().tokens['sso'].id
        req().body(root).post().then()
                .spec(successSchema)
                .body('response[0].savedCardId', equalTo((user() as AlipayUser).savedCards[0].idxNo))
    }

    @Override
    @Merchant(edit = true, value = { it.preferences.saveCard.enabled == true })
    @Test
    void 'test able to fetch saved card when MAESTRO card is saved on custId and mId'() {
        def root = root()
        (m() as AlipayMerchant).users[0].savedCards.clear()
        assert (m() as AlipayMerchant).users[0].savedCards.add(cards.find { it.scheme == 'maestro' })
        root.CUSTID = m().users[0].id
        root.SSO_TOKEN = null
        req().body(root).post().then()
                .spec(successSchema)
                .body('response[0].savedCardId', equalTo(m().users[0].savedCards[0].id))
    }

    @Override
    @Merchant
    @AUser(edit = true)
    @Test
    void 'test able to fetch saved card when MAESTRO card is saved on userId'() {
        def root = root()
        (user() as AlipayUser).savedCards.clear()
        assert (user() as AlipayUser).savedCards.add(cards.find { it.scheme == 'maestro' })
        root.CUSTID = null
        root.SSO_TOKEN = user().tokens['sso'].id
        req().body(root).post().then()
                .spec(successSchema)
                .body('response[0].savedCardId', equalTo((user() as AlipayUser).savedCards[0].idxNo))
    }

    @Override
    @Merchant(edit = true, value = { it.preferences.saveCard.enabled == true })
    @Test
    void 'test able to fetch saved card when RUPAY card is saved on custId and mId'() {
        def root = root()
        (m() as AlipayMerchant).users[0].savedCards.clear()
        assert (m() as AlipayMerchant).users[0].savedCards.add(cards.find { it.scheme == 'rupay' })
        root.CUSTID = m().users[0].id
        root.SSO_TOKEN = null
        req().body(root).post().then()
                .spec(successSchema)
                .body('response[0].savedCardId', equalTo(m().users[0].savedCards[0].id))
    }

    @Override
    @Merchant
    @AUser(edit = true)
    @Test
    void 'test able to fetch saved card when RUPAY card is saved on userId'() {
        def root = root()
        (user() as AlipayUser).savedCards.clear()
        assert (user() as AlipayUser).savedCards.add(cards.find { it.scheme == 'rupay' })
        root.CUSTID = null
        root.SSO_TOKEN = user().tokens['sso'].id
        req().body(root).post().then()
                .spec(successSchema)
                .body('response[0].savedCardId', equalTo((user() as AlipayUser).savedCards[0].idxNo))
    }

    @Override
    @Merchant(edit = true, value = { it.preferences.saveCard.enabled == true })
    @Test
    void 'test able to fetch saved card when DISCOVER card is saved on custId and mId'() {
        def root = root()
        (m() as AlipayMerchant).users[0].savedCards.clear()
        assert (m() as AlipayMerchant).users[0].savedCards.add(cards.find { it.scheme == 'discover' })
        root.CUSTID = m().users[0].id
        root.SSO_TOKEN = null
        req().body(root).post().then()
                .spec(successSchema)
                .body('response[0].savedCardId', equalTo(m().users[0].savedCards[0].id))
    }

    @Override
    @Merchant
    @AUser(edit = true)
    @Test
    void 'test able to fetch saved card when DISCOVER card is saved on userId'() {
        def root = root()
        (user() as AlipayUser).savedCards.clear()
        assert (user() as AlipayUser).savedCards.add(cards.find { it.scheme == 'discover' })
        root.CUSTID = null
        root.SSO_TOKEN = user().tokens['sso'].id
        req().body(root).post().then()
                .spec(successSchema)
                .body('response[0].savedCardId', equalTo((user() as AlipayUser).savedCards[0].idxNo))
    }

    @Override
    @Merchant(edit = true, value = { it.preferences.saveCard.enabled == true && it.acquirings.any { it.bank == 'bajajfn' } })
    @Test
    void 'test able to fetch saved card when BAJAJFN card is saved on custId and mId'() {
        def root = root()
        (m() as AlipayMerchant).users[0].savedCards.clear()
        assert (m() as AlipayMerchant).users[0].savedCards.add(cards.find { it.scheme == 'bajajfn' })
        root.CUSTID = m().users[0].id
        root.SSO_TOKEN = null
        req().body(root).post().then()
                .spec(emptySchema)
    }

    @Override
    @Merchant({ it.preferences.saveCard.enabled == true && it.acquirings.any { it.bank == 'bajajfn' } })
    @AUser(edit = true)
    @Test
    void 'test able to fetch saved card when BAJAJFN card is saved on userId'() {
        def root = root()
        (user() as AlipayUser).savedCards.clear()
        assert (user() as AlipayUser).savedCards.add(cards.find { it.scheme == 'bajajfn' })
        root.CUSTID = null
        root.SSO_TOKEN = user().tokens['sso'].id
        req().body(root).post().then()
                .spec(emptySchema)
    }

    @Override
    @Merchant(edit = true, value = { it.preferences.saveCard.enabled == true })
    @Test
    void 'test able to fetch saved card when credit card is saved on custId and mId'() {
        def root = root()
        (m() as AlipayMerchant).users[0].savedCards.clear()
        assert (m() as AlipayMerchant).users[0].savedCards.add(cards.find { it.type == 'credit' })
        root.CUSTID = m().users[0].id
        root.SSO_TOKEN = null
        req().body(root).post().then()
                .spec(successSchema)
                .body('response[0].savedCardId', equalTo(m().users[0].savedCards[0].id))
    }

    @Override
    @Merchant
    @AUser(edit = true)
    @Test
    void 'test able to fetch saved card when credit card is saved on userId'() {
        def root = root()
        (user() as AlipayUser).savedCards.clear()
        assert (user() as AlipayUser).savedCards.add(cards.find { it.type == 'credit' })
        root.CUSTID = null
        root.SSO_TOKEN = user().tokens['sso'].id
        req().body(root).post().then()
                .spec(successSchema)
                .body('response[0].savedCardId', equalTo((user() as AlipayUser).savedCards[0].idxNo))
    }

    @Override
    @Merchant(edit = true, value = { it.preferences.saveCard.enabled == true })
    @Test
    void 'test able to fetch saved card when debit card is saved on custId and mId'() {
        def root = root()
        (m() as AlipayMerchant).users[0].savedCards.clear()
        assert (m() as AlipayMerchant).users[0].savedCards.add(cards.find { it.type == 'debit' })
        root.CUSTID = m().users[0].id
        root.SSO_TOKEN = null
        req().body(root).post().then()
                .spec(successSchema)
                .body('response[0].savedCardId', equalTo(m().users[0].savedCards[0].id))
    }

    @Override
    @Merchant
    @AUser(edit = true)
    @Test
    void 'test able to fetch saved card when debit card is saved on userId'() {
        def root = root()
        (user() as AlipayUser).savedCards.clear()
        assert (user() as AlipayUser).savedCards.add(cards.find { it.type == 'debit' })
        root.CUSTID = null
        root.SSO_TOKEN = user().tokens['sso'].id
        req().body(root).post().then()
                .spec(successSchema)
                .body('response[0].savedCardId', equalTo((user() as AlipayUser).savedCards[0].idxNo))
    }

    @Merchant(edit = true, value = { it.preferences.saveCard.enabled == true })
    @AUser(edit = true)
    @Test
    void 'test able to fetch saved card when CUSTID and SSO_TOKEN are provided given card is saved on custId and mId but not on userId'() {
        def root = root()
        (m() as AlipayMerchant).users[0].savedCards.clear()
        (user() as AlipayUser).savedCards.clear()
        assert (m() as AlipayMerchant).users[0].savedCards.add(cards.find())
        root.CUSTID = m().users[0].id
        root.SSO_TOKEN = user().tokens['sso'].id
        req().body(root).post().then()
                .spec(successSchema)
                .body('response', hasSize(1))
    }

    @Merchant(edit = true, value = { it.preferences.saveCard.enabled == true })
    @AUser(edit = true)
    @Test
    void 'test able to fetch saved card when CUSTID and SSO_TOKEN are provided given card is saved on userId but not on custId and mId'() {
        def root = root()
        (m() as AlipayMerchant).users[0].savedCards.clear()
        (user() as AlipayUser).savedCards.clear()
        assert (user() as AlipayUser).savedCards.add(cards.find())
        root.CUSTID = m().users[0].id
        root.SSO_TOKEN = user().tokens['sso'].id
        req().body(root).post().then()
                .spec(successSchema)
                .body('response', hasSize(1))
    }

    @Override
    @Merchant(edit = true, value = {  it.acquirings.any { it.payMode == 'dc' && !(it.bank in ['bajajfn', 'amex']) } && it.acquirings.any { it.payMode == 'cc' && !(it.bank in ['bajajfn', 'amex']) } && it.acquirings.any { it.bank == 'amex' } && it.acquirings.any { it.bank == 'bajajfn' } && it.preferences.saveCard.enabled == true })
    @Test
    void 'test able to fetch max 10 saved card(s) when greater than 10 cards are saved on custId and mId'() {
        def root = root()
        (m() as AlipayMerchant).users[0].savedCards.clear()
        if (!(m() as AlipayMerchant).users[0].savedCards.addAll(cards[0..10])) throw new SkipException('unable to save more than 10 cards')
        root.CUSTID = m().users[0].id
        root.SSO_TOKEN = null
        req().body(root).post().then()
                .spec(successSchema)
                .body('response', hasSize(10))
    }

    @Override
    @Merchant({ it.acquirings.any { it.payMode == 'dc' && !(it.bank in ['bajajfn', 'amex']) } && it.acquirings.any { it.payMode == 'cc' && !(it.bank in ['bajajfn', 'amex']) } && it.acquirings.any { it.bank == 'amex' } && it.acquirings.any { it.bank == 'bajajfn' } && it.preferences.saveCard.enabled == true })
    @AUser(edit = true)
    @Test
    void 'test able to fetch max 10 saved card(s) when greater than 10 cards are saved on userId'() {
        def root = root()
        (user() as AlipayUser).savedCards.clear()
        if (!(user() as AlipayUser).savedCards.addAll(cards[0..10])) throw new SkipException('unable to save more than 10 cards')
        root.CUSTID = null
        root.SSO_TOKEN = user().tokens['sso'].id
        req().body(root).post().then()
                .spec(successSchema)
                .body('response', hasSize(10))
    }

    @Merchant(edit = true, value = { it.preferences.saveCard.enabled == true })
    @AUser(edit = true)
    @Test
    void 'test able to fetch saved card(s) when cards are saved on userId and custId and mId'() {
        def root = root()
        (user() as AlipayUser).savedCards.clear()
        (m() as AlipayMerchant).users[0].savedCards.clear()
        def card1 = cards.find()
        def card2 = cards.find { it.no != card1.no }
        if (!(m() as AlipayMerchant).users[0].savedCards.add(card1)) throw new SkipException('unable to save card on custId and mId')
        if (!(user() as AlipayUser).savedCards.add(card2)) throw new SkipException('unable to save card on userId')
        root.CUSTID = m().users[0].id
        root.SSO_TOKEN = user().tokens['sso'].id
        req().body(root).post().then()
                .spec(successSchema)
                .body('response', hasSize(2))
    }

    @Merchant(edit = true, value = { it.preferences.saveCard.enabled == true })
    @Test
    void 'test cardLastFourDigits in response is as expected when last 4th digit of card num is 0'() {
        def root = root()
        (m() as AlipayMerchant).users[0].savedCards.clear()
        def card = cards.find { it.no[-4] == '0' }
        assert (m() as AlipayMerchant).users[0].savedCards.add(card)
        root.CUSTID = m().users[0].id
        root.SSO_TOKEN = null
        req().body(root).post().then()
                .spec(successSchema)
                .body('response.cardLastFourDigits', everyItem(equalTo(card.no[-4..-1])))
    }

    @Merchant(edit = true, value = { it.preferences.saveCard.enabled == true })
    @AUser(edit = true)
    @Test
    void 'test able to fetch saved card(s) when same card is saved on both custId & mId and userId'() {
        def root = root()
        (m() as AlipayMerchant).users[0].savedCards.clear()
        (user() as AlipayUser).savedCards.clear()
        cards.find().with {
            assert (m() as AlipayMerchant).users[0].savedCards.add(it)
            assert (user() as AlipayUser).savedCards.add(it)
        }
        root.CUSTID = m().users[0].id
        root.SSO_TOKEN = user().tokens['sso'].id
        req().body(root).post().then()
                .spec(successSchema)
                .body('response', hasSize(1))
    }

    @Override
    @Merchant(value = { !it.acquirings.any { it.bank == 'amex' } && it.preferences.saveCard.enabled == true }, edit = true)
    @AUser(edit = true)
    @Test
    void 'test unable to fetch saved card(s) when card is saved on custId & mId given corresponding acquiring is not configured on merchant'() {
        def root = root()
        (m() as AlipayMerchant).users[0].savedCards.clear()
        assert (m() as AlipayMerchant).users[0].savedCards.add(cards.find { it.scheme == 'amex' })
        root.CUSTID = m().users[0].id
        root.SSO_TOKEN = null
        req().body(root).post().then()
                .spec(emptySchema)
    }

    @Merchant({ !it.acquirings.any { it.bank == 'amex' } })
    @AUser(edit = true)
    @Test
    void 'test unable to fetch saved card(s) when card is saved on userId given corresponding acquiring is not configured on merchant'() {
        def root = root()
        (user() as AlipayUser).savedCards.clear()
        assert (user() as AlipayUser).savedCards.add(cards.find { it.scheme == 'amex' })
        root.CUSTID = null
        root.SSO_TOKEN = user().tokens['sso'].id
        req().body(root).post().then()
                .spec(emptySchema)
    }
}
