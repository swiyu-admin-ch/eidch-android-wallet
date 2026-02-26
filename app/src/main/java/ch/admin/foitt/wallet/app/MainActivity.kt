package ch.admin.foitt.wallet.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import ch.admin.foitt.wallet.app.presentation.MainScreen
import ch.admin.foitt.wallet.platform.navigation.domain.model.EntryProviderInstaller
import ch.admin.foitt.wallet.platform.userInteraction.domain.usecase.UserInteraction
import ch.admin.foitt.wallet.platform.utils.LocalIntent
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var userInteraction: UserInteraction

    private val currentIntent = mutableStateOf(Intent())

    /**
     * All [EntryProviderInstaller] are injected into a single Set.
     * See [ch.admin.foitt.wallet.app.di.EntryProviderInstallerModule] for an example how to define the Hilt Module
     */
    @Inject
    lateinit var entryProviderBuilders: Set<@JvmSuppressWildcards EntryProviderInstaller>

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.setRecentsScreenshotEnabled(false)
        }
        currentIntent.value = intent
        setContent {
            CompositionLocalProvider(
                LocalIntent provides currentIntent.value
            ) {
                MainScreen(
                    this,
                    entryProviderBuilders
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        currentIntent.value = intent
        Timber.d("onNewIntent: $intent, extras: ${intent.extras}")
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        userInteraction()
    }
}
