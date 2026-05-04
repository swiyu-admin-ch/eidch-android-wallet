package ch.admin.foitt.wallet.platform.imageValidation.domain.model

interface ImageValidationError {
    data object UnsupportedImageFormat : ValidateImageError
}

sealed interface ValidateImageError
