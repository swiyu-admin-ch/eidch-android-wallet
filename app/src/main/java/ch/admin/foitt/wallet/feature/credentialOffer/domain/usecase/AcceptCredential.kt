package ch.admin.foitt.wallet.feature.credentialOffer.domain.usecase

import ch.admin.foitt.wallet.feature.credentialOffer.domain.model.AcceptCredentialError
import com.github.michaelbull.result.Result

interface AcceptCredential {
    suspend operator fun invoke(credentialId: Long): Result<Int, AcceptCredentialError>
}
