package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.openid4vc.utils.safeGetUrl
import ch.admin.foitt.wallet.platform.oca.domain.model.FetchVcMetadataByFormatError
import ch.admin.foitt.wallet.platform.oca.domain.model.MetaDataIntegrity
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.model.toFetchVcMetadataByFormatError
import ch.admin.foitt.wallet.platform.oca.domain.usecase.ResolveMetaDataIntegrity
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import java.net.URL
import javax.inject.Inject

class ResolveMetaDataIntegrityImpl @Inject constructor() : ResolveMetaDataIntegrity {
    override suspend fun invoke(metaDataIntegrity: MetaDataIntegrity): Result<Pair<URL?, String?>, FetchVcMetadataByFormatError> {
        return if (metaDataIntegrity.vctMetadataUri != null) {
            safeGetUrl(metaDataIntegrity.vctMetadataUri)
                .mapError { it.toFetchVcMetadataByFormatError() }
                .map { url -> url to metaDataIntegrity.vctMetadataUriIntegrity }
        } else {
            val url = safeGetUrl(metaDataIntegrity.vct).get()
            if (url == null && metaDataIntegrity.vctIntegrity != null) {
                val exception = IllegalStateException("Vct is not a url, but vct#integrity is provided")
                Err(OcaError.Unexpected(exception))
            } else {
                Ok(url to metaDataIntegrity.vctIntegrity)
            }
        }
    }
}
