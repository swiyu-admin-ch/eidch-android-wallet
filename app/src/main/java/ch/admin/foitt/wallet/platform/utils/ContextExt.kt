package ch.admin.foitt.wallet.platform.utils

import android.content.Context
import android.content.Context.ACCESSIBILITY_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.nfc.NfcAdapter
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.annotation.StringRes
import androidx.core.net.toUri
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.onFailure
import timber.log.Timber

fun Context.openSecuritySettings() {
    val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    if (intent.resolveActivity(packageManager) == null) {
        // Some phones do not have direct jump to security settings thus jump to settings
        intent.action = Settings.ACTION_SETTINGS
    }
    startActivity(intent)
}

fun Context.openAppDetailsSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
        data = Uri.fromParts("package", this@openAppDetailsSettings.packageName, null)
    }
    if (intent.resolveActivity(this.packageManager) == null) {
        // Some phones do not have direct jump to app settings thus jump to settings
        intent.action = Settings.ACTION_SETTINGS
    }
    startActivity(intent)
}

fun Context.openLink(@StringRes uriResource: Int) {
    val link = getString(uriResource)
    openLink(link)
}

fun Context.openLink(uri: String) {
    runSuspendCatching {
        val intent = Intent(Intent.ACTION_VIEW, uri.toUri())
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }.onFailure {
        Timber.w(t = it, message = "Could not open uri: $uri")
    }
}

fun Context.isScreenReaderOn(): Boolean {
    val manager = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager?
    return manager != null && manager.isEnabled && manager.isTouchExplorationEnabled
}

fun Context.hasNFCHardware(): Boolean =
    this.packageManager.hasSystemFeature(PackageManager.FEATURE_NFC) &&
        NfcAdapter.getDefaultAdapter(this) != null

fun Context.openNFCSettings() {
    runSuspendCatching {
        val intent = Intent(Settings.ACTION_NFC_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if (intent.resolveActivity(this.packageManager) == null) {
            // Some phones do not have direct jump to NFC settings thus jump to settings
            intent.action = Settings.ACTION_SETTINGS

            if (!hasNFCHardware()) {
                // Method got called on a device that has no NFC hardware
                Timber.w(message = "Try to open NFC settings on device that does not have hardware")
            } else {
                Timber.w(message = "Phone has no NFC settings shortcut")
            }
        }
        startActivity(intent)
    }.onFailure {
        Timber.w(t = it, message = "Exception while trying to open NFC settings")
    }
}

fun Context.shareText(
    title: String? = null,
    textContent: String,
    mimeType: String,
) {
    runSuspendCatching {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            title?.let {
                putExtra(Intent.EXTRA_TITLE, it)
            }
            putExtra(Intent.EXTRA_TEXT, textContent)
            type = mimeType
        }

        val shareIntent = Intent.createChooser(sendIntent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(shareIntent)
    }.onFailure {
        Timber.w(t = it, message = "Failed sharing text")
    }
}
