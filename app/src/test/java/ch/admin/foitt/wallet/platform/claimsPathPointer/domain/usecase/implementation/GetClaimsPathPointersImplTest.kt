package ch.admin.foitt.wallet.platform.claimsPathPointer.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent
import ch.admin.foitt.wallet.platform.claimsPathPointer.domain.usecase.GetClaimsPathPointers
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetClaimsPathPointersImplTest {

    private val safeJson = SafeJsonTestInstance.safeJson

    private lateinit var useCase: GetClaimsPathPointers

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = GetClaimsPathPointersImpl()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Claims path pointer is correctly constructed for empty object`() = runTest {
        val result = useCase(JsonObject(emptyMap()))

        assertEquals(0, result.size)
    }

    @Test
    fun `Claims path pointer is correctly constructed for single string element`() = runTest {
        val key = "key"
        val value = JsonPrimitive("value")

        val result = useCase(JsonObject(mapOf(key to value)))

        assertEquals(1, result.size)
        assertEquals(value, result[buildClaimsPathPointer(listOf("key"))])
    }

    @Test
    fun `Claims path pointer is correctly constructed for array elements`() = runTest {
        val value1 = JsonPrimitive(1)
        val value2 = JsonPrimitive(2)
        val array = JsonArray(listOf(value1, value2))

        val result = useCase(array)

        assertEquals(3, result.size)
        assertEquals(array, result[buildClaimsPathPointer(listOf(null))])
        assertEquals(value1, result[buildClaimsPathPointer(listOf(0))])
        assertEquals(value2, result[buildClaimsPathPointer(listOf(1))])
    }

    @Test
    fun `Claims path pointer is correctly constructed for nested string`() = runTest {
        val key = "key"
        val value = JsonPrimitive("value")
        val nestedKey = "nested"
        val nestedObject = JsonObject(mapOf(key to value))

        val result = useCase(JsonObject(mapOf(nestedKey to nestedObject)))

        assertEquals(2, result.size)
        assertEquals(nestedObject, result[buildClaimsPathPointer(listOf(nestedKey))])
        assertEquals(value, result[buildClaimsPathPointer(listOf(nestedKey, key))])
    }

    @Test
    fun `Claims path pointer is correctly constructed for array with nested string`() = runTest {
        val key = "key"
        val value = JsonPrimitive("value")
        val nestedObject = JsonObject(mapOf(key to value))
        val array = JsonArray(listOf(nestedObject))

        val result = useCase(array)

        assertEquals(3, result.size)
        assertEquals(array, result[buildClaimsPathPointer(listOf(null))])
        assertEquals(nestedObject, result[buildClaimsPathPointer(listOf(0))])
        assertEquals(value, result[buildClaimsPathPointer(listOf(0, key))])
    }

    @Test
    fun `Claims path pointer is correctly constructed for nested array`() = runTest {
        val value = JsonPrimitive(1)
        val array = JsonArray(listOf(value))
        val key = "key"
        val nestedObject = JsonObject(mapOf(key to array))

        val result = useCase(nestedObject)

        assertEquals(2, result.size)
        assertEquals(array, result[buildClaimsPathPointer(listOf(key, null))])
        assertEquals(value, result[buildClaimsPathPointer(listOf(key, 0))])
    }

    @Test
    fun `Claims path pointer is correctly constructed for complex structure`() = runTest {
        val result = useCase(claims)

        val expectedResult = mapOf(
            nameClaimsPathPointer to nameElement,
            addressClaimsPathPointer to addressElement,
            streetClaimsPathPointer to streetAddressElement,
            localityClaimsPathPointer to localityElement,
            postalCodeClaimsPathPointer to postalCodeElement,
            degreesClaimsPathPointer to degreesElement,
            degree0ClaimsPathPointer to degree0Element,
            type0ClaimsPathPointer to type0Element,
            university0ClaimsPathPointer to university0Element,
            degree1ClaimsPathPointer to degree1Element,
            type1ClaimsPathPointer to type1Element,
            university1ClaimsPathPointer to university1Element,
            nationalityClaimsPathPointer to nationalityElement,
            nationality0ClaimsPathPointer to nationality0Element,
            nationality1ClaimsPathPointer to nationality1Element,
        )

        assertEquals(expectedResult.size, result.size)
        assertEquals(expectedResult, result)
    }

    private fun buildClaimsPathPointer(list: List<Any?>) =
        list.map {
            when (it) {
                is String -> ClaimsPathPointerComponent.String(it)
                is Int -> ClaimsPathPointerComponent.Index(it)
                null -> ClaimsPathPointerComponent.Null
                else -> error("unhandled type")
            }
        }

    private val claimsJson = """
        {
          "name": "Arthur Dent",
          "address": {
            "street_address": "42 Market Street",
            "locality": "Milliways",
            "postal_code": "12345"
          },
          "degrees": [
            {
              "type": "Bachelor of Science",
              "university": "University of Betelgeuse"
            },
            {
              "type": "Master of Science",
              "university": "University of Betelgeuse"
            }
          ],
          "nationalities": ["British", "Betelgeusian"]
        }
    """.trimIndent()

    @OptIn(UnsafeResultValueAccess::class)
    private val claims = safeJson.safeParseToJsonElement(claimsJson).value.jsonObject
    private val nameElement = claims["name"]
    private val addressElement = claims["address"]
    private val streetAddressElement = addressElement?.jsonObject["street_address"]
    private val localityElement = addressElement?.jsonObject["locality"]
    private val postalCodeElement = addressElement?.jsonObject["postal_code"]
    private val degreesElement = claims["degrees"]
    private val degree0Element = degreesElement?.jsonArray[0]
    private val type0Element = degree0Element?.jsonObject["type"]
    private val university0Element = degree0Element?.jsonObject["university"]
    private val degree1Element = degreesElement?.jsonArray[1]
    private val type1Element = degree1Element?.jsonObject["type"]
    private val university1Element = degree1Element?.jsonObject["university"]
    private val nationalityElement = claims["nationalities"]
    private val nationality0Element = nationalityElement?.jsonArray[0]?.jsonPrimitive
    private val nationality1Element = nationalityElement?.jsonArray[1]?.jsonPrimitive
    private val nameClaimsPathPointer = buildClaimsPathPointer(listOf("name"))
    private val addressClaimsPathPointer = buildClaimsPathPointer(listOf("address"))
    private val streetClaimsPathPointer = buildClaimsPathPointer(listOf("address", "street_address"))
    private val localityClaimsPathPointer = buildClaimsPathPointer(listOf("address", "locality"))
    private val postalCodeClaimsPathPointer = buildClaimsPathPointer(listOf("address", "postal_code"))
    private val degreesClaimsPathPointer = buildClaimsPathPointer(listOf("degrees", null))
    private val degree0ClaimsPathPointer = buildClaimsPathPointer(listOf("degrees", 0))
    private val type0ClaimsPathPointer = buildClaimsPathPointer(listOf("degrees", 0, "type"))
    private val university0ClaimsPathPointer = buildClaimsPathPointer(listOf("degrees", 0, "university"))
    private val degree1ClaimsPathPointer = buildClaimsPathPointer(listOf("degrees", 1))
    private val type1ClaimsPathPointer = buildClaimsPathPointer(listOf("degrees", 1, "type"))
    private val university1ClaimsPathPointer = buildClaimsPathPointer(listOf("degrees", 1, "university"))
    private val nationalityClaimsPathPointer = buildClaimsPathPointer(listOf("nationalities", null))
    private val nationality0ClaimsPathPointer = buildClaimsPathPointer(listOf("nationalities", 0))
    private val nationality1ClaimsPathPointer = buildClaimsPathPointer(listOf("nationalities", 1))
}
