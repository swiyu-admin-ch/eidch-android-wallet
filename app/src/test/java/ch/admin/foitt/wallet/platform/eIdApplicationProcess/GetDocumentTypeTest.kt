package ch.admin.foitt.wallet.platform.eIdApplicationProcess

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.di.EidApplicationProcessEntryPoint
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdUiDocumentType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EidApplicationProcessRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.GetDocumentTypeImpl
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponentManager
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class GetDocumentTypeTest {

    @MockK
    private lateinit var mockDestinationScopedComponentManager: DestinationScopedComponentManager

    @MockK
    private lateinit var mockEidApplicationProcessRepository: EidApplicationProcessRepository

    @MockK
    private lateinit var mockEidApplicationProcessEntryPoint: EidApplicationProcessEntryPoint

    private lateinit var mockFlow: MutableStateFlow<EIdUiDocumentType>

    private lateinit var useCase: GetDocumentTypeImpl

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockFlow = MutableStateFlow(EIdUiDocumentType.IDENTITY_CARD)

        useCase = GetDocumentTypeImpl(
            destinationScopedComponentManager = mockDestinationScopedComponentManager,
        )

        coEvery {
            mockDestinationScopedComponentManager.getEntryPoint(
                EidApplicationProcessEntryPoint::class.java,
                componentScope = any()
            )
        } returns mockEidApplicationProcessEntryPoint

        coEvery {
            mockEidApplicationProcessEntryPoint.eidApplicationProcessRepository()
        } returns mockEidApplicationProcessRepository

        coEvery {
            mockEidApplicationProcessRepository.documentType
        } returns mockFlow
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @ParameterizedTest
    @EnumSource(value = EIdUiDocumentType::class)
    fun `DocumentType is getting the proper value from the repository`(documentType: EIdUiDocumentType): Unit =
        runTest {
            mockFlow.value = documentType

            val resultFlow = useCase.invoke()

            assert(documentType == resultFlow.value)

            coVerifyOrder {
                mockDestinationScopedComponentManager.getEntryPoint(
                    EidApplicationProcessEntryPoint::class.java,
                    any()
                )
                mockEidApplicationProcessEntryPoint.eidApplicationProcessRepository()
                mockEidApplicationProcessRepository.documentType
            }
        }
}
