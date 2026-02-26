package ch.admin.foitt.wallet.platform.credential.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialResult
import com.github.michaelbull.result.Result

fun interface FetchAndSaveCredential {
    suspend operator fun invoke(
        credentialOffer: CredentialOffer,
    ): Result<FetchCredentialResult, FetchCredentialError>
}
