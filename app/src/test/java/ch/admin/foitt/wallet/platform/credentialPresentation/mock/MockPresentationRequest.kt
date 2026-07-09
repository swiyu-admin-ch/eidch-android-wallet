package ch.admin.foitt.wallet.platform.credentialPresentation.mock

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientMetaData
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientName
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.LogoUri
import uniffi.heidi_dcql_rust.CredentialQuery
import uniffi.heidi_dcql_rust.DcqlQuery

object MockPresentationRequest {

    const val CLIENT_ID = "decentralized_identifier:did:example:12345"
    const val VALID_JWT =
        "eyJraWQiOiJkaWQ6ZXhhbXBsZToxMjM0NSNrZXktMDEiLCJhbGciOiJFUzI1NiIsInR5cCI6Im9hdXRoLWF1dGh6LXJlcStqd3QifQ.eyJyZXNwb25zZV91cmkiOiJodHRwczovL2V4YW1wbGUuY29tIiwiYXVkIjoiZGlkOmV4YW1wbGU6MTIzNDUiLCJpc3MiOiJkaWQ6ZXhhbXBsZToxMjM0NSIsInJlc3BvbnNlX3R5cGUiOiJ2cF90b2tlbiIsInByZXNlbnRhdGlvbl9kZWZpbml0aW9uIjp7ImlkIjoiM2ZhODVmNjQtMDAwMC0wMDAwLWIzZmMtMmM5NjNmNjZhZmE2IiwibmFtZSI6InN0cmluZyIsInB1cnBvc2UiOiJzdHJpbmciLCJpbnB1dF9kZXNjcmlwdG9ycyI6W3siaWQiOiIzZmE4NWY2NC01NzE3LTQ1NjItYjNmYy0yYzk2M2Y2NmFmYTYiLCJuYW1lIjoiQSBuYW1lIiwiZm9ybWF0Ijp7InZjK3NkLWp3dCI6eyJzZC1qd3RfYWxnX3ZhbHVlcyI6WyJFUzI1NiJdLCJrYi1qd3RfYWxnX3ZhbHVlcyI6WyJFUzI1NiJdfX0sImNvbnN0cmFpbnRzIjp7ImZpZWxkcyI6W3sicGF0aCI6WyIkLmxhc3ROYW1lIl19XX19XX0sIm5vbmNlIjoiSTAyRmliTEY0azVFc2ZETzJqZ2pEb29QNEEvWnVrUTMiLCJjbGllbnRfaWQiOiJkZWNlbnRyYWxpemVkX2lkZW50aWZpZXI6ZGlkOmV4YW1wbGU6MTIzNDUiLCJjbGllbnRfbWV0YWRhdGEiOnsiY2xpZW50X25hbWUiOiJSZWYgVGVzdCIsImxvZ29fdXJpIjoid3d3LmV4YW1wbGUuaWNvIn0sInJlc3BvbnNlX21vZGUiOiJkaXJlY3RfcG9zdCIsInN0YXRlIjoic3RhdGUifQ.5-3bMiZSxToZHrP8rsQPmSQPsJ5j4aOLrl6WdvBHan5I2JimhoIoU-kHTg0zDMXATosDSOdTYUTY0xlxv1RGNA"

    val authorizationRequest = AuthorizationRequest(
        nonce = "iusto",
        responseUri = "tincidunt",
        responseMode = "direct_post",
        clientId = CLIENT_ID,
        responseType = "vp_token",
        clientMetaData = null,
        dcqlQuery = DcqlQuery(
            credentials = listOf(
                CredentialQuery(
                    id = "id",
                    format = CredentialFormat.VC_SD_JWT.format,
                    requireCryptographicHolderBinding = true,
                )
            )
        ),
        state = "state",
        expectedOrigins = emptyList()
    )

    val authorizationRequestDirectPostJwt = authorizationRequest.copy(
        responseMode = "direct_post.jwt",
        clientMetaData = ClientMetaData(
            clientNameList = emptyList(),
            logoUriList = emptyList(),
        ),
    )

    val authorizationRequestNoState = authorizationRequest.copy(
        state = null,
    )

    val authorizationRequestDCQL = AuthorizationRequest(
        nonce = "iusto",
        responseUri = "tincidunt",
        responseMode = "direct_post",
        clientId = CLIENT_ID,
        responseType = "vp_token",
        clientMetaData = null,
        dcqlQuery = DcqlQuery(
            credentials = listOf(
                CredentialQuery(
                    id = "id",
                    format = CredentialFormat.VC_SD_JWT.format,
                    requireCryptographicHolderBinding = true,
                )
            )
        ),
        state = null,
        expectedOrigins = emptyList()
    )

    val authorizationRequestDCQLHolderBindingAndState = authorizationRequestDCQL.copy(
        state = "state",
    )

    val authorizationRequestDCQLNoHolderBinding = authorizationRequestDCQL.copy(
        dcqlQuery = DcqlQuery(
            credentials = listOf(
                CredentialQuery(
                    id = "id",
                    format = CredentialFormat.VC_SD_JWT.format,
                    requireCryptographicHolderBinding = false,
                )
            )
        ),
        state = "state",
    )

    fun invalidPresentationRequestClaims() = authorizationRequest.copy(
        dcqlQuery = DcqlQuery(
            credentials = listOf(
                CredentialQuery(
                    id = "id",
                    format = CredentialFormat.VC_SD_JWT.format,
                    claims = emptyList(),
                )
            )
        )
    )

    fun invalidPresentationRequestPresentationRequestDCQL() = authorizationRequest.copy(
        dcqlQuery = null,
    )

    fun invalidDCQLPresentationRequestState() = authorizationRequestDCQL.copy(
        dcqlQuery = DcqlQuery(
            credentials = listOf(
                CredentialQuery(
                    id = "id",
                    format = CredentialFormat.VC_SD_JWT.format,
                    requireCryptographicHolderBinding = false,
                ),
                CredentialQuery(
                    id = "id2",
                    format = CredentialFormat.VC_SD_JWT.format,
                    requireCryptographicHolderBinding = true,
                ),
            )
        ),
    )

    val authorizationRequestWithDisplays = AuthorizationRequest(
        nonce = "iusto",
        responseUri = "tincidunt",
        responseMode = "suscipit",
        clientId = "clientId",
        responseType = "responseType",
        clientMetaData = ClientMetaData(
            clientNameList = listOf(
                ClientName(
                    clientName = "firstClientName",
                    locale = "en"
                ),
                ClientName(
                    clientName = "secondClientName",
                    locale = "fr"
                ),
                ClientName(
                    clientName = "clientName",
                    locale = "fallback"
                )
            ),
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
                    logoUri = "logoUri",
                    locale = "fallback"
                )
            )
        ),
        dcqlQuery = null,
        state = "state",
        expectedOrigins = emptyList()
    )
}
