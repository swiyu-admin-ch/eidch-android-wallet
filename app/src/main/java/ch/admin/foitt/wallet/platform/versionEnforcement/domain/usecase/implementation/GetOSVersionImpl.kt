package ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.implementation

import android.os.Build
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.GetOSVersion
import javax.inject.Inject

class GetOSVersionImpl @Inject constructor() : GetOSVersion {
    override fun invoke(): String = Build.VERSION.RELEASE
}
