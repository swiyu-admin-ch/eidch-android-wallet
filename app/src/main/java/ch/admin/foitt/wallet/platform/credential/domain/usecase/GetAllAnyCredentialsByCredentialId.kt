package ch.admin.foitt.wallet.platform.credential.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.wallet.platform.credential.domain.model.GetAllAnyCredentialsByCredentialIdError
import com.github.michaelbull.result.Result

fun interface GetAllAnyCredentialsByCredentialId {
    suspend operator fun invoke(
        credentialId: Long,
    ): Result<List<AnyCredential>, GetAllAnyCredentialsByCredentialIdError>
}
