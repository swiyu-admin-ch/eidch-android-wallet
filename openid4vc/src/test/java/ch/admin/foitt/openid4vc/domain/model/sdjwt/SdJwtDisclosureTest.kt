package ch.admin.foitt.openid4vc.domain.model.sdjwt

import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.ComplexSdJwt
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.FlatDisclosures
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.FlatSdJwt
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.FlatSimpleArraySdJwt
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.RecursiveSdJwt
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.SdJwtSeparator
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
    fun `Creating selective disclosure of a flat SdJwt with requiring all paths returns the whole payload`() {
        val verifiableCredential = sdJwt.createSelectiveDisclosure(FlatSdJwt.requestedPathAll)

        FlatSdJwt.assertWhole(verifiableCredential)
    }

    @Test
    fun `Creating selective disclosure of a flat SdJwt with requiring some paths returns the jwt with required disclosures`() {
        val verifiableCredential = sdJwt.createSelectiveDisclosure(FlatSdJwt.requestedPathPartial)

        FlatSdJwt.assertPartial(verifiableCredential)
    }

    @Test
    fun `Creating selective disclosure of a flat SdJwt with requiring no paths returns the jwt`() {
        val verifiableCredential = sdJwt.createSelectiveDisclosure(FlatSdJwt.requestedPathEmpty)

        val expected = FlatSdJwt.JWT + SdJwtSeparator
        assertEquals(expected, verifiableCredential)
    }

    @Test
    fun `Creating selective disclosure of a flat SdJwt with requiring other paths returns the jwt`() {
        val verifiableCredential = sdJwt.createSelectiveDisclosure(FlatSdJwt.requestedPathOther)

        val expected = FlatSdJwt.JWT + SdJwtSeparator
        assertEquals(expected, verifiableCredential)
    }

    @Test
    fun `Creating selective disclosure with a flat array requesting the whole array returns the jwt`() = runTest {
        val verifiableCredential = SdJwt(FlatSimpleArraySdJwt.SD_JWT_WITH_ARRAY_ONLY)
            .createSelectiveDisclosure(FlatSimpleArraySdJwt.requestedPathArray)

        FlatSimpleArraySdJwt.assertWhole(verifiableCredential)
    }

    @Test
    fun `Creating selective disclosure with a flat array requesting one element returns the jwt with element`() = runTest {
        val verifiableCredential = SdJwt(FlatSimpleArraySdJwt.SD_JWT_WITH_ARRAY_ONLY)
            .createSelectiveDisclosure(FlatSimpleArraySdJwt.requestedPathArrayValue2)

        FlatSimpleArraySdJwt.assertPartial(verifiableCredential)
    }

    @Test
    fun `Creating selective disclosure with recursive object requesting the root object returns the jwt`() = runTest {
        val selectiveDisclosure = SdJwt(RecursiveSdJwt.SD_JWT)
            .createSelectiveDisclosure(RecursiveSdJwt.requestedPathRoot)

        RecursiveSdJwt.assert(selectiveDisclosure)
    }

    @Test
    fun `Creating selective disclosure with recursive object requesting element returns the jwt`() = runTest {
        val selectiveDisclosure = SdJwt(RecursiveSdJwt.SD_JWT)
            .createSelectiveDisclosure(RecursiveSdJwt.requestedPathRecursive)

        RecursiveSdJwt.assert(selectiveDisclosure)
    }

    @Test
    fun `Creating selective disclosure with complex object requesting nested object returns the jwt with nested disclosure`() = runTest {
        val selectiveDisclosure = SdJwt(ComplexSdJwt.SD_JWT)
            .createSelectiveDisclosure(ComplexSdJwt.requestedPathWhole)

        ComplexSdJwt.assertWhole(selectiveDisclosure)
    }

    @Test
    fun `Creating selective disclosure with complex object requesting parts of object returns the jwt with disclosures`() = runTest {
        val selectiveDisclosure = SdJwt(ComplexSdJwt.SD_JWT)
            .createSelectiveDisclosure(ComplexSdJwt.requestedPathPartial)

        ComplexSdJwt.assertPartial(selectiveDisclosure)
    }

    @Test
    fun `Getting presentation paths with complex object requesting nested returns the jwt with disclosures`() = runTest {
        val paths = SdJwt(ComplexSdJwt.SD_JWT).getPresentationPaths(listOf(ComplexSdJwt.pathObjectPartly))

        assertEquals(13, paths.size)
        assertTrue(paths.contains(ComplexSdJwt.pathObjectPartly_1_2))
        assertTrue(paths.contains(ComplexSdJwt.pathObjectPartly_1_3_array))

        assertTrue(paths.contains(ComplexSdJwt.pathObjectPartly_1_2_1))
        assertTrue(paths.contains(ComplexSdJwt.pathObjectPartly_1_2_2))
        assertTrue(paths.contains(ComplexSdJwt.pathObjectPartly_1_2_3_array))

        assertTrue(paths.contains(ComplexSdJwt.pathObjectPartly_1_2_3_1))
        assertTrue(paths.contains(ComplexSdJwt.pathObjectPartly_1_2_3_2))
        assertTrue(paths.contains(ComplexSdJwt.pathObjectPartly_1_2_3_1_1_array))
        assertTrue(paths.contains(ComplexSdJwt.pathObjectPartly_1_2_3_2_1_array))
        assertTrue(paths.contains(ComplexSdJwt.pathObjectPartly_1_2_3_1_1_1))
        assertTrue(paths.contains(ComplexSdJwt.pathObjectPartly_1_2_3_2_1_1))

        assertTrue(paths.contains(ComplexSdJwt.pathObjectPartly_1_3_1))
        assertTrue(paths.contains(ComplexSdJwt.pathObjectPartly_1_3_2))
    }

    @Test
    fun `Getting presentation paths with complex object requesting parts of object returns the jwt with disclosures`() = runTest {
        val paths = SdJwt(ComplexSdJwt.SD_JWT).getPresentationPaths(
            listOf(
                ComplexSdJwt.pathObjectPartly_1_2_1,
                ComplexSdJwt.pathObjectPartly_1_2_3_1_1,
                ComplexSdJwt.pathObjectPartly_1_3,
            )
        )

        assertEquals(10, paths.size)
        assertTrue(paths.contains(ComplexSdJwt.pathObjectPartly_1_2))
        assertTrue(paths.contains(ComplexSdJwt.pathObjectPartly_1_2_1))
        assertTrue(paths.contains(ComplexSdJwt.pathObjectPartly_1_2_2))
        assertTrue(paths.contains(ComplexSdJwt.pathObjectPartly_1_2_3_array))
        assertTrue(paths.contains(ComplexSdJwt.pathObjectPartly_1_2_3_1))
        assertTrue(paths.contains(ComplexSdJwt.pathObjectPartly_1_2_3_1_1_array))
        assertTrue(paths.contains(ComplexSdJwt.pathObjectPartly_1_2_3_1_1_1))
        assertTrue(paths.contains(ComplexSdJwt.pathObjectPartly_1_3_array))
        assertTrue(paths.contains(ComplexSdJwt.pathObjectPartly_1_3_1))
        assertTrue(paths.contains(ComplexSdJwt.pathObjectPartly_1_3_2))
    }

    private companion object {
        val PAYLOAD_WITH_FLAT_DISCLOSURES = FlatSdJwt.JWT + FlatDisclosures
    }
}
