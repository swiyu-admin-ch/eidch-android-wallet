package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.eid.didresolver.did_sidekicks.DidDoc
import ch.admin.eid.didresolver.did_sidekicks.Jwk
import ch.admin.foitt.didResolver.domain.DidResolverHelper
import ch.admin.foitt.openid4vc.domain.model.ResolveDidError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.usecase.ResolveDid
import ch.admin.foitt.openid4vc.domain.usecase.ResolvePublicKey
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ResolvePublicKeyImplTest {
    @MockK
    private lateinit var mockDidResolverHelper: DidResolverHelper

    @MockK
    private lateinit var mockResolveDid: ResolveDid

    @MockK
    private lateinit var mockDidDoc: DidDoc

    @MockK
    private lateinit var mockDidJwk: Jwk

    private lateinit var useCase: ResolvePublicKey

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = ResolvePublicKeyImpl(
            didResolverHelper = mockDidResolverHelper,
            resolveDid = mockResolveDid,
        )

        every { mockDidResolverHelper.getDidStringFromAbsoluteKeyId(KEY_ID) } returns Ok(ISSUER_DID)

        coEvery { mockResolveDid(ISSUER_DID) } returns Ok(mockDidDoc)

        every { mockDidDoc.getDeactivated() } returns false

        every { mockDidDoc.getKeyByMethodId(KEY_ID) } returns mockDidJwk

        every { mockDidJwk.x } returns X_VALUE
        every { mockDidJwk.y } returns Y_VALUE
        every { mockDidJwk.crv } returns CRV
        every { mockDidJwk.kty } returns KTY
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Resolving public key from did returns a success`() = runTest {
        val result = useCase(KEY_ID).assertOk()

        val expected = ch.admin.foitt.openid4vc.domain.model.jwk.Jwk(
            x = X_VALUE,
            y = Y_VALUE,
            crv = CRV,
            kty = KTY,
        )

        assertEquals(expected, result)
    }

    @Test
    fun `Resolving public key maps errors from did resolver helper`() = runTest {
        val exception = Exception("Did exception")
        every { mockDidResolverHelper.getDidStringFromAbsoluteKeyId(KEY_ID) } returns Err(exception)

        useCase(KEY_ID).assertErrorType(VcSdJwtError.InvalidDid::class)
    }

    @Test
    fun `Resolving public key maps errors from ResolveDid`() = runTest {
        val exception = Exception("Did exception")
        coEvery { mockResolveDid(ISSUER_DID) } returns Err(ResolveDidError.Unexpected(exception))

        useCase(KEY_ID).assertErrorType(VcSdJwtError.Unexpected::class)
    }

    @Test
    fun `Resolving public key with deactivated did document returns an error`() = runTest {
        every { mockDidDoc.getDeactivated() } returns true

        useCase(KEY_ID).assertErrorType(VcSdJwtError.DidDocumentDeactivated::class)
    }

    @Test
    fun `Resolving public key where getting the public key fails returns an error`() = runTest {
        val exception = IllegalStateException("public key error")
        every { mockDidDoc.getKeyByMethodId(KEY_ID) } throws exception

        useCase(KEY_ID).assertErrorType(VcSdJwtError.InvalidJwt::class)
    }

    @Test
    fun `Resolving public key where the public key has missing x value returns an error`() = runTest {
        every { mockDidJwk.x } returns null
        useCase(KEY_ID).assertErrorType(VcSdJwtError.InvalidJwt::class)
    }

    @Test
    fun `Resolving public key where the public key has missing y value returns an error`() = runTest {
        every { mockDidJwk.y } returns null
        useCase(KEY_ID).assertErrorType(VcSdJwtError.InvalidJwt::class)
    }

    @Test
    fun `Resolving public key where the public key has missing crv value returns an error`() = runTest {
        every { mockDidJwk.crv } returns null
        useCase(KEY_ID).assertErrorType(VcSdJwtError.InvalidJwt::class)
    }

    @Test
    fun `Resolving public key where the public key has missing kty value returns an error`() = runTest {
        every { mockDidJwk.kty } returns null
        useCase(KEY_ID).assertErrorType(VcSdJwtError.InvalidJwt::class)
    }

    private companion object {
        const val ISSUER_DID = "did:method:identifier"
        const val KEY_ID = "$ISSUER_DID#key-01"
        const val X_VALUE = "x"
        const val Y_VALUE = "y"
        const val CRV = "crv"
        const val KTY = "kty"
    }
}
