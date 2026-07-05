package scripts

import com.paytm.api.TxnStatus
import com.paytm.appconstants.Constants
import com.paytm.apphelpers.SavedCardHelpers
import com.paytm.apphelpers.WalletHelpers
import com.paytm.base.test.TestSetUp
import com.paytm.base.test.User
import com.paytm.dto.OrderDTO
import com.paytm.dto.OrderFactory
import com.paytm.dto.PaymentDTO
import com.paytm.framework.conditions.SoftAssertion
import com.paytm.pages.CashierPage
import com.paytm.pages.CashierPageFactory
import com.paytm.pages.CheckoutPage
import com.paytm.pages.responsePage.ResponsePage
import com.paytm.utils.merchant.Peon
import com.paytm.utils.merchant.merchant.util.annotations.Merchant
import com.paytm.utils.merchant.user.Card
import com.paytm.utils.merchant.user.annotations.AUser
import com.paytm.utils.merchant.util.PayMethodType
import io.qameta.allure.Epic
import io.qameta.allure.Epics
import io.qameta.allure.Feature
import io.qameta.allure.Features
import io.qameta.allure.Owner
import io.restassured.specification.ResponseSpecification
import org.testng.annotations.Test
import scripts.api.merchantStatus.GetPaymentStatus
import scripts.api.theia.InitiateTransaction

import static com.paytm.base.test.Group.Status.TO_BE_FIXED
import static com.paytm.pages.responsePage.ResponsePage.*
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasKey
import static org.hamcrest.Matchers.not

@Owner("Deepak")
@Epics([@Epic(Constants.Sprint.SPRINT31_2), @Epic(Constants.Sprint.SPRINT32_2)])
@Features([@Feature('PGP-19648'), @Feature('PGP-21143')])
class BinInResponseV1 extends TestSetUp {

    private final InitiateTransaction initTxn = new InitiateTransaction()
    private final CheckoutPage checkoutPage = new CheckoutPage()
    private final ResponsePage responsePage = new ResponsePage()
    private final GetPaymentStatus getPaymentStatus = new GetPaymentStatus()
    private static final List<Constants.MerchantType> NON_ALLOWED_MERCHANTS = [Constants.MerchantType.PGOnly, Constants.MerchantType.Hybrid, Constants.MerchantType.NATIVE_PROMO_HYBRID]

    @Merchant({ !it.preferences.binInResponse.enabled && it.payModes.contains('dc') && !(it.id in NON_ALLOWED_MERCHANTS.collect { it.id }) })
    @AUser(edit = true)
    @Test
    void testWhenBinInResponsePrefIsDisabled() {
        Card card = cards.find { !(it.scheme in ['amex', 'bajajfn']) && it.type == 'debit' }.tap { assert it }
        def root = initTxn.root()
        root.body.txnAmount.value = '2'
        root.body.paytmSsoToken = null
        def token = initTxn.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken')
        OrderDTO order = new OrderDTO.Builder()
                .setORDER_ID(root.body.orderId)
                .setMID(m().id)
                .setCHANNEL_ID('WEB')
                .setTXN_TOKEN(token)
                .setPAYMENT_TYPE_ID(PayMethodType.DEBIT_CARD.toString())
                .setCardInfo("|$card.no|$card.cvv|${card.with { expMo + expYr }}" as String)
                .setPaymentFlow('NONE')
                .build()
        checkoutPage.createNativeOrder(order, false)
        assertion.apply(pageWait.apply(responsePage.hasLoaded()))
        final SoftAssertion sAssert = new SoftAssertion()
        sAssert.apply(
                responsePage.get(Attribute.STATUS).equals('TXN_SUCCESS'),
                responsePage.keys().contains(Attribute.CARD_SCHEME).not()
        )
        sAssert.eval()
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AMEX_PCF.getId() && !(it.id in NON_ALLOWED_MERCHANTS.collect { it.id }) })
    @AUser(edit = true)
    @Test
    void testForAddnPay() {
        assert m().preferences.with { it.add(it.binInResponse) }
        Card card = cards.find { !(it.scheme in ['amex', 'bajajfn']) && it.type == 'credit' }.tap { assert it }
        def root = initTxn.root()
        root.body.paytmSsoToken = user().tokens['sso'].id
        def token = initTxn.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken')
        user().wallets.each { it.balance = 0 }
        OrderDTO order = new OrderDTO.Builder()
                .setORDER_ID(root.body.orderId)
                .setMID(m().id)
                .setCHANNEL_ID('WEB')
                .setTXN_TOKEN(token)
                .setPAYMENT_TYPE_ID(PayMethodType.CREDIT_CARD.toString())
                .setCardInfo("|$card.no|$card.cvv|${card.with { expMo + expYr }}" as String)
                .setPaymentFlow('ADDANDPAY')
                .build()
        checkoutPage.createNativeOrder(order, false)
        assertion.apply(pageWait.apply(responsePage.hasLoaded()))
        final SoftAssertion sAssert = new SoftAssertion()
        sAssert.apply(
                responsePage.get(Attribute.STATUS).equals('TXN_SUCCESS'),
                responsePage.keys().contains(Attribute.CARD_SCHEME).not(),
        )
        sAssert.eval()
        TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID())
        txnStatus.executeUntilNotPending()
        def getPaymentStatusRoot = getPaymentStatus.root()
        getPaymentStatusRoot.body.orderId = order.getORDER_ID()
        getPaymentStatus.req().body(getPaymentStatusRoot).post().then()
                .root("body")
                .body('', not(hasKey('cardScheme')))
        assertion.apply(peonWait.apply({ peons[order.getORDER_ID()] != null }))
        Peon peon = peons.getAt(order.getORDER_ID())
        sAssert.apply(
                peon.keys().contains(Attribute.CARD_SCHEME).not(),
        )
        sAssert.eval()
    }

    @Merchant(edit = true, value = { it.preferences.hybrid.enabled && it.payModes.contains('cc') && it.peonEnabled && !(it.id in NON_ALLOWED_MERCHANTS.collect { it.id }) })
    @AUser(edit = true)
    @Test
    void testForSuccessfulHybridCC() {
        assert m().preferences.with { it.add(it.binInResponse) }
        Card card = cards.find { !(it.scheme in ['amex', 'bajajfn']) && it.type == 'credit' }.tap { assert it }
        def root = initTxn.root()
        root.body.txnAmount.value = '2'
        root.body.paytmSsoToken = user().tokens['sso'].id
        def token = initTxn.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken')
        user().wallets.find { it.name == 'main' }.balance = 1
        user().wallets.findAll { it.name != 'main' }.each { it.balance = 0 }
        OrderDTO order = new OrderDTO.Builder()
                .setORDER_ID(root.body.orderId)
                .setMID(m().id)
                .setCHANNEL_ID('WEB')
                .setTXN_TOKEN(token)
                .setPAYMENT_TYPE_ID(PayMethodType.CREDIT_CARD.toString())
                .setCardInfo("|$card.no|$card.cvv|${card.with { expMo + expYr }}" as String)
                .setPaymentFlow('HYBRID')
                .build()
        checkoutPage.createNativeOrder(order, false)
        assertion.apply(pageWait.apply(responsePage.hasLoaded()))
        final SoftAssertion sAssert = new SoftAssertion()
        sAssert.apply(
                responsePage.get(Attribute.STATUS).equals('TXN_SUCCESS'),
                responsePage.get(Attribute.CHILD_TXN_LIST).contains(Attribute.CARD_SCHEME),
        )
        sAssert.eval()
        TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID())
        txnStatus.executeUntilNotPending()
        def getPaymentStatusRoot = getPaymentStatus.root()
        getPaymentStatusRoot.body.orderId = order.getORDER_ID()
        getPaymentStatus.req().body(getPaymentStatusRoot).post().then()
                .root("body.childTransaction.find { it.paymentMode != 'PPI' }")
                .body('cardScheme', equalTo(card.scheme.toUpperCase()))
        assertion.apply(peonWait.apply({ peons[order.getORDER_ID()] != null }))
        Peon peon = peons.getAt(order.getORDER_ID())
        sAssert.apply(peon.childTxnList().contains('"cardScheme":"' + card.scheme.toUpperCase() + '"'))
        sAssert.eval()
    }

    @Merchant(edit = true, value = { it.id == Constants.MerchantType.AddnPay.getId() && !(it.id in NON_ALLOWED_MERCHANTS.collect { it.id }) })
    @AUser(edit = true)
    @Test
    void testForSuccessfulDC() {
        assert m().preferences.with { it.add(it.binInResponse) }
        Card card = cards.find { !(it.scheme in ['amex', 'bajajfn']) && it.type == 'debit' }.tap { assert it }
        def root = initTxn.root()
        root.body.txnAmount.value = '2'
        root.body.paytmSsoToken = null
        def token = initTxn.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken')
        OrderDTO order = new OrderDTO.Builder()
                .setORDER_ID(root.body.orderId)
                .setMID(m().id)
                .setCHANNEL_ID('WEB')
                .setTXN_TOKEN(token)
                .setPAYMENT_TYPE_ID(PayMethodType.DEBIT_CARD.toString())
                .setCardInfo("|$card.no|$card.cvv|${card.with { expMo + expYr }}" as String)
                .setPaymentFlow('NONE')
                .build()
        checkoutPage.createNativeOrder(order, false)
        assertion.apply(pageWait.apply(responsePage.hasLoaded()))
        final SoftAssertion sAssert = new SoftAssertion()
        sAssert.apply(
                responsePage.get(Attribute.STATUS).equals('TXN_SUCCESS'),
                responsePage.get(Attribute.CARD_SCHEME).equals(card.scheme.toUpperCase())
        )
        sAssert.eval()
        TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID())
        txnStatus.executeUntilNotPending()
        def getPaymentStatusRoot = getPaymentStatus.root()
        getPaymentStatusRoot.body.orderId = order.getORDER_ID()
        getPaymentStatus.req().body(getPaymentStatusRoot).post().then()
                .root("body")
                .body('cardScheme', equalTo(card.scheme.toUpperCase()))
        assertion.apply(peonWait.apply({ peons[order.getORDER_ID()] != null }))
        Peon peon = peons.getAt(order.getORDER_ID())
        sAssert.apply(peon.cardScheme().equals(card.scheme.toUpperCase()))
        sAssert.eval()
    }

    @Merchant(edit = true, value = {
        it.preferences.hybrid.enabled && it.emis.any {
            it.bank.code.equalsIgnoreCase('AMEX') && it.type == 'cc'
        } && it.peonEnabled && !(it.id in NON_ALLOWED_MERCHANTS.collect { it.id })
    })
//    @AUser(edit = true)
//    @Test(enabled = false, groups = [TO_BE_FIXED])
//TODO getting incorrect card no.
    void testForSuccessfulHybridEMICC() {
        assert m().preferences.with { it.add(it.binInResponse) }
        double txnAmt = 2
        Card card = cards.find { it.scheme == 'amex' && it.type == 'credit' }.tap { assert it }
        String planId = m().emis.
                find { it.minAmt <= txnAmt && it.maxAmt >= txnAmt && it.bank.code.equalsIgnoreCase('AMEX') }
                .with { it.bank.code.toUpperCase() + '|' + it.months }
        def root = initTxn.root()
        root.body.txnAmount.value = txnAmt as String
        root.body.paytmSsoToken = user().tokens['sso'].id
        def token = initTxn.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken')
        user().wallets.find { it.name == 'main' }.balance = 1
        user().wallets.findAll { it.name != 'main' }.each { it.balance = 0 }
        OrderDTO order = new OrderDTO.Builder()
                .setORDER_ID(root.body.orderId)
                .setMID(m().id)
                .setCHANNEL_ID('WEB')
                .setTXN_TOKEN(token)
                .setPAYMENT_TYPE_ID(PayMethodType.EMI.toString())
                .setCardInfo("|$card.no|$card.cvv|${card.with { expMo + expYr }}" as String)
                .setPlanId(planId)
                .setPaymentFlow('HYBRID')
                .build()
        checkoutPage.createNativeOrder(order, false)
        assertion.apply(pageWait.apply(responsePage.hasLoaded()))
        final SoftAssertion sAssert = new SoftAssertion()
        sAssert.apply(
                responsePage.get(Attribute.STATUS).equals('TXN_SUCCESS'),
                responsePage.get(Attribute.CHILD_TXN_LIST).contains(Attribute.CARD_SCHEME),
        )
        sAssert.eval()
        TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID())
        txnStatus.executeUntilNotPending()
        def getPaymentStatusRoot = getPaymentStatus.root()
        getPaymentStatusRoot.body.orderId = order.getORDER_ID()
        getPaymentStatus.req().body(getPaymentStatusRoot).post().then()
                .root("body.childTransaction.find { it.paymentMode != 'PPI' }")
                .body('cardScheme', equalTo(card.scheme.toUpperCase()))
        assertion.apply(peonWait.apply({ peons[order.getORDER_ID()] != null }))
        Peon peon = peons.getAt(order.getORDER_ID())
        sAssert.apply(peon.childTxnList().contains('"cardScheme":"' + card.scheme.toUpperCase() + '"'))
        sAssert.eval()
    }

    @Test
    void testForSuccessfulEMIDC() {
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly_Retry
        String theme = 'enhancedweb_revamp'
        assert new com.paytm.utils.merchant.merchant.util.Merchant(merchant.getId(), merchant.getKey(), true).preferences.with { it.add(it.binInResponse) }
        User user = userManager.getForWrite(Label.BASIC)
        SavedCardHelpers.deleteSavedCard(user)
        WalletHelpers.setZeroBalance(user)
        OrderDTO order = new OrderFactory.PGOnly(merchant, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("12")
                .build()
        PaymentDTO paymenDetails = new PaymentDTO().setEmiCard(PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER).setExpMonth('12').setExpYear('40').setBankName("ICICI Bank Debit Card").setMonth(3)
        checkoutPage.createOrder(order)
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme)
        cashierPage.payBy(Constants.PayMode.EMI_DC, paymenDetails)
        assertion.apply(pageWait.apply(responsePage.hasLoaded()))
        final SoftAssertion sAssert = new SoftAssertion()
        sAssert.apply(
                responsePage.get(Attribute.STATUS).equals('TXN_SUCCESS'),
                responsePage.get(Attribute.CARD_SCHEME).equals('MASTER')
        )
        sAssert.eval()
    }

    @Merchant(edit = true, value = {
        it.preferences.hybrid.enabled && it.payModes.contains('cc') && it.peonEnabled && !(it.id in NON_ALLOWED_MERCHANTS.collect { it.id })
    })
    @AUser(edit = true)
    @Test
    void testForSuccessfulHybridSavedCC() {
        assert m().preferences.with { it.add(it.binInResponse) }
        Card card = cards.find { !(it.scheme in ['amex', 'bajajfn']) && it.type == 'credit' }.tap { assert it }
        user().savedCards.clear()
        assert user().savedCards.add(card)
        def root = initTxn.root()
        root.body.txnAmount.value = '2'
        root.body.paytmSsoToken = user().tokens['sso'].id
        def token = initTxn.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken')
        user().wallets.find { it.name == 'main' }.balance = 1
        user().wallets.findAll { it.name != 'main' }.each { it.balance = 0 }
        OrderDTO order = new OrderDTO.Builder()
                .setORDER_ID(root.body.orderId)
                .setMID(m().id)
                .setCHANNEL_ID('WEB')
                .setTXN_TOKEN(token)
                .setPAYMENT_TYPE_ID(PayMethodType.CREDIT_CARD.toString())
                .setCardInfo("${user().savedCards.find().id}||$card.cvv|" as String)
                .setPaymentFlow('HYBRID')
                .build()
        checkoutPage.createNativeOrder(order, false)
        assertion.apply(pageWait.apply(responsePage.hasLoaded()))
        final SoftAssertion sAssert = new SoftAssertion()
        sAssert.apply(
                responsePage.get(Attribute.STATUS).equals('TXN_SUCCESS'),
                responsePage.get(Attribute.CHILD_TXN_LIST).contains(Attribute.CARD_SCHEME),
        )
        sAssert.eval()
        TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID())
        txnStatus.executeUntilNotPending()
        def getPaymentStatusRoot = getPaymentStatus.root()
        getPaymentStatusRoot.body.orderId = order.getORDER_ID()
        getPaymentStatus.req().body(getPaymentStatusRoot).post().then()
                .root("body.childTransaction.find { it.paymentMode != 'PPI' }")
                .body('cardScheme', equalTo(card.scheme.toUpperCase()))
        assertion.apply(peonWait.apply({ peons[order.getORDER_ID()] != null }))
        Peon peon = peons.getAt(order.getORDER_ID())
        sAssert.apply(peon.childTxnList().contains('"cardScheme":"' + card.scheme.toUpperCase() + '"'))
        sAssert.eval()
    }

    @Merchant(edit = true, value = {
        it.id == Constants.MerchantType.EMI.getId() && !(it.id in NON_ALLOWED_MERCHANTS.collect { it.id })
    })
    @AUser(edit = true)
    @Test
    void testForSuccessfulHybridSavedDC() {
        assert m().preferences.with { it.add(it.binInResponse) }
        Card card = cards.find {
            !(it.scheme in ['amex', 'bajajfn']) && it.type == 'debit'
        }.tap { assert it }
        user().savedCards.clear()
        assert user().savedCards.add(card)
        def root = initTxn.root()
        root.body.txnAmount.value = '2'
        root.body.paytmSsoToken = user().tokens['sso'].id
        def token = initTxn.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken')
        user().wallets.find { it.name == 'main' }.balance = 1
        user().wallets.findAll { it.name != 'main' }.each { it.balance = 0 }
        OrderDTO order = new OrderDTO.Builder()
                .setORDER_ID(root.body.orderId)
                .setMID(m().id)
                .setCHANNEL_ID('WEB')
                .setTXN_TOKEN(token)
                .setPAYMENT_TYPE_ID(PayMethodType.DEBIT_CARD.toString())
                .setCardInfo("${user().savedCards.find().id}||$card.cvv|" as String)
                .setPaymentFlow('HYBRID')
                .build()
        checkoutPage.createNativeOrder(order, false)
        assertion.apply(pageWait.apply(responsePage.hasLoaded()))
        final SoftAssertion sAssert = new SoftAssertion()
        sAssert.apply(
                responsePage.get(Attribute.STATUS).equals('TXN_SUCCESS'),
                responsePage.get(Attribute.CHILD_TXN_LIST).contains(Attribute.CARD_SCHEME),
        )
        sAssert.eval()
        TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID())
        def getPaymentStatusRoot = getPaymentStatus.root()
        getPaymentStatusRoot.body.orderId = order.getORDER_ID()
        getPaymentStatus.req().body(getPaymentStatusRoot).post().then()
                .root("body.childTransaction.find { it.paymentMode != 'PPI' }")
                .body('cardScheme', equalTo(card.scheme.toUpperCase()))
        assertion.apply(peonWait.apply({ peons[order.getORDER_ID()] != null }))
        Peon peon = peons.getAt(order.getORDER_ID())
        sAssert.apply(peon.childTxnList().contains('"cardScheme":"' + card.scheme.toUpperCase() + '"'))
        sAssert.eval()
    }

    @Merchant(edit = true, value = {
        it.preferences.hybrid.enabled && it.emis.any {
            it.bank.code.equalsIgnoreCase('AMEX') && it.type == 'cc'
        } && it.peonEnabled && !(it.id in NON_ALLOWED_MERCHANTS.collect { it.id })
    })
    @AUser(edit = true)
    @Test
    void testForSuccessfulHybridSavedEMICC() {
        assert m().preferences.with { it.add(it.binInResponse) }
        double txnAmt = 2
        Card card = cards.find { it.scheme == 'amex' && it.type == 'credit' }.tap { assert it }
        String planId = m().emis.
                find { it.minAmt <= txnAmt && it.maxAmt >= txnAmt && it.bank.code.equalsIgnoreCase('AMEX') }
                .with { it.bank.code.toUpperCase() + '|' + it.months }
        user().savedCards.clear()
        assert user().savedCards.add(card)
        def root = initTxn.root()
        root.body.txnAmount.value = txnAmt as String
        root.body.paytmSsoToken = user().tokens['sso'].id
        def token = initTxn.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken')
        user().wallets.find { it.name == 'main' }.balance = 1
        user().wallets.findAll { it.name != 'main' }.each { it.balance = 0 }
        OrderDTO order = new OrderDTO.Builder()
                .setORDER_ID(root.body.orderId)
                .setMID(m().id)
                .setCHANNEL_ID('WEB')
                .setTXN_TOKEN(token)
                .setPAYMENT_TYPE_ID(PayMethodType.EMI.toString())
                .setCardInfo("${user().savedCards.find().id}||$card.cvv|" as String)
                .setPlanId(planId)
                .setPaymentFlow('HYBRID')
                .build()
        checkoutPage.createNativeOrder(order, false)
        assertion.apply(pageWait.apply(responsePage.hasLoaded()))
        final SoftAssertion sAssert = new SoftAssertion()
        sAssert.apply(
                responsePage.get(Attribute.STATUS).equals('TXN_SUCCESS'),
                responsePage.get(Attribute.CHILD_TXN_LIST).contains(Attribute.CARD_SCHEME),
        )
        sAssert.eval()
        TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID())
        txnStatus.executeUntilNotPending()
        def getPaymentStatusRoot = getPaymentStatus.root()
        getPaymentStatusRoot.body.orderId = order.getORDER_ID()
        getPaymentStatus.req().body(getPaymentStatusRoot).post().then()
                .root("body.childTransaction.find { it.paymentMode != 'PPI' }")
                .body('cardScheme', equalTo(card.scheme.toUpperCase()))
        assertion.apply(peonWait.apply({ peons[order.getORDER_ID()] != null }))
        Peon peon = peons.getAt(order.getORDER_ID())
        sAssert.apply(peon.childTxnList().contains('"cardScheme":"' + card.scheme.toUpperCase() + '"'))
        sAssert.eval()
    }

//    @Merchant(edit = true, value = {
//        it.preferences.hybrid.enabled && it.emis.any {
//            it.type == 'dc' && it.bank.code.equalsIgnoreCase('icie')
//        } && it.peonEnabled && !(it.id in NON_ALLOWED_MERCHANTS.collect { it.id })
//    })
//    @Merchant({ it.id == 'dZbxHf15041674388503' })
//    @AUser(edit = true)
//    @Test(enabled = false, groups = [TO_BE_FIXED])
//TODO
    void testForSuccessfulHybridSavedEMIDC() {
        assert m().preferences.with { it.add(it.binInResponse) }
        double txnAmt = 10
//        Card card = cards.find { !(it.scheme in ['amex', 'bajajfn']) && it.type == 'credit' }.tap { assert it }
//        String planId = m().emis.find().with { it.bank.code.toUpperCase() + '|' + it.months }
        Card card = cards.find { it.no == PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER }.tap { assert it }
        user().savedCards.clear()
        assert user().savedCards.add(card)
        def root = initTxn.root()
        root.body.txnAmount.value = txnAmt as String
        root.body.paytmSsoToken = user().tokens['sso'].id
        def token = initTxn.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken')
//        user().wallets.find { it.name == 'main' }.balance = 1
//        user().wallets.findAll { it.name != 'main' }.each { it.balance = 0 }
        OrderDTO order = new OrderDTO.Builder()
                .setORDER_ID(root.body.orderId)
                .setMID(m().id)
                .setCHANNEL_ID('WEB')
                .setTXN_TOKEN(token)
                .setPAYMENT_TYPE_ID(PayMethodType.EMI.toString())
                .setCardInfo("${user().savedCards.find().id}||$card.cvv|" as String)
                .setPlanId('ICIE|3')
//                .setPaymentFlow('HYBRID')
                .setEMI_TYPE('DEBIT_CARD')
                .build()
        checkoutPage.createNativeOrder(order, false)
        assertion.apply(pageWait.apply(responsePage.hasLoaded()))
        final SoftAssertion sAssert = new SoftAssertion()
        sAssert.apply(
                responsePage.get(Attribute.STATUS).equals('TXN_SUCCESS'),
                responsePage.get(Attribute.CHILD_TXN_LIST).contains(Attribute.CARD_SCHEME),
        )
        sAssert.eval()
        TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID())
        txnStatus.executeUntilNotPending()
        def getPaymentStatusRoot = getPaymentStatus.root()
        getPaymentStatusRoot.body.orderId = order.getORDER_ID()
        getPaymentStatus.req().body(getPaymentStatusRoot).post().then()
                .root("body.childTransaction.find { it.paymentMode != 'PPI' }")
                .body('cardScheme', equalTo(card.scheme.toUpperCase()))
        assertion.apply(peonWait.apply({ peons[order.getORDER_ID()] != null }))
        Peon peon = peons.getAt(order.getORDER_ID())
        sAssert.apply(peon.childTxnList().contains('"cardScheme":"' + card.scheme.toUpperCase() + '"'))
        sAssert.eval()
    }

    @Merchant(edit = true, value = { !it.pcfEnabled && it.payModes.contains('cc') && it.peonEnabled && !(it.id in NON_ALLOWED_MERCHANTS.collect { it.id }) })
    @Test
    void testForFailedTxn() {
        assert m().preferences.with { it.add(it.binInResponse) }
        Card card = cards.find { !(it.scheme in ['amex', 'bajajfn']) && it.type == 'credit' }.tap { assert it }
        def root = initTxn.root()
        root.body.txnAmount.value = '99.98'
        def token = initTxn.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken')
        OrderDTO order = new OrderDTO.Builder()
                .setORDER_ID(root.body.orderId)
                .setMID(m().id)
                .setCHANNEL_ID('WEB')
                .setTXN_TOKEN(token)
                .setPAYMENT_TYPE_ID(PayMethodType.CREDIT_CARD.toString())
                .setCardInfo("|$card.no|$card.cvv|${card.with { expMo + expYr }}" as String)
                .build()
        checkoutPage.createNativeOrder(order, false)
        assertion.apply(pageWait.apply(responsePage.hasLoaded()))
        final SoftAssertion sAssert = new SoftAssertion()
        sAssert.apply(
                responsePage.get(Attribute.STATUS).equals('TXN_FAILURE'),
                responsePage.get(Attribute.CARD_SCHEME).equals(card.scheme.toUpperCase())
        )
        sAssert.eval()
        TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID())
        txnStatus.executeUntilNotPending()
        def getPaymentStatusRoot = getPaymentStatus.root()
        getPaymentStatusRoot.body.orderId = order.getORDER_ID()
        getPaymentStatus.req().body(getPaymentStatusRoot).post().then()
                .root("body")
                .body('cardScheme', equalTo(card.scheme.toUpperCase()))
        assertion.apply(peonWait.apply({ peons[order.getORDER_ID()] != null }))
        Peon peon = peons.getAt(order.getORDER_ID())
        sAssert.apply(
                peon.cardScheme().equals(card.scheme.toUpperCase())
        )
        sAssert.eval()
    }

    @Merchant(edit = true, value = { !it.preferences.addnpay.enabled && !it.preferences.hybrid.enabled && it.payModes.contains('cc') && it.peonEnabled && !(it.id in NON_ALLOWED_MERCHANTS.collect { it.id }) })
    @Test
    void testForPGOnly() {
        assert m().preferences.with { it.add(it.binInResponse) }
        Card card = cards.find { !(it.scheme in ['amex', 'bajajfn']) && it.type == 'credit' }.tap { assert it }
        def root = initTxn.root()
        def token = initTxn.req().body(root).post().then().spec(results.success as ResponseSpecification).extract().path('body.txnToken')
        OrderDTO order = new OrderDTO.Builder()
                .setORDER_ID(root.body.orderId)
                .setMID(m().id)
                .setCHANNEL_ID('WEB')
                .setTXN_TOKEN(token)
                .setPAYMENT_TYPE_ID(PayMethodType.CREDIT_CARD.toString())
                .setCardInfo("|$card.no|$card.cvv|${card.with { expMo + expYr }}" as String)
                .build()
        checkoutPage.createNativeOrder(order, false)
        assertion.apply(pageWait.apply(responsePage.hasLoaded()))
        final SoftAssertion sAssert = new SoftAssertion()
        sAssert.apply(
                responsePage.get(Attribute.STATUS).equals('TXN_SUCCESS'),
                responsePage.get(Attribute.CARD_SCHEME).equals(card.scheme.toUpperCase())
        )
        sAssert.eval()
        TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID())
        txnStatus.executeUntilNotPending()
        def getPaymentStatusRoot = getPaymentStatus.root()
        getPaymentStatusRoot.body.orderId = order.getORDER_ID()
        getPaymentStatus.req().body(getPaymentStatusRoot).post().then()
                .root("body")
                .body('cardScheme', equalTo(card.scheme.toUpperCase()))
        assertion.apply(peonWait.apply({ peons[order.getORDER_ID()] != null }))
        Peon peon = peons.getAt(order.getORDER_ID())
        sAssert.apply(
                peon.cardScheme().equals(card.scheme.toUpperCase()),
                peon.bin().equals(card.no[0..5]),
                peon.lastFourDigits().equals(card.no[-4..-1])
        )
        sAssert.eval()
    }
}
