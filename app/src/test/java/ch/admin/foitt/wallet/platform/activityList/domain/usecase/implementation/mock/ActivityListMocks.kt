package ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityActorDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityType
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialDisplayData
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityActorDisplayEntity
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityActorDisplayWithImage
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityClaimEntity
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityWithActorDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityWithDetails
import ch.admin.foitt.wallet.platform.database.domain.model.ClusterWithDisplaysAndClaims
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialActivityEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimClusterEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClusterWithDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.ImageEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithDisplaysAndClusters
import ch.admin.foitt.wallet.platform.locale.LocaleCompat
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimCluster
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimText
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import io.mockk.mockk

object ActivityListMocks {
    const val CREDENTIAL_ID = 1L
    const val ACTIVITY_ID = 1L
    const val NON_COMPLIANCE_DATA = "nonComplianceData"
    val locale = LocaleCompat.of("de", "CH")
    val imageData1 = byteArrayOf(0, 1)

    val activity = CredentialActivityEntity(
        id = 1,
        type = ActivityType.ISSUANCE,
        credentialId = CREDENTIAL_ID,
        actorTrust = TrustStatus.TRUSTED,
        vcSchemaTrust = VcSchemaTrustStatus.TRUSTED,
        nonComplianceData = NON_COMPLIANCE_DATA,
        createdAt = 1759837408,
    )

    val activityClaim = ActivityClaimEntity(
        id = 1,
        activityId = 1,
        claimId = 1,
    )

    val activityActorDisplay1 = ActivityActorDisplayEntity(
        id = 1,
        activityId = ACTIVITY_ID,
        imageHash = "imageHash1",
        name = "issuerName de",
        locale = "de-CH",
    )

    val imageEntity1 = ImageEntity(
        id = 1,
        hash = "imageHash1",
        image = imageData1
    )

    val activityActorDisplay2 = ActivityActorDisplayEntity(
        id = 2,
        activityId = ACTIVITY_ID,
        imageHash = "imageHash2",
        name = "issuerName fr",
        locale = "fr-CH"
    )

    val imageEntity2 = ImageEntity(
        id = 2,
        hash = "imageHash2",
        image = byteArrayOf(1, 0)
    )

    val actorDisplay1 = ActivityActorDisplayWithImage(
        actorDisplay = activityActorDisplay1,
        image = imageEntity1,
    )

    val actorDisplay2 = ActivityActorDisplayWithImage(
        actorDisplay = activityActorDisplay2,
        image = imageEntity2,
    )

    val activityWithActorDisplays = ActivityWithActorDisplays(
        activity = activity,
        actorDisplays = listOf(actorDisplay1, actorDisplay2),
    )

    val activityWithDetails = ActivityWithDetails(
        activity = activity,
        actorDisplays = listOf(actorDisplay1, actorDisplay2),
        claims = listOf(activityClaim),
    )

    val activityDisplayData = ActivityDisplayData(
        id = activity.id,
        activityType = activity.type,
        date = "07.10.2025 13:43",
        nonComplianceData = NON_COMPLIANCE_DATA,
        localizedActorName = actorDisplay1.actorDisplay.name
    )

    val activityActorDisplayData = ActivityActorDisplayData(
        id = activity.id,
        localizedActorName = actorDisplay1.actorDisplay.name,
        actorImageData = actorDisplay1.image?.image,
    )

    val mockVerifiableCredential = mockk<VerifiableCredentialEntity>()
    val mockCredentialDisplays = listOf(mockk<CredentialDisplay>())
    val claimWithDisplays1 = CredentialClaimWithDisplays(
        claim = CredentialClaim(
            id = 1,
            clusterId = 1,
            key = "claim 1",
            value = "claim 1 value",
            valueType = "string",
            valueDisplayInfo = null,
            order = 1,
            isSensitive = false,
        ),
        displays = emptyList(),
    )
    val claimWithDisplays2 = CredentialClaimWithDisplays(
        claim = CredentialClaim(
            id = 2,
            clusterId = 1,
            key = "claim 2",
            value = "claim 2 value",
            valueType = "string",
            valueDisplayInfo = null,
            order = 2,
            isSensitive = false,
        ),
        displays = emptyList(),
    )

    val claimsWithDisplays = listOf(claimWithDisplays1, claimWithDisplays2)
    val clusterWithDisplays = CredentialClusterWithDisplays(
        cluster = CredentialClaimClusterEntity(
            id = 1,
            verifiableCredentialId = 1,
            parentClusterId = null,
            order = 1,
        ),
        displays = emptyList()
    )
    val clusters = listOf(
        ClusterWithDisplaysAndClaims(
            clusterWithDisplays = clusterWithDisplays,
            claimsWithDisplays = claimsWithDisplays,
        )
    )

    val filteredClusters = listOf(
        ClusterWithDisplaysAndClaims(
            clusterWithDisplays = clusterWithDisplays,
            claimsWithDisplays = listOf(claimWithDisplays1)
        )
    )

    val verifiableCredentialWithDisplaysAndClusters = VerifiableCredentialWithDisplaysAndClusters(
        verifiableCredential = mockVerifiableCredential,
        credentialDisplays = mockCredentialDisplays,
        clusters = clusters,
    )

    val mockCredentialDisplayData = mockk<CredentialDisplayData>()

    val credentialClaimCluster = CredentialClaimCluster(
        id = 1,
        order = 1,
        localizedLabel = "cluster 1 label",
        isSensitive = false,
        parentId = null,
        items = mutableListOf(
            CredentialClaimText(
                id = 2,
                localizedLabel = "claim 2",
                order = 2,
                isSensitive = false,
                value = "claim 2 value",
            )
        ),
        numberOfNonClusterChildren = 1,
    )
}
