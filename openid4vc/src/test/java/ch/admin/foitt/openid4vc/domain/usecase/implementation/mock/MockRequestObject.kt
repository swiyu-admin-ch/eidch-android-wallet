package ch.admin.foitt.openid4vc.domain.usecase.implementation.mock

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.RequestObject
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.jwk.Curve
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64

object MockRequestObject {

    const val REQUEST_OBJECT_JWT_DID_STRING =
        "eyJraWQiOiJkaWQ6dGR3OmlkZW50aWZpZXIja2V5LTAxIiwiYWxnIjoiRVMyNTYifQ.eyJyZXNwb25zZV91cmkiOiJodHRwczovL2V4YW1wbGUuY29tIiwicmVzcG9uc2VfdHlwZSI6InZwX3Rva2VuIiwicHJlc2VudGF0aW9uX2RlZmluaXRpb24iOnsiaWQiOiIzZmE4NWY2NC0wMDAwLTAwMDAtYjNmYy0yYzk2M2Y2NmFmYTYiLCJuYW1lIjoic3RyaW5nIiwicHVycG9zZSI6InN0cmluZyIsImlucHV0X2Rlc2NyaXB0b3JzIjpbeyJpZCI6IjNmYTg1ZjY0LTU3MTctNDU2Mi1iM2ZjLTJjOTYzZjY2YWZhNiIsIm5hbWUiOiJBIG5hbWUiLCJmb3JtYXQiOnsidmMrc2Qtand0Ijp7InNkLWp3dF9hbGdfdmFsdWVzIjpbIkVTMjU2Il0sImtiLWp3dF9hbGdfdmFsdWVzIjpbIkVTMjU2Il19fSwiY29uc3RyYWludHMiOnsiZmllbGRzIjpbeyJwYXRoIjpbIiQubGFzdE5hbWUiXX1dfX1dfSwibm9uY2UiOiJJMDJGaWJMRjRrNUVzZkRPMmpnakRvb1A0QS9adWtRMyIsImNsaWVudF9pZCI6ImRlY2VudHJhbGl6ZWRfaWRlbnRpZmllcjpkaWQ6dGR3OmlkZW50aWZpZXIiLCJjbGllbnRfbWV0YWRhdGEiOnsiY2xpZW50X25hbWUiOiJSZWYgVGVzdCIsImxvZ29fdXJpIjoid3d3LmV4YW1wbGUuaWNvIn0sInJlc3BvbnNlX21vZGUiOiJkaXJlY3RfcG9zdCJ9.KIGSLxP07TKPVkX6oNcdW0OkmPwxmCjn8CXBc_mSZJwVPojUxX9tKd8qqVeMZAJw2A7HDte9BvNWC7bkCkT46w"

    val requestObjectJwtDid = Jwt(REQUEST_OBJECT_JWT_DID_STRING)

    const val REQUEST_OBJECT_JWT_NO_CLIENT_ID_STRING =
        "eyJraWQiOiJkaWQ6dGR3OmlkZW50aWZpZXIja2V5LTAxIiwiYWxnIjoiRVMyNTYifQ.eyJyZXNwb25zZV91cmkiOiJodHRwczovL2V4YW1wbGUuY29tIiwicmVzcG9uc2VfdHlwZSI6InZwX3Rva2VuIiwicHJlc2VudGF0aW9uX2RlZmluaXRpb24iOnsiaWQiOiIzZmE4NWY2NC0wMDAwLTAwMDAtYjNmYy0yYzk2M2Y2NmFmYTYiLCJuYW1lIjoic3RyaW5nIiwicHVycG9zZSI6InN0cmluZyIsImlucHV0X2Rlc2NyaXB0b3JzIjpbeyJpZCI6IjNmYTg1ZjY0LTU3MTctNDU2Mi1iM2ZjLTJjOTYzZjY2YWZhNiIsIm5hbWUiOiJBIG5hbWUiLCJmb3JtYXQiOnsidmMrc2Qtand0Ijp7InNkLWp3dF9hbGdfdmFsdWVzIjpbIkVTMjU2Il0sImtiLWp3dF9hbGdfdmFsdWVzIjpbIkVTMjU2Il19fSwiY29uc3RyYWludHMiOnsiZmllbGRzIjpbeyJwYXRoIjpbIiQubGFzdE5hbWUiXX1dfX1dfSwibm9uY2UiOiJJMDJGaWJMRjRrNUVzZkRPMmpnakRvb1A0QS9adWtRMyIsImNsaWVudF9tZXRhZGF0YSI6eyJjbGllbnRfbmFtZSI6IlJlZiBUZXN0IiwibG9nb191cmkiOiJ3d3cuZXhhbXBsZS5pY28ifSwicmVzcG9uc2VfbW9kZSI6ImRpcmVjdF9wb3N0In0.opFt6PRteAeVACohZdMZttvvPz0IayhWy8DZQ10nTQoqTmT0_uSGL8dkbHhi528_Jzm6OUnkKBOz2GHl1m8xRw"

    val requestObjectJwtNoClientId = Jwt(REQUEST_OBJECT_JWT_NO_CLIENT_ID_STRING)

    const val REQUEST_OBJECT_JWT_NO_KEY_ID_STRING =
        "eyJhbGciOiJFUzI1NiJ9.eyJyZXNwb25zZV91cmkiOiJodHRwczovL2V4YW1wbGUuY29tIiwicmVzcG9uc2VfdHlwZSI6InZwX3Rva2VuIiwicHJlc2VudGF0aW9uX2RlZmluaXRpb24iOnsiaWQiOiIzZmE4NWY2NC0wMDAwLTAwMDAtYjNmYy0yYzk2M2Y2NmFmYTYiLCJuYW1lIjoic3RyaW5nIiwicHVycG9zZSI6InN0cmluZyIsImlucHV0X2Rlc2NyaXB0b3JzIjpbeyJpZCI6IjNmYTg1ZjY0LTU3MTctNDU2Mi1iM2ZjLTJjOTYzZjY2YWZhNiIsIm5hbWUiOiJBIG5hbWUiLCJmb3JtYXQiOnsidmMrc2Qtand0Ijp7InNkLWp3dF9hbGdfdmFsdWVzIjpbIkVTMjU2Il0sImtiLWp3dF9hbGdfdmFsdWVzIjpbIkVTMjU2Il19fSwiY29uc3RyYWludHMiOnsiZmllbGRzIjpbeyJwYXRoIjpbIiQubGFzdE5hbWUiXX1dfX1dfSwibm9uY2UiOiJJMDJGaWJMRjRrNUVzZkRPMmpnakRvb1A0QS9adWtRMyIsImNsaWVudF9pZCI6ImRlY2VudHJhbGl6ZWRfaWRlbnRpZmllcjpkaWQ6dGR3OmlkZW50aWZpZXIiLCJjbGllbnRfbWV0YWRhdGEiOnsiY2xpZW50X25hbWUiOiJSZWYgVGVzdCIsImxvZ29fdXJpIjoid3d3LmV4YW1wbGUuaWNvIn0sInJlc3BvbnNlX21vZGUiOiJkaXJlY3RfcG9zdCJ9.86nGvleb5nVjgixzVMayUn39tdxcvhaSjv9Vt_dHhgcrM5x-YrdIuZ5E8CLzj4STVvxYYk-xLOeQ7lhCLyivZw"

    val requestObjectJwtNoKeyId = Jwt(REQUEST_OBJECT_JWT_NO_KEY_ID_STRING)

    const val REQUEST_OBJECT_JWT_INVALID_DID_STRING =
        "eyJraWQiOiJkaWQ6dGR3OmlkZW50aWZpZXIja2V5LTAxIiwiYWxnIjoiRVMyNTYifQ.eyJyZXNwb25zZV91cmkiOiJodHRwczovL2V4YW1wbGUuY29tIiwicmVzcG9uc2VfdHlwZSI6InZwX3Rva2VuIiwicHJlc2VudGF0aW9uX2RlZmluaXRpb24iOnsiaWQiOiIzZmE4NWY2NC0wMDAwLTAwMDAtYjNmYy0yYzk2M2Y2NmFmYTYiLCJuYW1lIjoic3RyaW5nIiwicHVycG9zZSI6InN0cmluZyIsImlucHV0X2Rlc2NyaXB0b3JzIjpbeyJpZCI6IjNmYTg1ZjY0LTU3MTctNDU2Mi1iM2ZjLTJjOTYzZjY2YWZhNiIsIm5hbWUiOiJBIG5hbWUiLCJmb3JtYXQiOnsidmMrc2Qtand0Ijp7InNkLWp3dF9hbGdfdmFsdWVzIjpbIkVTMjU2Il0sImtiLWp3dF9hbGdfdmFsdWVzIjpbIkVTMjU2Il19fSwiY29uc3RyYWludHMiOnsiZmllbGRzIjpbeyJwYXRoIjpbIiQubGFzdE5hbWUiXX1dfX1dfSwibm9uY2UiOiJJMDJGaWJMRjRrNUVzZkRPMmpnakRvb1A0QS9adWtRMyIsImNsaWVudF9pZCI6ImRlY2VudHJhbGl6ZWRfaWRlbnRpZmllcjpub3RBRGlkIiwiY2xpZW50X21ldGFkYXRhIjp7ImNsaWVudF9uYW1lIjoiUmVmIFRlc3QiLCJsb2dvX3VyaSI6Ind3dy5leGFtcGxlLmljbyJ9LCJyZXNwb25zZV9tb2RlIjoiZGlyZWN0X3Bvc3QifQ.JHP378Fz0WTB19bgNMPMNIG7AYhFly3U-rccsxwQOfu-F0cbXpBXElDSxfo-ThBe8V59GcpgzqyDXBtdZe8g2w"

    val requestObjectJwtInvalidDid = Jwt(REQUEST_OBJECT_JWT_INVALID_DID_STRING)
    const val REQUEST_OBJECT_JWT_OTHER_KID =
        "eyJraWQiOiJkaWQ6dGR3Om90aGVySWRlbnRpZmllciNrZXktMDEiLCJhbGciOiJFUzI1NiJ9.eyJyZXNwb25zZV91cmkiOiJodHRwczovL2V4YW1wbGUuY29tIiwicmVzcG9uc2VfdHlwZSI6InZwX3Rva2VuIiwicHJlc2VudGF0aW9uX2RlZmluaXRpb24iOnsiaWQiOiIzZmE4NWY2NC0wMDAwLTAwMDAtYjNmYy0yYzk2M2Y2NmFmYTYiLCJuYW1lIjoic3RyaW5nIiwicHVycG9zZSI6InN0cmluZyIsImlucHV0X2Rlc2NyaXB0b3JzIjpbeyJpZCI6IjNmYTg1ZjY0LTU3MTctNDU2Mi1iM2ZjLTJjOTYzZjY2YWZhNiIsIm5hbWUiOiJBIG5hbWUiLCJmb3JtYXQiOnsidmMrc2Qtand0Ijp7InNkLWp3dF9hbGdfdmFsdWVzIjpbIkVTMjU2Il0sImtiLWp3dF9hbGdfdmFsdWVzIjpbIkVTMjU2Il19fSwiY29uc3RyYWludHMiOnsiZmllbGRzIjpbeyJwYXRoIjpbIiQubGFzdE5hbWUiXX1dfX1dfSwibm9uY2UiOiJJMDJGaWJMRjRrNUVzZkRPMmpnakRvb1A0QS9adWtRMyIsImNsaWVudF9pZCI6ImRlY2VudHJhbGl6ZWRfaWRlbnRpZmllcjpkaWQ6dGR3OmlkZW50aWZpZXIiLCJjbGllbnRfbWV0YWRhdGEiOnsiY2xpZW50X25hbWUiOiJSZWYgVGVzdCIsImxvZ29fdXJpIjoid3d3LmV4YW1wbGUuaWNvIn0sInJlc3BvbnNlX21vZGUiOiJkaXJlY3RfcG9zdCJ9.eLHTrhZOhciUaVpJLKs1sdheKHJUEn6MS4LDIlEZ99h0M96brEhu9DzkPAgTvMAcgxjnOSXHfTH29iPUryUmaQ"

    val requestObjectJwtOtherKid = Jwt(REQUEST_OBJECT_JWT_OTHER_KID)

    val didRequestObject = RequestObject(
        clientId = CLIENT_ID_DID,
        jwt = requestObjectJwtDid,
        redirectUri = null,
    )

    const val CLIENT_ID_PREFIX_DID = "decentralized_identifier:"
    const val DID = "did:tdw:identifier"
    const val CLIENT_ID_DID = "$CLIENT_ID_PREFIX_DID$DID"
    const val KEY_ID = "$DID#key-01"
    const val KEY_ID_OTHER = "did:tdw:otherIdentifier#key-01"

    // ── verifier-attestation fixtures ───────────────────────────────────────
    const val CLIENT_ID_PREFIX_ATTESTATION = "verifier_attestation:"
    const val CLIENT_ID_VERIFIER = "verifier.example.org"
    const val ATTESTATION_CLIENT_ID = "$CLIENT_ID_PREFIX_ATTESTATION$CLIENT_ID_VERIFIER"
    const val ATTESTATION_ISSUER_DID = "did:tdw:attestationIssuer"
    const val ATTESTATION_KEY_ID = "$ATTESTATION_ISSUER_DID#key-01"
    private const val ATTESTATION_HEADER_TYP = "verifier-attestation+jwt"

    private val testPrivateKey: ECPrivateKey by lazy {
        val privateKeyBase64 = "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQglBnO+qn+RecAQ31T" +
            "jBklNu+AwiFN5eVHBFbnjecmMryhRANCAARGpVef6j7rMQ6lYSwbDkKwH7B3zM6P" +
            "G7S4BIamIY/7Bh9xzW6fIzFxK1sPNSNG45tjwNqVoIn38npSuRCRkG1n"
        val bytes = Base64.getDecoder().decode(privateKeyBase64)
        KeyFactory.getInstance("EC").generatePrivate(PKCS8EncodedKeySpec(bytes)) as ECPrivateKey
    }

    private fun signJwt(headerBuilder: JWSHeader.Builder, payloadJson: String): String {
        val jws = JWSObject(headerBuilder.build(), Payload(payloadJson))
        jws.sign(ECDSASigner(testPrivateKey, Curve.P_256))
        return jws.serialize()
    }

    private val attestationJwtRaw: String by lazy {
        val payload = """
            {
              "sub": "$CLIENT_ID_VERIFIER",
              "cnf": { "jwk": { "kty": "EC", "crv": "P-256", "x": "xValue", "y": "yValue" } }
            }
        """.trimIndent()
        signJwt(
            JWSHeader.Builder(JWSAlgorithm.ES256)
                .type(JOSEObjectType(ATTESTATION_HEADER_TYP))
                .keyID(ATTESTATION_KEY_ID),
            payload,
        )
    }

    val requestObjectJwtAttestation: Jwt by lazy {
        val outerPayload = """{"client_id":"$ATTESTATION_CLIENT_ID"}"""
        val raw = signJwt(
            JWSHeader.Builder(JWSAlgorithm.ES256)
                .customParam("jwt", attestationJwtRaw),
            outerPayload,
        )
        Jwt(raw)
    }

    val attestationRequestObject: RequestObject by lazy {
        RequestObject(
            clientId = ATTESTATION_CLIENT_ID,
            jwt = requestObjectJwtAttestation,
            redirectUri = null,
        )
    }
}
