package uz.savatim.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.savatim.app.AppViewModel
import uz.savatim.app.R
import uz.savatim.app.UiState
import uz.savatim.app.data.Catalog
import uz.savatim.app.data.Product
import uz.savatim.app.ui.components.CategoryChip
import uz.savatim.app.ui.components.SearchField
import uz.savatim.app.ui.theme.SColors

/**
 * Kangsayur "Explore" grid. Tap products to select several, then add them in one go
 * → the family gets ONE notification ("Ona roʻyxatga 3 ta mahsulot qoʻshdi…").
 */
@Composable
fun AddTab(vm: AppViewModel, state: UiState, onAdded: () -> Unit) {
    val lang = vm.lang
    val busy by vm.busy.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    val selected = remember { mutableStateListOf<Product>() }
    val customs = remember { mutableStateListOf<String>() }

    val inList = state.items.filter { !it.bought }.mapNotNull { it.catalogKey }.toSet()
    val results = Catalog.search(query).filter { query.isNotBlank() || category == null || it.category == category }
    val typed = query.trim()
    val showCustom = typed.length >= 2 &&
        results.none { Catalog.normalize(it.name(lang)) == Catalog.normalize(typed) } &&
        customs.none { it.equals(typed, ignoreCase = true) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Text(
                stringResource(R.string.add_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 20.dp, top = 18.dp, bottom = 12.dp),
            )
            SearchField(query, { query = it }, stringResource(R.string.search_hint), Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(8.dp))
            if (query.isBlank()) {
                LazyRow(contentPadding = PaddingValues(horizontal = 14.dp)) {
                    item { CategoryChip("🧺", stringResource(R.string.all), category == null) { category = null } }
                    items(Catalog.categories) { c ->
                        CategoryChip(c.emoji, c.name(lang), category == c.key) { category = if (category == c.key) null else c.key }
                    }
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f),
            ) {
                if (showCustom) {
                    item(key = "custom") {
                        ProductCard(
                            emoji = "✏️",
                            title = typed,
                            subtitle = stringResource(R.string.add_custom),
                            selected = false,
                            already = false,
                        ) {
                            customs.add(typed)
                            query = ""
                        }
                    }
                }
                items(customs.toList(), key = { "c_$it" }) { name ->
                    ProductCard("📝", name, stringResource(R.string.custom_item), selected = true, already = false) {
                        customs.remove(name)
                    }
                }
                items(results, key = { it.key }) { p ->
                    val isSel = selected.any { it.key == p.key }
                    ProductCard(
                        emoji = p.emoji,
                        title = p.name(lang),
                        subtitle = Catalog.category(p.category).name(lang),
                        selected = isSel,
                        already = p.key in inList,
                    ) {
                        if (isSel) selected.removeAll { it.key == p.key } else selected.add(p)
                    }
                }
            }
        }

        // Nectar "Go to Checkout" style button with a count badge
        val count = selected.size + customs.size
        if (count > 0) {
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .fillMaxWidth()
                    .height(60.dp)
                    .shadow(6.dp, RoundedCornerShape(19.dp))
                    .clip(RoundedCornerShape(19.dp))
                    .background(SColors.Primary)
                    .clickable(enabled = !busy) {
                        vm.addProducts(selected.toList(), customs.toList()) {
                            selected.clear()
                            customs.clear()
                            onAdded()
                        }
                    }
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.add_to_list),
                    style = MaterialTheme.typography.labelLarge,
                    color = SColors.White,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                Text(
                    "$count",
                    style = MaterialTheme.typography.labelMedium,
                    color = SColors.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SColors.PrimaryDark)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}

/** Kangsayur product card: picture area on top, name, small "+" button bottom-right. */
@Composable
private fun ProductCard(
    emoji: String,
    title: String,
    subtitle: String,
    selected: Boolean,
    already: Boolean,
    onClick: () -> Unit,
) {
    Column(
        Modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, if (selected) SColors.Primary else SColors.Border, RoundedCornerShape(14.dp))
            .background(SColors.White)
            .clickable(onClick = onClick),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(96.dp)
                .background(if (selected) SColors.PrimarySoft else SColors.Field),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, fontSize = 46.sp)
        }
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (already) stringResource(R.string.already_in_list) else subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (already) SColors.Primary else SColors.GrayText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(6.dp))
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) SColors.Primary else SColors.White)
                        .border(1.dp, SColors.Primary, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (selected) Icons.Rounded.Check else Icons.Rounded.Add,
                        null,
                        tint = if (selected) SColors.White else SColors.Primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
