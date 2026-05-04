package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptorFormat
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationDefinition
import ch.admin.foitt.swiyu.shared.dcql.DcqlSupport
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.toAnyCredentials
import ch.admin.foitt.wallet.platform.credential.domain.model.toGetCompatibleCredentialsError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.GetCompatibleCredentialsError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.GetRequestedFieldsError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestField
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.toGetCompatibleCredentialsError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.GetCompatibleCredentials
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.GetRequestedFields
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithBundleItemsWithKeyBinding
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithKeyBindingRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialWithBundleItemsWithKeyBindingRepository
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.coroutines.CoroutineBindingScope
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import timber.log.Timber
import uniffi.heidi_credentials_rust.PointerPart
import uniffi.heidi_dcql_rust.DcqlQuery
import javax.inject.Inject

class GetCompatibleCredentialsImpl @Inject constructor(
    private val verifiableCredentialWithBundleItemsWithKeyBindingRepository: VerifiableCredentialWithBundleItemsWithKeyBindingRepository,
    private val getRequestedFields: GetRequestedFields,
    private val safeJson: SafeJson
) : GetCompatibleCredentials {
    override suspend fun invoke(
        authorizationRequest: AuthorizationRequest
    ): Result<Set<CompatibleCredential>, GetCompatibleCredentialsError> =
        verifiableCredentialWithBundleItemsWithKeyBindingRepository.getAll()
            .mapError(CredentialWithKeyBindingRepositoryError::toGetCompatibleCredentialsError)
            .andThen { credentials ->
                findCompatibleCredentials(credentials, authorizationRequest)
            }

    private suspend fun findCompatibleCredentials(
        credentialsWithBundleItems: List<VerifiableCredentialWithBundleItemsWithKeyBinding>,
        authorizationRequest: AuthorizationRequest
    ): Result<Set<CompatibleCredential>, GetCompatibleCredentialsError> = coroutineBinding {
        val dcqlQuery = authorizationRequest.dcqlQuery
        val presentationDefinition = authorizationRequest.presentationDefinition
        if (dcqlQuery != null) {
            Timber.d("findCompatibleCredentials: dcqlQuery = $dcqlQuery")
            findCompatibleCredentialsByDcqlQuery(dcqlQuery, credentialsWithBundleItems)
        } else if (presentationDefinition != null) {
            Timber.d("findCompatibleCredentials: presentationDefinition = $presentationDefinition")
            findCompatibleCredentialsByPresentationDefinition(presentationDefinition, credentialsWithBundleItems)
        } else {
            emptySet()
        }
    }

    private suspend fun CoroutineBindingScope<GetCompatibleCredentialsError>.findCompatibleCredentialsByDcqlQuery(
        dcqlQuery: DcqlQuery,
        credentialsWithBundleItems: List<VerifiableCredentialWithBundleItemsWithKeyBinding>
    ): Set<CompatibleCredential> = buildSet {
        DcqlSupport.matchDcqlCredentials(
            dcqlQuery,
            credentialsWithBundleItems.map {
                it.bundleItemsWithKeyBinding.first().bundleItem.payload
            }
        ).map { dcqlCredentialMatch ->
            credentialsWithBundleItems.find {
                it.bundleItemsWithKeyBinding.first().bundleItem.payload == dcqlCredentialMatch.credentialPayload &&
                    it.verifiableCredential.progressionState == VerifiableProgressionState.ACCEPTED
            }?.let { credentialWithBundleItem ->
                add(
                    CompatibleCredential(
                        credentialWithBundleItem.credential.id,
                        dcqlCredentialMatch.claimValues.map {
                            PresentationRequestField(
                                path = it.paths.toClaimsPathPointer(),
                                value = safeJson.safeEncodeObjectToString(it.value).mapError(
                                    JsonParsingError::toGetCompatibleCredentialsError
                                ).bind()
                            )
                        },
                        dcqlCredentialMatch.credentialQueryId
                    )
                )
            }
        }
    }

    private suspend fun CoroutineBindingScope<GetCompatibleCredentialsError>.findCompatibleCredentialsByPresentationDefinition(
        presentationDefinition: PresentationDefinition,
        credentialsWithBundleItems: List<VerifiableCredentialWithBundleItemsWithKeyBinding>
    ): Set<CompatibleCredential> = buildSet {
        credentialsWithBundleItems.forEach { credentialWithBundleItems ->
            runSuspendCatching {
                val credential = credentialWithBundleItems.credential
                val verifiableCredential = credentialWithBundleItems.verifiableCredential
                if (verifiableCredential.progressionState == VerifiableProgressionState.ACCEPTED) {
                    val inputDescriptors = presentationDefinition.inputDescriptors
                    val inputDescriptor = inputDescriptors.first()
                    val compatibleFormat = getCompatibleFormat(
                        credentialFormat = credential.format,
                        inputDescriptorFormats = inputDescriptor.formats
                    )
                    val anyCredential = credentialWithBundleItems.toAnyCredentials()
                        .mapError(AnyCredentialError::toGetCompatibleCredentialsError)
                        .bind().first()
                    if (compatibleFormat != null && isProofTypeCompatible(compatibleFormat, anyCredential.keyBinding)) {
                        val fields = getRequestedFields(anyCredential.getClaimsForPresentation().toString(), inputDescriptors)
                            .mapError(GetRequestedFieldsError::toGetCompatibleCredentialsError)
                            .bind()
                        if (fields.isNotEmpty()) {
                            add(
                                CompatibleCredential(
                                    credentialId = credential.id,
                                    requestedFields = fields,
                                )
                            )
                        }
                    }
                }
            }.mapError { throwable ->
                throwable.toGetCompatibleCredentialsError("findCompatibleCredentials error")
            }.bind()
        }
    }

    private fun getCompatibleFormat(credentialFormat: CredentialFormat, inputDescriptorFormats: List<InputDescriptorFormat>) =
        inputDescriptorFormats.find { it.name == credentialFormat.format }

    private fun isProofTypeCompatible(
        compatibleFormat: InputDescriptorFormat,
        keyBinding: KeyBinding?,
    ) = when (compatibleFormat) {
        is InputDescriptorFormat.VcSdJwt ->
            keyBinding?.algorithm?.let {
                compatibleFormat.kbJwtAlgorithms?.contains(it) == true
            } ?: compatibleFormat.kbJwtAlgorithms.isNullOrEmpty()
    }

    private fun List<PointerPart>.toClaimsPathPointer() =
        map { part ->
            when (part) {
                is PointerPart.Index -> ClaimsPathPointerComponent.Index(part.v1.toInt())
                is PointerPart.Null -> ClaimsPathPointerComponent.Null
                is PointerPart.String -> ClaimsPathPointerComponent.String(part.v1)
            }
        }
}
