package ch.admin.foitt.wallet.platform.credential.domain.usecase

interface RefreshVerifiableCredentials {
    suspend operator fun invoke()
}
