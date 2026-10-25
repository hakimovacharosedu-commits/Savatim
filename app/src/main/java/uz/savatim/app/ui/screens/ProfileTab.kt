package uz.savatim.app.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import uz.savatim.app.AppViewModel
import uz.savatim.app.BuildConfig
import uz.savatim.app.R
import uz.savatim.app.UiState
import uz.savatim.app.i18n.Lang
import uz.savatim.app.i18n.Roles
import uz.savatim.app.ui.Routes
import uz.savatim.app.ui.components.Avatar
import uz.savatim.app.ui.components.Badge
import uz.savatim.app.ui.goFresh
import uz.savatim.app.ui.theme.SColors

/** Nectar "Account" screen. */
@Composable
fun ProfileTab(vm: AppViewModel, state: UiState, nav: NavHostController, onOpenSwitcher: () -> Unit) {
    val context = LocalContext.current
    val lang = vm.lang
    val profile = state.profile
    var showLanguage by remember { mutableStateOf(false) }
    var confirmLogout by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    // Coming back from Telegram → refresh "connected" status
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) vm.reloadProfile()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // ---- Header
        Row(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(Roles.emoji(profile?.roleLabel ?: "other"), 64.dp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(profile?.name ?: "", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Rounded.Edit, null, tint = SColors.Primary,
                        modifier = Modifier.size(18.dp).clickable { nav.navigate(Routes.PROFILE_EDIT) },
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(state.email, style = MaterialTheme.typography.bodyMedium, color = SColors.GrayText)
                if (profile != null && profile.roleLabel != "other") {
                    Spacer(Modifier.height(6.dp))
                    Badge(Roles.label(profile.roleLabel, lang))
                }
            }
        }
        HorizontalDivider(color = SColors.Divider)

        // ---- Home
        SettingsRow(Icons.Rounded.Groups, stringResource(R.string.members), state.home?.name) { nav.navigate(Routes.MEMBERS) }
        SettingsRow(Icons.Rounded.Home, stringResource(R.string.my_homes), state.homes.size.toString(), onClick = onOpenSwitcher)

        // ---- Notifications
        SectionLabel(stringResource(R.string.notifications))
        SwitchRow(
            Icons.Rounded.NotificationsActive,
            stringResource(R.string.notify_in_app),
            stringResource(R.string.notify_in_app_desc),
            checked = profile?.notifyApp ?: true,
        ) { vm.setNotifyApp(it) }

        val telegramConnected = profile?.telegramChatId != null
        if (telegramConnected) {
            SwitchRow(
                Icons.AutoMirrored.Rounded.Send,
                stringResource(R.string.telegram),
                stringResource(R.string.telegram_connected),
                checked = profile?.notifyTelegram ?: false,
            ) { vm.setNotifyTelegram(it) }
            TextButton(onClick = { vm.disconnectTelegram() }, modifier = Modifier.padding(start = 60.dp)) {
                Text(stringResource(R.string.telegram_disconnect), color = SColors.GrayText)
            }
        } else {
            SettingsRow(
                Icons.AutoMirrored.Rounded.Send,
                stringResource(R.string.telegram_connect),
                stringResource(R.string.telegram_connect_desc),
            ) {
                if (BuildConfig.TELEGRAM_BOT_USERNAME.isBlank()) return@SettingsRow
                vm.telegramLink { link ->
                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link))) }
                }
            }
        }

        // ---- App
        SectionLabel(stringResource(R.string.app_settings))
        SettingsRow(Icons.Rounded.Language, stringResource(R.string.language), "${lang.flag} ${lang.nativeName}") { showLanguage = true }

        Spacer(Modifier.height(28.dp))
        // Nectar "Log Out" button
        Row(
            Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(19.dp))
                .background(SColors.Field)
                .clickable { confirmLogout = true }
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.AutoMirrored.Rounded.ExitToApp, null, tint = SColors.Primary)
            Text(
                stringResource(R.string.logout),
                style = MaterialTheme.typography.labelLarge,
                color = SColors.Primary,
                modifier = Modifier.weight(1f).padding(end = 24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
        TextButton(onClick = { confirmDelete = true }, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)) {
            Text(stringResource(R.string.delete_account), color = SColors.Danger, fontSize = 13.sp)
        }
        Text(
            "Savatim ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodySmall,
            color = SColors.Gray4,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 24.dp),
        )
    }

    if (showLanguage) {
        LanguageDialog(current = lang, onDismiss = { showLanguage = false }) { picked ->
            showLanguage = false
            if (picked != lang) {
                vm.setLanguage(picked.code)
                (context as? Activity)?.recreate()
            }
        }
    }
    if (confirmLogout) {
        ConfirmDialog(
            text = stringResource(R.string.logout_confirm),
            confirm = stringResource(R.string.logout),
            onDismiss = { confirmLogout = false },
        ) { vm.logout { nav.goFresh(Routes.WELCOME) } }
    }
    if (confirmDelete) {
        ConfirmDialog(
            text = stringResource(R.string.delete_account_confirm),
            confirm = stringResource(R.string.delete),
            danger = true,
            onDismiss = { confirmDelete = false },
        ) { vm.deleteAccount { nav.goFresh(Routes.WELCOME) } }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = SColors.GrayText,
        modifier = Modifier.padding(start = 20.dp, top = 22.dp, bottom = 4.dp),
    )
}

@Composable
fun SettingsRow(icon: ImageVector, title: String, subtitle: String? = null, onClick: () -> Unit) {
    Column {
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null, tint = SColors.Dark, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (!subtitle.isNullOrBlank()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SColors.GrayText)
                }
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = SColors.Dark)
        }
        HorizontalDivider(Modifier.padding(horizontal = 20.dp), color = SColors.Divider)
    }
}

@Composable
private fun SwitchRow(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Column {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null, tint = SColors.Dark, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SColors.GrayText)
            }
            Switch(
                checked = checked,
                onCheckedChange = onChange,
                colors = SwitchDefaults.colors(checkedTrackColor = SColors.Primary, checkedThumbColor = SColors.White),
            )
        }
        HorizontalDivider(Modifier.padding(horizontal = 20.dp), color = SColors.Divider)
    }
}

@Composable
private fun LanguageDialog(current: Lang, onDismiss: () -> Unit, onPick: (Lang) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SColors.White,
        title = { Text(stringResource(R.string.language)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Lang.entries.forEach { l ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (l == current) SColors.PrimarySoft else SColors.White)
                            .clickable { onPick(l) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(l.flag, fontSize = 20.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(l.nativeName, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        },
        confirmButton = {},
    )
}

@Composable
fun ConfirmDialog(
    text: String,
    confirm: String,
    onDismiss: () -> Unit,
    danger: Boolean = false,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SColors.White,
        text = { Text(text, style = MaterialTheme.typography.bodyLarge) },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                onConfirm()
            }) { Text(confirm, color = if (danger) SColors.Danger else SColors.Primary) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = SColors.GrayText) }
        },
    )
}
