package ch.admin.foitt.wallet.platform.composables.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun LottieIcon(
    @RawRes animationRes: Int,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    @DrawableRes fallbackImage: Int? = null,
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
            modifier = modifier,
            contentScale = contentScale,
            alignment = Alignment.Center,
        )
    } ?: fallbackImage?.let {
        Image(
            painter = painterResource(id = fallbackImage),
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}
