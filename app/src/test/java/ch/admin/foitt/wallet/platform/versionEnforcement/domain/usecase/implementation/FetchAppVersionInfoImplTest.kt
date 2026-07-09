package ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.utils.AppVersion
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.AppVersionInfo
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.EnforcementType
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.VersionEnforcement
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.VersionEnforcementError
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.repository.VersionEnforcementRepository
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.GetAppVersion
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.GetDeviceModel
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.GetOSVersion
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.LocalDate

class FetchAppVersionInfoImplTest {

    @MockK
    private lateinit var mockGetAppVersion: GetAppVersion

    @MockK
    private lateinit var mockGetOSVersion: GetOSVersion

    @MockK
    private lateinit var mockGetDeviceModel: GetDeviceModel

    @MockK
    private lateinit var mockVersionEnforcementRepository: VersionEnforcementRepository

    @MockK
    private lateinit var mockGetLocalizedDisplay: GetLocalizedDisplay

    private lateinit var useCase: FetchAppVersionInfoImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = FetchAppVersionInfoImpl(
            getAppVersion = mockGetAppVersion,
            versionEnforcementRepository = mockVersionEnforcementRepository,
            getLocalizedDisplay = mockGetLocalizedDisplay,
            getOsVersion = mockGetOSVersion,
            getDeviceModel = mockGetDeviceModel,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Fetching app version info where OS version is too low returns blocked`() = runTest {
        every { mockGetOSVersion() } returns "14"

        coEvery { mockVersionEnforcementRepository.fetchVersionEnforcement() } returns Ok(
            defaultVersionEnforcement
        )

        val info = useCase()

        assertTrue(info is AppVersionInfo.Blocked)
        info as AppVersionInfo.Blocked
        assertEquals(EnforcementType.OS_UPDATE, info.type)
    }

    @Test
    fun `Fetching app version info where device is blacklisted returns blocked`() = runTest {
        coEvery { mockVersionEnforcementRepository.fetchVersionEnforcement() } returns Ok(
            defaultVersionEnforcement.copy(defaultBlacklist = listOf(DEVICE_MODEL))
        )

        val info = useCase()

        assertTrue(info is AppVersionInfo.Blocked)
        info as AppVersionInfo.Blocked
        assertEquals(EnforcementType.DEVICE_BLACKLIST, info.type)
    }

    @Test
    fun `Fetching app version info where version is lower than a forced version returns blocked`() = runTest {
        mockVersions(
            appVersion = appVersion,
            forcedVersion = forcedVersion,
        )

        val info = useCase()

        assertTrue(info is AppVersionInfo.Blocked)
        info as AppVersionInfo.Blocked
        assertEquals(TITLE, info.title)
        assertEquals(TEXT, info.text)
        assertEquals(EnforcementType.APP_BLOCKED, info.type)
    }

    @Test
    fun `Fetching app version info where a newer optional version exists returns update suggested`() = runTest {
        val suggestedVersion = VersionEnforcement.Versions(
            message = listOf(display),
            releaseDate = LocalDate.now(),
            supportUntil = LocalDate.now().plusDays(365),
            updateType = "optional",
            version = forcedVersion
        )

        coEvery { mockVersionEnforcementRepository.fetchVersionEnforcement() } returns Ok(
            defaultVersionEnforcement.copy(versions = listOf(suggestedVersion))
        )

        val info = useCase()

        assertTrue(info is AppVersionInfo.Blocked)
        info as AppVersionInfo.Blocked
        assertEquals(EnforcementType.UPDATE_SUGGESTED, info.type)
    }

    @ParameterizedTest
    @ValueSource(strings = [APP_VERSION, FORCED_APP_VERSION])
    fun `Fetching app version info where version is equal or higher than forced version returns valid`(
        version: String
    ) = runTest {
        mockVersions(
            appVersion = AppVersion(version),
            forcedVersion = appVersion,
        )

        val info = useCase()

        assertTrue(info is AppVersionInfo.Valid)
    }

    @Test
    fun `Fetching app version info where version is blocked and no localized display is found returns blocked`() = runTest {
        mockVersions(
            appVersion = appVersion,
            forcedVersion = forcedVersion,
        )
        every { mockGetLocalizedDisplay.invoke<VersionEnforcement.Display>(any()) } returns null

        val info = useCase()

        assertTrue(info is AppVersionInfo.Blocked)
        info as AppVersionInfo.Blocked
        assertEquals(null, info.title)
        assertEquals(null, info.text)
    }

    @Test
    fun `Fetching app version info where no info is available returns valid`() = runTest {
        coEvery { mockVersionEnforcementRepository.fetchVersionEnforcement() } returns Ok(null)

        val info = useCase()

        assertTrue(info is AppVersionInfo.Valid)
    }

    @Test
    fun `Fetching app version info where repository has an error returns unknown`() = runTest {
        coEvery {
            mockVersionEnforcementRepository.fetchVersionEnforcement()
        } returns Err(VersionEnforcementError.Unexpected(null))

        val info = useCase()

        assertTrue(info is AppVersionInfo.Unknown)
    }

    private fun setupDefaultMocks() {
        every { mockGetAppVersion() } returns appVersion
        every { mockGetOSVersion() } returns CURRENT_OS_VERSION
        every { mockGetDeviceModel() } returns DEVICE_MODEL

        every { mockGetLocalizedDisplay.invoke<VersionEnforcement.Display>(any()) } returns display

        coEvery {
            mockVersionEnforcementRepository.fetchVersionEnforcement()
        } returns Ok(defaultVersionEnforcement)
    }

    private fun mockVersions(
        appVersion: AppVersion,
        forcedVersion: AppVersion? = null,
    ) {
        every { mockGetAppVersion() } returns appVersion

        val versions = forcedVersion?.let {
            listOf(
                VersionEnforcement.Versions(
                    message = listOf(display),
                    releaseDate = LocalDate.now(),
                    supportUntil = LocalDate.now().plusDays(30),
                    updateType = "forced",
                    version = it
                )
            )
        } ?: defaultVersionEnforcement.versions

        coEvery {
            mockVersionEnforcementRepository.fetchVersionEnforcement()
        } returns Ok(defaultVersionEnforcement.copy(versions = versions))
    }

    private companion object {
        const val TITLE = "title"
        const val TEXT = "text"
        const val APP_VERSION = "1.1.0"
        const val FORCED_APP_VERSION = "1.2.0"
        const val CURRENT_OS_VERSION = "16"
        const val DEVICE_MODEL = "Pixel 9"

        val appVersion = AppVersion(APP_VERSION)
        val forcedVersion = AppVersion(FORCED_APP_VERSION)

        val display = VersionEnforcement.Display(
            title = TITLE,
            text = TEXT,
            locale = "en"
        )

        val defaultVersionEnforcement = VersionEnforcement(
            appId = "ch.admin.foitt.wallet",
            displays = listOf(display),
            lifetime = 30,
            defaultBlacklist = emptyList(),
            minOSVersion = AppVersion("15"),
            platform = VersionEnforcement.Platform.ANDROID,
            storeUrl = "https://play.google.com/store/apps/details?id=ch.admin.foitt.wallet",
            versions = listOf(
                VersionEnforcement.Versions(
                    message = listOf(display),
                    releaseDate = LocalDate.now().minusDays(10),
                    supportUntil = LocalDate.now().plusDays(365),
                    updateType = "optional",
                    version = appVersion
                )
            )
        )
    }
}
