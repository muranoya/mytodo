package net.meshpeak.mytodo.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object TrashCleanerScheduler {

    /**
     * 1 日 1 回、深夜帯（03:00 ローカル時刻）に [TrashCleanerWorker] を実行する定期ジョブを登録する。
     * 既存のジョブがある場合は KEEP で保持するため、起動毎に重複して enqueue しても副作用なし。
     */
    fun schedule(context: Context, now: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())) {
        val request = PeriodicWorkRequestBuilder<TrashCleanerWorker>(Duration.ofDays(1))
            .setInitialDelay(initialDelayTo03(now))
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build(),
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            TrashCleanerWorker.UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    private fun initialDelayTo03(now: ZonedDateTime): Duration {
        val todayAt03 = now.with(LocalTime.of(3, 0))
        val nextAt03 = if (now.isBefore(todayAt03)) todayAt03 else todayAt03.plusDays(1)
        return Duration.between(now, nextAt03)
    }

    /** テスト補助: 特定日時基準で次回 03:00 までのディレイを計算する。 */
    internal fun initialDelayTo03(today: LocalDate, timeOfDay: LocalTime, zone: ZoneId): Duration =
        initialDelayTo03(ZonedDateTime.of(today, timeOfDay, zone))
}
