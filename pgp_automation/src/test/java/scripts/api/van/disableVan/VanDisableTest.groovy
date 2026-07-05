package scripts.api.van.disableVan

import org.testng.annotations.Test
import scripts.api.theia.HeadTest

interface VanDisableTest extends HeadTest{

    @Test
    void 'Validate the disable van api when valid mid is provided'()

    @Test
    void 'Validate the disable van api response when MID is provided which is not having bank transfer'()

    @Test
    void 'Validate the response when invalid token is provided'()

    @Test
    void 'Validate the response when requestid id provided which is having more then 64 digit'()

    @Test
    void 'Validate the response when mid is provided as blank'()
}