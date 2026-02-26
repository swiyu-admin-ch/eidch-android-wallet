package ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceTextInputConstraints
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceValidationState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.ValidateTextLength
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class ValidateTextLengthImplTest {

    val nonComplianceTextInputConstraints = NonComplianceTextInputConstraints()

    private lateinit var useCase: ValidateTextLength

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = ValidateTextLengthImpl(
            nonComplianceTextInputConstraints = nonComplianceTextInputConstraints,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @TestFactory
    fun `Input text returns correct validation result`(): List<DynamicTest> = inputStrings().flatMap { (expected, strings) ->
        strings.map { string ->
            DynamicTest.dynamicTest("$string should return $expected") {
                runTest {
                    val actual = useCase(string)
                    assertEquals(expected, actual)
                }
            }
        }
    }

    private fun inputStrings() = mapOf(
        NonComplianceValidationState.Valid to listOf(
            stringGenerator(nonComplianceTextInputConstraints.minLength),
            stringGenerator(nonComplianceTextInputConstraints.maxLength),
            stringGenerator(100),
        ),
        NonComplianceValidationState.TooShort to listOf(
            "",
            stringGenerator(nonComplianceTextInputConstraints.minLength - 1),
        ),
        NonComplianceValidationState.TooLong to listOf(
            stringGenerator(nonComplianceTextInputConstraints.maxLength + 1)
        )
    )

    private fun stringGenerator(length: Int): String {
        val allowedChars = "abcdefghijklmnopqrstuvwxyz"
        val chars = mutableListOf<Char>()
        repeat(length) {
            chars.add(allowedChars.random())
        }
        return chars.joinToString("")
    }
}
