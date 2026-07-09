package ch.admin.foitt.wallet.platform.oca.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyClaimDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.oca.domain.model.GenerateOcaDisplaysError
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaClaimData
import com.github.michaelbull.result.Result

interface GenerateOcaClaimDisplays {
    operator fun invoke(
        claimsPathPointer: ClaimsPathPointer,
        value: String?,
        ocaClaimData: OcaClaimData?,
        order: Int?,
    ): Result<Pair<CredentialClaim, List<AnyClaimDisplay>>, GenerateOcaDisplaysError>
}
