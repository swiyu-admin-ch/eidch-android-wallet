package ch.admin.foitt.openid4vc.domain.model.presentationRequest

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.util.assertErr
import ch.admin.foitt.openid4vc.util.assertOk
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class ClientIdentifierTest {

    @MockK
    private lateinit var mockRequestObject: RequestObject

    @MockK
    private lateinit var mockJwt: Jwt

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        every { mockRequestObject.jwt } returns mockJwt
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @TestFactory
    fun `client identifier prefix is correctly created`(): List<DynamicTest> {
        val input = mapOf(
            "decentralized_identifier:did:example:123" to ClientIdentifier.ClientIdPrefix.DecentralizedIdentifier,
            "verifier_attestation:attestationSubClaim" to ClientIdentifier.ClientIdPrefix.VerifierAttestationJwt,
            "did:example:123" to ClientIdentifier.ClientIdPrefix.DecentralizedIdentifier,
            "unsupportedPrefix:clientId" to ClientIdentifier.ClientIdPrefix.DecentralizedIdentifier,
            "other" to ClientIdentifier.ClientIdPrefix.DecentralizedIdentifier,
        )

        return input.map { (clientId, result) ->
            DynamicTest.dynamicTest("client_id: $clientId should return $result") {
                runTest {
                    every { mockJwt.payloadJson } returns buildJsonObject { put("client_id", JsonPrimitive(clientId)) }

                    val clientIdentifier = ClientIdentifier.fromRequestObject(mockRequestObject).assertOk()

                    assertEquals(result, clientIdentifier.clientIdPrefix)
                }
            }
        }
    }

    @TestFactory
    fun `client identifier with invalid client_id returns an error`(): List<DynamicTest> {
        val input = listOf(
            "decentralized_identifier:",
            "verifier_attestation:",
        )

        return input.map { clientId ->
            DynamicTest.dynamicTest("client_id: $clientId should return error") {
                runTest {
                    every { mockJwt.payloadJson } returns buildJsonObject { put("client_id", JsonPrimitive(clientId)) }

                    ClientIdentifier.fromRequestObject(mockRequestObject).assertErr()
                }
            }
        }
    }

    @Test
    fun `client identifier with missing client_id returns an error`() = runTest {
        every { mockJwt.payloadJson } returns buildJsonObject { put("otherClaim", JsonPrimitive("value")) }

        ClientIdentifier.fromRequestObject(mockRequestObject).assertErr()
    }
}
