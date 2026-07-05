package scripts.api.van.vanSearch

import org.testng.annotations.Test

interface VanSearchTest {

    @Test
    void 'Validate the search van api response when all mandatory paramters are provided and search by MID'()

    @Test
    void 'Validate the search van api response when token validation failed'()

    @Test
    void 'Validate the search api response when MID is used which is not having bank transfer supported'()

    @Test
    void 'Validate the search api response when requestid is passed which is having more then 64 digit'()

    @Test
    void 'Validate the search van api response when active status true'()

    @Test
    void 'Validate the search van api response when active status false'()

    @Test
    void 'Validate the search van api response when date range is provided in search param'()

    @Test
    void 'Validate the search van api response when date range is provided in search param and active status false'()

    @Test
    void 'Validate the search api response when start date is greater then end date '()

    @Test
    void 'Validate the search api response when start date is provided and end date is not provided'()

    @Test
    void 'Validate the search api response when searchParams is null'()

    @Test
    void 'Validate the search api response when pageInfo is null'()

    @Test
    void 'Validate the response of van search api when limit is 50'()

    @Test
    void 'Validate the response of van search api when limit is 101'()

    @Test
    void 'Validate the response of van search when page limit is in decimal'()

    @Test
    void 'Validate the response of van search when page limit is in negative'()

    @Test
    void 'Validate the response of van search api when page limit is 100'()

    @Test
    void 'Validate the response of van search api when page limit is 1'()

    @Test
    void 'Validate the response when page limit is 0'()

}