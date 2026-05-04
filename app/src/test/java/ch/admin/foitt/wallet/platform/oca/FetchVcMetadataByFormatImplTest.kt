package ch.admin.foitt.wallet.platform.oca

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.TypeMetadata
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.TypeMetadataError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSchema
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSchemaError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchTypeMetadata
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchVcSchema
import ch.admin.foitt.wallet.platform.jsonSchema.domain.usecase.JsonSchemaValidator
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.model.RawOcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchOcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchVcMetadataByFormat
import ch.admin.foitt.wallet.platform.oca.domain.usecase.ResolveMetaDataIntegrity
import ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation.FetchVcMetadataByFormatImpl
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.CREDENTIAL_VCT
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.OCA_URL
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.OCA_URL_INTEGRITY
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.TYPE_METADATA_URL
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.VCT_METADATA_URI
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.VCT_METADATA_URI_INTEGRITY
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.VCT_URL_INTEGRITY
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.VC_SCHEMA_URL
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.VC_SCHEMA_URL_INTEGRITY
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.typeMetadataFullExample
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.typeMetadataWithInvalidVcSchemaUrl
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.typeMetadataWithOcaMultipleRenderings
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.typeMetadataWithoutOcaRendering
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.typeMetadataWithoutVcSchemaUrl
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ocaResponse
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json.Default.parseToJsonElement
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

@OptIn(UnsafeResultValueAccess::class)
class FetchVcMetadataByFormatImplTest {
    @MockK
    private lateinit var mockFetchTypeMetadata: FetchTypeMetadata

    @MockK
    private lateinit var mockFetchVcSchema: FetchVcSchema

    @MockK
    private lateinit var mockFetchOcaBundle: FetchOcaBundle

    @MockK
    private lateinit var mockVcSdJwtCredential: VcSdJwtCredential

    @MockK
    private lateinit var mockJsonSchemaValidator: JsonSchemaValidator

    private val safeJson = SafeJsonTestInstance.safeJson

    private lateinit var useCase: FetchVcMetadataByFormat

    @MockK
    private lateinit var mockResolveMetaDataIntegrity: ResolveMetaDataIntegrity

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = FetchVcMetadataByFormatImpl(
            resolveMetaDataIntegrity = mockResolveMetaDataIntegrity,
            fetchTypeMetadata = mockFetchTypeMetadata,
            fetchVcSchema = mockFetchVcSchema,
            fetchOcaBundle = mockFetchOcaBundle,
            vcSdJwtJsonSchemaValidator = mockJsonSchemaValidator
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Fetching vc metadata for VcSdJwt credential from vct returns vc metadata`() = runTest {
        every { mockVcSdJwtCredential.vctMetadataUri } returns null
        every { mockVcSdJwtCredential.vctMetadataUriIntegrity } returns null
        coEvery {
            mockResolveMetaDataIntegrity(any())
        } returns Ok(Pair(URL(CREDENTIAL_VCT), VCT_URL_INTEGRITY))

        val result = useCase(mockVcSdJwtCredential)

        val vcMetadata = result.assertOk()
        assertEquals(VcSchema(VC_SCHEMA), vcMetadata.vcSchema)
        assertEquals(RawOcaBundle(ocaResponse), vcMetadata.rawOcaBundle)

        coVerify {
            mockFetchTypeMetadata(CREDENTIAL_VCT, URL(CREDENTIAL_VCT), VCT_URL_INTEGRITY)
            mockFetchVcSchema(URL(VC_SCHEMA_URL), VC_SCHEMA_URL_INTEGRITY)
            mockJsonSchemaValidator(any(), any())
            mockFetchOcaBundle(OCA_URL, OCA_URL_INTEGRITY)
        }
    }

    @Test
    fun `Fetching vc metadata for VcSdJwt credential from vct_metadata_uri returns vc metadata`() = runTest {
        val result = useCase(mockVcSdJwtCredential)

        val vcMetadata = result.assertOk()
        assertEquals(VcSchema(VC_SCHEMA), vcMetadata.vcSchema)
        assertEquals(RawOcaBundle(ocaResponse), vcMetadata.rawOcaBundle)

        coVerify {
            mockFetchTypeMetadata(CREDENTIAL_VCT, URL(VCT_METADATA_URI), VCT_METADATA_URI_INTEGRITY)
            mockFetchVcSchema(URL(VC_SCHEMA_URL), VC_SCHEMA_URL_INTEGRITY)
            mockJsonSchemaValidator(any(), any())
            mockFetchOcaBundle(OCA_URL, OCA_URL_INTEGRITY)
        }
    }

    @Test
    fun `Fetching vc metadata for VcSdJwt credential where vct and vct_metadata_uri are available uses vct_metadata_uri`() = runTest {
        val result = useCase(mockVcSdJwtCredential)

        val vcMetadata = result.assertOk()
        assertEquals(VcSchema(VC_SCHEMA), vcMetadata.vcSchema)
        assertEquals(RawOcaBundle(ocaResponse), vcMetadata.rawOcaBundle)

        coVerify {
            mockFetchTypeMetadata(CREDENTIAL_VCT, URL(VCT_METADATA_URI), VCT_METADATA_URI_INTEGRITY)
            mockFetchVcSchema(URL(VC_SCHEMA_URL), VC_SCHEMA_URL_INTEGRITY)
            mockJsonSchemaValidator(any(), any())
            mockFetchOcaBundle(OCA_URL, OCA_URL_INTEGRITY)
        }
    }

    @Test
    fun `Fetching vc metadata for VcSdJwt credential where vct is no url does not fetch anything`() = runTest {
        every { mockVcSdJwtCredential.vctMetadataUri } returns null
        every { mockVcSdJwtCredential.vct } returns "not a url"
        every { mockVcSdJwtCredential.vctIntegrity } returns null
        coEvery {
            mockResolveMetaDataIntegrity(any())
        } returns Ok(Pair(null, null))

        val result = useCase(mockVcSdJwtCredential).assertOk()
        assertNull(result.vcSchema)
        assertNull(result.rawOcaBundle)

        coVerify(exactly = 0) {
            mockFetchTypeMetadata(any(), any(), any())
            mockFetchVcSchema(any(), any())
            mockJsonSchemaValidator(any(), any())
            mockFetchOcaBundle(any(), any())
        }
    }

    @Test
    fun `Fetching vc metadata for VcSdJwt credential maps errors from resolving meta data integrity`() = runTest {
        coEvery {
            mockResolveMetaDataIntegrity(any())
        } returns Err(OcaError.Unexpected(IllegalStateException("Vct is not a url, but vct#integrity is provided")))

        useCase(mockVcSdJwtCredential).assertErrorType(OcaError.Unexpected::class)
    }

    @Test
    fun `Fetching vc metadata for VcSdJwt credential maps errors from fetching type metadata`() = runTest {
        coEvery { mockFetchTypeMetadata(any(), any(), any()) } returns Err(TypeMetadataError.InvalidData)

        useCase(mockVcSdJwtCredential).assertErrorType(OcaError.InvalidOca::class)
    }

    @Test
    fun `Fetching vc metadata for VcSdJwt credential where type metadata schema url is not provided does not fetch vc schema`() = runTest {
        val typeMetadata = safeJson.safeDecodeStringTo<TypeMetadata>(typeMetadataWithoutVcSchemaUrl).value
        coEvery {
            mockFetchTypeMetadata(CREDENTIAL_VCT, URL(VCT_METADATA_URI), VCT_METADATA_URI_INTEGRITY)
        } returns Ok(typeMetadata)

        val result = useCase(mockVcSdJwtCredential).assertOk()
        assertNull(result.vcSchema)

        coVerify(exactly = 0) {
            mockFetchVcSchema(any(), any())
        }
    }

    @Test
    fun `Fetching vc metadata for VcSdJwt credential where type metadata schema url is not a url returns an error`() = runTest {
        val typeMetadata = safeJson.safeDecodeStringTo<TypeMetadata>(typeMetadataWithInvalidVcSchemaUrl).value
        coEvery {
            mockFetchTypeMetadata(CREDENTIAL_VCT, URL(VCT_METADATA_URI), VCT_METADATA_URI_INTEGRITY)
        } returns Ok(typeMetadata)

        useCase(mockVcSdJwtCredential).assertErrorType(OcaError.InvalidOca::class)
    }

    @Test
    fun `Fetching vc metadata for VcSdJwt credential maps errors from fetching vc schema`() = runTest {
        coEvery {
            mockFetchVcSchema(URL(VC_SCHEMA_URL), VC_SCHEMA_URL_INTEGRITY)
        } returns Err(VcSchemaError.InvalidVcSchema)

        useCase(mockVcSdJwtCredential).assertErrorType(OcaError.InvalidOca::class)
    }

    @Test
    fun `Fetching vc metadata for VcSdJwt credential without oca rendering does not fetch the oca bundle`() = runTest {
        val typeMetadata = safeJson.safeDecodeStringTo<TypeMetadata>(typeMetadataWithoutOcaRendering).value
        coEvery {
            mockFetchTypeMetadata(CREDENTIAL_VCT, URL(VCT_METADATA_URI), VCT_METADATA_URI_INTEGRITY)
        } returns Ok(typeMetadata)

        val result = useCase(mockVcSdJwtCredential).assertOk()
        assertNull(result.rawOcaBundle)

        coVerify(exactly = 0) {
            mockFetchOcaBundle(any(), any())
        }
    }

    @Test
    fun `Fetching vc metadata for VcSdJwt credential with multiple oca renderings uses the first one`() = runTest {
        val typeMetadata = safeJson.safeDecodeStringTo<TypeMetadata>(typeMetadataWithOcaMultipleRenderings).value
        coEvery {
            mockFetchTypeMetadata(CREDENTIAL_VCT, URL(VCT_METADATA_URI), VCT_METADATA_URI_INTEGRITY)
        } returns Ok(typeMetadata)

        val result = useCase(mockVcSdJwtCredential).assertOk()
        assertNotNull(result.rawOcaBundle)

        coVerify {
            mockFetchOcaBundle(OCA_URL, OCA_URL_INTEGRITY)
        }
    }

    @Test
    fun `Fetching vc metadata for VcSdJwt credential maps errors from fetching the oca bundle`() = runTest {
        coEvery { mockFetchOcaBundle(any(), any()) } returns Err(OcaError.InvalidOca)

        useCase(mockVcSdJwtCredential).assertErrorType(OcaError.InvalidOca::class)
    }

    @Test
    fun `For VcSdJwt, no Json schema is validated if no vc schema is returned`() = runTest {
        coEvery {
            mockFetchVcSchema(URL(VC_SCHEMA_URL), VC_SCHEMA_URL_INTEGRITY)
        } returns Err(VcSchemaError.Unexpected(null))

        useCase(mockVcSdJwtCredential)

        coVerify(exactly = 0) {
            mockJsonSchemaValidator(any(), any())
        }
    }

    @Test
    fun `Fetching vc metadata for other credential format returns an error`() = runTest {
        val otherCredential = mockk<AnyCredential>()
        every { otherCredential.format } returns CredentialFormat.UNKNOWN

        useCase(otherCredential)
            .assertErrorType(OcaError.UnsupportedCredentialFormat::class)
    }

    private fun setupDefaultMocks() {
        every { mockVcSdJwtCredential.format } returns CredentialFormat.VC_SD_JWT
        every { mockVcSdJwtCredential.vct } returns CREDENTIAL_VCT
        every { mockVcSdJwtCredential.vctIntegrity } returns VCT_URL_INTEGRITY
        every { mockVcSdJwtCredential.vctMetadataUri } returns VCT_METADATA_URI
        every { mockVcSdJwtCredential.vctMetadataUriIntegrity } returns VCT_METADATA_URI_INTEGRITY
        every {
            mockVcSdJwtCredential.getClaimsForPresentation()
        } returns parseToJsonElement(CREDENTIAL_CLAIMS_FOR_PRESENTATION)

        val typeMetadata = safeJson.safeDecodeStringTo<TypeMetadata>(typeMetadataFullExample).value
        coEvery {
            mockFetchTypeMetadata(CREDENTIAL_VCT, URL(CREDENTIAL_VCT), VCT_URL_INTEGRITY)
        } returns Ok(typeMetadata)
        coEvery {
            mockFetchTypeMetadata(CREDENTIAL_VCT, URL(TYPE_METADATA_URL), VCT_URL_INTEGRITY)
        } returns Ok(typeMetadata)
        coEvery {
            mockFetchTypeMetadata(CREDENTIAL_VCT, URL(VCT_METADATA_URI), VCT_METADATA_URI_INTEGRITY)
        } returns Ok(typeMetadata)

        coEvery { mockJsonSchemaValidator(any(), VC_SCHEMA) } returns Ok(Unit)

        coEvery { mockFetchVcSchema(URL(VC_SCHEMA_URL), VC_SCHEMA_URL_INTEGRITY) } returns Ok(VcSchema(VC_SCHEMA))

        coEvery { mockFetchOcaBundle(OCA_URL, OCA_URL_INTEGRITY) } returns Ok(RawOcaBundle(ocaResponse))
        coEvery {
            mockResolveMetaDataIntegrity(any())
        } returns Ok(Pair(URL(VCT_METADATA_URI), VCT_METADATA_URI_INTEGRITY))
    }

    private companion object {
        const val VC_SCHEMA = "vc schema"
        val CREDENTIAL_CLAIMS_FOR_PRESENTATION = """
            {
                "key":"value"
            }
        """.trimIndent()
    }
}
