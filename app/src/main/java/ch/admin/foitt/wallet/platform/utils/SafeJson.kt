package ch.admin.foitt.wallet.platform.utils

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import timber.log.Timber
import javax.inject.Inject

class SafeJson @Inject constructor(
    val json: Json
) {
    inline fun <reified T> safeDecodeStringTo(
        string: String,
    ): Result<T, JsonParsingError> = runSuspendCatching {
        json.decodeFromString<T>(string)
    }.mapError { throwable ->
        throwable.toJsonError("safeDecodeStringTo error")
    }

    inline fun <reified T> safeDecodeFromJsonElement(
        objectToDecode: JsonElement,
    ): Result<T, JsonParsingError> = runSuspendCatching {
        json.decodeFromJsonElement<T>(objectToDecode)
    }.mapError { throwable ->
        throwable.toJsonError("safeDecodeFromJsonElement error")
    }

    inline fun <reified T> safeEncodeObjectToString(objectToEncode: T): Result<String, JsonParsingError> = runSuspendCatching {
        json.encodeToString(objectToEncode)
    }.mapError { throwable ->
        throwable.toJsonError("SafeJson encodeToString error")
    }

    inline fun <reified T> safeDecodeElementTo(
        jsonElement: JsonElement,
    ): Result<T, JsonParsingError> = runSuspendCatching {
        json.decodeFromJsonElement<T>(jsonElement)
    }.mapError { throwable ->
        throwable.toJsonError("safeDecodeElementTo error")
    }

    inline fun <reified T> safeEncodeObjectToJsonElement(
        objectToEncode: T,
    ): Result<JsonElement, JsonParsingError> = runSuspendCatching {
        json.encodeToJsonElement(objectToEncode)
    }.mapError { throwable ->
        throwable.toJsonError("safeEncodeObjectToJsonElement error")
    }

    fun safeParseToJsonElement(
        stringToParse: String,
    ): Result<JsonElement, JsonParsingError> = runSuspendCatching {
        json.parseToJsonElement(stringToParse)
    }.mapError { throwable ->
        throwable.toJsonError("safeParseToJsonElement error")
    }
}

interface JsonError {
    data class Unexpected(val throwable: Throwable) : JsonParsingError
}

sealed interface JsonParsingError

fun Throwable.toJsonError(message: String): JsonParsingError {
    Timber.e(t = this, message = message)
    return JsonError.Unexpected(this)
}
