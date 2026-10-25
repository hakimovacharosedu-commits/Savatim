package uz.savatim.app.ui.screens

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uz.savatim.app.AppViewModel
import uz.savatim.app.R
import uz.savatim.app.i18n.Lang
import uz.savatim.app.ui.Routes
import uz.savatim.app.ui.components.Art
import uz.savatim.app.ui.components.GreenCurveHeader
import uz.savatim.app.ui.components.Logo
import uz.savatim.app.ui.components.PrimaryButton
import uz.savatim.app.ui.components.SecondaryButton
import uz.savatim.app.ui.components.WhiteButton
import uz.savatim.app.ui.goFresh
import uz.savatim.app.ui.theme.SColors

// ------------------------------------------------------------------ Splash (Nectar splash)

@Composable
fun SplashScreen(vm: AppViewModel, nav: NavHostController) {
    LaunchedEffect(Unit) {
        val started = System.currentTimeMillis()
        val dest = vm.resolveStart()
        val wait = 900 - (System.currentTimeMillis() - started)
        if (wait > 0) delay(wait)
        nav.goFresh(dest)
    }
    Box(Modifier.fillMaxSize().background(SColors.Primary), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Logo(size = 40)
        }
    }
}

// ------------------------------------------------------------------ Language picker

@Composable
fun LanguageScreen(vm: AppViewModel, nav: NavHostController) {
    val activity = LocalContext.current as? Activity
    var selected by remember { mutableStateOf(Lang.fromSystem()) }

    Column(
        Modifier
            .fillMaxSize()
            .background(SColors.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))
        Art(110.dp)
        Spacer(Modifier.height(20.dp))
        // Shown in all three languages because we don't know the user's language yet
        Text("Tilni tanlang", style = MaterialTheme.typography.headlineSmall)
        Text("Выберите язык · Choose language", style = MaterialTheme.typography.bodyMedium, color = SColors.GrayText)
        Spacer(Modifier.height(32.dp))
        Lang.entries.forEach { lang ->
            val isSel = lang == selected
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSel) SColors.PrimarySoft else SColors.Field)
                    .border(1.dp, if (isSel) SColors.Primary else SColors.Field, RoundedCornerShape(14.dp))
                    .clickable { selected = lang }
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(lang.flag, fontSize = 24.sp)
                Spacer(Modifier.width(14.dp))
                Text(lang.nativeName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (isSel) Icon(Icons.Rounded.CheckCircle, null, tint = SColors.Primary)
            }
        }
        Spacer(Modifier.weight(1f))
        PrimaryButton(
            text = when (selected) {
                Lang.UZ -> "Davom etish"
                Lang.RU -> "Продолжить"
                Lang.EN -> "Continue"
            },
            onClick = {
                vm.setLanguage(selected.code)
                nav.goFresh(Routes.SPLASH)
                activity?.recreate()
            },
        )
        Spacer(Modifier.height(20.dp))
    }
}

// ------------------------------------------------------------------ Onboarding (Kangsayur X-6)

private data class OnboardingPage(val emoji: String, val title: Int, val text: Int)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(vm: AppViewModel, nav: NavHostController) {
    val pages = listOf(
        OnboardingPage("🏠", R.string.onb1_title, R.string.onb1_text),
        OnboardingPage("🔔", R.string.onb2_title, R.string.onb2_text),
        OnboardingPage("✈️", R.string.onb3_title, R.string.onb3_text),
    )
    val pager = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize().background(SColors.Primary)) {
        // Lighter circle in the top-left, like the Kangsayur splash
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(SColors.PrimaryCircle, radius = size.width * 0.75f, center = Offset(size.width * 0.2f, size.height * 0.12f))
        }
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(36.dp))
            Logo(size = 32)
            HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { i ->
                val page = pages[i]
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Art(190.dp, emoji = page.emoji, onGreen = true)
                    Spacer(Modifier.height(36.dp))
                    Text(
                        stringResource(page.title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = SColors.White,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        stringResource(page.text),
                        style = MaterialTheme.typography.bodyLarge,
                        color = SColors.White.copy(alpha = 0.92f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            val last = pager.currentPage == pages.lastIndex
            WhiteButton(
                text = stringResource(if (last) R.string.get_started else R.string.next),
                onClick = {
                    if (last) {
                        vm.finishOnboarding()
                        nav.goFresh(Routes.SPLASH)
                    } else {
                        scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                    }
                },
            )
            Spacer(Modifier.height(28.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                pages.indices.forEach { i ->
                    Box(
                        Modifier
                            .height(8.dp)
                            .width(if (i == pager.currentPage) 24.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (i == pager.currentPage) SColors.White else SColors.White.copy(alpha = 0.5f)),
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ------------------------------------------------------------------ Welcome (Kangsayur X-5)

@Composable
fun WelcomeScreen(nav: NavHostController) {
    Column(Modifier.fillMaxSize().background(SColors.White).navigationBarsPadding()) {
        GreenCurveHeader(height = 400.dp) {
            Column(
                Modifier.fillMaxSize().statusBarsPadding().padding(top = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Logo(size = 30)
                Spacer(Modifier.height(28.dp))
                Art(190.dp, onGreen = true)
            }
        }
        Spacer(Modifier.height(28.dp))
        Text(
            stringResource(R.string.welcome_text),
            style = MaterialTheme.typography.titleMedium,
            color = SColors.Gray1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        )
        Spacer(Modifier.weight(1f))
        Column(Modifier.padding(horizontal = 20.dp)) {
            PrimaryButton(stringResource(R.string.login), onClick = { nav.navigate(Routes.LOGIN) })
            Spacer(Modifier.height(16.dp))
            SecondaryButton(stringResource(R.string.register), onClick = { nav.navigate(Routes.REGISTER) })
        }
        Spacer(Modifier.height(32.dp))
    }
}

// ------------------------------------------------------------------ Shown if local.properties has no Supabase keys

@Composable
fun NotConfiguredScreen() {
    Column(
        Modifier.fillMaxSize().background(SColors.White).statusBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Art(110.dp)
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.not_configured_title), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            "local.properties:\nSUPABASE_URL=…\nSUPABASE_ANON_KEY=…\nTELEGRAM_BOT_USERNAME=…",
            style = MaterialTheme.typography.bodyMedium,
            color = SColors.GrayText,
            textAlign = TextAlign.Center,
        )
    }
}
