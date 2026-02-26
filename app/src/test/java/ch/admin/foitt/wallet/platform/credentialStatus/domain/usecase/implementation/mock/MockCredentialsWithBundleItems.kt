package ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.implementation.mock

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.wallet.platform.database.domain.model.BundleItemEntity
import ch.admin.foitt.wallet.platform.database.domain.model.BundleItemWithKeyBinding
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithBundleItemsWithKeyBinding
import java.net.URL

object MockCredentialsWithBundleItems {
    private const val payload = "payload"
    private val format = CredentialFormat.VC_SD_JWT
    private const val ISSUER_URL = "https://example.com/issuer"

    private val credential = Credential(
        id = 0,
        format = format,
        createdAt = 1700463600000,
        issuerUrl = URL(ISSUER_URL),
    )

    private val credentials = listOf(
        credential,
        credential.copy(id = 1),
        credential.copy(id = 2),
    )

    private val verifiableCredential = VerifiableCredentialEntity(
        createdAt = 1700463600000,
        updatedAt = null,
        issuer = "issuer",
        validFrom = 0,
        credentialId = 0,
        validUntil = 17768026519L,
    )

    private val verifiableCredentials = listOf(
        verifiableCredential,
        verifiableCredential.copy(credentialId = 1),
        verifiableCredential.copy(credentialId = 2),
    )

    private val validBundleItem = BundleItemEntity(
        credentialId = credentials[0].id,
        payload = payload,
        status = CredentialStatus.VALID,
    )

    private val revokedBundleItem = BundleItemEntity(
        credentialId = credentials[1].id,
        payload = payload,
        status = CredentialStatus.REVOKED,
    )

    private val unknownBundleItem = BundleItemEntity(
        credentialId = credentials[2].id,
        payload = payload,
        status = CredentialStatus.UNKNOWN,
    )

    private val validVerifiableCredentialWithBundleItemsWithKeyBinding =
        VerifiableCredentialWithBundleItemsWithKeyBinding(
            credential = credentials[0],
            verifiableCredential = verifiableCredentials[0],
            bundleItemsWithKeyBinding = listOf(
                BundleItemWithKeyBinding(
                    bundleItem = validBundleItem,
                    keyBinding = null
                )
            ),
        )

    private val revokedVerifiableCredentialWithBundleItemsWithKeyBinding =
        VerifiableCredentialWithBundleItemsWithKeyBinding(
            credential = credentials[1],
            verifiableCredential = verifiableCredentials[1],
            bundleItemsWithKeyBinding = listOf(
                BundleItemWithKeyBinding(
                    bundleItem = revokedBundleItem,
                    keyBinding = null
                )
            ),
        )

    private val unknownVerifiableCredentialWithBundleItemsWithKeyBinding =
        VerifiableCredentialWithBundleItemsWithKeyBinding(
            credential = credentials[2],
            verifiableCredential = verifiableCredentials[2],
            bundleItemsWithKeyBinding = listOf(
                BundleItemWithKeyBinding(
                    bundleItem = unknownBundleItem,
                    keyBinding = null
                )
            ),
        )

    val verifiableCredentialsWithBundleItems = listOf(
        validVerifiableCredentialWithBundleItemsWithKeyBinding,
        revokedVerifiableCredentialWithBundleItemsWithKeyBinding,
        unknownVerifiableCredentialWithBundleItemsWithKeyBinding,
    )
}
