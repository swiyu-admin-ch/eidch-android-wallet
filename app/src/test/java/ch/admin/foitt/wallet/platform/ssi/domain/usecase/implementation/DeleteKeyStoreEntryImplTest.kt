package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.security.KeyStore

class DeleteKeyStoreEntryImplTest {

    private lateinit var useCase: DeleteKeyStoreEntryImpl

    val keyStore = mockk<KeyStore>()

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = DeleteKeyStoreEntryImpl()

        // Mock static method for KeyStore
        mockkStatic(KeyStore::class)

        every { KeyStore.getInstance(any<String>()) } returns keyStore
        every { keyStore.load(null) } just runs
        every { keyStore.deleteEntry(KEY_ID) } just runs
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Deletion runs certain methods`() = runTest {
        useCase.invoke(KEY_ID)

        verify(exactly = 1) { KeyStore.getInstance(any<String>()) }
        verify(exactly = 1) { keyStore.load(null) }
        verify(exactly = 1) { keyStore.deleteEntry(KEY_ID) }
    }

    @Test
    fun `Failure does not throw`() = runTest {
        val keyIdentifier = "failing-key-id"
        val failure = RuntimeException("delete failed")
        every { keyStore.deleteEntry(keyIdentifier) } throws failure

        // Should not throw
        useCase.invoke(keyIdentifier)
    }

    companion object Companion {
        private const val KEY_ID = "test-key-id"
    }
}
