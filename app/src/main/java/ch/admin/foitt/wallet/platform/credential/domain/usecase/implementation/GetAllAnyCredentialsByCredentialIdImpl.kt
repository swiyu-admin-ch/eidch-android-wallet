package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.GetAllAnyCredentialsByCredentialIdError
import ch.admin.foitt.wallet.platform.credential.domain.model.toAnyCredentials
import ch.admin.foitt.wallet.platform.credential.domain.model.toGetAllAnyCredentialsByCredentialIdError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAllAnyCredentialsByCredentialId
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithKeyBindingRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialWithBundleItemsWithKeyBindingRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class GetAllAnyCredentialsByCredentialIdImpl @Inject constructor(
    private val verifiableCredentialWithBundleItemsWithKeyBindingRepository: VerifiableCredentialWithBundleItemsWithKeyBindingRepository,
) : GetAllAnyCredentialsByCredentialId {
    override suspend fun invoke(credentialId: Long): Result<List<AnyCredential>, GetAllAnyCredentialsByCredentialIdError> {
        return verifiableCredentialWithBundleItemsWithKeyBindingRepository.getByCredentialId(credentialId)
            .mapError(CredentialWithKeyBindingRepositoryError::toGetAllAnyCredentialsByCredentialIdError)
            .andThen { credentialWithKeyBinding ->
                coroutineBinding {
                    credentialWithKeyBinding.toAnyCredentials()
                        .mapError(AnyCredentialError::toGetAllAnyCredentialsByCredentialIdError)
                        .bind()
                }
            }
    }
}
