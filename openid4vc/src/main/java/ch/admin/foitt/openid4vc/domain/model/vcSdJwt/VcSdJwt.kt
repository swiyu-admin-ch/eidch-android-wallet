package ch.admin.foitt.openid4vc.domain.model.vcSdJwt

import ch.admin.foitt.openid4vc.domain.model.sdjwt.SdJwt
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.get
import com.github.michaelbull.result.recoverCatching
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

    val vcIssuer: String = iss ?: error("missing iss claim")
    val kid: String = keyId ?: error("missing keyId claim")
    val vct = processedJson.jsonObject[CLAIM_KEY_VCT]?.jsonPrimitive?.content ?: error("missing vct claim")
    val vctIntegrity: String? = processedJson.jsonObject[CLAIM_KEY_VCT_INTEGRITY]?.jsonPrimitive?.content
    val vctMetadataUri: String? = processedJson.jsonObject[CLAIM_KEY_VCT_METADATA_URI]?.jsonPrimitive?.content
    val vctMetadataUriIntegrity: String? = processedJson.jsonObject[CLAIM_KEY_VCT_METADATA_URI_INTEGRITY]?.jsonPrimitive?.content
    val cnfJwk = processedJson.jsonObject[CLAIM_KEY_CNF]?.jsonObject[CLAIM_KEY_CNF_JWK]
        // Support for both malformed and standard format of cnf claim
        ?: processedJson.jsonObject[CLAIM_KEY_CNF]
    val status = processedJson.jsonObject[CLAIM_KEY_STATUS]

    /* "sub" claim can optionally be put in disclosures, so it has to be read here */
    override val subject: String? = runSuspendCatching {
        processedJson.jsonObject[JWTClaimNames.SUBJECT]?.jsonPrimitive?.content
    }.get()

    /* "iat" claim can optionally be put in disclosures, so it has to be read here */
    override val issuedAt: Instant? = runSuspendCatching {
        processedJson.jsonObject[JWTClaimNames.ISSUED_AT]?.jsonPrimitive?.content
    }.get()?.toInstant()

    private fun String.toInstant(): Instant? = runSuspendCatching {
        Instant.ofEpochSecond(this.toLong())
    }.recoverCatching {
        Instant.parse(this)
    }.get()

    companion object {
        private const val CLAIM_KEY_CNF = "cnf"
        private const val CLAIM_KEY_CNF_JWK = "jwk"
        private const val CLAIM_KEY_VCT = "vct"
        private const val CLAIM_KEY_VCT_INTEGRITY = "vct#integrity"
        private const val CLAIM_KEY_VCT_METADATA_URI = "vct_metadata_uri"
        private const val CLAIM_KEY_VCT_METADATA_URI_INTEGRITY = "vct_metadata_uri#integrity"
        private const val CLAIM_KEY_STATUS = "status"

        // See https://www.ietf.org/archive/id/draft-ietf-oauth-sd-jwt-vc-15.html#section-3.2.2.2
        val NON_SELECTIVELY_DISCLOSABLE_CLAIMS = setOf(
            "iss", "nbf", "exp", "iat", CLAIM_KEY_CNF, CLAIM_KEY_STATUS,
            "_sd", "_sd_alg",
            CLAIM_KEY_VCT, CLAIM_KEY_VCT_INTEGRITY, CLAIM_KEY_VCT_METADATA_URI, CLAIM_KEY_VCT_METADATA_URI_INTEGRITY,
        )
    }
}
