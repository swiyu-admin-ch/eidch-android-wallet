package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import android.content.Context
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.model.AttestationUiState
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.model.AttestationUiState.IntegrityError
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.model.AttestationUiState.IntegrityNetworkError
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.model.AttestationUiState.InvalidClientAttestation
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.model.AttestationUiState.InvalidKeyAttestation
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.model.AttestationUiState.NetworkError
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.model.AttestationUiState.Unexpected
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.KeyAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.RequestClientAttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.RequestKeyAttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.RequestClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.RequestKeyAttestation
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.ValidateAttestationsError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.ValidateAttestations
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openLink
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import com.github.michaelbull.result.get
import com.github.michaelbull.result.unwrapError
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
internal class EIdAttestationViewModel @Inject constructor(
    private val requestClientAttestation: RequestClientAttestation,
    private val requestKeyAttestation: RequestKeyAttestation,
    private val validateAttestations: ValidateAttestations,
    private val navManager: NavigationManager,
    @param:ApplicationContext private val context: Context,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.Empty

    private val isLoading = MutableStateFlow(false)

    private val keyAttestationResult = MutableStateFlow<Result<KeyAttestation, RequestKeyAttestationError>?>(null)
    private val clientAttestationResult =
        MutableStateFlow<Result<ClientAttestation, RequestClientAttestationError>?>(null)
    private val sIdValidationResult = MutableStateFlow<Result<Unit, ValidateAttestationsError>?>(null)

    val attestationState: StateFlow<AttestationUiState> = combine(
        isLoading,
        keyAttestationResult,
        clientAttestationResult,
        sIdValidationResult,
    ) { isLoading, keyAttestation, clientAttestation, sidValidation ->
        when {
            isLoading -> AttestationUiState.Loading
            sidValidation?.isOk == true -> AttestationUiState.Valid
            sidValidation?.isErr == true -> sidValidation.unwrapError().toUiState()
            keyAttestation?.isErr == true && keyAttestation.unwrapError() is AttestationError.NetworkError ->
                NetworkError(
                    onClose = ::onClose,
                    onRetry = ::onRetry,
                )

            clientAttestation?.isErr == true && clientAttestation.unwrapError() is AttestationError.NetworkError ->
                IntegrityNetworkError(
                    onClose = ::onClose,
                    onRetry = ::onRetry,
                )

            clientAttestation?.isErr == true && clientAttestation.unwrapError() is AttestationError.Unexpected ->
                IntegrityError(
                    onClose = ::onClose,
                )

            else -> Unexpected(
                onClose = ::onClose,
                onRetry = ::onRetry,
            )
        }
    }.toStateFlow(AttestationUiState.Loading)

    @OptIn(UnsafeResultValueAccess::class)
    fun onRefreshState() {
        if (isLoading.value) {
            return
        }
        viewModelScope.launch {
            if (keyAttestationResult.value?.isErr == true) {
                keyAttestationResult.value = null
            }

            if (sIdValidationResult.value?.isErr == true) {
                sIdValidationResult.value = null
            }

            if (clientAttestationResult.value?.isErr == true) {
                clientAttestationResult.value = null
            }

            val currentClientAttestation = clientAttestationResult.value ?: requestClientAttestation()
            clientAttestationResult.value = currentClientAttestation
            Timber.d(message = "Client attestation result: ${clientAttestationResult.value}")

            val currentKeyAttestation = keyAttestationResult.value ?: requestKeyAttestation()
            keyAttestationResult.value = currentKeyAttestation
            Timber.d(message = "Key Attestation result: ${currentKeyAttestation.get()}")

            if (currentClientAttestation.isOk && currentKeyAttestation.isOk) {
                val currentSIdValidation = sIdValidationResult.value ?: validateAttestations(
                    clientAttestation = currentClientAttestation.value,
                    keyAttestation = currentKeyAttestation.value,
                )
                sIdValidationResult.value = currentSIdValidation

                Timber.d(message = "Validation result: ${sIdValidationResult.value}")
                if (currentSIdValidation.isOk) {
                    navManager.replaceCurrentWith(Destination.EIdGuardianshipScreen)
                }
            }
        }.trackCompletion(isLoading)
    }

    init {
        onRefreshState()
    }

    fun onClose() = navManager.popBackStackOrToRoot()

    fun onHelp() = context.openLink(context.getString(R.string.tk_eidRequest_attestation_helpLink_url))

    fun onRetry() {
        onRefreshState()
    }

    fun onPlaystore() = context.openLink(context.getString(R.string.tk_eidRequest_attestation_clientNotSupported_button_playstore_url))

    private fun ValidateAttestationsError.toUiState(): AttestationUiState = when (this) {
        EIdRequestError.InvalidKeyAttestation,
        EIdRequestError.InsufficientKeyStorageResistance -> InvalidKeyAttestation(
            onClose = ::onClose,
            onHelp = ::onHelp,
        )

        EIdRequestError.InvalidClientAttestation -> InvalidClientAttestation(
            onClose = ::onClose,
            onHelp = ::onHelp,
            onPlaystore = ::onPlaystore,
        )

        EIdRequestError.NetworkError -> NetworkError(
            onClose = ::onClose,
            onRetry = ::onRetry,
        )

        is EIdRequestError.Unexpected -> Unexpected(
            onClose = ::onClose,
            onRetry = ::onRetry,
        )
    }
}
