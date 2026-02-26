package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.model.ActorEnvironment
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.model.toDisplayStatus
import ch.admin.foitt.wallet.platform.database.domain.model.BundleItemEntity
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimCluster
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimText
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialDetail
import java.net.URL

object MockCredentialDetail {

    const val CREDENTIAL_ID = 5L
    private const val CREDENTIAL_ID_2 = 6L
    private const val CLUSTER_ID = 1L
    private const val CLAIM_ID_1 = 1L
    private const val CLAIM_ID_2 = 2L
    private const val ISSUER_URL = "https://example.com/issuer"

    private val credential = Credential(
        id = CREDENTIAL_ID,
        format = CredentialFormat.VC_SD_JWT,
        createdAt = 1700463600000,
        issuerUrl = URL(ISSUER_URL),
    )

    private val bundleItem = BundleItemEntity(
        credentialId = CREDENTIAL_ID,
        payload = "payload",
        status = CredentialStatus.VALID,
    )

    @OptIn(ExperimentalStdlibApi::class)
    val credentialDisplay1 = CredentialDisplay(
        id = 23,
        credentialId = CREDENTIAL_ID,
        locale = "locale1",
        name = "name1",
        backgroundColor = "#" + Color.Black.toArgb().toHexString()
    )

    val credentialDisplay2 = CredentialDisplay(
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

    val claims = listOf(claimWithDisplays1, claimWithDisplays2)

    val claimData1 = CredentialClaimText(
        id = 1L,
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

    val listOfCredentialClaimCluster = listOf(
        CredentialClaimCluster(
            id = 1,
            order = 1,
            localizedLabel = "label",
            parentId = null,
            items = mutableListOf(claimData1, claimData2),
        )
    )

    val credentialDisplayData = CredentialDisplayData(
        credentialId = credential.id,
        status = bundleItem.status.toDisplayStatus(),
        credentialDisplay = credentialDisplay1,
        actorEnvironment = ActorEnvironment.PRODUCTION,
        progressionState = VerifiableProgressionState.ACCEPTED,
    )

    val credentialDetail = CredentialDetail(
        credential = credentialDisplayData,
        clusterItems = listOfCredentialClaimCluster
    )

    val credentialDetail2 = CredentialDetail(
        credential = credentialDisplayData,
        clusterItems = listOfCredentialClaimCluster
    )

    val credentialDisplayData1 = CredentialDisplayData(
        credentialId = CREDENTIAL_ID,
        status = CredentialStatus.VALID.toDisplayStatus(),
        credentialDisplay = credentialDisplay1,
        actorEnvironment = ActorEnvironment.PRODUCTION,
        progressionState = VerifiableProgressionState.ACCEPTED,
    )

    val credentialDisplayData2 = CredentialDisplayData(
        credentialId = CREDENTIAL_ID_2,
        status = CredentialStatus.VALID.toDisplayStatus(),
        credentialDisplay = credentialDisplay2,
        actorEnvironment = ActorEnvironment.BETA,
        progressionState = VerifiableProgressionState.ACCEPTED,
    )
}
