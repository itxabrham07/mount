package com.hollow.mount.isolation.data.repository

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.hollow.mount.MainActivity
import com.hollow.mount.isolation.model.AppSettings
import com.hollow.mount.isolation.model.HollowIconTheme
import com.hollow.mount.isolation.model.IsolationRuleManager
import com.hollow.mount.isolation.model.SettingsStore
import com.hollow.mount.isolation.mount.MountEntry
import com.hollow.mount.isolation.mount.MountManager
import com.hollow.mount.isolation.util.runHollowCatching
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Shared reactive repository for application settings, mount diagnostics, root status,
 * status messages, and terminal logs across ViewModels.
 */
class SettingsRepository(
    private val context: Context,
    private val appScope: CoroutineScope
) {
    private val _settings = MutableStateFlow(SettingsStore.load(context))
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _terminalLogs = MutableStateFlow<List<String>>(emptyList())
    val terminalLogs: StateFlow<List<String>> = _terminalLogs.asStateFlow()

    private val _systemMounts = MutableStateFlow<List<MountEntry>>(emptyList())
    val systemMounts: StateFlow<List<MountEntry>> = _systemMounts.asStateFlow()

    private val _selinuxStatus = MutableStateFlow("Unknown")
    val selinuxStatus: StateFlow<String> = _selinuxStatus.asStateFlow()

    private val _hookStatus = MutableStateFlow(com.hollow.mount.isolation.hook.IsoVaultHookBridge.checkStatus(context))
    val hookStatus: StateFlow<com.hollow.mount.isolation.hook.IsoVaultHookBridge.FrameworkStatus> = _hookStatus.asStateFlow()

    private val _isLsposedActive = MutableStateFlow(
        com.hollow.mount.isolation.hook.IsoVaultHookBridge.isHookActive() ||
            MainActivity.isLsposedModuleActiveReal() ||
            IsolationRuleManager.isXSharedPrefsHooked(context)
    )
    val isLsposedActive: StateFlow<Boolean> = _isLsposedActive.asStateFlow()

    private val _rootStatus = MutableStateFlow("Not checked")
    val rootStatus: StateFlow<String> = _rootStatus.asStateFlow()

    init {
        refreshDiagnostics()
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun postStatusMessage(msg: String) {
        _statusMessage.value = msg
    }

    fun appendTerminalLog(log: String) {
        val stamp = android.text.format.DateFormat.format("HH:mm:ss", System.currentTimeMillis())
        _terminalLogs.update { (it + "[$stamp] $log").takeLast(150) }
    }

    fun updateSettings(transform: (AppSettings) -> AppSettings): AppSettings {
        val next = transform(_settings.value)
        _settings.value = next
        appScope.launch(Dispatchers.IO) {
            runHollowCatching("SettingsRepository") {
                SettingsStore.save(context, next)
            }
        }
        return next
    }

    fun refreshDiagnostics() {
        appScope.launch(Dispatchers.IO) {
            runHollowCatching("SettingsRepository") {
                val status = com.hollow.mount.isolation.hook.IsoVaultHookBridge.checkStatus(context)
                _hookStatus.value = status
                _systemMounts.value = MountManager.readSystemMounts()
                _selinuxStatus.value = MountManager.getSelinuxStatus()
                _isLsposedActive.value = status.isHookActive
            }
        }
    }

    suspend fun checkRoot(): Boolean = withContext(Dispatchers.IO) {
        _rootStatus.value = "Checking..."
        val diag = runHollowCatching("SettingsRepository") {
            MountManager.getDetailedRootDiagnostics()
        }.getOrNull()
        val granted = diag?.isRootAvailable ?: false
        _rootStatus.value = if (granted) "Root UID 0 verified (${diag?.suVersion})" else "No root access"
        if (diag != null) {
            appendTerminalLog("Root Diagnostic:\n${diag.details}")
        }
        granted
    }

    fun applyLauncherIconTheme(iconTheme: HollowIconTheme = _settings.value.hollowIconTheme) {
        runHollowCatching("SettingsRepository") {
            val pm = context.packageManager
            val mainComp = ComponentName(context, "com.hollow.mount.MainActivity")
            val cyberAlias = ComponentName(context, "com.hollow.mount.MainActivityCyberAlias")
            val crimsonAlias = ComponentName(context, "com.hollow.mount.MainActivityCrimsonAlias")
            val minimalAlias = ComponentName(context, "com.hollow.mount.MainActivityMinimalAlias")
            val allThemeAliases = listOf(cyberAlias, crimsonAlias, minimalAlias)

            val targetAliasClass = iconTheme.aliasClassName
            if (targetAliasClass == null) {
                pm.setComponentEnabledSetting(
                    mainComp,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
                allThemeAliases.forEach {
                    pm.setComponentEnabledSetting(
                        it,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
            } else {
                pm.setComponentEnabledSetting(
                    mainComp,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                allThemeAliases.forEach { alias ->
                    val state = if (alias.className == targetAliasClass) {
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    } else {
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    }
                    pm.setComponentEnabledSetting(alias, state, PackageManager.DONT_KILL_APP)
                }
            }
        }
    }
}
