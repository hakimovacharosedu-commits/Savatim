package uz.savatim.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val name: String = "",
    @SerialName("role_label") val roleLabel: String = "other",
    val gender: String = "f",
    val language: String = "uz",
    @SerialName("notify_app") val notifyApp: Boolean = true,
    @SerialName("notify_telegram") val notifyTelegram: Boolean = false,
    @SerialName("telegram_chat_id") val telegramChatId: Long? = null,
    @SerialName("current_home_id") val currentHomeId: String? = null,
)

@Serializable
data class Home(
    val id: String,
    val name: String,
    @SerialName("invite_code") val inviteCode: String,
    @SerialName("owner_id") val ownerId: String,
)

@Serializable
data class HomeMemberRow(
    @SerialName("home_id") val homeId: String,
    @SerialName("user_id") val userId: String,
    val role: String,
)

/** A member of the current home: their profile + their role in this home (owner/admin/member). */
data class Member(val profile: Profile, val role: String)

@Serializable
data class Item(
    val id: String,
    @SerialName("home_id") val homeId: String,
    val name: String,
    @SerialName("catalog_key") val catalogKey: String? = null,
    val category: String = "other",
    val quantity: String = "",
    val note: String = "",
    @SerialName("added_by") val addedBy: String? = null,
    val bought: Boolean = false,
    @SerialName("bought_by") val boughtBy: String? = null,
    @SerialName("bought_at") val boughtAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class NewItem(
    @SerialName("home_id") val homeId: String,
    val name: String,
    @SerialName("catalog_key") val catalogKey: String? = null,
    val category: String = "other",
    val quantity: String = "",
    val note: String = "",
    @SerialName("added_by") val addedBy: String,
)

/** One product inside an activity event. Catalog products carry all three translations. */
@Serializable
data class ActivityItem(
    val key: String? = null,
    val name: String? = null,
    val uz: String? = null,
    val ru: String? = null,
    val en: String? = null,
)

@Serializable
data class Activity(
    val id: Long,
    @SerialName("home_id") val homeId: String,
    @SerialName("actor_id") val actorId: String? = null,
    val type: String,
    val items: List<ActivityItem> = emptyList(),
    @SerialName("created_at") val createdAt: String = "",
)

@Serializable
data class NewActivity(
    @SerialName("home_id") val homeId: String,
    @SerialName("actor_id") val actorId: String,
    val type: String,
    val items: List<ActivityItem> = emptyList(),
)

object ActivityType {
    const val ADDED = "added"
    const val BOUGHT = "bought"
    const val REMOVED = "removed"
    const val JOINED = "joined"
    const val LEFT = "left"
    const val ALL_BOUGHT = "all_bought"
}

object HomeRole {
    const val OWNER = "owner"
    const val ADMIN = "admin"
    const val MEMBER = "member"
}
