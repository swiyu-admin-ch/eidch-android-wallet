package ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.implementation

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CreateKeyGenSpecImplTest {
    @Test
    fun `Test to  make konsist happy`() = runTest {
        // test for this use case is instrumented -> in androidTest directory
        assertEquals(2, 1 + 1)
    }
}
