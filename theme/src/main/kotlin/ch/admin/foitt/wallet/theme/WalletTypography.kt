package ch.admin.foitt.wallet.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

@Immutable
data class WalletTypography(
    val displayLarge: TextStyle,
    val displayMedium: TextStyle,
    val displaySmall: TextStyle,
    val headlineLarge: TextStyle,
    val headlineMedium: TextStyle,
    val headlineSmall: TextStyle,
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val titleSmall: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val labelLarge: TextStyle,
    val labelMedium: TextStyle,
    val labelSmall: TextStyle,
    val credentialMedium: TextStyle,
    val credentialLarge: TextStyle,
) {
    val materialTypography: Typography by lazy {
        Typography(
            displayLarge = displayLarge,
            displayMedium = displayMedium,
            displaySmall = displaySmall,
            headlineLarge = headlineLarge,
            headlineMedium = headlineMedium,
            headlineSmall = headlineSmall,
            titleLarge = titleLarge,
            titleMedium = titleMedium,
            titleSmall = titleSmall,
            bodyLarge = bodyLarge,
            bodyMedium = bodyMedium,
            bodySmall = bodySmall,
            labelLarge = labelLarge,
            labelMedium = labelMedium,
            labelSmall = labelSmall
        )
    }

    companion object {

        private val baseTextStyle = TextStyle(
            fontFamily = abcDiatype,
            fontWeight = FontWeight.Normal,
            hyphens = Hyphens.Auto,
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Proportional,
                trim = LineHeightStyle.Trim.None,
            ),
        )

        val default by lazy {
            WalletTypography(
                displayLarge = TextStyle(
                    fontFamily = abcDiatype,
                    fontWeight = FontWeight.Normal,
                    fontSize = 57.sp,
                    lineHeight = 64.sp,
                    hyphens = Hyphens.Auto,
                ),
                displayMedium = TextStyle(
                    fontFamily = abcDiatype,
                    fontWeight = FontWeight.Normal,
                    fontSize = 45.sp,
                    lineHeight = 52.sp,
                    hyphens = Hyphens.Auto,
                ),
                displaySmall = TextStyle(
                    fontFamily = abcDiatype,
                    fontWeight = FontWeight.Normal,
                    fontSize = 36.sp,
                    lineHeight = 44.sp,
                    hyphens = Hyphens.Auto,
                ),
                headlineLarge = TextStyle(
                    fontFamily = abcDiatype,
                    fontWeight = FontWeight.Normal,
                    fontSize = 32.sp,
                    lineHeight = 38.sp,
                    hyphens = Hyphens.Auto,
                ),
                headlineMedium = TextStyle(
                    fontFamily = abcDiatype,
                    fontWeight = FontWeight.Normal,
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                    hyphens = Hyphens.Auto,
                ),
                headlineSmall = TextStyle(
                    fontFamily = abcDiatype,
                    fontWeight = FontWeight.Normal,
                    fontSize = 24.sp,
                    lineHeight = 30.sp,
                    hyphens = Hyphens.Auto,
                ),
                titleLarge = TextStyle(
                    fontFamily = abcDiatype,
                    fontWeight = FontWeight.Normal,
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    hyphens = Hyphens.Auto,
                ),
                titleMedium = baseTextStyle.merge(
                    TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                    )
                ),
                titleSmall = baseTextStyle.merge(
                    TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                    )
                ),
                labelLarge = baseTextStyle.merge(
                    TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                    )
                ),
                labelMedium = TextStyle(
                    fontFamily = abcDiatype,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    hyphens = Hyphens.Auto,
                ),
                labelSmall = TextStyle(
                    fontFamily = abcDiatype,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    hyphens = Hyphens.Auto,
                ),
                bodyLarge = baseTextStyle.merge(
                    TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                    )
                ),
                bodyMedium = baseTextStyle.merge(
                    TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                    )
                ),
                bodySmall = baseTextStyle.merge(
                    TextStyle(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                    )
                ),
                credentialMedium = TextStyle(
                    fontFamily = abcDiatypeSemiMono,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    hyphens = Hyphens.Auto,
                ),
                credentialLarge = TextStyle(
                    fontFamily = abcDiatypeSemiMono,
                    fontWeight = FontWeight.Normal,
                    fontSize = 20.sp,
                    lineHeight = 26.sp,
                    hyphens = Hyphens.Auto,
                )
            )
        }
    }
}
