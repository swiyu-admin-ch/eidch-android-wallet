package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.SaveVcSdJwtCredentials
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.credentialConfig
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.oneConfigCredentialInformation
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertSuccessType
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

class HandleCredentialResultImplTest {
    @MockK
    private lateinit var mockVcSdJwtCredential: VcSdJwtCredential

    @MockK
    private lateinit var mockSaveVcSdJwtCredentials: SaveVcSdJwtCredentials

    private lateinit var useCase: HandleCredentialResultImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = HandleCredentialResultImpl(saveVcSdJwtCredentials = mockSaveVcSdJwtCredentials)
    }

    @Test
    fun `Saving credential maps errors from SaveVcSdJwtCredentials`() = runTest {
        coEvery {
            mockSaveVcSdJwtCredentials(
                issuerUrl = any(),
                vcSdJwtCredentials = any(),
                rawAndParsedCredentialInfo = any(),
                credentialConfig = any(),
            )
        } returns Err(CredentialError.InvalidCredentialOffer)

        useCase.invoke(
            issuerUrl = ISSUER_URL,
            anyVerifiedCredential = AnyVerifiedCredential(mockVcSdJwtCredential),
            rawAndParsedCredentialInfo = RawAndParsedIssuerCredentialInfo(
                issuerCredentialInfo = oneConfigCredentialInformation,
                rawIssuerCredentialInfo = ""
            ),
            credentialConfig = credentialConfig,
        ).assertErrorType(CredentialError.InvalidCredentialOffer::class)
    }

    @Test
    fun `Saving a credential runs specific steps`() = runTest {
        coEvery {
            mockSaveVcSdJwtCredentials(
                issuerUrl = any(),
                vcSdJwtCredentials = any(),
                rawAndParsedCredentialInfo = any(),
                credentialConfig = any(),
            )
        } returns Ok(CREDENTIAL_ID)

        val result = useCase.invoke(
            issuerUrl = ISSUER_URL,
            anyVerifiedCredential = AnyVerifiedCredential(mockVcSdJwtCredential),
            rawAndParsedCredentialInfo = RawAndParsedIssuerCredentialInfo(
                issuerCredentialInfo = oneConfigCredentialInformation,
                rawIssuerCredentialInfo = ""
            ),
            credentialConfig = credentialConfig,
        )

        val credentialResult = result.assertSuccessType(FetchCredentialResult.Credential::class)
        assertEquals(CREDENTIAL_ID, credentialResult.credentialId)

        coVerify {
            mockSaveVcSdJwtCredentials(
                issuerUrl = ISSUER_URL,
                vcSdJwtCredentials = listOf(mockVcSdJwtCredential),
                rawAndParsedCredentialInfo = RawAndParsedIssuerCredentialInfo(
                    issuerCredentialInfo = oneConfigCredentialInformation,
                    rawIssuerCredentialInfo = ""
                ),
                credentialConfig = credentialConfig,
            )
        }
    }

    companion object {
        private const val CREDENTIAL_ID = 1337L
        private val ISSUER_URL = URL("https://issuer.example")
    }
}
