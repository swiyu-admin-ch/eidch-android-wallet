package ch.admin.foitt.wallet.platform.claimsPathPointer.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import kotlinx.serialization.json.JsonElement

interface GetClaimsPathPointers {
    suspend operator fun invoke(
        jsonElement: JsonElement,
    ): Map<ClaimsPathPointer, JsonElement>
}
