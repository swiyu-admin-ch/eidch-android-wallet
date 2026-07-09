package ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation.mock

import ch.admin.foitt.openid4vc.domain.model.BatchSize
import ch.admin.foitt.openid4vc.domain.model.TokenType
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.wallet.platform.database.domain.model.BatchRefreshDataEntity
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialAuthenticationEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialAuthenticationWithDpopBinding
import ch.admin.foitt.wallet.platform.database.domain.model.DpopBindingEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithBatchDataAndAuthentication
import java.net.URL

object MockBatchRefreshData {
    const val CREDENTIAL_ID = 42L
    const val AUTHENTICATION_ID = 24L
    const val REFRESH_TOKEN = "refresh-token"
    const val ACCESS_TOKEN = "access-token"
    val tokenType = TokenType.BEARER
    const val SELECTED_CONFIG_ID = "config-id"
    const val CREDENTIAL_ISSUER = "https://issuer.example"
    const val KEY_ID = "dpop-key-id"

    fun createBatchRefreshData(
        batchSize: BatchSize,
        accessToken: String = ACCESS_TOKEN,
        refreshToken: String = REFRESH_TOKEN,
        dpopBinding: DpopBindingEntity? = null,
    ) = VerifiableCredentialWithBatchDataAndAuthentication(
        verifiableCredential = VerifiableCredentialEntity(
            credentialId = CREDENTIAL_ID,
            issuer = "issuer",
            validFrom = 0,
            validUntil = null,
            nextPresentableBundleItemId = 1L,
        ),
        credential = Credential(
            id = CREDENTIAL_ID,
            format = CredentialFormat.VC_SD_JWT,
            issuerUrl = URL(CREDENTIAL_ISSUER),
            selectedConfigurationId = SELECTED_CONFIG_ID,
        ),
        batchData = BatchRefreshDataEntity(
            credentialId = CREDENTIAL_ID,
            batchSize = batchSize,
        ),
        authentication = CredentialAuthenticationWithDpopBinding(
            credentialAuthentication = CredentialAuthenticationEntity(
                id = AUTHENTICATION_ID,
                credentialId = CREDENTIAL_ID,
                refreshToken = refreshToken,
                accessToken = accessToken,
                tokenType = tokenType,
            ),
            dpopBinding = dpopBinding,
        ),
    )
}
