package ch.admin.foitt.wallet.platform.pushNotification.data

import ch.admin.foitt.wallet.platform.pushNotification.domain.model.FetchPushDeviceTokenError
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.toFetchPushDeviceTokenError
import ch.admin.foitt.wallet.platform.pushNotification.domain.repository.PushDeviceTokenRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PushDeviceTokenRepositoryImpl @Inject constructor() : PushDeviceTokenRepository {

    override suspend fun fetchToken(): Result<String, FetchPushDeviceTokenError> =
        runSuspendCatching {
            suspendCancellableCoroutine { cont ->
                FirebaseMessaging.getInstance().token
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
        }.mapError { it.toFetchPushDeviceTokenError("Fetch device push token failed") }
}
