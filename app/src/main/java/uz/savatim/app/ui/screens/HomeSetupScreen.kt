package uz.savatim.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import uz.savatim.app.AppViewModel
import uz.savatim.app.R
import uz.savatim.app.ui.Routes
import uz.savatim.app.ui.components.Art
import uz.savatim.app.ui.components.BackButton
import uz.savatim.app.ui.components.LabeledField
import uz.savatim.app.ui.components.PrimaryButton
import uz.savatim.app.ui.goFresh
import uz.savatim.app.ui.theme.SColors

/** Create a new home or join one with an invite code (replaces Nectar's "Select your location"). */
@Composable
fun HomeSetupScreen(vm: AppViewModel, nav: NavHostController, canGoBack: Boolean) {
    val busy by vm.busy.collectAsState()
    var joinMode by rememberSaveable { mutableStateOf(false) }
    var homeName by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    val done = { nav.goFresh(Routes.MAIN) }

    Column(
        Modifier
            .fillMaxSize()
            .background(SColors.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxWidth().height(48.dp)) {
            if (canGoBack) BackButton({ nav.popBackStack() })
        }
        Art(150.dp, emoji = "🏡")
        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.home_setup_title), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.home_setup_text),
            style = MaterialTheme.typography.bodyMedium,
            color = SColors.GrayText,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))

        // Segmented switch: Create / Join
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SColors.Field).padding(4.dp),
        ) {
            listOf(false to R.string.create_home, true to R.string.join_home).forEach { (mode, label) ->
                val sel = joinMode == mode
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (sel) SColors.White else SColors.Field)
                        .clickable { joinMode = mode }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        stringResource(label),
                        style = MaterialTheme.typography.titleSmall,
                        color = if (sel) SColors.Primary else SColors.GrayText,
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        if (!joinMode) {
            LabeledField(
                stringResource(R.string.home_name), homeName, { homeName = it }, stringResource(R.string.home_name_hint),
                capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done,
            )
            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                stringResource(R.string.create_home),
                enabled = homeName.isNotBlank(),
                loading = busy,
                onClick = { vm.createHome(homeName, done) },
            )
        } else {
            LabeledField(
                stringResource(R.string.invite_code), code, { code = it.uppercase().take(6) }, "ABC234",
                capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done,
            )
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.invite_code_where), style = MaterialTheme.typography.bodySmall, color = SColors.GrayText)
            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                stringResource(R.string.join_home),
                enabled = code.trim().length == 6,
                loading = busy,
                onClick = { vm.joinHome(code, done) },
            )
        }
        Spacer(Modifier.height(32.dp))
        if (!canGoBack) {
            TextButton(onClick = { vm.logout { nav.goFresh(Routes.WELCOME) } }) {
                Text(stringResource(R.string.logout), color = SColors.GrayText)
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}
