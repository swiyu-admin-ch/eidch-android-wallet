package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponse
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.CreateAnyVerifiablePresentationError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.GetAuthorizationResponseConfigError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptor
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptorFormat
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationDefinition
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationSubmission
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.SubmitAnyCredentialPresentationError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.toSubmitAnyCredentialPresentationError
import ch.admin.foitt.openid4vc.domain.repository.PresentationRequestRepository
import ch.admin.foitt.openid4vc.domain.usecase.CreateAnyDescriptorMapByPresentationDefinition
import ch.admin.foitt.openid4vc.domain.usecase.CreateAnyVerifiablePresentation
import ch.admin.foitt.openid4vc.domain.usecase.GetAuthorizationResponseConfig
import ch.admin.foitt.openid4vc.domain.usecase.SubmitAnyCredentialPresentation
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import timber.log.Timber
import java.net.URL
import java.util.UUID
import javax.inject.Inject

internal class SubmitAnyCredentialPresentationImpl @Inject constructor(
    private val createAnyVerifiablePresentation: CreateAnyVerifiablePresentation,
    private val createAnyDescriptorMapByPresentationDefinition: CreateAnyDescriptorMapByPresentationDefinition,
    private val getAuthorizationResponseConfig: GetAuthorizationResponseConfig,
    private val presentationRequestRepository: PresentationRequestRepository,
) : SubmitAnyCredentialPresentation {
    override suspend fun invoke(
        anyCredential: AnyCredential,
        requestedFields: List<String>,
        authorizationRequest: AuthorizationRequest,
        usePayloadEncryption: Boolean,
        dcqlQueryId: String?,
    ): Result<Unit, SubmitAnyCredentialPresentationError> = coroutineBinding {
        val presentationDefinition = authorizationRequest.presentationDefinition

        val verifiablePresentation = createAnyVerifiablePresentation(
            anyCredential = anyCredential,
            requestedFields = requestedFields,
            authorizationRequest = authorizationRequest,
        ).mapError(CreateAnyVerifiablePresentationError::toSubmitAnyCredentialPresentationError)
            .bind()

        val authorizationResponse = if (dcqlQueryId != null) {
            getAuthorizationResponseByDcqlId(
                dcqlQueryId = dcqlQueryId,
                verifiablePresentation = verifiablePresentation,
                state = authorizationRequest.state,
            ).bind()
        } else if (presentationDefinition != null) {
            getAuthorizationResponseByPresentationDefinition(
                presentationDefinition = presentationDefinition,
                verifiablePresentation = verifiablePresentation,
                state = authorizationRequest.state,
            ).bind()
        } else {
            Err(
                PresentationRequestError.Unexpected(IllegalStateException("No presentation definition or dcql query provided"))
            ).bind()
        }

        val responseURL = runSuspendCatching { URL(authorizationRequest.responseUri) }
            .mapError { throwable -> throwable.toSubmitAnyCredentialPresentationError("presentationRequest.responseUri error") }
            .bind()

        val authorizationResponseConfig = getAuthorizationResponseConfig(
            authorizationRequest = authorizationRequest,
            authorizationResponse = authorizationResponse,
            usePayloadEncryption = usePayloadEncryption,
        ).mapError(GetAuthorizationResponseConfigError::toSubmitAnyCredentialPresentationError)
            .bind()

        presentationRequestRepository.submitPresentation(
            url = responseURL,
            authorizationResponseConfig = authorizationResponseConfig,
        ).bind()
    }

    private suspend fun getAuthorizationResponseByDcqlId(
        dcqlQueryId: String,
        verifiablePresentation: String,
        state: String?,
    ): Result<AuthorizationResponse, SubmitAnyCredentialPresentationError> = coroutineBinding {
        AuthorizationResponse.Dcql(
            vpToken = mapOf(dcqlQueryId to listOf(verifiablePresentation)),
            state = state,
        )
    }

    private suspend fun getAuthorizationResponseByPresentationDefinition(
        presentationDefinition: PresentationDefinition,
        verifiablePresentation: String,
        state: String?,
    ): Result<AuthorizationResponse, SubmitAnyCredentialPresentationError> = coroutineBinding {
        val descriptors = presentationDefinition.inputDescriptors
        descriptors.checkCredentialFormat().bind()
        val descriptorMap = createAnyDescriptorMapByPresentationDefinition(presentationDefinition)
        val presentationSubmission = PresentationSubmission(
            definitionId = presentationDefinition.id,
            descriptorMap = descriptorMap,
            id = UUID.randomUUID().toString(),
        )
        AuthorizationResponse.Dif(
            vpToken = verifiablePresentation,
            presentationSubmission = presentationSubmission,
            state = state,
        )
    }

    private fun List<InputDescriptor>.checkCredentialFormat(): Result<Unit, SubmitAnyCredentialPresentationError> {
        val formatSet = map { it.formats.first() }.toSet()
        return if (formatSet.isEmpty() || formatSet.first().isNotSupported()) {
            val exception = IllegalArgumentException("Invalid credential format in input descriptors: $formatSet")
            Timber.Forest.e(exception)
            Err(PresentationRequestError.Unexpected(exception))
        } else {
            Ok(Unit)
        }
    }

    private fun InputDescriptorFormat.isNotSupported() = when (this) {
        is InputDescriptorFormat.VcSdJwt -> sdJwtAlgorithms.isEmpty()
    }
}
