package ch.admin.foitt.openid4vc.domain.model.vcSdJwt

import ch.admin.foitt.openid4vc.domain.model.anycredential.toBusinessExpiryInstant
import ch.admin.foitt.openid4vc.domain.model.anycredential.toInstant
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.toPointerString
import ch.admin.foitt.openid4vc.domain.model.sdjwt.SdJwt
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.get
import com.nimbusds.jwt.JWTClaimNames
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant

/**
 * https://www.ietf.org/archive/id/draft-ietf-oauth-sd-jwt-vc-04.html
 */
open class VcSdJwt(
    rawVcSdJwt: String
) : SdJwt(rawSdJwt = rawVcSdJwt, nonSelectivelyDisclosableClaims = NON_SELECTIVELY_DISCLOSABLE_CLAIMS) {

    init {
        val nonSelectivelyDisclosableClaims = payloadJson.keys - NON_SELECTIVELY_DISCLOSABLE_CLAIMS
        check(nonSelectivelyDisclosableClaims.isEmpty()) {
            "VcSdJwt contains unregistered non-selectively disclosable claims"
        }
    }
    val profileVersion: String? = signedJwt.header.getCustomParam(CLAIM_KEY_PROFILE_VERSION)?.toString()
    val exp: Instant? = processedJson.jsonObject[JWTClaimNames.EXPIRATION_TIME]?.jsonPrimitive?.content?.toInstant()

    val kid: String = keyId ?: error("missing keyId claim")
    val vct = processedJson.jsonObject[CLAIM_KEY_VCT]?.jsonPrimitive?.content ?: error("missing vct claim")
    val vctIntegrity: String? = processedJson.jsonObject[CLAIM_KEY_VCT_INTEGRITY]?.jsonPrimitive?.content
    val vctMetadataUri: String? = processedJson.jsonObject[CLAIM_KEY_VCT_METADATA_URI]?.jsonPrimitive?.content
    val vctMetadataUriIntegrity: String? = processedJson.jsonObject[CLAIM_KEY_VCT_METADATA_URI_INTEGRITY]?.jsonPrimitive?.content

    /**
     * Resolves the key binding JWK by decoding the `cnf` claim, supporting both the standard and legacy structures.
     *
     * Two CNF structures are intentionally supported:
     * - the standard `cnf.jwk`, where the JWK is nested under the `jwk` key;
     * - the legacy flat `cnf`, where the JWK fields are held directly.
     *
     * Keeping both decoding paths is an accepted business decision for the time being. Some early production
     * credentials were released with the flat CNF format and we decided to keep managing them to not break usage
     * until further notice.
     */
    val cnfJwk = processedJson.jsonObject[CLAIM_KEY_CNF]?.jsonObject[CLAIM_KEY_CNF_JWK]
        ?: processedJson.jsonObject[CLAIM_KEY_CNF]
    val status = processedJson.jsonObject[CLAIM_KEY_STATUS]

    /* expiry_date claim can optionally be put in disclosures, so it has to be read here */
    val businessExpiryDate: Instant? = runSuspendCatching {
        processedJson.jsonObject[CLAIM_KEY_BUSINESS_EXPIRY_DATE]?.jsonPrimitive?.content
    }.get()?.toBusinessExpiryInstant()

    /* "sub" claim can optionally be put in disclosures, so it has to be read here */
    override val subject: String? = runSuspendCatching {
        processedJson.jsonObject[JWTClaimNames.SUBJECT]?.jsonPrimitive?.content
    }.get()

    /* "iat" claim can optionally be put in disclosures, so it has to be read here */
    override val issuedAt: Instant? = runSuspendCatching {
        processedJson.jsonObject[JWTClaimNames.ISSUED_AT]?.jsonPrimitive?.content
    }.get()?.toInstant()

    companion object {
        private const val CLAIM_KEY_CNF = "cnf"
        private const val CLAIM_KEY_CNF_JWK = "jwk"
        private const val CLAIM_KEY_VCT = "vct"
        private const val CLAIM_KEY_VCT_INTEGRITY = "vct#integrity"
        private const val CLAIM_KEY_VCT_METADATA_URI = "vct_metadata_uri"
        private const val CLAIM_KEY_VCT_METADATA_URI_INTEGRITY = "vct_metadata_uri#integrity"
        private const val CLAIM_KEY_STATUS = "status"
        private const val CLAIM_KEY_BUSINESS_EXPIRY_DATE = "expiry_date"

        private const val CLAIM_KEY_PROFILE_VERSION = "profile_version"

        // See https://www.ietf.org/archive/id/draft-ietf-oauth-sd-jwt-vc-15.html#section-3.2.2.2
        val NON_SELECTIVELY_DISCLOSABLE_CLAIMS = setOf(
            "iss", "nbf", "exp", "iat", CLAIM_KEY_CNF, CLAIM_KEY_STATUS,
            "_sd", "_sd_alg",
            CLAIM_KEY_VCT, CLAIM_KEY_VCT_INTEGRITY, CLAIM_KEY_VCT_METADATA_URI, CLAIM_KEY_VCT_METADATA_URI_INTEGRITY,
        )

        val BUSINESS_EXPIRY_DATE_CLAIM_PATH = listOf(
            ClaimsPathPointerComponent.String(CLAIM_KEY_BUSINESS_EXPIRY_DATE)
        ).toPointerString()
    }
}
