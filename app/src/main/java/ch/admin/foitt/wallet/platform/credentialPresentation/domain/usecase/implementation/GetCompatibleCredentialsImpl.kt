package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.Validity
import ch.admin.foitt.openid4vc.domain.model.anycredential.getValidity
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.swiyu.shared.dcql.DcqlSupport
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.toAnyCredentials
import ch.admin.foitt.wallet.platform.credential.domain.model.toGetCompatibleCredentialsError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.GetCompatibleCredentialsError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.toGetCompatibleCredentialsError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.GetCompatibleCredentials
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithBundleItemsWithKeyBinding
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithKeyBindingRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialWithBundleItemsWithKeyBindingRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import timber.log.Timber
import uniffi.heidi_credentials_rust.PointerPart
import uniffi.heidi_dcql_rust.DcqlQuery
import javax.inject.Inject

class GetCompatibleCredentialsImpl @Inject constructor(
    private val verifiableCredentialWithBundleItemsWithKeyBindingRepository: VerifiableCredentialWithBundleItemsWithKeyBindingRepository,
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
        if (dcqlQuery != null) {
            Timber.d("findCompatibleCredentials: dcqlQuery = $dcqlQuery")
            val validCredentials = filterOnValidityAndStatus(credentialsWithBundleItems)
            findCompatibleCredentialsByDcqlQuery(dcqlQuery, validCredentials).bind()
        } else {
            emptySet()
        }
    }

    private fun filterOnValidityAndStatus(
        credentialsWithBundleItems: List<VerifiableCredentialWithBundleItemsWithKeyBinding>
    ): List<VerifiableCredentialWithBundleItemsWithKeyBinding> {
        return credentialsWithBundleItems.filter { credentialWithBundleItem ->
            val validity = getValidity(
                credentialWithBundleItem.verifiableCredential.validFrom,
                credentialWithBundleItem.verifiableCredential.validUntil
            )
            val valid = when (validity) {
                is Validity.Expired,
                is Validity.NotYetValid -> false

                is Validity.BusinessExpired,
                is Validity.Valid -> true
            }
            if (!valid) {
                return@filter false
            }
            val status = credentialWithBundleItem.nextBundleItemToPresent.status
            return@filter when (status) {
                CredentialStatus.REVOKED -> false
                CredentialStatus.VALID,
                CredentialStatus.SUSPENDED,
                CredentialStatus.UNSUPPORTED,
                CredentialStatus.UNKNOWN -> true
            }
        }
    }

    private fun findCompatibleCredentialsByDcqlQuery(
        dcqlQuery: DcqlQuery,
        credentialsWithBundleItems: List<VerifiableCredentialWithBundleItemsWithKeyBinding>
    ): Result<Set<CompatibleCredential>, GetCompatibleCredentialsError> = binding {
        buildSet {
            DcqlSupport.matchDcqlCredentials(
                dcqlQuery,
                credentialsWithBundleItems.map {
                    it.nextBundleItemToPresent.payload
                }
            ).forEach { dcqlCredentialMatch ->
                credentialsWithBundleItems.find {
                    it.nextBundleItemToPresent.payload == dcqlCredentialMatch.credentialPayload &&
                        it.verifiableCredential.progressionState == VerifiableProgressionState.ACCEPTED
                }?.let { credentialWithBundleItem ->
                    val pathsForPresentation = getPathsForPresentation(
                        credentialsWithBundleItem = credentialWithBundleItem,
                        requestedPaths = dcqlCredentialMatch.claimValues.map { it.paths.toClaimsPathPointer() },
                    ).bind()

                    add(
                        CompatibleCredential(
                            credentialId = credentialWithBundleItem.credential.id,
                            presentationPaths = pathsForPresentation,
                            dcqlQueryId = dcqlCredentialMatch.credentialQueryId
                        )
                    )
                }
            }
        }
    }

    private fun getPathsForPresentation(
        credentialsWithBundleItem: VerifiableCredentialWithBundleItemsWithKeyBinding,
        requestedPaths: List<ClaimsPathPointer>
    ): Result<List<ClaimsPathPointer>, GetCompatibleCredentialsError> = binding {
        val anyCredentials = credentialsWithBundleItem.toAnyCredentials()
            .mapError(AnyCredentialError::toGetCompatibleCredentialsError)
            .bind()
        anyCredentials.firstOrNull()?.getPathsForPresentation(requestedPaths)?.toList() ?: emptyList()
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
