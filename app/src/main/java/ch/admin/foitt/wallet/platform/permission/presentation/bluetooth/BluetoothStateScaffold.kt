package ch.admin.foitt.wallet.platform.permission.presentation.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import ch.admin.foitt.wallet.platform.permission.domain.model.BluetoothState
import ch.admin.foitt.wallet.platform.permission.presentation.PermissionScaffold
import ch.admin.foitt.wallet.platform.utils.openAppDetailsSettings

@SuppressLint("InlinedApi")
@Composable
fun BluetoothStateScaffold(
    bluetoothState: State<BluetoothState>,
    modifier: Modifier = Modifier,
    onBluetoothStateChanged: () -> Unit = {},
    updateContentShown: (Boolean) -> Unit,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    val bluetoothEnableLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        onBluetoothStateChanged()
    }

    LaunchedEffect(Unit) {
        // Update bluetooth state when the screen is first composed, which is earlier than the LifecycleResumeEffect (this prevents flickering)
        onBluetoothStateChanged()
    }

    LifecycleResumeEffect(Unit) {
        // Update bluetooth state whenever the screen is resumed, to react to permission or enabled state changes
        onBluetoothStateChanged()
        onPauseOrDispose {}
    }

    DisposableEffect(Unit) {
        // Listen for bluetooth state changes while the scanner is running
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(localContext: Context?, intent: Intent?) {
                onBluetoothStateChanged()
            }
        }

        context.registerReceiver(
            receiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    PermissionScaffold(
        modifier = modifier,
        permissions = listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
        ),
        permissionRationaleContent = { onHandled ->
            updateContentShown(false)
            BluetoothPermissionRationalScreenContent(
                onOpenSettings = {
                    context.openAppDetailsSettings()
                    onHandled()
                },
            )
        },
        permissionNotGrantedContent = { handler ->
            updateContentShown(false)
            BluetoothPermissionNotGrantedScreenContent(
                onAllow = handler::requestPermission,
            )
        },
        permissionGrantedContent = {
            if (bluetoothState.value.isBluetoothEnabled) {
                updateContentShown(true)
                content()
            } else {
                updateContentShown(false)
                BluetoothDisabledScreenContent(
                    onEnableBluetooth = {
                        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        bluetoothEnableLauncher.launch(intent)
                    }
                )
            }
        },
        onPermissionGranted = onBluetoothStateChanged,
    )
}
