package ch.admin.foitt.wallet.platform.permission.presentation.bluetooth

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import ch.admin.foitt.wallet.platform.permission.domain.model.BluetoothState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BluetoothPermissionViewModel(setTopBarState: SetTopBarState) : ScreenViewModel(setTopBarState) {

    private val bluetoothStateMutable = MutableStateFlow(BluetoothState())
    val bluetoothState = bluetoothStateMutable.asStateFlow()

    fun updateBluetoothState(context: Context) {
        val isPermissionGranted = context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val isBluetoothEnabled = bluetoothManager?.adapter?.isEnabled ?: false

        bluetoothStateMutable.value = BluetoothState(isPermissionGranted, isBluetoothEnabled)
    }
}
