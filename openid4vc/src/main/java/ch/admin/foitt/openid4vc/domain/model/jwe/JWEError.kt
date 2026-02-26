package ch.admin.foitt.openid4vc.domain.model.jwe

sealed interface JWEError {
    data class Unexpected(val throwable: Throwable) : DecryptJWEError, CreateJWEError
}

sealed interface DecryptJWEError
sealed interface CreateJWEError
