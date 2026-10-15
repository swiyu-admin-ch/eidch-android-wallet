package ch.admin.foitt.wallet.platform.crypto.domain.usecase.implementation.mock

object MockHashConfiguration {
    const val HASH_ALGORITHM = "PBKDF2withHmacSHA512"
    const val HASH_ITERATIONS = 210000
    const val HASH_KEY_LENGTH = 512
    const val SALT_LENGTH = 16
}
