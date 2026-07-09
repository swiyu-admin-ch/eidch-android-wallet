package ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase

import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.RegisterEIdPushNotificationError
import com.github.michaelbull.result.Result

interface RegisterEIdPushNotification {
    suspend operator fun invoke(caseId: String): Result<Unit, RegisterEIdPushNotificationError>
}
