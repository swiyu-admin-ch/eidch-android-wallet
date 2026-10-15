package ch.admin.foitt.wallet.platform.actorMetadata

import ch.admin.foitt.wallet.platform.actorMetadata.di.ActorRepositoryEntryPoint
import ch.admin.foitt.wallet.platform.actorMetadata.domain.repository.ActorRepository
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.InitializeActorForScope
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.implementation.InitializeActorForScopeImpl
import ch.admin.foitt.wallet.platform.actorMetadata.mock.ActorMetadataMocks.mockActorDisplayData01
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponentManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InitializeActorForScopeTest {

    @MockK
    private lateinit var mockDestinationScopedComponentManager: DestinationScopedComponentManager

    @MockK
    private lateinit var mockActorRepository: ActorRepository

    @MockK
    private lateinit var mockActorRepositoryEntryPoint: ActorRepositoryEntryPoint

    private lateinit var useCase: InitializeActorForScope

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = InitializeActorForScopeImpl(
            destinationScopedComponentManager = mockDestinationScopedComponentManager,
        )

        every {
            mockDestinationScopedComponentManager.isInScope(any())
        } returns true

        coEvery {
            mockDestinationScopedComponentManager.getEntryPoint(ActorRepositoryEntryPoint::class.java, componentScope = any())
        } returns mockActorRepositoryEntryPoint

        coEvery {
            mockActorRepositoryEntryPoint.actorRepository()
        } returns mockActorRepository

        coEvery {
            mockActorRepository.setActor(any())
        } just runs
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `InitializeActorForScope does set actor when the scope is already gone`(): Unit = runTest {
        every {
            mockDestinationScopedComponentManager.isInScope(any())
        } returns false

        useCase.invoke(
            actorDisplayData = mockActorDisplayData01,
            componentScope = ComponentScope.CredentialIssuer,
        )

        coVerify(exactly = 0) {
            mockDestinationScopedComponentManager.getEntryPoint(ActorRepositoryEntryPoint::class.java, any())
            mockActorRepository.setActor(any())
        }
    }

    @Test
    fun `InitializeActorForScope is following specific steps`(): Unit = runTest {
        useCase.invoke(
            actorDisplayData = mockActorDisplayData01,
            componentScope = ComponentScope.CredentialIssuer,
        )

        coVerifyOrder {
            mockDestinationScopedComponentManager.getEntryPoint(ActorRepositoryEntryPoint::class.java, any())
            mockActorRepositoryEntryPoint.actorRepository()
            mockActorRepository.setActor(
                actor = mockActorDisplayData01,
            )
        }
    }
}
