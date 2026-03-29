package com.netsservices.dct.presentation.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.netsservices.dct.presentation.config.ConfigScreen
import com.netsservices.dct.presentation.home.HomeScreen
import com.netsservices.dct.presentation.home.HomeViewModel
import com.netsservices.dct.presentation.location.LocationScreen
import com.netsservices.dct.presentation.login.LoginScreen


@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun MainNavHost(
    navController: NavHostController,
    activity: MainActivity,
    mainViewModel: MainViewModel
) {
    val isLoggedIn by remember { mainViewModel::isLoggedIn }

    val startAuthDestination = if (isLoggedIn) {
        Routes.MAIN_GRAPH
    } else {
        Routes.AUTH_GRAPH
    }

    NavHost(
        navController = navController,
        startDestination = startAuthDestination
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
                    }
                )
            }
        }

        navigation(
            route = Routes.MAIN_GRAPH,
            startDestination = Screen.Home.route
        ) {

            composable(Screen.Home.route) {
                val parentEntry = remember(it) {
                    navController.getBackStackEntry(Routes.MAIN_GRAPH)
                }
                val homeViewModel: HomeViewModel = hiltViewModel(parentEntry)
                HomeScreen(
                    mainViewModel = mainViewModel,
                    viewModel = homeViewModel
                )
            }

            composable(Screen.Config.route) {
                ConfigScreen(activity = activity, onOpenLocation = {
                    navController.navigate(Screen.Location.route)
                })
            }

            composable(Screen.Location.route) {
                LocationScreen()
            }
        }
    }
}


sealed class Screen(val route: String) {
    data object Login : Screen(route = Routes.LOGIN_SCREEN)
    data object Home : Screen(route = Routes.HOME_SCREEN)
    data object Config : Screen(route = Routes.CONFIG_SCREEN)
    data object Location : Screen(route = Routes.LOCATION_SCREEN)
}

object Routes {
    const val AUTH_GRAPH = "auth_graph"
    const val MAIN_GRAPH = "main_graph"
    const val LOGIN_SCREEN = "login"
    const val HOME_SCREEN = "home"
    const val CONFIG_SCREEN = "config"
    const val LOCATION_SCREEN = "location"
}
