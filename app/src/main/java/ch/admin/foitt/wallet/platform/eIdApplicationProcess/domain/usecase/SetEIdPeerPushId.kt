package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SetEIdPeerPushIdError
import com.github.michaelbull.result.Result

interface SetEIdPeerPushId {
    suspend operator fun invoke(caseId: String, pushId: String): Result<Unit, SetEIdPeerPushIdError>
}
