package ch.admin.foitt.openid4vc.domain.model.sdjwt.mock

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
    const val KEY_2 = "test_key_2"
    const val KEY_3 = "test_key_3"

    const val JSON = """{"$KEY_1":"test_value_1", "$KEY_2":"test_value_2", "$KEY_3":"test_value_3"}"""

    const val JWT =
        "eyJhbGciOiJFUzUxMiIsInR5cCI6ImZsYXQifQ.eyJfc2QiOlsiWVJMZjYwNmNsd3Q0LWhqeUd6ZTQ5eVNGaTZWQ213YjluNWh3YjRWVUpTWSIsIlFodXZJTVFkNUx5WDhnT1Izd2VWelNZMHlHWkdHSGRWWFkwRS1OaGhVZnciLCJxbDZ5Qk1iLTVRbDFnRzgzM0oxbzNwb0ZJRExWdDlDazc5YXN0UWVWWWIwIl0sIl9zZF9hbGciOiJzaGEtMjU2In0.ANplFuJmpC3Ys7mlCxRpOtqZK45eK4Tp7UEL4o2Ng2otUcNhUi3816Jp85kAflt6y0hIw8QXTRElHxzQKDKPcAhcAYPmekYTtHJOPRJQYYW0O9YULbxHjqk4rhKBehwkmhMKnVEmOgPcu0wMjdTyzDyDrUMd5UYf-MnzVeS8yopmcn5w"

    const val JWT384 =
        "eyJhbGciOiJFUzUxMiIsInR5cCI6ImZsYXQifQ.eyJfc2QiOlsib3VMV3pzSC0td1lOWFZCMXFQRGozLU1Ma21JMEp3Yk52bXV6ejFSSE16Y0Y0dXQ1UDAzd2F1WUNxdEVieW5vdSIsIkNfd2hDRVFHY2Y5M19yVmFYZ1I0Ulp1dWpDWDBCOW05ZzBFQTUyU2VKUjZBbTc0cGYzT193elV6alNDSS0wdmEiLCJ2MDNlOEdSU3AwLU01YklGRWEzSTJ6cFVpUTYxVXEwTHBMVU9mdFd0MENHb0tsUnRzSkUyYjIwa0IzVFExQV9zIl0sIl9zZF9hbGciOiJzaGEtMzg0In0.Acv6TXARNRkyxxvkhkYtgDgsFUdxMsQ-_zAxIMHjAWAMAhh4hEDk52a0vKu2ZU5ZDK_zppQWlGMyN_hParB0dfLTAFVC94yO5jlAeA72BlSlyZUHci_gWSchW0S6GF2Vn938u8YWvUwH5RTqOEDPau7R7iNUcttKtI1yw1IagfkhLlHX"

    const val JWT512 =
        "eyJhbGciOiJFUzUxMiIsInR5cCI6ImZsYXQifQ.eyJfc2QiOlsiaC1ocVpCS3FKTE9jU3JEampZejh2ajM0eDljTHJFZzNERHY3ZGtGczNDUDBPZ3RtVS1jcGtJbkNPYWE0VFNBT296eXM0TFVvdXctalBtTkstM0t6bFEiLCJnWVpwWGdGdE9xMXRGTTVRaktCWW9jb1hMdDA3cmhHU0RoWWJWZkM1UmF2dUJ6aVdnUGRJYkUwc1pkclBtLXAzcmhBMmlkMVFodUJnWFE4N1pjV1dMZyIsIlBuX2xNR2FlN1k0VHRJeEcwdXZITDZLa0hrY2lwVkxHS2d3Yl96aXVPTU1oOE9JSXdjdHZHM1NXU0JaWXNJOGpESXJhenFoWUNyVUl3YWRZcXlvemlBIl0sIl9zZF9hbGciOiJzaGEtNTEyIn0.ASVFjHR64yl44JtgGNEe_wK6vnZ2fHpCgQIlCzyG5dIUYX7OqKIVPasQpTion9pFadiwNrEuXeVttnJwYpSra7s8AA8zYU-1q_j7kcw3DxjhJ_FnUG7VADgEgu4zsOK7yUoyXzAMeCeW-CgtI6MNX-LU0rrwgWixioCtKPg2bljdymSS"
}
