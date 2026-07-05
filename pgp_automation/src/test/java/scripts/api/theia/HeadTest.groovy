package scripts.api.theia

import org.testng.annotations.Test

interface HeadTest {
    @Test
    void 'test when head requestId is not provided'()

    @Test
    void 'test when head requestId = null'()

    @Test
    void 'test when head requestId = \'\''()

    @Test
    void 'test when head requestId equals random value'()

    @Test
    void 'test when head requestTimestamp is not provided'()

    @Test
    void 'test when head requestTimestamp = null'()

    @Test
    void 'test when head requestTimestamp = \'\''()

    @Test
    void 'test when head requestTimestamp equals random value'()

    @Test
    void 'test when head channelId is not provided'()

    @Test
    void 'test when head channelId = null'()

    @Test
    void 'test when head channelId = \'\''()

    @Test
    void 'test when head channelId equals random value'()

    @Test
    void 'test when head tokenType is not provided'()

    @Test
    void 'test when head tokenType = null'()

    @Test
    void 'test when head tokenType = \'\''()

    @Test
    void 'test when head tokenType equals random value'()
}