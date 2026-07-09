package ch.admin.foitt.openid4vc.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.CreateJwkError
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import com.github.michaelbull.result.Result
import java.security.KeyPair

interface CreateJwk {
    @CheckResult
    suspend operator fun invoke(
        keyPair: KeyPair,
        algorithm: SigningAlgorithm,
    ): Result<String, CreateJwkError>
}
