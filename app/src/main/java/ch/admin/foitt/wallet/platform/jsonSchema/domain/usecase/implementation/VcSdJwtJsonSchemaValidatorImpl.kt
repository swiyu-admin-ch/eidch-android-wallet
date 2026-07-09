package ch.admin.foitt.wallet.platform.jsonSchema.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.jsonSchema.domain.model.JsonSchemaError
import ch.admin.foitt.wallet.platform.jsonSchema.domain.model.toJsonSchemaError
import ch.admin.foitt.wallet.platform.jsonSchema.domain.usecase.JsonSchemaValidator
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import com.networknt.schema.InputFormat
import com.networknt.schema.Schema
import com.networknt.schema.SchemaLocation
import com.networknt.schema.SchemaRegistry
import com.networknt.schema.dialect.Dialects
import javax.inject.Inject

internal class VcSdJwtJsonSchemaValidatorImpl @Inject constructor() : JsonSchemaValidator {
    override suspend fun invoke(data: String, jsonSchema: String): Result<Unit, JsonSchemaError> = coroutineBinding {
        val schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft202012())

        // validate jsonSchema conforms to Json Schema Draft 2010-12
        val draft202012Schema = schemaRegistry.getSchema(SchemaLocation.of(Dialects.getDraft202012().id))
        validateDataWithJsonSchema(data = jsonSchema, jsonSchema = draft202012Schema).bind()

        // validate jsonSchema can validate a VcSdJwt schema
        val vcSdJwtMetaSchema = schemaRegistry.buildJsonSchema(schema = vcSdJwtMetaSchemaString).bind()
        validateDataWithJsonSchema(data = jsonSchema, jsonSchema = vcSdJwtMetaSchema).bind()

        // validate the data against the jsonSchema
        val schema = schemaRegistry.getSchema(jsonSchema, InputFormat.JSON)
        validateDataWithJsonSchema(data = data, jsonSchema = schema).bind()
    }

    private fun validateDataWithJsonSchema(
        data: String,
        jsonSchema: Schema
    ): Result<Unit, JsonSchemaError> = binding {
        val assertions = runSuspendCatching {
            jsonSchema.validate(data, InputFormat.JSON)
        }.mapError { throwable ->
            throwable.toJsonSchemaError()
        }.bind()

        if (assertions.isNotEmpty()) {
            Err(JsonSchemaError.ValidationFailed).bind()
        }
    }

    private fun SchemaRegistry.buildJsonSchema(
        schema: String,
    ): Result<Schema, JsonSchemaError> = runSuspendCatching {
        this.getSchema(schema)
    }.mapError { throwable ->
        throwable.toJsonSchemaError()
    }

    companion object {
        private val vcSdJwtMetaSchemaString = """
            {
              "properties": {
                "additionalItems": true,
                "type": "object",
                "required": ["properties", "required"],
                "required": {
                  "type": "array",
                  "allOf": [
                    { "contains": { "const": "vct" } }
                  ]
                },
                "properties": {
                  "type": "object",
                  "required": [ "iss", "vct", "nbf", "exp", "cnf", "status", "sub", "iat" ],
                  "properties": {
                    "vct": {
                      "type": "object",
                      "required": ["type"],
                      "properties": {
                        "type": { "const": "string" }
                      }
                    },
                    "iss": {
                      "type": "object",
                      "required": ["type"],
                      "properties": {
                        "type": { "const": "string" }
                      }
                    },
                    "nbf": {
                      "type": "object",
                      "required": ["type"],
                      "properties": {
                        "type": { "const": "number" }
                      }
                    },
                    "exp": {
                      "type": "object",
                      "required": ["type"],
                      "properties": {
                        "type": { "const": "number" }
                      }
                    },
                    "cnf": {
                      "type": "object",
                      "required": ["type"],
                      "properties": {
                        "type": { "const": "object" }
                      }
                    },
                    "status": {
                      "type": "object",
                      "required": ["type"],
                      "properties": {
                        "type": { "const": "object" }
                      }
                    },
                    "sub": {
                      "type": "object",
                      "required": ["type"],
                      "properties": {
                        "type": { "const": "string" }
                      }
                    },
                    "iat": {
                      "type": "object",
                      "required": ["type"],
                      "properties": {
                        "type": { "const": "number" }
                      }
                    }
                  }
                }
              }
            }
        """.trimIndent()
    }
}
