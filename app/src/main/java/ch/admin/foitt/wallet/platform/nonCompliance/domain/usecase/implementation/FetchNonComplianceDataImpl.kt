package ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceData
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceReasonDisplay
import ch.admin.foitt.wallet.platform.nonCompliance.domain.repository.NonComplianceTrustRepository
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.FetchNonComplianceData
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustDomainFromDid
import com.github.michaelbull.result.mapBoth
import javax.inject.Inject

class FetchNonComplianceDataImpl @Inject constructor(
    private val getTrustDomainFromDid: GetTrustDomainFromDid,
    private val nonComplianceTrustRepository: NonComplianceTrustRepository,
) : FetchNonComplianceData {
    override suspend fun invoke(actorDid: String): NonComplianceData = getTrustDomainFromDid(actorDid)
        .mapBoth(
            success = {
                fetchNonComplianceData(trustDomain = it, actorDid = actorDid)
            },
            failure = {
                unknown
            }
        )

    private suspend fun fetchNonComplianceData(
        trustDomain: String,
        actorDid: String,
    ) = nonComplianceTrustRepository.fetchNonComplianceData(trustDomain)
        .mapBoth(
            success = { nonComplianceResponse ->
                // check if provided did is part of reported actors list
                nonComplianceResponse.nonCompliantActors.find { it.did == actorDid }?.let { actor ->
                    val displays = actor.reason.map { (locale, translation) ->
                        NonComplianceReasonDisplay(locale = locale, reason = translation)
                    }.toList()

                    NonComplianceData(
                        state = ActorComplianceState.REPORTED,
                        reasonDisplays = displays,
                    )
                } ?: notReported
            },
            failure = {
                unknown
            }
        )

    private val notReported = NonComplianceData(
        state = ActorComplianceState.NOT_REPORTED,
        reasonDisplays = null,
    )

    private val unknown = NonComplianceData(
        state = ActorComplianceState.UNKNOWN,
        reasonDisplays = null,
    )
}
