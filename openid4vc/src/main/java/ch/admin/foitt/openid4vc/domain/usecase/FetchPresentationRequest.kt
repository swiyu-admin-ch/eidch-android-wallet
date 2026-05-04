package ch.admin.foitt.openid4vc.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.FetchPresentationRequestError
import com.github.michaelbull.result.Result
import java.net.URL

fun interface FetchPresentationRequest {
    suspend operator fun invoke(url: URL): Result<Jwt, FetchPresentationRequestError>
}
