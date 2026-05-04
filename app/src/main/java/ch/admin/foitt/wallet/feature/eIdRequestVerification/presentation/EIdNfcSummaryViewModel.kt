package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.GetStartAutoVerificationResult
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber

@HiltViewModel(assistedFactory = EIdNfcSummaryViewModel.Factory::class)
class EIdNfcSummaryViewModel @AssistedInject constructor(
    private val getStartAutoVerificationResult: GetStartAutoVerificationResult,
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState,
    @Assisted("caseId") private val caseId: String,
    @Assisted internal val picture: ByteArray,
    @Assisted("givenName") internal val givenName: String,
    @Assisted("surname") internal val surname: String,
    @Assisted("documentId") internal val documentId: String,
    @Assisted("expiryDate") internal val expiryDate: String,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.WithCloseButton(::onClose)

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("caseId") caseId: String,
            picture: ByteArray,
            @Assisted("givenName") givenName: String,
            @Assisted("surname") surname: String,
            @Assisted("documentId") documentId: String,
            @Assisted("expiryDate") expiryDate: String,
        ): EIdNfcSummaryViewModel
    }

    fun onContinue() {
        val startAutoVerificationResult = getStartAutoVerificationResult().value

        when {
            startAutoVerificationResult == null -> {
                Timber.e("Nfc Summary: Start auto verification result is null")
                navManager.replaceCurrentWith(
                    Destination.EIdStartSelfieVideoScreen(caseId = caseId)
                )
            }

            startAutoVerificationResult.recordDocumentVideo -> navManager.replaceCurrentWith(
                Destination.EIdDocumentRecordingInfoScreen(caseId = caseId)
            )

            else -> navManager.replaceCurrentWith(
                Destination.EIdStartSelfieVideoScreen(caseId = caseId)
            )
        }
    }

    private fun onClose() {
        navManager.navigateBackToHomeScreen(popUntil = Destination.EIdNfcSummaryScreen::class)
    }
}
