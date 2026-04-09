package com.netsservices.dct.presentation.main

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.netsservices.dct.R
import com.netsservices.dct.data.remote.AppEvent
import com.netsservices.dct.data.remote.AppEventBus
import com.netsservices.dct.data.remote.utils.PreferenceManager
import com.netsservices.dct.i18n.LangKey
import com.netsservices.dct.presentation.common.Constants
import com.netsservices.dct.presentation.common.LanguagePrefs
import com.netsservices.dct.presentation.common.getText
import com.netsservices.dct.presentation.components.TopBar
import com.netsservices.dct.presentation.helper.PermissionManager
import com.netsservices.dct.presentation.theme.DurianCameraTrainingTheme
import com.netsservices.dct.presentation.utils.Utils.setAppLocale
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val permissionManager = PermissionManager(this)
    lateinit var navController: NavHostController
    lateinit var snackBarHostState: SnackbarHostState

    lateinit var mapLang: Map<String, String>

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onStart() {
        super.onStart()
        permissionManager.requestAll {
            viewModel.getCoordinate()
            viewModel.isAllGranted = true
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isLoggedIn = runBlocking {
            PreferenceManager.getAuthToken(this@MainActivity).isNotEmpty()
        }
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val lang by LanguagePrefs.getLanguageId(context).collectAsState(initial = "en")
            setAppLocale(this, lang)

            navController = rememberNavController()
            snackBarHostState = remember { SnackbarHostState() }
            mapLang = viewModel.mapLang.collectAsState().value

            val navBackStackEntry by navController.currentBackStackEntryFlow
                .collectAsState(initial = navController.currentBackStackEntry)
            val currentRoute = navBackStackEntry?.destination?.route
            val showTopBar = currentRoute == Screen.Home.route ||
                        currentRoute == Screen.Config.route ||
                        currentRoute == Screen.Location.route ||
                        currentRoute == Screen.DurianVariety.route
            val startDestination = if (isLoggedIn) Routes.MAIN_GRAPH else Routes.AUTH_GRAPH
            val topBarTitle = remember { mutableStateOf("") }

            DurianCameraTrainingTheme {
                Scaffold(
                    topBar = {
                        if (showTopBar) {
                            TopBar(
                                title = topBarTitle.value.ifEmpty { stringResource(R.string.app_name) },
                                navigationIcon = if (
                                    currentRoute == Screen.Config.route ||
                                    currentRoute == Screen.Location.route ||
                                    currentRoute == Screen.DurianVariety.route
                                ) {
                                    {
                                        IconButton(onClick = { navController.navigateUp() }) {
                                            Icon(
                                                Icons.Default.ArrowBackIosNew,
                                                contentDescription = "Back",
                                                tint = colorResource(R.color.black)
                                            )
                                        }
                                    }
                                } else null,
                                actions = if (currentRoute == Screen.Home.route) {
                                    {
                                        IconButton(onClick = { navController.navigate(Screen.Config.route) }) {
                                            Icon(
                                                Icons.Default.Settings,
                                                contentDescription = "Settings",
                                                modifier = Modifier.size(28.dp),
                                                tint = colorResource(R.color.black)
                                            )
                                        }
                                    }
                                } else null
                            )
                        }
                    },
                    snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
                    content = { padding ->
                        Box(modifier = Modifier.padding(padding)) {
                            MainNavHost(
                                navController = navController,
                                activity = this@MainActivity,
                                mainViewModel = viewModel,
                                startDestination = startDestination,
                                onTopBarTitleChange = { title -> topBarTitle.value = title }
                            )
                            DoubleBackPressHandler(navController, mapLang)
                        }
                    }
                )
            }
        }
        observeAppEvents(viewModel)
    }

    private fun observeAppEvents(viewModel: MainViewModel) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                AppEventBus.events.collect { event ->
                    when (event) {
                        is AppEvent.ShowToast -> {
                            snackBarHostState.showSnackbar(event.message)
                        }
                        is AppEvent.Unauthorized -> {
                            if (snackBarHostState.currentSnackbarData != null) return@collect
                            val currentRoute = navController.currentBackStackEntry?.destination?.route
                            if(currentRoute == Screen.Login.route) {
                                snackBarHostState.showSnackbar(event.message)
                            } else if (currentRoute != Screen.Login.route) {
                                if(event.message == Constants.INVALID_TOKEN) {
                                    snackBarHostState.showSnackbar(message = mapLang[LangKey.Message.SessionExpired]?:"")
                                    viewModel.clearData()
                                } else {
                                    snackBarHostState.showSnackbar(event.message)
                                }
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==== BACK PRESS HANDLER ====
@Composable
private fun DoubleBackPressHandler(navController: NavHostController, mapLang: Map<String, String>) {
    var showToast by remember { mutableStateOf(false) }
    var backPressState by remember { mutableStateOf<BackPress>(BackPress.Idle) }
    val context = LocalContext.current
    val currentScreen = navController.currentBackStackEntry?.destination?.route

    when (currentScreen) {
        Screen.Home.route -> {
            if (showToast) {
                Toast.makeText(
                    context,
                    mapLang.getText(LangKey.Message.PressBackAgainToExit, R.string.press_back_again_to_exit),
                    Toast.LENGTH_SHORT
                ).show()
            }

            LaunchedEffect(key1 = backPressState) {
                if (backPressState == BackPress.InitialTouch) {
                    delay(2000)
                    backPressState = BackPress.Idle
                }
            }

            BackHandler(backPressState == BackPress.Idle) {
                backPressState = BackPress.InitialTouch
                showToast = true
            }
        }
    }
}

sealed class BackPress {
    data object Idle : BackPress()
    data object InitialTouch : BackPress()
}