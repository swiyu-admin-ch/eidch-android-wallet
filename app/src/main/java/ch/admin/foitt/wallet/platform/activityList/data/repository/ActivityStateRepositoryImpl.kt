package ch.admin.foitt.wallet.platform.activityList.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityStateRepository
import ch.admin.foitt.wallet.platform.di.IoDispatcherScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class ActivityStateRepositoryImpl @Inject constructor(
    private val sharedPreferences: EncryptedSharedPreferences,
    @param:IoDispatcherScope private val ioDispatcherScope: CoroutineScope,
) : ActivityStateRepository {

    private val prefKey = "activity_history_state"

    override suspend fun saveAreActivitiesEnabled(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(prefKey, enabled)
        }
    }

    override fun areActivitiesEnabled(): Boolean = sharedPreferences.getBoolean(prefKey, true)

    override fun areActivitiesEnabledFlow(): StateFlow<Boolean> = callbackFlow {
        trySend(sharedPreferences.getBoolean(prefKey, true))

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { preferences, changedKey ->
            if (changedKey == prefKey) {
                trySend(preferences.getBoolean(prefKey, true))
            }
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.stateIn(
        ioDispatcherScope,
        SharingStarted.Eagerly,
        initialValue = sharedPreferences.getBoolean(prefKey, true)
    )
}
