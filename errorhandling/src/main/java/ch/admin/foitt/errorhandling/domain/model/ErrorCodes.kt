package ch.admin.foitt.errorhandling.domain.model

@JvmInline
value class ErrorCode(val value: String) {
    override fun toString(): String = value
}

/**
 * General ErrorCode Format: $PREFIX$CATEGORY-$RANDOM
 * PREFIX: 2-4 uppercase characters indicating the feature (e.g., "CO" for CredentialOffer).
 * CATEGORY: A 3-digit number where the hundreds digit encodes the error type:
 * 0xx: General / Catch-all
 * 1xx: Lifecycle / Setup
 * 2xx: Connection / Network
 * 3xx: Parsing / Deserialization
 * 4xx: Validation / Business Logic
 * 5xx: Internal

 * Example: "CO201-8343"
 */
object ErrorCodes {
    object Keystore {
        private const val PREFIX = "KS" // keystore
        fun generalKeystoreError() = createErrorCode(PREFIX, category = "500")
        fun invalidatedKeyError() = createErrorCode(PREFIX, category = "501")
        fun invalidKeyError() = createErrorCode(PREFIX, category = "502")
    }

    object Passphrase {
        private const val PREFIX = "PA" // passphrase
        fun generalPassphraseError() = createErrorCode(PREFIX, category = "500")
    }

    object Crypto {
        private const val PREFIX = "CR" // crypto
        fun generalCryptoError() = createErrorCode(PREFIX, category = "500")
    }

    object Database {
        private const val PREFIX = "DB" // database
        fun wrongPassphraseError() = createErrorCode(PREFIX, category = "400")
        fun reKeyError() = createErrorCode(PREFIX, category = "401")
        fun alreadyOpenError() = createErrorCode(PREFIX, category = "402")
        fun setupFailedError() = createErrorCode(PREFIX, category = "403")
    }

    object BiometricAuthentication {
        private const val PREFIX = "BA" // biometric authentication
        fun generalError() = createErrorCode(PREFIX, category = "500")
        fun promptCancelledError() = createErrorCode(PREFIX, category = "501")
        fun promptLockedError() = createErrorCode(PREFIX, category = "502")
        fun promptFailureError() = createErrorCode(PREFIX, category = "503")
    }

    object Biometrics {
        private const val PREFIX = "BI" // biometrics
        fun invalidatedKeyError() = createErrorCode(PREFIX, category = "500")
        fun generalError() = createErrorCode(PREFIX, category = "501")
    }

    private fun createErrorCode(prefix: String, category: String): ErrorCode {
        // append 4-digit random number every time an ErrorCode is created
        return ErrorCode("${prefix}$category-${(1000..9999).random()}")
    }
}
