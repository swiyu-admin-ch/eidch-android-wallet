package ch.admin.foitt.wallet.platform.cameraPermissionHandler

import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.model.PermissionResult
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.model.PermissionState
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.usecase.CheckCameraPermission
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.usecase.implementation.CheckCameraPermissionImpl
import ch.admin.foitt.wallet.util.getFlagLists
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class CheckCameraPermissionImplTest {

    private lateinit var checkCameraPermissionUseCase: CheckCameraPermission

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        checkCameraPermissionUseCase = CheckCameraPermissionImpl()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Given a fresh state, a permission check should return the intro state`() = runTest {
        val result = checkCameraPermissionUseCase(
            permissionsAreGranted = false,
            rationaleShouldBeShown = false,
            introPromptWasAccepted = false,
            autoPromptWasTriggered = false,
            manualPromptWasTriggered = false,
            rationaleWasShown = false,
        )

        assertEquals(PermissionState.Intro, result)
    }

    @Test
    fun `Given the intro was shown, a denied permission check without rationale should return the auto trigger state`() = runTest {
        val result = checkCameraPermissionUseCase(
            permissionsAreGranted = false,
            rationaleShouldBeShown = false,
            introPromptWasAccepted = true,
            autoPromptWasTriggered = false,
            manualPromptWasTriggered = false,
            rationaleWasShown = false,
        )
        assertEquals(PermissionState.AutoPrompt, result)
    }

    @Test
    fun `Given the auto prompt was triggered, a denied permission check without rationale should return the manual prompt state`() = runTest {
        val result = checkCameraPermissionUseCase(
            permissionsAreGranted = false,
            rationaleShouldBeShown = false,
            introPromptWasAccepted = true,
            autoPromptWasTriggered = true,
            manualPromptWasTriggered = false,
            rationaleWasShown = false,
        )
        assertEquals(PermissionState.ManualPrompt, result)
    }

    @TestFactory
    fun `When permissions are denied, a failed manual prompt without rationale should always lead to the blocked state`(): List<DynamicTest> {
        val expected = PermissionState.Blocked
        return getAllBlockedPermissionsStates().map { currentPermissionState ->
            DynamicTest.dynamicTest("$currentPermissionState should return $expected") {
                runTest {
                    val actual = checkCameraPermissionUseCase(
                        permissionsAreGranted = currentPermissionState.permissionsAreGranted,
                        rationaleShouldBeShown = currentPermissionState.rationaleShouldBeShown,
                        introPromptWasAccepted = currentPermissionState.introPromptWasAccepted,
                        autoPromptWasTriggered = currentPermissionState.autoPromptWasTriggered,
                        manualPromptWasTriggered = currentPermissionState.manualPromptWasTriggered,
                        rationaleWasShown = currentPermissionState.rationaleWasShown,
                    )
                    assertEquals(expected, actual)
                }
            }
        }
    }

    @TestFactory
    fun `When permissions are denied, a rationale flag should always lead to the rationale state`(): List<DynamicTest> {
        val expected = PermissionState.Rationale
        return getAllRationalePermissionsStates().map { currentPermissionState ->
            DynamicTest.dynamicTest("$currentPermissionState should return $expected") {
                runTest {
                    val actual = checkCameraPermissionUseCase(
                        permissionsAreGranted = currentPermissionState.permissionsAreGranted,
                        rationaleShouldBeShown = currentPermissionState.rationaleShouldBeShown,
                        introPromptWasAccepted = currentPermissionState.introPromptWasAccepted,
                        autoPromptWasTriggered = currentPermissionState.autoPromptWasTriggered,
                        manualPromptWasTriggered = currentPermissionState.manualPromptWasTriggered,
                        rationaleWasShown = currentPermissionState.rationaleWasShown,
                    )
                    assertEquals(expected, actual)
                }
            }
        }
    }

    @TestFactory
    fun `When permissions are granted the usecase should always return PermissionState Granted`(): List<DynamicTest> {
        val expected = PermissionState.Granted
        return getAllGrantedPermissionsStates().map { currentPermissionState ->
            DynamicTest.dynamicTest("$currentPermissionState should return $expected") {
                runTest {
                    val actual = checkCameraPermissionUseCase(
                        permissionsAreGranted = currentPermissionState.permissionsAreGranted,
                        rationaleShouldBeShown = currentPermissionState.rationaleShouldBeShown,
                        autoPromptWasTriggered = currentPermissionState.autoPromptWasTriggered,
                        introPromptWasAccepted = currentPermissionState.introPromptWasAccepted,
                        manualPromptWasTriggered = currentPermissionState.manualPromptWasTriggered,
                        rationaleWasShown = currentPermissionState.rationaleWasShown,
                    )
                    assertEquals(expected, actual)
                }
            }
        }
    }

    private fun getAllGrantedPermissionsStates() = getFlagLists(0, 31).map { flags ->
        PermissionResult(
            permissionsAreGranted = true,
            introPromptWasAccepted = flags.getOrElse(0) { false },
            rationaleShouldBeShown = flags.getOrElse(1) { false },
            autoPromptWasTriggered = flags.getOrElse(2) { false },
            manualPromptWasTriggered = flags.getOrElse(3) { false },
            rationaleWasShown = flags.getOrElse(4) { false },
        )
    }

    private fun getAllRationalePermissionsStates() = getFlagLists(0, 15).map { flags ->
        PermissionResult(
            permissionsAreGranted = false,
            introPromptWasAccepted = flags.getOrElse(0) { false },
            rationaleShouldBeShown = true,
            autoPromptWasTriggered = flags.getOrElse(1) { false },
            manualPromptWasTriggered = flags.getOrElse(2) { false },
            rationaleWasShown = flags.getOrElse(3) { false },
        )
    }

    private fun getAllBlockedPermissionsStates() = getFlagLists(0, 7).map { flags ->
        PermissionResult(
            permissionsAreGranted = false,
            introPromptWasAccepted = flags.getOrElse(0) { false },
            rationaleShouldBeShown = false,
            autoPromptWasTriggered = flags.getOrElse(1) { false },
            manualPromptWasTriggered = true,
            rationaleWasShown = flags.getOrElse(2) { false },
        )
    }
}
