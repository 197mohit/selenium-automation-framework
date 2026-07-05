package scripts.api.theia

import org.testng.annotations.Test

interface SendOtpTest {

    @Test
    void testSuccess()

    @Test
    void 'test when merchant has send otp v5 flag enabled and autoReadHash is passed'()

    @Test
    void 'test when merchant has send otp v5 flag enabled and autoReadHash is not passed'()

    @Test
    void 'test when merchant has send otp v5 flag disabled and autoReadHash is passed'()

    @Test
    void 'test when merchant has send otp v5 flag disabled and autoReadHash is not passed'()
}