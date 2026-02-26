package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import android.content.Context
import android.os.Build
import ch.admin.foitt.wallet.platform.credential.domain.model.DeferredCredentialDisplayData
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialWithDisplays
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedAndThemedDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithDisplaysRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.GetCredentialsWithDetailsFlowError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toGetCredentialsWithDetailsFlowError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.DeferredCredentialWithDisplaysRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetDeferredCredentialsWithDetailsFlow
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

class GetDeferredCredentialsWithDetailsFlowImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val deferredCredentialWithDisplaysRepository: DeferredCredentialWithDisplaysRepository,
    private val getLocalizedAndThemedDisplay: GetLocalizedAndThemedDisplay,
) : GetDeferredCredentialsWithDetailsFlow {
    override suspend operator fun invoke(): Flow<Result<List<DeferredCredentialDisplayData>, GetCredentialsWithDetailsFlowError>> =
        deferredCredentialWithDisplaysRepository.getAllFlow()
            .mapError(CredentialWithDisplaysRepositoryError::toGetCredentialsWithDetailsFlowError)
            .andThen { deferredCredentials ->
                coroutineBinding {
                    deferredCredentials.toDisplayData().bind()
                }
            }

    private suspend fun List<DeferredCredentialWithDisplays>.toDisplayData():
        Result<List<DeferredCredentialDisplayData>, GetCredentialsWithDetailsFlowError> = coroutineBinding {
        map { deferredCredentialWithDisplays ->
            val display = getDisplay(deferredCredentialWithDisplays.credentialDisplays).bind()

            DeferredCredentialDisplayData(
                credentialId = deferredCredentialWithDisplays.deferredCredential.credentialId,
                credentialDisplay = display,
                status = deferredCredentialWithDisplays.deferredCredential.progressionState,
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
