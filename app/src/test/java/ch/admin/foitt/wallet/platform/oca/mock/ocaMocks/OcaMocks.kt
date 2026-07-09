package ch.admin.foitt.wallet.platform.oca.mock.ocaMocks

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.toPointerString
import ch.admin.foitt.wallet.platform.oca.domain.model.AttributeType
import ch.admin.foitt.wallet.platform.oca.domain.model.CaptureBase1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaClaimData
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.BrandingOverlay1x1
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.CharacterEncodingOverlay1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.DataSourceOverlay2x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.EntryOverlay1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.FormatOverlay1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.LabelOverlay1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.MetaOverlay1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.OrderOverlay1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.SensitiveOverlay1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.StandardOverlay1x0
import ch.admin.foitt.wallet.platform.oca.util.createClaimsPathPointer

object OcaMocks {
    val ocaResponse = """
        {
            "oca": "displayData"
        }
    """.trimIndent()

    const val DIGEST = "digest"
    const val LANGUAGE_EN = "en"
    const val LANGUAGE_DE = "de"
    const val ATTRIBUTE_KEY_FIRSTNAME = "firstname"
    const val ATTRIBUTE_LABEL_FIRSTNAME_EN = "Firstname"
    const val ATTRIBUTE_LABEL_FIRSTNAME_DE = "Vorname"
    const val ATTRIBUTE_KEY_AGE = "age"
    const val ATTRIBUTE_LABEL_AGE_EN = "Age"
    const val ATTRIBUTE_LABEL_AGE_DE = "Alter"
    const val CREDENTIAL_FORMAT = "vc+sd-jwt"
    const val JSON_PATH_FIRSTNAME = "$.firstname"
    const val JSON_PATH_AGE = "$.age"
    val CLAIMS_PATH_POINTER_FIRSTNAME = createClaimsPathPointer("firstname")
    val CLAIMS_PATH_POINTER_STRING_FIRSTNAME = CLAIMS_PATH_POINTER_FIRSTNAME.toPointerString()
    val CLAIMS_PATH_POINTER_AGE = createClaimsPathPointer("age")
    private const val VALID_DIGEST_HUMAN = "IDif6Jd863C_YYjp1cHFCTAUr1_TzZSS1l-pv21Q56qs"
    const val ATTRIBUTE_KEY_LASTNAME = "lastname"
    const val ATTRIBUTE_KEY_ADDRESS_STREET = "address_street"
    private const val ATTRIBUTE_KEY_ADDRESS_CITY = "address_city"
    private const val ATTRIBUTE_KEY_ADDRESS_COUNTRY = "address_country"
    private const val ATTRIBUTE_KEY_PETS = "pets"
    private const val VALID_DIGEST_PET = "IKLvtGx1NU0007DUTTmI_6Zw-hnGRFicZ5R4vAxg4j2j"
    const val ATTRIBUTE_KEY_NAME = "name"
    const val ATTRIBUTE_KEY_RACE = "race"
    val CLAIMS_PATH_POINTER_LASTNAME = createClaimsPathPointer("lastname")
    val CLAIMS_PATH_POINTER_STRING_LASTNAME = CLAIMS_PATH_POINTER_LASTNAME.toPointerString()
    val CLAIMS_PATH_POINTER_ADDRESS_STREET = createClaimsPathPointer("address", "street")
    val CLAIMS_PATH_POINTER_STRING_ADDRESS_STREET = CLAIMS_PATH_POINTER_ADDRESS_STREET.toPointerString()
    val CLAIMS_PATH_POINTER_ADDRESS_CITY = createClaimsPathPointer("address", "city")
    val CLAIMS_PATH_POINTER_STRING_ADDRESS_CITY = CLAIMS_PATH_POINTER_ADDRESS_CITY.toPointerString()
    val CLAIMS_PATH_POINTER_ADDRESS_COUNTRY = createClaimsPathPointer("address", "country")
    val CLAIMS_PATH_POINTER_STRING_ADDRESS_COUNTRY = CLAIMS_PATH_POINTER_ADDRESS_COUNTRY.toPointerString()
    val CLAIMS_PATH_POINTER_PETS = createClaimsPathPointer("pets")
    val CLAIMS_PATH_POINTER_STRING_PETS = CLAIMS_PATH_POINTER_PETS.toPointerString()
    val CLAIMS_PATH_POINTER_PETS_NAME = createClaimsPathPointer("pets", null, "name")
    val CLAIMS_PATH_POINTER_STRING_PETS_NAME = CLAIMS_PATH_POINTER_PETS_NAME.toPointerString()
    val CLAIMS_PATH_POINTER_PETS_RACE = createClaimsPathPointer("pets", null, "race")
    val CLAIMS_PATH_POINTER_STRING_PETS_RACE = CLAIMS_PATH_POINTER_PETS_RACE.toPointerString()
    private const val LOGO =
        "data:image/jpeg;base64,iVBORw0KGgoAAAANSUhEUgAAABoAAAAaCAYAAACpSkzOAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAGbSURBVHgBvVaBUcMwDFQ4BjAbmA06QjYgGzQbkA1aJmg3yHWCwgSGCVImcJkg3UDYjVMUI6VxW/g7X+701kuWZTsAI0DEwo0GO9RuKMKpYGvdsH4uXALnqPE3DOFrhs8hFc5pizxyN2YCZyS9+5FYuWCfgYzZJYFUon2UuwMZ+xG7xO3gXKBQ95xwGyHIuxuvbhwY/oPo+WbSQAyKtDCGVtWM3aMifmXENcGnb3uqp6Q2NZHgEpnWDQl5rsJwxgS9NTBZ98ghEdgdcA6t3yOpJQtGSIUVekHN+DwJWsfSWSGLmsm2xWHti2iOEbQav6I3IYtPIqDdZwvDc3K0OY5W5EvQ2vUb2jJZaBLIogxL5pUcf9LC7gzZQPigJXFe4HmsyPw1sRvk9jKsjj4Fc5yOOfFTyDcLcEGfMR0VpACnlUvC4j+C9FjFulkURLuPhdvgIcuy08UbPxMabofBjRMHOsAfIS6db21fOgXXYe/K9kgNgxWFmr7A9dhMmoXD001h8OcvSPpLWkIKsLu3THC2yBxG7B48S5IoJb1vHubbPPxs2qsAAAAASUVORK5CYII="
    const val FORMAT = "utf-8"
    private const val STANDARD = "urn:ietf:rfc:2397"
    const val BASE64_ENCODING = "base64"
    const val UNKNOWN_ENCODING = "default encoding"
    const val META_NAME = "name"
    const val META_DESCRIPTION = "description"
    const val BRANDING_LIGHT_THEME = "light"
    const val BRANDING_DARK_THEME = "dark"
    const val BRANDING_LOGO = "logo"
    const val BRANDING_BACKGROUND_COLOR = "background color"
    const val BRANDING_PRIMARY_FIELD = "primary field"

    const val ENTRY_CODE_A = "a"
    const val ENTRY_CODE_B = "b"

    const val ENTRY_CODE_A_EN = "a en"
    const val ENTRY_CODE_A_DE = "a de"
    const val ENTRY_CODE_B_EN = "b en"
    const val ENTRY_CODE_B_DE = "b de"

    val simpleCaptureBase = CaptureBase1x0(
        digest = DIGEST,
        attributes = mapOf(
            ATTRIBUTE_KEY_FIRSTNAME to AttributeType.Text,
            ATTRIBUTE_KEY_AGE to AttributeType.Numeric,
        )
    )

    val sensitiveCaptureBase = CaptureBase1x0(
        digest = DIGEST,
        attributes = mapOf(
            ATTRIBUTE_KEY_PETS to AttributeType.Text,
            ATTRIBUTE_KEY_FIRSTNAME to AttributeType.Binary,
            ATTRIBUTE_KEY_AGE to AttributeType.Boolean,
            ATTRIBUTE_KEY_NAME to AttributeType.DateTime,
            ATTRIBUTE_KEY_RACE to AttributeType.Numeric,
            ATTRIBUTE_KEY_LASTNAME to AttributeType.Text,
            ATTRIBUTE_KEY_ADDRESS_CITY to AttributeType.Text,
        )
    )

    val ocaSimpleLabel = OcaBundle(
        captureBases = listOf(simpleCaptureBase),
        overlays = listOf(
            LabelOverlay1x0(
                captureBaseDigest = DIGEST,
                language = LANGUAGE_EN,
                attributeLabels = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to ATTRIBUTE_LABEL_FIRSTNAME_EN,
                    ATTRIBUTE_KEY_AGE to ATTRIBUTE_LABEL_AGE_EN,
                )
            ),
            LabelOverlay1x0(
                captureBaseDigest = DIGEST,
                language = LANGUAGE_DE,
                attributeLabels = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to ATTRIBUTE_LABEL_FIRSTNAME_DE,
                    ATTRIBUTE_KEY_AGE to ATTRIBUTE_LABEL_AGE_DE,
                )
            )
        )
    )

    val ocaSimpleDataSourceV2 = OcaBundle(
        captureBases = listOf(simpleCaptureBase),
        overlays = listOf(
            DataSourceOverlay2x0(
                captureBaseDigest = DIGEST,
                format = CREDENTIAL_FORMAT,
                attributeSources = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to CLAIMS_PATH_POINTER_FIRSTNAME,
                    ATTRIBUTE_KEY_AGE to CLAIMS_PATH_POINTER_AGE,
                )
            )
        )
    )

    val ocaSimpleDataSourceMultiVersion = OcaBundle(
        captureBases = listOf(simpleCaptureBase),
        overlays = listOf(
            DataSourceOverlay2x0(
                captureBaseDigest = DIGEST,
                format = CREDENTIAL_FORMAT,
                attributeSources = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to CLAIMS_PATH_POINTER_FIRSTNAME,
                    ATTRIBUTE_KEY_AGE to CLAIMS_PATH_POINTER_AGE,
                )
            )
        )
    )

    val ocaSimpleFormat = OcaBundle(
        captureBases = listOf(simpleCaptureBase),
        overlays = listOf(
            FormatOverlay1x0(
                captureBaseDigest = DIGEST,
                attributeFormats = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to FORMAT
                )
            )
        )
    )

    val ocaSimpleMeta = OcaBundle(
        captureBases = listOf(simpleCaptureBase),
        overlays = listOf(
            MetaOverlay1x0(
                captureBaseDigest = DIGEST,
                language = LANGUAGE_EN,
                name = META_NAME,
                description = META_DESCRIPTION
            )
        )
    )

    val ocaSimpleBranding = OcaBundle(
        captureBases = listOf(simpleCaptureBase),
        overlays = listOf(
            BrandingOverlay1x1(
                captureBaseDigest = DIGEST,
                language = LANGUAGE_EN,
                theme = BRANDING_LIGHT_THEME,
                logo = BRANDING_LOGO,
                primaryBackgroundColor = BRANDING_BACKGROUND_COLOR,
                primaryField = BRANDING_PRIMARY_FIELD,
            )
        )
    )

    val ocaSimpleStandard = OcaBundle(
        captureBases = listOf(simpleCaptureBase),
        overlays = listOf(
            StandardOverlay1x0(
                captureBaseDigest = DIGEST,
                attributeStandards = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to STANDARD
                )
            )
        )
    )

    val ocaSimpleEncoding = OcaBundle(
        captureBases = listOf(simpleCaptureBase),
        overlays = listOf(
            CharacterEncodingOverlay1x0(
                captureBaseDigest = DIGEST,
                defaultCharacterEncoding = UNKNOWN_ENCODING,
                attributeCharacterEncoding = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to BASE64_ENCODING
                )
            )
        )
    )

    val ocaSimpleEncodingNoDefault = OcaBundle(
        captureBases = listOf(simpleCaptureBase),
        overlays = listOf(
            CharacterEncodingOverlay1x0(
                captureBaseDigest = DIGEST,
                attributeCharacterEncoding = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to BASE64_ENCODING
                )
            )
        )
    )

    val ocaSimpleOrder = OcaBundle(
        captureBases = listOf(simpleCaptureBase),
        overlays = listOf(
            OrderOverlay1x0(
                captureBaseDigest = DIGEST,
                attributeOrders = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to 2,
                    ATTRIBUTE_KEY_AGE to 1,
                )
            )
        )
    )

    val ocaSimpleEntry = OcaBundle(
        captureBases = listOf(simpleCaptureBase),
        overlays = listOf(
            EntryOverlay1x0(
                captureBaseDigest = DIGEST,
                language = LANGUAGE_EN,
                attributeEntries = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to mapOf(ENTRY_CODE_A to ENTRY_CODE_A_EN),
                    ATTRIBUTE_KEY_AGE to mapOf(ENTRY_CODE_B to ENTRY_CODE_B_EN),
                )
            ),
            EntryOverlay1x0(
                captureBaseDigest = DIGEST,
                language = LANGUAGE_DE,
                attributeEntries = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to mapOf(ENTRY_CODE_A to ENTRY_CODE_A_DE),
                    ATTRIBUTE_KEY_AGE to mapOf(ENTRY_CODE_B to ENTRY_CODE_B_DE),
                )
            )
        )
    )

    val ocaSensitiveEntry = OcaBundle(
        captureBases = listOf(sensitiveCaptureBase),
        overlays = listOf(
            SensitiveOverlay1x0(
                captureBaseDigest = DIGEST,
                attributes = listOf(
                    ATTRIBUTE_KEY_FIRSTNAME,
                    ATTRIBUTE_KEY_AGE,
                    ATTRIBUTE_KEY_NAME,
                    ATTRIBUTE_KEY_RACE,
                    ATTRIBUTE_KEY_LASTNAME
                )
            ),
        )
    )

    val ocaSimple = OcaBundle(
        captureBases = listOf(simpleCaptureBase),
        overlays = listOf(
            LabelOverlay1x0(
                captureBaseDigest = DIGEST,
                language = LANGUAGE_EN,
                attributeLabels = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to ATTRIBUTE_LABEL_FIRSTNAME_EN,
                    ATTRIBUTE_KEY_AGE to ATTRIBUTE_LABEL_AGE_EN,
                )
            ),
            LabelOverlay1x0(
                captureBaseDigest = DIGEST,
                language = LANGUAGE_DE,
                attributeLabels = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to ATTRIBUTE_LABEL_FIRSTNAME_DE,
                    ATTRIBUTE_KEY_AGE to ATTRIBUTE_LABEL_AGE_DE,
                )
            ),
            DataSourceOverlay2x0(
                captureBaseDigest = DIGEST,
                format = CREDENTIAL_FORMAT,
                attributeSources = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to CLAIMS_PATH_POINTER_FIRSTNAME,
                    ATTRIBUTE_KEY_AGE to CLAIMS_PATH_POINTER_AGE,
                )
            )
        ),
        ocaClaimData = listOf(
            OcaClaimData(
                attributeType = AttributeType.Text,
                captureBaseDigest = DIGEST,
                flagged = false,
                name = ATTRIBUTE_KEY_FIRSTNAME,
                characterEncoding = null,
                dataSources = mapOf(
                    CREDENTIAL_FORMAT to CLAIMS_PATH_POINTER_FIRSTNAME
                ),
                entryMappings = emptyMap(),
                format = null,
                labels = mapOf(
                    LANGUAGE_EN to ATTRIBUTE_LABEL_FIRSTNAME_EN,
                    LANGUAGE_DE to ATTRIBUTE_LABEL_FIRSTNAME_DE
                ),
                standard = null,
                isSensitive = false
            ),
            OcaClaimData(
                attributeType = AttributeType.Numeric,
                captureBaseDigest = DIGEST,
                flagged = false,
                name = ATTRIBUTE_KEY_AGE,
                characterEncoding = null,
                dataSources = mapOf(
                    CREDENTIAL_FORMAT to CLAIMS_PATH_POINTER_AGE
                ),
                entryMappings = emptyMap(),
                format = null,
                labels = mapOf(
                    LANGUAGE_EN to ATTRIBUTE_LABEL_AGE_EN,
                    LANGUAGE_DE to ATTRIBUTE_LABEL_AGE_DE
                ),
                standard = null,
                isSensitive = false
            ),
        )
    )

    val ocaSimpleClaimsPathPointer = OcaBundle(
        captureBases = listOf(simpleCaptureBase),
        overlays = listOf(
            LabelOverlay1x0(
                captureBaseDigest = DIGEST,
                language = LANGUAGE_EN,
                attributeLabels = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to ATTRIBUTE_LABEL_FIRSTNAME_EN,
                    ATTRIBUTE_KEY_AGE to ATTRIBUTE_LABEL_AGE_EN,
                )
            ),
            LabelOverlay1x0(
                captureBaseDigest = DIGEST,
                language = LANGUAGE_DE,
                attributeLabels = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to ATTRIBUTE_LABEL_FIRSTNAME_DE,
                    ATTRIBUTE_KEY_AGE to ATTRIBUTE_LABEL_AGE_DE,
                )
            ),
            DataSourceOverlay2x0(
                captureBaseDigest = DIGEST,
                format = CREDENTIAL_FORMAT,
                attributeSources = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to CLAIMS_PATH_POINTER_FIRSTNAME,
                    ATTRIBUTE_KEY_AGE to CLAIMS_PATH_POINTER_AGE,
                )
            )
        ),
        ocaClaimData = listOf(
            OcaClaimData(
                attributeType = AttributeType.Text,
                captureBaseDigest = DIGEST,
                flagged = false,
                name = ATTRIBUTE_KEY_FIRSTNAME,
                characterEncoding = null,
                dataSources = mapOf(
                    CREDENTIAL_FORMAT to CLAIMS_PATH_POINTER_FIRSTNAME
                ),
                entryMappings = emptyMap(),
                format = null,
                labels = mapOf(
                    LANGUAGE_EN to ATTRIBUTE_LABEL_FIRSTNAME_EN,
                    LANGUAGE_DE to ATTRIBUTE_LABEL_FIRSTNAME_DE
                ),
                standard = null,
                isSensitive = false
            ),
            OcaClaimData(
                attributeType = AttributeType.Numeric,
                captureBaseDigest = DIGEST,
                flagged = false,
                name = ATTRIBUTE_KEY_AGE,
                characterEncoding = null,
                dataSources = mapOf(
                    CREDENTIAL_FORMAT to CLAIMS_PATH_POINTER_AGE
                ),
                entryMappings = emptyMap(),
                format = null,
                labels = mapOf(
                    LANGUAGE_EN to ATTRIBUTE_LABEL_AGE_EN,
                    LANGUAGE_DE to ATTRIBUTE_LABEL_AGE_DE
                ),
                standard = null,
                isSensitive = false
            ),
        )
    )

    val ocaNested = OcaBundle(
        captureBases = listOf(
            CaptureBase1x0(
                digest = VALID_DIGEST_HUMAN,
                attributes = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to AttributeType.Text,
                    ATTRIBUTE_KEY_LASTNAME to AttributeType.Text,
                    ATTRIBUTE_KEY_ADDRESS_STREET to AttributeType.Text,
                    ATTRIBUTE_KEY_ADDRESS_CITY to AttributeType.Text,
                    ATTRIBUTE_KEY_ADDRESS_COUNTRY to AttributeType.Text,
                    ATTRIBUTE_KEY_PETS to AttributeType.Array(
                        AttributeType.Reference(
                            VALID_DIGEST_PET
                        )
                    ),
                ),
            ),
            CaptureBase1x0(
                digest = VALID_DIGEST_PET,
                attributes = mapOf(
                    ATTRIBUTE_KEY_NAME to AttributeType.Text,
                    ATTRIBUTE_KEY_RACE to AttributeType.Text,
                )
            )
        ),
        overlays = listOf(
            DataSourceOverlay2x0(
                captureBaseDigest = VALID_DIGEST_HUMAN,
                format = CREDENTIAL_FORMAT,
                attributeSources = mapOf<String, ClaimsPathPointer>(
                    ATTRIBUTE_KEY_FIRSTNAME to CLAIMS_PATH_POINTER_FIRSTNAME,
                    ATTRIBUTE_KEY_LASTNAME to CLAIMS_PATH_POINTER_LASTNAME,
                    ATTRIBUTE_KEY_ADDRESS_STREET to CLAIMS_PATH_POINTER_ADDRESS_STREET,
                    ATTRIBUTE_KEY_ADDRESS_CITY to CLAIMS_PATH_POINTER_ADDRESS_CITY,
                    ATTRIBUTE_KEY_ADDRESS_COUNTRY to CLAIMS_PATH_POINTER_ADDRESS_COUNTRY,
                    ATTRIBUTE_KEY_PETS to CLAIMS_PATH_POINTER_PETS,
                )
            ),
            DataSourceOverlay2x0(
                captureBaseDigest = VALID_DIGEST_PET,
                format = CREDENTIAL_FORMAT,
                attributeSources = mapOf(
                    ATTRIBUTE_KEY_NAME to CLAIMS_PATH_POINTER_PETS,
                    ATTRIBUTE_KEY_RACE to CLAIMS_PATH_POINTER_PETS_RACE,
                )
            ),
            LabelOverlay1x0(
                captureBaseDigest = VALID_DIGEST_HUMAN,
                language = LANGUAGE_EN,
                attributeLabels = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to "Firstname",
                    ATTRIBUTE_KEY_LASTNAME to "Lastname",
                    ATTRIBUTE_KEY_ADDRESS_STREET to "street name",
                    ATTRIBUTE_KEY_ADDRESS_CITY to "city name",
                    ATTRIBUTE_KEY_ADDRESS_COUNTRY to "country name",
                    ATTRIBUTE_KEY_PETS to "pets"
                )
            ),
            LabelOverlay1x0(
                captureBaseDigest = VALID_DIGEST_PET,
                language = LANGUAGE_EN,
                attributeLabels = mapOf(
                    ATTRIBUTE_KEY_NAME to "name",
                    ATTRIBUTE_KEY_RACE to "race",
                )
            ),
            BrandingOverlay1x1(
                captureBaseDigest = VALID_DIGEST_HUMAN,
                language = LANGUAGE_EN,
                logo = LOGO,
                primaryBackgroundColor = "#2C75E3",
                primaryField = "{{firstname}} {{lastname}} from {{address_country}}",
            ),
            MetaOverlay1x0(
                captureBaseDigest = VALID_DIGEST_HUMAN,
                language = LANGUAGE_EN,
                name = "Pet permit",
            ),
            OrderOverlay1x0(
                captureBaseDigest = VALID_DIGEST_HUMAN,
                attributeOrders = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to 1,
                    ATTRIBUTE_KEY_LASTNAME to 2,
                    ATTRIBUTE_KEY_ADDRESS_STREET to 3,
                    ATTRIBUTE_KEY_ADDRESS_CITY to 4,
                    ATTRIBUTE_KEY_ADDRESS_COUNTRY to 5,
                    "pets" to 6,
                )
            ),
            OrderOverlay1x0(
                captureBaseDigest = VALID_DIGEST_PET,
                attributeOrders = mapOf(
                    ATTRIBUTE_KEY_RACE to 1,
                    ATTRIBUTE_KEY_NAME to 2,
                ),
            )
        ),
        ocaClaimData = listOf(
            OcaClaimData(
                attributeType = AttributeType.Text,
                captureBaseDigest = VALID_DIGEST_HUMAN,
                flagged = false,
                name = ATTRIBUTE_KEY_FIRSTNAME,
                characterEncoding = null,
                dataSources = mapOf(CREDENTIAL_FORMAT to CLAIMS_PATH_POINTER_FIRSTNAME),
                entryMappings = emptyMap(),
                format = null,
                labels = mapOf(LANGUAGE_EN to ATTRIBUTE_LABEL_FIRSTNAME_EN),
                standard = null,
                isSensitive = false
            ),
            OcaClaimData(
                attributeType = AttributeType.Text,
                captureBaseDigest = VALID_DIGEST_HUMAN,
                flagged = false,
                name = ATTRIBUTE_KEY_LASTNAME,
                characterEncoding = null,
                dataSources = mapOf(CREDENTIAL_FORMAT to CLAIMS_PATH_POINTER_LASTNAME),
                entryMappings = emptyMap(),
                format = null,
                labels = mapOf(LANGUAGE_EN to ATTRIBUTE_KEY_LASTNAME),
                standard = null,
                isSensitive = false
            ),
            OcaClaimData(
                attributeType = AttributeType.Text,
                captureBaseDigest = VALID_DIGEST_HUMAN,
                flagged = false,
                name = ATTRIBUTE_KEY_ADDRESS_STREET,
                characterEncoding = null,
                dataSources = mapOf(CREDENTIAL_FORMAT to CLAIMS_PATH_POINTER_ADDRESS_STREET),
                entryMappings = emptyMap(),
                format = null,
                labels = mapOf(LANGUAGE_EN to ATTRIBUTE_KEY_ADDRESS_STREET),
                standard = null,
                isSensitive = false
            ),
            OcaClaimData(
                attributeType = AttributeType.Text,
                captureBaseDigest = VALID_DIGEST_HUMAN,
                flagged = false,
                name = ATTRIBUTE_KEY_ADDRESS_CITY,
                characterEncoding = null,
                dataSources = mapOf(CREDENTIAL_FORMAT to CLAIMS_PATH_POINTER_ADDRESS_CITY),
                entryMappings = emptyMap(),
                format = null,
                labels = mapOf(LANGUAGE_EN to ATTRIBUTE_KEY_ADDRESS_CITY),
                standard = null,
                isSensitive = false
            ),
            OcaClaimData(
                attributeType = AttributeType.Text,
                captureBaseDigest = VALID_DIGEST_PET,
                flagged = false,
                name = ATTRIBUTE_KEY_PETS,
                characterEncoding = null,
                dataSources = mapOf(CREDENTIAL_FORMAT to CLAIMS_PATH_POINTER_PETS),
                entryMappings = emptyMap(),
                format = null,
                labels = mapOf(LANGUAGE_EN to ATTRIBUTE_KEY_PETS),
                standard = null,
                isSensitive = false
            ),
            OcaClaimData(
                attributeType = AttributeType.Text,
                captureBaseDigest = VALID_DIGEST_PET,
                flagged = false,
                name = ATTRIBUTE_KEY_NAME,
                characterEncoding = null,
                dataSources = mapOf(CREDENTIAL_FORMAT to CLAIMS_PATH_POINTER_PETS_NAME),
                entryMappings = emptyMap(),
                format = null,
                labels = mapOf(LANGUAGE_EN to ATTRIBUTE_KEY_NAME),
                standard = null,
                isSensitive = false
            ),
            OcaClaimData(
                attributeType = AttributeType.Text,
                captureBaseDigest = VALID_DIGEST_PET,
                flagged = false,
                name = ATTRIBUTE_KEY_RACE,
                characterEncoding = null,
                dataSources = mapOf(CREDENTIAL_FORMAT to CLAIMS_PATH_POINTER_PETS_RACE),
                entryMappings = emptyMap(),
                format = null,
                labels = mapOf(LANGUAGE_EN to ATTRIBUTE_KEY_RACE),
                standard = null,
                isSensitive = false
            )
        )
    )

    val ocaNestedClaimsPathPointer = OcaBundle(
        captureBases = listOf(
            CaptureBase1x0(
                digest = VALID_DIGEST_HUMAN,
                attributes = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to AttributeType.Text,
                    ATTRIBUTE_KEY_LASTNAME to AttributeType.Text,
                    ATTRIBUTE_KEY_ADDRESS_STREET to AttributeType.Text,
                    ATTRIBUTE_KEY_ADDRESS_CITY to AttributeType.Text,
                    ATTRIBUTE_KEY_ADDRESS_COUNTRY to AttributeType.Text,
                    ATTRIBUTE_KEY_PETS to AttributeType.Array(
                        AttributeType.Reference(
                            VALID_DIGEST_PET
                        )
                    ),
                ),
            ),
            CaptureBase1x0(
                digest = VALID_DIGEST_PET,
                attributes = mapOf(
                    ATTRIBUTE_KEY_NAME to AttributeType.Text,
                    ATTRIBUTE_KEY_RACE to AttributeType.Text,
                )
            )
        ),
        overlays = listOf(
            DataSourceOverlay2x0(
                captureBaseDigest = VALID_DIGEST_HUMAN,
                format = CREDENTIAL_FORMAT,
                attributeSources = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to CLAIMS_PATH_POINTER_FIRSTNAME,
                    ATTRIBUTE_KEY_LASTNAME to CLAIMS_PATH_POINTER_LASTNAME,
                    ATTRIBUTE_KEY_ADDRESS_STREET to CLAIMS_PATH_POINTER_ADDRESS_STREET,
                    ATTRIBUTE_KEY_ADDRESS_CITY to CLAIMS_PATH_POINTER_ADDRESS_CITY,
                    ATTRIBUTE_KEY_ADDRESS_COUNTRY to CLAIMS_PATH_POINTER_ADDRESS_COUNTRY,
                    ATTRIBUTE_KEY_PETS to CLAIMS_PATH_POINTER_PETS,
                )
            ),
            DataSourceOverlay2x0(
                captureBaseDigest = VALID_DIGEST_PET,
                format = CREDENTIAL_FORMAT,
                attributeSources = mapOf(
                    ATTRIBUTE_KEY_NAME to CLAIMS_PATH_POINTER_PETS_NAME,
                    ATTRIBUTE_KEY_RACE to CLAIMS_PATH_POINTER_PETS_RACE,
                )
            ),
            LabelOverlay1x0(
                captureBaseDigest = VALID_DIGEST_HUMAN,
                language = LANGUAGE_EN,
                attributeLabels = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to "Firstname",
                    ATTRIBUTE_KEY_LASTNAME to "Lastname",
                    ATTRIBUTE_KEY_ADDRESS_STREET to "street name",
                    ATTRIBUTE_KEY_ADDRESS_CITY to "city name",
                    ATTRIBUTE_KEY_ADDRESS_COUNTRY to "country name",
                    ATTRIBUTE_KEY_PETS to "pets"
                )
            ),
            LabelOverlay1x0(
                captureBaseDigest = VALID_DIGEST_PET,
                language = LANGUAGE_EN,
                attributeLabels = mapOf(
                    ATTRIBUTE_KEY_NAME to "name",
                    ATTRIBUTE_KEY_RACE to "race",
                )
            ),
            BrandingOverlay1x1(
                captureBaseDigest = VALID_DIGEST_HUMAN,
                language = LANGUAGE_EN,
                logo = LOGO,
                primaryBackgroundColor = "#2C75E3",
                primaryField = "{{firstname}} {{lastname}} from {{address_country}}",
            ),
            MetaOverlay1x0(
                captureBaseDigest = VALID_DIGEST_HUMAN,
                language = LANGUAGE_EN,
                name = "Pet permit",
            ),
            OrderOverlay1x0(
                captureBaseDigest = VALID_DIGEST_HUMAN,
                attributeOrders = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to 1,
                    ATTRIBUTE_KEY_LASTNAME to 2,
                    ATTRIBUTE_KEY_ADDRESS_STREET to 3,
                    ATTRIBUTE_KEY_ADDRESS_CITY to 4,
                    ATTRIBUTE_KEY_ADDRESS_COUNTRY to 5,
                    "pets" to 6,
                )
            ),
            OrderOverlay1x0(
                captureBaseDigest = VALID_DIGEST_PET,
                attributeOrders = mapOf(
                    ATTRIBUTE_KEY_RACE to 1,
                    ATTRIBUTE_KEY_NAME to 2,
                ),
            )
        ),
        ocaClaimData = listOf(
            OcaClaimData(
                attributeType = AttributeType.Text,
                captureBaseDigest = VALID_DIGEST_HUMAN,
                flagged = false,
                name = ATTRIBUTE_KEY_FIRSTNAME,
                characterEncoding = null,
                dataSources = mapOf(CREDENTIAL_FORMAT to CLAIMS_PATH_POINTER_FIRSTNAME),
                entryMappings = emptyMap(),
                format = null,
                labels = mapOf(LANGUAGE_EN to ATTRIBUTE_LABEL_FIRSTNAME_EN),
                standard = null,
                isSensitive = false
            ),
            OcaClaimData(
                attributeType = AttributeType.Text,
                captureBaseDigest = VALID_DIGEST_HUMAN,
                flagged = false,
                name = ATTRIBUTE_KEY_LASTNAME,
                characterEncoding = null,
                dataSources = mapOf(CREDENTIAL_FORMAT to CLAIMS_PATH_POINTER_LASTNAME),
                entryMappings = emptyMap(),
                format = null,
                labels = mapOf(LANGUAGE_EN to ATTRIBUTE_KEY_LASTNAME),
                standard = null,
                isSensitive = false
            ),
            OcaClaimData(
                attributeType = AttributeType.Text,
                captureBaseDigest = VALID_DIGEST_HUMAN,
                flagged = false,
                name = ATTRIBUTE_KEY_ADDRESS_STREET,
                characterEncoding = null,
                dataSources = mapOf(CREDENTIAL_FORMAT to CLAIMS_PATH_POINTER_ADDRESS_STREET),
                entryMappings = emptyMap(),
                format = null,
                labels = mapOf(LANGUAGE_EN to ATTRIBUTE_KEY_ADDRESS_STREET),
                standard = null,
                isSensitive = false
            ),
            OcaClaimData(
                attributeType = AttributeType.Text,
                captureBaseDigest = VALID_DIGEST_HUMAN,
                flagged = false,
                name = ATTRIBUTE_KEY_ADDRESS_CITY,
                characterEncoding = null,
                dataSources = mapOf(CREDENTIAL_FORMAT to CLAIMS_PATH_POINTER_ADDRESS_CITY),
                entryMappings = emptyMap(),
                format = null,
                labels = mapOf(LANGUAGE_EN to ATTRIBUTE_KEY_ADDRESS_CITY),
                standard = null,
                isSensitive = false
            ),
            OcaClaimData(
                attributeType = AttributeType.Text,
                captureBaseDigest = VALID_DIGEST_PET,
                flagged = false,
                name = ATTRIBUTE_KEY_PETS,
                characterEncoding = null,
                dataSources = mapOf(CREDENTIAL_FORMAT to CLAIMS_PATH_POINTER_PETS),
                entryMappings = emptyMap(),
                format = null,
                labels = mapOf(LANGUAGE_EN to ATTRIBUTE_KEY_PETS),
                standard = null,
                isSensitive = false
            ),
            OcaClaimData(
                attributeType = AttributeType.Text,
                captureBaseDigest = VALID_DIGEST_PET,
                flagged = false,
                name = ATTRIBUTE_KEY_NAME,
                characterEncoding = null,
                dataSources = mapOf(CREDENTIAL_FORMAT to CLAIMS_PATH_POINTER_PETS_NAME),
                entryMappings = emptyMap(),
                format = null,
                labels = mapOf(LANGUAGE_EN to ATTRIBUTE_KEY_NAME),
                standard = null,
                isSensitive = false
            ),
            OcaClaimData(
                attributeType = AttributeType.Text,
                captureBaseDigest = VALID_DIGEST_PET,
                flagged = false,
                name = ATTRIBUTE_KEY_RACE,
                characterEncoding = null,
                dataSources = mapOf(CREDENTIAL_FORMAT to CLAIMS_PATH_POINTER_PETS_RACE),
                entryMappings = emptyMap(),
                format = null,
                labels = mapOf(LANGUAGE_EN to ATTRIBUTE_KEY_RACE),
                standard = null,
                isSensitive = false
            )
        )
    )
}
