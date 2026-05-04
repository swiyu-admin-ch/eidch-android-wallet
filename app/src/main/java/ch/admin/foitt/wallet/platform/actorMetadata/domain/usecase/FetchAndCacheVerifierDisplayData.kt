package ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest

fun interface FetchAndCacheVerifierDisplayData {
    suspend operator fun invoke(
        authorizationRequest: AuthorizationRequest,
    )
}
