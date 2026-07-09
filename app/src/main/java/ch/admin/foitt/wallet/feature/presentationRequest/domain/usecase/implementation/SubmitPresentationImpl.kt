package ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseConfig
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.GetAuthorizationResponseConfigError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.SubmitAnyCredentialPresentationError
import ch.admin.foitt.openid4vc.domain.usecase.GetAuthorizationResponseConfig
import ch.admin.foitt.openid4vc.domain.usecase.SubmitAnyCredentialNetworkPresentation
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.SubmitPresentationError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.toSubmitPresentationError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.SubmitPresentation
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.toNextAnyCredentialToPresent
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.VerificationProcessType
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithBundleItemsWithKeyBinding
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.proximity.domain.usecase.GetProximityRepositoryForScope
import ch.admin.foitt.wallet.platform.ssi.domain.model.BundleItemRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithKeyBindingRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.VerifiableCredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialWithBundleItemsWithKeyBindingRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class SubmitPresentationImpl @Inject constructor(
    private val environmentSetupRepository: EnvironmentSetupRepository,
    private val verifiableCredentialWithBundleItemsWithKeyBindingRepository: VerifiableCredentialWithBundleItemsWithKeyBindingRepository,
    private val verifiableCredentialRepository: VerifiableCredentialRepository,
    private val bundleItemRepository: BundleItemRepository,
    private val submitAnyCredentialNetworkPresentation: SubmitAnyCredentialNetworkPresentation,
    private val getAuthorizationResponseConfig: GetAuthorizationResponseConfig,
    private val getProximityRepositoryForScope: GetProximityRepositoryForScope,
) : SubmitPresentation {

    override suspend fun invoke(
        presentationRequestWithRaw: PresentationRequestWithRaw,
        compatibleCredential: CompatibleCredential,
    ): Result<Unit, SubmitPresentationError> = coroutineBinding {
        val verifiableCredentialWithBundleItemsWithKeyBinding =
            verifiableCredentialWithBundleItemsWithKeyBindingRepository.getByCredentialId(compatibleCredential.credentialId)
                .mapError(CredentialWithKeyBindingRepositoryError::toSubmitPresentationError)
                .bind()

        val nextAnyCredentialToPresent = verifiableCredentialWithBundleItemsWithKeyBinding
            .toNextAnyCredentialToPresent()
            .mapError(AnyCredentialError::toSubmitPresentationError)
            .bind()

        val authorizationResponseConfig = getAuthorizationResponseConfig(
            anyCredential = nextAnyCredentialToPresent,
            presentationPaths = compatibleCredential.presentationPaths,
            authorizationRequest = presentationRequestWithRaw.authorizationRequest,
            usePayloadEncryption = environmentSetupRepository.payloadEncryptionEnabled,
            dcqlQueryId = compatibleCredential.dcqlQueryId,
        ).mapError(GetAuthorizationResponseConfigError::toSubmitPresentationError)
            .bind()

        when (presentationRequestWithRaw.verificationProcessType) {
            VerificationProcessType.NETWORK -> submitNetwork(
                presentationRequestWithRaw,
                authorizationResponseConfig
            )

            VerificationProcessType.PROXIMITY -> submitProximity(
                authorizationResponseConfig
            )
        }.mapError { error ->
            updateBundleIdToPresent(verifiableCredentialWithBundleItemsWithKeyBinding)
            error
        }.bind()

        updateBundleIdToPresent(verifiableCredentialWithBundleItemsWithKeyBinding)
            .bind()
    }

    private suspend fun updateBundleIdToPresent(
        verifiableCredentialWithBundleItemsWithKeyBinding: VerifiableCredentialWithBundleItemsWithKeyBinding
    ): Result<Unit, SubmitPresentationError> = coroutineBinding {
        val nextPresentableBundleItemId = bundleItemRepository.onPresented(
            verifiableCredentialWithBundleItemsWithKeyBinding.credential.id,
            verifiableCredentialWithBundleItemsWithKeyBinding.nextBundleItemToPresent.id
        ).mapError(BundleItemRepositoryError::toSubmitPresentationError)
            .bind()

        verifiableCredentialRepository.updateNextBundleIdByCredentialId(
            credentialId = verifiableCredentialWithBundleItemsWithKeyBinding.credential.id,
            nextPresentableBundleItemId = nextPresentableBundleItemId
        ).mapError(VerifiableCredentialRepositoryError::toSubmitPresentationError)
            .bind()
    }

    private suspend fun submitProximity(
        authorizationResponseConfig: AuthorizationResponseConfig,
    ): Result<Unit, SubmitPresentationError> {
        return getProximityRepositoryForScope().submit(authorizationResponseConfig)
            .mapError { error ->
                error.toSubmitPresentationError()
            }
    }

    private suspend fun submitNetwork(
        presentationRequestWithRaw: PresentationRequestWithRaw,
        authorizationResponseConfig: AuthorizationResponseConfig,
    ): Result<Unit, SubmitPresentationError> = submitAnyCredentialNetworkPresentation(
        presentationRequestWithRaw.authorizationRequest,
        authorizationResponseConfig
    ).mapError(SubmitAnyCredentialPresentationError::toSubmitPresentationError)
}
