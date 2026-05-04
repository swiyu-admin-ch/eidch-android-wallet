package ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignatureFromDid
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusList
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusListResponse
import ch.admin.foitt.wallet.util.SafeJsonTestInstance.safeJson
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
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
    private lateinit var mockVerifyJwtSignatureFromDid: VerifyJwtSignatureFromDid

    private lateinit var useCase: ValidateTokenStatusListImpl

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = ValidateTokenStatusListImpl(
            safeJson = safeJson,
            verifyJwtSignatureFromDid = mockVerifyJwtSignatureFromDid,
        )

        coEvery { mockVerifyJwtSignatureFromDid(did = any(), kid = any(), jwt = any()) } returns Ok(Unit)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Validating token status list which is valid returns response`(): Unit = runTest {
        val result = useCase(ISSUER, JWT, SUBJECT)

        val response = result.assertOk()
        assertEquals(tokenResponse, response)
    }

    @Test
    fun `Validating token status list which has no issuer returns error`(): Unit = runTest {
        val result = useCase(ISSUER, JWT_WITHOUT_ISSUER, SUBJECT)
        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertInstanceOf<IllegalStateException>(error.cause)
    }

    @Test
    fun `Validating token status list which is not a JWT returns error`(): Unit = runTest {
        val result = useCase(ISSUER, "invalidJwt", SUBJECT)

        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertInstanceOf<ParseException>(error.cause)
    }

    @Test
    fun `Validating token status list with wrong issuer returns error`(): Unit = runTest {
        val result = useCase("otherIssuer", JWT, SUBJECT)

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
    fun `Validating token status list with invalid payload returns error`(): Unit = runTest {
        val result = useCase(ISSUER, JWT_WITHOUT_BITS, SUBJECT)

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

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun `Validating token status list with missing status_list returns error`(): Unit = runTest {
        val result = useCase(ISSUER, JWT_WITHOUT_STATUS_LIST, SUBJECT)

        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertInstanceOf<SerializationException>(error.cause)
    }

    private companion object {
        const val ISSUER = "issuer"
        const val SUBJECT = "subject"
        val statusList = TokenStatusList(bits = 2, lst = "lst")
        val tokenResponse = TokenStatusListResponse(timeToLive = 43200, statusList = statusList)
        const val JWT =
            "eyJhbGciOiJFUzI1NiIsImtpZCI6ImM0NDI5NzI4MGU1ZDNjYjVmOTE1ZjRiN2JiOGRjZWFjIiwidHlwIjoic3RhdHVzbGlzdCtqd3QifQ.eyJleHAiOjIyOTE3MjAxNzAsImlhdCI6MTY4NjkyMDE3MCwiaXNzIjoiaXNzdWVyIiwic3RhdHVzX2xpc3QiOnsiYml0cyI6MiwibHN0IjoibHN0In0sInN1YiI6InN1YmplY3QiLCJ0dGwiOjQzMjAwfQ.Di0wLIouZjHbQvvMhK3chhQ97gnkfj-oKrTeYbnRT3Xb2qQyURuroD9LOsc72TQytsCoueFyzd-z_xz95ZR-VA"
        const val JWT_WITH_EXPIRATION_DATE_ZERO =
            "eyJhbGciOiJFUzI1NiIsImtpZCI6ImM0NDI5NzI4MGU1ZDNjYjVmOTE1ZjRiN2JiOGRjZWFjIiwidHlwIjoic3RhdHVzbGlzdCtqd3QifQ.eyJleHAiOjAsImlhdCI6MTY4NjkyMDE3MCwiaXNzIjoiaXNzdWVyIiwic3RhdHVzX2xpc3QiOnsiYml0cyI6MiwibHN0IjoibHN0In0sInN1YiI6InN1YmplY3QiLCJ0dGwiOjQzMjAwfQ.PJkzqde4UgfY9yBPMgIfCYKfCksRbqRDVo-1QiiiA5mrefbrrcLxFEporWNajw1UQhhHKg_fdgmHElFI70buXw"
        const val JWT_WITHOUT_ISSUER =
            "eyJhbGciOiJFUzI1NiIsImtpZCI6ImM0NDI5NzI4MGU1ZDNjYjVmOTE1ZjRiN2JiOGRjZWFjIiwidHlwIjoic3RhdHVzbGlzdCtqd3QifQ.eyJleHAiOjIyOTE3MjAxNzAsImlhdCI6MTY4NjkyMDE3MCwic3RhdHVzX2xpc3QiOnsiYml0cyI6MiwibHN0IjoibHN0In0sInN1YiI6InN1YmplY3QiLCJ0dGwiOjQzMjAwfQ.qNVbi2GEZBzpQ_K-3jWty1kGk_IwKgYsnPH5B570CXVSHCogYGkgK76NFE4utBcppPDN2moB3-iIj2fJMRyBgg"
        const val JWT_WITHOUT_BITS =
            "eyJhbGciOiJFUzI1NiIsImtpZCI6ImM0NDI5NzI4MGU1ZDNjYjVmOTE1ZjRiN2JiOGRjZWFjIiwidHlwIjoic3RhdHVzbGlzdCtqd3QifQ.eyJleHAiOjIyOTE3MjAxNzAsImlhdCI6MTY4NjkyMDE3MCwiaXNzIjoiaXNzdWVyIiwic3RhdHVzX2xpc3QiOnsibHN0IjoibHN0In0sInN1YiI6InN1YmplY3QiLCJ0dGwiOjQzMjAwfQ.VD_fKygkmcJFDLD1MkuHOrlarfhIHeHPhLXOhYwA347NYQJ7k-hhO_o29X0ieloJow85-iB5OXiPTqYOPP9ULQ"
        const val JWT_WITHOUT_SUBJECT =
            "eyJhbGciOiJFUzI1NiIsImtpZCI6ImM0NDI5NzI4MGU1ZDNjYjVmOTE1ZjRiN2JiOGRjZWFjIiwidHlwIjoic3RhdHVzbGlzdCtqd3QifQ.eyJleHAiOjIyOTE3MjAxNzAsImlhdCI6MTY4NjkyMDE3MCwiaXNzIjoiaXNzdWVyIiwic3RhdHVzX2xpc3QiOnsiYml0cyI6MiwibHN0IjoibHN0In0sInR0bCI6NDMyMDB9.NF11zqGHRRRU-3p7arjhXD8LCLypM3p_i9fwE4Wtcs9_J0CJROsBJnCe09ZG86EThnGCVYpqFx2GCnatdvBWMw"
        const val JWT_WITHOUT_IAT =
            "eyJhbGciOiJFUzI1NiIsImtpZCI6ImM0NDI5NzI4MGU1ZDNjYjVmOTE1ZjRiN2JiOGRjZWFjIiwidHlwIjoic3RhdHVzbGlzdCtqd3QifQ.eyJleHAiOjIyOTE3MjAxNzAsImlzcyI6Imlzc3VlciIsInN0YXR1c19saXN0Ijp7ImJpdHMiOjIsImxzdCI6ImxzdCJ9LCJzdWIiOiJzdWJqZWN0IiwidHRsIjo0MzIwMH0.9yZAC_8eGOflsxamKQXVcqZF6TovQSH10ckpug39hsUHD2O3FxGIaRzHPJYN040pCnDgYArtUSAiHmsZXOxxBA"
        const val JWT_WITH_WRONG_TYPE =
            "eyJhbGciOiJFUzI1NiIsImtpZCI6ImM0NDI5NzI4MGU1ZDNjYjVmOTE1ZjRiN2JiOGRjZWFjIiwidHlwIjoianNvbiJ9.eyJleHAiOjIyOTE3MjAxNzAsImlhdCI6MTY4NjkyMDE3MCwiaXNzIjoiaXNzdWVyIiwic3RhdHVzX2xpc3QiOnsiYml0cyI6MiwibHN0IjoibHN0In0sInR0bCI6NDMyMDB9.BjQo9W0oHZ2kIUe6Z_qSYVathL2vEjIZzZ7rnsmw3Tzqyf4oPnI91IptdXSdz8kJNPMSbB0frihb7DzpGmzh_g"
        const val JWT_WITHOUT_STATUS_LIST =
            "eyJhbGciOiJFUzI1NiIsImtpZCI6ImM0NDI5NzI4MGU1ZDNjYjVmOTE1ZjRiN2JiOGRjZWFjIiwidHlwIjoic3RhdHVzbGlzdCtqd3QifQ.eyJleHAiOjIyOTE3MjAxNzAsImlhdCI6MTY4NjkyMDE3MCwiaXNzIjoiaXNzdWVyIiwic3ViIjoic3ViamVjdCIsInR0bCI6NDMyMDB9.K7nU10KOZAx-UIsd57pJeaCKjfdzJSRUj4RnvTpQyecteCZpt0aCR6VPwl4el1pc5sc8k3yPqKqy5jF5EL1t6A"
/*
JWT Header is:
{
    "alg": "ES256",
    "kid": "c44297280e5d3cb5f915f4b7bb8dceac",
    "typ": "statuslist+jwt"
}
JWT payload is: {
                  "exp": 2291720170,
                  "iat": 1686920170,
                  "iss": "issuer",
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
