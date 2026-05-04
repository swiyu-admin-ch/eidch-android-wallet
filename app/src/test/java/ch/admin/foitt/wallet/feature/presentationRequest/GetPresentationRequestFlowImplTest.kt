package ch.admin.foitt.wallet.feature.presentationRequest

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationRequestError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.implementation.GetPresentationRequestFlowImpl
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.MapToCredentialDisplayData
import ch.admin.foitt.wallet.platform.credentialCluster.domain.usercase.MapToCredentialClaimCluster
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestField
import ch.admin.foitt.wallet.platform.database.domain.model.ClusterWithDisplaysAndClaims
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClusterWithDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithDisplaysAndClusters
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialWithDisplaysAndClustersRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialDetail
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialDetail.claims
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getError
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
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

class GetPresentationRequestFlowImplTest {

    @MockK
    lateinit var mockCredentialWithDisplaysAndClustersRepository: VerifiableCredentialWithDisplaysAndClustersRepository

    @MockK
    lateinit var mockMapToCredentialDisplayData: MapToCredentialDisplayData

    @MockK
    lateinit var mockMapToCredentialClaimCluster: MapToCredentialClaimCluster

    @MockK
    lateinit var mockCredentialWithDisplaysAndClusters: VerifiableCredentialWithDisplaysAndClusters

    @MockK
    lateinit var mockRequestedField: PresentationRequestField

    @MockK
    lateinit var mockAuthorizationRequest: AuthorizationRequest

    @MockK
    lateinit var mockClusterWithDisplays: CredentialClusterWithDisplays

    @MockK
    lateinit var mockVerifiableCredential: VerifiableCredentialEntity

    private lateinit var getPresentationRequestFlow: GetPresentationRequestFlowImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        getPresentationRequestFlow = GetPresentationRequestFlowImpl(
            mockCredentialWithDisplaysAndClustersRepository,
            mockMapToCredentialDisplayData,
            mockMapToCredentialClaimCluster,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Getting the presentation request flow returns a flow with one presentation request ui`() = runTest {
        val result = getPresentationRequestFlow(
            id = CREDENTIAL_ID1,
            requestedFields = listOf(mockRequestedField),
        ).firstOrNull()

        assertNotNull(result)
        result?.assertOk()
    }

    @Test
    fun `Getting the presentation request flow maps errors from the repository`() = runTest {
        val exception = IllegalStateException("db error")
        coEvery {
            mockCredentialWithDisplaysAndClustersRepository.getVerifiableCredentialWithDisplaysAndClustersFlowById(CREDENTIAL_ID1)
        } returns flowOf(Err(SsiError.Unexpected(exception)))

        val result = getPresentationRequestFlow(
            id = CREDENTIAL_ID1,
            requestedFields = listOf(mockRequestedField),
        ).firstOrNull()

        assertNotNull(result)
        result?.assertErrorType(PresentationRequestError.Unexpected::class)
        val error = result?.getError() as PresentationRequestError.Unexpected
        assertEquals(exception, error.throwable)
    }

    @Test
    fun `Getting the presentation request flow maps errors from the MapToCredentialDisplayData use case`() = runTest {
        val exception = IllegalStateException("map to credential claim display data error")
        coEvery {
            mockMapToCredentialDisplayData(any(), any(), any())
        } returns Err(CredentialError.Unexpected(exception))

        val result = getPresentationRequestFlow(
            id = CREDENTIAL_ID1,
            requestedFields = listOf(mockRequestedField),
        ).firstOrNull()

        assertNotNull(result)
        result?.assertErrorType(PresentationRequestError.Unexpected::class)
    }

    private fun setupDefaultMocks() {
        coEvery {
            mockCredentialWithDisplaysAndClustersRepository.getVerifiableCredentialWithDisplaysAndClustersFlowById(CREDENTIAL_ID1)
        } returns flowOf(Ok(mockCredentialWithDisplaysAndClusters))

        every {
            mockCredentialWithDisplaysAndClusters.verifiableCredential
        } returns mockVerifiableCredential
        every { mockCredentialWithDisplaysAndClusters.credentialDisplays } returns MockCredentialDetail.credentialDisplays
        every { mockCredentialWithDisplaysAndClusters.clusters } returns listOf(
            ClusterWithDisplaysAndClaims(
                clusterWithDisplays = mockClusterWithDisplays,
                claimsWithDisplays = claims,
            )
        )

        coEvery {
            mockCredentialWithDisplaysAndClustersRepository.getNullableVerifiableCredentialWithDisplaysAndClustersFlowById(
                CREDENTIAL_ID1
            )
        } returns flowOf(Ok(mockCredentialWithDisplaysAndClusters))

        coEvery {
            mockMapToCredentialDisplayData(mockVerifiableCredential, MockCredentialDetail.credentialDisplays, claims)
        } returns Ok(MockCredentialDetail.credentialDisplayData)

        coEvery { mockRequestedField.path } returns path
        coEvery { mockAuthorizationRequest.clientId } returns CLIENT_ID

        coEvery {
            mockMapToCredentialClaimCluster(any())
        } returns MockCredentialDetail.listOfCredentialClaimCluster
    }

    private companion object {
        val path = listOf(ClaimsPathPointerComponent.String("claimKey"))
        const val CLIENT_ID = "clientId"

        const val CREDENTIAL_ID1 = 1L
    }
}
