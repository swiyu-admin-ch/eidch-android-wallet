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

        val expected = safeJson.safeDecodeElementTo<IdentityV1TrustStatement>(validTrustStatement.sdJwtJson).value

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
    private val trustStatementRaw = "eyJ2ZXIiOiIxLjAiLCJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6a2lkIn0.eyJ2Y3QiOiJUcnVzdFN0YXRlbWVudElkZW50aXR5VjEiLCJpc3MiOiJkaWQ6dGR3OmlzcyIsInN1YiI6ImRpZDp0ZHc6c3ViIiwiaWF0IjowLCJzdGF0dXMiOnsic3RhdHVzX2xpc3QiOnsidXJpIjoiaHR0cHM6Ly9leGFtcGxlLm9yZy9hcGkvdjEvc3RhdHVzbGlzdC9pZC5qd3QiLCJ0eXBlIjoiU3dpc3NUb2tlblN0YXR1c0xpc3QtMS4wIiwiaWR4IjoyMjV9fSwiZXhwIjoxNzY3MjI1NjAwLCJuYmYiOjEsImVudGl0eU5hbWUiOnsiZW4iOiJJc3N1ZXIgKGVuKSIsImRlLUNIIjoiSXNzdWVyIChkZS1DSCkifSwicmVnaXN0cnlJZHMiOlt7InR5cGUiOiJ0eXBlMSIsInZhbHVlIjoidmFsdWUxIn0seyJ0eXBlIjoidHlwZTIiLCJ2YWx1ZSI6InZhbHVlMiJ9XSwiaXNTdGF0ZUFjdG9yIjp0cnVlfQ.CJFxgiJGdH67BkGsJ31-2SAoLr5v_abHcbAlCEESs-mPDb3RlQJ6UMIlypA7Yc9BpVmhvFBFggW3x3rL_Fypjg"
    private val trustStatementRaw2 = "eyJ2ZXIiOiIxLjAiLCJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6a2lkIn0.eyJ2Y3QiOiJUcnVzdFN0YXRlbWVudElkZW50aXR5VjEiLCJpc3MiOiJkaWQ6dGR3OmlzcyIsInN1YiI6ImRpZDp0ZHc6c3ViIiwiaWF0IjowLCJzdGF0dXMiOnsic3RhdHVzX2xpc3QiOnsidXJpIjoiaHR0cHM6Ly9leGFtcGxlLm9yZy9hcGkvdjEvc3RhdHVzbGlzdC9pZC5qd3QiLCJ0eXBlIjoiU3dpc3NUb2tlblN0YXR1c0xpc3QtMS4wIiwiaWR4IjoxfX0sImV4cCI6MTc2NzIyNTYwMCwibmJmIjoxLCJlbnRpdHlOYW1lIjp7ImVuIjoiSXNzdWVyIChlbikiLCJkZS1DSCI6Iklzc3VlciAoZGUtQ0gpIn0sInJlZ2lzdHJ5SWRzIjpbeyJ0eXBlIjoidHlwZTEiLCJ2YWx1ZSI6InZhbHVlMSJ9LHsidHlwZSI6InR5cGUyIiwidmFsdWUiOiJ2YWx1ZTIifV0sImlzU3RhdGVBY3RvciI6dHJ1ZX0.ebCxRc2dbgdE-FhDN-n33rqcuruYbCnV4S9MKcG__2lEil1_RUyP8XGRvQItoIMV0NC1iMqUiF9_1Hu9z-2Izw"
    private val trustStatementRawNoIss = "eyJ2ZXIiOiIxLjAiLCJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6a2lkIn0.eyJ2Y3QiOiJUcnVzdFN0YXRlbWVudElkZW50aXR5VjEiLCJzdWIiOiJkaWQ6dGR3OnN1YiIsImlhdCI6MCwic3RhdHVzIjp7InN0YXR1c19saXN0Ijp7InVyaSI6Imh0dHBzOi8vZXhhbXBsZS5vcmcvYXBpL3YxL3N0YXR1c2xpc3QvaWQuand0IiwidHlwZSI6IlN3aXNzVG9rZW5TdGF0dXNMaXN0LTEuMCIsImlkeCI6MX19LCJleHAiOjE3NjcyMjU2MDAsIm5iZiI6MSwiZW50aXR5TmFtZSI6eyJlbiI6Iklzc3VlciAoZW4pIiwiZGUtQ0giOiJJc3N1ZXIgKGRlLUNIKSJ9LCJyZWdpc3RyeUlkcyI6W3sidHlwZSI6InR5cGUxIiwidmFsdWUiOiJ2YWx1ZTEifSx7InR5cGUiOiJ0eXBlMiIsInZhbHVlIjoidmFsdWUyIn1dLCJpc1N0YXRlQWN0b3IiOnRydWV9.bfQ3gXDR0JuwCX0sy3KRZKBKmFn8xPbh6O4WiTQqJwEgzAgVdsZQ51eCiVa_etgs6xNUCQPizc_8Bnc03qCc8w"
    private val trustStatementRawOtherVct = "eyJ2ZXIiOiIxLjAiLCJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6a2lkIn0.eyJ2Y3QiOiJvdGhlclZjdCIsImlzcyI6ImRpZDp0ZHc6aXNzIiwic3ViIjoiZGlkOnRkdzpzdWIiLCJpYXQiOjAsInN0YXR1cyI6eyJzdGF0dXNfbGlzdCI6eyJ1cmkiOiJodHRwczovL2V4YW1wbGUub3JnL2FwaS92MS9zdGF0dXNsaXN0L2lkLmp3dCIsInR5cGUiOiJTd2lzc1Rva2VuU3RhdHVzTGlzdC0xLjAiLCJpZHgiOjF9fSwiZXhwIjoxNzY3MjI1NjAwLCJuYmYiOjEsImVudGl0eU5hbWUiOnsiZW4iOiJJc3N1ZXIgKGVuKSIsImRlLUNIIjoiSXNzdWVyIChkZS1DSCkifSwicmVnaXN0cnlJZHMiOlt7InR5cGUiOiJ0eXBlMSIsInZhbHVlIjoidmFsdWUxIn0seyJ0eXBlIjoidHlwZTIiLCJ2YWx1ZSI6InZhbHVlMiJ9XSwiaXNTdGF0ZUFjdG9yIjp0cnVlfQ._2C_14mySPQlIXhiEDh4MkNHJFcwITf_zqgRQM12a_MP1md3W_qOqgIMOxwN6_gKbLBAgapg71Xf8QXgA8W9Cw"
    private val trustStatementRawNoEntityName = "eyJ2ZXIiOiIxLjAiLCJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6a2lkIn0.eyJ2Y3QiOiJUcnVzdFN0YXRlbWVudElkZW50aXR5VjEiLCJpc3MiOiJkaWQ6dGR3OmlzcyIsInN1YiI6ImRpZDp0ZHc6c3ViIiwiaWF0IjowLCJzdGF0dXMiOnsic3RhdHVzX2xpc3QiOnsidXJpIjoiaHR0cHM6Ly9leGFtcGxlLm9yZy9hcGkvdjEvc3RhdHVzbGlzdC9pZC5qd3QiLCJ0eXBlIjoiU3dpc3NUb2tlblN0YXR1c0xpc3QtMS4wIiwiaWR4IjoxfX0sImV4cCI6MTc2NzIyNTYwMCwibmJmIjoxLCJyZWdpc3RyeUlkcyI6W3sidHlwZSI6InR5cGUxIiwidmFsdWUiOiJ2YWx1ZTEifSx7InR5cGUiOiJ0eXBlMiIsInZhbHVlIjoidmFsdWUyIn1dLCJpc1N0YXRlQWN0b3IiOnRydWV9.fxizXaP7YJpVkFifilmuLTjQAKFw6xqggp2DL9-qW37GEUzAjo9PHBYWECxP8yVCCZhG6ascR-GqMMENOA9e5Q"
    private val validTrustStatement = VcSdJwt(trustStatementRaw)
    private val validTrustStatement2 = VcSdJwt(trustStatementRaw2)
    private val validTrustStatementNoEntityName = VcSdJwt(trustStatementRawNoEntityName)
}
