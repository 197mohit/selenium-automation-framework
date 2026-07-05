package scripts.flows

import io.qameta.allure.Owner
import org.testng.annotations.Test

@Owner("Arsh | Deepak")
interface AuthorizationTokenTest {

    @Test
    void 'Verify success transaction with ECOM token with all mandatory and optional fields in PTC'()

    @Test
    void 'Verify success transaction with ECOM token for MDR+PCF merchant'()

    @Test
    void 'Verify failure transaction with ECOM token with all mandatory and optional fields in PTC'()

    @Test
    void 'Verify failure transaction with ECOM token with all mandatory and optional fields in PTC for MDR+PCF merchant'()

    @Test
    void 'Verify that PTC API fails for merchant not having token support preference for transaction with ECOM token'()

    @Test
    void 'Verify success transaction with CC ECOM token with only mandatory fields in PTC'()

    @Test
    void 'Verify that transaction with prepaid card ECOM token should fail'()

    @Test
    void 'Verify that transaction with prepaid card ECOM token should fail for MDR+PCF merchant'()

    @Test
    void 'Verify the transaction with corporate card ECOM token is successful'()

    @Test
    void 'Verify the transaction with corporate card ECOM token is successful for MDR+PCF merchant'()

    @Test
    void 'Verify that PTC API fails with ECOM token when mandatory parameter ecomtoken is missing'()

    @Test
    void 'Verify that PTC API fails with ECOM token when mandatory parameter expiryMonth is missing'()

    @Test
    void 'Verify that PTC API fails with ECOM token when mandatory parameter expiryYear is missing'()

    @Test
    void 'Verify that PTC API fails with ECOM token when mandatory parameter authenticationValue is missing'()

    @Test
    void 'Verify that in case of mismatch of paymentMode and issuingBank between merchant and our DB, our DB credentials are picked'()

    @Test
    void 'Verify that the PTC API fails when expiryMonth field has value more than 12'()

    @Test
    void 'Verify that the PTC API fails when expiryMonth field has only single digit value'()

    @Test
    void 'Verify transaction with VISA CC ECOM token'()

    @Test
    void 'Verify transaction with MASTER CC ECOM token'()

    @Test
    void 'Verify transaction with MASTER DC ECOM token'()

    @Test
    void 'Verify transaction should fail when CC ECOM token transaction is initiated when CC acquiring is not present on MID'()

    @Test
    void 'Verify transaction should fail for prepaid ECOM token transaction is initiated when Prepaid acquiring is not present on MID'()

    @Test
    void 'Verify transaction should fail for corporate ECOM token transaction is initiated when Corporate acquiring is not present on MID'()

    @Test
    void 'Verify successful refund of ECOM token transaction'()

    @Test
    void 'Verify success transaction with normal card with cybersource gateway'()

    @Test
    void 'Verify that the PTC API fails when EMI transaction is initiated with ECOM token'()

    @Test
    void 'Verify that the PTC API fails when expiryYear is of past'()

    @Test
    void 'Verify that the PTC API fails when ECOM token digits are less than 16'()

    @Test
    void 'Verify that transaction does not fail at theia end when ECOM token passed has bank as null in get token API'()

    @Test
    void 'Verify that corporate card transaction does not fail at theia end when ECOM token passed has bank as null in get token API'()
}