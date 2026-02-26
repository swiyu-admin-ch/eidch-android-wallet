package ch.admin.foitt.wallet.feature.eIdRequestVerification

import ch.admin.foitt.avwrapper.DocumentScanPackageResult
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.EIdRequestVerificationError
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation.GetEIdMrzValuesImpl
import ch.admin.foitt.wallet.platform.utils.SafeJson
import ch.admin.foitt.wallet.util.assertErrorType
import com.github.michaelbull.result.get
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetEIdMrzValuesImplTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    private val safeJson = SafeJson(json)
    private lateinit var useCase: GetEIdMrzValuesImpl
    val line1Id = DocumentScanPackageResult.MRZ_LINE1
    val line2Id = DocumentScanPackageResult.MRZ_LINE2
    val line3Id = DocumentScanPackageResult.MRZ_LINE3

    @BeforeEach
    fun setUp() {
        useCase = GetEIdMrzValuesImpl(safeJson)
    }

    @Test
    fun `should successfully parse MRZ lines and replace stars with brackets`() = runTest {
        val jsonInput = """
            {
                "b": "$line1Id",
                "c": "P*CAN*SMITH**JOHN*",
                "d": {
                    "b": "$line2Id",
                    "c": "123456789*0123456*",
                    "d": {
                        "b": "$line3Id",
                        "c": "800101*M*250101*",
                        "d": null
                    }
                }
            }
        """.trimIndent()

        val result = useCase(jsonInput)

        val expected = listOf(
            "P<CAN<SMITH<<JOHN<",
            "123456789<0123456<",
            "800101<M<250101<"
        )
        assertEquals(expected, result.get())
    }

    @Test
    fun `should return error when no MRZ keys are found in the JSON`() = runTest {
        val jsonInput = """
        {
            "b": "999",
            "c": "Some random data",
            "d": {
                "b": "888",
                "c": "More random data",
                "d": null
            }
        }
        """.trimIndent()

        val error = useCase(jsonInput).assertErrorType(EIdRequestVerificationError.Unexpected::class)
        assertEquals("No MRZ values found in JSON", error.cause?.message)
    }

    @Test
    fun `should skip empty values and continue parsing`() = runTest {
        val jsonInput = """
        {
            "b": "$line1Id",
            "c": "LINE*1*",
            "d": {
                "b": "$line2Id",
                "c": "", 
                "d": {
                    "b": "$line3Id",
                    "c": "LINE*3*",
                    "d": null
                }
            }
        }
        """.trimIndent()

        val result = useCase(jsonInput)

        val actualList = result.get()
        val expected = listOf(
            "LINE<1<",
            "LINE<3<"
        )
        assertEquals(expected, actualList)
    }
}
