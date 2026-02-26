package ch.admin.foitt.wallet.platform.oca.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.wallet.platform.oca.domain.model.FetchVcMetadataByFormatError
import ch.admin.foitt.wallet.platform.oca.domain.model.VcMetadata
import com.github.michaelbull.result.Result

interface FetchDeferredVcMetadataByFormat {
    suspend operator fun invoke(
        credentialConfig: AnyCredentialConfiguration,
    ): Result<VcMetadata, FetchVcMetadataByFormatError>
}
