package ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.implementation

import ch.admin.foitt.didResolver.domain.DidResolverHelper
import ch.admin.foitt.openid4vc.domain.model.jwt.JwtError
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignatureFromDid
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusList
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusListResponse
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.ValidateTokenStatusList
import ch.admin.foitt.wallet.util.SafeJsonTestInstance.safeJson
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import java.text.ParseException

class ValidateTokenStatusListImplTest {
    @MockK
    private lateinit var mockDidResolverHelper: DidResolverHelper

    @MockK
    private lateinit var mockVerifyJwtSignatureFromDid: VerifyJwtSignatureFromDid

    private lateinit var useCase: ValidateTokenStatusList

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = ValidateTokenStatusListImpl(
            didResolverHelper = mockDidResolverHelper,
            verifyJwtSignatureFromDid = mockVerifyJwtSignatureFromDid,
            safeJson = safeJson,
        )

        every { mockDidResolverHelper.getDidStringFromAbsoluteKeyId(KEY_ID) } returns Ok(ISSUER)
        coEvery { mockVerifyJwtSignatureFromDid(kid = any(), jwt = any()) } returns Ok(Unit)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Validating token status list which is valid returns response`(): Unit = runTest {
        val result = useCase(ISSUER, JWT, SUBJECT).assertOk()
        assertEquals(tokenResponse, result)
    }

    @Test
    fun `Validating token status list which is not a JWT returns error`(): Unit = runTest {
        val result = useCase(ISSUER, "invalidJwt", SUBJECT)

        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertInstanceOf<ParseException>(error.cause)
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun `Validating token status list with missing status_list returns error`(): Unit = runTest {
        val result = useCase(ISSUER, JWT_WITHOUT_STATUS_LIST, SUBJECT)

        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertInstanceOf<SerializationException>(error.cause)
    }

    @Test
    fun `Validating token status list with wrong type returns error`(): Unit = runTest {
        val result = useCase(ISSUER, JWT_WITH_WRONG_TYPE, SUBJECT)

        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertInstanceOf<IllegalStateException>(error.cause)
    }

    @Test
    fun `Validating token status list with missing iat returns error`(): Unit = runTest {
        val result = useCase(ISSUER, JWT_WITHOUT_IAT, SUBJECT)

        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertInstanceOf<IllegalStateException>(error.cause)
    }

    @Test
    fun `Validating token status list with missing subject returns error`(): Unit = runTest {
        val result = useCase(ISSUER, JWT_WITHOUT_SUBJECT, SUBJECT)

        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertInstanceOf<IllegalStateException>(error.cause)
    }

    @Test
    fun `Validating token status list with wrong subject returns error`(): Unit = runTest {
        val result = useCase(ISSUER, JWT, "otherSubject")

        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertInstanceOf<IllegalStateException>(error.cause)
    }

    @Test
    fun `Validating token status list with expired JWT returns error`(): Unit = runTest {
        val result = useCase(ISSUER, JWT_WITH_EXPIRATION_DATE_ZERO, SUBJECT)

        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertInstanceOf<IllegalStateException>(error.cause)
    }

    @Test
    fun `Validating token status list with missing keyId returns error`(): Unit = runTest {
        val result = useCase(ISSUER, JWT_WITHOUT_KEY_ID, SUBJECT)

        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertInstanceOf<IllegalStateException>(error.cause)
    }

    @Test
    fun `Validating token status list with wrong issuer returns error`(): Unit = runTest {
        val result = useCase("otherIssuer", JWT, SUBJECT)

        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertInstanceOf<IllegalStateException>(error.cause)
    }

    @Test
    fun `Validating token status list with wrong trust statement issuer returns error`(): Unit = runTest {
        every {
            mockDidResolverHelper.getDidStringFromAbsoluteKeyId(KEY_ID)
        } returns Ok("otherIssuer")

        val result = useCase(ISSUER, JWT, SUBJECT)

        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertInstanceOf<IllegalStateException>(error.cause)
    }

    @Test
    fun `Validating token status list maps errors from did resolver`(): Unit = runTest {
        val exception = IllegalStateException("did error")
        every {
            mockDidResolverHelper.getDidStringFromAbsoluteKeyId(KEY_ID)
        } returns Err(exception)

        val result = useCase(ISSUER, JWT, SUBJECT)

        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertInstanceOf<IllegalStateException>(error.cause)
    }

    @Test
    fun `Validating token status list maps errors from verifying jwt signature`(): Unit = runTest {
        coEvery {
            mockVerifyJwtSignatureFromDid(kid = any(), jwt = any())
        } returns Err(JwtError.DidDocumentDeactivated)

        useCase(ISSUER, JWT, SUBJECT).assertErrorType(CredentialStatusError.DidDocumentDeactivated::class)
    }

    @Test
    fun `Validating token status list with invalid payload returns error`(): Unit = runTest {
        val result = useCase(ISSUER, JWT_WITHOUT_BITS, SUBJECT)

        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertInstanceOf<SerializationException>(error.cause)
    }

    private companion object {
        const val ISSUER = "issuerDid"
        const val KEY_ID = "$ISSUER#key-01"
        const val SUBJECT = "subject"
        val statusList = TokenStatusList(bits = 2, lst = "lst")
        val tokenResponse = TokenStatusListResponse(timeToLive = 43200, statusList = statusList)
        const val JWT =
            "eyJhbGciOiJFUzI1NiIsImtpZCI6Imlzc3VlckRpZCNrZXktMDEiLCJ0eXAiOiJzdGF0dXNsaXN0K2p3dCJ9.eyJleHAiOjIyOTE3MjAxNzAsImlhdCI6MTY4NjkyMDE3MCwic3RhdHVzX2xpc3QiOnsiYml0cyI6MiwibHN0IjoibHN0In0sInN1YiI6InN1YmplY3QiLCJ0dGwiOjQzMjAwfQ.VZ4ZsBUYWcXRCArg9Tcs-7F6kYLp-_1PeL-A9VQipuNG2qvhHyYoeQ6PIrcBI2Lt-XGRJJhVFZIQmo4_LsHeGw"
        const val JWT_WITHOUT_KEY_ID =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InN0YXR1c2xpc3Qrand0In0.eyJleHAiOjIyOTE3MjAxNzAsImlhdCI6MTY4NjkyMDE3MCwic3RhdHVzX2xpc3QiOnsiYml0cyI6MiwibHN0IjoibHN0In0sInN1YiI6InN1YmplY3QiLCJ0dGwiOjQzMjAwfQ.yCZPrK_bMtLJe3f0tvk2SpoqhrUV7O6poyTwn30xGIBMCnPL--yOaUbc8WPthGHXjexFFYWtkAuM_Z1bVwejww"
        const val JWT_WITH_EXPIRATION_DATE_ZERO =
            "eyJhbGciOiJFUzI1NiIsImtpZCI6Imlzc3VlckRpZCNrZXktMDEiLCJ0eXAiOiJzdGF0dXNsaXN0K2p3dCJ9.eyJleHAiOjAsImlhdCI6MTY4NjkyMDE3MCwic3RhdHVzX2xpc3QiOnsiYml0cyI6MiwibHN0IjoibHN0In0sInN1YiI6InN1YmplY3QiLCJ0dGwiOjQzMjAwfQ.4vgRp_62qW6_wSsiRuW18SVk-f7CvexxH0DsiqrCxK6t6oOzMOfP1CF8awJfOXR8gNVOAtdG7pZQptWWH0zkqQ"
        const val JWT_WITHOUT_BITS =
            "eyJhbGciOiJFUzI1NiIsImtpZCI6Imlzc3VlckRpZCNrZXktMDEiLCJ0eXAiOiJzdGF0dXNsaXN0K2p3dCJ9.eyJleHAiOjIyOTE3MjAxNzAsImlhdCI6MTY4NjkyMDE3MCwic3RhdHVzX2xpc3QiOnsibHN0IjoibHN0In0sInN1YiI6InN1YmplY3QiLCJ0dGwiOjQzMjAwfQ.YNpEaukt1Dvp2R6v2f4EkFPygNYe6k5HM5TN-2qsWWUimptwWGbJD7mS6IM19a5w-ck1m2-GhRcAISrLwErm5w"
        const val JWT_WITHOUT_SUBJECT =
            "eyJhbGciOiJFUzI1NiIsImtpZCI6Imlzc3VlckRpZCNrZXktMDEiLCJ0eXAiOiJzdGF0dXNsaXN0K2p3dCJ9.eyJleHAiOjIyOTE3MjAxNzAsImlhdCI6MTY4NjkyMDE3MCwic3RhdHVzX2xpc3QiOnsiYml0cyI6MiwibHN0IjoibHN0In0sInR0bCI6NDMyMDB9.KUvqdrJ8Q3kYyyrxawgyiEIUHCQ1vcIXOvKAGGXvoKd65VbX5-hRdKo3UcpnKkKgi9tZW51bwPU-Rdv_EJ3hpw"
        const val JWT_WITHOUT_IAT =
            "eyJhbGciOiJFUzI1NiIsImtpZCI6Imlzc3VlckRpZCNrZXktMDEiLCJ0eXAiOiJzdGF0dXNsaXN0K2p3dCJ9.eyJleHAiOjIyOTE3MjAxNzAsInN0YXR1c19saXN0Ijp7ImJpdHMiOjIsImxzdCI6ImxzdCJ9LCJzdWIiOiJzdWJqZWN0IiwidHRsIjo0MzIwMH0.pHSlJKviRPAHfKEiVkysYgGGshG_fliDRxeQYmqg22Z0li_W20LrXVWK3F1SHYxg6wYyVbL4yGr0MWyfq_QqUQ"
        const val JWT_WITH_WRONG_TYPE =
            "eyJhbGciOiJFUzI1NiIsImtpZCI6Imlzc3VlckRpZCNrZXktMDEiLCJ0eXAiOiJ3cm9uZ1R5cGUifQ.eyJleHAiOjIyOTE3MjAxNzAsImlhdCI6MTY4NjkyMDE3MCwic3RhdHVzX2xpc3QiOnsiYml0cyI6MiwibHN0IjoibHN0In0sInN1YiI6InN1YmplY3QiLCJ0dGwiOjQzMjAwfQ.qcscri_siWlaWSEDX735URorWhDAcyK0Q2rpjtAYVqhRM-2uxJCmwAh3UMhpblGFyap6qbBo56kOjl4L_DCRfg"
        const val JWT_WITHOUT_STATUS_LIST =
            "eyJhbGciOiJFUzI1NiIsImtpZCI6Imlzc3VlckRpZCNrZXktMDEiLCJ0eXAiOiJzdGF0dXNsaXN0K2p3dCJ9.eyJleHAiOjIyOTE3MjAxNzAsImlhdCI6MTY4NjkyMDE3MCwic3ViIjoic3ViamVjdCIsInR0bCI6NDMyMDB9.73XOBdRETiNH6ZgRXWZOfk1HOnVQMRt6sVCu__vhncaUtFc4MT_u_mQSRfvYsrStdxN9kTKNGA_8JgbBVfLi9g"
/*
JWT Header is:
{
    "alg": "ES256",
    "kid": "issuerDid#key-01",
    "typ": "statuslist+jwt"
}
JWT payload is: {
                  "exp": 2291720170,
                  "iat": 1686920170,
                  "status_list": {
                    "bits": 2,
                    "lst": "lst"
                  },
                  "sub": "uri",
                  "ttl": 43200
                }
-----BEGIN PUBLIC KEY-----
MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAERqVXn+o+6zEOpWEsGw5CsB+wd8zO
jxu0uASGpiGP+wYfcc1unyMxcStbDzUjRuObY8DalaCJ9/J6UrkQkZBtZw==
-----END PUBLIC KEY-----
-----BEGIN PRIVATE KEY-----
MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQglBnO+qn+RecAQ31T
jBklNu+AwiFN5eVHBFbnjecmMryhRANCAARGpVef6j7rMQ6lYSwbDkKwH7B3zM6P
G7S4BIamIY/7Bh9xzW6fIzFxK1sPNSNG45tjwNqVoIn38npSuRCRkG1n
-----END PRIVATE KEY-----
*/
    }
}
