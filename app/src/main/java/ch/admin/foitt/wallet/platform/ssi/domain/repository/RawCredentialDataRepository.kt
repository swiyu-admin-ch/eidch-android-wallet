package ch.admin.foitt.wallet.platform.ssi.domain.repository

import ch.admin.foitt.wallet.platform.database.domain.model.RawCredentialData
import ch.admin.foitt.wallet.platform.ssi.domain.model.RawCredentialDataRepositoryError
import com.github.michaelbull.result.Result

interface RawCredentialDataRepository {
    suspend fun getByCredentialId(credentialId: Long): Result<RawCredentialData, RawCredentialDataRepositoryError>
    suspend fun updateMetadataByCredentialId(credentialId: Long, metadata: ByteArray): Result<Int, RawCredentialDataRepositoryError>
}
