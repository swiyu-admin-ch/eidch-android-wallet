package ch.admin.foitt.wallet.platform.utils

import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimCluster
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimImage
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimText
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialElement
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CredentialElementCollectionExtTest {

    private val claimWithOrder1 = createClaimText(1)
    private val claimWithOrder2 = createClaimImage(2)
    private val claimWithOrder3 = createClaimCluster(3)

    private val claimWithoutOrder1 = createClaimText(-1)
    private val claimWithoutOrder2 = createClaimImage(-1)
    private val claimWithoutOrder3 = createClaimCluster(-1)

    @Test
    fun `A sorted list of elements with order will be returned in the correct order`() = runTest {
        val list = listOf(claimWithOrder1, claimWithOrder2, claimWithOrder3)
        val result = list.sortByOrder()

        val expected = listOf(claimWithOrder1, claimWithOrder2, claimWithOrder3)
        assertEquals(expected, result)
    }

    @Test
    fun `An unsorted list of elements with order will be returned in the correct order`() = runTest {
        val list = listOf(claimWithOrder2, claimWithOrder3, claimWithOrder1)
        val result = list.sortByOrder()

        val expected = listOf(claimWithOrder1, claimWithOrder2, claimWithOrder3)
        assertEquals(expected, result)
    }

    @Test
    fun `A list of elements with and without order will place items without order at the end`() = runTest {
        val list = listOf(
            claimWithoutOrder2,
            claimWithoutOrder1,
            claimWithOrder1,
            claimWithoutOrder3,
            claimWithOrder2
        )

        val result = list.sortByOrder()

        val expected = listOf(
            claimWithOrder1,
            claimWithOrder2,
            claimWithoutOrder2,
            claimWithoutOrder1,
            claimWithoutOrder3
        )
        assertEquals(expected, result)
    }

    @Test
    fun `A sorted list of elements with order will be sorted in-place in the correct order`() = runTest {
        val list = mutableListOf(claimWithOrder1, claimWithOrder2, claimWithOrder3)
        list.sortInPlaceByOrder()

        val expected = listOf(claimWithOrder1, claimWithOrder2, claimWithOrder3)
        assertEquals(expected, list)
    }

    @Test
    fun `An unsorted list of elements with order will be sorted in-place in the correct order`() = runTest {
        val list = mutableListOf(claimWithOrder2, claimWithOrder3, claimWithOrder1)
        list.sortInPlaceByOrder()

        val expected = listOf(claimWithOrder1, claimWithOrder2, claimWithOrder3)
        assertEquals(expected, list)
    }

    @Test
    fun `A list of elements with and without order will sort items without order in-place at the end`() = runTest {
        val list = mutableListOf(
            claimWithoutOrder2,
            claimWithoutOrder1,
            claimWithOrder1,
            claimWithoutOrder3,
            claimWithOrder2
        )

        list.sortInPlaceByOrder()
        val expected = listOf(
            claimWithOrder1,
            claimWithOrder2,
            claimWithoutOrder2,
            claimWithoutOrder1,
            claimWithoutOrder3
        )
        assertEquals(expected, list)
    }

    @Test
    fun `A list of elements without order are sorted in-place in the same order`() = runTest {
        val list = mutableListOf(claimWithoutOrder2, claimWithoutOrder3, claimWithoutOrder1)

        list.sortInPlaceByOrder()
        val expected = listOf(claimWithoutOrder2, claimWithoutOrder3, claimWithoutOrder1)
        assertEquals(expected, list)
    }

    private fun createClaimText(order: Int): CredentialElement = CredentialClaimText(
        id = 1L,
        localizedLabel = "label",
        order = order,
        value = "value",
        isSensitive = false
    )

    private fun createClaimImage(order: Int): CredentialElement = CredentialClaimImage(
        id = 1L,
        localizedLabel = "label",
        order = order,
        imageData = "data".toByteArray(),
        isSensitive = false
    )

    private fun createClaimCluster(order: Int): CredentialElement = CredentialClaimCluster(
        localizedLabel = "label",
        order = order,
        id = -1L,
        items = mutableListOf(),
        parentId = null
    )
}
