package uz.savatim.app.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import uz.savatim.app.BuildConfig
import java.util.concurrent.TimeUnit

/** Error returned by Supabase. [message] contains the server's text (e.g. "Invalid login credentials"). */
class ApiException(val status: Int, message: String) : Exception(message)

/**
 * Talks to Supabase with plain HTTP requests (OkHttp) – no Supabase SDK.
 *  - /auth/v1/...  → sign up, log in, refresh the login token
 *  - /rest/v1/...  → read/write tables and call our SQL functions
 */
object Supa {
    /** Accepts the URL in any form people copy it: with /rest/v1/, trailing slash, spaces… */
    private val url: String = BuildConfig.SUPABASE_URL.trim()
        .removeSuffix("/")
        .removeSuffix("/rest/v1")
        .removeSuffix("/")
    private val key: String = BuildConfig.SUPABASE_ANON_KEY.trim()

    val isConfigured: Boolean = url.startsWith("https://") && key.isNotBlank()

    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
    }

    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val jsonType = "application/json; charset=utf-8".toMediaType()
    private val refreshLock = Mutex()
    private var prefs: SharedPreferences? = null

    /** Call once from Application.onCreate. */
    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences("session", Context.MODE_PRIVATE)
    }

    // ------------------------------------------------------------------ session (login) storage

    @Serializable
    data class Session(
        val accessToken: String,
        val refreshToken: String,
        val expiresAt: Long,
        val userId: String,
        val email: String = "",
    )

    var session: Session?
        get() = prefs?.getString("session", null)?.let { runCatching { json.decodeFromString<Session>(it) }.getOrNull() }
        set(value) {
            prefs?.edit()?.apply {
                if (value == null) remove("session") else putString("session", json.encodeToString(Session.serializer(), value))
            }?.commit()
        }

    @Serializable
    private data class AuthUser(val id: String, val email: String? = null)

    /** Response of /auth/v1/token and /auth/v1/signup. */
    @Serializable
    private data class AuthResponse(
        val access_token: String? = null,
        val refresh_token: String? = null,
        val expires_in: Long? = null,
        val expires_at: Long? = null,
        val user: AuthUser? = null,
    )

    private fun now() = System.currentTimeMillis() / 1000

    /** Stores the login if the response contains one. Returns true if the user is now logged in. */
    private fun saveAuth(body: String): Boolean {
        val r = json.decodeFromString<AuthResponse>(body)
        val token = r.access_token ?: return false
        val user = r.user ?: return false
        session = Session(
            accessToken = token,
            refreshToken = r.refresh_token ?: "",
            expiresAt = r.expires_at ?: (now() + (r.expires_in ?: 3600)),
            userId = user.id,
            email = user.email ?: "",
        )
        return true
    }

    // ------------------------------------------------------------------ auth endpoints

    suspend fun signUp(bodyJson: String): Boolean = saveAuth(send("POST", "/auth/v1/signup", bodyJson, token = null))

    suspend fun signIn(bodyJson: String) {
        saveAuth(send("POST", "/auth/v1/token?grant_type=password", bodyJson, token = null))
    }

    suspend fun signOut() {
        val s = session
        session = null
        if (s != null) runCatching { send("POST", "/auth/v1/logout", null, token = s.accessToken) }
    }

    /** Returns a valid access token, refreshing it first if it's about to expire. */
    private suspend fun accessToken(): String = refreshLock.withLock {
        val s = session ?: throw ApiException(401, "not_authenticated")
        if (s.expiresAt - now() > 60) return@withLock s.accessToken
        refreshNow(s)
    }

    private suspend fun refreshNow(s: Session): String {
        try {
            val body = send("POST", "/auth/v1/token?grant_type=refresh_token",
                """{"refresh_token":${json.encodeToString(String.serializer(), s.refreshToken)}}""", token = null)
            if (!saveAuth(body)) throw ApiException(401, "not_authenticated")
        } catch (e: ApiException) {
            // Refresh token is no longer valid → the user must log in again.
            if (e.status in 400..499) session = null
            throw e
        }
        return session?.accessToken ?: throw ApiException(401, "not_authenticated")
    }

    // ------------------------------------------------------------------ database (PostgREST)

    /**
     * Calls the database API. [path] is e.g. "items?home_id=eq.123&order=created_at.desc"
     * or "rpc/create_home". Returns the raw JSON text of the response.
     */
    suspend fun rest(method: String, path: String, bodyJson: String? = null, prefer: String? = null): String {
        val token = accessToken()
        return try {
            send(method, "/rest/v1/$path", bodyJson, token, prefer)
        } catch (e: ApiException) {
            if (e.status != 401) throw e
            // Token was rejected – refresh once and retry.
            val fresh = refreshLock.withLock { refreshNow(session ?: throw e) }
            send(method, "/rest/v1/$path", bodyJson, fresh, prefer)
        }
    }

    private suspend fun send(
        method: String,
        path: String,
        bodyJson: String?,
        token: String?,
        prefer: String? = null,
    ): String = withContext(Dispatchers.IO) {
        val body = when {
            bodyJson != null -> bodyJson.toRequestBody(jsonType)
            method == "GET" || method == "DELETE" -> null
            else -> "{}".toRequestBody(jsonType)
        }
        val request = Request.Builder()
            .url(url + path)
            .method(method, body)
            .header("apikey", key)
            .header("Authorization", "Bearer ${token ?: key}")
            .header("Accept", "application/json")
            .apply { if (prefer != null) header("Prefer", prefer) }
            .build()
        http.newCall(request).execute().use { response ->
            val text = response.body?.string() ?: ""
            if (!response.isSuccessful) throw ApiException(response.code, text.ifBlank { "HTTP ${response.code}" })
            text
        }
    }
}
