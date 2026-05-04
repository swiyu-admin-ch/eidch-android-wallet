package ch.admin.foitt.openid4vc.domain.model.sdjwt

import ch.admin.foitt.openid4vc.domain.model.DigestAlgorithm
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.utils.createDigest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import timber.log.Timber
import java.util.Base64

/**
 * https://www.rfc-editor.org/rfc/rfc9901.html
 */

private typealias Digest = String

open class SdJwt(
    rawSdJwt: String,
    private val nonSelectivelyDisclosableClaims: Set<String> = emptySet(),
) : Jwt(rawJwt = rawSdJwt.split(SD_JWT_SEPARATOR).first()) {

    private val digestAlgorithm = run {
        val rawAlgorithm = payloadJson[ALGORITHM_KEY]?.jsonPrimitive?.content ?: DEFAULT_DIGEST_ALGORITHM
        DigestAlgorithm.from(rawAlgorithm) ?: error("Unsupported digest algorithm $rawAlgorithm")
    }

    private val disclosureMap: Map<Digest, Disclosure> = run {
        getDisclosures(rawSdJwt)?.let { disclosures ->
            parseDisclosures(disclosures, digestAlgorithm)
        } ?: emptyMap()
    }

    val processedJson: JsonObject = run {
        val disclosures = disclosureMap.toMutableMap()
        val jsonObject = JsonObject(payloadJson.toMutableMap() - ALGORITHM_KEY)
        val json =
            resolveDigestsWithDisclosures(jsonElement = jsonObject, unresolvedDisclosureMap = disclosures, seenDigests = mutableSetOf())
        check(disclosures.isEmpty()) {
            "Disclosures that have no matching digest in the payload: ${disclosures.values.joinToString(", ") { it.disclosure }}"
        }
        json.jsonObject
    }

    private fun getDisclosures(rawSdJwt: String): String? {
        val matchResult = Regex(SD_JWT_PATTERN).matchEntire(rawSdJwt)
        val matchGroups = matchResult?.groups?.let { groupCollection ->
            groupCollection as MatchNamedGroupCollection
        } ?: error("Could not parse SdJwt from payload: $rawSdJwt")
        return matchGroups[DISCLOSURES]?.value
    }

    private fun parseDisclosures(
        rawDisclosures: String,
        digestAlgorithm: DigestAlgorithm,
    ): Map<Digest, Disclosure> {
        val disclosures = rawDisclosures.trim(SD_JWT_SEPARATOR).split(SD_JWT_SEPARATOR)
        val disclosureByDigest = disclosures.associate { disclosure ->
            val digest = disclosure.createDigest(digestAlgorithm)
            digest to parseDisclosure(disclosure)
        }
        check(disclosureByDigest.size == disclosures.size) {
            "Multiple disclosures with same digest detected"
        }
        return disclosureByDigest
    }

    private fun parseDisclosure(disclosure: String): Disclosure {
        val decoded = base64UrlEncodedStringToByteArray(disclosure)
        val jsonElement = Json.parseToJsonElement(String(decoded))
        check(jsonElement is JsonArray)
        val array = jsonElement.jsonArray
        return when (array.size) {
            3 -> parseDisclosureWithKey(array, disclosure)
            2 -> Disclosure.ArrayElement(value = array[1], disclosure = disclosure)
            else -> error("Invalid disclosure: $disclosure")
        }
    }

    private fun parseDisclosureWithKey(
        array: JsonArray,
        disclosure: String
    ): Disclosure.KeyedElement {
        val claimName = array[1].jsonPrimitive.content
        check(claimName !in listOf(DIGESTS_KEY, ARRAY_DIGEST_KEY, ALGORITHM_KEY) + nonSelectivelyDisclosableClaims) {
            "Disclosure with invalid claim name: $disclosure"
        }
        return Disclosure.KeyedElement(key = claimName, value = array[2], disclosure = disclosure)
    }

    private fun base64UrlEncodedStringToByteArray(string: String): ByteArray =
        Base64.getUrlDecoder().decode(string)

    private fun resolveDigestsWithDisclosures(
        jsonElement: JsonElement,
        unresolvedDisclosureMap: MutableMap<Digest, Disclosure>,
        seenDigests: MutableSet<Digest>,
    ): JsonElement = when (jsonElement) {
        is JsonObject -> resolveDigestsWithDisclosures(
            jsonObject = jsonElement,
            unresolvedDisclosureMap = unresolvedDisclosureMap,
            seenDigests = seenDigests,
        )

        is JsonArray -> resolveArrayElementsWithDisclosures(
            jsonArray = jsonElement,
            unresolvedDisclosureMap = unresolvedDisclosureMap,
            seenDigests = seenDigests,
        )

        else -> jsonElement
    }

    private fun resolveDigestsWithDisclosures(
        jsonObject: JsonObject,
        unresolvedDisclosureMap: MutableMap<Digest, Disclosure>,
        seenDigests: MutableSet<Digest>,
    ): JsonObject {
        check(jsonObject[ARRAY_DIGEST_KEY] == null && jsonObject[ALGORITHM_KEY] == null) {
            "Reserved key ('$ARRAY_DIGEST_KEY', '$ALGORITHM_KEY') detected in JSON payload: $jsonObject"
        }
        val digestsJsonElement = jsonObject[DIGESTS_KEY]?.let { element ->
            resolveDigestArrayWithDisclosures(
                element = element,
                unresolvedDisclosureMap = unresolvedDisclosureMap,
                seenDigests = seenDigests
            )
        }.orEmpty()

        val otherElements = jsonObject.toMutableMap() - DIGESTS_KEY
        val otherElementsWithClaims = otherElements.mapValues { element ->
            resolveDigestsWithDisclosures(
                jsonElement = element.value,
                unresolvedDisclosureMap = unresolvedDisclosureMap,
                seenDigests = seenDigests,
            )
        }
        val duplicatedKeys = otherElementsWithClaims.keys.intersect(digestsJsonElement.keys)
        check(duplicatedKeys.isEmpty()) {
            "Claims with same key name detected: $duplicatedKeys"
        }
        return JsonObject(otherElementsWithClaims + digestsJsonElement)
    }

    private fun resolveDigestArrayWithDisclosures(
        element: JsonElement,
        unresolvedDisclosureMap: MutableMap<Digest, Disclosure>,
        seenDigests: MutableSet<Digest>
    ): JsonObject {
        val digests = parseDigestArray(element, seenDigests)
        val disclosures = digests.mapNotNull { unresolvedDisclosureMap[it] } // ignores decoy digests
        unresolvedDisclosureMap -= digests.toSet()
        val json = disclosures.associate { disclosure ->
            check(disclosure is Disclosure.KeyedElement) {
                "Digest does not reference keyed element disclosure: $disclosure"
            }
            val resolvedElement = resolveDigestsWithDisclosures(
                jsonElement = disclosure.value,
                unresolvedDisclosureMap = unresolvedDisclosureMap,
                seenDigests = seenDigests,
            )
            disclosure.key to resolvedElement
        }
        check(disclosures.size == json.size) {
            val duplicatedKeys = disclosures.map { (it as Disclosure.KeyedElement).key } - json.keys
            "Claims with same key name detected: $duplicatedKeys"
        }
        return JsonObject(json)
    }

    private fun parseDigestArray(element: JsonElement, seenDigests: MutableSet<Digest>): List<String> =
        if (element is JsonArray) {
            val digests = element.jsonArray.map { it.jsonPrimitive.content }
            val duplicatedDigests = (digests - digests.toSet()) + digests.intersect(seenDigests)
            check(duplicatedDigests.isEmpty()) { "Duplicated digests found: $duplicatedDigests" }
            seenDigests += digests
            digests
        } else {
            error("Invalid digests: $element")
        }

    private fun resolveArrayElementsWithDisclosures(
        jsonArray: JsonArray,
        unresolvedDisclosureMap: MutableMap<Digest, Disclosure>,
        seenDigests: MutableSet<Digest>
    ): JsonArray {
        val jsonElements = jsonArray.mapNotNull { element ->
            if (element is JsonObject && element.keys == setOf(ARRAY_DIGEST_KEY)) {
                val digest = element[ARRAY_DIGEST_KEY]!!.jsonPrimitive.content
                check(digest !in seenDigests) {
                    "Duplicate digest found: $digest"
                }
                seenDigests += digest
                val disclosure = unresolvedDisclosureMap[digest] ?: return@mapNotNull null // ignore decoy digests
                unresolvedDisclosureMap -= digest
                check(disclosure is Disclosure.ArrayElement) {
                    "Digest does not reference array element disclosure: $disclosure"
                }
                resolveDigestsWithDisclosures(
                    jsonElement = disclosure.value,
                    unresolvedDisclosureMap = unresolvedDisclosureMap,
                    seenDigests = seenDigests,
                )
            } else {
                resolveDigestsWithDisclosures(
                    jsonElement = element,
                    unresolvedDisclosureMap = unresolvedDisclosureMap,
                    seenDigests = seenDigests
                )
            }
        }
        return JsonArray(jsonElements)
    }

    fun createSelectiveDisclosure(requestedFieldKeys: List<String>): String {
        val disclosures = disclosureMap.values
            .filter {
                when (it) {
                    is Disclosure.KeyedElement -> it.key in requestedFieldKeys
                    is Disclosure.ArrayElement -> false
                }
            }
            .map { it.disclosure }
        return StringBuilder(signedJwt.parsedString).apply {
            append(SD_JWT_SEPARATOR)
            if (disclosures.isNotEmpty()) {
                disclosures.forEach { disclosure ->
                    append(disclosure)
                    append(SD_JWT_SEPARATOR)
                }
            } else {
                Timber.w(message = "No disclosure for this verification")
            }
        }.toString()
    }

    private companion object {
        const val JWT = "jwt"
        const val ALGORITHM_KEY = "_sd_alg"
        const val DISCLOSURES = "disclosures"
        const val KEYBINDING_JWT = "keyBindingJwt"
        const val SD_JWT_SEPARATOR = '~'
        const val SD_JWT_PATTERN = "^" +
            "(?<$JWT>(?<header>[A-Za-z0-9-_]+)\\.(?<body>[A-Za-z0-9-_]+)\\.(?<signature>[A-Za-z0-9-_]+))" + // 1 issuer-signed JWT
            "($SD_JWT_SEPARATOR" +
            "(?<$DISCLOSURES>(([A-Za-z0-9-_]+)$SD_JWT_SEPARATOR)+)?" + // 0..* Disclosures + "~"
            "(?<$KEYBINDING_JWT>([A-Za-z0-9-_]+)\\.([A-Za-z0-9-_]+)\\.([A-Za-z0-9-_]+))?" + // 0..1 Key Binding JWT
            ")$"

        const val DIGESTS_KEY = "_sd"
        const val ARRAY_DIGEST_KEY = "..."
        val DEFAULT_DIGEST_ALGORITHM = DigestAlgorithm.SHA256.stdName
    }
}
