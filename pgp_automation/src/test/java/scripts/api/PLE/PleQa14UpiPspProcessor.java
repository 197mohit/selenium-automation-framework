package scripts.api.PLE;

import com.paytm.api.UpiPspProcessor;
import com.paytm.dto.upiIntent.staticQR.StaticQrUpiPSPRequest;

/**
 * POST {@code upi-psp-processor/v1/order/pay/upipsp} on QA14 ({@code pgp-qa14})
 * without changing {@link UpiPspProcessor}.
 */
class PleQa14UpiPspProcessor extends UpiPspProcessor {

    static final String QA14_PGP_HOST = "https://pgp-qa14.paytm.in";

    PleQa14UpiPspProcessor(StaticQrUpiPSPRequest staticQrUpiPSPRequest) {
        super(staticQrUpiPSPRequest);
        getRequestSpecBuilder().setBaseUri(QA14_PGP_HOST);
    }
}
