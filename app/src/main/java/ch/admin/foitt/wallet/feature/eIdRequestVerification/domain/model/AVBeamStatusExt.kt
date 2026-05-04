package ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model

import androidx.annotation.StringRes
import ch.admin.foitt.avwrapper.AVBeamStatus
import ch.admin.foitt.wallet.R

@Suppress("CyclomaticComplexMethod")
@StringRes
fun AVBeamStatus.toTextRes(): Int? = when (this) {
    AVBeamStatus.MessageClear -> R.string.avbeam_notification_messageClear
    AVBeamStatus.QRCodeRecognitionStarted -> R.string.avbeam_notification_qrCodeRecognitionStarted
    AVBeamStatus.QRCodeExtractionDone -> R.string.avbeam_notification_qrCodeExtractionDone
    AVBeamStatus.QRCodeMoveFurther -> R.string.avbeam_notification_qrCodeMoveFurther
    AVBeamStatus.QRCodeMoveCloser -> R.string.avbeam_notification_qrCodeMoveCloser
    AVBeamStatus.QRCodeRecognitionStopped -> R.string.avbeam_notification_qrCodeRecognitionStopped
    AVBeamStatus.QRCodeCaptured -> R.string.avbeam_notification_qrCodeCaptured
    AVBeamStatus.IdRecognitionStarted -> R.string.avbeam_notification_idRecognitionStarted
    AVBeamStatus.IdRecognitionStopped -> R.string.avbeam_notification_idRecognitionStopped
    AVBeamStatus.IdFinalDataSet -> R.string.avbeam_notification_idFinalDataSet
    AVBeamStatus.IdRotateLess -> R.string.avbeam_notification_idRotateLess
    AVBeamStatus.IdMoveCloser -> R.string.avbeam_notification_idMoveCloser
    AVBeamStatus.IdMoveFurther -> R.string.avbeam_notification_idMoveFurther
    AVBeamStatus.IdDetectionDone -> R.string.avbeam_notification_idDetectionDone
    AVBeamStatus.IdMrzFound -> R.string.avbeam_notification_idMrzFound
    AVBeamStatus.IdMrzNotFound -> R.string.avbeam_notification_idMrzNotFound
    AVBeamStatus.IdDocMatched -> R.string.avbeam_notification_idDocMatched
    AVBeamStatus.IdDocNotMatched -> R.string.avbeam_notification_idDocNotMatched
    AVBeamStatus.IdNeedSecondPageForMrz -> R.string.avbeam_notification_idNeedSecondPageForMrz
    AVBeamStatus.IdNeedSecondPageForMatching -> R.string.avbeam_notification_idNeedSecondPageForMatching
    AVBeamStatus.IdNeedSamePageForMatching -> R.string.avbeam_notification_idNeedSamePageForMatching
    AVBeamStatus.WaitingForViz -> R.string.avbeam_notification_waitingForViz
    AVBeamStatus.IdDataForUploadSet -> R.string.avbeam_notification_idDataForUploadSet
    AVBeamStatus.IdNeedSamePageForMrz -> R.string.avbeam_notification_idNeedSamePageForMrz
    AVBeamStatus.IdBadPosition -> R.string.avbeam_notification_idBadPosition
    AVBeamStatus.IdHoldStill -> R.string.avbeam_notification_idHoldStill
    AVBeamStatus.IdRotate90 -> R.string.avbeam_notification_idRotate90
    AVBeamStatus.IdRotateScreen -> R.string.avbeam_notification_idRotateScreen
    AVBeamStatus.IdRotateScreenAndDoc -> R.string.avbeam_notification_idRotateScreenAndDoc
    AVBeamStatus.IdReflectionDetected -> R.string.avbeam_notification_idReflectionDetected
    AVBeamStatus.IdNotDetected -> R.string.avbeam_notification_idNotDetected
    AVBeamStatus.DocCapturingStarted -> R.string.avbeam_notification_docCapturingStarted
    AVBeamStatus.DocCapturingStopped -> R.string.avbeam_notification_docCapturingStopped
    AVBeamStatus.DocCaptured -> R.string.avbeam_notification_docCaptured
    AVBeamStatus.SignatureStarted -> R.string.avbeam_notification_signatureStarted
    AVBeamStatus.SignatureAccepted -> R.string.avbeam_notification_signatureAccepted
    AVBeamStatus.SignatureCleared -> R.string.avbeam_notification_signatureCleared
    AVBeamStatus.SignatureStopped -> R.string.avbeam_notification_signatureStopped
    AVBeamStatus.SignatureDrawingStarted -> R.string.avbeam_notification_signatureDrawingStarted
    AVBeamStatus.FaceCapturingStarted -> R.string.avbeam_notification_faceCapturingStarted
    AVBeamStatus.FaceCapturingStopped -> R.string.avbeam_notification_faceCapturingStopped
    AVBeamStatus.FaceCaptured -> R.string.avbeam_notification_faceCaptured
    AVBeamStatus.FaceCaptureMoveRight -> R.string.avbeam_notification_faceCaptureMoveRight
    AVBeamStatus.FaceCaptureMoveLeft -> R.string.avbeam_notification_faceCaptureMoveLeft
    AVBeamStatus.FaceCaptureTiltRight -> R.string.avbeam_notification_faceCaptureTiltRight
    AVBeamStatus.FaceCaptureTiltLeft -> R.string.avbeam_notification_faceCaptureTiltLeft
    AVBeamStatus.FaceCaptureBlink -> R.string.avbeam_notification_faceCaptureBlink
    AVBeamStatus.FaceCaptureWait -> R.string.avbeam_notification_faceCaptureTiltWait
    AVBeamStatus.FaceCaptureSmile -> R.string.avbeam_notification_faceCaptureTiltSmile
    AVBeamStatus.FaceCaptureLivenessFailed -> R.string.avbeam_notification_faceCaptureLivenessFailed
    AVBeamStatus.FaceVerificationStarted -> R.string.avbeam_notification_faceVerificationStarted
    AVBeamStatus.FaceVerificationStopped -> R.string.avbeam_notification_faceVerificationStopped
    AVBeamStatus.FaceVerified -> R.string.avbeam_notification_faceVerified
    AVBeamStatus.FaceVerificationFailed -> R.string.avbeam_notification_faceVerificationFailed
    AVBeamStatus.DataEncryptionStarted -> R.string.avbeam_notification_dataEncryptionStarted
    AVBeamStatus.DataEncryptionStopped -> R.string.avbeam_notification_dataEncryptionStopped
    AVBeamStatus.DataEncrypted -> R.string.avbeam_notification_dataEncrypted
    AVBeamStatus.DataDecryptionStarted -> R.string.avbeam_notification_dataDecryptionStarted
    AVBeamStatus.DataDecryptionStopped -> R.string.avbeam_notification_dataDecryptionStopped
    AVBeamStatus.DataDecrypted -> R.string.avbeam_notification_dataDecrypted
    AVBeamStatus.IdNeedSecurityFeatures -> R.string.avbeam_notification_idNeedSecurityFeatures
    AVBeamStatus.SecurityFeaturesStarted -> R.string.avbeam_notification_securityFeaturesStarted
    AVBeamStatus.SecurityFeaturesStopped -> R.string.avbeam_notification_securityFeaturesStopped
    AVBeamStatus.SecurityFeaturesReady -> R.string.avbeam_notification_securityFeaturesReady
    AVBeamStatus.SecurityFeaturesTracking -> R.string.avbeam_notification_securityFeaturesTracking
    AVBeamStatus.SecurityFeaturesTrackingLost -> R.string.avbeam_notification_securityFeaturesTrackingLost
    AVBeamStatus.StreamingStarted -> R.string.avbeam_notification_streamingStarted
    AVBeamStatus.NfcAuthenticationPass_DEPRECATED -> R.string.avbeam_notification_nfcAuthenticationPassDeprecated
    AVBeamStatus.NfcDataReadingStart -> R.string.avbeam_notification_nfcDataReadingStart
    AVBeamStatus.NfcDataReadingEndSuccess -> R.string.avbeam_notification_nfcDataReadingEndSuccess
    AVBeamStatus.NfcDataReadingEndFail -> R.string.avbeam_notification_nfcDataReadingEndFail
    AVBeamStatus.NfcPhotoReadingStart_DEPRECATED -> R.string.avbeam_notification_nfcPhotoReadingStartDeprecated
    AVBeamStatus.NfcPhotoReadingFinish_DEPRECATED -> R.string.avbeam_notification_nfcPhotoReadingFinishDeprecated
    AVBeamStatus.NfcReadingStopped -> R.string.avbeam_notification_nfcReadingStopped
    AVBeamStatus.NfcUnavailable -> R.string.avbeam_notification_nfcUnavailable
    AVBeamStatus.NfcReadAtrInfo -> R.string.avbeam_notification_nfcReadAtrInfo
    AVBeamStatus.NfcAccessControl -> R.string.avbeam_notification_nfcAccessControl
    AVBeamStatus.NfcReadSod -> R.string.avbeam_notification_nfcReadSod
    AVBeamStatus.NfcReadDg14 -> R.string.avbeam_notification_nfcReadDg14
    AVBeamStatus.NfcChipAuthentication -> R.string.avbeam_notification_nfcChipAuthentication
    AVBeamStatus.NfcReadDg15 -> R.string.avbeam_notification_nfcReadDg15
    AVBeamStatus.NfcActiveAuthentication -> R.string.avbeam_notification_nfcActiveAuthentication
    AVBeamStatus.NfcReadDg1 -> R.string.avbeam_notification_nfcReadDg1
    AVBeamStatus.NfcReadDg2 -> R.string.avbeam_notification_nfcReadDg2
    AVBeamStatus.NfcReadDg7 -> R.string.avbeam_notification_nfcReadDg7
    AVBeamStatus.NfcReadDg11 -> R.string.avbeam_notification_nfcReadDg11
    AVBeamStatus.NfcReadDg12 -> R.string.avbeam_notification_nfcReadDg12
    AVBeamStatus.NfcPassiveAuthentication -> R.string.avbeam_notification_nfcPassiveAuthentication
    AVBeamStatus.NfcChipClonedDetectionStart -> R.string.avbeam_notification_nfcChipClonedDetectionStart
    AVBeamStatus.NfcChipClonedDetectionEndSuccess -> R.string.avbeam_notification_nfcChipClonedDetectionEndSuccess
    AVBeamStatus.NfcConnectingToServer -> R.string.avbeam_notification_nfcConnectingToServer
    AVBeamStatus.DocRecordingStarted -> R.string.avbeam_notification_docRecordingStarted
    AVBeamStatus.DocRecordingStopped -> R.string.avbeam_notification_docRecordingStopped
    AVBeamStatus.DocRecorded -> R.string.avbeam_notification_docRecorded
    AVBeamStatus.DeviceIntegrityCheckSuccess -> R.string.avbeam_notification_deviceIntegrityCheckSuccess
    AVBeamStatus.DeviceIntegrityCheckFailed -> R.string.avbeam_notification_deviceIntegrityCheckFailed
    AVBeamStatus.Unknown -> R.string.avbeam_error_unknown
    else -> null
}
