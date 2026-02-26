package ch.admin.foitt.wallet.platform.holderBinding.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.ProofTypeConfig
import ch.admin.foitt.openid4vc.domain.model.keyBinding.BindingKeyPair
import ch.admin.foitt.wallet.platform.holderBinding.domain.model.GenerateProofKeyPairError
import com.github.michaelbull.result.Result

fun interface GenerateProofKeyPairs {
    @CheckResult
    suspend operator fun invoke(
        amount: Int,
        proofTypeConfig: ProofTypeConfig,
    ): Result<List<BindingKeyPair>, GenerateProofKeyPairError>
}
