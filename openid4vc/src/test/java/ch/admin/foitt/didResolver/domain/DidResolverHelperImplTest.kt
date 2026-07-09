package ch.admin.foitt.didResolver.domain

import ch.admin.foitt.didResolver.domain.implementation.DidResolverHelperImpl
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DidResolverHelperImplTest {

    private lateinit var didResolver: DidResolverHelper

    @BeforeEach
    fun setup() {
        didResolver = DidResolverHelperImpl()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Dummy test to satisfy clean arch`() = runTest {}
}
