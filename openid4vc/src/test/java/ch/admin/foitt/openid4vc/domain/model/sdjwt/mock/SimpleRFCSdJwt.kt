package ch.admin.foitt.openid4vc.domain.model.sdjwt.mock

/**
 * SD-JWT example can be found here: https://www.rfc-editor.org/rfc/rfc9901.html#name-simple-structured-sd-jwt
 */
internal object SimpleRFCSdJwt {
    /*
{
  "_sd": [
    "C9inp6YoRaEXR427zYJP7Qrk1WH_8bdwOA_YUrUnGQU",
    "Kuet1yAa0HIQvYnOVd59hcViO9Ug6J2kSfqYRBeowvE",
    "MMldOFFzB2d0umlmpTIaGerhWdU_PpYfLvKhh_f_9aY",
    "X6ZAYOII2vPN40V7xExZwVwz7yRmLNcVwt5DL8RLv4g",
    "Y34zmIo0QLLOtdMpXGwjBgLvr17yEhhYT0FGofR-aIE",
    "fyGp0WTwwPv2JDQln1lSiaeobZsMWA10bQ5989-9DTs",
    "ommFAicVT8LGHCB0uywx7fYuo3MHYKO15cz-RZEYM5Q",
    "s0BKYsLWxQQeU8tVlltM7MKsIRTrEIa1PkJmqxBBf5U"
  ],
  "iss": "https://issuer.example.com",
  "iat": 1683000000,
  "exp": 1883000000,
  "address": {
    "_sd": [
      "6aUhzYhZ7SJ1kVmagQAO3u2ETN2CC1aHheZpKnaF0_E",
      "AzLlFobkJ2xiaupREPyoJz-9-NSldB6Cgjr7fUyoHzg",
      "PzzcVu0qbMuBGSjulfewzkesD9zutOExn5EWNwkrQ-k",
      "b2Dkw0jcIF9rGg8_PF8ZcvncW7zwZj5ryBWvXfrpzek",
      "cPYJHIZ8Vu-f9CCyVub2UfgEk8jvvXezwK1p_JneeXQ",
      "glT3hrSU7fSWgwF5UDZmWwBTw32gnUldIhi8hGVCaV4",
      "rvJd6iq6T5ejmsBMoGwuNXh9qAAFATAci40oidEeVsA",
      "uNHoWYhXsZhVJCNE2Dqy-zqt7t69gJKy5QaFv7GrMX4"
    ]
  },
  "_sd_alg": "sha-256"
}
     */

    const val JSON = """
{
  "iss": "https://issuer.example.com",
  "exp": 1883000000,
  "sub": "6c5c0a49-b589-431d-bae7-219122a9ec2c",
  "given_name": "太郎",
  "family_name": "山田",
  "email": "\"unusual email address\"@example.jp",
  "phone_number": "+81-80-1234-5678",
  "address": {
    "street_address": "東京都港区芝公園４丁目２−８",
    "locality": "東京都",
    "region": "港区",
    "country": "JP"
  },
  "birthdate": "1940-01-01"
}
    """

    const val JWT =
        "eyJhbGciOiAiRVMyNTYiLCAidHlwIjogImV4YW1wbGUrc2Qtand0In0.eyJfc2QiOiBbIkM5aW5wNllvUmFFWFI0Mjd6WUpQN1FyazFXSF84YmR3T0FfWVVyVW5HUVUiLCAiS3VldDF5QWEwSElRdlluT1ZkNTloY1ZpTzlVZzZKMmtTZnFZUkJlb3d2RSIsICJNTWxkT0ZGekIyZDB1bWxtcFRJYUdlcmhXZFVfUHBZZkx2S2hoX2ZfOWFZIiwgIlg2WkFZT0lJMnZQTjQwVjd4RXhad1Z3ejd5Um1MTmNWd3Q1REw4Ukx2NGciLCAiWTM0em1JbzBRTExPdGRNcFhHd2pCZ0x2cjE3eUVoaFlUMEZHb2ZSLWFJRSIsICJmeUdwMFdUd3dQdjJKRFFsbjFsU2lhZW9iWnNNV0ExMGJRNTk4OS05RFRzIiwgIm9tbUZBaWNWVDhMR0hDQjB1eXd4N2ZZdW8zTUhZS08xNWN6LVJaRVlNNVEiLCAiczBCS1lzTFd4UVFlVTh0VmxsdE03TUtzSVJUckVJYTFQa0ptcXhCQmY1VSJdLCAiaXNzIjogImh0dHBzOi8vaXNzdWVyLmV4YW1wbGUuY29tIiwgImlhdCI6IDE2ODMwMDAwMDAsICJleHAiOiAxODgzMDAwMDAwLCAiYWRkcmVzcyI6IHsiX3NkIjogWyI2YVVoelloWjdTSjFrVm1hZ1FBTzN1MkVUTjJDQzFhSGhlWnBLbmFGMF9FIiwgIkF6TGxGb2JrSjJ4aWF1cFJFUHlvSnotOS1OU2xkQjZDZ2pyN2ZVeW9IemciLCAiUHp6Y1Z1MHFiTXVCR1NqdWxmZXd6a2VzRDl6dXRPRXhuNUVXTndrclEtayIsICJiMkRrdzBqY0lGOXJHZzhfUEY4WmN2bmNXN3p3Wmo1cnlCV3ZYZnJwemVrIiwgImNQWUpISVo4VnUtZjlDQ3lWdWIyVWZnRWs4anZ2WGV6d0sxcF9KbmVlWFEiLCAiZ2xUM2hyU1U3ZlNXZ3dGNVVEWm1Xd0JUdzMyZ25VbGRJaGk4aEdWQ2FWNCIsICJydkpkNmlxNlQ1ZWptc0JNb0d3dU5YaDlxQUFGQVRBY2k0MG9pZEVlVnNBIiwgInVOSG9XWWhYc1poVkpDTkUyRHF5LXpxdDd0NjlnSkt5NVFhRnY3R3JNWDQiXX0sICJfc2RfYWxnIjogInNoYS0yNTYifQ.EOZa2YqK8j4i7cqBDkfPcTMaFsgPwcx3aYJkFoMfvV46LxL-PPqrWsIyNukB4x8Y2LT31eIHDc4Wg4XNzaqu4w"
    val SD_JWT = JWT + listOf(
        "WyIyR0xDNDJzS1F2ZUNmR2ZyeU5STjl3IiwgInN1YiIsICI2YzVjMGE0OS1iNTg5LTQzMWQtYmFlNy0yMTkxMjJhOWVjMmMiXQ",
        "WyJlbHVWNU9nM2dTTklJOEVZbnN4QV9BIiwgImdpdmVuX25hbWUiLCAiXHU1OTJhXHU5MGNlIl0",
        "WyI2SWo3dE0tYTVpVlBHYm9TNXRtdlZBIiwgImZhbWlseV9uYW1lIiwgIlx1NWM3MVx1NzUzMCJd",
        "WyJlSThaV205UW5LUHBOUGVOZW5IZGhRIiwgImVtYWlsIiwgIlwidW51c3VhbCBlbWFpbCBhZGRyZXNzXCJAZXhhbXBsZS5qcCJd",
        "WyJRZ19PNjR6cUF4ZTQxMmExMDhpcm9BIiwgInBob25lX251bWJlciIsICIrODEtODAtMTIzNC01Njc4Il0",
        "WyJBSngtMDk1VlBycFR0TjRRTU9xUk9BIiwgInN0cmVldF9hZGRyZXNzIiwgIlx1Njc3MVx1NGVhY1x1OTBmZFx1NmUyZlx1NTMzYVx1ODI5ZFx1NTE2Y1x1NTcxMlx1ZmYxNFx1NGUwMVx1NzZlZVx1ZmYxMlx1MjIxMlx1ZmYxOCJd",
        "WyJQYzMzSk0yTGNoY1VfbEhnZ3ZfdWZRIiwgImxvY2FsaXR5IiwgIlx1Njc3MVx1NGVhY1x1OTBmZCJd",
        "WyJHMDJOU3JRZmpGWFE3SW8wOXN5YWpBIiwgInJlZ2lvbiIsICJcdTZlMmZcdTUzM2EiXQ",
        "WyJsa2x4RjVqTVlsR1RQVW92TU5JdkNBIiwgImNvdW50cnkiLCAiSlAiXQ",
        "WyJ5eXRWYmRBUEdjZ2wyckk0QzlHU29nIiwgImJpcnRoZGF0ZSIsICIxOTQwLTAxLTAxIl0",
    ).toDisclosures()
}
