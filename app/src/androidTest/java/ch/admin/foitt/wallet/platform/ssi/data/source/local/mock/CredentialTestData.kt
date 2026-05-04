package ch.admin.foitt.wallet.platform.ssi.data.source.local.mock

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimClusterDisplayEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimClusterEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialIssuerDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialKeyBindingEntity
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import java.net.URL

object CredentialTestData {
    private val FORMAT = CredentialFormat.VC_SD_JWT
    private const val ISSUER_URL = "https://issuer.example.com"
    const val VALUE = "VALUE"
    private const val VALUE2 = "VALUE2"
    const val PATH = "[\"path\"]"
    private const val PATH2 = "[\"path2\"]"
    const val NAME1 = "NAME1"
    const val CORRECT = "CORRECT"
    const val FALLBACK = "FALLBACK"
    const val DISPLAY_VALUE = "VALUE"

    private const val NAME2 = "NAME2"
    private const val NAME3 = "NAME3"
    private const val IDENTIFIER = "IDENTIFIER"
    private const val SIGNING_ALGORITHM = "SIGNING_ALGORITHM"
    private const val LOGO_DATA = "logo data"

    val keyBinding1 = CredentialKeyBindingEntity(
        id = IDENTIFIER,
        credentialId = 1,
        algorithm = SIGNING_ALGORITHM,
        bindingType = KeyBindingType.HARDWARE,
    )

    val keyBinding2 = CredentialKeyBindingEntity(
        id = IDENTIFIER,
        credentialId = 2,
        algorithm = SIGNING_ALGORITHM,
        bindingType = KeyBindingType.HARDWARE,
    )

    val keyBinding3 = CredentialKeyBindingEntity(
        id = "key1",
        credentialId = 3,
        algorithm = "ES256",
        bindingType = KeyBindingType.HARDWARE,
    )

    val credential1 = Credential(
        id = 1,
        format = FORMAT,
        issuerUrl = URL(ISSUER_URL),
        createdAt = 1,
    )

    val verifiableCredential1 = VerifiableCredentialEntity(
        credentialId = 1,
        createdAt = 1,
        updatedAt = 1,
        issuer = "issuer",
        validFrom = 0,
        validUntil = 17768026519L,
        nextPresentableBundleItemId = 1,
    )

    val deferredCredential1 = DeferredCredentialEntity(
        credentialId = 1,
        progressionState = DeferredProgressionState.IN_PROGRESS,
        transactionId = "1",
        accessToken = "token",
        refreshToken = "refreshToken",
        endpoint = "endpoint",
        pollInterval = 1000,
        createdAt = 1,
        polledAt = 1,
    )

    val credential2 = Credential(
        id = 2,
        format = FORMAT,
        issuerUrl = URL(ISSUER_URL),
        createdAt = 2
    )

    val verifiableCredential2 = VerifiableCredentialEntity(
        credentialId = 2,
        createdAt = 2,
        updatedAt = 2,
        issuer = "issuer",
        validFrom = 0,
        validUntil = 17768026519L,
        nextPresentableBundleItemId = 1,
    )
    val credentialWithPayload = Credential(
        id = 3,
        format = CredentialFormat.VC_SD_JWT,
        issuerUrl = URL(ISSUER_URL),
        createdAt = 3
    )

    val verifiableCredentialWithPayload = VerifiableCredentialEntity(
        credentialId = 3,
        createdAt = 3,
        updatedAt = 3,
        issuer = "issuer",
        validFrom = 0,
        validUntil = 17768026519L,
        nextPresentableBundleItemId = 1,
    )

    val cluster1 = CredentialClaimClusterEntity(id = 1, verifiableCredentialId = 1, parentClusterId = null, order = -1)
    val cluster2 = CredentialClaimClusterEntity(id = 2, verifiableCredentialId = 2, parentClusterId = null, order = -1)
    val clusterWithParent = CredentialClaimClusterEntity(id = 3, verifiableCredentialId = 1, parentClusterId = 1, order = 2)

    val clusterDisplay1 = CredentialClaimClusterDisplayEntity(id = 1, clusterId = 1, name = "name", locale = "locale")
    val clusterDisplay2 = CredentialClaimClusterDisplayEntity(id = 2, clusterId = 2, name = "name", locale = "locale")

    val credentialClaim1 = CredentialClaim(id = 1, clusterId = 1, path = PATH, value = VALUE, valueType = null)
    val credentialClaim2 = CredentialClaim(id = 2, clusterId = 2, path = PATH2, value = VALUE2, valueType = null)

    val credentialClaimDisplay1 = CredentialClaimDisplay(
        id = 1,
        claimId = credentialClaim1.id,
        name = NAME1,
        locale = "xx",
        value = DISPLAY_VALUE
    )
    val credentialClaimDisplay2 = CredentialClaimDisplay(
        id = 2,
        claimId = credentialClaim2.id,
        name = NAME2,
        locale = "xx_XX",
        value = DISPLAY_VALUE
    )
    val credentialClaimDisplay3 = CredentialClaimDisplay(id = 3, claimId = credentialClaim1.id, name = NAME3, locale = "xx_XX", value = DISPLAY_VALUE)

    val credentialDisplay1 = CredentialDisplay(
        id = 1,
        credentialId = credential1.id,
        locale = "xx_XX",
        name = CORRECT
    )
    val credentialDisplay2 = CredentialDisplay(
        id = 2,
        credentialId = credential2.id,
        locale = DisplayLanguage.FALLBACK,
        name = FALLBACK
    )

    val credentialIssuerDisplay1 = CredentialIssuerDisplay(id = 1, credentialId = credential1.id, name = NAME1, locale = "xx")
    val credentialIssuerDisplay2 = CredentialIssuerDisplay(id = 2, credentialId = credential2.id, name = NAME2, locale = "xx_XX")
}
