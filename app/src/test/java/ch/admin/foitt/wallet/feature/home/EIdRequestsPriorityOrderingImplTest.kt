package ch.admin.foitt.wallet.feature.home

import ch.admin.foitt.wallet.feature.home.domain.usecase.implementation.EIdRequestsPriorityOrderingImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SIdRequestDisplayData
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SIdRequestDisplayStatus
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.priority
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class EIdRequestsPriorityOrderingImplTest {

    private lateinit var useCase: EIdRequestsPriorityOrderingImpl

    @BeforeEach
    fun setUp() {
        useCase = EIdRequestsPriorityOrderingImpl()
    }

    @Test
    fun `sorting should prioritize status and then descending createdAt`() = runTest {
        val request1 = SIdRequestDisplayData(
            caseId = "4",
            status = SIdRequestDisplayStatus.AV_READY,
            firstName = "FirstNameReady",
            lastName = "LastName",
            createdAt = 4L
        )
        val request2 = SIdRequestDisplayData(
            caseId = "6",
            status = SIdRequestDisplayStatus.AV_READY,
            firstName = "FirstNameReady2",
            lastName = "LastName",
            createdAt = 6L
        )
        val request3 = SIdRequestDisplayData(
            caseId = "10",
            status = SIdRequestDisplayStatus.UNKNOWN,
            firstName = "FirstNameUnknown",
            lastName = "LastName",
            createdAt = 10L
        )
        val request4 = SIdRequestDisplayData(
            caseId = "20",
            status = SIdRequestDisplayStatus.IN_AUTO_VERIFICATION,
            firstName = "FirstNameAutoVerification",
            lastName = "LastName",
            createdAt = 20L
        )

        val input = listOf(request1, request2, request3, request4)

        val result = useCase(input)

        // Expected order:
        // 1. IN_AUTO_VERIFICATION (priority 1) -> createdAt 20
        // 2. AV_READY (priority 4) -> createdAt 6 (descending createdAt)
        // 3. AV_READY (priority 4) -> createdAt 4 (descending createdAt)
        // 4. UNKNOWN (priority 13) -> createdAt 10

        assertEquals(request4, result[0])
        assertEquals(request2, result[1])
        assertEquals(request1, result[2])
        assertEquals(request3, result[3])
    }

    @Test
    fun `sorting all statuses should follow their defined priority`() = runTest {
        val allStatuses = SIdRequestDisplayStatus.entries
        val input = allStatuses.shuffled().mapIndexed { index, status ->
            SIdRequestDisplayData(
                caseId = index.toString(),
                status = status,
                firstName = "FirstName",
                lastName = "LastName",
                createdAt = Instant.now().epochSecond
            )
        }

        val result = useCase(input)

        val sortedStatuses = result.map { it.status }
        val expectedStatuses = allStatuses.sortedBy { it.priority }

        assertEquals(expectedStatuses, sortedStatuses)
    }
}
