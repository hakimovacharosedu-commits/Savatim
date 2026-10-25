package uz.savatim.app.notify

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import uz.savatim.app.data.Prefs
import uz.savatim.app.data.Repo
import uz.savatim.app.data.Supa
import uz.savatim.app.i18n.ActivityText
import uz.savatim.app.i18n.Lang
import java.util.concurrent.TimeUnit

/**
 * Runs about every 15 minutes in the background: checks each home for new activity
 * by other family members and shows phone notifications for it.
 * (Instant push when the app is closed would need Firebase Cloud Messaging – a later step.)
 */
class ActivityWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = try {
        work()
    } catch (e: Throwable) {
        e.printStackTrace()
        Result.success()
    }

    private suspend fun work(): Result {
        if (!Supa.isConfigured) return Result.success()
        if (uz.savatim.app.SavatimApp.lastCrash(applicationContext) != null) return Result.success()
        val ctx = applicationContext
        if (!Notifier.canNotify(ctx)) return Result.success()

        val myId = Repo.userId() ?: return Result.success()

        return try {
            val me = Repo.profile() ?: return Result.success()
            if (!me.notifyApp) return Result.success()
            val prefs = Prefs(ctx)
            val lang = Lang.current(ctx)

            for (home in Repo.homes()) {
                val lastSeen = prefs.lastSeenActivity(home.id)
                val fresh = Repo.activity(home.id, afterId = lastSeen)
                if (fresh.isEmpty()) continue
                prefs.setLastSeenActivity(home.id, fresh.first().id)
                // First run for this home: just remember where we are, don't flood old events.
                if (lastSeen == 0L) continue

                val others = fresh.filter { it.actorId != myId }.sortedBy { it.id }.takeLast(5)
                if (others.isEmpty()) continue
                val profiles = Repo.members(home.id).map { it.profile }
                for (a in others) {
                    val actor = profiles.firstOrNull { it.id == a.actorId }
                    val text = ActivityText.build(a.type, actor, profiles, a.items, lang)
                    Notifier.show(ctx, (a.id % Int.MAX_VALUE).toInt(), "🏠 ${home.name}", text)
                }
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        private const val NAME = "savatim-activity-check"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ActivityWorker>(15, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
