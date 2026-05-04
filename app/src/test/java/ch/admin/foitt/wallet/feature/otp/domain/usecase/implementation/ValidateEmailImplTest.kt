package ch.admin.foitt.wallet.feature.otp.domain.usecase.implementation

import androidx.core.util.PatternsCompat
import ch.admin.foitt.wallet.feature.otp.domain.model.OtpValidationState
import ch.admin.foitt.wallet.feature.otp.domain.usecase.ValidateEmail
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ValidateEmailImplTest {
    private lateinit var useCase: ValidateEmail

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = ValidateEmailImpl()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Valid input text returns valid`() = runTest {
        val input = "input"
        mockkObject(PatternsCompat.EMAIL_ADDRESS)
        every { PatternsCompat.EMAIL_ADDRESS.matcher(input).matches() } returns true

        val result = useCase(input)

        assertEquals(OtpValidationState.Valid, result)
    }

    @Test
    fun `Invalid input text returns invalid`() = runTest {
        val input = "input"
        mockkObject(PatternsCompat.EMAIL_ADDRESS)
        every { PatternsCompat.EMAIL_ADDRESS.matcher(input).matches() } returns false

        val result = useCase(input)

        assertEquals(OtpValidationState.Invalid, result)
    }
}
