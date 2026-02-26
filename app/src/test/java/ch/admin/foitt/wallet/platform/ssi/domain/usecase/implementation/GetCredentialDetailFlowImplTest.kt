package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.credentialOffer.mock.MockCredentialOffer.ISSUER
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.MapToCredentialDisplayData
import ch.admin.foitt.wallet.platform.credentialCluster.domain.usercase.MapToCredentialClaimCluster
import ch.admin.foitt.wallet.platform.database.domain.model.ClusterWithDisplaysAndClaims
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClusterWithDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithDisplaysAndClusters
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialWithDisplaysAndClustersRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialDetailFlow
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialDetail
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialDetail.CREDENTIAL_ID
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialDetail.claims
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialDetail.credentialDetail
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialDetail.credentialDetail2
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import com.github.michaelbull.result.getError
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetCredentialDetailFlowImplTest {

    @MockK
    lateinit var mockCredentialWithDisplaysAndClustersRepository: VerifiableCredentialWithDisplaysAndClustersRepository

    @MockK
    lateinit var mockMapToCredentialDisplayData: MapToCredentialDisplayData

    @MockK
    lateinit var mockMapToCredentialClaimCluster: MapToCredentialClaimCluster

    @MockK
    lateinit var mockVerifiableCredential: VerifiableCredentialEntity

    @MockK
    lateinit var mockClusterWithDisplays: CredentialClusterWithDisplays

    @MockK
    lateinit var mockCredentialWithDisplaysAndClusters: VerifiableCredentialWithDisplaysAndClusters

    private lateinit var useCase: GetCredentialDetailFlow

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = GetCredentialDetailFlowImpl(
            verifiableCredentialWithDisplaysAndClustersRepository = mockCredentialWithDisplaysAndClustersRepository,
            mapToCredentialDisplayData = mockMapToCredentialDisplayData,
            mapToCredentialClaimCluster = mockMapToCredentialClaimCluster,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Getting the credential detail flow without updates returns a flow with one credential detail`() = runTest {
        val result = useCase(CREDENTIAL_ID).firstOrNull()

        assertNotNull(result)
        result?.assertOk()
    }

    @OptIn(UnsafeResultValueAccess::class)
    @Test
    fun `Getting the credential detail flow with updates returns a flow with the credential detail`() = runTest {
        coEvery {
            mockCredentialWithDisplaysAndClustersRepository.getNullableVerifiableCredentialWithDisplaysAndClustersFlowById(CREDENTIAL_ID)
        } returns flow {
            emit(Ok(mockCredentialWithDisplaysAndClusters))
            emit(Ok(mockCredentialWithDisplaysAndClusters))
        }

        val result = useCase(CREDENTIAL_ID).toList()
        assertEquals(2, result.size)
        result[0].assertOk()
        result[1].assertOk()
        assertEquals(credentialDetail, result[0].value)
        assertEquals(credentialDetail2, result[1].value)
    }

    @Test
    fun `Getting the credential detail flow maps errors from the repository`() = runTest {
        val exception = IllegalStateException("db error")
        coEvery {
            mockCredentialWithDisplaysAndClustersRepository.getNullableVerifiableCredentialWithDisplaysAndClustersFlowById(CREDENTIAL_ID)
        } returns flowOf(Err(SsiError.Unexpected(exception)))

        val result = useCase(CREDENTIAL_ID).firstOrNull()

        assertNotNull(result)
        result?.assertErrorType(SsiError.Unexpected::class)
        val error = result?.getError() as SsiError.Unexpected
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Getting the credential detail flow maps errors from the MapToCredentialDisplayData use case`() = runTest {
        val exception = IllegalStateException("map to credential display data error")
        coEvery {
            mockMapToCredentialDisplayData(any(), any(), any())
        } returns Err(CredentialError.Unexpected(exception))

        val result = useCase(CREDENTIAL_ID).firstOrNull()

        assertNotNull(result)
        result?.assertErrorType(SsiError.Unexpected::class)
    }

    private fun setupDefaultMocks() {
        coEvery { mockVerifiableCredential.issuer } returns ISSUER
        coEvery {
            mockCredentialWithDisplaysAndClusters.verifiableCredential
        } returns mockVerifiableCredential
        coEvery { mockCredentialWithDisplaysAndClusters.credentialDisplays } returns MockCredentialDetail.credentialDisplays
        coEvery { mockCredentialWithDisplaysAndClusters.clusters } returns listOf(
            ClusterWithDisplaysAndClaims(
                clusterWithDisplays = mockClusterWithDisplays,
                claimsWithDisplays = claims,
            )
        )
        coEvery {
            mockCredentialWithDisplaysAndClustersRepository.getNullableVerifiableCredentialWithDisplaysAndClustersFlowById(MockCredentialDetail.CREDENTIAL_ID)
        } returns flowOf(Ok(mockCredentialWithDisplaysAndClusters))
        coEvery {
            mockMapToCredentialDisplayData(mockVerifiableCredential, MockCredentialDetail.credentialDisplays, claims)
        } returns Ok(MockCredentialDetail.credentialDisplayData)
        coEvery {
            mockMapToCredentialClaimCluster(any())
        } returns MockCredentialDetail.listOfCredentialClaimCluster
    }
}
