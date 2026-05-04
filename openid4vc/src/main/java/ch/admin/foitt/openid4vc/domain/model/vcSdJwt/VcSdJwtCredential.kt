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
) : VcSdJwt(rawVcSdJwt = payload), AnyCredential {

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
    override fun getClaimsToSave(): JsonElement {
        val claims = processedJson.jsonObject.filterNot { it.key in NON_SELECTIVELY_DISCLOSABLE_CLAIMS }
        return JsonObject(claims)
    }

    /**
     * @returns all claims that can be requested by a verifier (i. e. disclosable claims + technical (=reserved) claims)
     */
    override fun getClaimsForPresentation(): JsonElement = processedJson

    override fun createVerifiableCredential(requestedFieldKeys: List<String>): String = createSelectiveDisclosure(requestedFieldKeys)
}
