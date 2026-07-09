package ch.admin.foitt.wallet.platform.pushNotification.domain.usecase

import ch.admin.foitt.wallet.platform.pushNotification.domain.model.DeletePushIdError
import com.github.michaelbull.result.Result

interface DeletePushId {
    suspend operator fun invoke(pushId: String): Result<Unit, DeletePushIdError>
}
