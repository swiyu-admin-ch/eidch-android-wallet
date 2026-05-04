package ch.admin.foitt.wallet.platform.imageValidation.domain.usecase

import ch.admin.foitt.wallet.platform.imageValidation.domain.model.ValidateImageError
import com.github.michaelbull.result.Result

fun interface ValidateImage {
    operator fun invoke(
        mimeType: String,
        image: String?,
    ): Result<Unit, ValidateImageError>
}
