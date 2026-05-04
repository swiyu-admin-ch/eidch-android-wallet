package ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation

import android.text.format.DateFormat
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityDetailDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.MapToActivityDetailDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.activityActorDisplay1
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.activityActorDisplay2
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.activityWithDetails
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.imageData1
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.locale
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.nonComplianceReasonDisplay1
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.nonComplianceReasonDisplay2
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

class MapToActivityDetailDisplayDataImplTest {

    @MockK
    private lateinit var mockGetCurrentAppLocale: GetCurrentAppLocale

    @MockK
    private lateinit var mockGetLocalizedDisplay: GetLocalizedDisplay

    private lateinit var useCase: MapToActivityDetailDisplayData

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = MapToActivityDetailDisplayDataImpl(
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
        coEvery {
            mockGetLocalizedDisplay(listOf(nonComplianceReasonDisplay1, nonComplianceReasonDisplay2))
        } returns nonComplianceReasonDisplay1
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Mapping an activityWithDisplays to an ActivityActorData works correctly`() = runTest {
        val result = useCase(activityWithDetails)

        val expected = ActivityDetailDisplayData(
            activityId = activityWithDetails.activity.id,
            activityType = activityWithDetails.activity.type,
            date = "07.10.2025 | 13:43",
            localizedActorName = activityActorDisplay1.name,
            actorTrustStatus = activityWithDetails.activity.actorTrust,
            vcSchemaTrustStatus = activityWithDetails.activity.vcSchemaTrust,
            actorComplianceState = activityWithDetails.activity.actorCompliance,
            localizedNonComplianceReason = nonComplianceReasonDisplay1.reason,
            actorImageData = imageData1
        )

        assertEquals(expected, result)
    }
}
