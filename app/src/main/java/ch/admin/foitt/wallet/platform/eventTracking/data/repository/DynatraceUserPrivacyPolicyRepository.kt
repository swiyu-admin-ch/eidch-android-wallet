package ch.admin.foitt.wallet.platform.eventTracking.data.repository

import ch.admin.foitt.wallet.platform.eventTracking.domain.repository.UserPrivacyPolicyRepository
import com.dynatrace.android.agent.Dynatrace
import com.dynatrace.android.agent.conf.DataCollectionLevel
import com.dynatrace.android.agent.conf.UserPrivacyOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class DynatraceUserPrivacyPolicyRepository @Inject constructor() : UserPrivacyPolicyRepository {

    private val _isUserPrivacyPolicyAcceptedFlow = MutableStateFlow(isDynatraceEnabled)
    override val isUserPrivacyPolicyAcceptedFlow = _isUserPrivacyPolicyAcceptedFlow.asStateFlow()

    override fun applyUserPrivacyPolicy(hasAccepted: Boolean) {
        Dynatrace.applyUserPrivacyOptions(
            UserPrivacyOptions.builder()
                .withDataCollectionLevel(if (hasAccepted) DataCollectionLevel.PERFORMANCE else DataCollectionLevel.OFF)
                .withCrashReportingOptedIn(hasAccepted)
                .withScreenRecordOptedIn(false) // always disable crash replay
                .build()
        )
        _isUserPrivacyPolicyAcceptedFlow.value = isDynatraceEnabled
    }

    private val isDynatraceEnabled: Boolean
        get() = Dynatrace.getUserPrivacyOptions().dataCollectionLevel != DataCollectionLevel.OFF
}
