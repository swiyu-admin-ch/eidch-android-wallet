package ch.admin.foitt.openid4vc.domain.model.anycredential

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import kotlinx.serialization.json.JsonObject
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

    fun getClaimsToSave(): JsonObject
    fun getClaimsForPresentation(): JsonObject
    fun getPathsForPresentation(requestedPaths: List<ClaimsPathPointer>): Set<ClaimsPathPointer>
    fun createVerifiableCredential(presentationPaths: List<ClaimsPathPointer>): String
}
