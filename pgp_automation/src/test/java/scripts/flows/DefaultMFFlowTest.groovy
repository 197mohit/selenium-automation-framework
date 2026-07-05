package scripts.flows

import org.testng.annotations.Test

interface DefaultMFFlowTest {
    @Test
    void 'test order is successful when payment is done by ppbl'()

    @Test
    void 'test order is unsuccessful when account number passed in the request does not match with account number associated with mobile no used to login'()

    @Test
    void 'test order is unsuccessful when account number is not passed in request'()
}