package uz.savatim.app.data

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant

/** All calls to Supabase live here. Row-level security on the server decides what each user may see. */
object Repo {
    private val json get() = Supa.json

    /** Builds "id=in.(a,b,c)" style filter values. */
    private fun inList(ids: List<String>) = ids.joinToString(",", prefix = "in.(", postfix = ")")

    private const val MINIMAL = "return=minimal"

    // ---------------- Auth ----------------

    fun userId(): String? = Supa.session?.userId
    fun email(): String = Supa.session?.email ?: ""

    /** Returns false if Supabase wants the email confirmed first (no login yet). */
    suspend fun signUp(email: String, password: String, name: String, language: String): Boolean {
        val body = buildJsonObject {
            put("email", email)
            put("password", password)
            put("data", buildJsonObject {
                put("name", name)
                put("language", language)
            })
        }
        return Supa.signUp(body.toString())
    }

    suspend fun signIn(email: String, password: String) {
        Supa.signIn(buildJsonObject {
            put("email", email)
            put("password", password)
        }.toString())
    }

    suspend fun signOut() = Supa.signOut()

    // ---------------- Profile ----------------

    suspend fun profile(): Profile? {
        val id = userId() ?: return null
        val text = Supa.rest("GET", "profiles?id=eq.$id&select=*")
        return json.decodeFromString(ListSerializer(Profile.serializer()), text).firstOrNull()
    }

    suspend fun updateProfile(fields: JsonObject) {
        val id = userId() ?: return
        Supa.rest("PATCH", "profiles?id=eq.$id", fields.toString(), MINIMAL)
    }

    // ---------------- Homes & members ----------------

    /** Row-level security only returns homes the user is a member of. */
    suspend fun homes(): List<Home> =
        json.decodeFromString(ListSerializer(Home.serializer()), Supa.rest("GET", "homes?select=*&order=created_at.asc"))

    suspend fun members(homeId: String): List<Member> {
        val rows = json.decodeFromString(
            ListSerializer(HomeMemberRow.serializer()),
            Supa.rest("GET", "home_members?home_id=eq.$homeId&select=*"),
        )
        if (rows.isEmpty()) return emptyList()
        val profiles = json.decodeFromString(
            ListSerializer(Profile.serializer()),
            Supa.rest("GET", "profiles?id=${inList(rows.map { it.userId })}&select=*"),
        ).associateBy { it.id }
        return rows.mapNotNull { row -> profiles[row.userId]?.let { Member(it, row.role) } }
            .sortedWith(compareBy({ roleOrder(it.role) }, { it.profile.name }))
    }

    private fun roleOrder(role: String) = when (role) {
        HomeRole.OWNER -> 0
        HomeRole.ADMIN -> 1
        else -> 2
    }

    private suspend fun rpc(name: String, params: JsonObject = JsonObject(emptyMap())): String =
        Supa.rest("POST", "rpc/$name", params.toString())

    suspend fun createHome(name: String): Home =
        json.decodeFromString(Home.serializer(), rpc("create_home", buildJsonObject { put("p_name", name) }))

    suspend fun joinHome(code: String): Home =
        json.decodeFromString(Home.serializer(), rpc("join_home", buildJsonObject { put("p_code", code) }))

    suspend fun setCurrentHome(homeId: String) =
        updateProfile(buildJsonObject { put("current_home_id", homeId) })

    suspend fun renameHome(homeId: String, name: String) {
        Supa.rest("PATCH", "homes?id=eq.$homeId", buildJsonObject { put("name", name) }.toString(), MINIMAL)
    }

    suspend fun setMemberRole(homeId: String, userId: String, role: String) {
        rpc("set_member_role", buildJsonObject {
            put("p_home", homeId)
            put("p_user", userId)
            put("p_role", role)
        })
    }

    suspend fun transferOwnership(homeId: String, userId: String) {
        rpc("transfer_ownership", buildJsonObject {
            put("p_home", homeId)
            put("p_user", userId)
        })
    }

    suspend fun removeMember(homeId: String, userId: String) {
        Supa.rest("DELETE", "home_members?home_id=eq.$homeId&user_id=eq.$userId", prefer = MINIMAL)
    }

    suspend fun leaveHome(homeId: String) {
        rpc("leave_home", buildJsonObject { put("p_home", homeId) })
    }

    suspend fun deleteHome(homeId: String) {
        rpc("delete_home", buildJsonObject { put("p_home", homeId) })
    }

    suspend fun regenerateInviteCode(homeId: String): String =
        json.decodeFromString(String.serializer(), rpc("regenerate_invite_code", buildJsonObject { put("p_home", homeId) }))

    // ---------------- Items ----------------

    suspend fun items(homeId: String): List<Item> =
        json.decodeFromString(
            ListSerializer(Item.serializer()),
            Supa.rest("GET", "items?home_id=eq.$homeId&select=*&order=created_at.desc"),
        )

    suspend fun addItems(items: List<NewItem>) {
        if (items.isEmpty()) return
        Supa.rest("POST", "items", json.encodeToString(ListSerializer(NewItem.serializer()), items), MINIMAL)
    }

    suspend fun setBought(ids: List<String>, bought: Boolean) {
        if (ids.isEmpty()) return
        val me = userId()
        val body = buildJsonObject {
            put("bought", bought)
            if (bought) put("bought_by", me) else put("bought_by", JsonNull)
            if (bought) put("bought_at", Instant.now().toString()) else put("bought_at", JsonNull)
        }
        Supa.rest("PATCH", "items?id=${inList(ids)}", body.toString(), MINIMAL)
    }

    suspend fun updateItem(id: String, name: String?, quantity: String, note: String) {
        val body = buildJsonObject {
            if (name != null) put("name", name)
            put("quantity", quantity)
            put("note", note)
        }
        Supa.rest("PATCH", "items?id=eq.$id", body.toString(), MINIMAL)
    }

    suspend fun deleteItems(ids: List<String>) {
        if (ids.isEmpty()) return
        Supa.rest("DELETE", "items?id=${inList(ids)}", prefer = MINIMAL)
    }

    // ---------------- Activity ----------------

    suspend fun activity(homeId: String, afterId: Long = 0L): List<Activity> {
        val after = if (afterId > 0L) "&id=gt.$afterId" else ""
        return json.decodeFromString(
            ListSerializer(Activity.serializer()),
            Supa.rest("GET", "activity?home_id=eq.$homeId$after&select=*&order=id.desc&limit=60"),
        )
    }

    /** Cheap check used for polling: id of the newest event in a home (0 if none). */
    suspend fun latestActivityId(homeId: String): Long {
        val text = Supa.rest("GET", "activity?home_id=eq.$homeId&select=id&order=id.desc&limit=1")
        return json.decodeFromString(ListSerializer(IdOnly.serializer()), text).firstOrNull()?.id ?: 0L
    }

    @kotlinx.serialization.Serializable
    private data class IdOnly(val id: Long)

    suspend fun logActivity(homeId: String, type: String, items: List<ActivityItem> = emptyList()) {
        val me = userId() ?: return
        Supa.rest(
            "POST", "activity",
            json.encodeToString(NewActivity.serializer(), NewActivity(homeId, me, type, items)),
            MINIMAL,
        )
    }

    // ---------------- Telegram & account ----------------

    suspend fun createTelegramLinkCode(): String =
        json.decodeFromString(String.serializer(), rpc("create_telegram_link_code"))

    suspend fun disconnectTelegram() {
        rpc("disconnect_telegram")
    }

    suspend fun deleteAccount() {
        rpc("delete_account")
        Supa.session = null
    }
}
