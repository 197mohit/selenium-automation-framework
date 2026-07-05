package scripts.api.theia.fetchBinDetail

import com.paytm.base.test.Group
import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.annotations.AUser
import io.qameta.allure.Issue
import io.qameta.allure.Owner
import io.restassured.builder.RequestSpecBuilder
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.testng.annotations.Test
import com.paytm.appconstants.Constants
import static org.hamcrest.Matchers.*

@Owner('Deepak')
abstract class FetchBinDetailTest extends TestSetUp {

    private static final String LANGUAGE_HEADER = 'X-accept-language'
    private static final String LANGUAGE_HEADER_VALUE = 'hi-IN'

    abstract RequestSpecBuilder reqBldr();

    abstract RequestSpecification req();

    abstract Map root();

    @AUser
    @Merchant(edit = true, value = {it.id == Constants.MerchantType.ZEST_MONEY.getId()})
    @Test(groups = 'paymentMode', description = 'test for pay mode dc when supported')
    final void testForPayModeDCWhenSupported() {
        def root = root()
        root.body.paymentMode = 'DC'
        root.body.bin = cards.find { it.type == 'debit' && it.scheme != 'amex' && !it.prepaid }.no[0..5]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.binDetail.paymentMode', equalTo('DEBIT_CARD'))
    }

    @AUser
    @Merchant(edit = true, value = {it.id == Constants.MerchantType.BANK_TRANSFER_MERCH.getId() })
    @Test(groups = 'paymentMode', description = 'test for pay mode dc when not supported')
    final void testForPayModeDCWhenNotSupported() {
        def root = root()
        def card = cards.find { it.type == 'debit' && !it.prepaid }
        root.body.paymentMode = 'DC'
        root.body.bin = card.no[0..5]
        req().body(root).post().then()
                .root('body.resultInfo')
                .body('resultStatus', equalTo('F'),
                'resultCode', equalTo('2011'),
                'resultMsg', equalToIgnoringCase("$card.scheme Debit card is not allowed for DC payment. Please try paying using other cards/options."))
    }

//    @AUser
//    @Merchant(edit = true, value = { !!it.acquirings.any { it.payMode == 'dc' && it.bank != 'amex' } && !it.pcfEnabled })
//    @Test(enabled = false, description = 'test when bin not active and pay mode not supported')
//TODO need to implement setActive() in utils
    final void testWhenBinNotActiveAndPayModeNotSupported() {
        def root = root()
        def card = cards.find { it.type == 'debit' && it.scheme != 'amex' && !it.prepaid }
        assert card.setActive(false)
        root.body.paymentMode = 'DC'
        root.body.bin = card.no[0..5]
        req().body(root).post().thenReturn()
        assert card.setActive(true)
    }

    @AUser
    @Merchant(edit = true, value = {it.id == Constants.MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getId()})
    @Test(groups = 'paymentMode', description = 'test for pay mode cc when supported')
    final voidtestForPayModeCCWhenSupported() {
        def root = root()
        root.body.paymentMode = 'CC'
        root.body.bin = cards.find { it.type == 'credit' && !(it.scheme in ['amex', 'bajajfn'] && !it.prepaid) }.no[0..5]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.binDetail.paymentMode', equalTo('CREDIT_CARD'))
    }

    @AUser
    @Merchant(edit = true, value = {it.id == Constants.MerchantType.WalletOnly.getId()})
    @Test(groups = 'paymentMode', description = 'test for pay mode cc when not supported')
    final void testForPayModeCCWhenNotSupported() {
        def root = root()
        root.body.paymentMode = 'CC'
        def card = cards.find { it.type == 'credit' && !it.prepaid }
        root.body.bin = card.no[0..5]
        req().body(root).post().then()
                .root('body.resultInfo')
                .body('resultStatus', equalTo('F'),
                'resultCode', equalTo('2011'),
                'resultMsg', equalToIgnoringCase("$card.scheme Credit card is not allowed for CC payment. Please try paying using other cards/options."))
    }

    @Issue('PGP-20131')
    @AUser
    @Merchant(edit = true, value = { it.acquirings.any { it.payMode == 'emi' && it.bank == 'bajajfn' } && !it.pcfEnabled })
    @Test( description = 'test for pay mode emi when supported')
    final void testForPayModeEMIWhenSupported() {
        def root = root()
        root.body.paymentMode = 'EMI'
        root.body.bin = cards.find { it.scheme == 'bajajfn' && !it.prepaid }.no[0..5]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.binDetail.paymentMode', equalTo('EMI'))
    }

//    @AUser
//    @Merchant(edit = true, value = { !it.acquirings.any { it.payMode == 'emi' } && !it.pcfEnabled })
//    @Test(groups = 'paymentMode', enabled = false, description = 'test for pay mode emi when not supported')
//TODO need to check correct res
    final void testForPayModeEMIWhenNotSupported() {
        def root = root()
        root.body.paymentMode = 'EMI'
        root.body.bin = cards.find { it.type == 'credit' && !it.prepaid }.no[0..5]
        req().body(root).post().thenReturn()
    }

    @AUser
    @Merchant({it.id == Constants.MerchantType.PGOnly_Retry.getId()})
    @Test(groups = 'bin.scheme', description = 'test for non amex bajajfn bin when supported')
    final void testForNonAmexBajajfnBinWhenSupported() {
        def root = root()
        def card = cards.find { !(it.scheme in ['amex', 'bajajfn']) && it.type == 'credit' && !it.prepaid }
        root.body.paymentMode = 'CC'
        root.body.bin = "444433"
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.binDetail.channelName', equalToIgnoringCase("VISA"),
                'body.binDetail.channelCode', equalToIgnoringCase("VISA"))
    }

    @AUser
    @Merchant({it.id == Constants.MerchantType.WalletOnly.getId()})
    @Test(groups = 'bin.scheme', description = 'test for non amex bajajfn bin when not supported')
    final void testForNonAmexBajajfnBinWhenNotSupported() {
        def root = root()
        def card = cards.find { !(it.scheme in ['amex', 'bajajfn']) && it.type == 'credit' && !it.prepaid }
        root.body.paymentMode = 'CC'
        root.body.bin = card.no[0..5]
        req().body(root).post().then()
                .root('body.resultInfo')
                .body('resultStatus', equalTo('F'),
                'resultCode', equalTo('2011'),
                'resultMsg', equalToIgnoringCase("$card.scheme Credit card is not allowed for CC payment. Please try paying using other cards/options."))
    }

//    @AUser
//    @Merchant({it.id == Constants.MerchantType.BRAND_BO_DISC_BAJAJ.getId()})
//    @Test(groups = 'bin.scheme', description = 'test for amex bin when supported',enabled  =false)
    final void testForAmexBinWhenSupported() {
        def root = root()
        def card = cards.find { it.scheme == 'amex' && it.type == 'credit' && !it.prepaid }
        root.body.paymentMode = 'CC'
        root.body.bin = card.no[0..5]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.binDetail.channelName', equalToIgnoringCase(card.scheme),
                'body.binDetail.channelCode', equalToIgnoringCase(card.scheme))
    }

//    @AUser
//    @Merchant(edit = true, value = {
//        it.acquirings.any { it.payMode == 'cc' && it.bank == 'amex' } &&
//                !it.acquirings.any { it.payMode == 'emi' && it.bank == 'amex' } && !it.pcfEnabled })
//    @Test(groups = 'bin.scheme', enabled = false, description = 'test for amex bin emi when amex cc supported but not amex emi')
//TODO need to find correct res
    final void testForAmexBinEmiWhenAmexCCSupportedButNotAmexEmi() {
        def root = root()
        root.body.paymentMode = 'EMI'
        root.body.bin = cards.find { it.scheme == 'amex' && it.type == 'credit' && !it.prepaid }.no[0..5]
        req().body(root).post().thenReturn()
    }

//    @AUser
//    @Merchant(edit = true, value = {
//        it.acquirings.any { it.payMode == 'emi' && it.bank == 'amex' } &&
//                !it.acquirings.any { it.payMode == 'cc' && it.bank == 'amex' } && !it.pcfEnabled })
//    @Test(groups = 'bin.scheme', enabled = false, description = 'test for amex bin cc when amex emi supported but not amex cc')
//TODO need to find correct res
    final void testForAmexBinCCWhenAmexEmiSupportedButNotAmexCC() {
        def root = root()
        root.body.paymentMode = 'CC'
        root.body.bin = cards.find { it.scheme == 'amex' && it.type == 'credit' && !it.prepaid }.no[0..5]
        req().body(root).post().thenReturn()
    }

    @AUser
    @Merchant({it.id == Constants.MerchantType.WalletOnly.getId()})
    @Test(groups = 'bin.scheme', description = 'test for amex bin when not supported')
    final void testForAmexBinWhenNotSupported() {
        def root = root()
        def card = cards.find { it.scheme == 'amex' && it.type == 'credit' && !it.prepaid }
        root.body.paymentMode = 'CC'
        root.body.bin = "379863"
        req().body(root).post().then()
                .root('body.resultInfo')
                .body('resultStatus', equalTo('F'),
                'resultCode', equalTo('2011'),
                'resultMsg', equalToIgnoringCase("$card.scheme Credit card is not allowed for CC payment. Please try paying using other cards/options."))
    }

    @Issue('PGP-20131')
    @AUser
    @Merchant(edit = true, value = { it.acquirings.any { it.payMode == 'emi' && it.bank == 'bajajfn' } && !it.pcfEnabled })
    @Test(groups = ['bin.scheme', Group.Status.BUG], description = 'test for bajajfn bin when supported')
    final void testForBajajfnBinWhenSupported() {
        def root = root()
        def card = cards.find { it.scheme == 'bajajfn' && it.type == 'credit' && !it.prepaid }
        root.body.paymentMode = 'EMI'
        root.body.bin = card.no[0..5]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.binDetail.channelName', equalToIgnoringCase(card.scheme),
                'body.binDetail.channelCode', equalToIgnoringCase(card.scheme))
    }

//    @AUser
//    @Merchant(edit = true, value = { !it.acquirings.any { it.payMode == 'emi' && it.bank == 'bajajfn' } && !it.pcfEnabled })
//    @Test(groups = 'bin.scheme', enabled = false, description = 'test for bajajfn bin when not supported')
//TODO need to find correct res
    final void testForBajajfnBinWhenNotSupported() {
        def root = root()
        root.body.paymentMode = 'EMI'
        root.body.bin = cards.find { it.scheme == 'bajajfn' && it.type == 'credit' && !it.prepaid }.no[0..5]
        req().body(root).post().thenReturn()
    }

    @AUser
    @Merchant({it.id == Constants.MerchantType.BAJAJFINEMI.getId()})
//using double negation as a hint to test reader that it is just a basic pre-requisite that should atleast be met to support tc run and it is not directly related to variable under test
    @Test(groups = 'bin.length', description = 'test for 6 bin')
    final void testFor6Bin() {
        def root = root()
        root.body.paymentMode = 'DC'
        root.body.bin = cards.find { it.type == 'debit' && it.scheme != 'amex' && !it.prepaid }.no[0..5]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @AUser
    @Merchant({it.id == Constants.MerchantType.PGOnly.getId()})
//using double negation as a hint to test reader that it is just a basic pre-requisite that should atleast be met to support tc run and it is not directly related to variable under test
    @Test(groups = 'bin.length', description = 'test for 8 bin')
    final void testFor8Bin() {
        def root = root()
        root.body.paymentMode = 'DC'
        root.body.bin = cards.find { it.type == 'debit' && it.scheme != 'amex' && !it.prepaid }.no[0..7]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @AUser
    @Merchant({it.id == Constants.MerchantType.REFUND_IMPSHYBRID.getId()})
    @Test(groups = 'bin.type', description = 'test for cc bin')
    final void testForCCBin() {
        def root = root()
        root.body.paymentMode = 'CC'
        //root.body.bin = cards.find { it.type == 'credit' && it.scheme != 'amex' && !it.prepaid }.no[0..5]
        root.body.bin ="471865";
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @AUser
    @Merchant(edit = true, value = {it.id == Constants.MerchantType.ADD_MONEY_HDFC.getId()})
    @Test(groups = 'bin.type', description = 'test for dc bin')
    final void testForDCBin() {
        def root = root()
        root.body.paymentMode = 'DC'
        root.body.bin = cards.find { it.type == 'debit' && it.scheme != 'amex' && !it.prepaid }.no[0..5]
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
    }

    @AUser
    @Merchant(edit = true, value = {it.id == Constants.MerchantType.PPBL_PAYTMCC_VPA.getId()})
    @Test(description = 'test param isEmiAvailable when emi available on bin')
    final void testParamIsEmiAvailableWhenEmiAvailableOnBin() {
        def root = root()
        root.body.paymentMode = 'CC'
        root.body.bin = cards.find { it.scheme == 'amex' && it.type == 'credit' && !it.prepaid }.no[0..5]
        req().body(root).post().then()
                .body('body.isEmiAvailable', equalTo(true))
    }

//    @AUser
//    @Merchant(edit = true, value = {
//        !it.acquirings.any { it.payMode == 'emi' && it.bank == 'amex' } && it.acquirings.any {
//            it.payMode == 'cc' && it.bank == 'amex'
//        } && !it.pcfEnabled })
//    @Test(description = 'test param isEmiAvailable when emi not available on bin', enabled = false)
//TODO need to check correct behaviour
    final void testParamIsEmiAvailableWhenEmiNotAvailableOnBin() {
        def root = root()
        root.body.paymentMode = 'CC'
        root.body.bin = cards.find { it.scheme == 'amex' && it.type == 'credit' && !it.prepaid }.no[0..5]
        req().body(root).post().then()
                .body('body.isEmiAvailable', equalTo(false))
    }

    @AUser
    @Merchant(edit = true, value = {it.id == Constants.MerchantType.EMI_DC.getId()})
    @Test(description = 'test when bin has high success rate')
    final void testWhenBinHasHighSuccessRate() {
        def root = root()
        root.body.paymentMode = 'CC'
        root.body.bin="471865";
//        root.body.bin = cards.find {
//            !(it.scheme in ['amex', 'bajajfn']) && it.type == 'credit' && it.successRate == 'high' && !it.prepaid
//        }.no[0..5]
        req().body(root).post().then()
                .root('body.hasLowSuccessRate')
                .body('status', equalTo('false'),
                'msg', equalTo(''))
    }

//    @AUser
//    @Merchant(edit = true, value = { !!it.acquirings.any { it.payMode == 'cc' && it.bank != 'amex' } && !it.pcfEnabled })
//    @Test(enabled = false, description = 'test when bin has low success rate')
//TODO need to add functionality to make a card low success
    final void testWhenBinHasLowSuccessRate() {
        def root = root()
        root.body.paymentMode = 'CC'
        root.body.bin = cards.find { it.scheme != 'amex' && it.type == 'credit' && it.successRate == 'low' && !it.prepaid }.no[0..5]
        req().body(root).post().then()
                .root('body.hasLowSuccessRate')
                .body('status', equalTo('true'),
                'msg', equalTo(''))
    }

//    @AUser
//    @Merchant(edit = true, value = { it.oneClickSupported == true && !it.pcfEnabled })
//    @Test(description = 'test when one click option supported on bin', enabled = false)
    final void testWhenOneClickOptionSupportedOnBin() {
        //TODO Never got passed
        def root = root()
        root.body.paymentMode = 'CC'
        root.body.bin = cards.find { it.oneClickSupported && it.type == 'credit' && it.scheme == 'visa' && !it.prepaid }.no[0..5]
        req().body(root).post().then()
                .body('body.oneClickSupported', equalTo(true))
    }

    @AUser
    @Merchant(edit = true, value = {it.id == Constants.MerchantType.BRAND_BO_DISC_BAJAJ_HDFC.getId() })
    @Test(description = 'test when one click option not supported on bin')
    final void testWhenOneClickOptionNotSupportedOnBin() {
        def root = root()
        root.body.paymentMode = 'DC'
        root.body.bin = cards.find { !it.oneClickSupported && it.type == 'debit' && it.scheme != 'amex' && !it.prepaid }.no[0..5]
        req().body(root).post().then()
                .body('body.oneClickSupported', equalTo(false))
    }

    @AUser
    @Merchant({it.id == Constants.MerchantType.ZEST_MONEY.getId()})
    @Test(description = 'test bin in res is same as in req')
    final void testBinInResIsSameAsInReq() {
        def root = root()
        root.body.paymentMode = 'DC'
        root.body.bin = cards.find { it.type == 'debit' && it.scheme != 'amex' && !it.prepaid }.no[0..5]
        req().body(root).post().then()
                .body('body.binDetail.bin', equalTo(root.body.bin))
    }

    @Merchant({it.id == Constants.MerchantType.PGOnly_Retry.getId()})
    @AUser
    @Test
    final void 'test localisation fields are not present when localisation is disabled on merchant but language header is passed'() {
        def root = root()
        req().header(LANGUAGE_HEADER, LANGUAGE_HEADER_VALUE).body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.resultInfo', not(hasKey('resultMsgRegional')),
                        'body.binDetail', not(hasKey('channelNameRegional')),
                        'body.binDetail', not(hasKey('issuingBankRegional')),
                        'body', not(hasKey('errorMessageRegional')),
                        'body.hasLowSuccessRate', not(hasKey('msgRegional')))
    }

    @Merchant({it.id == Constants.MerchantType.Seamless_Hybrid_Offus.getId()})
    @AUser
    @Test
    final void 'test localisation fields are not present when localisation is enabled on merchant but language header is not passed'() {
        def root = root()
        req().body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.resultInfo', not(hasKey('resultMsgRegional')),
                        'body.binDetail', not(hasKey('channelNameRegional')),
                        'body.binDetail', not(hasKey('issuingBankRegional')),
                        'body', not(hasKey('errorMessageRegional')),
                        'body.hasLowSuccessRate', not(hasKey('msgRegional')))
    }

    @Merchant({it.id == Constants.MerchantType.Seamless_Hybrid_Offus.getId()})
    @AUser
    @Test
    final void 'test localisation fields are present when localisation is enabled on merchant and language header is passed'() {
        def root = root()
        req().header(LANGUAGE_HEADER, LANGUAGE_HEADER_VALUE).body(root).post().then()
                .spec(results.success as ResponseSpecification)
                .body('body.resultInfo', (hasKey('resultMsgRegional')),
                        'body.binDetail', (hasKey('channelNameRegional')),
                        'body.binDetail', (hasKey('issuingBankRegional')),
                        'body', (hasKey('errorMessageRegional')),
                        'body.hasLowSuccessRate', (hasKey('msgRegional')))
    }
}
