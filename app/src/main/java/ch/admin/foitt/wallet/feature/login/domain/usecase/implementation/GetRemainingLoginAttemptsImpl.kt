package ch.admin.foitt.wallet.feature.login.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.login.domain.Constants.MAX_LOGIN_ATTEMPTS
import ch.admin.foitt.wallet.feature.login.domain.repository.LoginAttemptsRepository
import ch.admin.foitt.wallet.feature.login.domain.usecase.GetRemainingLoginAttempts
import javax.inject.Inject

class GetRemainingLoginAttemptsImpl @Inject constructor(
    private val loginAttemptsRepository: LoginAttemptsRepository,
) : GetRemainingLoginAttempts {
    override fun invoke() = MAX_LOGIN_ATTEMPTS - loginAttemptsRepository.getAttempts()
}
