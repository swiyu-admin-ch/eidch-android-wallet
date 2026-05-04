package ch.admin.foitt.wallet.platform.activityList.domain.usecase

import ch.admin.foitt.wallet.platform.activityList.domain.model.DeleteActivityError
import com.github.michaelbull.result.Result

interface DeleteAllActivities {
    suspend operator fun invoke(): Result<Unit, DeleteActivityError>
}
