package ch.admin.foitt.wallet.platform.navigation.implementation

import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.domain.model.DestinationGroup
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class NavigationManagerImplTest {

    private lateinit var navManager: NavigationManager

    @BeforeEach
    fun setUp() {
        navManager = NavigationManagerImpl()
    }

    @Test
    fun `fresh navigation manager starts with Start`() = runTest {
        assertEquals(Destination.StartScreen, navManager.currentDestination)
        navManager.assertBackstack(Destination.StartScreen)
    }

    @Test
    fun `navigateTo pushes destinations`() = runTest {
        navManager.assertBackstack(Destination.StartScreen)

        listOf(Destination.OnboardingIntroScreen).addToBackstack()
        navManager.assertBackstack(Destination.StartScreen, Destination.OnboardingIntroScreen)

        listOf(Destination.StartScreen).addToBackstack()
        navManager.assertBackstack(
            Destination.StartScreen,
            Destination.OnboardingIntroScreen,
            Destination.StartScreen
        )
    }

    @Test
    fun `popBackStack removes the top element until empty`() = runTest {
        listOf(Destination.OnboardingIntroScreen, Destination.OnboardingLocalDataScreen).addToBackstack()
        assertEquals(3, navManager.backstack.size)
        assertEquals(Destination.OnboardingLocalDataScreen, navManager.currentDestination)

        repeat(3) {
            navManager.popBackStack()
        }

        assertEquals(0, navManager.backstack.size)
    }

    @Test
    fun `popBackStack on empty stack does nothing`() = runTest {
        navManager.popBackStack()
        assertEquals(0, navManager.backstack.size)

        navManager.popBackStack()
        assertEquals(0, navManager.backstack.size)
    }

    @Test
    fun `popBackStackTo navigates to correct destination in backStack`() = runTest {
        navManager.popBackStackTo(Destination.StartScreen::class, true)
        navManager.assertBackstack()

        listOf(Destination.StartScreen).addToBackstack()
        navManager.popBackStackTo(Destination.StartScreen::class, false)
        navManager.assertBackstack(Destination.StartScreen)

        listOf(Destination.OnboardingIntroScreen, Destination.HomeScreen).addToBackstack()
        navManager.popBackStackTo(Destination.StartScreen::class, false)
        navManager.assertBackstack(Destination.StartScreen)

        listOf(Destination.OnboardingIntroScreen, Destination.HomeScreen).addToBackstack()
        navManager.popBackStackTo(Destination.StartScreen::class, true)
        navManager.assertBackstack()

        listOf(Destination.StartScreen, Destination.HomeScreen, Destination.StartScreen).addToBackstack()
        navManager.popBackStackTo(Destination.StartScreen::class, true)
        navManager.assertBackstack(Destination.StartScreen, Destination.HomeScreen)
    }

    @Test
    fun `popBackStackTo with destination not in backStack does not navigate`() = runTest {
        listOf(Destination.HomeScreen, Destination.OnboardingIntroScreen).addToBackstack()
        navManager.popBackStackTo(Destination.OnboardingErrorScreen::class, true)
        navManager.assertBackstack(Destination.StartScreen, Destination.HomeScreen, Destination.OnboardingIntroScreen)

        navManager.popBackStackTo(Destination.OnboardingErrorScreen::class, false)
        navManager.assertBackstack(Destination.StartScreen, Destination.HomeScreen, Destination.OnboardingIntroScreen)
    }

    @Test
    fun `replaceCurrentWith replaces the top destination`() = runTest {
        listOf(Destination.OnboardingIntroScreen).addToBackstack()
        navManager.assertBackstack(Destination.StartScreen, Destination.OnboardingIntroScreen)

        navManager.replaceCurrentWith(Destination.HomeScreen)
        navManager.assertBackstack(Destination.StartScreen, Destination.HomeScreen)
        assertEquals(Destination.HomeScreen, navManager.currentDestination)
    }

    @Test
    fun `popBackStackOrToRoot navigates to Home if no previous destination in backstack`() = runTest {
        navManager.popBackStack() // empty backstack
        assertEquals(0, navManager.backstack.size)

        navManager.popBackStackOrToRoot()
        navManager.assertBackstack(Destination.HomeScreen)
    }

    @Test
    fun `popBackStackOrToRoot pops backstack if possible`() = runTest {
        // Add another destination to backstack so navigating back is possible
        listOf(Destination.OnboardingIntroScreen).addToBackstack()

        navManager.popBackStackOrToRoot()
        navManager.assertBackstack(Destination.StartScreen)
    }

    @Test
    fun `navigateToAndPopUpTo pops inclusive and pushes new destination`() = runTest {
        listOf(Destination.OnboardingIntroScreen, Destination.OnboardingLocalDataScreen).addToBackstack()

        navManager.popUpToAndNavigate(
            popToInclusive = Destination.OnboardingIntroScreen::class,
            destination = Destination.HomeScreen
        )

        navManager.assertBackstack(Destination.StartScreen, Destination.HomeScreen)
    }

    @Test
    fun `navigateToAndPopUpTo just navigates when popUpTo not found`() = runTest {
        navManager.popUpToAndNavigate(
            popToInclusive = Destination.OnboardingErrorScreen::class,
            destination = Destination.HomeScreen
        )

        navManager.assertBackstack(Destination.StartScreen, Destination.HomeScreen)
    }

    @Test
    fun `navigateOutOf navigates out of navigation scope`() = runTest {
        listOf(Destination.OnboardingIntroScreen, Destination.HomeScreen, Destination.OnboardingLocalDataScreen).addToBackstack()
        navManager.navigateOutOf(DestinationGroup.Onboarding::class)
        navManager.assertBackstack(Destination.StartScreen, Destination.OnboardingIntroScreen, Destination.HomeScreen)

        navManager.navigateOutOf(DestinationGroup.Onboarding::class)
        navManager.assertBackstack(Destination.StartScreen, Destination.OnboardingIntroScreen, Destination.HomeScreen)
    }

    @Test
    fun `Calling navigateOutOf with a Screen destination throws`() = runTest {
        assertThrows<IllegalStateException> {
            navManager.navigateOutOf(Destination.OnboardingLocalDataScreen::class)
        }
    }

    @Test
    fun `navigateOutAndTo navigates out of navigation scope`() = runTest {
        listOf(Destination.OnboardingIntroScreen, Destination.HomeScreen, Destination.OnboardingLocalDataScreen).addToBackstack()
        navManager.navigateOutAndTo(DestinationGroup.Onboarding::class, Destination.HomeScreen)
        navManager.assertBackstack(Destination.StartScreen, Destination.OnboardingIntroScreen, Destination.HomeScreen)
    }

    @Test
    fun `Calling navigateOutAndTo with a Screen destination as navigationScope throws`() = runTest {
        assertThrows<IllegalStateException> {
            navManager.navigateOutAndTo(
                destinationGroup = Destination.OnboardingLocalDataScreen::class,
                destination = Destination.HomeScreen
            )
        }
    }

    // Helper
    private fun List<Destination>.addToBackstack() = this.forEach { navManager.navigateTo(it) }
    private fun NavigationManager.assertBackstack(vararg expected: Destination) = assertEquals(expected.asList(), backstack)
}
