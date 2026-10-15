package ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.SwissProfile
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.VersionEnforcement
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.GetVersionEnforcement
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetSwissProfileStatusImplTest {
    @MockK
    private lateinit var mockGetVersionEnforcement: GetVersionEnforcement

    private lateinit var useCase: GetSwissProfileStatusImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = GetSwissProfileStatusImpl(mockGetVersionEnforcement)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Returns CURRENT when there is no version enforcement data`() = runTest {
        coEvery { mockGetVersionEnforcement() } returns Ok(null)

        assertEquals(SwissProfile.Status.CURRENT, useCase("swiss-profile-vc:1"))
    }

    @Test
    fun `Returns CURRENT when there are no vc profiles`() = runTest {
        coEvery { mockGetVersionEnforcement() } returns Ok(versionEnforcement(emptyList()))

        assertEquals(SwissProfile.Status.CURRENT, useCase("swiss-profile-vc:1"))
    }

    @Test
    fun `Returns CURRENT when there is no vc profile of type verifiable credential`() = runTest {
        val profiles = listOf(profile(type = SwissProfile.ProfileType.ISSUANCE, version = "1"))
        coEvery { mockGetVersionEnforcement() } returns Ok(versionEnforcement(profiles))

        assertEquals(SwissProfile.Status.CURRENT, useCase("swiss-profile-vc:1"))
    }

    @Test
    fun `Returns the status of the matching version`() = runTest {
        val profiles = listOf(profile(status = SwissProfile.Status.DEPRECATED, version = "1"))
        coEvery { mockGetVersionEnforcement() } returns Ok(versionEnforcement(profiles))

        assertEquals(SwissProfile.Status.DEPRECATED, useCase("swiss-profile-vc:1"))
    }

    @Test
    fun `Returns null for an unknown version`() = runTest {
        val profiles = listOf(profile(status = SwissProfile.Status.CURRENT, version = "1"))
        coEvery { mockGetVersionEnforcement() } returns Ok(versionEnforcement(profiles))

        assertNull(useCase("swiss-profile-vc:99"))
    }

    @Test
    fun `Returns null when the version has no swiss-profile-vc prefix`() = runTest {
        val profiles = listOf(profile(status = SwissProfile.Status.CURRENT, version = "1"))
        coEvery { mockGetVersionEnforcement() } returns Ok(versionEnforcement(profiles))

        assertNull(useCase("1"))
    }

    @Test
    fun `Returns null when the profile version is null`() = runTest {
        val profiles = listOf(profile(status = SwissProfile.Status.CURRENT, version = "1"))
        coEvery { mockGetVersionEnforcement() } returns Ok(versionEnforcement(profiles))

        assertNull(useCase(null))
    }

    @Test
    fun `Returns CURRENT when the swiss profile lifecycle is missing`() = runTest {
        val enforcement = mockk<VersionEnforcement> { every { swissProfileLifecycle } returns null }
        coEvery { mockGetVersionEnforcement() } returns Ok(enforcement)

        assertEquals(SwissProfile.Status.CURRENT, useCase("swiss-profile-vc:1"))
    }

    private fun versionEnforcement(vc: List<SwissProfile>): VersionEnforcement = mockk {
        every { swissProfileLifecycle } returns VersionEnforcement.SwissProfileLifecycle(vc = vc)
    }

    private fun profile(
        type: SwissProfile.ProfileType = SwissProfile.ProfileType.VERIFIABLE_CREDENTIAL,
        status: SwissProfile.Status = SwissProfile.Status.CURRENT,
        version: String,
    ) = SwissProfile(type = type, status = status, version = version)
}
