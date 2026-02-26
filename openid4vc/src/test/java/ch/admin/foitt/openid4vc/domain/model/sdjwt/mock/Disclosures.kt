package ch.admin.foitt.openid4vc.domain.model.sdjwt.mock

// ["test_salt_1", "test_key_1", "test_value_1"]
internal const val Disclosure1 = "WyJ0ZXN0X3NhbHRfMSIsICJ0ZXN0X2tleV8xIiwgInRlc3RfdmFsdWVfMSJd"

// ["test_salt_2", "test_key_2", "test_value_2"]
internal const val Disclosure2 = "WyJ0ZXN0X3NhbHRfMiIsICJ0ZXN0X2tleV8yIiwgInRlc3RfdmFsdWVfMiJd"

// ["test_salt_3", "test_key_3", "test_value_3"]
internal const val Disclosure3 = "WyJ0ZXN0X3NhbHRfMyIsICJ0ZXN0X2tleV8zIiwgInRlc3RfdmFsdWVfMyJd"

internal const val SdJwtSeparator = "~"

internal val FlatDisclosures = listOf(
    Disclosure1,
    Disclosure2,
    Disclosure3
).joinToString(prefix = SdJwtSeparator, separator = SdJwtSeparator, postfix = SdJwtSeparator)
