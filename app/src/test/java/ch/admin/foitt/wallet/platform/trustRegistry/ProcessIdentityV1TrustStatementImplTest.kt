package ch.admin.foitt.wallet.platform.trustRegistry

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV1TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementType
import ch.admin.foitt.wallet.platform.trustRegistry.domain.repository.TrustStatementRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustUrlFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityV1TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ProcessIdentityV1TrustStatementImpl
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

class ProcessIdentityV1TrustStatementImplTest {
    @MockK
    private lateinit var mockGetTrustUrlFromDid: GetTrustUrlFromDid

    @MockK
    private lateinit var mockTrustStatementRepository: TrustStatementRepository

    @MockK
    private lateinit var mockValidateTrustStatement: ValidateTrustStatement

    private val safeJson = SafeJsonTestInstance.safeJson

    private lateinit var useCase: ProcessIdentityV1TrustStatement

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = ProcessIdentityV1TrustStatementImpl(
            getTrustUrlFromDid = mockGetTrustUrlFromDid,
            trustStatementRepository = mockTrustStatementRepository,
            validateTrustStatement = mockValidateTrustStatement,
            safeJson = safeJson,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @OptIn(UnsafeResultValueAccess::class)
    @Test
    fun `A IdentityV1 trust statement is correctly processed`() = runTest {
        val result = useCase(issuerDid).assertOk()

        val expected = safeJson.safeDecodeElementTo<IdentityV1TrustStatement>(validTrustStatement.processedJson).value

        assertEquals(expected, result)
    }

    @Test
    fun `Processing IdentityV1 trust statements for multiple valid trust statements returns an error`() = runTest {
        coEvery {
            mockTrustStatementRepository.fetchTrustStatements(any())
        } returns Ok(listOf(trustStatementRaw, trustStatementRaw2))

        coEvery {
            mockValidateTrustStatement(any(), issuerDid)
        } returnsMany listOf(Ok(validTrustStatement), Ok(validTrustStatement2))

        useCase(issuerDid).assertErrorType(TrustRegistryError.InvalidTrustStatus::class)
    }

    @Test
    fun `Processing IdentityV1 trust statements maps errors from getting trust domain`() = runTest {
        coEvery {
            mockGetTrustUrlFromDid(trustStatementType = TrustStatementType.IDENTITY, actorDid = issuerDid, vcSchemaId = null)
        } returns Err(TrustRegistryError.Unexpected(IllegalStateException("get trust url error")))

        useCase(issuerDid).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `Processing IdentityV1 trust statements maps errors from vc sd jwt creation`() = runTest {
        coEvery { mockTrustStatementRepository.fetchTrustStatements(any()) } returns Ok(listOf(trustStatementRawNoIss))

        useCase(issuerDid).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `Processing IdentityV1 trust statements where the statements contain no IdentityV1 statement returns an error`() = runTest {
        coEvery { mockTrustStatementRepository.fetchTrustStatements(any()) } returns Ok(listOf(trustStatementRawOtherVct))

        useCase(issuerDid).assertErrorType(TrustRegistryError.InvalidTrustStatus::class)
    }

    @Test
    fun `Processing IdentityV1 trust statements where all statements are invalid returns an error`() = runTest {
        coEvery {
            mockTrustStatementRepository.fetchTrustStatements(any())
        } returns Ok(listOf(trustStatementRaw, trustStatementRaw2))

        val exception = IllegalStateException("no valid trust statements")
        coEvery {
            mockValidateTrustStatement(any(), any())
        } returns Err(TrustRegistryError.Unexpected(exception))

        useCase(issuerDid).assertErrorType(TrustRegistryError.InvalidTrustStatus::class)
    }

    @Test
    fun `Processing IdentityV1 trust statements maps errors from parsing to IdentityV1`() = runTest {
        coEvery { mockTrustStatementRepository.fetchTrustStatements(any()) } returns Ok(listOf(trustStatementRawNoEntityName))
        coEvery { mockValidateTrustStatement(any(), issuerDid) } returns Ok(validTrustStatementNoEntityName)

        useCase(issuerDid).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    private fun setupDefaultMocks() {
        coEvery {
            mockGetTrustUrlFromDid(trustStatementType = TrustStatementType.IDENTITY, actorDid = issuerDid, vcSchemaId = null)
        } returns Ok(trustUrl)

        coEvery { mockTrustStatementRepository.fetchTrustStatements(any()) } returns Ok(listOf(trustStatementRaw))

        coEvery { mockValidateTrustStatement(any(), issuerDid) } returns Ok(validTrustStatement)
    }

    private val issuerDid = "issuer did"
    private val trustUrl = URL("https://example.org/trust")
    private val trustStatementRaw = "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJ2Y3QiOiJUcnVzdFN0YXRlbWVudElkZW50aXR5VjEiLCJpc3MiOiJkaWQ6dGR3OmlzcyIsImlhdCI6MCwic3RhdHVzIjp7InN0YXR1c19saXN0Ijp7InVyaSI6Imh0dHBzOi8vZXhhbXBsZS5vcmcvYXBpL3YxL3N0YXR1c2xpc3QvaWQuand0IiwidHlwZSI6IlN3aXNzVG9rZW5TdGF0dXNMaXN0LTEuMCIsImlkeCI6MjI1fX0sImV4cCI6MTc2NzIyNTYwMCwibmJmIjoxLCJfc2QiOlsiMFZaS1RfVDZvbG52Y0hJU0ttM0hnbE9OWUN0TTY3QXRUbDcxWnBRUnFDUSIsIjE2eU5jaFQ0bTdDcXVqWUFoT0NyTWVnMFFheDFyRVdsdGgtVmplNjJ4RDAiLCJTT3R0bU5rcXExTmJpaXBBSWNnZGN0aFhoR1I1M0I3bHpQRUF5eHBoQ0dzIiwiYWh0LWxpeGZHVkJHc0wwUWNUaWpCQUd6RUR5cktBVWNTUG12SDlDWXBjTSJdLCJfc2RfYWxnIjoiU0hBLTI1NiJ9.Daualo2lXZfsy5K8rZgfT-9vhpj5TK33zb4M8e3-e_R330oGSONKSpUEdEkrBrbwR4xJRYQdIUvLLRexyNKKBQ~WyI0MDVkYzE5NDkxNTBjYzg4Iiwic3ViIiwiZGlkOnRkdzpzdWIiXQ~WyI2Y2U0ZTg0NjM3NDRhZjE0IiwiZW50aXR5TmFtZSIseyJlbiI6Iklzc3VlciAoZW4pIiwiZGUtQ0giOiJJc3N1ZXIgKGRlLUNIKSJ9XQ~WyI3YjU4YmE2NGZlN2Q3ZGZiIiwicmVnaXN0cnlJZHMiLFt7InR5cGUiOiJ0eXBlMSIsInZhbHVlIjoidmFsdWUxIn0seyJ0eXBlIjoidHlwZTIiLCJ2YWx1ZSI6InZhbHVlMiJ9XV0~WyJkNjBhOTg5NWQ5NWM3NDkyIiwiaXNTdGF0ZUFjdG9yIix0cnVlXQ~"
    private val trustStatementRaw2 = "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJ2Y3QiOiJUcnVzdFN0YXRlbWVudElkZW50aXR5VjEiLCJpc3MiOiJkaWQ6dGR3OmlzcyIsImlhdCI6MCwic3RhdHVzIjp7InN0YXR1c19saXN0Ijp7InVyaSI6Imh0dHBzOi8vZXhhbXBsZS5vcmcvYXBpL3YxL3N0YXR1c2xpc3QvaWQuand0IiwidHlwZSI6IlN3aXNzVG9rZW5TdGF0dXNMaXN0LTEuMCIsImlkeCI6MX19LCJleHAiOjE3NjcyMjU2MDAsIm5iZiI6MSwiX3NkIjpbIlM0aVU5MHN1bVNnbU0wNTNXSWRiTzVyZmJrSHphTUFSbi05QkxZREt0S0UiLCJYaTdyVGUyRzhHNVlUenlQU1l0ZGo5bHlhelJGRXB1VGNFODNwa19KUzRBIiwiYjNpOURLaGlZRDVTdHgxQ2stRWlaQ09Uc0FWTmMwRnlDbkVlS3hUWGNMYyIsImpoZjZVbVBYVUgtdzFYeHNQOHNoM1BTLWRQdUYxVHJxT3hJbEZOYWxESG8iXSwiX3NkX2FsZyI6IlNIQS0yNTYifQ.lrAf-7UlnlNkivSCmEc6EwZmI5NCiWzBKc7WsReZQqk5mtaSnh4OquYXl2CefD2o3hXiAZUBa_ruL4PCOK1gEA~WyI1Njg4ODY1YjA4M2IyZjk5Iiwic3ViIiwiZGlkOnRkdzpzdWIiXQ~WyIyOTliN2E4OTRjZDQ3Y2JjIiwiZW50aXR5TmFtZSIseyJlbiI6Iklzc3VlciAoZW4pIiwiZGUtQ0giOiJJc3N1ZXIgKGRlLUNIKSJ9XQ~WyI4MDFlMGVmMGY4MjdlNmQ4IiwicmVnaXN0cnlJZHMiLFt7InR5cGUiOiJ0eXBlMSIsInZhbHVlIjoidmFsdWUxIn0seyJ0eXBlIjoidHlwZTIiLCJ2YWx1ZSI6InZhbHVlMiJ9XV0~WyIyY2RiODA0MjA2MzI4NDNhIiwiaXNTdGF0ZUFjdG9yIix0cnVlXQ~"
    private val trustStatementRawNoIss = "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJ2Y3QiOiJUcnVzdFN0YXRlbWVudElkZW50aXR5VjEiLCJpYXQiOjAsInN0YXR1cyI6eyJzdGF0dXNfbGlzdCI6eyJ1cmkiOiJodHRwczovL2V4YW1wbGUub3JnL2FwaS92MS9zdGF0dXNsaXN0L2lkLmp3dCIsInR5cGUiOiJTd2lzc1Rva2VuU3RhdHVzTGlzdC0xLjAiLCJpZHgiOjF9fSwiZXhwIjoxNzY3MjI1NjAwLCJuYmYiOjEsIl9zZCI6WyJDVEVFU1RfVVNGY3JMOFR1YUl1NXFQZ0MyQUdHd0RKbmVkcHZHTjdkb1JVIiwiUGhzZTJUcG9yR3Y0dUE3VW9NM2NMLWV0alE0aFJCb3pDN2Z2d2t2OVZ4QSIsImJJTGpHeEFKWnU3bTI1a2ZuVHlPYTFyZFNwa1JqNmFxb3ZFYlNrLVRqVXMiLCJ4bVY4aUJ4WW1IV1U4NVVmNjdkaHF5bG5hY0Q1UEtZRnNLSlMtQzF4TWQ4Il0sIl9zZF9hbGciOiJTSEEtMjU2In0.w-u0ghDKiz3m8tzYpaL4_frItQLkK_KyOf31sC1s-E7Q0zysfbcIafmTD9JvrIxeHmcaGNyDjWQM14YoehH56g~WyJhYmY4ZjczOWNmYTA2ZDgyIiwic3ViIiwiZGlkOnRkdzpzdWIiXQ~WyJmZTc1MzBlNjFkZjcyNWJhIiwiZW50aXR5TmFtZSIseyJlbiI6Iklzc3VlciAoZW4pIiwiZGUtQ0giOiJJc3N1ZXIgKGRlLUNIKSJ9XQ~WyIxY2E4Yjk5YjU4OGZiMDQ0IiwicmVnaXN0cnlJZHMiLFt7InR5cGUiOiJ0eXBlMSIsInZhbHVlIjoidmFsdWUxIn0seyJ0eXBlIjoidHlwZTIiLCJ2YWx1ZSI6InZhbHVlMiJ9XV0~WyIwYzRhZmVkMmJmMWMyZDliIiwiaXNTdGF0ZUFjdG9yIix0cnVlXQ~"
    private val trustStatementRawOtherVct = "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJ2Y3QiOiJvdGhlclZjdCIsImlzcyI6ImRpZDp0ZHc6aXNzIiwiaWF0IjowLCJzdGF0dXMiOnsic3RhdHVzX2xpc3QiOnsidXJpIjoiaHR0cHM6Ly9leGFtcGxlLm9yZy9hcGkvdjEvc3RhdHVzbGlzdC9pZC5qd3QiLCJ0eXBlIjoiU3dpc3NUb2tlblN0YXR1c0xpc3QtMS4wIiwiaWR4IjoxfX0sImV4cCI6MTc2NzIyNTYwMCwibmJmIjoxLCJfc2QiOlsiRndjajdGM1NlN21HMVRHby16OHNCUzZ1UVhCa2FNR1lnRUdiaFF0OEphcyIsIkhXOTQxYTdqekFnVlNYZEdoUDFJMThvRm01dHlTQ1FGWF9TenFaYnVyZ28iLCJSZjlvQ0I4T2R1eG9TREJHbWwxV2RMRjdBT184ZHZJelRucGk2cXJpOUhjIiwibWhxVGJxa0tpTTZIZUc4WXdtMXlCVDNaX3FqZE1Gdl9PZk8xX0duaEJaYyJdLCJfc2RfYWxnIjoiU0hBLTI1NiJ9.mt53HtQ0HowcJrpgEQLPEQ2ILWrHZZE1Wx49hSbUlS5CyaiWIIyYElEi8ejEphwqBdYpwwX2eVcUtaBS3fl5hQ~WyI2YzkzYjQxYzRjZGM4N2EyIiwic3ViIiwiZGlkOnRkdzpzdWIiXQ~WyIxZjJiN2M4NDA5ZmVhZDBkIiwiZW50aXR5TmFtZSIseyJlbiI6Iklzc3VlciAoZW4pIiwiZGUtQ0giOiJJc3N1ZXIgKGRlLUNIKSJ9XQ~WyIwNWVmMmRkMjc2YmNiYjA2IiwicmVnaXN0cnlJZHMiLFt7InR5cGUiOiJ0eXBlMSIsInZhbHVlIjoidmFsdWUxIn0seyJ0eXBlIjoidHlwZTIiLCJ2YWx1ZSI6InZhbHVlMiJ9XV0~WyI4ZDUzNzFkM2UzY2JlZTUxIiwiaXNTdGF0ZUFjdG9yIix0cnVlXQ~"
    private val trustStatementRawNoEntityName = "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJ2Y3QiOiJUcnVzdFN0YXRlbWVudElkZW50aXR5VjEiLCJpc3MiOiJkaWQ6dGR3OmlzcyIsImlhdCI6MCwic3RhdHVzIjp7InN0YXR1c19saXN0Ijp7InVyaSI6Imh0dHBzOi8vZXhhbXBsZS5vcmcvYXBpL3YxL3N0YXR1c2xpc3QvaWQuand0IiwidHlwZSI6IlN3aXNzVG9rZW5TdGF0dXNMaXN0LTEuMCIsImlkeCI6MX19LCJleHAiOjE3NjcyMjU2MDAsIm5iZiI6MSwiX3NkIjpbIk5HbEJPOGJaMTJ0aGJnSVpoVE9RT1JqV1ZwZ0RVY204bTBoUDRmVXRGNDQiLCJXbG5kU0JoZUNHbDdkVWRnSFZUMG9POHJJUnAwM3ROLWktX2tOQlJpdW1JIiwiWnpIMUN6YVNMYTdLUmQ2VnVXNjVYbVBOUzJUN2xJUS1iVWhiR0JJbDJYbyJdLCJfc2RfYWxnIjoiU0hBLTI1NiJ9.RooHtjCedHE0egKLO2T7MyFOp20qDuvHN0Zz5LPTgL2D2oAeEfAcquO3ZHdGUVgTECOX4RgZ-CDMOd-gj82aWA~WyJhNDI0M2FjNzFjMmFjY2U0Iiwic3ViIiwiZGlkOnRkdzpzdWIiXQ~WyI2OWVkNGM5OGZhY2Q1MjFlIiwicmVnaXN0cnlJZHMiLFt7InR5cGUiOiJ0eXBlMSIsInZhbHVlIjoidmFsdWUxIn0seyJ0eXBlIjoidHlwZTIiLCJ2YWx1ZSI6InZhbHVlMiJ9XV0~WyIxNjk5ZWY5YTFjYTY1ZTZhIiwiaXNTdGF0ZUFjdG9yIix0cnVlXQ~"
    private val validTrustStatement = VcSdJwt(trustStatementRaw)
    private val validTrustStatement2 = VcSdJwt(trustStatementRaw2)
    private val validTrustStatementNoEntityName = VcSdJwt(trustStatementRawNoEntityName)
}
