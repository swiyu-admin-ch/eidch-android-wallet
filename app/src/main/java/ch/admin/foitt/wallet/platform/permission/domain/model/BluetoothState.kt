package ch.admin.foitt.wallet.platform.permission.domain.model

data class BluetoothState(
    val isBluetoothPermissionGranted: Boolean = false,
    val isBluetoothEnabled: Boolean = false,
) {
    fun isReady() = isBluetoothPermissionGranted && isBluetoothEnabled
}
