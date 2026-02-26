package ch.admin.foitt.openid4vc.domain.model.presentationRequest

import ch.admin.foitt.openid4vc.domain.model.mock.ClientMetaDataMock
import ch.admin.foitt.openid4vc.domain.model.mock.ClientMetaDataMock.clientMetadataWithNewFieldsString
import ch.admin.foitt.openid4vc.domain.model.mock.ClientMetaDataMock.clientMetadataWithNewFieldsString2
import ch.admin.foitt.openid4vc.domain.model.mock.ClientMetaDataMock.clientMetadataWithNewFieldsString3
import ch.admin.foitt.openid4vc.util.SafeJsonTestInstance
import ch.admin.foitt.openid4vc.util.assertOk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ClientMetaDataSerializerTest {

    private val safeJson = SafeJsonTestInstance.safeJson
    private val json = SafeJsonTestInstance.json

    private val fallbackClientJson = """"client_name":"My Example""""
    private val fallbackUriJson = """"logo_uri":"someURI""""
    private val clientJson = """"client_name#ja-Jpan-JP":"クライアント名","client_name#fr":"Mon example","client_name#de-ch":"mein Beispiel""""
    private val uriJson = """"logo_uri#en":"firstLogoUri","logo_uri#de":"secondLogoUri""""
    private val payload = "{$clientJson,$fallbackClientJson,$fallbackUriJson}"
    private val payloadWithUri = "{$clientJson,$fallbackClientJson,$uriJson,$fallbackUriJson}"
    private val payloadOnlyWithFallbacks = "{$fallbackClientJson,$fallbackUriJson}"

    @Test
    fun `decode a json string to a ClientMetaData correctly`() = runTest {
        val result = safeJson.safeDecodeStringTo<ClientMetaData>(payload).assertOk()

        assertEquals(result.clientNameList.size, 4)
        assertEquals(result.logoUriList.size, 1)
        assertEquals(result.clientNameList[3].clientName, "My Example")
        assertEquals(result.clientNameList[3].locale, "fallback")
        assertEquals(result.logoUriList[0].logoUri, "someURI")
        assertEquals(result.logoUriList[0].locale, "fallback")
    }

    @Test
    fun `decode a json string only with fallback elements to a ClientMetaData correctly`() = runTest {
        val result = safeJson.safeDecodeStringTo<ClientMetaData>(payloadOnlyWithFallbacks).assertOk()

        assertEquals(result.clientNameList.size, 1)
        assertEquals(result.logoUriList.size, 1)
        assertEquals("My Example", result.clientNameList[0].clientName)
        assertEquals("fallback", result.clientNameList[0].locale)
        assertEquals("someURI", result.logoUriList[0].logoUri)
        assertEquals("fallback", result.logoUriList[0].locale)
    }

    @Test
    fun `decode a json string with uri elements to a ClientMetaData correctly`() = runTest {
        val result = safeJson.safeDecodeStringTo<ClientMetaData>(payloadWithUri).assertOk()

        assertEquals(result.clientNameList.size, 4)
        assertEquals(result.logoUriList.size, 3)
        assertEquals(result.clientNameList[3].clientName, "My Example")
        assertEquals(result.clientNameList[3].locale, "fallback")
        assertEquals(result.logoUriList[0].logoUri, "firstLogoUri")
        assertEquals(result.logoUriList[0].locale, "en")
    }

    @Test
    fun `Client metadata with new fields is correctly decoded`() = runTest {
        val result = safeJson.safeDecodeStringTo<ClientMetaData>(clientMetadataWithNewFieldsString).assertOk()

        assertEquals("A128GCM", result.encryptedResponseEncValuesSupported?.first())
    }

    @Test
    fun `Client metadata with new fields 2 is correctly decoded`() = runTest {
        val result = safeJson.safeDecodeStringTo<ClientMetaData>(clientMetadataWithNewFieldsString2).assertOk()

        assertEquals(null, result.jwks)
        assertEquals(null, result.encryptedResponseEncValuesSupported)
    }

    @Test
    fun `Client metadata with new fields 3 is correctly decoded`() = runTest {
        val result = safeJson.safeDecodeStringTo<ClientMetaData>(clientMetadataWithNewFieldsString3).assertOk()

        assertEquals(null, result.jwks)
        assertEquals(null, result.encryptedResponseEncValuesSupported)
    }

    @Test
    fun `encode a ClientMetaData to a json string correctly`() = runTest {
        val mockClientMetaData = ClientMetaDataMock.clientMetaData

        val result = json.encodeToString(ClientMetaData.serializer(), mockClientMetaData)

        assertEquals(payload, result)
    }

    @Test
    fun `encode a ClientMetaData with logoUri to a json string correctly`() = runTest {
        val mockClientMetaData = ClientMetaDataMock.clientMetaDataWithUri

        val result = json.encodeToString(ClientMetaData.serializer(), mockClientMetaData)

        assertEquals(payloadWithUri, result)
    }
}
