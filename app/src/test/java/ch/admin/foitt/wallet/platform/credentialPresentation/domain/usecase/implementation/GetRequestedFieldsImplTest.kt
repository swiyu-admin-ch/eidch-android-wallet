package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.Field
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.Filter
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.Filter.Companion.TYPE_NUMBER
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.Filter.Companion.TYPE_STRING
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptor
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestField
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import ch.admin.foitt.wallet.util.create
import ch.admin.foitt.wallet.util.createFieldPerPath
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetRequestedFieldsImplTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var useCase: GetRequestedFieldsImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = GetRequestedFieldsImpl(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Getting requested fields where one path matches returns the matching field`() = runTest(testDispatcher) {
        val descriptors = listOf(oneMatchingPathInputDescriptor)

        val result = useCase(CREDENTIAL_JSON, descriptors).assertOk()

        assertEquals(1, result.size)
        assertTrue(PresentationRequestField(path1, VALUE_1) in result)
    }

    @Test
    fun `Getting requested fields where two path match returns the matching fields`() = runTest(testDispatcher) {
        val inputDescriptors = listOf(twoMatchingPathsInputDescriptor)

        val result = useCase(CREDENTIAL_JSON, inputDescriptors).assertOk()

        assertEquals(2, result.size)
        assertTrue(PresentationRequestField(path1, VALUE_1) in result)
        assertTrue(PresentationRequestField(path2, VALUE_2) in result)
    }

    @Test
    fun `Getting requested fields where a string field matches returns the matching field`() = runTest(testDispatcher) {
        val inputDescriptors = listOf(oneMatchingStringFieldInputDescriptor)

        val result = useCase(CREDENTIAL_JSON, inputDescriptors).assertOk()

        assertEquals(1, result.size)
        assertTrue(PresentationRequestField(path1, VALUE_1) in result)
    }

    @Test
    fun `Getting requested fields where one string field and one path match returns the matching fields`() =
        runTest(testDispatcher) {
            val inputDescriptors = listOf(oneStringFieldAndOnePathMatchingInputDescriptor)

            val result = useCase(CREDENTIAL_JSON, inputDescriptors).assertOk()

            assertEquals(2, result.size)
            assertTrue(PresentationRequestField(path1, VALUE_1) in result)
            assertTrue(PresentationRequestField(path2, VALUE_2) in result)
        }

    @Test
    fun `Getting requested fields where two string fields match returns the matching fields`() = runTest(testDispatcher) {
        val inputDescriptors = listOf(twoMatchingStringFieldsInputDescriptor)

        val result = useCase(CREDENTIAL_JSON, inputDescriptors).assertOk()

        assertEquals(2, result.size)
        assertTrue(PresentationRequestField(path1, VALUE_1) in result)
        assertTrue(PresentationRequestField(path2, VALUE_2) in result)
    }

    @Test
    fun `Getting requested fields where one number field matches returns the matching field`() = runTest(testDispatcher) {
        val inputDescriptors = listOf(oneMatchingNumberFieldInputDescriptor)

        val result = useCase(CREDENTIAL_JSON, inputDescriptors).assertOk()

        assertEquals(1, result.size)
        assertEquals(pathNumber, result[0].path)
        assertEquals(NUMBER.toString(), result[0].value)
    }

    @Test
    fun `Getting requested fields for two input descriptors returns matching fields for both descriptors`() = runTest(testDispatcher) {
        val inputDescriptors = listOf(oneMatchingNumberFieldInputDescriptor, twoMatchingStringFieldsInputDescriptor)

        val result = useCase(CREDENTIAL_JSON, inputDescriptors).assertOk()

        assertEquals(3, result.size)
        assertTrue(PresentationRequestField(path1, VALUE_1) in result)
        assertTrue(PresentationRequestField(path2, VALUE_2) in result)
        assertTrue(PresentationRequestField(pathNumber, NUMBER.toString()) in result)
    }

    @Test
    fun `Getting requested fields where no fields are requested returns empty list`() = runTest(testDispatcher) {
        val inputDescriptors = listOf(InputDescriptor.create(emptyList()))

        val result = useCase(CREDENTIAL_JSON, inputDescriptors).assertOk()

        assertEquals(0, result.size)
    }

    @Test
    fun `Getting requested fields where none match a path returns empty list`() = runTest(testDispatcher) {
        val inputDescriptors = listOf(notMatchingPathInputDescriptor)

        val result = useCase(CREDENTIAL_JSON, inputDescriptors).assertOk()

        assertEquals(0, result.size)
    }

    @Test
    fun `Getting requested fields where none match a field returns empty list`(): Unit = runTest(testDispatcher) {
        val inputDescriptors = listOf(notMatchingStringFieldInputDescriptor)

        val result = useCase(CREDENTIAL_JSON, inputDescriptors).assertOk()

        assertEquals(0, result.size)
    }

    @Test
    fun `A valid filter get the proper field`(): Unit = runTest(testDispatcher) {
        val inputDescriptors = listOf(oneValidFilterInputDescriptor)

        val result = useCase(CREDENTIAL_JSON, inputDescriptors).assertOk()

        assertEquals(1, result.size)
        assertTrue(PresentationRequestField(pathVct, VALUE_VCT) in result)
    }

    @Test
    fun `A valid filter filter-out individual fields`(): Unit = runTest(testDispatcher) {
        val inputDescriptors = listOf(InputDescriptor.create(listOf(stringField1, nonMatchingVctFilter)))

        val result = useCase(CREDENTIAL_JSON, inputDescriptors).assertOk()

        assertEquals(1, result.size)
        assertTrue(PresentationRequestField(path1, VALUE_1) in result)
    }

    @Test
    fun `Filter is ignored if path is not $vct`(): Unit = runTest(testDispatcher) {
        val result = useCase(CREDENTIAL_JSON, listOf(invalidPathFilterInputDescriptor)).assertOk()

        assertTrue(PresentationRequestField(path1, VALUE_1) in result)
    }

    @Test
    fun `Filter is ignored if type is not string`(): Unit = runTest(testDispatcher) {
        val result = useCase(CREDENTIAL_JSON, listOf(invalidTypeFilterInputDescriptor)).assertOk()

        assertTrue(PresentationRequestField(pathVct, VALUE_VCT) in result)
    }

    @Test
    fun `Getting requested fields maps errors from json path parsing`(): Unit = runTest(testDispatcher) {
        val exception = IllegalStateException()
        val inputDescriptors = listOf(oneMatchingPathInputDescriptor)
        mockkStatic(JsonPath::class)
        every { JsonPath.using(any<Configuration>()) } throws exception

        val result = useCase(CREDENTIAL_JSON, inputDescriptors)

        val error = result.assertErrorType(CredentialPresentationError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    private companion object {
        const val KEY_1 = "key1"
        const val KEY_2 = "key2"
        const val KEY_NUMBER = "keyNumber"
        const val KEY_VCT = "vct"
        const val VALUE_1 = "value1"
        const val VALUE_2 = "value2"
        const val NUMBER = 1
        const val VALUE_VCT = "vctype1"
        const val CREDENTIAL_JSON = """{"$KEY_2": "$VALUE_2", "$KEY_1": "$VALUE_1", "$KEY_NUMBER": $NUMBER, "$KEY_VCT": "$VALUE_VCT"}"""
        const val JSON_PATH_1 = "$.$KEY_1"
        const val JSON_PATH_2 = "$.$KEY_2"
        const val JSON_PATH_NUMBER = "$.$KEY_NUMBER"
        const val JSON_PATH_VCT = "$.$KEY_VCT"
        val path1 = listOf(ClaimsPathPointerComponent.String(KEY_1))
        val path2 = listOf(ClaimsPathPointerComponent.String(KEY_2))
        val pathNumber = listOf(ClaimsPathPointerComponent.String(KEY_NUMBER))
        val pathVct = listOf(ClaimsPathPointerComponent.String(KEY_VCT))

        val oneMatchingPathInputDescriptor = InputDescriptor.create(Field(path = listOf("$.test", JSON_PATH_1)))
        val twoMatchingPathsInputDescriptor = InputDescriptor.createFieldPerPath(listOf(JSON_PATH_2, JSON_PATH_1))
        val notMatchingPathInputDescriptor = InputDescriptor.create(Field(path = listOf("$.test")))

        val oneValidFilterInputDescriptor = InputDescriptor.create(validFilterField)
        val invalidTypeFilterInputDescriptor = InputDescriptor.create(invalidFilterTypeField)
        val invalidPathFilterInputDescriptor = InputDescriptor.create(invalidFilterPathField)

        val validFilterField
            get() = Field(
                path = listOf(JSON_PATH_VCT),
                filter = Filter(
                    type = TYPE_STRING,
                    const = VALUE_VCT,
                )
            )

        val nonMatchingVctFilter
            get() = Field(
                path = listOf(JSON_PATH_VCT),
                filter = Filter(
                    type = TYPE_STRING,
                    const = "vctype2"
                )
            )

        val invalidFilterTypeField
            get() = Field(
                path = listOf(JSON_PATH_VCT),
                filter = Filter(
                    type = TYPE_NUMBER,
                    const = "1",
                )
            )

        val invalidFilterPathField
            get() = Field(
                path = listOf(JSON_PATH_1),
                filter = Filter(
                    type = TYPE_STRING,
                    const = "a",
                )
            )

        val stringField1 = Field(
            path = listOf(JSON_PATH_1),
        )
        val stringField2 = Field(
            path = listOf(JSON_PATH_2),
        )
        val numberField = Field(
            path = listOf(JSON_PATH_NUMBER),
        )

        val oneMatchingStringFieldInputDescriptor = InputDescriptor.create(stringField1)
        val twoMatchingStringFieldsInputDescriptor = InputDescriptor.create(listOf(stringField1, stringField2))
        val oneStringFieldAndOnePathMatchingInputDescriptor = InputDescriptor.create(
            listOf(
                stringField1,
                Field(path = listOf(JSON_PATH_2))
            )
        )
        val oneMatchingNumberFieldInputDescriptor = InputDescriptor.create(numberField)
        val notMatchingStringFieldInputDescriptor = InputDescriptor.create(nonMatchingVctFilter)
    }
}
