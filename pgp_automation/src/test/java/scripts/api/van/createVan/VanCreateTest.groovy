package scripts.api.van.createVan

import org.testng.annotations.Test

interface VanCreateTest {
    @Test
    void 'Validate the create van api response when all mandatory parameters are provided'()

    @Test
    void 'validate the response of create van api when token validation failed'()

    @Test
    void 'validate the response of create van api when mid is provided which is not having bank transfer as paymode'()

    @Test
    void 'validate the response of create van api when same identification number for which VAN is exist'()

    @Test
    void 'Validate the response of create van api when length of identification number is more then 10'()

    @Test
    void 'Validate the response of create van api when length of identification number is less then 10'()

    @Test
    void 'Validate the response of create van api when merchant prefix length is provided more then 4'()

    @Test
    void 'Validate the response of create van api when merchant prefix length is provided less then 4'()

    @Test
    void 'validate the response of create van api when more then 1 van details are provided'()

    @Test
    void 'Validate the response of create van api when 2 VAN details are provided in which 1 is correct and 1 is incorrect'()

    @Test
    void 'validate the response of create van api when more then 10 van details are provided'()

    @Test
    void 'Validate the response of create van api when requestid provided which is having more then 64 digit'()

    @Test
    void 'Validate the create van api response when mid is empty'()

}
