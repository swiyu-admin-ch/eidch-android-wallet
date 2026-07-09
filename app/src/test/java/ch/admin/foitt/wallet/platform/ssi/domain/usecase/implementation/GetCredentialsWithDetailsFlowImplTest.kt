package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.MapToCredentialDisplayData
import ch.admin.foitt.wallet.platform.database.domain.model.ClusterWithDisplaysAndClaims
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClusterWithDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithDisplaysAndClusters
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialWithDisplaysAndClustersRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialsWithDetailsFlow
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialDetail
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialDetail.claims
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialDetail.credentialDisplay1
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialDetail.credentialDisplay2
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialDetail.credentialDisplayData1
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialDetail.credentialDisplayData2
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
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

class GetCredentialsWithDetailsFlowImplTest {

    @MockK
    lateinit var mockCredentialWithDisplaysAndClustersRepository: VerifiableCredentialWithDisplaysAndClustersRepository

    @MockK
    lateinit var mockMapToCredentialDisplayData: MapToCredentialDisplayData

    @MockK
    lateinit var mockCredential1: Credential

    @MockK
    lateinit var mockCredential2: Credential

    @MockK
    lateinit var mockVerifiableCredential1: VerifiableCredentialEntity

    @MockK
    lateinit var mockVerifiableCredential2: VerifiableCredentialEntity

    @MockK
    lateinit var mockCluster1: CredentialClusterWithDisplays

    @MockK
    lateinit var mockCluster2: CredentialClusterWithDisplays

    @MockK
    lateinit var mockCredentialWithDisplaysAndClusters1: VerifiableCredentialWithDisplaysAndClusters

    @MockK
    lateinit var mockCredentialWithDisplaysAndClusters2: VerifiableCredentialWithDisplaysAndClusters

    private lateinit var getCredentialsWithDetailsFlow: GetCredentialsWithDetailsFlow

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        getCredentialsWithDetailsFlow = GetCredentialsWithDetailsFlowImpl(
            mockCredentialWithDisplaysAndClustersRepository,
            mockMapToCredentialDisplayData,
        )

        success()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Getting the credentialsWithDisplays flow without updates returns a flow with 2 credential display data`() = runTest {
        val result = getCredentialsWithDetailsFlow().firstOrNull()

        assertNotNull(result)
        val credentialList = result?.assertOk()
        assertEquals(2, credentialList?.size)
        assertEquals(credentialDisplayData1, credentialList?.get(0))
        assertEquals(credentialDisplayData2, credentialList?.get(1))
    }

    @Test
    fun `Getting the credentialsWithDisplays flow maps errors from the repository`() = runTest {
        val exception = IllegalStateException("db error")
        coEvery {
            mockCredentialWithDisplaysAndClustersRepository.getVerifiableCredentialsWithDisplaysAndClustersFlow()
        } returns flowOf(Err(SsiError.Unexpected(exception)))

        val result = getCredentialsWithDetailsFlow().firstOrNull()

        assertNotNull(result)
        val error = result?.assertErrorType(SsiError.Unexpected::class)
        assertEquals(exception, error?.cause)
    }

    @Test
    fun `Getting the credentialsWithDisplays flow maps errors from the MapToCredentialDisplayData use case`() = runTest {
        val exception = IllegalStateException("map to credential display data error")
        coEvery {
            mockMapToCredentialDisplayData(mockVerifiableCredential1, listOf(credentialDisplay1), claims, CredentialFormat.VC_SD_JWT)
        } returns Err(CredentialError.Unexpected(exception))

        val result = getCredentialsWithDetailsFlow().firstOrNull()

        assertNotNull(result)
        result?.assertErrorType(SsiError.Unexpected::class)
    }

    private fun success() {
        every {
            mockCredentialWithDisplaysAndClusters1.verifiableCredential
        } returns mockVerifiableCredential1
        every { mockCredentialWithDisplaysAndClusters1.credentialDisplays } returns listOf(credentialDisplay1)
        every { mockCredentialWithDisplaysAndClusters1.clusters } returns listOf(
            ClusterWithDisplaysAndClaims(
                clusterWithDisplays = mockCluster1,
                claimsWithDisplays = claims,
            )
        )
        coEvery { mockCredentialWithDisplaysAndClusters1.credential } returns MockCredentialDetail.credential
        every {
            mockCredentialWithDisplaysAndClusters2.verifiableCredential
        } returns mockVerifiableCredential2
        every { mockCredentialWithDisplaysAndClusters2.credentialDisplays } returns listOf(credentialDisplay2)
        every { mockCredentialWithDisplaysAndClusters2.clusters } returns listOf(
            ClusterWithDisplaysAndClaims(
                clusterWithDisplays = mockCluster2,
                claimsWithDisplays = claims,
            )
        )
        coEvery { mockCredentialWithDisplaysAndClusters2.credential } returns MockCredentialDetail.credential
        coEvery {
            mockCredentialWithDisplaysAndClustersRepository.getVerifiableCredentialsWithDisplaysAndClustersFlow()
        } returns flowOf(Ok(listOf(mockCredentialWithDisplaysAndClusters1, mockCredentialWithDisplaysAndClusters2)))

        coEvery {
            mockMapToCredentialDisplayData(mockVerifiableCredential1, listOf(credentialDisplay1), claims, CredentialFormat.VC_SD_JWT)
        } returns Ok(credentialDisplayData1)

        coEvery {
            mockMapToCredentialDisplayData(mockVerifiableCredential2, listOf(credentialDisplay2), claims, CredentialFormat.VC_SD_JWT)
        } returns Ok(credentialDisplayData2)
    }
}
