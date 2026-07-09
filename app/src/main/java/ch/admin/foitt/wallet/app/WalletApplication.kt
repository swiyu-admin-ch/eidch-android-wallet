package ch.admin.foitt.wallet.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log.ERROR
import android.util.Log.WARN
import ch.admin.foitt.wallet.BuildConfig
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.eventTracking.domain.usecase.ReportError
import ch.admin.foitt.wallet.platform.pushNotification.data.WalletFirebaseMessagingService
import ch.ubique.heidi.proximity.HeidiProximity
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class WalletApplication : Application() {
    @Inject
    lateinit var reportError: ReportError

    override fun onCreate() {
        super.onCreate()
        setupLogging()
        setupNotificationChannels()

        HeidiProximity(this).initialize()
    }

    private fun setupLogging() {
        val trees = mutableListOf<Timber.Tree>(
            // Dynatrace tree
            object : Timber.Tree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    when (priority) {
                        ERROR, WARN -> reportError(message, t)
                    }
                }
            }
        )

        // debug log tree
        if (BuildConfig.DEBUG) {
            trees.add(
                object : Timber.DebugTree() {
                    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                        super.log(priority, "sw_$tag", message, t)
                    }
                }
            )
        }

        trees.forEach { tree ->
            tree
                .takeIf { it !in Timber.forest() }
                ?.apply { Timber.plant(this) }
        }
    }

    private fun setupNotificationChannels() {
        val channel = NotificationChannel(
            WalletFirebaseMessagingService.PUSH_CHANNEL_EID,
            getString(R.string.tk_pushNotification_eid_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.createNotificationChannel(channel)
    }
}
