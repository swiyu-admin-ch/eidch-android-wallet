package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.CreateAnyVerifiablePresentationError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.DescriptorMap
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.GetPresentationRequestTypeError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptor
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptorFormat
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestBody
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationSubmission
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.SubmitAnyCredentialPresentationError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.toSubmitAnyCredentialPresentationError
import ch.admin.foitt.openid4vc.domain.repository.PresentationRequestRepository
import ch.admin.foitt.openid4vc.domain.usecase.CreateAnyDescriptorMaps
import ch.admin.foitt.openid4vc.domain.usecase.CreateAnyVerifiablePresentation
import ch.admin.foitt.openid4vc.domain.usecase.GetPresentationRequestType
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
    private val createAnyDescriptorMaps: CreateAnyDescriptorMaps,
    private val getPresentationRequestType: GetPresentationRequestType,
    private val presentationRequestRepository: PresentationRequestRepository,
) : SubmitAnyCredentialPresentation {
    override suspend fun invoke(
        anyCredential: AnyCredential,
        requestedFields: List<String>,
        presentationRequest: PresentationRequest,
        usePayloadEncryption: Boolean,
    ): Result<Unit, SubmitAnyCredentialPresentationError> = coroutineBinding {
        val descriptors = presentationRequest.presentationDefinition.inputDescriptors
        descriptors.checkCredentialFormat().bind()

        val verifiablePresentation = createAnyVerifiablePresentation(
            anyCredential = anyCredential,
            requestedFields = requestedFields,
            presentationRequest = presentationRequest,
        ).mapError(CreateAnyVerifiablePresentationError::toSubmitAnyCredentialPresentationError)
            .bind()

        val descriptorMaps = createAnyDescriptorMaps(presentationRequest)
        val responseURL = runSuspendCatching { URL(presentationRequest.responseUri) }
            .mapError { throwable -> throwable.toSubmitAnyCredentialPresentationError("presentationRequest.responseUri error") }
            .bind()
        val body = createPresentationRequestBody(
            presentationRequest = presentationRequest,
            verifiablePresentation = verifiablePresentation,
            descriptorMap = descriptorMaps
        )

        val presentationRequestType = getPresentationRequestType(
            presentationRequest = presentationRequest,
            presentationRequestBody = body,
            usePayloadEncryption = usePayloadEncryption,
        ).mapError(GetPresentationRequestTypeError::toSubmitAnyCredentialPresentationError)
            .bind()

        presentationRequestRepository.submitPresentation(
            url = responseURL,
            presentationRequestType = presentationRequestType,
        ).bind()
    }

    private fun List<InputDescriptor>.checkCredentialFormat(): Result<Unit, SubmitAnyCredentialPresentationError> {
        val formatSet = map { it.formats.first() }.toSet()
        return if (formatSet.isEmpty() || formatSet.first().isNotSupported()) {
            val exception = IllegalArgumentException("Invalid credential format in input descriptors: $formatSet")
            Timber.e(exception)
            Err(PresentationRequestError.Unexpected(exception))
        } else {
            Ok(Unit)
        }
    }

    private fun createPresentationRequestBody(
        presentationRequest: PresentationRequest,
        verifiablePresentation: String,
        descriptorMap: List<DescriptorMap>,
    ): PresentationRequestBody {
        val presentationSubmission = PresentationSubmission(
            definitionId = presentationRequest.presentationDefinition.id,
            descriptorMap = descriptorMap,
            id = UUID.randomUUID().toString(),
        )
        return PresentationRequestBody(
            vpToken = verifiablePresentation,
            presentationSubmission = presentationSubmission,
        )
    }

    private fun InputDescriptorFormat.isNotSupported() = when (this) {
        is InputDescriptorFormat.VcSdJwt -> sdJwtAlgorithms.isEmpty()
    }
}
