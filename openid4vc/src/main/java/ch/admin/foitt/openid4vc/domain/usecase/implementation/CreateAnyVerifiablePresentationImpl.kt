package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.CreateAnyVerifiablePresentationError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.CreateVcSdJwtVerifiablePresentationError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.toCreateAnyVerifiablePresentationError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.usecase.CreateAnyVerifiablePresentation
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.CreateVcSdJwtVerifiablePresentation
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapError
import timber.log.Timber
import javax.inject.Inject

internal class CreateAnyVerifiablePresentationImpl @Inject constructor(
    private val createVcSdJwtVerifiablePresentation: CreateVcSdJwtVerifiablePresentation,
) : CreateAnyVerifiablePresentation {
    override suspend fun invoke(
        anyCredential: AnyCredential,
        requestedFields: List<String>,
        authorizationRequest: AuthorizationRequest,
    ): Result<String, CreateAnyVerifiablePresentationError> =
        when (anyCredential) {
            is VcSdJwtCredential -> createVcSdJwtVerifiablePresentation(
                credential = anyCredential,
                keyBinding = anyCredential.keyBinding,
                requestedFields = requestedFields,
                authorizationRequest = authorizationRequest,
            ).mapError(CreateVcSdJwtVerifiablePresentationError::toCreateAnyVerifiablePresentationError)

            else -> {
                val exception =
                    IllegalArgumentException("Unsupported credential format")
                Timber.e(exception)
                Err(PresentationRequestError.Unexpected(exception))
            }
        }
}
