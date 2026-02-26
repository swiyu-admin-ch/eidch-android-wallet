package ch.admin.foitt.wallet.platform.keybindingMatching.domain.model

import ch.admin.foitt.wallet.platform.utils.JsonError
import ch.admin.foitt.wallet.platform.utils.JsonParsingError

sealed interface MatchKeyBindingToPayloadCnfError {
    data class Unexpected(val throwable: Throwable?) : MatchKeyBindingToPayloadCnfError
}

fun JsonParsingError.toMatchKeyBindingToPayloadCnfError(): MatchKeyBindingToPayloadCnfError = when (this) {
    is JsonError.Unexpected -> MatchKeyBindingToPayloadCnfError.Unexpected(throwable)
}
