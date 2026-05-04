package ch.admin.foitt.openid4vc.domain.model.sdjwt.mock

internal object DuplicateNameSdJwt {
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
        "eyJ0eXAiOiJkdXBsaWNhdGVOYW1lIiwiYWxnIjoiRVM1MTIifQ.eyJ0ZXN0X2tleV8xIjoidGVzdF92YWx1ZV8xIiwiX3NkIjpbInBrMXc3QzhjTHBPaGdUTmFOZURsOXZVU28zMTBNQlhrNDV6SzE5UVdkVHciXSwidGVzdF9rZXlfMyI6eyJfc2QiOlsiRS13TU1IZ2ttTUdfT2RBd2h1aWxzc1VmQXZfTkZjVFFINERUOXRteVhWayJdfSwiX3NkX2FsZyI6InNoYS0yNTYifQ.AShV6_mtqeGsORdoLEf5MT_ggoOdchpVvbpR7yvcAQzluQMmGns6KYzV5k2cBU3wMeNT_W40HCreVjnhWuPRZrZ6ARDfrOCgGEEHwQur_tfDmFvMvx_7rlFj2FGkz5koOChaw7oSqAtmWm0H1R9J4HSMtF4ZoQQkg06LVcqKDpNZzAzY"

    // ["test_salt_1", "test_key_1", "test_value_2"]
    // wKj4p6UpMHCEGnR2MuSlL1OQ6SGI_-am1i2lu1fv5cw
    const val DISCLOSURE_1 =
        "WyJ0ZXN0X3NhbHRfMSIsICJ0ZXN0X2tleV8xIiwgInRlc3RfdmFsdWVfMiJd"

    // ["test_salt_2", "test_key_2", "test_value_3"]
    // E-wMMHgkmMG_OdAwhuilssUfAv_NFcTQH4DT9tmyXVk
    const val DISCLOSURE_2 =
        "WyJ0ZXN0X3NhbHRfMiIsICJ0ZXN0X2tleV8yIiwgInRlc3RfdmFsdWVfMyJd"

    // ["test_salt_3", "test_key_2", {"_sd":["wKj4p6UpMHCEGnR2MuSlL1OQ6SGI_-am1i2lu1fv5cw"]}]
    // pk1w7C8cLpOhgTNaNeDl9vUSo310MBXk45zK19QWdTw
    const val DISCLOSURE_NESTED =
        "WyJ0ZXN0X3NhbHRfMyIsICJ0ZXN0X2tleV8yIiwgeyJfc2QiOlsid0tqNHA2VXBNSENFR25SMk11U2xMMU9RNlNHSV8tYW0xaTJsdTFmdjVjdyJdfV0"

    const val JSON = """
{
   "test_key_1":"test_value_1",
   "test_key_2":{
      "test_key_1":"test_value_2"
   },
   "test_key_3":{
      "test_key_2":"test_value_3"
   }
}
    """

    val SD_JWT = JWT + listOf(DISCLOSURE_1, DISCLOSURE_2, DISCLOSURE_NESTED).toDisclosures()
}
