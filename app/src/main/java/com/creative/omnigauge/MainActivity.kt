package com.creative.omnigauge

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.creative.feature_battery.presentation.ui.chart.BatteryChartScreen
import com.creative.feature_battery.presentation.ui.chart.BatteryChartViewModel
import com.creative.feature_battery.presentation.ui.chart.BatteryLongTermScreen
import com.creative.feature_battery.presentation.ui.debug.BatteryDebugScreen
import com.creative.feature_battery.presentation.ui.BatterySettingsScreen
import com.creative.feature_battery.presentation.ui.AppDischargeScreen
import com.creative.feature_battery.service.BatteryMonitoringService
import com.creative.feature_network.presentation.ui.NetworkScreen
import android.content.pm.ApplicationInfo
import androidx.compose.ui.platform.LocalContext
import com.creative.omnigauge.ui.AdBanner
import com.creative.feature_battery.data.history.BatteryHistoryDatabase
import com.creative.feature_battery.data.history.DataMigrationManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private val migrationManager: DataMigrationManager by inject()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val notificationsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.POST_NOTIFICATIONS] ?: true
        } else {
            true
        }
        if (notificationsGranted) {
            checkMigrationAndStartService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Check if DB version changed and we need to ask the user
        if (migrationManager.checkMigrationNeeded("battery_history.db", BatteryHistoryDatabase.VERSION)) {
            migrationManager.setMigrationChoicePending("battery_history.db", true)
        }

        val permissionsToRequest = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.NEARBY_WIFI_DEVICES)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            checkMigrationAndStartService()
        }

        setContent {
            val darkTheme = isSystemInDarkTheme()
            val colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()
            
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                }
            }

            MaterialTheme(colorScheme = colorScheme) {
                MainContent()
            }
        }
    }

    private fun checkMigrationAndStartService() {
        if (!migrationManager.isMigrationChoicePending("battery_history.db")) {
            startBatteryService()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainContent() {
        val context = LocalContext.current
        val isDebug = remember { (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0 }
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        var showMigrationDialog by remember { 
            mutableStateOf(migrationManager.isMigrationChoicePending("battery_history.db")) 
        }

        if (showMigrationDialog) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Database Update Required") },
                text = { Text("We have updated our internal data models. Would you like to keep your existing battery history data and attempt an automatic update, or start with a fresh database?") },
                confirmButton = {
                    Button(onClick = {
                        migrationManager.markMigrationHandled("battery_history.db")
                        showMigrationDialog = false
                        startBatteryService()
                    }) {
                        Text("Keep & Update")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        migrationManager.requestWipe("battery_history.db")
                        migrationManager.markMigrationHandled("battery_history.db")
                        showMigrationDialog = false
                        startBatteryService()
                    }) {
                        Text("Start Fresh")
                    }
                }
            )
        }
        
        val title = remember(currentDestination) {
            when (currentDestination?.route) {
                "battery_metrics" -> "Battery Metrics"
                "battery_trends" -> "Battery Trends"
                "battery_long_term" -> "Long-term Trends"
                "app_discharge" -> "App Power Impact"
                "battery_debug" -> "Debug Data"
                "network" -> "Network Diagnostics"
                "settings" -> "Battery Settings"
                else -> "OmniGauge"
            }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Spacer(modifier = Modifier.height(12.dp))
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.BatteryFull, contentDescription = null) },
                        label = { Text("Battery Metrics") },
                        selected = currentDestination?.hierarchy?.any { it.route == "battery_metrics" } == true,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("battery_metrics") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Timeline, contentDescription = null) },
                        label = { Text("Battery Trends") },
                        selected = currentDestination?.hierarchy?.any { it.route == "battery_trends" } == true,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("battery_trends") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.History, contentDescription = null) },
                        label = { Text("Long-term Trends") },
                        selected = currentDestination?.hierarchy?.any { it.route == "battery_long_term" } == true,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("battery_long_term") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Bolt, contentDescription = null) },
                        label = { Text("App Power Impact") },
                        selected = currentDestination?.hierarchy?.any { it.route == "app_discharge" } == true,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("app_discharge") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    if (isDebug) {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.BugReport, contentDescription = null) },
                            label = { Text("Debug Data") },
                            selected = currentDestination?.hierarchy?.any { it.route == "battery_debug" } == true,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("battery_debug") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.NetworkCheck, contentDescription = null) },
                        label = { Text("Network Diagnostics") },
                        selected = currentDestination?.hierarchy?.any { it.route == "network" } == true,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("network") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(title) },
                        navigationIcon = {
                            if (currentDestination?.route == "settings") {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            } else {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                            }
                        },
                        actions = {
                            if (currentDestination?.route != "settings") {
                                IconButton(onClick = {
                                    navController.navigate("settings") {
                                        launchSingleTop = true
                                    }
                                }) {
                                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        )
                    )
                },
                bottomBar = {
                    AdBanner()
                }
            ) { innerPadding ->
                Surface(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(8.dp)
                        .fillMaxSize()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "battery_metrics",
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        composable("battery_metrics") {
                            val viewModel: BatteryChartViewModel = koinViewModel()
                            BatteryChartScreen(showTrends = false, viewModel = viewModel)
                        }
                        composable("battery_trends") {
                            val viewModel: BatteryChartViewModel = koinViewModel()
                            BatteryChartScreen(showTrends = true, viewModel = viewModel)
                        }
                        composable("battery_long_term") {
                            BatteryLongTermScreen()
                        }
                        composable("app_discharge") {
                            AppDischargeScreen()
                        }
                        composable("network") {
                            NetworkScreen()
                        }
                        if (isDebug) {
                            composable("battery_debug") {
                                BatteryDebugScreen()
                            }
                        }
                        composable("settings") {
                            BatterySettingsScreen()
                        }
                    }
                }
            }
        }
    }

    private fun startBatteryService() {
        val intent = Intent(this, BatteryMonitoringService::class.java)
        startForegroundService(intent)
    }
}
