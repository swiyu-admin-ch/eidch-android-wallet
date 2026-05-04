package ch.admin.foitt.wallet.feature.presentationRequest.mock

object PresentationRequestMocks {
    const val MOCK_AUTHORIZATION_REQUEST = """
        {
           "client_id":"did:example:12345",
           "response_type":"vp_token",
           "response_mode":"direct_post",
           "response_uri":"https://example.org/request-object/21fa2dd6-920b-49d4-913d-36b28de767cd/response-data",
           "nonce":"nonce",
           "presentation_definition":{
              "id":"667b23f1-0839-48d5-b782-b62c2eb0e101",
              "purpose":"string",
              "input_descriptors":[
                 {
                    "id":"3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "name":"Elfa",
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
                    "constraints":{
                       "fields":[
                          {
                             "path":[
                                "$.firstName"
                             ]
                          },
                          {
                             "path":[
                                "$.lastName"
                             ]
                          },
                          {
                             "path":[
                                "$.dateOfBirth"
                             ]
                          },
                          {
                             "path":[
                                "$.hometown"
                             ]
                          },
                          {
                             "path":[
                                "$.categoryCode"
                             ]
                          }
                       ]
                    }
                 }
              ]
           },
           "client_metadata":{
             "client_name": "Verifier",
             "client_name#de": "DE Verifier",
             "client_name#fr": "FR Verifier",
             "client_name#en": "EN Verifier",
             "logo_uri": "www.examle.com/logo.png",
             "logo_uri#fr": "www.example.org/french-logo.png",
             "logo_uri#en": "www.example.org/english-logo.png"
           }
        }
    """
}
