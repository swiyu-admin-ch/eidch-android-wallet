package ch.admin.foitt.openid4vc.domain.model.anycredential

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential

data class AnyVerifiedCredential(
    val vcSdJwtCredential: VcSdJwtCredential,
) : AnyCredentialResult
