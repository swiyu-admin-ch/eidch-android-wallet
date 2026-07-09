package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.di.DefaultDispatcher
import ch.admin.foitt.openid4vc.domain.model.DeleteKeyPairError
import ch.admin.foitt.openid4vc.domain.model.KeyPairError
import ch.admin.foitt.openid4vc.domain.usecase.DeleteKeyPair
import ch.admin.foitt.openid4vc.utils.Constants.ANDROID_KEY_STORE
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.toErrorIfNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.security.KeyStore
import javax.inject.Inject

internal class DeleteKeyPairImpl @Inject constructor(
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : DeleteKeyPair {
    override suspend fun invoke(
        keyId: String,
    ): Result<Unit, DeleteKeyPairError> = withContext(defaultDispatcher) {
        runSuspendCatching {
            val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
            keyStore.load(null)
            keyStore.deleteEntry(keyId)
        }.mapError { throwable ->
            KeyPairError.Unexpected(throwable)
        }.toErrorIfNull {
            KeyPairError.DeleteFailed
        }
    }
}
