package ch.admin.foitt.wallet.platform.claimsPathPointer.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent
import ch.admin.foitt.wallet.platform.claimsPathPointer.domain.usecase.GetClaimsPathPointers
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject

class GetClaimsPathPointersImpl @Inject constructor() : GetClaimsPathPointers {
    override suspend fun invoke(jsonElement: JsonElement): Map<ClaimsPathPointer, JsonElement> =
        buildPaths(jsonElement = jsonElement, currentPath = emptyList()).toMap()

    private fun buildPaths(jsonElement: JsonElement, currentPath: ClaimsPathPointer): Map<ClaimsPathPointer, JsonElement> =
        when (jsonElement) {
            is JsonObject -> buildObjectPaths(jsonElement, currentPath)
            is JsonArray -> buildArrayPaths(jsonElement, currentPath)
            else -> mapOf(currentPath to jsonElement)
        }

    private fun buildObjectPaths(jsonElement: JsonObject, currentPath: ClaimsPathPointer): Map<ClaimsPathPointer, JsonElement> {
        val paths = mutableMapOf<ClaimsPathPointer, JsonElement>()
        if (currentPath.isNotEmpty()) {
            paths[currentPath] = jsonElement
        }
        jsonElement.forEach { (key, value) ->
            paths += buildPaths(jsonElement = value, currentPath = currentPath + ClaimsPathPointerComponent.String(key))
        }
        return paths
    }

    private fun buildArrayPaths(jsonElement: JsonArray, currentPath: ClaimsPathPointer): Map<ClaimsPathPointer, JsonElement> {
        val paths = mutableMapOf<ClaimsPathPointer, JsonElement>()
        paths[currentPath + ClaimsPathPointerComponent.Null] = jsonElement
        jsonElement.forEachIndexed { index, value ->
            val elementPath = currentPath + ClaimsPathPointerComponent.Index(index)
            paths[elementPath] = value
            paths += buildPaths(jsonElement = value, currentPath = elementPath)
        }
        return paths
    }
}
