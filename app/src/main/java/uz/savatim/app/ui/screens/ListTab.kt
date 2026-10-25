package uz.savatim.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import uz.savatim.app.AppViewModel
import uz.savatim.app.R
import uz.savatim.app.UiState
import uz.savatim.app.data.Catalog
import uz.savatim.app.data.Item
import uz.savatim.app.i18n.ActivityText
import uz.savatim.app.i18n.Lang
import uz.savatim.app.ui.components.Art
import uz.savatim.app.ui.components.CategoryChip
import uz.savatim.app.ui.components.EmojiTile
import uz.savatim.app.ui.components.EmptyState
import uz.savatim.app.ui.components.InputBox
import uz.savatim.app.ui.components.PrimaryButton
import uz.savatim.app.ui.theme.SColors

@Composable
fun ListTab(
    vm: AppViewModel,
    state: UiState,
    onOpenSwitcher: () -> Unit,
    onOpenMembers: () -> Unit,
    onGoAdd: () -> Unit,
) {
    val lang = vm.lang
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    var openedItemId by rememberSaveable { mutableStateOf<String?>(null) }

    val toBuy = state.items.filter { !it.bought && (category == null || it.category == category) }
    val bought = state.items.filter { it.bought }
    val presentCategories = state.items.filter { !it.bought }.map { it.category }.distinct()
        .sortedBy { key -> Catalog.categories.indexOfFirst { it.key == key } }

    LazyColumn(Modifier.fillMaxSize()) {
        // ---- Header: home name (switcher) + members + refresh
        item {
            Row(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).clickable(onClick = onOpenSwitcher).padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("🏠", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        state.home?.name ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Icon(Icons.Rounded.KeyboardArrowDown, null, tint = SColors.Dark)
                }
                IconButton(onClick = { vm.refresh() }) { Icon(Icons.Rounded.Refresh, null, tint = SColors.GrayText) }
                IconButton(onClick = onOpenMembers) { Icon(Icons.Rounded.GroupAdd, null, tint = SColors.Primary) }
            }
        }

        // ---- Summary banner (Kangsayur discount banner style)
        item {
            SummaryBanner(toBuyCount = state.items.count { !it.bought }, boughtCount = bought.size, members = state.members.size)
        }

        if (state.items.none { !it.bought }) {
            item {
                EmptyState(
                    title = stringResource(R.string.list_empty_title),
                    text = stringResource(R.string.list_empty_text),
                ) {
                    PrimaryButton(stringResource(R.string.add_products), onClick = onGoAdd, modifier = Modifier.width(240.dp))
                }
            }
        } else {
            // ---- Category filter (only categories that are in the list)
            if (presentCategories.size > 1) {
                item {
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 4.dp),
                    ) {
                        CategoryChip("🧺", stringResource(R.string.all), category == null) { category = null }
                        presentCategories.forEach { key ->
                            val c = Catalog.category(key)
                            CategoryChip(c.emoji, c.name(lang), category == key) { category = if (category == key) null else key }
                        }
                    }
                }
            }
            item { SectionHeader(stringResource(R.string.to_buy_count, toBuy.size)) }
            items(toBuy, key = { it.id }) { item ->
                ItemRow(item, state, lang, onToggle = { vm.toggleBought(item) }, onOpen = { openedItemId = item.id })
            }
        }

        if (bought.isNotEmpty()) {
            item {
                Row(
                    Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.bought_count, bought.size),
                        style = MaterialTheme.typography.titleMedium,
                        color = SColors.GrayText,
                        modifier = Modifier.weight(1f),
                    )
                    if (state.canManage) {
                        TextButton(onClick = { vm.clearBought() }) {
                            Text(stringResource(R.string.clear), color = SColors.Primary)
                        }
                    }
                }
            }
            items(bought, key = { it.id }) { item ->
                ItemRow(item, state, lang, onToggle = { vm.toggleBought(item) }, onOpen = { openedItemId = item.id })
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }

    val opened = state.items.firstOrNull { it.id == openedItemId }
    if (opened != null) {
        ItemSheet(vm = vm, item = opened, state = state, lang = lang, onDismiss = { openedItemId = null })
    }
}

@Composable
private fun SummaryBanner(toBuyCount: Int, boughtCount: Int, members: Int) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(SColors.Primary, SColors.PrimaryCircle)))
            .padding(20.dp),
    ) {
        Column(Modifier.fillMaxWidth(0.62f)) {
            Text(stringResource(R.string.banner_title), style = MaterialTheme.typography.titleSmall, color = SColors.White)
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.banner_count, toBuyCount),
                style = MaterialTheme.typography.headlineMedium,
                color = SColors.White,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.banner_sub, boughtCount, members),
                style = MaterialTheme.typography.bodySmall,
                color = SColors.White.copy(alpha = 0.9f),
            )
        }
        Art(84.dp, Modifier.align(Alignment.CenterEnd), onGreen = true)
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp),
    )
}

/** Nectar "My Cart" row: picture tile, bold name, gray details, round check on the right. */
@Composable
private fun ItemRow(item: Item, state: UiState, lang: Lang, onToggle: () -> Unit, onOpen: () -> Unit) {
    val adder = state.member(item.addedBy)
    val who = ActivityText.actorName(adder, state.members.map { it.profile }, lang)
    val details = buildList {
        if (item.quantity.isNotBlank()) add(item.quantity)
        if (item.bought) {
            val buyer = state.member(item.boughtBy)
            add("✓ " + ActivityText.actorName(buyer, state.members.map { it.profile }, lang))
        } else {
            add(who)
        }
    }.joinToString(" · ")

    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .alpha(if (item.bought) 0.55f else 1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EmojiTile(Catalog.emojiFor(item), 56.dp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    Catalog.displayName(item, lang),
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (item.bought) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(details, style = MaterialTheme.typography.bodyMedium, color = SColors.GrayText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (item.note.isNotBlank()) {
                    Text("💬 " + item.note, style = MaterialTheme.typography.bodySmall, color = SColors.GrayText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.width(12.dp))
            CheckCircle(checked = item.bought, onClick = onToggle)
        }
        HorizontalDivider(Modifier.padding(horizontal = 20.dp), color = SColors.Divider)
    }
}

@Composable
private fun CheckCircle(checked: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(if (checked) SColors.Primary else SColors.White)
            .border(1.5.dp, if (checked) SColors.Primary else SColors.Border, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) Icon(Icons.Rounded.Check, null, tint = SColors.White, modifier = Modifier.size(20.dp))
    }
}

/** Details of one item: quantity, note, who added, delete. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ItemSheet(vm: AppViewModel, item: Item, state: UiState, lang: Lang, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember(item.id) { mutableStateOf(item.name) }
    var quantity by remember(item.id) { mutableStateOf(item.quantity) }
    var note by remember(item.id) { mutableStateOf(item.note) }
    val isCustom = item.catalogKey == null
    val profiles = state.members.map { it.profile }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = SColors.White) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp).imePadding()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiTile(Catalog.emojiFor(item), 64.dp)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        Catalog.category(item.category).name(lang),
                        style = MaterialTheme.typography.labelSmall,
                        color = SColors.Primary,
                        modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(SColors.PrimarySoft).padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(Catalog.displayName(item, lang), style = MaterialTheme.typography.headlineSmall)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.added_by, ActivityText.actorName(state.member(item.addedBy), profiles, lang)),
                style = MaterialTheme.typography.bodySmall,
                color = SColors.GrayText,
            )
            Spacer(Modifier.height(20.dp))

            if (isCustom) {
                Text(stringResource(R.string.item_name), style = MaterialTheme.typography.labelMedium, color = SColors.Gray1)
                Spacer(Modifier.height(8.dp))
                InputBox(name, { name = it }, stringResource(R.string.item_name))
                Spacer(Modifier.height(16.dp))
            }

            Text(stringResource(R.string.quantity), style = MaterialTheme.typography.labelMedium, color = SColors.Gray1)
            Spacer(Modifier.height(8.dp))
            InputBox(quantity, { quantity = it }, stringResource(R.string.quantity_hint))
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("1", "2", "3", "1 kg", "2 kg", "0.5 kg", "1 L").forEach { q ->
                    val sel = quantity == q
                    Text(
                        q,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (sel) SColors.White else SColors.Dark,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (sel) SColors.Primary else SColors.Field)
                            .clickable { quantity = q }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.note), style = MaterialTheme.typography.labelMedium, color = SColors.Gray1)
            Spacer(Modifier.height(8.dp))
            InputBox(note, { note = it }, stringResource(R.string.note_hint), singleLine = false)
            Spacer(Modifier.height(24.dp))

            PrimaryButton(stringResource(R.string.save), onClick = {
                vm.updateItem(item, name, quantity, note)
                onDismiss()
            })
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = {
                    vm.toggleBought(item)
                    onDismiss()
                }) {
                    Text(
                        stringResource(if (item.bought) R.string.mark_not_bought else R.string.mark_bought),
                        color = SColors.Primary,
                    )
                }
                if (vm.canDelete(item)) {
                    TextButton(onClick = {
                        vm.deleteItem(item)
                        onDismiss()
                    }) { Text(stringResource(R.string.delete), color = SColors.Danger) }
                }
            }
        }
    }
}
