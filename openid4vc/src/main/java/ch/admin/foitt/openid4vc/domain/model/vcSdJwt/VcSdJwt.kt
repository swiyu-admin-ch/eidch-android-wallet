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
    rawVcSdJwt: String,
    reservedClaimNames: Set<String> = emptySet()
) : SdJwt(rawSdJwt = rawVcSdJwt, reservedClaimNames = reservedClaimNames) {
    val vcIssuer: String = iss ?: error("missing iss claim")
    val kid: String = keyId ?: error("missing keyId claim")
    val vct = sdJwtJson.jsonObject[CLAIM_KEY_VCT]?.jsonPrimitive?.content ?: error("missing vct claim")
    val vctIntegrity: String? = sdJwtJson.jsonObject[CLAIM_KEY_VCT_INTEGRITY]?.jsonPrimitive?.content
    val vctMetadataUri: String? = sdJwtJson.jsonObject[CLAIM_KEY_VCT_METADATA_URI]?.jsonPrimitive?.content
    val vctMetadataUriIntegrity: String? = sdJwtJson.jsonObject[CLAIM_KEY_VCT_METADATA_URI_INTEGRITY]?.jsonPrimitive?.content
    val cnfJwk = sdJwtJson.jsonObject[CLAIM_KEY_CNF]?.jsonObject[CLAIM_KEY_CNF_JWK]
        // Support for both malformed and standard format of cnf claim
        ?: sdJwtJson.jsonObject[CLAIM_KEY_CNF]
    val status = sdJwtJson.jsonObject[CLAIM_KEY_STATUS]

    /* "sub" claim can optionally be put in disclosures, so it has to be read here */
    override val subject: String? = runSuspendCatching {
        sdJwtJson.jsonObject[JWTClaimNames.SUBJECT]?.jsonPrimitive?.content
    }.get()

    /* "iat" claim can optionally be put in disclosures, so it has to be read here */
    override val issuedAt: Instant? = runSuspendCatching {
        sdJwtJson.jsonObject[JWTClaimNames.ISSUED_AT]?.jsonPrimitive?.content
    }.get()?.toInstant()

    private fun String.toInstant(): Instant? = runSuspendCatching {
        Instant.ofEpochSecond(this.toLong())
    }.recoverCatching {
        Instant.parse(this)
    }.get()

    private companion object {
        const val CLAIM_KEY_CNF = "cnf"
        const val CLAIM_KEY_CNF_JWK = "jwk"
        const val CLAIM_KEY_VCT = "vct"
        const val CLAIM_KEY_VCT_INTEGRITY = "vct#integrity"
        const val CLAIM_KEY_VCT_METADATA_URI = "vct_metadata_uri"
        const val CLAIM_KEY_VCT_METADATA_URI_INTEGRITY = "vct_metadata_uri#integrity"
        const val CLAIM_KEY_STATUS = "status"
    }
}
