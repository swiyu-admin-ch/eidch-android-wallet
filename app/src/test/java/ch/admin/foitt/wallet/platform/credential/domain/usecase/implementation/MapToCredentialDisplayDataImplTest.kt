package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import android.content.Context
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.model.ActorEnvironment
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.usecase.GetActorEnvironment
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.toDisplayStatus
import ch.admin.foitt.wallet.platform.credential.domain.usecase.MapToCredentialDisplayData
import ch.admin.foitt.wallet.platform.database.domain.model.BundleItemEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedAndThemedDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemRepository
import ch.admin.foitt.wallet.platform.theme.domain.model.Theme
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.stream.Stream

class MapToCredentialDisplayDataImplTest {

    @MockK
    private lateinit var mockGetLocalizedAndThemedDisplay: GetLocalizedAndThemedDisplay

    @MockK
    private lateinit var mockGetActorEnvironment: GetActorEnvironment

    @MockK
    private lateinit var mockBundleItemRepository: BundleItemRepository

    @MockK
    private lateinit var mockAppContext: Context

    @MockK
    private lateinit var mockVerifiableCredential: VerifiableCredentialEntity

    private lateinit var useCase: MapToCredentialDisplayData

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = MapToCredentialDisplayDataImpl(
            mockAppContext,
            mockGetLocalizedAndThemedDisplay,
            mockGetActorEnvironment,
            mockBundleItemRepository,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Valid input returns credential display data`() = runTest {
        val result = useCase(mockVerifiableCredential, credentialDisplays, claims)

        val displayData = result.assertOk()
        assertEquals(CREDENTIAL_ID, displayData.credentialId)
        assertEquals(CredentialStatus.VALID.toDisplayStatus(), displayData.status)
        assertEquals(NAME, displayData.title)
        assertEquals(DESCRIPTION, displayData.subtitle)
        assertEquals(LOGO_URI, displayData.logoUri)
        assertEquals(BACKGROUND_COLOR, displayData.backgroundColor)
        assertEquals(ActorEnvironment.PRODUCTION, displayData.actorEnvironment)
        assertEquals(progressionState, displayData.progressionState)
    }

    @ParameterizedTest
    @MethodSource("environmentInputs")
    fun `Credential issuer environment check is indicated in the result`(
        actorEnvironment: ActorEnvironment,
    ) = runTest {
        coEvery { mockGetActorEnvironment(ISSUER) } returns actorEnvironment

        val result = useCase(mockVerifiableCredential, credentialDisplays, claims)

        val displayData = result.assertOk()
        assertEquals(actorEnvironment, displayData.actorEnvironment)
    }

    @Test
    fun `Mapping the credential display data maps errors from the GetLocalizedDisplay use case`() = runTest {
        coEvery {
            mockGetLocalizedAndThemedDisplay(listOf(element = credentialDisplay), preferredTheme = Theme.LIGHT)
        } returns null

        val result = useCase(mockVerifiableCredential, credentialDisplays, claims)
        result.assertErrorType(CredentialError.Unexpected::class)
    }

    @Test
    fun `Mapping the credential display data correctly resolves a simple template`() = runTest {
        val credentialDisplays = listOf(credentialDisplaySimpleTemplate)
        coEvery {
            mockGetLocalizedAndThemedDisplay(credentialDisplays = credentialDisplays, preferredTheme = Theme.LIGHT)
        } returns credentialDisplaySimpleTemplate

        val result = useCase(
            verifiableCredential = mockVerifiableCredential,
            credentialDisplays = credentialDisplays,
            claims = claims
        ).assertOk()

        assertEquals("Test: value1", result.subtitle)
    }

    @Test
    fun `Mapping the credential display data correctly resolves a multi template`() = runTest {
        val credentialDisplays = listOf(credentialDisplayMultiTemplate)
        coEvery {
            mockGetLocalizedAndThemedDisplay(credentialDisplays = credentialDisplays, preferredTheme = Theme.LIGHT)
        } returns credentialDisplayMultiTemplate

        val claims = listOf(
            CredentialClaimWithDisplays(
                claim = claim1,
                displays = listOf(claimDisplay)
            ),
            CredentialClaimWithDisplays(
                claim = claim2,
                displays = listOf(claimDisplay)
            )
        )
        val result = useCase(mockVerifiableCredential, credentialDisplays, claims).assertOk()

        assertEquals("Test: value1, value2", result.subtitle)
    }

    @Test
    fun `Mapping the credential display where the template references an unknown key is replaced by empty string`() = runTest {
        val credentialDisplays = listOf(credentialDisplayUnknownKey)
        coEvery {
            mockGetLocalizedAndThemedDisplay(credentialDisplays = credentialDisplays, preferredTheme = Theme.LIGHT)
        } returns credentialDisplayUnknownKey

        val result = useCase(mockVerifiableCredential, credentialDisplays, claims).assertOk()

        assertEquals("Test: ", result.subtitle)
    }

    @Test
    fun `Mapping the credential display where the template references an null claim is replaced by hyphen`() = runTest {
        val credentialDisplays = listOf(credentialDisplaySimpleTemplate)
        coEvery {
            mockGetLocalizedAndThemedDisplay(credentialDisplays = credentialDisplays, preferredTheme = Theme.LIGHT)
        } returns credentialDisplaySimpleTemplate

        val result = useCase(mockVerifiableCredential, credentialDisplays, claimsWithNullValue).assertOk()

        assertEquals("Test: –", result.subtitle)
    }

    @ParameterizedTest
    @MethodSource("generateNestedTests")
    fun `Mapping the credential display correctly resolves nested templates`(input: Pair<String, String>) = runTest {
        val credentialDisplay = createCredentialDisplay(input.first)
        val credentialDisplays = listOf(credentialDisplay)
        coEvery {
            mockGetLocalizedAndThemedDisplay(credentialDisplays = credentialDisplays, preferredTheme = Theme.LIGHT)
        } returns credentialDisplay

        val result = useCase(mockVerifiableCredential, credentialDisplays, claims).assertOk()

        assertEquals(input.second, result.subtitle)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "{{$.claim1Key }}", // whitespace
            "{{}}", // empty
            "{{ }}", // empty
            "{$.claim1Key}", // single brackets
            "{$.claim1Key}}", // missing opening bracket
            "{{$.claim1Key}", // missing closing bracket
        ]
    )
    fun `Mapping the credential display does not match and resolve invalid templates`(template: String) = runTest {
        val credentialDisplayInvalidTemplate = createCredentialDisplay(template)
        val credentialDisplays = listOf(credentialDisplayInvalidTemplate)
        coEvery {
            mockGetLocalizedAndThemedDisplay(credentialDisplays = credentialDisplays, preferredTheme = Theme.LIGHT)
        } returns credentialDisplayInvalidTemplate

        val result = useCase(mockVerifiableCredential, credentialDisplays, claims).assertOk()

        assertEquals(template, result.subtitle)
    }

    private fun setupDefaultMocks() {
        every { mockVerifiableCredential.credentialId } returns CREDENTIAL_ID
        every { mockVerifiableCredential.validFrom } returns 0
        every { mockVerifiableCredential.validUntil } returns 17768026519L
        every { mockVerifiableCredential.issuer } returns ISSUER
        every { mockVerifiableCredential.progressionState } returns progressionState

        coEvery {
            mockBundleItemRepository.getAllByCredentialId(CREDENTIAL_ID)
        } returns Ok(
            listOf(
                BundleItemEntity(
                    id = BUNDLE_ITEM_ID,
                    status = CredentialStatus.VALID,
                    credentialId = CREDENTIAL_ID,
                    payload = "payload"
                )
            )
        )

        coEvery {
            mockGetLocalizedAndThemedDisplay(
                credentialDisplays = listOf(credentialDisplay),
                preferredTheme = Theme.LIGHT
            )
        } returns credentialDisplay
        coEvery { mockGetActorEnvironment(ISSUER) } returns ActorEnvironment.PRODUCTION
    }

    private companion object {
        const val CREDENTIAL_ID = 1L
        const val BUNDLE_ITEM_ID = 11L
        const val ISSUER = "issuer"
        const val NAME = "name"
        const val DESCRIPTION = "description"
        const val LOGO_URI = "logoUri"
        const val BACKGROUND_COLOR = "backgroundColor"
        val progressionState = VerifiableProgressionState.ACCEPTED

        val credentialDisplay = createCredentialDisplay(DESCRIPTION)

        val credentialDisplays = listOf(credentialDisplay)

        val credentialDisplaySimpleTemplate = createCredentialDisplay("Test: {{$.claim1Key}}")

        val credentialDisplayMultiTemplate = createCredentialDisplay("Test: {{$.claim1Key}}, {{$.claim2Key}}")

        val credentialDisplayUnknownKey = createCredentialDisplay("Test: {{$.claim3Key}}")

        @JvmStatic
        fun generateNestedTests() = listOf(
            "{{$.claim2Key {{$.claim1Key}}}}" to "{{$.claim2Key value1}}",
            "{{{$.claim1Key}}}" to "{value1}",
            "{{{{$.claim1Key}}}}" to "{{value1}}",
            "{{{$.claim1Key}}}}" to "{value1}}",
        )

        @JvmStatic
        fun environmentInputs(): Stream<Arguments> = Stream.of(
            Arguments.of(ActorEnvironment.PRODUCTION),
            Arguments.of(ActorEnvironment.BETA),
            Arguments.of(ActorEnvironment.EXTERNAL),
        )

        val claim1 = CredentialClaim(
            clusterId = 1,
            key = "claim1Key",
            value = "value1",
            valueType = "string"
        )

        val claim2 = CredentialClaim(
            clusterId = 1,
            key = "claim2Key",
            value = "value2",
            valueType = "string"
        )

        val claimWithNullValue = CredentialClaim(
            clusterId = 1,
            key = "claim1Key",
            value = null,
            valueType = "string"
        )

        val claimDisplay = CredentialClaimDisplay(
            claimId = 1,
            name = "name1",
            locale = "locale1",
            value = null,
        )

        val claims = listOf(
            CredentialClaimWithDisplays(
                claim = claim1,
                displays = listOf(claimDisplay)
            )
        )

        val claimsWithNullValue = listOf(
            CredentialClaimWithDisplays(
                claim = claimWithNullValue,
                displays = listOf(claimDisplay)
            )
        )

        fun createCredentialDisplay(template: String) = CredentialDisplay(
            id = 1,
            credentialId = 1,
            locale = "locale",
            name = NAME,
            description = template,
            logoUri = LOGO_URI,
            backgroundColor = BACKGROUND_COLOR,
        )
    }
}
