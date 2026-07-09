package ch.admin.foitt.wallet.platform.pushNotification.domain.usecase

import ch.admin.foitt.wallet.platform.pushNotification.domain.model.UpdatePushDeviceTokenError
import com.github.michaelbull.result.Result

interface UpdatePushToken {
    suspend operator fun invoke(): Result<Unit, UpdatePushDeviceTokenError>
}
