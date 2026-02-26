package ch.admin.foitt.wallet.feature.credentialOffer.mock

import ch.admin.foitt.wallet.feature.credentialOffer.domain.model.CredentialOffer
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.model.ActorEnvironment
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorField
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.model.toDisplayStatus
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceState
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimCluster
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimText
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialDetail
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus

object MockCredentialOffer {

    const val CREDENTIAL_ID = 5L
    private const val CLUSTER_ID = 1L
    private const val CLAIM_ID_1 = 1L
    private const val CLAIM_ID_2 = 2L

    const val ISSUER = "issuer"

    private val credentialDisplay1 = CredentialDisplay(
        id = 23,
        credentialId = CREDENTIAL_ID,
        locale = "locale1",
        name = "name1",
        backgroundColor = "#ff000000"
    )

    private val credentialDisplay2 = CredentialDisplay(
        id = 24,
        credentialId = CREDENTIAL_ID,
        locale = "locale2",
        name = "name2",
    )

    val credentialDisplays = listOf(credentialDisplay1, credentialDisplay2)

    private val claim1 = CredentialClaim(
        id = CLAIM_ID_1,
        clusterId = CLUSTER_ID,
        key = "key1",
        value = "value1",
        valueType = "valueType1",
        order = 0,
    )

    private val claim2 = CredentialClaim(
        id = CLAIM_ID_2,
        clusterId = CLUSTER_ID,
        key = "key2",
        value = "value2",
        valueType = "valueType2",
        order = 1,
    )

    private val claimDisplay1 = CredentialClaimDisplay(
        id = 0,
        claimId = CLAIM_ID_1,
        name = "name1",
        locale = "locale1",
        value = null,
    )

    private val claimDisplay2 = CredentialClaimDisplay(
        id = 1,
        claimId = CLAIM_ID_1,
        name = "name2",
        locale = "locale2",
        value = null,
    )

    private val claimDisplay3 = CredentialClaimDisplay(
        id = 2,
        claimId = CLAIM_ID_2,
        name = "name3",
        locale = "locale3",
        value = null,
    )

    private val claimDisplay4 = CredentialClaimDisplay(
        id = 3,
        claimId = CLAIM_ID_2,
        name = "name4",
        locale = "locale4",
        value = null,
    )

    val claimWithDisplays1 = CredentialClaimWithDisplays(
        claim = claim1,
        displays = listOf(
            claimDisplay1,
            claimDisplay2,
        )
    )

    val claimWithDisplays2 = CredentialClaimWithDisplays(
        claim = claim2,
        displays = listOf(
            claimDisplay3,
            claimDisplay4,
        )
    )

    val claimsWithDisplays = listOf(claimWithDisplays1, claimWithDisplays2)

    val claimData1 = CredentialClaimText(
        id = 1,
        localizedLabel = claimDisplay1.name,
        order = 1,
        value = claim1.value,
        isSensitive = false
    )

    val claimData2 = CredentialClaimText(
        id = 2L,
        localizedLabel = claimDisplay3.name,
        order = 2,
        value = claim2.value,
        isSensitive = false
    )

    val credentialDisplayData = CredentialDisplayData(
        credentialId = CREDENTIAL_ID,
        status = CredentialStatus.VALID.toDisplayStatus(),
        credentialDisplay = credentialDisplay1,
        actorEnvironment = ActorEnvironment.PRODUCTION,
        progressionState = VerifiableProgressionState.ACCEPTED,
    )

    val mockIssuerDisplayData = ActorDisplayData(
        name = listOf(
            ActorField(value = "a", "de"),
            ActorField(value = "b", "en"),
        ),
        image = null,
        preferredLanguage = "de",
        trustStatus = TrustStatus.TRUSTED,
        vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
        actorType = ActorType.ISSUER,
        nonComplianceState = NonComplianceState.REPORTED,
        nonComplianceReason = null
    )

    val listOfCredentialClaimCluster = listOf(
        CredentialClaimCluster(
            id = 1,
            order = 1,
            localizedLabel = "label",
            parentId = null,
            items = mutableListOf(MockCredentialDetail.claimData1, MockCredentialDetail.claimData2),
        )
    )

    val credentialOffer = CredentialOffer(
        credential = credentialDisplayData,
        claims = listOfCredentialClaimCluster
    )

    val credentialOffer2 = CredentialOffer(
        credential = credentialDisplayData,
        claims = listOfCredentialClaimCluster
    )
}
