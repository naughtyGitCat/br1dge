package com.example.notifybridge.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import uk.deprecated.notifybridge.R
import com.example.notifybridge.core.ui.NotifyBridgeTheme
import com.example.notifybridge.feature.dashboard.DashboardScreenRoute
import com.example.notifybridge.feature.detail.LogDetailScreenRoute
import com.example.notifybridge.feature.logs.LogsScreenRoute
import com.example.notifybridge.feature.privacy.PrivacyDisclosureScreen
import com.example.notifybridge.feature.privacy.PrivacyPolicyScreenRoute
import com.example.notifybridge.feature.privacy.PrivacyViewModel
import com.example.notifybridge.feature.settings.SettingsScreenRoute

private sealed class TopLevelDestination(
    val route: String,
    val labelRes: Int,
    val icon: @Composable () -> Unit,
) {
    data object Dashboard : TopLevelDestination("dashboard", R.string.nav_dashboard, { Icon(Icons.Outlined.Dashboard, null) })
    data object Logs : TopLevelDestination("logs", R.string.nav_logs, { Icon(Icons.AutoMirrored.Outlined.List, null) })
    data object Settings : TopLevelDestination("settings", R.string.nav_settings, { Icon(Icons.Outlined.Settings, null) })
}

@Composable
fun NotifyBridgeRoot(
    onRequestNotificationPermission: () -> Unit = {},
) {
    NotifyBridgeTheme {
        val privacyViewModel: PrivacyViewModel = hiltViewModel()
        val privacyState by privacyViewModel.uiState.collectAsStateWithLifecycle()
        if (!privacyState.prominentDisclosureAccepted) {
            var showPolicy by rememberSaveable { mutableStateOf(false) }
            if (showPolicy) {
                PrivacyPolicyScreenRoute(
                    contentPadding = PaddingValues(),
                    onBack = { showPolicy = false },
                )
            } else {
                PrivacyDisclosureScreen(
                    onAccept = {
                        privacyViewModel.acceptDisclosure()
                        onRequestNotificationPermission()
                    },
                    onOpenPrivacyPolicy = { showPolicy = true },
                )
            }
        } else {
            val navController = rememberNavController()
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    BottomBar(navController)
                }
            ) { padding ->
                NotifyBridgeNavHost(navController = navController, padding = padding)
            }
        }
    }
}

@Composable
private fun BottomBar(navController: NavHostController) {
    val destinations = listOf(
        TopLevelDestination.Dashboard,
        TopLevelDestination.Logs,
        TopLevelDestination.Settings,
    )
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    NavigationBar {
        destinations.forEach { destination ->
            NavigationBarItem(
                selected = route == destination.route,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = destination.icon,
                label = { Text(stringResource(destination.labelRes)) }
            )
        }
    }
}

@Composable
private fun NotifyBridgeNavHost(
    navController: NavHostController,
    padding: PaddingValues,
) {
    NavHost(
        navController = navController,
        startDestination = TopLevelDestination.Dashboard.route,
    ) {
        composable(TopLevelDestination.Dashboard.route) {
            DashboardScreenRoute(contentPadding = padding, onNavigateToSettings = {
                navController.navigate(TopLevelDestination.Settings.route)
            })
        }
        composable(TopLevelDestination.Logs.route) {
            LogsScreenRoute(contentPadding = padding, onOpenDetail = { id ->
                navController.navigate("logDetail/$id")
            })
        }
        composable(TopLevelDestination.Settings.route) {
            SettingsScreenRoute(
                contentPadding = padding,
                onOpenPrivacyPolicy = { navController.navigate("privacyPolicy") },
            )
        }
        composable("logDetail/{eventId}") { backStackEntry ->
            LogDetailScreenRoute(
                contentPadding = padding,
                eventId = backStackEntry.arguments?.getString("eventId").orEmpty(),
                onBack = { navController.popBackStack() }
            )
        }
        composable("privacyPolicy") {
            PrivacyPolicyScreenRoute(
                contentPadding = padding,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
