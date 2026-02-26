package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptor
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptorFormat
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.toAnyCredentials
import ch.admin.foitt.wallet.platform.credential.domain.model.toGetCompatibleCredentialsError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.GetCompatibleCredentialsError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.GetRequestedFieldsError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.toGetCompatibleCredentialsError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.GetCompatibleCredentials
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.GetRequestedFields
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithBundleItemsWithKeyBinding
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithKeyBindingRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialWithBundleItemsWithKeyBindingRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class GetCompatibleCredentialsImpl @Inject constructor(
    private val verifiableCredentialWithBundleItemsWithKeyBindingRepository: VerifiableCredentialWithBundleItemsWithKeyBindingRepository,
    private val getRequestedFields: GetRequestedFields,
) : GetCompatibleCredentials {
    override suspend fun invoke(
        inputDescriptors: List<InputDescriptor>
    ): Result<Set<CompatibleCredential>, GetCompatibleCredentialsError> =
        verifiableCredentialWithBundleItemsWithKeyBindingRepository.getAll()
            .mapError(CredentialWithKeyBindingRepositoryError::toGetCompatibleCredentialsError)
            .andThen { credentials ->
                findCompatibleCredentials(credentials, inputDescriptors)
            }

    private suspend fun findCompatibleCredentials(
        credentialsWithBundleItems: List<VerifiableCredentialWithBundleItemsWithKeyBinding>,
        inputDescriptors: List<InputDescriptor>
    ): Result<Set<CompatibleCredential>, GetCompatibleCredentialsError> = coroutineBinding {
        buildSet {
            credentialsWithBundleItems.forEach { credentialWithBundleItems ->
                runSuspendCatching {
                    val credential = credentialWithBundleItems.credential
                    val verifiableCredential = credentialWithBundleItems.verifiableCredential
                    if (verifiableCredential.progressionState == VerifiableProgressionState.ACCEPTED) {
                        val inputDescriptor = inputDescriptors.first()
                        val compatibleFormat = getCompatibleFormat(credential.format, inputDescriptor.formats)
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
}
