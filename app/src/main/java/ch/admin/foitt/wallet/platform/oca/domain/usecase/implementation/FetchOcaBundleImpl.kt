package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.openid4vc.utils.SafeGetUrlError
import ch.admin.foitt.openid4vc.utils.base64ToDecodedString
import ch.admin.foitt.openid4vc.utils.safeGetUrl
import ch.admin.foitt.sriValidator.domain.SRIValidator
import ch.admin.foitt.sriValidator.domain.model.SRIError
import ch.admin.foitt.wallet.platform.oca.domain.model.FetchOcaBundleError
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaRepositoryError
import ch.admin.foitt.wallet.platform.oca.domain.model.RawOcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.toFetchOcaBundleError
import ch.admin.foitt.wallet.platform.oca.domain.repository.OcaRepository
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchOcaBundle
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class FetchOcaBundleImpl @Inject constructor(
    private val ocaRepository: OcaRepository,
    private val sriValidator: SRIValidator,
) : FetchOcaBundle {
    override suspend fun invoke(
        uri: String,
        integrity: String?,
    ): Result<RawOcaBundle, FetchOcaBundleError> = coroutineBinding {
        val rawOcaBundleString = when {
            uri.startsWith(URL_PATTERN) -> fetchOcaBundleFromUrl(uri, integrity).bind()
            uri.startsWith(DATA_URI_PATTERN) -> fetchOcaBundleFromDataUri(uri, integrity).bind()
            else -> Err(OcaError.InvalidOca).bind<String>()
        }

        RawOcaBundle(rawOcaBundleString)
    }

    private suspend fun fetchOcaBundleFromUrl(
        uri: String,
        integrity: String?,
    ): Result<String, FetchOcaBundleError> = coroutineBinding {
        val url = safeGetUrl(uri)
            .mapError(SafeGetUrlError::toFetchOcaBundleError)
            .bind()
        val rawOcaBundleString = ocaRepository.fetchOcaBundleByUrl(url)
            .mapError(OcaRepositoryError::toFetchOcaBundleError)
            .bind()

        integrity?.let {
            validateSubresource(rawOcaBundleString, integrity).bind()
        }

        rawOcaBundleString
    }

    private suspend fun fetchOcaBundleFromDataUri(
        uri: String,
        integrity: String?,
    ): Result<String, FetchOcaBundleError> = coroutineBinding {
        val rawOcaBundleString = uri.substringAfter(DATA_URI_PATTERN).base64ToDecodedString()

        integrity?.let {
            validateSubresource(uri, integrity).bind()
        }

        rawOcaBundleString
    }

    private suspend fun validateSubresource(
        data: String,
        integrity: String,
    ): Result<Unit, FetchOcaBundleError> = coroutineBinding {
        sriValidator(data.encodeToByteArray(), integrity)
            .mapError(SRIError::toFetchOcaBundleError)
            .bind()
    }

    private companion object {
        const val URL_PATTERN = "https://"
        const val DATA_URI_PATTERN = "data:application/json;base64,"
    }
}
