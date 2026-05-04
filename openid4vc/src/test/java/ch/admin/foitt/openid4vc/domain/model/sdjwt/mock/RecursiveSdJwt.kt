package ch.admin.foitt.openid4vc.domain.model.sdjwt.mock

internal object RecursiveSdJwt {
    /*   {
               "_sd":[
                  "EieNhILyn4oeWiVzOIGvy3shkJgwApK4OzLTRnqU8rY"
               ],
            "_sd_alg":"sha-256"
         } */

    /* Disclosure1
        [
           "test_salt_1",
           "test_key_1",
           {
              "_sd":[
                 "QhuvIMQd5LyX8gOR3weVzSY0yGZGGHdVXY0E-NhhUfw" // digest of disclosure 2
              ]
           }
        ] */

    const val JSON = """{"test_key_1":{"test_key_2":"test_value_2"}}"""
    private const val DISCLOSURE_1 =
        "WwogICAidGVzdF9zYWx0XzEiLAogICAidGVzdF9rZXlfMSIsCiAgIHsKICAgICAgIl9zZCI6WwogICAgICAgICAiUWh1dklNUWQ1THlYOGdPUjN3ZVZ6U1kweUdaR0dIZFZYWTBFLU5oaFVmdyIKICAgICAgXQogICB9Cl0"

    const val JWT =
        "eyJ0eXAiOiJyZWN1cnNpdmUiLCJhbGciOiJFUzUxMiJ9.eyJfc2QiOlsiRWllTmhJTHluNG9lV2lWek9JR3Z5M3Noa0pnd0FwSzRPekxUUm5xVThyWSJdLCJfc2RfYWxnIjoic2hhLTI1NiJ9.AIseB9SQD3kpzAzsvka_pmZ4uMh9ir42ofmcnZVgKv3cG4dRFweqxJDs6w8oz904B3XxidHWfC0p5U9gi3W68jy6AMYhp0Jg5tPuTKrFLOLx7VxZ3xD9A4_j01Ty0z6mYfNp12nC8mqYHWqh60VEGToN5nNN0RXHmPD5Y0R50Ioh1fFD"
    private val DISCLOSURES = listOf(DISCLOSURE_1, Disclosure2).toDisclosures()
    val SD_JWT = JWT + DISCLOSURES
}
