package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyVcSdJwtSignatureError
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.VerifyVcSdJwtSignature
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.GenerateCredentialDisplaysError
import ch.admin.foitt.wallet.platform.credential.domain.model.SaveCredentialFromDeferredError
import ch.admin.foitt.wallet.platform.credential.domain.model.toKeyBinding
import ch.admin.foitt.wallet.platform.credential.domain.model.toSaveCredentialFromDeferredError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchTrustForIssuance
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateAnyDisplays
import ch.admin.foitt.wallet.platform.credential.domain.usecase.SaveCredentialFromDeferred
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialWithKeyBinding
import ch.admin.foitt.wallet.platform.database.domain.model.RawCredentialData
import ch.admin.foitt.wallet.platform.oca.domain.model.FetchVcMetadataByFormatError
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchVcMetadataByFormat
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaBundler
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialOfferRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialOfferRepository
import ch.admin.foitt.wallet.platform.utils.compress
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getOr
import com.github.michaelbull.result.mapError
import timber.log.Timber
import javax.inject.Inject

class SaveCredentialFromDeferredImpl @Inject constructor(
    private val verifyVcSdJwtSignature: VerifyVcSdJwtSignature,
    private val fetchVcMetadataByFormat: FetchVcMetadataByFormat,
    private val fetchTrustForIssuance: FetchTrustForIssuance,
    private val ocaBundler: OcaBundler,
    private val generateAnyDisplays: GenerateAnyDisplays,
    private val credentialOfferRepository: CredentialOfferRepository,
) : SaveCredentialFromDeferred {
    override suspend fun invoke(
        deferredCredentialEntity: DeferredCredentialWithKeyBinding,
        credentialResponse: CredentialResponse.VerifiableCredential,
        rawAndParsedIssuerCredentialInfo: RawAndParsedIssuerCredentialInfo,
    ): Result<Long, SaveCredentialFromDeferredError> = coroutineBinding {
        Timber.d("Deferred refresh: handle getting credential for ${deferredCredentialEntity.deferredCredential.credentialId}")

        val anyCredential: AnyCredential = when (deferredCredentialEntity.credential.format) {
            CredentialFormat.VC_SD_JWT -> {
                val keyBinding: KeyBinding? = deferredCredentialEntity.firstKeyBinding
                val payload: String = credentialResponse.firstCredential
                    ?: Err(CredentialError.InvalidCredentialOffer).bind()

                verifyVcSdJwtSignature(
                    keyBinding = keyBinding,
                    payload = payload,
                ).mapError(VerifyVcSdJwtSignatureError::toSaveCredentialFromDeferredError).bind()
            }

            CredentialFormat.UNKNOWN -> {
                Err(CredentialError.UnsupportedCredentialFormat).bind()
            }
        }

        val vcMetadata = fetchVcMetadataByFormat(anyCredential)
            .mapError(FetchVcMetadataByFormatError::toSaveCredentialFromDeferredError).bind()

        val trustCheckResult = fetchTrustForIssuance(
            issuerDid = anyCredential.issuer,
            vcSchemaId = anyCredential.vcSchemaId,
        )

        val rawOcaBundle = vcMetadata.rawOcaBundle?.rawOcaBundle
        val ocaBundle = rawOcaBundle?.let {
            ocaBundler(it).get()
        }

        val credentialConfig = rawAndParsedIssuerCredentialInfo.issuerCredentialInfo.credentialConfigurations.firstOrNull {
            it.identifier == deferredCredentialEntity.credential.selectedConfigurationId
        } ?: Err(CredentialError.InvalidCredentialOffer).bind()

        val displays = generateAnyDisplays(
            anyCredential = anyCredential,
            issuerInfo = rawAndParsedIssuerCredentialInfo.issuerCredentialInfo,
            trustStatement = trustCheckResult.actorTrustStatement,
            metadata = credentialConfig,
            ocaBundle = ocaBundle,
        ).mapError(GenerateCredentialDisplaysError::toSaveCredentialFromDeferredError).bind()

        val rawCredentialData = RawCredentialData(
            credentialId = deferredCredentialEntity.credential.id,
            rawOcaBundle = rawOcaBundle?.toByteArray()?.compress(),
            rawOIDMetadata = rawAndParsedIssuerCredentialInfo.rawIssuerCredentialInfo.toByteArray().compress()
        )

        credentialOfferRepository.saveCredentialFromDeferred(
            credentialId = deferredCredentialEntity.credential.id,
            payloads = listOf(anyCredential.payload),
            validFrom = anyCredential.validFromInstant?.epochSecond,
            validUntil = anyCredential.validUntilInstant?.epochSecond,
            issuer = anyCredential.issuer,
            issuerDisplays = displays.issuerDisplays,
            credentialDisplays = displays.credentialDisplays,
            clusters = displays.clusters,
            rawCredentialData = rawCredentialData,
        ).mapError(CredentialOfferRepositoryError::toSaveCredentialFromDeferredError).bind()
    }

    private val DeferredCredentialWithKeyBinding.firstKeyBinding: KeyBinding?
        get() = this.keyBindings.firstOrNull()?.toKeyBinding()?.getOr(null)

    private val CredentialResponse.VerifiableCredential.firstCredential
        get() = this.credentials.firstOrNull()?.credential
}
