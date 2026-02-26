package ch.admin.foitt.openid4vc.domain.model

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyDeferredCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import java.net.URL

sealed interface FetchCredentialResult

data class DeferredCredential(
    override val format: CredentialFormat,
    override val transactionId: String,
    override val accessToken: String,
    override val endpoint: URL,
    override val pollInterval: Int,
    override val keyBindings: List<KeyBinding>?,
) : FetchCredentialResult, AnyDeferredCredential

data class BatchCredential(
    val refreshToken: String?,
    val credentials: List<VerifiableCredential>,
) : FetchCredentialResult

data class VerifiableCredential(
    val credential: String,
    val keyBinding: KeyBinding?,
) : FetchCredentialResult
