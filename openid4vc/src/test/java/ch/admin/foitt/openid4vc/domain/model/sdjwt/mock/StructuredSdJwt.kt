package ch.admin.foitt.openid4vc.domain.model.sdjwt.mock

internal object StructuredSdJwt {
   /*   {
           "test":{
              "_sd":[
                 "YRLf606clwt4-hjyGze49ySFi6VCmwb9n5hwb4VUJSY",
                 "QhuvIMQd5LyX8gOR3weVzSY0yGZGGHdVXY0E-NhhUfw"
              ]
           },
           "_sd_alg":"sha-256"
        } */

    const val JSON = """{"test":{"test_key_1":"test_value_1", "test_key_2":"test_value_2"}}"""

    const val JWT =
        "eyJ0eXAiOiJzdHJ1Y3R1cmVkIiwiYWxnIjoiRVM1MTIifQ.eyJ0ZXN0Ijp7Il9zZCI6WyJZUkxmNjA2Y2x3dDQtaGp5R3plNDl5U0ZpNlZDbXdiOW41aHdiNFZVSlNZIiwiUWh1dklNUWQ1THlYOGdPUjN3ZVZ6U1kweUdaR0dIZFZYWTBFLU5oaFVmdyJdfSwiX3NkX2FsZyI6InNoYS0yNTYifQ.AUD8pZhe2-1sx5A0v0p8amxjkHPgKkzvC1QHfg3KR8LjS7ej8TQRcfKM7WzpA33lXrkAZh7DATRG4ICJao66HuvNAegi7QRff6-y9S1y5ZFZxNqzPci5kUA4Hy50RiquODRE6V3L95Fx2fk49iviZCiuiB9uMVOGXy_1z6MY9eEYHtl2"
}
