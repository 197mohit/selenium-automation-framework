package scripts.flows

import org.testng.annotations.Test

interface ProcessTxnEventLinkIdTest {
    @Test
    void 'test eventLinkId is coming in response when order is initiated for CC pay mode in case of risk pass'()

    @Test
    void 'test eventLinkId is coming in response when order is initiated for DC pay mode in case of risk pass'()

    @Test
    void 'test eventLinkId is coming in response when order is initiated for NB pay mode in case of risk pass'()

    @Test
    void 'test eventLinkId is not coming in response in case of risk reject'()

    @Test
    void 'test eventLinkId is not coming in response in case of risk verify'()
}