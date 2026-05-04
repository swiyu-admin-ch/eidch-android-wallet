package ch.admin.foitt.wallet.platform.oca.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.wallet.platform.oca.domain.model.GenerateOcaDisplaysError
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaDisplays
import com.github.michaelbull.result.Result
import kotlinx.serialization.json.JsonElement

interface GenerateOcaDisplays {
    suspend operator fun invoke(
        credentialClaims: Map<ClaimsPathPointer, JsonElement>,
        credentialFormat: String,
        ocaBundle: OcaBundle,
    ): Result<OcaDisplays, GenerateOcaDisplaysError>
}
