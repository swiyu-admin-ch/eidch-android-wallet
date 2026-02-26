package ch.admin.foitt.wallet.feature.sessionTimeout

import ch.admin.foitt.wallet.feature.sessionTimeout.domain.SessionTimeoutNavigation
import ch.admin.foitt.wallet.feature.sessionTimeout.domain.implementation.SessionTimeoutNavigationImpl
import ch.admin.foitt.wallet.platform.login.domain.usecase.NavigateToLogin
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertNull

class SessionTimeoutNavigationTest {

    @MockK
    private lateinit var mockNavManager: NavigationManager

    @MockK
    private lateinit var mockNavigateToLogin: NavigateToLogin

    private lateinit var sessionTimeoutNavigation: SessionTimeoutNavigation

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        sessionTimeoutNavigation = SessionTimeoutNavigationImpl(
            mockNavManager,
            mockNavigateToLogin,
        )

        coEvery { mockNavManager.navigateTo(any()) } just Runs
    }

    @TestFactory
    fun `Blacklisted destinations stay on same screen when the session timeouts`(): List<DynamicTest> {
        val noAutoLogoutScreenDestinationExamples = listOf(
            Destination.OnboardingIntroScreen,
            Destination.PassphraseLoginScreen(biometricsLocked = false),
        )
        return noAutoLogoutScreenDestinationExamples.map { destination ->
            DynamicTest.dynamicTest("$destination should not trigger auto-logout") {
                runTest {
                    coEvery { mockNavManager.currentDestination } returns destination
                    assertNull(sessionTimeoutNavigation())
                }
            }
        }
    }

    @Test
    fun `Non-blacklisted destinations navigate to the login screen when the session timeouts`() = runTest {
        val navToLoginReturn = Destination.BiometricLoginScreen

        coEvery { mockNavManager.currentDestination } returns Destination.HomeScreen
        coEvery { mockNavigateToLogin() } returns navToLoginReturn

        val result = sessionTimeoutNavigation()

        assertEquals(navToLoginReturn, result)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }
}
