package ch.admin.foitt.wallet.platform.actorMetadata.domain.model

import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorMetaDataError.Unexpected
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.GetAllAnyCredentialsByCredentialIdError
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialIssuerDisplayRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError

interface ActorMetaDataError {
    data class Unexpected(val cause: Throwable?) :
        FetchAndCacheIssuerDisplayDataError
}

sealed interface FetchAndCacheIssuerDisplayDataError

fun GetAllAnyCredentialsByCredentialIdError.toFetchAndCacheIssuerDisplayDataError(): FetchAndCacheIssuerDisplayDataError = when (this) {
    is CredentialError.Unexpected -> Unexpected(cause)
}

fun CredentialIssuerDisplayRepositoryError.toFetchAndCacheIssuerDisplayDataError(): FetchAndCacheIssuerDisplayDataError = when (this) {
    is SsiError.Unexpected -> Unexpected(cause)
}
