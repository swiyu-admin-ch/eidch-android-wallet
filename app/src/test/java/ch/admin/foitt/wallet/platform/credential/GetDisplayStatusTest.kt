package ch.admin.foitt.wallet.platform.credential

import ch.admin.foitt.wallet.platform.credential.domain.model.getDisplayStatus
import ch.admin.foitt.wallet.platform.credential.domain.model.toDisplayStatus
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialDisplayStatus
import ch.admin.foitt.wallet.platform.database.domain.model.BundleItemEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.time.Instant

class GetDisplayStatusTest {

    @MockK
    lateinit var mockVerifiableCredential: VerifiableCredentialEntity

    @MockK
    lateinit var mockBundleItem: BundleItemEntity

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { mockBundleItem.status } returns CredentialStatus.VALID
        every { mockVerifiableCredential.validFrom } returns 0
        every { mockVerifiableCredential.validUntil } returns 17768026519L
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A valid credential returns a valid result`() {
        val status = mockVerifiableCredential.getDisplayStatus(mockBundleItem.status)
        assertEquals(CredentialDisplayStatus.Valid, status)
    }

    @Test
    fun `An expired validity returns an expired result`() {
        val validUntil = 1516183639L
        every { mockVerifiableCredential.validUntil } returns validUntil
        val expectedInstant = Instant.ofEpochSecond(validUntil)

        val status = mockVerifiableCredential.getDisplayStatus(mockBundleItem.status)
        assertTrue(status is CredentialDisplayStatus.Expired)
        val expectedStatus = status as CredentialDisplayStatus.Expired
        assertEquals(expectedInstant, expectedStatus.expiredAt)
    }

    @Test
    fun `A not-yet-valid validity returns a not-yet-valid result`() {
        // Sat Jan 17 2533 11:55:19 -> test will fail in ~500 years
        val validFrom = 17768026519L
        every { mockVerifiableCredential.validFrom } returns validFrom
        val expectedInstant = Instant.ofEpochSecond(validFrom)

        val status = mockVerifiableCredential.getDisplayStatus(mockBundleItem.status)
        assertTrue(status is CredentialDisplayStatus.NotYetValid)
        val expectedStatus = status as CredentialDisplayStatus.NotYetValid
        assertEquals(expectedInstant, expectedStatus.validFrom)
    }

    @Test
    fun `A not-yet-valid validity returns a revoked status if revoked`() {
        // Sat Jan 17 2533 11:55:19 -> test will fail in ~500 years
        val validFrom = 17768026519L
        every { mockVerifiableCredential.validFrom } returns validFrom
        every { mockBundleItem.status } returns CredentialStatus.REVOKED

        val status = mockVerifiableCredential.getDisplayStatus(mockBundleItem.status)
        assertEquals(CredentialDisplayStatus.Revoked, status)
    }

    @Test
    fun `A non-valid validity take precedence over a status`() {
        val validUntil = 1516183639L
        every { mockVerifiableCredential.validUntil } returns validUntil
        val expectedInstant = Instant.ofEpochSecond(validUntil)
        coEvery { mockBundleItem.status } returns CredentialStatus.REVOKED

        val status = mockVerifiableCredential.getDisplayStatus(mockBundleItem.status)
        assertTrue(status is CredentialDisplayStatus.Expired)
        val expectedStatus = status as CredentialDisplayStatus.Expired
        assertEquals(expectedInstant, expectedStatus.expiredAt)
    }

    @ParameterizedTest
    @EnumSource(
        value = CredentialStatus::class,
    )
    fun `Given a valid validity, the status is returned`(credentialStatus: CredentialStatus) {
        coEvery { mockBundleItem.status } returns credentialStatus
        val displayStatus = mockVerifiableCredential.getDisplayStatus(mockBundleItem.status)
        assertEquals(credentialStatus.toDisplayStatus(), displayStatus)
    }

    @Test
    fun `A business-expired credential returns BusinessExpired status`() {
        // Expired a second ago
        val status = mockVerifiableCredential.getDisplayStatus(mockBundleItem.status, Instant.now().minusSeconds(1))
        assertTrue(status is CredentialDisplayStatus.BusinessExpired)
    }

    @Test
    fun `A business-expired credential with a revoked status-list still returns BusinessExpired`() {
        every { mockBundleItem.status } returns CredentialStatus.REVOKED

        val status = mockVerifiableCredential.getDisplayStatus(mockBundleItem.status, Instant.now().minusSeconds(1))
        assertTrue(status is CredentialDisplayStatus.BusinessExpired)
    }

    @Test
    fun `A credential with businessExpiryDate in the future returns the status-list status`() {
        // Sat January first 2070, test will fail in ~45 years
        val status = mockVerifiableCredential.getDisplayStatus(mockBundleItem.status, Instant.parse("2070-01-01T00:00:00Z"))

        assertEquals(CredentialDisplayStatus.Valid, status)
    }

    @Test
    fun `A credential with null businessExpiryDate returns the status-list status`() {
        // Null is default mock setup
        val status = mockVerifiableCredential.getDisplayStatus(mockBundleItem.status)
        assertEquals(CredentialDisplayStatus.Valid, status)
    }

    @Test
    fun `JWT expiry takes priority over businessExpiryDate`() {
        // January 2018
        val validUntil = 1516183639L
        every { mockVerifiableCredential.validUntil } returns validUntil
        val status = mockVerifiableCredential.getDisplayStatus(mockBundleItem.status, Instant.now().minusSeconds(1))

        assertTrue(status is CredentialDisplayStatus.Expired)
    }
}
