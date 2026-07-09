package ch.admin.foitt.didResolver.domain.implementation

import ch.admin.eid.didresolver.didresolver.Did
import ch.admin.eid.didresolver.didresolver.getDidFromAbsoluteKid
import ch.admin.foitt.didResolver.domain.DidResolverHelper
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import java.net.URL
import javax.inject.Inject

internal class DidResolverHelperImpl @Inject constructor() : DidResolverHelper {
    override fun getHttpsUrl(did: String): Result<URL, Throwable> = runCatching {
        val didUrl = Did(did).getHttpsUrl()
        URL(didUrl)
    }

    override fun getDidStringFromAbsoluteKeyId(keyId: String): Result<String, Throwable> = runCatching {
        getDidFromAbsoluteKid(keyId).asString()
    }
}
