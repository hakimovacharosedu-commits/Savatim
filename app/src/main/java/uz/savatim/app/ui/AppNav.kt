package uz.savatim.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uz.savatim.app.AppViewModel
import uz.savatim.app.StartDestination
import uz.savatim.app.ui.screens.HomeSetupScreen
import uz.savatim.app.ui.screens.LanguageScreen
import uz.savatim.app.ui.screens.LoginScreen
import uz.savatim.app.ui.screens.MainScreen
import uz.savatim.app.ui.screens.MembersScreen
import uz.savatim.app.ui.screens.NotConfiguredScreen
import uz.savatim.app.ui.screens.OnboardingScreen
import uz.savatim.app.ui.screens.ProfileSetupScreen
import uz.savatim.app.ui.screens.RegisterScreen
import uz.savatim.app.ui.screens.SplashScreen
import uz.savatim.app.ui.screens.WelcomeScreen
import uz.savatim.app.ui.theme.SColors

object Routes {
    const val SPLASH = "splash"
    const val LANGUAGE = "language"
    const val ONBOARDING = "onboarding"
    const val NOT_CONFIGURED = "not_configured"
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val PROFILE_SETUP = "profile_setup"
    const val PROFILE_EDIT = "profile_edit"
    const val HOME_SETUP = "home_setup"
    const val HOME_ADD = "home_add"
    const val MAIN = "main"
    const val MEMBERS = "members"

    fun of(dest: StartDestination) = when (dest) {
        StartDestination.NOT_CONFIGURED -> NOT_CONFIGURED
        StartDestination.LANGUAGE -> LANGUAGE
        StartDestination.ONBOARDING -> ONBOARDING
        StartDestination.WELCOME -> WELCOME
        StartDestination.PROFILE_SETUP -> PROFILE_SETUP
        StartDestination.HOME_SETUP -> HOME_SETUP
        StartDestination.MAIN -> MAIN
    }
}

/** Navigate and forget everything behind (no going "back" to login etc.). */
fun NavHostController.goFresh(route: String) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

fun NavHostController.goFresh(dest: StartDestination) = goFresh(Routes.of(dest))

@Composable
fun AppNav() {
    val vm: AppViewModel = viewModel()
    val nav = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm.messages.collect { msg ->
            val text = msg.text ?: msg.res?.let { context.getString(it) } ?: return@collect
            snackbar.showSnackbar(text)
        }
    }

    Box(Modifier.fillMaxSize()) {
        NavHost(navController = nav, startDestination = Routes.SPLASH) {
            composable(Routes.SPLASH) { SplashScreen(vm, nav) }
            composable(Routes.LANGUAGE) { LanguageScreen(vm, nav) }
            composable(Routes.ONBOARDING) { OnboardingScreen(vm, nav) }
            composable(Routes.NOT_CONFIGURED) { NotConfiguredScreen() }
            composable(Routes.WELCOME) { WelcomeScreen(nav) }
            composable(Routes.LOGIN) { LoginScreen(vm, nav) }
            composable(Routes.REGISTER) { RegisterScreen(vm, nav) }
            composable(Routes.PROFILE_SETUP) { ProfileSetupScreen(vm, nav, editing = false) }
            composable(Routes.PROFILE_EDIT) { ProfileSetupScreen(vm, nav, editing = true) }
            composable(Routes.HOME_SETUP) { HomeSetupScreen(vm, nav, canGoBack = false) }
            composable(Routes.HOME_ADD) { HomeSetupScreen(vm, nav, canGoBack = true) }
            composable(Routes.MAIN) { MainScreen(vm, nav) }
            composable(Routes.MEMBERS) { MembersScreen(vm, nav) }
        }
        SnackbarHost(
            snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 80.dp),
        ) { data ->
            Snackbar(
                modifier = Modifier.padding(horizontal = 16.dp),
                containerColor = SColors.Dark,
                contentColor = SColors.White,
            ) { Text(data.visuals.message) }
        }
    }
}
