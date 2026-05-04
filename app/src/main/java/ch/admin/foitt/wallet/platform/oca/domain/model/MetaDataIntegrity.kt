package ch.admin.foitt.wallet.platform.oca.domain.model

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.VcSdJwtCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential

data class MetaDataIntegrity(
    val vctMetadataUri: String?,
    val vctMetadataUriIntegrity: String?,
    val vct: String,
    val vctIntegrity: String?
) {
    companion object {
        fun from(credential: VcSdJwtCredential) = MetaDataIntegrity(
            credential.vctMetadataUri,
            credential.vctMetadataUriIntegrity,
            credential.vct,
            credential.vctIntegrity
        )

        fun from(config: VcSdJwtCredentialConfiguration) = MetaDataIntegrity(
            config.vctMetadataUri,
            config.vctMetadataUriIntegrity,
            config.vct,
            config.vctIntegrity
        )
    }
}
