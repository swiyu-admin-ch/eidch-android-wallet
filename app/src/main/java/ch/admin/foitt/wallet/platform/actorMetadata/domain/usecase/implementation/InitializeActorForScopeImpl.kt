package ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.actorMetadata.di.ActorRepositoryEntryPoint
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.InitializeActorForScope
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponentManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import javax.inject.Inject

internal class InitializeActorForScopeImpl @Inject constructor(
    private val destinationScopedComponentManager: DestinationScopedComponentManager,
) : InitializeActorForScope {
    override suspend operator fun invoke(
        actorDisplayData: ActorDisplayData,
        componentScope: ComponentScope,
    ) {
        if (!destinationScopedComponentManager.isInScope(componentScope)) return

        val actorRepository = destinationScopedComponentManager.getEntryPoint(
            entryPointClass = ActorRepositoryEntryPoint::class.java,
            componentScope = componentScope,
        ).actorRepository()
        actorRepository.setActor(actorDisplayData)
    }
}
