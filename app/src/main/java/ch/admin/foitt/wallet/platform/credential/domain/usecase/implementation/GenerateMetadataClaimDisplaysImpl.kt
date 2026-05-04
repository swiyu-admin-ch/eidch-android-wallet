package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.toPointerString
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.Claim
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyClaimDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.GenerateMetadataDisplaysError
import ch.admin.foitt.wallet.platform.credential.domain.model.toGenerateMetadataDisplaysError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateMetadataClaimDisplays
import ch.admin.foitt.wallet.platform.credential.domain.util.addFallbackLanguageIfNecessary
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.imageValidation.domain.model.ValidateImageError
import ch.admin.foitt.wallet.platform.imageValidation.domain.usecase.ValidateImage
import ch.admin.foitt.wallet.platform.oca.domain.util.parseIso8601
import ch.admin.foitt.wallet.platform.ssi.domain.model.ImageType
import ch.admin.foitt.wallet.platform.ssi.domain.model.ImageType.Companion.hasMagicNumber
import ch.admin.foitt.wallet.platform.ssi.domain.model.ValueType
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

internal class GenerateMetadataClaimDisplaysImpl @Inject constructor(
    private val validateImage: ValidateImage,
) : GenerateMetadataClaimDisplays {
    override suspend fun invoke(
        claimsPathPointer: ClaimsPathPointer,
        claimValueJson: JsonElement,
        metadataClaim: Claim?,
        order: Int,
    ): Result<Pair<CredentialClaim, List<AnyClaimDisplay>>, GenerateMetadataDisplaysError> = coroutineBinding {
        val (claimValue, valueType) = when (claimValueJson) {
            is JsonPrimitive -> getClaimValueWithType(claimValueJson.jsonPrimitive).bind()
            is JsonArray -> claimValueJson.jsonArray.toString() to ValueType.STRING.value
            is JsonObject -> claimValueJson.jsonObject.toString() to ValueType.STRING.value
        }

        val credentialClaim = CredentialClaim(
            clusterId = UNKNOWN_CLUSTER_DISPLAY_ID,
            path = claimsPathPointer.toPointerString(),
            value = claimValue,
            valueType = valueType,
            order = order,
        )
        val claimDisplays: List<AnyClaimDisplay> = metadataClaim?.display?.map {
            AnyClaimDisplay(
                locale = it.locale,
                name = it.name,
            )
        }.addFallbackLanguageIfNecessary {
            AnyClaimDisplay(name = claimsPathPointer.toPointerString(), locale = DisplayLanguage.FALLBACK)
        }

        credentialClaim to claimDisplays
    }

    private suspend fun getClaimValueWithType(
        value: JsonPrimitive,
    ): Result<Pair<String?, String>, GenerateMetadataDisplaysError> = coroutineBinding {
        if (value.intOrNull != null) {
            value.content to ValueType.NUMERIC.value
        } else if (value.isString) {
            parseBoolean(value.content)
                ?: parseImage(value.content).bind()
                ?: parseDate(value.content)
                ?: (value.content to ValueType.STRING.value)
        } else {
            value.contentOrNull to DEFAULT_VALUE_TYPE.value
        }
    }

    private fun parseBoolean(value: String): Pair<String, String>? = when (value.toBooleanStrictOrNull()) {
        null -> null
        else -> value to ValueType.BOOLEAN.value
    }

    private suspend fun parseImage(value: String): Result<Pair<String, String>?, GenerateMetadataDisplaysError> = coroutineBinding {
        parseDataUrlImage(value).bind() ?: parseBase64Image(value)
    }

    private suspend fun parseDataUrlImage(
        value: String
    ): Result<Pair<String, String>?, GenerateMetadataDisplaysError> = coroutineBinding {
        if (ImageType.isValidImageDataUri(value)) {
            val parts = value.split(";base64,")
            val mimeType = parts[0].substringAfter("data:")

            validateImage(
                mimeType = mimeType,
                image = value,
            ).mapError(ValidateImageError::toGenerateMetadataDisplaysError)
                .bind()

            parseBase64Image(parts[1])
        } else {
            null
        }
    }

    private fun parseBase64Image(value: String): Pair<String, String>? = if (ImageType.PNG.hasMagicNumber(value)) {
        value to ImageType.PNG.mimeType
    } else if (ImageType.JPEG.hasMagicNumber(value)) {
        value to ImageType.JPEG.mimeType
    } else {
        null
    }

    private fun parseDate(value: String): Pair<String, String>? = when (parseIso8601(value).second) {
        null -> null
        else -> value to ValueType.DATETIME.value
    }

    companion object {
        private val DEFAULT_VALUE_TYPE = ValueType.STRING
        private const val UNKNOWN_CLUSTER_DISPLAY_ID = -1L
    }
}
