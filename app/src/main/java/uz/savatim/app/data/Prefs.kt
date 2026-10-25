package uz.savatim.app.data

import android.content.Context

/** Small on-device settings (not synced). */
class Prefs(context: Context) {
    private val sp = context.applicationContext.getSharedPreferences("savatim", Context.MODE_PRIVATE)

    /** "uz" | "ru" | "en", or null if the user hasn't picked a language yet. */
    var language: String?
        get() = sp.getString("language", null)
        set(value) = sp.edit().putString("language", value).apply()

    var onboardingDone: Boolean
        get() = sp.getBoolean("onboarding_done", false)
        set(value) = sp.edit().putBoolean("onboarding_done", value).apply()

    var askedNotificationPermission: Boolean
        get() = sp.getBoolean("asked_notif", false)
        set(value) = sp.edit().putBoolean("asked_notif", value).apply()

    /** Highest activity id the user has already seen/been notified about, per home. */
    fun lastSeenActivity(homeId: String): Long = sp.getLong("seen_$homeId", 0L)

    fun setLastSeenActivity(homeId: String, id: Long) {
        if (id > lastSeenActivity(homeId)) sp.edit().putLong("seen_$homeId", id).apply()
    }
}
