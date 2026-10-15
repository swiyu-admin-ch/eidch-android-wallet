package ch.admin.foitt.wallet.platform.navigation.implementation

import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.domain.model.DestinationGroup
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NavigationManagerImplTest {

    private lateinit var navManager: NavigationManager

    @BeforeEach
    fun setUp() {
        navManager = NavigationManagerImpl()
    }

    @Test
    fun `fresh navigation manager starts with Start`() = runTest {
        // Arrange
        // (handled by setUp: navManager is freshly constructed)

        // Act
        // No explicit action — verifying initial state

        // Assert
        assertEquals(Destination.StartScreen, navManager.currentDestination)
        navManager.assertBackstack(Destination.StartScreen)
    }

    @Test
    fun `navigateTo pushes destinations`() = runTest {
        // Arrange
        navManager.assertBackstack(Destination.StartScreen)

        // Act
        listOf(Destination.OnboardingIntroScreen).addToBackstack()

        // Assert
        navManager.assertBackstack(Destination.StartScreen, Destination.OnboardingIntroScreen)

        // Act
        listOf(Destination.StartScreen).addToBackstack()

        // Assert
        navManager.assertBackstack(
            Destination.StartScreen,
            Destination.OnboardingIntroScreen,
            Destination.StartScreen
        )
    }

    @Test
    fun `popBackStack removes the top element until empty`() = runTest {
        // Arrange
        listOf(Destination.OnboardingIntroScreen, Destination.OnboardingLocalDataScreen).addToBackstack()
        assertEquals(3, navManager.backstack.size)
        assertEquals(Destination.OnboardingLocalDataScreen, navManager.currentDestination)

        // Act
        repeat(3) {
            navManager.popBackStack()
        }

        // Assert
        assertEquals(0, navManager.backstack.size)
    }

    @Test
    fun `popBackStack on empty stack does nothing`() = runTest {
        // Arrange
        navManager.popBackStack()
        assertEquals(0, navManager.backstack.size)

        // Act
        navManager.popBackStack()

        // Assert
        assertEquals(0, navManager.backstack.size)
    }

    @Test
    fun `popBackStackTo navigates to correct destination in backStack`() = runTest {
        // Arrange
        // (initial state: [StartScreen])

        // Act
        navManager.popBackStackTo(Destination.StartScreen::class, true)

        // Assert
        navManager.assertBackstack()

        // Arrange
        listOf(Destination.StartScreen).addToBackstack()

        // Act
        navManager.popBackStackTo(Destination.StartScreen::class, false)

        // Assert
        navManager.assertBackstack(Destination.StartScreen)

        // Arrange
        listOf(Destination.OnboardingIntroScreen, Destination.HomeScreen).addToBackstack()

        // Act
        navManager.popBackStackTo(Destination.StartScreen::class, false)

        // Assert
        navManager.assertBackstack(Destination.StartScreen)

        // Arrange
        listOf(Destination.OnboardingIntroScreen, Destination.HomeScreen).addToBackstack()

        // Act
        navManager.popBackStackTo(Destination.StartScreen::class, true)

        // Assert
        navManager.assertBackstack()

        // Arrange
        listOf(Destination.StartScreen, Destination.HomeScreen, Destination.StartScreen).addToBackstack()

        // Act
        navManager.popBackStackTo(Destination.StartScreen::class, true)

        // Assert
        navManager.assertBackstack(Destination.StartScreen, Destination.HomeScreen)
    }

    @Test
    fun `popBackStackTo with destination not in backStack does not navigate`() = runTest {
        // Arrange
        listOf(Destination.HomeScreen, Destination.OnboardingIntroScreen).addToBackstack()

        // Act
        navManager.popBackStackTo(Destination.OnboardingFatalErrorScreen::class, true)

        // Assert
        navManager.assertBackstack(Destination.StartScreen, Destination.HomeScreen, Destination.OnboardingIntroScreen)

        // Act
        navManager.popBackStackTo(Destination.OnboardingFatalErrorScreen::class, false)

        // Assert
        navManager.assertBackstack(Destination.StartScreen, Destination.HomeScreen, Destination.OnboardingIntroScreen)
    }

    @Test
    fun `replaceCurrentWith replaces the top destination`() = runTest {
        // Arrange
        listOf(Destination.OnboardingIntroScreen).addToBackstack()
        navManager.assertBackstack(Destination.StartScreen, Destination.OnboardingIntroScreen)

        // Act
        navManager.replaceCurrentWith(Destination.HomeScreen)

        // Assert
        navManager.assertBackstack(Destination.StartScreen, Destination.HomeScreen)
        assertEquals(Destination.HomeScreen, navManager.currentDestination)
    }

    @Test
    fun `popBackStackOrToRoot navigates to Home if no previous destination in backstack`() = runTest {
        // Arrange
        navManager.popBackStack()
        assertEquals(0, navManager.backstack.size)

        // Act
        navManager.popBackStackOrToRoot()

        // Assert
        navManager.assertBackstack(Destination.HomeScreen)
    }

    @Test
    fun `popBackStackOrToRoot pops backstack if possible`() = runTest {
        // Arrange
        // Add another destination to backstack so navigating back is possible
        listOf(Destination.OnboardingIntroScreen).addToBackstack()

        // Act
        navManager.popBackStackOrToRoot()

        // Assert
        navManager.assertBackstack(Destination.StartScreen)
    }

    @Test
    fun `navigateToAndPopUpTo pops inclusive and pushes new destination`() = runTest {
        // Arrange
        listOf(Destination.OnboardingIntroScreen, Destination.OnboardingLocalDataScreen).addToBackstack()

        // Act
        navManager.popUpToAndNavigate(
            popToInclusive = Destination.OnboardingIntroScreen::class,
            destination = Destination.HomeScreen
        )

        // Assert
        navManager.assertBackstack(Destination.StartScreen, Destination.HomeScreen)
    }

    @Test
    fun `navigateToAndPopUpTo just navigates when popUpTo not found`() = runTest {
        // Arrange
        // (initial state: [StartScreen])

        // Act
        navManager.popUpToAndNavigate(
            popToInclusive = Destination.OnboardingFatalErrorScreen::class,
            destination = Destination.HomeScreen
        )

        // Assert
        navManager.assertBackstack(Destination.StartScreen, Destination.HomeScreen)
    }

    @Test
    fun `navigateOutOf navigates out of navigation scope`() = runTest {
        // Arrange
        listOf(Destination.OnboardingIntroScreen, Destination.HomeScreen, Destination.OnboardingLocalDataScreen).addToBackstack()

        // Act
        navManager.navigateOutOf(DestinationGroup.ONBOARDING)

        // Assert
        navManager.assertBackstack(Destination.StartScreen, Destination.OnboardingIntroScreen, Destination.HomeScreen)

        // Act
        navManager.navigateOutOf(DestinationGroup.ONBOARDING)

        // Assert
        navManager.assertBackstack(Destination.StartScreen, Destination.OnboardingIntroScreen, Destination.HomeScreen)
    }

    @Test
    fun `navigateOutAndTo navigates out of navigation scope`() = runTest {
        // Arrange
        listOf(Destination.OnboardingIntroScreen, Destination.HomeScreen, Destination.OnboardingLocalDataScreen).addToBackstack()

        // Act
        navManager.navigateOutAndTo(DestinationGroup.ONBOARDING, Destination.HomeScreen)

        // Assert
        navManager.assertBackstack(Destination.StartScreen, Destination.OnboardingIntroScreen, Destination.HomeScreen)
    }

    @Test
    fun `replaceCurrentWith on empty stack pushes the destination`() = runTest {
        // Arrange
        navManager.popBackStack()
        assertEquals(0, navManager.backstack.size)

        // Act
        navManager.replaceCurrentWith(Destination.HomeScreen)

        // Assert
        navManager.assertBackstack(Destination.HomeScreen)
        assertEquals(Destination.HomeScreen, navManager.currentDestination)
    }

    @Test
    fun `popBackStackOrToRoot with single non-root entry replaces with root`() = runTest {
        // Arrange
        // Stack is [StartScreen] — single entry, not HomeScreen

        // Act
        navManager.popBackStackOrToRoot()

        // Assert
        navManager.assertBackstack(Destination.HomeScreen)
        assertEquals(Destination.HomeScreen, navManager.currentDestination)
    }

    @Test
    fun `popBackStackOrToRoot with single root entry is a no-op`() = runTest {
        // Arrange
        // Set up stack as [HomeScreen]
        navManager.popBackStack()
        navManager.navigateTo(Destination.HomeScreen)

        // Act
        navManager.popBackStackOrToRoot()

        // Assert
        navManager.assertBackstack(Destination.HomeScreen)
    }

    @Test
    fun `popBackStackOrToRoot with deep stack pops one entry`() = runTest {
        // Arrange
        listOf(Destination.OnboardingIntroScreen, Destination.OnboardingLocalDataScreen, Destination.HomeScreen).addToBackstack()

        // Act
        navManager.popBackStackOrToRoot()

        // Assert
        navManager.assertBackstack(Destination.StartScreen, Destination.OnboardingIntroScreen, Destination.OnboardingLocalDataScreen)
    }

    @Test
    fun `navigateBackToHomeScreen pops to HomeScreen when it is already in the backstack`() = runTest {
        // Arrange
        listOf(Destination.HomeScreen, Destination.OnboardingIntroScreen, Destination.OnboardingLocalDataScreen).addToBackstack()

        // Act
        navManager.navigateBackToHomeScreen(popUntil = Destination.OnboardingIntroScreen::class)

        // Assert
        navManager.assertBackstack(Destination.StartScreen, Destination.HomeScreen)
        assertEquals(Destination.HomeScreen, navManager.currentDestination)
    }

    @Test
    fun `navigateBackToHomeScreen pops up to popUntil and pushes HomeScreen when HomeScreen is not in the backstack`() = runTest {
        // Arrange
        listOf(Destination.OnboardingIntroScreen, Destination.OnboardingLocalDataScreen).addToBackstack()

        // Act
        navManager.navigateBackToHomeScreen(popUntil = Destination.OnboardingIntroScreen::class)

        // Assert
        navManager.assertBackstack(Destination.StartScreen, Destination.HomeScreen)
        assertEquals(Destination.HomeScreen, navManager.currentDestination)
    }

    @Test
    fun `navigateBackToHomeScreen just pushes HomeScreen when popUntil is not found`() = runTest {
        // Arrange
        // Stack: [StartScreen]

        // Act
        navManager.navigateBackToHomeScreen(popUntil = Destination.OnboardingFatalErrorScreen::class)

        // Assert
        navManager.assertBackstack(Destination.StartScreen, Destination.HomeScreen)
        assertEquals(Destination.HomeScreen, navManager.currentDestination)
    }

    @Test
    fun `navigateOutAndTo does not push duplicate when destination is already on top after pop`() = runTest {
        // Arrange
        listOf(Destination.OnboardingIntroScreen, Destination.HomeScreen, Destination.OnboardingLocalDataScreen).addToBackstack()

        // Act
        navManager.navigateOutAndTo(destinationGroup = DestinationGroup.ONBOARDING, destination = Destination.HomeScreen)

        // Assert
        // After navigateOutOf(Onboarding), HomeScreen is already on top → should NOT push another HomeScreen
        navManager.assertBackstack(Destination.StartScreen, Destination.OnboardingIntroScreen, Destination.HomeScreen)
    }

    @Test
    fun `navigateOutOf restores root when entire backstack is in the destination group`() = runTest {
        // Arrange
        // Empty the default stack, then fill with only Onboarding destinations
        navManager.popBackStack()
        listOf(
            Destination.OnboardingIntroScreen,
            Destination.OnboardingLocalDataScreen
        ).addToBackstack()
        // Stack: [OnboardingIntroScreen, OnboardingLocalDataScreen] — all in Onboarding group

        // Act
        navManager.navigateOutOf(DestinationGroup.ONBOARDING)

        // Assert
        // Should not be empty — root (HomeScreen) is restored
        assertEquals(1, navManager.backstack.size)
        assertEquals(Destination.HomeScreen, navManager.currentDestination)
    }

    @Test
    fun `popBackStackTo returns true when destination is found`() = runTest {
        // Arrange
        listOf(Destination.OnboardingIntroScreen).addToBackstack()

        // Act
        val result = navManager.popBackStackTo(Destination.StartScreen::class, false)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `popBackStackTo returns false when destination is not found`() = runTest {
        // Arrange
        // (initial state: [StartScreen])

        // Act
        val result = navManager.popBackStackTo(Destination.OnboardingFatalErrorScreen::class, true)

        // Assert
        assertFalse(result)
    }

    @Test
    fun `popBackStackTo returns false when destination is on top and inclusive is false`() = runTest {
        // Arrange
        // Stack: [StartScreen], popBackStackTo(StartScreen, false) → nothing to remove

        // Act
        val result = navManager.popBackStackTo(Destination.StartScreen::class, false)

        // Assert
        assertFalse(result)
    }

    @Test
    fun `popBackStack returns true when an entry was popped`() = runTest {
        // Arrange
        // (initial state: [StartScreen])

        // Act
        val result = navManager.popBackStack()

        // Assert
        assertTrue(result)
        assertEquals(0, navManager.backstack.size)
    }

    @Test
    fun `popBackStack returns false when the stack is empty`() = runTest {
        // Arrange
        navManager.popBackStack()

        // Act
        val result = navManager.popBackStack()

        // Assert
        assertFalse(result)
    }

    @Test
    fun `popUpToAndNavigate pops up to the last matching destination when duplicates exist`() = runTest {
        // Arrange
        // stack contains duplicate OnboardingIntroScreen
        listOf(
            Destination.OnboardingIntroScreen,
            Destination.HomeScreen,
            Destination.OnboardingIntroScreen
        ).addToBackstack()

        // Act
        navManager.popUpToAndNavigate(
            popToInclusive = Destination.OnboardingIntroScreen::class,
            destination = Destination.QrScannerScreen
        )

        // Assert
        // indexOfLast finds the LAST OnboardingIntroScreen, removes it (inclusive),
        // then pushes QrScannerScreen
        navManager.assertBackstack(
            Destination.StartScreen,
            Destination.OnboardingIntroScreen,
            Destination.HomeScreen,
            Destination.QrScannerScreen
        )
    }

    @Test
    fun `currentDestination reflects the top of the backstack after navigation`() = runTest {
        // Arrange
        assertEquals(Destination.StartScreen, navManager.currentDestination)

        // Act
        listOf(Destination.OnboardingIntroScreen, Destination.HomeScreen).addToBackstack()

        // Assert
        assertEquals(Destination.HomeScreen, navManager.currentDestination)

        // Act
        navManager.popBackStack()

        // Assert
        assertEquals(Destination.OnboardingIntroScreen, navManager.currentDestination)
    }

    @Test
    fun `currentDestination on empty stack never return null`() = runTest {
        // Arrange
        // An empty backstack means the app is closed.
        // Nevertheless, async code might try to get navManager.currentDestination.
        // To avoid an NPE, currentDestination never returns null but a fallback destination
        navManager.popBackStack()
        assertEquals(0, navManager.backstack.size)

        // Act
        val current = navManager.currentDestination

        // Assert
        // Should return a safe fallback (root) rather than throwing
        assertNotNull(current)
    }

    // Helper
    private fun List<Destination>.addToBackstack() = this.forEach { navManager.navigateTo(it) }
    private fun NavigationManager.assertBackstack(vararg expected: Destination) = assertEquals(expected.asList(), backstack)
}
