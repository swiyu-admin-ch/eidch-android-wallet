package ch.admin.foitt.wallet.platform.trustRegistry

import ch.admin.foitt.openid4vc.domain.model.jwt.JwtError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwt
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignatureFromDid
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.FetchCredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustDomainFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ValidateTrustStatementImpl
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErr
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ValidateTrustStatementImplTest {

    @MockK
    private lateinit var mockGetTrustDomainFromDid: GetTrustDomainFromDid

    @MockK
    private lateinit var mockEnvironmentSetup: EnvironmentSetupRepository

    @MockK
    private lateinit var mockVerifyJwtSignatureFromDid: VerifyJwtSignatureFromDid

    @MockK
    private lateinit var mockFetchCredentialStatus: FetchCredentialStatus

    private val testSafeJson = SafeJsonTestInstance.safeJson

    private lateinit var useCase: ValidateTrustStatement

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = ValidateTrustStatementImpl(
            getTrustDomainFromDid = mockGetTrustDomainFromDid,
            environmentSetupRepo = mockEnvironmentSetup,
            verifyJwtSignatureFromDid = mockVerifyJwtSignatureFromDid,
            safeJson = testSafeJson,
            fetchCredentialStatus = mockFetchCredentialStatus
        )

        coEvery {
            mockGetTrustDomainFromDid(VALID_ACTOR_DID)
        } returns Ok(trustDomain)

        coEvery {
            mockEnvironmentSetup.trustRegistryTrustedDids
        } returns trustedDids

        coEvery { mockVerifyJwtSignatureFromDid.invoke(did = any(), kid = any(), jwt = any()) } returns Ok(Unit)
        coEvery {
            mockFetchCredentialStatus.invoke(credentialIssuer = any(), properties = any())
        } returns Ok(CredentialStatus.VALID)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A valid trust statement pass validation`(): Unit = runTest {
        val vcSdJwt = VcSdJwt(VALID_TRUST_STATEMENT)
        useCase(vcSdJwt, VALID_ACTOR_DID).assertOk()

        coVerify(exactly = 1) {
            mockEnvironmentSetup.trustRegistryTrustedDids
            mockVerifyJwtSignatureFromDid.invoke(did = any(), kid = any(), jwt = any())
        }
    }

    @Test
    fun `A trust statement not whitelisted fails validation`(): Unit = runTest {
        val vcSdJwt = VcSdJwt(OTHER_ISSUER_TRUST_STATEMENT)
        useCase(vcSdJwt, VALID_ACTOR_DID).assertErr()

        coVerify(exactly = 1) {
            mockGetTrustDomainFromDid(VALID_ACTOR_DID)
            mockEnvironmentSetup.trustRegistryTrustedDids
        }
        coVerify(exactly = 0) {
            mockVerifyJwtSignatureFromDid.invoke(did = any(), kid = any(), jwt = any())
        }
    }

    @Test
    fun `A trust statement where getting the trust domain fails is considered not trusted`(): Unit = runTest {
        val exception = IllegalStateException("trust domain error")
        coEvery {
            mockGetTrustDomainFromDid(VALID_ACTOR_DID)
        } returns Err(TrustRegistryError.Unexpected(exception))

        val vcSdJwt = VcSdJwt(VALID_TRUST_STATEMENT)
        useCase(vcSdJwt, VALID_ACTOR_DID).assertErr()

        coVerify(exactly = 1) {
            mockGetTrustDomainFromDid(VALID_ACTOR_DID)
        }
        coVerify(exactly = 0) {
            mockEnvironmentSetup.trustRegistryTrustedDids
            mockVerifyJwtSignatureFromDid.invoke(did = any(), kid = any(), jwt = any())
        }
    }

    @Test
    fun `A trust statement declaring the wrong type fails validation`(): Unit = runTest {
        val vcSdJwt = VcSdJwt(WRONG_TYPE_TRUST_STATEMENT)
        useCase(vcSdJwt, VALID_ACTOR_DID).assertErr()
    }

    @Test
    fun `A trust statement declaring the wrong algorithm fails validation`(): Unit = runTest {
        val vcSdJwt = VcSdJwt(WRONG_ALGO_TRUST_STATEMENT)
        useCase(vcSdJwt, VALID_ACTOR_DID).assertErr()
    }

    @Test
    fun `A trust statement with an invalid signature fails validation`(): Unit = runTest {
        coEvery {
            mockVerifyJwtSignatureFromDid.invoke(did = any(), kid = any(), jwt = any())
        } returns Err(JwtError.InvalidJwt)

        val vcSdJwt = VcSdJwt(VALID_TRUST_STATEMENT)
        useCase(vcSdJwt, VALID_ACTOR_DID).assertErr()

        coVerify(exactly = 1) {
            mockEnvironmentSetup.trustRegistryTrustedDids
            mockVerifyJwtSignatureFromDid.invoke(did = any(), kid = any(), jwt = any())
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            MISSING_IAT,
            MISSING_EXP,
            MISSING_NBF,
            MISSING_SUB
        ]
    )
    fun `A trust statement missing a mandatory reserved claim fails validation`(sdJwt: String): Unit = runTest {
        val vcSdJwt = VcSdJwt(sdJwt)
        useCase(vcSdJwt, VALID_ACTOR_DID).assertErr()
    }

    @Test
    fun `A trust statement where the subject does not match the actor did fails validation`() = runTest {
        val invalidSubjectVcSdJwt = VcSdJwt(INVALID_SUBJECT)
        useCase(invalidSubjectVcSdJwt, VALID_ACTOR_DID).assertErr()
        val vcSdJwt = VcSdJwt(VALID_TRUST_STATEMENT)
        useCase(vcSdJwt, "invalid actor did").assertErr()
    }

    @Test
    fun `A trust statement with an invalid validity fails validation`(): Unit = runTest {
        val vcSdJwt = VcSdJwt(EXPIRED)
        useCase(vcSdJwt, VALID_ACTOR_DID).assertErr()
    }

    @Test
    fun `A trust statement with an unsupported vct claim value fails validation`(): Unit = runTest {
        val vcSdJwt = VcSdJwt(WRONG_VCT_VALUE)
        useCase(vcSdJwt, VALID_ACTOR_DID).assertErr()
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            MISSING_ORG_NAME,
            MISSING_PREF_LANG,
        ]
    )
    fun `A trust statement missing a mandatory claim fails validation`(sdJwt: String): Unit = runTest {
        val vcSdJwt = VcSdJwt(sdJwt)
        useCase(vcSdJwt, VALID_ACTOR_DID).assertErr()
    }

    @Test
    fun `A trust statement with an unexpected type fails validation`(): Unit = runTest {
        val vcSdJwt = VcSdJwt(WRONG_FIELD_TYPES)
        useCase(vcSdJwt, VALID_ACTOR_DID).assertErr()
    }

    @Test
    fun `A trust statement missing status properties fails validation`(): Unit = runTest {
        val vcSdJwt = VcSdJwt(MISSING_STATUS_PROPERTIES)
        useCase(vcSdJwt, VALID_ACTOR_DID).assertErr()

        coVerify(exactly = 0) {
            mockFetchCredentialStatus.invoke(any(), any())
        }
    }

    @Test
    fun `A failed status list call fails validation`(): Unit = runTest {
        coEvery { mockFetchCredentialStatus.invoke(any(), any()) } returns Err(CredentialStatusError.NetworkError)

        val vcSdJwt = VcSdJwt(VALID_TRUST_STATEMENT)
        useCase(vcSdJwt, VALID_ACTOR_DID).assertErr()

        coVerify(exactly = 1) {
            mockFetchCredentialStatus.invoke(any(), any())
        }
    }

    @Test
    fun `A trust statement with any other status than valid fails validation`(): Unit = runTest {
        coEvery { mockFetchCredentialStatus.invoke(any(), any()) } returns Ok(CredentialStatus.SUSPENDED)

        val vcSdJwt = VcSdJwt(VALID_TRUST_STATEMENT)
        useCase(vcSdJwt, VALID_ACTOR_DID).assertErr()

        coVerify(exactly = 1) {
            mockFetchCredentialStatus.invoke(any(), any())
        }
    }

    private val trustDomain = "example.org"
    private val trustedDids = mapOf(
        trustDomain to listOf("did:tdw:aaa", "did:tdw:abc")
    )

    companion object {
        private const val VALID_ACTOR_DID = "did:tdw:abcd"

        private const val VALID_TRUST_STATEMENT =
            "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJpc3MiOiJkaWQ6dGR3OmFiYyIsIm5iZiI6MCwiZXhwIjo5OTk5OTk5OTk5LCJpYXQiOjAsIl9zZF9hbGciOiJTSEEtMjU2IiwidmN0IjoiVHJ1c3RTdGF0ZW1lbnRNZXRhZGF0YVYxIiwic3RhdHVzIjp7InN0YXR1c19saXN0Ijp7ImlkeCI6MCwidXJpIjoidXJpIn19LCJfc2QiOlsiSnk2N3FsZDQ5dHF5VU85X2YySGZMNkpIWDRMUDZnQXdaSi1VMDNJTVdiOCIsIlZkdFhzaW0tUWRCb3hKUml1NXV1cGluTEExRUZ1cVVscU5tWDVQQTFZVGMiLCJWeGJpaUI0bEdfcF9JMDY2dzdCVHVEYmw3SXJnNjY2eU5telV0NWVGdGk0Iiwiby05Y2QyLUJDMWh0X001RV8wUHdqTGRjaHB3MVdqdF9IVUs3b2ltbHNKYyJdfQ._2YmVMQyCyLtBL7AuvE-Pk4OBpIBKa0IxIx6lpzY71w0op9SZUqVThEGGKYVJEQb1pB1-WlqIpyKMSFyCaHUUw~WyIwZDRlNTZkODg4MjkwOWZhIiwic3ViIiwiZGlkOnRkdzphYmNkIl0~WyJmMjE0MjIzMDllMjRjMmI5Iiwib3JnTmFtZSIseyJlbiI6Im9yZ05hbWUgRW4iLCJkZS1DSCI6Im9yZ05hbWUgRGUifV0~WyI4OTEyZmYwYzhhNGQwNDhmIiwicHJlZkxhbmciLCJkZSJd~WyI1NTQ2MjQ0MjBjYTZjMjRlIiwibG9nb1VyaSIseyJlbiI6ImxvZ29VcmlFbiIsImRlIjoibG9nb1VyaURlIn1d~"
        private const val OTHER_ISSUER_TRUST_STATEMENT =
            "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJpc3MiOiJkaWQ6dGR3Om90aGVySXNzdWVyIiwibmJmIjowLCJleHAiOjk5OTk5OTk5OTksImlhdCI6MCwiX3NkX2FsZyI6IlNIQS0yNTYiLCJ2Y3QiOiJUcnVzdFN0YXRlbWVudE1ldGFkYXRhVjEiLCJzdGF0dXMiOnsic3RhdHVzX2xpc3QiOnsiaWR4IjowLCJ1cmkiOiJ1cmkifX0sIl9zZCI6WyIzOEVKeU1Fbm5SNkp4RHJWMmpQMnFqdmZOdFpqQ0NnUHRZUFpLSXdLUkNNIiwiOVZZeF9HTkVVOTNqTU42WXhJVVJGUGlvd3gySE1FdG1iSFprX3k0bmlodyIsIkNvS3FneFFtcXVrWHdEY3FiNTN0VjEzeUNXQmpsZ0NQUHdEMzdHU1FLSFUiLCJ2TFB1VktRcEZ2ajlPUUV2UTdZUGJ3aG1xNW4zU01UdFdCU0FkOGJ0LUhjIl19.ZBo_4UIn1MEJcgsmk-0OkY--7pDM5za5TjObaQw9E4rLY_Gq9EJBSg872l2g-Bxw8m4_vB4vZheNTD2dtGfBHg~WyI4ZWJiOTc4ODU5MWVkNGY0Iiwic3ViIiwiZGlkOnRkdzphYmNkIl0~WyIxY2EzMzYzY2IwOGE5NjM3Iiwib3JnTmFtZSIseyJlbiI6Im9yZ05hbWUgRW4iLCJkZS1DSCI6Im9yZ05hbWUgRGUifV0~WyI0ZDY5OWQ0NjdiMmFmZjE0IiwicHJlZkxhbmciLCJkZSJd~WyJkZmQ4MzA1ODQzY2FlMzRkIiwibG9nb1VyaSIseyJlbiI6ImxvZ29VcmlFbiIsImRlIjoibG9nb1VyaURlIn1d~"
        private const val WRONG_TYPE_TRUST_STATEMENT =
            "eyJ0eXAiOiJzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJpc3MiOiJkaWQ6dGR3OmFiYyIsIm5iZiI6MCwiZXhwIjo5OTk5OTk5OTk5LCJpYXQiOjAsIl9zZF9hbGciOiJTSEEtMjU2IiwidmN0IjoiVHJ1c3RTdGF0ZW1lbnRNZXRhZGF0YVYxIiwiX3NkIjpbIkhRRlNpcG81enI5Q2VtVHZSUERMQlVjYzc1bV9FX3NfcFowWEQtYnNLbGciLCJVMDRIUVNadjJaNGVTNTBEbzlIdWlfRUFBcGxQa29sQ0U3ZktJUDI3VWI0IiwibFQ4Y1h2aUpZaGx6NHVsRXFhZ0ZOVWpDNmJTT1Y0QjBqV0NhV2IyY0JBTSIsIm1WR1k3SVYzUVJzU2M2Zk5IbWJuV0M1aHJUTnJSVWIybU8zMHRZTWtpclkiXX0.0RSmdpxgAzzxMvRLpUeosD_WNedvP_O2exMccY1b323q9bJ8ibCk3NNpSyAkJkoKEaRrF37Czy3bVhCezJIIkw~WyIwYTQ3OWZiMzI4N2QwYzE0Iiwic3ViIiwiZGlkOnRkdzphYmNkIl0~WyI3YTA1ZTAwMmI0NTEyYjZkIiwib3JnTmFtZSIseyJlbiI6Im9yZ05hbWUgRW4iLCJkZS1DSCI6Im9yZ05hbWUgRGUifV0~WyI5YjhkMzQxYWRlNDBlODM4IiwicHJlZkxhbmciLCJkZSJd~WyJjNjIwN2IyYWE4NWU2YjE3IiwibG9nb1VyaSIseyJlbiI6ImxvZ29VcmlFbiIsImRlIjoibG9nb1VyaURlIn1d~"
        private const val WRONG_ALGO_TRUST_STATEMENT =
            "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJpc3MiOiJkaWQ6dGR3OmFiYyIsIm5iZiI6MCwiZXhwIjo5OTk5OTk5OTk5LCJpYXQiOjAsIl9zZF9hbGciOiJTSEEtMjU2IiwidmN0IjoiVHJ1c3RTdGF0ZW1lbnRNZXRhZGF0YVYxIiwiX3NkIjpbIjJuUTJaV1JRVzVOT0Z1amxDQzFwUERPbVliQ0NTOXdUZi1tWUZTMmJGa0UiLCI4ZVR1cUxvdlVnSEQ3QmdtbDhCeXdocHl6UWdncE9PTVFwM0JOcFN5TXJNIiwiUVptLTV3NUh5MFFZZUlWazVRUGYyWkRNTTdSNnRPQmh3QzQ2dnZ1M3JlTSIsIlU5cUhVcXlHRVZkckxCbHktNy14OG1xUEFra08zY3djTHN3bHV0TGNQOEkiXX0.a-fZpWMU_Qct-w51-et0Nx5sunSFF81xRNHBXtz3used-4fgRF7ahX0d6Dc0n6LyNpLP7gQG7cQMrE0ma9H9dw~WyI0ZjdiNmIzMTBmZThjZDQ3Iiwic3ViIiwiZGlkOnRkdzphYmNkIl0~WyIyNjk3MjIwNTAyYmU1N2I5Iiwib3JnTmFtZSIseyJlbiI6Im9yZ05hbWUgRW4iLCJkZS1DSCI6Im9yZ05hbWUgRGUifV0~WyI1YzA3YTIyZDBkY2ExNjcxIiwicHJlZkxhbmciLCJkZSJd~WyI5ZTVhNTk0M2UyNGYwMDNkIiwibG9nb1VyaSIseyJlbiI6ImxvZ29VcmlFbiIsImRlIjoibG9nb1VyaURlIn1d~"
        private const val MISSING_IAT =
            "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJpc3MiOiJkaWQ6dGR3OmFiYyIsIm5iZiI6MCwiZXhwIjo5OTk5OTk5OTk5LCJfc2RfYWxnIjoiU0hBLTI1NiIsInZjdCI6IlRydXN0U3RhdGVtZW50TWV0YWRhdGFWMSIsIl9zZCI6WyI2YUpzUHFtOGVKTWtrQmxwTjBITlFQWEwxRVhrRTh5cHMwR1JjMkd6R3hvIiwiOHh4MzVaVExtOTNsb1V4bFJ3dEY3UHdFSi1VY3FaaGs5d3BVVG4xQWVrayIsIllJVV9XSmRrMXhleUxSWXcySjR2Nnlob2IyQXY3YkRFVjZIbUhONVk1VVUiLCJ3Mnd4OVVCZzFOeEdiNEp5dXYwQTg4blhtSEtLa19zUktzQnlqZGJEV3VFIl19.6aIFJ1jBKnvSFLySY-7ryGDo8VO4DmqOrlBJVo_WOFnRjHIoozAvo3NHY2jEQbztCa4shFnWW75dOASr6wqAUA~WyJjODc5ZTBkYzIyNzYxOTU1Iiwic3ViIiwiZGlkOnRkdzphYmNkIl0~WyJkMTZjODM2NjQwNDgyOGIwIiwib3JnTmFtZSIseyJlbiI6Im9yZ05hbWUgRW4iLCJkZS1DSCI6Im9yZ05hbWUgRGUifV0~WyJkNzMzYzljNTI3ZDA4NGY1IiwicHJlZkxhbmciLCJkZSJd~WyJlMjAyMDg5ZTVhYzI5OWJhIiwibG9nb1VyaSIseyJlbiI6ImxvZ29VcmlFbiIsImRlIjoibG9nb1VyaURlIn1d~"
        private const val MISSING_NBF =
            "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJpc3MiOiJkaWQ6dGR3OmFiYyIsImV4cCI6OTk5OTk5OTk5OSwiaWF0IjowLCJfc2RfYWxnIjoiU0hBLTI1NiIsInZjdCI6IlRydXN0U3RhdGVtZW50TWV0YWRhdGFWMSIsIl9zZCI6WyI3cVBocEg3bC1wdWtYNUVEOGhRZEk4OGtxM09tbldYT3hHQTRmNWZ2UktRIiwiWkJZQkE2b2xIMkRGcUh0WFhWU3ZvRmdzaFpvMlNRaGNaUE9VWWlVQWRKdyIsIm1faGxQdWYyYXZJeUxyZGY0YkZxLWZvRnplZzhLbEJqX3RaZzNLenZOclkiLCJ4UFlFTnFWY2pyWndZNnhPcE44M3BLSjFmUTFWM3Q0T3YxNUJ0N2h2ek44Il19.1nCL4KvAdoOyWPNTlm-BRHLPeiJ35GCVonsKerOh_EDF9IVa445h41mDRc9DdpJfoBvW6Oom9FnoPUE9SuwN5A~WyIyOWIxNDEwNjY4MDJmMjNmIiwic3ViIiwiZGlkOnRkdzphYmNkIl0~WyI1ZjcxODEzZTlmNzZkOWM4Iiwib3JnTmFtZSIseyJlbiI6Im9yZ05hbWUgRW4iLCJkZS1DSCI6Im9yZ05hbWUgRGUifV0~WyIyNTAwYmNlZmE0MjBhNjE1IiwicHJlZkxhbmciLCJkZSJd~WyI2MjEwMWNmZDkzOTUxNzZmIiwibG9nb1VyaSIseyJlbiI6ImxvZ29VcmlFbiIsImRlIjoibG9nb1VyaURlIn1d~"
        private const val MISSING_EXP =
            "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJpc3MiOiJkaWQ6dGR3OmFiYyIsIm5iZiI6MCwiaWF0IjowLCJfc2RfYWxnIjoiU0hBLTI1NiIsInZjdCI6IlRydXN0U3RhdGVtZW50TWV0YWRhdGFWMSIsIl9zZCI6WyJZYkxJSnRpVjlMbTlGcEkzYUY2OFVfbENnWWZONG1TWG5TSEgyb0hnODR3IiwiX05McnhxV25iRmdDUUhtTkRHSTZhRVZKZk1EQThZZ0JYam0wR2ZqTU5NTSIsImI0dnJxbzFjQkRMOGJ2ZVlqcUFoX0s1THNLa25TMjhmaDk0RzlIbU1ab28iLCJuNl9QbG5namNvY3Nfa0dkQk9SM3RxUDNyQTByMVk0SG9tNDY0UTVad0lFIl19.nq_qoo9l584FARfvy4qCjlusiK-G5SWYw9FmyUfJvz_YLTWe9r6dSvnheA_JyThnW9LlXGfz3f7VjTBnAb4trQ~WyJiNDJhM2M2NTYzNWNiZmE3Iiwic3ViIiwiZGlkOnRkdzphYmNkIl0~WyI4MmE0YjJhMDQ0YzA5ODA5Iiwib3JnTmFtZSIseyJlbiI6Im9yZ05hbWUgRW4iLCJkZS1DSCI6Im9yZ05hbWUgRGUifV0~WyJkZGZjZTBmNmIyNWIzMGY1IiwicHJlZkxhbmciLCJkZSJd~WyJhMDgwZWJmNTM4OWMxYTJjIiwibG9nb1VyaSIseyJlbiI6ImxvZ29VcmlFbiIsImRlIjoibG9nb1VyaURlIn1d~"
        private const val MISSING_SUB =
            "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJpc3MiOiJkaWQ6dGR3OmFiYyIsIm5iZiI6MCwiZXhwIjo5OTk5OTk5OTk5LCJpYXQiOjAsIl9zZF9hbGciOiJTSEEtMjU2IiwidmN0IjoiVHJ1c3RTdGF0ZW1lbnRNZXRhZGF0YVYxIiwic3RhdHVzIjp7InN0YXR1c19saXN0Ijp7ImlkeCI6MCwidXJpIjoidXJpIn19LCJfc2QiOlsiM0VfQ2tLVC04VERKX1pFSE9RcTJrTjhMNURmWGdESFNIdGw5NGxuamdHQSIsIkNCUllPQ2Q4TlpmS25JRThkejBSbVdKLXpSTk84VkhqWHp2QjJhZVNnX0EiLCJhazhxa2RRbzBLYlVUd1RwZmpZbXBEOW9OWnFWc2lDbXg5M0w2Vm5SaXFFIl19.GCZ-How8uwYRg51w0nwORMEXvYNXqvyc2e5fvwou-DFAx2ca0k3hn-DuclLb4yABKJqMQp7cfH5a0P-e5rkF4A~WyJiMTg1ZTYwZjkzN2Q0NDE3Iiwib3JnTmFtZSIseyJlbiI6Im9yZ05hbWUgRW4iLCJkZS1DSCI6Im9yZ05hbWUgRGUifV0~WyI0Y2YxYmRjMWFkZGI3ZmFkIiwicHJlZkxhbmciLCJkZSJd~WyI5NWQ5ZTc1ZTMxMTNhY2RmIiwibG9nb1VyaSIseyJlbiI6ImxvZ29VcmlFbiIsImRlIjoibG9nb1VyaURlIn1d~"
        private const val INVALID_SUBJECT =
            "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJpc3MiOiJkaWQ6dGR3OmFiYyIsIm5iZiI6MCwiZXhwIjo5OTk5OTk5OTk5LCJpYXQiOjAsIl9zZF9hbGciOiJTSEEtMjU2IiwidmN0IjoiVHJ1c3RTdGF0ZW1lbnRNZXRhZGF0YVYxIiwic3RhdHVzIjp7InN0YXR1c19saXN0Ijp7ImlkeCI6MCwidXJpIjoidXJpIn19LCJfc2QiOlsiQ0pXdVF1YzlGd0loYy1TOUt5V19Cem9wTWFweXhuNmc5U3lNR09ENXRlQSIsIlk3aXM3VlQ3blRvb0QyNkw2eDh4UXg5c2hCbUc0NU9BMktxLWpsMGE1R2siLCJsLXQ3U3hZc0sxZEdxTDl1bDVxOWZtaVhEN3pDbWZodUxUQ0dTclctZnNzIiwidzVNcDBnbm9NWjI4VWt0RTNGOE5ZZmktNk5iTnhXOG9mRFA4X282bnVYMCJdfQ.XY9bIo4KSJDu57iIrFgRO8E-n3DzUX-U4iWU_-7-o3NP3_NCeApLXKQqUbVmqC2cQ6xZ6ooxaJiSgw4tq9jsQg~WyI4OGM3ZjI3NjZjMmQ3YjVjIiwic3ViIiwiZGlkOnRkdzppbnZhbGlkIl0~WyIyODIwYzZjYTc0MzdlM2MxIiwib3JnTmFtZSIseyJlbiI6Im9yZ05hbWUgRW4iLCJkZS1DSCI6Im9yZ05hbWUgRGUifV0~WyJjNTJjZDJlYjAzOTI2ODNhIiwicHJlZkxhbmciLCJkZSJd~WyJlNTMwYTU5YzIwMDhhYjljIiwibG9nb1VyaSIseyJlbiI6ImxvZ29VcmlFbiIsImRlIjoibG9nb1VyaURlIn1d~"
        private const val EXPIRED =
            "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJpc3MiOiJkaWQ6dGR3OmFiYyIsIm5iZiI6MCwiZXhwIjoxLCJpYXQiOjAsIl9zZF9hbGciOiJTSEEtMjU2IiwidmN0IjoiVHJ1c3RTdGF0ZW1lbnRNZXRhZGF0YVYxIiwiX3NkIjpbIi00R0MySDAwRko0bzN4anhaMEx5aXFteHllZXVkR0h1bHJGYWlfRGplYkkiLCJQdTRVckNGX1B0WnhmdDRUMWVNekliTV9ibG1hUS1TWkQ2Wk1xR01nZ1MwIiwibGxWM3VmV2pCR1otaEFrV1VETmRMQ1ZFTWhwMDBReThVMEFGTnBMdTZ3SSIsInJQQjk4ZFh2RkRVN2Q0Y0gxTWttak0zRGp5b1Y3cklGZmZCMElUbWE4b28iXX0.dm836pbXWxLEzYFxnyhaDg6p87GffqRLBdBohtBHH6V3bEgQ8svIDn877NsCt-nVziEeH5x6_PLjIV77GT9-Wg~WyIzYTdmNDQyNzdlZDlkMmY3Iiwic3ViIiwiZGlkOnRkdzphYmNkIl0~WyI0NDQ4ZDg1MmQzM2VlOGZmIiwib3JnTmFtZSIseyJlbiI6Im9yZ05hbWUgRW4iLCJkZS1DSCI6Im9yZ05hbWUgRGUifV0~WyI0ZTM2YzM0MjhhNWFhMDRiIiwicHJlZkxhbmciLCJkZSJd~WyIxNzQ4MDU0MTYyYjg2YmU3IiwibG9nb1VyaSIseyJlbiI6ImxvZ29VcmlFbiIsImRlIjoibG9nb1VyaURlIn1d~"
        private const val WRONG_VCT_VALUE =
            "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJpc3MiOiJkaWQ6dGR3OmFiYyIsIm5iZiI6MCwiZXhwIjo5OTk5OTk5OTk5LCJpYXQiOjAsIl9zZF9hbGciOiJTSEEtMjU2IiwidmN0IjoiU29tZVR5cGUiLCJfc2QiOlsiMU4xaGoxTWxXWmNyNUFvOHJuWEotZy0wLWhpN0U2dlA2NHNncC12SjhiayIsIkw5VkJYQUl4VnA2VFdMc2lBY3dhTlhkMENhWmRwOTRYWi1zX2xWRWVha3ciLCJYc28zdHYwSXlBQUtXT2hnUlIxV1NnSkg1TDM1c0wwWlpFMDV5dGJsbzRzIiwiZ1lnYld4bEswa0VJcDF1Qno1bWs1VVFPbGFRZ2pIZmgyR3RsVnozdUZvNCJdfQ.tQEnuHIbYMZVmynTbolWCKDfLn-bNnZ5M15uPl5E1guUbgQ8Szh_CiDBf18IrZFva2cZZBfCgs-ZixXHSIYpcg~WyI2OGY2OGYxZTI5MTY0OWIzIiwic3ViIiwiZGlkOnRkdzphYmNkIl0~WyJiNDc4ODczMjBkMjkxZDIwIiwib3JnTmFtZSIseyJlbiI6Im9yZ05hbWUgRW4iLCJkZS1DSCI6Im9yZ05hbWUgRGUifV0~WyI3ZDBhOTE4NzkyZTdhM2Q0IiwicHJlZkxhbmciLCJkZSJd~WyI4M2EwNjlkMDZmMzNiMDFjIiwibG9nb1VyaSIseyJlbiI6ImxvZ29VcmlFbiIsImRlIjoibG9nb1VyaURlIn1d~"
        private const val MISSING_ORG_NAME =
            "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJpc3MiOiJkaWQ6dGR3OmFiYyIsIm5iZiI6MCwiZXhwIjo5OTk5OTk5OTk5LCJpYXQiOjAsIl9zZF9hbGciOiJTSEEtMjU2IiwidmN0IjoiVHJ1c3RTdGF0ZW1lbnRNZXRhZGF0YVYxIiwiX3NkIjpbIkhBc3NyZG5RUVkwSWlrS2Z0YjZ1TU1wRlZRb3BwRVdpWDVHZTVrNHMzY1kiLCJQZ3U3OHZRMVA3RENwVG9LYTBvRUdwc0p3bl9HZmUwZTkxSjItMG80REtNIiwicUxPcnRfWU1RMnZWRHVzX1ZUMVBWYzBpLWlka2pYaHA5UHlQN0w4dExMZyJdfQ.Yu2wAdtPbOP8bJojdwjP-s1pRvgwUp6XKl29hsBFoujljY0fmrShcj1rDu5xZR4JfVgrLZx2rxX-jXRHglMLtA~WyJmYzI3NTZiYTE0MDYzZjkzIiwic3ViIiwiZGlkOnRkdzphYmNkIl0~WyI0MzBhYjA0YjA5NTZjNTM0Iiwib3JnTmFtZSIseyJlbiI6Im9yZ05hbWUgRW4iLCJkZS1DSCI6Im9yZ05hbWUgRGUifV0~WyJjYjQyYWYzZjhmZWFhZTI1IiwicHJlZkxhbmciLCJkZSJd~"
        private const val MISSING_PREF_LANG =
            "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJpc3MiOiJkaWQ6dGR3OmFiYyIsIm5iZiI6MCwiZXhwIjo5OTk5OTk5OTk5LCJpYXQiOjAsIl9zZF9hbGciOiJTSEEtMjU2IiwidmN0IjoiVHJ1c3RTdGF0ZW1lbnRNZXRhZGF0YVYxIiwiX3NkIjpbIjhIeFAwU3lBVS1mSXVOUnpTRVlLQkVxSHB6VWJZRl8ta0hpcUhZa2lidUkiLCJLSFVHWUlKV3hoT3dEOFhGZjh5bmlpN3lxdE5VWll0OWpEQnoxX2RaWVNZIiwiT1FmaF84VWZBZjZ5Z0JzU0RodFJLV1liVGdGSTNpbW9xNDlybnRLOU5yYyJdfQ.Pd9SVJBnGHiHwutzDtn5dCe0-lKuK6Myqgxl82koLGRmxLRUenEZx8IQTwrdVhWPhQS8ufciHbHNg7F2cD1Z9g~WyJhYzNlMjZlN2JkYjRlNDEwIiwic3ViIiwiZGlkOnRkdzphYmNkIl0~WyI3NzZiNmNjMzRmYjE5MzcwIiwib3JnTmFtZSIseyJlbiI6Im9yZ05hbWUgRW4iLCJkZS1DSCI6Im9yZ05hbWUgRGUifV0~WyJmNTE5YjgyODQwYmI4ODBjIiwibG9nb1VyaSIseyJlbiI6ImxvZ29VcmlFbiIsImRlIjoibG9nb1VyaURlIn1d~"
        private const val WRONG_FIELD_TYPES =
            "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJpc3MiOiJkaWQ6dGR3OmFiYyIsIm5iZiI6MCwiZXhwIjo5OTk5OTk5OTk5LCJpYXQiOjAsIl9zZF9hbGciOiJTSEEtMjU2IiwidmN0IjoiVHJ1c3RTdGF0ZW1lbnRNZXRhZGF0YVYxIiwiX3NkIjpbIjFMdWlPRm1tZkRqc3N0NWJqZFYyREZLWDBFRVRMTkdyMVczckswamg1NFEiLCJEMFhUQ1pOcXM5OUE3Mm1QWXpiVW5fb0JaLVZ6NDZoc0VodlhDQ3NWaTFRIiwicDJxRTJBZWRMT2xpcEtfbmlibjY2NEVPS2lKYnVJZXFBd09iUW5BLXNSWSIsInhqS2xQTHY4T1VuMWdjbUE3LWxUTTZPNkZDN1AzREhMX2U5UllueXpUUzQiXX0.1xJwRfSt-I_lzVGIJNfyOwVThEO2Fkh7KEKoB0TUfvuo5HRoajJRNgyBmf3UOCxioICOZzBRcUQK6D71ADvMmA~WyIwYzYwYjA2ZTAxZDg0NDhiIiwic3ViIiwiZGlkOnRkdzphYmNkIl0~WyJkZmM0ZmVkMTEwZGI2M2NlIiwib3JnTmFtZSIsIm15IG9yZyBuYW1lIl0~WyIzMGQwNWI2ODkyYmJlMDFkIiwicHJlZkxhbmciLCJkZSJd~WyJhNWVkNGRiYzExMjY4ZjJhIiwibG9nb1VyaSIsIm15IGxvZ28gVXJpIl0~"
        private const val MISSING_STATUS_PROPERTIES =
            "eyJ0eXAiOiJ2YytzZC1qd3QiLCJhbGciOiJFUzI1NiIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJpc3MiOiJkaWQ6dGR3OmFiYyIsIm5iZiI6MCwiZXhwIjo5OTk5OTk5OTk5LCJpYXQiOjAsIl9zZF9hbGciOiJTSEEtMjU2IiwidmN0IjoiVHJ1c3RTdGF0ZW1lbnRNZXRhZGF0YVYxIiwiX3NkIjpbIk1TemNCd1RWLU15M3lUNmtLRk55akRCZ3BUbng5elpHVW4wQzJYZWRQSzQiLCJWTEVFNG5HcFlQdEk1QXhKdnhMMS05aEh0OVQ3WlMwYkFNYUx1MEJ0QUg0IiwiaGZLR1lCSkJUOWF3YnZPQ3d2MjY5bjQwY3RuUHpnd2pDTzlfeHNKQTBrYyIsImliTWFhcy15SXBWTUU3YW1yZXRGYnlQWUk2bkJOWkI3T0ZjUU52ZG9xR1UiXX0.6DzvyWjm4Dv64XsC78aBjTR_bLEQ1vH2RyMNahd5gvtIQG5RLO1NCoWRssyx3hu9hCvEypkBahWnfsrhGi-bjw~WyIyYmE2ZTUxNWEzODY3ZThiIiwic3ViIiwiZGlkOnRkdzphYmNkIl0~WyI0NzdiNzU3YzU1YmExYjY4Iiwib3JnTmFtZSIseyJlbiI6Im9yZ05hbWUgRW4iLCJkZS1DSCI6Im9yZ05hbWUgRGUifV0~WyI0ZTY4ODJkZTNhZTZkYmUyIiwicHJlZkxhbmciLCJkZSJd~WyI3MGVhMjRjZTVhZjk0Y2NjIiwibG9nb1VyaSIseyJlbiI6ImxvZ29VcmlFbiIsImRlIjoibG9nb1VyaURlIn1d~"
        //region Trust statement source

/* Trust statement content
header:

{
  "alg": "ES256",
  "typ": "vc+sd-jwt",
  "kid": "did:tdw:abc#key01"
}

payload
{
  "iss": "did:tdw:abc",
  "nbf": 0,
  "exp": 9999999999,
  "iat": 0,
  "_sd_alg": "sha-256",
  "sub": "did:tdw:abcd",
  "orgName": {
    "en": "orgName En",
    "de-CH": "orgName De"
  },
  "prefLang": "de",
  "vct": "TrustStatementMetadataV1",
  "logoUri": {
    "en": "logoUriEn",
    "de": "logoUriDe"
  },
  "status":{
      "status_list":{
         "idx":0,
         "uri":"uri"
      }
   }
}

-----BEGIN PUBLIC KEY-----
MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEEVs/o5+uQbTjL3chynL4wXgUg2R9
q9UU8I5mEovUf86QZ7kOBIjJwqnzD1omageEHWwHdBO6B+dFabmdT9POxg==
-----END PUBLIC KEY-----
-----BEGIN PRIVATE KEY-----
MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgevZzL1gdAFr88hb2
OF/2NxApJCzGCEDdfSp6VQO30hyhRANCAAQRWz+jn65BtOMvdyHKcvjBeBSDZH2r
1RTwjmYSi9R/zpBnuQ4EiMnCqfMPWiZqB4QdbAd0E7oH50VpuZ1P087G
-----END PRIVATE KEY-----
 */
        //endregion
    }
}
