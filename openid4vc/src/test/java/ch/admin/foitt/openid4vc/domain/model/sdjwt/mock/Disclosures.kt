package ch.admin.foitt.openid4vc.domain.model.sdjwt.mock

// ["test_salt_1", "test_key_1", "test_value_1"]
// YRLf606clwt4-hjyGze49ySFi6VCmwb9n5hwb4VUJSY
internal const val Disclosure1 = "WyJ0ZXN0X3NhbHRfMSIsICJ0ZXN0X2tleV8xIiwgInRlc3RfdmFsdWVfMSJd"

// ["test_salt_2", "test_key_2", "test_value_2"]
// QhuvIMQd5LyX8gOR3weVzSY0yGZGGHdVXY0E-NhhUfw
internal const val Disclosure2 = "WyJ0ZXN0X3NhbHRfMiIsICJ0ZXN0X2tleV8yIiwgInRlc3RfdmFsdWVfMiJd"

// ["test_salt_3", "test_key_3", "test_value_3"]
// ql6yBMb-5Ql1gG833J1o3poFIDLVt9Ck79astQeVYb0
internal const val Disclosure3 = "WyJ0ZXN0X3NhbHRfMyIsICJ0ZXN0X2tleV8zIiwgInRlc3RfdmFsdWVfMyJd"

// ["test_salt_3", "_sd", "test_value"]
// 4RJqVG8THlVOjXG_ZUvmEukm_xpgXwqQmJxAhTjNNyQ
internal const val DisclosureSdKey = "WyJ0ZXN0X3NhbHRfMyIsICJfc2QiLCAidGVzdF92YWx1ZSJd"

// ["test_salt_3", "...", "test_value"]
// PTD0kXFEreE8xjSn6Wh2otra3xO_EBwnkJ1Tn2OtW9o
internal const val DisclosureArrayKey = "WyJ0ZXN0X3NhbHRfMyIsICIuLi4iLCAidGVzdF92YWx1ZSJd"

// ["test_salt_3", "_sd_alg", "test_value"]
// P7OEk6qsrIpfH7t7aUOBbXhvBaOT9jytW6GJUGmOgss
internal const val DisclosureAlgorithmKey = "WyJ0ZXN0X3NhbHRfMyIsICJfc2RfYWxnIiwgInRlc3RfdmFsdWUiXQ"

internal const val SdJwtSeparator = "~"

internal val StructuredDisclosures = listOf(
    Disclosure1,
    Disclosure2,
).toDisclosures()

internal val FlatDisclosures = listOf(
    Disclosure1,
    Disclosure2,
    Disclosure3,
).toDisclosures()

internal fun List<String>.toDisclosures() = this.joinToString(prefix = SdJwtSeparator, separator = SdJwtSeparator, postfix = SdJwtSeparator)
