package scripts.api.van.vanPayment

import org.testng.annotations.Test

interface VanPaymentTest {

    @Test
    void 'Validate the successful txn using callback PPBL api'()

    @Test
    void  'Validate the response of callback PPBL api when invalid JWT token is provided'()

    @Test
    void 'Validate the response of callback PPBL api when event_tracking_id is null '()

    @Test
    void 'Validate the response of callback PPBL api when status is null'()

    @Test
    void  'Validate the response of call back PPBL api when Status is failure'()

    @Test
    void 'Validate the response of callback PPBL api when vanNumber is null'()

    @Test
    void 'Validate the response of callback PPBL api when beneficiaryAccountNumber is null'()

    @Test
    void 'Validate the response of callback PPBL api when beneficiaryIfsc is null'()

    @Test
    void 'Validate the response of callback PPBL api when remitterAccountNumber is null'()

    @Test
    void 'Validate the response of callback PPBL api when remitterName is null'()

    @Test
    void 'Validate the response of callback PPBL api when amount is 0'()

    @Test
    void 'Validate the response of callback PPBL api when banktxnidentifier is null'()

    @Test
    void 'Validate the response of callback PPBL api when transactionRequestId is null'()

    @Test
    void 'Validate the response of callback PPBL api when transferMode is null'()

    @Test
    void 'Validate the response of callback PPBL api when transactionDate is null'()

    @Test
    void 'Validate the response of callback PPBL api when transactionType is null'()

    @Test
    void 'Validate the response of callback PPBL api when responseCode is null'()

    @Test
    void 'Validate the response of callback PPBL api when amount is greater then 2 decimal'()

    @Test
    void 'Validate the response of callback PPBL api when transferMode is IMPS'()
}