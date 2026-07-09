package ch.admin.foitt.openid4vc.domain.model.sdjwt.mock

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent
import ch.admin.foitt.openid4vc.domain.model.sdjwt.SdJwtDisclosure

internal object FlatObjectArraySdJwt {
    /*
{
   "array_key":[
      {
         "test_key_1":"test_value_1",
         "test_key_2":"test_value_2"
      },
      {
         "...":"V4cLLcpCfh_E_dvB3bgSGJCVwwKYclks5CNtApcwZEg"
      }
   ],
   "_sd_alg":"sha-256"
}
     */
    const val JWT_WITH_ONE_DISCLOSED_ELEMENT =
        "eyJ0eXAiOiJmbGF0T2JqZWN0QXJyYXkiLCJhbGciOiJFUzUxMiJ9.eyJhcnJheV9rZXkiOlt7InRlc3Rfa2V5XzEiOiJ0ZXN0X3ZhbHVlXzEiLCJ0ZXN0X2tleV8yIjoidGVzdF92YWx1ZV8yIn0seyIuLi4iOiJWNGNMTGNwQ2ZoX0VfZHZCM2JnU0dKQ1Z3d0tZY2xrczVDTnRBcGN3WkVnIn1dLCJfc2RfYWxnIjoic2hhLTI1NiJ9.AKd8UH-guoIeCC4S8bUlROeTk49y82vME0foiBoPaLFB7s72Wn3_W1essa94H1FXgxNxaqACpHWWLnrJd_sHQFcIAQkS4P3gzy-mc_ebV1Wn5qNhwWYZLqA39MBy3TGyg8RWpuaR_HZ3X1lGs8Gh_FdR_wfLsU6MvxB7wffDf-C7t8N-"

    /*
{
   "array_key":[
      {
         "...":"6KyeusuhLExIaIKOx2ejZE9776pTDf1cQjnraJfAV94"
      },
      {
         "...":"V4cLLcpCfh_E_dvB3bgSGJCVwwKYclks5CNtApcwZEg"
      }
   ],
   "_sd_alg":"sha-256"
}
     */
    const val JWT_WITH_DISCLOSED_ELEMENTS_ONLY =
        "eyJ0eXAiOiJmbGF0T2JqZWN0QXJyYXkiLCJhbGciOiJFUzUxMiJ9.eyJhcnJheV9rZXkiOlt7Ii4uLiI6IjZLeWV1c3VoTEV4SWFJS094MmVqWkU5Nzc2cFREZjFjUWpucmFKZkFWOTQifSx7Ii4uLiI6IlY0Y0xMY3BDZmhfRV9kdkIzYmdTR0pDVnd3S1ljbGtzNUNOdEFwY3daRWcifV0sIl9zZF9hbGciOiJzaGEtMjU2In0.ALoF8WV5SShn3mfHnJWOptsULkIO_RgTm9PUux7rbJdf0gfGJcp9QgORaSsOvUwZxr33YRPsBiLCTddbEPd_co4bALM2PoWzoTaUnOalTl5fUvQCyx5KGbkRXAZfBclsua4fc_ocbbpXQtqKQDeo1SqWMWLR2FG96zN2TF4OJsqDAvUm"
    const val ARRAY_KEY = "array_key"
    const val TEST_KEY_1 = "test_key_1"
    const val TEST_KEY_2 = "test_key_2"

    // ["test_salt_1", {"test_key_1":"test_value_1", "test_key_2":"test_value_2"}]
    // 6KyeusuhLExIaIKOx2ejZE9776pTDf1cQjnraJfAV94
    const val DISCLOSURE_ELEMENT_1 = "WyJ0ZXN0X3NhbHRfMSIsIHsidGVzdF9rZXlfMSI6InRlc3RfdmFsdWVfMSIsICJ0ZXN0X2tleV8yIjoidGVzdF92YWx1ZV8yIn1d"

    // ["test_salt_2", {"test_key_1":"test_value_3", "test_key_2":"test_value_4"}]
    // V4cLLcpCfh_E_dvB3bgSGJCVwwKYclks5CNtApcwZEg
    const val DISCLOSURE_ELEMENT_2 = "WyJ0ZXN0X3NhbHRfMiIsIHsidGVzdF9rZXlfMSI6InRlc3RfdmFsdWVfMyIsICJ0ZXN0X2tleV8yIjoidGVzdF92YWx1ZV80In1d"

    val ARRAY_VALUE_1_PATH = listOf(
        ClaimsPathPointerComponent.String(ARRAY_KEY),
        ClaimsPathPointerComponent.Index(0),
    )
    val ARRAY_VALUE_1_ELEMENT_1_PATH = listOf(
        ClaimsPathPointerComponent.String(ARRAY_KEY),
        ClaimsPathPointerComponent.Index(0),
        ClaimsPathPointerComponent.String(TEST_KEY_1),
    )
    val ARRAY_VALUE_1_ELEMENT_2_PATH = listOf(
        ClaimsPathPointerComponent.String(ARRAY_KEY),
        ClaimsPathPointerComponent.Index(0),
        ClaimsPathPointerComponent.String(TEST_KEY_2),
    )
    val sdJwtArrayDisclosure1 = SdJwtDisclosure(
        paths = listOf(ARRAY_VALUE_1_ELEMENT_1_PATH, ARRAY_VALUE_1_ELEMENT_2_PATH, ARRAY_VALUE_1_PATH),
        disclosure = DISCLOSURE_ELEMENT_1
    )
    val ARRAY_VALUE_2_PATH = listOf(
        ClaimsPathPointerComponent.String(ARRAY_KEY),
        ClaimsPathPointerComponent.Index(1),
    )
    val ARRAY_VALUE_2_ELEMENT_1_PATH = listOf(
        ClaimsPathPointerComponent.String(ARRAY_KEY),
        ClaimsPathPointerComponent.Index(1),
        ClaimsPathPointerComponent.String(TEST_KEY_1),
    )
    val ARRAY_VALUE_2_ELEMENT_2_PATH = listOf(
        ClaimsPathPointerComponent.String(ARRAY_KEY),
        ClaimsPathPointerComponent.Index(1),
        ClaimsPathPointerComponent.String(TEST_KEY_2),
    )
    val sdJwtArrayDisclosure2 = SdJwtDisclosure(
        paths = listOf(ARRAY_VALUE_2_ELEMENT_1_PATH, ARRAY_VALUE_2_ELEMENT_2_PATH, ARRAY_VALUE_2_PATH),
        disclosure = DISCLOSURE_ELEMENT_2
    )

    val sdJwtDisclosures = setOf(sdJwtArrayDisclosure2)
    val sdJwtDisclosuresObjectArray = setOf(
        sdJwtArrayDisclosure1,
        sdJwtArrayDisclosure2,
    )

    const val JSON = """{"$ARRAY_KEY":[{"$TEST_KEY_1": "test_value_1", "$TEST_KEY_2": "test_value_2"}, {"$TEST_KEY_1": "test_value_3", "$TEST_KEY_2": "test_value_4"}]}"""

    val SD_JWT_WITH_ONE_DISCLOSED_ELEMENT = JWT_WITH_ONE_DISCLOSED_ELEMENT + listOf(DISCLOSURE_ELEMENT_2).toDisclosures()

    val SD_JWT_WITH_DISCLOSED_ELEMENTS_ONLY = JWT_WITH_DISCLOSED_ELEMENTS_ONLY + listOf(DISCLOSURE_ELEMENT_1, DISCLOSURE_ELEMENT_2).toDisclosures()
}
