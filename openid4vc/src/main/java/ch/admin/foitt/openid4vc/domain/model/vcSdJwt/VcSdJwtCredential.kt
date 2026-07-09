package ch.admin.foitt.openid4vc.domain.model.vcSdJwt

import ch.admin.eid.didresolver.didresolver.getDidFromAbsoluteKid
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.Validity
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import java.time.Instant

class VcSdJwtCredential(
    override val id: Long? = null,
    override val keyBinding: KeyBinding? = null,
    override val payload: String,
    validFrom: Long? = null,
    validUntil: Long? = null,
    override val format: CredentialFormat = CredentialFormat.DC_SD_JWT
) : VcSdJwt(rawVcSdJwt = payload), AnyCredential {

    override val issuer: String
        get() = getDidFromAbsoluteKid(kid).asString()

    override val validity: Validity
        get() {
            val baseValidity = jwtValidity
            if (baseValidity != Validity.Valid) {
                return baseValidity
            }
            val businessExp = businessExpiryDate
            if (businessExp != null && Instant.now().isAfter(businessExp)) {
                return Validity.BusinessExpired(businessExp)
            }
            return Validity.Valid
        }

    override val claimsPath = "$"

    override val validFromInstant: Instant? = validFrom?.let { Instant.ofEpochSecond(it) } ?: nbfInstant

    override val validUntilInstant: Instant? = validUntil?.let { Instant.ofEpochSecond(it) } ?: expInstant

    override val vcSchemaId: String = vct

    /**
     * @returns all claims that we want to save in the database (i. e. only the disclosable claims)
     */
    override fun getClaimsToSave(): JsonObject {
        val claims = processedJson.jsonObject.filterNot { it.key in NON_SELECTIVELY_DISCLOSABLE_CLAIMS }
        return JsonObject(claims)
    }

    /**
     * @returns all claims that can be requested by a verifier (i. e. disclosable claims + technical (=reserved) claims)
     */
    override fun getClaimsForPresentation(): JsonObject = processedJson
    override fun getPathsForPresentation(
        requestedPaths: List<ClaimsPathPointer>
    ): Set<ClaimsPathPointer> = getPresentationPaths(requestedPaths)

    override fun createVerifiableCredential(presentationPaths: List<ClaimsPathPointer>): String = createSelectiveDisclosure(
        presentationPaths
    )
}
