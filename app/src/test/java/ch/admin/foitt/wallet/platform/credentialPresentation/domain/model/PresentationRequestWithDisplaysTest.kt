package ch.admin.foitt.wallet.platform.credentialPresentation.domain.model

import ch.admin.foitt.wallet.platform.credentialPresentation.mock.MockPresentationRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PresentationRequestWithDisplaysTest {

    @Test
    fun `Mapping from ClientNameRequest to ClientNameDisplay is correct`() {
        val mockPresentationRequest = MockPresentationRequest.authorizationRequestWithDisplays
        val result = ClientNameDisplay.fromClientName(mockPresentationRequest.clientMetaData?.clientNameList ?: emptyList())

        assertEquals(result.size, mockPresentationRequest.clientMetaData?.clientNameList?.size)
        assertEquals(
            result.first { it.clientName == "firstClientName" }.clientName,
            mockPresentationRequest.clientMetaData?.clientNameList?.first { it.clientName == "firstClientName" }?.clientName
        )
        assertEquals(
            result.first { it.locale == "fallback" }.clientName,
            mockPresentationRequest.clientMetaData?.clientNameList?.first { it.locale == "fallback" }?.clientName
        )
    }

    @Test
    fun `Mapping from PresentationRequest with LogoUri to LogoUriDisplay is correct`() {
        val mockPresentationRequest = MockPresentationRequest.authorizationRequestWithDisplays
        val result = LogoUriDisplay.fromLogoUri(mockPresentationRequest.clientMetaData?.logoUriList ?: emptyList())

        assertEquals(result.size, mockPresentationRequest.clientMetaData?.logoUriList?.size)
        assertEquals(
            result.first { it.logoUri == "firstLogoUri" }.logoUri,
            mockPresentationRequest.clientMetaData?.logoUriList?.first { it.logoUri == "firstLogoUri" }?.logoUri
        )
        assertEquals(
            result.first { it.locale == "fallback" }.logoUri,
            mockPresentationRequest.clientMetaData?.logoUriList?.first { it.locale == "fallback" }?.logoUri
        )
    }
}
