package ch.admin.foitt.wallet.platform.utils

import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

fun CoroutineScope.launchTimer(
    durationMillis: Long,
    coroutineContext: CoroutineContext = Dispatchers.IO,
    onProgress: (progressRatio: Float) -> Unit
): Job = launch(coroutineContext) {
    countDownFlow(durationMillis = durationMillis).collect { remainingTimeMillis ->
        val progress = 1f - (remainingTimeMillis.toFloat() / durationMillis.toFloat())
        onProgress(progress)
    }
}
private fun countDownFlow(durationMillis: Long, tickIntervalMillis: Long = 250) = flow {
    val startTime = SystemClock.elapsedRealtime()
    val endTime = startTime + durationMillis

    var nextTickTime = startTime

    while (SystemClock.elapsedRealtime() < endTime) {
        val remaining = endTime - SystemClock.elapsedRealtime()
        emit(remaining.coerceAtLeast(0L))

        nextTickTime += tickIntervalMillis

        // Delay only the remaining time until the next scheduled tick
        val sleepTime = nextTickTime - SystemClock.elapsedRealtime()
        if (sleepTime > 0) {
            delay(sleepTime)
        }
    }
    emit(0L)
}
