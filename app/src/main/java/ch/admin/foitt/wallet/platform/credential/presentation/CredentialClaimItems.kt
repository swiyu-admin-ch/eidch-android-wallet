package ch.admin.foitt.wallet.platform.credential.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.badges.presentation.SensitiveBadge
import ch.admin.foitt.wallet.platform.composables.Avatar
import ch.admin.foitt.wallet.platform.composables.AvatarSize
import ch.admin.foitt.wallet.platform.composables.presentation.ClaimClusterCard
import ch.admin.foitt.wallet.platform.composables.presentation.InfoClusterCard
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimCluster
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimImage
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimItem
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimText
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import coil.compose.AsyncImage

fun LazyListScope.credentialClaimItems(
    claimItems: List<CredentialClaimCluster>,
    showIssuer: Boolean = false,
    issuer: String? = null,
    issuerIcon: Painter? = null,
    onWrongData: (() -> Unit)? = null,
) {
    itemsIndexed(claimItems) { index, claim ->
        CredentialClaimCluster(claim = claim, firstCluster = claimItems.indices.first == index)
    }

    if (showIssuer) {
        item {
            Spacer(modifier = Modifier.height(Sizes.s06))
            CredentialIssuer(issuer, issuerIcon)
        }
    }

    onWrongData?.let {
        item {
            Spacer(modifier = Modifier.height(Sizes.s06))
            WrongDataCard(onWrongData)
        }
    }
}

@Composable
private fun CredentialClaimCluster(
    claim: CredentialClaimCluster,
    firstCluster: Boolean,
) {
    if (!firstCluster) {
        Spacer(modifier = Modifier.height(Sizes.s06))
    }
    WalletTexts.ClusterHeadline(text = claim.localizedLabel, depth = 0)
    ClaimClusterCard {
        claim.items.forEachIndexed { index, item ->
            ClaimItem(node = item, depth = 1, lastItem = index == claim.items.indices.last)
        }
    }
}

@Composable
private fun ClaimItem(node: CredentialClaimItem, depth: Int, lastItem: Boolean) {
    if (node is CredentialClaimCluster) {
        WalletTexts.ClusterHeadline(text = node.localizedLabel, depth = depth)
        node.items.forEachIndexed { index, item ->
            ClaimItem(node = item, depth = depth + 1, lastItem = index == node.items.indices.last)
        }
    } else {
        ListItem(
            modifier = Modifier.testTag(tag = node.localizedLabel),
            colors = ListItemDefaults.colors(containerColor = WalletTheme.colorScheme.listItemBackground),
            overlineContent = {
                WalletTexts.LabelMedium(
                    text = node.localizedLabel
                )
            },
            headlineContent = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    when (node) {
                        is CredentialClaimText -> WalletTexts.BodyLarge(
                            modifier = Modifier.weight(1f),
                            text = node.value ?: "–"
                        )

                        is CredentialClaimImage -> ClaimImage(claimImage = node)
                        else -> Unit
                    }
                    if (node.isSensitive) {
                        SensitiveBadge()
                    }
                }
            },
        )
        if (!lastItem) {
            ItemDivider()
        }
    }
}

@Composable
private fun ClaimImage(
    claimImage: CredentialClaimImage,
) {
    AsyncImage(
        modifier = Modifier
            .padding(top = Sizes.s02, bottom = Sizes.s01)
            .heightIn(max = Sizes.claimImageMaxHeight)
            .clip(RoundedCornerShape(Sizes.s02)),
        model = claimImage.imageData,
        alignment = Alignment.TopStart,
        contentScale = ContentScale.Fit,
        contentDescription = null,
        filterQuality = FilterQuality.High,
    )
}

@Composable
private fun ItemDivider() = Box(
    modifier = Modifier
        .padding(start = Sizes.s04)
) {
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        color = WalletTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun CredentialIssuer(
    issuer: String?,
    issuerIcon: Painter?
) {
    WalletTexts.ClusterHeadline(
        text = stringResource(R.string.tk_displaydelete_displaycredential1_title5),
        depth = 0
    )
    Spacer(modifier = Modifier.height(Sizes.s02))
    InfoClusterCard {
        val issuer = issuer ?: stringResource(R.string.tk_credential_offer_issuer_name_unknown)
        val issuerIcon = issuerIcon ?: painterResource(id = R.drawable.wallet_ic_actor_default)
        ListItem(
            colors = ListItemDefaults.colors(containerColor = WalletTheme.colorScheme.listItemBackground),
            headlineContent = { Text(text = issuer) },
            leadingContent = {
                Avatar(
                    imagePainter = issuerIcon,
                    size = AvatarSize.SMALL,
                    imageTint = WalletTheme.colorScheme.onSurface,
                )
            },
        )
    }
}

@Composable
private fun WrongDataCard(onWrongData: () -> Unit) = InfoClusterCard {
    ListItem(
        modifier = Modifier
            .clickable(onClick = onWrongData)
            .spaceBarKeyClickable(onWrongData)
            .semantics {
                role = Role.Button
            },
        colors = ListItemDefaults.colors(containerColor = WalletTheme.colorScheme.listItemBackground),
        headlineContent = {
            Text(
                text = stringResource(id = R.string.tk_global_wrongdata),
                modifier = Modifier.testTag(TestTags.WRONG_DATA_LINK.name)
            )
        },
        leadingContent = {
            Icon(
                painter = painterResource(id = R.drawable.wallet_ic_wrong_data),
                contentDescription = null,
            )
        },
        trailingContent = {
            Icon(
                modifier = Modifier.size(Sizes.s06),
                painter = painterResource(id = R.drawable.wallet_ic_chevron),
                contentDescription = null,
            )
        },
    )
}
