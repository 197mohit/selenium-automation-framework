package scripts.api.van.updateVan

import org.testng.annotations.Test

interface VanUpdateTest {

    @Test
    void 'Validate the response of Van update api for disable a VAN'()

    @Test
    void 'Validate the response of van update api for active a van which is already active'()

    @Test
    void 'Validate the response of van update api when 4 vans are provided from which 1 is correct and 3 are incorrect'()

    @Test
    void 'Validate the response when merchant is not bank transfer pref enabled'()

    @Test
    void 'Validate the response of update van api for token validation failed'()

    @Test
    void 'Validate the response of update van api when request id is having more the 64 digit'()

    @Test
    void 'Validate the response of update van api when vanInfo is null'()

    @Test
    void 'Validate the response when invalid VAN is provided in request'()

    @Test
    void 'Validate the response of van update api when active is null'()

    @Test
    void 'Validate the response of update van api when van is null'()

    @Test
    void 'validate the response of update van api when more then 10 van details are provided'()

    @Test
    void 'Validate the response of update van api when activate a deactivated van'()

    @Test
    void 'Validate the response when mid is blank'()

}