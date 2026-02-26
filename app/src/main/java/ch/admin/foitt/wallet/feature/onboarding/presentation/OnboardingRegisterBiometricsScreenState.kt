package ch.admin.foitt.wallet.feature.onboarding.presentation

sealed interface OnboardingRegisterBiometricsScreenState {
    data object Initial : OnboardingRegisterBiometricsScreenState
    data object Available : OnboardingRegisterBiometricsScreenState
    data object Lockout : OnboardingRegisterBiometricsScreenState
    data object Error : OnboardingRegisterBiometricsScreenState
    data object DisabledOnDevice : OnboardingRegisterBiometricsScreenState
    data object DisabledForApp : OnboardingRegisterBiometricsScreenState
}
