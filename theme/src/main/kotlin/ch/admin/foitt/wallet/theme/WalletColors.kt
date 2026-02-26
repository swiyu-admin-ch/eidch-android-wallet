package ch.admin.foitt.wallet.theme

import androidx.compose.ui.graphics.Color

internal object WalletColors {
    val blue10 = Color(0xFF001D34)
    val blue15 = Color(0xFF092740)
    val blue20 = Color(0xFF17324C)
    val blue30 = Color(0xFF2F4963)
    val blue40 = Color(0xFF47607C)
    val blue50 = Color(0xFF607996)
    val blue70 = Color(0xFF91ABCA)
    val blue80 = Color(0xFFAFC9E9)
    val blue90 = Color(0xFFD0E4FF)
    val blue95 = Color(0xFFE9F1FF)

    val grey04 = Color(0xFF07090B)
    val grey06 = Color(0xFF0F1316)
    val grey10 = Color(0xFF161C21)
    val grey12 = Color(0xFF1D2328)
    val grey17 = Color(0xFF242A30)
    val grey20 = Color(0xFF2F3032)
    val grey22 = Color(0xFF30363C)
    val grey24 = Color(0xFF38393B)
    val grey25 = Color(0xFF363C42)
    val grey30 = Color(0xFF41484D)
    val grey40 = Color(0xFF595F65)
    val grey50 = Color(0xFF71787E)
    val grey60 = Color(0xFF8B9198)
    val grey80 = Color(0xFFC1C7CE)
    val grey87 = Color(0xFFD6DCE3)
    val grey90 = Color(0xFFDDE3EA)
    val grey92 = Color(0xFFE2E8EF)
    val grey94 = Color(0xFFE6ECF3)
    val grey95 = Color(0xFFF1F0F3)
    val grey96 = Color(0xFFF1F5FC)

    val green08 = Color(0xFF002C26)
    val green20 = Color(0xFF003731)
    val green30 = Color(0xFF005047)
    val green35 = Color(0xFF135C53)
    val green50 = Color(0xFF408277)
    val green80 = Color(0xFF91D3C6)
    val green90 = Color(0xFFACEFE2)
    val green98 = Color(0xFFE5FFF8)

    val red15 = Color(0xFF540003)
    val red16 = Color(0xFF5F0000)
    val red20 = Color(0xFF690005)
    val red40 = Color(0xFFC00012)
    val red49 = Color(0xFFE94366)
    val red50 = Color(0xFFEA1C21)
    val red80 = Color(0xFFFFB4AB)
    val red95 = Color(0xFFFFEDEA)

    val purple18 = Color(0xFF500A5A)
    val purple27 = Color(0xFF64236E)
    val purple91 = Color(0xFFF8D8FA)

    val orange15 = Color(0xFF4A1300)
    val orange50 = Color(0xFFD54500)
    val orange80 = Color(0xFFFFB59C)
    val orange95 = Color(0xFFFFEDE8)

    val white = Color(0xFFFFFFFF)
    val black = Color(0xFF000000)

    val transparentWhite01 = Color(0x66FFFFFF)
    val transparentWhite02 = Color(0x26FFFFFF)
    val transparentWhite03 = white.copy(alpha = 0.6f)
    val transparentBlack01 = Color(0x66000000)
    val transparentBlack02 = Color(0x26121315)
    val tansparentBlack03 = grey10.copy(alpha = 0.6f)
}

// The light scrim color used in the platform API 29+
// https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/com/android/internal/policy/DecorView.java;drc=6ef0f022c333385dba2c294e35b8de544455bf19;l=142
val DefaultLightScrim = Color(0xFFe6FFFF)

// The dark scrim color used in the platform.
// https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/res/res/color/system_bar_background_semi_transparent.xml
// https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/res/remote_color_resources_res/values/colors.xml;l=67
val DefaultDarkScrim = Color(0x801b1b1b)
