package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.oca.domain.model.DateTimePattern
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimImage
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimItem
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimText
import ch.admin.foitt.wallet.platform.ssi.domain.model.MapToCredentialClaimDataError
import ch.admin.foitt.wallet.platform.ssi.domain.model.ValueType
import ch.admin.foitt.wallet.platform.ssi.domain.model.toMapToCredentialClaimDataError
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.MapToCredentialClaimData
import ch.admin.foitt.wallet.platform.utils.asBestLocalizedForPattern
import ch.admin.foitt.wallet.platform.utils.base64NonUrlStringToByteArray
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.get
import com.github.michaelbull.result.mapError
import java.text.NumberFormat
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Locale
import javax.inject.Inject

class MapToCredentialClaimDataImpl @Inject constructor(
    private val getLocalizedDisplay: GetLocalizedDisplay,
    private val getCurrentAppLocale: GetCurrentAppLocale
) : MapToCredentialClaimData {
    override suspend fun invoke(
        claimWithDisplays: CredentialClaimWithDisplays,
    ): Result<CredentialClaimItem, MapToCredentialClaimDataError> =
        runSuspendCatching {
            val displays = claimWithDisplays.displays
            val claim = claimWithDisplays.claim
            val locale = getCurrentAppLocale()
            getLocalizedDisplay(displays)?.let { display ->
                when (ValueType.getByType(claim.valueType)) {
                    ValueType.BOOLEAN,
                    ValueType.STRING -> CredentialClaimText(
                        id = claim.id,
                        localizedLabel = display.name,
                        order = claim.order,
                        value = display.value ?: claim.value?.let { truncateClaimValue(claimValue = it) },
                        isSensitive = claim.isSensitive
                    )

                    ValueType.DATETIME -> CredentialClaimText(
                        id = claim.id,
                        localizedLabel = display.name,
                        order = claim.order,
                        value = claim.value?.let {
                            localizeDateTime(
                                value = it,
                                pattern = claim.valueDisplayInfo,
                                locale = locale
                            )
                        },
                        isSensitive = claim.isSensitive
                    )

                    ValueType.NUMERIC -> CredentialClaimText(
                        id = claim.id,
                        localizedLabel = display.name,
                        order = claim.order,
                        value = display.value ?: claim.value?.let { localizeNumber(numberString = it, locale = locale) },
                        isSensitive = claim.isSensitive
                    )

                    ValueType.IMAGE -> {
                        if (claim.value != null) {
                            val byteArray = base64NonUrlStringToByteArray(claim.value)
                            CredentialClaimImage(
                                id = claim.id,
                                localizedLabel = display.name,
                                order = claim.order,
                                imageData = byteArray,
                                isSensitive = claim.isSensitive
                            )
                        } else {
                            CredentialClaimText(
                                id = claim.id,
                                localizedLabel = display.name,
                                value = null,
                                order = claim.order,
                                isSensitive = claim.isSensitive
                            )
                        }
                    }

                    ValueType.UNSUPPORTED -> {
                        error("Unsupported value type '${claim.valueType}' found for claim '${claim.path}'")
                    }
                }
            } ?: error("No localized display found")
        }.mapError { throwable ->
            throwable.toMapToCredentialClaimDataError("MapToCredentialClaimData error")
        }

    private fun truncateClaimValue(claimValue: String) = if (claimValue.length > MAX_CLAIM_LENGTH) {
        claimValue.take(MAX_CLAIM_LENGTH) + "…"
    } else {
        claimValue
    }

    private fun localizeDateTime(value: String, pattern: String?, locale: Locale): String = runSuspendCatching {
        when (val dateTimePattern = DateTimePattern.getPatternFor(pattern)) {
            null -> value

            DateTimePattern.DATE_TIME_TIMEZONE_SECONDS_FRACTION,
            DateTimePattern.DATE_TIME_TIMEZONE_SECONDS,
            DateTimePattern.DATE_TIME_TIMEZONE,
            DateTimePattern.DATE_TIMEZONE,
            DateTimePattern.TIME_TIMEZONE_SECONDS_FRACTION,
            DateTimePattern.TIME_TIMEZONE_SECONDS,
            DateTimePattern.TIME_TIMEZONE -> {
                // output date in local timezone
                ZonedDateTime.parse(value).withZoneSameInstant(ZoneOffset.systemDefault()).asBestLocalizedForPattern(
                    locale = locale,
                    pattern = dateTimePattern.pattern
                )
            }

            DateTimePattern.DATE_TIME_SECONDS_FRACTION,
            DateTimePattern.DATE_TIME_SECONDS,
            DateTimePattern.DATE_TIME,
            DateTimePattern.DATE,
            DateTimePattern.TIME_SECONDS_FRACTION,
            DateTimePattern.TIME_SECONDS,
            DateTimePattern.TIME,
            DateTimePattern.YEAR_MONTH,
            DateTimePattern.YEAR -> {
                // output date as is
                ZonedDateTime.parse(value).withZoneSameInstant(ZoneOffset.UTC).asBestLocalizedForPattern(
                    locale = locale,
                    pattern = dateTimePattern.pattern
                )
            }
        }
    }.get() ?: value

    private fun localizeNumber(numberString: String, locale: Locale): String = runSuspendCatching {
        val matchResult = numberRegex.matchEntire(numberString)
        if (matchResult == null) {
            // input is not a number we handle
            return@runSuspendCatching numberString
        } else {
            formatNumber(preferredLocale = locale, matchResult = matchResult)
        }
    }.get() ?: numberString

    private fun formatNumber(preferredLocale: Locale, matchResult: MatchResult): String {
        val signPart = matchResult.groups[1]?.value
        val integerPart = matchResult.groups[2]?.value ?: error("missing mandatory integer part")
        val decimalPart = matchResult.groups[3]?.value
        val exponentPart = matchResult.groups[4]?.value

        val numberToFormat = integerPart + (decimalPart?.let { ".$it" } ?: "")

        val formattedNumber = NumberFormat.getNumberInstance(preferredLocale)
            .apply {
                isGroupingUsed = true
                minimumIntegerDigits = integerPart.length
                maximumIntegerDigits = integerPart.length
                minimumFractionDigits = decimalPart?.length ?: 0
                maximumFractionDigits = decimalPart?.length ?: 0
            }
            .format(numberToFormat.toDouble())

        return (signPart ?: "") + formattedNumber + (exponentPart ?: "")
    }

    private companion object {
        // group 1: optional sign part (minus)
        // group 2: mandatory integer part
        // group 3: optional decimal part (dot followed by an integer)
        // group 4: optional exponent part (e/E followed by optional plus/minus followed by an integer)
        val numberRegex = Regex("""^(-?)(\d+)(?:\.(\d+))?([eE][+-]?\d+)?$""")

        // Claim values can be long texts (or misconfigured images that are shown as text), which can cause an ANR
        const val MAX_CLAIM_LENGTH = 1800
    }
}
