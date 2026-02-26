package ch.admin.foitt.wallet.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

@Suppress("detekt:TooManyFunctions")
object WalletTexts {

    @Composable
    fun Button(
        text: String,
        modifier: Modifier = Modifier,
    ) = Text(
        text = text,
        style = WalletTheme.typography.labelLarge,
        textAlign = TextAlign.Start,
        overflow = TextOverflow.Ellipsis,
        maxLines = 2,
        modifier = modifier,
    )

    @Composable
    fun ClusterHeadline(
        text: String,
        depth: Int,
    ) {
        val h1TopPadding = Sizes.s04
        val h2TopPadding = Sizes.s08
        val h3TopPadding = Sizes.s06

        if (text.isNotBlank()) {
            // Show text if not blank. Set padding, style and textItem according to depth
            val paddingValues = when (depth) {
                0 -> PaddingValues(start = Sizes.s08, top = h1TopPadding, bottom = Sizes.s01, end = Sizes.s04)
                1 -> PaddingValues(start = Sizes.s04, top = h2TopPadding, end = Sizes.s04)
                else -> PaddingValues(start = Sizes.s04, top = h3TopPadding, end = Sizes.s04)
            }

            when (depth) {
                0 -> HeadlineSmallEmphasized(
                    text = text,
                    modifier = Modifier
                        .padding(paddingValues)
                        .semantics { heading() }
                )

                1 -> TitleLarge(
                    text = text,
                    modifier = Modifier
                        .padding(paddingValues)
                        .background(WalletTheme.colorScheme.listItemBackground),
                )

                else -> TitleMedium(
                    text = text,
                    modifier = Modifier
                        .padding(paddingValues)
                        .background(WalletTheme.colorScheme.listItemBackground),
                )
            }
        } else {
            // If text is blank, add a spacer with height according to depth
            val height = when (depth) {
                0 -> h1TopPadding
                1 -> h2TopPadding
                else -> h3TopPadding
            }
            Spacer(modifier = Modifier.height(height))
        }
    }

    @Composable
    fun LargeCredentialTitle(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = WalletTheme.colorScheme.onPrimary,
        maxLines: Int = Int.MAX_VALUE
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.credentialLarge,
        textAlign = TextAlign.Start,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    @Composable
    fun LargeCredentialSubtitle(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = WalletTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
        maxLines: Int = Int.MAX_VALUE,
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.credentialLarge,
        textAlign = TextAlign.Start,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    @Composable
    fun MediumCredentialTitle(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = WalletTheme.colorScheme.onPrimary,
        maxLines: Int = Int.MAX_VALUE
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.credentialMedium,
        textAlign = TextAlign.Start,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    @Composable
    fun MediumCredentialSubtitle(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = WalletTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
        maxLines: Int = Int.MAX_VALUE,
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.credentialMedium,
        textAlign = TextAlign.Start,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    @Composable
    fun Body(
        text: String,
        modifier: Modifier = Modifier,
        textAlign: TextAlign = TextAlign.Start,
        color: Color = MaterialTheme.colorScheme.onBackground,
    ) = Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = textAlign,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    @Composable
    fun LabelMedium(
        text: AnnotatedString,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface,
    ) = Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.labelMedium,
        textAlign = TextAlign.Start,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    @Composable
    fun LabelSmall(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface,
    ) = Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Start,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    //region PublicWallet Texts
    @Composable
    fun TitleTopBar(
        modifier: Modifier = Modifier,
        text: String,
        color: Color = WalletTheme.colorScheme.onSurface,
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.titleLarge,
        textAlign = TextAlign.Start,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    @Composable
    fun TitleScreen(
        text: String,
        modifier: Modifier = Modifier,
        textAlign: TextAlign = TextAlign.Start,
        maxLines: Int = Int.MAX_VALUE,
        color: Color = WalletTheme.colorScheme.onSurface
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.headlineMedium,
        textAlign = textAlign,
        overflow = TextOverflow.Ellipsis,
        maxLines = maxLines,
        modifier = modifier
            .fillMaxWidth()
            .semantics { heading() }
            .testTag("TITLE_TEXT"),
    )

    @Composable
    fun HeadlineMedium(
        modifier: Modifier = Modifier,
        text: String,
        textAlign: TextAlign = TextAlign.Start,
        color: Color = WalletTheme.colorScheme.primary,
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.headlineMedium,
        textAlign = textAlign,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    @Composable
    fun HeadlineSmall(
        modifier: Modifier = Modifier,
        text: String,
        textAlign: TextAlign = TextAlign.Start,
        color: Color = WalletTheme.colorScheme.onSurface,
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.headlineSmall,
        textAlign = textAlign,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    @Composable
    fun HeadlineSmallEmphasized(
        modifier: Modifier = Modifier,
        text: String,
        textAlign: TextAlign = TextAlign.Start,
        color: Color = WalletTheme.colorScheme.onSurface,
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium),
        textAlign = textAlign,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    @Composable
    fun TitleLarge(
        text: String,
        modifier: Modifier = Modifier,
        textAlign: TextAlign = TextAlign.Start,
        maxLines: Int = 3,
        color: Color = WalletTheme.colorScheme.onSurface
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.titleLarge,
        textAlign = textAlign,
        overflow = TextOverflow.Ellipsis,
        maxLines = maxLines,
        modifier = modifier
            .semantics { heading() },
    )

    @Composable
    fun TitleMedium(
        text: String,
        modifier: Modifier = Modifier,
        textAlign: TextAlign = TextAlign.Start,
        color: Color = WalletTheme.colorScheme.onSurface
    ) = Text(
        modifier = modifier,
        text = text,
        color = color,
        style = WalletTheme.typography.titleMedium,
        textAlign = textAlign,
        overflow = TextOverflow.Ellipsis,
    )

    @Composable
    fun TitleSmall(
        modifier: Modifier = Modifier,
        text: String,
        textAlign: TextAlign = TextAlign.Start,
        color: Color = WalletTheme.colorScheme.primary,
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.titleSmall,
        textAlign = textAlign,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    @Composable
    fun BodyLarge(
        text: String,
        modifier: Modifier = Modifier,
        textAlign: TextAlign = TextAlign.Start,
        color: Color = WalletTheme.colorScheme.secondary
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.bodyLarge,
        textAlign = textAlign,
        overflow = TextOverflow.Ellipsis,
        maxLines = Int.MAX_VALUE,
        modifier = modifier,
    )

    @Composable
    fun BodyLargeEmphasized(
        text: String,
        modifier: Modifier = Modifier,
        textAlign: TextAlign = TextAlign.Start,
        color: Color = WalletTheme.colorScheme.secondary
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
        textAlign = textAlign,
        overflow = TextOverflow.Ellipsis,
        maxLines = Int.MAX_VALUE,
        modifier = modifier,
    )

    @Composable
    fun BodyMedium(
        text: String,
        modifier: Modifier = Modifier,
        textAlign: TextAlign = TextAlign.Start,
        color: Color = WalletTheme.colorScheme.secondary
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.bodyMedium,
        textAlign = textAlign,
        overflow = TextOverflow.Ellipsis,
        maxLines = Int.MAX_VALUE,
        modifier = modifier,
    )

    @Composable
    fun BodySmall(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = WalletTheme.colorScheme.onBackground,
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.bodySmall,
        textAlign = TextAlign.Start,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    @Composable
    fun LabelLarge(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = WalletTheme.colorScheme.secondary,
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.labelLarge,
        textAlign = TextAlign.Start,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    @Composable
    fun LabelLargeEmphasized(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = WalletTheme.colorScheme.secondary,
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
        textAlign = TextAlign.Start,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    @Composable
    fun LabelMedium(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = WalletTheme.colorScheme.secondary,
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.labelMedium,
        textAlign = TextAlign.Start,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )

//endregion
}
