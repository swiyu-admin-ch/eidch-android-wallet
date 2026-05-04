package ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation.mock

import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceReasonDisplay
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceResponse
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonCompliantActor

object NonComplianceMocks {
    const val REPORTED_ACTOR_DID = "reported actor did"
    const val NON_REPORTED_ACTOR_DID = "non reported actor did"
    const val NON_COMPLIANCE_REASON_EN = "reason en"
    val nonComplianceReasonDisplayEn = NonComplianceReasonDisplay(locale = "en", NON_COMPLIANCE_REASON_EN)

    val nonComplianceReasonDisplays = listOf(
        nonComplianceReasonDisplayEn,
        NonComplianceReasonDisplay(locale = "de", "reason de"),
        NonComplianceReasonDisplay(locale = "fr", "reason fr"),
    )

    val nonComplianceResponseSuccess = NonComplianceResponse(
        nonCompliantActors = listOf(
            NonCompliantActor(
                did = REPORTED_ACTOR_DID,
                flaggedAsNonCompliantAt = "01.02.2025",
                reason = mapOf(
                    "en" to "reason en",
                    "de" to "reason de",
                    "fr" to "reason fr",
                )
            )
        )
    )

    const val PRESENTATION_REQUEST_JWT = "eyJraWQiOiJkaWQ6dGR3OjEyMyIsImFsZyI6IkVTMjU2In0.eyJyZXNwb25zZV91cmkiOiJodHRwczovL2V4YW1wbGUuY29tIiwiY2xpZW50X2lkX3NjaGVtZSI6ImRpZCIsImlzcyI6ImRpZDpleGFtcGxlOjEyMzQiLCJyZXNwb25zZV90eXBlIjoidnBfdG9rZW4iLCJwcmVzZW50YXRpb25fZGVmaW5pdGlvbiI6eyJpZCI6IjEyMyIsIm5hbWUiOiJzdHJpbmciLCJwdXJwb3NlIjoic3RyaW5nIiwiaW5wdXRfZGVzY3JpcHRvcnMiOlt7ImlkIjoiMTIzNCIsIm5hbWUiOiJBIG5hbWUiLCJmb3JtYXQiOnsidmMrc2Qtand0Ijp7InNkLWp3dF9hbGdfdmFsdWVzIjpbIkVTMjU2Il0sImtiLWp3dF9hbGdfdmFsdWVzIjpbIkVTMjU2Il19fSwiY29uc3RyYWludHMiOnsiZmllbGRzIjpbeyJwYXRoIjpbInBhdGgxIl19LHsicGF0aCI6WyJwYXRoMiJdLCJmaWx0ZXIiOnsiY29uc3QiOiJjb25zdHJhaW50MiIsInR5cGUiOiJ0eXBlMiJ9fV19fV19LCJub25jZSI6Im5vbmNlIiwiY2xpZW50X2lkIjoiZGlkOmV4YW1wbGU6MTIzNDUiLCJjbGllbnRfbWV0YWRhdGEiOnsiY2xpZW50X25hbWUiOiJSZWYgVGVzdCIsImxvZ29fdXJpIjoid3d3LmV4YW1wbGUuaWNvIn0sInJlc3BvbnNlX21vZGUiOiJkaXJlY3RfcG9zdCJ9.oEVCBUFpvKWjTeUsx3tofbKCSVivJgdYT0aRox_MG7lPS2ar6rfB6GlteRO0W2v5WQi41vVV3tpnjssv0PPPJA"

    val presentationRequestJson = """
{
  "response_uri": "https://example.com",
  "iss": "did:example:1234",
  "response_type": "vp_token",
  "presentation_definition": {
    "id": "123",
    "name": "string",
    "purpose": "string",
    "input_descriptors": [
      {
        "id": "1234",
        "name": "A name",
        "format": {
          "vc+sd-jwt": {
            "sd-jwt_alg_values": [
              "ES256"
            ],
            "kb-jwt_alg_values": [
              "ES256"
            ]
          }
        },
        "constraints": {
          "fields": [
            {
              "path": [
                "path1"
              ]
            },
            {
              "path": [
                "path2"
              ],
              "filter": {
                "const": "constraint2",
                "type": "type2"
              }
            }
          ]
        }
      }
    ]
  },
  "nonce": "nonce",
  "client_id": "did:example:12345",
  "client_metadata": {
    "client_name": "Ref Test",
    "logo_uri": "www.example.ico"
  },
  "response_mode": "direct_post"
}
    """.trimIndent()
}
