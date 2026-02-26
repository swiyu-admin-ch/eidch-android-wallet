package ch.admin.foitt.wallet.platform.activityList.mock

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityType
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityActorDisplayEntity
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityActorDisplayWithImage
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityClaimEntity
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialActivityEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimClusterEntity
import ch.admin.foitt.wallet.platform.database.domain.model.ImageEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import java.net.URL

object ActivityListMocks {

    val credential1 = Credential(
        id = 1,
        format = CredentialFormat.VC_SD_JWT,
        issuerUrl = URL("https://issuer.example.com")
    )

    val verifiableCredential1 = VerifiableCredentialEntity(
        credentialId = 1,
        progressionState = VerifiableProgressionState.ACCEPTED,
        issuer = "issuer",
        validFrom = 1,
        validUntil = 123456789,
        createdAt = 1,
        updatedAt = 1,
    )

    val cluster1 = CredentialClaimClusterEntity(
        id = 1,
        verifiableCredentialId = 1,
        parentClusterId = null,
        order = 1,
    )

    val claim1 = CredentialClaim(
        id = 1,
        clusterId = 1,
        key = "key",
        value = "value",
        valueType = "valueType",
        valueDisplayInfo = null,
        order = 1,
    )

    val credentialActivity1 = CredentialActivityEntity(
        id = 1,
        credentialId = 1,
        type = ActivityType.ISSUANCE,
        actorTrust = TrustStatus.TRUSTED,
        vcSchemaTrust = VcSchemaTrustStatus.TRUSTED,
        nonComplianceData = null
    )

    val credentialActivity2 = CredentialActivityEntity(
        id = 2,
        credentialId = 1,
        type = ActivityType.PRESENTATION_ACCEPTED,
        actorTrust = TrustStatus.TRUSTED,
        vcSchemaTrust = VcSchemaTrustStatus.TRUSTED,
        nonComplianceData = null
    )

    val credentialActivity3 = CredentialActivityEntity(
        id = 3,
        credentialId = 1,
        type = ActivityType.PRESENTATION_DECLINED,
        actorTrust = TrustStatus.TRUSTED,
        vcSchemaTrust = VcSchemaTrustStatus.TRUSTED,
        nonComplianceData = null
    )

    val activityClaim1 = ActivityClaimEntity(
        id = 1,
        activityId = 2,
        claimId = 1,
    )

    val image1 = ImageEntity(
        id = 1,
        hash = "imageHash1",
        image = byteArrayOf(0, 1)
    )

    val image2 = ImageEntity(
        id = 2,
        hash = "imageHash2",
        image = byteArrayOf(1, 0)
    )

    val activityActorDisplay1 = ActivityActorDisplayEntity(
        id = 1,
        activityId = 1,
        name = "name",
        locale = "en-CH",
        imageHash = "imageHash1"
    )

    val activityActorDisplay2 = ActivityActorDisplayEntity(
        id = 2,
        activityId = 2,
        name = "name",
        locale = "en-CH",
        imageHash = "imageHash2"
    )

    val activityActorDisplay3 = ActivityActorDisplayEntity(
        id = 3,
        activityId = 3,
        name = "name",
        locale = "en-CH",
        imageHash = "imageHash2"
    )

    val activityActorDisplay4 = ActivityActorDisplayEntity(
        id = 4,
        activityId = 2,
        name = "name",
        locale = "en-CH",
        imageHash = null
    )

    val activityActorDisplayWithImage = ActivityActorDisplayWithImage(
        actorDisplay =activityActorDisplay4,
        image = null
    )
}
