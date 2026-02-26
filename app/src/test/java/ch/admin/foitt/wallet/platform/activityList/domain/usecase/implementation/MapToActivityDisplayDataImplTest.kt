package ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation

import android.text.format.DateFormat
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.MapToActivityActorDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.MapToActivityDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.NON_COMPLIANCE_DATA
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.activity
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.activityActorDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.activityWithActorDisplays
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.actorDisplay1
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.imageData1
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.locale
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
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

class MapToActivityDisplayDataImplTest {

    @MockK
    private lateinit var mockGetCurrentAppLocale: GetCurrentAppLocale

    @MockK
    private lateinit var mockMapToActivityActorDisplayData: MapToActivityActorDisplayData

    private lateinit var useCase: MapToActivityDisplayData

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = MapToActivityDisplayDataImpl(
            getCurrentAppLocale = mockGetCurrentAppLocale,
            mapToActivityActorDisplayData = mockMapToActivityActorDisplayData,
        )

        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Europe/Zurich")))

        mockkStatic(DateFormat::class)
        every { DateFormat.getBestDateTimePattern(locale, "ddMMyyyy") } returns "dd.MM.yyyy"
        every { DateFormat.getBestDateTimePattern(locale, "HH:mm") } returns "HH:mm"

        coEvery { mockGetCurrentAppLocale() } returns locale
        coEvery {
            mockMapToActivityActorDisplayData(
                activityId = activity.id,
                actorDisplaysWithImages = activityWithActorDisplays.actorDisplays
            )
        } returns activityActorDisplayData
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Mapping an activityWithDisplays to an ActivityActorData works correctly`() = runTest {
        val result = useCase(activityWithActorDisplays)

        val expected = ActivityDisplayData(
            id = activity.id,
            activityType = activity.type,
            date = "07.10.2025 | 13:43",
            nonComplianceData = NON_COMPLIANCE_DATA,
            localizedActorName = actorDisplay1.actorDisplay.name,
            actorImageData = imageData1,
        )

        assertEquals(expected, result)
    }
}
