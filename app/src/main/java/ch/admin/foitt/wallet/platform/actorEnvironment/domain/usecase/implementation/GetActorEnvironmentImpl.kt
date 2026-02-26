package ch.admin.foitt.wallet.platform.actorEnvironment.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.actorEnvironment.domain.model.ActorEnvironment
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.usecase.GetActorEnvironment
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import javax.inject.Inject

class GetActorEnvironmentImpl @Inject constructor(
    environmentSetupRepository: EnvironmentSetupRepository,
) : GetActorEnvironment {
    private val prodPattern = Regex(pattern = environmentSetupRepository.trustEnvironmentDidRegex)
    private val betaPattern = Regex(pattern = environmentSetupRepository.demoTrustEnvironmentDidRegex)

    override suspend fun invoke(credentialIssuer: String?): ActorEnvironment = when {
        credentialIssuer == null -> ActorEnvironment.EXTERNAL
        prodPattern.containsMatchIn(credentialIssuer) -> ActorEnvironment.PRODUCTION
        betaPattern.containsMatchIn(credentialIssuer) -> ActorEnvironment.BETA
        else -> ActorEnvironment.EXTERNAL
    }
}
