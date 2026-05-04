package ch.admin.foitt.wallet.platform.credentialCluster.domain.usercase.implementation.mock

import ch.admin.foitt.wallet.platform.database.domain.model.ClusterWithDisplaysAndClaims
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimClusterDisplayEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimClusterEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClusterWithDisplays
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimCluster
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimText

object ClusterMocks {
    const val CREDENTIAL_ID = 1L
    const val CLAIM_ID_MINUS_1 = -1L
    const val CLAIM_ID_1 = 1L
    const val CLAIM_ID_2 = 2L
    const val CLAIM_ID_3 = 3L
    const val CLAIM_ID_4 = 4L
    const val CLAIM_ID_5 = 5L
    const val CLUSTER_ID_MINUS_1 = -1L
    const val CLUSTER_ID_1 = 1L
    const val CLUSTER_ID_2 = 2L
    const val CLUSTER_ID_3 = 3L
    const val CLUSTER_ID_4 = 4L
    const val CLUSTER_ID_5 = 5L
    const val NAME_MINUS_1 = "name_minus_1"
    const val NAME_1 = "name_1"
    const val NAME_2 = "name_2"
    const val NAME_3 = "name_3"
    const val NAME_4 = "name_4"
    const val NAME_5 = "name_5"

    val credentialClaimClusterEntitiesMinus1 = listOf(
        CredentialClaimClusterDisplayEntity(
            id = -1,
            clusterId = CLUSTER_ID_MINUS_1,
            name = "Cluster #$CLUSTER_ID_MINUS_1",
            locale = "en"
        )
    )
    val credentialClaimClusterEntities1 = listOf(
        CredentialClaimClusterDisplayEntity(
            id = 1,
            clusterId = CLUSTER_ID_1,
            name = "Cluster #$CLUSTER_ID_1",
            locale = "en"
        )
    )
    val credentialClaimClusterEntities2 = listOf(
        CredentialClaimClusterDisplayEntity(
            id = 2,
            clusterId = CLUSTER_ID_2,
            name = "Cluster #$CLUSTER_ID_2",
            locale = "en"
        )
    )
    val credentialClaimClusterEntities3 = listOf(
        CredentialClaimClusterDisplayEntity(
            id = 3,
            clusterId = CLUSTER_ID_3,
            name = "Cluster #$CLUSTER_ID_3",
            locale = "en"
        )
    )
    val credentialClaimClusterEntities4 = listOf(
        CredentialClaimClusterDisplayEntity(
            id = 4,
            clusterId = CLUSTER_ID_4,
            name = "Cluster #$CLUSTER_ID_4",
            locale = "en"
        )
    )
    val credentialClaimClusterEntities5 = listOf(
        CredentialClaimClusterDisplayEntity(
            id = 5,
            clusterId = CLUSTER_ID_5,
            name = "Cluster #$CLUSTER_ID_5",
            locale = "en"
        )
    )
    val credentialClaimWithDisplayMinus1 = CredentialClaimWithDisplays(
        claim = CredentialClaim(
            id = CLAIM_ID_MINUS_1,
            clusterId = CLUSTER_ID_MINUS_1,
            path = "key",
            value = "value",
            valueType = "string"
        ),
        displays = listOf(
            CredentialClaimDisplay(
                id = -1,
                claimId = CLAIM_ID_MINUS_1,
                name = NAME_MINUS_1,
                locale = "en",
                value = null,
            )
        )
    )
    val credentialClaimWithDisplay1 = CredentialClaimWithDisplays(
        claim = CredentialClaim(
            id = CLAIM_ID_1,
            clusterId = CLUSTER_ID_1,
            path = "key",
            value = "value",
            valueType = "string"
        ),
        displays = listOf(
            CredentialClaimDisplay(
                id = 1,
                claimId = CLAIM_ID_1,
                name = NAME_1,
                locale = "en",
                value = null,
            )
        )
    )
    val credentialClaimWithDisplay2 = CredentialClaimWithDisplays(
        claim = CredentialClaim(
            id = CLAIM_ID_2,
            clusterId = CLUSTER_ID_2,
            path = "key",
            value = "value",
            valueType = "string",
        ),
        displays = listOf(
            CredentialClaimDisplay(
                id = 2,
                claimId = CLAIM_ID_2,
                name = NAME_2,
                locale = "en",
                value = null,
            )
        )
    )
    val credentialClaimWithDisplay3 = CredentialClaimWithDisplays(
        claim = CredentialClaim(
            id = CLAIM_ID_3,
            clusterId = CLUSTER_ID_3,
            path = "key",
            value = "value",
            valueType = "string"
        ),
        displays = listOf(
            CredentialClaimDisplay(
                id = 3,
                claimId = CLAIM_ID_3,
                name = NAME_3,
                locale = "en",
                value = null,
            )
        )
    )
    val credentialClaimWithDisplay4 = CredentialClaimWithDisplays(
        claim = CredentialClaim(
            id = CLAIM_ID_4,
            clusterId = CLUSTER_ID_4,
            path = "key",
            value = "value",
            valueType = "string"
        ),
        displays = listOf(
            CredentialClaimDisplay(
                id = 4,
                claimId = CLAIM_ID_4,
                name = NAME_4,
                locale = "en",
                value = null,
            )
        )
    )
    val credentialClaimWithDisplay5 = CredentialClaimWithDisplays(
        claim = CredentialClaim(
            id = CLAIM_ID_5,
            clusterId = CLUSTER_ID_5,
            path = "key",
            value = "value",
            valueType = "string"
        ),
        displays = listOf(
            CredentialClaimDisplay(
                id = 5,
                claimId = CLAIM_ID_5,
                name = NAME_5,
                locale = "en",
                value = null,
            )
        )
    )

    val credentialClaimItemMinus1 = CredentialClaimText(
        id = -1L,
        localizedLabel = "key",
        order = -1,
        value = NAME_MINUS_1,
        isSensitive = false
    )

    val credentialClaimItem1 = CredentialClaimText(
        id = 1L,
        localizedLabel = "key",
        order = 1,
        value = NAME_1,
        isSensitive = false
    )

    val credentialClaimItem2 = CredentialClaimText(
        id = 2L,
        localizedLabel = "key",
        order = 2,
        value = NAME_2,
        isSensitive = false
    )

    val credentialClaimItem3 = CredentialClaimText(
        id = 3L,
        localizedLabel = "key",
        order = 3,
        value = NAME_3,
        isSensitive = false
    )
    val credentialClaimItem4 = CredentialClaimText(
        id = 4L,
        localizedLabel = "key",
        order = 4,
        value = NAME_4,
        isSensitive = false
    )
    val credentialClaimItem5 = CredentialClaimText(
        id = 5L,
        localizedLabel = "key",
        order = 5,
        value = NAME_5,
        isSensitive = false
    )

    /*
    - cluster1
        - item1
    - cluster2
        - item2
     */
    val simpleClusterInput: List<ClusterWithDisplaysAndClaims> = listOf(
        ClusterWithDisplaysAndClaims(
            clusterWithDisplays = CredentialClusterWithDisplays(
                cluster = CredentialClaimClusterEntity(
                    id = CLUSTER_ID_1,
                    verifiableCredentialId = CREDENTIAL_ID,
                    parentClusterId = null,
                    order = 2
                ),
                displays = credentialClaimClusterEntities1
            ),
            claimsWithDisplays = listOf(credentialClaimWithDisplay1)
        ),
        ClusterWithDisplaysAndClaims(
            clusterWithDisplays = CredentialClusterWithDisplays(
                cluster = CredentialClaimClusterEntity(
                    id = CLUSTER_ID_2,
                    verifiableCredentialId = CREDENTIAL_ID,
                    parentClusterId = null,
                    order = 1
                ),
                displays = credentialClaimClusterEntities2
            ),
            claimsWithDisplays = listOf(credentialClaimWithDisplay2)
        )
    )

    /*
   - cluster1
       - item1
   - cluster2
       - item2
       - cluster3
            - item3
     */
    val clusterInput: List<ClusterWithDisplaysAndClaims> = listOf(
        ClusterWithDisplaysAndClaims(
            clusterWithDisplays = CredentialClusterWithDisplays(
                cluster = CredentialClaimClusterEntity(
                    id = CLUSTER_ID_1,
                    verifiableCredentialId = CREDENTIAL_ID,
                    parentClusterId = null,
                    order = 2
                ),
                displays = credentialClaimClusterEntities1
            ),
            claimsWithDisplays = listOf(credentialClaimWithDisplay1)
        ),
        ClusterWithDisplaysAndClaims(
            clusterWithDisplays = CredentialClusterWithDisplays(
                cluster = CredentialClaimClusterEntity(
                    id = CLUSTER_ID_2,
                    verifiableCredentialId = CREDENTIAL_ID,
                    parentClusterId = null,
                    order = 1
                ),
                displays = credentialClaimClusterEntities2
            ),
            claimsWithDisplays = listOf(credentialClaimWithDisplay2)
        ),
        ClusterWithDisplaysAndClaims(
            clusterWithDisplays = CredentialClusterWithDisplays(
                cluster = CredentialClaimClusterEntity(
                    id = CLUSTER_ID_3,
                    verifiableCredentialId = CREDENTIAL_ID,
                    parentClusterId = CLUSTER_ID_2,
                    order = 1
                ),
                displays = credentialClaimClusterEntities3
            ),
            claimsWithDisplays = listOf(credentialClaimWithDisplay3)
        )
    )

    val expectedSimpleCluster = listOf(
        CredentialClaimCluster(
            id = CLUSTER_ID_2,
            order = 1,
            localizedLabel = "Cluster #$CLUSTER_ID_2",
            parentId = null,
            numberOfNonClusterChildren = 1,
            items = mutableListOf(
                credentialClaimItem2
            ),
        ),
        CredentialClaimCluster(
            id = CLUSTER_ID_1,
            order = 2,
            localizedLabel = "Cluster #$CLUSTER_ID_1",
            parentId = null,
            numberOfNonClusterChildren = 1,
            items = mutableListOf(
                credentialClaimItem1
            ),
        ),
    )

    val expectedCluster = listOf(
        CredentialClaimCluster(
            id = CLUSTER_ID_2,
            order = 1,
            localizedLabel = "Cluster #$CLUSTER_ID_2",
            parentId = null,
            numberOfNonClusterChildren = 2,
            items = mutableListOf(
                CredentialClaimCluster(
                    id = CLUSTER_ID_3,
                    order = 1,
                    localizedLabel = "Cluster #$CLUSTER_ID_3",
                    parentId = CLUSTER_ID_2,
                    numberOfNonClusterChildren = 1,
                    items = mutableListOf(
                        credentialClaimItem3
                    ),
                ),
                credentialClaimItem2,
            ),
        ),
        CredentialClaimCluster(
            id = CLUSTER_ID_1,
            order = 2,
            localizedLabel = "Cluster #$CLUSTER_ID_1",
            parentId = null,
            numberOfNonClusterChildren = 1,
            items = mutableListOf(
                credentialClaimItem1
            ),
        ),
    )

    /*
   - cluster1
       - item1
       - cluster5
            - item5
   - cluster2
       - item2
       - cluster3
            - item3
       - cluster4
            - item4
     */
    val complexClusterInput = listOf(
        ClusterWithDisplaysAndClaims(
            clusterWithDisplays = CredentialClusterWithDisplays(
                cluster = CredentialClaimClusterEntity(
                    id = CLUSTER_ID_1,
                    verifiableCredentialId = CREDENTIAL_ID,
                    parentClusterId = null,
                    order = 1
                ),
                displays = credentialClaimClusterEntities1
            ),
            claimsWithDisplays = listOf(credentialClaimWithDisplayMinus1, credentialClaimWithDisplay1)
        ),
        ClusterWithDisplaysAndClaims(
            clusterWithDisplays = CredentialClusterWithDisplays(
                cluster = CredentialClaimClusterEntity(
                    id = CLUSTER_ID_5,
                    verifiableCredentialId = CREDENTIAL_ID,
                    parentClusterId = CLUSTER_ID_1,
                    order = -1,
                ),
                displays = credentialClaimClusterEntities5
            ),
            claimsWithDisplays = listOf(credentialClaimWithDisplay5)
        ),
        ClusterWithDisplaysAndClaims(
            clusterWithDisplays = CredentialClusterWithDisplays(
                cluster = CredentialClaimClusterEntity(
                    id = CLUSTER_ID_3,
                    verifiableCredentialId = CREDENTIAL_ID,
                    parentClusterId = CLUSTER_ID_2,
                    order = 1
                ),
                displays = credentialClaimClusterEntities3
            ),
            claimsWithDisplays = listOf(credentialClaimWithDisplay3)
        ),
        ClusterWithDisplaysAndClaims(
            clusterWithDisplays = CredentialClusterWithDisplays(
                cluster = CredentialClaimClusterEntity(
                    id = CLUSTER_ID_4,
                    verifiableCredentialId = CREDENTIAL_ID,
                    parentClusterId = CLUSTER_ID_2,
                    order = 1
                ),
                displays = credentialClaimClusterEntities4
            ),
            claimsWithDisplays = listOf(credentialClaimWithDisplay4)
        ),
        ClusterWithDisplaysAndClaims(
            clusterWithDisplays = CredentialClusterWithDisplays(
                cluster = CredentialClaimClusterEntity(
                    id = CLUSTER_ID_2,
                    verifiableCredentialId = CREDENTIAL_ID,
                    parentClusterId = CLUSTER_ID_1,
                    order = 1
                ),
                displays = credentialClaimClusterEntities2
            ),
            claimsWithDisplays = listOf(credentialClaimWithDisplay2)
        )
    )

    val expectedComplexCluster = listOf(
        CredentialClaimCluster(
            id = CLUSTER_ID_1,
            order = 1,
            localizedLabel = "Cluster #$CLUSTER_ID_1",
            parentId = null,
            numberOfNonClusterChildren = 6,
            items = mutableListOf(
                credentialClaimItem1,
                CredentialClaimCluster(
                    id = CLUSTER_ID_2,
                    order = 1,
                    localizedLabel = "Cluster #$CLUSTER_ID_2",
                    parentId = CLUSTER_ID_1,
                    numberOfNonClusterChildren = 3,
                    items = mutableListOf(
                        CredentialClaimCluster(
                            id = CLUSTER_ID_3,
                            order = 1,
                            localizedLabel = "Cluster #$CLUSTER_ID_3",
                            parentId = CLUSTER_ID_2,
                            numberOfNonClusterChildren = 1,
                            items = mutableListOf(
                                credentialClaimItem3
                            ),
                        ),
                        CredentialClaimCluster(
                            id = CLUSTER_ID_4,
                            order = 1,
                            localizedLabel = "Cluster #$CLUSTER_ID_4",
                            parentId = CLUSTER_ID_2,
                            numberOfNonClusterChildren = 1,
                            items = mutableListOf(
                                credentialClaimItem4
                            ),
                        ),
                        credentialClaimItem2,
                    ),
                ),
                credentialClaimItemMinus1,
                CredentialClaimCluster(
                    id = CLUSTER_ID_5,
                    order = -1,
                    localizedLabel = "Cluster #$CLUSTER_ID_5",
                    parentId = CLUSTER_ID_1,
                    numberOfNonClusterChildren = 1,
                    items = mutableListOf(
                        credentialClaimItem5
                    ),
                ),
            )
        )
    )

    val simpleEmptyClusterInput: List<ClusterWithDisplaysAndClaims> = listOf(
        ClusterWithDisplaysAndClaims(
            clusterWithDisplays = CredentialClusterWithDisplays(
                cluster = CredentialClaimClusterEntity(
                    id = CLUSTER_ID_1,
                    verifiableCredentialId = CREDENTIAL_ID,
                    parentClusterId = null,
                    order = 2
                ),
                displays = credentialClaimClusterEntities1
            ),
            claimsWithDisplays = listOf(credentialClaimWithDisplay1)
        ),
        ClusterWithDisplaysAndClaims(
            clusterWithDisplays = CredentialClusterWithDisplays(
                cluster = CredentialClaimClusterEntity(
                    id = CLUSTER_ID_2,
                    verifiableCredentialId = CREDENTIAL_ID,
                    parentClusterId = null,
                    order = 1
                ),
                displays = credentialClaimClusterEntities2
            ),
            claimsWithDisplays = emptyList()
        )
    )

    val expectedSimpleEmptyCluster = listOf(
        CredentialClaimCluster(
            id = CLUSTER_ID_1,
            order = 2,
            localizedLabel = "Cluster #$CLUSTER_ID_1",
            parentId = null,
            numberOfNonClusterChildren = 1,
            items = mutableListOf(
                credentialClaimItem1
            ),
        ),
    )

    val complexEmptyClusterInput: List<ClusterWithDisplaysAndClaims> = listOf(
        ClusterWithDisplaysAndClaims(
            clusterWithDisplays = CredentialClusterWithDisplays(
                cluster = CredentialClaimClusterEntity(
                    id = CLUSTER_ID_1,
                    verifiableCredentialId = CREDENTIAL_ID,
                    parentClusterId = null,
                    order = 2
                ),
                displays = credentialClaimClusterEntities1
            ),
            claimsWithDisplays = listOf(credentialClaimWithDisplay1)
        ),
        ClusterWithDisplaysAndClaims(
            clusterWithDisplays = CredentialClusterWithDisplays(
                cluster = CredentialClaimClusterEntity(
                    id = CLUSTER_ID_2,
                    verifiableCredentialId = CREDENTIAL_ID,
                    parentClusterId = null,
                    order = 1
                ),
                displays = credentialClaimClusterEntities2
            ),
            claimsWithDisplays = listOf(credentialClaimWithDisplay2)
        ),
        ClusterWithDisplaysAndClaims(
            clusterWithDisplays = CredentialClusterWithDisplays(
                cluster = CredentialClaimClusterEntity(
                    id = CLUSTER_ID_3,
                    verifiableCredentialId = CREDENTIAL_ID,
                    parentClusterId = CLUSTER_ID_2,
                    order = 1
                ),
                displays = credentialClaimClusterEntities3
            ),
            claimsWithDisplays = emptyList()
        )
    )

    val expectedComplexEmptyCluster = listOf(
        CredentialClaimCluster(
            id = CLUSTER_ID_2,
            order = 1,
            localizedLabel = "Cluster #$CLUSTER_ID_2",
            parentId = null,
            numberOfNonClusterChildren = 1,
            items = mutableListOf(
                credentialClaimItem2,
            ),
        ),
        CredentialClaimCluster(
            id = CLUSTER_ID_1,
            order = 2,
            localizedLabel = "Cluster #$CLUSTER_ID_1",
            parentId = null,
            numberOfNonClusterChildren = 1,
            items = mutableListOf(
                credentialClaimItem1
            ),
        ),
    )
}
