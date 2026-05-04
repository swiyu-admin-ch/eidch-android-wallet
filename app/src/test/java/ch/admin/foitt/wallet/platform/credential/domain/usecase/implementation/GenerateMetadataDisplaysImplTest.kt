package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.toPointerString
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.Claim
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialMetadata
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.OidClaimDisplay
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.OidCredentialDisplay
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.VcSdJwtCredentialConfiguration
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyClaimDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyCredentialDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateMetadataClaimDisplays
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateMetadataDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.Ordering
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GenerateMetadataDisplaysImplTest {
    @MockK
    private lateinit var mockGenerateMetadataClaimDisplays: GenerateMetadataClaimDisplays

    @MockK
    private lateinit var mockMetadata: VcSdJwtCredentialConfiguration

    @MockK
    private lateinit var mockCredentialMetadata: CredentialMetadata

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
    fun `Generating valid metadata displays returns success`() = runTest {
        val result = useCase(credentialClaims, mockMetadata).assertOk()

        val expectedCredentialDisplays = listOf(
            AnyCredentialDisplay(locale = LANGUAGE_EN, name = "credential"),
            AnyCredentialDisplay(locale = DisplayLanguage.FALLBACK, name = IDENTIFIER)
        )

        assertEquals(expectedCredentialDisplays, result.credentialDisplays)
        assertEquals(1, result.clusters.size)
        assertEquals(localizedClaim01, result.clusters.first().claims)

        coVerify {
            mockGenerateMetadataClaimDisplays(
                claimsPathPointer = claimPathPointer,
                claimValueJson = claimValueElement,
                metadataClaim = metadataClaims[0],
                order = 0,
            )
        }
    }

    @Test
    fun `Generating valid metadata displays uses the order defined in the metadata`() = runTest {
        val credentialClaims2 = mapOf(claimPathPointer2 to claimValueElement2) + credentialClaims

        val metadataClaims2 = metadataClaims + listOf(
            Claim(
                path = claimPathPointer2,
                mandatory = true,
                display = listOf(
                    OidClaimDisplay(
                        locale = LANGUAGE_EN,
                        name = "claim_value2 en",
                    )
                )
            )
        )

        every { mockCredentialMetadata.claims } returns metadataClaims2
        coEvery {
            mockGenerateMetadataClaimDisplays(
                claimsPathPointer = claimPathPointer2,
                claimValueJson = claimValueElement2,
                metadataClaim = metadataClaims2[1],
                order = 1,
            )
        } returns Ok(claim02 to claim02Displays)

        useCase(credentialClaims2, mockMetadata).assertOk()

        coVerify(ordering = Ordering.ORDERED) {
            mockGenerateMetadataClaimDisplays(
                claimsPathPointer = claimPathPointer2,
                claimValueJson = claimValueElement2,
                metadataClaim = metadataClaims2[1],
                order = 1,
            )

            mockGenerateMetadataClaimDisplays(
                claimsPathPointer = claimPathPointer,
                claimValueJson = claimValueElement,
                metadataClaim = metadataClaims2[0],
                order = 0,
            )
        }
    }

    @Test
    fun `Generating metadata credential displays adds fallback language if empty`() = runTest {
        every { mockCredentialMetadata.display } returns null

        val result = useCase(credentialClaims, mockMetadata).assertOk()

        val expectedCredentialDisplays = listOf(
            AnyCredentialDisplay(locale = DisplayLanguage.FALLBACK, name = IDENTIFIER)
        )

        assertEquals(expectedCredentialDisplays, result.credentialDisplays)

        coVerify {
            mockGenerateMetadataClaimDisplays(
                claimsPathPointer = claimPathPointer,
                claimValueJson = claimValueElement,
                metadataClaim = metadataClaims[0],
                order = 0,
            )
        }
    }

    @Test
    fun `Generating metadata displays uses empty metadata claims to create displays`() = runTest {
        every { mockCredentialMetadata.claims } returns null
        coEvery {
            mockGenerateMetadataClaimDisplays(
                claimsPathPointer = claimPathPointer,
                claimValueJson = claimValueElement,
                metadataClaim = null,
                order = -1,
            )
        } returns Ok(claim01 to claim01Displays)

        useCase(credentialClaims, mockMetadata).assertOk()

        coVerify {
            mockGenerateMetadataClaimDisplays(
                claimsPathPointer = claimPathPointer,
                claimValueJson = claimValueElement,
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

        val error = useCase(credentialClaims, mockMetadata).assertErrorType(CredentialError.Unexpected::class)

        assertEquals(exception, error.cause)
    }

    private fun setupDefaultMocks() {
        every { mockMetadata.identifier } returns IDENTIFIER
        every { mockMetadata.credentialMetadata } returns mockCredentialMetadata
        every { mockCredentialMetadata.display } returns credentialDisplays
        every { mockCredentialMetadata.claims } returns metadataClaims

        coEvery {
            mockGenerateMetadataClaimDisplays(
                claimsPathPointer = claimPathPointer,
                claimValueJson = claimValueElement,
                metadataClaim = metadataClaims.first(),
                order = 0,
            )
        } returns Ok(claim01 to claim01Displays)
    }

    private companion object {
        const val IDENTIFIER = "identifier"
        val claimPathPointer = listOf(ClaimsPathPointerComponent.String("claim"))
        val claimPathPointer2 = listOf(ClaimsPathPointerComponent.String("claim2"))
        val claimPathPointerString = claimPathPointer.toPointerString()
        val claimPathPointerString2 = claimPathPointer2.toPointerString()
        const val CLAIM_VALUE = "claim_value"
        const val CLAIM_VALUE2 = "claim_value2"
        val claimValueElement = JsonPrimitive(CLAIM_VALUE)
        val claimValueElement2 = JsonPrimitive(CLAIM_VALUE2)
        const val LANGUAGE_EN = "en"
        const val CLAIM_VALUE_EN = "claim value en"
        const val CLAIM_VALUE2_EN = "claim value 2 en"

        val credentialDisplay = OidCredentialDisplay(name = "credential", locale = LANGUAGE_EN)
        val fallbackCredentialDisplay = OidCredentialDisplay(name = IDENTIFIER, locale = DisplayLanguage.FALLBACK)
        val credentialDisplays = listOf(credentialDisplay, fallbackCredentialDisplay)

        val credentialClaims = mapOf<ClaimsPathPointer, JsonElement>(claimPathPointer to claimValueElement)

        val metadataClaims = listOf(
            Claim(
                path = claimPathPointer,
                mandatory = true,
                display = listOf(
                    OidClaimDisplay(
                        locale = LANGUAGE_EN,
                        name = CLAIM_VALUE_EN,
                    )
                )
            )
        )

        val claim01 = CredentialClaim(
            clusterId = -1,
            path = claimPathPointerString,
            value = CLAIM_VALUE,
            valueType = "string",
        )

        val claim01Displays = listOf(
            AnyClaimDisplay(locale = LANGUAGE_EN, name = CLAIM_VALUE_EN),
            AnyClaimDisplay(locale = DisplayLanguage.FALLBACK, name = claimPathPointerString)
        )

        val localizedClaim01 = mapOf(claim01 to claim01Displays)

        val claim02 = CredentialClaim(
            clusterId = -1,
            path = claimPathPointerString2,
            value = CLAIM_VALUE2,
            valueType = "string",
        )

        val claim02Displays = listOf(
            AnyClaimDisplay(locale = LANGUAGE_EN, name = CLAIM_VALUE2_EN),
            AnyClaimDisplay(locale = DisplayLanguage.FALLBACK, name = claimPathPointerString2)
        )

        val localizedClaim02 = mapOf(claim02 to claim02Displays)
    }
}
