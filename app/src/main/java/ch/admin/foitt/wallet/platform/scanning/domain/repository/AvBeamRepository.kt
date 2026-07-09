package ch.admin.foitt.wallet.platform.scanning.domain.repository

import androidx.appcompat.app.AppCompatActivity
import ch.admin.foitt.avwrapper.AVBeam

interface AvBeamRepository {
    suspend fun init(activity: AppCompatActivity)

    fun getBeam(): AVBeam

    suspend fun release()
}
