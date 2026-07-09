package ch.admin.foitt.wallet.platform.pushNotification.data

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WalletFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var getCurrentAppLocale: GetCurrentAppLocale

    override fun onNewToken(token: String) {
        // We update the token after login. Here we do not have access to the database
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val lang = getCurrentAppLocale().language
        val title = message.data["title#$lang"] ?: message.data["title#en"] ?: return
        val body = message.data["body#$lang"] ?: message.data["body#en"] ?: return
        val messageType = message.data["type"] ?: ""
        val notificationId = notificationIdFromMessageType(messageType)

        val intent = buildNotificationIntent()
        val notification = NotificationCompat.Builder(this, PUSH_CHANNEL_EID)
            .setSmallIcon(R.drawable.wallet_ic_swiss_cross)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(intent)
            .build()

        var permissionGranted = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionGranted = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }

        if (permissionGranted) {
            NotificationManagerCompat.from(this).notify(notificationId, notification)
        }
    }

    private fun buildNotificationIntent(): PendingIntent {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun notificationIdFromMessageType(messageType: String): Int {
        return when (messageType) {
            "NEW_MESSAGE" -> NEW_MESSAGE_ID
            "NEW_ACTION" -> NEW_ACTION_ID
            "NEW_VC" -> NEW_VC_ID
            else -> DEFAULT_NOTIFICATION_ID
        }
    }

    companion object {
        private const val DEFAULT_NOTIFICATION_ID = 1000
        private const val NEW_MESSAGE_ID = 1001
        private const val NEW_ACTION_ID = 1002
        private const val NEW_VC_ID = 1003

        const val PUSH_CHANNEL_EID = "wallet_push_channel_eid"
    }
}
