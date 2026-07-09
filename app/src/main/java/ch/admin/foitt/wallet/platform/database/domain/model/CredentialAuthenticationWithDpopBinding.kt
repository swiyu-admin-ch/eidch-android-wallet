package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.Embedded
import androidx.room.Relation
import ch.admin.foitt.openid4vc.domain.model.TokenType

data class CredentialAuthenticationWithDpopBinding(
    @Embedded
    val credentialAuthentication: CredentialAuthenticationEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "credentialAuthenticationId",
    )
    val dpopBinding: DpopBindingEntity?,
) {
    val tokenType: TokenType
        get() = credentialAuthentication.tokenType

    val accessToken: String
        get() = credentialAuthentication.accessToken

    val refreshToken: String?
        get() = credentialAuthentication.refreshToken
}
