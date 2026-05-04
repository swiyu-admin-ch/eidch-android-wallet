package ch.admin.foitt.wallet.platform.oca.domain.usecase

import ch.admin.foitt.wallet.platform.oca.domain.model.FetchVcMetadataByFormatError
import ch.admin.foitt.wallet.platform.oca.domain.model.MetaDataIntegrity
import com.github.michaelbull.result.Result
import java.net.URL

interface ResolveMetaDataIntegrity {
    suspend operator fun invoke(metaDataIntegrity: MetaDataIntegrity): Result<Pair<URL?, String?>, FetchVcMetadataByFormatError>
}
