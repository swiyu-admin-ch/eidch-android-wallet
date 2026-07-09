package ch.admin.foitt.openid4vc.domain.model.sdjwt.mock

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent
import ch.admin.foitt.openid4vc.domain.model.sdjwt.SdJwtDisclosure

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
    const val NUMBER_DISCLOSURE = "WyJzYWx0X251bWJlciIsICJrZXlfbnVtYmVyIiwgNDJd"

    // ["salt_boolean", "key_boolean", true]
    // 8E9yFMJMl7WfdxTKPSRWHOfpirT-udb3r3rLCw8f7qc
    const val BOOLEAN_DISCLOSURE = "WyJzYWx0X2Jvb2xlYW4iLCAia2V5X2Jvb2xlYW4iLCB0cnVlXQ"

    // ["salt_null", "key_null", null]
    // elB_obeWnlIBhWYILJTVZpbmTrAYwCTjPZa22VBgB70
    const val NULL_DISCLOSURE = "WyJzYWx0X251bGwiLCAia2V5X251bGwiLCBudWxsXQ"

    const val NUMBER_KEY = "key_number"
    const val NUMBER_VALUE = 42
    val NUMBER_PATH = listOf(ClaimsPathPointerComponent.String(NUMBER_KEY))
    val numberSdJwtDisclosure = SdJwtDisclosure(
        paths = listOf(NUMBER_PATH),
        disclosure = NUMBER_DISCLOSURE
    )

    const val BOOLEAN_KEY = "key_boolean"
    const val BOOLEAN_VALUE = true
    val BOOLEAN_PATH = listOf(ClaimsPathPointerComponent.String(BOOLEAN_KEY))
    val booleanSdJwtDisclosure = SdJwtDisclosure(
        paths = listOf(BOOLEAN_PATH),
        disclosure = BOOLEAN_DISCLOSURE
    )

    const val NULL_KEY = "key_null"
    val NULL_VALUE = null
    val NULL_PATH = listOf(ClaimsPathPointerComponent.String(NULL_KEY))
    val nullSdJwtDisclosure = SdJwtDisclosure(
        paths = listOf(NULL_PATH),
        disclosure = NULL_DISCLOSURE
    )

    val sdJwtDisclosures = setOf(
        numberSdJwtDisclosure,
        booleanSdJwtDisclosure,
        nullSdJwtDisclosure,
    )

    val JSON = """{"$NUMBER_KEY":$NUMBER_VALUE, "$BOOLEAN_KEY":$BOOLEAN_VALUE, "$NULL_KEY":$NULL_VALUE}"""

    const val JWT =
        "eyJhbGciOiJFUzUxMiIsInR5cCI6InR5cGVkIn0.eyJfc2QiOlsic2hsWFVXTG9sMkRxYTZ3LWhOSG5JZXVFZ1BkQjI1c3ZBZTUtQm5QVDFhNCIsIjhFOXlGTUpNbDdXZmR4VEtQU1JXSE9mcGlyVC11ZGIzcjNyTEN3OGY3cWMiLCJlbEJfb2JlV25sSUJoV1lJTEpUVlpwYm1UckFZd0NUalBaYTIyVkJnQjcwIl0sIl9zZF9hbGciOiJzaGEtMjU2In0.ALp-zljx5YKyMZRKXv_v39mz4gZ4Ft-dqikYEerEWX53ehz_g9oUin5--MAYG0FrnifcgIgdAvr3UTmOeRvJLBiLAMInlJAiOdkSvfctWpd9_-vaf6cUO9EMXqOkYcqbW60qEQoMyVGYqCBcdQTqUW4d0rtHe0hfFDMlGvAss_5oYHxq"

    val SD_JWT = JWT + listOf(NUMBER_DISCLOSURE, BOOLEAN_DISCLOSURE, NULL_DISCLOSURE).toDisclosures()
}
