package scripts.api.theia.fetchCardIdxNo

import org.testng.annotations.Test

interface FetchCardIndexNoTest {


    @Test
    void 'test when reference id in header'()

    @Test
    void 'test when without reference id in headers'()


    @Test
    void 'test when with incomplete bank account number'()

    @Test
    void 'test when with incomplete IFSC number'()

    @Test
    void 'test bank account number set as blank'()

    @Test
    void 'test with bankifsc number set as blank'()

    @Test
    void 'test with bank, ifsc and cardnumber'()

}