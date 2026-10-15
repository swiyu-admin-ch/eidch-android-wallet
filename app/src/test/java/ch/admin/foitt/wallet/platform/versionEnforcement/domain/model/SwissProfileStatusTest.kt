package ch.admin.foitt.wallet.platform.versionEnforcement.domain.model

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SwissProfileStatusTest {

    @Test
    fun `Freshness ranks the statuses from current (newest) to end of life (oldest)`() {
        assertTrue(SwissProfile.Status.CURRENT.freshness > SwissProfile.Status.DEPRECATED.freshness)
        assertTrue(SwissProfile.Status.DEPRECATED.freshness > SwissProfile.Status.SUNSET.freshness)
        assertTrue(SwissProfile.Status.SUNSET.freshness > SwissProfile.Status.END_OF_LIFE.freshness)
    }
}
