package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.credential.domain.model.RefreshDeferredCredentialsError
import ch.admin.foitt.wallet.platform.credential.domain.model.toRefreshDeferredCredentialsError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchAndUpdateDeferredCredential
import ch.admin.foitt.wallet.platform.credential.domain.usecase.RefreshDeferredCredentials
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState
import ch.admin.foitt.wallet.platform.ssi.domain.model.DeferredCredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.DeferredCredentialRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

class RefreshDeferredCredentialsImpl @Inject constructor(
    private val deferredCredentialRepository: DeferredCredentialRepository,
    private val fetchAndUpdateDeferredCredential: FetchAndUpdateDeferredCredential,
) : RefreshDeferredCredentials {
    override suspend fun invoke(): Result<Unit, RefreshDeferredCredentialsError> = coroutineBinding {
        Timber.d("Deferred refresh: starting refresh for all entries")

        val deferredCredentials = deferredCredentialRepository.getAll()
            .mapError(DeferredCredentialRepositoryError::toRefreshDeferredCredentialsError).bind()

        deferredCredentials.forEach { deferredCredential ->
            val deferredEntity = deferredCredential.deferredCredential
            if (!deferredEntity.shouldRefresh) {
                Timber.d("Deferred refresh: no refresh for ${deferredEntity.credentialId}, pollinterval ${deferredEntity.pollInterval}")
                return@forEach
            }
            fetchAndUpdateDeferredCredential(deferredCredential)
        }
    }

    private val DeferredCredentialEntity.shouldRefresh: Boolean
        get() = progressionState == DeferredProgressionState.IN_PROGRESS &&
            (polledAt ?: 0L) + pollInterval.toLong() <= Instant.now().epochSecond
}
