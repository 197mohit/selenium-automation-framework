package scripts.api.merchantStatus.txnStatusListApp

import org.testng.annotations.Test

interface TxnStatusListAppTest {
    @Test
    void 'test when order is successful'()

    @Test
    void 'test when order is failure'()

    @Test
    void 'test when order is pending'()

}