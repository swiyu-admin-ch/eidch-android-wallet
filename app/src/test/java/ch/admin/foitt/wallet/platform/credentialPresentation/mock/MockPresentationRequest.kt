package ch.admin.foitt.wallet.platform.credentialPresentation.mock

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientMetaData
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientName
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.Constraints
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.Field
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptor
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptorFormat
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.LogoUri
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationDefinition
import uniffi.heidi_dcql_rust.CredentialQuery
import uniffi.heidi_dcql_rust.DcqlQuery

object MockPresentationRequest {

    const val CLIENT_ID = "decentralized_identifier:did:example:12345"
    const val VALID_JWT =
        "eyJraWQiOiJkaWQ6ZXhhbXBsZToxMjM0NSNrZXktMDEiLCJhbGciOiJFUzI1NiIsInR5cCI6Im9hdXRoLWF1dGh6LXJlcStqd3QifQ.eyJyZXNwb25zZV91cmkiOiJodHRwczovL2V4YW1wbGUuY29tIiwiY2xpZW50X2lkX3NjaGVtZSI6ImRpZCIsImlzcyI6ImRpZDpleGFtcGxlOjEyMzQ1IiwicmVzcG9uc2VfdHlwZSI6InZwX3Rva2VuIiwicHJlc2VudGF0aW9uX2RlZmluaXRpb24iOnsiaWQiOiIzZmE4NWY2NC0wMDAwLTAwMDAtYjNmYy0yYzk2M2Y2NmFmYTYiLCJuYW1lIjoic3RyaW5nIiwicHVycG9zZSI6InN0cmluZyIsImlucHV0X2Rlc2NyaXB0b3JzIjpbeyJpZCI6IjNmYTg1ZjY0LTU3MTctNDU2Mi1iM2ZjLTJjOTYzZjY2YWZhNiIsIm5hbWUiOiJBIG5hbWUiLCJmb3JtYXQiOnsidmMrc2Qtand0Ijp7InNkLWp3dF9hbGdfdmFsdWVzIjpbIkVTMjU2Il0sImtiLWp3dF9hbGdfdmFsdWVzIjpbIkVTMjU2Il19fSwiY29uc3RyYWludHMiOnsiZmllbGRzIjpbeyJwYXRoIjpbIiQubGFzdE5hbWUiXX1dfX1dfSwibm9uY2UiOiJJMDJGaWJMRjRrNUVzZkRPMmpnakRvb1A0QS9adWtRMyIsImNsaWVudF9pZCI6ImRlY2VudHJhbGl6ZWRfaWRlbnRpZmllcjpkaWQ6ZXhhbXBsZToxMjM0NSIsImNsaWVudF9tZXRhZGF0YSI6eyJjbGllbnRfbmFtZSI6IlJlZiBUZXN0IiwibG9nb191cmkiOiJ3d3cuZXhhbXBsZS5pY28ifSwicmVzcG9uc2VfbW9kZSI6ImRpcmVjdF9wb3N0Iiwic3RhdGUiOiJzdGF0ZSJ9.kCM4UMdvzGZGkwjSp-8fxd6AI2UMKmTrkdwJmZ9TDv0gEXoFqk0nS6KT8N3P0S6_UAQJodCOUrGP5J_OzuqcYA"

    const val NOT_YET_VALID_JWT =
        "eyJraWQiOiJkaWQ6ZXhhbXBsZToxMjM0NSNrZXktMDEiLCJhbGciOiJFUzI1NiIsInR5cCI6Im9hdXRoLWF1dGh6LXJlcStqd3QifQ.eyJuYmYiOjE5MjQ5ODgzOTksInJlc3BvbnNlX3VyaSI6Imh0dHBzOi8vZXhhbXBsZS5jb20iLCJpc3MiOiJkaWQ6ZXhhbXBsZToxMjM0NSIsInJlc3BvbnNlX3R5cGUiOiJ2cF90b2tlbiIsInByZXNlbnRhdGlvbl9kZWZpbml0aW9uIjp7ImlkIjoiM2ZhODVmNjQtMDAwMC0wMDAwLWIzZmMtMmM5NjNmNjZhZmE2IiwibmFtZSI6InN0cmluZyIsInB1cnBvc2UiOiJzdHJpbmciLCJpbnB1dF9kZXNjcmlwdG9ycyI6W3siaWQiOiIzZmE4NWY2NC01NzE3LTQ1NjItYjNmYy0yYzk2M2Y2NmFmYTYiLCJuYW1lIjoiQSBuYW1lIiwiZm9ybWF0Ijp7InZjK3NkLWp3dCI6eyJzZC1qd3RfYWxnX3ZhbHVlcyI6WyJFUzI1NiJdLCJrYi1qd3RfYWxnX3ZhbHVlcyI6WyJFUzI1NiJdfX0sImNvbnN0cmFpbnRzIjp7ImZpZWxkcyI6W3sicGF0aCI6WyIkLmxhc3ROYW1lIl19XX19XX0sIm5vbmNlIjoiSTAyRmliTEY0azVFc2ZETzJqZ2pEb29QNEEvWnVrUTMiLCJjbGllbnRfaWQiOiJkZWNlbnRyYWxpemVkX2lkZW50aWZpZXI6ZGlkOmV4YW1wbGU6MTIzNDUiLCJjbGllbnRfbWV0YWRhdGEiOnsiY2xpZW50X25hbWUiOiJSZWYgVGVzdCIsImxvZ29fdXJpIjoid3d3LmV4YW1wbGUuaWNvIn0sInJlc3BvbnNlX21vZGUiOiJkaXJlY3RfcG9zdCJ9._8NNvx7den7EnVmlv-0kUCgRhKlRU_7BNppDdIb9GssxAxPsEZnKdvgAPAAIKC7LkIrLKfOH7cGz_t9fCKTZIQ"

    const val EXPIRED_JWT =
        "eyJraWQiOiJkaWQ6ZXhhbXBsZToxMjM0NSNrZXktMDEiLCJhbGciOiJFUzI1NiIsInR5cCI6Im9hdXRoLWF1dGh6LXJlcStqd3QifQ.eyJleHAiOjAsInJlc3BvbnNlX3VyaSI6Imh0dHBzOi8vZXhhbXBsZS5jb20iLCJpc3MiOiJkaWQ6ZXhhbXBsZToxMjM0NSIsInJlc3BvbnNlX3R5cGUiOiJ2cF90b2tlbiIsInByZXNlbnRhdGlvbl9kZWZpbml0aW9uIjp7ImlkIjoiM2ZhODVmNjQtMDAwMC0wMDAwLWIzZmMtMmM5NjNmNjZhZmE2IiwibmFtZSI6InN0cmluZyIsInB1cnBvc2UiOiJzdHJpbmciLCJpbnB1dF9kZXNjcmlwdG9ycyI6W3siaWQiOiIzZmE4NWY2NC01NzE3LTQ1NjItYjNmYy0yYzk2M2Y2NmFmYTYiLCJuYW1lIjoiQSBuYW1lIiwiZm9ybWF0Ijp7InZjK3NkLWp3dCI6eyJzZC1qd3RfYWxnX3ZhbHVlcyI6WyJFUzI1NiJdLCJrYi1qd3RfYWxnX3ZhbHVlcyI6WyJFUzI1NiJdfX0sImNvbnN0cmFpbnRzIjp7ImZpZWxkcyI6W3sicGF0aCI6WyIkLmxhc3ROYW1lIl19XX19XX0sIm5vbmNlIjoiSTAyRmliTEY0azVFc2ZETzJqZ2pEb29QNEEvWnVrUTMiLCJjbGllbnRfaWQiOiJkZWNlbnRyYWxpemVkX2lkZW50aWZpZXI6ZGlkOmV4YW1wbGU6MTIzNDUiLCJjbGllbnRfbWV0YWRhdGEiOnsiY2xpZW50X25hbWUiOiJSZWYgVGVzdCIsImxvZ29fdXJpIjoid3d3LmV4YW1wbGUuaWNvIn0sInJlc3BvbnNlX21vZGUiOiJkaXJlY3RfcG9zdCJ9.KDtjCoWGe8amxrxALEwQ5VgNPgEOs-ML-xAShfQRhPDPgam8lWmPTrwMkKHz-BcFm0Q3yQ_bcaIstreMM7QESg"

    const val JWT_MISSING_RESPONSE_URI =
        "eyJraWQiOiJkaWQ6ZXhhbXBsZToxMjM0NSNrZXktMDEiLCJhbGciOiJFUzI1NiIsInR5cCI6Im9hdXRoLWF1dGh6LXJlcStqd3QifQ.eyJpc3MiOiJkaWQ6ZXhhbXBsZToxMjM0NSIsInJlc3BvbnNlX3R5cGUiOiJ2cF90b2tlbiIsInByZXNlbnRhdGlvbl9kZWZpbml0aW9uIjp7ImlkIjoiM2ZhODVmNjQtMDAwMC0wMDAwLWIzZmMtMmM5NjNmNjZhZmE2IiwibmFtZSI6InN0cmluZyIsInB1cnBvc2UiOiJzdHJpbmciLCJpbnB1dF9kZXNjcmlwdG9ycyI6W3siaWQiOiIzZmE4NWY2NC01NzE3LTQ1NjItYjNmYy0yYzk2M2Y2NmFmYTYiLCJuYW1lIjoiQSBuYW1lIiwiZm9ybWF0Ijp7InZjK3NkLWp3dCI6eyJzZC1qd3RfYWxnX3ZhbHVlcyI6WyJFUzI1NiJdLCJrYi1qd3RfYWxnX3ZhbHVlcyI6WyJFUzI1NiJdfX0sImNvbnN0cmFpbnRzIjp7ImZpZWxkcyI6W3sicGF0aCI6WyIkLmxhc3ROYW1lIl19XX19XX0sIm5vbmNlIjoiSTAyRmliTEY0azVFc2ZETzJqZ2pEb29QNEEvWnVrUTMiLCJjbGllbnRfaWQiOiJkZWNlbnRyYWxpemVkX2lkZW50aWZpZXI6ZGlkOmV4YW1wbGU6MTIzNDUiLCJjbGllbnRfbWV0YWRhdGEiOnsiY2xpZW50X25hbWUiOiJSZWYgVGVzdCIsImxvZ29fdXJpIjoid3d3LmV4YW1wbGUuaWNvIn0sInJlc3BvbnNlX21vZGUiOiJkaXJlY3RfcG9zdCIsInN0YXRlIjoic3RhdGUifQ.E8J430Hu6aMzS_R58Kltk1330APz1e6NxbhI3q8USM1qswh0_iOEKrBkSIFsgXAoJFvpxqY3ihMAOLpLZRitpg"

    const val JWT_CONTAINING_INVALID_AUTHORIZATION_REQUEST =
        "eyJraWQiOiJkaWQ6ZXhhbXBsZToxMjM0NSNrZXktMDEiLCJhbGciOiJFUzI1NiIsInR5cCI6Im9hdXRoLWF1dGh6LXJlcStqd3QifQ.eyJpc3MiOiJkaWQ6ZXhhbXBsZToxMjM0NSIsInJlc3BvbnNlX3R5cGUiOiJ2cF90b2tlbiIsInByZXNlbnRhdGlvbl9kZWZpbml0aW9uIjp7ImlkIjoiM2ZhODVmNjQtMDAwMC0wMDAwLWIzZmMtMmM5NjNmNjZhZmE2IiwibmFtZSI6InN0cmluZyIsInB1cnBvc2UiOiJzdHJpbmciLCJpbnB1dF9kZXNjcmlwdG9ycyI6W3siaWQiOiIzZmE4NWY2NC01NzE3LTQ1NjItYjNmYy0yYzk2M2Y2NmFmYTYiLCJuYW1lIjoiQSBuYW1lIiwiZm9ybWF0Ijp7InZjK3NkLWp3dCI6eyJzZC1qd3RfYWxnX3ZhbHVlcyI6WyJFUzI1NiJdLCJrYi1qd3RfYWxnX3ZhbHVlcyI6WyJFUzI1NiJdfX0sImNvbnN0cmFpbnRzIjp7ImZpZWxkcyI6W3sicGF0aCI6WyIkLmxhc3ROYW1lIl19XX19XX0sIm5vbmNlIjoiSTAyRmliTEY0azVFc2ZETzJqZ2pEb29QNEEvWnVrUTMiLCJjbGllbnRfaWQiOiJkZWNlbnRyYWxpemVkX2lkZW50aWZpZXI6ZGlkOmV4YW1wbGU6MTIzNDUiLCJjbGllbnRfbWV0YWRhdGEiOnsiY2xpZW50X25hbWUiOiJSZWYgVGVzdCIsImxvZ29fdXJpIjoid3d3LmV4YW1wbGUuaWNvIn0sInJlc3BvbnNlX21vZGUiOiJkaXJlY3RfcG9zdCIsInN0YXRlIjoic3RhdGUiLCJvdGhlckNsYWltIjoib3RoZXJWYWx1ZSJ9.QN5ZT7DD4ABstg4uVyhvx-WzkB61gr2iM30TcbgRa7pyGfcI_-FJjxHh6pBODZScrx99oKbbJ3QtuD7W2Ln7Dg"

    const val JWT_MISSING_CLIENT_ID =
        "eyJraWQiOiJkaWQ6ZXhhbXBsZToxMjM0NSNrZXktMDEiLCJhbGciOiJFUzI1NiIsInR5cCI6Im9hdXRoLWF1dGh6LXJlcStqd3QifQ.eyJyZXNwb25zZV91cmkiOiJodHRwczovL2V4YW1wbGUuY29tIiwiaXNzIjoiZGlkOmV4YW1wbGU6MTIzNDUiLCJyZXNwb25zZV90eXBlIjoidnBfdG9rZW4iLCJwcmVzZW50YXRpb25fZGVmaW5pdGlvbiI6eyJpZCI6IjNmYTg1ZjY0LTAwMDAtMDAwMC1iM2ZjLTJjOTYzZjY2YWZhNiIsIm5hbWUiOiJzdHJpbmciLCJwdXJwb3NlIjoic3RyaW5nIiwiaW5wdXRfZGVzY3JpcHRvcnMiOlt7ImlkIjoiM2ZhODVmNjQtNTcxNy00NTYyLWIzZmMtMmM5NjNmNjZhZmE2IiwibmFtZSI6IkEgbmFtZSIsImZvcm1hdCI6eyJ2YytzZC1qd3QiOnsic2Qtand0X2FsZ192YWx1ZXMiOlsiRVMyNTYiXSwia2Itand0X2FsZ192YWx1ZXMiOlsiRVMyNTYiXX19LCJjb25zdHJhaW50cyI6eyJmaWVsZHMiOlt7InBhdGgiOlsiJC5sYXN0TmFtZSJdfV19fV19LCJub25jZSI6IkkwMkZpYkxGNGs1RXNmRE8yamdqRG9vUDRBL1p1a1EzIiwiY2xpZW50X21ldGFkYXRhIjp7ImNsaWVudF9uYW1lIjoiUmVmIFRlc3QiLCJsb2dvX3VyaSI6Ind3dy5leGFtcGxlLmljbyJ9LCJyZXNwb25zZV9tb2RlIjoiZGlyZWN0X3Bvc3QiLCJzdGF0ZSI6InN0YXRlIn0.IvK_f4W7YRUgijye_PcCzjdCt5QCtKfnzaVyn8nRNbF4hnuLw8po4ymVv7Yy7Etb3sQoAaYpSQ81DrH7Ztey1Q"

    private val constraints = Constraints(
        fields = listOf(
            Field(
                path = listOf()
            )
        )
    )

    private val inputDescriptorFormatVcSdJwt = InputDescriptorFormat.VcSdJwt(
        sdJwtAlgorithms = listOf(),
        kbJwtAlgorithms = listOf(),
    )

    val inputDescriptor = InputDescriptor(
        id = "id",
        name = "name",
        formats = listOf(
            inputDescriptorFormatVcSdJwt
        ),
        constraints = constraints,
        purpose = "constraintPurpose",
    )

    private val presentationDefinition = PresentationDefinition(
        id = "diam",
        inputDescriptors = listOf(inputDescriptor),
        purpose = "definitionPurpose",
        name = "name",
    )

    val authorizationRequest = AuthorizationRequest(
        nonce = "iusto",
        presentationDefinition = presentationDefinition,
        responseUri = "tincidunt",
        responseMode = "direct_post",
        clientId = CLIENT_ID,
        responseType = "vp_token",
        clientMetaData = null,
        dcqlQuery = null,
        state = "state",
    )

    fun invalidPresentationRequestFields() = authorizationRequest.copy(
        presentationDefinition = presentationDefinition.copy(
            inputDescriptors = listOf(
                inputDescriptor.copy(
                    constraints = Constraints(
                        fields = emptyList()
                    )
                )
            )
        )
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
        presentationDefinition = null,
        dcqlQuery = null,
    )

    fun invalidPresentationRequestPath(paths: List<String>) = authorizationRequest.copy(
        presentationDefinition = presentationDefinition.copy(
            inputDescriptors = listOf(
                inputDescriptor.copy(
                    constraints = Constraints(
                        fields = listOf(
                            Field(path = paths)
                        ),
                    )
                )
            )
        )
    )

    fun invalidPresentationRequestState() = authorizationRequest.copy(
        dcqlQuery = DcqlQuery(
            credentials = listOf(
                CredentialQuery(
                    id = "id",
                    format = CredentialFormat.VC_SD_JWT.format,
                    requireCryptographicHolderBinding = false,
                )
            )
        ),
        state = null,
    )

    val authorizationRequestWithDisplays = AuthorizationRequest(
        nonce = "iusto",
        presentationDefinition = presentationDefinition,
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
        state = "state"
    )
}
