package ch.admin.foitt.wallet.platform.scaffold.presentation

import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.platform.scaffold.domain.model.SystemBarsSetter
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.theme.DefaultLightScrim
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

abstract class ScreenViewModel(
    private val setTopBarState: SetTopBarState,
    private val areBaseSystemBarsInverted: Boolean = false,
    var systemBarsFixedLightColor: Boolean = false,
) : ViewModel() {
    protected abstract val topBarState: TopBarState

    fun syncScaffoldState(
        systemBarsSetter: SystemBarsSetter,
        isInDarkTheme: Boolean,
    ) {
        setTopBarState(topBarState)
        syncSystemBars(
            systemBarsSetter,
            isInDarkTheme,
        )
    }

    private fun syncSystemBars(
        systemBarsSetter: SystemBarsSetter,
        isInDarkTheme: Boolean,
    ) {
        val systemBarsStyle = when {
            systemBarsFixedLightColor -> SystemBarStyle.dark(Color.TRANSPARENT)
            areBaseSystemBarsInverted && isInDarkTheme -> SystemBarStyle.light(Color.TRANSPARENT, DefaultLightScrim.toArgb())
            areBaseSystemBarsInverted && !isInDarkTheme -> SystemBarStyle.dark(Color.TRANSPARENT)
            !areBaseSystemBarsInverted && isInDarkTheme -> SystemBarStyle.dark(Color.TRANSPARENT)
            else -> SystemBarStyle.light(Color.TRANSPARENT, DefaultLightScrim.toArgb())
        }

        systemBarsSetter(
            statusBarStyle = systemBarsStyle,
            navigationBarStyle = systemBarsStyle,
        )
    }

    protected fun <T> Flow<T>.toStateFlow(initialValue: T, timeout: Long = 5000) = this.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(timeout),
        initialValue,
    )
}
