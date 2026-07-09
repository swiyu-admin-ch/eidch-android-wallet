package ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation

import ch.admin.foitt.avwrapper.AVBeamError
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.TextKeyType
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.CreateSDKErrorTextKeys
import javax.inject.Inject

class CreateSDKErrorTextKeysImpl @Inject constructor() : CreateSDKErrorTextKeys {
    @Suppress("CyclomaticComplexMethod")
    override suspend fun invoke(errorCode: AVBeamError, textKeyType: TextKeyType): Int {
        return when (errorCode to textKeyType) {
            AVBeamError.IdNoData to TextKeyType.TITLE -> R.string.avbeam_error_idNoData_title
            AVBeamError.IdNoData to TextKeyType.CONTENT -> R.string.avbeam_error_idNoData_content

            AVBeamError.IdNotDetected to TextKeyType.TITLE -> R.string.avbeam_error_idNotDetected_title
            AVBeamError.IdNotDetected to TextKeyType.CONTENT -> R.string.avbeam_error_idNotDetected_content

            AVBeamError.FaceCaptureIntegrityCheckFailed to TextKeyType.TITLE -> R.string.avbeam_error_faceCaptureIntegrityCheckFailed_title
            AVBeamError.FaceCaptureIntegrityCheckFailed to TextKeyType.CONTENT ->
                R.string.avbeam_error_faceCaptureIntegrityCheckFailed_content

            AVBeamError.FaceNotRecognised to TextKeyType.TITLE -> R.string.avbeam_error_faceNotRecognized_title
            AVBeamError.FaceNotRecognised to TextKeyType.CONTENT -> R.string.avbeam_error_faceNotRecognized_content

            AVBeamError.IdBadMrzFields to TextKeyType.TITLE -> R.string.avbeam_error_idBadMrzFields_title
            AVBeamError.IdBadMrzFields to TextKeyType.CONTENT -> R.string.avbeam_error_idBadMrzFields_content

            AVBeamError.IdExpired to TextKeyType.TITLE -> R.string.avbeam_error_idExpired_title
            AVBeamError.IdExpired to TextKeyType.CONTENT -> R.string.avbeam_error_idExpired_content

            AVBeamError.IdMatchingFailed to TextKeyType.TITLE -> R.string.avbeam_error_idMatchingFailed_title
            AVBeamError.IdMatchingFailed to TextKeyType.CONTENT -> R.string.avbeam_error_idMatchingFailed_content

            AVBeamError.IdNotInList to TextKeyType.TITLE -> R.string.avbeam_error_idNotInList_title
            AVBeamError.IdNotInList to TextKeyType.CONTENT -> R.string.avbeam_error_idNotInList_content

            AVBeamError.IdPageMissing to TextKeyType.TITLE -> R.string.avbeam_error_idPageMissing_title
            AVBeamError.IdPageMissing to TextKeyType.CONTENT -> R.string.avbeam_error_idPageMissing_content

            AVBeamError.ImageBlured to TextKeyType.TITLE -> R.string.avbeam_error_imageBlurred_title
            AVBeamError.ImageBlured to TextKeyType.CONTENT -> R.string.avbeam_error_imageBlurred_content

            AVBeamError.MrzNotDetected to TextKeyType.TITLE -> R.string.avbeam_error_mrzNotDetected_title
            AVBeamError.MrzNotDetected to TextKeyType.CONTENT -> R.string.avbeam_error_mrzNotDetected_content

            AVBeamError.Reflection to TextKeyType.TITLE -> R.string.avbeam_error_reflection_title
            AVBeamError.Reflection to TextKeyType.CONTENT -> R.string.avbeam_error_reflection_content

            AVBeamError.UnsupportedCameraResolution to TextKeyType.TITLE -> R.string.avbeam_error_unsupportedCameraResolution_title
            AVBeamError.UnsupportedCameraResolution to TextKeyType.CONTENT -> R.string.avbeam_error_unsupportedCameraResolution_content

            AVBeamError.UnsupportedVideoConfiguration to TextKeyType.TITLE -> R.string.avbeam_error_unsupportedVideoConfiguration_title
            AVBeamError.UnsupportedVideoConfiguration to TextKeyType.CONTENT -> R.string.avbeam_error_unsupportedVideoConfiguration_content

            else -> {
                if (textKeyType == TextKeyType.TITLE) {
                    R.string.tk_error_generic_primary
                } else {
                    R.string.tk_error_generic_secondary
                }
            }
        }
    }
}
