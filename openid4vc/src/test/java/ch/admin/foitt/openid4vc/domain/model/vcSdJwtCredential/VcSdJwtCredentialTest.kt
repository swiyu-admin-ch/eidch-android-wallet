package ch.admin.foitt.openid4vc.domain.model.vcSdJwtCredential

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.mock.VcSdJwtMocks
import ch.admin.foitt.openid4vc.util.SafeJsonTestInstance
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VcSdJwtCredentialTest {

    @Test
    fun `getClaimsJson returns only the disclosable claims as json`() = runTest {
        val vcSdJwtCredential = createVcSdJwtCredential(VcSdJwtMocks.VC_SD_JWT_FULL_SAMPLE)

        assertEquals(
            SafeJsonTestInstance.json.parseToJsonElement(VcSdJwtMocks.VC_SD_JWT_FULL_SAMPLE_JSON),
            vcSdJwtCredential.getClaimsToSave(),
        )
    }

    @Test
    fun `getClaimsForPresentation returns json containing technical and non-technical claims`() = runTest {
        val vcSdJwtCredential = createVcSdJwtCredential(VcSdJwtMocks.VC_SD_JWT_FULL_SAMPLE)

        assertEquals(
            SafeJsonTestInstance.json.parseToJsonElement(VcSdJwtMocks.VC_SD_JWT_FULL_SAMPLE_PLUS_TECHNICAL_CLAIMS_JSON),
            vcSdJwtCredential.getClaimsForPresentation()
        )
    }

    private fun createVcSdJwtCredential(payload: String) = VcSdJwtCredential(
        keyBinding = null,
        payload = payload,
    )
}
