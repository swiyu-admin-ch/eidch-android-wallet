package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.usecase.CreateAnyVerifiablePresentation
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.CreateVcSdJwtVerifiablePresentation
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CreateAnyVerifiablePresentationImplTest {

    @MockK
    private lateinit var mockCreateVcSdJwtVerifiablePresentation: CreateVcSdJwtVerifiablePresentation

    @MockK
    private lateinit var mockVcSdJwtCredential: VcSdJwtCredential

    @MockK
    private lateinit var mockAnyCredential: AnyCredential

    @MockK
    private lateinit var mockAuthorizationRequest: AuthorizationRequest

    @MockK
    private lateinit var mockKeyBinding: KeyBinding

    private lateinit var useCase: CreateAnyVerifiablePresentation

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = CreateAnyVerifiablePresentationImpl(mockCreateVcSdJwtVerifiablePresentation)

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Submitting presentation for vc+sd_jwt credential returns verifiable presentation`() = runTest {
        val result = useCase(
            anyCredential = mockVcSdJwtCredential,
            requestedFields = requestedFields,
            authorizationRequest = mockAuthorizationRequest,
        ).assertOk()

        assertEquals(VERIFIABLE_PRESENTATION, result)
    }

    @Test
    fun `Submitting presentation for unsupported credential format returns error`() = runTest {
        val result = useCase(
            anyCredential = mockAnyCredential,
            requestedFields = requestedFields,
            authorizationRequest = mockAuthorizationRequest,
        )

        result.assertErrorType(PresentationRequestError.Unexpected::class)
    }

    private fun setupDefaultMocks() {
        coEvery {
            mockCreateVcSdJwtVerifiablePresentation(
                credential = mockVcSdJwtCredential,
                requestedFields = requestedFields,
                authorizationRequest = mockAuthorizationRequest,
                keyBinding = mockKeyBinding,
            )
        } returns Ok(VERIFIABLE_PRESENTATION)

        every { mockVcSdJwtCredential.keyBinding } returns mockKeyBinding
    }

    private companion object {
        const val FIELD = "field"
        val requestedFields = listOf(FIELD)

        const val VERIFIABLE_PRESENTATION = "verifiablePresentation"
    }
}
