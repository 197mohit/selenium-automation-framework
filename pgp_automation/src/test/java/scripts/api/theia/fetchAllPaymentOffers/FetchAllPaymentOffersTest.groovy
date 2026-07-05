package scripts.api.theia.fetchAllPaymentOffers

import org.testng.annotations.Test
import scripts.api.theia.HeadTest

interface FetchAllPaymentOffersTest extends HeadTest {
    @Test
    void 'test when body mid is not provided'()

    @Test
    void 'test when body mid = null'()

    @Test
    void 'test when body mid = \'\''()

    @Test
    void 'test when body mid equals random value'()

    @Test
    void 'test when body orderId is not provided'()

    @Test
    void 'test when body orderId = null'()

    @Test
    void 'test when body orderId = \'\''()

    @Test
    void 'test when body orderId equals random value'()

    @Test
    void "test when mid provided in request body is different from mid provided in query params"()

    @Test
    void "test when orderId provided in request body is different from orderId provided in query params"()

    @Test
    void "test when mid in query params is not provided"()

    @Test
    void "test when orderId in query params is not provided"()

    @Test
    void "test when merchant has promos configured"()

    @Test
    void "test when merchant does have not promos configured"()

    //TODO move to txn token class only
    @Test
    void "test when simplifiedPaymentOffers object was not provided in Initate Txn API while creating txn token"()

    @Test
    void 'test when head token is not provided'()

    @Test
    void 'test when head token = null'()

    @Test
    void 'test when head token = \'\''()

    @Test
    void 'test when head token equals random value'()

    //TODO move to txn token interface only
    @Test
    void 'test when head token equals expired token'()
}