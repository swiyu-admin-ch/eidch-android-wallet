package ch.admin.foitt.wallet.platform.trustRegistry

import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementType
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustDomainFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustUrlFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.GetTrustUrlFromDidImpl
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.ktor.utils.io.charsets.name
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URLEncoder

class GetTrustUrlFromDidImplTest {

    @MockK
    private lateinit var mockGetTrustDomainFromDid: GetTrustDomainFromDid

    private lateinit var useCase: GetTrustUrlFromDid

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = GetTrustUrlFromDidImpl(mockGetTrustDomainFromDid)

        coEvery { mockGetTrustDomainFromDid(inputWithDomain01) } returns Ok(trustDomain)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A did for a identity trust statement is built correctly`() = runTest {
        val result = useCase(
            trustStatementType = TrustStatementType.IDENTITY,
            actorDid = inputWithDomain01,
            vcSchemaId = null
        ).assertOk()

        val didUrlEncoded = URLEncoder.encode(inputWithDomain01, Charsets.UTF_8.name)
        val expected = "https://example.org/api/v1/truststatements/identity/$didUrlEncoded"

        assertEquals(expected, result.toString())
    }

    @Test
    fun `A did for a issuance trust statement is built correctly`() = runTest {
        val vcSchemaId = "vcSchemaId"
        val result = useCase(
            trustStatementType = TrustStatementType.ISSUANCE,
            actorDid = inputWithDomain01,
            vcSchemaId = vcSchemaId
        ).assertOk()

        val vcSchemaIdUrlEncoded = URLEncoder.encode(vcSchemaId, Charsets.UTF_8.name)
        val expected = "https://example.org/api/v1/truststatements/issuance/?vcSchemaId=$vcSchemaIdUrlEncoded"

        assertEquals(expected, result.toString())
    }

    @Test
    fun `A did for a verification trust statement is built correctly`() = runTest {
        val vcSchemaId = "vcSchemaId"
        val result = useCase(
            trustStatementType = TrustStatementType.VERIFICATION,
            actorDid = inputWithDomain01,
            vcSchemaId = vcSchemaId
        ).assertOk()

        val vcSchemaIdUrlEncoded = URLEncoder.encode(vcSchemaId, Charsets.UTF_8.name)
        val expected = "https://example.org/api/v1/truststatements/verification/?vcSchemaId=$vcSchemaIdUrlEncoded"

        assertEquals(expected, result.toString())
    }

    @Test
    fun `Getting the trust url maps errors from getting the trust domain`() = runTest {
        coEvery {
            mockGetTrustDomainFromDid(any())
        } returns Err(TrustRegistryError.Unexpected(IllegalStateException("error when getting trust domain")))

        useCase(
            trustStatementType = TrustStatementType.VERIFICATION,
            actorDid = inputWithDomain01,
            vcSchemaId = "vcSchemaId",
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    private val domain01 = "some.domain.swiyu.admin.ch"

    private val inputWithDomain01 = "did:tdw:randomid=:$domain01:api:v1:did:randomuuid"

    private val trustDomain = "example.org"
}
