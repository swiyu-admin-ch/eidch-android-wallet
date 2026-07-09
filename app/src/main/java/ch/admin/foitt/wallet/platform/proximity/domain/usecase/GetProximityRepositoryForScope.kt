package ch.admin.foitt.wallet.platform.proximity.domain.usecase

import ch.admin.foitt.wallet.platform.proximity.domain.model.domain.repository.ProximityRepository

fun interface GetProximityRepositoryForScope {
    operator fun invoke(): ProximityRepository
}
