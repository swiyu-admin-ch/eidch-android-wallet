package ch.admin.foitt.openid4vc.domain.model.mock

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientMetaData
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientName
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.LogoUri

object ClientMetaDataMock {
    val clientMetaData = ClientMetaData(
        clientNameList = listOf(
            ClientName(
                clientName = "クライアント名",
                locale = "ja-Jpan-JP"
            ),
            ClientName(
                clientName = "Mon example",
                locale = "fr"
            ),
            ClientName(
                clientName = "mein Beispiel",
                locale = "de-ch"
            ),
            ClientName(
                clientName = "My Example",
                locale = "fallback"
            )
        ),
        logoUriList = listOf(
            LogoUri(
                logoUri = "someURI",
                locale = "fallback"
            )
        ),
        jwks = null,
        encryptedResponseEncValuesSupported = null,
    )

    val clientMetaDataWithUri = clientMetaData.copy(
        logoUriList = listOf(
            LogoUri(
                logoUri = "firstLogoUri",
                locale = "en"
            ),
            LogoUri(
                logoUri = "secondLogoUri",
                locale = "de"
            ),
            LogoUri(
                logoUri = "someURI",
                locale = "fallback"
            )
        )
    )

    val clientMetadataWithNewFieldsString = """
        {
              "jwks":{
                "keys": [
                    {
                        "x": "x value",
                        "y": "y value",
                        "crv": "P-256",
                        "kty": "EC"
                    }
                ]
              },
              "encrypted_response_enc_values_supported": [
                "A128GCM", "A256GCM"
              ],
              "client_name":"My Example",
              "logo_uri":"someURI"
           }
    """.trimIndent()

    val clientMetadataWithNewFieldsString2 = """
        {
              "client_name":"My Example",
              "logo_uri":"someURI"
           }
    """.trimIndent()

    val clientMetadataWithNewFieldsString3 = """
        {
              "jwks":null,
              "encrypted_response_enc_values_supported": null,
              "client_name":"My Example",
              "logo_uri":"someURI"
           }
    """.trimIndent()
}
