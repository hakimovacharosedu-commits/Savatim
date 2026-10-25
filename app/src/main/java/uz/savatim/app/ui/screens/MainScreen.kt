package uz.savatim.app.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import uz.savatim.app.AppViewModel
import uz.savatim.app.R
import uz.savatim.app.data.Prefs
import uz.savatim.app.ui.Routes
import uz.savatim.app.ui.components.EmojiTile
import uz.savatim.app.ui.theme.SColors

enum class Tab(val icon: ImageVector, val label: Int) {
    LIST(Icons.Rounded.ShoppingBasket, R.string.tab_list),
    ADD(Icons.Rounded.Storefront, R.string.tab_add),
    ACTIVITY(Icons.Rounded.Notifications, R.string.tab_activity),
    PROFILE(Icons.Rounded.Person, R.string.tab_profile),
}

@Composable
fun MainScreen(vm: AppViewModel, nav: NavHostController) {
    val state by vm.state.collectAsState()
    var tab by rememberSaveable { mutableStateOf(Tab.LIST) }
    var showSwitcher by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Check for family changes only while the app is on screen
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_START -> vm.setForeground(true)
                androidx.lifecycle.Lifecycle.Event.ON_STOP -> vm.setForeground(false)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Ask for notification permission once (Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        val prefs = Prefs(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !prefs.askedNotificationPermission) {
            prefs.askedNotificationPermission = true
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        containerColor = SColors.White,
        bottomBar = { BottomBar(tab) { tab = it } },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                Tab.LIST -> ListTab(
                    vm = vm,
                    state = state,
                    onOpenSwitcher = { showSwitcher = true },
                    onOpenMembers = { nav.navigate(Routes.MEMBERS) },
                    onGoAdd = { tab = Tab.ADD },
                )
                Tab.ADD -> AddTab(vm = vm, state = state, onAdded = { tab = Tab.LIST })
                Tab.ACTIVITY -> ActivityTab(vm = vm, state = state)
                Tab.PROFILE -> ProfileTab(vm = vm, state = state, nav = nav, onOpenSwitcher = { showSwitcher = true })
            }
        }
    }

    if (showSwitcher) {
        HomeSwitcherSheet(
            vm = vm,
            onDismiss = { showSwitcher = false },
            onAddHome = {
                showSwitcher = false
                nav.navigate(Routes.HOME_ADD)
            },
        )
    }
}

/** Nectar bottom bar: white, rounded top corners, soft shadow, green selected item. */
@Composable
private fun BottomBar(selected: Tab, onSelect: (Tab) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp), ambientColor = Color(0x22555E58), spotColor = Color(0x22555E58))
            .clip(RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp))
            .background(SColors.White)
            .navigationBarsPadding()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        Tab.entries.forEach { t ->
            val sel = t == selected
            val color = if (sel) SColors.Primary else SColors.Dark
            Column(
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onSelect(t) }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(t.icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                Spacer(Modifier.height(4.dp))
                Text(stringResource(t.label), style = MaterialTheme.typography.labelSmall, color = color)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSwitcherSheet(vm: AppViewModel, onDismiss: () -> Unit, onAddHome: () -> Unit) {
    val state by vm.state.collectAsState()
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = SColors.White) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Text(stringResource(R.string.my_homes), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            state.homes.forEach { home ->
                val current = home.id == state.home?.id
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (!current) vm.selectHome(home)
                            onDismiss()
                        }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    EmojiTile("🏠", 44.dp)
                    Spacer(Modifier.width(14.dp))
                    Text(home.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    if (current) Icon(Icons.Rounded.Check, null, tint = SColors.Primary)
                }
                HorizontalDivider(color = SColors.Divider)
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onAddHome)
                    .padding(vertical = 14.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Add, null, tint = SColors.Primary, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(14.dp))
                Text(stringResource(R.string.add_or_join_home), style = MaterialTheme.typography.titleMedium, color = SColors.Primary)
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 4.dp, start = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.GroupAdd, null, tint = SColors.GrayText, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.homes_hint), style = MaterialTheme.typography.bodySmall, color = SColors.GrayText)
            }
        }
    }
}
