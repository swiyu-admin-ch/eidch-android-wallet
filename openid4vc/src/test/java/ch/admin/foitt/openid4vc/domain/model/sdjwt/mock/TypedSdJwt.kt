package ch.admin.foitt.openid4vc.domain.model.sdjwt.mock

internal object TypedSdJwt {
    /*
{
   "_sd":[
      "shlXUWLol2Dqa6w-hNHnIeuEgPdB25svAe5-BnPT1a4",
      "8E9yFMJMl7WfdxTKPSRWHOfpirT-udb3r3rLCw8f7qc",
      "elB_obeWnlIBhWYILJTVZpbmTrAYwCTjPZa22VBgB70"
   ],
   "_sd_alg":"sha-256"
}
     */

    // ["salt_number", "key_number", 42]
    // shlXUWLol2Dqa6w-hNHnIeuEgPdB25svAe5-BnPT1a4
    private const val NUMBER_DISCLOSURE = "WyJzYWx0X251bWJlciIsICJrZXlfbnVtYmVyIiwgNDJd"

    // ["salt_boolean", "key_boolean", true]
    // 8E9yFMJMl7WfdxTKPSRWHOfpirT-udb3r3rLCw8f7qc
    private const val BOOLEAN_DISCLOSURE = "WyJzYWx0X2Jvb2xlYW4iLCAia2V5X2Jvb2xlYW4iLCB0cnVlXQ"

    // ["salt_null", "key_null", null]
    // elB_obeWnlIBhWYILJTVZpbmTrAYwCTjPZa22VBgB70
    private const val NULL_DISCLOSURE = "WyJzYWx0X251bGwiLCAia2V5X251bGwiLCBudWxsXQ"

    const val JSON = """{"key_number":42, "key_boolean":true, "key_null":null}"""

    const val JWT =
        "eyJhbGciOiJFUzUxMiIsInR5cCI6InR5cGVkIn0.eyJfc2QiOlsic2hsWFVXTG9sMkRxYTZ3LWhOSG5JZXVFZ1BkQjI1c3ZBZTUtQm5QVDFhNCIsIjhFOXlGTUpNbDdXZmR4VEtQU1JXSE9mcGlyVC11ZGIzcjNyTEN3OGY3cWMiLCJlbEJfb2JlV25sSUJoV1lJTEpUVlpwYm1UckFZd0NUalBaYTIyVkJnQjcwIl0sIl9zZF9hbGciOiJzaGEtMjU2In0.ALp-zljx5YKyMZRKXv_v39mz4gZ4Ft-dqikYEerEWX53ehz_g9oUin5--MAYG0FrnifcgIgdAvr3UTmOeRvJLBiLAMInlJAiOdkSvfctWpd9_-vaf6cUO9EMXqOkYcqbW60qEQoMyVGYqCBcdQTqUW4d0rtHe0hfFDMlGvAss_5oYHxq"

    val SD_JWT = JWT + listOf(NUMBER_DISCLOSURE, BOOLEAN_DISCLOSURE, NULL_DISCLOSURE).toDisclosures()
}
