package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.eid.didresolver.did_sidekicks.DidDoc
import ch.admin.eid.didresolver.didresolver.Did
import ch.admin.foitt.openid4vc.domain.model.ResolveDidError
import ch.admin.foitt.openid4vc.domain.model.toResolveDidError
import ch.admin.foitt.openid4vc.domain.repository.FetchDidLogRepository
import ch.admin.foitt.openid4vc.domain.usecase.ResolveDid
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import java.net.URL
import javax.inject.Inject

class ResolveDidImpl @Inject constructor(
    private val repo: FetchDidLogRepository
) : ResolveDid {
    override suspend fun invoke(didString: String): Result<DidDoc, ResolveDidError> = coroutineBinding {
        runSuspendCatching {
            val did = Did(didString)
            val url = URL(did.getUrl())
            val didLog = repo.fetchDidLog(url).bind()
            did.resolve(didLog)
        }.mapError(Throwable::toResolveDidError).bind()
    }
}
