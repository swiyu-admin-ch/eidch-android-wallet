package ch.admin.foitt.wallet.feature.eIdRequestVerification

import ch.admin.foitt.avwrapper.AVBeamError
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.TextKeyType
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation.CreateSDKErrorTextKeysImpl
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class CreateSDKErrorTextKeysImplTest {

    private val useCase = CreateSDKErrorTextKeysImpl()

    @ParameterizedTest
    @MethodSource("errorMappingProvider")
    fun `invoke should return correct resource for error and type`(
        errorCode: AVBeamError,
        textKeyType: TextKeyType,
        expectedResId: Int
    ) = runTest {
        val result = useCase(errorCode, textKeyType)
        Assertions.assertEquals(expectedResId, result)
    }

    @Test
    fun `invoke with unknown error and TITLE should return generic primary error resource`() =
        runTest {
            val result = useCase(AVBeamError.Unknown, TextKeyType.TITLE)
            Assertions.assertEquals(R.string.tk_error_generic_primary, result)
        }

    @Test
    fun `invoke with unknown error and CONTENT should return generic secondary error resource`() =
        runTest {
            val result = useCase(AVBeamError.Unknown, TextKeyType.CONTENT)
            Assertions.assertEquals(R.string.tk_error_generic_secondary, result)
        }

    companion object {
        @JvmStatic
        fun errorMappingProvider(): Stream<Arguments> = Stream.of(
            Arguments.of(AVBeamError.IdNoData, TextKeyType.TITLE, R.string.avbeam_error_idNoData_title),
            Arguments.of(AVBeamError.IdNoData, TextKeyType.CONTENT, R.string.avbeam_error_idNoData_content),
            Arguments.of(AVBeamError.IdNotDetected, TextKeyType.TITLE, R.string.avbeam_error_idNotDetected_title),
            Arguments.of(AVBeamError.IdNotDetected, TextKeyType.CONTENT, R.string.avbeam_error_idNotDetected_content),
            Arguments.of(
                AVBeamError.FaceCaptureIntegrityCheckFailed,
                TextKeyType.TITLE,
                R.string.avbeam_error_faceCaptureIntegrityCheckFailed_title
            ),
            Arguments.of(
                AVBeamError.FaceCaptureIntegrityCheckFailed,
                TextKeyType.CONTENT,
                R.string.avbeam_error_faceCaptureIntegrityCheckFailed_content
            ),
            Arguments.of(AVBeamError.FaceNotRecognised, TextKeyType.TITLE, R.string.avbeam_error_faceNotRecognized_title),
            Arguments.of(AVBeamError.FaceNotRecognised, TextKeyType.CONTENT, R.string.avbeam_error_faceNotRecognized_content),
            Arguments.of(AVBeamError.IdBadMrzFields, TextKeyType.TITLE, R.string.avbeam_error_idBadMrzFields_title),
            Arguments.of(AVBeamError.IdBadMrzFields, TextKeyType.CONTENT, R.string.avbeam_error_idBadMrzFields_content),
            Arguments.of(AVBeamError.IdExpired, TextKeyType.TITLE, R.string.avbeam_error_idExpired_title),
            Arguments.of(AVBeamError.IdExpired, TextKeyType.CONTENT, R.string.avbeam_error_idExpired_content),
            Arguments.of(AVBeamError.IdMatchingFailed, TextKeyType.TITLE, R.string.avbeam_error_idMatchingFailed_title),
            Arguments.of(AVBeamError.IdMatchingFailed, TextKeyType.CONTENT, R.string.avbeam_error_idMatchingFailed_content),
            Arguments.of(AVBeamError.IdNotInList, TextKeyType.TITLE, R.string.avbeam_error_idNotInList_title),
            Arguments.of(AVBeamError.IdNotInList, TextKeyType.CONTENT, R.string.avbeam_error_idNotInList_content),
            Arguments.of(AVBeamError.IdPageMissing, TextKeyType.TITLE, R.string.avbeam_error_idPageMissing_title),
            Arguments.of(AVBeamError.IdPageMissing, TextKeyType.CONTENT, R.string.avbeam_error_idPageMissing_content),
            Arguments.of(AVBeamError.ImageBlured, TextKeyType.TITLE, R.string.avbeam_error_imageBlurred_title),
            Arguments.of(AVBeamError.ImageBlured, TextKeyType.CONTENT, R.string.avbeam_error_imageBlurred_content),
            Arguments.of(AVBeamError.MrzNotDetected, TextKeyType.TITLE, R.string.avbeam_error_mrzNotDetected_title),
            Arguments.of(AVBeamError.MrzNotDetected, TextKeyType.CONTENT, R.string.avbeam_error_mrzNotDetected_content),
            Arguments.of(AVBeamError.Reflection, TextKeyType.TITLE, R.string.avbeam_error_reflection_title),
            Arguments.of(AVBeamError.Reflection, TextKeyType.CONTENT, R.string.avbeam_error_reflection_content),
            Arguments.of(
                AVBeamError.UnsupportedCameraResolution,
                TextKeyType.TITLE,
                R.string.avbeam_error_unsupportedCameraResolution_title
            ),
            Arguments.of(
                AVBeamError.UnsupportedCameraResolution,
                TextKeyType.CONTENT,
                R.string.avbeam_error_unsupportedCameraResolution_content
            ),
            Arguments.of(
                AVBeamError.UnsupportedVideoConfiguration,
                TextKeyType.TITLE,
                R.string.avbeam_error_unsupportedVideoConfiguration_title
            ),
            Arguments.of(
                AVBeamError.UnsupportedVideoConfiguration,
                TextKeyType.CONTENT,
                R.string.avbeam_error_unsupportedVideoConfiguration_content
            ),
        )
    }
}
