package ch.admin.foitt.openid4vc.domain.usecase

import ch.admin.eid.didresolver.did_sidekicks.DidDoc
import ch.admin.foitt.openid4vc.domain.model.ResolveDidError
import com.github.michaelbull.result.Result

/**
 * Interface that wraps the native DID resolver to allow mocking and unit testing.
 */
fun interface ResolveDid {
    suspend operator fun invoke(didString: String): Result<DidDoc, ResolveDidError>
}
