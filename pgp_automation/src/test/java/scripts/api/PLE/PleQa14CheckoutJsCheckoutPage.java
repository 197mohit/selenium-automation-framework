package scripts.api.PLE;

import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.pages.CheckoutJsCheckoutPage;

/**
 * Checkout JS test harness on QA14 ({@code pgp-qa14}) without changing
 * {@link CheckoutJsCheckoutPage}.
 * Values match {@code profiles/qa14/localconfig.properties} —
 * {@code CHECKOUTJS_URL} and
 * {@code CHECKOUTJS_LOAD_URL}.
 */
class PleQa14CheckoutJsCheckoutPage extends CheckoutJsCheckoutPage {

    static final String QA14_CHECKOUTJS_PAGE = "https://pgp-qa14.paytm.in/merchant-checkout/checkout.html";
    static final String QA14_CHECKOUTJS_LOAD = "https://pgp-qa14.paytm.in/merchantpgpui/checkoutjs/merchants/{mid}";

    PleQa14CheckoutJsCheckoutPage() {
        super();
        this.pageURL = QA14_CHECKOUTJS_PAGE;
    }

    @Override
    protected String setCheckoutjsLoadUrl(InitTxnDTO initTxnDTO, String theme) {
        String mid = initTxnDTO.getBody().getMid();
        String loadUrl = QA14_CHECKOUTJS_LOAD.replace("{mid}", mid);
        return "document.getElementById('merchantCheckout').value='" + loadUrl + "'";
    }
}
