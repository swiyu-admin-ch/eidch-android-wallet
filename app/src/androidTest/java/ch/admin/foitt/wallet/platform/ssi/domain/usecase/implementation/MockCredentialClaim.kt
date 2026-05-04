package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import ch.admin.foitt.wallet.platform.ssi.domain.model.ValueType

object MockCredentialClaim {
    private const val CLAIM_ID = 1L

    val credentialClaimDisplay = CredentialClaimDisplay(
        claimId = CLAIM_ID,
        name = "name",
        locale = "xxx",
        value = "value"
    )
    val credentialClaimDisplays = listOf(credentialClaimDisplay)

    fun buildClaimWithDateTime(
        value: String,
        valueDisplayInfo: String?
    ) = CredentialClaimWithDisplays(
        claim = CredentialClaim(
            clusterId = 1,
            path = "[\"path\"]",
            value = value,
            valueType = ValueType.DATETIME.value,
            valueDisplayInfo = valueDisplayInfo,
        ),
        displays = credentialClaimDisplays
    )
}
