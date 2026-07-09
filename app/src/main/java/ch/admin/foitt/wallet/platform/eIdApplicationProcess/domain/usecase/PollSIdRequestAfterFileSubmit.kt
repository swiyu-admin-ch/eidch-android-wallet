package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase

fun interface PollSIdRequestAfterFileSubmit {
    suspend operator fun invoke()
}
