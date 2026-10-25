package uz.savatim.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import uz.savatim.app.AppViewModel
import uz.savatim.app.R
import uz.savatim.app.UiState
import uz.savatim.app.data.ActivityType
import uz.savatim.app.i18n.ActivityText
import uz.savatim.app.i18n.Roles
import uz.savatim.app.ui.components.Avatar
import uz.savatim.app.ui.components.EmptyState
import uz.savatim.app.ui.theme.SColors
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ActivityTab(vm: AppViewModel, state: UiState) {
    val lang = vm.lang
    val profiles = state.members.map { it.profile }
    val today = stringResource(R.string.today)
    val yesterday = stringResource(R.string.yesterday)

    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Text(
                stringResource(R.string.tab_activity),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 20.dp, top = 18.dp, bottom = 8.dp),
            )
        }
        if (state.activity.isEmpty()) {
            item { EmptyState(stringResource(R.string.activity_empty_title), stringResource(R.string.activity_empty_text)) }
        }
        items(state.activity, key = { it.id }) { a ->
            val actor = state.member(a.actorId)
            val icon = when (a.type) {
                ActivityType.BOUGHT -> "✅"
                ActivityType.ALL_BOUGHT -> "🎉"
                ActivityType.REMOVED -> "🗑️"
                ActivityType.JOINED -> "👋"
                ActivityType.LEFT -> "🚪"
                else -> actor?.let { Roles.emoji(it.roleLabel) } ?: "🙂"
            }
            Column {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(icon)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            ActivityText.build(a.type, actor, profiles, a.items, lang),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(formatTime(a.createdAt, today, yesterday), style = MaterialTheme.typography.bodySmall, color = SColors.GrayText)
                    }
                }
                HorizontalDivider(Modifier.padding(start = 78.dp, end = 20.dp), color = SColors.Divider)
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
private val dateFormat = DateTimeFormatter.ofPattern("dd.MM HH:mm")

fun formatTime(iso: String, today: String, yesterday: String): String = try {
    val t = OffsetDateTime.parse(iso).atZoneSameInstant(ZoneId.systemDefault())
    val d = t.toLocalDate()
    val now = LocalDate.now()
    when (d) {
        now -> "$today, ${t.format(timeFormat)}"
        now.minusDays(1) -> "$yesterday, ${t.format(timeFormat)}"
        else -> t.format(dateFormat)
    }
} catch (e: Exception) {
    ""
}
