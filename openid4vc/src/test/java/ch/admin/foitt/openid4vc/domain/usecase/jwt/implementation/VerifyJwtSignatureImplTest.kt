package ch.admin.foitt.openid4vc.domain.usecase.jwt.implementation

import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignature
import ch.admin.foitt.openid4vc.util.assertErr
import ch.admin.foitt.openid4vc.util.assertOk
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.crypto.Ed25519Signer
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.OctetKeyPair
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VerifyJwtSignatureImplTest {

    private lateinit var useCase: VerifyJwtSignature

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = VerifyJwtSignatureImpl()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Successfully verifying an EC public key returns Ok`() = runTest {
        val ecKey = ECKeyGenerator(Curve.P_256).generate()
        val jwt = createEcSignedJwt(ecKey)
        val publicKey = ecKey.toPublicJwk()

        useCase(publicKey = publicKey, jwt = jwt).assertOk()
    }

    @Test
    fun `Verifying an EC signature that does not match returns an error`() = runTest {
        val ecKey = ECKeyGenerator(Curve.P_256).generate()
        val otherEcKey = ECKeyGenerator(Curve.P_256).generate()
        val jwt = createEcSignedJwt(ecKey)
        val publicKey = otherEcKey.toPublicJwk()

        useCase(publicKey = publicKey, jwt = jwt).assertErr()
    }

    @Test
    fun `Error during curve creation returns an error`() = runTest {
        val ecKey = ECKeyGenerator(Curve.P_256).generate()
        val jwt = createEcSignedJwt(ecKey)
        val publicKey = ecKey.toPublicJwk().copy(crv = "something")

        useCase(publicKey = publicKey, jwt = jwt).assertErr()
    }

    @Test
    fun `EC public key without y coordinate returns an error`() = runTest {
        val ecKey = ECKeyGenerator(Curve.P_256).generate()
        val jwt = createEcSignedJwt(ecKey)
        val publicKey = ecKey.toPublicJwk().copy(y = null)

        useCase(publicKey = publicKey, jwt = jwt).assertErr()
    }

    @Test
    fun `Unsupported key type returns an error`() = runTest {
        val ecKey = ECKeyGenerator(Curve.P_256).generate()
        val jwt = createEcSignedJwt(ecKey)
        val publicKey = ecKey.toPublicJwk().copy(kty = "RSA")

        useCase(publicKey = publicKey, jwt = jwt).assertErr()
    }

    @Test
    fun `Successfully verifying an Ed25519 signed jwt returns Ok`() = runTest {
        val keyPair = OctetKeyPairGenerator(Curve.Ed25519).generate()
        val jwt = createEd25519SignedJwt(keyPair)
        val publicKey = keyPair.toOkpJwk()

        useCase(publicKey = publicKey, jwt = jwt).assertOk()
    }

    @Test
    fun `Verifying an Ed25519 signed jwt with a non-matching public key returns an error`() = runTest {
        val keyPair = OctetKeyPairGenerator(Curve.Ed25519).generate()
        val otherKeyPair = OctetKeyPairGenerator(Curve.Ed25519).generate()
        val jwt = createEd25519SignedJwt(keyPair)
        val publicKey = otherKeyPair.toOkpJwk()

        useCase(publicKey = publicKey, jwt = jwt).assertErr()
    }

    @Test
    fun `Successfully verifying a jwt signed with the fully-specified Ed25519 header algorithm returns Ok`() = runTest {
        val keyPair = OctetKeyPairGenerator(Curve.Ed25519).generate()
        val jwt = createEd25519SignedJwt(keyPair, algorithm = JWSAlgorithm.parse("Ed25519"))
        val publicKey = keyPair.toOkpJwk()

        useCase(publicKey = publicKey, jwt = jwt).assertOk()
    }

    @Test
    fun `Verifying an Ed25519 signed jwt with an EC public key returns an error`() = runTest {
        val keyPair = OctetKeyPairGenerator(Curve.Ed25519).generate()
        val ecKey = ECKeyGenerator(Curve.P_256).generate()
        val jwt = createEd25519SignedJwt(keyPair)
        val publicKey = ecKey.toPublicJwk()

        useCase(publicKey = publicKey, jwt = jwt).assertErr()
    }

    private fun createEcSignedJwt(ecKey: ECKey): Jwt {
        val signedJwt = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.ES256).build(),
            JWTClaimsSet.Builder().subject("subject").build(),
        )
        signedJwt.sign(ECDSASigner(ecKey))
        return Jwt(signedJwt.serialize())
    }

    private fun createEd25519SignedJwt(keyPair: OctetKeyPair, algorithm: JWSAlgorithm = JWSAlgorithm.EdDSA): Jwt {
        val signedJwt = SignedJWT(
            JWSHeader.Builder(algorithm).build(),
            JWTClaimsSet.Builder().subject("subject").build(),
        )
        signedJwt.sign(Ed25519Signer(keyPair))
        return Jwt(signedJwt.serialize())
    }

    private fun ECKey.toPublicJwk() = Jwk(
        x = x.toString(),
        y = y.toString(),
        crv = curve.name,
        kty = keyType.value,
    )

    private fun OctetKeyPair.toOkpJwk() = Jwk(
        x = x.toString(),
        y = null,
        crv = curve.name,
        kty = keyType.value,
    )
}
