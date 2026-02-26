package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.model.BundleItemEntity
import ch.admin.foitt.wallet.platform.database.domain.model.BundleItemWithKeyBinding
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialKeyBindingEntity
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemWithKeyBindingRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteKeyStoreEntry
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeleteBundleItemsImplTest {

    @MockK
    private lateinit var mockBundleItemWithKeyBindingRepository: BundleItemWithKeyBindingRepository

    @MockK
    private lateinit var mockBundleItemRepository: BundleItemRepository

    @MockK
    private lateinit var mockDeleteKeyStoreEntry: DeleteKeyStoreEntry

    @MockK
    private lateinit var mockBundleItem1: BundleItemEntity

    @MockK
    private lateinit var mockBundleItem2: BundleItemEntity

    @MockK
    private lateinit var mockKeyBinding: CredentialKeyBindingEntity

    private lateinit var useCase: DeleteBundleItemsImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        every { mockBundleItem1.id } returns BUNDLE_ITEM_ID_1
        every { mockBundleItem2.id } returns BUNDLE_ITEM_ID_2
        every { mockKeyBinding.id } returns KEY_ID

        coEvery { mockDeleteKeyStoreEntry(KEY_ID) } returns Unit

        useCase = DeleteBundleItemsImpl(
            bundleItemWithKeyBindingRepository = mockBundleItemWithKeyBindingRepository,
            bundleItemRepository = mockBundleItemRepository,
            deleteKeyStoreEntry = mockDeleteKeyStoreEntry,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Success calls repositories and deletes keystore entries for present key bindings`() = runTest {
        val items = listOf(
            BundleItemWithKeyBinding(bundleItem = mockBundleItem1, keyBinding = mockKeyBinding),
            BundleItemWithKeyBinding(bundleItem = mockBundleItem2, keyBinding = null),
        )

        coEvery { mockBundleItemWithKeyBindingRepository.getByBundleItemIds(BUNDLE_ITEM_IDS) } returns Ok(items)
        coEvery { mockBundleItemRepository.deleteByIds(BUNDLE_ITEM_IDS) } returns Ok(items.size)

        val result = useCase.invoke(BUNDLE_ITEM_IDS).assertOk()
        assertEquals(items.size, result)

        coVerify(exactly = 1) { mockBundleItemWithKeyBindingRepository.getByBundleItemIds(BUNDLE_ITEM_IDS) }
        coVerify(exactly = 1) { mockDeleteKeyStoreEntry(KEY_ID) }
        coVerify(exactly = 1) { mockBundleItemRepository.deleteByIds(BUNDLE_ITEM_IDS) }
    }

    @Test
    fun `No key bindings means no keystore deletions`() = runTest {
        val items = listOf(
            BundleItemWithKeyBinding(bundleItem = mockBundleItem1, keyBinding = null),
            BundleItemWithKeyBinding(bundleItem = mockBundleItem2, keyBinding = null),
        )

        coEvery {
            mockBundleItemWithKeyBindingRepository.getByBundleItemIds(BUNDLE_ITEM_IDS)
        } returns Ok(items)
        coEvery { mockBundleItemRepository.deleteByIds(BUNDLE_ITEM_IDS) } returns Ok(items.size)

        useCase.invoke(BUNDLE_ITEM_IDS).assertOk()

        coVerify(exactly = 1) { mockBundleItemWithKeyBindingRepository.getByBundleItemIds(BUNDLE_ITEM_IDS) }
        coVerify(exactly = 0) { mockDeleteKeyStoreEntry.invoke(any()) }
        coVerify(exactly = 1) { mockBundleItemRepository.deleteByIds(BUNDLE_ITEM_IDS) }
    }

    @Test
    fun `Error while fetching items is mapped to DeleteBundleItemError`() = runTest {
        coEvery {
            mockBundleItemWithKeyBindingRepository.getByBundleItemIds(BUNDLE_ITEM_IDS)
        } returns Err(SsiError.Unexpected(Exception()))

        useCase.invoke(BUNDLE_ITEM_IDS).assertErrorType(SsiError.Unexpected::class)
    }

    @Test
    fun `Error while deleting items is mapped to DeleteBundleItemError`() = runTest {
        val items = listOf(
            BundleItemWithKeyBinding(bundleItem = mockBundleItem1, keyBinding = mockKeyBinding),
            BundleItemWithKeyBinding(bundleItem = mockBundleItem2, keyBinding = null),
        )

        coEvery {
            mockBundleItemWithKeyBindingRepository.getByBundleItemIds(BUNDLE_ITEM_IDS)
        } returns Ok(items)
        coEvery {
            mockBundleItemRepository.deleteByIds(BUNDLE_ITEM_IDS)
        } returns Err(SsiError.Unexpected(Exception()))

        useCase.invoke(BUNDLE_ITEM_IDS).assertErrorType(SsiError.Unexpected::class)
    }

    private companion object {
        const val BUNDLE_ITEM_ID_1 = 1L
        const val BUNDLE_ITEM_ID_2 = 2L
        val BUNDLE_ITEM_IDS = listOf(BUNDLE_ITEM_ID_1, BUNDLE_ITEM_ID_2)
        const val KEY_ID = "private-key-id"
    }
}
