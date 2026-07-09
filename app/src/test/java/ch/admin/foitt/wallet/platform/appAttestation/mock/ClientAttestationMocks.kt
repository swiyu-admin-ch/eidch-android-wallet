package ch.admin.foitt.wallet.platform.appAttestation.mock

internal object ClientAttestationMocks {
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

    val jwtAttestation01 get() =
        "eyJraWQiOiJkaWQ6dGR3Omlzc3VlciNrZXktMDEiLCJ0eXAiOiJvYXV0aC1jbGllbnQtYXR0ZXN0YXRpb24rand0IiwiYWxnIjoiRVMyNTYifQ.eyJzdWIiOiJkaWQ6andrOmV5SmpjbllpT2lKUUxUSTFOaUlzSW10MGVTSTZJa1ZESWl3aWVDSTZJbXhtUW5OSmRHcENWamhWZHpaTmIwMXFSbEJOYXpaMFNrTjRWbkZXVEZkR056aENkemhSZVhKTkxWRWlMQ0o1SWpvaVdXSXlaVTVqT1RVMFJGZHJUakZaTWtOUVZHNXRia0owUjFaQ2FEUkdabVF0U2xRNGJFdFZXV3RRVVNKOSIsImNuZiI6eyJqd2siOnsia3R5IjoiRUMiLCJjcnYiOiJQLTI1NiIsIngiOiJsZkJzSXRqQlY4VXc2TW9NakZQTWs2dEpDeFZxVkxXRjc4Qnc4UXlyTS1RIiwieSI6IlliMmVOYzk1NERXa04xWTJDUFRubW5CdEdWQmg0RmZkLUpUOGxLVVlrUFEifX0sIm5iZiI6MTc0ODk2MDk2OSwiZXhwIjoxODEyMDMyOTY5LCJ3YWxsZXRfbmFtZSI6InN3aXl1In0.VxOpAq_PWzFrUhCUmheTxgzi_MKnJvxiIy4fNBZ5q19B3gbbShxvvsVSnexRNgxtRie-HzJe8hc4BJwZ9XChoQ"

    val jwkEcP256_01 = """
        {
            "kty": "EC",
            "crv": "P-256",
            "x": "lfBsItjBV8Uw6MoMjFPMk6tJCxVqVLWF78Bw8QyrM-Q",
            "y": "Yb2eNc954DWkN1Y2CPTnmnBtGVBh4Ffd-JT8lKUYkPQ"
        }
    """.trimIndent()

    val jwkEcP256_02_didJwk get() =
        "did:jwk:eyJrdHkiOiJFQyIsImNydiI6IlAtMjU2IiwieCI6ImY4M09KM0QyeEY0eU9kd2pUYk5KeFRBZFVERnpaeWJ4VFVUUityOXNWcVkiLCJ5IjoieF9GRXpSdTluYUZ4ZVd3NldZRjhJUkx3Rko5YzJWOFFQRVpreTgxaVF4MCJ9"

    val jwkEcP256_02 = """
        {
            "kty": "EC",
            "crv": "P-256",
            "x": "f83OJ3D2xF4yOdwjTbNJxTAdUDFzZybxTUTR-f9sVqY",
            "y": "x_FEzRu9naFxeVw6WYF8IRLwFJ9c2V9QPEZkY81iQx0"
        }
    """.trimIndent()
}
