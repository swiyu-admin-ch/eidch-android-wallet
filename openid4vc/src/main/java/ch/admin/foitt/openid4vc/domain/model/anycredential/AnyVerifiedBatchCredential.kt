package ch.admin.foitt.openid4vc.domain.model.anycredential

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential

data class AnyVerifiedBatchCredential(
    val refreshToken: String?,
    val vcSdJwtCredentials: List<VcSdJwtCredential>,
) : AnyCredentialResult
