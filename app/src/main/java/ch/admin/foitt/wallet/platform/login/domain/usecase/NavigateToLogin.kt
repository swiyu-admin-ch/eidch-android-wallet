package ch.admin.foitt.wallet.platform.login.domain.usecase

import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination

fun interface NavigateToLogin {
    suspend operator fun invoke(): Destination
}
