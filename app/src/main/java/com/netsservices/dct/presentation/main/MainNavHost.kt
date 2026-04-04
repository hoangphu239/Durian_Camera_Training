package com.netsservices.dct.presentation.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.netsservices.dct.R
import com.netsservices.dct.presentation.change_pwd.ChangePwdScreen
import com.netsservices.dct.presentation.config.ConfigScreen
import com.netsservices.dct.presentation.config.ConfigViewModel
import com.netsservices.dct.presentation.home.HomeScreen
import com.netsservices.dct.presentation.home.HomeViewModel
import com.netsservices.dct.presentation.login.LoginScreen
import com.netsservices.dct.presentation.register.RegisterScreen
import com.netsservices.dct.presentation.variety.DurianVarietyScreen


@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun MainNavHost(
    navController: NavHostController,
    activity: MainActivity,
    mainViewModel: MainViewModel,
    startDestination: String,
    onTopBarTitleChange: (String) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        navigation(
            route = Routes.AUTH_GRAPH,
            startDestination = Screen.Login.route
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.MAIN_GRAPH) {
                            popUpTo(Routes.AUTH_GRAPH) {
                                inclusive = true
                            }
                        }
                    },
                    onNavigateRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onNavigateLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        navigation(
            route = Routes.MAIN_GRAPH,
            startDestination = Screen.Home.route
        ) {
            composable(Screen.Home.route) { backStackEntry ->
                val title = stringResource(R.string.app_name)
                LaunchedEffect(backStackEntry) {
                    onTopBarTitleChange(title)
                }
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.MAIN_GRAPH)
                }
                val homeViewModel: HomeViewModel = hiltViewModel(parentEntry)
                val configViewModel: ConfigViewModel = hiltViewModel(parentEntry)

                HomeScreen(
                    viewModel = homeViewModel,
                    configViewModel = configViewModel,
                    gps = mainViewModel.gps,
                    navigateLocation = {
                        navController.navigate(Screen.Location.route)
                    },
                    navigateVariety = {
                        navController.navigate(Screen.DurianVariety.route)
                    }
                )
            }

            composable(Screen.Config.route) { backStackEntry ->
                val title = stringResource(R.string.configuration)
                LaunchedEffect(backStackEntry) {
                    onTopBarTitleChange(title)
                }
                ConfigScreen(
                    activity = activity,
                    openLocation = {
                        navController.navigate(Screen.Location.route)
                    },
                    openDurianVariety = {
                        navController.navigate(Screen.DurianVariety.route)
                    },
                    onChangePwd = {
                        navController.navigate(Screen.ChangePassword.route)
                    }
                )
            }

//            composable(Screen.Location.route) { backStackEntry ->
//                val title = stringResource(R.string.site_title)
//                LaunchedEffect(backStackEntry) {
//                    onTopBarTitleChange(title)
//                }
//                mainViewModel.gps?.let {
//                    LocationScreen(countryName = mainViewModel.countryInfo.name)
//                }
//            }

            composable(Screen.DurianVariety.route) { backStackEntry ->
                val title = stringResource(R.string.durian_variety)
                LaunchedEffect(backStackEntry) {
                    onTopBarTitleChange(title)
                }
                DurianVarietyScreen(countryCode = mainViewModel.countryInfo.code)
            }

            composable(Screen.ChangePassword.route) {
                ChangePwdScreen(
                    navigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Routes.MAIN_GRAPH) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}


sealed class Screen(val route: String) {
    data object Login : Screen(route = Routes.LOGIN_SCREEN)
    data object Home : Screen(route = Routes.HOME_SCREEN)
    data object Config : Screen(route = Routes.CONFIG_SCREEN)
    data object Location : Screen(route = Routes.LOCATION_SCREEN)
    data object DurianVariety : Screen(route = Routes.DURIAN_VARIETY_SCREEN)

    data object Register : Screen(route = Routes.REGISTER_SCREEN)

    data object ChangePassword : Screen(route = Routes.CHANGE_PASSWORD_SCREEN)
}

object Routes {
    const val AUTH_GRAPH = "auth_graph"
    const val MAIN_GRAPH = "main_graph"
    const val LOGIN_SCREEN = "login"
    const val HOME_SCREEN = "home"
    const val CONFIG_SCREEN = "config"
    const val LOCATION_SCREEN = "location"
    const val DURIAN_VARIETY_SCREEN = "durian_variety"
    const val REGISTER_SCREEN = "register"
    const val CHANGE_PASSWORD_SCREEN = "change_password"
}
