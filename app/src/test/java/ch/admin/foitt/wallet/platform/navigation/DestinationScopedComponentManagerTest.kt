package ch.admin.foitt.wallet.platform.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import ch.admin.foitt.wallet.platform.actorMetadata.di.ActorRepositoryEntryPoint
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.implementation.DestinationScopedComponentManagerImpl
import ch.admin.foitt.wallet.util.assertTrue
import dagger.hilt.EntryPoints
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

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
            backstack
        }

        coEvery { EntryPoints.get<Any>(any(), any()) } returns mockActorRepositoryEntryPoint

        coEvery { mockDestinationsComponentBuilder.setScope(any()) } returns mockDestinationsComponentBuilder
        coEvery { mockDestinationsComponentBuilder.build() } returns mockDestinationScopedComponent

        coEvery { mockNavManager.currentDestination } returns Destination.CredentialOfferScreen(credentialId = -1)
        coEvery { mockNavManager.backstack } returns backstack
        coEvery { mockNavManager.backstackFlow } returns backStackFlow

        manager = spyk(
            DestinationScopedComponentManagerImpl(
                componentBuilder = mockDestinationsComponentBuilder,
                navManager = mockNavManager,
                ioDispatcherScope = TestScope()
            ),
            recordPrivateCalls = true,
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
    }

    @Test
    fun `Getting an EntryPoint without a corresponding scope throw an exception`(): Unit = runTest {
        // Home is not in Verifier.Scope
        coEvery { mockNavManager.currentDestination } returns Destination.HomeScreen

        assertThrows<IllegalArgumentException> {
            manager.getEntryPoint(
                ActorRepositoryEntryPoint::class.java,
                ComponentScope.Verifier
            )
        }
    }

    @Test
    fun `Getting an EntryPoint for a not yet cached scope generate a new component`(): Unit = runTest {
        val scopedComponents = manager.getScopedComponentsField()

        coEvery { mockNavManager.currentDestination } returns Destination.NonComplianceDescriptionInputScreen
        manager.getEntryPoint(
            ActorRepositoryEntryPoint::class.java,
            ComponentScope.NonComplianceFormInput
        )

        assertEquals(1, scopedComponents.size)
        assertTrue(scopedComponents.entries.elementAt(0).key == ComponentScope.NonComplianceFormInput) {
            "Retrieved component has expected type"
        }

        coEvery { mockNavManager.currentDestination } returns Destination.PresentationSuccessScreen(sentFields = emptyList())
        manager.getEntryPoint(
            ActorRepositoryEntryPoint::class.java,
            ComponentScope.Verifier
        )

        assertEquals(2, scopedComponents.size)
        assertTrue(scopedComponents.entries.elementAt(1).key == ComponentScope.Verifier) {
            "Retrieved component has expected type"
        }
    }

    @Test
    fun `Getting an EntryPoint for an already cached scope reuse an existing component`(): Unit = runTest {
        val scopedComponents = manager.getScopedComponentsField()

        coEvery { mockNavManager.currentDestination } returns Destination.ActivityListScreen(0)
        manager.getEntryPoint(
            ActorRepositoryEntryPoint::class.java,
            ComponentScope.Verifier
        )

        assertEquals(1, scopedComponents.size)
        assertTrue(scopedComponents.entries.elementAt(0).key == ComponentScope.Verifier) {
            "Retrieved component has expected type"
        }

        coEvery { mockNavManager.currentDestination } returns Destination.ActivityListScreen(0)
        manager.getEntryPoint(
            ActorRepositoryEntryPoint::class.java,
            ComponentScope.Verifier
        )
        assertEquals(1, scopedComponents.size)
    }

    @Suppress("UNCHECKED_CAST")
    private fun DestinationScopedComponentManagerImpl.getScopedComponentsField(): MutableMap<ComponentScope, DestinationScopedComponent> {
        val field = this::class.memberProperties
            .first { member ->
                member.name == "scopedComponents"
            }.apply { isAccessible = true }
            .getter.call(this)
        return field as MutableMap<ComponentScope, DestinationScopedComponent>
    }
}
