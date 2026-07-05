package scripts.api.theia.fetchEmiDetail


import io.qameta.allure.Owner
import org.testng.annotations.Test

@Owner('Deepak')
interface FetchEmiDetailTest {
    @Test
    void testSuccess()

    @Test
    void 'test localisation fields are not present when localisation is disabled on merchant but language header is passed'()

    @Test
    void 'test localisation fields are not present when localisation is enabled on merchant but language header is not passed'()

    @Test
    void 'test localisation fields are present when localisation is enabled on merchant and language header is passed'()
}
