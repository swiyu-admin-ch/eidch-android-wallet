package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.toPointerString
import ch.admin.foitt.wallet.platform.credential.domain.usecase.ResolveClaimTemplate
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class ResolveClaimTemplateImplTest {
    @MockK
    private lateinit var mockGetLocalizedDisplay: GetLocalizedDisplay

    private lateinit var useCase: ResolveClaimTemplate

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = ResolveClaimTemplateImpl(mockGetLocalizedDisplay)

        every { mockGetLocalizedDisplay(listOf(claimDisplay)) } returns claimDisplay
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `ResolveClaimTemplate correctly resolves a simple template`() = runTest {
        val template = "Test: {{$claim1PathString}}"
        val claims = listOf(
            CredentialClaimWithDisplays(
                claim = claim1,
                displays = listOf(claimDisplay)
            ),
        )

        val result = useCase(template = template, claims = claims)
        assertEquals("Test: value1", result)
    }

    @Test
    fun `ResolveClaimTemplate correctly resolves a multi template`() = runTest {
        val template = "Test: {{$claim1PathString}}, {{$claim2PathString}}"
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

        val result = useCase(template = template, claims = claims)
        assertEquals("Test: value1, value2", result)
    }

    @TestFactory
    fun `ResolveClaimTemplate where the template references an unknown key is replaced by empty string`(): List<DynamicTest> {
        val inputs = listOf(
            "Test: {{}}",
            "Test: {{ }}",
            "Test: {{$claim3PathString}}"
        )
        return inputs.map { input ->
            DynamicTest.dynamicTest("input: $input is replaced by empty string") {
                runTest {
                    val claims = listOf(
                        CredentialClaimWithDisplays(
                            claim = claim1,
                            displays = listOf(claimDisplay)
                        ),
                    )

                    val result = useCase(template = input, claims = claims)
                    assertEquals("Test: ", result)
                }
            }
        }
    }

    @Test
    fun `ResolveClaimTemplate where the template references an null claim is replaced by hyphen`() = runTest {
        val template = "Test: {{$claim1PathString}}"
        val claims = listOf(
            CredentialClaimWithDisplays(
                claim = claim1.copy(value = null),
                displays = listOf(claimDisplay)
            ),
        )

        val result = useCase(template = template, claims = claims)
        assertEquals("Test: –", result)
    }

    @Test
    fun `ResolveClaimTemplate where the template references a claim with display is replaced by display value`() = runTest {
        val template = "Test: {{$claim1PathString}}"
        val display = claimDisplay.copy(value = "otherValue")
        val claims = listOf(
            CredentialClaimWithDisplays(
                claim = claim1,
                displays = listOf(display)
            ),
        )
        every { mockGetLocalizedDisplay(listOf(display)) } returns display

        val result = useCase(template = template, claims = claims)
        assertEquals("Test: otherValue", result)
    }

    @TestFactory
    fun `ResolveClaimTemplate correctly resolves array templates`(): List<DynamicTest> {
        val input = listOf(
            "{{$claimArrayPathString}}" to "value1, value2", // uses default separator as none is provided
            "{{$claimArrayPathString.join('; ')}}" to "value1; value2",
            "{{$claimArrayPathString.join(\"; \")}}" to "value1; value2",
            "{{$claimArrayPath1String}}" to "value1",
            "{{$claimArrayPath2String.join('; ')}}" to "value2", // ignores separator, because only 1 element is selected
        )

        return input.map { (template, resolvedResult) ->
            DynamicTest.dynamicTest("template $template is resolved correctly") {
                runTest {
                    val claims = listOf(
                        CredentialClaimWithDisplays(
                            claim = claim1.copy(path = claimArrayPath1String),
                            displays = listOf(claimDisplay)
                        ),
                        CredentialClaimWithDisplays(
                            claim = claim2.copy(path = claimArrayPath2String),
                            displays = listOf(claimDisplay)
                        )
                    )

                    val result = useCase(template, claims)
                    assertEquals(resolvedResult, result)
                }
            }
        }
    }

    @TestFactory
    fun `ResolveClaimTemplate does not match and resolve invalid templates`(): List<DynamicTest> {
        val input = listOf(
            "{$claim1PathString}", // single brackets
            "{$claim1PathString}}", // missing opening bracket
            "{{$claim1PathString}", // missing closing bracket
        )

        return input.map { template ->
            DynamicTest.dynamicTest("template $template is not resolved") {
                runTest {
                    val claims = listOf(
                        CredentialClaimWithDisplays(
                            claim = claim1,
                            displays = listOf(claimDisplay)
                        ),
                    )

                    val result = useCase(template, claims)
                    assertEquals(template, result)
                }
            }
        }
    }

    val claim1Path = listOf(ClaimsPathPointerComponent.String("claim1Key"))
    val claim1PathString = claim1Path.toPointerString()
    val claim2Path = listOf(ClaimsPathPointerComponent.String("claim2Key"))
    val claim2PathString = claim2Path.toPointerString()
    val claim3Path = listOf(ClaimsPathPointerComponent.String("claim3Key"))
    val claim3PathString = claim3Path.toPointerString()

    val claimArrayPath = listOf(
        ClaimsPathPointerComponent.String("claimKey"),
        ClaimsPathPointerComponent.Null,
    )
    val claimArrayPathString = claimArrayPath.toPointerString()
    val claimArrayPath1 = listOf(
        ClaimsPathPointerComponent.String("claimKey"),
        ClaimsPathPointerComponent.Index(0),
    )
    val claimArrayPath1String = claimArrayPath1.toPointerString()
    val claimArrayPath2 = listOf(
        ClaimsPathPointerComponent.String("claimKey"),
        ClaimsPathPointerComponent.Index(1),
    )
    val claimArrayPath2String = claimArrayPath2.toPointerString()

    val claim1 = CredentialClaim(
        clusterId = 1,
        path = claim1PathString,
        value = "value1",
        valueType = "string"
    )

    val claim2 = CredentialClaim(
        clusterId = 1,
        path = claim2PathString,
        value = "value2",
        valueType = "string"
    )

    val claimDisplay = CredentialClaimDisplay(
        claimId = 1,
        name = "name1",
        locale = "locale1",
        value = null,
    )
}
