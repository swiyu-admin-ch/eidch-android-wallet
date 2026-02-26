package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.wallet.platform.credential.domain.model.GetAnyCredentialsError
import ch.admin.foitt.wallet.platform.credential.domain.model.toAnyCredentials
import ch.admin.foitt.wallet.platform.credential.domain.model.toGetAnyCredentialsError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAllAnyCredentials
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithKeyBindingRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialWithBundleItemsWithKeyBindingRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.get
import com.github.michaelbull.result.mapError
import javax.inject.Inject

internal class GetAllAnyCredentialsImpl @Inject constructor(
    private val verifiableCredentialWithBundleItemsWithKeyBindingRepository: VerifiableCredentialWithBundleItemsWithKeyBindingRepository,
) : GetAllAnyCredentials {
    override suspend fun invoke(): Result<List<AnyCredential>, GetAnyCredentialsError> = coroutineBinding {
        val credentials = verifiableCredentialWithBundleItemsWithKeyBindingRepository.getAll()
            .mapError(CredentialWithKeyBindingRepositoryError::toGetAnyCredentialsError)
            .bind()
        buildList {
            credentials.forEach { credential ->
                addAll(
                    credential.toAnyCredentials().get() ?: emptyList()
                )
            }
        }
    }
}
