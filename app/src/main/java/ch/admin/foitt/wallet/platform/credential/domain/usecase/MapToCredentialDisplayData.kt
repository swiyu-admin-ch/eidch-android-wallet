package ch.admin.foitt.wallet.platform.credential.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.model.MapToCredentialDisplayDataError
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import com.github.michaelbull.result.Result

interface MapToCredentialDisplayData {
    suspend operator fun invoke(
        verifiableCredential: VerifiableCredentialEntity,
        credentialDisplays: List<CredentialDisplay>,
        claims: List<CredentialClaimWithDisplays>,
        credentialFormat: CredentialFormat
    ): Result<CredentialDisplayData, MapToCredentialDisplayDataError>
}
