package ch.admin.foitt.wallet.feature.home.presentation.model

data class HomeContainerState(
    val showEIdRequestButton: Boolean,
    val showBetaIdRequestButton: Boolean,
    val showMenu: Boolean,
    val onGetEId: () -> Unit,
    val onGetBetaId: () -> Unit,
    val onSettings: () -> Unit,
    val onHelp: () -> Unit,
    val onScan: () -> Unit,
) {
    companion object {
        val EMPTY = HomeContainerState(
            showEIdRequestButton = true,
            showBetaIdRequestButton = true,
            showMenu = true,
            onGetEId = {},
            onGetBetaId = {},
            onSettings = {},
            onHelp = {},
            onScan = {},
        )
    }
}
