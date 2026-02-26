package ch.admin.foitt.wallet.platform.credential.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.wallet.platform.credential.domain.model.GetAnyCredentialsError
import com.github.michaelbull.result.Result

fun interface GetAllAnyCredentials {
    suspend operator fun invoke(): Result<List<AnyCredential>, GetAnyCredentialsError>
}
