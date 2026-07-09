package ch.admin.foitt.openid4vc.domain.model.vcSdJwtCredential

import ch.admin.foitt.openid4vc.domain.model.anycredential.Validity
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.mock.VcSdJwtMocks
import ch.admin.foitt.openid4vc.util.SafeJsonTestInstance
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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

    @Test
    fun `validity is BusinessExpired when businessExpiryDate disclosure is in the past`() {
        val credential = createVcSdJwtCredential(VcSdJwtMocks.VC_SD_JWT_WITH_PAST_EXPIRY_DATE_DISCLOSURE)
        assertTrue(credential.validity is Validity.BusinessExpired)
    }

    @Test
    fun `validity is Valid when businessExpiryDate disclosure is in the future and JWT validity is fine`() {
        val credential = createVcSdJwtCredential(VcSdJwtMocks.VC_SD_JWT_WITH_FUTURE_EXPIRY_DATE_DISCLOSURE)
        assertTrue(credential.validity is Validity.Valid)
    }

    @Test
    fun `validity is Expired when businessExpiryDate disclosure is valid but exp is not`() {
        val credential = createVcSdJwtCredential(VcSdJwtMocks.VC_SD_JWT_WITH_EXPIRED_DATE_BUT_VALID_BUSINESS_EXPIRY)
        assertTrue(credential.validity is Validity.Expired)
    }

    @Test
    fun `validity is Valid when businessExpiryDate is null or unparsable but exp dates are valid`() {
        val credential =
            createVcSdJwtCredential(VcSdJwtMocks.VC_SD_JWT_WITH_VALID_EXPIRED_DATE_BUT_UNPARSABLE_BUSINESS_EXPIRY)
        assertTrue(credential.validity is Validity.Valid)
    }

    private fun createVcSdJwtCredential(payload: String) = VcSdJwtCredential(
        keyBinding = null,
        payload = payload,
    )
}
