package ch.admin.foitt.wallet.platform.locale.domain.usecase

import ch.admin.foitt.wallet.platform.database.domain.model.LocalizedDisplay

interface GetLocalizedDisplay {
    operator fun <T : LocalizedDisplay> invoke(
        displays: Collection<T>,
        preferredLocaleString: String? = null,
    ): T?
}
