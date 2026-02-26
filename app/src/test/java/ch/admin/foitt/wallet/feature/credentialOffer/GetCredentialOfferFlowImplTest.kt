package ch.admin.foitt.wallet.feature.credentialOffer

import ch.admin.foitt.wallet.feature.credentialOffer.domain.model.CredentialOffer
import ch.admin.foitt.wallet.feature.credentialOffer.domain.model.CredentialOfferError
import ch.admin.foitt.wallet.feature.credentialOffer.domain.usecase.GetCredentialOfferFlow
import ch.admin.foitt.wallet.feature.credentialOffer.domain.usecase.implementation.GetCredentialOfferFlowImpl
import ch.admin.foitt.wallet.feature.credentialOffer.mock.MockCredentialOffer.CREDENTIAL_ID
import ch.admin.foitt.wallet.feature.credentialOffer.mock.MockCredentialOffer.ISSUER
import ch.admin.foitt.wallet.feature.credentialOffer.mock.MockCredentialOffer.credentialOffer
import ch.admin.foitt.wallet.feature.credentialOffer.mock.MockCredentialOffer.credentialOffer2
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.MapToCredentialDisplayData
import ch.admin.foitt.wallet.platform.credentialCluster.domain.usercase.MapToCredentialClaimCluster
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
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
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

@OptIn(UnsafeResultValueAccess::class)
class GetCredentialOfferFlowImplTest {

    @MockK
    lateinit var mockCredentialWithDisplaysAndClustersRepository: VerifiableCredentialWithDisplaysAndClustersRepository

    @MockK
    lateinit var mockMapToCredentialDisplayData: MapToCredentialDisplayData

    @MockK
    lateinit var mockMapToCredentialClaimCluster: MapToCredentialClaimCluster

    @MockK
    lateinit var mockVerifiableCredential: VerifiableCredentialEntity

    @MockK
    private lateinit var mockClusterWithDisplays: CredentialClusterWithDisplays

    @MockK
    lateinit var mockCredentialWithDisplaysAndClusters: VerifiableCredentialWithDisplaysAndClusters

    private lateinit var useCase: GetCredentialOfferFlow

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = GetCredentialOfferFlowImpl(
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
    fun `Getting the credential offer returns a result with one credential offer`() = runTest {
        val result = useCase(CREDENTIAL_ID).firstOrNull()

        assertNotNull(result)
        result?.assertOk()

        val expected = CredentialOffer(
            credential = credentialOffer.credential,
            claims = credentialOffer.claims,
        )

        assertEquals(expected, result?.value)
    }

    @Test
    fun `Getting the credential offer flow with updates returns a flow with the credential offer`() = runTest {
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
        assertEquals(credentialOffer, result[0].value, "result[0] is not as expected")
        assertEquals(credentialOffer2, result[1].value, "result[1] is not as expected")
    }

    @Test
    fun `Getting the credential offer flow maps errors from the repository`() = runTest {
        val exception = IllegalStateException("db error")
        coEvery {
            mockCredentialWithDisplaysAndClustersRepository.getNullableVerifiableCredentialWithDisplaysAndClustersFlowById(CREDENTIAL_ID)
        } returns flowOf(Err(SsiError.Unexpected(exception)))

        val result = useCase(CREDENTIAL_ID).firstOrNull()

        assertNotNull(result)
        val error = result?.assertErrorType(CredentialOfferError.Unexpected::class)
        assertEquals(exception, error?.throwable)
    }

    @Test
    fun `Getting the credential offer flow maps error from MapToCredentialDisplayData use case`() = runTest {
        val exception = IllegalStateException("map to credential claim display data error")
        coEvery {
            mockMapToCredentialDisplayData(any(), any(), any())
        } returns Err(CredentialError.Unexpected(exception))

        val result = useCase(CREDENTIAL_ID).firstOrNull()

        assertNotNull(result)
        val error = result?.assertErrorType(CredentialOfferError.Unexpected::class)
        assertEquals(exception, error?.throwable)
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
            mockCredentialWithDisplaysAndClustersRepository.getNullableVerifiableCredentialWithDisplaysAndClustersFlowById(
                MockCredentialDetail.CREDENTIAL_ID
            )
        } returns flowOf(Ok(mockCredentialWithDisplaysAndClusters))
        coEvery {
            mockMapToCredentialDisplayData(mockVerifiableCredential, MockCredentialDetail.credentialDisplays, claims)
        } returns Ok(MockCredentialDetail.credentialDisplayData)
        coEvery {
            mockMapToCredentialClaimCluster(any())
        } returns MockCredentialDetail.listOfCredentialClaimCluster
    }
}
