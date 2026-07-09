package ch.admin.foitt.openid4vc.domain.model.anycredential

import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential

data class AnyVerifiedBatchCredential(
    val accessToken: String,
    val refreshToken: String?,
    val dpopKeyBinding: KeyBinding?,
    val vcSdJwtCredentials: List<VcSdJwtCredential>,
) : AnyCredentialResult
