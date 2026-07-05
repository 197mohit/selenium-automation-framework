package scripts.api.theia

import org.testng.annotations.Test

interface TxnTokenAuthenticationTest {
    @Test
    void 'test when head token equals txn token generated using different mid'()

    @Test
    void 'test when head token equals txn token generated using different orderId'()

    @Test
    void 'test when head token equals txn token generated for OFFUS user'()

    @Test
    void 'test when head token equals txn token generated for ONUS user'()
}