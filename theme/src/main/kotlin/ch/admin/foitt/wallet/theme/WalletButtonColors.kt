@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable

object WalletButtonColors {
    @Composable
    fun primary(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = WalletTheme.colorScheme.primary,
        contentColor = WalletTheme.colorScheme.onPrimary,
        disabledContainerColor = WalletTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor = WalletTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    )

    @Composable
    fun lightPrimary(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = WalletTheme.colorScheme.lightPrimary,
        contentColor = WalletTheme.colorScheme.onLightPrimary,
        disabledContainerColor = WalletTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor = WalletTheme.colorScheme.onSurface,
    )

    @Composable
    fun secondary(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = WalletTheme.colorScheme.secondary,
        contentColor = WalletTheme.colorScheme.onSecondary,
        disabledContainerColor = WalletTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor = WalletTheme.colorScheme.onSurface,
    )

    @Composable
    fun primaryFixed(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = WalletTheme.colorScheme.primaryFixed,
        contentColor = WalletTheme.colorScheme.onPrimaryFixed,
        disabledContainerColor = WalletTheme.colorScheme.onSurfaceFixed.copy(alpha = 0.12f),
        disabledContentColor = WalletTheme.colorScheme.onSurfaceFixed,
    )

    @Composable
    fun secondaryFixed(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = WalletTheme.colorScheme.secondaryFixed,
        contentColor = WalletTheme.colorScheme.onSecondaryFixed,
        disabledContainerColor = WalletTheme.colorScheme.onSurfaceFixed.copy(alpha = 0.12f),
        disabledContentColor = WalletTheme.colorScheme.onSurfaceFixed,
    )

    @Composable
    fun secondaryContainerFixed(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = WalletTheme.colorScheme.secondaryContainerFixed,
        contentColor = WalletTheme.colorScheme.onSecondaryContainerFixed,
        disabledContainerColor = WalletTheme.colorScheme.onSurfaceFixed.copy(alpha = 0.12f),
        disabledContentColor = WalletTheme.colorScheme.onSurfaceFixed,
    )

    @Composable
    fun tertiary(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = WalletTheme.colorScheme.tertiary,
        contentColor = WalletTheme.colorScheme.onTertiary,
        disabledContainerColor = WalletTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor = WalletTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    )

    @Composable
    fun tonal(): ButtonColors = ButtonDefaults.filledTonalButtonColors(
        containerColor = WalletTheme.colorScheme.secondaryContainer,
        contentColor = WalletTheme.colorScheme.onSecondaryContainer,
        disabledContainerColor = WalletTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor = WalletTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    )

    @Composable
    fun outlined(): ButtonColors = ButtonDefaults.outlinedButtonColors(
        containerColor = WalletTheme.colorScheme.onSurface.copy(alpha = 0f),
        contentColor = WalletTheme.colorScheme.primary,
        disabledContainerColor = WalletTheme.colorScheme.onSurface.copy(alpha = 0f),
        disabledContentColor = WalletTheme.colorScheme.onSurface.copy(alpha = 0.12f),
    )

    @Composable
    fun text(): ButtonColors = ButtonDefaults.textButtonColors(
        containerColor = WalletTheme.colorScheme.primary.copy(alpha = 0f),
        contentColor = WalletTheme.colorScheme.primary,
        disabledContainerColor = WalletTheme.colorScheme.primary.copy(alpha = 0f),
        disabledContentColor = WalletTheme.colorScheme.onSurface,
    )

    @Composable
    fun textError(): ButtonColors = ButtonDefaults.textButtonColors(
        containerColor = WalletTheme.colorScheme.background,
        contentColor = WalletTheme.colorScheme.error,
        disabledContainerColor = WalletTheme.colorScheme.primary.copy(alpha = 0f),
        disabledContentColor = WalletTheme.colorScheme.onSurface,
    )

    @Composable
    fun elevated(): ButtonColors = ButtonDefaults.elevatedButtonColors(
        containerColor = WalletTheme.colorScheme.surfaceContainerLow,
        contentColor = WalletTheme.colorScheme.primary,
        disabledContainerColor = WalletTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor = WalletTheme.colorScheme.onSurface,
    )

    @Composable
    fun iconSecondaryFixed(): IconButtonColors = IconButtonDefaults.iconButtonColors(
        containerColor = WalletTheme.colorScheme.secondaryFixed,
        contentColor = WalletTheme.colorScheme.onSecondaryFixed,
        disabledContainerColor = WalletTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor = WalletTheme.colorScheme.onSurface,
    )

    @Composable
    fun brandRed(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = WalletTheme.colorScheme.surfaceContainerLow,
        contentColor = WalletTheme.colorScheme.errorFixed,
        disabledContainerColor = WalletTheme.colorScheme.surfaceContainerLow,
        disabledContentColor = WalletTheme.colorScheme.onLightPrimary,
    )

    @Composable
    fun feedbackFailurePrimary(): ButtonColors = ButtonDefaults.textButtonColors(
        containerColor = WalletTheme.colorScheme.primary,
        contentColor = WalletTheme.colorScheme.surfaceContainerHighest,
        disabledContainerColor = WalletTheme.colorScheme.primary.copy(alpha = 0f),
        disabledContentColor = WalletTheme.colorScheme.onSurface,
    )

    @Composable
    fun feedbackFailureSecondary(): ButtonColors = ButtonDefaults.textButtonColors(
        containerColor = WalletTheme.colorScheme.surfaceContainerHighest,
        contentColor = WalletTheme.colorScheme.primary,
        disabledContainerColor = WalletTheme.colorScheme.primary.copy(alpha = 0f),
        disabledContentColor = WalletTheme.colorScheme.onSurface,
    )

    @Composable
    fun feedbackDeclinePrimary(): ButtonColors = ButtonDefaults.textButtonColors(
        containerColor = WalletTheme.colorScheme.onPrimary,
        contentColor = WalletTheme.colorScheme.onSurface,
        disabledContainerColor = WalletTheme.colorScheme.primary.copy(alpha = 0f),
        disabledContentColor = WalletTheme.colorScheme.onSurface,
    )

    @Composable
    fun feedbackDeclineSecondary(): ButtonColors = ButtonDefaults.textButtonColors(
        containerColor = WalletTheme.colorScheme.primary,
        contentColor = WalletTheme.colorScheme.lightPrimary,
        disabledContainerColor = WalletTheme.colorScheme.primary.copy(alpha = 0f),
        disabledContentColor = WalletTheme.colorScheme.onSurface,
    )

    @Composable
    fun feedbackSuccessPrimary(): ButtonColors = ButtonDefaults.textButtonColors(
        containerColor = WalletTheme.colorScheme.lightTertiary,
        contentColor = WalletTheme.colorScheme.onLightTertiary,
        disabledContainerColor = WalletTheme.colorScheme.primary.copy(alpha = 0f),
        disabledContentColor = WalletTheme.colorScheme.onSurface,
    )
}
