package ch.admin.foitt.wallet.platform.utils

import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialElement

/**
 * Sorts this list in-place according to the `order` property of each [CredentialElement].
 *
 * Items with a negative order value are treated as having the highest possible priority, ensuring they appear
 * after items with non-negative orders.
 */
fun MutableList<CredentialElement>.sortInPlaceByOrder() = this.sortWith(elementComparator)

/**
 * Returns this list sorted according to the `order` property of each [CredentialElement].
 *
 * Items with a negative order value are treated as having the highest possible priority, ensuring they appear
 * after items with non-negative orders.
 */
fun <T : CredentialElement> Collection<T>.sortByOrder() = this.sortedWith(elementComparator)

private val elementComparator = Comparator<CredentialElement> { element1, element2 ->
    val order1 = if (element1.order < 0) Int.MAX_VALUE else element1.order
    val order2 = if (element2.order < 0) Int.MAX_VALUE else element2.order
    when {
        order1 == order2 -> 0
        order1 < order2 -> -1
        else -> 1
    }
}
