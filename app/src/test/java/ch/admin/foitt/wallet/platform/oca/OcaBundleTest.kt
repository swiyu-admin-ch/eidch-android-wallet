package ch.admin.foitt.wallet.platform.oca

import ch.admin.foitt.wallet.platform.oca.domain.model.AttributeType
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaClaimData
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GenerateOcaClaimData
import ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation.GenerateOcaClaimDataImpl
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ATTRIBUTE_KEY_AGE
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ATTRIBUTE_KEY_FIRSTNAME
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ATTRIBUTE_LABEL_AGE_DE
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ATTRIBUTE_LABEL_AGE_EN
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ATTRIBUTE_LABEL_FIRSTNAME_DE
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ATTRIBUTE_LABEL_FIRSTNAME_EN
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.CLAIMS_PATH_POINTER_AGE
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.CLAIMS_PATH_POINTER_FIRSTNAME
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.CREDENTIAL_FORMAT
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.DIGEST
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.LANGUAGE_DE
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.LANGUAGE_EN
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ocaSimple
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.ocaSimpleClaimsPathPointer
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class OcaBundleTest {

    private lateinit var generateOcaClaimData: GenerateOcaClaimData

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        generateOcaClaimData = GenerateOcaClaimDataImpl()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @TestFactory
    fun `OcaBundle correctly gets attributes for data source format`(): List<DynamicTest> {
        val input = listOf(ocaSimple, ocaSimpleClaimsPathPointer)

        return input.map { ocaBundle ->
            DynamicTest.dynamicTest("Oca bundle should return success") {
                runTest {
                    val result = ocaBundle.getAttributesForDataSourceFormat(CREDENTIAL_FORMAT, null)

                    val expectedLabelsFirstname = mapOf(
                        LANGUAGE_EN to ATTRIBUTE_LABEL_FIRSTNAME_EN,
                        LANGUAGE_DE to ATTRIBUTE_LABEL_FIRSTNAME_DE,
                    )

                    val expectedDataSourcesFirstname = mapOf(
                        CREDENTIAL_FORMAT to CLAIMS_PATH_POINTER_FIRSTNAME
                    )

                    val expectedLabelsAge = mapOf(
                        LANGUAGE_EN to ATTRIBUTE_LABEL_AGE_EN,
                        LANGUAGE_DE to ATTRIBUTE_LABEL_AGE_DE,
                    )

                    val expectedDataSourcesAge = mapOf(
                        CREDENTIAL_FORMAT to CLAIMS_PATH_POINTER_AGE
                    )

                    val expected = mapOf(
                        CLAIMS_PATH_POINTER_FIRSTNAME to OcaClaimData(
                            captureBaseDigest = DIGEST,
                            name = ATTRIBUTE_KEY_FIRSTNAME,
                            attributeType = AttributeType.Text,
                            labels = expectedLabelsFirstname,
                            dataSources = expectedDataSourcesFirstname,
                            isSensitive = false
                        ),
                        CLAIMS_PATH_POINTER_AGE to OcaClaimData(
                            captureBaseDigest = DIGEST,
                            name = ATTRIBUTE_KEY_AGE,
                            attributeType = AttributeType.Numeric,
                            labels = expectedLabelsAge,
                            dataSources = expectedDataSourcesAge,
                            isSensitive = false
                        )
                    )

                    assertEquals(expected, result)
                }
            }
        }
    }
}
