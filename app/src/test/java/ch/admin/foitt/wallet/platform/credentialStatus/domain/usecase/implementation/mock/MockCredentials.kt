package ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.implementation.mock

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus

object MockCredentials {
    private const val keyIdentifier = "keyIdentifier"
    private const val payload = "payload"
    private const val signingAlgorithm = "ES512"
    private val format = CredentialFormat.VC_SD_JWT

    val validCredential = Credential(
        id = 0,
        status = CredentialStatus.VALID,
        privateKeyIdentifier = keyIdentifier,
        payload = payload,
        format = format,
        createdAt = 1700463600000,
        updatedAt = null,
        signingAlgorithm = signingAlgorithm,
    )

    val credentials = listOf(
        validCredential,
        validCredential.copy(id = 1, status = CredentialStatus.REVOKED),
        validCredential.copy(id = 2, status = CredentialStatus.UNKNOWN),
    )
}
