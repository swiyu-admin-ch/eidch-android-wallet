package ch.admin.foitt.wallet.platform.credential.domain.model

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.OidIssuerDisplay

data class AnyIssuerDisplay(
    override val locale: String? = null,
    override val name: String? = null,
    val logo: String? = null,
    val logoAltText: String? = null,
) : AnyDisplay

fun OidIssuerDisplay.toAnyIssuerDisplay() =
    AnyIssuerDisplay(
        locale = locale,
        name = name,
        logo = logo?.uri,
        logoAltText = logo?.altText,
    )
