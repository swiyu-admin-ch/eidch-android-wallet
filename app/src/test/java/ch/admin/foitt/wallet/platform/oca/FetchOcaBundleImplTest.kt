package ch.admin.foitt.wallet.platform.oca

import ch.admin.foitt.sriValidator.domain.SRIValidator
import ch.admin.foitt.sriValidator.domain.model.SRIError
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.repository.OcaRepository
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchOcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation.FetchOcaBundleImpl
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.OCA_DATA_URI
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.OCA_DATA_URI_CONTENT
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.OCA_DATA_URI_INTEGRITY
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.OCA_INVALID_HTTPS_URI
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.OCA_INVALID_URI
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.OCA_URL
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.OCA_URL_INTEGRITY
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ocaResponse
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

class FetchOcaBundleImplTest {
    @MockK
    private lateinit var mockOcaRepository: OcaRepository

    @MockK
    private lateinit var mockSRIValidator: SRIValidator

    private lateinit var useCase: FetchOcaBundle

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = FetchOcaBundleImpl(
            ocaRepository = mockOcaRepository,
            sriValidator = mockSRIValidator,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Fetching oca bundle with url and url integrity returns the raw oca bundle`() = runTest {
        val result = useCase(OCA_URL, OCA_URL_INTEGRITY).assertOk()

        assertEquals(ocaResponse, result.rawOcaBundle)
    }

    @Test
    fun `Fetching oca bundle with url maps errors from parsing the url`() = runTest {
        useCase(OCA_INVALID_HTTPS_URI, OCA_URL_INTEGRITY).assertErrorType(OcaError.InvalidOca::class)
    }

    @Test
    fun `Fetching oca bundle with url maps error from the oca repository`() = runTest {
        val exception = IllegalStateException("oca repo error")
        coEvery { mockOcaRepository.fetchOcaBundleByUrl(URL(OCA_URL)) } returns Err(OcaError.Unexpected(exception))

        useCase(OCA_URL, OCA_URL_INTEGRITY).assertErrorType(OcaError.Unexpected::class)
    }

    @Test
    fun `Fetching oca bundle with url maps errors from validating the sub resource`() = runTest {
        coEvery { mockSRIValidator(ocaResponse.encodeToByteArray(), OCA_URL_INTEGRITY) } returns Err(SRIError.ValidationFailed)

        useCase(OCA_URL, OCA_URL_INTEGRITY).assertErrorType(OcaError.InvalidOca::class)
    }

    @Test
    fun `Fetching oca bundle with url but without url integrity does not validate the data`() = runTest {
        val result = useCase(OCA_URL, null).assertOk()

        assertEquals(ocaResponse, result.rawOcaBundle)

        coVerify(exactly = 0) {
            mockSRIValidator(any(), any())
        }
    }

    @Test
    fun `Fetching oca bundle with data uri returns the raw oca bundle`() = runTest {
        val result = useCase(OCA_DATA_URI, OCA_DATA_URI_INTEGRITY).assertOk()

        assertEquals(OCA_DATA_URI_CONTENT, result.rawOcaBundle)
    }

    @Test
    fun `Fetching oca bundle with data uri but without uri integrity does not validate the data`() = runTest {
        val result = useCase(OCA_DATA_URI, null).assertOk()

        assertEquals(OCA_DATA_URI_CONTENT, result.rawOcaBundle)

        coVerify(exactly = 0) {
            mockSRIValidator(any(), any())
        }
    }

    @Test
    fun `Fetching oca bundle with other uri returns an error`() = runTest {
        useCase(OCA_INVALID_URI, OCA_URL_INTEGRITY).assertErrorType(OcaError.InvalidOca::class)
    }

    private fun setupDefaultMocks() {
        coEvery { mockOcaRepository.fetchOcaBundleByUrl(URL(OCA_URL)) } returns Ok(ocaResponse)

        coEvery { mockSRIValidator(ocaResponse.encodeToByteArray(), OCA_URL_INTEGRITY) } returns Ok(Unit)
        coEvery { mockSRIValidator(OCA_DATA_URI.encodeToByteArray(), OCA_DATA_URI_INTEGRITY) } returns Ok(Unit)
    }
}
