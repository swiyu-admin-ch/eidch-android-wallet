package ch.admin.foitt.wallet.platform.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex

fun Modifier.traversalIndex(index: TraversalIndex) = semantics {
    traversalIndex = index.value
}

fun Modifier.setIsTraversalGroup(isTraversalGroup: Boolean = true, index: TraversalIndex = TraversalIndex.DEFAULT) = semantics {
    this.isTraversalGroup = isTraversalGroup
    this.traversalIndex = index.value
}

fun Modifier.replaceContentDescription(description: String) = clearAndSetSemantics {
    contentDescription = description
}

fun Modifier.contentDescription(description: String) = semantics {
    contentDescription = description
}
