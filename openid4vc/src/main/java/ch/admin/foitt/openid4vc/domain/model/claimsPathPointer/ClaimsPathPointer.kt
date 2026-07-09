package ch.admin.foitt.openid4vc.domain.model.claimsPathPointer

import com.github.michaelbull.result.get
import com.github.michaelbull.result.getOr
import com.github.michaelbull.result.runCatching
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

typealias ClaimsPathPointer = List<ClaimsPathPointerComponent>

@Serializable(with = ClaimsPathPointerComponentSerializer::class)
sealed interface ClaimsPathPointerComponent {
    @Serializable(with = ClaimsPathPointerComponentStringSerializer::class)
    data class String(val name: kotlin.String) : ClaimsPathPointerComponent

    @Serializable(with = ClaimsPathPointerComponentNullSerializer::class)
    data object Null : ClaimsPathPointerComponent

    @Serializable(with = ClaimsPathPointerComponentIndexSerializer::class)
    data class Index(val index: Int) : ClaimsPathPointerComponent
}

fun ClaimsPathPointer.toPointerString() = runCatching { Json.encodeToString(this) }.getOr("")

fun claimsPathPointerFrom(rawString: String): ClaimsPathPointer? =
    runCatching {
        Json.decodeFromString<ClaimsPathPointer>(rawString)
    }.get()

fun ClaimsPathPointer.pointsAtSetOf(otherPath: ClaimsPathPointer, enforceLength: Boolean = false): Boolean {
    if (enforceLength && size != otherPath.size || size > otherPath.size) {
        return false
    }
    zip(otherPath) { component, otherComponent ->
        if (component != otherComponent) {
            if (component != ClaimsPathPointerComponent.Null || otherComponent !is ClaimsPathPointerComponent.Index) {
                return false
            }
        }
    }
    return true
}

val ClaimsPathPointer.allIndices: List<Int>
    get() = mapNotNull {
        val component = it as? ClaimsPathPointerComponent.Index
        component?.index
    }

fun ClaimsPathPointer.resolveWildCards(indices: List<Int>): ClaimsPathPointer {
    var indices = indices
    return map { component ->
        if (component is ClaimsPathPointerComponent.Null) {
            val newComponent = indices.firstOrNull()?.let {
                ClaimsPathPointerComponent.Index(it)
            } ?: component
            indices = indices.drop(1)
            newComponent
        } else {
            component
        }
    }
}
