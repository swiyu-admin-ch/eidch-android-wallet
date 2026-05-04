package ch.admin.foitt.openid4vc.domain.usecase.implementation.mock

object MockPresentationRequest {
    const val validJwt = "eyJraWQiOiJkaWQ6dGR3OjEyMzQ9OmV4YW1wbGUuY29tOmFwaTp2MTpkaWQ6MTIzNDU2I2tleS0wMSIsImFsZyI6IkVTMjU2In0.eyJyZXNwb25zZV91cmkiOiJodHRwczovL2V4YW1wbGUuY29tIiwiaXNzIjoiZGlkOmV4YW1wbGU6MTIzNDUiLCJyZXNwb25zZV90eXBlIjoidnBfdG9rZW4iLCJwcmVzZW50YXRpb25fZGVmaW5pdGlvbiI6eyJpZCI6IjNmYTg1ZjY0LTAwMDAtMDAwMC1iM2ZjLTJjOTYzZjY2YWZhNiIsIm5hbWUiOiJzdHJpbmciLCJwdXJwb3NlIjoic3RyaW5nIiwiaW5wdXRfZGVzY3JpcHRvcnMiOlt7ImlkIjoiM2ZhODVmNjQtNTcxNy00NTYyLWIzZmMtMmM5NjNmNjZhZmE2IiwibmFtZSI6IkEgbmFtZSIsImZvcm1hdCI6eyJ2YytzZC1qd3QiOnsic2Qtand0X2FsZ192YWx1ZXMiOlsiRVMyNTYiXSwia2Itand0X2FsZ192YWx1ZXMiOlsiRVMyNTYiXX19LCJjb25zdHJhaW50cyI6eyJmaWVsZHMiOlt7InBhdGgiOlsiJC5sYXN0TmFtZSJdfV19fV19LCJub25jZSI6IkkwMkZpYkxGNGs1RXNmRE8yamdqRG9vUDRBL1p1a1EzIiwiY2xpZW50X2lkIjoiZGlkOmV4YW1wbGU6MTIzNDUiLCJjbGllbnRfbWV0YWRhdGEiOnsiY2xpZW50X25hbWUiOiJSZWYgVGVzdCIsImxvZ29fdXJpIjoid3d3LmV4YW1wbGUuaWNvIn0sInJlc3BvbnNlX21vZGUiOiJkaXJlY3RfcG9zdCJ9.QO8Cnbg96lMAe1LgUnOMoXpfOIVEfQnLmBLeGe8r6Qhtj1IH-Xp-3rMLX1G6TxPDZHYGHD64Qwq6HQ1rBoBtVQ"

    val validJson = """
{
  "client_id" : "did:example:12345",
  "response_type" : "vp_token",
  "response_mode" : "direct_post",
  "response_uri" : "https://example.com",
  "nonce" : "zW0qUvtH3AczW8MTTSebAFrSbQsqSjc5",
  "presentation_definition" : {
    "id" : "3fa85f64-0000-0000-b3fc-2c963f66afa6",
    "name" : "string",
    "purpose" : "string",
    "input_descriptors" : [
      {
        "id" : "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        "name" : "A name",
        "format" : {
          "vc+sd-jwt" : {
            "sd-jwt_alg_values" : [
              "ES256"
            ],
            "kb-jwt_alg_values" : [
              "ES256"
            ]
          }
        },
        "constraints" : {
          "fields" : [
            {
              "path" : [
                "${'$'}.lastName"
              ]
            }
          ]
        }
      }
    ]
  },
  "client_metadata" : {
    "client_name" : "Ref Test",
    "logo_uri" : "www.example.ico"
  }
}
    """.trimIndent()

    const val invalidJwt = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.5EQDWnS78bM4ZPTgZ2HKbAAbTrkTdBH5JPbmfw34Dp1gOjrfiwVHYcYh7trwSreAA8VVQoQ_No6vCr3oRqkzRg"

    const val invalidJsonString = "{\"sub\": \"1234567890\",\n\"name\": \"John Doe\",\n\"iat\": 1516239022\n\"}"

    //region Presentation request source
    /* header
{
  "kid": "did:tdw:1234=:example.com:api:v1:did:123456#key-01",
  "alg": "ES256"
}
     */
    /* payload
{
    "response_uri": "https://example.com",
    "iss": "did:example:12345",
    "response_type": "vp_token",
    "presentation_definition": {
    "id": "3fa85f64-0000-0000-b3fc-2c963f66afa6",
    "name": "string",
    "purpose": "string",
    "input_descriptors": [
    {
        "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
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
            "$.lastName"
            ]
        }
        ]
    }
    }
    ]
},
    "nonce": "I02FibLF4k5EsfDO2jgjDooP4A/ZukQ3",
    "client_id": "did:example:12345",
    "client_metadata": {
    "client_name": "Ref Test",
    "logo_uri": "www.example.ico"
},
    "response_mode": "direct_post"
}
     */

    /* keys
-----BEGIN PUBLIC KEY-----
MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEEVs/o5+uQbTjL3chynL4wXgUg2R9
q9UU8I5mEovUf86QZ7kOBIjJwqnzD1omageEHWwHdBO6B+dFabmdT9POxg==
-----END PUBLIC KEY-----

-----BEGIN PRIVATE KEY-----
MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgevZzL1gdAFr88hb2
OF/2NxApJCzGCEDdfSp6VQO30hyhRANCAAQRWz+jn65BtOMvdyHKcvjBeBSDZH2r
1RTwjmYSi9R/zpBnuQ4EiMnCqfMPWiZqB4QdbAd0E7oH50VpuZ1P087G
-----END PRIVATE KEY-----
     */

    //endregion
}
