package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.Claim
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialMetadata
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.OidCredentialDisplay
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.VcSdJwtCredentialConfiguration
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyClaimDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyCredentialDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateMetadataClaimDisplays
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateMetadataDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.Cluster
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GenerateMetadataDisplaysImplTest {
    @MockK
    private lateinit var mockGenerateMetadataClaimDisplays: GenerateMetadataClaimDisplays

    @MockK
    private lateinit var mockCredentialConfiguration: VcSdJwtCredentialConfiguration

    @MockK
    private lateinit var mockCredentialMetadata: CredentialMetadata

    @MockK
    private lateinit var mockMetadataClaim: Claim

    @MockK
    private lateinit var mockClaim: CredentialClaim

    @MockK
    private lateinit var mockClaimDisplays: List<AnyClaimDisplay>

    @MockK
    private lateinit var mockClaimPair: Pair<CredentialClaim, List<AnyClaimDisplay>>

    private lateinit var useCase: GenerateMetadataDisplays

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = GenerateMetadataDisplaysImpl(
            generateMetadataClaimDisplays = mockGenerateMetadataClaimDisplays,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Generating metadata displays for flat json returns meta displays`() = runTest {
        val result = useCase(jsonObject, mockCredentialConfiguration).assertOk()

        assertCredentialDisplays(result.credentialDisplays)
        assertFlatClaims(result.clusters)
    }

    @Test
    fun `Generating metadata displays for flat json passes arguments`() = runTest {
        useCase(jsonObject, mockCredentialConfiguration).assertOk()

        coVerify(exactly = 1) {
            mockGenerateMetadataClaimDisplays(any(), any(), any(), any())
        }
    }

    @Test
    fun `Generating metadata displays for null json object returns meta displays`() = runTest {
        val result = useCase(null, mockCredentialConfiguration).assertOk()

        assertCredentialDisplays(result.credentialDisplays)
        assertEquals(0, result.clusters.size)
    }

    @Test
    fun `Generating metadata displays when no metadata is available returns meta displays`() = runTest {
        every { mockCredentialMetadata.display } returns null
        every { mockCredentialMetadata.claims } returns null
        mockGenerateClaim(metadataClaim = null, order = -1)

        val result = useCase(jsonObject, mockCredentialConfiguration).assertOk()

        assertEquals(1, result.credentialDisplays.size)
        assertEquals(DisplayLanguage.FALLBACK, result.credentialDisplays[0].locale)
        assertEquals(IDENTIFIER, result.credentialDisplays[0].name)
        assertFlatClaims(result.clusters)

        coVerify(exactly = 1) {
            mockGenerateMetadataClaimDisplays(
                claimsPathPointer = claimPathPointer,
                jsonPrimitive = jsonPrimitive,
                metadataClaim = null,
                order = -1,
            )
        }
    }

    @Test
    fun `A GenerateMetadataClaimDisplays error is mapped`() = runTest {
        val exception = Exception("claim exception")
        coEvery {
            mockGenerateMetadataClaimDisplays(any<ClaimsPathPointer>(), any(), any(), any())
        } returns Err(CredentialError.Unexpected(exception))

        val error = useCase(jsonObject, mockCredentialConfiguration).assertErrorType(CredentialError.Unexpected::class)

        assertEquals(exception, error.cause)
    }

    private fun setupDefaultMocks() {
        every { mockCredentialConfiguration.identifier } returns IDENTIFIER
        every { mockCredentialConfiguration.credentialMetadata } returns mockCredentialMetadata

        every { mockMetadataClaim.path } returns claimPathPointer
        every { mockCredentialMetadata.display } returns listOf(OidCredentialDisplay(name = CREDENTIAL_NAME, locale = LANGUAGE))
        every { mockCredentialMetadata.claims } returns listOf(mockMetadataClaim)

        every { mockClaimPair.component1() } returns mockClaim
        every { mockClaimPair.component2() } returns mockClaimDisplays
        mockGenerateClaim()
    }

    private fun mockGenerateClaim(
        path: ClaimsPathPointer = claimPathPointer,
        jsonValue: JsonPrimitive = jsonPrimitive,
        metadataClaim: Claim? = mockMetadataClaim,
        order: Int = 0,
    ) {
        coEvery {
            mockGenerateMetadataClaimDisplays(
                claimsPathPointer = path,
                jsonPrimitive = jsonValue,
                metadataClaim = metadataClaim,
                order = order,
            )
        } returns Ok(mockClaim to mockClaimDisplays)
    }

    private fun assertCredentialDisplays(credentialDisplays: List<AnyCredentialDisplay>) {
        assertEquals(2, credentialDisplays.size)
        assertEquals(LANGUAGE, credentialDisplays[0].locale)
        assertEquals(CREDENTIAL_NAME, credentialDisplays[0].name)
        assertEquals(DisplayLanguage.FALLBACK, credentialDisplays[1].locale)
        assertEquals(IDENTIFIER, credentialDisplays[1].name)
    }

    private fun assertFlatClaims(clusters: List<Cluster>) {
        assertEquals(1, clusters.size)
        val cluster = clusters.first()
        assertEquals(cluster.path, "[]")
        assertEquals(mockClaimDisplays, cluster.claims[mockClaim])
    }

    private companion object {
        const val IDENTIFIER = "identifier"
        const val CREDENTIAL_NAME = "credential"
        const val KEY = "key"
        const val LANGUAGE = "language"
        val claimPathPointer = listOf(ClaimsPathPointerComponent.String(KEY))
        val jsonPrimitive = JsonPrimitive("value")

        val jsonObject = JsonObject(mapOf(KEY to jsonPrimitive))
    }
}
