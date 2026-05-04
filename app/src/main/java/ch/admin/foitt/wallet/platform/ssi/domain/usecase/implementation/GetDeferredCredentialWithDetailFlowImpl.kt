package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import android.content.Context
import android.os.Build
import ch.admin.foitt.wallet.platform.credential.domain.model.DeferredCredentialDisplayData
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedAndThemedDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithDisplaysRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.GetCredentialsWithDetailsFlowError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toGetCredentialsWithDetailsFlowError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.DeferredCredentialWithDisplaysRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetDeferredCredentialWithDetailFlow
import ch.admin.foitt.wallet.platform.theme.domain.model.Theme
import ch.admin.foitt.wallet.platform.utils.andThen
import ch.admin.foitt.wallet.platform.utils.mapError
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDeferredCredentialWithDetailFlowImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val deferredCredentialWithDisplaysRepository: DeferredCredentialWithDisplaysRepository,
    private val getLocalizedAndThemedDisplay: GetLocalizedAndThemedDisplay,
) : GetDeferredCredentialWithDetailFlow {
    override suspend operator fun invoke(
        credentialId: Long
    ): Flow<Result<DeferredCredentialDisplayData, GetCredentialsWithDetailsFlowError>> =
        deferredCredentialWithDisplaysRepository.getByIdFlow(credentialId)
            .mapError(CredentialWithDisplaysRepositoryError::toGetCredentialsWithDetailsFlowError)
            .andThen { deferredCredential ->
                coroutineBinding {
                    val display = getDisplay(deferredCredential.credentialDisplays).bind()

                    DeferredCredentialDisplayData(
                        credentialId = deferredCredential.deferredCredential.credentialId,
                        credentialDisplay = display,
                        status = deferredCredential.deferredCredential.progressionState,
                    )
                }
            }

    private fun getDisplay(displays: List<CredentialDisplay>): Result<CredentialDisplay, GetCredentialsWithDetailsFlowError> {
        val currentTheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && context.resources.configuration.isNightModeActive) {
            Theme.DARK
        } else {
            Theme.LIGHT
        }

        return getLocalizedAndThemedDisplay(
            credentialDisplays = displays,
            preferredTheme = currentTheme,
        )?.let { Ok(it) }
            ?: Err(SsiError.Unexpected(IllegalStateException("No localized display found")))
    }
}
