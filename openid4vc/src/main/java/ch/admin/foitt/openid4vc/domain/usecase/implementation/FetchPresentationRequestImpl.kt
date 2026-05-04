package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.FetchPresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.toFetchPresentationRequestError
import ch.admin.foitt.openid4vc.domain.repository.PresentationRequestRepository
import ch.admin.foitt.openid4vc.domain.usecase.FetchPresentationRequest
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import java.net.URL
import javax.inject.Inject

internal class FetchPresentationRequestImpl @Inject constructor(
    private val presentationRequestRepository: PresentationRequestRepository,
) : FetchPresentationRequest {
    override suspend fun invoke(
        url: URL,
    ): Result<Jwt, FetchPresentationRequestError> = coroutineBinding {
        val presentationRequestString = presentationRequestRepository.fetchPresentationRequest(url)
            .bind()

        runSuspendCatching {
            Jwt(presentationRequestString)
        }.mapError { throwable ->
            throwable.toFetchPresentationRequestError("Presentation request is not a jwt")
        }.bind()
    }
}
