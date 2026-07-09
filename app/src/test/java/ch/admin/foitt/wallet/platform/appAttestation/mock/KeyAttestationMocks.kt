package ch.admin.foitt.wallet.platform.appAttestation.mock

import ch.admin.foitt.wallet.platform.appAttestation.domain.model.KeyAttestationJwt

internal object KeyAttestationMocks {

/*
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
    val jwtAttestation01 get() = KeyAttestationJwt(
        "eyJ0eXAiOiJrZXktYXR0ZXN0YXRpb24rand0IiwiYWxnIjoiRVMyNTYiLCJjcnYiOiJFZDQ0OCIsImtpZCI6ImRpZDp0ZHc6ZXhhbXBsZS5jb20ja2V5LTAxIn0.eyJpYXQiOjE1MTYyNDcwMjIsImV4cCI6MjA2ODAxMDExNCwia2V5X3N0b3JhZ2UiOlsiaXNvXzE4MDQ1X21vZGVyYXRlIl0sImF0dGVzdGVkX2tleXMiOlt7Imt0eSI6IkVDIiwiY3J2IjoiUC0yNTYiLCJ4IjoiVENBRVIxOVp2dTNPSEY0ajRXNHZmU1ZvSElQMUlMaWxEbHM3dkNlR2VtYyIsInkiOiJaeGppV1diWk1RR0hWV0tWUTRoYlNJaXJzVmZ1ZWNDRTZ0NGpUOUYySFpRIn1dfQ.ylaqOGrnUFJ_6YI_wTwzbcZARH_7AAdK-hbQSJcXzweKqcZ7x-86QorIpKWId5DxxWYx8kDDxiaKvx7EEu34cw"
    )
    val jwtAttestation02NoStorage get() = KeyAttestationJwt(
        "eyJ0eXAiOiJrZXktYXR0ZXN0YXRpb24rand0IiwiYWxnIjoiRVMyNTYiLCJraWQiOiJkaWQ6dGR3OmV4YW1wbGUuY29tI2tleS0wMSJ9.eyJpYXQiOjE1MTYyNDcwMjIsImV4cCI6MTU0MTQ5MzcyNCwiYXR0ZXN0ZWRfa2V5cyI6W3sia3R5IjoiRUMiLCJjcnYiOiJQLTI1NiIsIngiOiJUQ0FFUjE5WnZ1M09IRjRqNFc0dmZTVm9ISVAxSUxpbERsczd2Q2VHZW1jIiwieSI6Ilp4amlXV2JaTVFHSFZXS1ZRNGhiU0lpcnNWZnVlY0NFNnQ0alQ5RjJIWlEifV19.rHaWiVf8FiXgzean3A3zml3QxK-ZgbVtIuRc58sYZHXOjGXFMw3F58aloKwQFs30uVUExiqTQHDEts7YeFCglQ"
    )
    val jwtAttestation03UnknownStorage get() = KeyAttestationJwt(
        "eyJ0eXAiOiJrZXktYXR0ZXN0YXRpb24rand0IiwiYWxnIjoiRVMyNTYiLCJraWQiOiJkaWQ6dGR3OmV4YW1wbGUuY29tI2tleS0wMSJ9.eyJpYXQiOjE1MTYyNDcwMjIsImV4cCI6MTU0MTQ5MzcyNCwia2V5X3N0b3JhZ2UiOlsiaXNvXzE4MDQ1X3N1cGVyX2R1cGVyIl0sImF0dGVzdGVkX2tleXMiOlt7Imt0eSI6IkVDIiwiY3J2IjoiUC0yNTYiLCJ4IjoiVENBRVIxOVp2dTNPSEY0ajRXNHZmU1ZvSElQMUlMaWxEbHM3dkNlR2VtYyIsInkiOiJaeGppV1diWk1RR0hWV0tWUTRoYlNJaXJzVmZ1ZWNDRTZ0NGpUOUYySFpRIn1dfQ.s6Q0DADZAGz_5oOuQRN9dlqDvV1Z_3b2QCDvuGADbmkast88Cnidasf4RZyk7VGpcGjpOXPV6P5SsHC9_96-gg"
    )
    val jwtSimple01 get() = KeyAttestationJwt(
        "eyJ0eXAiOiJrZXktYXR0ZXN0YXRpb24rand0IiwiYWxnIjoiRVMyNTYiLCJraWQiOiJkaWQ6dGR3OmV4YW1wbGUuY29tI2tleS0wMSJ9.eyJpYXQiOjE1MTYyNDcwMjIsImV4cCI6MTU0MTQ5MzcyNH0.hMWjWi_EOdFMCqlBsC7dhQi8aatr8xcQtL3pwteNCYt-iZqpWkV03DWfpqrWCBBFcetNS1dFYZVX_tJQWIQ66g"
    )
    val jwkEcP256_01 = """
        {
            "kty":"EC",
            "crv":"P-256",
            "x":"TCAER19Zvu3OHF4j4W4vfSVoHIP1ILilDls7vCeGemc",
            "y":"ZxjiWWbZMQGHVWKVQ4hbSIirsVfuecCE6t4jT9F2HZQ"
        }
    """.trimIndent()

    val jwkEcP256_02 = """
        {
            "kty":"EC",
            "crv":"P-256",
            "x":"MKBCTNIcKUSDii11ySs3526iDZ8AiTo7Tu6KPAqv7D4",
            "y":"4Etl6SRW2YiLUrN5vfvVHuhp7x8PxltmWWlbbM4IFyM",
            "kid":"1"
        }
    """.trimIndent()
}
