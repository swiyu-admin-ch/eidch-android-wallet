package ch.admin.foitt.wallet.platform.oca.domain.usecase

import ch.admin.foitt.wallet.platform.oca.domain.model.GenerateOcaDisplaysError
import ch.admin.foitt.wallet.platform.oca.domain.model.MetaDisplays
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import com.github.michaelbull.result.Result
import kotlinx.serialization.json.JsonObject

interface GenerateOcaDisplays {
    suspend operator fun invoke(
        jsonObject: JsonObject?,
        credentialFormat: String,
        ocaBundle: OcaBundle,
    ): Result<MetaDisplays, GenerateOcaDisplaysError>
}
