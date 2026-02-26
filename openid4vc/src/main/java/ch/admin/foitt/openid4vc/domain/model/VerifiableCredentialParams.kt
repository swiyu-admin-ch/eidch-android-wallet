package ch.admin.foitt.openid4vc.domain.model

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.Grant
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.ProofTypeConfig
import java.net.URL

data class VerifiableCredentialParams(
    val proofTypeConfig: ProofTypeConfig?,
    val tokenEndpoint: URL,
    val grants: Grant,
    val issuerEndpoint: URL,
    val credentialEndpoint: URL,
    val deferredCredentialEndpoint: URL?,
    val credentialConfiguration: AnyCredentialConfiguration,
    val nonceEndpoint: URL?,
    val isBatch: Boolean,
)
