package ch.admin.foitt.wallet.platform.utils

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource

/**
 * ATTENTION: Use this class sparingly and use unwrapped resources if possible.
 *
 * Wrapper for strings meant to be used in the UI.
 * This can be used in components where you don't have access to [Context].
 *
 * Use [unwrap] to acquire the value, when you have access to [Context].
 */
sealed class UiString {
    data class Dynamic(val value: String) : UiString()

    class Resource(
        @param:StringRes val resId: Int,
        vararg val args: Any,
    ) : UiString() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Resource) return false
            return resId == other.resId && args.contentEquals(other.args)
        }

        override fun hashCode(): Int = 31 * resId + args.contentHashCode()
    }

    fun unwrap(context: Context): String = when (this) {
        is Dynamic -> value
        is Resource -> {
            if (args.isEmpty()) {
                context.getString(resId)
            } else {
                @Suppress("SpreadOperator")
                context.getString(resId, *args)
            }
        }
    }

    @Composable
    @ReadOnlyComposable
    fun unwrap(): String = when (this) {
        is Dynamic -> value
        is Resource -> {
            if (args.isEmpty()) {
                stringResource(resId)
            } else {
                @Suppress("SpreadOperator")
                stringResource(resId, *args)
            }
        }
    }
}
