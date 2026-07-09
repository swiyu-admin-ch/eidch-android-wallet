package ch.admin.foitt.openid4vc.domain.model.sdjwt.mock

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent
import ch.admin.foitt.openid4vc.domain.model.sdjwt.SdJwtDisclosure
import ch.admin.foitt.openid4vc.domain.model.sdjwt.util.assertSelectiveDisclosure

internal object FlatSdJwt {
    /*
{
   "_sd":[
      "YRLf606clwt4-hjyGze49ySFi6VCmwb9n5hwb4VUJSY",
      "QhuvIMQd5LyX8gOR3weVzSY0yGZGGHdVXY0E-NhhUfw",
      "ql6yBMb-5Ql1gG833J1o3poFIDLVt9Ck79astQeVYb0"
   ],
   "_sd_alg":"sha-256"
}
     */

    const val KEY_1 = "test_key_1"
    const val VALUE_1 = "test_value_1"
    val path1 = listOf(ClaimsPathPointerComponent.String(KEY_1))
    val sdJwtDisclosure1 = SdJwtDisclosure(
        paths = listOf(path1),
        disclosure = Disclosure1
    )
    const val KEY_2 = "test_key_2"
    const val VALUE_2 = "test_value_2"
    val path2 = listOf(ClaimsPathPointerComponent.String(KEY_2))
    val sdJwtDisclosure2 = SdJwtDisclosure(
        paths = listOf(path2),
        disclosure = Disclosure2
    )
    const val KEY_3 = "test_key_3"
    const val VALUE_3 = "test_value_3"
    val path3 = listOf(ClaimsPathPointerComponent.String(KEY_3))
    val sdJwtDisclosure3 = SdJwtDisclosure(
        paths = listOf(path3),
        disclosure = Disclosure3
    )

    val sdJwtDisclosures = setOf(
        sdJwtDisclosure1,
        sdJwtDisclosure2,
        sdJwtDisclosure3,
    )

    val requestedPathAll = listOf(path1, path2, path3)
    val requestedPathPartial = listOf(path1, path3)
    val requestedPathEmpty = listOf<ClaimsPathPointer>()
    val requestedPathOther = listOf(
        listOf(ClaimsPathPointerComponent.String("otherKey")),
        listOf(ClaimsPathPointerComponent.String("otherKey2")),
    )

    const val JSON = """{"$KEY_1":"$VALUE_1", "$KEY_2":"$VALUE_2", "$KEY_3":"$VALUE_3"}"""

    const val JWT =
        "eyJhbGciOiJFUzUxMiIsInR5cCI6ImZsYXQifQ.eyJfc2QiOlsiWVJMZjYwNmNsd3Q0LWhqeUd6ZTQ5eVNGaTZWQ213YjluNWh3YjRWVUpTWSIsIlFodXZJTVFkNUx5WDhnT1Izd2VWelNZMHlHWkdHSGRWWFkwRS1OaGhVZnciLCJxbDZ5Qk1iLTVRbDFnRzgzM0oxbzNwb0ZJRExWdDlDazc5YXN0UWVWWWIwIl0sIl9zZF9hbGciOiJzaGEtMjU2In0.ANplFuJmpC3Ys7mlCxRpOtqZK45eK4Tp7UEL4o2Ng2otUcNhUi3816Jp85kAflt6y0hIw8QXTRElHxzQKDKPcAhcAYPmekYTtHJOPRJQYYW0O9YULbxHjqk4rhKBehwkmhMKnVEmOgPcu0wMjdTyzDyDrUMd5UYf-MnzVeS8yopmcn5w"

    fun assertWhole(selectiveDisclosure: String) {
        assertSelectiveDisclosure(
            selectiveDisclosure = selectiveDisclosure,
            jwt = JWT,
            disclosures = listOf(
                Disclosure1,
                Disclosure2,
                Disclosure3,
            )
        )
    }

    fun assertPartial(selectiveDisclosure: String) {
        assertSelectiveDisclosure(
            selectiveDisclosure = selectiveDisclosure,
            jwt = JWT,
            disclosures = listOf(
                Disclosure1,
                Disclosure3,
            )
        )
    }
}
