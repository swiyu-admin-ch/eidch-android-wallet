package ch.admin.foitt.wallet.platform.credential.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.Claim
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyClaimDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.GenerateMetadataDisplaysError
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import com.github.michaelbull.result.Result
import kotlinx.serialization.json.JsonElement

fun interface GenerateMetadataClaimDisplays {
    suspend operator fun invoke(
        claimsPathPointer: ClaimsPathPointer,
        claimValueJson: JsonElement,
        metadataClaim: Claim?,
        order: Int,
    ): Result<Pair<CredentialClaim, List<AnyClaimDisplay>>, GenerateMetadataDisplaysError>
}
