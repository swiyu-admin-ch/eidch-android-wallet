package ch.admin.foitt.wallet.platform.scanning.data

import androidx.appcompat.app.AppCompatActivity
import ch.admin.foitt.avwrapper.AVBeam
import ch.admin.foitt.avwrapper.AVBeamInitConfig
import ch.admin.foitt.avwrapper.config.AVBeamConfigLogLevel
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.navigation.DestinationsScoped
import ch.admin.foitt.wallet.platform.scanning.domain.repository.AvBeamRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@DestinationsScoped
class AvBeamRepositoryImpl @Inject constructor(
    private val avBeam: AVBeam,
    private val environmentSetupRepository: EnvironmentSetupRepository,
) : AvBeamRepository {

    override suspend fun init(
        activity: AppCompatActivity
    ) = withContext(Dispatchers.IO) {
        val logLevel = if (environmentSetupRepository.avBeamLoggingEnabled) {
            AVBeamConfigLogLevel.DEBUG
        } else {
            AVBeamConfigLogLevel.NONE
        }
        avBeam.init(AVBeamInitConfig(logLevel), activity)
    }

    override fun getBeam(): AVBeam {
        return avBeam
    }

    override suspend fun release() = withContext(Dispatchers.Main) {
        avBeam.shutDown()
    }
}
