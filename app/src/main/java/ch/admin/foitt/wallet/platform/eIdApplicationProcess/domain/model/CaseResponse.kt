package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CaseResponse(
    @SerialName("caseId")
    val caseId: String,
    @SerialName("surname")
    val surname: String,
    @SerialName("givenNames")
    val givenNames: String,
    @SerialName("dateOfBirth")
    val dateOfBirth: String,
    @SerialName("identityType")
    val identityType: IdentityType,
    @SerialName("identityNumber")
    val identityNumber: String,
    @SerialName("validUntil")
    val validUntil: String,
    @SerialName("legalRepresentant")
    val legalRepresentant: Boolean,
    @SerialName("email")
    val email: String?
)

enum class IdentityType {
    SWISS_IDK,
    SWISS_PASS,
    FOREIGNER_PERMIT,
}

fun IdentityType.toEIdDocumentType() = when (this) {
    IdentityType.SWISS_IDK -> EIdUiDocumentType.IDENTITY_CARD
    IdentityType.FOREIGNER_PERMIT -> EIdUiDocumentType.RESIDENT_PERMIT
    IdentityType.SWISS_PASS -> EIdUiDocumentType.PASSPORT
}
