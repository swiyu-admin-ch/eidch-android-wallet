package ch.admin.foitt.openid4vc.domain.model.sdjwt.mock

internal object FlatSimpleArraySdJwt {
    /*
{
   "_sd":[
      "YRLf606clwt4-hjyGze49ySFi6VCmwb9n5hwb4VUJSY"
   ],
   "array_key":[
      "test_array_value_1",
      {
         "...":"HWWQ_E69DRWp7FhCHyQdS01ushRMA9GXJpzh5DosDHU"
      }
   ],
   "_sd_alg":"sha-256"
}
     */
    const val JWT_WITH_OTHER_CLAIMS =
        "eyJraWQiOiI0OTBmZDQ4NC1jNTE2LTQwNzktYmE4Mi1kYzkyZTA0NjAzZTEiLCJhbGciOiJFUzUxMiJ9.eyJfc2QiOlsiWVJMZjYwNmNsd3Q0LWhqeUd6ZTQ5eVNGaTZWQ213YjluNWh3YjRWVUpTWSJdLCJhcnJheV9rZXkiOlsidGVzdF9hcnJheV92YWx1ZV8xIix7Ii4uLiI6IkhXV1FfRTY5RFJXcDdGaENIeVFkUzAxdXNoUk1BOUdYSnB6aDVEb3NESFUifV0sIl9zZF9hbGciOiJzaGEtMjU2In0.AIufSuPCcXs42JeCyTF-Iy0YxzlmNmRxQBjFXcMnOCHH9gUUK7EQ1OimflUQNJqQi7shvzI2Lga9pu3fRpFHCftVAFqEmncaFuXMThId3RrH6ID7XBI5o9RF5VXPGigHENWxiDZUMsR8w1da1_sNiuOnWiDCOvc-e0VYauWwLWdghXLr"

    /*
{
   "array_key":[
      {
         "...":"bgUmES59QYf9VR8IbgJy3LpbXWMrwEaoblSFS3poqEg"
      },
      {
         "...":"HWWQ_E69DRWp7FhCHyQdS01ushRMA9GXJpzh5DosDHU"
      }
   ],
   "_sd_alg":"sha-256"
}
     */
    const val JWT_WITH_ARRAY_ONLY =
        "eyJraWQiOiI0OTBmZDQ4NC1jNTE2LTQwNzktYmE4Mi1kYzkyZTA0NjAzZTEiLCJhbGciOiJFUzUxMiJ9.eyJhcnJheV9rZXkiOlt7Ii4uLiI6ImJnVW1FUzU5UVlmOVZSOEliZ0p5M0xwYlhXTXJ3RWFvYmxTRlMzcG9xRWcifSx7Ii4uLiI6IkhXV1FfRTY5RFJXcDdGaENIeVFkUzAxdXNoUk1BOUdYSnB6aDVEb3NESFUifV0sIl9zZF9hbGciOiJzaGEtMjU2In0.ALRDKIu67Ze8U3XTvkvzeR0lbiDpXvEoIlrPaeI8KImT2JqmUCc9oyNoZnW8klMN4h23Aqk7SJX26AdaCsTzJnXdAcp3LSiJtEQnr4aglNUCIA3EexZ3R_5R5LkwJEum6YBR2xm9GVapzGW0n2k2iW2Mwvy4Qc7yxvciF_j8tDcCLfU3"

    // ["test_salt", "test_array_value_1"]
    // bgUmES59QYf9VR8IbgJy3LpbXWMrwEaoblSFS3poqEg
    const val DISCLOSURE_ELEMENT_1 = "WyJ0ZXN0X3NhbHQiLCAidGVzdF9hcnJheV92YWx1ZV8xIl0"

    // ["test_salt", "test_array_value_2"]
    // HWWQ_E69DRWp7FhCHyQdS01ushRMA9GXJpzh5DosDHU
    const val DISCLOSURE_ELEMENT_2 = "WyJ0ZXN0X3NhbHQiLCAidGVzdF9hcnJheV92YWx1ZV8yIl0"

    const val JSON_WITH_OTHER_CLAIMS = """{"test_key_1":"test_value_1", "array_key":["test_array_value_1", "test_array_value_2"]}"""

    const val JSON_WITH_ARRAY_ONLY = """{"array_key":["test_array_value_1", "test_array_value_2"]}"""

    val SD_JWT_WITH_OTHER_CLAIMS = JWT_WITH_OTHER_CLAIMS + listOf(Disclosure1, DISCLOSURE_ELEMENT_2).toDisclosures()

    val SD_JWT_WITH_ARRAY_ONLY = JWT_WITH_ARRAY_ONLY + listOf(DISCLOSURE_ELEMENT_1, DISCLOSURE_ELEMENT_2).toDisclosures()
}
