package ch.admin.foitt.wallet.platform.composables.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import ch.admin.foitt.wallet.theme.FadingVisibility
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay

@Composable
fun ScreenMainAnimation(
    @RawRes animationRes: Int,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    @DrawableRes fallbackImage: Int? = null,
    fallbackDelay: Long = 1000L,
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationRes))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    composition?.let {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = modifier
                .fillMaxSize()
                .clip(WalletTheme.shapes.extraLarge)
                .background(WalletTheme.colorScheme.surfaceContainerLow),
            contentScale = contentScale,
            alignment = Alignment.Center,
        )
    } ?: fallbackImage?.let {
        var showContent by rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            delay(fallbackDelay)
            showContent = true
        }

        FadingVisibility(showContent) {
            ScreenMainImage(
                iconRes = fallbackImage,
                backgroundColor = WalletTheme.colorScheme.surfaceContainerLow,
                fillMaxWidth = 0.75f,
                paddingValues = PaddingValues(vertical = Sizes.s04)
            )
        }
    }
}
