package ch.admin.foitt.wallet.platform.login.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.ResetBiometrics
import ch.admin.foitt.wallet.platform.login.domain.model.CanUseBiometricsForLoginResult
import ch.admin.foitt.wallet.platform.login.domain.usecase.CanUseBiometricsForLogin
import ch.admin.foitt.wallet.platform.login.domain.usecase.NavigateToLogin
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import javax.inject.Inject

class NavigateToLoginImpl @Inject constructor(
    private val canUseBiometricsForLogin: CanUseBiometricsForLogin,
    private val resetBiometrics: ResetBiometrics,
) : NavigateToLogin {
    override suspend fun invoke(): Destination {
        return when (canUseBiometricsForLogin()) {
            CanUseBiometricsForLoginResult.Usable -> Destination.BiometricLoginScreen
            CanUseBiometricsForLoginResult.NotSetUpInApp -> Destination.PassphraseLoginScreen(biometricsLocked = false)
            else -> {
                resetBiometrics()
                Destination.PassphraseLoginScreen(biometricsLocked = false)
            }
        }
    }
}
