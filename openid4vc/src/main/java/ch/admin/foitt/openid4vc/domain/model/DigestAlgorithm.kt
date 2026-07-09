package ch.admin.foitt.openid4vc.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class DigestAlgorithm(val stdName: String) {
    SHA256("SHA-256");

    companion object {
        fun from(value: String): DigestAlgorithm? {
            val normalizedName = value
                .uppercase()
                .replace("-", "")
                .replace("_", "")
            return entries.find { it.name == normalizedName }
        }
    }
}
