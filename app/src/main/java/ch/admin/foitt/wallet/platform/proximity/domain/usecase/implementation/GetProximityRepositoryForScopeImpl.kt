package ch.admin.foitt.wallet.platform.proximity.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponentManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.proximity.di.ProximityRepositoryEntryPoint
import ch.admin.foitt.wallet.platform.proximity.domain.model.domain.repository.ProximityRepository
import ch.admin.foitt.wallet.platform.proximity.domain.usecase.GetProximityRepositoryForScope
import javax.inject.Inject

internal class GetProximityRepositoryForScopeImpl @Inject constructor(
    private val destinationScopedComponentManager: DestinationScopedComponentManager,
) : GetProximityRepositoryForScope {
    override fun invoke(): ProximityRepository {
        return destinationScopedComponentManager.getEntryPoint(
            entryPointClass = ProximityRepositoryEntryPoint::class.java,
            componentScope = ComponentScope.Verifier,
        ).proximityRepository()
    }
}
