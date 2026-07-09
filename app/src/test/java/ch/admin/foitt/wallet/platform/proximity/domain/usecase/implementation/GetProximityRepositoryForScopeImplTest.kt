package ch.admin.foitt.wallet.platform.proximity.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponentManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.proximity.di.ProximityRepositoryEntryPoint
import ch.admin.foitt.wallet.platform.proximity.domain.model.domain.repository.ProximityRepository
import ch.admin.foitt.wallet.platform.proximity.domain.usecase.GetProximityRepositoryForScope
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verifyOrder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetProximityRepositoryForScopeImplTest {

    @MockK
    private lateinit var mockDestinationScopedComponentManager: DestinationScopedComponentManager

    @MockK
    private lateinit var mockProximityRepository: ProximityRepository

    @MockK
    private lateinit var mockProximityRepositoryEntryPoint: ProximityRepositoryEntryPoint

    private lateinit var useCase: GetProximityRepositoryForScope

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = GetProximityRepositoryForScopeImpl(
            destinationScopedComponentManager = mockDestinationScopedComponentManager,
        )

        every {
            mockDestinationScopedComponentManager.getEntryPoint(ProximityRepositoryEntryPoint::class.java, componentScope = any())
        } returns mockProximityRepositoryEntryPoint

        every {
            mockProximityRepositoryEntryPoint.proximityRepository()
        } returns mockProximityRepository
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `GetProximityRepositoryForScopeImpl is following specific steps`() {
        val proximityRepository = useCase.invoke()

        assertEquals(mockProximityRepository, proximityRepository)

        verifyOrder {
            mockDestinationScopedComponentManager.getEntryPoint(ProximityRepositoryEntryPoint::class.java, ComponentScope.Verifier)
            mockProximityRepositoryEntryPoint.proximityRepository()
        }
    }
}
