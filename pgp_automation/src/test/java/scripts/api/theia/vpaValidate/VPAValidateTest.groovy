package scripts.api.theia.vpaValidate

import org.testng.annotations.Test
import scripts.api.theia.HeadTest

interface VPAValidateTest extends HeadTest {
    @Test
    void 'test when query params are not provided'()

    @Test
    void 'test when mid in query params is not provided'()

    @Test
    void 'test when orderId in query params is not provided'()

    @Test
    void 'test unable to validate vpa when body vpa is not provided'()

    @Test
    void 'test unable to validate vpa when body vpa = null'()

    @Test
    void 'test unable to validate vpa when body vpa = \'\''()

    @Test
    void 'test unable to validate vpa when body vpa equals random alphabetical value'()

    @Test
    void 'test unable to validate vpa when body vpa equals random numerical value'()

    @Test
    void 'test unable to validate vpa when body vpa equals value having pattern equals upiHandler@psp'()

    @Test
    void 'test unable to validate vpa when body vpa equals vpa whose psp is not registered'()

    @Test
    void 'test able to validate vpa when body vpa equals paytm vpa'()

    @Test
    void 'test when body mid is not provided'()

    @Test
    void 'test when body mid = null'()

    @Test
    void 'test when body mid = \'\''()

    @Test
    void 'test when body mid equals random value'()

    @Test
    void 'test when mid provided in query params is different from mid provided in request body'()
}