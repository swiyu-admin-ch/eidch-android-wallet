package ch.admin.foitt.openid4vc.domain.model.presentationRequest

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class ClientIdentifier(
    val clientIdPrefix: ClientIdPrefix,
    val clientId: String
) {
    @Serializable
    enum class ClientIdPrefix(val value: String) {
        DecentralizedIdentifier("decentralized_identifier"),
        VerifierAttestationJwt("verifier_attestation"),
    }

    companion object {
        fun fromRequestObject(requestObject: RequestObject): Result<ClientIdentifier, Throwable> = runCatching {
            val payloadJson = requestObject.jwt.payloadJson
            // if client_id is not present, this is an invalid request
            val clientId = payloadJson.jsonObject["client_id"]?.jsonPrimitive?.content
                ?: error("authorization request jwt client_id missing")

            if (clientId.hasValidPrefix()) {
                val split = clientId.split(":", limit = 2)
                if (split[1].isEmpty()) {
                    error("authorization request jwt contains invalid client_id")
                } else {
                    ClientIdentifier(
                        clientIdPrefix = clientIdPrefixFromString(split[0]),
                        clientId = split[1],
                    )
                }
            } else {
                ClientIdentifier(
                    clientIdPrefix = clientIdPrefixFromString(null),
                    clientId = clientId,
                )
            }
        }

        private fun String.hasValidPrefix(): Boolean {
            return this.contains(":") && ClientIdPrefix.entries.any { this.startsWith(it.value) }
        }

        private fun clientIdPrefixFromString(clientIdPrefixString: String?): ClientIdPrefix = when (clientIdPrefixString) {
            "decentralized_identifier" -> ClientIdPrefix.DecentralizedIdentifier
            "verifier_attestation" -> ClientIdPrefix.VerifierAttestationJwt
            // Acc. swiss profile verification section 5.9.2 use decentralized_identifier as fallback
            else -> ClientIdPrefix.DecentralizedIdentifier
        }
    }
}
