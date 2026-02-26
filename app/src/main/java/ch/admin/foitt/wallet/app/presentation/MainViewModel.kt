package ch.admin.foitt.wallet.app.presentation

import android.content.Intent
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import ch.admin.foitt.wallet.BuildConfig
import ch.admin.foitt.wallet.feature.login.domain.usecase.LockTrigger
import ch.admin.foitt.wallet.feature.sessionTimeout.domain.SessionTimeoutNavigation
import ch.admin.foitt.wallet.feature.sessionTimeout.domain.UserInteractionFlow
import ch.admin.foitt.wallet.platform.appLifecycleRepository.domain.model.AppLifecycleState
import ch.admin.foitt.wallet.platform.appLifecycleRepository.domain.usecase.GetAppLifecycleState
import ch.admin.foitt.wallet.platform.database.domain.usecase.CloseAppDatabase
import ch.admin.foitt.wallet.platform.deeplink.domain.usecase.SetDeepLinkIntent
import ch.admin.foitt.wallet.platform.di.IoDispatcherScope
import ch.admin.foitt.wallet.platform.login.domain.usecase.AfterLoginWork
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val lockTriggerUseCase: LockTrigger,
    private val userInteractionFlowUseCase: UserInteractionFlow,
    private val sessionTimeoutNavigation: SessionTimeoutNavigation,
    private val getAppLifecycleState: GetAppLifecycleState,
    private val afterLoginWork: AfterLoginWork,
    private val closeAppDatabase: CloseAppDatabase,
    private val setDeepLinkIntent: SetDeepLinkIntent,
    private val navManager: NavigationManager,
    @param:IoDispatcherScope private val ioDispatcherScope: CoroutineScope,
) : ViewModel() {
    private var lockTriggerJob: Job? = null
    private var sessionTimeoutJob: Job? = null
    private var appLifecycleJob: Job? = null
    private var afterLoginWorkJob: Job? = null

    private val countdown = object : CountDownTimer(SESSION_TIMEOUT, 1000) {
        @Suppress("EmptyFunctionBlock")
        override fun onTick(millisUntilFinished: Long) {
        }

        override fun onFinish() {
            ioDispatcherScope.launch {
                withContext(Dispatchers.Main) {
                    sessionTimeoutNavigation()?.let { direction ->
                        navManager.navigateTo(direction)
                    }
                }
            }
        }
    }

    fun getBackStack() = navManager.backstack

    fun handleIntent(activity: ComponentActivity) {
        val intent = activity.intent
        Timber.d("Intent received,\naction: ${intent.action},\ndata: ${intent.dataString}")
        if (intent.isDeeplinkIntent) {
            intent.dataString?.let { intentDataString ->
                setDeepLinkIntent(intentDataString)
                // clear intent to avoid re-handling it on activity recreation
                activity.intent = Intent()
            } ?: Timber.w("Intent dataString null")
        } else {
            Timber.d("Intent considered useless $intent")
        }
    }

    private val Intent.isDeeplinkIntent
        get() =
            action == Intent.ACTION_VIEW && (scheme.isOfferScheme() || scheme.isPresentationScheme()) && dataString != null

    private fun String?.isOfferScheme() = this == BuildConfig.SCHEME_CREDENTIAL_OFFER || this == BuildConfig.SCHEME_CREDENTIAL_OFFER_SWIYU
    private fun String?.isPresentationScheme() = this == BuildConfig.SCHEME_PRESENTATION_REQUEST ||
        this == BuildConfig.SCHEME_PRESENTATION_REQUEST_SWIYU || this == BuildConfig.SCHEME_PRESENTATION_REQUEST_OID

    init {
        Timber.d("MainViewModel initialized")
        lockTriggerJob = ioDispatcherScope.launch {
            lockTriggerUseCase().collect { newNavigationAction ->
                withContext(Dispatchers.Main) {
                    newNavigationAction.navigate()
                }
            }
        }

        appLifecycleJob = ioDispatcherScope.launch {
            getAppLifecycleState().collect { state ->
                when (state) {
                    AppLifecycleState.Foreground -> setupSessionTimeout()
                    AppLifecycleState.Background -> cancelSessionTimeout()
                }
            }
        }

        setupSessionTimeout()

        afterLoginWorkJob = ioDispatcherScope.launch {
            afterLoginWork()
        }
    }

    private fun setupSessionTimeout() {
        if (sessionTimeoutJob == null) {
            sessionTimeoutJob = ioDispatcherScope.launch {
                userInteractionFlowUseCase().collect { _ ->
                    countdown.cancel()
                    countdown.start()
                }
            }
        }
    }

    private fun cancelSessionTimeout() {
        countdown.cancel()
        sessionTimeoutJob?.cancel()
        sessionTimeoutJob = null
    }

    override fun onCleared() {
        afterLoginWorkJob?.cancel()
        afterLoginWorkJob = null

        ioDispatcherScope.launch {
            closeAppDatabase()
        }
        lockTriggerJob?.cancel()
        lockTriggerJob = null

        cancelSessionTimeout()

        appLifecycleJob?.cancel()
        appLifecycleJob = null

        Timber.d("MainViewModel cleared")
        super.onCleared()
    }

    companion object {
        private const val SESSION_TIMEOUT = 2 * 60 * 1000L
    }
}
