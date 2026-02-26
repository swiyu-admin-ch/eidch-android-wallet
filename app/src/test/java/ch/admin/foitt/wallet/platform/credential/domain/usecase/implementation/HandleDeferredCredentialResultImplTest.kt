package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import android.annotation.SuppressLint
import ch.admin.foitt.openid4vc.domain.model.DeferredCredential
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyDisplays
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateAnyDisplays
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleDeferredCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.credentialConfig
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.oneConfigCredentialInformation
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.model.RawOcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.VcMetadata
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchDeferredVcMetadataByFormat
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaBundler
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialOfferRepository
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertSuccessType
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

class HandleDeferredCredentialResultImplTest {
    @MockK
    private lateinit var mockFetchDeferredVcMetadataByFormat: FetchDeferredVcMetadataByFormat

    @MockK
    private lateinit var mockOcaBundler: OcaBundler

    @MockK
    private lateinit var mockGenerateAnyDisplays: GenerateAnyDisplays

    @MockK
    private lateinit var mockCredentialOfferRepository: CredentialOfferRepository

    private lateinit var useCase: HandleDeferredCredentialResult

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = HandleDeferredCredentialResultImpl(
            fetchDeferredVcMetadataByFormat = mockFetchDeferredVcMetadataByFormat,
            ocaBundler = mockOcaBundler,
            generateAnyDisplays = mockGenerateAnyDisplays,
            credentialOfferRepository = mockCredentialOfferRepository,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @SuppressLint("CheckResult")
    @Test
    fun `Saving a deferred credential runs specific steps`() = runTest {
        val result = useCase.invoke(
            issuerUrl = ISSUER_URL,
            deferredCredential = deferredCredential,
            rawAndParsedCredentialInfo = RawAndParsedIssuerCredentialInfo(
                issuerCredentialInfo = oneConfigCredentialInformation,
                rawIssuerCredentialInfo = ""
            ),
            credentialConfig = credentialConfig,
        )

        val deferredResult = result.assertSuccessType(FetchCredentialResult.DeferredCredential::class)
        assertEquals(DEFERRED_CREDENTIAL_ID, deferredResult.credentialId)

        coVerifyOrder {
            mockFetchDeferredVcMetadataByFormat(credentialConfig)
            mockOcaBundler(vcMetadata.rawOcaBundle!!.rawOcaBundle)
            mockGenerateAnyDisplays(
                anyCredential = null,
                issuerInfo = oneConfigCredentialInformation,
                trustStatement = null,
                metadata = credentialConfig,
                ocaBundle = ocaBundle
            )
            mockCredentialOfferRepository.saveDeferredCredentialOffer(
                transactionId = deferredCredential.transactionId,
                accessToken = deferredCredential.accessToken,
                endpoint = deferredCredential.endpoint,
                pollInterval = deferredCredential.pollInterval,
                keyBindings = deferredCredential.keyBindings,
                format = deferredCredential.format,
                issuerDisplays = anyDisplays.issuerDisplays,
                credentialDisplays = anyDisplays.credentialDisplays,
                rawCredentialData = any(),
                selectedConfigurationId = any(),
                issuerUrl = any(),
            )
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `Errors from getting a deferred credential metadata are mapped`() = runTest {
        val exception = Exception("my exception")

        coEvery {
            mockFetchDeferredVcMetadataByFormat(any())
        } returns Err(OcaError.Unexpected(exception))

        val error = useCase.invoke(
            issuerUrl = ISSUER_URL,
            deferredCredential = deferredCredential,
            rawAndParsedCredentialInfo = RawAndParsedIssuerCredentialInfo(
                issuerCredentialInfo = oneConfigCredentialInformation,
                rawIssuerCredentialInfo = ""
            ),
            credentialConfig = credentialConfig,
        ).assertErrorType(CredentialError.Unexpected::class)
        assertEquals(exception.message, error.cause?.message)
    }

    @SuppressLint("CheckResult")
    @Test
    fun `Errors from a deferred credential displays are mapped`() = runTest {
        val exception = Exception("my exception")
        coEvery {
            mockGenerateAnyDisplays(
                anyCredential = any(),
                issuerInfo = any(),
                trustStatement = any(),
                metadata = any(),
                ocaBundle = any()
            )
        } returns Err(CredentialError.Unexpected(exception))

        val error = useCase.invoke(
            issuerUrl = ISSUER_URL,
            deferredCredential = deferredCredential,
            rawAndParsedCredentialInfo = RawAndParsedIssuerCredentialInfo(
                issuerCredentialInfo = oneConfigCredentialInformation,
                rawIssuerCredentialInfo = ""
            ),
            credentialConfig = credentialConfig,
        ).assertErrorType(CredentialError.Unexpected::class)
        assertEquals(exception.message, error.cause?.message)
    }

    @SuppressLint("CheckResult")
    @Test
    fun `Errors from a deferred credential oca bundle are ignored`() = runTest {
        val exception = Exception("my exception")

        coEvery {
            mockOcaBundler(any())
        } returns Err(OcaError.Unexpected(exception))

        val result = useCase.invoke(
            issuerUrl = ISSUER_URL,
            deferredCredential = deferredCredential,
            rawAndParsedCredentialInfo = RawAndParsedIssuerCredentialInfo(
                issuerCredentialInfo = oneConfigCredentialInformation,
                rawIssuerCredentialInfo = ""
            ),
            credentialConfig = credentialConfig,
        )

        val deferredResult = result.assertSuccessType(FetchCredentialResult.DeferredCredential::class)
        assertEquals(DEFERRED_CREDENTIAL_ID, deferredResult.credentialId)
    }

    private fun setupDefaultMocks(
        credentialInfo: IssuerCredentialInfo = oneConfigCredentialInformation,
    ) {
        coEvery { mockOcaBundler(any()) } returns Ok(ocaBundle)

        coEvery {
            mockGenerateAnyDisplays(
                anyCredential = any(),
                issuerInfo = credentialInfo,
                trustStatement = any(),
                metadata = credentialConfig,
                ocaBundle = any(),
            )
        } returns Ok(anyDisplays)

        coEvery {
            mockCredentialOfferRepository.saveDeferredCredentialOffer(
                transactionId = any(),
                accessToken = any(),
                endpoint = any(),
                pollInterval = any(),
                keyBindings = any(),
                format = any(),
                issuerDisplays = any(),
                credentialDisplays = any(),
                rawCredentialData = any(),
                selectedConfigurationId = any(),
                issuerUrl = any(),
            )
        } returns Ok(DEFERRED_CREDENTIAL_ID)

        coEvery {
            mockFetchDeferredVcMetadataByFormat.invoke(credentialConfig = any())
        } returns Ok(vcMetadata)
    }

    private companion object {
        const val DEFERRED_CREDENTIAL_ID = 222L
        val ISSUER_URL = URL("https://issuer.example.com")

        val vcMetadata = VcMetadata(vcSchema = null, rawOcaBundle = RawOcaBundle(RAW_OCA_BUNDLE))
        const val RAW_OCA_BUNDLE = "oca bundle"
        val ocaBundle = OcaBundle(emptyList(), emptyList())
        val anyDisplays = AnyDisplays(emptyList(), emptyList(), emptyList())

        private val keyBinding = KeyBinding(
            identifier = "keyId",
            algorithm = SigningAlgorithm.ES512,
            bindingType = KeyBindingType.SOFTWARE,
        )

        val deferredCredential = DeferredCredential(
            format = CredentialFormat.VC_SD_JWT,
            keyBindings = listOf(keyBinding),
            transactionId = "transactionId",
            accessToken = "accessToken",
            endpoint = URL("https://example"),
            pollInterval = 1,
        )
    }
}
