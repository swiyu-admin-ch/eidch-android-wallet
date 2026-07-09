package ch.admin.foitt.wallet.platform.composables.presentation.adapter.implementation

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetColor
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.getOrElse
import javax.inject.Inject

internal class GetColorImpl @Inject constructor() : GetColor {
    override fun invoke(colorString: String?): Color? = runSuspendCatching {
        colorString?.let {
            Color(colorString.toColorInt())
        }
    }.getOrElse { null }
}
