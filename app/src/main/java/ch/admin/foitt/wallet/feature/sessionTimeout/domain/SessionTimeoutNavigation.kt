package ch.admin.foitt.wallet.feature.sessionTimeout.domain

import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination

interface SessionTimeoutNavigation {
    suspend operator fun invoke(): Destination?
}
