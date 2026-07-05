package scripts.api.theia.validateAndFetchMerchantInfo

import io.qameta.allure.Owner
import org.testng.annotations.Test
import scripts.api.theia.HeadTest

@Owner("Deepak")
interface ValidateAndFetchMerchantInfoTest extends HeadTest {
    @Test
    void 'test when body mids is not provided'()

    @Test
    void 'test when body mids = null'()

    @Test
    void 'test when body mids = \'\''()

    @Test
    void 'test when body mids equals empty list'()

    @Test
    void 'test when body mids equals iterable containing single random value'()

    @Test
    void 'test when body mids equals iterable containing multiple random values'()

    @Test
    void 'test when body mids equals iterable containing single valid mid'()

    @Test
    void 'test when body mids equals iterable containing multiple valid mids'()

    @Test
    void 'test when body mids equals iterable containing both single valid mid and null'()

    @Test
    void 'test when body mids equals iterable containing both single valid mid and \'\''()

    @Test
    void 'test when body mids equals iterable containing both single valid mid and a random value'()

    @Test
    void 'test when head clientId is not provided'()

    @Test
    void 'test when head clientId = null'()

    @Test
    void 'test when head clientId = \'\''()

    @Test
    void 'test when head clientId equals random value'()
}