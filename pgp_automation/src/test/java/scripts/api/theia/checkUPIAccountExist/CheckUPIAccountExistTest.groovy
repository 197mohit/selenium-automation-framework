package scripts.api.theia.checkUPIAccountExist

import org.testng.annotations.Test
import scripts.api.theia.ChecksumAuthenticationTest
import scripts.api.theia.HeadTest

interface CheckUPIAccountExistTest extends HeadTest, ChecksumAuthenticationTest{



    @Test(enabled = false)
    void 'test CHECK_UPI_ACCOUNT_EXISTS=N on merchant error will invoke'()

    @Test (enabled = false)
    void 'test SSO token support is removed from the api'()


    @Test (enabled = false)
    void 'test with invalid checksum it should invoke error'()


    @Test (enabled = false)
    void 'test mid is mandatory parameter and error message is returned in case mid is not send in request body'()


    @Test (enabled = false)
    void 'test error message is retuned in response is incorrect mid is sent in request body'()


    @Test (enabled = false)
    void 'test mobileNumber is mandatory parameter and error message is returned in case mobileNumber is not send in request body'()


    @Test (enabled = false)
    void 'test error message is retuned in response is incorrect mobileNumber is sent in request body'()


    @Test (enabled = false)
    void 'test success response if all mandatory parameters are sent and correct'()


    @Test (enabled = false)
    void 'test upiAccountWithTokenExists=false upi account doesnot exist for a number'()


    @Test (enabled = false)
    void 'test upi account exist for a number but mpin is not set then api should provide account not exist response with api status success'()



}