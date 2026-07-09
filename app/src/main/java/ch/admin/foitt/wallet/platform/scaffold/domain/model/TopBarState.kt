package ch.admin.foitt.wallet.platform.scaffold.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.utils.UiString

sealed interface TopBarState {
    data object None : TopBarState
    data object Empty : TopBarState

    data class DetailsWithCloseButton(
        val onUp: () -> Unit,
        @param:StringRes
        val titleId: Int?,
        val topBarBackground: TopBarBackground = TopBarBackground.TRANSPARENT,
        val onClose: () -> Unit,
    ) : TopBarState

    data class DetailsWithCloseRoundButtons(
        val onUp: () -> Unit,
        @param:StringRes
        val titleId: Int?,
        val topBarBackground: TopBarBackground = TopBarBackground.TRANSPARENT,
        val onClose: () -> Unit,
    ) : TopBarState

    data class Details(
        val onUp: () -> Unit,
        @param:StringRes val titleId: Int?,
        @param:StringRes val titleAltTextId: Int? = null,
        val topBarBackground: TopBarBackground = TopBarBackground.TRANSPARENT,
        val onAXDown: (() -> Unit)? = null
    ) : TopBarState

    data class WithCloseButton(
        val onClose: () -> Unit,
        @param:StringRes val titleId: Int? = null,
        @param:StringRes val titleAltTextId: Int? = null,
        val topBarBackground: TopBarBackground = TopBarBackground.TRANSPARENT,
    ) : TopBarState

    data class OnGradient(
        val onUp: () -> Unit,
        @param:StringRes
        val titleId: Int,
        val onAXDown: (() -> Unit)? = null
    ) : TopBarState

    /*
     * Highly customizable TopBarState variant that maps to a M3 TopAppBar.
     * Continuing, this could be extended with more styling and few variants
     * e.g. for CenterAlignedTopAppBar
     */
    data class Custom(
        val title: UiString? = null,
        val titleAltText: UiString? = null,
        val titleMaxLines: Int = Int.MAX_VALUE,
        val onUp: (() -> Unit)? = null,
        val actions: List<TopBarAction> = emptyList(),
        val topBarBackground: TopBarBackground = TopBarBackground.TRANSPARENT,
        val useFilledButtons: Boolean = false,
        val onAXDown: (() -> Unit)? = null
    ) : TopBarState
}

open class TopBarAction(
    val onClick: () -> Unit,
    @param:DrawableRes val icon: Int,
    @param:StringRes val contentDescription: Int? = null,
) {
    class Close(
        val onClose: () -> Unit,
    ) : TopBarAction(
        onClick = onClose,
        icon = R.drawable.wallet_ic_cross,
        contentDescription = R.string.tk_global_close_alt,
    )
}
