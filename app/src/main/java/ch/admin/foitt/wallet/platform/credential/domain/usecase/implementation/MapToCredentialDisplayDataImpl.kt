package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import android.content.Context
import android.os.Build
import ch.admin.foitt.openid4vc.domain.model.anycredential.toBusinessExpiryInstant
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwt
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.usecase.GetActorEnvironment
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.MapToCredentialDisplayDataError
import ch.admin.foitt.wallet.platform.credential.domain.model.getDisplayStatus
import ch.admin.foitt.wallet.platform.credential.domain.usecase.MapToCredentialDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.usecase.ResolveClaimTemplate
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedAndThemedDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithKeyBindingRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toMapToCredentialDisplayDataError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialWithBundleItemsWithKeyBindingRepository
import ch.admin.foitt.wallet.platform.theme.domain.model.Theme
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject

class MapToCredentialDisplayDataImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val getLocalizedAndThemedDisplay: GetLocalizedAndThemedDisplay,
    private val resolveClaimTemplate: ResolveClaimTemplate,
    private val verifiableCredentialWithBundleItemsWithKeyBindingRepository: VerifiableCredentialWithBundleItemsWithKeyBindingRepository,
    private val getActorEnvironment: GetActorEnvironment,
) : MapToCredentialDisplayData {
    override suspend fun invoke(
        verifiableCredential: VerifiableCredentialEntity,
        credentialDisplays: List<CredentialDisplay>,
        claims: List<CredentialClaimWithDisplays>,
        credentialFormat: CredentialFormat
    ): Result<CredentialDisplayData, MapToCredentialDisplayDataError> = coroutineBinding {
        val credentialDisplay = getDisplay(credentialDisplays).bind()

        val resolvedDisplay = credentialDisplay.description?.let {
            val description = resolveClaimTemplate(template = it, claims = claims)
            credentialDisplay.copy(description = description)
        } ?: credentialDisplay

        val verifiableCredentialWithBundleItemsWithKeyBinding = verifiableCredentialWithBundleItemsWithKeyBindingRepository
            .getByCredentialId(verifiableCredential.credentialId)
            .mapError(CredentialWithKeyBindingRepositoryError::toMapToCredentialDisplayDataError)
            .bind()
        val status = verifiableCredentialWithBundleItemsWithKeyBinding.nextBundleItemToPresent.status
        CredentialDisplayData(
            credentialId = verifiableCredential.credentialId,
            status = verifiableCredential.getDisplayStatus(status, determineBusinessExpiryInstant(credentialFormat, claims)),
            credentialDisplay = resolvedDisplay,
            progressionState = verifiableCredential.progressionState,
            actorEnvironment = getActorEnvironment(verifiableCredential.issuer)
        )
    }

    private fun getDisplay(displays: List<CredentialDisplay>): Result<CredentialDisplay, MapToCredentialDisplayDataError> {
        val currentTheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && context.resources.configuration.isNightModeActive) {
            Theme.DARK
        } else {
            Theme.LIGHT
        }
        return getLocalizedAndThemedDisplay(
            credentialDisplays = displays,
            preferredTheme = currentTheme,
        )?.let { Ok(it) }
            ?: Err(CredentialError.Unexpected(IllegalStateException("No localized display found")))
    }

    private fun determineBusinessExpiryInstant(credentialFormat: CredentialFormat, claims: List<CredentialClaimWithDisplays>): Instant? {
        return when (credentialFormat) {
            CredentialFormat.VC_SD_JWT -> claims.firstOrNull { it.claim.path == VcSdJwt.BUSINESS_EXPIRY_DATE_CLAIM_PATH }
                ?.claim
                ?.value
                ?.toBusinessExpiryInstant()

            else -> {
                null
            }
        }
    }
}
