@file:Suppress("LongParameterList")

package ch.admin.foitt.wallet.platform.ssi.domain.repository

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyCredentialDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyIssuerDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.Cluster
import ch.admin.foitt.wallet.platform.database.domain.model.RawCredentialData
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialOfferRepositoryError
import com.github.michaelbull.result.Result
import java.net.URL

interface CredentialOfferRepository {
    suspend fun saveCredentialOffer(
        credentialId: Long = 0L,
        keyBindings: List<KeyBinding?>,
        payloads: List<String>,
        format: CredentialFormat,
        selectedConfigurationId: String,
        validFrom: Long?,
        validUntil: Long?,
        issuer: String?,
        issuerDisplays: List<AnyIssuerDisplay>,
        credentialDisplays: List<AnyCredentialDisplay>,
        clusters: List<Cluster>,
        rawCredentialData: RawCredentialData,
        issuerUrl: URL,
    ): Result<Long, CredentialOfferRepositoryError>

    suspend fun saveDeferredCredentialOffer(
        transactionId: String,
        accessToken: String,
        refreshToken: String?,
        endpoint: URL,
        pollInterval: Int,
        keyBindings: List<KeyBinding>?,
        format: CredentialFormat,
        issuerUrl: URL,
        selectedConfigurationId: String,
        issuerDisplays: List<AnyIssuerDisplay>,
        credentialDisplays: List<AnyCredentialDisplay>,
        rawCredentialData: RawCredentialData,
    ): Result<Long, CredentialOfferRepositoryError>

    suspend fun updateDeferredCredentialMetaData(
        credentialId: Long,
        issuerDisplays: List<AnyIssuerDisplay>,
        credentialDisplays: List<AnyCredentialDisplay>,
        rawMetadata: ByteArray
    ): Result<Unit, CredentialOfferRepositoryError>

    suspend fun saveCredentialFromDeferred(
        credentialId: Long,
        payloads: List<String>,
        validFrom: Long?,
        validUntil: Long?,
        issuer: String?,
        issuerDisplays: List<AnyIssuerDisplay>,
        credentialDisplays: List<AnyCredentialDisplay>,
        clusters: List<Cluster>,
        rawCredentialData: RawCredentialData,
    ): Result<Long, CredentialOfferRepositoryError>
}
