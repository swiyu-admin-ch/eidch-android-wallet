package ch.admin.foitt.openid4vc.domain.model.sdjwt.mock

internal object ReservedClaimNameSdJwt {
    /*
{
   "test_key_1":"test_value_1",
   "_sd":[
      "FULKYr37Epe1EE-B6Mp5I8fpa7S3f1WPAN3ohZ_4ViE"
   ],
   "test_key_3":{
      "_sd":[
         "QhuvIMQd5LyX8gOR3weVzSY0yGZGGHdVXY0E-NhhUfw"
      ]
   },
   "_sd_alg":"sha-256"
}
     */
    const val JWT =
        "eyJraWQiOiI0OTBmZDQ4NC1jNTE2LTQwNzktYmE4Mi1kYzkyZTA0NjAzZTEiLCJhbGciOiJFUzUxMiJ9.eyJ0ZXN0X2tleV8xIjoidGVzdF92YWx1ZV8xIiwiX3NkIjpbIkZVTEtZcjM3RXBlMUVFLUI2TXA1SThmcGE3UzNmMVdQQU4zb2haXzRWaUUiXSwidGVzdF9rZXlfMyI6eyJfc2QiOlsiUWh1dklNUWQ1THlYOGdPUjN3ZVZ6U1kweUdaR0dIZFZYWTBFLU5oaFVmdyJdfSwiX3NkX2FsZyI6InNoYS0yNTYifQ.AZgCnqdj8wvv6_mayeWeMqg7GRkXXyNB_Bc63DI3mtKmhZsxfPNhp2ACG4wsp4JH28CGKVuPQXORYb4wMXaEkhEAAdovqdU4Uc58HhQzSlZ_heRjuGI5j0U1ncU8SPdJhpnNm0AOvjLItJJqxaQPjwFcX_unhhG0h2apleftP3_B1OnU"

    // ["test_salt_3", "test_key_2", {"_sd":["YRLf606clwt4-hjyGze49ySFi6VCmwb9n5hwb4VUJSY"]}]
    // FULKYr37Epe1EE-B6Mp5I8fpa7S3f1WPAN3ohZ_4ViE
    const val DISCLOSURE_NESTED =
        "WyJ0ZXN0X3NhbHRfMyIsICJ0ZXN0X2tleV8yIiwgeyJfc2QiOlsiWVJMZjYwNmNsd3Q0LWhqeUd6ZTQ5eVNGaTZWQ213YjluNWh3YjRWVUpTWSJdfV0"

    const val JSON = """
{
   "test_key_1":"test_value_1",
   "test_key_2":{
      "test_key_1":"test_value_1"
   },
   "test_key_3":{
      "test_key_2":"test_value_2"
   }
}
    """

    val SD_JWT = JWT + listOf(DISCLOSURE_NESTED, Disclosure1, Disclosure2).toDisclosures()
}
