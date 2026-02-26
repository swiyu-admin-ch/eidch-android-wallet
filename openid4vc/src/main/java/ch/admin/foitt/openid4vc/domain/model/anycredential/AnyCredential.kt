package ch.admin.foitt.openid4vc.domain.model.anycredential

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import kotlinx.serialization.json.JsonElement
import java.time.Instant

interface AnyCredential {
    val id: Long?
    val keyBinding: KeyBinding?
    val payload: String
    val format: CredentialFormat
    val claimsPath: String
    val validity: Validity
    val issuer: String
    val validFromInstant: Instant?
    val validUntilInstant: Instant?
    val vcSchemaId: String

    fun getClaimsToSave(): JsonElement
    fun getClaimsForPresentation(): JsonElement
    fun createVerifiableCredential(requestedFieldKeys: List<String>): String
}
