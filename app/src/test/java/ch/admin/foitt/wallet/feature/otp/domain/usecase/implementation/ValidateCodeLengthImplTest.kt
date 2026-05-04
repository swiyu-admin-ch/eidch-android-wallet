package ch.admin.foitt.wallet.feature.otp.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.otp.domain.model.OtpValidationState
import ch.admin.foitt.wallet.feature.otp.domain.usecase.ValidateCodeLength
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class ValidateCodeLengthImplTest {

    private lateinit var useCase: ValidateCodeLength

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = ValidateCodeLengthImpl()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @TestFactory
    fun `Input code returns correct validation result`(): List<DynamicTest> = inputStrings().flatMap { (expected, strings) ->
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
        OtpValidationState.Valid to listOf(
            stringGenerator(100),
        ),
        OtpValidationState.TooShort to listOf(
            "",
        )
    )

    private fun stringGenerator(length: Int): String {
        val allowedChars = "1234567890"
        val chars = mutableListOf<Char>()
        repeat(length) {
            chars.add(allowedChars.random())
        }
        return chars.joinToString("")
    }
}
