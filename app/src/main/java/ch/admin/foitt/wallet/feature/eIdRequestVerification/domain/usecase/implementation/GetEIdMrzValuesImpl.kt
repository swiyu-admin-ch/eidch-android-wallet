package ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation

import ch.admin.foitt.avwrapper.DocumentScanPackageResult
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.EIdRequestVerificationError
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.GetDocumentScanDataError
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.toGetDocumentScanDataError
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.GetEIdMrzValues
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class GetEIdMrzValuesImpl @Inject constructor(
    private val safeJson: SafeJson
) : GetEIdMrzValues {
    override suspend fun invoke(serializedDataList: String): Result<List<String>, GetDocumentScanDataError> = coroutineBinding {
        val rootObject = safeJson.safeDecodeStringTo<JsonObject>(serializedDataList)
            .mapError { error -> error.toGetDocumentScanDataError() }
            .bind()

        val keysToFind = listOf(
            DocumentScanPackageResult.MRZ_LINE1,
            DocumentScanPackageResult.MRZ_LINE2,
            DocumentScanPackageResult.MRZ_LINE3
        )

        val rawMrzValues = keysToFind.mapNotNull { key ->
            findValueInLinkedListJson(rootObject, key)?.let { value ->
                replaceStarWithLessThan(value)
            }
        }.filter { it.isNotEmpty() }

        if (rawMrzValues.isEmpty()) {
            Err(EIdRequestVerificationError.Unexpected(Exception("No MRZ values found in JSON"))).bind()
        }

        rawMrzValues
    }

    private fun replaceStarWithLessThan(value: String): String {
        return value.replace("*", "<")
    }

    private fun findValueInLinkedListJson(root: JsonObject?, targetKey: Int): String? {
        var current = root

        while (current != null) {
            val currentKey = current["b"]?.jsonPrimitive?.contentOrNull?.toIntOrNull()

            if (currentKey == targetKey) {
                return current["c"]?.jsonPrimitive?.contentOrNull
            }
            current = current["d"] as? JsonObject
        }
        return null
    }
}
