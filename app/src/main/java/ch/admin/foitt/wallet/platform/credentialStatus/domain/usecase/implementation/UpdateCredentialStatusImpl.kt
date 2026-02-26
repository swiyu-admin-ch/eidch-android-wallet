package ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.Validity
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.wallet.platform.credential.domain.model.GetAllAnyCredentialsByCredentialIdError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAllAnyCredentialsByCredentialId
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusProperties
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.FetchCredentialStatusError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.UpdateCredentialStatusError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.toUpdateCredentialStatusError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.FetchCredentialStatus
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.UpdateCredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.ssi.domain.model.BundleItemRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.VerifiableCredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialRepository
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.get
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class UpdateCredentialStatusImpl @Inject constructor(
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val verifiableCredentialRepository: VerifiableCredentialRepository,
    private val bundleItemRepository: BundleItemRepository,
    private val getAllAnyCredentialsByCredentialId: GetAllAnyCredentialsByCredentialId,
    private val fetchCredentialStatus: FetchCredentialStatus,
    private val safeJson: SafeJson,
) : UpdateCredentialStatus {

    override suspend fun invoke(credentialId: Long): Result<Unit, UpdateCredentialStatusError> = withContext(ioDispatcher) {
        coroutineBinding {
            val anyCredentials = getAllAnyCredentialsByCredentialId(credentialId)
                .mapError(GetAllAnyCredentialsByCredentialIdError::toUpdateCredentialStatusError)
                .bind()
            anyCredentials.forEach { anyCredential ->
                if (anyCredential.validity == Validity.Valid) {
                    checkStatusList(credentialId, anyCredential).bind()
                } else {
                    // No point in getting the status, local validity has precedence for now.
                    Timber.d("Try to update status of invalid credential with id: ${anyCredential.id}")
                }
            }
        }
    }

    private suspend fun checkStatusList(
        credentialId: Long,
        anyCredential: AnyCredential
    ): Result<Unit, UpdateCredentialStatusError> = coroutineBinding {
        val issuer = anyCredential.issuer
        val properties = runSuspendCatching {
            anyCredential.parseStatusProperties()
        }.mapError { throwable ->
            throwable.toUpdateCredentialStatusError("Error while updating credential, more info: ${throwable.message}")
        }.bind()

        if (issuer.isBlank() || properties == null) {
            Timber.w("Credential does not have an issuer and/or any status to check")
            return@coroutineBinding
        }

        val status = fetchCredentialStatus(issuer, properties)
            .mapError(FetchCredentialStatusError::toUpdateCredentialStatusError)
            .bind()
        if (status != CredentialStatus.UNKNOWN) {
            updateCredentialStatus(credentialId, status).bind()
        }
    }

    private suspend fun updateCredentialStatus(
        credentialId: Long,
        newStatus: CredentialStatus
    ): Result<Int, UpdateCredentialStatusError> = coroutineBinding {
        verifiableCredentialRepository.onBundleItemUpdate(credentialId)
            .mapError(VerifiableCredentialRepositoryError::toUpdateCredentialStatusError)
            .bind()
        bundleItemRepository.updateStatusByCredentialId(credentialId, newStatus)
            .mapError(BundleItemRepositoryError::toUpdateCredentialStatusError)
            .bind()
    }

    private fun AnyCredential.parseStatusProperties() = when (this.format) {
        CredentialFormat.VC_SD_JWT -> {
            (this as VcSdJwtCredential).status?.let {
                safeJson.safeDecodeElementTo<CredentialStatusProperties>(it).get()
            }
        }

        else -> null
    }
}
