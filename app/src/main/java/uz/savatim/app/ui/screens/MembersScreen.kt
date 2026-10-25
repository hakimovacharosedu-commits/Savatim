package uz.savatim.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import uz.savatim.app.AppViewModel
import uz.savatim.app.R
import uz.savatim.app.data.HomeRole
import uz.savatim.app.data.Member
import uz.savatim.app.i18n.Roles
import uz.savatim.app.ui.Routes
import uz.savatim.app.ui.components.Avatar
import uz.savatim.app.ui.components.BackButton
import uz.savatim.app.ui.components.Badge
import uz.savatim.app.ui.components.InputBox
import uz.savatim.app.ui.goFresh
import uz.savatim.app.ui.theme.Inter
import uz.savatim.app.ui.theme.SColors

@Composable
fun MembersScreen(vm: AppViewModel, nav: NavHostController) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val home = state.home
    val lang = vm.lang
    var renaming by remember { mutableStateOf(false) }
    var confirmLeave by remember { mutableStateOf(false) }
    var confirmDeleteHome by remember { mutableStateOf(false) }
    var memberToRemove by remember { mutableStateOf<Member?>(null) }
    var memberToOwner by remember { mutableStateOf<Member?>(null) }
    val isOwner = state.myRole == HomeRole.OWNER

    Column(
        Modifier
            .fillMaxSize()
            .background(SColors.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            BackButton({ nav.popBackStack() })
            Text(
                home?.name ?: "",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            if (state.canManage) {
                IconButton(onClick = { renaming = true }) { Icon(Icons.Rounded.Edit, null, tint = SColors.Primary) }
            }
        }

        // ---- Invite card
        if (home != null) {
            Column(
                Modifier
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(SColors.Primary, SColors.PrimaryCircle)))
                    .padding(20.dp),
            ) {
                Text(stringResource(R.string.invite_title), style = MaterialTheme.typography.titleSmall, color = SColors.White)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        home.inviteCode,
                        color = SColors.White,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 34.sp,
                        letterSpacing = 6.sp,
                        modifier = Modifier.weight(1f),
                    )
                    if (state.canManage) {
                        IconButton(onClick = { vm.regenerateCode() }) {
                            Icon(Icons.Rounded.Refresh, stringResource(R.string.new_code), tint = SColors.White)
                        }
                    }
                }
                Text(stringResource(R.string.invite_text), style = MaterialTheme.typography.bodySmall, color = SColors.White.copy(alpha = 0.9f))
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val shareText = stringResource(R.string.invite_share_text, home.name, home.inviteCode)
                    InviteButton(Icons.Rounded.ContentCopy, stringResource(R.string.copy), Modifier.weight(1f)) {
                        copy(context, home.inviteCode)
                    }
                    InviteButton(Icons.Rounded.Share, stringResource(R.string.share), Modifier.weight(1f)) {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, null))
                    }
                }
            }
        }

        // ---- Members
        Text(
            stringResource(R.string.members_count, state.members.size),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 4.dp),
        )
        state.members.forEach { m ->
            val me = m.profile.id == state.myId
            var menu by remember(m.profile.id) { mutableStateOf(false) }
            Row(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Avatar(Roles.emoji(m.profile.roleLabel))
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        m.profile.name + if (me) " " + stringResource(R.string.you) else "",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (m.profile.roleLabel != "other") {
                            Text(Roles.label(m.profile.roleLabel, lang), style = MaterialTheme.typography.bodySmall, color = SColors.GrayText)
                        }
                        when (m.role) {
                            HomeRole.OWNER -> Badge(stringResource(R.string.role_owner))
                            HomeRole.ADMIN -> Badge(stringResource(R.string.role_admin), SColors.Link)
                        }
                        if (m.profile.telegramChatId != null) Text("✈️", fontSize = 12.sp)
                    }
                }
                val canEdit = !me && m.role != HomeRole.OWNER &&
                    (isOwner || (state.myRole == HomeRole.ADMIN && m.role == HomeRole.MEMBER))
                if (canEdit) {
                    Box {
                        IconButton(onClick = { menu = true }) { Icon(Icons.Rounded.MoreVert, null, tint = SColors.GrayText) }
                        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                            if (isOwner) {
                                if (m.role == HomeRole.MEMBER) {
                                    DropdownMenuItem(text = { Text(stringResource(R.string.make_admin)) }, onClick = {
                                        menu = false
                                        vm.setMemberRole(m.profile.id, HomeRole.ADMIN)
                                    })
                                } else {
                                    DropdownMenuItem(text = { Text(stringResource(R.string.remove_admin)) }, onClick = {
                                        menu = false
                                        vm.setMemberRole(m.profile.id, HomeRole.MEMBER)
                                    })
                                }
                                DropdownMenuItem(text = { Text(stringResource(R.string.make_owner)) }, onClick = {
                                    menu = false
                                    memberToOwner = m
                                })
                            }
                            DropdownMenuItem(text = { Text(stringResource(R.string.remove_member), color = SColors.Danger) }, onClick = {
                                menu = false
                                memberToRemove = m
                            })
                        }
                    }
                }
            }
            HorizontalDivider(Modifier.padding(start = 78.dp, end = 20.dp), color = SColors.Divider)
        }

        Spacer(Modifier.height(28.dp))
        TextButton(onClick = { confirmLeave = true }, modifier = Modifier.padding(horizontal = 12.dp)) {
            Text(stringResource(R.string.leave_home), color = SColors.Danger)
        }
        if (isOwner) {
            TextButton(onClick = { confirmDeleteHome = true }, modifier = Modifier.padding(horizontal = 12.dp)) {
                Text(stringResource(R.string.delete_home), color = SColors.Danger)
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    val afterLeave: (uz.savatim.app.StartDestination) -> Unit = { dest -> nav.goFresh(dest) }
    if (confirmLeave) {
        ConfirmDialog(
            text = stringResource(if (isOwner) R.string.leave_home_owner_confirm else R.string.leave_home_confirm),
            confirm = stringResource(R.string.leave_home),
            danger = true,
            onDismiss = { confirmLeave = false },
        ) { vm.leaveOrDeleteHome(delete = false, onResult = afterLeave) }
    }
    if (confirmDeleteHome) {
        ConfirmDialog(
            text = stringResource(R.string.delete_home_confirm),
            confirm = stringResource(R.string.delete),
            danger = true,
            onDismiss = { confirmDeleteHome = false },
        ) { vm.leaveOrDeleteHome(delete = true, onResult = afterLeave) }
    }
    memberToRemove?.let { m ->
        ConfirmDialog(
            text = stringResource(R.string.remove_member_confirm, m.profile.name),
            confirm = stringResource(R.string.remove_member),
            danger = true,
            onDismiss = { memberToRemove = null },
        ) { vm.removeMember(m.profile.id) }
    }
    memberToOwner?.let { m ->
        ConfirmDialog(
            text = stringResource(R.string.make_owner_confirm, m.profile.name),
            confirm = stringResource(R.string.make_owner),
            onDismiss = { memberToOwner = null },
        ) { vm.transferOwnership(m.profile.id) }
    }
    if (renaming && home != null) {
        var name by remember { mutableStateOf(home.name) }
        AlertDialog(
            onDismissRequest = { renaming = false },
            containerColor = SColors.White,
            title = { Text(stringResource(R.string.rename_home)) },
            text = { InputBox(name, { name = it }, stringResource(R.string.home_name_hint)) },
            confirmButton = {
                TextButton(
                    enabled = name.isNotBlank(),
                    onClick = {
                        renaming = false
                        vm.renameHome(name)
                    },
                ) { Text(stringResource(R.string.save), color = SColors.Primary) }
            },
            dismissButton = {
                TextButton(onClick = { renaming = false }) { Text(stringResource(R.string.cancel), color = SColors.GrayText) }
            },
        )
    }
}

@Composable
private fun InviteButton(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SColors.White, contentColor = SColors.Primary),
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.titleSmall, color = SColors.Primary)
    }
}

private fun copy(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("Savatim", text))
}
