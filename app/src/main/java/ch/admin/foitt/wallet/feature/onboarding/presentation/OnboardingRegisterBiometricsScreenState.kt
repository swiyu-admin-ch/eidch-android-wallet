package ch.admin.foitt.wallet.feature.onboarding.presentation

sealed interface OnboardingRegisterBiometricsScreenState {
    data object Initial : OnboardingRegisterBiometricsScreenState
    data object Available : OnboardingRegisterBiometricsScreenState
    data class Lockout(val errorCode: String) : OnboardingRegisterBiometricsScreenState
    data class Error(val errorCode: String) : OnboardingRegisterBiometricsScreenState
    data object DisabledOnDevice : OnboardingRegisterBiometricsScreenState
    data object DisabledForApp : OnboardingRegisterBiometricsScreenState
}
