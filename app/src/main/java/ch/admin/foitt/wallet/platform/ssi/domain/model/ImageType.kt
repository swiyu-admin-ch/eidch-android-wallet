package ch.admin.foitt.wallet.platform.ssi.domain.model

enum class ImageType(val mimeType: String) {
    PNG("image/png"),
    JPEG("image/jpeg");

    companion object {
        private val base64MagicNumbers = mapOf(
            // PNG magic numbers, base64 encoded: [0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A]
            PNG to "iVBORw0KGgo",
            // JPEG magic numbers, base64 encoded: [0xFF, 0xD8, 0xFF]
            JPEG to "/9j/",
        )

        fun getByType(mimeType: String): ImageType = entries.first { it.mimeType == mimeType }

        val ImageType.base64MagicNumber: String
            get() = base64MagicNumbers.getValue(this)

        fun ImageType.hasMagicNumber(base64ImageData: String) =
            base64ImageData.startsWith(this.base64MagicNumber)

        fun isValidImageDataUri(input: String): Boolean = ImageType.entries.any { imageType ->
            input.startsWith("data:${imageType.mimeType};base64,")
        }
    }
}
