package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import ch.admin.foitt.openid4vc.utils.Constants.ANDROID_KEY_STORE
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteKeyStoreEntry
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.getOrElse
import timber.log.Timber
import java.security.KeyStore
import javax.inject.Inject

class DeleteKeyStoreEntryImpl @Inject constructor() : DeleteKeyStoreEntry {
    override suspend fun invoke(keyIdentifier: String) = runSuspendCatching {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)
        keyStore.deleteEntry(keyIdentifier)
    }.getOrElse { throwable ->
        Timber.e(t = throwable, message = "Could not delete key store entry for credential")
    }
}
