package ch.admin.foitt.wallet.platform.invitation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.FetchPresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.RequestObject
import ch.admin.foitt.openid4vc.domain.usecase.FetchPresentationRequest
import ch.admin.foitt.wallet.BuildConfig
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ValidatePresentationRequestError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ValidatePresentationRequest
import ch.admin.foitt.wallet.platform.invitation.domain.model.GetPresentationRequestError
import ch.admin.foitt.wallet.platform.invitation.domain.model.toGetPresentationRequestError
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.GetPresentationRequestFromUri
import ch.admin.foitt.wallet.platform.utils.getQueryParameter
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import java.net.URI
import java.net.URL
import javax.inject.Inject

internal class GetPresentationRequestFromUriImpl @Inject constructor(
    private val fetchPresentationRequest: FetchPresentationRequest,
    private val validatePresentationRequest: ValidatePresentationRequest,
) : GetPresentationRequestFromUri {
    override suspend fun invoke(uri: URI): Result<PresentationRequestWithRaw, GetPresentationRequestError> = coroutineBinding {
        val requestObject = runSuspendCatching {
            when (uri.scheme) {
                BuildConfig.SCHEME_PRESENTATION_REQUEST -> {
                    val presentationRequestJwt = fetchPresentationRequest(uri.toURL())
                        .mapError(FetchPresentationRequestError::toGetPresentationRequestError)
                        .bind()

                    RequestObject(presentationRequestJwt, null, null)
                }
                BuildConfig.SCHEME_PRESENTATION_REQUEST_OID,
                BuildConfig.SCHEME_PRESENTATION_REQUEST_SWIYU -> {
                    val clientId = uri.getQueryParameter(QUERY_PARAM_CLIENT_ID)
                        .mapError {
                            it.toGetPresentationRequestError(uri)
                        }.bind()
                    checkNotNull(clientId) { "client_id must not be null" }

                    val redirectUri = uri.getQueryParameter(QUERY_PARAM_REDIRECT_URI)
                        .mapError {
                            it.toGetPresentationRequestError(uri)
                        }.bind()

                    val requestUri = uri.getQueryParameter(QUERY_PARAM_REQUEST_URI)
                        .mapError {
                            it.toGetPresentationRequestError(uri)
                        }.bind()

                    val presentationRequestJwt = fetchPresentationRequest(URL(requestUri))
                        .mapError(FetchPresentationRequestError::toGetPresentationRequestError)
                        .bind()

                    RequestObject(presentationRequestJwt, clientId, redirectUri)
                }
                else -> error("invalid presentation schema")
            }
        }.mapError {
            it.toGetPresentationRequestError(uri)
        }.bind()

        validatePresentationRequest(requestObject)
            .mapError(ValidatePresentationRequestError::toGetPresentationRequestError)
            .bind()
    }

    private companion object {
        const val QUERY_PARAM_CLIENT_ID = "client_id"
        const val QUERY_PARAM_REQUEST_URI = "request_uri"
        const val QUERY_PARAM_REDIRECT_URI = "redirect_uri"
    }
}
