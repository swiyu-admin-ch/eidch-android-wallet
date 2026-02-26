package ch.admin.foitt.wallet.feature.credentialDetail

import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.GetCredentialIssuerDisplaysFlowError
import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.IssuerDisplay
import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.GetCredentialIssuerDisplaysFlow
import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.implementation.GetCredentialIssuerDisplaysFlowImpl
import ch.admin.foitt.wallet.feature.credentialDetail.mock.MockIssuerDisplays.CREDENTIAL_ID
import ch.admin.foitt.wallet.feature.credentialDetail.mock.MockIssuerDisplays.issuerDisplay1
import ch.admin.foitt.wallet.feature.credentialDetail.mock.MockIssuerDisplays.issuerDisplays
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialIssuerDisplayRepo
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(UnsafeResultValueAccess::class)
class GetCredentialIssuerDisplaysFlowImplTest {

    @MockK
    lateinit var mockCredentialIssuerDisplayRepo: CredentialIssuerDisplayRepo

    @MockK
    lateinit var mockGetLocalizedDisplay: GetLocalizedDisplay

    private lateinit var getCredentialIssuerDisplaysFlow: GetCredentialIssuerDisplaysFlow

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        getCredentialIssuerDisplaysFlow = GetCredentialIssuerDisplaysFlowImpl(
            mockCredentialIssuerDisplayRepo,
            mockGetLocalizedDisplay,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Getting the credential issuer display flow without updates returns a flow with one localized issuer display`() = runTest {
        val result = getCredentialIssuerDisplaysFlow(CREDENTIAL_ID).firstOrNull()

        assertNotNull(result)
        result?.assertOk()

        val expected = IssuerDisplay(
            issuerDisplay1.name,
            issuerDisplay1.image,
            issuerDisplay1.imageAltText,
            issuerDisplay1.locale,
        )

        assertEquals(expected, result?.value)
    }

    @Test
    fun `Getting the credential issuer display flow maps errors from credential issuer display repository`() = runTest {
        val exception = IllegalStateException("repo error")
        coEvery {
            mockCredentialIssuerDisplayRepo.getIssuerDisplaysFlow(CREDENTIAL_ID)
        } returns flowOf(Err(SsiError.Unexpected(exception)))

        val result = getCredentialIssuerDisplaysFlow(CREDENTIAL_ID).firstOrNull()

        assertNotNull(result)
        result?.assertErrorType(GetCredentialIssuerDisplaysFlowError::class)
    }

    @Test
    fun `Getting the credential issuer display flow maps errors from get localized display`() = runTest {
        coEvery {
            mockGetLocalizedDisplay(issuerDisplays)
        } returns null

        val result = getCredentialIssuerDisplaysFlow(CREDENTIAL_ID).firstOrNull()

        assertNotNull(result)
        result?.assertErrorType(GetCredentialIssuerDisplaysFlowError::class)
    }

    private fun setupDefaultMocks() {
        coEvery {
            mockCredentialIssuerDisplayRepo.getIssuerDisplaysFlow(CREDENTIAL_ID)
        } returns flowOf(Ok(issuerDisplays))

        coEvery {
            mockGetLocalizedDisplay(issuerDisplays)
        } returns issuerDisplay1
    }
}
