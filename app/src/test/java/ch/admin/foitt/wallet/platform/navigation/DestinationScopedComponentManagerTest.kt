package ch.admin.foitt.wallet.platform.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import ch.admin.foitt.wallet.platform.actorMetadata.di.ActorRepositoryEntryPoint
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.implementation.DestinationScopedComponentManagerImpl
import dagger.hilt.EntryPoints
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DestinationScopedComponentManagerTest {
    @MockK
    private lateinit var mockNavManager: NavigationManager

    @MockK
    private lateinit var mockDestinationsComponentBuilder: DestinationsComponentBuilder

    @MockK
    private lateinit var mockDestinationScopedComponent: DestinationScopedComponent

    @MockK
    private lateinit var mockActorRepositoryEntryPoint: ActorRepositoryEntryPoint

    private lateinit var backstack: SnapshotStateList<Destination>
    private lateinit var backStackFlow: Flow<List<Destination>>
    private lateinit var manager: DestinationScopedComponentManagerImpl

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockkStatic(EntryPoints::class)

        backstack = mutableStateListOf(
            Destination.HomeScreen,
            Destination.OnboardingSuccessScreen,
        )
        backStackFlow = flow {
            emit(backstack.toList())
        }

        coEvery { EntryPoints.get<Any>(any(), any()) } returns mockActorRepositoryEntryPoint

        coEvery { mockDestinationsComponentBuilder.setScope(any()) } returns mockDestinationsComponentBuilder
        coEvery { mockDestinationsComponentBuilder.build() } returns mockDestinationScopedComponent

        coEvery { mockNavManager.currentDestination } returns Destination.CredentialOfferScreen(credentialId = -1)
        coEvery { mockNavManager.backstack } returns backstack
        coEvery { mockNavManager.backstackFlow } returns backStackFlow

        manager = DestinationScopedComponentManagerImpl(
            componentBuilder = mockDestinationsComponentBuilder,
            navManager = mockNavManager,
            ioDispatcherScope = TestScope()
        )
    }

    @AfterEach
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `Getting an EntryPoint with a corresponding registered scope succeeds`(): Unit = runTest {
        coEvery { mockNavManager.currentDestination } returns Destination.ActivityListScreen(0)

        manager.getEntryPoint(
            ActorRepositoryEntryPoint::class.java,
            ComponentScope.Verifier
        )

        // Verify the builder was actually used to create the component
        verify(exactly = 1) { mockDestinationsComponentBuilder.build() }
    }

    @Test
    fun `Getting an EntryPoint without a corresponding scope throw an exception`(): Unit = runTest {
        coEvery { mockNavManager.currentDestination } returns Destination.HomeScreen

        assertThrows<IllegalArgumentException> {
            manager.getEntryPoint(
                ActorRepositoryEntryPoint::class.java,
                ComponentScope.Verifier
            )
        }
        // Verify that if it fails, it never even tried to build the component
        verify(exactly = 0) { mockDestinationsComponentBuilder.build() }
    }

    @Test
    fun `Getting an EntryPoint for a not yet cached scope generate a new component`(): Unit = runTest {
        coEvery { mockNavManager.currentDestination } returns Destination.NonComplianceDescriptionInputScreen
        manager.getEntryPoint(
            ActorRepositoryEntryPoint::class.java,
            ComponentScope.NonComplianceFormInput
        )

        coEvery { mockNavManager.currentDestination } returns Destination.PresentationSuccessScreen(redirectUri = "")
        manager.getEntryPoint(
            ActorRepositoryEntryPoint::class.java,
            ComponentScope.Verifier
        )

        verify(exactly = 2) { mockDestinationsComponentBuilder.build() }
    }

    @Test
    fun `Getting an EntryPoint for an already cached scope reuse an existing component`(): Unit = runTest {
        coEvery { mockNavManager.currentDestination } returns Destination.ActivityListScreen(0)

        // First call: should trigger build()
        manager.getEntryPoint(
            ActorRepositoryEntryPoint::class.java,
            ComponentScope.Verifier
        )

        // Second call: should NOT trigger build() again (it should come from the map)
        coEvery { mockNavManager.currentDestination } returns Destination.ActivityListScreen(0)
        manager.getEntryPoint(
            ActorRepositoryEntryPoint::class.java,
            ComponentScope.Verifier
        )

        // build() was called only once despite two requests
        verify(exactly = 1) { mockDestinationsComponentBuilder.build() }
    }
}
