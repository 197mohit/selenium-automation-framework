package scripts.api.theia

import org.testng.annotations.Test

interface ChecksumAuthenticationTest {
    @Test
    void 'test when head token equals checksum generated using different merchant\'s key'()
}