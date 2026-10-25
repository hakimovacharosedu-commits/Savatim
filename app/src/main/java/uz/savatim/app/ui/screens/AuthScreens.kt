package uz.savatim.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import uz.savatim.app.AppViewModel
import uz.savatim.app.R
import uz.savatim.app.i18n.Roles
import uz.savatim.app.ui.Routes
import uz.savatim.app.ui.components.Art
import uz.savatim.app.ui.components.BackButton
import uz.savatim.app.ui.components.GreenCurveHeader
import uz.savatim.app.ui.components.LabeledField
import uz.savatim.app.ui.components.PrimaryButton
import uz.savatim.app.ui.goFresh
import uz.savatim.app.ui.theme.SColors

private fun validEmail(e: String) = android.util.Patterns.EMAIL_ADDRESS.matcher(e.trim()).matches()

/** Kangsayur X-7: green curve with big white title + illustration, form below. */
@Composable
private fun AuthLayout(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(SColors.White)
            .imePadding()
            .verticalScroll(rememberScrollState()),
    ) {
        GreenCurveHeader(height = 300.dp) {
            Box(Modifier.statusBarsPadding().padding(start = 8.dp, top = 8.dp)) {
                BackButton(onBack, tint = SColors.White)
            }
            Text(
                title,
                style = MaterialTheme.typography.displaySmall,
                color = SColors.White,
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 20.dp, bottom = 70.dp),
            )
            Art(110.dp, Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 40.dp), onGreen = true)
        }
        Column(Modifier.padding(horizontal = 20.dp).navigationBarsPadding()) {
            Spacer(Modifier.height(12.dp))
            content()
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SwitchLink(question: String, action: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(question, style = MaterialTheme.typography.bodySmall, color = SColors.Link)
        Spacer(Modifier.width(4.dp))
        Text(action, style = MaterialTheme.typography.bodySmall, color = SColors.Link, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LoginScreen(vm: AppViewModel, nav: NavHostController) {
    val busy by vm.busy.collectAsState()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val ok = validEmail(email) && password.length >= 6
    val submit = { if (ok) vm.login(email, password) { nav.goFresh(it) } }

    AuthLayout(stringResource(R.string.login), onBack = { nav.popBackStack() }) {
        LabeledField(
            stringResource(R.string.email), email, { email = it }, "yourmail@mail.com",
            keyboardType = KeyboardType.Email,
        )
        Spacer(Modifier.height(16.dp))
        LabeledField(
            stringResource(R.string.password), password, { password = it }, stringResource(R.string.password_hint),
            isPassword = true, imeAction = ImeAction.Done, onDone = submit,
        )
        Spacer(Modifier.height(28.dp))
        PrimaryButton(stringResource(R.string.login), onClick = submit, enabled = ok, loading = busy)
        Spacer(Modifier.height(12.dp))
        SwitchLink(stringResource(R.string.no_account), stringResource(R.string.register)) {
            nav.navigate(Routes.REGISTER) { popUpTo(Routes.WELCOME) }
        }
    }
}

@Composable
fun RegisterScreen(vm: AppViewModel, nav: NavHostController) {
    val busy by vm.busy.collectAsState()
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val ok = name.isNotBlank() && validEmail(email) && password.length >= 6
    val submit = { if (ok) vm.register(name, email, password) { nav.goFresh(it) } }

    AuthLayout(stringResource(R.string.register), onBack = { nav.popBackStack() }) {
        LabeledField(
            stringResource(R.string.your_name), name, { name = it }, stringResource(R.string.your_name_hint),
            capitalization = KeyboardCapitalization.Words,
        )
        Spacer(Modifier.height(16.dp))
        LabeledField(
            stringResource(R.string.email), email, { email = it }, "yourmail@mail.com",
            keyboardType = KeyboardType.Email,
        )
        Spacer(Modifier.height(16.dp))
        LabeledField(
            stringResource(R.string.password), password, { password = it }, stringResource(R.string.password_min),
            isPassword = true, imeAction = ImeAction.Done, onDone = submit,
        )
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.terms_note), style = MaterialTheme.typography.bodySmall, color = SColors.GrayText)
        Spacer(Modifier.height(20.dp))
        PrimaryButton(stringResource(R.string.register), onClick = submit, enabled = ok, loading = busy)
        Spacer(Modifier.height(12.dp))
        SwitchLink(stringResource(R.string.have_account), stringResource(R.string.login)) {
            nav.navigate(Routes.LOGIN) { popUpTo(Routes.WELCOME) }
        }
    }
}

/** "Who are you in the family?" – name + role label (+ gender for "other"). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSetupScreen(vm: AppViewModel, nav: NavHostController, editing: Boolean) {
    val state by vm.state.collectAsState()
    val busy by vm.busy.collectAsState()
    val profile = state.profile
    var name by rememberSaveable { mutableStateOf(profile?.name ?: "") }
    var role by rememberSaveable { mutableStateOf(profile?.roleLabel ?: "other") }
    var gender by rememberSaveable { mutableStateOf(profile?.gender ?: "f") }
    val lang = vm.lang

    Column(
        Modifier
            .fillMaxSize()
            .background(SColors.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        if (editing) {
            Box(Modifier.padding(top = 8.dp)) { BackButton({ nav.popBackStack() }) }
        } else {
            Spacer(Modifier.height(40.dp))
        }
        Text(stringResource(R.string.profile_setup_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.profile_setup_text), style = MaterialTheme.typography.bodyMedium, color = SColors.GrayText)
        Spacer(Modifier.height(24.dp))
        LabeledField(
            stringResource(R.string.your_name), name, { name = it }, stringResource(R.string.your_name_hint),
            capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done,
        )
        Spacer(Modifier.height(20.dp))
        Text(stringResource(R.string.who_are_you), style = MaterialTheme.typography.labelMedium, color = SColors.Gray1)
        Spacer(Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Roles.keys.forEach { key ->
                val sel = key == role
                Row(
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (sel) SColors.PrimarySoft else SColors.Field)
                        .border(1.dp, if (sel) SColors.Primary else SColors.Field, RoundedCornerShape(20.dp))
                        .clickable {
                            role = key
                            Roles.impliedGender(key)?.let { gender = it }
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(Roles.emoji(key), fontSize = 18.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        Roles.label(key, lang),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (sel) SColors.Primary else SColors.Dark,
                    )
                }
            }
        }
        if (role == "other") {
            Spacer(Modifier.height(20.dp))
            Text(stringResource(R.string.gender), style = MaterialTheme.typography.labelMedium, color = SColors.Gray1)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("f" to R.string.female, "m" to R.string.male).forEach { (g, label) ->
                    val sel = g == gender
                    Text(
                        stringResource(label),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (sel) SColors.Primary else SColors.Dark,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (sel) SColors.PrimarySoft else SColors.Field)
                            .border(1.dp, if (sel) SColors.Primary else SColors.Field, RoundedCornerShape(20.dp))
                            .clickable { gender = g }
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.gender_why), style = MaterialTheme.typography.bodySmall, color = SColors.GrayText)
        }
        Spacer(Modifier.height(32.dp))
        PrimaryButton(
            stringResource(if (editing) R.string.save else R.string.continue_),
            enabled = name.isNotBlank(),
            loading = busy,
            onClick = {
                vm.saveProfile(name, role, gender) { dest ->
                    if (editing) nav.popBackStack() else nav.goFresh(dest)
                }
            },
        )
        Spacer(Modifier.height(24.dp))
    }
}
