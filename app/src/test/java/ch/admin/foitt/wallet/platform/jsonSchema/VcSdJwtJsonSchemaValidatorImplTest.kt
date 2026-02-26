package ch.admin.foitt.wallet.platform.jsonSchema

import ch.admin.foitt.wallet.platform.jsonSchema.domain.model.JsonSchemaError
import ch.admin.foitt.wallet.platform.jsonSchema.domain.usecase.JsonSchemaValidator
import ch.admin.foitt.wallet.platform.jsonSchema.domain.usecase.implementation.VcSdJwtJsonSchemaValidatorImpl
import ch.admin.foitt.wallet.platform.jsonSchema.mock.JsonSchemaMocks.credentialContentValid
import ch.admin.foitt.wallet.platform.jsonSchema.mock.JsonSchemaMocks.credentialMissingRequiredClaim
import ch.admin.foitt.wallet.platform.jsonSchema.mock.JsonSchemaMocks.extendedValidVcSdJwtJsonSchema
import ch.admin.foitt.wallet.platform.jsonSchema.mock.JsonSchemaMocks.invalidVcSdJwtJsonSchema
import ch.admin.foitt.wallet.platform.jsonSchema.mock.JsonSchemaMocks.jsonSchemaInvalidSchemaReference
import ch.admin.foitt.wallet.platform.jsonSchema.mock.JsonSchemaMocks.jsonSchemaKeywordMisuse
import ch.admin.foitt.wallet.platform.jsonSchema.mock.JsonSchemaMocks.jsonSchemaKeywordMisuse2
import ch.admin.foitt.wallet.platform.jsonSchema.mock.JsonSchemaMocks.minimalValidVcSdJwtJsonSchema
import ch.admin.foitt.wallet.platform.jsonSchema.mock.JsonSchemaMocks.vcSdJwtJsonSchemaWithExternalRef
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class VcSdJwtJsonSchemaValidatorImplTest {

    private lateinit var jsonSchemaValidator: JsonSchemaValidator

    private val safeJson = SafeJsonTestInstance.safeJson

    @BeforeEach
    fun setUp() {
        jsonSchemaValidator = VcSdJwtJsonSchemaValidatorImpl(safeJson = safeJson)
    }

    @Test
    fun `Invalid Json schema for validating vc sd-jwt returns error`() = runTest {
        jsonSchemaValidator(credentialContentValid, invalidVcSdJwtJsonSchema).assertErrorType(JsonSchemaError.ValidationFailed::class)
    }

    @Test
    fun `Valid and minimal Json schema for validating vc sd-jwt returns success`() = runTest {
        jsonSchemaValidator(credentialContentValid, minimalValidVcSdJwtJsonSchema).assertOk()
    }

    @Test
    fun `Json schema containing url as $ref for validating vc sd-jwt returns error`() = runTest {
        jsonSchemaValidator(
            credentialContentValid,
            vcSdJwtJsonSchemaWithExternalRef
        ).assertErrorType(JsonSchemaError.ValidationFailed::class)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "invalid json",
            """{  "firstName": "John",  "lastName": "Doe"  "age": 21}""",
        ]
    )
    fun `Valid and minimal Json schema for validating invalid vc sd-jwt returns error`(jsonInput: String) = runTest {
        jsonSchemaValidator(jsonInput, minimalValidVcSdJwtJsonSchema).assertErrorType(JsonSchemaError.ValidationFailed::class)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "invalid json",
            """{  "firstName": "John",  "lastName": "Doe"  "age": 21}""",
        ]
    )
    fun `Invalid Json schema for validating valid vc sd-jwt returns error`(jsonInput: String) = runTest {
        jsonSchemaValidator(credentialContentValid, jsonInput).assertErrorType(JsonSchemaError.ValidationFailed::class)
    }

    @Test
    fun `Valid and extended Json schema for validating vc sd-jwt returns success`() = runTest {
        jsonSchemaValidator(credentialContentValid, extendedValidVcSdJwtJsonSchema).assertOk()
    }

    @Test
    fun `Json schema validations for invalid json schema (invalid schema reference) returns an error`() = runTest {
        jsonSchemaValidator(
            credentialContentValid,
            jsonSchemaInvalidSchemaReference
        ).assertErrorType(JsonSchemaError.ValidationFailed::class)
    }

    @Test
    fun `Json schema validations for invalid json schema (type must be a string) returns an error`() = runTest {
        jsonSchemaValidator(credentialContentValid, jsonSchemaKeywordMisuse).assertErrorType(JsonSchemaError.ValidationFailed::class)
    }

    @Test
    fun `Json schema validations for invalid json schema ('required' must be an array) returns an error`() = runTest {
        jsonSchemaValidator(credentialContentValid, jsonSchemaKeywordMisuse2).assertErrorType(JsonSchemaError.ValidationFailed::class)
    }

    @Test
    fun `Json schema validation for credential missing a required claim returns an error`() = runTest {
        jsonSchemaValidator(
            credentialMissingRequiredClaim,
            extendedValidVcSdJwtJsonSchema
        ).assertErrorType(JsonSchemaError.ValidationFailed::class)
    }
}
