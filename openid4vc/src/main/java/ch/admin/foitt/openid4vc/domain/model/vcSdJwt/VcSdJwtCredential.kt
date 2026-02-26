package ch.admin.foitt.openid4vc.domain.model.vcSdJwt

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.Validity
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import java.time.Instant

class VcSdJwtCredential(
    override val id: Long? = null,
    override val keyBinding: KeyBinding? = null,
    override val payload: String,
    validFrom: Long? = null,
    validUntil: Long? = null,
) : VcSdJwt(rawVcSdJwt = payload, reservedClaimNames = RESERVED_CLAIM_NAMES), AnyCredential {

    override val issuer: String = this.vcIssuer
    override val format: CredentialFormat = CredentialFormat.VC_SD_JWT

    override val validity: Validity
        get() = jwtValidity

    override val claimsPath = "$"

    override val validFromInstant: Instant? = validFrom?.let { Instant.ofEpochSecond(it) } ?: nbfInstant

    override val validUntilInstant: Instant? = validUntil?.let { Instant.ofEpochSecond(it) } ?: expInstant

    override val vcSchemaId: String = vct

    /**
     * @returns all claims that we want to save in the database (i. e. only the disclosable claims)
     */
    override fun getClaimsToSave(): JsonElement = getDisclosableClaimsJson()

    /**
     * @returns all claims that can be requested by a verifier (i. e. disclosable claims + technical (=reserved) claims)
     */
    override fun getClaimsForPresentation(): JsonElement = sdJwtJson

    override fun createVerifiableCredential(requestedFieldKeys: List<String>): String = createSelectiveDisclosure(requestedFieldKeys)

    fun hasNonDisclosableClaims(): Boolean = (payloadJson.keys - RESERVED_CLAIM_NAMES).isNotEmpty()

    private fun getDisclosableClaimsJson(): JsonElement {
        val disclosableClaims = sdJwtJson.jsonObject.entries.filterNot { RESERVED_CLAIM_NAMES.contains(it.key) }
        val disclosableClaimsJson = JsonObject(disclosableClaims.associate { it.toPair() })
        return disclosableClaimsJson
    }

    private companion object {
        // Reserved claim names
        // See https://www.ietf.org/archive/id/draft-ietf-oauth-sd-jwt-vc-04.html#name-registered-jwt-claims and
        // https://www.ietf.org/archive/id/draft-ietf-oauth-selective-disclosure-jwt-10.html#section-5.1
        private val RESERVED_CLAIM_NAMES = setOf(
            "iss", "nbf", "exp", "sub", "iat", "aud", "jti", // JWT
            "_sd_alg", "_sd", // SD-JWT
            "cnf", "vct", "vct#integrity", "vct_metadata_uri", "vct_metadata_uri#integrity", "status", // VcSdJwt
        )
    }
}
