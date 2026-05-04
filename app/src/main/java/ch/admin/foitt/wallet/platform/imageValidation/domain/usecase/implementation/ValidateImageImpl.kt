package ch.admin.foitt.wallet.platform.imageValidation.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.imageValidation.domain.model.ImageValidationError
import ch.admin.foitt.wallet.platform.imageValidation.domain.model.ValidateImageError
import ch.admin.foitt.wallet.platform.imageValidation.domain.usecase.ValidateImage
import ch.admin.foitt.wallet.platform.ssi.domain.model.ImageType
import ch.admin.foitt.wallet.platform.ssi.domain.model.ImageType.Companion.hasMagicNumber
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import javax.inject.Inject

internal class ValidateImageImpl @Inject constructor(
    private val environmentSetupRepository: EnvironmentSetupRepository,
) : ValidateImage {
    override fun invoke(
        mimeType: String,
        image: String?,
    ): Result<Unit, ValidateImageError> = binding {
        if (!environmentSetupRepository.isImageValidationEnabled) {
            return@binding
        }

        val imageType = runSuspendCatching {
            ImageType.getByType(mimeType)
        }.mapError {
            ImageValidationError.UnsupportedImageFormat
        }.bind()

        if (image == null) return@binding

        val imageWithoutPrefix = image.substringAfter(",")

        if (!imageType.hasMagicNumber(imageWithoutPrefix)) {
            Err(ImageValidationError.UnsupportedImageFormat).bind()
        }

        Ok(Unit)
    }
}
