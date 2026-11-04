package com.hollow.mount

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hollow.mount.isolation.feature.apps.AppsScreen
import com.hollow.mount.isolation.feature.files.FilesScreen
import com.hollow.mount.isolation.feature.settings.SettingsScreen
import com.hollow.mount.isolation.feature.settings.SettingsViewModel
import com.hollow.mount.isolation.feature.terminal.MountTerminalScreen
import com.hollow.mount.isolation.ui.components.FloatingNavigationBar
import com.hollow.mount.isolation.ui.components.HollowDynamicEmblem
import com.hollow.mount.isolation.ui.components.NavItem
import com.hollow.mount.isolation.ui.navigation.Motion
import com.hollow.mount.isolation.ui.viewmodel.AppViewModelProvider
import com.hollow.mount.ui.theme.IsoVaultTheme
import com.hollow.mount.ui.theme.JakartaFontFamily

enum class NavDestination(val label: String) {
    APPS("Storage Isolate"),
    FILES("File Manager"),
    SETTINGS("Settings"),
    MOUNTS("Mount Terminal")
}

class MainActivity : ComponentActivity() {
    companion object {
        @JvmStatic
        fun isLsposedModuleActiveReal(): Boolean {
            return false // Hooked by StorageIsolationHook on Companion and outer MainActivity
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val settings by viewModel.settings.collectAsState()
            IsoVaultTheme(
                darkTheme = settings.darkTheme,
                dynamicColor = settings.dynamicColor
            ) {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val scheme = MaterialTheme.colorScheme
    var currentScreen by remember { mutableStateOf(NavDestination.APPS) }
    var filesInitialTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val statusMessage by viewModel.statusMessage.collectAsState()
    val settings by viewModel.settings.collectAsState()

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    BackHandler(enabled = currentScreen != NavDestination.APPS) {
        currentScreen = if (currentScreen == NavDestination.MOUNTS) {
            NavDestination.SETTINGS
        } else {
            NavDestination.APPS
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = scheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HollowDynamicEmblem(
                            iconTheme = settings.hollowIconTheme,
                            size = 34.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Hollow Mount",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JakartaFontFamily,
                            color = scheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.setDarkTheme(!settings.darkTheme) }
                    ) {
                        Icon(
                            imageVector = if (settings.darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Dark Theme",
                            tint = scheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = scheme.background,
                    titleContentColor = scheme.onBackground
                )
            )
        },
        bottomBar = {
            val navItems = listOf(
                NavItem(label = "Isolate", icon = Icons.Filled.Shield, testTag = "nav_apps"),
                NavItem(label = "Files", icon = Icons.Filled.Folder, testTag = "nav_files"),
                NavItem(label = "Settings", icon = Icons.Filled.Settings, testTag = "nav_settings")
            )
            val selectedIndex = when (currentScreen) {
                NavDestination.APPS -> 0
                NavDestination.FILES -> 1
                NavDestination.SETTINGS,
                NavDestination.MOUNTS -> 2
            }
            FloatingNavigationBar(
                items = navItems,
                selectedIndex = selectedIndex,
                onSelect = { idx ->
                    when (idx) {
                        0 -> currentScreen = NavDestination.APPS
                        1 -> {
                            filesInitialTab = 0
                            currentScreen = NavDestination.FILES
                        }
                        2 -> currentScreen = NavDestination.SETTINGS
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    Motion.fadeThroughEnter().togetherWith(Motion.fadeThroughExit())
                },
                label = "nav_animated_content"
            ) { screen ->
                when (screen) {
                    NavDestination.APPS -> AppsScreen(
                        onBack = null
                    )
                    NavDestination.FILES -> FilesScreen(
                        initialTab = filesInitialTab,
                        onNavigateToApps = { currentScreen = NavDestination.APPS }
                    )
                    NavDestination.MOUNTS -> MountTerminalScreen(viewModel = viewModel)
                    NavDestination.SETTINGS -> SettingsScreen(
                        viewModel = viewModel,
                        onOpenDiagnostics = { currentScreen = NavDestination.MOUNTS }
                    )
                }
            }
        }
    }
}
