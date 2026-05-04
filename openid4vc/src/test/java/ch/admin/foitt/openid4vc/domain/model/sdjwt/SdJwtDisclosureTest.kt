package ch.admin.foitt.openid4vc.domain.model.sdjwt

import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.Disclosure1
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.Disclosure3
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.FlatDisclosures
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.FlatSdJwt
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.SdJwtSeparator
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SdJwtDisclosureTest {

    private lateinit var sdJwt: SdJwt

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        sdJwt = SdJwt(PAYLOAD_WITH_FLAT_DISCLOSURES)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Creating selective disclosure of a flat SdJwt with requiring all keys returns the whole payload`() {
        val requiredFieldKeys = listOf(FlatSdJwt.KEY_1, FlatSdJwt.KEY_2, FlatSdJwt.KEY_3)

        val verifiableCredential = sdJwt.createSelectiveDisclosure(requiredFieldKeys)

        assertEquals(PAYLOAD_WITH_FLAT_DISCLOSURES, verifiableCredential)
    }

    @Test
    fun `Creating selective disclosure of a flat SdJwt with requiring some keys returns the jwt with required disclosures`() {
        val requiredFieldKeys = listOf(FlatSdJwt.KEY_1, FlatSdJwt.KEY_3)

        val verifiableCredential = sdJwt.createSelectiveDisclosure(requiredFieldKeys)

        val expected = listOf(FlatSdJwt.JWT, Disclosure1, Disclosure3, "").joinToString(SdJwtSeparator)
        assertEquals(expected, verifiableCredential)
    }

    @Test
    fun `Creating selective disclosure of a flat SdJwt with requiring no keys returns the jwt`() {
        val requiredFieldKeys = emptyList<String>()

        val verifiableCredential = sdJwt.createSelectiveDisclosure(requiredFieldKeys)

        val expected = FlatSdJwt.JWT + SdJwtSeparator
        assertEquals(expected, verifiableCredential)
    }

    @Test
    fun `Creating selective disclosure of a flat SdJwt with requiring other keys returns the jwt`() {
        val requiredFieldKeys = listOf("otherKey", "otherKey2")

        val verifiableCredential = sdJwt.createSelectiveDisclosure(requiredFieldKeys)

        val expected = FlatSdJwt.JWT + SdJwtSeparator
        assertEquals(expected, verifiableCredential)
    }

    private companion object {
        val PAYLOAD_WITH_FLAT_DISCLOSURES = FlatSdJwt.JWT + FlatDisclosures
    }
}
