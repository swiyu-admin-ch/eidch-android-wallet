package ch.admin.foitt.openid4vc.domain.model.vcSdJwt

import ch.admin.foitt.openid4vc.util.SafeJsonTestInstance
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.jwk.Curve
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.interfaces.ECPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Instant
import java.util.Base64

class VcSdJwtTest {
    @Test
    fun `Creating a VcSdJwt with a valid vcSdJwt succeeds`() = runTest {
        val vcSdJwt = VcSdJwt(VALID_VC_SD_JWT)

        assertEquals(KID, vcSdJwt.kid)
        assertEquals(ISS, vcSdJwt.iss)
        assertEquals(VCT, vcSdJwt.vct)
        assertEquals(SafeJsonTestInstance.json.parseToJsonElement(CNF_JWK), vcSdJwt.cnfJwk)
        assertEquals(SafeJsonTestInstance.json.parseToJsonElement(STATUS), vcSdJwt.status)
    }

    @Test
    fun `Creating a VcSdJwt without the kid claim throws an exception`() = runTest {
        assertThrows<IllegalStateException> {
            VcSdJwt(VC_SD_JWT_MISSING_KID)
        }
    }

    @Test
    fun `Creating a VcSdJwt without the vct claim throws an exception`() = runTest {
        assertThrows<IllegalStateException> {
            VcSdJwt(VC_SD_JWT_MISSING_VCT)
        }
    }

    @Test
    fun `Creating a VcSdJwt without the cnf claim succeeds but has a null field`() = runTest {
        val vcSdJwt = VcSdJwt(VC_SD_JWT_WITHOUT_CNF)
        assertNull(vcSdJwt.cnfJwk)
    }

    // Support for both malformed and standard format of cnf claim
    @Test
    fun `Creating a VcSdJwt with a malformed cnf claim still succeeds and has a valid cnfJwk field`() = runTest {
        val vcSdJwt = VcSdJwt(VC_SD_JWT_WITH_MALFORMED_CNF)
        assertEquals(SafeJsonTestInstance.json.parseToJsonElement(CNF_JWK), vcSdJwt.cnfJwk)
    }

    // Support for both malformed and standard format of cnf claim
    @Test
    fun `Creating a VcSdJwt with an expanded cnf claim still succeeds and use the contract cnfJwk field`() = runTest {
        val vcSdJwt = VcSdJwt(VC_SD_JWT_WITH_EXPANDED_CNF)
        assertEquals(SafeJsonTestInstance.json.parseToJsonElement(CNF_JWK), vcSdJwt.cnfJwk)
    }

    @Test
    fun `Creating a VcSdJwt without the status claim succeeds but has a null field`() = runTest {
        val vcSdJwt = VcSdJwt(
            VC_SD_JWT_WITHOUT_STATUS
        )
        assertNull(vcSdJwt.status)
    }

    @Test
    fun `Creating a VcSdJwt containing unregistered non-selectively disclosable claims throws an exception`() = runTest {
        assertThrows<IllegalStateException> {
            VcSdJwt(VC_SD_JWT_WITH_NON_SELECTIVELY_DISCLOSABLE_CLAIM)
        }
    }

    @Test
    fun `Creating a VcSdJwt without an expiry_date set results in a null value for businessExpiryDate`() {
        val vcSdJwt = VcSdJwt(VALID_VC_SD_JWT)
        assertNull(vcSdJwt.businessExpiryDate)
    }

    @Test
    fun `Creating a VcSdJwt with an expiry-date disclosure in full-date format correctly parses the businessExpiryDate`() {
        val vcSdJwt = VcSdJwt(VC_SD_JWT_WITH_FULL_DATE_EXPIRY_DISCLOSURE)
        assertEquals(Instant.parse("2020-01-01T23:59:59Z").epochSecond, vcSdJwt.businessExpiryDate?.epochSecond)
    }

    @Test
    fun `Creating a VcSdJwt with an expiry_date disclosure in epoch-seconds format correctly parses the businessExpiryDate`() {
        val vcSdJwt = VcSdJwt(VC_SD_JWT_WITH_EPOCH_SECONDS_EXPIRY_DISCLOSURE)
        assertEquals(1577923199, vcSdJwt.businessExpiryDate?.epochSecond)
    }

    @Test
    fun `Creating a VcSdJwt with an unparsable expiry_date disclosure value results in a null businessExpiryDate`() {
        val vcSdJwt = VcSdJwt(VC_SD_JWT_WITH_UNPARSABLE_EXPIRY_DISCLOSURE)
        assertNull(vcSdJwt.businessExpiryDate)
    }

    @Test
    fun `Creating a VcSdJwt with an expiry date in the payload throws an exception`() {
        assertThrows<IllegalStateException> {
            VcSdJwt(VC_SD_JWT_WITH_EXPIRY_DATE_IN_PAYLOAD)
        }
    }

    private companion object {
        const val KID = "keyId"
        const val ISS = "issuer"
        const val VCT = "vct"

        val CNF_JWK = """
          {
            "kty": "EC",
            "crv": "P-256",
            "x": "xValue",
            "y": "yValue"
          }
        """.trimIndent()

        val STATUS = """
          {
            "status_list": {
              "uri": "example.com",
              "idx": 1
            }
          }
        """.trimIndent()

        /*
        -----BEGIN PUBLIC KEY-----
        MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAERqVXn+o+6zEOpWEsGw5CsB+wd8zO
        jxu0uASGpiGP+wYfcc1unyMxcStbDzUjRuObY8DalaCJ9/J6UrkQkZBtZw==
        -----END PUBLIC KEY-----
        -----BEGIN PRIVATE KEY-----
        MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQglBnO+qn+RecAQ31T
        jBklNu+AwiFN5eVHBFbnjecmMryhRANCAARGpVef6j7rMQ6lYSwbDkKwH7B3zM6P
        G7S4BIamIY/7Bh9xzW6fIzFxK1sPNSNG45tjwNqVoIn38npSuRCRkG1n
        -----END PRIVATE KEY-----
         */

        /*
        header:
        {
          "alg":"ES256",
          "typ":"type",
          "kid":"keyId"
        }
        payload:
        {
          "iss":"issuer",
          "vct":"vct",
          "cnf": {
            "jwk": {
              "kty": "EC",
              "crv": "P-256",
              "x": "xValue",
              "y": "yValue"
            }
          },
          "status": {
            "status_list": {
              "uri": "example.com",
              "idx": 1
            }
          }
        }
         */
        const val VALID_VC_SD_JWT =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InR5cGUiLCJraWQiOiJrZXlJZCJ9.eyJpc3MiOiJpc3N1ZXIiLCJ2Y3QiOiJ2Y3QiLCJjbmYiOnsiandrIjp7Imt0eSI6IkVDIiwiY3J2IjoiUC0yNTYiLCJ4IjoieFZhbHVlIiwieSI6InlWYWx1ZSJ9fSwic3RhdHVzIjp7InN0YXR1c19saXN0Ijp7InVyaSI6ImV4YW1wbGUuY29tIiwiaWR4IjoxfX19.m1LjSGvqosKczHnNyRKhYa4NJY0OytOkobmOLLEPEeY8W7_ziWqENWvDZvWwQN8pUQWSklEjx4hpP3P5L8Nxvw~"
        const val VC_SD_JWT_MISSING_ISS =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InR5cGUiLCJraWQiOiJrZXlJZCJ9.eyJ2Y3QiOiJ2Y3QiLCJjbmYiOnsiandrIjp7Imt0eSI6IkVDIiwiY3J2IjoiUC0yNTYiLCJ4IjoieFZhbHVlIiwieSI6InlWYWx1ZSJ9fSwic3RhdHVzIjp7InN0YXR1c19saXN0Ijp7InVyaSI6ImV4YW1wbGUuY29tIiwiaWR4IjoxfX19.RD4NLxbY2xTGLYIhfubdIa3FHdo0xoiWSBip1kWn2gKTyfhHNfpr_YnxjoPfmTtX24RU7aF60zOg4VVch-ytqA~"
        const val VC_SD_JWT_MISSING_KID =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InR5cGUifQ.eyJpc3MiOiJpc3N1ZXIiLCJ2Y3QiOiJ2Y3QiLCJjbmYiOnsiandrIjp7Imt0eSI6IkVDIiwiY3J2IjoiUC0yNTYiLCJ4IjoieFZhbHVlIiwieSI6InlWYWx1ZSJ9fSwic3RhdHVzIjp7InN0YXR1c19saXN0Ijp7InVyaSI6ImV4YW1wbGUuY29tIiwiaWR4IjoxfX19._4Rq8DUl-0zVn0cTJz48aTHxq-taHNm6oaKRxq3m3VwhUj27FnSVDSPSu7wEiIQ-rrV_0zppPXiN9jGiCqygvA~"
        const val VC_SD_JWT_MISSING_VCT =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InR5cGUiLCJraWQiOiJrZXlJZCJ9.eyJpc3MiOiJpc3N1ZXIiLCJjbmYiOnsiandrIjp7Imt0eSI6IkVDIiwiY3J2IjoiUC0yNTYiLCJ4IjoieFZhbHVlIiwieSI6InlWYWx1ZSJ9fSwic3RhdHVzIjp7InN0YXR1c19saXN0Ijp7InVyaSI6ImV4YW1wbGUuY29tIiwiaWR4IjoxfX19.8J3_OBp2BYVhavLgOUPtT-z7uEnmbQGo5bw9jLk4H6KZmKFvT3RHbwiUAVC55LEqC0l6DoXLtZzF_4jBBJ2HqQ~"
        const val VC_SD_JWT_WITHOUT_CNF =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InR5cGUiLCJraWQiOiJrZXlJZCJ9.eyJpc3MiOiJpc3N1ZXIiLCJ2Y3QiOiJ2Y3QiLCJzdGF0dXMiOnsic3RhdHVzX2xpc3QiOnsidXJpIjoiZXhhbXBsZS5jb20iLCJpZHgiOjF9fX0.1ma9gUUfhPY1_1DAQ6zs7oGP_Dqaebk1bE4SxYJ6xogLG0K-XAfXcbyCUpoH4Esj2-p0qR-cX3bv__NkgvpvEQ~"
        const val VC_SD_JWT_WITH_MALFORMED_CNF =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InR5cGUiLCJraWQiOiJrZXlJZCJ9.eyJpc3MiOiJpc3N1ZXIiLCJ2Y3QiOiJ2Y3QiLCJjbmYiOnsia3R5IjoiRUMiLCJjcnYiOiJQLTI1NiIsIngiOiJ4VmFsdWUiLCJ5IjoieVZhbHVlIn0sInN0YXR1cyI6eyJzdGF0dXNfbGlzdCI6eyJ1cmkiOiJleGFtcGxlLmNvbSIsImlkeCI6MX19fQ.zEjzlliqCDzlcs7UFAOJQi9xXi_uQNXcLKqZZ2cQq-ZpnUHsxZoScxokLcFv1L4j1QKDsZXlg3yhQYQo6D66Hg~"
        const val VC_SD_JWT_WITH_EXPANDED_CNF =
            "eyJ0eXAiOiJ0eXBlIiwiYWxnIjoiRVMyNTYiLCJraWQiOiJrZXlJZCJ9.eyJpc3MiOiJpc3N1ZXIiLCJ2Y3QiOiJ2Y3QiLCJjbmYiOnsia3R5IjoiRUIiLCJjcnYiOiJQLUJhZCIsIngiOiJiYWRYIiwieSI6ImJhZFkiLCJqd2siOnsia3R5IjoiRUMiLCJjcnYiOiJQLTI1NiIsIngiOiJ4VmFsdWUiLCJ5IjoieVZhbHVlIn19LCJzdGF0dXMiOnsic3RhdHVzX2xpc3QiOnsidXJpIjoiZXhhbXBsZS5jb20iLCJpZHgiOjF9fX0.QaR1Zi1mwPiWPg4fkUzs9KMPAeYx1sRQzN4WbWrZB52AKrknZUXrfiutUHj0f2l15VSReyADUsLduLMHFcHFWg~"
        const val VC_SD_JWT_WITHOUT_STATUS =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InR5cGUiLCJraWQiOiJrZXlJZCJ9.eyJpc3MiOiJpc3N1ZXIiLCJ2Y3QiOiJ2Y3QiLCJjbmYiOnsiandrIjp7Imt0eSI6IkVDIiwiY3J2IjoiUC0yNTYiLCJ4IjoieFZhbHVlIiwieSI6InlWYWx1ZSJ9fX0.C9hZ3sNghE7U749Di6nNNrrhiC1aRmewfBcDTYD2qgPa2WpwpCuh2EOExwzNWXpop9RWXez9-Rbyni7o30aE1g~"
        const val VC_SD_JWT_WITH_NON_SELECTIVELY_DISCLOSABLE_CLAIM =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InR5cGUiLCJraWQiOiJrZXlJZCJ9.eyJpc3MiOiJpc3N1ZXIiLCJ2Y3QiOiJ2Y3QiLCJjbmYiOnsiandrIjp7Imt0eSI6IkVDIiwiY3J2IjoiUC0yNTYiLCJ4IjoieFZhbHVlIiwieSI6InlWYWx1ZSJ9fSwic3RhdHVzIjp7InN0YXR1c19saXN0Ijp7InVyaSI6ImV4YW1wbGUuY29tIiwiaWR4IjoxfX0sInN1YiI6InN1YmplY3QifQ.Kk248WapVHlKBM1Uo7dIluono5RchPkzqMUZNFy5Sz8hAFul5eem6Ggc2jZYNQ4a5PtvNGicOqdYs0Ucxigb7w~"

        const val VC_SD_JWT_WITH_EXPIRY_DATE_IN_PAYLOAD =
            "eyJraWQiOiJrZXlJZCIsInR5cCI6InR5cGUiLCJhbGciOiJFUzI1NiJ9.ewogICJpc3MiOiAiaXNzdWVyIiwKICAidmN0IjogInZjdCIsCiAgImNuZiI6IHsKICAgICJqd2siOiB7CiAgICAgICJrdHkiOiAiRUMiLAogICAgICAiY3J2IjogIlAtMjU2IiwKICAgICAgIngiOiAieFZhbHVlIiwKICAgICAgInkiOiAieVZhbHVlIgogICAgfQogIH0sCiAgInN0YXR1cyI6IHsKICAgICJzdGF0dXNfbGlzdCI6IHsKICAgICAgInVyaSI6ICJleGFtcGxlLmNvbSIsCiAgICAgICJpZHgiOiAxCiAgICB9CiAgfSwKICAiZXhwaXJ5X2RhdGUiOiAiMjAyMC0wMS0wMSIKfQ.D5NtKo4goXUHHQbzLEIt5EJTR_oaCjYeFWxRyJUii6ZKa-FS9xvY-uI5W6kbdC8Qp_jH3PrM9_W34n4SQmbRVg~"

        const val VC_SD_JWT_WITH_FULL_DATE_EXPIRY_DISCLOSURE =
            "eyJraWQiOiJrZXlJZCIsInR5cCI6InR5cGUiLCJhbGciOiJFUzI1NiJ9.ewogICJpc3MiOiAiaXNzdWVyIiwKICAidmN0IjogInZjdCIsCiAgImNuZiI6IHsKICAgICJqd2siOiB7ICJrdHkiOiAiRUMiLCAiY3J2IjogIlAtMjU2IiwgIngiOiAieFZhbHVlIiwgInkiOiAieVZhbHVlIiB9CiAgfSwKICAic3RhdHVzIjogeyAic3RhdHVzX2xpc3QiOiB7ICJ1cmkiOiAiZXhhbXBsZS5jb20iLCAiaWR4IjogMSB9IH0sCiAgIl9zZCI6IFsiejFvUGVCbDIyUTRjVWFBOVlGUnVtYThWZXR1bjgzM3ZsbHQtUVZVaFZzYyJdLAogICJfc2RfYWxnIjogInNoYS0yNTYiCn0.qGv-cRAKHqWdRxe8D0fw51yAYEo4RjRKHKiYrWCE8_98fpM9ndj7wFGlRRq6M5dPWK55nCAxWaVpQpfkav3k0w~WyJzYWx0Rm9yRXhwaXJ5RGF0ZTEiLCJleHBpcnlfZGF0ZSIsIjIwMjAtMDEtMDEiXQ~"

        const val VC_SD_JWT_WITH_EPOCH_SECONDS_EXPIRY_DISCLOSURE =
            "eyJraWQiOiJrZXlJZCIsInR5cCI6InR5cGUiLCJhbGciOiJFUzI1NiJ9.ewogICJpc3MiOiAiaXNzdWVyIiwKICAidmN0IjogInZjdCIsCiAgImNuZiI6IHsKICAgICJqd2siOiB7ICJrdHkiOiAiRUMiLCAiY3J2IjogIlAtMjU2IiwgIngiOiAieFZhbHVlIiwgInkiOiAieVZhbHVlIiB9CiAgfSwKICAic3RhdHVzIjogeyAic3RhdHVzX2xpc3QiOiB7ICJ1cmkiOiAiZXhhbXBsZS5jb20iLCAiaWR4IjogMSB9IH0sCiAgIl9zZCI6IFsiUU1Pc0JVb2FfQXk0MDBBYWtjWEowUTNSRTRmWUl3eW9yYWZVWm9wU19tTSJdLAogICJfc2RfYWxnIjogInNoYS0yNTYiCn0.kw6T5vUGH2lPust2PTQaDZyC-Ag-Q-8U1smjYOqYmi6i0yTww7SgJOqpgTUuknD11ezl9k6kkLrsZDscjpm20w~WyJzYWx0Rm9yRXhwaXJ5RGF0ZTEiLCJleHBpcnlfZGF0ZSIsIjE1Nzc5MjMxOTkiXQ~"

        const val VC_SD_JWT_WITH_UNPARSABLE_EXPIRY_DISCLOSURE =
            "eyJraWQiOiJrZXlJZCIsInR5cCI6InR5cGUiLCJhbGciOiJFUzI1NiJ9.ewogICJpc3MiOiAiaXNzdWVyIiwKICAidmN0IjogInZjdCIsCiAgImNuZiI6IHsKICAgICJqd2siOiB7ICJrdHkiOiAiRUMiLCAiY3J2IjogIlAtMjU2IiwgIngiOiAieFZhbHVlIiwgInkiOiAieVZhbHVlIiB9CiAgfSwKICAic3RhdHVzIjogeyAic3RhdHVzX2xpc3QiOiB7ICJ1cmkiOiAiZXhhbXBsZS5jb20iLCAiaWR4IjogMSB9IH0sCiAgIl9zZCI6IFsic2lxX0ctOWNUcUdkWHFpdTdRQWFuQnFISTBUWFRlSlhDTkowd1Exc2dKcyJdLAogICJfc2RfYWxnIjogInNoYS0yNTYiCn0.Y2e7Gt_HidMwfSUBbB5OA1s0bS3KMHbCL2KmO2LKwUkzPVsM8bDFpjfWNntLLisGXJ92GAYKEP5M_rn33HfolQ~WyJzYWx0Rm9yRXhwaXJ5RGF0ZTEiLCJleHBpcnlfZGF0ZSIsIk5vdCBwYXJzYWJsZSJd~"
    }
}

/**
 * Run these tests once to generate token strings, then copy the output into VcSdJwtTest.kt.
 *
 * To add more disclosures:
 * 1. Add a new entry to the [disclosures] map with key = claim name, value = claim value
 * 2. The generator will compute the correct digest and add it to `_sd` automatically
 * 3. Run the test and copy the printed token into VcSdJwtTest.kt
 */
class GenerateVcSdJwt {

    private val base64Encoder = Base64.getUrlEncoder().withoutPadding()
    private val base64Decoder = Base64.getDecoder()

    private val privateKey: ECPrivateKey by lazy {
        val privateKeyBase64 = "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQglBnO+qn+RecAQ31T" +
            "jBklNu+AwiFN5eVHBFbnjecmMryhRANCAARGpVef6j7rMQ6lYSwbDkKwH7B3zM6P" +
            "G7S4BIamIY/7Bh9xzW6fIzFxK1sPNSNG45tjwNqVoIn38npSuRCRkG1n"
        val privateKeyBytes = base64Decoder.decode(privateKeyBase64.replace("\n", ""))
        KeyFactory.getInstance("EC").generatePrivate(PKCS8EncodedKeySpec(privateKeyBytes)) as ECPrivateKey
    }

    /**
     * Computes the Base64url-encoded SHA-256 digest of a disclosure string.
     * This digest is what goes into the `_sd` array of the JWT payload.
     */
    private fun computeDisclosureDigest(disclosure: String): String {
        val hash = MessageDigest.getInstance("SHA-256").digest(disclosure.toByteArray())
        return base64Encoder.encodeToString(hash)
    }

    /**
     * Encodes a disclosure [salt, key, value] tuple as a Base64url string.
     */
    private fun encodeDisclosure(salt: String, key: String, value: String): String {
        val json = """["$salt","$key","$value"]"""
        return base64Encoder.encodeToString(json.toByteArray())
    }

    /**
     * Signs a payload with the test private key and returns the full SD-JWT token string.
     *
     * @param payloadJson the JWT payload as a JSON string
     * @param disclosures list of encoded disclosure strings to append after the `~` separator
     */
    private fun signToken(payloadJson: String, disclosures: List<String> = emptyList()): String {
        val header = JWSHeader.Builder(JWSAlgorithm.ES256)
            .type(com.nimbusds.jose.JOSEObjectType("type"))
            .keyID("keyId")
            .build()
        val jwsObject = JWSObject(header, Payload(payloadJson))
        jwsObject.sign(ECDSASigner(privateKey, Curve.P_256))
        return buildString {
            append(jwsObject.serialize())
            append("~")
            disclosures.forEach { disclosure ->
                append(disclosure)
                append("~")
            }
        }
    }

    @Test
    fun `generate VC SD JWT with expiry_date in payload`() {
        // expiry_date is directly in the payload — NOT a disclosure.
        // This token should fail VcSdJwt init check.
        val token = signToken(
            payloadJson = """
                {
                  "iss": "issuer",
                  "vct": "vct",
                  "cnf": {
                    "jwk": { "kty": "EC", "crv": "P-256", "x": "xValue", "y": "yValue" }
                  },
                  "status": { "status_list": { "uri": "example.com", "idx": 1 } },
                  "expiry_date": "2020-01-01"
                }
            """.trimIndent()
        )
        println("VC_SD_JWT_WITH_EXPIRY_DATE_IN_PAYLOAD = \"$token\"")
    }

    @Test
    fun `generate VC SD JWT with expiry_date as full-date disclosure`() {
        // expiry_date is a disclosure with a full-date value (RFC 8943)
        val disclosure = encodeDisclosure(
            salt = "saltForExpiryDate1",
            key = "expiry_date",
            value = "Not parsable"
        )
        val digest = computeDisclosureDigest(disclosure)

        val token = signToken(
            payloadJson = """
                {
                  "iss": "issuer",
                  "vct": "vct",
                  "cnf": {
                    "jwk": { "kty": "EC", "crv": "P-256", "x": "xValue", "y": "yValue" }
                  },
                  "status": { "status_list": { "uri": "example.com", "idx": 1 } },
                  "_sd": ["$digest"],
                  "_sd_alg": "sha-256"
                }
            """.trimIndent(),
            disclosures = listOf(disclosure)
        )
        println("Disclosure (full-date): $disclosure")
        println("VC_SD_JWT_WITH_FULL_DATE_EXPIRY_DISCLOSURE = \"$token\"")
    }

    @Test
    fun `generate VC SD JWT with expiry_date as epoch-second disclosure`() {
        // expiry_date is a disclosure with an epoch-second value
        // 1577836800 = 2020-01-01T00:00:00Z
        val disclosure = encodeDisclosure(
            salt = "saltForExpiryDate2",
            key = "expiry_date",
            value = "INVALID"
        )
        val digest = computeDisclosureDigest(disclosure)

        val token = signToken(
            payloadJson = """
                {
  "nbf": 1722499200,
  "vct": "https://credentials.example.com/identity_credential",
  "vct#integrity": "sha256-abcdef",
  "vct_metadata_uri": "https://metadata.example.com/metadata",
  "vct_metadata_uri#integrity": "sha256-metadata",
  "iss": "did:tdw:example",
  "cnf": {
    "jwk": {
      "kty": "EC",
      "crv": "P-256",
      "x": "1rBmcvygc51308vVv-4EGIqvLrheRqMkfpsDLCT9ecI",
      "y": "VKlPl4xkvIkFin9BHKxun_i-In8s-Zzq-asiFYD4WE8"
    }
  },
  "exp": 3374557641,
  "iat": 3348292041,
  "status": {
    "status_list": {
      "uri": "https://example.com/statuslist/example.jwt",
      "idx": 285,
      "type": "SwissTokenStatusList-1.0"
    }
  },
  "_sd": [
    "$digest"
  ],
  "_sd_alg": "SHA-256"
}
            """.trimIndent(),
            disclosures = listOf(disclosure)
        )
        println("Disclosure (epoch-second): $disclosure")
        println("VC_SD_JWT_WITH_EPOCH_SECOND_EXPIRY_DISCLOSURE = \"$token\"")
    }
}
