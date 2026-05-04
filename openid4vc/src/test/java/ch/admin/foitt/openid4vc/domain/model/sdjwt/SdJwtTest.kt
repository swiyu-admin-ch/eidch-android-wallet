package ch.admin.foitt.openid4vc.domain.model.sdjwt

import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.ComplexRFCSdJwt
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.ComplexSdJwt
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.Disclosure1
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.DisclosureAlgorithmKey
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.DisclosureArrayKey
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.DisclosureSdKey
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.DuplicateNameSdJwt
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.FlatDisclosures
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.FlatObjectArraySdJwt
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.FlatSdJwt
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.FlatSdJwt.KEY_1
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.FlatSimpleArraySdJwt
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.KeyBindingJwt
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.RecursiveSdJwt
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.SdJwtSeparator
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.Sha1SdJwt
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.SimpleRFCSdJwt
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.StructuredDisclosures
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.StructuredSdJwt
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.TypedSdJwt
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.UndisclosedJwt
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.toDisclosures
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.text.ParseException

class SdJwtTest {

    @Test
    fun `parsing a valid undisclosed JWT should return the JWT and the JSON with actual values`() = runTest {
        val sdJwt = SdJwt(UndisclosedJwt.JWT + SdJwtSeparator)

        assertEquals(UndisclosedJwt.JWT, sdJwt.signedJwt.parsedString)
        assertJsonEquals(UndisclosedJwt.JSON, sdJwt.processedJson)
    }

    @Test
    fun `parsing a valid SD-JWT with different types should return the JWT and the JSON with actual values`() = runTest {
        val sdJwt = SdJwt(TypedSdJwt.SD_JWT)

        assertEquals(TypedSdJwt.JWT, sdJwt.signedJwt.parsedString)
        assertJsonEquals(TypedSdJwt.JSON, sdJwt.processedJson)
    }

    @Test
    fun `parsing a valid flat SD-JWT should return the JWT and the JSON with actual values`() = runTest {
        val sdJwt = SdJwt(FlatSdJwt.JWT + FlatDisclosures)

        assertEquals(FlatSdJwt.JWT, sdJwt.signedJwt.parsedString)
        assertJsonEquals(FlatSdJwt.JSON, sdJwt.processedJson)
    }

    @Test
    fun `parsing a valid flat SD-JWT with SHA-384 should return the JWT and the JSON with actual values`() = runTest {
        val sdJwt = SdJwt(FlatSdJwt.JWT384 + FlatDisclosures)

        assertEquals(FlatSdJwt.JWT384, sdJwt.signedJwt.parsedString)
        assertJsonEquals(FlatSdJwt.JSON, sdJwt.processedJson)
    }

    @Test
    fun `parsing a valid flat SD-JWT with SHA-512 should return the JWT and the JSON with actual values`() = runTest {
        val sdJwt = SdJwt(FlatSdJwt.JWT512 + FlatDisclosures)

        assertEquals(FlatSdJwt.JWT512, sdJwt.signedJwt.parsedString)
        assertJsonEquals(FlatSdJwt.JSON, sdJwt.processedJson)
    }

    @Test
    fun `parsing a valid flat SD-JWT with simple array and other claims should return the JWT and the JSON with actual values`() = runTest {
        val sdJwt = SdJwt(FlatSimpleArraySdJwt.SD_JWT_WITH_OTHER_CLAIMS)

        assertEquals(FlatSimpleArraySdJwt.JWT_WITH_OTHER_CLAIMS, sdJwt.signedJwt.parsedString)
        assertJsonEquals(FlatSimpleArraySdJwt.JSON_WITH_OTHER_CLAIMS, sdJwt.processedJson)
    }

    @Test
    fun `parsing a valid flat SD-JWT with only simple array should return the JWT and the JSON with actual values`() = runTest {
        val sdJwt = SdJwt(FlatSimpleArraySdJwt.SD_JWT_WITH_ARRAY_ONLY)

        assertEquals(FlatSimpleArraySdJwt.JWT_WITH_ARRAY_ONLY, sdJwt.signedJwt.parsedString)
        assertJsonEquals(FlatSimpleArraySdJwt.JSON_WITH_ARRAY_ONLY, sdJwt.processedJson)
    }

    @Test
    fun `parsing a valid flat SD-JWT with an object array and one disclosed element should return the JWT and the JSON with actual values`() = runTest {
        val sdJwt = SdJwt(FlatObjectArraySdJwt.SD_JWT_WITH_ONE_DISCLOSED_ELEMENT)

        assertEquals(FlatObjectArraySdJwt.JWT_WITH_ONE_DISCLOSED_ELEMENT, sdJwt.signedJwt.parsedString)
        assertJsonEquals(FlatObjectArraySdJwt.JSON, sdJwt.processedJson)
    }

    @Test
    fun `parsing a valid flat SD-JWT with an object array where all elements are disclosed should return the JWT and the JSON with actual values`() = runTest {
        val sdJwt = SdJwt(FlatObjectArraySdJwt.SD_JWT_WITH_DISCLOSED_ELEMENTS_ONLY)

        assertEquals(FlatObjectArraySdJwt.JWT_WITH_DISCLOSED_ELEMENTS_ONLY, sdJwt.signedJwt.parsedString)
        assertJsonEquals(FlatObjectArraySdJwt.JSON, sdJwt.processedJson)
    }

    @Test
    fun `parsing a valid structured SD-JWT should return the JWT and JSON with actual values`() = runTest {
        val sdJwt = SdJwt(StructuredSdJwt.JWT + StructuredDisclosures)

        assertEquals(StructuredSdJwt.JWT, sdJwt.signedJwt.parsedString)
        assertJsonEquals(StructuredSdJwt.JSON, sdJwt.processedJson)
    }

    @Test
    fun `parsing a valid structured SD-JWT with KeyBindingJWT should return the JWT and the JSON with actual values`() =
        runTest {
            val sdJwt = SdJwt(StructuredSdJwt.JWT + StructuredDisclosures + KeyBindingJwt)

            assertEquals(StructuredSdJwt.JWT, sdJwt.signedJwt.parsedString)
            assertJsonEquals(StructuredSdJwt.JSON, sdJwt.processedJson)
        }

    @Test
    fun `parsing a valid recursive SD-JWT should return the JWT and the JSON with actual values`() = runTest {
        val sdJwt = SdJwt(RecursiveSdJwt.SD_JWT)

        assertEquals(RecursiveSdJwt.JWT, sdJwt.signedJwt.parsedString)
        assertJsonEquals(RecursiveSdJwt.JSON, sdJwt.processedJson)
    }

    @Test
    fun `parsing a valid flat SD-JWT with decoys should return the JWT and the JSON with actual values`() = runTest {
        val sdJwt = SdJwt(FlatSdJwt.JWT + listOf(Disclosure1).toDisclosures())

        assertEquals(FlatSdJwt.JWT, sdJwt.signedJwt.parsedString)
        assertJsonEquals("""{"$KEY_1":"test_value_1"}""", sdJwt.processedJson)
    }

    @Test
    fun `parsing a valid flat SD-JWT with array decoys should return the JWT and the JSON with actual values`() = runTest {
        val sdJwt =
            SdJwt(FlatSimpleArraySdJwt.JWT_WITH_ARRAY_ONLY + listOf(FlatSimpleArraySdJwt.DISCLOSURE_ELEMENT_1).toDisclosures())

        assertEquals(FlatSimpleArraySdJwt.JWT_WITH_ARRAY_ONLY, sdJwt.signedJwt.parsedString)
        assertJsonEquals("""{"array_key":["test_array_value_1"]}""", sdJwt.processedJson)
    }

    @Test
    fun `parsing a valid complex SD-JWT should return the JWT and the JSON with actual values`() = runTest {
        val sdJwt = SdJwt(ComplexSdJwt.SD_JWT)

        assertEquals(ComplexSdJwt.JWT, sdJwt.signedJwt.parsedString)
        assertJsonEquals(ComplexSdJwt.JSON, sdJwt.processedJson)
    }

    @Test
    fun `parsing a valid simple RFC SD-JWT should return the JWT and the JSON with actual values`() = runTest {
        val sdJwt = SdJwt(SimpleRFCSdJwt.SD_JWT)

        assertEquals(SimpleRFCSdJwt.JWT, sdJwt.signedJwt.parsedString)
        assertJsonEquals(SimpleRFCSdJwt.JSON, sdJwt.processedJson)
    }

    @Test
    fun `parsing a valid complex RFC SD-JWT should return the JWT and the JSON with actual values`() = runTest {
        val sdJwt = SdJwt(ComplexRFCSdJwt.SD_JWT)

        assertEquals(ComplexRFCSdJwt.JWT, sdJwt.signedJwt.parsedString)
        assertJsonEquals(ComplexRFCSdJwt.JSON, sdJwt.processedJson)
    }

    @Test
    fun `parsing an SD-JWT with duplicate claim name on different levels should return the JWT and the JSON with actual values`() = runTest {
        val sdJwt = SdJwt(DuplicateNameSdJwt.SD_JWT)

        assertEquals(DuplicateNameSdJwt.JWT, sdJwt.signedJwt.parsedString)
        assertJsonEquals(DuplicateNameSdJwt.JSON, sdJwt.processedJson)
    }

    @Test
    fun `parsing an SD-JWT with empty _sd array should return the JWT and the JSON with actual values`() = runTest {
        // {"_sd":[]}
        val jwt =
            "eyJ0eXAiOiJmbGF0IiwiYWxnIjoiRVM1MTIifQ.eyJfc2QiOltdfQ.AQIrJ8faKv6VmkoO6ciV-aIXfK5eQ6ofagRVs5Qt3PMHBcnkk7J0s5hdDli8vQkCrs6FMj2YaVAVNZ3oaJ7LK06vAFW8TFKE9rEPWKF9bhAl7SFEvB930aVdqPEW1Ifrk3-OFeFlYa7APhPVGGIYtdlXAQ6AYcyIZkt4-xslrWLfgF5c~"
        val sdJwt = SdJwt(jwt)

        assertJsonEquals("{}", sdJwt.processedJson)
    }

    @Test
    fun `parsing an SD-JWT with only two JWT parts should throw an exception`() = runTest {
        val invalidSdJwt = "test.test" + listOf("disclosures").toDisclosures()

        assertThrows<ParseException> {
            SdJwt(invalidSdJwt)
        }
    }

    @Test
    fun `parsing an SD-JWT with only one JWT part should throw an exception`() = runTest {
        val invalidSdJwt = "test" + listOf("disclosures").toDisclosures()

        assertThrows<ParseException> {
            SdJwt(invalidSdJwt)
        }
    }

    @Test
    fun `parsing an SD-JWT with a disclosure with an non-selectively disclosable claim name should throw an exception`() {
        assertThrows<IllegalStateException> {
            SdJwt(rawSdJwt = FlatSdJwt.JWT + FlatDisclosures, nonSelectivelyDisclosableClaims = setOf("test_key_2"))
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [DisclosureSdKey, DisclosureArrayKey, DisclosureAlgorithmKey]
    )
    fun `parsing an SD-JWT with a disclosure with a reserved disclosure name should throw an exception`(disclosure: String) {
        assertThrows<IllegalStateException> {
            SdJwt(rawSdJwt = FlatSdJwt.JWT + listOf(disclosure).toDisclosures())
        }
    }

    @Test
    fun `parsing an SD-JWT with disclosures with duplicate claim names should throw an exception`() {
        // ["test_salt_1", "key", "value1"]
        val disclosure1 = "WyJ0ZXN0X3NhbHRfMSIsICJrZXkiLCAidmFsdWUxIl0"

        // ["test_salt_1", "key", "value2"]
        val disclosure2 = "WyJ0ZXN0X3NhbHRfMSIsICJrZXkiLCAidmFsdWUyIl0"

        // jwt referencing both disclosures' digests in _sd
        val jwt =
            "eyJ0eXAiOiJzZCtqd3QiLCJhbGciOiJFUzI1NiJ9.eyJfc2QiOlsickptc2w4cHZnLWlMeTR4dVVZYkNXUXVvZE14UVF6NV9LbTA5b2FrR0k4dyIsIjlLeTlieExOa292STcwcmU1R1djTXFuSHFUMzctN3VMUnA4NkdqRTVqbGciXSwiX3NkX2FsZyI6IlNIQS0yNTYifQ.7-4yC1vRDMWyzyIqX9Ro2_rtLZPwdKUkG_eQWwC69kuZH7xI8uc_7GACFXGcTvGOTziISb_PcDYyi6DwD_uQPg"

        assertThrows<IllegalStateException> {
            SdJwt(rawSdJwt = jwt + listOf(disclosure1, disclosure2).toDisclosures())
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "InRlc3Rfc2FsdF8xIg", // "test_salt_1"
            "bnVsbA", // null
            "WyJ0ZXN0X3NhbHRfMSJd", // ["test_salt_1"]
            "WyJ0ZXN0X3NhbHRfMSIsICJ0ZXN0X2tleV8xIiwgInRlc3RfdmFsdWVfMSIsICJ0ZXN0Il0", // ["test_salt_1", "test_key_1", "test_value_1", "test"]
            "eyJrZXkiOiAidmFsdWUifQ", // {"key": "value"}
        ]
    )
    fun `parsing an SD-JWT with an invalid disclosure should throw an exception`(disclosure: String) = runTest {
        val invalidSdJwt = FlatSdJwt.JWT + listOf(disclosure).toDisclosures()

        assertThrows<IllegalStateException> {
            SdJwt(invalidSdJwt)
        }
    }

    @Test
    fun `parsing an SD-JWT with a disclosure that has no matching digest should throw an exception`() = runTest {
        // ["test_salt_4", "test_key_4", "test_value_4"]
        val invalidSdJwt = FlatSdJwt.JWT + listOf("WyJ0ZXN0X3NhbHRfNCIsICJ0ZXN0X2tleV80IiwgInRlc3RfdmFsdWVfNCJd").toDisclosures()

        assertThrows<IllegalStateException> {
            SdJwt(invalidSdJwt)
        }
    }

    @Test
    fun `parsing a random string should throw an exception`() = runTest {
        val invalidSdJwt = "foobar"

        assertThrows<ParseException> {
            SdJwt(invalidSdJwt)
        }
    }

    @Test
    fun `parsing an empty string should throw an exception`() = runTest {
        val invalidSdJwt = ""

        assertThrows<ParseException> {
            SdJwt(invalidSdJwt)
        }
    }

    @Test
    fun `parsing an SD-JWT where an _sd key references a json object should throw an exception`() = runTest {
        /*
        {
           "test":{
              "_sd":{
                 "not_good":"true"
              }
           },
           "_sd_alg":"sha-256"
        }
         */
        val jwt =
            "eyJ0eXAiOiJmbGF0IiwiYWxnIjoiRVM1MTIifQ.eyJ0ZXN0Ijp7Il9zZCI6eyJub3RfZ29vZCI6InRydWUifX0sIl9zZF9hbGciOiJzaGEtMjU2In0.AYiN4WOC_zYKkPxYEZc5Ej7yY_s4GTcAyw2RlhHYZHVZxxYPT5ENiNKrBHfGbXJZnULqr47eYVJDGzZXBNBN3878AclIsqjqsMB5NKLPWSW0KG7lZp6sGAgWCIhVYlNVFmHy-wqOiVRJ_huP9WriXkbfZUT8-0YsHqMC0tBRVJzcaWiw"

        assertThrows<IllegalStateException> {
            SdJwt(jwt + FlatDisclosures)
        }
    }

    @Test
    fun `parsing an SD-JWT where an _sd key references a json primitive should throw an exception`() = runTest {
        /*
        {
           "test":{
              "_sd":"not good"
           },
           "_sd_alg":"sha-256"
        }
         */
        val jwt =
            "eyJ0eXAiOiJmbGF0IiwiYWxnIjoiRVM1MTIifQ.eyJ0ZXN0Ijp7Il9zZCI6Im5vdCBnb29kIn0sIl9zZF9hbGciOiJzaGEtMjU2In0.AE7u0qbm15sXWrdS56yi0HwUxVGtx49dG59Q3TH6KnPC_5O2dIt145fNQYmOHIMp0zjjkYyNhQ339HC_Jytbug_UAHccAtD58044Ph1ZAdK83u-I4Ls40eRKi_EsF95K43Qt3hSQc7kA1uzIFRLm6Ru0vc0LCv_HSn2i-eF-S8mlN6L2"

        assertThrows<IllegalStateException> {
            SdJwt(jwt + FlatDisclosures)
        }
    }

    @Test
    fun `parsing an SD-JWT with a _sd_alg not in top level in payload should throw an exception`() {
        /*
        {
           "test":{
              "_sd_alg":"sha-256"
           }
        }
         */
        val jwt =
            "eyJ0eXAiOiJmbGF0IiwiYWxnIjoiRVM1MTIifQ.eyJ0ZXN0Ijp7Il9zZF9hbGciOiJzaGEtMjU2In19.APokLlipcA2fv3ozhHQaO_EHy6IMeCEBAfCTcPWSQXhb9hNKWMXTuK6Xop_QhJJ_DD-wIipPb4EifAvCTlN1TnxaAWecNbiHlfwKhh0maasJP6IA0hCKJFOERYXOiHjb8RTG8ndgjruHs8oy68xxvaBsU-PGRAXmO0snXaNkkkYlHlgv"

        assertThrows<IllegalStateException> {
            SdJwt(jwt + FlatDisclosures)
        }
    }

    @Test
    fun `parsing an SD-JWT with a array element key in payload should throw an exception`() {
        // {"...":"test"}
        val jwt =
            "eyJ0eXAiOiJmbGF0IiwiYWxnIjoiRVM1MTIifQ.eyIuLi4iOiJ0ZXN0In0.AQACF7MRBUBvtw9lqjD3LHMjm17box9HfwOZ767MNLxLpQcS50q7RP_hYb1RkY6De0c6MKuiT57CLloxBlqNoMXNAAphVsYgJCcVFdIdy0KweNfbW3kT3o3uCSrfRhTkk9YkQQy4akQPJ1qgdICZsnj6HQGz_JBK3cLAIzckYFpP6Yw3"

        assertThrows<IllegalStateException> {
            SdJwt(jwt + FlatDisclosures)
        }
    }

    @Test
    fun `parsing a SD-JWT with duplicate digest in same array should throw an exception`() = runTest {
        /*
        {
           "_sd":[
              "YRLf606clwt4-hjyGze49ySFi6VCmwb9n5hwb4VUJSY",
              "YRLf606clwt4-hjyGze49ySFi6VCmwb9n5hwb4VUJSY"
           ],
           "_sd_alg":"sha-256"
        }
         */
        val jwt =
            "eyJ0eXAiOiJmbGF0IiwiYWxnIjoiRVM1MTIifQ.eyJfc2QiOlsiWVJMZjYwNmNsd3Q0LWhqeUd6ZTQ5eVNGaTZWQ213YjluNWh3YjRWVUpTWSIsIllSTGY2MDZjbHd0NC1oanlHemU0OXlTRmk2VkNtd2I5bjVod2I0VlVKU1kiXSwiX3NkX2FsZyI6InNoYS0yNTYifQ.AOhYb6lnqer2VomCqPu1s15kQTWbbioPuNEqf_7j8aF4tAhlVZ1dGgUBVZIkB5oE5gmF9ixvLBqBVoJBn2rO79NJAPP8owvNhB0R7XxkEynosrLcYE9CqxG9pCVLD5brwn7Tzk6UQ5QuFkTheRVa2Q75WSBFPWzcZlFSESGdHG9RV0hM"

        assertThrows<IllegalStateException> {
            SdJwt(jwt + listOf(Disclosure1).toDisclosures())
        }
    }

    @Test
    fun `parsing a SD-JWT with duplicate digest in different arrays should throw an exception`() = runTest {
        /*
        {
        "_sd":[
           "YRLf606clwt4-hjyGze49ySFi6VCmwb9n5hwb4VUJSY"
        ],
        "array":[
           {
              "...":"YRLf606clwt4-hjyGze49ySFi6VCmwb9n5hwb4VUJSY"
           }
        ],
        "_sd_alg":"sha-256"
     }
         */
        val jwt =
            "eyJ0eXAiOiJmbGF0IiwiYWxnIjoiRVM1MTIifQ.eyJfc2QiOlsiWVJMZjYwNmNsd3Q0LWhqeUd6ZTQ5eVNGaTZWQ213YjluNWh3YjRWVUpTWSJdLCJhcnJheSI6W3siLi4uIjoiWVJMZjYwNmNsd3Q0LWhqeUd6ZTQ5eVNGaTZWQ213YjluNWh3YjRWVUpTWSJ9XSwiX3NkX2FsZyI6InNoYS0yNTYifQ.AetmUJniNzDBHh4Kn_UwP8ovsCdcjAYha2DoIEelSHhsWpoc9Y6ZTntWFQHCD_5gpVUMHu_tgOWMfM_BD31f6viqAdGULrwFmd2hTJJixXPZo6BOQohb4wKxay0OysklTr1ffoLWQoKiKFuucP7uTrP0UaSILCXzsQV3SBS3y2t_dc0M"

        assertThrows<IllegalStateException> {
            SdJwt(jwt + listOf(Disclosure1).toDisclosures())
        }
    }

    @Test
    fun `parsing a valid SD-JWT with an unsupported algorithm should throw an exception`() = runTest {
        val throwable = assertThrows<IllegalStateException> {
            SdJwt(rawSdJwt = Sha1SdJwt.SD_JWT)
        }
        assert(throwable.message?.contains("sha-1") == true)
    }

    private fun assertJsonEquals(expected: String, actualJson: JsonElement) {
        val expectedJson = Json.parseToJsonElement(expected)
        val filteredJson = actualJson.jsonObject.filterKeys { key ->
            key != ISSUED_AT_KEY
        }
        val jsonObject = JsonObject(filteredJson)
        assertEquals(expectedJson, jsonObject)
    }

    companion object {
        private const val ISSUED_AT_KEY = "iat"
    }
}
