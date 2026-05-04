package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.di.DefaultDispatcher
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.Field
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.Filter
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptor
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.GetRequestedFieldsError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestField
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.toGetRequestedFieldsError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.GetRequestedFields
import ch.admin.foitt.wallet.platform.oca.domain.util.naiveJsonPathToClaimsPathPointer
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class GetRequestedFieldsImpl @Inject constructor(
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : GetRequestedFields {
    override suspend fun invoke(
        credentialJson: String,
        inputDescriptors: List<InputDescriptor>
    ): Result<List<PresentationRequestField>, GetRequestedFieldsError> = withContext(defaultDispatcher) {
        coroutineBinding {
            inputDescriptors.flatMap { descriptor ->
                getMatchingClaims(credentialJson, descriptor.constraints.fields).bind()
            }
        }
    }

    private fun getMatchingClaims(
        credentialJson: String,
        fields: List<Field>
    ): Result<List<PresentationRequestField>, GetRequestedFieldsError> = runSuspendCatching {
        val credentialDocument = JsonPath.using(
            Configuration.defaultConfiguration().addOptions(Option.ALWAYS_RETURN_LIST)
        ).parse(credentialJson)
        fields.mapNotNull { field ->
            val matchingClaim = getMatchingClaim(credentialDocument, field)
            if (matchingClaim == null) {
                return@runSuspendCatching emptyList()
            } else if (matchingClaim.second.size == 1) {
                PresentationRequestField(
                    path = naiveJsonPathToClaimsPathPointer(matchingClaim.first),
                    value = matchingClaim.second.first().toString()
                )
            } else {
                null
            }
        }
    }.mapError { throwable ->
        throwable.toGetRequestedFieldsError("getMatchingClaims error")
    }

    private fun getMatchingClaim(credentialDocument: DocumentContext, field: Field): Pair<String, List<Any>>? {
        field.path.forEach { path ->
            try {
                val values: List<Any> = credentialDocument.read(path)

                val filteredValues = field.getFilterIfValid(path)?.let { filter ->
                    Timber.d("Presentation valid filter: $filter")
                    filterMatchingValues(values, filter)
                } ?: values
                return path to filteredValues
            } catch (e: Exception) {
                // jsonPath does not match credential format, try next one
                Timber.e(e)
            }
        }
        return null
    }

    private fun filterMatchingValues(values: List<Any>, filter: Filter): List<Any> =
        values.filter { value ->
            value.toString() == filter.const
        }

    private fun Field.getFilterIfValid(path: String): Filter? {
        val filter = filter
        return when {
            filter == null -> null
            filter.type != Filter.TYPE_STRING -> null
            filter.const.isNullOrBlank() -> null
            path != PATH_VC_SDJWT -> null
            else -> filter
        }
    }

    companion object {
        private const val PATH_VC_SDJWT = "$.vct"
    }
}
