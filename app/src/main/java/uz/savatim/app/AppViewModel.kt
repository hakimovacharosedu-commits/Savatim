package uz.savatim.app

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import uz.savatim.app.data.Activity
import uz.savatim.app.data.ApiException
import uz.savatim.app.data.ActivityItem
import uz.savatim.app.data.ActivityType
import uz.savatim.app.data.Catalog
import uz.savatim.app.data.Home
import uz.savatim.app.data.HomeRole
import uz.savatim.app.data.Item
import uz.savatim.app.data.Member
import uz.savatim.app.data.NewItem
import uz.savatim.app.data.Prefs
import uz.savatim.app.data.Product
import uz.savatim.app.data.Profile
import uz.savatim.app.data.Repo
import uz.savatim.app.data.Supa
import uz.savatim.app.i18n.ActivityText
import uz.savatim.app.i18n.Lang

/** Where the app should go after the splash screen. */
enum class StartDestination { NOT_CONFIGURED, LANGUAGE, ONBOARDING, WELCOME, PROFILE_SETUP, HOME_SETUP, MAIN }

data class UiState(
    val profile: Profile? = null,
    val email: String = "",
    val homes: List<Home> = emptyList(),
    val home: Home? = null,
    val members: List<Member> = emptyList(),
    val items: List<Item> = emptyList(),
    val activity: List<Activity> = emptyList(),
    val refreshing: Boolean = false,
) {
    val myId: String? get() = profile?.id
    val myRole: String get() = members.firstOrNull { it.profile.id == myId }?.role ?: HomeRole.MEMBER
    val canManage: Boolean get() = myRole == HomeRole.OWNER || myRole == HomeRole.ADMIN
    fun member(id: String?): Profile? = members.firstOrNull { it.profile.id == id }?.profile
}

/** A one-off message for a snackbar: either a string resource or ready text. */
data class UiMessage(@StringRes val res: Int? = null, val text: String? = null)

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = Prefs(app)
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _messages = MutableSharedFlow<UiMessage>(extraBufferCapacity = 8)
    val messages: SharedFlow<UiMessage> = _messages

    /** True while a button action (login, create home...) is running. */
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    /** Checks the server every few seconds for changes by family members (while the app is open). */
    private var pollJob: Job? = null
    private var foreground = true

    val lang: Lang get() = Lang.current(getApplication<Application>())

    // ------------------------------------------------------------------ start

    suspend fun resolveStart(): StartDestination {
        if (prefs.language == null) return StartDestination.LANGUAGE
        if (!prefs.onboardingDone) return StartDestination.ONBOARDING
        if (!Supa.isConfigured) return StartDestination.NOT_CONFIGURED
        if (Repo.userId() == null) return StartDestination.WELCOME
        return try {
            afterLogin()
        } catch (e: Exception) {
            if (e is ApiException && e.status == 401) Repo.signOut() else report(e)
            StartDestination.WELCOME
        }
    }

    /** Loads profile + homes and decides the next screen. */
    private suspend fun afterLogin(): StartDestination {
        val profile = Repo.profile() ?: return StartDestination.WELCOME
        syncLanguage(profile)
        _state.update { it.copy(profile = profile, email = Repo.email()) }
        if (profile.roleLabel == "other" && profile.name.isBlank()) return StartDestination.PROFILE_SETUP
        val homes = Repo.homes()
        if (homes.isEmpty()) {
            _state.update { it.copy(homes = emptyList(), home = null) }
            return StartDestination.HOME_SETUP
        }
        val current = homes.firstOrNull { it.id == profile.currentHomeId } ?: homes.first()
        _state.update { it.copy(homes = homes, home = current) }
        openHome(current)
        return StartDestination.MAIN
    }

    /** Keeps the server copy of the language in sync (used for Telegram messages). */
    private suspend fun syncLanguage(profile: Profile) {
        val local = prefs.language ?: return
        if (profile.language != local) {
            runCatching { Repo.updateProfile(buildJsonObject { put("language", local) }) }
        }
    }

    fun setLanguage(code: String) {
        prefs.language = code
        if (Supa.isConfigured && Repo.userId() != null) {
            viewModelScope.launch { runCatching { Repo.updateProfile(buildJsonObject { put("language", code) }) } }
        }
    }

    fun finishOnboarding() {
        prefs.onboardingDone = true
    }

    // ------------------------------------------------------------------ auth

    fun login(email: String, password: String, onResult: (StartDestination) -> Unit) = action {
        Repo.signIn(email.trim(), password)
        onResult(afterLogin())
    }

    fun register(name: String, email: String, password: String, onResult: (StartDestination) -> Unit) = action {
        val loggedIn = Repo.signUp(email.trim(), password, name.trim(), prefs.language ?: lang.code)
        if (!loggedIn) {
            // Email confirmation is switched on in Supabase → user must confirm first.
            _messages.tryEmit(UiMessage(R.string.msg_check_email))
            return@action
        }
        Repo.updateProfile(buildJsonObject { put("name", name.trim()) })
        onResult(StartDestination.PROFILE_SETUP)
    }

    fun saveProfile(name: String, roleLabel: String, gender: String, onDone: (StartDestination) -> Unit) = action {
        Repo.updateProfile(buildJsonObject {
            put("name", name.trim())
            put("role_label", roleLabel)
            put("gender", gender)
        })
        val profile = Repo.profile()
        _state.update { it.copy(profile = profile) }
        if (_state.value.home != null) {
            reloadMembers()
            onDone(StartDestination.MAIN)
        } else {
            onDone(afterLogin())
        }
    }

    fun logout(onDone: () -> Unit) = action {
        stopPolling()
        Repo.signOut()
        _state.value = UiState()
        onDone()
    }

    fun deleteAccount(onDone: () -> Unit) = action {
        stopPolling()
        Repo.deleteAccount()
        _state.value = UiState()
        onDone()
    }

    // ------------------------------------------------------------------ homes

    fun createHome(name: String, onDone: () -> Unit) = action {
        val home = Repo.createHome(name.trim())
        switchTo(home)
        onDone()
    }

    fun joinHome(code: String, onDone: () -> Unit) = action {
        val home = Repo.joinHome(code.trim())
        switchTo(home)
        onDone()
    }

    fun selectHome(home: Home) = action {
        Repo.setCurrentHome(home.id)
        switchTo(home)
    }

    private suspend fun switchTo(home: Home) {
        val homes = Repo.homes()
        _state.update { it.copy(homes = homes, home = home, items = emptyList(), activity = emptyList(), members = emptyList()) }
        openHome(home)
    }

    fun renameHome(name: String) = homeAction { home ->
        Repo.renameHome(home.id, name.trim())
        val homes = Repo.homes()
        _state.update { s -> s.copy(homes = homes, home = homes.firstOrNull { it.id == home.id } ?: s.home) }
    }

    fun regenerateCode() = homeAction { home ->
        val code = Repo.regenerateInviteCode(home.id)
        _state.update { it.copy(home = home.copy(inviteCode = code)) }
    }

    fun setMemberRole(userId: String, role: String) = homeAction { home ->
        Repo.setMemberRole(home.id, userId, role)
        reloadMembers()
    }

    fun transferOwnership(userId: String) = homeAction { home ->
        Repo.transferOwnership(home.id, userId)
        reloadMembers()
    }

    fun removeMember(userId: String) = homeAction { home ->
        Repo.removeMember(home.id, userId)
        reloadMembers()
    }

    /** Leave (or delete, for the owner) the current home, then go to another home or to home setup. */
    fun leaveOrDeleteHome(delete: Boolean, onResult: (StartDestination) -> Unit) = homeAction { home ->
        stopPolling()
        if (delete) Repo.deleteHome(home.id) else Repo.leaveHome(home.id)
        _state.update { it.copy(home = null, members = emptyList(), items = emptyList(), activity = emptyList()) }
        onResult(afterLogin())
    }

    // ------------------------------------------------------------------ list

    private suspend fun openHome(home: Home) {
        loadHome(home.id)
        startPolling(home.id)
    }

    private suspend fun loadHome(homeId: String) {
        val members = Repo.members(homeId)
        val items = Repo.items(homeId)
        val activity = Repo.activity(homeId)
        _state.update { it.copy(members = members, items = items, activity = activity) }
        activity.firstOrNull()?.let { prefs.setLastSeenActivity(homeId, it.id) }
    }

    private suspend fun reloadMembers() {
        val home = _state.value.home ?: return
        _state.update { it.copy(members = Repo.members(home.id)) }
    }

    fun refresh() {
        val home = _state.value.home ?: return
        viewModelScope.launch {
            _state.update { it.copy(refreshing = true) }
            runCatching { loadHome(home.id) }.onFailure { report(it) }
            _state.update { it.copy(refreshing = false) }
        }
    }

    /** Adds catalog products and/or custom names as ONE event, so family gets one notification. */
    fun addProducts(products: List<Product>, customNames: List<String>, onDone: () -> Unit = {}) = homeAction { home ->
        val me = Repo.userId() ?: return@homeAction
        val existing = _state.value.items.filter { !it.bought }
        val newProducts = products.filter { p -> existing.none { it.catalogKey == p.key } }
        val newCustom = customNames.map { it.trim() }.filter { it.isNotEmpty() }
            .filter { n -> existing.none { it.catalogKey == null && it.name.equals(n, ignoreCase = true) } }
        if (newProducts.isEmpty() && newCustom.isEmpty()) {
            _messages.tryEmit(UiMessage(R.string.msg_already_in_list))
            onDone()
            return@homeAction
        }
        val rows = newProducts.map {
            NewItem(homeId = home.id, name = it.uz, catalogKey = it.key, category = it.category, addedBy = me)
        } + newCustom.map {
            NewItem(homeId = home.id, name = it, catalogKey = null, category = "other", addedBy = me)
        }
        Repo.addItems(rows)
        Repo.logActivity(
            home.id, ActivityType.ADDED,
            newProducts.map { it.toActivityItem() } + newCustom.map { ActivityItem(name = it) },
        )
        loadHome(home.id)
        onDone()
    }

    fun toggleBought(item: Item) = homeAction { home ->
        val nowBought = !item.bought
        Repo.setBought(listOf(item.id), nowBought)
        if (nowBought) {
            val remaining = _state.value.items.count { !it.bought && it.id != item.id }
            Repo.logActivity(home.id, if (remaining == 0) ActivityType.ALL_BOUGHT else ActivityType.BOUGHT, listOf(item.toActivityItem()))
        }
        loadHome(home.id)
    }

    fun updateItem(item: Item, name: String, quantity: String, note: String) = homeAction { home ->
        val newName = if (item.catalogKey == null && name.isNotBlank()) name.trim() else null
        Repo.updateItem(item.id, newName, quantity.trim(), note.trim())
        loadHome(home.id)
    }

    fun deleteItem(item: Item) = homeAction { home ->
        Repo.deleteItems(listOf(item.id))
        if (!item.bought) Repo.logActivity(home.id, ActivityType.REMOVED, listOf(item.toActivityItem()))
        loadHome(home.id)
    }

    /** Clears the "bought" section (no notification: nothing new happened). */
    fun clearBought() = homeAction { home ->
        Repo.deleteItems(_state.value.items.filter { it.bought }.map { it.id })
        loadHome(home.id)
    }

    fun canDelete(item: Item): Boolean = _state.value.canManage || item.addedBy == _state.value.myId

    private fun Item.toActivityItem(): ActivityItem =
        Catalog.product(catalogKey)?.toActivityItem() ?: ActivityItem(name = name)

    // ------------------------------------------------------------------ notifications settings

    fun setNotifyApp(enabled: Boolean) = profileUpdate { put("notify_app", enabled) }

    fun setNotifyTelegram(enabled: Boolean) = profileUpdate { put("notify_telegram", enabled) }

    private fun profileUpdate(block: kotlinx.serialization.json.JsonObjectBuilder.() -> Unit) = action {
        Repo.updateProfile(buildJsonObject(block))
        _state.update { it.copy(profile = Repo.profile()) }
    }

    /** Returns a t.me link that connects this account to the Telegram bot. */
    fun telegramLink(onLink: (String) -> Unit) = action {
        val code = Repo.createTelegramLinkCode()
        onLink("https://t.me/${BuildConfig.TELEGRAM_BOT_USERNAME}?start=$code")
    }

    fun disconnectTelegram() = action {
        Repo.disconnectTelegram()
        _state.update { it.copy(profile = Repo.profile()) }
    }

    fun reloadProfile() {
        if (!Supa.isConfigured || Repo.userId() == null) return
        viewModelScope.launch {
            runCatching { Repo.profile() }.getOrNull()?.let { p -> _state.update { it.copy(profile = p) } }
        }
    }

    // ------------------------------------------------------------------ live updates (polling)

    /** Called by the main screen when the app goes to the foreground/background. */
    fun setForeground(value: Boolean) {
        foreground = value
        val homeId = _state.value.home?.id
        if (value && homeId != null) {
            startPolling(homeId)
            viewModelScope.launch { runCatching { loadHome(homeId) } }
        } else {
            stopPolling()
        }
    }

    private fun startPolling(homeId: String) {
        stopPolling()
        if (!foreground) return
        pollJob = viewModelScope.launch {
            var tick = 0
            while (true) {
                delay(4_000)
                tick++
                val known = _state.value.activity.firstOrNull()?.id ?: 0L
                val latest = runCatching { Repo.latestActivityId(homeId) }.getOrNull() ?: continue
                if (latest != known) {
                    reloadAndAnnounce(homeId, known)
                } else if (tick % 8 == 0) {
                    // Every ~30 s also pick up quiet edits (quantity, notes…)
                    runCatching { loadHome(homeId) }
                }
            }
        }
    }

    private fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    /** Reloads the home and shows what family members just did as a snackbar. */
    private suspend fun reloadAndAnnounce(homeId: String, before: Long) {
        runCatching { loadHome(homeId) }
        val s = _state.value
        val fresh = s.activity.filter { it.id > before && it.actorId != s.myId }
        fresh.firstOrNull()?.let { a ->
            val profiles = s.members.map { it.profile }
            _messages.tryEmit(UiMessage(text = ActivityText.build(a.type, s.member(a.actorId), profiles, a.items, lang)))
        }
    }

    override fun onCleared() {
        stopPolling()
        super.onCleared()
    }

    // ------------------------------------------------------------------ helpers

    private fun action(block: suspend () -> Unit) {
        if (_busy.value) return
        viewModelScope.launch {
            _busy.value = true
            try {
                block()
            } catch (e: Exception) {
                report(e)
            } finally {
                _busy.value = false
            }
        }
    }

    private fun homeAction(block: suspend (Home) -> Unit) {
        val home = _state.value.home ?: return
        viewModelScope.launch {
            try {
                block(home)
            } catch (e: Exception) {
                report(e)
            }
        }
    }

    private fun report(e: Throwable) {
        e.printStackTrace()
        val msg = (e.message ?: "").lowercase()
        val res = when {
            "invalid login credentials" in msg -> R.string.err_login
            "already registered" in msg || "already exists" in msg -> R.string.err_user_exists
            "password should be" in msg || "weak password" in msg -> R.string.err_weak_password
            "invalid_code" in msg -> R.string.err_invalid_code
            "email not confirmed" in msg -> R.string.msg_check_email
            "not_allowed" in msg || "row-level security" in msg -> R.string.err_not_allowed
            "unable to resolve host" in msg || "timeout" in msg || "failed to connect" in msg ||
                e is java.io.IOException -> R.string.err_network
            else -> R.string.err_generic
        }
        if (res == R.string.err_generic) {
            // Unknown error: show the technical detail too, so it can be reported and fixed.
            val detail = (e.message ?: e.javaClass.simpleName).replace("\n", " ").take(180)
            _messages.tryEmit(UiMessage(text = getApplication<Application>().getString(res) + "\n" + detail))
        } else {
            _messages.tryEmit(UiMessage(res))
        }
    }
}
