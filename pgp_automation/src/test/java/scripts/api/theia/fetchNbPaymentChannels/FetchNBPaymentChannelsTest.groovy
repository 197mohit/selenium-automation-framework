package scripts.api.theia.fetchNbPaymentChannels

import org.testng.annotations.Test
import scripts.api.theia.HeadTest

interface FetchNBPaymentChannelsTest extends HeadTest {
    @Test
    void 'test when query params are not provided'()

    @Test
    void 'test when mid in query params is not provided'()

    @Test
    void 'test when orderId in query params is not provided'()

    @Test
    void 'test when body mid is not provided'()

    @Test
    void 'test when body mid = null'()

    @Test
    void 'test when body mid = \'\''()

    @Test
    void 'test when body mid equals random value'()

    @Test
    void 'test when body type is not provided'()

    @Test
    void 'test when body type = null'()

    @Test
    void 'test when body type = \'\''()

    @Test
    void 'test when body type equals random value'()

    @Test
    void 'test when body type = MERCHANT'()

    @Test
    void 'test when body type = ADD_MONEY'()

    @Test
    void 'test when merchant has neither nb nor ppbl configured'()

    @Test
    void "test when merchant has nb configured but not ppbl"()

    @Test
    void "test when merchant has both nb and ppbl configured"()

    @Test
    void "test when merchant had ppbl configured but not nb"()

    @Test
    void "test when merchant has nb configured but is disabled"()

    @Test
    void 'test when one or more nb channels is disabled'()

    @Test
    void 'test when one or more nb channels is enabled'()

    @Test
    void 'test when one or more nb channels has low success rate'()

    @Test
    void 'test when one or more nb channels has high success rate'()

    @Test
    void 'test when mid provided in query params is different from mid provided in request body'()

    @Test
    void 'test localisation fields are not present when localisation is disabled on merchant but language header is passed'()

    @Test
    void 'test localisation fields are not present when localisation is enabled on merchant but language header is not passed'()

    @Test
    void 'test localisation fields are present when localisation is enabled on merchant and language header is passed'()
}