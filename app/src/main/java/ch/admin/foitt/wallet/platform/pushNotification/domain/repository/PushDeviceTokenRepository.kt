package ch.admin.foitt.wallet.platform.pushNotification.domain.repository

import ch.admin.foitt.wallet.platform.pushNotification.domain.model.FetchPushDeviceTokenError
import com.github.michaelbull.result.Result

interface PushDeviceTokenRepository {

    suspend fun fetchToken(): Result<String, FetchPushDeviceTokenError>
}
