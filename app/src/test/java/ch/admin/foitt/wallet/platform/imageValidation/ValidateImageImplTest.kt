package ch.admin.foitt.wallet.platform.imageValidation

import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.imageValidation.domain.usecase.ValidateImage
import ch.admin.foitt.wallet.platform.imageValidation.domain.usecase.implementation.ValidateImageImpl
import ch.admin.foitt.wallet.platform.ssi.domain.model.ImageType
import ch.admin.foitt.wallet.util.assertErr
import ch.admin.foitt.wallet.util.assertOk
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class ValidateImageImplTest {

    @MockK
    private lateinit var mockEnvironmentSetupRepository: EnvironmentSetupRepository

    private lateinit var useCase: ValidateImage

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = ValidateImageImpl(mockEnvironmentSetupRepository)

        every {
            mockEnvironmentSetupRepository.isImageValidationEnabled
        } returns true
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @ParameterizedTest
    @EnumSource(ImageType::class)
    fun `A supported image passes validation`(imageType: ImageType) = runTest {
        useCase(
            mimeType = imageType.mimeType,
            image = testDataMap[imageType],
        ).assertOk()
    }

    @ParameterizedTest
    @MethodSource("generateImageMimeType")
    fun `An unsupported mimeType fails validation`(mimeType: String) = runTest {
        useCase(
            mimeType = mimeType,
            image = testJpeg,
        ).assertErr()
    }

    @Test
    fun `An image without magic number fails validation`() = runTest {
        useCase(
            mimeType = ImageType.PNG.mimeType,
            image = wrongImage,
        ).assertErr()
    }

    @Test
    fun `No image validation occurs if the image validation flag is disabled`() = runTest {
        every {
            mockEnvironmentSetupRepository.isImageValidationEnabled
        } returns false

        useCase(
            mimeType = ImageType.PNG.mimeType,
            image = wrongImage,
        ).assertOk()
    }

    private companion object {
        val testJpeg = """
            data:image/jpeg;base64,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
        """.trimIndent()

        val testPng = """
            data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAGkAAACkCAYAAACU9ABQAAAACXBIWXMAABYlAAAWJQFJUiTwAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAADwvSURBVHgBrX3LznRZctWKkx8tJoZqCSQPa8AACyT7EcozZi7EyBfZ7RFXCfsJmn6D9hzRZSFhiQnmCdzMGCBRSCCm/6Bt9czNZUB3/7mDPHvHilixz/nK5W5nVf6Zec4+t1h7rVg79sn8DH+Jx7d+7/NP8H+f3zr8+GUfHz8zOz6B+yd2rnTA/PXPfL7ev/4739t8f771WI+57Hw9/zFul/uoz9revNqe+zYfOHT5cDxe7x/PX8Bf+/Ev4cDa7hiv5+sEzraPV5vjbPPa/jHXne99Lj+XvY2x9vF6fTvbv9a9xecHxloer4/xXMt8rTvGx9fr83Xctc7i/eEfX8/Xqz0/mPmH1/IvH+b/8W
        """.trimIndent()

        val wrongImage = """
            data:image/png;base64,iVBORwKGgoAAANSUhEUgAAAGkAAACkCAYAAACU9ABQAAAACXBIWXMAABYlAAAWJQFJUiTwAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAADwvSURBVHgBrX3LznRZctWKkx8tJoZqCSQPa8AACyT7EcozZi7EyBfZ7RFXCfsJmn6D9hzRZSFhiQnmCdzMGCBRSCCm/6Bt9czNZUB3/7mDPHvHilixz/nK5W5nVf6Zec4+t1h7rVg79sn8DH+Jx7d+7/NP8H+f3zr8+GUfHz8zOz6B+yd2rnTA/PXPfL7ev/4739t8f771WI+57Hw9/zFul/uoz9revNqe+zYfOHT5cDxe7x/PX8Bf+/Ev4cDa7hiv5+sEzraPV5vjbPPa/jHXne99Lj+XvY2x9vF6fTvbv9a9xecHxloer4/xXMt8rTvGx9fr83Xctc7i/eEfX8/Xqz0/mPmH1/IvH+b/8W
        """.trimIndent()

        val testDataMap = mapOf(
            ImageType.JPEG to testJpeg,
            ImageType.PNG to testPng,
        )

        @JvmStatic
        fun generateImageMimeType(): Stream<Arguments> = Stream.of(
            Arguments.of(
                "image/tiff",
                "image/svg+xml",
                "image/webp",
                "text/plain",
                "application/json",
            )
        )
    }
}
