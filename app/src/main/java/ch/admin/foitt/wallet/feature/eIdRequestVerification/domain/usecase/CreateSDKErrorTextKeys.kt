package ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase

import ch.admin.foitt.avwrapper.AVBeamError
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.TextKeyType

interface CreateSDKErrorTextKeys {
    suspend operator fun invoke(errorCode: AVBeamError, textKeyType: TextKeyType): Int
}
