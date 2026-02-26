package ch.admin.foitt.openid4vc.domain.model.sdjwt

import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.ComplexSdJwt
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.FlatDisclosures
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.FlatSdJwt
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.KeyBindingJwt
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.RecursiveSdJwt
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.SdJwtSeparator
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.StructuredSdJwt
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.UndisclosedJwt
import ch.admin.foitt.openid4vc.utils.createDigest
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.text.ParseException

class SdJwtTest {

    @Test
    fun `parsing a valid undisclosed JWT should return the JWT and the JSON with actual values`() = runTest {
        val sdJwt = SdJwt(UndisclosedJwt.JWT)

        assertEquals(UndisclosedJwt.JWT, sdJwt.signedJwt.parsedString)
        assertJsonEquals(UndisclosedJwt.JSON, sdJwt.sdJwtJson)
    }

    @Test
    fun `parsing a valid flat SD-JWT should return the JWT and the JSON with actual values`() = runTest {
        val sdJwt = SdJwt(FlatSdJwt.JWT + FlatDisclosures)

        assertEquals(FlatSdJwt.JWT, sdJwt.signedJwt.parsedString)
        assertJsonEquals(FlatSdJwt.JSON, sdJwt.sdJwtJson)
    }

    @Test
    fun `parsing a valid structured SD-JWT should return the JWT and JSON with actual values`() = runTest {
        val sdJwt = SdJwt(StructuredSdJwt.JWT + FlatDisclosures)

        assertEquals(StructuredSdJwt.JWT, sdJwt.signedJwt.parsedString)
        assertJsonEquals(StructuredSdJwt.JSON, sdJwt.sdJwtJson)
    }

    @Test
    fun `parsing a valid structured SD-JWT with KeyBindingJWT should return the JWT and the JSON with actual values`() =
        runTest {
            val sdJwt = SdJwt(StructuredSdJwt.JWT + FlatDisclosures + KeyBindingJwt)

            assertEquals(StructuredSdJwt.JWT, sdJwt.signedJwt.parsedString)
            assertJsonEquals(StructuredSdJwt.JSON, sdJwt.sdJwtJson)
        }

    @Test
    fun `parsing a valid recursive SD-JWT should return the JWT and the JSON with actual values`() = runTest {
        val sdJwt = SdJwt(RecursiveSdJwt.SD_JWT)

        assertEquals(RecursiveSdJwt.JWT, sdJwt.signedJwt.parsedString)
        assertJsonEquals(RecursiveSdJwt.JSON, sdJwt.sdJwtJson)
    }

    @Test
    fun `parsing a valid complex SD-JWT should return the JWT and the JSON with actual values`() = runTest {
        val sdJwt = SdJwt(ComplexSdJwt.SD_JWT)

        assertEquals(ComplexSdJwt.JWT, sdJwt.signedJwt.parsedString)
        assertJsonEquals(ComplexSdJwt.JSON, sdJwt.sdJwtJson)
    }

    @Test
    fun `parsing an SD-JWT with only two JWT parts should throw an exception`() = runTest {
        val invalidSdJwt = "test.test${SdJwtSeparator}disclosures$SdJwtSeparator"

        assertThrows<ParseException> {
            SdJwt(invalidSdJwt)
        }
    }

    @Test
    fun `parsing an SD-JWT with only one JWT part should throw an exception`() = runTest {
        val invalidSdJwt = "test${SdJwtSeparator}disclosures$SdJwtSeparator"

        assertThrows<ParseException> {
            SdJwt(invalidSdJwt)
        }
    }

    @Test
    fun `parsing an SD-JWT with a disclosure with two elements should throw an exception`() = runTest {
        // ["test_salt_1", "test_key_1"]
        val invalidSdJwt = FlatSdJwt.JWT + "${SdJwtSeparator}WyJ0ZXN0X3NhbHRfMSIsICJ0ZXN0X2tleV8xIl0$SdJwtSeparator"

        assertThrows<IllegalStateException> {
            SdJwt(invalidSdJwt)
        }
    }

    @Test
    fun `parsing an SD-JWT with a disclosure with as reserved claim name should throw an exception`() {
        assertThrows<IllegalStateException> {
            SdJwt(rawSdJwt = FlatSdJwt.JWT + FlatDisclosures, reservedClaimNames = setOf("test_key_2"))
        }
    }

    @Test
    fun `parsing an SD-JWT with disclosures with duplicate claim names should throw an exception`() {
        // Key: claim1
        // Value: value1
        val disclosure1 = "WyI5Mzc0NTNkMWVmODZmY2E4IiwiY2xhaW0xIiwidmFsdWUxIl0"

        // Key: claim1
        // Value: value2
        val disclosure2 = "WyIyZmEwZWQ0NzQ0YTM1ZjdiIiwiY2xhaW0xIiwidmFsdWUyIl0"

        // jwt referencing both disclosures' digests in _sd
        val jwt = "eyJ0eXAiOiJzZCtqd3QiLCJhbGciOiJFUzI1NiJ9.eyJfc2QiOlsickptc2w4cHZnLWlMeTR4dVVZYkNXUXVvZE14UVF6NV9LbTA5b2FrR0k4dyIsIjlLeTlieExOa292STcwcmU1R1djTXFuSHFUMzctN3VMUnA4NkdqRTVqbGciXSwiX3NkX2FsZyI6IlNIQS0yNTYifQ.7-4yC1vRDMWyzyIqX9Ro2_rtLZPwdKUkG_eQWwC69kuZH7xI8uc_7GACFXGcTvGOTziISb_PcDYyi6DwD_uQPg"

        assertThrows<IllegalStateException> {
            SdJwt(rawSdJwt = jwt + SdJwtSeparator + disclosure1 + SdJwtSeparator + disclosure2 + SdJwtSeparator)
        }
    }

    @Test
    fun `parsing an SD-JWT with a disclosure with one element should throw an exception`() = runTest {
        // ["test_salt_1"]
        val invalidSdJwt = FlatSdJwt.JWT + "${SdJwtSeparator}WyJ0ZXN0X3NhbHRfMSJd$SdJwtSeparator"

        assertThrows<IllegalStateException> {
            SdJwt(invalidSdJwt)
        }
    }

    @Test
    fun `parsing an SD-JWT with a disclosure with too many elements should  throw an exception`() = runTest {
        // ["test_salt_1", "test_key_1", "test_value_1", "test"]
        val invalidSdJwt = FlatSdJwt.JWT +
            "${SdJwtSeparator}WyJ0ZXN0X3NhbHRfMSIsICJ0ZXN0X2tleV8xIiwgInRlc3RfdmFsdWVfMSIsICJ0ZXN0Il0$SdJwtSeparator"

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
            "eyJhbGciOiJFUzUxMiIsInR5cCI6IkpXVCJ9.eyJ0ZXN0Ijp7Il9zZCI6eyJub3RfZ29vZCI6InRydWUifX0sIl9zZF9hbGciOiJzaGEtMjU2IiwiaWF0IjoxNjk3ODA5NzMxfQ.AaQdx2Pwj0jPE2Z8dCa9Jiam8tyzkOJb_5HCEZumuLRlh3nFtvmAxLGWBqYO54zotDOgGMH5WBdPuad5sJzdbWfHAbpFN6APM9FSNk3uk4C2qvb1osGeehE2REtJ1EjPOqFldgO36zqmMG8jSHm5YH9p1Xw4oYkeehXJpLL2qRsZPdZU"

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
            "eyJhbGciOiJFUzUxMiIsInR5cCI6IkpXVCJ9.eyJ0ZXN0Ijp7Il9zZCI6Im5vdCBnb29kIn0sIl9zZF9hbGciOiJzaGEtMjU2IiwiaWF0IjoxNjk3ODEwODkzfQ.AZmwJlPifMvTWxUJfTrbnq4-lqzKPsrqi2CDjuIaDwSIeyouTcCEO5SNHfYQwlFEiMq5qpM5qUo6opVGbTOsge2HAH81GBymZR4n5cKPvMmIVe6rQ-fcdV-rfbV4RfEuXSla_qZGl6NR8CX9slVc3YRBr_UK7rgl_bGh_EH2sJAP19-N"

        assertThrows<IllegalStateException> {
            SdJwt(jwt + FlatDisclosures)
        }
    }

    @Test
    fun `parsing a SD-JWT with duplicate digest should return an error`() = runTest {
        mockkStatic("ch.admin.foitt.openid4vc.utils.StringExtKt")

        every { any<String>().createDigest(any()) } returns "DUPLICATE_DIGEST"

        assertThrows<IllegalStateException> {
            SdJwt(RecursiveSdJwt.SD_JWT)
        }

        unmockkStatic("ch.admin.foitt.openid4vc.utils.StringExtKt")
    }

    private fun assertJsonEquals(expected: String, actualJson: JsonElement) {
        val expectedJson = Json.parseToJsonElement(expected)
        val filteredJson = actualJson.jsonObject.filterKeys { key ->
            key != ISSUED_AT_KEY && key != SD_ALGORITHM_KEY
        }
        assertEquals(expectedJson, filteredJson)
    }

    companion object {
        private const val ISSUED_AT_KEY = "iat"
        private const val SD_ALGORITHM_KEY = "_sd_alg"
    }
}
