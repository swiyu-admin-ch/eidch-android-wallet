package ch.admin.foitt.wallet.platform.oca

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.VcSdJwtCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.TypeMetadata
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.TypeMetadataError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSchema
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSchemaError
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchTypeMetadata
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchVcSchema
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.model.RawOcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchDeferredVcMetadataByFormat
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchOcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation.FetchDeferredVcMetadataByFormatImpl
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
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

@OptIn(UnsafeResultValueAccess::class)
class FetchDeferredVcMetadataByFormatImplTest {

    @MockK
    private lateinit var mockFetchTypeMetadata: FetchTypeMetadata

    @MockK
    private lateinit var mockFetchVcSchema: FetchVcSchema

    @MockK
    private lateinit var mockFetchOcaBundle: FetchOcaBundle

    private val safeJson = SafeJsonTestInstance.safeJson
    private val testVcSchema: VcSchema = VcSchema(schema = "vcSchema 01")

    @MockK
    private lateinit var mockCredentialConfig: VcSdJwtCredentialConfiguration

    private val vctUrl = "https://example"
    private val vctString = "vctString"
    private val schemaUri = "https://schemaUri"
    private val schemaUriIntegrity = "schemaUriIntegrity"
    private val ocaBundle = RawOcaBundle(
        """
        {
            "oca": "displayData"
        }
        """.trimIndent()
    )
    private val testTypeMetadata by lazy {
        safeJson.safeDecodeStringTo<TypeMetadata>(TypeMetadataMocks.typeMetadataFullExample).value
    }

    private lateinit var useCase: FetchDeferredVcMetadataByFormat

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = FetchDeferredVcMetadataByFormatImpl(
            fetchTypeMetadata = mockFetchTypeMetadata,
            fetchVcSchema = mockFetchVcSchema,
            fetchOcaBundle = mockFetchOcaBundle,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Fetching deferred credential metadata successfully returns a TypeMetadata`() = runTest {
        val result = useCase(credentialConfig = mockCredentialConfig).assertOk()

        assertEquals(testVcSchema, result.vcSchema)
        assertEquals(ocaBundle, result.rawOcaBundle)

        coVerifyOrder {
            mockCredentialConfig.vct
            mockFetchTypeMetadata(credentialVct = any(), url = any(), integrity = any(), expectIntegrity = any())
            mockFetchVcSchema(schemaUrl = any(), schemaUriIntegrity = any())
            mockFetchOcaBundle(uri = any(), integrity = any())
        }
    }

    @Test
    fun `Fetching deferred credential metadata with no displays successfully returns a TypeMetadata`() = runTest {
        coEvery {
            mockFetchTypeMetadata(credentialVct = any(), url = any(), integrity = any(), expectIntegrity = any())
        } returns Ok(testTypeMetadata.copy(displays = null))

        val result = useCase(credentialConfig = mockCredentialConfig).assertOk()

        assertEquals(testVcSchema, result.vcSchema)
        assertEquals(null, result.rawOcaBundle)
    }

    @Test
    fun `Fetching deferred credential metadata with non url vct successfully returns an empty TypeMetadata`() = runTest {
        coEvery { mockCredentialConfig.vct } returns vctString

        val result = useCase(credentialConfig = mockCredentialConfig).assertOk()

        assertEquals(null, result.vcSchema)
        assertEquals(null, result.rawOcaBundle)
    }

    @Test
    fun `Fetching deferred credential metadata with unknown config format returns an error`() = runTest {
        coEvery { mockCredentialConfig.format } returns CredentialFormat.UNKNOWN

        useCase(credentialConfig = mockCredentialConfig).assertErrorType(OcaError.UnsupportedCredentialFormat::class)
    }

    @Test
    fun `Fetch type metadata errors are mapped`() = runTest {
        val exception = Exception("my exception")
        coEvery {
            mockFetchTypeMetadata(credentialVct = any(), url = any(), integrity = any(), expectIntegrity = any())
        } returns Err(TypeMetadataError.Unexpected(exception))

        val error = useCase(credentialConfig = mockCredentialConfig).assertErrorType(OcaError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Fetch vc schema errors are mapped`() = runTest {
        val exception = Exception("my exception")
        coEvery {
            mockFetchVcSchema(schemaUrl = any(), schemaUriIntegrity = any())
        } returns Err(VcSchemaError.Unexpected(exception))

        val error = useCase(credentialConfig = mockCredentialConfig).assertErrorType(OcaError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Fetch oca bundle errors are mapped`() = runTest {
        val exception = Exception("my exception")
        coEvery {
            mockFetchOcaBundle(uri = any(), integrity = any())
        } returns Err(OcaError.Unexpected(exception))

        val error = useCase(credentialConfig = mockCredentialConfig).assertErrorType(OcaError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Fetching deferred credential metadata with an improper typeMetadata schema Uri returns an error`() = runTest {
        coEvery {
            mockFetchTypeMetadata(credentialVct = any(), url = any(), integrity = any(), expectIntegrity = any())
        } returns Ok(testTypeMetadata.copy(schemaUri = "not an uri"))

        useCase(credentialConfig = mockCredentialConfig).assertErrorType(OcaError.InvalidOca::class)
    }

    private fun setupDefaultMocks() {
        coEvery {
            mockFetchTypeMetadata(credentialVct = any(), url = any(), integrity = any(), expectIntegrity = any())
        } returns Ok(testTypeMetadata)

        coEvery { mockCredentialConfig.vct } returns vctUrl

        coEvery { mockCredentialConfig.format } returns CredentialFormat.VC_SD_JWT

        coEvery {
            mockFetchOcaBundle(uri = any(), integrity = any())
        } returns Ok(ocaBundle)

        coEvery {
            mockFetchVcSchema(schemaUrl = any(), schemaUriIntegrity = any())
        } returns Ok(testVcSchema)
    }
}
