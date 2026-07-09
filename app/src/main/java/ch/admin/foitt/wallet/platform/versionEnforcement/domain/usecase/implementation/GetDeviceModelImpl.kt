package ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.implementation

import android.os.Build
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.GetDeviceModel
import javax.inject.Inject

class GetDeviceModelImpl @Inject constructor() : GetDeviceModel {
    override fun invoke(): String = Build.MODEL
}
