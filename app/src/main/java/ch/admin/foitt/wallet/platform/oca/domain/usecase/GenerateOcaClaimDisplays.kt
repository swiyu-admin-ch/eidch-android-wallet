package ch.admin.foitt.wallet.platform.oca.domain.usecase

import ch.admin.foitt.wallet.platform.credential.domain.model.AnyClaimDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.oca.domain.model.GenerateOcaDisplaysError
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaClaimData
import com.github.michaelbull.result.Result

interface GenerateOcaClaimDisplays {
    operator fun invoke(
        claimsPathPointer: String,
        value: String?,
        ocaClaimData: OcaClaimData,
    ): Result<Pair<CredentialClaim, List<AnyClaimDisplay>>, GenerateOcaDisplaysError>
}
