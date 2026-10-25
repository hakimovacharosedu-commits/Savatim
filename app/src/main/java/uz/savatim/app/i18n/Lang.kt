package uz.savatim.app.i18n

import android.content.Context
import android.content.res.Configuration
import uz.savatim.app.data.ActivityItem
import uz.savatim.app.data.ActivityType
import uz.savatim.app.data.Prefs
import uz.savatim.app.data.Profile
import java.util.Locale

/** App languages. Uzbek is the default/fallback. */
enum class Lang(val code: String, val nativeName: String, val flag: String) {
    UZ("uz", "Oʻzbekcha", "🇺🇿"),
    RU("ru", "Русский", "🇷🇺"),
    EN("en", "English", "🇬🇧");

    companion object {
        fun of(code: String?): Lang = Lang.entries.firstOrNull { it.code == code } ?: UZ

        /** Language currently used by the app UI. */
        fun current(context: Context): Lang =
            Prefs(context).language?.let { of(it) } ?: fromSystem()

        fun fromSystem(): Lang = when (Locale.getDefault().language) {
            "ru" -> RU
            "en" -> EN
            else -> UZ
        }
    }
}

object LocaleHelper {
    /** Wraps a context so its resources use the language chosen inside the app. */
    fun wrap(base: Context): Context {
        val code = Prefs(base).language ?: return base
        val locale = Locale.forLanguageTag(code)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }
}

/** Family role labels a member can pick for themselves. */
object Roles {
    val keys = listOf(
        "mom", "dad", "grandma", "grandpa",
        "sister_older", "brother_older", "sister_younger", "brother_younger", "other",
    )

    private val female = setOf("mom", "grandma", "sister_older", "sister_younger")
    private val male = setOf("dad", "grandpa", "brother_older", "brother_younger")

    /** Returns the gender implied by the role, or null for "other". */
    fun impliedGender(key: String): String? = when (key) {
        in female -> "f"
        in male -> "m"
        else -> null
    }

    fun emoji(key: String): String = when (key) {
        "mom" -> "👩"
        "dad" -> "👨"
        "grandma" -> "👵"
        "grandpa" -> "👴"
        "sister_older", "sister_younger" -> "👧"
        "brother_older", "brother_younger" -> "👦"
        else -> "🙂"
    }

    private val labels = mapOf(
        Lang.UZ to mapOf(
            "mom" to "Ona", "dad" to "Dada", "grandma" to "Buvi", "grandpa" to "Bobo",
            "sister_older" to "Opa", "brother_older" to "Aka",
            "sister_younger" to "Singil", "brother_younger" to "Uka", "other" to "Boshqa",
        ),
        Lang.RU to mapOf(
            "mom" to "Мама", "dad" to "Папа", "grandma" to "Бабушка", "grandpa" to "Дедушка",
            "sister_older" to "Старшая сестра", "brother_older" to "Старший брат",
            "sister_younger" to "Младшая сестра", "brother_younger" to "Младший брат", "other" to "Другое",
        ),
        Lang.EN to mapOf(
            "mom" to "Mom", "dad" to "Dad", "grandma" to "Grandma", "grandpa" to "Grandpa",
            "sister_older" to "Older sister", "brother_older" to "Older brother",
            "sister_younger" to "Younger sister", "brother_younger" to "Younger brother", "other" to "Other",
        ),
    )

    fun label(key: String, lang: Lang): String = labels[lang]?.get(key) ?: key
}

/**
 * Builds the human sentence for an activity event, e.g. "Ona roʻyxatga sabzi qoʻshdi".
 * Keep in sync with supabase/functions/notify/index.ts (the Telegram version).
 */
object ActivityText {

    /** "Ona", or "Ona (Malika)" if two members share the label, or the name for "other". */
    fun actorName(actor: Profile?, members: List<Profile>, lang: Lang): String {
        if (actor == null) return when (lang) {
            Lang.RU -> "Кто-то"
            Lang.EN -> "Someone"
            Lang.UZ -> "Kimdir"
        }
        if (actor.roleLabel == "other" || actor.roleLabel !in Roles.keys) return actor.name.ifBlank { "?" }
        val label = Roles.label(actor.roleLabel, lang)
        val duplicates = members.count { it.roleLabel == actor.roleLabel } > 1
        return if (duplicates && actor.name.isNotBlank()) "$label (${actor.name})" else label
    }

    fun itemName(item: ActivityItem, lang: Lang): String {
        val localized = when (lang) {
            Lang.UZ -> item.uz
            Lang.RU -> item.ru
            Lang.EN -> item.en
        }
        if (item.key != null && !localized.isNullOrBlank()) return localized.lowercase()
        return item.name ?: localized ?: "?"
    }

    private fun ruCount(n: Int): String {
        val m10 = n % 10
        val m100 = n % 100
        return when {
            m10 == 1 && m100 != 11 -> "$n товар"
            m10 in 2..4 && (m100 < 12 || m100 > 14) -> "$n товара"
            else -> "$n товаров"
        }
    }

    fun build(
        type: String,
        actor: Profile?,
        members: List<Profile>,
        items: List<ActivityItem>,
        lang: Lang,
    ): String {
        val a = actorName(actor, members, lang)
        val names = items.map { itemName(it, lang) }
        val n = names.size
        val one = names.firstOrNull() ?: ""
        val list = names.joinToString(", ")
        val f = actor?.gender == "f"

        return when (lang) {
            Lang.RU -> {
                fun v(m: String, w: String) = if (f) w else m
                when (type) {
                    ActivityType.ADDED -> if (n <= 1) "$a ${v("добавил", "добавила")} в список: $one"
                    else "$a ${v("добавил", "добавила")} в список ${ruCount(n)}: $list"
                    ActivityType.BOUGHT -> if (n <= 1) "$a ${v("купил", "купила")}: $one"
                    else "$a ${v("купил", "купила")} ${ruCount(n)}: $list"
                    ActivityType.REMOVED -> if (n <= 1) "$a ${v("удалил", "удалила")} из списка: $one"
                    else "$a ${v("удалил", "удалила")} из списка ${ruCount(n)}: $list"
                    ActivityType.JOINED -> "$a ${v("присоединился", "присоединилась")} к дому"
                    ActivityType.LEFT -> "$a ${v("вышел", "вышла")} из дома"
                    ActivityType.ALL_BOUGHT -> "$a ${v("купил", "купила")} всё! Список пуст 🎉"
                    else -> a
                }
            }
            Lang.EN -> when (type) {
                ActivityType.ADDED -> if (n <= 1) "$a added $one to the list" else "$a added $n items to the list: $list"
                ActivityType.BOUGHT -> if (n <= 1) "$a bought $one" else "$a bought $n items: $list"
                ActivityType.REMOVED -> if (n <= 1) "$a removed $one from the list" else "$a removed $n items from the list: $list"
                ActivityType.JOINED -> "$a joined the home"
                ActivityType.LEFT -> "$a left the home"
                ActivityType.ALL_BOUGHT -> "$a bought everything! The list is empty 🎉"
                else -> a
            }
            Lang.UZ -> when (type) {
                ActivityType.ADDED -> if (n <= 1) "$a roʻyxatga $one qoʻshdi" else "$a roʻyxatga $n ta mahsulot qoʻshdi: $list"
                ActivityType.BOUGHT -> if (n <= 1) "$a $one sotib oldi" else "$a $n ta mahsulot sotib oldi: $list"
                ActivityType.REMOVED -> if (n <= 1) "$a roʻyxatdan ${one}ni oʻchirdi" else "$a roʻyxatdan $n ta mahsulotni oʻchirdi: $list"
                ActivityType.JOINED -> "$a uyga qoʻshildi"
                ActivityType.LEFT -> "$a uydan chiqdi"
                ActivityType.ALL_BOUGHT -> "$a hammasini sotib oldi! Roʻyxat boʻsh 🎉"
                else -> a
            }
        }
    }
}
