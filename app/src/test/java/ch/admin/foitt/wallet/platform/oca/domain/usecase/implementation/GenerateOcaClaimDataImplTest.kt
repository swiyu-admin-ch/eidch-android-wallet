package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.oca.domain.model.AttributeType
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.CharacterEncoding
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.Standard
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GenerateOcaClaimData
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ATTRIBUTE_KEY_AGE
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ATTRIBUTE_KEY_FIRSTNAME
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ATTRIBUTE_KEY_LASTNAME
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ATTRIBUTE_KEY_NAME
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ATTRIBUTE_KEY_RACE
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ATTRIBUTE_LABEL_AGE_DE
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ATTRIBUTE_LABEL_AGE_EN
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ATTRIBUTE_LABEL_FIRSTNAME_DE
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ATTRIBUTE_LABEL_FIRSTNAME_EN
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.CLAIMS_PATH_POINTER_AGE
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.CLAIMS_PATH_POINTER_FIRSTNAME
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.CREDENTIAL_FORMAT
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.DIGEST
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ENTRY_CODE_A
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ENTRY_CODE_A_DE
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ENTRY_CODE_A_EN
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ENTRY_CODE_B
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ENTRY_CODE_B_DE
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ENTRY_CODE_B_EN
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.FORMAT
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.LANGUAGE_DE
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.LANGUAGE_EN
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.UNKNOWN_ENCODING
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ocaSensitiveEntry
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ocaSimpleDataSourceMultiVersion
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ocaSimpleDataSourceV1
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ocaSimpleDataSourceV2
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ocaSimpleEncoding
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ocaSimpleEncodingNoDefault
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ocaSimpleEntry
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ocaSimpleFormat
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ocaSimpleLabel
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ocaSimpleOrder
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ocaSimpleStandard
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class GenerateOcaClaimDataImplTest {

    private lateinit var useCase: GenerateOcaClaimData

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = GenerateOcaClaimDataImpl()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Generator correctly generates all attributes labels`() = runTest {
        val overlayBundleAttributes =
            useCase(overlays = ocaSimpleLabel.overlays, captureBases = ocaSimpleLabel.captureBases)

        val expectedLabelsFirstname = mapOf(
            LANGUAGE_EN to ATTRIBUTE_LABEL_FIRSTNAME_EN,
            LANGUAGE_DE to ATTRIBUTE_LABEL_FIRSTNAME_DE,
        )

        val expectedLabelsAge = mapOf(
            LANGUAGE_EN to ATTRIBUTE_LABEL_AGE_EN,
            LANGUAGE_DE to ATTRIBUTE_LABEL_AGE_DE,
        )

        assertEquals(2, overlayBundleAttributes.size)

        assertEquals(DIGEST, overlayBundleAttributes[0].captureBaseDigest)
        assertEquals(ATTRIBUTE_KEY_FIRSTNAME, overlayBundleAttributes[0].name)
        assertEquals(AttributeType.Text, overlayBundleAttributes[0].attributeType)
        assertEquals(expectedLabelsFirstname, overlayBundleAttributes[0].labels)

        assertEquals(DIGEST, overlayBundleAttributes[1].captureBaseDigest)
        assertEquals(ATTRIBUTE_KEY_AGE, overlayBundleAttributes[1].name)
        assertEquals(AttributeType.Numeric, overlayBundleAttributes[1].attributeType)
        assertEquals(expectedLabelsAge, overlayBundleAttributes[1].labels)
    }

    @TestFactory
    fun `Generator correctly generates all attributes data sources overlay`(): List<DynamicTest> {
        val input = listOf(
            ocaSimpleDataSourceV1,
            ocaSimpleDataSourceV2,
            ocaSimpleDataSourceMultiVersion,
        )

        return input.map { ocaBundle ->
            DynamicTest.dynamicTest("Oca: $ocaBundle should return claim data containing claims path pointers") {
                runTest {
                    val overlayBundleAttributes =
                        useCase(overlays = ocaBundle.overlays, captureBases = ocaBundle.captureBases)

                    val expectedDataSourcesFirstname = mapOf(
                        CREDENTIAL_FORMAT to CLAIMS_PATH_POINTER_FIRSTNAME
                    )

                    val expectedDataSourcesAge = mapOf(
                        CREDENTIAL_FORMAT to CLAIMS_PATH_POINTER_AGE
                    )

                    assertEquals(2, overlayBundleAttributes.size)

                    assertEquals(DIGEST, overlayBundleAttributes[0].captureBaseDigest)
                    assertEquals(ATTRIBUTE_KEY_FIRSTNAME, overlayBundleAttributes[0].name)
                    assertEquals(AttributeType.Text, overlayBundleAttributes[0].attributeType)
                    assertEquals(expectedDataSourcesFirstname, overlayBundleAttributes[0].dataSources)

                    assertEquals(DIGEST, overlayBundleAttributes[1].captureBaseDigest)
                    assertEquals(ATTRIBUTE_KEY_AGE, overlayBundleAttributes[1].name)
                    assertEquals(AttributeType.Numeric, overlayBundleAttributes[1].attributeType)
                    assertEquals(expectedDataSourcesAge, overlayBundleAttributes[1].dataSources)
                }
            }
        }
    }

    @Test
    fun `Generator correctly generates all attributes formats`() = runTest {
        val overlayBundleAttributes =
            useCase(overlays = ocaSimpleFormat.overlays, captureBases = ocaSimpleFormat.captureBases)

        assertEquals(2, overlayBundleAttributes.size)

        assertEquals(DIGEST, overlayBundleAttributes[0].captureBaseDigest)
        assertEquals(ATTRIBUTE_KEY_FIRSTNAME, overlayBundleAttributes[0].name)
        assertEquals(AttributeType.Text, overlayBundleAttributes[0].attributeType)
        assertEquals(FORMAT, overlayBundleAttributes[0].format)

        assertEquals(DIGEST, overlayBundleAttributes[1].captureBaseDigest)
        assertEquals(ATTRIBUTE_KEY_AGE, overlayBundleAttributes[1].name)
        assertEquals(AttributeType.Numeric, overlayBundleAttributes[1].attributeType)
        assertEquals(null, overlayBundleAttributes[1].format)
    }

    @Test
    fun `Generator correctly generates all attributes standards`() = runTest {
        val overlayBundleAttributes =
            useCase(overlays = ocaSimpleStandard.overlays, captureBases = ocaSimpleStandard.captureBases)

        assertEquals(2, overlayBundleAttributes.size)

        assertEquals(DIGEST, overlayBundleAttributes[0].captureBaseDigest)
        assertEquals(ATTRIBUTE_KEY_FIRSTNAME, overlayBundleAttributes[0].name)
        assertEquals(AttributeType.Text, overlayBundleAttributes[0].attributeType)
        assertEquals(Standard.DataUrl, overlayBundleAttributes[0].standard)

        assertEquals(DIGEST, overlayBundleAttributes[1].captureBaseDigest)
        assertEquals(ATTRIBUTE_KEY_AGE, overlayBundleAttributes[1].name)
        assertEquals(AttributeType.Numeric, overlayBundleAttributes[1].attributeType)
        assertEquals(null, overlayBundleAttributes[1].standard)
    }

    @Test
    fun `Generator correctly generates all attributes encodings`() = runTest {
        val overlayBundleAttributes =
            useCase(overlays = ocaSimpleEncoding.overlays, captureBases = ocaSimpleEncoding.captureBases)

        assertEquals(2, overlayBundleAttributes.size)

        assertEquals(DIGEST, overlayBundleAttributes[0].captureBaseDigest)
        assertEquals(ATTRIBUTE_KEY_FIRSTNAME, overlayBundleAttributes[0].name)
        assertEquals(AttributeType.Text, overlayBundleAttributes[0].attributeType)
        assertEquals(CharacterEncoding.Base64, overlayBundleAttributes[0].characterEncoding)

        assertEquals(DIGEST, overlayBundleAttributes[1].captureBaseDigest)
        assertEquals(ATTRIBUTE_KEY_AGE, overlayBundleAttributes[1].name)
        assertEquals(AttributeType.Numeric, overlayBundleAttributes[1].attributeType)
        assertEquals(CharacterEncoding.Unknown(rawValue = UNKNOWN_ENCODING), overlayBundleAttributes[1].characterEncoding)
    }

    @Test
    fun `Generator correctly generates all attributes encodings without default encoding`() = runTest {
        val overlayBundleAttributes =
            useCase(overlays = ocaSimpleEncodingNoDefault.overlays, captureBases = ocaSimpleEncodingNoDefault.captureBases)

        assertEquals(2, overlayBundleAttributes.size)

        assertEquals(DIGEST, overlayBundleAttributes[1].captureBaseDigest)
        assertEquals(ATTRIBUTE_KEY_AGE, overlayBundleAttributes[1].name)
        assertEquals(AttributeType.Numeric, overlayBundleAttributes[1].attributeType)
        assertEquals(null, overlayBundleAttributes[1].characterEncoding)
    }

    @Test
    fun `Generator correctly generate all attributes orders`() = runTest {
        val overlayBundleAttributes =
            useCase(overlays = ocaSimpleOrder.overlays, captureBases = ocaSimpleOrder.captureBases)

        assertEquals(2, overlayBundleAttributes.size)

        assertEquals(DIGEST, overlayBundleAttributes[0].captureBaseDigest)
        assertEquals(ATTRIBUTE_KEY_FIRSTNAME, overlayBundleAttributes[0].name)
        assertEquals(2, overlayBundleAttributes[0].order)

        assertEquals(DIGEST, overlayBundleAttributes[1].captureBaseDigest)
        assertEquals(ATTRIBUTE_KEY_AGE, overlayBundleAttributes[1].name)
        assertEquals(1, overlayBundleAttributes[1].order)
    }

    @Test
    fun `Generator correctly generates all attributes entry mappings`() = runTest {
        val ocaClaimData = useCase(overlays = ocaSimpleEntry.overlays, captureBases = ocaSimpleEntry.captureBases)

        assertEquals(2, ocaClaimData.size)

        val expectedEntriesFirstname = mapOf(
            LANGUAGE_EN to mapOf(ENTRY_CODE_A to ENTRY_CODE_A_EN),
            LANGUAGE_DE to mapOf(ENTRY_CODE_A to ENTRY_CODE_A_DE),
        )

        val expectedEntriesAge = mapOf(
            LANGUAGE_EN to mapOf(ENTRY_CODE_B to ENTRY_CODE_B_EN),
            LANGUAGE_DE to mapOf(ENTRY_CODE_B to ENTRY_CODE_B_DE),
        )

        assertEquals(DIGEST, ocaClaimData[0].captureBaseDigest)
        assertEquals(expectedEntriesFirstname, ocaClaimData[0].entryMappings)

        assertEquals(DIGEST, ocaClaimData[1].captureBaseDigest)
        assertEquals(expectedEntriesAge, ocaClaimData[1].entryMappings)
    }

    @Test
    fun `Generator correctly generates all sensitive attributes`() = runTest {
        val ocaClaimData = useCase(overlays = ocaSensitiveEntry.overlays, captureBases = ocaSensitiveEntry.captureBases)

        assertEquals(7, ocaClaimData.size)

        val sensitiveAttributes =
            listOf(ATTRIBUTE_KEY_FIRSTNAME, ATTRIBUTE_KEY_AGE, ATTRIBUTE_KEY_NAME, ATTRIBUTE_KEY_RACE, ATTRIBUTE_KEY_LASTNAME)

        assertEquals(DIGEST, ocaClaimData[0].captureBaseDigest)
        assertEquals(sensitiveAttributes, ocaClaimData.filter { it.isSensitive }.map { it.name })
    }
}
