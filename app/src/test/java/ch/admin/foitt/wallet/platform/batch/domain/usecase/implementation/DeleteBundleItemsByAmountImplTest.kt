package ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.batch.domain.error.DeleteBundleItemsByAmountError
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeleteBundleItemsByAmountImplTest {

    @MockK
    private lateinit var mockBundleItemRepository: BundleItemRepository

    @MockK
    private lateinit var mockBundleItemWithKeyBindingRepository: BundleItemWithKeyBindingRepository

    @MockK
    private lateinit var mockDeleteKeyStoreEntry: DeleteKeyStoreEntry

    @MockK
    private lateinit var mockBundleItem1: BundleItemEntity

    @MockK
    private lateinit var mockBundleItem2: BundleItemEntity

    @MockK
    private lateinit var mockKeyBinding: CredentialKeyBindingEntity

    private lateinit var useCase: DeleteBundleItemsByAmountImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        every { mockBundleItem1.id } returns BUNDLE_ITEM_ID_1
        every { mockBundleItem2.id } returns BUNDLE_ITEM_ID_2
        every { mockKeyBinding.id } returns KEY_ID

        coEvery { mockDeleteKeyStoreEntry(KEY_ID) } returns Unit

        useCase = DeleteBundleItemsByAmountImpl(
            bundleItemRepository = mockBundleItemRepository,
            bundleItemWithKeyBindingRepository = mockBundleItemWithKeyBindingRepository,
            deleteKeyStoreEntry = mockDeleteKeyStoreEntry,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Success deletes keystore entries for present key bindings and deletes items`() = runTest {
        val items = listOf(
            BundleItemWithKeyBinding(bundleItem = mockBundleItem1, keyBinding = mockKeyBinding),
            BundleItemWithKeyBinding(bundleItem = mockBundleItem2, keyBinding = null),
        )

        coEvery {
            mockBundleItemWithKeyBindingRepository.getBundleItemsWithKeyBindingsToDelete(CREDENTIAL_ID, AMOUNT)
        } returns Ok(items)
        coEvery { mockBundleItemRepository.deleteByIds(BUNDLE_ITEM_IDS) } returns Ok(items.size)

        useCase.invoke(CREDENTIAL_ID, AMOUNT).assertOk()

        coVerify(exactly = 1) {
            mockBundleItemWithKeyBindingRepository.getBundleItemsWithKeyBindingsToDelete(CREDENTIAL_ID, AMOUNT)
        }
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
            mockBundleItemWithKeyBindingRepository.getBundleItemsWithKeyBindingsToDelete(CREDENTIAL_ID, AMOUNT)
        } returns Ok(items)
        coEvery { mockBundleItemRepository.deleteByIds(BUNDLE_ITEM_IDS) } returns Ok(items.size)

        useCase.invoke(CREDENTIAL_ID, AMOUNT).assertOk()

        coVerify(exactly = 1) {
            mockBundleItemWithKeyBindingRepository.getBundleItemsWithKeyBindingsToDelete(CREDENTIAL_ID, AMOUNT)
        }
        coVerify(exactly = 0) { mockDeleteKeyStoreEntry.invoke(any()) }
        coVerify(exactly = 1) { mockBundleItemRepository.deleteByIds(BUNDLE_ITEM_IDS) }
    }

    @Test
    fun `Error while fetching items is mapped to ReducePresentableCredentialCountByError`() = runTest {
        coEvery {
            mockBundleItemWithKeyBindingRepository.getBundleItemsWithKeyBindingsToDelete(CREDENTIAL_ID, AMOUNT)
        } returns Err(SsiError.Unexpected(Exception()))

        useCase.invoke(CREDENTIAL_ID, AMOUNT).assertErrorType(DeleteBundleItemsByAmountError.Unexpected::class)
    }

    @Test
    fun `Error while deleting items is mapped to ReducePresentableCredentialCountByError`() = runTest {
        val items = listOf(
            BundleItemWithKeyBinding(bundleItem = mockBundleItem1, keyBinding = mockKeyBinding),
            BundleItemWithKeyBinding(bundleItem = mockBundleItem2, keyBinding = null),
        )

        coEvery {
            mockBundleItemWithKeyBindingRepository.getBundleItemsWithKeyBindingsToDelete(CREDENTIAL_ID, AMOUNT)
        } returns Ok(items)
        coEvery {
            mockBundleItemRepository.deleteByIds(BUNDLE_ITEM_IDS)
        } returns Err(SsiError.Unexpected(Exception()))

        useCase.invoke(CREDENTIAL_ID, AMOUNT).assertErrorType(DeleteBundleItemsByAmountError.Unexpected::class)
    }

    private companion object {
        const val CREDENTIAL_ID = 42L
        const val AMOUNT = 2
        const val BUNDLE_ITEM_ID_1 = 1L
        const val BUNDLE_ITEM_ID_2 = 2L
        val BUNDLE_ITEM_IDS = listOf(BUNDLE_ITEM_ID_1, BUNDLE_ITEM_ID_2)
        const val KEY_ID = "private-key-id"
    }
}
