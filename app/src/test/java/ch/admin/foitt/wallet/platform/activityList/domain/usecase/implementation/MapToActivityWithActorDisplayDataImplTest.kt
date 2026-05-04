package ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation

import android.text.format.DateFormat
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityWithActorDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.MapToActivityWithActorDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.activityActorDisplay1
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.activityActorDisplay2
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.activityWithActorDisplays
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.locale
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.util.TimeZone

class MapToActivityWithActorDisplayDataImplTest {

    @MockK
    private lateinit var mockGetCurrentAppLocale: GetCurrentAppLocale

    @MockK
    private lateinit var mockGetLocalizedDisplay: GetLocalizedDisplay

    private lateinit var useCase: MapToActivityWithActorDisplayData

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = MapToActivityWithActorDisplayDataImpl(
            getCurrentAppLocale = mockGetCurrentAppLocale,
            getLocalizedDisplay = mockGetLocalizedDisplay,
        )

        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Europe/Zurich")))

        mockkStatic(DateFormat::class)
        every { DateFormat.getBestDateTimePattern(locale, "ddMMyyyy") } returns "dd.MM.yyyy"
        every { DateFormat.getBestDateTimePattern(locale, "HH:mm") } returns "HH:mm"

        coEvery { mockGetCurrentAppLocale() } returns locale
        coEvery {
            mockGetLocalizedDisplay(listOf(activityActorDisplay1, activityActorDisplay2))
        } returns activityActorDisplay1
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Mapping an activityWithDisplays to an ActivityActorData works correctly`() = runTest {
        val result = useCase(activityWithActorDisplays)

        val expected = ActivityWithActorDisplayData(
            activityId = activityWithActorDisplays.activity.id,
            activityType = activityWithActorDisplays.activity.type,
            localizedActorName = activityActorDisplay1.name,
            date = "07.10.2025 | 13:43",
        )

        assertEquals(expected, result)
    }
}
