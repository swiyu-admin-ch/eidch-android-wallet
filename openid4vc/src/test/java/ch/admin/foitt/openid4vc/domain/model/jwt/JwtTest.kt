package ch.admin.foitt.openid4vc.domain.model.jwt

import ch.admin.foitt.openid4vc.domain.model.anycredential.Validity
import ch.admin.foitt.openid4vc.util.SafeJsonTestInstance
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.text.ParseException
import java.time.Instant

class JwtTest {

    @Test
    fun `Parsing a valid jwt succeeds`() = runTest {
        val jwt = Jwt(VALID_JWT)

        assertEquals(VALID_JWT, jwt.signedJwt.parsedString)
        assertEquals(VALID_JWT_PAYLOAD, jwt.payloadString)
        assertEquals(SafeJsonTestInstance.json.parseToJsonElement(VALID_JWT_PAYLOAD).jsonObject, jwt.payloadJson)
        assertEquals(TYP, jwt.type)
        assertEquals(KID, jwt.keyId)
        assertEquals(ISS, jwt.iss)
        assertEquals(SUB, jwt.subject)
        assertEquals(Instant.ofEpochSecond(IAT.toLong()), jwt.issuedAt)
        assertEquals(Instant.ofEpochSecond(EXP.toLong()), jwt.expInstant)
        assertEquals(Instant.ofEpochSecond(NBF.toLong()), jwt.nbfInstant)
    }

    @Test
    fun `Parsing an invalid jwt throws an exception`() = runTest {
        assertThrows<ParseException> {
            Jwt(rawJwt = "notAValidJwt")
        }
    }

    @Test
    fun `Parsing a valid jwt without typ claim succeeds but has null field`() = runTest {
        val jwt = Jwt(JWT_MISSING_TYP)
        assertNull(jwt.type)
    }

    @Test
    fun `Parsing a valid jwt without kid claim succeeds but has null field`() = runTest {
        val jwt = Jwt(JWT_MISSING_KID)
        assertNull(jwt.keyId)
    }

    @Test
    fun `Parsing a valid jwt without iss claim succeeds but has null field`() = runTest {
        val jwt = Jwt(JWT_MISSING_ISS)
        assertNull(jwt.iss)
    }

    @Test
    fun `Parsing a valid jwt without sub claim succeeds but has null field`() = runTest {
        val jwt = Jwt(JWT_MISSING_SUB)
        assertNull(jwt.subject)
    }

    @Test
    fun `Parsing a valid jwt without iat claim succeeds but has null field`() = runTest {
        val jwt = Jwt(JWT_MISSING_IAT)
        assertNull(jwt.issuedAt)
    }

    @Test
    fun `Parsing a valid jwt without exp claim succeeds but has null field`() = runTest {
        val jwt = Jwt(JWT_MISSING_EXP)
        assertNull(jwt.expInstant)
    }

    @Test
    fun `Parsing a valid jwt without nbf claim succeeds but has null field`() = runTest {
        val jwt = Jwt(JWT_MISSING_NBF)
        assertNull(jwt.nbfInstant)
    }

    @Test
    fun `Parsing a valid jwt with valid time stamps succeeds and is valid`() = runTest {
        val jwt = Jwt(VALID_JWT)
        assertEquals(Validity.Valid, jwt.jwtValidity)
    }

    @Test
    fun `Parsing a valid jwt with missing exp claim succeeds and is valid`() = runTest {
        val jwt = Jwt(JWT_MISSING_EXP)
        assertEquals(Validity.Valid, jwt.jwtValidity)
    }

    @Test
    fun `Parsing a valid jwt with missing nbf claim succeeds and is valid`() = runTest {
        val jwt = Jwt(JWT_MISSING_NBF)
        assertEquals(Validity.Valid, jwt.jwtValidity)
    }

    @Test
    fun `Parsing a valid jwt with expired time stamps succeeds and is expired`() = runTest {
        val jwt = Jwt(EXPIRED_JWT)
        assertEquals(Validity.Expired(Instant.ofEpochSecond(0)), jwt.jwtValidity)
    }

    @Test
    fun `Parsing a valid jwt with not yet valid time stamps succeeds and is not yet valid`() = runTest {
        val jwt = Jwt(NOT_YET_VALID_JWT)
        assertEquals(Validity.NotYetValid(Instant.ofEpochSecond(1924988399)), jwt.jwtValidity)
    }

    private companion object {
        const val TYP = "type"
        const val KID = "keyId"
        const val ISS = "issuer"
        const val SUB = "subject"
        const val EXP = 1924988399
        const val IAT = 0
        const val NBF = 1

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
          "sub":"subject",
          "exp":"1924988399",
          "iat":"0",
          "nbf":"1"
        }
         */
        const val VALID_JWT = "ewogICJhbGciOiJFUzI1NiIsCiAgInR5cCI6InR5cGUiLAogICJraWQiOiJrZXlJZCIKfQ.ewogICJpc3MiOiJpc3N1ZXIiLAogICJzdWIiOiJzdWJqZWN0IiwKICAiZXhwIjoxOTI0OTg4Mzk5LAogICJpYXQiOjAsCiAgIm5iZiI6MQp9.ZXdvZ0lDSmhiR2NpT2lKRlV6STFOaUlzQ2lBZ0luUjVjQ0k2SW5SNWNHVWlMQW9nSUNKcmFXUWlPaUpyWlhsSlpDSUtmUS4udHR4THhjWnlja0FiQWJuT1NmUkxDa3BvUXFwblhpMlExYUI0M3ZOZWpRRTdMU2gwdXN6Q3lENV8wRUYteVRpTl9rN2o5dlJHYnAyVndLRDQxd1l0NHc"
        val VALID_JWT_PAYLOAD = """
          {
            "iss":"$ISS",
            "sub":"$SUB",
            "exp":$EXP,
            "iat":$IAT,
            "nbf":$NBF
          }
        """.trimIndent()
        const val JWT_MISSING_TYP = "ewogICJhbGciOiJFUzI1NiIsCiAgImtpZCI6ImtleUlkIgp9.ewogICJpc3MiOiJpc3N1ZXIiLAogICJzdWIiOiJzdWJqZWN0IiwKICAiZXhwIjoxOTI0OTg4Mzk5LAogICJpYXQiOjAsCiAgIm5iZiI6MQp9.ZXdvZ0lDSmhiR2NpT2lKRlV6STFOaUlzQ2lBZ0ltdHBaQ0k2SW10bGVVbGtJZ3A5Li4tV3p2TDMyeE0wWjl5Y3FLOXZMZnNiaFhQUnlhYVhGUFZHZEgzYXoxSGN0SGE1bEtId1NCVnEzdTR1UVR4MGUtdHFqQno3Tl9UN2ZFYUhRZEJmS2hXdw"
        const val JWT_MISSING_KID = "ewogICJhbGciOiJFUzI1NiIsCiAgInR5cCI6InR5cGUiCn0.ewogICJpc3MiOiJpc3N1ZXIiLAogICJzdWIiOiJzdWJqZWN0IiwKICAiZXhwIjoxOTI0OTg4Mzk5LAogICJpYXQiOjAsCiAgIm5iZiI6MQp9.ZXdvZ0lDSmhiR2NpT2lKRlV6STFOaUlzQ2lBZ0luUjVjQ0k2SW5SNWNHVWlDbjAuLm0yV1hHN1NhYjEweUVDLVo4VERIOGR3UE9ZVW1GLU9KRTNOMlQ0emRaUGtXcnBSRm5ZQWdLZzB5akF1ajNvMDRqNTA4MkpVTnVEQlo2NXhzaEVwY0dn"
        const val JWT_MISSING_ISS = "ewogICJhbGciOiJFUzI1NiIsCiAgInR5cCI6InR5cGUiLAogICJraWQiOiJrZXlJZCIKfQ.ewogICJzdWIiOiJzdWJqZWN0IiwKICAiZXhwIjoxOTI0OTg4Mzk5LAogICJpYXQiOjAsCiAgIm5iZiI6MQp9.ZXdvZ0lDSmhiR2NpT2lKRlV6STFOaUlzQ2lBZ0luUjVjQ0k2SW5SNWNHVWlMQW9nSUNKcmFXUWlPaUpyWlhsSlpDSUtmUS4uWlBhTlFlRXFyT0lZX20za2dicTNCWkNCNk1DSXNQaVdRMlowalNPRDlLU2FyN3lBYnR2MkhPQVlYM2tuei0zQndGdFQtZUNSNFZFLTdUbHBBWXptdFE"
        const val JWT_MISSING_SUB = "ewogICJhbGciOiJFUzI1NiIsCiAgInR5cCI6InR5cGUiLAogICJraWQiOiJrZXlJZCIKfQ.ewogICJpc3MiOiJpc3N1ZXIiLAogICJleHAiOjE5MjQ5ODgzOTksCiAgImlhdCI6MCwKICAibmJmIjoxCn0.ZXdvZ0lDSmhiR2NpT2lKRlV6STFOaUlzQ2lBZ0luUjVjQ0k2SW5SNWNHVWlMQW9nSUNKcmFXUWlPaUpyWlhsSlpDSUtmUS4uS0d6WlVlenh3M2NCR2g5V2ZLSV9sV0lDUGhURUpROXBPd3lGSzBZTmF6SmE2ZDdrcnFNQU1oc00zaEstVTBfYzVCYWFGUmJPNEZWZ0gtQkZfWU5JN0E"
        const val JWT_MISSING_IAT = "ewogICJhbGciOiJFUzI1NiIsCiAgInR5cCI6InR5cGUiLAogICJraWQiOiJrZXlJZCIKfQ.ewogICJpc3MiOiJpc3N1ZXIiLAogICJzdWIiOiJzdWJqZWN0IiwKICAiZXhwIjoxOTI0OTg4Mzk5LAogICJuYmYiOjEKfQ.ZXdvZ0lDSmhiR2NpT2lKRlV6STFOaUlzQ2lBZ0luUjVjQ0k2SW5SNWNHVWlMQW9nSUNKcmFXUWlPaUpyWlhsSlpDSUtmUS4uYk8wc0Nad2g0TDVwWlk5dXBfN3c2N2ZBb2xpQU1WbzZFdzhvWTBWVmNuV3JuV0E0QUhDVnptbUxSaXBWSERyN2Y5TXI3SVZfb29oRHRZLUkwWDUzUVE"
        const val JWT_MISSING_EXP = "ewogICJhbGciOiJFUzI1NiIsCiAgInR5cCI6InR5cGUiLAogICJraWQiOiJrZXlJZCIKfQ.ewogICJpc3MiOiJpc3N1ZXIiLAogICJzdWIiOiJzdWJqZWN0IiwKICAiaWF0IjowLAogICJuYmYiOjEKfQ.ZXdvZ0lDSmhiR2NpT2lKRlV6STFOaUlzQ2lBZ0luUjVjQ0k2SW5SNWNHVWlMQW9nSUNKcmFXUWlPaUpyWlhsSlpDSUtmUS4uQzBOT3pKUXY2QXNQUlA0cXM0aS1FM0NFS3pHLUZHc05NZjNzRXBmeWpadU9vN2JNT3U5ZUlJMEROOVZaSjJ0eHE4VG1LM1VmYmdWRlhWelExSU8weWc"
        const val JWT_MISSING_NBF = "ewogICJhbGciOiJFUzI1NiIsCiAgInR5cCI6InR5cGUiLAogICJraWQiOiJrZXlJZCIKfQ.ewogICJpc3MiOiJpc3N1ZXIiLAogICJzdWIiOiJzdWJqZWN0IiwKICAiZXhwIjoxOTI0OTg4Mzk5LAogICJpYXQiOjAKfQ.ZXdvZ0lDSmhiR2NpT2lKRlV6STFOaUlzQ2lBZ0luUjVjQ0k2SW5SNWNHVWlMQW9nSUNKcmFXUWlPaUpyWlhsSlpDSUtmUS4uZkdBSV9NbHpXSEdLNk1WQXJvaDhIcEYwb0g1VkN4Si1DUnNyeDh4ZGhSc3hXUlhZcmtOMjlHdll6MnBvTFNFTGhjcXJxTF9Tbml2bDZoUmtiRW5DeXc"

        // exp = 0
        const val EXPIRED_JWT = "ewogICJhbGciOiJFUzI1NiIsCiAgInR5cCI6InR5cGUiLAogICJraWQiOiJrZXlJZCIKfQ.ewogICJpc3MiOiJpc3N1ZXIiLAogICJzdWIiOiJzdWJqZWN0IiwKICAiZXhwIjowLAogICJpYXQiOjAsCiAgIm5iZiI6MQp9.ZXdvZ0lDSmhiR2NpT2lKRlV6STFOaUlzQ2lBZ0luUjVjQ0k2SW5SNWNHVWlMQW9nSUNKcmFXUWlPaUpyWlhsSlpDSUtmUS4uMHNqN2Fzb3h2MGFJTkltWlNSTmJJMTJiUVQwQ1RHQ2dWMUVRQXl3ZGNieUY0M25WSzFHOTZMWDlZY256V0piSUt0eXdfVG81U2dhWjhXdmRndG5yN0E"

        // nbf = 1924988399
        const val NOT_YET_VALID_JWT = "ewogICJhbGciOiJFUzI1NiIsCiAgInR5cCI6InR5cGUiLAogICJraWQiOiJrZXlJZCIKfQ.ewogICJpc3MiOiJpc3N1ZXIiLAogICJzdWIiOiJzdWJqZWN0IiwKICAiZXhwIjoxOTI0OTg4Mzk5LAogICJpYXQiOjAsCiAgIm5iZiI6MTkyNDk4ODM5OQp9.ZXdvZ0lDSmhiR2NpT2lKRlV6STFOaUlzQ2lBZ0luUjVjQ0k2SW5SNWNHVWlMQW9nSUNKcmFXUWlPaUpyWlhsSlpDSUtmUS4uS094M2VSdmt2NXdkN2JvSUdndHRacDNiMDNqV0NfejNFeDRiNlBhUFVEVnd6bFBCMzk4dkg4a0pFNUVTTHhUSkZoWkJrcUhmdDEyUnR4azJxb3NKR1E"
    }
}
