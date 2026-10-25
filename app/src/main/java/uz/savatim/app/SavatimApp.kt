package uz.savatim.app

import android.app.Application
import android.content.Context
import android.util.Log
import uz.savatim.app.data.Supa
import uz.savatim.app.notify.ActivityWorker
import uz.savatim.app.notify.Notifier

class SavatimApp : Application() {
    override fun onCreate() {
        installCrashRecorder(this)
        super.onCreate()
        Supa.init(this)
        // Safe mode: after a crash, start nothing else so the crash report screen can show.
        if (lastCrash(this) != null) return
        runCatching { Notifier.createChannel(this) }
        runCatching { ActivityWorker.schedule(this) }
    }

    companion object {
        private const val PREFS = "crash"
        private const val KEY = "last"

        /** Saves the error of any crash so the next launch can show it (useful when testing via APK). */
        private fun installCrashRecorder(context: Context) {
            val previous = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, error ->
                runCatching {
                    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                        .edit().putString(KEY, Log.getStackTraceString(error)).commit()
                }
                previous?.uncaughtException(thread, error)
            }
        }

        fun lastCrash(context: Context): String? =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)

        fun clearCrash(context: Context) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY).commit()
        }
    }
}
