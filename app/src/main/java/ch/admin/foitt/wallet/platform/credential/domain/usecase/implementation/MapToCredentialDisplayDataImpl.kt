package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import android.content.Context
import android.os.Build
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.usecase.GetActorEnvironment
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.MapToCredentialDisplayDataError
import ch.admin.foitt.wallet.platform.credential.domain.model.getDisplayStatus
import ch.admin.foitt.wallet.platform.credential.domain.usecase.MapToCredentialDisplayData
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedAndThemedDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.model.BundleItemRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toMapToCredentialDisplayDataError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemRepository
import ch.admin.foitt.wallet.platform.theme.domain.model.Theme
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MapToCredentialDisplayDataImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val getLocalizedAndThemedDisplay: GetLocalizedAndThemedDisplay,
    private val getActorEnvironment: GetActorEnvironment,
    private val bundleItemRepository: BundleItemRepository,
) : MapToCredentialDisplayData {
    override suspend fun invoke(
        verifiableCredential: VerifiableCredentialEntity,
        credentialDisplays: List<CredentialDisplay>,
        claims: List<CredentialClaimWithDisplays>,
    ): Result<CredentialDisplayData, MapToCredentialDisplayDataError> = coroutineBinding {
        val credentialDisplay = getDisplay(credentialDisplays).bind()

        val resolvedDisplay = credentialDisplay.resolveTemplate(claims)

        val bundleItems = bundleItemRepository.getAllByCredentialId(verifiableCredential.credentialId)
            .mapError(BundleItemRepositoryError::toMapToCredentialDisplayDataError)
            .bind()

        val status = runSuspendCatching {
            bundleItems.first().status
        }.mapError {
            CredentialError.Unexpected(it)
        }.bind()

        CredentialDisplayData(
            credentialId = verifiableCredential.credentialId,
            status = verifiableCredential.getDisplayStatus(status),
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

    private fun CredentialDisplay.resolveTemplate(claims: List<CredentialClaimWithDisplays>): CredentialDisplay {
        val description = this.description ?: ""

        // matches must start with: {{$.
        // contain 1 to n: word characters, dots, [, and ]
        // end with: }}
        val jsonPathFinder = Regex("""\{\{(\$\.[\w.\[\]]+?)\}\}""")
        val matches = jsonPathFinder.findAll(description).map { match ->
            val matchText = match.groupValues[1]
            val range = match.range
            Pair(matchText, range)
        }.toList()

        var resolvedField = description
        matches.reversed().forEach { (text, range) ->
            val claim = claims.find { text == "$.${it.claim.key}" }?.claim
            val replacement = claim?.let { it.value ?: "–" } ?: ""
            resolvedField = resolvedField.replaceRange(range, replacement)
        }

        return this.copy(description = resolvedField)
    }
}
