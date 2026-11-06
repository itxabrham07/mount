package com.hollow.mount.isolation.feature.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hollow.mount.isolation.model.HollowIconTheme
import com.hollow.mount.isolation.model.PickerPolicy
import com.hollow.mount.isolation.ui.components.GroupedList
import com.hollow.mount.isolation.ui.components.HollowDynamicEmblem
import com.hollow.mount.isolation.ui.components.SectionRow
import com.hollow.mount.isolation.ui.viewmodel.AppViewModelProvider
import com.hollow.mount.ui.theme.JakartaFontFamily

private enum class SettingsSection(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
) {
    STORAGE_ENGINE(
        title = "Storage Isolation Engine",
        subtitle = "Private root volumes, AES-256 rest encryption & picker policy",
        icon = Icons.Default.Storage
    ),
    APPEARANCE(
        title = "Appearance & Theme",
        subtitle = "Dark theme, dynamic wallpaper colors & launcher icons",
        icon = Icons.Default.Palette
    ),
    SYSTEM_DIAGNOSTICS(
        title = "System & Diagnostics",
        subtitle = "Root status verification, LSPosed guides & mount telemetry",
        icon = Icons.Default.Tune
    )
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onOpenDiagnostics: () -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    val moduleLoadedInManagerProcess by viewModel.moduleLoadedInManagerProcess.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val rootStatus by viewModel.rootStatus.collectAsState()

    var activeSubSection by remember { mutableStateOf<SettingsSection?>(null) }
    var showSetupGuideDialog by remember { mutableStateOf(false) }

    // Sub-section back navigation
    BackHandler(enabled = activeSubSection != null) {
        activeSubSection = null
    }

    if (activeSubSection != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("settings_sub_screen")
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                IconButton(
                    onClick = { activeSubSection = null },
                    modifier = Modifier.size(38.dp).testTag("btn_settings_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = scheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activeSubSection?.title ?: "Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        fontFamily = JakartaFontFamily,
                        color = scheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = activeSubSection?.subtitle ?: "",
                        fontSize = 11.sp,
                        color = scheme.onSurfaceVariant,
                        fontFamily = JakartaFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                when (activeSubSection) {
                    SettingsSection.STORAGE_ENGINE -> {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = scheme.surface),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, scheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Root Storage Sandboxing",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        fontFamily = JakartaFontFamily,
                                        color = scheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    ToggleRow(
                                        title = "Private volumes (Root)",
                                        description = "Mount a dedicated storage sandbox per app when rooted",
                                        checked = settings.rootVolumesEnabled,
                                        enabled = true,
                                        onChange = { viewModel.setRootVolumesEnabled(it) }
                                    )
                                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.5f))
                                    ToggleRow(
                                        title = "Lock volumes when unmounted",
                                        description = "Automatically encrypt app volumes with AES-256 when closed",
                                        checked = settings.encryptVolumesAtRest,
                                        enabled = settings.rootVolumesEnabled,
                                        onChange = { viewModel.setEncryptVolumesAtRest(it) }
                                    )
                                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.5f))
                                    ToggleRow(
                                        title = "Block system photo picker",
                                        description = "Require isolated apps to use only their own isolated gallery",
                                        checked = settings.pickerPolicy == PickerPolicy.BLOCK,
                                        enabled = true,
                                        onChange = { blocked ->
                                            viewModel.setPickerPolicy(
                                                if (blocked) PickerPolicy.BLOCK else PickerPolicy.ALLOW
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    SettingsSection.APPEARANCE -> {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = scheme.surface),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, scheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Theme & Dynamics",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        fontFamily = JakartaFontFamily,
                                        color = scheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    ToggleRow(
                                        title = "Dark theme",
                                        description = "Deep dark AMOLED aesthetic",
                                        checked = settings.darkTheme,
                                        enabled = true,
                                        onChange = { viewModel.setDarkTheme(it) }
                                    )
                                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.5f))
                                    ToggleRow(
                                        title = "Dynamic wallpaper colors (Material You)",
                                        description = "Harmonize with system wallpaper on Android 12+",
                                        checked = settings.dynamicColor,
                                        enabled = true,
                                        onChange = { viewModel.setDynamicColor(it) }
                                    )
                                }
                            }
                        }

                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = scheme.surface),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, scheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Launcher Icon Preset",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = scheme.onSurface,
                                        fontFamily = JakartaFontFamily
                                    )
                                    Text(
                                        text = "Select custom launcher emblem style",
                                        fontSize = 11.sp,
                                        color = scheme.onSurfaceVariant,
                                        fontFamily = JakartaFontFamily
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        HollowIconTheme.entries.forEach { themeOption ->
                                            val isSelected = settings.hollowIconTheme == themeOption
                                            Card(
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSelected) {
                                                        scheme.primaryContainer.copy(alpha = 0.45f)
                                                    } else {
                                                        scheme.surfaceContainerLow
                                                    }
                                                ),
                                                shape = RoundedCornerShape(14.dp),
                                                border = BorderStroke(
                                                    width = if (isSelected) 1.5.dp else 1.dp,
                                                    color = if (isSelected) scheme.primary else scheme.outlineVariant
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { viewModel.setHollowIconTheme(themeOption) }
                                                    .testTag("icon_theme_${themeOption.name.lowercase()}")
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 12.dp, horizontal = 4.dp)
                                                ) {
                                                    HollowDynamicEmblem(
                                                        iconTheme = themeOption,
                                                        size = 36.dp
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(
                                                        text = themeOption.label,
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) scheme.primary else scheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        fontFamily = JakartaFontFamily
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    SettingsSection.SYSTEM_DIAGNOSTICS -> {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = scheme.surface),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, scheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(scheme.secondaryContainer)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.HelpOutline,
                                                    contentDescription = null,
                                                    tint = scheme.onSecondaryContainer,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "LSPosed Hook Activation",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = scheme.onSurface,
                                                    fontFamily = JakartaFontFamily
                                                )
                                                Text(
                                                    text = if (moduleLoadedInManagerProcess) "Module loaded in manager process; target hooks unverified" else "Not observed in manager process; target hooks unknown",
                                                    fontSize = 11.sp,
                                                    color = if (moduleLoadedInManagerProcess) scheme.primary else scheme.onSurfaceVariant,
                                                    fontFamily = JakartaFontFamily
                                                )
                                            }
                                        }
                                        OutlinedButton(
                                            onClick = { showSetupGuideDialog = true },
                                            shape = RoundedCornerShape(50),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("btn_open_setup_guide")
                                        ) {
                                            Text(
                                                text = "Guide",
                                                fontSize = 12.sp,
                                                fontFamily = JakartaFontFamily,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Superuser (Root) Check",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = scheme.onSurface,
                                                fontFamily = JakartaFontFamily
                                            )
                                            Text(
                                                text = rootStatus,
                                                fontSize = 11.sp,
                                                color = scheme.onSurfaceVariant,
                                                fontFamily = JakartaFontFamily
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { viewModel.checkRoot() },
                                            shape = RoundedCornerShape(50),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "Check",
                                                fontSize = 12.sp,
                                                fontFamily = JakartaFontFamily,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onOpenDiagnostics() }
                                            .padding(vertical = 4.dp)
                                            .testTag("nav_mounts")
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Tune,
                                                contentDescription = null,
                                                tint = scheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "Mount Terminal & Live Telemetry",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = scheme.onSurface,
                                                fontFamily = JakartaFontFamily
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = scheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    null -> {}
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("settings_screen"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    fontFamily = JakartaFontFamily,
                    color = scheme.onBackground
                )
                Text(
                    text = "Configure Hollow Mount storage isolation & appearance",
                    fontSize = 12.sp,
                    color = scheme.onSurfaceVariant,
                    fontFamily = JakartaFontFamily
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            item {
                GroupedList(horizontalPadding = 0.dp) {
                    SettingsSection.entries.forEach { section ->
                        val dynamicStatus = when (section) {
                            SettingsSection.STORAGE_ENGINE -> {
                                if (settings.rootVolumesEnabled) "Private Volumes Active • AES-256"
                                else "Standard Sandbox Engine"
                            }
                            SettingsSection.APPEARANCE -> {
                                val themeMode = if (settings.darkTheme) "Dark" else "Light"
                                val dyn = if (settings.dynamicColor) "Material You" else "Default"
                                "$themeMode • $dyn"
                            }
                            SettingsSection.SYSTEM_DIAGNOSTICS -> {
                                "Root: $rootStatus • Module in manager: ${if (moduleLoadedInManagerProcess) "Loaded" else "Not observed"}"
                            }
                        }
                        add {
                            SectionRow(
                                icon = section.icon,
                                title = section.title,
                                subtitle = "$dynamicStatus • ${section.subtitle}",
                                onClick = { activeSubSection = section },
                                testTag = "settings_card_${section.name.lowercase()}"
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showSetupGuideDialog) {
        AlertDialog(
            onDismissRequest = { showSetupGuideDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = scheme.surface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(scheme.primaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = scheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Hollow Mount Setup Guide",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        fontFamily = JakartaFontFamily,
                        color = scheme.onSurface
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SetupStep(
                        step = "1",
                        title = "Enable Hollow Mount in LSPosed / Vector",
                        desc = "Open your Xposed manager, enable the Hollow Mount module, and select target apps in its scope."
                    )
                    SetupStep(
                        step = "2",
                        title = "Add apps in Storage Isolate",
                        desc = "Open 'Storage Isolate', tap '+ Add Apps', and switch on any app you want isolated."
                    )
                    SetupStep(
                        step = "3",
                        title = "Force-stop & reopen the app",
                        desc = "The app will now run inside Hollow Drive or its own Separate App Drive."
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSetupGuideDialog = false },
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Got it", fontFamily = JakartaFontFamily, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSetupGuideDialog = false }) {
                    Text("Close", fontFamily = JakartaFontFamily)
                }
            }
        )
    }
}

@Composable
fun ToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean,
    onChange: (Boolean) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) scheme.onSurface else scheme.onSurfaceVariant,
                fontFamily = JakartaFontFamily
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = JakartaFontFamily
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            enabled = enabled
        )
    }
}

@Composable
fun SetupStep(step: String, title: String, desc: String) {
    val scheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(scheme.primaryContainer)
        ) {
            Text(
                text = step,
                color = scheme.onPrimaryContainer,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JakartaFontFamily
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = scheme.onSurface,
                fontFamily = JakartaFontFamily
            )
            Text(
                text = desc,
                fontSize = 12.sp,
                color = scheme.onSurfaceVariant,
                lineHeight = 16.sp,
                fontFamily = JakartaFontFamily
            )
        }
    }
}
