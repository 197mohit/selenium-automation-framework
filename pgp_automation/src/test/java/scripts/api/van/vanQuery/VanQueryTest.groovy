package scripts.api.van.vanQuery

import org.testng.annotations.Test

interface VanQueryTest {

    @Test
    void 'Validate the query van api response when all mandatory parameters are provided and search by VAN'()

    @Test
    void 'Validate the query van api response when all mandatory parameters are provided and search by IDN'()

    @Test
    void 'validate the response of query van api when token validation failed'()

    @Test
    void 'validate the response of query van api when mid is provided which is not having bank transfer as paymode'()

    @Test
    void  'Validate the response when searchkey is VAN and IDN is provided in searchid'()

    @Test
    void 'Validate the response when searchKey is IDN and VAN is provided in searchid'()

    @Test
    void 'Validate the response when searchKey is IDN and merchant prefix is null'()

    @Test
    void  'Validate the response when searchKey is IDN and merchant prefix is invalid'()

    @Test
    void  'Validate the response when searchKey is IDN and merchant prefix with invalid length'()

    @Test
    void 'Validate the response when searchKey is IDN and invalid IDN is provided'()

    @Test
    void 'Validate the response when searchKey is IDN and IDN provided with invalid length'()

    @Test
    void 'Validate the response when searchKey is VAN and invalid van id provided'()

    @Test
    void 'Validate the response when searchKey is VAN and van id provided with invalid length'()


    @Test
    void 'Validate the response of van query api when 2 Vans has been provided in Search details 1 is correct and another is incorrect'()

    @Test
    void  'Validate the response when try to query more then 10 van info'()

    @Test
    void 'Validate the response of van query api when requestid length is more then 64 digit'()

    @Test
    void "Validate the create van api response when mid is empty"()
}