package ch.admin.foitt.wallet.platform.credential.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.wallet.platform.credential.domain.model.GenerateMetadataDisplaysError
import ch.admin.foitt.wallet.platform.oca.domain.model.MetaDisplays
import com.github.michaelbull.result.Result
import kotlinx.serialization.json.JsonObject

interface GenerateMetadataDisplays {
    suspend operator fun invoke(
        jsonObject: JsonObject?,
        credentialConfiguration: AnyCredentialConfiguration,
    ): Result<MetaDisplays, GenerateMetadataDisplaysError>
}
