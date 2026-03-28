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


@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun MainNavHost(
    navController: NavHostController,
    activity: MainActivity,
    mainViewModel: MainViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Routes.MAIN_GRAPH
    ) {

        navigation(
            route = Routes.MAIN_GRAPH,
            startDestination = Screen.HomeScreen.route
        ) {

            composable(Screen.HomeScreen.route) {
                val parentEntry = remember(it) {
                    navController.getBackStackEntry(Routes.MAIN_GRAPH)
                }
                val homeViewModel: HomeViewModel = hiltViewModel(parentEntry)
                HomeScreen(viewModel = homeViewModel)
            }
        }

        composable(Screen.ConfigScreen.route) {
            ConfigScreen(activity = activity)
        }
    }
}


sealed class Screen(val route: String) {
    data object HomeScreen : Screen(route = Routes.HOME_SCREEN)
    data object ConfigScreen : Screen(route = Routes.CONFIG_SCREEN)
}

object Routes {
    const val MAIN_GRAPH = "main_graph"
    const val HOME_SCREEN = "home"
    const val CONFIG_SCREEN = "config"
}
