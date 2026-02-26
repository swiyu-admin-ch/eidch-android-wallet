package ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.actorEnvironment.domain.model.ActorEnvironment
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorField
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.CacheIssuerDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.InitializeActorForScope
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyIssuerDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceData
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceReasonDisplay
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustCheckResult
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import javax.inject.Inject

internal class CacheIssuerDisplayDataImpl @Inject constructor(
    private val initializeActorForScope: InitializeActorForScope,
) : CacheIssuerDisplayData {
    override suspend fun invoke(
        trustCheckResult: TrustCheckResult,
        issuerDisplays: List<AnyIssuerDisplay>,
        nonComplianceData: NonComplianceData,
    ) {
        val trustStatus = getTrustStatus(trustCheckResult)

        val issuerTrustNameDisplay: List<ActorField<String>> = issuerDisplays.toIssuerName()
        val issuerTrustLogoDisplay: List<ActorField<String>> = issuerDisplays.toIssuerLogo()

        val reasonDisplay: List<ActorField<String>>? = nonComplianceData.reasonDisplays?.toNonComplianceReason()

        val offerIssuerDisplay = ActorDisplayData(
            name = issuerTrustNameDisplay,
            image = issuerTrustLogoDisplay,
            trustStatus = trustStatus,
            vcSchemaTrustStatus = trustCheckResult.vcSchemaTrustStatus,
            preferredLanguage = null,
            actorType = ActorType.ISSUER,
            nonComplianceState = nonComplianceData.state,
            nonComplianceReason = reasonDisplay
        )

        initializeActorForScope(
            actorDisplayData = offerIssuerDisplay,
            componentScope = ComponentScope.CredentialIssuer,
        )
    }

    private fun getTrustStatus(trustCheckResult: TrustCheckResult) = when (trustCheckResult.actorEnvironment) {
        ActorEnvironment.PRODUCTION, ActorEnvironment.BETA -> {
            if (trustCheckResult.actorTrustStatement != null) {
                TrustStatus.TRUSTED
            } else {
                TrustStatus.NOT_TRUSTED
            }
        }

        ActorEnvironment.EXTERNAL -> TrustStatus.EXTERNAL
    }

    private fun List<AnyIssuerDisplay>.toIssuerName(): List<ActorField<String>> = map { entry ->
        ActorField(
            value = entry.name,
            locale = entry.locale ?: DisplayLanguage.UNKNOWN,
        )
    }

    private fun List<AnyIssuerDisplay>.toIssuerLogo(): List<ActorField<String>> = mapNotNull { entry ->
        entry.logo?.let {
            ActorField(
                value = entry.logo,
                locale = entry.locale ?: DisplayLanguage.UNKNOWN,
            )
        }
    }

    private fun List<NonComplianceReasonDisplay>.toNonComplianceReason(): List<ActorField<String>> = map { entry ->
        ActorField(
            value = entry.reason,
            locale = entry.locale,
        )
    }
}
