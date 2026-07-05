package scripts.api.merchantStatus.txnStatusApp

import org.testng.annotations.Test

interface TxnStatusAppTest {
    @Test
    void 'test when order is successful'()

    @Test
    void 'test when order is failure'()

    @Test
    void 'test when order is pending'()
}