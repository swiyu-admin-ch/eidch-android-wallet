package ch.admin.foitt.didResolver.domain

import com.github.michaelbull.result.Result
import java.net.URL

interface DidResolverHelper {
    fun getHttpsUrl(did: String): Result<URL, Throwable>
    fun getDidStringFromAbsoluteKeyId(keyId: String): Result<String, Throwable>
}
