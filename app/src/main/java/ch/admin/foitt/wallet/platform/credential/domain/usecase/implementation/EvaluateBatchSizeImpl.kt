package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.BatchSize
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.EvaluateBatchSize
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import timber.log.Timber
import javax.inject.Inject

class EvaluateBatchSizeImpl @Inject constructor() : EvaluateBatchSize {
    override fun invoke(
        issuerCredentialInfo: IssuerCredentialInfo,
    ): Result<BatchSize, CredentialError.InvalidIssuerCredentialInfo> {
        val batchSize = issuerCredentialInfo.batchCredentialIssuance?.batchSize
            ?: return Err(CredentialError.InvalidIssuerCredentialInfo)

        return if (batchSize in MIN_BATCH_SIZE..MAX_BATCH_SIZE) {
            Ok(batchSize)
        } else {
            Timber.e("Batch size not in bounds: $batchSize, min: $MIN_BATCH_SIZE, max: $MAX_BATCH_SIZE")
            Err(CredentialError.InvalidIssuerCredentialInfo)
        }
    }

    private companion object {
        private const val MIN_BATCH_SIZE = 10
        private const val MAX_BATCH_SIZE = 100
    }
}
