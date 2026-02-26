package ch.admin.foitt.openid4vc.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.ValidateIssuerMetadataJwtError
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import com.github.michaelbull.result.Result

internal interface ValidateIssuerMetadataJwt {
    @CheckResult
    suspend operator fun invoke(credentialIssuerIdentifier: String, jwt: Jwt, type: String?): Result<Unit, ValidateIssuerMetadataJwtError>
}
