package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.model

import androidx.annotation.DrawableRes

data class DocumentTypeDrawable(
    @param:DrawableRes val front: Int,
    @param:DrawableRes val back: Int,
)
