package ch.admin.foitt.openid4vc.domain.model.vcSdJwt

import ch.admin.foitt.openid4vc.util.SafeJsonTestInstance
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

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
    fun `Creating a VcSdJwt without the iss claim throws an exception`() = runTest {
        assertThrows<IllegalStateException> {
            VcSdJwt(VC_SD_JWT_MISSING_ISS)
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
    }
}
