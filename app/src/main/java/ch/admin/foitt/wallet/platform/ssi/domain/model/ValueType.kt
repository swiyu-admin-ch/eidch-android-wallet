package ch.admin.foitt.wallet.platform.ssi.domain.model

enum class ValueType(val value: String) {
    BOOLEAN("bool"),
    DATETIME("datetime"),
    IMAGE("image"),
    NUMERIC("numeric"),
    STRING("string"),
    UNSUPPORTED("unsupported");

    companion object {
        fun isImageMimeType(mimeType: String): Boolean = mimeType.startsWith("image/")

        private val mapping: Map<String, ValueType> = ValueType.entries
            .filterNot { it.value == IMAGE.value } // do not map generic ValueType.IMAGE value
            .associateBy { it.value } +
            ImageType.entries.associate { it.mimeType to IMAGE } // map specific ImageType mime types to ValueType.IMAGE

        fun getByType(type: String?): ValueType = mapping.getOrDefault(type, UNSUPPORTED)
    }
}
