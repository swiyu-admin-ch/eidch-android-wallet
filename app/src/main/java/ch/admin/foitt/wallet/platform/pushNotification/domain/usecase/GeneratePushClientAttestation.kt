package ch.admin.foitt.wallet.platform.pushNotification.domain.usecase

import ch.admin.foitt.wallet.platform.pushNotification.domain.model.GeneratePushClientAttestationError
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.PushClientAttestation
import com.github.michaelbull.result.Result
import kotlinx.serialization.json.JsonElement

interface GeneratePushClientAttestation {
    suspend operator fun invoke(requestBody: JsonElement): Result<PushClientAttestation, GeneratePushClientAttestationError>
}
