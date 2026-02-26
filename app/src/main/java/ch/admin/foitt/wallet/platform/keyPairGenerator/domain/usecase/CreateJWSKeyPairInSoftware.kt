package ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.model.CreateJWSKeyPairError
import com.github.michaelbull.result.Result

interface CreateJWSKeyPairInSoftware {
    @CheckResult
    suspend operator fun invoke(
        signingAlgorithm: SigningAlgorithm,
    ): Result<JWSKeyPair, CreateJWSKeyPairError>
}
