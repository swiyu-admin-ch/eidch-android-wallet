package ch.admin.foitt.wallet.platform.ssi.domain.model

data class CredentialClaimText(
    override val id: Long,
    override val localizedLabel: String,
    override val order: Int,
    override val isSensitive: Boolean,
    val value: String?,
) : CredentialElement
